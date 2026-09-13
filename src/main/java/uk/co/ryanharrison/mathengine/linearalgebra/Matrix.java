package uk.co.ryanharrison.mathengine.linearalgebra;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable implementation of a two-dimensional matrix with support for linear algebra operations.
 * <p>
 * A matrix is a rectangular array of numbers arranged in rows and columns. This implementation
 * provides comprehensive support for matrix arithmetic, decompositions, solving systems of linear
 * equations, and various matrix transformations.
 * </p>
 *
 * <h2>Key Properties:</h2>
 * <ul>
 *     <li><b>Immutability</b>: All operations return new Matrix instances; original matrices are never modified</li>
 *     <li><b>Size normalization</b>: Operations between matrices of different sizes automatically pad with zeros</li>
 *     <li><b>Type safety</b>: All dimensions are validated at construction time</li>
 * </ul>
 *
 * <h2>Supported Operations:</h2>
 * <ul>
 *     <li>Arithmetic: addition, subtraction, multiplication (scalar and matrix), division</li>
 *     <li>Element-wise operations: multiply, divide, power</li>
 *     <li>Matrix transformations: transpose, inverse, determinant, trace</li>
 *     <li>Linear algebra: LU decomposition, QR decomposition, solving linear systems</li>
 *     <li>Matrix properties: identity test, symmetry test, norms</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Create matrices using factory methods
 * Matrix identity = Matrix.identity(3);
 * Matrix zeros = Matrix.ofSize(3, 4);
 * Matrix filled = Matrix.filled(2, 2, 5.0);
 *
 * // Create from arrays
 * Matrix m1 = Matrix.of(new double[][] {
 *     {1, 2, 3},
 *     {4, 5, 6}
 * });
 *
 * Matrix columnVector = Matrix.of(new double[] {1, 2, 3});
 *
 * // Matrix arithmetic
 * Matrix sum = m1.add(identity);
 * Matrix product = m1.multiply(m2);
 * Matrix scaled = m1.multiply(2.5);
 *
 * // Linear algebra operations
 * Matrix transposed = m1.transpose();
 * double det = m1.determinant();
 * Matrix inv = m1.inverse();
 *
 * // Solve A*X = B
 * Matrix solution = A.solve(B);
 *
 * // Decompositions
 * LUDecomposition lu = m1.getLUDecomposition();
 * QRDecomposition qr = m1.getQRDecomposition();
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class Matrix implements Cloneable {
    /**
     * The elements of this matrix, stored as a 2D array where elements[i][j]
     * represents the element at row i and column j.
     */
    private final double[][] elements;

    /**
     * The number of rows in this matrix.
     */
    private final int rows;

    /**
     * The number of columns in this matrix.
     */
    private final int columns;

    // ==================== Factory Methods ====================

    /**
     * Creates a matrix from a 2D array of elements.
     * <p>
     * All rows must have the same length. The input array is deep-copied to ensure immutability.
     * </p>
     *
     * @param elements the 2D array of matrix elements
     * @return a new Matrix containing a copy of the provided elements
     * @throws IllegalArgumentException if the array is empty, null, or rows have different lengths
     */
    public static Matrix of(double[][] elements) {
        if (elements == null || elements.length == 0) {
            throw new IllegalArgumentException("Matrix elements cannot be null or empty");
        }
        return new Matrix(elements);
    }

    /**
     * Creates a column vector from a 1D array.
     * <p>
     * The resulting matrix will have dimensions n×1, where n is the length of the vector.
     * </p>
     *
     * @param vector the array of values to form a column vector
     * @return a new Matrix representing a column vector
     * @throws IllegalArgumentException if the vector is null or empty
     */
    public static Matrix of(double[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Vector cannot be null or empty");
        }
        return new Matrix(vector);
    }

    /**
     * Creates a row matrix from a Vector.
     * <p>
     * The resulting matrix will have dimensions 1×n, where n is the size of the vector.
     * </p>
     *
     * @param v the vector to convert to a row matrix
     * @return a new Matrix representing the vector as a row
     * @throws IllegalArgumentException if the vector is null
     */
    public static Matrix of(Vector v) {
        if (v == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        return new Matrix(v);
    }

    /**
     * Creates a zero matrix of the specified dimensions.
     * <p>
     * All elements are initialized to 0.0.
     * </p>
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @return a new zero Matrix with the specified dimensions
     * @throws IllegalArgumentException if rows or cols is negative
     */
    public static Matrix ofSize(int rows, int cols) {
        if (rows < 0 || cols < 0) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimensions must be non-negative, got: rows=%d, cols=%d", rows, cols));
        }
        return new Matrix(rows, cols);
    }

    /**
     * Creates a square zero matrix of the specified dimension.
     * <p>
     * All elements are initialized to 0.0. This is equivalent to {@code ofSize(n, n)}.
     * </p>
     *
     * @param n the dimension (both rows and columns)
     * @return a new square zero Matrix of dimension n×n
     * @throws IllegalArgumentException if n is negative
     */
    public static Matrix square(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Matrix dimension must be non-negative, got: " + n);
        }
        return new Matrix(n);
    }

    /**
     * Creates a matrix of the specified dimensions filled with a constant value.
     * <p>
     * All elements are initialized to the specified value.
     * </p>
     *
     * @param rows  the number of rows
     * @param cols  the number of columns
     * @param value the value to fill all elements with
     * @return a new Matrix filled with the specified value
     * @throws IllegalArgumentException if rows or cols is negative
     */
    public static Matrix filled(int rows, int cols, double value) {
        if (rows < 0 || cols < 0) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimensions must be non-negative, got: rows=%d, cols=%d", rows, cols));
        }
        return new Matrix(rows, cols, value);
    }

    /**
     * Creates an n×n identity matrix.
     * <p>
     * The identity matrix has ones on the main diagonal and zeros elsewhere.
     * Mathematically, for any matrix A of compatible dimensions: A × I = I × A = A
     * </p>
     *
     * @param n the dimension of the identity matrix
     * @return a new n×n identity Matrix
     * @throws IllegalArgumentException if n is negative
     */
    public static Matrix identity(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Matrix dimension must be non-negative, got: " + n);
        }
        double[][] elements = new double[n][n];
        for (int i = 0; i < n; i++) {
            elements[i][i] = 1.0;
        }
        return new Matrix(elements, n, n);
    }

    /**
     * Creates a matrix of the specified dimensions with random values.
     * <p>
     * Each element is initialized to a random value in the range [0.0, 1.0) using {@link Math#random()}.
     * </p>
     *
     * @param rows the number of rows
     * @param cols the number of columns
     * @return a new Matrix filled with random values
     * @throws IllegalArgumentException if rows or cols is negative
     */
    public static Matrix random(int rows, int cols) {
        if (rows < 0 || cols < 0) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimensions must be non-negative, got: rows=%d, cols=%d", rows, cols));
        }
        double[][] elements = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                elements[i][j] = Math.random();
            }
        }
        return new Matrix(elements, rows, cols);
    }

    // ==================== Constructors ====================

    /**
     * Package-private constructor from internal representation.
     * Does NOT copy the array - used internally where we already have a fresh array.
     */
    Matrix(double[][] elements, int rows, int cols) {
        this.elements = elements;
        this.rows = rows;
        this.columns = cols;
    }

    /**
     * Package-private constructor for a single scalar value.
     */
    Matrix(double d) {
        this.rows = 1;
        this.columns = 1;
        this.elements = new double[1][1];
        this.elements[0][0] = d;
    }

    /**
     * Package-private constructor from flat array with specified row count.
     */
    Matrix(double[] vals, int m) {
        if (vals == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }
        this.rows = m;
        this.columns = m != 0 ? vals.length / m : 0;
        if (m * columns != vals.length) {
            throw new IllegalArgumentException(
                    String.format("Array length (%d) must be a multiple of m (%d)", vals.length, m));
        }
        this.elements = new double[m][columns];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < columns; j++) {
                this.elements[i][j] = vals[i + j * m];
            }
        }
    }

    /**
     * Package-private constructor from 1D array (column vector).
     */
    Matrix(double[] vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        this.rows = vector.length;
        this.columns = 1;
        this.elements = new double[rows][1];
        for (int i = 0; i < rows; i++) {
            this.elements[i][0] = vector[i];
        }
    }

    /**
     * Package-private constructor from 2D array with deep copy.
     */
    Matrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            throw new IllegalArgumentException("Matrix cannot be null or empty");
        }
        this.rows = matrix.length;
        this.columns = matrix[0].length;

        // Validate all rows have same length
        for (int i = 0; i < rows; i++) {
            if (matrix[i].length != columns) {
                throw new IllegalArgumentException(
                        String.format("All rows must have the same length. Row 0 has %d columns, but row %d has %d columns",
                                columns, i, matrix[i].length));
            }
        }

        // Deep copy the array
        this.elements = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(matrix[i], 0, this.elements[i], 0, columns);
        }
    }

    /**
     * Package-private constructor for square zero matrix.
     */
    Matrix(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Matrix dimension must be non-negative, got: " + n);
        }
        this.rows = n;
        this.columns = n;
        this.elements = new double[n][n];
    }

    /**
     * Package-private constructor for rectangular zero matrix.
     */
    Matrix(int m, int n) {
        if (m < 0 || n < 0) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimensions must be non-negative, got: rows=%d, cols=%d", m, n));
        }
        this.rows = m;
        this.columns = n;
        this.elements = new double[m][n];
    }

    /**
     * Package-private constructor for filled matrix.
     */
    Matrix(int m, int n, double s) {
        if (m < 0 || n < 0) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimensions must be non-negative, got: rows=%d, cols=%d", m, n));
        }
        this.rows = m;
        this.columns = n;
        this.elements = new double[m][n];
        for (int i = 0; i < m; i++) {
            Arrays.fill(this.elements[i], s);
        }
    }

    /**
     * Package-private copy constructor.
     */
    Matrix(Matrix matrix) {
        this(matrix.elements);
    }

    /**
     * Package-private constructor from Vector (as row).
     */
    Matrix(Vector v) {
        if (v == null) {
            throw new IllegalArgumentException("Vector cannot be null");
        }
        this.rows = 1;
        this.columns = v.getSize();
        this.elements = new double[1][columns];
        for (int i = 0; i < columns; i++) {
            this.elements[0][i] = v.get(i);
        }
    }

    // ==================== Accessors ====================

    /**
     * Gets the element at the specified row and column.
     *
     * @param i the row index (0-based)
     * @param j the column index (0-based)
     * @return the element at position (i, j)
     * @throws ArrayIndexOutOfBoundsException if indices are out of bounds
     */
    public double get(int i, int j) {
        return elements[i][j];
    }

    /**
     * Gets the number of rows in this matrix.
     *
     * @return the row count
     */
    public int getRowCount() {
        return rows;
    }

    /**
     * Gets the number of columns in this matrix.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return columns;
    }

    /**
     * Returns the internal elements array.
     * <p>
     * <b>Warning:</b> This returns a reference to the internal array for performance reasons in
     * specific internal operations. Callers should NOT modify the returned array. For a safe copy,
     * use {@link #getArrayCopy()}.
     * </p>
     *
     * @return the internal elements array (do not modify)
     */
    double[][] getElements() {
        return this.elements;
    }

    /**
     * Returns a deep copy of the matrix elements as a 2D array.
     * <p>
     * The returned array is independent of this matrix and can be safely modified.
     * </p>
     *
     * @return a new 2D array containing a copy of all matrix elements
     */
    public double[][] getArrayCopy() {
        double[][] copy = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(elements[i], 0, copy[i], 0, columns);
        }
        return copy;
    }

    /**
     * Returns a copy of the matrix elements packed in column-major order.
     * <p>
     * The elements are stored in a 1D array where element (i,j) is at index i + j*rows.
     * This is the standard column-major (Fortran) ordering used by many numerical libraries.
     * </p>
     *
     * @return a 1D array of matrix elements in column-major order
     */
    public double[] getColumnPackedCopy() {
        double[] vals = new double[rows * columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                vals[i + j * rows] = elements[i][j];
            }
        }
        return vals;
    }

    /**
     * Returns a copy of the matrix elements packed in row-major order.
     * <p>
     * The elements are stored in a 1D array where element (i,j) is at index i*columns + j.
     * This is the standard row-major (C) ordering.
     * </p>
     *
     * @return a 1D array of matrix elements in row-major order
     */
    public double[] getRowPackedCopy() {
        double[] vals = new double[rows * columns];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(elements[i], 0, vals, i * columns, columns);
        }
        return vals;
    }

    // ==================== Matrix Properties ====================

    /**
     * Tests whether this matrix is square (same number of rows and columns).
     *
     * @return true if rows equals columns, false otherwise
     */
    public boolean isSquare() {
        return this.columns == this.rows;
    }

    /**
     * Tests whether this matrix is an identity matrix.
     * <p>
     * An identity matrix must be square with ones on the main diagonal and zeros elsewhere.
     * </p>
     *
     * @return true if this is an identity matrix, false otherwise
     * @throws UnsupportedOperationException if the matrix is not square
     */
    public boolean isIdentity() {
        if (!this.isSquare()) {
            throw new UnsupportedOperationException(
                    String.format("Cannot test identity on non-square matrix (dimensions: %d×%d)", rows, columns));
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double expected = (i == j) ? 1.0 : 0.0;
                if (Double.compare(this.elements[i][j], expected) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tests whether this matrix is symmetric.
     * <p>
     * A matrix is symmetric if it equals its transpose: A = A^T.
     * Only square matrices can be symmetric.
     * </p>
     *
     * @return true if this matrix equals its transpose, false otherwise
     */
    public boolean isSymmetric() {
        return this.equals(this.transpose());
    }

    /**
     * Computes the one-norm of this matrix.
     * <p>
     * The one-norm (or column-sum norm) is the maximum absolute column sum:
     * ||A||₁ = max_j Σ_i |a_ij|
     * </p>
     *
     * @return the one-norm of this matrix
     */
    public double norm1() {
        double maxSum = 0.0;
        for (int j = 0; j < columns; j++) {
            double columnSum = 0.0;
            for (int i = 0; i < rows; i++) {
                columnSum += Math.abs(elements[i][j]);
            }
            maxSum = Math.max(maxSum, columnSum);
        }
        return maxSum;
    }

    /**
     * Computes the trace of this matrix.
     * <p>
     * The trace is the sum of the diagonal elements: tr(A) = Σ_i a_ii.
     * For non-square matrices, this sums min(rows, columns) diagonal elements.
     * </p>
     *
     * @return the trace of this matrix
     */
    public double trace() {
        double t = 0.0;
        int diagonal = Math.min(rows, columns);
        for (int i = 0; i < diagonal; i++) {
            t += elements[i][i];
        }
        return t;
    }

    /**
     * Computes the sum of all elements in this matrix.
     *
     * @return the sum of all matrix elements
     */
    public double sum() {
        double sum = 0.0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                sum += elements[i][j];
            }
        }
        return sum;
    }

    /**
     * Computes the determinant of this matrix.
     * <p>
     * The determinant is only defined for square matrices. It is computed using LU decomposition.
     * </p>
     *
     * @return the determinant of this matrix
     * @throws IllegalArgumentException if the matrix is not square
     * @see LUDecomposition#getDeterminant()
     */
    public double determinant() {
        return new LUDecomposition(this).getDeterminant();
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Adds a scalar to every element of this matrix.
     *
     * @param d the scalar to add
     * @return a new Matrix with d added to each element
     */
    public Matrix add(double d) {
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = elements[i][j] + d;
            }
        }
        return new Matrix(result, rows, columns);
    }

    /**
     * Adds another matrix to this matrix element-wise.
     * <p>
     * If the matrices have different dimensions, they are normalized by padding with zeros
     * to match the largest dimensions.
     * </p>
     *
     * @param B the matrix to add
     * @return a new Matrix representing A + B
     */
    public Matrix add(Matrix B) {
        NormalizedPair normalized = normalizeMatrixSizes(B);
        Matrix A = normalized.first;
        Matrix BNorm = normalized.second;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = A.elements[i][j] + BNorm.elements[i][j];
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    /**
     * Adds a vector to each row of this matrix.
     * <p>
     * The vector is broadcast across all rows. If dimensions don't match,
     * normalization occurs by padding with zeros.
     * </p>
     *
     * @param v the vector to add to each row
     * @return a new Matrix with the vector added to each row
     */
    public Matrix add(Vector v) {
        NormalizedMatrixVector normalized = normalizeVectorSize(v);
        Matrix A = normalized.matrix;
        Vector vNorm = normalized.vector;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = A.elements[i][j] + vNorm.get(j);
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    /**
     * Subtracts a scalar from every element of this matrix.
     *
     * @param d the scalar to subtract
     * @return a new Matrix with d subtracted from each element
     */
    public Matrix subtract(double d) {
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = elements[i][j] - d;
            }
        }
        return new Matrix(result, rows, columns);
    }

    /**
     * Subtracts another matrix from this matrix element-wise.
     * <p>
     * If the matrices have different dimensions, they are normalized by padding with zeros
     * to match the largest dimensions.
     * </p>
     *
     * @param B the matrix to subtract
     * @return a new Matrix representing A - B
     */
    public Matrix subtract(Matrix B) {
        NormalizedPair normalized = normalizeMatrixSizes(B);
        Matrix A = normalized.first;
        Matrix BNorm = normalized.second;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = A.elements[i][j] - BNorm.elements[i][j];
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    /**
     * Subtracts a vector from each row of this matrix.
     * <p>
     * The vector is broadcast across all rows. If dimensions don't match,
     * normalization occurs by padding with zeros.
     * </p>
     *
     * @param v the vector to subtract from each row
     * @return a new Matrix with the vector subtracted from each row
     */
    public Matrix subtract(Vector v) {
        NormalizedMatrixVector normalized = normalizeVectorSize(v);
        Matrix A = normalized.matrix;
        Vector vNorm = normalized.vector;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = A.elements[i][j] - vNorm.get(j);
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    /**
     * Multiplies every element of this matrix by a scalar.
     *
     * @param d the scalar multiplier
     * @return a new Matrix with each element multiplied by d
     */
    public Matrix multiply(double d) {
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = elements[i][j] * d;
            }
        }
        return new Matrix(result, rows, columns);
    }

    /**
     * Performs linear algebraic matrix multiplication: A × B.
     * <p>
     * For this operation to be valid, the number of columns in A must equal the number of rows in B.
     * The resulting matrix has dimensions (A.rows × B.columns).
     * </p>
     *
     * @param B the matrix to multiply with
     * @return a new Matrix representing the matrix product A × B
     * @throws IllegalArgumentException if inner dimensions don't agree (this.columns != B.rows)
     */
    public Matrix multiply(Matrix B) {
        if (B.rows != columns) {
            throw new IllegalArgumentException(
                    String.format("Matrix inner dimensions must agree. Cannot multiply %d×%d by %d×%d",
                            rows, columns, B.rows, B.columns));
        }

        double[][] result = new double[rows][B.columns];
        double[] columnCache = new double[columns];

        for (int j = 0; j < B.columns; j++) {
            // Cache column j of B for better memory locality
            for (int k = 0; k < columns; k++) {
                columnCache[k] = B.elements[k][j];
            }

            for (int i = 0; i < rows; i++) {
                double sum = 0.0;
                for (int k = 0; k < columns; k++) {
                    sum += elements[i][k] * columnCache[k];
                }
                result[i][j] = sum;
            }
        }
        return new Matrix(result, rows, B.columns);
    }

    /**
     * Multiplies each element in a row by the corresponding vector element (element-wise).
     * <p>
     * The vector is broadcast across all rows. If dimensions don't match,
     * normalization occurs by padding with zeros.
     * </p>
     *
     * @param v the vector to multiply element-wise with each row
     * @return a new Matrix with element-wise multiplication applied
     */
    public Matrix multiply(Vector v) {
        NormalizedMatrixVector normalized = normalizeVectorSize(v);
        Matrix A = normalized.matrix;
        Vector vNorm = normalized.vector;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = A.elements[i][j] * vNorm.get(j);
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    /**
     * Divides every element of this matrix by a scalar.
     *
     * @param d the scalar divisor
     * @return a new Matrix with each element divided by d
     * @throws ArithmeticException if d is zero
     */
    public Matrix divide(double d) {
        if (Double.compare(d, 0.0) == 0) {
            throw new ArithmeticException("Cannot divide matrix by zero");
        }
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = elements[i][j] / d;
            }
        }
        return new Matrix(result, rows, columns);
    }

    /**
     * Performs element-wise division of this matrix by another matrix.
     * <p>
     * If the matrices have different dimensions, they are normalized by padding with zeros.
     * </p>
     *
     * @param B the matrix divisor
     * @return a new Matrix representing element-wise A / B
     */
    public Matrix divide(Matrix B) {
        NormalizedPair normalized = normalizeMatrixSizes(B);
        Matrix A = normalized.first;
        Matrix BNorm = normalized.second;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = A.elements[i][j] / BNorm.elements[i][j];
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    /**
     * Divides each element in a row by the corresponding vector element (element-wise).
     * <p>
     * The vector is broadcast across all rows. If dimensions don't match,
     * normalization occurs by padding with zeros.
     * </p>
     *
     * @param v the vector divisor
     * @return a new Matrix with element-wise division applied
     */
    public Matrix divide(Vector v) {
        NormalizedMatrixVector normalized = normalizeVectorSize(v);
        Matrix A = normalized.matrix;
        Vector vNorm = normalized.vector;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = A.elements[i][j] / vNorm.get(j);
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    // ==================== Element-wise Operations ====================

    /**
     * Performs element-wise multiplication (Hadamard product): A .* B.
     * <p>
     * Each element (i,j) in the result is A(i,j) * B(i,j).
     * Matrices must have the same dimensions.
     * </p>
     *
     * @param B the matrix to multiply element-wise
     * @return a new Matrix representing the element-wise product
     * @throws IllegalArgumentException if matrix dimensions don't match
     */
    public Matrix arrayMultiply(Matrix B) {
        checkMatrixDimensions(B);
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = elements[i][j] * B.elements[i][j];
            }
        }
        return new Matrix(result, rows, columns);
    }

    /**
     * Performs element-wise right division: A ./ B.
     * <p>
     * Each element (i,j) in the result is A(i,j) / B(i,j).
     * Matrices must have the same dimensions.
     * </p>
     *
     * @param B the matrix divisor
     * @return a new Matrix representing the element-wise quotient
     * @throws IllegalArgumentException if matrix dimensions don't match
     */
    public Matrix arrayRightDivide(Matrix B) {
        checkMatrixDimensions(B);
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = elements[i][j] / B.elements[i][j];
            }
        }
        return new Matrix(result, rows, columns);
    }

    /**
     * Performs element-wise left division: A .\ B (equivalent to B ./ A).
     * <p>
     * Each element (i,j) in the result is B(i,j) / A(i,j).
     * Matrices must have the same dimensions.
     * </p>
     *
     * @param B another matrix
     * @return a new Matrix representing B ./ A
     * @throws IllegalArgumentException if matrix dimensions don't match
     */
    public Matrix arrayLeftDivide(Matrix B) {
        checkMatrixDimensions(B);
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = B.elements[i][j] / elements[i][j];
            }
        }
        return new Matrix(result, rows, columns);
    }

    /**
     * Raises each element to the power of a scalar.
     *
     * @param d the exponent
     * @return a new Matrix with each element raised to power d
     */
    public Matrix pow(double d) {
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = Math.pow(elements[i][j], d);
            }
        }
        return new Matrix(result, rows, columns);
    }

    /**
     * Raises each element to the power of the corresponding element in another matrix.
     * <p>
     * If the matrices have different dimensions, they are normalized by padding with zeros.
     * </p>
     *
     * @param matrix the matrix of exponents
     * @return a new Matrix with element-wise exponentiation
     */
    public Matrix pow(Matrix matrix) {
        NormalizedPair normalized = normalizeMatrixSizes(matrix);
        Matrix A = normalized.first;
        Matrix BNorm = normalized.second;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = Math.pow(A.elements[i][j], BNorm.elements[i][j]);
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    /**
     * Raises each element in a row to the power of the corresponding vector element.
     * <p>
     * The vector is broadcast across all rows. If dimensions don't match,
     * normalization occurs by padding with zeros.
     * </p>
     *
     * @param v the vector of exponents
     * @return a new Matrix with element-wise exponentiation
     */
    public Matrix pow(Vector v) {
        NormalizedMatrixVector normalized = normalizeVectorSize(v);
        Matrix A = normalized.matrix;
        Vector vNorm = normalized.vector;

        double[][] result = new double[A.rows][A.columns];
        for (int i = 0; i < A.rows; i++) {
            for (int j = 0; j < A.columns; j++) {
                result[i][j] = Math.pow(A.elements[i][j], vNorm.get(j));
            }
        }
        return new Matrix(result, A.rows, A.columns);
    }

    /**
     * Returns the unary minus of this matrix (negates all elements).
     *
     * @return a new Matrix with all elements negated
     */
    public Matrix uminus() {
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = -elements[i][j];
            }
        }
        return new Matrix(result, rows, columns);
    }

    // ==================== Matrix Transformations ====================

    /**
     * Returns the transpose of this matrix.
     * <p>
     * The transpose A^T has dimensions (columns × rows) where element (i,j) of A^T
     * equals element (j,i) of A.
     * </p>
     *
     * @return a new Matrix representing the transpose
     */
    public Matrix transpose() {
        double[][] result = new double[columns][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[j][i] = elements[i][j];
            }
        }
        return new Matrix(result, columns, rows);
    }

    /**
     * Computes the inverse or pseudoinverse of this matrix.
     * <p>
     * For square matrices, returns the matrix inverse A^(-1) such that A × A^(-1) = I.
     * For non-square matrices, returns the pseudoinverse (Moore-Penrose inverse).
     * </p>
     *
     * @return the inverse (if square) or pseudoinverse (otherwise)
     * @throws RuntimeException if the matrix is singular (non-invertible)
     * @see #solve(Matrix)
     */
    public Matrix inverse() {
        return solve(identity(rows));
    }

    // ==================== Linear Algebra Operations ====================

    /**
     * Solves the linear system A*X = B.
     * <p>
     * For square matrices, uses LU decomposition to find the unique solution.
     * For non-square matrices, uses QR decomposition to find the least-squares solution.
     * </p>
     *
     * @param B the right-hand side matrix
     * @return the solution matrix X
     * @throws RuntimeException if the matrix is singular or the system has no solution
     * @see LUDecomposition#solve(Matrix)
     * @see QRDecomposition#solve(Matrix)
     */
    public Matrix solve(Matrix B) {
        return rows == columns
                ? new LUDecomposition(this).solve(B)
                : new QRDecomposition(this).solve(B);
    }

    /**
     * Returns the LU decomposition of this matrix.
     * <p>
     * The LU decomposition factors the matrix as A = L*U where L is lower triangular
     * and U is upper triangular. This is useful for solving linear systems and computing
     * determinants.
     * </p>
     *
     * @return a new LUDecomposition object
     * @see LUDecomposition
     */
    public LUDecomposition getLUDecomposition() {
        return new LUDecomposition(this);
    }

    /**
     * Returns the QR decomposition of this matrix.
     * <p>
     * The QR decomposition factors the matrix as A = Q*R where Q is orthogonal
     * and R is upper triangular. This is useful for solving least-squares problems.
     * </p>
     *
     * @return a new QRDecomposition object
     * @see QRDecomposition
     */
    public QRDecomposition getQRDecomposition() {
        return new QRDecomposition(this);
    }

    // ==================== Submatrix Operations ====================

    /**
     * Extracts a submatrix.
     *
     * @param i0 the initial row index (inclusive)
     * @param i1 the final row index (inclusive)
     * @param j0 the initial column index (inclusive)
     * @param j1 the final column index (inclusive)
     * @return a new Matrix containing the specified submatrix
     * @throws ArrayIndexOutOfBoundsException if indices are invalid
     */
    public Matrix getMatrix(int i0, int i1, int j0, int j1) {
        if (i0 < 0 || i1 >= rows || j0 < 0 || j1 >= columns || i0 > i1 || j0 > j1) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("Invalid submatrix indices: [%d:%d, %d:%d] for %d×%d matrix",
                            i0, i1, j0, j1, rows, columns));
        }

        int subRows = i1 - i0 + 1;
        int subCols = j1 - j0 + 1;
        double[][] result = new double[subRows][subCols];

        for (int i = 0; i < subRows; i++) {
            System.arraycopy(elements[i0 + i], j0, result[i], 0, subCols);
        }
        return new Matrix(result, subRows, subCols);
    }

    /**
     * Extracts a submatrix using row indices and column range.
     *
     * @param r  the array of row indices
     * @param j0 the initial column index (inclusive)
     * @param j1 the final column index (inclusive)
     * @return a new Matrix containing the specified submatrix
     * @throws ArrayIndexOutOfBoundsException if indices are invalid
     */
    public Matrix getMatrix(int[] r, int j0, int j1) {
        if (r == null || r.length == 0) {
            throw new IllegalArgumentException("Row index array cannot be null or empty");
        }
        if (j0 < 0 || j1 >= columns || j0 > j1) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("Invalid column range: [%d:%d] for matrix with %d columns", j0, j1, columns));
        }

        int subCols = j1 - j0 + 1;
        double[][] result = new double[r.length][subCols];

        for (int i = 0; i < r.length; i++) {
            if (r[i] < 0 || r[i] >= rows) {
                throw new ArrayIndexOutOfBoundsException(
                        String.format("Row index %d is out of bounds for matrix with %d rows", r[i], rows));
            }
            System.arraycopy(elements[r[i]], j0, result[i], 0, subCols);
        }
        return new Matrix(result, r.length, subCols);
    }

    // ==================== Utility Methods ====================

    /**
     * Creates a deep copy of this matrix.
     *
     * @return a new Matrix with the same elements
     */
    public Matrix copy() {
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(elements[i], 0, result[i], 0, columns);
        }
        return new Matrix(result, rows, columns);
    }

    @Override
    public Object clone() {
        return this.copy();
    }

    /**
     * Validates that another matrix has the same dimensions as this matrix.
     *
     * @param B the matrix to check
     * @throws IllegalArgumentException if dimensions don't match
     */
    private void checkMatrixDimensions(Matrix B) {
        if (B.rows != rows || B.columns != columns) {
            throw new IllegalArgumentException(
                    String.format("Matrix dimensions must agree. This: %d×%d, other: %d×%d",
                            rows, columns, B.rows, B.columns));
        }
    }

    /**
     * Normalizes two matrices to have the same dimensions by padding with zeros.
     * Returns new matrices if resizing is needed; otherwise returns the originals.
     *
     * @param B the second matrix
     * @return a pair of normalized matrices
     */
    private NormalizedPair normalizeMatrixSizes(Matrix B) {
        if (this.rows == B.rows && this.columns == B.columns) {
            return new NormalizedPair(this, B);
        }

        int maxRows = Math.max(this.rows, B.rows);
        int maxCols = Math.max(this.columns, B.columns);

        Matrix first = (this.rows == maxRows && this.columns == maxCols)
                ? this
                : resizeTo(maxRows, maxCols);

        Matrix second = (B.rows == maxRows && B.columns == maxCols)
                ? B
                : B.resizeTo(maxRows, maxCols);

        return new NormalizedPair(first, second);
    }

    /**
     * Normalizes this matrix and a vector to have compatible dimensions.
     * The vector size is matched to the matrix column count.
     *
     * @param v the vector
     * @return a normalized matrix-vector pair
     */
    private NormalizedMatrixVector normalizeVectorSize(Vector v) {
        if (this.columns == v.getSize()) {
            return new NormalizedMatrixVector(this, v);
        }

        int maxSize = Math.max(this.columns, v.getSize());

        Matrix matrix = (this.columns == maxSize) ? this : resizeTo(this.rows, maxSize);
        Vector vector = (v.getSize() == maxSize) ? v : resizeVector(v, maxSize);

        return new NormalizedMatrixVector(matrix, vector);
    }

    /**
     * Creates a new matrix with the specified dimensions, padding with zeros if larger.
     */
    private Matrix resizeTo(int newRows, int newCols) {
        double[][] result = new double[newRows][newCols];
        int copyRows = Math.min(this.rows, newRows);
        int copyCols = Math.min(this.columns, newCols);

        for (int i = 0; i < copyRows; i++) {
            System.arraycopy(this.elements[i], 0, result[i], 0, copyCols);
        }

        return new Matrix(result, newRows, newCols);
    }

    /**
     * Creates a new vector with the specified size, padding with zeros if larger.
     */
    private Vector resizeVector(Vector v, int newSize) {
        double[] result = new double[newSize];
        int copySize = Math.min(v.getSize(), newSize);

        for (int i = 0; i < copySize; i++) {
            result[i] = v.get(i);
        }

        return new Vector(result);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Matrix other)) {
            return false;
        }

        if (this.rows != other.rows || this.columns != other.columns) {
            return false;
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (Double.compare(this.elements[i][j], other.elements[i][j]) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, columns, Arrays.deepHashCode(elements));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < rows; i++) {
            if (i > 0) {
                builder.append('\n');
            }
            for (int j = 0; j < columns; j++) {
                if (j > 0) {
                    builder.append('\t');
                }
                builder.append(elements[i][j]);
            }
        }

        return builder.toString();
    }

    // ==================== Helper Classes ====================

    /**
     * Helper class to hold a pair of normalized matrices.
     */
    private record NormalizedPair(Matrix first, Matrix second) {
    }

    /**
     * Helper class to hold a normalized matrix-vector pair.
     */
    private record NormalizedMatrixVector(Matrix matrix, Vector vector) {
    }

    // ==================== Deprecated Static Method ====================

}
