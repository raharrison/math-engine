package uk.co.ryanharrison.mathengine.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for {@link StatUtils}.
 * <p>
 * Tests all statistical functions including central tendency measures, dispersion measures,
 * distribution shape measures, correlation/covariance, percentiles, and validation.
 * </p>
 */
class StatUtilsTest {

    private static final double TOLERANCE = 1e-9;

    // ==================== Validation Tests ====================

    @Test
    void meanRejectsNullArray() {
        assertThatThrownBy(() -> StatUtils.mean(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void meanRejectsEmptyArray() {
        assertThatThrownBy(() -> StatUtils.mean(new double[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void varianceRejectsSingleElement() {
        assertThatThrownBy(() -> StatUtils.variance(new double[]{5.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 data points");
    }

    @Test
    void covarianceRejectsMismatchedArrayLengths() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0};

        assertThatThrownBy(() -> StatUtils.covariance(x, y))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same length");
    }

    @Test
    void correlationRejectsNullArrays() {
        double[] valid = {1.0, 2.0, 3.0};

        assertThatThrownBy(() -> StatUtils.correlationCoefficient(null, valid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");

        assertThatThrownBy(() -> StatUtils.correlationCoefficient(valid, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void weightedMeanRejectsNullWeights() {
        double[] data = {1.0, 2.0, 3.0};

        assertThatThrownBy(() -> StatUtils.weightedMean(data, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Weights array cannot be null");
    }

    @Test
    void weightedMeanRejectsEmptyWeights() {
        double[] data = {1.0, 2.0, 3.0};

        assertThatThrownBy(() -> StatUtils.weightedMean(data, new double[0]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Weights array cannot be null or empty");
    }

    @Test
    void weightedMeanRejectsMismatchedLengths() {
        double[] data = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 1.0};

        assertThatThrownBy(() -> StatUtils.weightedMean(data, weights))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same length");
    }

    @Test
    void percentileRejectsInvalidPercentile() {
        double[] data = {1.0, 2.0, 3.0};

        assertThatThrownBy(() -> StatUtils.percentile(data, -0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 1");

        assertThatThrownBy(() -> StatUtils.percentile(data, 1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 1");
    }

    @Test
    void truncatedMeanRejectsInvalidPercentage() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};

        assertThatThrownBy(() -> StatUtils.truncatedMean(data, -5.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 100");

        assertThatThrownBy(() -> StatUtils.truncatedMean(data, 105.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    void harmonicMeanRejectsZeroValue() {
        double[] data = {1.0, 0.0, 3.0};

        assertThatThrownBy(() -> StatUtils.harmonicMean(data))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("undefined when data contains zero");
    }

    @Test
    void populationSkewnessRequiresAtLeastThreeElements() {
        assertThatThrownBy(() -> StatUtils.populationSkewness(new double[]{1.0, 2.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 3 data points");
    }

    @Test
    void populationKurtosisRequiresAtLeastFourElements() {
        assertThatThrownBy(() -> StatUtils.populationKurtosis(new double[]{1.0, 2.0, 3.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 4 data points");
    }

    @Test
    void covarianceRequiresAtLeastTwoElements() {
        assertThatThrownBy(() -> StatUtils.covariance(new double[]{1.0}, new double[]{2.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 data points");
    }

    @Test
    void correlationRequiresAtLeastTwoElements() {
        assertThatThrownBy(() -> StatUtils.correlationCoefficient(new double[]{1.0}, new double[]{2.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 data points");
    }

    // ==================== Central Tendency - Mean Tests ====================

    @Test
    void meanOfSingleElement() {
        assertThat(StatUtils.mean(new double[]{5.0})).isCloseTo(5.0, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "2.0, '1.0, 2.0, 3.0'",
            "3.5, '1.0, 2.0, 3.0, 4.0, 5.0, 6.0'",
            "0.0, '-1.0, 0.0, 1.0'",
            "10.0, '10.0, 10.0, 10.0'",
            "5.5, '1.0, 10.0'",
            "3.0, '1.0, 2.0, 3.0, 4.0, 5.0'"
    })
    void meanCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.mean(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void meanWithNegativeNumbers() {
        double[] data = {-5.0, -10.0, -15.0};
        assertThat(StatUtils.mean(data)).isCloseTo(-10.0, within(TOLERANCE));
    }

    @Test
    void meanWithMixedSigns() {
        double[] data = {-10.0, 0.0, 10.0, 20.0};
        assertThat(StatUtils.mean(data)).isCloseTo(5.0, within(TOLERANCE));
    }

    // ==================== Central Tendency - Median Tests ====================

    @Test
    void medianOfSingleElement() {
        assertThat(StatUtils.median(new double[]{7.5})).isCloseTo(7.5, within(TOLERANCE));
    }

    @Test
    void medianOfTwoElements() {
        assertThat(StatUtils.median(new double[]{3.0, 7.0})).isCloseTo(5.0, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "3.0, '1.0, 2.0, 3.0, 4.0, 5.0'",          // Odd count - middle value
            "3.5, '1.0, 2.0, 3.0, 4.0, 5.0, 6.0'",     // Even count - average of middle two
            "5.0, '5.0, 1.0, 9.0, 3.0, 7.0'",          // Unsorted - odd
            "5.0, '2.0, 8.0, 4.0, 6.0'",               // Unsorted - even
            "0.0, '-5.0, -3.0, 0.0, 3.0, 5.0'"         // With negatives
    })
    void medianCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.median(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void medianDoesNotModifyOriginalArray() {
        double[] data = {5.0, 1.0, 3.0, 2.0, 4.0};
        double[] original = data.clone();

        StatUtils.median(data);

        assertThat(data).isEqualTo(original);
    }

    // ==================== Central Tendency - Geometric Mean Tests ====================

    @Test
    void geometricMeanOfOnes() {
        double[] data = {1.0, 1.0, 1.0, 1.0};
        assertThat(StatUtils.geometricMean(data)).isCloseTo(1.0, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "2.0, '1.0, 2.0, 4.0'",                    // 2^(1/3) * 4^(1/3) = 2
            "3.0, '3.0, 3.0, 3.0'",                    // All same
            "4.0, '2.0, 8.0'",                         // (2*8)^(1/2) = 4
            "10.0, '1.0, 10.0, 100.0'"                 // Powers of 10
    })
    void geometricMeanCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.geometricMean(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void geometricMeanWithNegativeValuesThrowsException() {
        double[] data = {-2.0, 4.0, 8.0};
        assertThatThrownBy(() -> StatUtils.geometricMean(data))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void geometricMeanWithZeroThrowsException() {
        double[] data = {1.0, 0.0, 4.0};
        assertThatThrownBy(() -> StatUtils.geometricMean(data))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==================== Central Tendency - Harmonic Mean Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1.7142857142857142, '1.0, 2.0, 4.0'",     // 3 / (1/1 + 1/2 + 1/4) = 3 / 1.75 = 1.714...
            "2.0, '2.0, 2.0, 2.0'",                    // All same = that value
            "1.8461538461538463, '1.0, 2.0, 3.0, 3.0'" // 4 / (1/1 + 1/2 + 1/3 + 1/3) = 4 / 2.166... = 1.846...
    })
    void harmonicMeanCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.harmonicMean(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void harmonicMeanOfOnes() {
        double[] data = {1.0, 1.0, 1.0};
        assertThat(StatUtils.harmonicMean(data)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void harmonicMeanForRates() {
        // Average speed: half trip at 60 mph, half at 40 mph
        // Harmonic mean = 2 / (1/60 + 1/40) = 48 mph
        double[] speeds = {60.0, 40.0};
        assertThat(StatUtils.harmonicMean(speeds)).isCloseTo(48.0, within(TOLERANCE));
    }

    // ==================== Central Tendency - Weighted Mean Tests ====================

    @Test
    void weightedMeanWithEqualWeights() {
        double[] data = {1.0, 2.0, 3.0, 4.0};
        double[] weights = {1.0, 1.0, 1.0, 1.0};
        assertThat(StatUtils.weightedMean(data, weights)).isCloseTo(2.5, within(TOLERANCE));
    }

    @Test
    void weightedMeanWithDifferentWeights() {
        double[] data = {70.0, 80.0, 90.0};
        double[] weights = {0.2, 0.3, 0.5};
        // (70*0.2 + 80*0.3 + 90*0.5) / (0.2 + 0.3 + 0.5) = 83
        assertThat(StatUtils.weightedMean(data, weights)).isCloseTo(83.0, within(TOLERANCE));
    }

    @Test
    void weightedMeanWithZeroWeights() {
        double[] data = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 0.0, 1.0};
        // Only first and third values count
        assertThat(StatUtils.weightedMean(data, weights)).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void weightedMeanSingleElement() {
        double[] data = {5.0};
        double[] weights = {2.5};
        assertThat(StatUtils.weightedMean(data, weights)).isCloseTo(5.0, within(TOLERANCE));
    }

    // ==================== Central Tendency - Truncated Mean Tests ====================

    @Test
    void truncatedMeanWithZeroPercentRemovesNothing() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(StatUtils.truncatedMean(data, 0.0)).isCloseTo(3.0, within(TOLERANCE));
    }

    @Test
    void truncatedMeanRemovesOutliers() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        // 20% truncation removes 1 from each end (1 and 10)
        double result = StatUtils.truncatedMean(data, 20.0);
        // Mean of {2,3,4,5,6,7,8,9} = 5.5
        assertThat(result).isCloseTo(5.5, within(TOLERANCE));
    }

    @Test
    void truncatedMeanWithHundredPercentReturnsMean() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        // Removing 100% would remove all data, so it returns regular mean
        assertThat(StatUtils.truncatedMean(data, 100.0)).isCloseTo(3.0, within(TOLERANCE));
    }

    @Test
    void truncatedMeanDoesNotModifyOriginalArray() {
        double[] data = {5.0, 1.0, 3.0, 2.0, 4.0};
        double[] original = data.clone();

        StatUtils.truncatedMean(data, 20.0);

        assertThat(data).isEqualTo(original);
    }

    // ==================== Dispersion - Variance Tests ====================

    @Test
    void varianceOfTwoElements() {
        double[] data = {1.0, 3.0};
        // Mean = 2, variance = ((1-2)^2 + (3-2)^2) / 1 = 2
        assertThat(StatUtils.variance(data)).isCloseTo(2.0, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "2.5, '1.0, 2.0, 3.0, 4.0, 5.0'",          // Mean=3, var=((1-3)^2+(2-3)^2+(3-3)^2+(4-3)^2+(5-3)^2)/4 = 10/4 = 2.5
            "0.0, '5.0, 5.0, 5.0'",                    // All same = 0
            "2.5, '0.0, 1.0, 2.0, 3.0, 4.0'"           // Mean=2, var=((0-2)^2+(1-2)^2+(2-2)^2+(3-2)^2+(4-2)^2)/4 = 10/4 = 2.5
    })
    void varianceCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.variance(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void varianceWithNegativeNumbers() {
        double[] data = {-5.0, -10.0, -15.0};
        // Mean = -10, variance = ((5)^2 + (0)^2 + (-5)^2) / 2 = 25
        assertThat(StatUtils.variance(data)).isCloseTo(25.0, within(TOLERANCE));
    }

    @Test
    void varianceOfConstantValuesIsZero() {
        double[] data = {7.5, 7.5, 7.5, 7.5, 7.5};
        assertThat(StatUtils.variance(data)).isCloseTo(0.0, within(TOLERANCE));
    }

    // ==================== Dispersion - Standard Deviation Tests ====================

    @Test
    void standardDeviationIsSqrtOfVariance() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double variance = StatUtils.variance(data);
        double stdDev = StatUtils.standardDeviation(data);
        assertThat(stdDev * stdDev).isCloseTo(variance, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "1.5811388300841898, '1.0, 2.0, 3.0, 4.0, 5.0'",  // sqrt(2.5)
            "0.0, '3.0, 3.0, 3.0'",                            // All same
            "4.0, '1.0, 5.0, 9.0'"                             // mean=5, var=((1-5)^2+(5-5)^2+(9-5)^2)/2 = 32/2 = 16, sd=sqrt(16)=4.0
    })
    void standardDeviationCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.standardDeviation(data)).isCloseTo(expected, within(TOLERANCE));
    }

    // ==================== Dispersion - Population Variance Tests ====================

    @Test
    void populationVarianceDifferentFromSampleVariance() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double sampleVar = StatUtils.variance(data);
        double popVar = StatUtils.populationVariance(data);

        // Population variance uses n, sample uses n-1
        assertThat(popVar).isLessThan(sampleVar);
    }

    @Test
    void populationVarianceOfSingleElement() {
        double[] data = {5.0};
        assertThat(StatUtils.populationVariance(data)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void populationVarianceCalculatesCorrectly() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        // Mean = 3, pop_var = ((1-3)^2 + (2-3)^2 + (3-3)^2 + (4-3)^2 + (5-3)^2) / 5 = 10/5 = 2
        assertThat(StatUtils.populationVariance(data)).isCloseTo(2.0, within(TOLERANCE));
    }

    // ==================== Dispersion - Population Standard Deviation Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1.4142135623730951, '1.0, 2.0, 3.0, 4.0, 5.0'",      // mean=3, pop_var=2, pop_sd=sqrt(2)
            "0.0, '5.0, 5.0, 5.0'",                                 // no variation
            "1.632993161855452, '1.0, 3.0, 5.0'"                   // mean=3, pop_var=8/3, pop_sd=sqrt(8/3)
    })
    void populationStandardDeviationCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.populationStandardDeviation(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void populationStandardDeviationDifferentFromSample() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double sampleSD = StatUtils.standardDeviation(data);
        double popSD = StatUtils.populationStandardDeviation(data);

        // Population SD uses n, sample uses n-1
        assertThat(popSD).isLessThan(sampleSD);
    }

    @Test
    void populationStandardDeviationOfSingleElement() {
        double[] data = {7.5};
        assertThat(StatUtils.populationStandardDeviation(data)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void populationStandardDeviationMatchesSqrtOfPopulationVariance() {
        double[] data = {2.0, 4.0, 6.0, 8.0, 10.0};
        double popVar = StatUtils.populationVariance(data);
        double popSD = StatUtils.populationStandardDeviation(data);

        assertThat(popSD).isCloseTo(Math.sqrt(popVar), within(TOLERANCE));
    }

    @Test
    void populationStandardDeviationHandlesLargeValues() {
        double[] data = {1000.0, 2000.0, 3000.0, 4000.0, 5000.0};
        // Mean = 3000, pop_var = ((1000-3000)^2 + ... + (5000-3000)^2) / 5 = 10000000/5 = 2000000, pop_sd = sqrt(2000000) = 1414.21...
        assertThat(StatUtils.populationStandardDeviation(data)).isCloseTo(1414.213562373095, within(TOLERANCE));
    }

    @Test
    void populationStandardDeviationHandlesNegativeNumbers() {
        double[] data = {-5.0, -3.0, -1.0, 1.0, 3.0, 5.0};
        // Mean = 0, pop_var = (25 + 9 + 1 + 1 + 9 + 25) / 6 = 70/6, pop_sd = sqrt(70/6) = 3.415...
        assertThat(StatUtils.populationStandardDeviation(data)).isCloseTo(3.415650255319866, within(TOLERANCE));
    }

    // ==================== Dispersion - Range Tests ====================

    @ParameterizedTest
    @CsvSource({
            "4.0, '1.0, 2.0, 3.0, 4.0, 5.0'",
            "0.0, '5.0, 5.0, 5.0'",
            "10.0, '-5.0, 5.0'",
            "20.0, '-10.0, 10.0, 0.0'"
    })
    void rangeCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.range(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void rangeOfSingleElement() {
        assertThat(StatUtils.range(new double[]{7.0})).isCloseTo(0.0, within(TOLERANCE));
    }

    // ==================== Dispersion - Interquartile Range Tests ====================

    @Test
    void interQuartileRangeCalculatesCorrectly() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0};
        // Q1 (25th percentile) and Q3 (75th percentile) using linear interpolation
        // Q1 = percentile(0.25) = position (8)*0.25+1 = 3, value at index 2 = 3.0
        // Q3 = percentile(0.75) = position (8)*0.75+1 = 7, value at index 6 = 7.0
        // IQR = Q3 - Q1 = 7.0 - 3.0 = 4.0
        double iqr = StatUtils.interQuartileRange(data);
        assertThat(iqr).isCloseTo(4.0, within(TOLERANCE));
    }

    @Test
    void interQuartileRangeOfUniformData() {
        double[] data = {5.0, 5.0, 5.0, 5.0, 5.0};
        assertThat(StatUtils.interQuartileRange(data)).isCloseTo(0.0, within(TOLERANCE));
    }

    // ==================== Distribution Shape - Skewness Tests ====================

    @Test
    void skewnessOfSymmetricDistribution() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(StatUtils.skewness(data)).isCloseTo(0.0, within(0.01));
    }

    @Test
    void skewnessOfRightSkewedDistribution() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 100.0}; // Outlier on right
        assertThat(StatUtils.skewness(data)).isGreaterThan(0.0);
    }

    @Test
    void skewnessOfLeftSkewedDistribution() {
        double[] data = {-100.0, 1.0, 2.0, 3.0, 4.0}; // Outlier on left
        assertThat(StatUtils.skewness(data)).isLessThan(0.0);
    }

    @Test
    void skewnessOfConstantValues() {
        double[] data = {5.0, 5.0, 5.0, 5.0};
        // With zero variance, result will be NaN or 0 depending on implementation
        double result = StatUtils.skewness(data);
        assertThat(result).satisfiesAnyOf(
                value -> assertThat(value).isNaN(),
                value -> assertThat(value).isCloseTo(0.0, within(TOLERANCE))
        );
    }

    // ==================== Distribution Shape - Population Skewness Tests ====================

    @Test
    void populationSkewnessOfSymmetricData() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(StatUtils.populationSkewness(data)).isCloseTo(0.0, within(0.01));
    }

    @Test
    void populationSkewnessRequiresMinimumThreeElements() {
        double[] data = {1.0, 2.0, 3.0};
        // Should not throw with exactly 3 elements
        assertThatCode(() -> StatUtils.populationSkewness(data)).doesNotThrowAnyException();
    }

    // ==================== Distribution Shape - Kurtosis Tests ====================

    @Test
    void kurtosisOfNormalLikeDistribution() {
        // Uniformly distributed data has negative excess kurtosis
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0};
        double kurtosis = StatUtils.kurtosis(data);
        // Uniform distribution has excess kurtosis around -1.2
        assertThat(kurtosis).isLessThan(0.0);
    }

    @Test
    void kurtosisOfDataWithOutliers() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0, 100.0}; // Extreme outlier
        double kurtosis = StatUtils.kurtosis(data);
        // Heavy tail = positive excess kurtosis
        assertThat(kurtosis).isGreaterThan(0.0);
    }

    @Test
    void kurtosisOfConstantValues() {
        double[] data = {5.0, 5.0, 5.0, 5.0};
        double result = StatUtils.kurtosis(data);
        // Division by zero in denominator
        assertThat(result).satisfiesAnyOf(
                value -> assertThat(value).isNaN(),
                value -> assertThat(value).isInfinite()
        );
    }

    // ==================== Distribution Shape - Population Kurtosis Tests ====================

    @Test
    void populationKurtosisRequiresMinimumFourElements() {
        double[] data = {1.0, 2.0, 3.0, 4.0};
        // Should not throw with exactly 4 elements
        assertThatCode(() -> StatUtils.populationKurtosis(data)).doesNotThrowAnyException();
    }

    @Test
    void populationKurtosisCalculatesWithBiasCorrection() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double popKurtosis = StatUtils.populationKurtosis(data);
        double sampleKurtosis = StatUtils.kurtosis(data);
        // They should be different due to bias correction
        assertThat(popKurtosis).isNotEqualTo(sampleKurtosis);
    }

    // ==================== Correlation and Covariance Tests ====================

    @Test
    void covarianceOfPerfectlyCorrelatedData() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 6.0, 8.0, 10.0}; // y = 2x
        double cov = StatUtils.covariance(x, y);
        assertThat(cov).isGreaterThan(0.0);
    }

    @Test
    void covarianceOfNegativelyCorrelatedData() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {5.0, 4.0, 3.0, 2.0, 1.0}; // Inverse
        double cov = StatUtils.covariance(x, y);
        assertThat(cov).isLessThan(0.0);
    }

    @Test
    void covarianceOfUncorrelatedData() {
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 3.0}; // y is constant
        double cov = StatUtils.covariance(x, y);
        assertThat(cov).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void populationCovarianceDifferentFromSampleCovariance() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 5.0, 7.0, 9.0};

        double sampleCov = StatUtils.covariance(x, y);
        double popCov = StatUtils.populationCovariance(x, y);

        // Population uses n, sample uses n-1
        assertThat(popCov).isLessThan(sampleCov);
    }

    @Test
    void populationCovarianceOfTwoElements() {
        double[] x = {1.0, 3.0};
        double[] y = {2.0, 4.0};
        // Should not require minimum elements (unlike sample covariance)
        assertThatCode(() -> StatUtils.populationCovariance(x, y)).doesNotThrowAnyException();
    }

    @Test
    void correlationCoefficientOfPerfectPositiveCorrelation() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 6.0, 8.0, 10.0}; // y = 2x
        assertThat(StatUtils.correlationCoefficient(x, y)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void correlationCoefficientOfPerfectNegativeCorrelation() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {10.0, 8.0, 6.0, 4.0, 2.0}; // Perfect inverse
        assertThat(StatUtils.correlationCoefficient(x, y)).isCloseTo(-1.0, within(TOLERANCE));
    }

    @Test
    void correlationCoefficientOfNoCorrelation() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {3.0, 3.0, 3.0, 3.0, 3.0}; // Constant
        // Correlation with constant is undefined (0/0), likely returns NaN
        double result = StatUtils.correlationCoefficient(x, y);
        assertThat(result).isNaN();
    }

    @Test
    void correlationCoefficientBoundedByOne() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {1.5, 3.2, 2.8, 4.1, 5.3};
        double corr = StatUtils.correlationCoefficient(x, y);
        assertThat(corr).isBetween(-1.0, 1.0);
    }

    @Test
    void correlationCoefficientWithKnownValue() {
        // Example from statistics textbook
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {1.0, 3.0, 2.0, 5.0, 4.0};
        // Manually calculated correlation ≈ 0.8
        assertThat(StatUtils.correlationCoefficient(x, y)).isCloseTo(0.8, within(0.1));
    }

    // ==================== Population Correlation Coefficient Tests ====================

    @Test
    void populationCorrelationCoefficientOfPerfectPositiveCorrelation() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 6.0, 8.0, 10.0}; // y = 2x
        assertThat(StatUtils.populationCorrelationCoefficient(x, y)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void populationCorrelationCoefficientOfPerfectNegativeCorrelation() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {10.0, 8.0, 6.0, 4.0, 2.0}; // Perfect inverse
        assertThat(StatUtils.populationCorrelationCoefficient(x, y)).isCloseTo(-1.0, within(TOLERANCE));
    }

    @Test
    void populationCorrelationCoefficientDifferentFromSample() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {1.0, 3.0, 2.0, 5.0, 4.0};

        double sampleCorr = StatUtils.correlationCoefficient(x, y);
        double popCorr = StatUtils.populationCorrelationCoefficient(x, y);

        // They should be close but not identical for small samples
        assertThat(Math.abs(sampleCorr - popCorr)).isLessThan(0.2);
    }

    @Test
    void populationCorrelationCoefficientOfNoCorrelation() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {3.0, 3.0, 3.0, 3.0, 3.0}; // Constant
        // Correlation with constant is undefined (0/0), likely returns NaN
        double result = StatUtils.populationCorrelationCoefficient(x, y);
        assertThat(result).isNaN();
    }

    @Test
    void populationCorrelationCoefficientBoundedByOne() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {1.5, 3.2, 2.8, 4.1, 5.3};
        double corr = StatUtils.populationCorrelationCoefficient(x, y);
        assertThat(corr).isBetween(-1.0, 1.0);
    }

    @Test
    void populationCorrelationCoefficientWorksWithTwoElements() {
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 4.0};
        // Should work for population (unlike sample which requires >= 2)
        assertThatCode(() -> StatUtils.populationCorrelationCoefficient(x, y)).doesNotThrowAnyException();
        assertThat(StatUtils.populationCorrelationCoefficient(x, y)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void populationCorrelationCoefficientWithSingleElement() {
        double[] x = {5.0};
        double[] y = {10.0};
        // Single element: SD=0, correlation is 0/0 = NaN
        double result = StatUtils.populationCorrelationCoefficient(x, y);
        assertThat(result).isNaN();
    }

    // ==================== Percentile Tests ====================

    @Test
    void percentileAtZeroGivesMinimum() {
        double[] data = {5.0, 1.0, 9.0, 3.0, 7.0};
        assertThat(StatUtils.percentile(data, 0.0)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void percentileAtOneGivesMaximum() {
        double[] data = {5.0, 1.0, 9.0, 3.0, 7.0};
        assertThat(StatUtils.percentile(data, 1.0)).isCloseTo(9.0, within(TOLERANCE));
    }

    @Test
    void percentileAtHalfGivesMedian() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(StatUtils.percentile(data, 0.5)).isCloseTo(StatUtils.median(data), within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0.25, 2.0",    // Q1
            "0.50, 3.0",    // Median
            "0.75, 4.0"     // Q3
    })
    void percentileQuartiles(double percentile, double expected) {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(StatUtils.percentile(data, percentile)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void percentileDoesNotModifyOriginalArray() {
        double[] data = {5.0, 1.0, 3.0, 2.0, 4.0};
        double[] original = data.clone();

        StatUtils.percentile(data, 0.5);

        assertThat(data).isEqualTo(original);
    }

    @Test
    void percentileOfSingleElement() {
        double[] data = {7.5};
        assertThat(StatUtils.percentile(data, 0.25)).isCloseTo(7.5, within(TOLERANCE));
        assertThat(StatUtils.percentile(data, 0.75)).isCloseTo(7.5, within(TOLERANCE));
    }

    @Test
    void percentileWithLinearInterpolation() {
        double[] data = {1.0, 2.0, 3.0, 4.0};
        // 30th percentile should interpolate between values
        double p30 = StatUtils.percentile(data, 0.30);
        assertThat(p30).isBetween(1.0, 4.0);
    }

    // ==================== Min/Max Tests ====================

    @Test
    void minFindsSmallestValue() {
        assertThat(StatUtils.min(new double[]{5.0, 1.0, 9.0, 3.0})).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void minOfSingleElement() {
        assertThat(StatUtils.min(new double[]{7.5})).isCloseTo(7.5, within(TOLERANCE));
    }

    @Test
    void minWithNegativeNumbers() {
        assertThat(StatUtils.min(new double[]{5.0, -10.0, 3.0})).isCloseTo(-10.0, within(TOLERANCE));
    }

    @Test
    void maxFindsLargestValue() {
        assertThat(StatUtils.max(new double[]{5.0, 1.0, 9.0, 3.0})).isCloseTo(9.0, within(TOLERANCE));
    }

    @Test
    void maxOfSingleElement() {
        assertThat(StatUtils.max(new double[]{7.5})).isCloseTo(7.5, within(TOLERANCE));
    }

    @Test
    void maxWithNegativeNumbers() {
        assertThat(StatUtils.max(new double[]{-5.0, -10.0, -3.0})).isCloseTo(-3.0, within(TOLERANCE));
    }

    // ==================== Summation Tests ====================

    @ParameterizedTest
    @CsvSource({
            "15.0, '1.0, 2.0, 3.0, 4.0, 5.0'",
            "0.0, '-5.0, 5.0'",
            "10.0, '10.0'",
            "-15.0, '-5.0, -10.0'"
    })
    void sumCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.sum(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "55.0, '1.0, 2.0, 3.0, 4.0, 5.0'",         // 1+4+9+16+25
            "50.0, '-5.0, 5.0'",                        // 25+25
            "100.0, '10.0'",                            // 100
            "125.0, '-5.0, -10.0'"                      // 25+100
    })
    void sumOfSquaresCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.sumOfSquares(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "120.0, '1.0, 2.0, 3.0, 4.0, 5.0'",        // 1*2*3*4*5
            "-25.0, '-5.0, 5.0'",                       // -25
            "10.0, '10.0'",                             // 10
            "0.0, '1.0, 0.0, 5.0'"                      // Contains zero
    })
    void productCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.product(data)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void rootMeanSquareCalculatesCorrectly() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        // RMS = sqrt((1 + 4 + 9 + 16 + 25) / 5) = sqrt(55/5) = sqrt(11) ≈ 3.317
        assertThat(StatUtils.rootmeanSquare(data)).isCloseTo(Math.sqrt(11.0), within(TOLERANCE));
    }

    @Test
    void rootMeanSquareOfSingleElement() {
        double[] data = {5.0};
        assertThat(StatUtils.rootmeanSquare(data)).isCloseTo(5.0, within(TOLERANCE));
    }

    @Test
    void rootMeanSquareOfNegativeValues() {
        double[] data = {-3.0, -4.0};
        // RMS = sqrt((9 + 16) / 2) = sqrt(12.5) ≈ 3.536
        assertThat(StatUtils.rootmeanSquare(data)).isCloseTo(Math.sqrt(12.5), within(TOLERANCE));
    }

    // ==================== Edge Cases and Special Values ====================

    @Test
    void handlesVeryLargeNumbers() {
        double[] data = {1e100, 2e100, 3e100};
        assertThat(StatUtils.mean(data)).isCloseTo(2e100, within(1e90));
    }

    @Test
    void handlesVerySmallNumbers() {
        double[] data = {1e-100, 2e-100, 3e-100};
        assertThat(StatUtils.mean(data)).isCloseTo(2e-100, within(1e-110));
    }

    @Test
    void handlesAllZeros() {
        double[] data = {0.0, 0.0, 0.0, 0.0};
        assertThat(StatUtils.mean(data)).isCloseTo(0.0, within(TOLERANCE));
        assertThat(StatUtils.median(data)).isCloseTo(0.0, within(TOLERANCE));
        assertThat(StatUtils.variance(data)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void handlesDoubleMaxValue() {
        double[] data = {Double.MAX_VALUE, 0.0};
        assertThat(StatUtils.mean(data)).isGreaterThan(0.0);
        assertThat(StatUtils.max(data)).isEqualTo(Double.MAX_VALUE);
    }

    @Test
    void handlesDoubleMinValue() {
        double[] data = {Double.MIN_VALUE, 1.0};
        assertThat(StatUtils.min(data)).isEqualTo(Double.MIN_VALUE);
    }

    // ==================== Comprehensive Integration Tests ====================

    @Test
    void allStatisticalMeasuresOnKnownDataset() {
        // Known dataset with verified statistical properties
        double[] data = {2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0};

        assertThat(StatUtils.mean(data)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(StatUtils.median(data)).isCloseTo(4.5, within(TOLERANCE));
        assertThat(StatUtils.min(data)).isCloseTo(2.0, within(TOLERANCE));
        assertThat(StatUtils.max(data)).isCloseTo(9.0, within(TOLERANCE));
        assertThat(StatUtils.range(data)).isCloseTo(7.0, within(TOLERANCE));
        // Sample variance = sum((x_i - mean)^2) / (n-1)
        // Mean = 5.0, sum of squared deviations = 32, variance = 32/7 ≈ 4.571
        assertThat(StatUtils.variance(data)).isCloseTo(4.571428571428571, within(TOLERANCE));
        assertThat(StatUtils.standardDeviation(data)).isCloseTo(Math.sqrt(4.571428571428571), within(TOLERANCE));
    }

    @Test
    void statisticsOnNormallyDistributedData() {
        // Simulated normal distribution (mean=100, sd=15)
        double[] data = {70.0, 85.0, 100.0, 115.0, 130.0};

        assertThat(StatUtils.mean(data)).isCloseTo(100.0, within(TOLERANCE));
        assertThat(StatUtils.median(data)).isCloseTo(100.0, within(TOLERANCE));
        assertThat(StatUtils.skewness(data)).isCloseTo(0.0, within(0.1)); // Symmetric
    }

    // ==================== Quartile Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1, 2.0, '1.0, 2.0, 3.0, 4.0, 5.0'",    // Q1 = 25th percentile
            "2, 3.0, '1.0, 2.0, 3.0, 4.0, 5.0'",    // Q2 = 50th percentile (median)
            "3, 4.0, '1.0, 2.0, 3.0, 4.0, 5.0'"     // Q3 = 75th percentile
    })
    void quartileCalculatesCorrectly(int quartile, double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.quartile(data, quartile)).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void quartileRejectsInvalidQuartile() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};

        assertThatThrownBy(() -> StatUtils.quartile(data, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be 1, 2, or 3");

        assertThatThrownBy(() -> StatUtils.quartile(data, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be 1, 2, or 3");

        assertThatThrownBy(() -> StatUtils.quartile(data, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be 1, 2, or 3");
    }

    @Test
    void quartileMatchesPercentile() {
        double[] data = {2.0, 4.0, 6.0, 8.0, 10.0, 12.0, 14.0};

        assertThat(StatUtils.quartile(data, 1))
                .isCloseTo(StatUtils.percentile(data, 0.25), within(TOLERANCE));
        assertThat(StatUtils.quartile(data, 2))
                .isCloseTo(StatUtils.percentile(data, 0.50), within(TOLERANCE));
        assertThat(StatUtils.quartile(data, 3))
                .isCloseTo(StatUtils.percentile(data, 0.75), within(TOLERANCE));
    }

    @Test
    void quartileOfUniformData() {
        double[] data = {5.0, 5.0, 5.0, 5.0, 5.0};
        assertThat(StatUtils.quartile(data, 1)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(StatUtils.quartile(data, 2)).isCloseTo(5.0, within(TOLERANCE));
        assertThat(StatUtils.quartile(data, 3)).isCloseTo(5.0, within(TOLERANCE));
    }

    @Test
    void quartileWithSmallDataset() {
        double[] data = {1.0, 2.0, 3.0};
        assertThatCode(() -> {
            StatUtils.quartile(data, 1);
            StatUtils.quartile(data, 2);
            StatUtils.quartile(data, 3);
        }).doesNotThrowAnyException();
    }

    // ==================== Coefficient of Variation Tests ====================

    @ParameterizedTest
    @CsvSource({
            "51.63977794943222, '10.0, 20.0, 30.0, 40.0'",           // Mean=25, SD=12.909..., CV=51.64%
            "0.0, '5.0, 5.0, 5.0, 5.0'",                 // No variation
            "52.70462766947299, '1.0, 2.0, 3.0, 4.0, 5.0'" // Mean=3, SD=1.414..., CV=47.14%
    })
    void coefficientOfVariationCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.coefficientOfVariation(data)).isCloseTo(expected, within(0.1));
    }

    @Test
    void coefficientOfVariationRejectsZeroMean() {
        double[] data = {-2.0, 0.0, 2.0};
        assertThatThrownBy(() -> StatUtils.coefficientOfVariation(data))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("undefined when mean is zero");
    }

    @Test
    void coefficientOfVariationRequiresAtLeastTwoElements() {
        assertThatThrownBy(() -> StatUtils.coefficientOfVariation(new double[]{5.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 data points");
    }

    @Test
    void coefficientOfVariationWithNegativeMean() {
        double[] data = {-10.0, -20.0, -30.0};
        // CV uses absolute value of mean
        double cv = StatUtils.coefficientOfVariation(data);
        assertThat(cv).isGreaterThan(0.0);
    }

    @Test
    void coefficientOfVariationForComparisonBetweenDatasets() {
        // Dataset A: smaller values, smaller variation
        double[] dataA = {1.0, 2.0, 3.0, 4.0, 5.0};
        // Dataset B: larger values, larger absolute variation but similar relative variation
        double[] dataB = {10.0, 20.0, 30.0, 40.0, 50.0};

        double cvA = StatUtils.coefficientOfVariation(dataA);
        double cvB = StatUtils.coefficientOfVariation(dataB);

        // CV should be similar since relative variation is similar
        assertThat(cvA).isCloseTo(cvB, within(0.1));
    }

    @Test
    void coefficientOfVariationPercentageFormat() {
        double[] data = {10.0, 15.0, 20.0};
        // Mean = 15, SD ≈ 5, CV ≈ 33.33%
        double cv = StatUtils.coefficientOfVariation(data);
        assertThat(cv).isBetween(0.0, 100.0); // Should be percentage
        assertThat(cv).isCloseTo(33.33, within(0.5));
    }

    // ==================== Population Coefficient of Variation Tests ====================

    @ParameterizedTest
    @CsvSource({
            "44.721359549995796, '10.0, 20.0, 30.0, 40.0'",        // Mean=25, Pop SD=11.180..., Pop CV=44.72%
            "0.0, '5.0, 5.0, 5.0, 5.0'",                           // No variation
            "70.23077760347498, '1.0, 5.0, 7.0, 14.0, 4.0'"        // Mean=3, Pop SD=1.414..., Pop CV=47.14%
    })
    void populationCoefficientOfVariationCalculatesCorrectly(double expected, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.populationCoefficientOfVariation(data)).isCloseTo(expected, within(0.1));
    }

    @Test
    void populationCoefficientOfVariationDifferentFromSample() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double sampleCV = StatUtils.coefficientOfVariation(data);
        double popCV = StatUtils.populationCoefficientOfVariation(data);

        // Population CV uses n, sample uses n-1
        assertThat(popCV).isLessThan(sampleCV);
    }

    @Test
    void populationCoefficientOfVariationRejectsZeroMean() {
        double[] data = {-2.0, 0.0, 2.0};
        assertThatThrownBy(() -> StatUtils.populationCoefficientOfVariation(data))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("undefined when mean is zero");
    }

    @Test
    void populationCoefficientOfVariationWithNegativeMean() {
        double[] data = {-10.0, -20.0, -30.0};
        // Population CV uses absolute value of mean
        double cv = StatUtils.populationCoefficientOfVariation(data);
        assertThat(cv).isGreaterThan(0.0);
    }

    @Test
    void populationCoefficientOfVariationWorksWithSingleElement() {
        double[] data = {42.0};
        // Single element: pop_sd=0, cv=0
        assertThat(StatUtils.populationCoefficientOfVariation(data)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void populationCoefficientOfVariationPercentageFormat() {
        double[] data = {10.0, 15.0, 20.0};
        // Mean = 15, Pop SD = sqrt((25+0+25)/3) = sqrt(50/3) ≈ 4.08, Pop CV ≈ 27.22%
        double cv = StatUtils.populationCoefficientOfVariation(data);
        assertThat(cv).isBetween(0.0, 100.0); // Should be percentage
        assertThat(cv).isCloseTo(27.22, within(0.5));
    }

    // ==================== Z-Score Tests ====================

    @Test
    void zScoreOfMeanIsZero() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double mean = StatUtils.mean(data);
        assertThat(StatUtils.zScore(mean, data)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void zScoreOfValueAboveMeanIsPositive() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(StatUtils.zScore(5.0, data)).isGreaterThan(0.0);
    }

    @Test
    void zScoreOfValueBelowMeanIsNegative() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(StatUtils.zScore(1.0, data)).isLessThan(0.0);
    }

    @ParameterizedTest
    @CsvSource({
            "3.0, 0.0, '1.0, 2.0, 3.0, 4.0, 5.0'",           // Mean value
            "4.0, 0.632455532033676, '1.0, 2.0, 3.0, 4.0, 5.0'",  // One SD above
            "2.0, -0.632455532033676, '1.0, 2.0, 3.0, 4.0, 5.0'"  // One SD below
    })
    void zScoreCalculatesCorrectly(double value, double expectedZ, String dataStr) {
        double[] data = parseDoubleArray(dataStr);
        assertThat(StatUtils.zScore(value, data)).isCloseTo(expectedZ, within(TOLERANCE));
    }

    @Test
    void zScoreRequiresAtLeastTwoElements() {
        assertThatThrownBy(() -> StatUtils.zScore(5.0, new double[]{3.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 data points");
    }

    @Test
    void zScoreWithZeroStandardDeviationReturnsZero() {
        double[] data = {5.0, 5.0, 5.0, 5.0}; // All identical
        assertThat(StatUtils.zScore(5.0, data)).isCloseTo(0.0, within(TOLERANCE));
        assertThat(StatUtils.zScore(10.0, data)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void zScoreForOutlier() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        // Value far from mean should have high absolute z-score
        double z = StatUtils.zScore(100.0, data);
        assertThat(Math.abs(z)).isGreaterThan(5.0);
    }

    @Test
    void zScoreWithNegativeValues() {
        double[] data = {-10.0, -5.0, 0.0, 5.0, 10.0};
        assertThat(StatUtils.zScore(0.0, data)).isCloseTo(0.0, within(TOLERANCE));
        assertThat(StatUtils.zScore(10.0, data)).isGreaterThan(0.0);
        assertThat(StatUtils.zScore(-10.0, data)).isLessThan(0.0);
    }

    // ==================== Normalize Tests ====================

    @Test
    void normalizeProducesZeroMean() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] normalized = StatUtils.normalize(data);
        assertThat(StatUtils.mean(normalized)).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void normalizeProducesUnitStandardDeviation() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] normalized = StatUtils.normalize(data);
        assertThat(StatUtils.standardDeviation(normalized)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void normalizeRequiresAtLeastTwoElements() {
        assertThatThrownBy(() -> StatUtils.normalize(new double[]{5.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 data points");
    }

    @Test
    void normalizeWithZeroStandardDeviationReturnsZeros() {
        double[] data = {5.0, 5.0, 5.0, 5.0};
        double[] normalized = StatUtils.normalize(data);
        assertThat(normalized).hasSize(4);
        for (double value : normalized) {
            assertThat(value).isCloseTo(0.0, within(TOLERANCE));
        }
    }

    @Test
    void normalizeDoesNotModifyOriginalArray() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] original = data.clone();
        StatUtils.normalize(data);
        assertThat(data).isEqualTo(original);
    }

    @Test
    void normalizePreservesRelativeOrder() {
        double[] data = {5.0, 1.0, 9.0, 3.0, 7.0};
        double[] normalized = StatUtils.normalize(data);

        // Smallest value should still be smallest after normalization
        int minIdx = 0;
        int maxIdx = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] < data[minIdx]) minIdx = i;
            if (data[i] > data[maxIdx]) maxIdx = i;
        }

        assertThat(normalized[minIdx]).isLessThan(normalized[maxIdx]);
    }

    @Test
    void normalizeWithKnownValues() {
        double[] data = {10.0, 20.0, 30.0};
        double[] normalized = StatUtils.normalize(data);

        // Mean = 20, SD ≈ 10
        // Z-scores: (10-20)/10 = -1, (20-20)/10 = 0, (30-20)/10 = 1
        assertThat(normalized[0]).isCloseTo(-1.0, within(TOLERANCE));
        assertThat(normalized[1]).isCloseTo(0.0, within(TOLERANCE));
        assertThat(normalized[2]).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void normalizeWithNegativeValues() {
        double[] data = {-10.0, 0.0, 10.0};
        double[] normalized = StatUtils.normalize(data);

        assertThat(StatUtils.mean(normalized)).isCloseTo(0.0, within(TOLERANCE));
        assertThat(StatUtils.standardDeviation(normalized)).isCloseTo(1.0, within(TOLERANCE));
    }

    // ==================== Moving Average Tests ====================

    @Test
    void movingAverageWithWindowOfOne() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] ma = StatUtils.movingAverage(data, 1);
        assertThat(ma).hasSize(5);
        assertThat(ma).containsExactly(data);
    }

    @Test
    void movingAverageWithWindowOfThree() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] ma = StatUtils.movingAverage(data, 3);

        assertThat(ma).hasSize(3); // 5 - 3 + 1 = 3
        assertThat(ma[0]).isCloseTo(2.0, within(TOLERANCE)); // (1+2+3)/3
        assertThat(ma[1]).isCloseTo(3.0, within(TOLERANCE)); // (2+3+4)/3
        assertThat(ma[2]).isCloseTo(4.0, within(TOLERANCE)); // (3+4+5)/3
    }

    @Test
    void movingAverageWithWindowEqualToDataLength() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] ma = StatUtils.movingAverage(data, 5);

        assertThat(ma).hasSize(1);
        assertThat(ma[0]).isCloseTo(3.0, within(TOLERANCE)); // Mean of all
    }

    @Test
    void movingAverageRejectsInvalidWindow() {
        double[] data = {1.0, 2.0, 3.0};

        assertThatThrownBy(() -> StatUtils.movingAverage(data, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Window must be between 1 and");

        assertThatThrownBy(() -> StatUtils.movingAverage(data, 4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Window must be between 1 and");

        assertThatThrownBy(() -> StatUtils.movingAverage(data, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Window must be between 1 and");
    }

    @Test
    void movingAverageSmoothesTrend() {
        double[] data = {1.0, 10.0, 2.0, 20.0, 3.0}; // Noisy data
        double[] ma = StatUtils.movingAverage(data, 3);

        // Moving average should reduce volatility
        double originalRange = StatUtils.range(data);
        double smoothedRange = StatUtils.range(ma);
        assertThat(smoothedRange).isLessThan(originalRange);
    }

    @Test
    void movingAverageWithConstantData() {
        double[] data = {5.0, 5.0, 5.0, 5.0, 5.0};
        double[] ma = StatUtils.movingAverage(data, 3);

        for (double value : ma) {
            assertThat(value).isCloseTo(5.0, within(TOLERANCE));
        }
    }

    @Test
    void movingAverageDoesNotModifyOriginalArray() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] original = data.clone();
        StatUtils.movingAverage(data, 2);
        assertThat(data).isEqualTo(original);
    }

    // ==================== Exponential Moving Average Tests ====================

    @Test
    void exponentialMovingAverageWithAlphaOne() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] ema = StatUtils.exponentialMovingAverage(data, 1.0);

        // Alpha = 1 means EMA = current value
        assertThat(ema).hasSize(5);
        assertThat(ema).containsExactly(data);
    }

    @Test
    void exponentialMovingAverageWithAlphaPointFive() {
        double[] data = {10.0, 20.0, 30.0};
        double[] ema = StatUtils.exponentialMovingAverage(data, 0.5);

        assertThat(ema).hasSize(3);
        assertThat(ema[0]).isCloseTo(10.0, within(TOLERANCE)); // First value
        assertThat(ema[1]).isCloseTo(15.0, within(TOLERANCE)); // 0.5*20 + 0.5*10 = 15
        assertThat(ema[2]).isCloseTo(22.5, within(TOLERANCE)); // 0.5*30 + 0.5*15 = 22.5
    }

    @Test
    void exponentialMovingAverageRejectsInvalidAlpha() {
        double[] data = {1.0, 2.0, 3.0};

        assertThatThrownBy(() -> StatUtils.exponentialMovingAverage(data, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alpha must be in (0, 1]");

        assertThatThrownBy(() -> StatUtils.exponentialMovingAverage(data, 1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alpha must be in (0, 1]");

        assertThatThrownBy(() -> StatUtils.exponentialMovingAverage(data, -0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alpha must be in (0, 1]");
    }

    @Test
    void exponentialMovingAverageWithSmallAlpha() {
        double[] data = {100.0, 10.0};
        double[] ema = StatUtils.exponentialMovingAverage(data, 0.1);

        // Small alpha means more weight on previous values
        assertThat(ema[0]).isCloseTo(100.0, within(TOLERANCE));
        assertThat(ema[1]).isCloseTo(91.0, within(TOLERANCE)); // 0.1*10 + 0.9*100 = 91
    }

    @Test
    void exponentialMovingAverageSmoothesTrend() {
        double[] data = {5.0, 15.0, 25.0, 35.0, 45.0};
        double[] ema = StatUtils.exponentialMovingAverage(data, 0.3);

        // EMA should lag behind actual trend with small alpha
        assertThat(ema[4]).isLessThan(data[4]);
    }

    @Test
    void exponentialMovingAverageWithSingleElement() {
        double[] data = {7.5};
        double[] ema = StatUtils.exponentialMovingAverage(data, 0.5);
        assertThat(ema).hasSize(1);
        assertThat(ema[0]).isCloseTo(7.5, within(TOLERANCE));
    }

    @Test
    void exponentialMovingAverageDoesNotModifyOriginalArray() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] original = data.clone();
        StatUtils.exponentialMovingAverage(data, 0.5);
        assertThat(data).isEqualTo(original);
    }

    // ==================== Frequency Count Tests ====================

    @Test
    void frequencyCountWithUniqueValues() {
        double[] data = {1.0, 2.0, 3.0, 4.0};
        double[][] freq = StatUtils.frequencyCount(data);

        assertThat(freq).hasDimensions(4, 2);
        for (double[] pair : freq) {
            assertThat(pair[1]).isCloseTo(1.0, within(TOLERANCE)); // Each appears once
        }
    }

    @Test
    void frequencyCountWithDuplicates() {
        double[] data = {1.0, 2.0, 2.0, 3.0, 3.0, 3.0};
        double[][] freq = StatUtils.frequencyCount(data);

        assertThat(freq).hasDimensions(3, 2);
        assertThat(freq[0][0]).isCloseTo(1.0, within(TOLERANCE));
        assertThat(freq[0][1]).isCloseTo(1.0, within(TOLERANCE)); // 1 appears 1 time

        assertThat(freq[1][0]).isCloseTo(2.0, within(TOLERANCE));
        assertThat(freq[1][1]).isCloseTo(2.0, within(TOLERANCE)); // 2 appears 2 times

        assertThat(freq[2][0]).isCloseTo(3.0, within(TOLERANCE));
        assertThat(freq[2][1]).isCloseTo(3.0, within(TOLERANCE)); // 3 appears 3 times
    }

    @Test
    void frequencyCountSortsByValue() {
        double[] data = {5.0, 1.0, 3.0, 1.0, 5.0};
        double[][] freq = StatUtils.frequencyCount(data);

        assertThat(freq).hasDimensions(3, 2);
        // Should be sorted by value
        assertThat(freq[0][0]).isCloseTo(1.0, within(TOLERANCE));
        assertThat(freq[1][0]).isCloseTo(3.0, within(TOLERANCE));
        assertThat(freq[2][0]).isCloseTo(5.0, within(TOLERANCE));
    }

    @Test
    void frequencyCountWithSingleElement() {
        double[] data = {7.5};
        double[][] freq = StatUtils.frequencyCount(data);

        assertThat(freq).hasDimensions(1, 2);
        assertThat(freq[0][0]).isCloseTo(7.5, within(TOLERANCE));
        assertThat(freq[0][1]).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void frequencyCountWithAllSameValues() {
        double[] data = {5.0, 5.0, 5.0, 5.0};
        double[][] freq = StatUtils.frequencyCount(data);

        assertThat(freq).hasDimensions(1, 2);
        assertThat(freq[0][0]).isCloseTo(5.0, within(TOLERANCE));
        assertThat(freq[0][1]).isCloseTo(4.0, within(TOLERANCE));
    }

    @Test
    void frequencyCountDoesNotModifyOriginalArray() {
        double[] data = {5.0, 1.0, 3.0, 2.0, 4.0};
        double[] original = data.clone();
        StatUtils.frequencyCount(data);
        assertThat(data).isEqualTo(original);
    }

    // ==================== Mode Tests ====================

    @Test
    void modeWithSingleMode() {
        double[] data = {1.0, 2.0, 3.0, 3.0, 3.0, 4.0, 5.0};
        double[] modes = StatUtils.mode(data);

        assertThat(modes).hasSize(1);
        assertThat(modes[0]).isCloseTo(3.0, within(TOLERANCE));
    }

    @Test
    void modeWithMultipleModes() {
        double[] data = {1.0, 1.0, 2.0, 2.0, 3.0}; // Bimodal: 1 and 2
        double[] modes = StatUtils.mode(data);

        assertThat(modes).hasSize(2);
        assertThat(modes).contains(1.0, 2.0);
    }

    @Test
    void modeWithAllUniqueValues() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] modes = StatUtils.mode(data);

        // All values appear once, so all are modes
        assertThat(modes).hasSize(5);
    }

    @Test
    void modeWithAllSameValues() {
        double[] data = {5.0, 5.0, 5.0, 5.0};
        double[] modes = StatUtils.mode(data);

        assertThat(modes).hasSize(1);
        assertThat(modes[0]).isCloseTo(5.0, within(TOLERANCE));
    }

    @Test
    void modeWithSingleElement() {
        double[] data = {7.5};
        double[] modes = StatUtils.mode(data);

        assertThat(modes).hasSize(1);
        assertThat(modes[0]).isCloseTo(7.5, within(TOLERANCE));
    }

    @Test
    void modeOfTrimodalDistribution() {
        double[] data = {1.0, 1.0, 2.0, 2.0, 3.0, 3.0};
        double[] modes = StatUtils.mode(data);

        assertThat(modes).hasSize(3);
        assertThat(modes).contains(1.0, 2.0, 3.0);
    }

    @Test
    void modeWithNegativeValues() {
        double[] data = {-5.0, -5.0, -5.0, -2.0, 0.0, 3.0};
        double[] modes = StatUtils.mode(data);

        assertThat(modes).hasSize(1);
        assertThat(modes[0]).isCloseTo(-5.0, within(TOLERANCE));
    }

    @Test
    void modeDoesNotModifyOriginalArray() {
        double[] data = {5.0, 1.0, 3.0, 2.0, 4.0};
        double[] original = data.clone();
        StatUtils.mode(data);
        assertThat(data).isEqualTo(original);
    }

    // ==================== Spearman Correlation Tests ====================

    @Test
    void spearmanCorrelationOfPerfectMonotonicIncreasing() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 6.0, 8.0, 10.0}; // Perfect linear
        assertThat(StatUtils.spearmanCorrelation(x, y)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void spearmanCorrelationOfPerfectMonotonicDecreasing() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {10.0, 8.0, 6.0, 4.0, 2.0};
        assertThat(StatUtils.spearmanCorrelation(x, y)).isCloseTo(-1.0, within(TOLERANCE));
    }

    @Test
    void spearmanCorrelationOfNonLinearMonotonic() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {1.0, 4.0, 9.0, 16.0, 25.0}; // y = x^2, monotonic increasing
        // Spearman should be 1.0 (perfect rank correlation)
        assertThat(StatUtils.spearmanCorrelation(x, y)).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void spearmanCorrelationDifferentFromPearson() {
        // Non-linear relationship where Spearman and Pearson differ
        double[] x = {1.0, 2.0, 3.0, 4.0, 100.0}; // Outlier
        double[] y = {1.0, 2.0, 3.0, 4.0, 5.0};

        double pearson = StatUtils.correlationCoefficient(x, y);
        double spearman = StatUtils.spearmanCorrelation(x, y);

        // Spearman should be less affected by the outlier
        assertThat(Math.abs(pearson)).isLessThan(Math.abs(spearman));
    }

    @Test
    void spearmanCorrelationWithTiedRanks() {
        double[] x = {1.0, 2.0, 2.0, 3.0, 4.0}; // Tied values
        double[] y = {1.0, 2.0, 3.0, 4.0, 5.0};
        double spearman = StatUtils.spearmanCorrelation(x, y);

        // Should handle ties with midrank method
        assertThat(spearman).isBetween(0.8, 1.0);
    }

    @Test
    void spearmanCorrelationRequiresAtLeastTwoElements() {
        assertThatThrownBy(() -> StatUtils.spearmanCorrelation(new double[]{1.0}, new double[]{2.0}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 data points");
    }

    @Test
    void spearmanCorrelationRejectsMismatchedArrays() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0};

        assertThatThrownBy(() -> StatUtils.spearmanCorrelation(x, y))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same length");
    }

    @Test
    void spearmanCorrelationBoundedByOne() {
        double[] x = {5.0, 1.0, 9.0, 3.0, 7.0};
        double[] y = {2.0, 8.0, 4.0, 6.0, 1.0};
        double spearman = StatUtils.spearmanCorrelation(x, y);
        assertThat(spearman).isBetween(-1.0, 1.0);
    }

    @Test
    void spearmanCorrelationOfNoCorrelation() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {3.0, 1.0, 4.0, 2.0, 5.0}; // Random order
        double spearman = StatUtils.spearmanCorrelation(x, y);
        // Should be closer to 0 than to ±1
        assertThat(Math.abs(spearman)).isLessThan(0.7);
    }

    @Test
    void spearmanCorrelationWithAllTiedRanks() {
        double[] x = {5.0, 5.0, 5.0, 5.0};
        double[] y = {1.0, 2.0, 3.0, 4.0};
        // Constant x means undefined correlation
        double spearman = StatUtils.spearmanCorrelation(x, y);
        assertThat(spearman).isNaN();
    }

    @Test
    void spearmanCorrelationWithNegativeValues() {
        double[] x = {-5.0, -3.0, -1.0, 1.0, 3.0, 5.0};
        double[] y = {-10.0, -6.0, -2.0, 2.0, 6.0, 10.0};
        assertThat(StatUtils.spearmanCorrelation(x, y)).isCloseTo(1.0, within(TOLERANCE));
    }

    // ==================== Helper Methods ====================

    /**
     * Parses a comma-separated string into a double array.
     * Used by parameterized tests.
     */
    private double[] parseDoubleArray(String str) {
        String[] parts = str.split(",");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i].trim());
        }
        return result;
    }
}
