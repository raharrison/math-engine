package uk.co.ryanharrison.mathengine.special;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for the {@link Beta} function.
 * <p>
 * Tests verify:
 * </p>
 * <ul>
 *     <li>Known mathematical values and special cases</li>
 *     <li>Symmetry property: B(z, w) = B(w, z)</li>
 *     <li>Relationship with the Gamma function</li>
 *     <li>Input validation for invalid parameters</li>
 *     <li>Edge cases with very small and large values</li>
 * </ul>
 */
class BetaTest {

    /**
     * Tolerance for high-precision computations.
     * Used when comparing Beta function results computed directly.
     */
    private static final double HIGH_PRECISION_TOLERANCE = 1e-9;

    /**
     * Relaxed tolerance for Gamma function approximations.
     * The Beta function uses Gamma.gammaLn() which has some approximation error,
     * especially for certain parameter ranges.
     */
    private static final double GAMMA_APPROXIMATION_TOLERANCE = 1e-7;

    /**
     * Very relaxed tolerance for edge cases and extreme values.
     */
    private static final double RELAXED_TOLERANCE = 1e-6;

    // ==================== Known Values ====================

    /**
     * Tests the Beta function with well-known mathematical values.
     * <p>
     * Verifies:
     * </p>
     * <ul>
     *     <li>B(1, 1) = 1</li>
     *     <li>B(2, 3) = 1/12 ≈ 0.0833333</li>
     *     <li>B(5, 2) = 1/30 ≈ 0.0333333</li>
     *     <li>B(3, 4) = 1/60 ≈ 0.0166667</li>
     *     <li>B(2, 2) = 1/6 ≈ 0.1666667</li>
     * </ul>
     */
    @ParameterizedTest
    @CsvSource({
            "1.0, 1.0, 1.0",              // B(1,1) = 1
            "2.0, 3.0, 0.08333333333",    // B(2,3) = 1/12
            "5.0, 2.0, 0.03333333333",    // B(5,2) = 1/30
            "3.0, 4.0, 0.01666666667",    // B(3,4) = 1/60
            "2.0, 2.0, 0.16666666667"     // B(2,2) = 1/6
    })
    void betaWithKnownIntegerValues(double z, double w, double expected) {
        double result = Beta.beta(z, w);
        assertThat(result).isCloseTo(expected, within(HIGH_PRECISION_TOLERANCE));
    }

    /**
     * Tests B(0.5, 0.5) = π.
     * <p>
     * This is a well-known special value of the Beta function.
     * B(0.5, 0.5) = Γ(0.5)² / Γ(1) = (√π)² / 1 = π
     * </p>
     */
    @Test
    void betaHalfHalfEqualsPI() {
        double result = Beta.beta(0.5, 0.5);
        assertThat(result).isCloseTo(Math.PI, within(GAMMA_APPROXIMATION_TOLERANCE));
    }

    /**
     * Tests B(z, 1) = 1/z for various values of z.
     * <p>
     * This is a fundamental property: B(z, 1) = Γ(z) * Γ(1) / Γ(z+1) = Γ(z) / (z * Γ(z)) = 1/z
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "1.0, 1.0",         // B(1,1) = 1/1 = 1
            "2.0, 0.5",         // B(2,1) = 1/2 = 0.5
            "3.0, 0.3333333333",  // B(3,1) = 1/3
            "5.0, 0.2",         // B(5,1) = 1/5 = 0.2
            "10.0, 0.1"         // B(10,1) = 1/10 = 0.1
    })
    void betaWithOneEqualsReciprocal(double z, double expected) {
        double result = Beta.beta(z, 1.0);
        assertThat(result).isCloseTo(expected, within(HIGH_PRECISION_TOLERANCE));
    }

    /**
     * Tests B(1, w) = 1/w for various values of w.
     * <p>
     * By symmetry, B(1, w) = B(w, 1) = 1/w
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "2.0, 0.5",
            "4.0, 0.25",
            "8.0, 0.125",
            "0.5, 2.0"
    })
    void betaOneWithParameterEqualsReciprocal(double w, double expected) {
        double result = Beta.beta(1.0, w);
        assertThat(result).isCloseTo(expected, within(HIGH_PRECISION_TOLERANCE));
    }

    /**
     * Tests Beta function values with fractional parameters.
     * <p>
     * These values are computed using the relationship with the Gamma function
     * and verified against known mathematical results.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "0.5, 1.0, 2.0",           // B(0.5, 1) = 1/0.5 = 2
            "0.5, 2.0, 1.3333333333",  // B(0.5, 2) = Γ(0.5)Γ(2)/Γ(2.5) = √π/(1.5√π) = 2/3
            "1.5, 2.5, 0.1963495408",  // B(1.5, 2.5) = Γ(1.5)Γ(2.5)/Γ(4)
            "2.5, 3.5, 0.0368155389"   // B(2.5, 3.5) = Γ(2.5)Γ(3.5)/Γ(6)
    })
    void betaWithFractionalParameters(double z, double w, double expected) {
        double result = Beta.beta(z, w);
        assertThat(result).isCloseTo(expected, within(GAMMA_APPROXIMATION_TOLERANCE));
    }

    // ==================== Symmetry Property ====================

    /**
     * Tests the fundamental symmetry property: B(z, w) = B(w, z).
     * <p>
     * The Beta function is symmetric in its arguments. This property
     * follows directly from the definition using Gamma functions since
     * multiplication is commutative.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "1.0, 2.0",
            "2.0, 3.0",
            "3.0, 5.0",
            "5.0, 7.0",
            "0.5, 1.5",
            "2.5, 3.5",
            "10.0, 20.0"
    })
    void betaIsSymmetric(double z, double w) {
        double b1 = Beta.beta(z, w);
        double b2 = Beta.beta(w, z);
        assertThat(b1).isCloseTo(b2, within(HIGH_PRECISION_TOLERANCE));
    }

    // ==================== Relationship with Gamma Function ====================

    /**
     * Verifies the fundamental relationship: B(z, w) = Γ(z) * Γ(w) / Γ(z + w).
     * <p>
     * This tests that the Beta implementation correctly uses the Gamma function
     * relationship across various parameter values.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "1.0, 1.0",
            "2.0, 3.0",
            "3.0, 4.0",
            "5.0, 5.0",
            "0.5, 0.5",
            "1.5, 2.5",
            "10.0, 15.0"
    })
    void betaMatchesGammaRelationship(double z, double w) {
        double betaResult = Beta.beta(z, w);
        double gammaResult = Gamma.gamma(z) * Gamma.gamma(w) / Gamma.gamma(z + w);

        assertThat(betaResult).isCloseTo(gammaResult, within(GAMMA_APPROXIMATION_TOLERANCE));
    }

    // ==================== Input Validation ====================

    /**
     * Tests that the Beta function rejects zero for the first parameter.
     */
    @Test
    void betaRejectsZeroFirstParameter() {
        assertThatThrownBy(() -> Beta.beta(0.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First parameter must be positive")
                .hasMessageContaining("0.0");
    }

    /**
     * Tests that the Beta function rejects negative values for the first parameter.
     */
    @Test
    void betaRejectsNegativeFirstParameter() {
        assertThatThrownBy(() -> Beta.beta(-1.5, 2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First parameter must be positive")
                .hasMessageContaining("-1.5");
    }

    /**
     * Tests that the Beta function rejects zero for the second parameter.
     */
    @Test
    void betaRejectsZeroSecondParameter() {
        assertThatThrownBy(() -> Beta.beta(1.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Second parameter must be positive")
                .hasMessageContaining("0.0");
    }

    /**
     * Tests that the Beta function rejects negative values for the second parameter.
     */
    @Test
    void betaRejectsNegativeSecondParameter() {
        assertThatThrownBy(() -> Beta.beta(2.0, -3.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Second parameter must be positive")
                .hasMessageContaining("-3.5");
    }

    /**
     * Tests that the Beta function rejects negative values for both parameters.
     */
    @Test
    void betaRejectsBothNegativeParameters() {
        assertThatThrownBy(() -> Beta.beta(-1.0, -2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("First parameter must be positive");
    }

    // ==================== Edge Cases ====================

    /**
     * Tests Beta function with very small positive values.
     * <p>
     * Ensures the function handles values close to zero without numerical issues.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "0.01, 0.01",
            "0.1, 0.1",
            "0.001, 1.0",
            "1.0, 0.001"
    })
    void betaWithVerySmallValues(double z, double w) {
        double result = Beta.beta(z, w);

        // Result should be finite and positive
        // Note: Beta(x,x) for very small x (< 0.01) may overflow due to Γ(x) being very large
        assertThat(result).isFinite();
        assertThat(result).isPositive();

        // Verify symmetry still holds
        double symmetric = Beta.beta(w, z);
        assertThat(result).isCloseTo(symmetric, within(RELAXED_TOLERANCE));
    }

    /**
     * Tests Beta function with large parameter values.
     * <p>
     * The logarithmic implementation should prevent overflow even with large inputs.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "10.0, 10.0",
            "50.0, 50.0",
            "100.0, 100.0",
            "10.0, 100.0"
    })
    void betaWithLargeValues(double z, double w) {
        double result = Beta.beta(z, w);

        // Result should be finite and positive
        assertThat(result).isFinite();
        assertThat(result).isPositive();

        // For large equal values, Beta should be very small
        if (z == w && z >= 10.0) {
            assertThat(result).isLessThan(1.0);
        }

        // Verify symmetry still holds
        double symmetric = Beta.beta(w, z);
        assertThat(result).isCloseTo(symmetric, within(RELAXED_TOLERANCE));
    }

    /**
     * Tests Beta function with mixed small and large values.
     */
    @ParameterizedTest
    @CsvSource({
            "0.1, 100.0",
            "100.0, 0.1",
            "0.01, 50.0",
            "50.0, 0.01"
    })
    void betaWithMixedSmallAndLargeValues(double z, double w) {
        double result = Beta.beta(z, w);

        // Result should be finite and positive
        assertThat(result).isFinite();
        assertThat(result).isPositive();

        // Verify symmetry
        double symmetric = Beta.beta(w, z);
        assertThat(result).isCloseTo(symmetric, within(RELAXED_TOLERANCE));
    }

    /**
     * Tests that Beta function results are always positive.
     * <p>
     * By definition, B(z, w) > 0 for all positive z and w.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "0.5, 0.5",
            "1.0, 1.0",
            "2.0, 3.0",
            "10.0, 20.0",
            "0.1, 0.9",
            "100.0, 100.0"
    })
    void betaIsAlwaysPositive(double z, double w) {
        double result = Beta.beta(z, w);
        assertThat(result).isPositive();
    }

    // ==================== Regularized Incomplete Beta ====================

    @ParameterizedTest
    @CsvSource({
            "0.3, 1.0, 1.0, 0.3",                   // x, a, b, expected: I_x(1, 1) = x
            "0.3, 3.0, 1.0, 0.027",                 // I_x(a, 1) = x^a
            "0.3, 1.0, 3.0, 0.657",                 // I_x(1, b) = 1 - (1 - x)^b
            "0.5, 2.5, 2.5, 0.5",                   // I_(1/2)(a, a) = 1/2
            "0.2, 2.0, 3.0, 0.1808"                 // the Beta(2, 3) CDF, 6x^2 - 8x^3 + 3x^4
    })
    void regularizedIncompleteKnownValues(double x, double a, double b, double expected) {
        assertThat(Beta.regularizedIncomplete(x, a, b)).isCloseTo(expected, within(1e-14));
    }

    @Test
    void regularizedIncompleteIsSymmetric() {
        for (double x = 0.05; x < 1.0; x += 0.1) {
            assertThat(Beta.regularizedIncomplete(x, 2.5, 7.0))
                    .isCloseTo(1.0 - Beta.regularizedIncomplete(1.0 - x, 7.0, 2.5), within(1e-14));
        }
    }

    @Test
    void regularizedIncompleteAtTheEndsIsZeroAndOne() {
        assertThat(Beta.regularizedIncomplete(0.0, 2.0, 3.0)).isEqualTo(0.0);
        assertThat(Beta.regularizedIncomplete(1.0, 2.0, 3.0)).isEqualTo(1.0);
    }

    @Test
    void regularizedIncompleteRejectsInvalidArguments() {
        assertThatThrownBy(() -> Beta.regularizedIncomplete(-0.1, 1.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("[0, 1]");
        assertThatThrownBy(() -> Beta.regularizedIncomplete(0.5, 0.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
        assertThatThrownBy(() -> Beta.regularizedIncomplete(0.5, 1.0, -2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    // ==================== Monotonicity Properties ====================

    /**
     * Tests that B(z, w) decreases as z increases (with w fixed).
     * <p>
     * For fixed w > 0, the Beta function B(z, w) is a decreasing function of z.
     * This follows from the relationship with the Gamma function.
     * </p>
     */
    @Test
    void betaDecreasesAsFirstParameterIncreasesWithFixedSecond() {
        double w = 2.0;
        double prev = Double.POSITIVE_INFINITY;

        for (double z = 0.5; z <= 10.0; z += 0.5) {
            double current = Beta.beta(z, w);
            assertThat(current).isLessThan(prev);
            prev = current;
        }
    }

    /**
     * Tests that B(z, w) decreases as w increases (with z fixed).
     * <p>
     * By symmetry with the previous test, for fixed z > 0, the Beta function
     * B(z, w) is a decreasing function of w.
     * </p>
     */
    @Test
    void betaDecreasesAsSecondParameterIncreasesWithFixedFirst() {
        double z = 2.0;
        double prev = Double.POSITIVE_INFINITY;

        for (double w = 0.5; w <= 10.0; w += 0.5) {
            double current = Beta.beta(z, w);
            assertThat(current).isLessThan(prev);
            prev = current;
        }
    }

    // ==================== Special Relationships ====================

    /**
     * Tests the recurrence relation: B(z, w+1) = B(z, w) * w / (z + w).
     * <p>
     * This tests an important recurrence relationship of the Beta function.
     * </p>
     */
    @ParameterizedTest
    @CsvSource({
            "2.0, 2.0",
            "3.0, 4.0",
            "5.0, 5.0",
            "1.5, 2.5"
    })
    void betaRecurrenceRelation(double z, double w) {
        double betaZW = Beta.beta(z, w);
        double betaZWPlus1 = Beta.beta(z, w + 1);
        double expected = betaZW * w / (z + w);

        assertThat(betaZWPlus1).isCloseTo(expected, within(GAMMA_APPROXIMATION_TOLERANCE));
    }

    /**
     * Tests that B(n, n) = (2n-1)!! / ((2n)!! * (2n-1)) for small integers.
     * <p>
     * This verifies the Beta function value for equal integer parameters using
     * the factorial-based formula.
     * </p>
     */
    @Test
    void betaEqualIntegerParameters() {
        // B(2, 2) = 1/6
        assertThat(Beta.beta(2.0, 2.0)).isCloseTo(1.0 / 6.0, within(HIGH_PRECISION_TOLERANCE));

        // B(3, 3) = 1/30
        assertThat(Beta.beta(3.0, 3.0)).isCloseTo(1.0 / 30.0, within(HIGH_PRECISION_TOLERANCE));

        // B(4, 4) = 1/140
        assertThat(Beta.beta(4.0, 4.0)).isCloseTo(1.0 / 140.0, within(HIGH_PRECISION_TOLERANCE));
    }
}
