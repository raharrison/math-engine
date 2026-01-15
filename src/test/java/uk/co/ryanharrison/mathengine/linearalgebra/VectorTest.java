package uk.co.ryanharrison.mathengine.linearalgebra;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for the immutable {@link Vector} class.
 * <p>
 * Tests cover factory methods, element access, arithmetic operations (scalar and vector),
 * mathematical functions, vector products, norms, statistical operations, string operations,
 * equality/hashCode, edge cases, and immutability guarantees.
 * </p>
 */
class VectorTest {

    private static final double TOLERANCE = 1e-9;

    // ==================== Factory Methods ====================

    @Test
    void ofCreatesVectorWithSpecifiedValues() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThat(v.getSize()).isEqualTo(3);
        assertThat(v.get(0)).isEqualTo(1.0);
        assertThat(v.get(1)).isEqualTo(2.0);
        assertThat(v.get(2)).isEqualTo(3.0);
    }

    @Test
    void ofCreatesSingleElementVector() {
        Vector v = Vector.of(42.0);

        assertThat(v.getSize()).isEqualTo(1);
        assertThat(v.get(0)).isEqualTo(42.0);
    }

    @Test
    void ofRejectsNullArray() {
        assertThatThrownBy(() -> Vector.of((double[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one value");
    }

    @Test
    void ofRejectsEmptyArray() {
        assertThatThrownBy(() -> Vector.of(new double[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one value");
    }

    @ParameterizedTest
    @CsvSource({
            "1",
            "5",
            "10",
            "100"
    })
    void zerosCreatesVectorFilledWithZeros(int size) {
        Vector v = Vector.zeros(size);

        assertThat(v.getSize()).isEqualTo(size);
        for (int i = 0; i < size; i++) {
            assertThat(v.get(i)).isEqualTo(0.0);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "-1",
            "-100"
    })
    void zerosRejectsInvalidSize(int size) {
        assertThatThrownBy(() -> Vector.zeros(size))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 1");
    }

    @ParameterizedTest
    @CsvSource({
            "1",
            "3",
            "10"
    })
    void onesCreatesVectorFilledWithOnes(int size) {
        Vector v = Vector.ones(size);

        assertThat(v.getSize()).isEqualTo(size);
        for (int i = 0; i < size; i++) {
            assertThat(v.get(i)).isEqualTo(1.0);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "3, 7.5",
            "5, -2.0",
            "1, 0.0",
            "10, 3.14159"
    })
    void filledCreatesVectorWithSpecifiedValue(int size, double value) {
        Vector v = Vector.filled(size, value);

        assertThat(v.getSize()).isEqualTo(size);
        for (int i = 0; i < size; i++) {
            assertThat(v.get(i)).isEqualTo(value);
        }
    }

    @Test
    void filledRejectsInvalidSize() {
        assertThatThrownBy(() -> Vector.filled(0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 1");
    }

    // ==================== String Parsing ====================

    @Test
    void parsesVectorFromStringWithBraces() {
        Vector v = Vector.parse("{1.0, 2.0, 3.0}");

        assertThat(v.getSize()).isEqualTo(3);
        assertThat(v.get(0)).isEqualTo(1.0);
        assertThat(v.get(1)).isEqualTo(2.0);
        assertThat(v.get(2)).isEqualTo(3.0);
    }

    @Test
    void parsesVectorFromStringWithoutBraces() {
        Vector v = Vector.parse("4.5, -2.1, 3.0");

        assertThat(v.getSize()).isEqualTo(3);
        assertThat(v.get(0)).isEqualTo(4.5);
        assertThat(v.get(1)).isEqualTo(-2.1);
        assertThat(v.get(2)).isEqualTo(3.0);
    }

    @Test
    void parsesVectorWithScientificNotation() {
        Vector v = Vector.parse("1.5e-3, 2.0e2, -3e1");

        assertThat(v.getSize()).isEqualTo(3);
        assertThat(v.get(0)).isCloseTo(0.0015, within(TOLERANCE));
        assertThat(v.get(1)).isCloseTo(200.0, within(TOLERANCE));
        assertThat(v.get(2)).isCloseTo(-30.0, within(TOLERANCE));
    }

    @Test
    void parsesSingleElementVector() {
        Vector v = Vector.parse("{42}");

        assertThat(v.getSize()).isEqualTo(1);
        assertThat(v.get(0)).isEqualTo(42.0);
    }

    @Test
    void parseHandlesExtraWhitespace() {
        Vector v = Vector.parse("{  1.0  ,  2.0  ,  3.0  }");

        assertThat(v.getSize()).isEqualTo(3);
        assertThat(v.get(0)).isEqualTo(1.0);
    }

    @Test
    void parseRejectsNullString() {
        assertThatThrownBy(() -> Vector.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void parseRejectsEmptyString() {
        assertThatThrownBy(() -> Vector.parse(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null or empty");
    }

    @Test
    void parseRejectsStringWithNoNumbers() {
        assertThatThrownBy(() -> Vector.parse("{}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no valid numbers");
    }

    @Test
    void parseRejectsInvalidNumberFormat() {
        // The parser extracts numeric sequences, so "1.2.3" becomes an invalid number
        assertThatThrownBy(() -> Vector.parse("{1.2.3, 4.5}"))
                .isInstanceOf(NumberFormatException.class);
    }

    // ==================== Element Access ====================

    @Test
    void getsElementAtValidIndex() {
        Vector v = Vector.of(10.0, 20.0, 30.0);

        assertThat(v.get(0)).isEqualTo(10.0);
        assertThat(v.get(1)).isEqualTo(20.0);
        assertThat(v.get(2)).isEqualTo(30.0);
    }

    @Test
    void getRejectsNegativeIndex() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.get(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of range");
    }

    @Test
    void getRejectsIndexEqualToSize() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.get(3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of range");
    }

    @Test
    void getRejectsIndexGreaterThanSize() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.get(10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of range");
    }

    @Test
    void getElementsReturnsCopyOfInternalArray() {
        Vector v = Vector.of(1.0, 2.0, 3.0);
        double[] elements = v.getElements();

        assertThat(elements).containsExactly(1.0, 2.0, 3.0);

        // Verify it's a defensive copy
        elements[0] = 999.0;
        assertThat(v.get(0)).isEqualTo(1.0);
    }

    @Test
    void toArrayReturnsCopyOfInternalArray() {
        Vector v = Vector.of(1.0, 2.0, 3.0);
        double[] array = v.toArray();

        assertThat(array).containsExactly(1.0, 2.0, 3.0);

        // Verify it's a defensive copy
        array[0] = 999.0;
        assertThat(v.get(0)).isEqualTo(1.0);
    }

    @Test
    void getSizeReturnsCorrectDimension() {
        assertThat(Vector.of(1.0).getSize()).isEqualTo(1);
        assertThat(Vector.of(1.0, 2.0).getSize()).isEqualTo(2);
        assertThat(Vector.of(1.0, 2.0, 3.0).getSize()).isEqualTo(3);
        assertThat(Vector.zeros(100).getSize()).isEqualTo(100);
    }

    // ==================== Arithmetic Operations (Scalar) ====================

    @ParameterizedTest
    @CsvSource({
            "5.0",
            "-3.0",
            "0.0",
            "2.5"
    })
    void addScalarToVector(double scalar) {
        Vector v = Vector.of(1.0, 2.0, 3.0);
        Vector result = v.add(scalar);

        assertThat(result.get(0)).isCloseTo(1.0 + scalar, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(2.0 + scalar, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(3.0 + scalar, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "2.0",
            "-1.0",
            "0.0",
            "3.5"
    })
    void subtractScalarFromVector(double scalar) {
        Vector v = Vector.of(5.0, 7.0, 9.0);
        Vector result = v.subtract(scalar);

        assertThat(result.get(0)).isCloseTo(5.0 - scalar, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(7.0 - scalar, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(9.0 - scalar, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "2.0",
            "-3.0",
            "0.5",
            "1.0"
    })
    void multiplyVectorByScalar(double scalar) {
        Vector v = Vector.of(1.0, 2.0, 3.0);
        Vector result = v.multiply(scalar);

        assertThat(result.get(0)).isCloseTo(1.0 * scalar, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(2.0 * scalar, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(3.0 * scalar, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "2.0",
            "-4.0",
            "0.5"
    })
    void divideVectorByScalar(double scalar) {
        Vector v = Vector.of(6.0, 9.0, 12.0);
        Vector result = v.divide(scalar);

        assertThat(result.get(0)).isCloseTo(6.0 / scalar, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(9.0 / scalar, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(12.0 / scalar, within(TOLERANCE));
    }

    @Test
    void divideByZeroThrowsException() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.divide(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("divide vector by zero");
    }

    // ==================== Arithmetic Operations (Vector) ====================

    @Test
    void addVectorsOfSameSize() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0, 6.0);
        Vector result = v1.add(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(7.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(9.0, within(TOLERANCE));
    }

    @Test
    void addVectorsOfDifferentSizes() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0);
        Vector result = v1.add(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(7.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(3.0, within(TOLERANCE)); // 3.0 + 0.0
    }

    @Test
    void addNullVectorThrowsException() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.add(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null vector");
    }

    @Test
    void subtractVectorsOfSameSize() {
        Vector v1 = Vector.of(5.0, 7.0, 9.0);
        Vector v2 = Vector.of(1.0, 2.0, 3.0);
        Vector result = v1.subtract(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(4.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(6.0, within(TOLERANCE));
    }

    @Test
    void subtractVectorsOfDifferentSizes() {
        Vector v1 = Vector.of(5.0, 7.0, 9.0);
        Vector v2 = Vector.of(1.0, 2.0);
        Vector result = v1.subtract(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(4.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(9.0, within(TOLERANCE)); // 9.0 - 0.0
    }

    @Test
    void subtractNullVectorThrowsException() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.subtract(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null vector");
    }

    @Test
    void multiplyVectorsElementWise() {
        Vector v1 = Vector.of(2.0, 3.0, 4.0);
        Vector v2 = Vector.of(5.0, 6.0, 7.0);
        Vector result = v1.multiply(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(10.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(18.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(28.0, within(TOLERANCE));
    }

    @Test
    void multiplyVectorsOfDifferentSizes() {
        Vector v1 = Vector.of(2.0, 3.0, 4.0);
        Vector v2 = Vector.of(5.0, 6.0);
        Vector result = v1.multiply(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(10.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(18.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(0.0, within(TOLERANCE)); // 4.0 * 0.0
    }

    @Test
    void multiplyByNullVectorThrowsException() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.multiply(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null vector");
    }

    @Test
    void divideVectorsElementWise() {
        Vector v1 = Vector.of(10.0, 20.0, 30.0);
        Vector v2 = Vector.of(2.0, 4.0, 5.0);
        Vector result = v1.divide(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(6.0, within(TOLERANCE));
    }

    @Test
    void divideVectorsOfDifferentSizes() {
        Vector v1 = Vector.of(10.0, 20.0, 30.0);
        Vector v2 = Vector.of(2.0, 4.0);
        Vector result = v1.divide(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(2)).isEqualTo(Double.POSITIVE_INFINITY); // 30.0 / 0.0
    }

    @Test
    void divideByNullVectorThrowsException() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.divide(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null vector");
    }

    @Test
    void divideByVectorWithZeroProducesInfinity() {
        Vector v1 = Vector.of(10.0, 20.0);
        Vector v2 = Vector.of(2.0, 0.0);
        Vector result = v1.divide(v2);

        assertThat(result.get(0)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(result.get(1)).isEqualTo(Double.POSITIVE_INFINITY);
    }

    // ==================== Mathematical Functions ====================

    @Test
    void powRaisesElementsToScalarPower() {
        Vector v = Vector.of(2.0, 3.0, 4.0);
        Vector squared = v.pow(2.0);
        Vector cubed = v.pow(3.0);

        assertThat(squared.get(0)).isCloseTo(4.0, within(TOLERANCE));
        assertThat(squared.get(1)).isCloseTo(9.0, within(TOLERANCE));
        assertThat(squared.get(2)).isCloseTo(16.0, within(TOLERANCE));

        assertThat(cubed.get(0)).isCloseTo(8.0, within(TOLERANCE));
        assertThat(cubed.get(1)).isCloseTo(27.0, within(TOLERANCE));
        assertThat(cubed.get(2)).isCloseTo(64.0, within(TOLERANCE));
    }

    @Test
    void powWithFractionalExponent() {
        Vector v = Vector.of(4.0, 9.0, 16.0);
        Vector sqrt = v.pow(0.5);

        assertThat(sqrt.get(0)).isCloseTo(2.0, within(TOLERANCE));
        assertThat(sqrt.get(1)).isCloseTo(3.0, within(TOLERANCE));
        assertThat(sqrt.get(2)).isCloseTo(4.0, within(TOLERANCE));
    }

    @Test
    void powWithNegativeExponent() {
        Vector v = Vector.of(2.0, 4.0, 5.0);
        Vector reciprocal = v.pow(-1.0);

        assertThat(reciprocal.get(0)).isCloseTo(0.5, within(TOLERANCE));
        assertThat(reciprocal.get(1)).isCloseTo(0.25, within(TOLERANCE));
        assertThat(reciprocal.get(2)).isCloseTo(0.2, within(TOLERANCE));
    }

    @Test
    void powWithZeroExponent() {
        Vector v = Vector.of(2.0, 3.0, 4.0);
        Vector result = v.pow(0.0);

        assertThat(result.get(0)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void powWithVectorExponents() {
        Vector v1 = Vector.of(2.0, 3.0, 4.0);
        Vector v2 = Vector.of(3.0, 2.0, 1.0);
        Vector result = v1.pow(v2);

        assertThat(result.get(0)).isCloseTo(8.0, within(TOLERANCE));   // 2^3
        assertThat(result.get(1)).isCloseTo(9.0, within(TOLERANCE));   // 3^2
        assertThat(result.get(2)).isCloseTo(4.0, within(TOLERANCE));   // 4^1
    }

    @Test
    void powWithVectorOfDifferentSizes() {
        Vector v1 = Vector.of(2.0, 3.0, 4.0);
        Vector v2 = Vector.of(3.0, 2.0);
        Vector result = v1.pow(v2);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.get(0)).isCloseTo(8.0, within(TOLERANCE));   // 2^3
        assertThat(result.get(1)).isCloseTo(9.0, within(TOLERANCE));   // 3^2
        assertThat(result.get(2)).isCloseTo(1.0, within(TOLERANCE));   // 4^0
    }

    @Test
    void powWithNullVectorThrowsException() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.pow(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null vector");
    }

    @Test
    void sqrtCalculatesSquareRoot() {
        Vector v = Vector.of(4.0, 9.0, 16.0, 25.0);
        Vector result = v.sqrt();

        assertThat(result.get(0)).isCloseTo(2.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(3.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(4.0, within(TOLERANCE));
        assertThat(result.get(3)).isCloseTo(5.0, within(TOLERANCE));
    }

    @Test
    void sqrtOfZeroIsZero() {
        Vector v = Vector.of(0.0, 4.0);
        Vector result = v.sqrt();

        assertThat(result.get(0)).isEqualTo(0.0);
        assertThat(result.get(1)).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void sqrtOfNegativeProducesNaN() {
        Vector v = Vector.of(-1.0, 4.0);
        Vector result = v.sqrt();

        assertThat(result.get(0)).isNaN();
        assertThat(result.get(1)).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void logCalculatesNaturalLogarithm() {
        Vector v = Vector.of(1.0, Math.E, Math.E * Math.E);
        Vector result = v.log();

        assertThat(result.get(0)).isCloseTo(0.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void logOfOneIsZero() {
        Vector v = Vector.of(1.0);
        Vector result = v.log();

        assertThat(result.get(0)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void logOfZeroIsNegativeInfinity() {
        Vector v = Vector.of(0.0, 1.0);
        Vector result = v.log();

        assertThat(result.get(0)).isEqualTo(Double.NEGATIVE_INFINITY);
        assertThat(result.get(1)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void logOfNegativeProducesNaN() {
        Vector v = Vector.of(-1.0, 1.0);
        Vector result = v.log();

        assertThat(result.get(0)).isNaN();
        assertThat(result.get(1)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void expCalculatesExponential() {
        Vector v = Vector.of(0.0, 1.0, 2.0);
        Vector result = v.exp();

        assertThat(result.get(0)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(Math.E, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(Math.E * Math.E, within(TOLERANCE));
    }

    @Test
    void expOfZeroIsOne() {
        Vector v = Vector.of(0.0);
        Vector result = v.exp();

        assertThat(result.get(0)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void expOfNegativeValues() {
        Vector v = Vector.of(-1.0, -2.0);
        Vector result = v.exp();

        assertThat(result.get(0)).isCloseTo(1.0 / Math.E, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(1.0 / (Math.E * Math.E), within(TOLERANCE));
    }

    // ==================== Vector Products ====================

    @Test
    void dotProductOfSameSizeVectors() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0, 6.0);
        double result = v1.dotProduct(v2);

        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertThat(result).isCloseTo(32.0, within(TOLERANCE));
    }

    @Test
    void dotProductOfDifferentSizeVectors() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0);
        double result = v1.dotProduct(v2);

        // 1*4 + 2*5 + 3*0 = 4 + 10 + 0 = 14
        assertThat(result).isCloseTo(14.0, within(TOLERANCE));
    }

    @Test
    void dotProductWithSelfEqualsNormSquare() {
        Vector v = Vector.of(3.0, 4.0);
        double dotProduct = v.dotProduct(v);
        double normSquare = v.getNormSquare();

        assertThat(dotProduct).isCloseTo(normSquare, within(TOLERANCE));
    }

    @Test
    void dotProductWithZeroVectorIsZero() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.zeros(3);
        double result = v1.dotProduct(v2);

        assertThat(result).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void dotProductWithNullVectorThrowsException() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.dotProduct(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null vector");
    }

    @Test
    void crossProductOf3DVectors() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0, 6.0);
        Vector result = v1.crossProduct(v2);

        assertThat(result.getSize()).isEqualTo(3);
        // (2*6 - 3*5, 3*4 - 1*6, 1*5 - 2*4) = (12-15, 12-6, 5-8) = (-3, 6, -3)
        assertThat(result.get(0)).isCloseTo(-3.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(6.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(-3.0, within(TOLERANCE));
    }

    @Test
    void crossProductOfParallelVectorsIsZero() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(2.0, 4.0, 6.0); // v2 = 2*v1
        Vector result = v1.crossProduct(v2);

        assertThat(result.get(0)).isCloseTo(0.0, within(TOLERANCE));
        assertThat(result.get(1)).isCloseTo(0.0, within(TOLERANCE));
        assertThat(result.get(2)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void crossProductIsAntiCommutative() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0, 6.0);
        Vector cross1 = v1.crossProduct(v2);
        Vector cross2 = v2.crossProduct(v1);

        // v1 × v2 = -(v2 × v1)
        assertThat(cross1.get(0)).isCloseTo(-cross2.get(0), within(TOLERANCE));
        assertThat(cross1.get(1)).isCloseTo(-cross2.get(1), within(TOLERANCE));
        assertThat(cross1.get(2)).isCloseTo(-cross2.get(2), within(TOLERANCE));
    }

    @Test
    void crossProductRejectsNon3DVectorFirst() {
        Vector v1 = Vector.of(1.0, 2.0);
        Vector v2 = Vector.of(3.0, 4.0, 5.0);

        assertThatThrownBy(() -> v1.crossProduct(v2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3-dimensional");
    }

    @Test
    void crossProductRejectsNon3DVectorSecond() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0);

        assertThatThrownBy(() -> v1.crossProduct(v2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3-dimensional");
    }

    @Test
    void crossProductRejectsNullVector() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThatThrownBy(() -> v.crossProduct(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null vector");
    }

    // ==================== Norms and Properties ====================

    @Test
    void getNormCalculatesEuclideanNorm() {
        Vector v = Vector.of(3.0, 4.0);
        double norm = v.getNorm();

        // sqrt(3^2 + 4^2) = sqrt(9 + 16) = sqrt(25) = 5.0
        assertThat(norm).isCloseTo(5.0, within(TOLERANCE));
    }

    @Test
    void getNormOf3DVector() {
        Vector v = Vector.of(1.0, 2.0, 2.0);
        double norm = v.getNorm();

        // sqrt(1 + 4 + 4) = sqrt(9) = 3.0
        assertThat(norm).isCloseTo(3.0, within(TOLERANCE));
    }

    @Test
    void getNormOfZeroVectorIsZero() {
        Vector v = Vector.zeros(3);
        double norm = v.getNorm();

        assertThat(norm).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void getNormOfUnitVectorIsOne() {
        Vector v = Vector.of(1.0, 0.0, 0.0);
        double norm = v.getNorm();

        assertThat(norm).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void getNormSquareAvoidsSqrt() {
        Vector v = Vector.of(3.0, 4.0);
        double normSquare = v.getNormSquare();

        // 3^2 + 4^2 = 9 + 16 = 25
        assertThat(normSquare).isCloseTo(25.0, within(TOLERANCE));
    }

    @Test
    void getNormSquareEqualsNormSquared() {
        Vector v = Vector.of(1.0, 2.0, 3.0, 4.0);
        double normSquare = v.getNormSquare();
        double norm = v.getNorm();

        assertThat(normSquare).isCloseTo(norm * norm, within(TOLERANCE));
    }

    @Test
    void getUnitVectorHasNormOfOne() {
        Vector v = Vector.of(3.0, 4.0);
        Vector unit = v.getUnitVector();

        double norm = unit.getNorm();
        assertThat(norm).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void getUnitVectorPreservesDirection() {
        Vector v = Vector.of(3.0, 4.0);
        Vector unit = v.getUnitVector();

        // Unit vector should be (3/5, 4/5)
        assertThat(unit.get(0)).isCloseTo(0.6, within(TOLERANCE));
        assertThat(unit.get(1)).isCloseTo(0.8, within(TOLERANCE));
    }

    @Test
    void getUnitVectorOf3DVector() {
        Vector v = Vector.of(1.0, 2.0, 2.0);
        Vector unit = v.getUnitVector();

        // Norm is 3.0, so unit is (1/3, 2/3, 2/3)
        assertThat(unit.get(0)).isCloseTo(1.0 / 3.0, within(TOLERANCE));
        assertThat(unit.get(1)).isCloseTo(2.0 / 3.0, within(TOLERANCE));
        assertThat(unit.get(2)).isCloseTo(2.0 / 3.0, within(TOLERANCE));
        assertThat(unit.getNorm()).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void getUnitVectorRejectsZeroVector() {
        Vector v = Vector.zeros(3);

        assertThatThrownBy(() -> v.getUnitVector())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("norm of zero");
    }

    // ==================== Statistical Operations ====================

    @Test
    void sumCalculatesTotalOfElements() {
        Vector v = Vector.of(1.0, 2.0, 3.0, 4.0);
        double sum = v.sum();

        assertThat(sum).isCloseTo(10.0, within(TOLERANCE));
    }

    @Test
    void sumOfSingleElement() {
        Vector v = Vector.of(42.0);
        double sum = v.sum();

        assertThat(sum).isCloseTo(42.0, within(TOLERANCE));
    }

    @Test
    void sumOfZerosIsZero() {
        Vector v = Vector.zeros(10);
        double sum = v.sum();

        assertThat(sum).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void sumWithNegativeValues() {
        Vector v = Vector.of(1.0, -2.0, 3.0, -4.0);
        double sum = v.sum();

        assertThat(sum).isCloseTo(-2.0, within(TOLERANCE));
    }

    @Test
    void meanCalculatesAverage() {
        Vector v = Vector.of(1.0, 2.0, 3.0, 4.0);
        double mean = v.mean();

        assertThat(mean).isCloseTo(2.5, within(TOLERANCE));
    }

    @Test
    void meanOfSingleElement() {
        Vector v = Vector.of(7.5);
        double mean = v.mean();

        assertThat(mean).isCloseTo(7.5, within(TOLERANCE));
    }

    @Test
    void meanOfZerosIsZero() {
        Vector v = Vector.zeros(5);
        double mean = v.mean();

        assertThat(mean).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void meanWithNegativeValues() {
        Vector v = Vector.of(-5.0, -10.0, -15.0);
        double mean = v.mean();

        assertThat(mean).isCloseTo(-10.0, within(TOLERANCE));
    }

    @Test
    void minFindsSmallestValue() {
        Vector v = Vector.of(3.0, 1.0, 4.0, 1.0, 5.0, 9.0, 2.0, 6.0);
        double min = v.min();

        assertThat(min).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void minOfSingleElement() {
        Vector v = Vector.of(42.0);
        double min = v.min();

        assertThat(min).isCloseTo(42.0, within(TOLERANCE));
    }

    @Test
    void minWithNegativeValues() {
        Vector v = Vector.of(5.0, -10.0, 3.0, -2.0);
        double min = v.min();

        assertThat(min).isCloseTo(-10.0, within(TOLERANCE));
    }

    @Test
    void maxFindsLargestValue() {
        Vector v = Vector.of(3.0, 1.0, 4.0, 1.0, 5.0, 9.0, 2.0, 6.0);
        double max = v.max();

        assertThat(max).isCloseTo(9.0, within(TOLERANCE));
    }

    @Test
    void maxOfSingleElement() {
        Vector v = Vector.of(42.0);
        double max = v.max();

        assertThat(max).isCloseTo(42.0, within(TOLERANCE));
    }

    @Test
    void maxWithNegativeValues() {
        Vector v = Vector.of(-5.0, -10.0, -3.0, -2.0);
        double max = v.max();

        assertThat(max).isCloseTo(-2.0, within(TOLERANCE));
    }

    // ==================== String Operations ====================

    @Test
    void toStringFormatsVectorCorrectly() {
        Vector v = Vector.of(1.0, 2.0, 3.0);
        String result = v.toString();

        assertThat(result).contains("1.0");
        assertThat(result).contains("2.0");
        assertThat(result).contains("3.0");
        assertThat(result).startsWith("{");
        assertThat(result).endsWith("}");
    }

    @Test
    void toStringForSingleElement() {
        Vector v = Vector.of(42.0);
        String result = v.toString();

        assertThat(result).isEqualTo("{42.0}");
    }

    @Test
    void toStringAndParseAreInverses() {
        Vector original = Vector.of(1.5, 2.5, 3.5);
        String str = original.toString();
        Vector parsed = Vector.parse(str);

        assertThat(parsed).isEqualTo(original);
    }

    // ==================== Equality and HashCode ====================

    @Test
    void equalVectorsAreEqual() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(1.0, 2.0, 3.0);

        assertThat(v1).isEqualTo(v2);
        assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
    }

    @Test
    void vectorEqualsItself() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThat(v).isEqualTo(v);
    }

    @Test
    void vectorNotEqualToNull() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThat(v).isNotEqualTo(null);
    }

    @Test
    void vectorNotEqualToDifferentType() {
        Vector v = Vector.of(1.0, 2.0, 3.0);

        assertThat(v).isNotEqualTo("not a vector");
        assertThat(v).isNotEqualTo(new Object());
    }

    @Test
    void vectorsWithDifferentValuesAreNotEqual() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(1.0, 2.0, 4.0);

        assertThat(v1).isNotEqualTo(v2);
    }

    @Test
    void vectorsWithDifferentSizesAreNotEqual() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(1.0, 2.0);

        assertThat(v1).isNotEqualTo(v2);
    }

    @Test
    void equalVectorsHaveSameHashCode() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(1.0, 2.0, 3.0);

        assertThat(v1.hashCode()).isEqualTo(v2.hashCode());
    }

    @Test
    void cloneCreatesEqualVector() {
        Vector original = Vector.of(1.0, 2.0, 3.0);
        Vector cloned = (Vector) original.clone();

        assertThat(cloned).isEqualTo(original);
        assertThat(cloned.hashCode()).isEqualTo(original.hashCode());
    }

    // ==================== Edge Cases ====================

    @Test
    void largeVectorOperations() {
        int size = 1000;
        Vector v1 = Vector.ones(size);
        Vector v2 = Vector.filled(size, 2.0);

        Vector sum = v1.add(v2);
        Vector product = v1.multiply(v2);

        assertThat(sum.getSize()).isEqualTo(size);
        assertThat(product.getSize()).isEqualTo(size);
        assertThat(sum.get(0)).isCloseTo(3.0, within(TOLERANCE));
        assertThat(product.get(0)).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void verySmallValues() {
        Vector v = Vector.of(1e-10, 2e-10, 3e-10);
        Vector doubled = v.multiply(2.0);

        assertThat(doubled.get(0)).isCloseTo(2e-10, within(1e-20));
        assertThat(doubled.get(1)).isCloseTo(4e-10, within(1e-20));
        assertThat(doubled.get(2)).isCloseTo(6e-10, within(1e-20));
    }

    @Test
    void veryLargeValues() {
        Vector v = Vector.of(1e100, 2e100, 3e100);
        Vector halved = v.divide(2.0);

        assertThat(halved.get(0)).isCloseTo(5e99, within(1e90));
        assertThat(halved.get(1)).isCloseTo(1e100, within(1e91));
        assertThat(halved.get(2)).isCloseTo(1.5e100, within(1e91));
    }

    @Test
    void mixedPositiveAndNegativeValues() {
        Vector v = Vector.of(-1.0, 2.0, -3.0, 4.0);
        Vector squared = v.pow(2.0);

        assertThat(squared.get(0)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(squared.get(1)).isCloseTo(4.0, within(TOLERANCE));
        assertThat(squared.get(2)).isCloseTo(9.0, within(TOLERANCE));
        assertThat(squared.get(3)).isCloseTo(16.0, within(TOLERANCE));
    }

    @Test
    void allZeroesVector() {
        Vector v = Vector.zeros(5);

        assertThat(v.sum()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(v.mean()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(v.getNorm()).isCloseTo(0.0, within(TOLERANCE));
        assertThat(v.getNormSquare()).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void singleElementVectorOperations() {
        Vector v = Vector.of(5.0);

        assertThat(v.add(3.0).get(0)).isCloseTo(8.0, within(TOLERANCE));
        assertThat(v.multiply(2.0).get(0)).isCloseTo(10.0, within(TOLERANCE));
        assertThat(v.pow(2.0).get(0)).isCloseTo(25.0, within(TOLERANCE));
        assertThat(v.getNorm()).isCloseTo(5.0, within(TOLERANCE));
        assertThat(v.sum()).isCloseTo(5.0, within(TOLERANCE));
        assertThat(v.mean()).isCloseTo(5.0, within(TOLERANCE));
    }

    // ==================== Immutability ====================

    @Test
    void addDoesNotModifyOriginalVector() {
        Vector v = Vector.of(1.0, 2.0, 3.0);
        Vector originalCopy = Vector.of(1.0, 2.0, 3.0);

        v.add(5.0);
        v.add(Vector.of(10.0, 20.0, 30.0));

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void subtractDoesNotModifyOriginalVector() {
        Vector v = Vector.of(5.0, 7.0, 9.0);
        Vector originalCopy = Vector.of(5.0, 7.0, 9.0);

        v.subtract(2.0);
        v.subtract(Vector.of(1.0, 2.0, 3.0));

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void multiplyDoesNotModifyOriginalVector() {
        Vector v = Vector.of(1.0, 2.0, 3.0);
        Vector originalCopy = Vector.of(1.0, 2.0, 3.0);

        v.multiply(3.0);
        v.multiply(Vector.of(2.0, 3.0, 4.0));

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void divideDoesNotModifyOriginalVector() {
        Vector v = Vector.of(6.0, 9.0, 12.0);
        Vector originalCopy = Vector.of(6.0, 9.0, 12.0);

        v.divide(3.0);
        v.divide(Vector.of(2.0, 3.0, 4.0));

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void powDoesNotModifyOriginalVector() {
        Vector v = Vector.of(2.0, 3.0, 4.0);
        Vector originalCopy = Vector.of(2.0, 3.0, 4.0);

        v.pow(2.0);
        v.pow(Vector.of(1.0, 2.0, 3.0));

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void sqrtDoesNotModifyOriginalVector() {
        Vector v = Vector.of(4.0, 9.0, 16.0);
        Vector originalCopy = Vector.of(4.0, 9.0, 16.0);

        v.sqrt();

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void logDoesNotModifyOriginalVector() {
        Vector v = Vector.of(1.0, Math.E, Math.E * Math.E);
        Vector originalCopy = Vector.of(1.0, Math.E, Math.E * Math.E);

        v.log();

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void expDoesNotModifyOriginalVector() {
        Vector v = Vector.of(0.0, 1.0, 2.0);
        Vector originalCopy = Vector.of(0.0, 1.0, 2.0);

        v.exp();

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void dotProductDoesNotModifyOriginalVectors() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0, 6.0);
        Vector v1Copy = Vector.of(1.0, 2.0, 3.0);
        Vector v2Copy = Vector.of(4.0, 5.0, 6.0);

        v1.dotProduct(v2);

        assertThat(v1).isEqualTo(v1Copy);
        assertThat(v2).isEqualTo(v2Copy);
    }

    @Test
    void crossProductDoesNotModifyOriginalVectors() {
        Vector v1 = Vector.of(1.0, 2.0, 3.0);
        Vector v2 = Vector.of(4.0, 5.0, 6.0);
        Vector v1Copy = Vector.of(1.0, 2.0, 3.0);
        Vector v2Copy = Vector.of(4.0, 5.0, 6.0);

        v1.crossProduct(v2);

        assertThat(v1).isEqualTo(v1Copy);
        assertThat(v2).isEqualTo(v2Copy);
    }

    @Test
    void getUnitVectorDoesNotModifyOriginalVector() {
        Vector v = Vector.of(3.0, 4.0);
        Vector originalCopy = Vector.of(3.0, 4.0);

        v.getUnitVector();

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void statisticalOperationsDoNotModifyVector() {
        Vector v = Vector.of(1.0, 2.0, 3.0, 4.0);
        Vector originalCopy = Vector.of(1.0, 2.0, 3.0, 4.0);

        v.sum();
        v.mean();
        v.min();
        v.max();
        v.getNorm();
        v.getNormSquare();

        assertThat(v).isEqualTo(originalCopy);
    }

    @Test
    void modifyingReturnedArrayDoesNotModifyVector() {
        Vector v = Vector.of(1.0, 2.0, 3.0);
        double[] array = v.getElements();

        array[0] = 999.0;
        array[1] = 888.0;
        array[2] = 777.0;

        assertThat(v.get(0)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(v.get(1)).isCloseTo(2.0, within(TOLERANCE));
        assertThat(v.get(2)).isCloseTo(3.0, within(TOLERANCE));
    }

    @Test
    void modifyingConstructorArrayDoesNotModifyVector() {
        double[] array = {1.0, 2.0, 3.0};
        Vector v = Vector.of(array);

        array[0] = 999.0;
        array[1] = 888.0;
        array[2] = 777.0;

        assertThat(v.get(0)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(v.get(1)).isCloseTo(2.0, within(TOLERANCE));
        assertThat(v.get(2)).isCloseTo(3.0, within(TOLERANCE));
    }
}
