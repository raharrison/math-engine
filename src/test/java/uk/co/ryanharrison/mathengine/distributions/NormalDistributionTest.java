package uk.co.ryanharrison.mathengine.distributions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for {@link NormalDistribution}.
 */
class NormalDistributionTest {

    private static final double TOLERANCE = 1e-7;
    private static final double RELAXED_TOLERANCE = 1e-4;

    // ==================== Construction Tests ====================

    @Test
    void standardDistributionHasCorrectParameters() {
        NormalDistribution dist = NormalDistribution.standard();

        assertThat(dist.getMean()).isEqualTo(0.0);
        assertThat(dist.getStandardDeviation()).isEqualTo(1.0);
        assertThat(dist.getVariance()).isEqualTo(1.0);
    }

    @Test
    void ofCreatesDistributionWithGivenParameters() {
        NormalDistribution dist = NormalDistribution.of(10.0, 2.5);

        assertThat(dist.getMean()).isEqualTo(10.0);
        assertThat(dist.getStandardDeviation()).isEqualTo(2.5);
        assertThat(dist.getVariance()).isCloseTo(6.25, within(TOLERANCE));
    }

    @Test
    void ofRejectsNonPositiveStandardDeviation() {
        assertThatThrownBy(() -> NormalDistribution.of(0.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() -> NormalDistribution.of(0.0, -1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void builderCreatesDistributionWithDefaults() {
        NormalDistribution dist = NormalDistribution.builder().build();

        assertThat(dist.getMean()).isEqualTo(0.0);
        assertThat(dist.getStandardDeviation()).isEqualTo(1.0);
    }

    @Test
    void builderAllowsSettingMean() {
        NormalDistribution dist = NormalDistribution.builder()
                .mean(100.0)
                .build();

        assertThat(dist.getMean()).isEqualTo(100.0);
        assertThat(dist.getStandardDeviation()).isEqualTo(1.0);
    }

    @Test
    void builderAllowsSettingStandardDeviation() {
        NormalDistribution dist = NormalDistribution.builder()
                .standardDeviation(5.0)
                .build();

        assertThat(dist.getMean()).isEqualTo(0.0);
        assertThat(dist.getStandardDeviation()).isEqualTo(5.0);
    }

    @Test
    void builderAllowsSettingVariance() {
        NormalDistribution dist = NormalDistribution.builder()
                .variance(25.0)
                .build();

        assertThat(dist.getMean()).isEqualTo(0.0);
        assertThat(dist.getStandardDeviation()).isCloseTo(5.0, within(TOLERANCE));
        assertThat(dist.getVariance()).isCloseTo(25.0, within(TOLERANCE));
    }

    @Test
    void builderRejectsNonPositiveStandardDeviation() {
        assertThatThrownBy(() ->
                NormalDistribution.builder().standardDeviation(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() ->
                NormalDistribution.builder().standardDeviation(-2.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void builderRejectsNonPositiveVariance() {
        assertThatThrownBy(() ->
                NormalDistribution.builder().variance(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");

        assertThatThrownBy(() ->
                NormalDistribution.builder().variance(-4.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }

    // ==================== Probability Density Function Tests ====================

    @Test
    void densityOfStandardNormalAtMeanIsMaximum() {
        NormalDistribution dist = NormalDistribution.standard();

        double densityAtMean = dist.density(0.0);
        assertThat(densityAtMean).isCloseTo(0.398942280401433, within(TOLERANCE));

        // Density should be lower away from mean
        assertThat(dist.density(1.0)).isLessThan(densityAtMean);
        assertThat(dist.density(-1.0)).isLessThan(densityAtMean);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0.398942280401433",
            "1.0, 0.241970724519143",
            "-1.0, 0.241970724519143",
            "2.0, 0.053990966513188",
            "-2.0, 0.053990966513188",
            "3.0, 0.004431848411938"
    })
    void standardNormalDensityValues(double x, double expected) {
        NormalDistribution dist = NormalDistribution.standard();
        assertThat(dist.density(x)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void densityIsSymmetricAroundMean() {
        NormalDistribution dist = NormalDistribution.of(5.0, 2.0);

        assertThat(dist.density(5.0 + 1.0)).isCloseTo(dist.density(5.0 - 1.0), within(TOLERANCE));
        assertThat(dist.density(5.0 + 2.5)).isCloseTo(dist.density(5.0 - 2.5), within(TOLERANCE));
    }

    @Test
    void densityScalesWithStandardDeviation() {
        double mean = 0.0;
        double x = 2.0;

        NormalDistribution dist1 = NormalDistribution.of(mean, 1.0);
        NormalDistribution dist2 = NormalDistribution.of(mean, 2.0);

        // Wider distribution should have lower peak
        assertThat(dist2.density(mean)).isLessThan(dist1.density(mean));
    }

    // ==================== Cumulative Distribution Function Tests ====================

    @ParameterizedTest
    @CsvSource({
            "-3.0, 0.001349898031630",
            "-2.0, 0.022750131948179",
            "-1.0, 0.158655253931457",
            "0.0, 0.5",
            "1.0, 0.841344746068543",
            "2.0, 0.977249868051821",
            "3.0, 0.998650101968370"
    })
    void standardNormalCumulativeValues(double x, double expected) {
        NormalDistribution dist = NormalDistribution.standard();
        assertThat(dist.cumulative(x)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void cumulativeAtMeanIsHalf() {
        NormalDistribution dist1 = NormalDistribution.of(0.0, 1.0);
        NormalDistribution dist2 = NormalDistribution.of(10.0, 3.0);
        NormalDistribution dist3 = NormalDistribution.of(-5.0, 0.5);

        assertThat(dist1.cumulative(0.0)).isCloseTo(0.5, within(TOLERANCE));
        assertThat(dist2.cumulative(10.0)).isCloseTo(0.5, within(TOLERANCE));
        assertThat(dist3.cumulative(-5.0)).isCloseTo(0.5, within(TOLERANCE));
    }

    @Test
    void cumulativeIsMonotonicallyIncreasing() {
        NormalDistribution dist = NormalDistribution.of(5.0, 2.0);

        double prev = 0.0;
        for (double x = -10.0; x <= 20.0; x += 0.5) {
            double current = dist.cumulative(x);
            assertThat(current).isGreaterThanOrEqualTo(prev);
            prev = current;
        }
    }

    @Test
    void cumulativeApproachesZeroAndOne() {
        NormalDistribution dist = NormalDistribution.standard();

        assertThat(dist.cumulative(-10.0)).isCloseTo(0.0, within(1e-6));
        assertThat(dist.cumulative(10.0)).isCloseTo(1.0, within(1e-6));
    }

    // ==================== Inverse Cumulative Tests ====================

    @ParameterizedTest
    @CsvSource({
            "0.001, -3.090232306167813",    // p, true quantile
            "0.01, -2.326347874040841",
            "0.1, -1.281551565544601",
            "0.25, -0.6744897501960817",
            "0.5, 0.0",
            "0.75, 0.6744897501960817",
            "0.9, 1.281551565544601",
            "0.99, 2.326347874040841",
            "0.999, 3.090232306167813"
    })
    void standardNormalInverseCumulativeValues(double p, double expected) {
        // These used to be the starting approximation's own output, out by up to 4.5e-4
        NormalDistribution dist = NormalDistribution.standard();
        assertThat(dist.inverseCumulative(p)).isCloseTo(expected, within(1e-13));
    }

    @ParameterizedTest
    @CsvSource({
            "1e-10",
            "1e-5",
            "0.3",
            "0.5",
            "0.975",
            "0.99999"
    })
    void inverseCumulativeInvertsCumulative(double p) {
        NormalDistribution dist = NormalDistribution.of(100.0, 15.0);
        assertThat(dist.cumulative(dist.inverseCumulative(p))).isCloseTo(p, within(p * 1e-12));
    }

    @Test
    void inverseCumulativeIsInverseOfCumulative() {
        NormalDistribution dist = NormalDistribution.of(10.0, 3.0);

        double[] probabilities = {0.01, 0.1, 0.25, 0.5, 0.75, 0.9, 0.99};
        for (double p : probabilities) {
            double x = dist.inverseCumulative(p);
            double recoveredP = dist.cumulative(x);
            assertThat(recoveredP).isCloseTo(p, within(1e-4));
        }
    }

    @Test
    void inverseCumulativeRejectsInvalidProbabilities() {
        NormalDistribution dist = NormalDistribution.standard();

        assertThatThrownBy(() -> dist.inverseCumulative(0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("(0, 1)");

        assertThatThrownBy(() -> dist.inverseCumulative(1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("(0, 1)");

        assertThatThrownBy(() -> dist.inverseCumulative(-0.1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> dist.inverseCumulative(1.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void inverseCumulativeAtHalfIsMean() {
        NormalDistribution dist1 = NormalDistribution.of(0.0, 1.0);
        NormalDistribution dist2 = NormalDistribution.of(100.0, 15.0);
        NormalDistribution dist3 = NormalDistribution.of(-25.0, 5.0);

        assertThat(dist1.inverseCumulative(0.5)).isCloseTo(0.0, within(RELAXED_TOLERANCE));
        assertThat(dist2.inverseCumulative(0.5)).isCloseTo(100.0, within(RELAXED_TOLERANCE));
        assertThat(dist3.inverseCumulative(0.5)).isCloseTo(-25.0, within(RELAXED_TOLERANCE));
    }

    // ==================== Equality and Hashing Tests ====================

    @Test
    void equalDistributionsAreEqual() {
        NormalDistribution dist1 = NormalDistribution.of(5.0, 2.0);
        NormalDistribution dist2 = NormalDistribution.of(5.0, 2.0);

        assertThat(dist1).isEqualTo(dist2);
        assertThat(dist1.hashCode()).isEqualTo(dist2.hashCode());
    }

    @Test
    void differentDistributionsAreNotEqual() {
        NormalDistribution dist1 = NormalDistribution.of(5.0, 2.0);
        NormalDistribution dist2 = NormalDistribution.of(5.0, 3.0);
        NormalDistribution dist3 = NormalDistribution.of(6.0, 2.0);

        assertThat(dist1).isNotEqualTo(dist2);
        assertThat(dist1).isNotEqualTo(dist3);
        assertThat(dist2).isNotEqualTo(dist3);
    }

    @Test
    void equalsHandlesNullAndDifferentTypes() {
        NormalDistribution dist = NormalDistribution.standard();

        assertThat(dist).isNotEqualTo(null);
        assertThat(dist).isNotEqualTo("not a distribution");
        assertThat(dist).isEqualTo(dist);
    }

    // ==================== toString Tests ====================

    @Test
    void toStringContainsParameters() {
        NormalDistribution dist = NormalDistribution.of(10.5, 2.3);
        String str = dist.toString();

        assertThat(str).contains("NormalDistribution");
        assertThat(str).contains("10.5");
        assertThat(str).contains("2.3");
    }

    // ==================== Statistical Property Tests ====================

    @Test
    void empiricalRuleHoldsFor68Percent() {
        NormalDistribution dist = NormalDistribution.of(0.0, 1.0);

        // About 68% of values should be within 1 standard deviation of mean
        double probWithin1Std = dist.cumulative(1.0) - dist.cumulative(-1.0);
        assertThat(probWithin1Std).isCloseTo(0.6827, within(0.001));
    }

    @Test
    void empiricalRuleHoldsFor95Percent() {
        NormalDistribution dist = NormalDistribution.of(0.0, 1.0);

        // About 95% of values should be within 2 standard deviations of mean
        double probWithin2Std = dist.cumulative(2.0) - dist.cumulative(-2.0);
        assertThat(probWithin2Std).isCloseTo(0.9545, within(0.001));
    }

    @Test
    void empiricalRuleHoldsFor997Percent() {
        NormalDistribution dist = NormalDistribution.of(0.0, 1.0);

        // About 99.7% of values should be within 3 standard deviations of mean
        double probWithin3Std = dist.cumulative(3.0) - dist.cumulative(-3.0);
        assertThat(probWithin3Std).isCloseTo(0.9973, within(0.001));
    }

    // ==================== Edge Case Tests ====================

    @Test
    void handlesVerySmallStandardDeviation() {
        NormalDistribution dist = NormalDistribution.of(0.0, 0.0001);

        // Distribution should be very peaked at the mean
        assertThat(dist.density(0.0)).isGreaterThan(1000.0);
        assertThat(dist.density(0.01)).isCloseTo(0.0, within(1e-10));
    }

    @Test
    void handlesVeryLargeStandardDeviation() {
        NormalDistribution dist = NormalDistribution.of(0.0, 100.0);

        // Distribution should be very flat
        assertThat(dist.density(0.0)).isLessThan(0.01);
        assertThat(dist.density(50.0)).isCloseTo(dist.density(-50.0), within(TOLERANCE));
    }

    @Test
    void handlesExtremeValues() {
        NormalDistribution dist = NormalDistribution.of(0.0, 1.0);

        // Should not throw exceptions or produce NaN
        // Note: density at extreme values may underflow to 0.0
        assertThat(dist.density(100.0)).isGreaterThanOrEqualTo(0.0);
        assertThat(dist.density(-100.0)).isGreaterThanOrEqualTo(0.0);
        assertThat(dist.cumulative(100.0)).isLessThanOrEqualTo(1.0);
        assertThat(dist.cumulative(-100.0)).isGreaterThanOrEqualTo(0.0);
    }

    // ==================== Immutability Tests ====================

    @Test
    void distributionIsImmutable() {
        NormalDistribution dist = NormalDistribution.of(5.0, 2.0);

        double originalMean = dist.getMean();
        double originalStdDev = dist.getStandardDeviation();

        // Perform operations that might modify state
        dist.density(10.0);
        dist.cumulative(10.0);
        dist.inverseCumulative(0.5);

        // Verify state hasn't changed
        assertThat(dist.getMean()).isEqualTo(originalMean);
        assertThat(dist.getStandardDeviation()).isEqualTo(originalStdDev);
    }
}
