package uk.co.ryanharrison.mathengine.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for {@link BigRational}.
 */
class BigRationalTest {

    private static final double TOLERANCE = 1e-12;

    // ==================== Construction Tests ====================

    @Test
    void canCreateRationalFromIntegers() {
        BigRational r = BigRational.of(3, 4);

        assertThat(r.getNumerator()).isEqualTo(BigInteger.valueOf(3));
        assertThat(r.getDenominator()).isEqualTo(BigInteger.valueOf(4));
    }

    @Test
    void canCreateRationalFromLongs() {
        BigRational r = BigRational.of(5L, 7L);

        assertThat(r.getNumerator()).isEqualTo(BigInteger.valueOf(5));
        assertThat(r.getDenominator()).isEqualTo(BigInteger.valueOf(7));
    }

    @Test
    void canCreateRationalFromBigIntegers() {
        BigInteger num = BigInteger.valueOf(11);
        BigInteger den = BigInteger.valueOf(13);

        BigRational r = BigRational.of(num, den);

        assertThat(r.getNumerator()).isEqualTo(num);
        assertThat(r.getDenominator()).isEqualTo(den);
    }

    @Test
    void canCreateRationalFromSingleInt() {
        BigRational r = BigRational.of(42);

        assertThat(r.getNumerator()).isEqualTo(BigInteger.valueOf(42));
        assertThat(r.getDenominator()).isEqualTo(BigInteger.ONE);
    }

    @Test
    void canCreateRationalFromSingleLong() {
        BigRational r = BigRational.of(123456789L);

        assertThat(r.getNumerator()).isEqualTo(BigInteger.valueOf(123456789L));
        assertThat(r.getDenominator()).isEqualTo(BigInteger.ONE);
    }

    @Test
    void canCreateRationalFromSingleBigInteger() {
        BigInteger value = BigInteger.valueOf(999);
        BigRational r = BigRational.of(value);

        assertThat(r.getNumerator()).isEqualTo(value);
        assertThat(r.getDenominator()).isEqualTo(BigInteger.ONE);
    }

    @Test
    void canCreateRationalFromString() {
        BigRational r = BigRational.of("0.5");

        assertThat(r.doubleValue()).isCloseTo(0.5, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0.5, 1, 2",     // 1/2
            "0.25, 1, 4",    // 1/4
            "0.75, 3, 4",    // 3/4
            "-0.5, -1, 2",   // -1/2
    })
    void canCreateRationalFromSimpleDoubles(double value, long expectedNum, long expectedDen) {
        BigRational r = BigRational.of(value);

        assertThat(r.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(r.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @Test
    void creatingRationalFromZeroProducesZeroOverOne() {
        BigRational r = BigRational.of(0, 5);

        assertThat(r.getNumerator()).isEqualTo(BigInteger.ZERO);
        assertThat(r.getDenominator()).isEqualTo(BigInteger.ONE);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 4, 1, 2",      // Reduces to 1/2
            "6, 9, 2, 3",      // Reduces to 2/3
            "10, 15, 2, 3",    // Reduces to 2/3
            "100, 50, 2, 1",   // Reduces to 2/1
            "-4, 8, -1, 2",    // Reduces to -1/2
            "4, -8, -1, 2",    // Reduces to -1/2 (sign normalized)
            "-4, -8, 1, 2",    // Reduces to 1/2 (both negative)
    })
    void rationalsAreAutomaticallyReduced(int num, int den, long expectedNum, long expectedDen) {
        BigRational r = BigRational.of(num, den);

        assertThat(r.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(r.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @Test
    void signIsNormalizedToNumerator() {
        BigRational r1 = BigRational.of(3, -4);
        BigRational r2 = BigRational.of(-3, 4);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.getNumerator()).isEqualTo(BigInteger.valueOf(-3));
        assertThat(r1.getDenominator()).isEqualTo(BigInteger.valueOf(4));
    }

    @Test
    void denominatorIsAlwaysPositive() {
        BigRational r1 = BigRational.of(5, 7);
        BigRational r2 = BigRational.of(-5, 7);
        BigRational r3 = BigRational.of(5, -7);
        BigRational r4 = BigRational.of(-5, -7);

        assertThat(r1.getDenominator()).isPositive();
        assertThat(r2.getDenominator()).isPositive();
        assertThat(r3.getDenominator()).isPositive();
        assertThat(r4.getDenominator()).isPositive();
    }

    @Test
    void creatingRationalWithZeroDenominatorThrowsException() {
        assertThatThrownBy(() -> BigRational.of(1, 0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");
    }

    @Test
    void creatingRationalWithNullBigIntegerThrowsException() {
        assertThatThrownBy(() -> BigRational.of((BigInteger) null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> BigRational.of(null, BigInteger.ONE))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> BigRational.of(BigInteger.ONE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void creatingRationalFromNaNThrowsException() {
        assertThatThrownBy(() -> BigRational.of(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NaN");
    }

    @Test
    void creatingRationalFromInfinityThrowsException() {
        assertThatThrownBy(() -> BigRational.of(Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("infinity");

        assertThatThrownBy(() -> BigRational.of(Double.NEGATIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("infinity");
    }

    // ==================== Approximation Tests ====================

    @Test
    void canCreateRationalWithMaxDenominator() {
        // Pi with max denominator 100 should give 22/7
        BigRational r = BigRational.of(Math.PI, 100);

        assertThat(r.getNumerator()).isEqualTo(BigInteger.valueOf(22));
        assertThat(r.getDenominator()).isEqualTo(BigInteger.valueOf(7));
    }

    @Test
    void canCreateRationalWithEpsilon() {
        // Pi with small epsilon should converge to a good approximation
        BigRational r = BigRational.of(Math.PI, 1e-6, 100);

        // Should be close to pi
        assertThat(r.doubleValue()).isCloseTo(Math.PI, within(1e-6));
    }

    @Test
    void approximationWithInvalidMaxDenominatorThrowsException() {
        assertThatThrownBy(() -> BigRational.of(3.14, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() -> BigRational.of(3.14, -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void approximationWithInvalidEpsilonThrowsException() {
        assertThatThrownBy(() -> BigRational.of(3.14, -0.001, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-negative");
    }

    @Test
    void approximationWithInvalidMaxIterationsThrowsException() {
        assertThatThrownBy(() -> BigRational.of(3.14, 1e-6, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() -> BigRational.of(3.14, 1e-6, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    // ==================== Constants Tests ====================

    @Test
    void constantsHaveCorrectValues() {
        assertThat(BigRational.ZERO.getNumerator()).isEqualTo(BigInteger.ZERO);
        assertThat(BigRational.ZERO.getDenominator()).isEqualTo(BigInteger.ONE);

        assertThat(BigRational.ONE.getNumerator()).isEqualTo(BigInteger.ONE);
        assertThat(BigRational.ONE.getDenominator()).isEqualTo(BigInteger.ONE);

        assertThat(BigRational.TWO.getNumerator()).isEqualTo(BigInteger.TWO);
        assertThat(BigRational.TWO.getDenominator()).isEqualTo(BigInteger.ONE);

        assertThat(BigRational.MINUS_ONE.getNumerator()).isEqualTo(BigInteger.ONE.negate());
        assertThat(BigRational.MINUS_ONE.getDenominator()).isEqualTo(BigInteger.ONE);

        assertThat(BigRational.ONE_HALF).isEqualTo(BigRational.of(1, 2));
        assertThat(BigRational.ONE_THIRD).isEqualTo(BigRational.of(1, 3));
        assertThat(BigRational.ONE_QUARTER).isEqualTo(BigRational.of(1, 4));
        assertThat(BigRational.ONE_FIFTH).isEqualTo(BigRational.of(1, 5));
        assertThat(BigRational.TWO_THIRDS).isEqualTo(BigRational.of(2, 3));
        assertThat(BigRational.TWO_QUARTERS).isEqualTo(BigRational.of(1, 2));
        assertThat(BigRational.TWO_FIFTHS).isEqualTo(BigRational.of(2, 5));
        assertThat(BigRational.THREE_QUARTERS).isEqualTo(BigRational.of(3, 4));
        assertThat(BigRational.THREE_FIFTHS).isEqualTo(BigRational.of(3, 5));
        assertThat(BigRational.FOUR_FIFTHS).isEqualTo(BigRational.of(4, 5));
    }

    // ==================== Addition Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1, 2, 1, 3, 5, 6",     // 1/2 + 1/3 = 5/6
            "1, 4, 1, 4, 1, 2",     // 1/4 + 1/4 = 1/2
            "2, 3, 1, 6, 5, 6",     // 2/3 + 1/6 = 5/6
            "3, 4, -1, 4, 1, 2",    // 3/4 + (-1/4) = 1/2
            "-1, 2, -1, 3, -5, 6",  // -1/2 + -1/3 = -5/6
    })
    void addRationals(int n1, int d1, int n2, int d2, long expectedNum, long expectedDen) {
        BigRational r1 = BigRational.of(n1, d1);
        BigRational r2 = BigRational.of(n2, d2);

        BigRational result = r1.add(r2);

        assertThat(result.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(result.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @Test
    void addingZeroReturnsOriginal() {
        BigRational r = BigRational.of(3, 4);

        assertThat(r.add(BigRational.ZERO)).isEqualTo(r);
        assertThat(BigRational.ZERO.add(r)).isEqualTo(r);
    }

    @Test
    void addIntToRational() {
        BigRational r = BigRational.of(1, 2);
        BigRational result = r.add(2);

        assertThat(result).isEqualTo(BigRational.of(5, 2));
    }

    @Test
    void addLongToRational() {
        BigRational r = BigRational.of(1, 3);
        BigRational result = r.add(3L);

        assertThat(result).isEqualTo(BigRational.of(10, 3));
    }

    @Test
    void addBigIntegerToRational() {
        BigRational r = BigRational.of(2, 5);
        BigRational result = r.add(BigInteger.valueOf(5));

        assertThat(result).isEqualTo(BigRational.of(27, 5));
    }

    @Test
    void addingNullRationalThrowsException() {
        BigRational r = BigRational.of(1, 2);

        assertThatThrownBy(() -> r.add((BigRational) null))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Subtraction Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1, 2, 1, 3, 1, 6",      // 1/2 - 1/3 = 1/6
            "3, 4, 1, 4, 1, 2",      // 3/4 - 1/4 = 1/2
            "2, 3, 1, 6, 1, 2",      // 2/3 - 1/6 = 1/2
            "1, 2, 1, 2, 0, 1",      // 1/2 - 1/2 = 0
            "-1, 2, 1, 3, -5, 6",    // -1/2 - 1/3 = -5/6
    })
    void subtractRationals(int n1, int d1, int n2, int d2, long expectedNum, long expectedDen) {
        BigRational r1 = BigRational.of(n1, d1);
        BigRational r2 = BigRational.of(n2, d2);

        BigRational result = r1.subtract(r2);

        assertThat(result.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(result.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @Test
    void subtractingZeroReturnsOriginal() {
        BigRational r = BigRational.of(3, 4);

        assertThat(r.subtract(BigRational.ZERO)).isEqualTo(r);
    }

    @Test
    void subtractIntFromRational() {
        BigRational r = BigRational.of(5, 2);
        BigRational result = r.subtract(2);

        assertThat(result).isEqualTo(BigRational.of(1, 2));
    }

    @Test
    void subtractLongFromRational() {
        BigRational r = BigRational.of(10, 3);
        BigRational result = r.subtract(3L);

        assertThat(result).isEqualTo(BigRational.of(1, 3));
    }

    @Test
    void subtractBigIntegerFromRational() {
        BigRational r = BigRational.of(27, 5);
        BigRational result = r.subtract(BigInteger.valueOf(5));

        assertThat(result).isEqualTo(BigRational.of(2, 5));
    }

    @Test
    void subtractingNullRationalThrowsException() {
        BigRational r = BigRational.of(1, 2);

        assertThatThrownBy(() -> r.subtract((BigRational) null))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Multiplication Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1, 2, 1, 3, 1, 6",      // 1/2 * 1/3 = 1/6
            "2, 3, 3, 4, 1, 2",      // 2/3 * 3/4 = 1/2
            "5, 7, 7, 5, 1, 1",      // 5/7 * 7/5 = 1
            "-1, 2, 2, 3, -1, 3",    // -1/2 * 2/3 = -1/3
            "-1, 2, -2, 3, 1, 3",    // -1/2 * -2/3 = 1/3
    })
    void multiplyRationals(int n1, int d1, int n2, int d2, long expectedNum, long expectedDen) {
        BigRational r1 = BigRational.of(n1, d1);
        BigRational r2 = BigRational.of(n2, d2);

        BigRational result = r1.multiply(r2);

        assertThat(result.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(result.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @Test
    void multiplyingByZeroReturnsZero() {
        BigRational r = BigRational.of(3, 4);

        assertThat(r.multiply(BigRational.ZERO)).isEqualTo(BigRational.ZERO);
        assertThat(BigRational.ZERO.multiply(r)).isEqualTo(BigRational.ZERO);
    }

    @Test
    void multiplyingByOneReturnsOriginal() {
        BigRational r = BigRational.of(3, 4);

        assertThat(r.multiply(BigRational.ONE)).isEqualTo(r);
        assertThat(BigRational.ONE.multiply(r)).isEqualTo(r);
    }

    @Test
    void multiplyIntByRational() {
        BigRational r = BigRational.of(2, 3);
        BigRational result = r.multiply(3);

        assertThat(result).isEqualTo(BigRational.of(2, 1));
    }

    @Test
    void multiplyLongByRational() {
        BigRational r = BigRational.of(3, 5);
        BigRational result = r.multiply(10L);

        assertThat(result).isEqualTo(BigRational.of(6, 1));
    }

    @Test
    void multiplyBigIntegerByRational() {
        BigRational r = BigRational.of(4, 7);
        BigRational result = r.multiply(BigInteger.valueOf(14));

        assertThat(result).isEqualTo(BigRational.of(8, 1));
    }

    @Test
    void multiplyingByNullRationalThrowsException() {
        BigRational r = BigRational.of(1, 2);

        assertThatThrownBy(() -> r.multiply((BigRational) null))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Division Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1, 2, 1, 3, 3, 2",      // (1/2) / (1/3) = 3/2
            "2, 3, 4, 5, 5, 6",      // (2/3) / (4/5) = 5/6
            "1, 1, 1, 1, 1, 1",      // 1 / 1 = 1
            "-1, 2, 2, 3, -3, 4",    // (-1/2) / (2/3) = -3/4
            "1, 2, -2, 3, -3, 4",    // (1/2) / (-2/3) = -3/4
    })
    void divideRationals(int n1, int d1, int n2, int d2, long expectedNum, long expectedDen) {
        BigRational r1 = BigRational.of(n1, d1);
        BigRational r2 = BigRational.of(n2, d2);

        BigRational result = r1.divide(r2);

        assertThat(result.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(result.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @Test
    void divideByZeroThrowsException() {
        BigRational r = BigRational.of(3, 4);

        assertThatThrownBy(() -> r.divide(BigRational.ZERO))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");

        assertThatThrownBy(() -> r.divide(0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");

        assertThatThrownBy(() -> r.divide(0L))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");

        assertThatThrownBy(() -> r.divide(BigInteger.ZERO))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");
    }

    @Test
    void divideIntByRational() {
        BigRational r = BigRational.of(2, 3);
        BigRational result = r.divide(2);

        assertThat(result).isEqualTo(BigRational.of(1, 3));
    }

    @Test
    void divideLongByRational() {
        BigRational r = BigRational.of(6, 5);
        BigRational result = r.divide(3L);

        assertThat(result).isEqualTo(BigRational.of(2, 5));
    }

    @Test
    void divideBigIntegerByRational() {
        BigRational r = BigRational.of(8, 7);
        BigRational result = r.divide(BigInteger.valueOf(4));

        assertThat(result).isEqualTo(BigRational.of(2, 7));
    }

    @Test
    void dividingByNullRationalThrowsException() {
        BigRational r = BigRational.of(1, 2);

        assertThatThrownBy(() -> r.divide((BigRational) null))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Power Tests ====================

    @ParameterizedTest
    @CsvSource({
            "2, 3, 0, 1, 1",        // (2/3)^0 = 1
            "2, 3, 1, 2, 3",        // (2/3)^1 = 2/3
            "2, 3, 2, 4, 9",        // (2/3)^2 = 4/9
            "2, 3, 3, 8, 27",       // (2/3)^3 = 8/27
            "3, 2, -1, 2, 3",       // (3/2)^-1 = 2/3
            "2, 3, -2, 9, 4",       // (2/3)^-2 = 9/4
            "1, 2, 5, 1, 32",       // (1/2)^5 = 1/32
    })
    void powWithIntExponent(int n, int d, int exponent, long expectedNum, long expectedDen) {
        BigRational r = BigRational.of(n, d);
        BigRational result = r.pow(exponent);

        assertThat(result.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(result.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @Test
    void powWithLongExponent() {
        BigRational r = BigRational.of(2, 3);
        BigRational result = r.pow(4L);

        assertThat(result).isEqualTo(BigRational.of(16, 81));
    }

    @Test
    void powWithBigIntegerExponent() {
        BigRational r = BigRational.of(3, 5);
        BigRational result = r.pow(BigInteger.valueOf(3));

        assertThat(result).isEqualTo(BigRational.of(27, 125));
    }

    @Test
    void powZeroToZeroIsOne() {
        assertThat(BigRational.ZERO.pow(0)).isEqualTo(BigRational.ONE);
    }

    @Test
    void powZeroToPositiveIsZero() {
        assertThat(BigRational.ZERO.pow(5)).isEqualTo(BigRational.ZERO);
        assertThat(BigRational.ZERO.pow(100L)).isEqualTo(BigRational.ZERO);
    }

    @Test
    void powZeroToNegativeThrowsException() {
        assertThatThrownBy(() -> BigRational.ZERO.pow(-1))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");

        assertThatThrownBy(() -> BigRational.ZERO.pow(-5L))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");

        assertThatThrownBy(() -> BigRational.ZERO.pow(BigInteger.valueOf(-2)))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");
    }

    @Test
    void powWithDoubleExponent() {
        BigRational r = BigRational.of(1, 2);
        double result = r.pow(0.5);  // Square root

        assertThat(result).isCloseTo(Math.sqrt(0.5), within(TOLERANCE));
    }

    @Test
    void powWithNullBigIntegerThrowsException() {
        BigRational r = BigRational.of(1, 2);

        assertThatThrownBy(() -> r.pow(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Other Operations ====================

    @ParameterizedTest
    @CsvSource({
            "3, 4, 3, 4",
            "-3, 4, 3, 4",
            "5, 7, 5, 7",
            "-5, 7, 5, 7",
            "0, 1, 0, 1",
    })
    void absReturnsAbsoluteValue(int n, int d, long expectedNum, long expectedDen) {
        BigRational r = BigRational.of(n, d);
        BigRational result = r.abs();

        assertThat(result.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(result.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @ParameterizedTest
    @CsvSource({
            "3, 4, -3, 4",
            "-3, 4, 3, 4",
            "0, 1, 0, 1",
    })
    void negateFlipsSign(int n, int d, long expectedNum, long expectedDen) {
        BigRational r = BigRational.of(n, d);
        BigRational result = r.negate();

        assertThat(result.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(result.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @ParameterizedTest
    @CsvSource({
            "3, 4, 4, 3",
            "1, 2, 2, 1",
            "5, 1, 1, 5",
            "-2, 3, -3, 2",
    })
    void reciprocalSwapsNumeratorAndDenominator(int n, int d, long expectedNum, long expectedDen) {
        BigRational r = BigRational.of(n, d);
        BigRational result = r.reciprocal();

        assertThat(result.getNumerator()).isEqualTo(BigInteger.valueOf(expectedNum));
        assertThat(result.getDenominator()).isEqualTo(BigInteger.valueOf(expectedDen));
    }

    @Test
    void reciprocalOfZeroThrowsException() {
        assertThatThrownBy(() -> BigRational.ZERO.reciprocal())
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("zero");
    }

    @Test
    void reduceReturnsThis() {
        BigRational r = BigRational.of(3, 4);

        // Since all rationals are already reduced, reduce() returns this
        assertThat(r.reduce()).isSameAs(r);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 4, 1",
            "0, 1, 0",
            "-3, 4, -1",
    })
    void signumReturnsCorrectSign(int n, int d, int expectedSignum) {
        BigRational r = BigRational.of(n, d);

        assertThat(r.signum()).isEqualTo(expectedSignum);
    }

    // ==================== Conversion Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1, 4, 25.0",
            "1, 2, 50.0",
            "3, 4, 75.0",
            "2, 5, 40.0",
    })
    void percentageValueConvertsCorrectly(int n, int d, double expectedPercentage) {
        BigRational r = BigRational.of(n, d);

        assertThat(r.percentageValue()).isCloseTo(expectedPercentage, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "7, 2, 3",
            "10, 3, 3",
            "-7, 2, -3",
            "1, 2, 0",
    })
    void intValueReturnsWholeNumberPart(int n, int d, int expected) {
        BigRational r = BigRational.of(n, d);

        assertThat(r.intValue()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 3, 33",
            "200, 7, 28",
            "-100, 3, -33",
    })
    void longValueReturnsWholeNumberPart(long n, long d, long expected) {
        BigRational r = BigRational.of(n, d);

        assertThat(r.longValue()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 0.5",
            "1, 4, 0.25",
            "3, 4, 0.75",
            "2, 3, 0.6666666666666666",
            "-1, 2, -0.5",
    })
    void floatValueConvertsCorrectly(int n, int d, float expected) {
        BigRational r = BigRational.of(n, d);

        assertThat(r.floatValue()).isCloseTo(expected, within((float) TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 0.5",
            "1, 4, 0.25",
            "3, 4, 0.75",
            "2, 3, 0.6666666666666666",
            "-1, 2, -0.5",
    })
    void doubleValueConvertsCorrectly(int n, int d, double expected) {
        BigRational r = BigRational.of(n, d);

        assertThat(r.doubleValue()).isCloseTo(expected, within(TOLERANCE));
    }

    // ==================== Comparison Tests ====================

    @Test
    void compareToWorksCorrectly() {
        BigRational half = BigRational.of(1, 2);
        BigRational third = BigRational.of(1, 3);
        BigRational anotherHalf = BigRational.of(2, 4);

        assertThat(half.compareTo(third)).isPositive();
        assertThat(third.compareTo(half)).isNegative();
        assertThat(half.compareTo(anotherHalf)).isZero();
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 3, 6, 1",       // 1/2 > 3/6 (both equal)
            "2, 3, 1, 3, 1",       // 2/3 > 1/3
            "1, 4, 1, 2, -1",      // 1/4 < 1/2
            "0, 1, 1, 5, -1",      // 0 < 1/5
            "-1, 2, 1, 2, -1",     // -1/2 < 1/2
    })
    void compareToWithDifferentValues(int n1, int d1, int n2, int d2, int expectedSign) {
        BigRational r1 = BigRational.of(n1, d1);
        BigRational r2 = BigRational.of(n2, d2);

        int result = r1.compareTo(r2);

        if (expectedSign > 0) {
            assertThat(result).isGreaterThanOrEqualTo(0);
        } else if (expectedSign < 0) {
            assertThat(result).isLessThan(0);
        } else {
            assertThat(result).isZero();
        }
    }

    @Test
    void compareToNullThrowsException() {
        BigRational r = BigRational.of(1, 2);

        assertThatThrownBy(() -> r.compareTo(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalsWorksCorrectly() {
        BigRational r1 = BigRational.of(1, 2);
        BigRational r2 = BigRational.of(2, 4);
        BigRational r3 = BigRational.of(1, 3);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1).isNotEqualTo(r3);
        assertThat(r1).isEqualTo(r1);
    }

    @Test
    void equalsHandlesNull() {
        BigRational r = BigRational.of(1, 2);

        assertThat(r.equals(null)).isFalse();
    }

    @Test
    void equalsHandlesDifferentTypes() {
        BigRational r = BigRational.of(1, 2);

        assertThat(r.equals("1/2")).isFalse();
        assertThat(r.equals(0.5)).isFalse();
    }

    @Test
    void hashCodeIsConsistent() {
        BigRational r1 = BigRational.of(1, 2);
        BigRational r2 = BigRational.of(2, 4);

        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    void equalRationalsHaveSameHashCode() {
        BigRational r1 = BigRational.of(3, 5);
        BigRational r2 = BigRational.of(6, 10);

        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    // ==================== String Representation Tests ====================

    @ParameterizedTest
    @CsvSource({
            "3, 4, '3/4'",
            "1, 2, '1/2'",
            "5, 1, '5'",
            "0, 1, '0'",
            "-3, 4, '-3/4'",
            "6, 2, '3'",
    })
    void toStringFormatsCorrectly(int n, int d, String expected) {
        BigRational r = BigRational.of(n, d);

        assertThat(r.toString()).isEqualTo(expected);
    }

    // ==================== Immutability Tests ====================

    @Test
    void rationalsAreImmutable() {
        BigRational r = BigRational.of(3, 4);

        BigInteger originalNum = r.getNumerator();
        BigInteger originalDen = r.getDenominator();

        // Perform various operations
        r.add(BigRational.ONE);
        r.subtract(BigRational.ONE);
        r.multiply(BigRational.TWO);
        r.divide(BigRational.TWO);
        r.pow(2);
        r.negate();
        r.abs();
        r.reciprocal();

        // Verify state unchanged
        assertThat(r.getNumerator()).isEqualTo(originalNum);
        assertThat(r.getDenominator()).isEqualTo(originalDen);
    }

    // ==================== Edge Cases Tests ====================

    @Test
    void largeNumbersAreHandledCorrectly() {
        BigInteger largeNum = new BigInteger("123456789012345678901234567890");
        BigInteger largeDen = new BigInteger("987654321098765432109876543210");

        BigRational r = BigRational.of(largeNum, largeDen);

        assertThat(r.getNumerator()).isNotNull();
        assertThat(r.getDenominator()).isNotNull();
    }

    @Test
    void arithmeticWithLargeNumbers() {
        BigRational r1 = BigRational.of(BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ONE);
        BigRational r2 = BigRational.of(BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ONE);

        BigRational sum = r1.add(r2);
        BigRational product = r1.multiply(r2);

        assertThat(sum.getNumerator()).isPositive();
        assertThat(product.getNumerator()).isPositive();
    }

    @Test
    void verySmallDenominatorConversion() {
        double value = 1.0 / 3.0;
        BigRational r = BigRational.of(value, 10);

        // Should approximate 1/3 with max denominator 10
        assertThat(r.getDenominator().longValue()).isLessThanOrEqualTo(10);
        assertThat(r.doubleValue()).isCloseTo(value, within(0.05));
    }

    @Test
    void chainedArithmeticOperations() {
        BigRational r = BigRational.of(1, 2)
                .add(BigRational.of(1, 3))
                .multiply(BigRational.of(2, 1))
                .subtract(BigRational.of(1, 6));

        // (1/2 + 1/3) * 2 - 1/6 = (5/6) * 2 - 1/6 = 10/6 - 1/6 = 9/6 = 3/2
        assertThat(r).isEqualTo(BigRational.of(3, 2));
    }

    @Test
    void mixedSignArithmetic() {
        BigRational positive = BigRational.of(3, 4);
        BigRational negative = BigRational.of(-1, 2);

        BigRational sum = positive.add(negative);
        BigRational diff = positive.subtract(negative);
        BigRational product = positive.multiply(negative);
        BigRational quotient = positive.divide(negative);

        assertThat(sum).isEqualTo(BigRational.of(1, 4));
        assertThat(diff).isEqualTo(BigRational.of(5, 4));
        assertThat(product).isEqualTo(BigRational.of(-3, 8));
        assertThat(quotient).isEqualTo(BigRational.of(-3, 2));
    }
}
