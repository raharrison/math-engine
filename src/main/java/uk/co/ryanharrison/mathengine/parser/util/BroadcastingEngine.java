package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

/**
 * Spreads a scalar operation over vectors and matrices.
 *
 * <h2>Shape rules</h2>
 * <pre>
 * scalar op scalar  -> scalar
 * vector op scalar  -> vector          (the scalar reaches every element)
 * vector op vector  -> vector          (element-wise, zero-padding the shorter side)
 * matrix op scalar  -> matrix
 * matrix op matrix  -> matrix          (element-wise; a single row or column spreads
 *                                       across the other operand, otherwise zero-padded)
 * matrix op vector  -> matrix          (the vector spreads across rows or columns when
 *                                       its length matches, otherwise it is read as one row)
 * </pre>
 * Nested collections recurse, so a vector of vectors behaves as you would expect.
 * Operand order is preserved throughout, which matters for subtraction and division.
 */
public final class BroadcastingEngine {

    private BroadcastingEngine() {
    }

    @FunctionalInterface
    public interface UnaryOperation {
        NodeConstant apply(NodeConstant value);
    }

    @FunctionalInterface
    public interface BinaryOperation {
        NodeConstant apply(NodeConstant left, NodeConstant right);
    }

    // ==================== Unary ====================

    public static NodeConstant applyUnary(NodeConstant value, UnaryOperation op) {
        if (value instanceof NodeMatrix matrix) {
            return buildMatrix(matrix.getRows(), matrix.getCols(),
                    (i, j) -> applyUnary(element(matrix, i, j), op));
        }
        if (value instanceof NodeVector vector) {
            return buildVector(vector.size(), i -> applyUnary(element(vector, i), op));
        }
        return op.apply(value);
    }

    // ==================== Binary ====================

    public static NodeConstant applyBinary(NodeConstant left, NodeConstant right, BinaryOperation op) {
        if (left instanceof NodeMatrix leftMatrix) {
            return switch (right) {
                case NodeMatrix rightMatrix -> matrixByMatrix(leftMatrix, rightMatrix, op);
                case NodeVector rightVector -> matrixByVector(leftMatrix, rightVector, op, false);
                default -> buildMatrix(leftMatrix.getRows(), leftMatrix.getCols(),
                        (i, j) -> applyBinary(element(leftMatrix, i, j), right, op));
            };
        }

        if (right instanceof NodeMatrix rightMatrix) {
            if (left instanceof NodeVector leftVector) {
                return matrixByVector(rightMatrix, leftVector, op, true);
            }
            return buildMatrix(rightMatrix.getRows(), rightMatrix.getCols(),
                    (i, j) -> applyBinary(left, element(rightMatrix, i, j), op));
        }

        if (left instanceof NodeVector leftVector && right instanceof NodeVector rightVector) {
            int size = Math.max(leftVector.size(), rightVector.size());
            return buildVector(size, i -> applyBinary(
                    padded(leftVector, i), padded(rightVector, i), op));
        }
        if (left instanceof NodeVector leftVector) {
            return buildVector(leftVector.size(), i -> applyBinary(element(leftVector, i), right, op));
        }
        if (right instanceof NodeVector rightVector) {
            return buildVector(rightVector.size(), i -> applyBinary(left, element(rightVector, i), op));
        }

        if (isScalar(left) && isScalar(right)) {
            return op.apply(left, right);
        }
        throw new TypeError("Unsupported operand types: " + left.typeName() + " and " + right.typeName());
    }

    // ==================== Shape handling ====================

    private static NodeConstant matrixByMatrix(NodeMatrix left, NodeMatrix right, BinaryOperation op) {
        if (left.getRows() == right.getRows() && left.getCols() == right.getCols()) {
            return buildMatrix(left.getRows(), left.getCols(),
                    (i, j) -> applyBinary(element(left, i, j), element(right, i, j), op));
        }

        // A single row or a single column spreads across the other operand
        if (left.getRows() == 1 && left.getCols() == right.getCols()) {
            return buildMatrix(right.getRows(), right.getCols(),
                    (i, j) -> applyBinary(element(left, 0, j), element(right, i, j), op));
        }
        if (right.getRows() == 1 && right.getCols() == left.getCols()) {
            return buildMatrix(left.getRows(), left.getCols(),
                    (i, j) -> applyBinary(element(left, i, j), element(right, 0, j), op));
        }
        if (left.getCols() == 1 && left.getRows() == right.getRows()) {
            return buildMatrix(right.getRows(), right.getCols(),
                    (i, j) -> applyBinary(element(left, i, 0), element(right, i, j), op));
        }
        if (right.getCols() == 1 && right.getRows() == left.getRows()) {
            return buildMatrix(left.getRows(), left.getCols(),
                    (i, j) -> applyBinary(element(left, i, j), element(right, i, 0), op));
        }

        // A 1x1 matrix stands for its single value everywhere
        int rows = Math.max(left.getRows(), right.getRows());
        int cols = Math.max(left.getCols(), right.getCols());
        return buildMatrix(rows, cols, (i, j) -> applyBinary(padded(left, i, j), padded(right, i, j), op));
    }

    /**
     * Spreads a vector across a matrix: along rows when its length matches the column
     * count, along columns when it matches the row count, and otherwise as a single row.
     *
     * @param vectorOnLeft whether the vector was the left operand, which matters for
     *                     operations that are not commutative
     */
    private static NodeConstant matrixByVector(NodeMatrix matrix, NodeVector vector,
                                               BinaryOperation op, boolean vectorOnLeft) {
        BinaryOperation ordered = vectorOnLeft ? (a, b) -> op.apply(b, a) : op;

        if (vector.size() == matrix.getCols()) {
            return buildMatrix(matrix.getRows(), matrix.getCols(),
                    (i, j) -> applyBinary(element(matrix, i, j), element(vector, j), ordered));
        }
        if (vector.size() == matrix.getRows()) {
            return buildMatrix(matrix.getRows(), matrix.getCols(),
                    (i, j) -> applyBinary(element(matrix, i, j), element(vector, i), ordered));
        }

        NodeMatrix asRow = buildMatrix(1, vector.size(), (i, j) -> element(vector, j));
        return vectorOnLeft ? matrixByMatrix(asRow, matrix, op) : matrixByMatrix(matrix, asRow, op);
    }

    // ==================== Helpers ====================

    private interface ElementAt {
        NodeConstant get(int index);
    }

    private interface ElementAtRowCol {
        NodeConstant get(int row, int col);
    }

    private static NodeVector buildVector(int size, ElementAt source) {
        var elements = new Node[size];
        for (int i = 0; i < size; i++) {
            elements[i] = source.get(i);
        }
        return new NodeVector(elements);
    }

    private static NodeMatrix buildMatrix(int rows, int cols, ElementAtRowCol source) {
        var elements = new Node[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                elements[i][j] = source.get(i, j);
            }
        }
        return new NodeMatrix(elements);
    }

    private static NodeConstant element(NodeVector vector, int index) {
        return (NodeConstant) vector.getElement(index);
    }

    private static NodeConstant element(NodeMatrix matrix, int row, int col) {
        return (NodeConstant) matrix.getElement(row, col);
    }

    private static final NodeConstant ZERO = new NodeRational(0);

    private static NodeConstant padded(NodeVector vector, int index) {
        return index < vector.size() ? element(vector, index) : ZERO;
    }

    private static NodeConstant padded(NodeMatrix matrix, int row, int col) {
        if (matrix.getRows() == 1 && matrix.getCols() == 1) {
            return element(matrix, 0, 0);
        }
        return row < matrix.getRows() && col < matrix.getCols() ? element(matrix, row, col) : ZERO;
    }

    private static boolean isScalar(NodeConstant value) {
        return value instanceof NodeNumber || value instanceof NodeString || value instanceof NodeUnit;
    }
}
