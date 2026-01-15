package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.function.BiFunction;

/**
 * Unified broadcasting dispatcher for binary operations.
 * <p>
 * Implements NumPy/Matlab-style broadcasting with enhanced flexibility:
 * <ul>
 *     <li>Scalar operations broadcast to all elements</li>
 *     <li>Vector operations handle size mismatches intelligently</li>
 *     <li>Matrix operations support element-wise and true multiplication</li>
 *     <li>Nested structures are handled recursively</li>
 *     <li>Type preservation where possible (rational, percent, etc.)</li>
 * </ul>
 *
 * <h2>Broadcasting Rules:</h2>
 * <pre>
 * Scalar  op Scalar  → Scalar
 * Vector  op Scalar  → Vector (broadcast scalar to all elements)
 * Scalar  op Vector  → Vector (broadcast scalar to all elements)
 * Vector  op Vector  → Vector (element-wise, broadcasting if sizes differ)
 * Matrix  op Scalar  → Matrix (broadcast scalar to all elements)
 * Scalar  op Matrix  → Matrix (broadcast scalar to all elements)
 * Matrix  op Matrix  → Matrix (element-wise)
 * Matrix  op Vector  → Matrix (broadcast vector to rows/columns)
 * Vector  op Matrix  → Matrix (broadcast vector to rows/columns)
 * </pre>
 */
public final class BroadcastingDispatcher {

    private BroadcastingDispatcher() {
    }

    /**
     * Dispatches a binary operation with full broadcasting support.
     *
     * @param left     left operand
     * @param right    right operand
     * @param ctx      operator context
     * @param scalarOp the operation to apply to scalar pairs
     * @return result with appropriate type and structure
     */
    public static NodeConstant dispatch(
            NodeConstant left,
            NodeConstant right,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        // ==================== Matrix Operations ====================

        // Matrix op Matrix - element-wise
        if (left instanceof NodeMatrix leftMat && right instanceof NodeMatrix rightMat) {
            return matrixOpMatrix(leftMat, rightMat, ctx, scalarOp);
        }

        // Matrix op Scalar - broadcast scalar to all matrix elements
        if (left instanceof NodeMatrix leftMat && isScalar(right)) {
            return matrixOpScalar(leftMat, right, ctx, scalarOp);
        }

        // Scalar op Matrix - broadcast scalar to all matrix elements
        if (isScalar(left) && right instanceof NodeMatrix rightMat) {
            return scalarOpMatrix(left, rightMat, ctx, scalarOp);
        }

        // Matrix op Vector - broadcast vector appropriately
        if (left instanceof NodeMatrix leftMat && right instanceof NodeVector rightVec) {
            return matrixOpVector(leftMat, rightVec, ctx, scalarOp);
        }

        // Vector op Matrix - broadcast vector appropriately
        if (left instanceof NodeVector leftVec && right instanceof NodeMatrix rightMat) {
            return vectorOpMatrix(leftVec, rightMat, ctx, scalarOp);
        }

        // ==================== Vector Operations ====================

        // Vector op Vector - element-wise with broadcasting
        if (left instanceof NodeVector leftVec && right instanceof NodeVector rightVec) {
            return vectorOpVector(leftVec, rightVec, ctx, scalarOp);
        }

        // Vector op Scalar - broadcast scalar to all vector elements
        if (left instanceof NodeVector leftVec && isScalar(right)) {
            return vectorOpScalar(leftVec, right, ctx, scalarOp);
        }

        // Scalar op Vector - broadcast scalar to all vector elements
        if (isScalar(left) && right instanceof NodeVector rightVec) {
            return scalarOpVector(left, rightVec, ctx, scalarOp);
        }

        // ==================== Scalar Operations ====================

        // Scalar op Scalar - direct application
        if (isScalar(left) && isScalar(right)) {
            return scalarOp.apply(left, right);
        }

        // Unsupported combination
        throw new TypeError("Unsupported operand types: " +
                left.getClass().getSimpleName() + " and " +
                right.getClass().getSimpleName());
    }

    // ==================== Matrix Implementations ====================

    private static NodeMatrix matrixOpMatrix(
            NodeMatrix left,
            NodeMatrix right,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        // If dimensions match, perform element-wise operation
        if (left.getRows() == right.getRows() && left.getCols() == right.getCols()) {
            int rows = left.getRows();
            int cols = left.getCols();
            Node[][] result = new Node[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    NodeConstant leftElem = (NodeConstant) left.getElement(i, j);
                    NodeConstant rightElem = (NodeConstant) right.getElement(i, j);
                    result[i][j] = dispatch(leftElem, rightElem, ctx, scalarOp);
                }
            }

            return new NodeMatrix(result);
        }

        // Try broadcasting: if one matrix has a single row/column, broadcast it
        if (left.getRows() == 1 && left.getCols() == right.getCols()) {
            // Broadcast left row to all rows of right
            return broadcastRowToMatrix(left, right, ctx, scalarOp, false);
        }

        if (right.getRows() == 1 && right.getCols() == left.getCols()) {
            // Broadcast right row to all rows of left
            return broadcastRowToMatrix(right, left, ctx, scalarOp, true);
        }

        if (left.getCols() == 1 && left.getRows() == right.getRows()) {
            // Broadcast left column to all columns of right
            return broadcastColumnToMatrix(left, right, ctx, scalarOp, false);
        }

        if (right.getCols() == 1 && right.getRows() == left.getRows()) {
            // Broadcast right column to all columns of left
            return broadcastColumnToMatrix(right, left, ctx, scalarOp, true);
        }

        // Size mismatch: normalize by padding with zeros (for arithmetic operations)
        // This allows [[1,2],[3,4]] + [[1,2,3]] = [[1,2,0],[3,4,0]] + [[1,2,3],[0,0,0]] = [[2,4,3],[3,4,3]]
        int maxRows = Math.max(left.getRows(), right.getRows());
        int maxCols = Math.max(left.getCols(), right.getCols());
        NodeMatrix leftNorm = normalizeMatrix(left, maxRows, maxCols);
        NodeMatrix rightNorm = normalizeMatrix(right, maxRows, maxCols);

        Node[][] result = new Node[maxRows][maxCols];
        for (int i = 0; i < maxRows; i++) {
            for (int j = 0; j < maxCols; j++) {
                NodeConstant leftElem = (NodeConstant) leftNorm.getElement(i, j);
                NodeConstant rightElem = (NodeConstant) rightNorm.getElement(i, j);
                result[i][j] = dispatch(leftElem, rightElem, ctx, scalarOp);
            }
        }

        return new NodeMatrix(result);
    }

    private static NodeMatrix matrixOpScalar(
            NodeMatrix matrix,
            NodeConstant scalar,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        int rows = matrix.getRows();
        int cols = matrix.getCols();
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant elem = (NodeConstant) matrix.getElement(i, j);
                result[i][j] = dispatch(elem, scalar, ctx, scalarOp);
            }
        }

        return new NodeMatrix(result);
    }

    private static NodeMatrix scalarOpMatrix(
            NodeConstant scalar,
            NodeMatrix matrix,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        int rows = matrix.getRows();
        int cols = matrix.getCols();
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant elem = (NodeConstant) matrix.getElement(i, j);
                result[i][j] = dispatch(scalar, elem, ctx, scalarOp);
            }
        }

        return new NodeMatrix(result);
    }

    private static NodeMatrix matrixOpVector(
            NodeMatrix matrix,
            NodeVector vector,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        // If vector size matches number of columns, broadcast to each row
        if (vector.size() == matrix.getCols()) {
            int rows = matrix.getRows();
            int cols = matrix.getCols();
            Node[][] result = new Node[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    NodeConstant matElem = (NodeConstant) matrix.getElement(i, j);
                    NodeConstant vecElem = (NodeConstant) vector.getElement(j);
                    result[i][j] = dispatch(matElem, vecElem, ctx, scalarOp);
                }
            }

            return new NodeMatrix(result);
        }

        // If vector size matches number of rows, broadcast to each column
        if (vector.size() == matrix.getRows()) {
            int rows = matrix.getRows();
            int cols = matrix.getCols();
            Node[][] result = new Node[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    NodeConstant matElem = (NodeConstant) matrix.getElement(i, j);
                    NodeConstant vecElem = (NodeConstant) vector.getElement(i);
                    result[i][j] = dispatch(matElem, vecElem, ctx, scalarOp);
                }
            }

            return new NodeMatrix(result);
        }

        // No exact match: treat vector as row matrix and use matrix-matrix zero-padding
        // This is consistent with NumPy/MATLAB semantics where vectors default to row vectors
        NodeMatrix vectorAsMat = vectorToRowMatrix(vector);
        return matrixOpMatrix(matrix, vectorAsMat, ctx, scalarOp);
    }

    private static NodeMatrix vectorOpMatrix(
            NodeVector vector,
            NodeMatrix matrix,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        // Same logic as matrixOpVector but with operands swapped
        if (vector.size() == matrix.getCols()) {
            int rows = matrix.getRows();
            int cols = matrix.getCols();
            Node[][] result = new Node[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    NodeConstant vecElem = (NodeConstant) vector.getElement(j);
                    NodeConstant matElem = (NodeConstant) matrix.getElement(i, j);
                    result[i][j] = dispatch(vecElem, matElem, ctx, scalarOp);
                }
            }

            return new NodeMatrix(result);
        }

        if (vector.size() == matrix.getRows()) {
            int rows = matrix.getRows();
            int cols = matrix.getCols();
            Node[][] result = new Node[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    NodeConstant vecElem = (NodeConstant) vector.getElement(i);
                    NodeConstant matElem = (NodeConstant) matrix.getElement(i, j);
                    result[i][j] = dispatch(vecElem, matElem, ctx, scalarOp);
                }
            }

            return new NodeMatrix(result);
        }

        // No exact match: treat vector as row matrix and use matrix-matrix zero-padding
        // This is consistent with NumPy/MATLAB semantics where vectors default to row vectors
        NodeMatrix vectorAsMat = vectorToRowMatrix(vector);
        return matrixOpMatrix(vectorAsMat, matrix, ctx, scalarOp);
    }

    // ==================== Vector Implementations ====================

    private static NodeVector vectorOpVector(
            NodeVector left,
            NodeVector right,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        // If sizes match, perform element-wise operation
        if (left.size() == right.size()) {
            int size = left.size();
            Node[] result = new Node[size];

            for (int i = 0; i < size; i++) {
                NodeConstant leftElem = (NodeConstant) left.getElement(i);
                NodeConstant rightElem = (NodeConstant) right.getElement(i);
                result[i] = dispatch(leftElem, rightElem, ctx, scalarOp);
            }

            return new NodeVector(result);
        }

        // Size mismatch: normalize by padding with zeros (for arithmetic operations)
        // Vectors always zero-pad, regardless of size (maintains vector semantics)
        // {1,2} + {1,2,3} = {1,2,0} + {1,2,3} = {2,4,3}
        // {10} + {1,2,3,4} = {10,0,0,0} + {1,2,3,4} = {11,2,3,4}
        int maxSize = Math.max(left.size(), right.size());
        NodeVector leftNorm = normalizeVector(left, maxSize);
        NodeVector rightNorm = normalizeVector(right, maxSize);

        Node[] result = new Node[maxSize];
        for (int i = 0; i < maxSize; i++) {
            NodeConstant leftElem = (NodeConstant) leftNorm.getElement(i);
            NodeConstant rightElem = (NodeConstant) rightNorm.getElement(i);
            result[i] = dispatch(leftElem, rightElem, ctx, scalarOp);
        }

        return new NodeVector(result);
    }

    private static NodeVector vectorOpScalar(
            NodeVector vector,
            NodeConstant scalar,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        int size = vector.size();
        Node[] result = new Node[size];

        for (int i = 0; i < size; i++) {
            NodeConstant elem = (NodeConstant) vector.getElement(i);
            result[i] = dispatch(elem, scalar, ctx, scalarOp);
        }

        return new NodeVector(result);
    }

    private static NodeVector scalarOpVector(
            NodeConstant scalar,
            NodeVector vector,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp) {

        int size = vector.size();
        Node[] result = new Node[size];

        for (int i = 0; i < size; i++) {
            NodeConstant elem = (NodeConstant) vector.getElement(i);
            result[i] = dispatch(scalar, elem, ctx, scalarOp);
        }

        return new NodeVector(result);
    }

    // ==================== Utilities ====================

    private static boolean isScalar(NodeConstant node) {
        return node instanceof NodeNumber ||
                node instanceof NodeBoolean ||
                node instanceof NodeString ||
                node instanceof NodePercent ||
                node instanceof NodeUnit;
    }

    /**
     * Converts a vector to a 1×n row matrix.
     * Used when vector-matrix operations need to fall back to matrix-matrix operations.
     */
    private static NodeMatrix vectorToRowMatrix(NodeVector vector) {
        Node[][] elements = new Node[1][vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            elements[0][i] = vector.getElement(i);
        }
        return new NodeMatrix(elements);
    }

    /**
     * Normalizes a vector to the target size by padding with zeros.
     * Single-element vectors are NOT broadcast - they are zero-padded like any other vector.
     */
    private static NodeVector normalizeVector(NodeVector vector, int targetSize) {
        if (vector.size() == targetSize) {
            return vector;
        }

        // Pad with zeros to target size (applies to all vectors, including size-1)
        if (vector.size() < targetSize) {
            Node[] newElements = new Node[targetSize];
            for (int i = 0; i < vector.size(); i++) {
                newElements[i] = vector.getElement(i);
            }
            // Fill remaining with zeros
            for (int i = vector.size(); i < targetSize; i++) {
                newElements[i] = new NodeRational(0);
            }
            return new NodeVector(newElements);
        }

        // Vector is larger than target - should not happen in current logic
        return vector;
    }

    /**
     * Normalizes a matrix to the target dimensions by padding with zeros.
     */
    private static NodeMatrix normalizeMatrix(NodeMatrix matrix, int targetRows, int targetCols) {
        if (matrix.getRows() == targetRows && matrix.getCols() == targetCols) {
            return matrix;
        }

        // Single element (1x1): broadcast to all positions
        if (matrix.getRows() == 1 && matrix.getCols() == 1) {
            Node element = matrix.getElement(0, 0);
            Node[][] newElements = new Node[targetRows][targetCols];
            for (int i = 0; i < targetRows; i++) {
                java.util.Arrays.fill(newElements[i], element);
            }
            return new NodeMatrix(newElements);
        }

        // Pad with zeros to target dimensions
        Node[][] newElements = new Node[targetRows][targetCols];
        for (int i = 0; i < targetRows; i++) {
            for (int j = 0; j < targetCols; j++) {
                if (i < matrix.getRows() && j < matrix.getCols()) {
                    newElements[i][j] = matrix.getElement(i, j);
                } else {
                    newElements[i][j] = new NodeRational(0);
                }
            }
        }
        return new NodeMatrix(newElements);
    }

    private static NodeMatrix broadcastRowToMatrix(
            NodeMatrix row,
            NodeMatrix target,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp,
            boolean reversed) {

        int rows = target.getRows();
        int cols = target.getCols();
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant rowElem = (NodeConstant) row.getElement(0, j);
                NodeConstant targetElem = (NodeConstant) target.getElement(i, j);
                result[i][j] = reversed ?
                        dispatch(targetElem, rowElem, ctx, scalarOp) :
                        dispatch(rowElem, targetElem, ctx, scalarOp);
            }
        }

        return new NodeMatrix(result);
    }

    private static NodeMatrix broadcastColumnToMatrix(
            NodeMatrix column,
            NodeMatrix target,
            OperatorContext ctx,
            BiFunction<NodeConstant, NodeConstant, NodeConstant> scalarOp,
            boolean reversed) {

        int rows = target.getRows();
        int cols = target.getCols();
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant colElem = (NodeConstant) column.getElement(i, 0);
                NodeConstant targetElem = (NodeConstant) target.getElement(i, j);
                result[i][j] = reversed ?
                        dispatch(targetElem, colElem, ctx, scalarOp) :
                        dispatch(colElem, targetElem, ctx, scalarOp);
            }
        }

        return new NodeMatrix(result);
    }
}
