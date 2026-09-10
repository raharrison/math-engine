package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

/**
 * Linear-algebra products over matrix and vector values.
 * <p>
 * These are the operations that are not element-wise, so they cannot go through
 * {@link BroadcastingEngine}. Element arithmetic uses {@link NodeConstant#multiply}
 * and {@link NodeConstant#add}, so exact values stay exact.
 */
public final class MatrixOperations {

    /**
     * Only used where an element is already inexact, so a residue can be round-off.
     */
    private static final double INEXACT_ZERO_TOLERANCE = 1e-10;

    private MatrixOperations() {
    }

    /**
     * True matrix multiplication of an m×k by a k×n matrix.
     *
     * @throws TypeError if the inner dimensions disagree
     */
    public static NodeMatrix multiply(NodeMatrix left, NodeMatrix right) {
        int rows = left.getRows();
        int inner = left.getCols();
        int cols = right.getCols();

        if (inner != right.getRows()) {
            throw new TypeError("Cannot multiply matrices: " + rows + "x" + inner +
                    " and " + right.getRows() + "x" + cols + " (incompatible dimensions)");
        }

        Node[][] result = new Node[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                NodeConstant sum = new NodeRational(0);
                for (int k = 0; k < inner; k++) {
                    sum = sum.add(element(left, i, k).multiply(element(right, k, j)));
                }
                result[i][j] = sum;
            }
        }
        return new NodeMatrix(result);
    }

    /**
     * Repeated matrix multiplication, by squaring. {@code A^0} is the identity.
     *
     * @throws TypeError if the matrix is not square, or the exponent is negative
     */
    public static NodeMatrix power(NodeMatrix matrix, int exponent) {
        if (matrix.getRows() != matrix.getCols()) {
            throw new TypeError("Matrix exponentiation requires a square matrix, got " +
                    matrix.getRows() + "x" + matrix.getCols());
        }
        if (exponent < 0) {
            throw new TypeError("Negative matrix exponentiation requires a matrix inverse, which is not supported");
        }
        if (exponent == 1) {
            return matrix;
        }

        NodeMatrix result = identity(matrix.getRows());
        NodeMatrix base = matrix;
        for (int remaining = exponent; remaining > 0; remaining /= 2) {
            if (remaining % 2 == 1) {
                result = multiply(result, base);
            }
            base = multiply(base, base);
        }
        return result;
    }

    public static NodeMatrix identity(int size) {
        Node[][] elements = new Node[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                elements[i][j] = new NodeRational(i == j ? 1 : 0);
            }
        }
        return new NodeMatrix(elements);
    }

    /**
     * Determinant by Gaussian elimination, carried out in the element type.
     * <p>
     * Working through {@link NodeConstant} rather than {@code double} keeps an integer
     * matrix's determinant an exact integer. Going via a floating-point decomposition
     * used to answer -1.0000000000000004 for a matrix whose determinant is -1.
     *
     * @throws TypeError if the matrix is not square
     */
    public static NodeConstant determinant(NodeMatrix matrix) {
        int size = matrix.getRows();
        if (size != matrix.getCols()) {
            throw new TypeError("Determinant requires a square matrix, got " +
                    size + "x" + matrix.getCols());
        }

        NodeConstant[][] rows = new NodeConstant[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                rows[i][j] = element(matrix, i, j);
            }
        }

        NodeConstant determinant = new NodeRational(1);
        for (int column = 0; column < size; column++) {
            int pivot = pivotRow(rows, column);
            if (pivot < 0) {
                return new NodeRational(0);
            }
            if (pivot != column) {
                NodeConstant[] swapped = rows[pivot];
                rows[pivot] = rows[column];
                rows[column] = swapped;
                determinant = determinant.negate();
            }

            NodeConstant pivotValue = rows[column][column];
            determinant = determinant.multiply(pivotValue);

            for (int row = column + 1; row < size; row++) {
                NodeConstant factor = rows[row][column].divide(pivotValue);
                for (int j = column; j < size; j++) {
                    rows[row][j] = rows[row][j].subtract(factor.multiply(rows[column][j]));
                }
            }
        }
        return determinant;
    }

    /**
     * Inverse by Gauss-Jordan elimination, carried out in the element type.
     * <p>
     * As with {@link #determinant}, working in {@link NodeConstant} keeps an exact matrix
     * exact: the inverse of an integer matrix is a matrix of exact rationals rather than
     * of doubles that miss by 2e-16.
     *
     * @throws TypeError       if the matrix is not square
     * @throws DomainException if the matrix is singular, so has no inverse
     */
    public static NodeMatrix inverse(NodeMatrix matrix) {
        int size = matrix.getRows();
        if (size != matrix.getCols()) {
            throw new TypeError("Matrix inversion requires a square matrix, got " +
                    size + "x" + matrix.getCols());
        }

        // [ matrix | identity ], reduced until the left half is the identity
        NodeConstant[][] rows = new NodeConstant[size][2 * size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                rows[i][j] = element(matrix, i, j);
                rows[i][size + j] = new NodeRational(i == j ? 1 : 0);
            }
        }

        for (int column = 0; column < size; column++) {
            int pivot = pivotRow(rows, column);
            if (pivot < 0) {
                throw new DomainException("Matrix is singular, so it has no inverse");
            }
            if (pivot != column) {
                NodeConstant[] swapped = rows[pivot];
                rows[pivot] = rows[column];
                rows[column] = swapped;
            }

            NodeConstant pivotValue = rows[column][column];
            for (int j = 0; j < 2 * size; j++) {
                rows[column][j] = rows[column][j].divide(pivotValue);
            }

            for (int row = 0; row < size; row++) {
                if (row == column) {
                    continue;
                }
                NodeConstant factor = rows[row][column];
                for (int j = 0; j < 2 * size; j++) {
                    rows[row][j] = rows[row][j].subtract(factor.multiply(rows[column][j]));
                }
            }
        }

        Node[][] result = new Node[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(rows[i], size, result[i], 0, size);
        }
        return new NodeMatrix(result);
    }

    /**
     * Rank by Gaussian elimination, carried out in the element type.
     * <p>
     * The rank is the count of pivots, so everything turns on which elements count as zero.
     * An exact matrix is decided exactly, with no threshold to guess about; only a matrix
     * already holding a double falls back to {@link #INEXACT_ZERO_TOLERANCE}.
     *
     * @return the number of linearly independent rows, between 0 and {@code min(rows, cols)}
     */
    public static int rank(NodeMatrix matrix) {
        int rowCount = matrix.getRows();
        int colCount = matrix.getCols();

        NodeConstant[][] rows = new NodeConstant[rowCount][colCount];
        boolean allExact = true;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                rows[i][j] = element(matrix, i, j);
                allExact &= rows[i][j] instanceof NodeRational;
            }
        }
        double tolerance = allExact ? 0.0 : INEXACT_ZERO_TOLERANCE;

        int rank = 0;
        for (int column = 0; column < colCount && rank < rowCount; column++) {
            int pivot = pivotRow(rows, column, rank, tolerance);
            if (pivot < 0) {
                // No pivot in this column, so it adds nothing to the row space
                continue;
            }
            if (pivot != rank) {
                NodeConstant[] swapped = rows[pivot];
                rows[pivot] = rows[rank];
                rows[rank] = swapped;
            }

            NodeConstant pivotValue = rows[rank][column];
            for (int row = rank + 1; row < rowCount; row++) {
                NodeConstant factor = rows[row][column].divide(pivotValue);
                for (int j = column; j < colCount; j++) {
                    rows[row][j] = rows[row][j].subtract(factor.multiply(rows[rank][j]));
                }
            }
            rank++;
        }
        return rank;
    }

    /**
     * The remaining row with the largest pivot, or -1 when the column is all zeros.
     */
    private static int pivotRow(NodeConstant[][] rows, int column) {
        return pivotRow(rows, column, column, 0.0);
    }

    /**
     * The row at or below {@code fromRow} with the largest pivot in {@code column},
     * or -1 when no candidate exceeds {@code tolerance}.
     */
    private static int pivotRow(NodeConstant[][] rows, int column, int fromRow, double tolerance) {
        int best = -1;
        double bestMagnitude = tolerance;
        for (int row = fromRow; row < rows.length; row++) {
            double magnitude = Math.abs(rows[row][column].doubleValue());
            if (magnitude > bestMagnitude) {
                bestMagnitude = magnitude;
                best = row;
            }
        }
        return best;
    }

    /**
     * @throws TypeError if the vectors have different lengths
     */
    public static NodeConstant dotProduct(NodeVector left, NodeVector right) {
        if (left.size() != right.size()) {
            throw new TypeError("Dot product requires vectors of equal length: " +
                    left.size() + " vs " + right.size());
        }

        NodeConstant sum = new NodeRational(0);
        for (int i = 0; i < left.size(); i++) {
            sum = sum.add(((NodeConstant) left.getElement(i)).multiply((NodeConstant) right.getElement(i)));
        }
        return sum;
    }

    private static NodeConstant element(NodeMatrix matrix, int row, int col) {
        return (NodeConstant) matrix.getElement(row, col);
    }
}
