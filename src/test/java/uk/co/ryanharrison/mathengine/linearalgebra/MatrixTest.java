package uk.co.ryanharrison.mathengine.linearalgebra;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for the {@link Matrix} class.
 * <p>
 * Tests cover all factory methods, arithmetic operations, matrix properties, transformations,
 * linear algebra operations, and edge cases.
 * </p>
 */
class MatrixTest {
    private static final double TOLERANCE = 1e-9;
    private static final double RELAXED_TOLERANCE = 1e-6;

    // ==================== Factory Methods ====================

    @Test
    void ofCreatesMatrixFrom2DArray() {
        double[][] array = {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        };
        Matrix matrix = Matrix.of(array);

        assertThat(matrix.getRowCount()).isEqualTo(2);
        assertThat(matrix.getColumnCount()).isEqualTo(3);
        assertThat(matrix.get(0, 0)).isEqualTo(1.0);
        assertThat(matrix.get(0, 1)).isEqualTo(2.0);
        assertThat(matrix.get(1, 2)).isEqualTo(6.0);
    }

    @Test
    void ofPerformsDeepCopy() {
        double[][] array = {{1.0, 2.0}, {3.0, 4.0}};
        Matrix matrix = Matrix.of(array);

        // Modify original array
        array[0][0] = 999.0;

        // Matrix should be unchanged (defensive copy)
        assertThat(matrix.get(0, 0)).isEqualTo(1.0);
    }

    @Test
    void ofRejectsNullArray() {
        assertThatThrownBy(() -> Matrix.of((double[][]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void ofRejectsEmptyArray() {
        assertThatThrownBy(() -> Matrix.of(new double[0][0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void ofRejectsIrregularArray() {
        double[][] irregular = {
                {1.0, 2.0, 3.0},
                {4.0, 5.0}  // Different length
        };

        assertThatThrownBy(() -> Matrix.of(irregular))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same length");
    }

    @Test
    void ofCreatesColumnVectorFrom1DArray() {
        double[] vector = {1.0, 2.0, 3.0};
        Matrix matrix = Matrix.of(vector);

        assertThat(matrix.getRowCount()).isEqualTo(3);
        assertThat(matrix.getColumnCount()).isEqualTo(1);
        assertThat(matrix.get(0, 0)).isEqualTo(1.0);
        assertThat(matrix.get(1, 0)).isEqualTo(2.0);
        assertThat(matrix.get(2, 0)).isEqualTo(3.0);
    }

    @Test
    void ofRejectsNull1DArray() {
        assertThatThrownBy(() -> Matrix.of((double[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void ofRejectsEmpty1DArray() {
        assertThatThrownBy(() -> Matrix.of(new double[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void ofCreatesRowMatrixFromVector() {
        Vector v = new Vector(new double[]{1.0, 2.0, 3.0});
        Matrix matrix = Matrix.of(v);

        assertThat(matrix.getRowCount()).isEqualTo(1);
        assertThat(matrix.getColumnCount()).isEqualTo(3);
        assertThat(matrix.get(0, 0)).isEqualTo(1.0);
        assertThat(matrix.get(0, 1)).isEqualTo(2.0);
        assertThat(matrix.get(0, 2)).isEqualTo(3.0);
    }

    @Test
    void ofRejectsNullVector() {
        assertThatThrownBy(() -> Matrix.of((Vector) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 1",
            "3, 4",
            "5, 2"
    })
    void ofSizeCreatesZeroMatrix(int rows, int cols) {
        Matrix matrix = Matrix.ofSize(rows, cols);

        assertThat(matrix.getRowCount()).isEqualTo(rows);
        assertThat(matrix.getColumnCount()).isEqualTo(cols);

        // Verify all elements are zero
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                assertThat(matrix.get(i, j)).isEqualTo(0.0);
            }
        }
    }

    @Test
    void ofSizeRejectsNegativeDimensions() {
        assertThatThrownBy(() -> Matrix.ofSize(-1, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");

        assertThatThrownBy(() -> Matrix.ofSize(3, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "3",
            "5"
    })
    void squareCreatesSquareZeroMatrix(int n) {
        Matrix matrix = Matrix.square(n);

        assertThat(matrix.getRowCount()).isEqualTo(n);
        assertThat(matrix.getColumnCount()).isEqualTo(n);
        assertThat(matrix.isSquare()).isTrue();

        // Verify all elements are zero
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                assertThat(matrix.get(i, j)).isEqualTo(0.0);
            }
        }
    }

    @Test
    void squareRejectsNegativeDimension() {
        assertThatThrownBy(() -> Matrix.square(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @ParameterizedTest
    @CsvSource({
            "2, 3, 5.0",
            "3, 3, -2.5",
            "1, 1, 0.0",
            "4, 2, 1.5"
    })
    void filledCreatesMatrixWithConstantValue(int rows, int cols, double value) {
        Matrix matrix = Matrix.filled(rows, cols, value);

        assertThat(matrix.getRowCount()).isEqualTo(rows);
        assertThat(matrix.getColumnCount()).isEqualTo(cols);

        // Verify all elements equal the fill value
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                assertThat(matrix.get(i, j)).isEqualTo(value);
            }
        }
    }

    @Test
    void filledRejectsNegativeDimensions() {
        assertThatThrownBy(() -> Matrix.filled(-1, 3, 5.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @ParameterizedTest
    @CsvSource({
            "1",
            "2",
            "3",
            "5"
    })
    void identityCreatesIdentityMatrix(int n) {
        Matrix matrix = Matrix.identity(n);

        assertThat(matrix.getRowCount()).isEqualTo(n);
        assertThat(matrix.getColumnCount()).isEqualTo(n);

        // Verify diagonal is 1.0 and off-diagonal is 0.0
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double expected = (i == j) ? 1.0 : 0.0;
                assertThat(matrix.get(i, j)).isEqualTo(expected);
            }
        }
    }

    @Test
    void identityRejectsNegativeDimension() {
        assertThatThrownBy(() -> Matrix.identity(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void randomCreatesMatrixWithRandomValues() {
        Matrix matrix = Matrix.random(3, 4);

        assertThat(matrix.getRowCount()).isEqualTo(3);
        assertThat(matrix.getColumnCount()).isEqualTo(4);

        // Verify all values are in [0, 1)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                double value = matrix.get(i, j);
                assertThat(value).isGreaterThanOrEqualTo(0.0);
                assertThat(value).isLessThan(1.0);
            }
        }
    }

    @Test
    void randomRejectsNegativeDimensions() {
        assertThatThrownBy(() -> Matrix.random(-1, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    // ==================== Element Access ====================

    @Test
    void getReturnsCorrectElement() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });

        assertThat(matrix.get(0, 0)).isEqualTo(1.0);
        assertThat(matrix.get(0, 1)).isEqualTo(2.0);
        assertThat(matrix.get(0, 2)).isEqualTo(3.0);
        assertThat(matrix.get(1, 0)).isEqualTo(4.0);
        assertThat(matrix.get(1, 1)).isEqualTo(5.0);
        assertThat(matrix.get(1, 2)).isEqualTo(6.0);
    }

    @Test
    void getThrowsExceptionForInvalidIndices() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThatThrownBy(() -> matrix.get(2, 0))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThatThrownBy(() -> matrix.get(0, 2))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        assertThatThrownBy(() -> matrix.get(-1, 0))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void getArrayCopyReturnsDeepCopy() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        double[][] copy = matrix.getArrayCopy();

        // Modify copy
        copy[0][0] = 999.0;

        // Original matrix should be unchanged
        assertThat(matrix.get(0, 0)).isEqualTo(1.0);
    }

    @Test
    void getColumnPackedCopyReturnsCorrectOrdering() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });

        double[] packed = matrix.getColumnPackedCopy();

        // Column-major order: column 0, then column 1, then column 2
        assertThat(packed).containsExactly(1.0, 4.0, 2.0, 5.0, 3.0, 6.0);
    }

    @Test
    void getRowPackedCopyReturnsCorrectOrdering() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });

        double[] packed = matrix.getRowPackedCopy();

        // Row-major order: row 0, then row 1
        assertThat(packed).containsExactly(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
    }

    // ==================== Arithmetic Operations (Scalar) ====================

    @Test
    void addScalarAddsToAllElements() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix result = matrix.add(5.0);

        assertThat(result.get(0, 0)).isEqualTo(6.0);
        assertThat(result.get(0, 1)).isEqualTo(7.0);
        assertThat(result.get(1, 0)).isEqualTo(8.0);
        assertThat(result.get(1, 1)).isEqualTo(9.0);
    }

    @Test
    void subtractScalarSubtractsFromAllElements() {
        Matrix matrix = Matrix.of(new double[][]{{10.0, 8.0}, {6.0, 4.0}});
        Matrix result = matrix.subtract(3.0);

        assertThat(result.get(0, 0)).isEqualTo(7.0);
        assertThat(result.get(0, 1)).isEqualTo(5.0);
        assertThat(result.get(1, 0)).isEqualTo(3.0);
        assertThat(result.get(1, 1)).isEqualTo(1.0);
    }

    @Test
    void multiplyScalarScalesAllElements() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix result = matrix.multiply(2.5);

        assertThat(result.get(0, 0)).isEqualTo(2.5);
        assertThat(result.get(0, 1)).isEqualTo(5.0);
        assertThat(result.get(1, 0)).isEqualTo(7.5);
        assertThat(result.get(1, 1)).isEqualTo(10.0);
    }

    @Test
    void divideScalarDividesAllElements() {
        Matrix matrix = Matrix.of(new double[][]{{10.0, 20.0}, {30.0, 40.0}});
        Matrix result = matrix.divide(5.0);

        assertThat(result.get(0, 0)).isEqualTo(2.0);
        assertThat(result.get(0, 1)).isEqualTo(4.0);
        assertThat(result.get(1, 0)).isEqualTo(6.0);
        assertThat(result.get(1, 1)).isEqualTo(8.0);
    }

    @Test
    void divideByZeroThrowsException() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThatThrownBy(() -> matrix.divide(0.0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("divide");
    }

    // ==================== Arithmetic Operations (Matrix) ====================

    @Test
    void addMatrixPerformsElementWiseAddition() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{5.0, 6.0}, {7.0, 8.0}});
        Matrix result = A.add(B);

        assertThat(result.get(0, 0)).isEqualTo(6.0);
        assertThat(result.get(0, 1)).isEqualTo(8.0);
        assertThat(result.get(1, 0)).isEqualTo(10.0);
        assertThat(result.get(1, 1)).isEqualTo(12.0);
    }

    @Test
    void addMatrixNormalizesDifferentSizes() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{5.0}});
        Matrix result = A.add(B);

        // B is padded to match A's size with zeros
        assertThat(result.get(0, 0)).isEqualTo(6.0);   // 1 + 5
        assertThat(result.get(0, 1)).isEqualTo(2.0);   // 2 + 0
        assertThat(result.get(1, 0)).isEqualTo(3.0);   // 3 + 0
        assertThat(result.get(1, 1)).isEqualTo(4.0);   // 4 + 0
    }

    @Test
    void subtractMatrixPerformsElementWiseSubtraction() {
        Matrix A = Matrix.of(new double[][]{{10.0, 8.0}, {6.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix result = A.subtract(B);

        assertThat(result.get(0, 0)).isEqualTo(9.0);
        assertThat(result.get(0, 1)).isEqualTo(6.0);
        assertThat(result.get(1, 0)).isEqualTo(3.0);
        assertThat(result.get(1, 1)).isEqualTo(0.0);
    }

    @Test
    void subtractMatrixNormalizesDifferentSizes() {
        Matrix A = Matrix.of(new double[][]{{5.0, 6.0}, {7.0, 8.0}});
        Matrix B = Matrix.of(new double[][]{{1.0}});
        Matrix result = A.subtract(B);

        assertThat(result.get(0, 0)).isEqualTo(4.0);   // 5 - 1
        assertThat(result.get(0, 1)).isEqualTo(6.0);   // 6 - 0
        assertThat(result.get(1, 0)).isEqualTo(7.0);   // 7 - 0
        assertThat(result.get(1, 1)).isEqualTo(8.0);   // 8 - 0
    }

    @Test
    void divideMatrixPerformsElementWiseDivision() {
        Matrix A = Matrix.of(new double[][]{{10.0, 20.0}, {30.0, 40.0}});
        Matrix B = Matrix.of(new double[][]{{2.0, 4.0}, {5.0, 8.0}});
        Matrix result = A.divide(B);

        assertThat(result.get(0, 0)).isEqualTo(5.0);
        assertThat(result.get(0, 1)).isEqualTo(5.0);
        assertThat(result.get(1, 0)).isEqualTo(6.0);
        assertThat(result.get(1, 1)).isEqualTo(5.0);
    }

    // ==================== Arithmetic Operations (Vector) ====================

    @Test
    void addVectorBroadcastsAcrossRows() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });
        Vector v = new Vector(new double[]{10.0, 20.0, 30.0});
        Matrix result = matrix.add(v);

        assertThat(result.get(0, 0)).isEqualTo(11.0);
        assertThat(result.get(0, 1)).isEqualTo(22.0);
        assertThat(result.get(0, 2)).isEqualTo(33.0);
        assertThat(result.get(1, 0)).isEqualTo(14.0);
        assertThat(result.get(1, 1)).isEqualTo(25.0);
        assertThat(result.get(1, 2)).isEqualTo(36.0);
    }

    @Test
    void subtractVectorBroadcastsAcrossRows() {
        Matrix matrix = Matrix.of(new double[][]{
                {10.0, 20.0, 30.0},
                {40.0, 50.0, 60.0}
        });
        Vector v = new Vector(new double[]{1.0, 2.0, 3.0});
        Matrix result = matrix.subtract(v);

        assertThat(result.get(0, 0)).isEqualTo(9.0);
        assertThat(result.get(0, 1)).isEqualTo(18.0);
        assertThat(result.get(0, 2)).isEqualTo(27.0);
        assertThat(result.get(1, 0)).isEqualTo(39.0);
        assertThat(result.get(1, 1)).isEqualTo(48.0);
        assertThat(result.get(1, 2)).isEqualTo(57.0);
    }

    @Test
    void multiplyVectorPerformsElementWiseMultiplication() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });
        Vector v = new Vector(new double[]{2.0, 3.0, 4.0});
        Matrix result = matrix.multiply(v);

        assertThat(result.get(0, 0)).isEqualTo(2.0);
        assertThat(result.get(0, 1)).isEqualTo(6.0);
        assertThat(result.get(0, 2)).isEqualTo(12.0);
        assertThat(result.get(1, 0)).isEqualTo(8.0);
        assertThat(result.get(1, 1)).isEqualTo(15.0);
        assertThat(result.get(1, 2)).isEqualTo(24.0);
    }

    @Test
    void divideVectorPerformsElementWiseDivision() {
        Matrix matrix = Matrix.of(new double[][]{
                {10.0, 20.0, 30.0},
                {40.0, 50.0, 60.0}
        });
        Vector v = new Vector(new double[]{2.0, 4.0, 5.0});
        Matrix result = matrix.divide(v);

        assertThat(result.get(0, 0)).isEqualTo(5.0);
        assertThat(result.get(0, 1)).isEqualTo(5.0);
        assertThat(result.get(0, 2)).isEqualTo(6.0);
        assertThat(result.get(1, 0)).isEqualTo(20.0);
        assertThat(result.get(1, 1)).isEqualTo(12.5);
        assertThat(result.get(1, 2)).isEqualTo(12.0);
    }

    // ==================== Element-wise Operations ====================

    @Test
    void arrayMultiplyPerformsHadamardProduct() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{5.0, 6.0}, {7.0, 8.0}});
        Matrix result = A.arrayMultiply(B);

        assertThat(result.get(0, 0)).isEqualTo(5.0);
        assertThat(result.get(0, 1)).isEqualTo(12.0);
        assertThat(result.get(1, 0)).isEqualTo(21.0);
        assertThat(result.get(1, 1)).isEqualTo(32.0);
    }

    @Test
    void arrayMultiplyRejectsDifferentSizes() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{5.0}});

        assertThatThrownBy(() -> A.arrayMultiply(B))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dimensions must agree");
    }

    @Test
    void arrayRightDividePerformsElementWiseDivision() {
        Matrix A = Matrix.of(new double[][]{{10.0, 20.0}, {30.0, 40.0}});
        Matrix B = Matrix.of(new double[][]{{2.0, 4.0}, {5.0, 8.0}});
        Matrix result = A.arrayRightDivide(B);

        assertThat(result.get(0, 0)).isEqualTo(5.0);
        assertThat(result.get(0, 1)).isEqualTo(5.0);
        assertThat(result.get(1, 0)).isEqualTo(6.0);
        assertThat(result.get(1, 1)).isEqualTo(5.0);
    }

    @Test
    void arrayRightDivideRejectsDifferentSizes() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{5.0}});

        assertThatThrownBy(() -> A.arrayRightDivide(B))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dimensions must agree");
    }

    @Test
    void arrayLeftDividePerformsReverseDivision() {
        Matrix A = Matrix.of(new double[][]{{2.0, 4.0}, {5.0, 8.0}});
        Matrix B = Matrix.of(new double[][]{{10.0, 20.0}, {30.0, 40.0}});
        Matrix result = A.arrayLeftDivide(B);

        // Result is B ./ A
        assertThat(result.get(0, 0)).isEqualTo(5.0);
        assertThat(result.get(0, 1)).isEqualTo(5.0);
        assertThat(result.get(1, 0)).isEqualTo(6.0);
        assertThat(result.get(1, 1)).isEqualTo(5.0);
    }

    @Test
    void arrayLeftDivideRejectsDifferentSizes() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{5.0}});

        assertThatThrownBy(() -> A.arrayLeftDivide(B))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dimensions must agree");
    }

    @Test
    void powScalarRaisesElementsToScalarPower() {
        Matrix matrix = Matrix.of(new double[][]{{2.0, 3.0}, {4.0, 5.0}});
        Matrix result = matrix.pow(2.0);

        assertThat(result.get(0, 0)).isEqualTo(4.0);
        assertThat(result.get(0, 1)).isEqualTo(9.0);
        assertThat(result.get(1, 0)).isEqualTo(16.0);
        assertThat(result.get(1, 1)).isEqualTo(25.0);
    }

    @Test
    void powMatrixPerformsElementWiseExponentiation() {
        Matrix A = Matrix.of(new double[][]{{2.0, 3.0}, {4.0, 5.0}});
        Matrix B = Matrix.of(new double[][]{{2.0, 3.0}, {2.0, 2.0}});
        Matrix result = A.pow(B);

        assertThat(result.get(0, 0)).isEqualTo(4.0);    // 2^2
        assertThat(result.get(0, 1)).isEqualTo(27.0);   // 3^3
        assertThat(result.get(1, 0)).isEqualTo(16.0);   // 4^2
        assertThat(result.get(1, 1)).isEqualTo(25.0);   // 5^2
    }

    @Test
    void powVectorPerformsElementWiseExponentiation() {
        Matrix matrix = Matrix.of(new double[][]{
                {2.0, 3.0, 4.0},
                {5.0, 6.0, 7.0}
        });
        Vector v = new Vector(new double[]{2.0, 3.0, 2.0});
        Matrix result = matrix.pow(v);

        assertThat(result.get(0, 0)).isEqualTo(4.0);    // 2^2
        assertThat(result.get(0, 1)).isEqualTo(27.0);   // 3^3
        assertThat(result.get(0, 2)).isEqualTo(16.0);   // 4^2
        assertThat(result.get(1, 0)).isEqualTo(25.0);   // 5^2
        assertThat(result.get(1, 1)).isEqualTo(216.0);  // 6^3
        assertThat(result.get(1, 2)).isEqualTo(49.0);   // 7^2
    }

    @Test
    void uminusNegatesAllElements() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, -2.0}, {-3.0, 4.0}});
        Matrix result = matrix.uminus();

        assertThat(result.get(0, 0)).isEqualTo(-1.0);
        assertThat(result.get(0, 1)).isEqualTo(2.0);
        assertThat(result.get(1, 0)).isEqualTo(3.0);
        assertThat(result.get(1, 1)).isEqualTo(-4.0);
    }

    // ==================== Matrix Multiplication ====================

    @Test
    void multiplyMatrixPerformsLinearAlgebraMultiplication() {
        Matrix A = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });
        Matrix B = Matrix.of(new double[][]{
                {7.0, 8.0},
                {9.0, 10.0},
                {11.0, 12.0}
        });
        Matrix result = A.multiply(B);

        // Result should be 2x2
        assertThat(result.getRowCount()).isEqualTo(2);
        assertThat(result.getColumnCount()).isEqualTo(2);

        // [1*7 + 2*9 + 3*11, 1*8 + 2*10 + 3*12]
        // [4*7 + 5*9 + 6*11, 4*8 + 5*10 + 6*12]
        assertThat(result.get(0, 0)).isEqualTo(58.0);   // 7 + 18 + 33
        assertThat(result.get(0, 1)).isEqualTo(64.0);   // 8 + 20 + 36
        assertThat(result.get(1, 0)).isEqualTo(139.0);  // 28 + 45 + 66
        assertThat(result.get(1, 1)).isEqualTo(154.0);  // 32 + 50 + 72
    }

    @Test
    void multiplyMatrixWithIdentityProducesOriginal() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix I = Matrix.identity(2);
        Matrix result = A.multiply(I);

        assertThat(result).isEqualTo(A);
    }

    @Test
    void multiplySquareMatrices() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{5.0, 6.0}, {7.0, 8.0}});
        Matrix result = A.multiply(B);

        // [1*5 + 2*7, 1*6 + 2*8]
        // [3*5 + 4*7, 3*6 + 4*8]
        assertThat(result.get(0, 0)).isEqualTo(19.0);
        assertThat(result.get(0, 1)).isEqualTo(22.0);
        assertThat(result.get(1, 0)).isEqualTo(43.0);
        assertThat(result.get(1, 1)).isEqualTo(50.0);
    }

    @Test
    void multiplyRejectsIncompatibleDimensions() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}});  // 1x2
        Matrix B = Matrix.of(new double[][]{{3.0, 4.0}});  // 1x2

        // Cannot multiply 1x2 by 1x2 (inner dimensions don't match)
        assertThatThrownBy(() -> A.multiply(B))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inner dimensions");
    }

    // ==================== Matrix Properties ====================

    @Test
    void isSquareReturnsTrueForSquareMatrix() {
        assertThat(Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}}).isSquare()).isTrue();
        assertThat(Matrix.square(3).isSquare()).isTrue();
        assertThat(Matrix.identity(5).isSquare()).isTrue();
    }

    @Test
    void isSquareReturnsFalseForNonSquareMatrix() {
        assertThat(Matrix.of(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}}).isSquare()).isFalse();
        assertThat(Matrix.ofSize(3, 4).isSquare()).isFalse();
    }

    @Test
    void isIdentityReturnsTrueForIdentityMatrix() {
        assertThat(Matrix.identity(1).isIdentity()).isTrue();
        assertThat(Matrix.identity(2).isIdentity()).isTrue();
        assertThat(Matrix.identity(3).isIdentity()).isTrue();
    }

    @Test
    void isIdentityReturnsFalseForNonIdentityMatrix() {
        assertThat(Matrix.of(new double[][]{{1.0, 0.0}, {0.0, 2.0}}).isIdentity()).isFalse();
        assertThat(Matrix.of(new double[][]{{1.0, 1.0}, {0.0, 1.0}}).isIdentity()).isFalse();
        assertThat(Matrix.square(3).isIdentity()).isFalse();
    }

    @Test
    void isIdentityThrowsExceptionForNonSquareMatrix() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});

        assertThatThrownBy(() -> matrix.isIdentity())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("non-square");
    }

    @Test
    void isSymmetricReturnsTrueForSymmetricMatrix() {
        Matrix symmetric = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {2.0, 4.0, 5.0},
                {3.0, 5.0, 6.0}
        });

        assertThat(symmetric.isSymmetric()).isTrue();
    }

    @Test
    void isSymmetricReturnsTrueForIdentityMatrix() {
        assertThat(Matrix.identity(3).isSymmetric()).isTrue();
    }

    @Test
    void isSymmetricReturnsFalseForNonSymmetricMatrix() {
        Matrix nonSymmetric = Matrix.of(new double[][]{
                {1.0, 2.0},
                {3.0, 4.0}
        });

        assertThat(nonSymmetric.isSymmetric()).isFalse();
    }

    @Test
    void isSymmetricReturnsFalseForNonSquareMatrix() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}});
        assertThat(matrix.isSymmetric()).isFalse();
    }

    @Test
    void traceComputesSumOfDiagonal() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0},
                {7.0, 8.0, 9.0}
        });

        assertThat(matrix.trace()).isEqualTo(15.0);  // 1 + 5 + 9
    }

    @Test
    void traceWorksForNonSquareMatrix() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });

        // Sums min(rows, cols) diagonal elements
        assertThat(matrix.trace()).isEqualTo(6.0);  // 1 + 5
    }

    @Test
    void norm1ComputesMaximumAbsoluteColumnSum() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, -2.0, 3.0},
                {4.0, 5.0, -6.0}
        });

        // Column sums: |1|+|4|=5, |-2|+|5|=7, |3|+|-6|=9
        assertThat(matrix.norm1()).isEqualTo(9.0);
    }

    @Test
    void sumComputesTotalOfAllElements() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });

        assertThat(matrix.sum()).isEqualTo(21.0);  // 1+2+3+4+5+6
    }

    @Test
    void determinantOfIdentityMatrixIsOne() {
        assertThat(Matrix.identity(2).determinant()).isCloseTo(1.0, within(TOLERANCE));
        assertThat(Matrix.identity(3).determinant()).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void determinantOf2x2Matrix() {
        Matrix matrix = Matrix.of(new double[][]{{3.0, 8.0}, {4.0, 6.0}});
        // det = 3*6 - 8*4 = 18 - 32 = -14
        assertThat(matrix.determinant()).isCloseTo(-14.0, within(TOLERANCE));
    }

    @Test
    void determinantOf3x3Matrix() {
        Matrix matrix = Matrix.of(new double[][]{
                {6.0, 1.0, 1.0},
                {4.0, -2.0, 5.0},
                {2.0, 8.0, 7.0}
        });
        // Determinant = -306
        assertThat(matrix.determinant()).isCloseTo(-306.0, within(RELAXED_TOLERANCE));
    }

    @Test
    void determinantOfSingularMatrixIsZero() {
        Matrix singular = Matrix.of(new double[][]{
                {1.0, 2.0},
                {2.0, 4.0}  // Second row is 2 * first row
        });

        assertThat(singular.determinant()).isCloseTo(0.0, within(TOLERANCE));
    }

    // ==================== Matrix Transformations ====================

    @Test
    void transposeSwapsRowsAndColumns() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        });
        Matrix transposed = matrix.transpose();

        assertThat(transposed.getRowCount()).isEqualTo(3);
        assertThat(transposed.getColumnCount()).isEqualTo(2);

        assertThat(transposed.get(0, 0)).isEqualTo(1.0);
        assertThat(transposed.get(0, 1)).isEqualTo(4.0);
        assertThat(transposed.get(1, 0)).isEqualTo(2.0);
        assertThat(transposed.get(1, 1)).isEqualTo(5.0);
        assertThat(transposed.get(2, 0)).isEqualTo(3.0);
        assertThat(transposed.get(2, 1)).isEqualTo(6.0);
    }

    @Test
    void transposeOfTransposeIsOriginal() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix result = matrix.transpose().transpose();

        assertThat(result).isEqualTo(matrix);
    }

    @Test
    void transposeOfSquareMatrixPreservesDimensions() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix transposed = matrix.transpose();

        assertThat(transposed.getRowCount()).isEqualTo(2);
        assertThat(transposed.getColumnCount()).isEqualTo(2);
    }

    @Test
    void inverseOfIdentityIsIdentity() {
        Matrix identity = Matrix.identity(3);
        Matrix inverse = identity.inverse();

        assertMatricesEqual(inverse, identity, TOLERANCE);
    }

    @Test
    void inverseTimesMatrixIsIdentity() {
        Matrix A = Matrix.of(new double[][]{
                {4.0, 7.0},
                {2.0, 6.0}
        });
        Matrix inverse = A.inverse();
        Matrix product = A.multiply(inverse);

        assertMatricesEqual(product, Matrix.identity(2), RELAXED_TOLERANCE);
    }

    @Test
    void inverseOfInverseIsOriginal() {
        Matrix A = Matrix.of(new double[][]{
                {1.0, 2.0},
                {3.0, 4.0}
        });
        Matrix result = A.inverse().inverse();

        assertMatricesEqual(result, A, RELAXED_TOLERANCE);
    }

    // ==================== Submatrix Operations ====================

    @Test
    void getMatrixExtractsSubmatrix() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0, 4.0},
                {5.0, 6.0, 7.0, 8.0},
                {9.0, 10.0, 11.0, 12.0}
        });

        Matrix submatrix = matrix.getMatrix(0, 1, 1, 2);

        assertThat(submatrix.getRowCount()).isEqualTo(2);
        assertThat(submatrix.getColumnCount()).isEqualTo(2);
        assertThat(submatrix.get(0, 0)).isEqualTo(2.0);
        assertThat(submatrix.get(0, 1)).isEqualTo(3.0);
        assertThat(submatrix.get(1, 0)).isEqualTo(6.0);
        assertThat(submatrix.get(1, 1)).isEqualTo(7.0);
    }

    @Test
    void getMatrixWithSingleElement() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix submatrix = matrix.getMatrix(1, 1, 1, 1);

        assertThat(submatrix.getRowCount()).isEqualTo(1);
        assertThat(submatrix.getColumnCount()).isEqualTo(1);
        assertThat(submatrix.get(0, 0)).isEqualTo(4.0);
    }

    @Test
    void getMatrixRejectsInvalidIndices() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        // i0 > i1
        assertThatThrownBy(() -> matrix.getMatrix(1, 0, 0, 1))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        // j0 > j1
        assertThatThrownBy(() -> matrix.getMatrix(0, 1, 1, 0))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);

        // Out of bounds
        assertThatThrownBy(() -> matrix.getMatrix(0, 2, 0, 1))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void getMatrixWithRowIndicesExtractsSpecificRows() {
        Matrix matrix = Matrix.of(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0},
                {7.0, 8.0, 9.0}
        });

        int[] rows = {0, 2};
        Matrix submatrix = matrix.getMatrix(rows, 1, 2);

        assertThat(submatrix.getRowCount()).isEqualTo(2);
        assertThat(submatrix.getColumnCount()).isEqualTo(2);
        assertThat(submatrix.get(0, 0)).isEqualTo(2.0);
        assertThat(submatrix.get(0, 1)).isEqualTo(3.0);
        assertThat(submatrix.get(1, 0)).isEqualTo(8.0);
        assertThat(submatrix.get(1, 1)).isEqualTo(9.0);
    }

    @Test
    void getMatrixWithRowIndicesRejectsInvalidRowIndex() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        int[] rows = {0, 5};

        assertThatThrownBy(() -> matrix.getMatrix(rows, 0, 1))
                .isInstanceOf(ArrayIndexOutOfBoundsException.class)
                .hasMessageContaining("out of bounds");
    }

    @Test
    void getMatrixWithRowIndicesRejectsNullArray() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThatThrownBy(() -> matrix.getMatrix(null, 0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void getMatrixWithRowIndicesRejectsEmptyArray() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThatThrownBy(() -> matrix.getMatrix(new int[0], 0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    // ==================== Linear Algebra Operations ====================

    @Test
    void solveSystemOfLinearEquations() {
        // Solve: 2x + 3y = 8
        //        4x + y = 10
        Matrix A = Matrix.of(new double[][]{{2.0, 3.0}, {4.0, 1.0}});
        Matrix B = Matrix.of(new double[][]{{8.0}, {10.0}});
        Matrix X = A.solve(B);

        // Solution: x = 2.2, y = 1.2
        // Verification: 2(2.2) + 3(1.2) = 4.4 + 3.6 = 8 ✓
        //               4(2.2) + 1(1.2) = 8.8 + 1.2 = 10 ✓
        assertThat(X.get(0, 0)).isCloseTo(2.2, within(RELAXED_TOLERANCE));
        assertThat(X.get(1, 0)).isCloseTo(1.2, within(RELAXED_TOLERANCE));

        // Verify A*X = B
        Matrix result = A.multiply(X);
        assertMatricesEqual(result, B, RELAXED_TOLERANCE);
    }

    @Test
    void solveIdentitySystemReturnsRightHandSide() {
        Matrix I = Matrix.identity(3);
        Matrix B = Matrix.of(new double[][]{{1.0}, {2.0}, {3.0}});
        Matrix X = I.solve(B);

        assertMatricesEqual(X, B, TOLERANCE);
    }

    @Test
    void solveLeastSquaresForOverdeterminedSystem() {
        // More equations than unknowns - uses QR decomposition
        Matrix A = Matrix.of(new double[][]{
                {1.0, 1.0},
                {1.0, 2.0},
                {1.0, 3.0}
        });
        Matrix B = Matrix.of(new double[][]{{1.0}, {2.0}, {2.0}});
        Matrix X = A.solve(B);

        // Solution should minimize ||AX - B||
        assertThat(X.getRowCount()).isEqualTo(2);
        assertThat(X.getColumnCount()).isEqualTo(1);
    }

    @Test
    void getLUDecompositionReturnsValidDecomposition() {
        Matrix A = Matrix.of(new double[][]{
                {2.0, 3.0},
                {4.0, 9.0}
        });
        LUDecomposition lu = A.getLUDecomposition();

        assertThat(lu).isNotNull();
        assertThat(lu.isNonsingular()).isTrue();
    }

    @Test
    void getQRDecompositionReturnsValidDecomposition() {
        Matrix A = Matrix.of(new double[][]{
                {1.0, 2.0},
                {3.0, 4.0},
                {5.0, 6.0}
        });
        QRDecomposition qr = A.getQRDecomposition();

        assertThat(qr).isNotNull();
        assertThat(qr.isFullRank()).isTrue();
    }

    // ==================== Equality and HashCode ====================

    @Test
    void equalsReturnsTrueForIdenticalMatrices() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThat(A.equals(B)).isTrue();
        assertThat(B.equals(A)).isTrue();
    }

    @Test
    void equalsReturnsTrueForSameInstance() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThat(matrix.equals(matrix)).isTrue();
    }

    @Test
    void equalsReturnsFalseForDifferentValues() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 5.0}});

        assertThat(A.equals(B)).isFalse();
    }

    @Test
    void equalsReturnsFalseForDifferentDimensions() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{1.0, 2.0}});

        assertThat(A.equals(B)).isFalse();
    }

    @Test
    void equalsReturnsFalseForNull() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThat(matrix.equals(null)).isFalse();
    }

    @Test
    void equalsReturnsFalseForDifferentType() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThat(matrix.equals("not a matrix")).isFalse();
    }

    @Test
    void hashCodeIsConsistent() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        int hash1 = matrix.hashCode();
        int hash2 = matrix.hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void hashCodeEqualForEqualMatrices() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        assertThat(A.hashCode()).isEqualTo(B.hashCode());
    }

    @Test
    void toStringReturnsFormattedMatrix() {
        Matrix matrix = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        String str = matrix.toString();

        assertThat(str).isNotNull();
        assertThat(str).contains("1.0");
        assertThat(str).contains("2.0");
        assertThat(str).contains("3.0");
        assertThat(str).contains("4.0");
    }

    // ==================== Edge Cases ====================

    @Test
    void zeroMatrixOperations() {
        Matrix zero = Matrix.ofSize(2, 2);
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});

        // Adding zero matrix
        assertThat(A.add(zero)).isEqualTo(A);

        // Subtracting zero matrix
        assertThat(A.subtract(zero)).isEqualTo(A);

        // Multiplying by zero matrix produces zero
        Matrix result = A.multiply(zero);
        for (int i = 0; i < result.getRowCount(); i++) {
            for (int j = 0; j < result.getColumnCount(); j++) {
                assertThat(result.get(i, j)).isEqualTo(0.0);
            }
        }
    }

    @Test
    void singleElementMatrixOperations() {
        Matrix single = Matrix.of(new double[][]{{5.0}});

        assertThat(single.add(3.0).get(0, 0)).isEqualTo(8.0);
        assertThat(single.multiply(2.0).get(0, 0)).isEqualTo(10.0);
        assertThat(single.transpose()).isEqualTo(single);
        assertThat(single.trace()).isEqualTo(5.0);
        assertThat(single.determinant()).isCloseTo(5.0, within(TOLERANCE));
    }

    @Test
    void largeMatrixCreation() {
        Matrix large = Matrix.ofSize(100, 100);

        assertThat(large.getRowCount()).isEqualTo(100);
        assertThat(large.getColumnCount()).isEqualTo(100);
    }

    @Test
    void normalizationWithZeroPadding() {
        Matrix A = Matrix.of(new double[][]{{1.0}});
        Matrix B = Matrix.of(new double[][]{{2.0, 3.0}, {4.0, 5.0}});

        // A gets padded to match B's size
        Matrix result = A.add(B);

        assertThat(result.getRowCount()).isEqualTo(2);
        assertThat(result.getColumnCount()).isEqualTo(2);
        assertThat(result.get(0, 0)).isEqualTo(3.0);  // 1 + 2
        assertThat(result.get(0, 1)).isEqualTo(3.0);  // 0 + 3
        assertThat(result.get(1, 0)).isEqualTo(4.0);  // 0 + 4
        assertThat(result.get(1, 1)).isEqualTo(5.0);  // 0 + 5
    }

    @Test
    void matrixWithNegativeValues() {
        Matrix matrix = Matrix.of(new double[][]{{-1.0, -2.0}, {-3.0, -4.0}});

        assertThat(matrix.sum()).isEqualTo(-10.0);
        assertThat(matrix.trace()).isEqualTo(-5.0);
        assertThat(matrix.uminus().get(0, 0)).isEqualTo(1.0);
    }

    @Test
    void matrixWithVerySmallValues() {
        double small = 1e-15;
        Matrix matrix = Matrix.of(new double[][]{{small, small}, {small, small}});

        assertThat(matrix.sum()).isCloseTo(4 * small, within(TOLERANCE));
    }

    @Test
    void divisionResultingInInfinity() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}});
        Matrix B = Matrix.of(new double[][]{{0.0, 1.0}});
        Matrix result = A.divide(B);

        assertThat(result.get(0, 0)).isEqualTo(Double.POSITIVE_INFINITY);
        assertThat(result.get(0, 1)).isEqualTo(2.0);
    }

    // ==================== Immutability ====================

    @Test
    void addDoesNotModifyOriginalMatrix() {
        Matrix original = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        double originalValue = original.get(0, 0);

        original.add(5.0);

        assertThat(original.get(0, 0)).isEqualTo(originalValue);
    }

    @Test
    void multiplyDoesNotModifyOriginalMatrix() {
        Matrix original = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        double originalValue = original.get(0, 0);

        original.multiply(2.0);

        assertThat(original.get(0, 0)).isEqualTo(originalValue);
    }

    @Test
    void transposeDoesNotModifyOriginalMatrix() {
        Matrix original = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        int originalRows = original.getRowCount();
        int originalCols = original.getColumnCount();

        original.transpose();

        assertThat(original.getRowCount()).isEqualTo(originalRows);
        assertThat(original.getColumnCount()).isEqualTo(originalCols);
    }

    @Test
    void copyCreatesIndependentCopy() {
        Matrix original = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix copy = original.copy();

        assertThat(copy).isEqualTo(original);
        assertThat(copy).isNotSameAs(original);

        // Verify deep copy by checking arrays are different objects
        assertThat(copy.getArrayCopy()).isNotSameAs(original.getArrayCopy());
    }

    @Test
    void cloneCreatesIndependentCopy() {
        Matrix original = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix clone = (Matrix) original.clone();

        assertThat(clone).isEqualTo(original);
        assertThat(clone).isNotSameAs(original);
    }

    @Test
    void allOperationsReturnNewInstances() {
        Matrix A = Matrix.of(new double[][]{{1.0, 2.0}, {3.0, 4.0}});
        Matrix B = Matrix.of(new double[][]{{5.0, 6.0}, {7.0, 8.0}});

        assertThat(A.add(5.0)).isNotSameAs(A);
        assertThat(A.add(B)).isNotSameAs(A);
        assertThat(A.subtract(5.0)).isNotSameAs(A);
        assertThat(A.subtract(B)).isNotSameAs(A);
        assertThat(A.multiply(2.0)).isNotSameAs(A);
        assertThat(A.multiply(B)).isNotSameAs(A);
        assertThat(A.divide(2.0)).isNotSameAs(A);
        assertThat(A.transpose()).isNotSameAs(A);
        assertThat(A.uminus()).isNotSameAs(A);
        assertThat(A.pow(2.0)).isNotSameAs(A);
    }

    // ==================== Helper Methods ====================

    /**
     * Asserts that two matrices are equal within a specified tolerance.
     *
     * @param actual    the actual matrix
     * @param expected  the expected matrix
     * @param tolerance the tolerance for floating-point comparison
     */
    private void assertMatricesEqual(Matrix actual, Matrix expected, double tolerance) {
        assertThat(actual.getRowCount()).isEqualTo(expected.getRowCount());
        assertThat(actual.getColumnCount()).isEqualTo(expected.getColumnCount());

        for (int i = 0; i < actual.getRowCount(); i++) {
            for (int j = 0; j < actual.getColumnCount(); j++) {
                assertThat(actual.get(i, j))
                        .as("Element at (%d, %d)", i, j)
                        .isCloseTo(expected.get(i, j), within(tolerance));
            }
        }
    }
}
