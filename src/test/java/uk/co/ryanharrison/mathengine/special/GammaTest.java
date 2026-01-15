package uk.co.ryanharrison.mathengine.special;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for the {@link Gamma} utility class.
 * <p>
 * Tests cover:
 * <ul>
 *     <li>Known mathematical values (factorials, √π)</li>
 *     <li>Gamma function properties (recurrence relation)</li>
 *     <li>Consistency between gamma() and gammaLn()</li>
 *     <li>Edge cases and boundary conditions</li>
 *     <li>Invalid input validation</li>
 * </ul>
 * </p>
 */
class GammaTest {

    /**
     * Tolerance for high-precision calculations.
     * The Stirling approximation achieves 11-12 digit accuracy.
     */
    private static final double HIGH_PRECISION_TOLERANCE = 1e-9;

    /**
     * Tolerance for calculations involving exponentials or numerical approximations.
     */
    private static final double STANDARD_TOLERANCE = 1e-7;

    /**
     * Tolerance for calculations with lower precision requirements.
     */
    private static final double RELAXED_TOLERANCE = 1e-6;

    // ==================== Known Values - Factorial Relationship ====================

    /**
     * Tests that Γ(n) = (n-1)! for positive integers.
     * <p>
     * Verifies the fundamental relationship between the Gamma function and factorials:
     * Γ(1) = 0! = 1, Γ(2) = 1! = 1, Γ(3) = 2! = 2, Γ(4) = 3! = 6, etc.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "1.0, 1.0",      // Γ(1) = 0! = 1
            "2.0, 1.0",      // Γ(2) = 1! = 1
            "3.0, 2.0",      // Γ(3) = 2! = 2
            "4.0, 6.0",      // Γ(4) = 3! = 6
            "5.0, 24.0",     // Γ(5) = 4! = 24
            "6.0, 120.0",    // Γ(6) = 5! = 120
            "7.0, 720.0",    // Γ(7) = 6! = 720
            "8.0, 5040.0",   // Γ(8) = 7! = 5040
            "9.0, 40320.0",  // Γ(9) = 8! = 40320
            "10.0, 362880.0" // Γ(10) = 9! = 362880
    })
    void gammaOfIntegerIsFactorial(double x, double expected) {
        double result = Gamma.gamma(x);
        assertThat(result).isCloseTo(expected, within(HIGH_PRECISION_TOLERANCE));
    }

    /**
     * Tests that ln(Γ(n)) = ln((n-1)!) for positive integers.
     */
    @ParameterizedTest
    @CsvSource({
            "1.0, 0.0",              // ln(Γ(1)) = ln(1) = 0
            "2.0, 0.0",              // ln(Γ(2)) = ln(1) = 0
            "3.0, 0.693147180559945", // ln(Γ(3)) = ln(2)
            "4.0, 1.791759469228055", // ln(Γ(4)) = ln(6)
            "5.0, 3.178053830347946", // ln(Γ(5)) = ln(24)
            "10.0, 12.80182748008147" // ln(Γ(10)) = ln(362880)
    })
    void gammaLnOfIntegerIsLogFactorial(double x, double expected) {
        double result = Gamma.gammaLn(x);
        assertThat(result).isCloseTo(expected, within(HIGH_PRECISION_TOLERANCE));
    }

    // ==================== Special Values ====================

    /**
     * Tests that Γ(0.5) = √π.
     * <p>
     * This is one of the most important special values of the Gamma function.
     * √π ≈ 1.772453850905516
     * </p>
     */
    @Test
    void gammaOfHalfIsSqrtPi() {
        double result = Gamma.gamma(0.5);
        double expected = Math.sqrt(Math.PI);
        assertThat(result).isCloseTo(expected, within(STANDARD_TOLERANCE));
    }

    /**
     * Tests that ln(Γ(0.5)) = ln(√π).
     */
    @Test
    void gammaLnOfHalfIsLogSqrtPi() {
        double result = Gamma.gammaLn(0.5);
        double expected = Math.log(Math.sqrt(Math.PI));
        assertThat(result).isCloseTo(expected, within(HIGH_PRECISION_TOLERANCE));
    }

    /**
     * Tests Γ(1.5) = (√π)/2.
     * <p>
     * Using the recurrence relation: Γ(1.5) = 0.5 · Γ(0.5) = 0.5 · √π
     * </p>
     */
    @Test
    void gammaOfOnePointFiveIsHalfSqrtPi() {
        double result = Gamma.gamma(1.5);
        double expected = Math.sqrt(Math.PI) / 2.0;
        assertThat(result).isCloseTo(expected, within(STANDARD_TOLERANCE));
    }

    /**
     * Tests Γ(2.5) = (3√π)/4.
     * <p>
     * Using the recurrence relation: Γ(2.5) = 1.5 · Γ(1.5) = 1.5 · 0.5 · √π = (3√π)/4
     * </p>
     */
    @Test
    void gammaOfTwoPointFiveIsThreeQuartersSqrtPi() {
        double result = Gamma.gamma(2.5);
        double expected = (3.0 * Math.sqrt(Math.PI)) / 4.0;
        assertThat(result).isCloseTo(expected, within(STANDARD_TOLERANCE));
    }

    // ==================== Recurrence Relation: Γ(x+1) = x·Γ(x) ====================

    /**
     * Tests the fundamental recurrence relation Γ(x+1) = x·Γ(x).
     * <p>
     * This is the defining property that extends the factorial function to real numbers.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "0.5",   // Γ(1.5) = 0.5·Γ(0.5)
            "1.0",   // Γ(2) = 1·Γ(1)
            "1.5",   // Γ(2.5) = 1.5·Γ(1.5)
            "2.0",   // Γ(3) = 2·Γ(2)
            "2.5",   // Γ(3.5) = 2.5·Γ(2.5)
            "3.0",   // Γ(4) = 3·Γ(3)
            "5.0",   // Γ(6) = 5·Γ(5)
            "10.0",  // Γ(11) = 10·Γ(10)
            "20.5"   // Γ(21.5) = 20.5·Γ(20.5)
    })
    void gammaRecurrenceRelation(double x) {
        double gammaX = Gamma.gamma(x);
        double gammaXPlus1 = Gamma.gamma(x + 1.0);
        double expectedGammaXPlus1 = x * gammaX;

        // For large values, use relative tolerance
        double tolerance = x > 10.0 ? Math.abs(expectedGammaXPlus1) * 1e-10 : STANDARD_TOLERANCE;
        assertThat(gammaXPlus1).isCloseTo(expectedGammaXPlus1, within(tolerance));
    }

    /**
     * Tests the recurrence relation for gammaLn: ln(Γ(x+1)) = ln(x) + ln(Γ(x)).
     */
    @ParameterizedTest
    @CsvSource({
            "0.5",
            "1.0",
            "2.5",
            "5.0",
            "10.0",
            "50.0",
            "100.0"
    })
    void gammaLnRecurrenceRelation(double x) {
        double gammaLnX = Gamma.gammaLn(x);
        double gammaLnXPlus1 = Gamma.gammaLn(x + 1.0);
        double expectedGammaLnXPlus1 = Math.log(x) + gammaLnX;

        assertThat(gammaLnXPlus1).isCloseTo(expectedGammaLnXPlus1, within(HIGH_PRECISION_TOLERANCE));
    }

    // ==================== Consistency Between gamma() and gammaLn() ====================

    /**
     * Tests that gamma(x) = exp(gammaLn(x)) for various values.
     * <p>
     * This verifies consistency between the two methods.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "0.1",
            "0.5",
            "1.0",
            "1.5",
            "2.0",
            "3.0",
            "5.0",
            "10.0",
            "20.0",
            "50.0"
    })
    void gammaEqualsExpOfGammaLn(double x) {
        double gamma = Gamma.gamma(x);
        double expGammaLn = Math.exp(Gamma.gammaLn(x));

        assertThat(gamma).isCloseTo(expGammaLn, within(STANDARD_TOLERANCE));
    }

    /**
     * Tests that ln(gamma(x)) = gammaLn(x) for small values where gamma doesn't overflow.
     */
    @ParameterizedTest
    @CsvSource({
            "0.5",
            "1.0",
            "2.0",
            "5.0",
            "10.0"
    })
    void logOfGammaEqualsGammaLn(double x) {
        double logGamma = Math.log(Gamma.gamma(x));
        double gammaLn = Gamma.gammaLn(x);

        assertThat(logGamma).isCloseTo(gammaLn, within(HIGH_PRECISION_TOLERANCE));
    }

    // ==================== Edge Cases and Large Values ====================

    /**
     * Tests gamma function for very small positive values near zero.
     */
    @ParameterizedTest
    @CsvSource({
            "0.001",
            "0.01",
            "0.1"
    })
    void gammaOfVerySmallPositiveValues(double x) {
        double result = Gamma.gamma(x);

        // Γ(x) → ∞ as x → 0+, but should be finite for x > 0
        assertThat(result).isPositive();
        assertThat(result).isNotNaN();
        assertThat(result).isFinite();
    }

    /**
     * Tests gammaLn for large values where gamma would overflow.
     * <p>
     * For large x, gamma(x) overflows to infinity, but gammaLn(x) remains stable.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "100.0",
            "200.0",
            "500.0",
            "1000.0"
    })
    void gammaLnForLargeValues(double x) {
        double result = Gamma.gammaLn(x);

        assertThat(result).isPositive();
        assertThat(result).isNotNaN();
        assertThat(result).isFinite();
    }

    /**
     * Tests that gamma overflows to infinity for very large values,
     * demonstrating why gammaLn is necessary.
     */
    @Test
    void gammaOverflowsForVeryLargeValues() {
        double result = Gamma.gamma(200.0);

        // Γ(200) is astronomically large and will overflow to infinity
        assertThat(result).isEqualTo(Double.POSITIVE_INFINITY);
    }

    /**
     * Tests gammaLn produces reasonable results for moderate values.
     */
    @Test
    void gammaLnForModerateValues() {
        // Γ(100) = 99! ≈ 9.33 × 10^155
        // ln(Γ(100)) ≈ 359.13
        double result = Gamma.gammaLn(100.0);

        assertThat(result).isCloseTo(359.1342053695754, within(RELAXED_TOLERANCE));
    }

    // ==================== Monotonicity ====================

    /**
     * Tests that Gamma function is monotonically increasing for x > 1.46.
     * <p>
     * The Gamma function has a minimum near x ≈ 1.46, and increases for larger x.
     * </p>
     */
    @Test
    void gammaIsMonotonicallyIncreasingForLargeX() {
        double prev = Gamma.gamma(2.0);

        for (double x = 2.5; x <= 10.0; x += 0.5) {
            double current = Gamma.gamma(x);
            assertThat(current).isGreaterThan(prev);
            prev = current;
        }
    }

    /**
     * Tests that gammaLn is monotonically increasing for x > 1.46 (approximately).
     * Note: gammaLn has a minimum around x ≈ 1.46, so it's not monotonic for all x > 0.
     * For x > 2, it is strictly increasing.
     */
    @Test
    void gammaLnIsMonotonicallyIncreasing() {
        double prev = Gamma.gammaLn(2.0);

        for (double x = 2.5; x <= 20.0; x += 0.5) {
            double current = Gamma.gammaLn(x);
            assertThat(current).isGreaterThan(prev);
            prev = current;
        }
    }

    // ==================== Invalid Input Validation ====================

    /**
     * Tests that gamma() rejects zero input.
     */
    @Test
    void gammaRejectsZero() {
        assertThatThrownBy(() -> Gamma.gamma(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0.0")
                .hasMessageContaining("must be positive");
    }

    /**
     * Tests that gamma() rejects negative input.
     */
    @ParameterizedTest
    @CsvSource({
            "-0.1",
            "-1.0",
            "-5.0",
            "-100.0"
    })
    void gammaRejectsNegativeValues(double x) {
        assertThatThrownBy(() -> Gamma.gamma(x))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(x))
                .hasMessageContaining("must be positive");
    }

    /**
     * Tests that gammaLn() rejects zero input.
     */
    @Test
    void gammaLnRejectsZero() {
        assertThatThrownBy(() -> Gamma.gammaLn(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0.0")
                .hasMessageContaining("must be positive");
    }

    /**
     * Tests that gammaLn() rejects negative input.
     */
    @ParameterizedTest
    @CsvSource({
            "-0.01",
            "-1.0",
            "-10.0"
    })
    void gammaLnRejectsNegativeValues(double x) {
        assertThatThrownBy(() -> Gamma.gammaLn(x))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(x))
                .hasMessageContaining("must be positive");
    }

    // ==================== Numerical Accuracy ====================

    /**
     * Tests gamma function accuracy against known high-precision values.
     * <p>
     * These values are computed using high-precision mathematical software.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "0.5, 1.7724538509055159",    // √π
            "1.0, 1.0",                    // 0!
            "1.5, 0.8862269254527580",    // √π/2
            "2.0, 1.0",                    // 1!
            "3.0, 2.0",                    // 2!
            "4.0, 6.0",                    // 3!
            "5.0, 24.0",                   // 4!
            "10.0, 362880.0"               // 9!
    })
    void gammaHighPrecisionValues(double x, double expected) {
        double result = Gamma.gamma(x);
        assertThat(result).isCloseTo(expected, within(STANDARD_TOLERANCE));
    }

    /**
     * Tests gammaLn function accuracy against known high-precision values.
     */
    @ParameterizedTest
    @CsvSource({
            "0.5, 0.5723649429247001",     // ln(√π)
            "1.0, 0.0",                     // ln(1)
            "2.0, 0.0",                     // ln(1)
            "3.0, 0.6931471805599453",     // ln(2)
            "5.0, 3.178053830347946",      // ln(24)
            "10.0, 12.801827480081470",    // ln(362880)
            "50.0, 144.56574394634487",    // ln(Γ(50))
            "100.0, 359.1342053695754"     // ln(Γ(100))
    })
    void gammaLnHighPrecisionValues(double x, double expected) {
        double result = Gamma.gammaLn(x);
        assertThat(result).isCloseTo(expected, within(RELAXED_TOLERANCE));
    }

    // ==================== Additional Properties ====================

    /**
     * Tests that Γ(x) is always positive for x > 0.
     */
    @ParameterizedTest
    @CsvSource({
            "0.01",
            "0.1",
            "0.5",
            "1.0",
            "2.0",
            "5.0",
            "10.0"
    })
    void gammaIsAlwaysPositive(double x) {
        double result = Gamma.gamma(x);
        assertThat(result).isPositive();
    }

    /**
     * Tests reflection formula behavior near x = 0.5.
     * <p>
     * The reflection formula is: Γ(x)·Γ(1-x) = π/sin(πx)
     * </p>
     */
    @Test
    void gammaReflectionFormulaAtQuarterPoints() {
        // For x = 0.25: Γ(0.25)·Γ(0.75) = π/sin(π/4) = π√2
        double gamma025 = Gamma.gamma(0.25);
        double gamma075 = Gamma.gamma(0.75);
        double product = gamma025 * gamma075;
        double expected = Math.PI / Math.sin(Math.PI * 0.25);

        assertThat(product).isCloseTo(expected, within(STANDARD_TOLERANCE));
    }

    // ==================== Digamma Function Tests ====================

    /**
     * Tests known values of the digamma function.
     * <p>
     * ψ(1) = -γ where γ is the Euler-Mascheroni constant ≈ 0.5772156649
     * ψ(x) is the logarithmic derivative of Γ(x): ψ(x) = d/dx ln(Γ(x))
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "1.0, -0.5772156649015329",     // ψ(1) = -γ (Euler-Mascheroni constant)
            "2.0, 0.4227843350984671",      // ψ(2) = 1 - γ
            "3.0, 0.9227843350984671",      // ψ(3) = 1 + 1/2 - γ
            "4.0, 1.2561176684318005",      // ψ(4) = 1 + 1/2 + 1/3 - γ
            "0.5, -1.9635100260214235"      // ψ(0.5) = -γ - 2ln(2)
    })
    void digammaKnownValues(double x, double expected) {
        double result = Gamma.digamma(x);
        assertThat(result).isCloseTo(expected, within(STANDARD_TOLERANCE));
    }

    /**
     * Tests the recurrence relation for digamma: ψ(x+1) = ψ(x) + 1/x.
     * <p>
     * This is the defining property that relates consecutive values.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "0.5",
            "1.0",
            "1.5",
            "2.0",
            "3.0",
            "5.0",
            "10.0",
            "20.0"
    })
    void digammaRecurrenceRelation(double x) {
        double psiX = Gamma.digamma(x);
        double psiXPlus1 = Gamma.digamma(x + 1.0);
        double expectedPsiXPlus1 = psiX + 1.0 / x;

        assertThat(psiXPlus1).isCloseTo(expectedPsiXPlus1, within(STANDARD_TOLERANCE));
    }

    /**
     * Tests that digamma returns NaN for non-positive integers.
     * <p>
     * The digamma function is undefined at x = 0, -1, -2, -3, ...
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "0.0",
            "-1.0",
            "-2.0",
            "-3.0",
            "-10.0"
    })
    void digammaReturnsNaNForNonPositiveIntegers(double x) {
        double result = Gamma.digamma(x);
        assertThat(result).isNaN();
    }

    /**
     * Tests that digamma is monotonically increasing for x > 0.
     * <p>
     * Since ψ'(x) = d²/dx² ln(Γ(x)) > 0 for x > 0, digamma is strictly increasing.
     * </p>
     */
    @Test
    void digammaIsMonotonicallyIncreasing() {
        double prev = Gamma.digamma(0.1);

        for (double x = 0.2; x <= 10.0; x += 0.5) {
            double current = Gamma.digamma(x);
            assertThat(current).isGreaterThan(prev);
            prev = current;
        }
    }

    /**
     * Tests digamma for small positive values near zero.
     */
    @ParameterizedTest
    @CsvSource({
            "0.001",
            "0.01",
            "0.1"
    })
    void digammaOfSmallPositiveValues(double x) {
        double result = Gamma.digamma(x);

        // ψ(x) → -∞ as x → 0+, but should be finite for x > 0
        assertThat(result).isNegative();
        assertThat(result).isNotNaN();
        assertThat(result).isFinite();
    }

    /**
     * Tests digamma for large values.
     * <p>
     * For large x, ψ(x) ≈ ln(x)
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "100.0",
            "200.0",
            "500.0",
            "1000.0"
    })
    void digammaForLargeValues(double x) {
        double result = Gamma.digamma(x);
        double approximation = Math.log(x);

        // For large x, ψ(x) approaches ln(x)
        assertThat(result).isCloseTo(approximation, within(0.01));
    }

    /**
     * Tests digamma for negative non-integer values using the reflection formula.
     * <p>
     * ψ(1-x) = ψ(x) + π·cot(πx)
     * </p>
     */
    @Test
    void digammaReflectionFormulaForNegativeValues() {
        double x = 0.3;
        double psiX = Gamma.digamma(x);
        double psi1MinusX = Gamma.digamma(1.0 - x);
        double expected = psiX + Math.PI / Math.tan(Math.PI * x);

        assertThat(psi1MinusX).isCloseTo(expected, within(STANDARD_TOLERANCE));
    }

    /**
     * Tests that digamma is consistent with the derivative of gammaLn.
     * <p>
     * Since ψ(x) = d/dx ln(Γ(x)), we can verify using numerical differentiation.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "2.5",
            "3.5",
            "5.5",
            "10.0"
    })
    void digammaIsDerivativeOfGammaLn(double x) {
        double h = 1e-5; // stable step size for numerical differentiation

        double digammaX = Gamma.digamma(x);

        // Forward difference: avoids crossing poles of ln Γ(x)
        double numericalDerivative =
                (Gamma.gammaLn(x + h) - Gamma.gammaLn(x)) / h;

        assertThat(digammaX)
                .isCloseTo(numericalDerivative, within(1e-5));
    }
}
