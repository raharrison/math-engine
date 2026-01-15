package uk.co.ryanharrison.mathengine.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for {@link MathUtils}.
 * <p>
 * Tests all mathematical utility functions including trigonometric, hyperbolic,
 * factorial, combinatorial, number theory, and utility functions.
 * </p>
 */
class MathUtilsTest {

    private static final double TOLERANCE = 1e-9;
    private static final double GAMMA_TOLERANCE = 1e-7;  // Relaxed for Gamma function approximations

    // ==================== Constants ====================

    @Test
    void constantSQTPI_IsSquareRootOf2Pi() {
        // Note: Despite the name, SQTPI is actually sqrt(2*PI), not sqrt(PI)
        // This is documented in the code as a backward compatibility issue
        double expected = Math.sqrt(2.0 * Math.PI);
        assertThat(MathUtils.SQTPI).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void constantLOGPI_IsNaturalLogOfPi() {
        double expected = Math.log(Math.PI);
        assertThat(MathUtils.LOGPI).isCloseTo(expected, within(TOLERANCE));
    }

    // ==================== Factorial Functions ====================

    @ParameterizedTest
    @CsvSource({
            "0, 1",         // 0! = 1
            "1, 1",         // 1! = 1
            "2, 2",         // 2! = 2
            "3, 6",         // 3! = 6
            "4, 24",        // 4! = 24
            "5, 120",       // 5! = 120
            "6, 720",       // 6! = 720
            "10, 3628800",  // 10! = 3628800
            "12, 479001600" // 12! = 479001600
    })
    void factorial_Long_ValidInput_ReturnsCorrectValue(long input, long expected) {
        assertThat(MathUtils.factorial(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -5, -10})
    void factorial_Long_NegativeInput_ThrowsException(long input) {
        assertThatThrownBy(() -> MathUtils.factorial(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Factorial requires non-negative integer");
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0",         // 0! = 1
            "1.0, 1.0",         // 1! = 1
            "5.0, 120.0",       // 5! = 120
            "10.0, 3628800.0"   // 10! = 3628800
    })
    void factorial_Double_IntegerInput_ReturnsExactValue(double input, double expected) {
        assertThat(MathUtils.factorial(input)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0.5, 0.886226925452758",   // 0.5! = Γ(1.5) = √π/2
            "1.5, 1.329340388179137",   // 1.5! = Γ(2.5)
            "2.5, 3.32335097044784"     // 2.5! = Γ(3.5)
    })
    void factorial_Double_FractionalInput_ReturnsGammaValue(double input, double expected) {
        assertThat(MathUtils.factorial(input)).isCloseTo(expected, within(GAMMA_TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1.0",       // 0!! = 1
            "1, 1.0",       // 1!! = 1
            "2, 2.0",       // 2!! = 2
            "3, 3.0",       // 3!! = 3
            "4, 8.0",       // 4!! = 4 × 2 = 8
            "5, 15.0",      // 5!! = 5 × 3 × 1 = 15
            "6, 48.0",      // 6!! = 6 × 4 × 2 = 48
            "7, 105.0",     // 7!! = 7 × 5 × 3 × 1 = 105
            "8, 384.0",     // 8!! = 8 × 6 × 4 × 2 = 384
            "10, 3840.0"    // 10!! = 10 × 8 × 6 × 4 × 2 = 3840
    })
    void doubleFactorial_Long_ValidInput_ReturnsCorrectValue(long input, double expected) {
        assertThat(MathUtils.doubleFactorial(input)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, -5, -10})
    void doubleFactorial_Long_NegativeInput_ThrowsException(long input) {
        assertThatThrownBy(() -> MathUtils.doubleFactorial(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Double factorial requires non-negative integer");
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0",         // 0!! = 1
            "1.0, 1.0",         // 1!! = 1
            "5.0, 15.0",        // 5!! = 15
            "8.0, 384.0"        // 8!! = 384
    })
    void doubleFactorial_Double_IntegerInput_ReturnsExactValue(double input, double expected) {
        assertThat(MathUtils.doubleFactorial(input)).isCloseTo(expected, within(TOLERANCE));
    }

    // ==================== Combinatorial Functions ====================

    // Positive integer tests
    @ParameterizedTest
    @CsvSource({
            "5, 0, 1.0",
            "5, 1, 5.0",
            "5, 2, 10.0",
            "5, 3, 10.0",
            "5, 5, 1.0",
            "10, 3, 120.0",
            "10, 5, 252.0",
            "20, 10, 184756.0",
            "0, 0, 1.0"
    })
    void combination_NonNegativeInputs_ReturnsCorrectValue(double n, double k, double expected) {
        assertThat(MathUtils.combination(n, k)).isCloseTo(expected, within(GAMMA_TOLERANCE));
    }

    // Negative k
    @ParameterizedTest
    @CsvSource({
            "5, -1",
            "10, -5"
    })
    void combination_NegativeK_ReturnsZero(double n, double k) {
        assertThat(MathUtils.combination(n, k)).isEqualTo(0.0);
    }

    // Negative integer n
    @ParameterizedTest
    @CsvSource({
            "-5, 2, 15.0",
            "-10, 3, -220.0"
    })
    void combination_NegativeN_IntegerK_ReturnsCorrectValue(double n, double k, double expected) {
        assertThat(MathUtils.combination(n, k)).isCloseTo(expected, within(GAMMA_TOLERANCE));
    }

    // Negative non-integer n
    @Test
    void combination_NegativeN_FractionalK_ReturnsNaN() {
        assertThat(MathUtils.combination(-5.0, 2.5)).isNaN();
    }

    // Fractional n and k using gamma function
    @Test
    void combination_FractionalNAndK_ReturnsGammaResult() {
        double result = MathUtils.combination(5.5, 2.2);
        assertThat(result).isNotNaN();
        assertThat(result).isGreaterThan(0);
    }

    @ParameterizedTest
    @CsvSource({
            "5, 0, 1.0",        // P(5,0) = 1
            "5, 1, 5.0",        // P(5,1) = 5
            "5, 2, 20.0",       // P(5,2) = 20
            "5, 3, 60.0",       // P(5,3) = 60
            "5, 5, 120.0",      // P(5,5) = 5!
            "10, 3, 720.0",     // P(10,3) = 720
            "10, 5, 30240.0"    // P(10,5) = 30240
    })
    void permutation_ReturnsCorrectValue(double n, double r, double expected) {
        assertThat(MathUtils.permutation(n, r)).isCloseTo(expected, within(GAMMA_TOLERANCE));
    }

    // ==================== Number Theory Functions ====================

    @ParameterizedTest
    @CsvSource({
            "12, 18, 6",        // gcd(12, 18) = 6
            "48, 18, 6",        // gcd(48, 18) = 6
            "100, 50, 50",      // gcd(100, 50) = 50
            "17, 19, 1",        // gcd(17, 19) = 1 (coprime)
            "0, 5, 5",          // gcd(0, 5) = 5
            "5, 0, 5",          // gcd(5, 0) = 5
            "0, 0, 0",          // gcd(0, 0) = 0
            "-12, 18, 6",       // gcd(-12, 18) = 6
            "12, -18, 6",       // gcd(12, -18) = 6
            "-12, -18, 6"       // gcd(-12, -18) = 6
    })
    void gcd_ReturnsCorrectValue(int x, int y, int expected) {
        assertThat(MathUtils.gcd(x, y)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "4, 6, 12",         // lcm(4, 6) = 12
            "12, 18, 36",       // lcm(12, 18) = 36
            "21, 6, 42",        // lcm(21, 6) = 42
            "5, 7, 35",         // lcm(5, 7) = 35 (coprime)
            "10, 15, 30",       // lcm(10, 15) = 30
            "0, 5, 0",          // lcm(0, 5) = 0
            "5, 0, 0",          // lcm(5, 0) = 0
            "0, 0, 0",          // lcm(0, 0) = 0
            "-4, 6, 12",        // lcm(-4, 6) = 12
            "4, -6, 12",        // lcm(4, -6) = 12
            "-4, -6, 12"        // lcm(-4, -6) = 12
    })
    void lcm_ReturnsCorrectValue(long a, long b, long expected) {
        assertThat(MathUtils.lcm(a, b)).isEqualTo(expected);
    }

    @Test
    void greatestCommonFactor_SingleElement_ReturnsAbsoluteValue() {
        assertThat(MathUtils.greatestCommonFactor(new int[]{42})).isEqualTo(42);
        assertThat(MathUtils.greatestCommonFactor(new int[]{-42})).isEqualTo(42);
    }

    @Test
    void greatestCommonFactor_MultipleElements_ReturnsGCF() {
        assertThat(MathUtils.greatestCommonFactor(new int[]{12, 18, 24})).isEqualTo(6);
        assertThat(MathUtils.greatestCommonFactor(new int[]{100, 50, 150})).isEqualTo(50);
        assertThat(MathUtils.greatestCommonFactor(new int[]{7, 14, 21, 28})).isEqualTo(7);
    }

    @Test
    void greatestCommonFactor_CoprimeNumbers_ReturnsOne() {
        assertThat(MathUtils.greatestCommonFactor(new int[]{13, 17, 19})).isEqualTo(1);
    }

    @Test
    void greatestCommonFactor_WithNegativeNumbers_ReturnsGCF() {
        assertThat(MathUtils.greatestCommonFactor(new int[]{-12, 18, -24})).isEqualTo(6);
    }

    @Test
    void greatestCommonFactor_NullArray_ThrowsException() {
        assertThatThrownBy(() -> MathUtils.greatestCommonFactor(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data array cannot be null or empty");
    }

    @Test
    void greatestCommonFactor_EmptyArray_ThrowsException() {
        assertThatThrownBy(() -> MathUtils.greatestCommonFactor(new int[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data array cannot be null or empty");
    }

    @Test
    void lowestCommonMultiple_SingleElement_ReturnsAbsoluteValue() {
        assertThat(MathUtils.lowestCommonMultiple(new int[]{42})).isEqualTo(42);
        assertThat(MathUtils.lowestCommonMultiple(new int[]{-42})).isEqualTo(42);
    }

    @Test
    void lowestCommonMultiple_MultipleElements_ReturnsLCM() {
        assertThat(MathUtils.lowestCommonMultiple(new int[]{4, 6})).isEqualTo(12);
        assertThat(MathUtils.lowestCommonMultiple(new int[]{12, 18, 24})).isEqualTo(72);
        assertThat(MathUtils.lowestCommonMultiple(new int[]{5, 10, 15})).isEqualTo(30);
    }

    @Test
    void lowestCommonMultiple_WithZero_ReturnsZero() {
        assertThat(MathUtils.lowestCommonMultiple(new int[]{0, 5})).isEqualTo(0);
        assertThat(MathUtils.lowestCommonMultiple(new int[]{4, 0, 6})).isEqualTo(0);
    }

    @Test
    void lowestCommonMultiple_WithNegativeNumbers_ReturnsLCM() {
        assertThat(MathUtils.lowestCommonMultiple(new int[]{-4, 6})).isEqualTo(12);
        assertThat(MathUtils.lowestCommonMultiple(new int[]{-12, -18})).isEqualTo(36);
    }

    @Test
    void lowestCommonMultiple_NullArray_ThrowsException() {
        assertThatThrownBy(() -> MathUtils.lowestCommonMultiple(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data array cannot be null or empty");
    }

    @Test
    void lowestCommonMultiple_EmptyArray_ThrowsException() {
        assertThatThrownBy(() -> MathUtils.lowestCommonMultiple(new int[]{}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data array cannot be null or empty");
    }

    // ==================== Utility Functions ====================

    @ParameterizedTest
    @CsvSource({
            "3.7, 0.7",         // fPart(3.7) = 0.7
            "5.0, 0.0",         // fPart(5.0) = 0.0
            "0.25, 0.25",       // fPart(0.25) = 0.25
            "-2.3, 0.7",        // fPart(-2.3) = 0.7 (FIXED BUG: was returning -0.3)
            "-5.8, 0.2",        // fPart(-5.8) = 0.2 (FIXED BUG: was returning -0.8)
            "0.0, 0.0"          // fPart(0.0) = 0.0
    })
    void fPart_ReturnsCorrectFractionalPart(double input, double expected) {
        assertThat(MathUtils.fPart(input)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void fPart_AlwaysReturnsNonNegative() {
        // Verify the fixed behavior: fPart always returns [0, 1)
        for (double x = -10.0; x <= 10.0; x += 0.7) {
            double result = MathUtils.fPart(x);
            assertThat(result).isGreaterThanOrEqualTo(0.0);
            assertThat(result).isLessThan(1.0);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "3.0, 4.0, 5.0",            // 3-4-5 right triangle
            "5.0, 12.0, 13.0",          // 5-12-13 right triangle
            "0.0, 0.0, 0.0",            // zero
            "1.0, 0.0, 1.0",            // one side zero
            "0.0, 1.0, 1.0",            // other side zero
            "1.0, 1.0, 1.4142135623730951"  // √2
    })
    void hypot_ReturnsCorrectHypotenuse(double a, double b, double expected) {
        assertThat(MathUtils.hypot(a, b)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "8.0, 3.0, 2.0",            // ∛8 = 2
            "27.0, 3.0, 3.0",           // ∛27 = 3
            "16.0, 2.0, 4.0",           // √16 = 4
            "100.0, 2.0, 10.0",         // √100 = 10
            "32.0, 5.0, 2.0",           // 5√32 = 2
            "1.0, 5.0, 1.0",            // n√1 = 1
            "0.0, 2.0, 0.0"             // √0 = 0
    })
    void root_ReturnsCorrectNthRoot(double num, double n, double expected) {
        assertThat(MathUtils.root(num, n)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "3.14159, 0, 3.0",          // 0 places
            "3.14159, 1, 3.1",          // 1 place
            "3.14159, 2, 3.14",         // 2 places
            "3.14159, 3, 3.142",        // 3 places
            "3.14159, 4, 3.1416",       // 4 places (rounds up)
            "2.5, 0, 3.0",              // HALF_UP: 2.5 rounds to 3
            "2.4, 0, 2.0",              // 2.4 rounds to 2
            "-3.14159, 2, -3.14",       // negative number
            "0.0, 3, 0.0"               // zero
    })
    void round_ReturnsCorrectlyRoundedValue(double number, int places, double expected) {
        assertThat(MathUtils.round(number, places)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void round_NegativePlaces_TreatedAsAbsolute() {
        // Negative places should be treated as absolute value
        assertThat(MathUtils.round(3.14159, -2)).isCloseTo(3.14, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, 1.0",         // positive
            "0.0, 1.0",         // zero (returns 1.0)
            "-5.0, -1.0",       // negative
            "0.001, 1.0",       // small positive
            "-0.001, -1.0"      // small negative
    })
    void sign_ReturnsCorrectSign(double input, double expected) {
        assertThat(MathUtils.sign(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "1000.0, 5.0, 10.0, 1628.894626777442",    // $1000 at 5% for 10 years
            "5000.0, 3.5, 5.0, 5938.431528234372",     // $5000 at 3.5% for 5 years
            "10000.0, 2.0, 20.0, 14859.473959783549",  // $10000 at 2% for 20 years
            "100.0, 10.0, 1.0, 110.0",                 // $100 at 10% for 1 year
            "1000.0, 0.0, 10.0, 1000.0"                // 0% interest = no change
    })
    void compoundInterest_ReturnsCorrectAmount(double principal, double rate, double years, double expected) {
        assertThat(MathUtils.compoundInterest(principal, rate, years)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, 5.0, 0.0, true",              // exactly equal
            "5.0, 5.1, 0.2, true",              // within tolerance
            "5.0, 5.19, 0.2, true",             // within tolerance (avoid exact boundary)
            "5.0, 5.21, 0.2, false",            // outside tolerance
            "10.0, 10.049, 0.05, true",         // within tolerance (avoid exact boundary)
            "10.0, 10.051, 0.05, false",        // outside tolerance
            "-5.0, -5.1, 0.2, true",            // negative numbers
            "0.0, 0.0, 0.0, true"               // zeros
    })
    void isEqualWithinLimits_ReturnsCorrectComparison(double x, double y, double tolerance, boolean expected) {
        assertThat(MathUtils.isEqualWithinLimits(x, y, tolerance)).isEqualTo(expected);
    }

    @Test
    void isEqualWithinLimits_NegativeTolerance_UsesAbsoluteValue() {
        // Negative tolerance should be treated as absolute value
        assertThat(MathUtils.isEqualWithinLimits(5.0, 5.1, -0.2)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "100.0, 100.0, 1.0, true",          // exactly equal
            "100.0, 101.0, 2.0, true",          // within 2% of average (100.5)
            "100.0, 105.0, 5.0, true",          // within 5%
            "100.0, 106.0, 5.0, false",         // outside 5%
            "50.0, 52.0, 5.0, true",            // within tolerance
            "-100.0, -101.0, 2.0, true",        // negative numbers
            "0.0, 0.0, 10.0, true"              // zeros
    })
    void isEqualWithinPerCent_ReturnsCorrectComparison(double x, double y, double percentage, boolean expected) {
        assertThat(MathUtils.isEqualWithinPerCent(x, y, percentage)).isEqualTo(expected);
    }

    // ==================== Edge Cases and Special Values ====================

    @Test
    void factorial_LargeValues_DoesNotOverflow() {
        // For long factorial, verify it doesn't overflow inappropriately
        long result = MathUtils.factorial(20L);
        assertThat(result).isEqualTo(2432902008176640000L);
    }

    @Test
    void combination_SymmetryProperty() {
        // Verify: C(n,k) = C(n,n-k)
        assertThat(MathUtils.combination(10, 3)).isCloseTo(MathUtils.combination(10, 7), within(GAMMA_TOLERANCE));
        assertThat(MathUtils.combination(20, 5)).isCloseTo(MathUtils.combination(20, 15), within(GAMMA_TOLERANCE));
    }

    @Test
    void gcd_Commutativity() {
        // Verify: gcd(a,b) = gcd(b,a)
        assertThat(MathUtils.gcd(48, 18)).isEqualTo(MathUtils.gcd(18, 48));
        assertThat(MathUtils.gcd(100, 35)).isEqualTo(MathUtils.gcd(35, 100));
    }

    @Test
    void root_SquareRootMatchesMathSqrt() {
        // Verify: root(x, 2) = sqrt(x)
        double x = 25.0;
        assertThat(MathUtils.root(x, 2.0)).isCloseTo(Math.sqrt(x), within(TOLERANCE));
    }

    // ==================== Value Clamping and Range Functions ====================

    @ParameterizedTest
    @CsvSource({
            "5.0, 0.0, 10.0, 5.0",       // value in range
            "-5.0, 0.0, 10.0, 0.0",      // value below min
            "15.0, 0.0, 10.0, 10.0",     // value above max
            "0.0, 0.0, 10.0, 0.0",       // value equals min
            "10.0, 0.0, 10.0, 10.0",     // value equals max
            "5.5, 5.5, 5.5, 5.5",        // min equals max equals value
            "-100.0, -50.0, 50.0, -50.0", // negative range, below
            "100.0, -50.0, 50.0, 50.0",  // negative range, above
            "0.0, -50.0, 50.0, 0.0"      // negative range, in range
    })
    void clamp_ValidInput_ReturnsClampedValue(double value, double min, double max, double expected) {
        assertThat(MathUtils.clamp(value, min, max)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "10.0, 0.0, 0.0",            // min > max
            "-5.0, -10.0, 0.0",          // min > max (negative)
            "100.0, 50.0, 5.0"           // min > max (large values)
    })
    void clamp_MinGreaterThanMax_ThrowsException(double min, double max, double value) {
        assertThatThrownBy(() -> MathUtils.clamp(value, min, max))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Min cannot be greater than max");
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 10.0, 0.0, 0.0",       // t=0 returns start
            "0.0, 10.0, 1.0, 10.0",      // t=1 returns end
            "0.0, 10.0, 0.5, 5.0",       // t=0.5 returns midpoint
            "0.0, 10.0, 0.25, 2.5",      // t=0.25
            "0.0, 10.0, 0.75, 7.5",      // t=0.75
            "5.0, 15.0, 0.5, 10.0",      // non-zero start
            "-10.0, 10.0, 0.5, 0.0",     // negative start
            "100.0, 200.0, 0.3, 130.0",  // larger values
            "0.0, 10.0, -0.5, -5.0",     // extrapolation (t<0)
            "0.0, 10.0, 1.5, 15.0"       // extrapolation (t>1)
    })
    void lerp_ReturnsCorrectInterpolation(double start, double end, double t, double expected) {
        assertThat(MathUtils.lerp(start, end, t)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void lerp_IsLinear() {
        // Verify linearity: lerp is linear in t
        double start = 0.0;
        double end = 100.0;
        for (double t = 0.0; t <= 1.0; t += 0.1) {
            double expected = start + t * (end - start);
            assertThat(MathUtils.lerp(start, end, t)).isCloseTo(expected, within(TOLERANCE));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 10.0, 0.0, 0.0",       // value=start returns 0
            "0.0, 10.0, 10.0, 1.0",      // value=end returns 1
            "0.0, 10.0, 5.0, 0.5",       // midpoint returns 0.5
            "0.0, 10.0, 2.5, 0.25",      // quarter point
            "0.0, 10.0, 7.5, 0.75",      // three-quarter point
            "5.0, 15.0, 10.0, 0.5",      // non-zero start
            "-10.0, 10.0, 0.0, 0.5",     // negative start, value at zero
            "100.0, 200.0, 130.0, 0.3",  // larger values
            "0.0, 10.0, -5.0, -0.5",     // extrapolation (value<start)
            "0.0, 10.0, 15.0, 1.5"       // extrapolation (value>end)
    })
    void inverseLerp_ReturnsCorrectParameter(double start, double end, double value, double expected) {
        assertThat(MathUtils.inverseLerp(start, end, value)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void inverseLerp_StartEqualsEnd_ThrowsException() {
        assertThatThrownBy(() -> MathUtils.inverseLerp(5.0, 5.0, 7.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start and end cannot be equal");
    }

    @Test
    void inverseLerp_IsInverseOfLerp() {
        // Verify: inverseLerp(start, end, lerp(start, end, t)) = t
        double start = 10.0;
        double end = 50.0;
        for (double t = -1.0; t <= 2.0; t += 0.1) {
            double value = MathUtils.lerp(start, end, t);
            double recovered = MathUtils.inverseLerp(start, end, value);
            assertThat(recovered).isCloseTo(t, within(TOLERANCE));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, 0.0, 10.0, 0.0, 100.0, 50.0",      // map 5 from [0,10] to [0,100] = 50
            "7.5, 0.0, 10.0, 0.0, 100.0, 75.0",      // map 7.5 from [0,10] to [0,100] = 75
            "0.0, 0.0, 10.0, 0.0, 100.0, 0.0",       // map minimum
            "10.0, 0.0, 10.0, 0.0, 100.0, 100.0",    // map maximum
            "5.0, 0.0, 10.0, 100.0, 200.0, 150.0",   // map to different range
            "-5.0, -10.0, 0.0, 0.0, 100.0, 50.0",    // negative source range
            "5.0, 0.0, 10.0, -50.0, 50.0, 0.0",      // negative target range
            "15.0, 0.0, 10.0, 0.0, 100.0, 150.0",    // extrapolation (above)
            "-5.0, 0.0, 10.0, 0.0, 100.0, -50.0",    // extrapolation (below)
            "50.0, 0.0, 100.0, 32.0, 212.0, 122.0"   // Celsius to Fahrenheit (50C = 122F)
    })
    void remap_ReturnsCorrectMapping(double value, double fromMin, double fromMax, double toMin, double toMax, double expected) {
        assertThat(MathUtils.remap(value, fromMin, fromMax, toMin, toMax)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void remap_FromMinEqualsFromMax_ThrowsException() {
        assertThatThrownBy(() -> MathUtils.remap(5.0, 10.0, 10.0, 0.0, 100.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source range cannot have zero width");
    }

    @Test
    void remap_InverseMapping() {
        // Verify: mapping and reverse mapping are inverses
        double value = 37.5;
        double result = MathUtils.remap(value, 0.0, 100.0, 0.0, 1.0);
        double recovered = MathUtils.remap(result, 0.0, 1.0, 0.0, 100.0);
        assertThat(recovered).isCloseTo(value, within(TOLERANCE));
    }

    // ==================== Angle Conversion Functions ====================

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.0",                  // 0° normalizes to 0°
            "45.0, 45.0",                // 45° normalizes to 45°
            "180.0, 180.0",              // 180° normalizes to 180°
            "270.0, 270.0",              // 270° normalizes to 270°
            "360.0, 0.0",                // 360° normalizes to 0°
            "450.0, 90.0",               // 450° normalizes to 90°
            "720.0, 0.0",                // 720° normalizes to 0°
            "-90.0, 270.0",              // -90° normalizes to 270°
            "-180.0, 180.0",             // -180° normalizes to 180°
            "-270.0, 90.0",              // -270° normalizes to 90°
            "-360.0, 0.0",               // -360° normalizes to 0°
            "-450.0, 270.0",             // -450° normalizes to 270°
            "1000.0, 280.0"              // 1000° normalizes to 280°
    })
    void normalizeDegrees_ReturnsAngleInZeroTo360(double degrees, double expected) {
        assertThat(MathUtils.normalizeDegrees(degrees)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void normalizeDegrees_AlwaysReturnsValidRange() {
        // Verify all results are in [0, 360)
        for (double degrees = -1000.0; degrees <= 1000.0; degrees += 37.3) {
            double normalized = MathUtils.normalizeDegrees(degrees);
            assertThat(normalized).isGreaterThanOrEqualTo(0.0);
            assertThat(normalized).isLessThan(360.0);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.0",                              // 0 rad normalizes to 0 rad
            "1.5707963267948966, 1.5707963267948966", // π/2 rad normalizes to π/2 rad
            "3.141592653589793, 3.141592653589793",  // π rad normalizes to π rad
            "6.283185307179586, 0.0",                // 2π rad normalizes to 0 rad
            "7.853981633974483, 1.5707963267948966", // 5π/2 rad normalizes to π/2 rad
            "12.566370614359172, 0.0",               // 4π rad normalizes to 0 rad
            "-1.5707963267948966, 4.71238898038469", // -π/2 rad normalizes to 3π/2 rad
            "-3.141592653589793, 3.141592653589793", // -π rad normalizes to π rad
            "-6.283185307179586, 0.0",               // -2π rad normalizes to 0 rad
            "10.0, 3.7168146928204138"               // 10 rad normalizes
    })
    void normalizeRadians_ReturnsAngleInZeroTo2Pi(double radians, double expected) {
        assertThat(MathUtils.normalizeRadians(radians)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void normalizeRadians_AlwaysReturnsValidRange() {
        // Verify all results are in [0, 2π)
        double twoPi = 2.0 * Math.PI;
        for (double radians = -50.0; radians <= 50.0; radians += 1.3) {
            double normalized = MathUtils.normalizeRadians(radians);
            assertThat(normalized).isGreaterThanOrEqualTo(0.0);
            assertThat(normalized).isLessThan(twoPi);
        }
    }

    // ==================== Distance Functions ====================

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.0, 0.0, 0.0, 0.0",               // same point
            "0.0, 0.0, 3.0, 4.0, 5.0",               // 3-4-5 triangle
            "0.0, 0.0, 5.0, 12.0, 13.0",             // 5-12-13 triangle
            "1.0, 1.0, 4.0, 5.0, 5.0",               // shifted 3-4-5 triangle
            "0.0, 0.0, 1.0, 0.0, 1.0",               // horizontal distance
            "0.0, 0.0, 0.0, 1.0, 1.0",               // vertical distance
            "-3.0, -4.0, 0.0, 0.0, 5.0",             // negative coordinates
            "10.0, 20.0, 10.0, 20.0, 0.0",           // same point (non-origin)
            "-5.0, -5.0, 5.0, 5.0, 14.142135623730951" // diagonal across quadrants (10√2)
    })
    void distance_2D_ReturnsCorrectDistance(double x1, double y1, double x2, double y2, double expected) {
        assertThat(MathUtils.distance(x1, y1, x2, y2)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void distance_2D_IsCommutative() {
        // Verify: distance(A, B) = distance(B, A)
        assertThat(MathUtils.distance(1.0, 2.0, 5.0, 7.0))
                .isCloseTo(MathUtils.distance(5.0, 7.0, 1.0, 2.0), within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0",     // same point
            "0.0, 0.0, 0.0, 3.0, 4.0, 0.0, 5.0",     // 3-4-5 in y-z plane
            "0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.7320508075688772", // √3 diagonal
            "1.0, 2.0, 3.0, 4.0, 6.0, 7.0, 6.4031242374328485",  // general case
            "0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0",     // x-axis distance
            "0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0",     // y-axis distance
            "0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0",     // z-axis distance
            "-1.0, -1.0, -1.0, 1.0, 1.0, 1.0, 3.4641016151377544" // 2√3 diagonal
    })
    void distance_3D_ReturnsCorrectDistance(double x1, double y1, double z1, double x2, double y2, double z2, double expected) {
        assertThat(MathUtils.distance(x1, y1, z1, x2, y2, z2)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void distance_3D_IsCommutative() {
        // Verify: distance(A, B) = distance(B, A)
        assertThat(MathUtils.distance(1.0, 2.0, 3.0, 5.0, 7.0, 9.0))
                .isCloseTo(MathUtils.distance(5.0, 7.0, 9.0, 1.0, 2.0, 3.0), within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.0, 0.0, 0.0, 0.0",               // same point
            "0.0, 0.0, 3.0, 4.0, 7.0",               // Manhattan distance = 7
            "0.0, 0.0, 5.0, 12.0, 17.0",             // Manhattan distance = 17
            "1.0, 1.0, 4.0, 5.0, 7.0",               // shifted coordinates
            "0.0, 0.0, 1.0, 0.0, 1.0",               // horizontal
            "0.0, 0.0, 0.0, 1.0, 1.0",               // vertical
            "-3.0, -4.0, 0.0, 0.0, 7.0",             // negative coordinates
            "10.0, 20.0, 10.0, 20.0, 0.0",           // same point (non-origin)
            "-5.0, -5.0, 5.0, 5.0, 20.0",            // across quadrants
            "2.0, 3.0, 7.0, 15.0, 17.0"              // general case
    })
    void manhattanDistance_ReturnsCorrectDistance(double x1, double y1, double x2, double y2, double expected) {
        assertThat(MathUtils.manhattanDistance(x1, y1, x2, y2)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void manhattanDistance_IsCommutative() {
        // Verify: manhattanDistance(A, B) = manhattanDistance(B, A)
        assertThat(MathUtils.manhattanDistance(1.0, 2.0, 5.0, 7.0))
                .isCloseTo(MathUtils.manhattanDistance(5.0, 7.0, 1.0, 2.0), within(TOLERANCE));
    }

    @Test
    void manhattanDistance_IsGreaterOrEqualToEuclideanDistance() {
        // Manhattan distance is always >= Euclidean distance
        for (int i = 0; i < 10; i++) {
            double x1 = i * 3.5 - 10.0;
            double y1 = i * 2.3 - 5.0;
            double x2 = i * 1.7 + 5.0;
            double y2 = i * 4.1 - 8.0;

            double manhattan = MathUtils.manhattanDistance(x1, y1, x2, y2);
            double euclidean = MathUtils.distance(x1, y1, x2, y2);

            assertThat(manhattan).isGreaterThanOrEqualTo(euclidean - TOLERANCE);
        }
    }

    // ==================== Logarithm with Custom Base ====================

    @ParameterizedTest
    @CsvSource({
            "8.0, 2.0, 3.0",                // log₂(8) = 3
            "100.0, 10.0, 2.0",             // log₁₀(100) = 2
            "1000.0, 10.0, 3.0",            // log₁₀(1000) = 3
            "16.0, 2.0, 4.0",               // log₂(16) = 4
            "27.0, 3.0, 3.0",               // log₃(27) = 3
            "1.0, 10.0, 0.0",               // log₁₀(1) = 0
            "1.0, 2.0, 0.0",                // log₂(1) = 0
            "2.718281828459045, 2.718281828459045, 1.0",  // logₑ(e) = 1
            "4.0, 2.0, 2.0",                // log₂(4) = 2
            "32.0, 2.0, 5.0",               // log₂(32) = 5
            "81.0, 3.0, 4.0",               // log₃(81) = 4
            "1024.0, 2.0, 10.0"             // log₂(1024) = 10
    })
    void logBase_ValidInput_ReturnsCorrectValue(double value, double base, double expected) {
        assertThat(MathUtils.logBase(value, base)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -10.0, -0.001})
    void logBase_NonPositiveValue_ThrowsException(double value) {
        assertThatThrownBy(() -> MathUtils.logBase(value, 10.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Value must be positive");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -10.0, 1.0})
    void logBase_InvalidBase_ThrowsException(double base) {
        assertThatThrownBy(() -> MathUtils.logBase(100.0, base))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Base must be positive and not equal to 1");
    }

    @Test
    void logBase_ChangeOfBaseFormula() {
        // Verify: logₐ(x) = ln(x) / ln(a)
        double value = 256.0;
        double base = 4.0;

        double result = MathUtils.logBase(value, base);
        double expected = Math.log(value) / Math.log(base);

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void logBase_IdentityProperty() {
        // Verify: logₐ(a) = 1
        assertThat(MathUtils.logBase(2.0, 2.0)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(MathUtils.logBase(10.0, 10.0)).isCloseTo(1.0, within(TOLERANCE));
        assertThat(MathUtils.logBase(5.0, 5.0)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void logBase_PowerProperty() {
        // Verify: logₐ(aⁿ) = n
        double base = 3.0;
        for (int n = 0; n <= 5; n++) {
            double value = Math.pow(base, n);
            assertThat(MathUtils.logBase(value, base)).isCloseTo(n, within(TOLERANCE));
        }
    }

    // ==================== Integration Tests ====================

    @Test
    void clampAndRemap_Integration() {
        // Test using clamp followed by map
        double value = 150.0;
        double clamped = MathUtils.clamp(value, 0.0, 100.0);  // clamps to 100
        double mapped = MathUtils.remap(clamped, 0.0, 100.0, 0.0, 1.0);  // maps to 1.0

        assertThat(clamped).isCloseTo(100.0, within(TOLERANCE));
        assertThat(mapped).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void lerpAndInverseLerp_RoundTrip() {
        // Verify round-trip: value -> lerp -> inverseLerp -> value
        double start = 10.0;
        double end = 50.0;

        for (double t = 0.0; t <= 1.0; t += 0.1) {
            double value = MathUtils.lerp(start, end, t);
            double recoveredT = MathUtils.inverseLerp(start, end, value);
            double recoveredValue = MathUtils.lerp(start, end, recoveredT);

            assertThat(recoveredT).isCloseTo(t, within(TOLERANCE));
            assertThat(recoveredValue).isCloseTo(value, within(TOLERANCE));
        }
    }

    @Test
    void distanceComparison_EuclideanVsManhattan() {
        // For axis-aligned movement, Manhattan = Euclidean
        assertThat(MathUtils.distance(0.0, 0.0, 5.0, 0.0))
                .isCloseTo(MathUtils.manhattanDistance(0.0, 0.0, 5.0, 0.0), within(TOLERANCE));

        // For diagonal movement, Manhattan > Euclidean
        double euclidean = MathUtils.distance(0.0, 0.0, 3.0, 4.0);  // 5.0
        double manhattan = MathUtils.manhattanDistance(0.0, 0.0, 3.0, 4.0);  // 7.0

        assertThat(manhattan).isGreaterThan(euclidean);
    }
}
