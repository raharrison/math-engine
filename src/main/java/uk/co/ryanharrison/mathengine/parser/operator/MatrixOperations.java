package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.operator.binary.MultiplyOperator;
import uk.co.ryanharrison.mathengine.parser.operator.binary.PlusOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

/**
 * Centralized matrix and vector operations.
 * <p>
 * Provides reusable implementations of:
 * <ul>
 *     <li>Matrix multiplication</li>
 *     <li>Matrix exponentiation</li>
 *     <li>Vector dot product</li>
 *     <li>Matrix/vector utilities</li>
 * </ul>
 */
public final class MatrixOperations {

    private MatrixOperations() {
    }

    // ==================== Matrix Multiplication ====================

    /**
     * Performs true matrix multiplication (A @ B).
     *
     * @param left  left matrix (m x k)
     * @param right right matrix (k x n)
     * @param ctx   operator context
     * @return result matrix (m x n)
     * @throws TypeError if dimensions are incompatible
     */
    public static NodeMatrix multiply(NodeMatrix left, NodeMatrix right, OperatorContext ctx) {
        int leftRows = left.getRows();
        int leftCols = left.getCols();
        int rightRows = right.getRows();
        int rightCols = right.getCols();

        if (leftCols != rightRows) {
            throw new TypeError("Cannot multiply matrices: " + leftRows + "x" + leftCols +
                    " and " + rightRows + "x" + rightCols + " (incompatible dimensions)");
        }

        Node[][] result = new Node[leftRows][rightCols];

        for (int i = 0; i < leftRows; i++) {
            for (int j = 0; j < rightCols; j++) {
                // Compute dot product of row i of A and column j of B
                NodeConstant sum = new NodeRational(0);
                for (int k = 0; k < leftCols; k++) {
                    NodeConstant leftElement = (NodeConstant) left.getElement(i, k);
                    NodeConstant rightElement = (NodeConstant) right.getElement(k, j);
                    NodeConstant product = MultiplyOperator.INSTANCE.apply(leftElement, rightElement, ctx);
                    sum = PlusOperator.INSTANCE.apply(sum, product, ctx);
                }
                result[i][j] = sum;
            }
        }

        return new NodeMatrix(result);
    }

    // ==================== Matrix Exponentiation ====================

    /**
     * Computes matrix exponentiation via repeated multiplication.
     * <p>
     * For positive integers: A^n = A * A * ... * A (n times)
     * <br>
     * For zero: A^0 = I (identity matrix)
     * <br>
     * For negative integers: A^(-n) = (A^(-1))^n
     *
     * @param matrix the matrix to raise to a power (must be square)
     * @param exp    the integer exponent
     * @param ctx    operator context
     * @return matrix^exp
     * @throws TypeError if matrix is not square or exp is negative
     */
    public static NodeMatrix power(NodeMatrix matrix, int exp, OperatorContext ctx) {
        // Matrix must be square for exponentiation
        if (matrix.getRows() != matrix.getCols()) {
            throw new TypeError("Matrix exponentiation requires a square matrix, got " +
                    matrix.getRows() + "x" + matrix.getCols());
        }

        int n = matrix.getRows();

        // A^0 = I (identity matrix)
        if (exp == 0) {
            return identityMatrix(n);
        }

        // A^(-n) = (A^(-1))^n - would require matrix inversion
        if (exp < 0) {
            throw new TypeError("Negative matrix exponentiation not yet implemented (requires matrix inverse)");
        }

        // A^1 = A
        if (exp == 1) {
            return matrix;
        }

        // Use exponentiation by squaring for efficiency: O(log n)
        NodeMatrix result = identityMatrix(n);
        NodeMatrix base = matrix;
        int power = exp;

        while (power > 0) {
            if (power % 2 == 1) {
                result = multiply(result, base, ctx);
            }
            base = multiply(base, base, ctx);
            power /= 2;
        }

        return result;
    }

    /**
     * Creates an identity matrix of size n x n.
     */
    public static NodeMatrix identityMatrix(int n) {
        Node[][] elements = new Node[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                elements[i][j] = new NodeRational(i == j ? 1 : 0);
            }
        }
        return new NodeMatrix(elements);
    }

    // ==================== Vector Operations ====================

    /**
     * Computes the dot product of two vectors.
     *
     * @param left  left vector
     * @param right right vector (must be same size as left)
     * @param ctx   operator context
     * @return scalar result of dot product
     * @throws TypeError if vectors have different sizes
     */
    public static NodeConstant dotProduct(NodeVector left, NodeVector right, OperatorContext ctx) {
        if (left.size() != right.size()) {
            throw new TypeError("Dot product requires vectors of equal length: " +
                    left.size() + " vs " + right.size());
        }

        NodeConstant sum = new NodeRational(0);
        for (int i = 0; i < left.size(); i++) {
            NodeConstant leftElement = (NodeConstant) left.getElement(i);
            NodeConstant rightElement = (NodeConstant) right.getElement(i);
            NodeConstant product = MultiplyOperator.INSTANCE.apply(leftElement, rightElement, ctx);
            sum = PlusOperator.INSTANCE.apply(sum, product, ctx);
        }

        return sum;
    }
}
