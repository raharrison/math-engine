package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

/**
 * Unified broadcasting engine for unary and binary operations over scalars, vectors, and matrices.
 * <p>
 * Implements NumPy/Matlab-style broadcasting with enhanced flexibility:
 * <ul>
 *     <li>Scalar operations broadcast to all elements</li>
 *     <li>Vector operations handle size mismatches via zero-padding</li>
 *     <li>Matrix operations support element-wise, row/column broadcasting, and zero-padding</li>
 *     <li>Nested structures are handled recursively</li>
 * </ul>
 *
 * <h2>Unary Broadcasting Rules:</h2>
 * <pre>
 * Scalar  → apply directly
 * Vector  → apply to each element (recursive)
 * Matrix  → apply to each element
 * </pre>
 *
 * <h2>Binary Broadcasting Rules:</h2>
 * <pre>
 * Scalar  op Scalar  → Scalar
 * Vector  op Scalar  → Vector (broadcast scalar to all elements)
 * Scalar  op Vector  → Vector (broadcast scalar to all elements)
 * Vector  op Vector  → Vector (element-wise, zero-pad if sizes differ)
 * Matrix  op Scalar  → Matrix (broadcast scalar to all elements)
 * Scalar  op Matrix  → Matrix (broadcast scalar to all elements)
 * Matrix  op Matrix  → Matrix (element-wise, with row/column broadcasting)
 * Matrix  op Vector  → Matrix (broadcast vector to rows/columns)
 * Vector  op Matrix  → Matrix (broadcast vector to rows/columns)
 * </pre>
 *
 * @see uk.co.ryanharrison.mathengine.parser.function.FunctionContext
 */
public final class BroadcastingEngine {

    private BroadcastingEngine() {
    }

    /**
     * A unary operation on a scalar value.
     */
    @FunctionalInterface
    public interface UnaryOperation {
        NodeConstant apply(NodeConstant value);
    }

    /**
     * A binary operation on two scalar values.
     */
    @FunctionalInterface
    public interface BinaryOperation {
        NodeConstant apply(NodeConstant left, NodeConstant right);
    }

    // ==================== Unary Broadcasting ====================

    /**
     * Applies a unary operation with broadcasting over vectors and matrices.
     * <p>
     * When the value is a vector, the operation is recursively applied to each element.
     * When the value is a matrix, the operation is applied to each element.
     * Scalars are passed directly to the operation.
     *
     * @param value the value (scalar, vector, or matrix)
     * @param op    the operation to apply to scalar values
     * @return the result with matching structure
     */
    public static NodeConstant applyUnary(NodeConstant value, UnaryOperation op) {
        if (value instanceof NodeMatrix matrix) {
            int rows = matrix.getRows();
            int cols = matrix.getCols();
            Node[][] result = new Node[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    result[i][j] = applyUnary((NodeConstant) matrix.getElement(i, j), op);
                }
            }
            return new NodeMatrix(result);
        }

        if (value instanceof NodeVector vector) {
            Node[] elements = vector.getElements();
            Node[] result = new Node[elements.length];
            for (int i = 0; i < elements.length; i++) {
                result[i] = applyUnary((NodeConstant) elements[i], op);
            }
            return new NodeVector(result);
        }

        return op.apply(value);
    }

    // ==================== Binary Broadcasting ====================

    /**
     * Applies a binary operation with full broadcasting support.
     *
     * @param left  left operand
     * @param right right operand
     * @param op    the operation to apply to scalar pairs
     * @return result with appropriate type and structure
     */
    public static NodeConstant applyBinary(NodeConstant left, NodeConstant right, BinaryOperation op) {

        // ==================== Matrix Operations ====================

        if (left instanceof NodeMatrix leftMat && right instanceof NodeMatrix rightMat) {
            return matrixOpMatrix(leftMat, rightMat, op);
        }

        if (left instanceof NodeMatrix leftMat && isScalar(right)) {
            return matrixOpScalar(leftMat, right, op);
        }

        if (isScalar(left) && right instanceof NodeMatrix rightMat) {
            return scalarOpMatrix(left, rightMat, op);
        }

        if (left instanceof NodeMatrix leftMat && right instanceof NodeVector rightVec) {
            return matrixOpVector(leftMat, rightVec, op);
        }

        if (left instanceof NodeVector leftVec && right instanceof NodeMatrix rightMat) {
            return vectorOpMatrix(leftVec, rightMat, op);
        }

        // ==================== Vector Operations ====================

        if (left instanceof NodeVector leftVec && right instanceof NodeVector rightVec) {
            return vectorOpVector(leftVec, rightVec, op);
        }

        if (left instanceof NodeVector leftVec && isScalar(right)) {
            return vectorOpScalar(leftVec, right, op);
        }

        if (isScalar(left) && right instanceof NodeVector rightVec) {
            return scalarOpVector(left, rightVec, op);
        }

        // ==================== Scalar Operations ====================

        if (isScalar(left) && isScalar(right)) {
            return op.apply(left, right);
        }

        throw new TypeError("Unsupported operand types: " +
                left.getClass().getSimpleName() + " and " +
                right.getClass().getSimpleName());
    }

    // ==================== Matrix Implementations ====================

    private static NodeMatrix matrixOpMatrix(NodeMatrix left, NodeMatrix right, BinaryOperation op) {
        // If dimensions match, perform element-wise operation
        if (left.getRows() == right.getRows() && left.getCols() == right.getCols()) {
            int rows = left.getRows();
            int cols = left.getCols();
            Node[][] result = new Node[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    NodeConstant leftElem = (NodeConstant) left.getElement(i, j);
                    NodeConstant rightElem = (NodeConstant) right.getElement(i, j);
                    result[i][j] = applyBinary(leftElem, rightElem, op);
                }
            }

            return new NodeMatrix(result);
        }

        // Try broadcasting: if one matrix has a single row/column, broadcast it
        if (left.getRows() == 1 && left.getCols() == right.getCols()) {
            return broadcastRowToMatrix(left, right, op, false);
        }

        if (right.getRows() == 1 && right.getCols() == left.getCols()) {
            return broadcastRowToMatrix(right, left, op, true);
        }

        if (left.getCols() == 1 && left.getRows() == right.getRows()) {
            return broadcastColumnToMatrix(left, right, op, false);
        }

        if (right.getCols() == 1 && right.getRows() == left.getRows()) {
            return broadcastColumnToMatrix(right, left, op, true);
        }

        // Size mismatch: normalize by padding with zeros
        int maxRows = Math.max(left.getRows(), right.getRows());
        int maxCols = Math.max(left.getCols(), right.getCols());
        NodeMatrix leftNorm = normalizeMatrix(left, maxRows, maxCols);
        NodeMatrix rightNorm = normalizeMatrix(right, maxRows, maxCols);

        Node[][] result = new Node[maxRows][maxCols];
        for (int i = 0; i < maxRows; i++) {
            for (int j = 0; j < maxCols; j++) {
                NodeConstant leftElem = (NodeConstant) leftNorm.getElement(i, j);
                NodeConstant rightElem = (NodeConstant) rightNorm.getElement(i, j);
                result[i][j] = applyBinary(leftElem, rightElem, op);
            }
        }

        return new NodeMatrix(result);
    }

    private static NodeMatrix matrixOpScalar(NodeMatrix matrix, NodeConstant scalar, BinaryOperation op) {
        int rows = matrix.getRows();
        int cols = matrix.getCols();
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant elem = (NodeConstant) matrix.getElement(i, j);
                result[i][j] = applyBinary(elem, scalar, op);
            }
        }

        return new NodeMatrix(result);
    }

    private static NodeMatrix scalarOpMatrix(NodeConstant scalar, NodeMatrix matrix, BinaryOperation op) {
        int rows = matrix.getRows();
        int cols = matrix.getCols();
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant elem = (NodeConstant) matrix.getElement(i, j);
                result[i][j] = applyBinary(scalar, elem, op);
            }
        }

        return new NodeMatrix(result);
    }

    private static NodeMatrix matrixOpVector(NodeMatrix matrix, NodeVector vector, BinaryOperation op) {
        // If vector size matches number of columns, broadcast to each row
        if (vector.size() == matrix.getCols()) {
            int rows = matrix.getRows();
            int cols = matrix.getCols();
            Node[][] result = new Node[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    NodeConstant matElem = (NodeConstant) matrix.getElement(i, j);
                    NodeConstant vecElem = (NodeConstant) vector.getElement(j);
                    result[i][j] = applyBinary(matElem, vecElem, op);
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
                    result[i][j] = applyBinary(matElem, vecElem, op);
                }
            }

            return new NodeMatrix(result);
        }

        // No exact match: treat vector as row matrix and use matrix-matrix zero-padding
        NodeMatrix vectorAsMat = vectorToRowMatrix(vector);
        return matrixOpMatrix(matrix, vectorAsMat, op);
    }

    private static NodeMatrix vectorOpMatrix(NodeVector vector, NodeMatrix matrix, BinaryOperation op) {
        // Same logic as matrixOpVector but with operands swapped
        if (vector.size() == matrix.getCols()) {
            int rows = matrix.getRows();
            int cols = matrix.getCols();
            Node[][] result = new Node[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    NodeConstant vecElem = (NodeConstant) vector.getElement(j);
                    NodeConstant matElem = (NodeConstant) matrix.getElement(i, j);
                    result[i][j] = applyBinary(vecElem, matElem, op);
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
                    result[i][j] = applyBinary(vecElem, matElem, op);
                }
            }

            return new NodeMatrix(result);
        }

        // No exact match: treat vector as row matrix
        NodeMatrix vectorAsMat = vectorToRowMatrix(vector);
        return matrixOpMatrix(vectorAsMat, matrix, op);
    }

    // ==================== Vector Implementations ====================

    private static NodeVector vectorOpVector(NodeVector left, NodeVector right, BinaryOperation op) {
        // If sizes match, perform element-wise operation
        if (left.size() == right.size()) {
            int size = left.size();
            Node[] result = new Node[size];

            for (int i = 0; i < size; i++) {
                NodeConstant leftElem = (NodeConstant) left.getElement(i);
                NodeConstant rightElem = (NodeConstant) right.getElement(i);
                result[i] = applyBinary(leftElem, rightElem, op);
            }

            return new NodeVector(result);
        }

        // Size mismatch: normalize by padding with zeros
        int maxSize = Math.max(left.size(), right.size());
        NodeVector leftNorm = normalizeVector(left, maxSize);
        NodeVector rightNorm = normalizeVector(right, maxSize);

        Node[] result = new Node[maxSize];
        for (int i = 0; i < maxSize; i++) {
            NodeConstant leftElem = (NodeConstant) leftNorm.getElement(i);
            NodeConstant rightElem = (NodeConstant) rightNorm.getElement(i);
            result[i] = applyBinary(leftElem, rightElem, op);
        }

        return new NodeVector(result);
    }

    private static NodeVector vectorOpScalar(NodeVector vector, NodeConstant scalar, BinaryOperation op) {
        int size = vector.size();
        Node[] result = new Node[size];

        for (int i = 0; i < size; i++) {
            NodeConstant elem = (NodeConstant) vector.getElement(i);
            result[i] = applyBinary(elem, scalar, op);
        }

        return new NodeVector(result);
    }

    private static NodeVector scalarOpVector(NodeConstant scalar, NodeVector vector, BinaryOperation op) {
        int size = vector.size();
        Node[] result = new Node[size];

        for (int i = 0; i < size; i++) {
            NodeConstant elem = (NodeConstant) vector.getElement(i);
            result[i] = applyBinary(scalar, elem, op);
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

    private static NodeMatrix vectorToRowMatrix(NodeVector vector) {
        Node[][] elements = new Node[1][vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            elements[0][i] = vector.getElement(i);
        }
        return new NodeMatrix(elements);
    }

    private static NodeVector normalizeVector(NodeVector vector, int targetSize) {
        if (vector.size() == targetSize) {
            return vector;
        }

        if (vector.size() < targetSize) {
            Node[] newElements = new Node[targetSize];
            for (int i = 0; i < vector.size(); i++) {
                newElements[i] = vector.getElement(i);
            }
            for (int i = vector.size(); i < targetSize; i++) {
                newElements[i] = new NodeRational(0);
            }
            return new NodeVector(newElements);
        }

        return vector;
    }

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
            BinaryOperation op,
            boolean reversed) {

        int rows = target.getRows();
        int cols = target.getCols();
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant rowElem = (NodeConstant) row.getElement(0, j);
                NodeConstant targetElem = (NodeConstant) target.getElement(i, j);
                result[i][j] = reversed ?
                        applyBinary(targetElem, rowElem, op) :
                        applyBinary(rowElem, targetElem, op);
            }
        }

        return new NodeMatrix(result);
    }

    private static NodeMatrix broadcastColumnToMatrix(
            NodeMatrix column,
            NodeMatrix target,
            BinaryOperation op,
            boolean reversed) {

        int rows = target.getRows();
        int cols = target.getCols();
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant colElem = (NodeConstant) column.getElement(i, 0);
                NodeConstant targetElem = (NodeConstant) target.getElement(i, j);
                result[i][j] = reversed ?
                        applyBinary(targetElem, colElem, op) :
                        applyBinary(colElem, targetElem, op);
            }
        }

        return new NodeMatrix(result);
    }
}
