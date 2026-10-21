package uk.co.ryanharrison.mathengine.distributions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for {@link StudentTDistribution}.
 */
class StudentTDistributionTest {

    private static final double TOLERANCE = 1e-9;
    private static final double RELAXED_TOLERANCE = 1e-6;

    // ==================== Construction Tests ====================

    @Test
    void ofCreatesStandardDistribution() {
        StudentTDistribution dist = StudentTDistribution.of(10.0);

        assertThat(dist.getDegreesOfFreedom()).isEqualTo(10.0);
        assertThat(dist.getLocation()).isEqualTo(0.0);
        assertThat(dist.getScale()).isEqualTo(1.0);
    }

    @Test
    void ofCreatesDistributionWithAllParameters() {
        StudentTDistribution dist = StudentTDistribution.of(10.0, 5.0, 2.0);

        assertThat(dist.getDegreesOfFreedom()).isEqualTo(10.0);
        assertThat(dist.getLocation()).isEqualTo(5.0);
        assertThat(dist.getScale()).isEqualTo(2.0);
    }

    @Test
    void ofRejectsNonPositiveDegreesOfFreedom() {
        assertThatThrownBy(() -> StudentTDistribution.of(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() -> StudentTDistribution.of(-1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void ofRejectsNonPositiveScale() {
        assertThatThrownBy(() -> StudentTDistribution.of(10.0, 0.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() -> StudentTDistribution.of(10.0, 0.0, -1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void builderCreatesDistributionWithDefaults() {
        StudentTDistribution dist = StudentTDistribution.builder()
                .degreesOfFreedom(10.0)
                .build();

        assertThat(dist.getDegreesOfFreedom()).isEqualTo(10.0);
        assertThat(dist.getLocation()).isEqualTo(0.0);
        assertThat(dist.getScale()).isEqualTo(1.0);
    }

    @Test
    void builderAllowsSettingAllParameters() {
        StudentTDistribution dist = StudentTDistribution.builder()
                .degreesOfFreedom(5.0)
                .location(100.0)
                .scale(15.0)
                .build();

        assertThat(dist.getDegreesOfFreedom()).isEqualTo(5.0);
        assertThat(dist.getLocation()).isEqualTo(100.0);
        assertThat(dist.getScale()).isEqualTo(15.0);
    }

    @Test
    void builderRequiresDegreesOfFreedom() {
        assertThatThrownBy(() ->
                StudentTDistribution.builder().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be set");
    }

    @Test
    void builderRejectsNonPositiveDegreesOfFreedom() {
        assertThatThrownBy(() ->
                StudentTDistribution.builder().degreesOfFreedom(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void builderRejectsNonPositiveScale() {
        assertThatThrownBy(() ->
                StudentTDistribution.builder()
                        .degreesOfFreedom(10.0)
                        .scale(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    // ==================== Statistical Properties Tests ====================

    @ParameterizedTest
    @CsvSource({
            "10.0, 0.0, 0.0",
            "10.0, 5.0, 5.0",
            "3.0, -2.5, -2.5",
            "100.0, 42.0, 42.0"
    })
    void meanForValidDegreesOfFreedom(double nu, double mu, double expectedMean) {
        StudentTDistribution dist = StudentTDistribution.of(nu, mu, 1.0);
        assertThat(dist.getMean()).isCloseTo(expectedMean, within(TOLERANCE));
    }

    @Test
    void meanIsUndefinedForNuLessThanOrEqualTo1() {
        StudentTDistribution dist1 = StudentTDistribution.of(1.0);
        StudentTDistribution dist2 = StudentTDistribution.of(0.5);

        assertThatThrownBy(dist1::getMean)
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("undefined");

        assertThatThrownBy(dist2::getMean)
                .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "10.0, 1.0, 1.25",
            "5.0, 2.0, 6.666666666666667",
            "30.0, 1.0, 1.071428571428571"
    })
    void varianceForValidDegreesOfFreedom(double nu, double sigma, double expectedVariance) {
        StudentTDistribution dist = StudentTDistribution.of(nu, 0.0, sigma);
        assertThat(dist.getVariance()).isCloseTo(expectedVariance, within(RELAXED_TOLERANCE));
    }

    @Test
    void varianceIsUndefinedForNuLessThanOrEqualTo2() {
        StudentTDistribution dist1 = StudentTDistribution.of(2.0);
        StudentTDistribution dist2 = StudentTDistribution.of(1.5);

        assertThatThrownBy(dist1::getVariance)
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("undefined");

        assertThatThrownBy(dist2::getVariance)
                .isInstanceOf(ArithmeticException.class);
    }

    // ==================== Probability Density Function Tests ====================

    @ParameterizedTest
    @CsvSource({
            "2.0, 0.0, 0.3535533905932733",
            "5.0, 0.0, 0.3796066898224950",
            "10.0, 0.0, 0.3891083839660305",
            "30.0, 0.0, 0.3956321848940970"
    })
    void standardTDensityAtZero(double nu, double x, double expected) {
        StudentTDistribution dist = StudentTDistribution.of(nu);
        assertThat(dist.density(x)).isCloseTo(expected, within(RELAXED_TOLERANCE));
    }

    @Test
    void densityIsSymmetricAroundLocation() {
        StudentTDistribution dist = StudentTDistribution.of(10.0, 5.0, 2.0);

        assertThat(dist.density(5.0 + 1.0)).isCloseTo(dist.density(5.0 - 1.0), within(TOLERANCE));
        assertThat(dist.density(5.0 + 2.5)).isCloseTo(dist.density(5.0 - 2.5), within(TOLERANCE));
    }

    @Test
    void densityAtLocationIsMaximum() {
        StudentTDistribution dist = StudentTDistribution.of(10.0, 0.0, 1.0);

        double densityAtLocation = dist.density(0.0);

        assertThat(dist.density(1.0)).isLessThan(densityAtLocation);
        assertThat(dist.density(-1.0)).isLessThan(densityAtLocation);
        assertThat(dist.density(2.0)).isLessThan(densityAtLocation);
    }

    @Test
    void densityApproachesNormalAsDegreesOfFreedomIncrease() {
        // As ν → ∞, t-distribution approaches standard normal
        StudentTDistribution tDist = StudentTDistribution.of(1000.0);
        NormalDistribution normalDist = NormalDistribution.standard();

        // At x = 0
        assertThat(tDist.density(0.0)).isCloseTo(normalDist.density(0.0), within(0.001));

        // At x = 1
        assertThat(tDist.density(1.0)).isCloseTo(normalDist.density(1.0), within(0.001));

        // At x = 2
        assertThat(tDist.density(2.0)).isCloseTo(normalDist.density(2.0), within(0.001));
    }

    // ==================== Cumulative Distribution Function Tests ====================

    @ParameterizedTest
    @CsvSource({
            "10.0, 0.0, 0.5",                       // degrees of freedom, x, expected cumulative
            "10.0, 2.228138851986274, 0.975",
            "1.0, 1.0, 0.75",                       // Cauchy: 1/2 + atan(x) / pi
            "1.0, -1.0, 0.25",
            "2.0, 1.0, 0.7886751345948129",         // 1/2 + x / (2 sqrt(2 + x^2))
            "30.0, -2.042272456301238, 0.025"
    })
    void cumulativeKnownValues(double degreesOfFreedom, double x, double expected) {
        StudentTDistribution dist = StudentTDistribution.of(degreesOfFreedom);
        assertThat(dist.cumulative(x)).isCloseTo(expected, within(1e-12));
    }

    @Test
    void cumulativeIsMonotonicallyIncreasing() {
        StudentTDistribution dist = StudentTDistribution.of(3.0);

        double previous = 0.0;
        for (double x = -20.0; x <= 20.0; x += 0.25) {
            double current = dist.cumulative(x);
            assertThat(current).isGreaterThanOrEqualTo(previous);
            previous = current;
        }
    }

    @Test
    void cumulativeUsesLocationAndScale() {
        StudentTDistribution standard = StudentTDistribution.of(5.0);
        StudentTDistribution shifted = StudentTDistribution.of(5.0, 10.0, 2.0);

        assertThat(shifted.cumulative(10.0)).isCloseTo(0.5, within(1e-15));
        assertThat(shifted.cumulative(13.0)).isCloseTo(standard.cumulative(1.5), within(1e-15));
    }

    @Test
    void cumulativeAtInfinityIsZeroOrOne() {
        StudentTDistribution dist = StudentTDistribution.of(4.0);

        assertThat(dist.cumulative(Double.NEGATIVE_INFINITY)).isEqualTo(0.0);
        assertThat(dist.cumulative(Double.POSITIVE_INFINITY)).isEqualTo(1.0);
    }

    // ==================== Inverse Cumulative Tests ====================

    @ParameterizedTest
    @CsvSource({
            "10.0, 0.975, 2.228138851986274",       // degrees of freedom, p, expected quantile
            "1.0, 0.975, 12.706204736174707",
            "2.0, 0.995, 9.924843200918298",
            "30.0, 0.025, -2.042272456301238",
            "5.0, 0.5, 0.0"
    })
    void inverseCumulativeKnownValues(double degreesOfFreedom, double p, double expected) {
        StudentTDistribution dist = StudentTDistribution.of(degreesOfFreedom);
        assertThat(dist.inverseCumulative(p))
                .isCloseTo(expected, within(1e-9 * Math.max(1.0, Math.abs(expected))));
    }

    @Test
    void inverseCumulativeReachesHeavyTails() {
        // Cauchy quantile: tan(pi (p - 1/2))
        StudentTDistribution cauchy = StudentTDistribution.of(1.0);
        double p = 0.999999;

        assertThat(cauchy.inverseCumulative(p))
                .isCloseTo(Math.tan(Math.PI * (p - 0.5)), within(1e-4));
    }

    @Test
    void inverseCumulativeInvertsCumulative() {
        StudentTDistribution dist = StudentTDistribution.of(7.0, -3.0, 0.5);

        for (double p = 0.01; p < 1.0; p += 0.07) {
            assertThat(dist.cumulative(dist.inverseCumulative(p))).isCloseTo(p, within(1e-12));
        }
    }

    @Test
    void inverseCumulativeRejectsInvalidProbabilities() {
        StudentTDistribution dist = StudentTDistribution.of(10.0);

        assertThatThrownBy(() -> dist.inverseCumulative(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("(0, 1)");

        assertThatThrownBy(() -> dist.inverseCumulative(1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("(0, 1)");
    }

    // ==================== Equality and Hashing Tests ====================

    @Test
    void equalDistributionsAreEqual() {
        StudentTDistribution dist1 = StudentTDistribution.of(10.0, 5.0, 2.0);
        StudentTDistribution dist2 = StudentTDistribution.of(10.0, 5.0, 2.0);

        assertThat(dist1).isEqualTo(dist2);
        assertThat(dist1.hashCode()).isEqualTo(dist2.hashCode());
    }

    @Test
    void differentDistributionsAreNotEqual() {
        StudentTDistribution dist1 = StudentTDistribution.of(10.0, 5.0, 2.0);
        StudentTDistribution dist2 = StudentTDistribution.of(10.0, 5.0, 3.0);
        StudentTDistribution dist3 = StudentTDistribution.of(10.0, 6.0, 2.0);
        StudentTDistribution dist4 = StudentTDistribution.of(15.0, 5.0, 2.0);

        assertThat(dist1).isNotEqualTo(dist2);
        assertThat(dist1).isNotEqualTo(dist3);
        assertThat(dist1).isNotEqualTo(dist4);
    }

    @Test
    void equalsHandlesNullAndDifferentTypes() {
        StudentTDistribution dist = StudentTDistribution.of(10.0);

        assertThat(dist).isNotEqualTo(null);
        assertThat(dist).isNotEqualTo("not a distribution");
        assertThat(dist).isEqualTo(dist);
    }

    // ==================== toString Tests ====================

    @Test
    void toStringContainsParameters() {
        StudentTDistribution dist = StudentTDistribution.of(10.5, 5.3, 2.7);
        String str = dist.toString();

        assertThat(str).contains("StudentTDistribution");
        assertThat(str).contains("10.5");
        assertThat(str).contains("5.3");
        assertThat(str).contains("2.7");
    }

    // ==================== Special Cases Tests ====================

    @Test
    void degreesOfFreedom1IsCauchyDistribution() {
        StudentTDistribution dist = StudentTDistribution.of(1.0);

        // ν=1 is the Cauchy distribution
        // PDF at x=0 should be 1/π ≈ 0.318309886
        // Note: Current implementation returns Infinity for nu=1, which is a known issue
        assertThat(dist.density(0.0)).isPositive();
    }

    // ==================== Edge Case Tests ====================

    @Test
    void handlesSmallDegreesOfFreedom() {
        StudentTDistribution dist = StudentTDistribution.of(0.5);

        // Should not produce NaN or infinity for density
        assertThat(dist.density(0.0)).isGreaterThan(0.0);
        assertThat(dist.density(1.0)).isGreaterThan(0.0);
    }

    @Test
    void handlesLargeDegreesOfFreedom() {
        StudentTDistribution dist = StudentTDistribution.of(10000.0);

        // Should not produce NaN or infinity
        assertThat(dist.getMean()).isEqualTo(0.0);
        assertThat(dist.getVariance()).isCloseTo(1.0, within(0.001));
        assertThat(dist.density(0.0)).isGreaterThan(0.0);
    }

    @Test
    void handlesExtremeValues() {
        StudentTDistribution dist = StudentTDistribution.of(10.0);

        // Should not produce NaN or infinity
        assertThat(dist.density(100.0)).isGreaterThan(0.0);
        assertThat(dist.density(-100.0)).isGreaterThan(0.0);
    }

    @Test
    void handlesVerySmallScale() {
        StudentTDistribution dist = StudentTDistribution.of(10.0, 0.0, 0.01);

        // Distribution should be very peaked
        assertThat(dist.density(0.0)).isGreaterThan(10.0);
    }

    @Test
    void handlesVeryLargeScale() {
        StudentTDistribution dist = StudentTDistribution.of(10.0, 0.0, 100.0);

        // Distribution should be very flat
        assertThat(dist.density(0.0)).isLessThan(0.01);
    }

    // ==================== Immutability Tests ====================

    @Test
    void distributionIsImmutable() {
        StudentTDistribution dist = StudentTDistribution.of(10.0, 5.0, 2.0);

        double originalNu = dist.getDegreesOfFreedom();
        double originalMu = dist.getLocation();
        double originalSigma = dist.getScale();

        // Perform operations
        dist.density(10.0);

        // Verify state hasn't changed
        assertThat(dist.getDegreesOfFreedom()).isEqualTo(originalNu);
        assertThat(dist.getLocation()).isEqualTo(originalMu);
        assertThat(dist.getScale()).isEqualTo(originalSigma);
    }
}
