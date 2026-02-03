package uk.co.ryanharrison.mathengine.linearalgebra;

import java.util.Arrays;

/**
 * Immutable implementation of LU decomposition with partial pivoting.
 * <p>
 * The LU decomposition is a matrix factorization that expresses a matrix <b>A</b> as the product
 * of a lower triangular matrix <b>L</b> and an upper triangular matrix <b>U</b>, with row
 * permutations captured by a pivot vector. Mathematically:
 * </p>
 * <pre>
 * A(piv,:) = L * U
 * </pre>
 * <p>
 * where:
 * </p>
 * <ul>
 *     <li><b>L</b> is an m-by-n unit lower triangular matrix (diagonal elements = 1)</li>
 *     <li><b>U</b> is an n-by-n upper triangular matrix</li>
 *     <li><b>piv</b> is a permutation vector representing row exchanges during pivoting</li>
 * </ul>
 * <p>
 * For an m-by-n matrix A:
 * </p>
 * <ul>
 *     <li>If m ≥ n: L is m-by-n, U is n-by-n</li>
 *     <li>If m < n: L is m-by-m, U is m-by-n</li>
 * </ul>
 *
 * <h2>Mathematical Properties:</h2>
 * <ul>
 *     <li>The decomposition always exists, even for singular matrices</li>
 *     <li>Uses partial pivoting (row exchanges) for numerical stability</li>
 *     <li>The determinant of A equals the product of diagonal elements of U times the pivot sign</li>
 *     <li>The matrix is nonsingular if and only if all diagonal elements of U are non-zero</li>
 * </ul>
 *
 * <h2>Primary Use Cases:</h2>
 * <ul>
 *     <li>Solving systems of linear equations (A*X = B)</li>
 *     <li>Computing matrix determinants efficiently</li>
 *     <li>Computing matrix inverses</li>
 *     <li>Numerical stability analysis</li>
 * </ul>
 *
 * <h2>Implementation:</h2>
 * <p>
 * This implementation uses the Crout/Doolittle algorithm with partial pivoting,
 * a "left-looking" dot-product approach. The decomposition is computed at construction
 * time and all results are cached.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Create a matrix and decompose it
 * Matrix A = new Matrix(new double[][]{
 *     {2, 1, 1},
 *     {4, -6, 0},
 *     {-2, 7, 2}
 * });
 * LUDecomposition lu = new LUDecomposition(A);
 *
 * // Access decomposition components
 * Matrix L = lu.getL();  // Lower triangular matrix
 * Matrix U = lu.getU();  // Upper triangular matrix
 * int[] pivot = lu.getPivot();  // Pivot permutation vector
 *
 * // Solve a system of equations A*X = B
 * Matrix B = new Matrix(new double[][]{{4}, {-8}, {11}});
 * if (lu.isNonsingular()) {
 *     Matrix X = lu.solve(B);
 * }
 *
 * // Compute determinant
 * double det = lu.getDeterminant();
 * }</pre>
 *
 * @author Ryan Harrison
 * @see <a href="http://math.nist.gov/javanumerics/jama/">JAMA: Java Matrix Package</a>
 */
public final class LUDecomposition {

    /**
     * Internal storage of the LU decomposition.
     * <p>
     * The lower triangular factor L (with unit diagonal) and upper triangular factor U
     * are stored in a single array. For each element:
     * </p>
     * <ul>
     *     <li>Elements below the diagonal belong to L (diagonal of L is implicitly 1.0)</li>
     *     <li>Elements on and above the diagonal belong to U</li>
     * </ul>
     */
    private final double[][] lu;

    /**
     * Number of rows in the original matrix.
     */
    private final int rowCount;

    /**
     * Number of columns in the original matrix.
     */
    private final int columnCount;

    /**
     * Pivot sign, used for computing determinant.
     * <p>
     * Equals +1 or -1 depending on whether an even or odd number of row exchanges
     * occurred during decomposition. The determinant equals the product of the diagonal
     * elements of U multiplied by this sign.
     * </p>
     */
    private final int pivotSign;

    /**
     * Pivot permutation vector.
     * <p>
     * Records the row permutations applied during partial pivoting. The value at index i
     * indicates which original row is now at position i after all pivoting operations.
     * </p>
     */
    private final int[] pivot;

    /**
     * Constructs an LU decomposition of the specified matrix using partial pivoting.
     * <p>
     * The decomposition is computed immediately during construction using the Crout/Doolittle
     * algorithm with partial pivoting. The input matrix is deep-copied and not modified.
     * </p>
     * <p>
     * <b>Time Complexity:</b> O(n³) for an n-by-n matrix
     * </p>
     *
     * @param matrix the matrix to decompose (can be rectangular, singular, or non-singular)
     * @throws IllegalArgumentException if matrix is null
     */
    public LUDecomposition(Matrix matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }

        // Deep copy the matrix to ensure immutability
        this.lu = matrix.getArrayCopy();
        this.rowCount = matrix.getRowCount();
        this.columnCount = matrix.getColumnCount();
        this.pivot = new int[rowCount];

        // Initialize pivot vector to identity permutation
        for (int i = 0; i < rowCount; i++) {
            pivot[i] = i;
        }

        int currentPivotSign = 1;
        double[] columnBuffer = new double[rowCount];

        // Main decomposition loop (over columns)
        for (int j = 0; j < columnCount; j++) {

            // Copy j-th column to local buffer for cache efficiency
            for (int i = 0; i < rowCount; i++) {
                columnBuffer[i] = lu[i][j];
            }

            // Apply previous transformations to current column
            for (int i = 0; i < rowCount; i++) {
                double[] luRow = lu[i];

                // Compute dot product of L[i,0:min(i,j)] with U[0:min(i,j),j]
                int kmax = Math.min(i, j);
                double dotProduct = 0.0;
                for (int k = 0; k < kmax; k++) {
                    dotProduct += luRow[k] * columnBuffer[k];
                }

                // Update both the column buffer and the LU array
                luRow[j] = columnBuffer[i] -= dotProduct;
            }

            // Find pivot (row with largest absolute value in current column)
            int pivotRow = j;
            for (int i = j + 1; i < rowCount; i++) {
                if (Math.abs(columnBuffer[i]) > Math.abs(columnBuffer[pivotRow])) {
                    pivotRow = i;
                }
            }

            // Exchange rows if necessary
            if (pivotRow != j) {
                for (int k = 0; k < columnCount; k++) {
                    double temp = lu[pivotRow][k];
                    lu[pivotRow][k] = lu[j][k];
                    lu[j][k] = temp;
                }
                int tempPivot = pivot[pivotRow];
                pivot[pivotRow] = pivot[j];
                pivot[j] = tempPivot;
                currentPivotSign = -currentPivotSign;
            }

            // Compute multipliers for this column
            if (j < rowCount && lu[j][j] != 0.0) {
                for (int i = j + 1; i < rowCount; i++) {
                    lu[i][j] /= lu[j][j];
                }
            }
        }

        this.pivotSign = currentPivotSign;
    }

    /**
     * Returns the lower triangular factor L.
     * <p>
     * L is a unit lower triangular matrix (all diagonal elements equal 1.0).
     * For an m-by-n original matrix with m ≥ n, L has dimensions m-by-n.
     * </p>
     * <p>
     * <b>Time Complexity:</b> O(m*n) to construct the matrix
     * </p>
     *
     * @return a new Matrix containing the lower triangular factor
     */
    public Matrix getL() {
        Matrix lMatrix = new Matrix(rowCount, columnCount);
        double[][] l = lMatrix.getElements();

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                if (i > j) {
                    l[i][j] = lu[i][j];
                } else if (i == j) {
                    l[i][j] = 1.0;
                } else {
                    l[i][j] = 0.0;
                }
            }
        }

        return lMatrix;
    }

    /**
     * Returns the upper triangular factor U.
     * <p>
     * U is an upper triangular matrix with dimensions n-by-n, where n is the number
     * of columns in the original matrix.
     * </p>
     * <p>
     * <b>Time Complexity:</b> O(n²) to construct the matrix
     * </p>
     *
     * @return a new Matrix containing the upper triangular factor
     */
    public Matrix getU() {
        Matrix uMatrix = new Matrix(columnCount, columnCount);
        double[][] u = uMatrix.getElements();

        for (int i = 0; i < columnCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                if (i <= j) {
                    u[i][j] = lu[i][j];
                } else {
                    u[i][j] = 0.0;
                }
            }
        }

        return uMatrix;
    }

    /**
     * Returns a copy of the pivot permutation vector.
     * <p>
     * The pivot vector records which original row is at each position after pivoting.
     * For example, if pivot[2] = 5, then the row currently at position 2 was originally
     * at position 5 in the input matrix.
     * </p>
     *
     * @return a new array containing the pivot permutation indices
     */
    public int[] getPivot() {
        return Arrays.copyOf(pivot, rowCount);
    }

    /**
     * Returns the pivot permutation vector as a double array.
     * <p>
     * This is a convenience method that converts the integer pivot indices to doubles.
     * </p>
     *
     * @return a new double array containing the pivot permutation indices
     */
    public double[] getDoublePivot() {
        double[] doublePivot = new double[rowCount];
        for (int i = 0; i < rowCount; i++) {
            doublePivot[i] = pivot[i];
        }
        return doublePivot;
    }

    /**
     * Determines whether the matrix is nonsingular (invertible).
     * <p>
     * A matrix is nonsingular if and only if all diagonal elements of U are non-zero.
     * Only square matrices can be nonsingular; this method will return false for
     * non-square matrices.
     * </p>
     * <p>
     * <b>Note:</b> This method uses exact equality comparison with 0.0. For matrices
     * with very small diagonal elements, numerical precision issues may affect the result.
     * </p>
     * <p>
     * <b>Time Complexity:</b> O(n) where n is the number of columns
     * </p>
     *
     * @return {@code true} if the matrix is square and all diagonal elements of U are non-zero,
     * {@code false} otherwise
     */
    public boolean isNonsingular() {
        for (int j = 0; j < columnCount; j++) {
            if (lu[j][j] == 0.0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes the determinant of the original matrix.
     * <p>
     * The determinant is computed as the product of the diagonal elements of U,
     * multiplied by the pivot sign (±1). This is significantly more efficient
     * than computing the determinant directly.
     * </p>
     * <p>
     * <b>Time Complexity:</b> O(n) where n is the number of columns
     * </p>
     *
     * @return the determinant of the original matrix
     * @throws IllegalArgumentException if the matrix is not square
     */
    public double getDeterminant() {
        if (rowCount != columnCount) {
            throw new IllegalArgumentException(
                    "Determinant requires a square matrix, got " + rowCount + "x" + columnCount
            );
        }

        double determinant = pivotSign;
        for (int j = 0; j < columnCount; j++) {
            determinant *= lu[j][j];
        }
        return determinant;
    }

    /**
     * Solves the system of linear equations A*X = B using the LU decomposition.
     * <p>
     * Given the matrix A (from which this decomposition was computed) and a right-hand
     * side matrix B, this method computes the solution matrix X such that A*X = B.
     * </p>
     * <p>
     * The solution is computed in two steps:
     * </p>
     * <ol>
     *     <li>Solve L*Y = B(piv,:) for Y using forward substitution</li>
     *     <li>Solve U*X = Y for X using backward substitution</li>
     * </ol>
     * <p>
     * <b>Time Complexity:</b> O(n²*k) where n is the dimension and k is the number of
     * columns in B
     * </p>
     *
     * @param b the right-hand side matrix with the same number of rows as A and any number of columns
     * @return the solution matrix X such that A*X = B
     * @throws IllegalArgumentException if B has a different number of rows than A or if the matrix is singular (not invertible)
     */
    public Matrix solve(Matrix b) {
        if (b.getRowCount() != rowCount) {
            throw new IllegalArgumentException(
                    "Matrix row dimensions must agree. Expected " + rowCount + " rows, got " + b.getRowCount()
            );
        }
        if (!isNonsingular()) {
            throw new IllegalArgumentException(
                    "Cannot solve system: matrix is singular (determinant = 0)"
            );
        }

        // Copy right-hand side with pivoting applied
        int solutionColumns = b.getColumnCount();
        Matrix xMatrix = b.getMatrix(pivot, 0, solutionColumns - 1);
        double[][] x = xMatrix.getElements();

        // Solve L*Y = B(piv,:) using forward substitution
        for (int k = 0; k < columnCount; k++) {
            for (int i = k + 1; i < columnCount; i++) {
                for (int j = 0; j < solutionColumns; j++) {
                    x[i][j] -= x[k][j] * lu[i][k];
                }
            }
        }

        // Solve U*X = Y using backward substitution
        for (int k = columnCount - 1; k >= 0; k--) {
            for (int j = 0; j < solutionColumns; j++) {
                x[k][j] /= lu[k][k];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < solutionColumns; j++) {
                    x[i][j] -= x[k][j] * lu[i][k];
                }
            }
        }

        return xMatrix;
    }
}
