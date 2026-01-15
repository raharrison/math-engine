package uk.co.ryanharrison.mathengine.utils;

import java.util.Arrays;

/**
 * Comprehensive collection of statistical utility functions for data analysis.
 * <p>
 * This class provides implementations of various statistical measures including:
 * </p>
 * <ul>
 *     <li>Central tendency measures (mean, median, mode)</li>
 *     <li>Dispersion measures (variance, standard deviation, range)</li>
 *     <li>Distribution shape measures (skewness, kurtosis)</li>
 *     <li>Correlation and covariance</li>
 *     <li>Percentiles and quartiles</li>
 *     <li>Specialized means (geometric, harmonic, weighted, truncated)</li>
 * </ul>
 * <p>
 * All methods are static and the class cannot be instantiated. Input arrays are never modified.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * double[] data = {1.5, 2.3, 3.7, 4.2, 5.8};
 *
 * // Central tendency
 * double avg = StatUtils.mean(data);       // 3.5
 * double mid = StatUtils.median(data);     // 3.7
 *
 * // Dispersion
 * double std = StatUtils.standardDeviation(data);
 * double var = StatUtils.variance(data);
 * double rng = StatUtils.range(data);      // 4.3
 *
 * // Distribution shape
 * double skew = StatUtils.skewness(data);
 * double kurt = StatUtils.kurtosis(data);
 *
 * // Correlation
 * double[] x = {1, 2, 3, 4, 5};
 * double[] y = {2, 4, 6, 8, 10};
 * double corr = StatUtils.correlationCoefficient(x, y);  // 1.0 (perfect correlation)
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class StatUtils {

    /**
     * Not permitted to create an instance of this class.
     */
    private StatUtils() {
    }

    // ==================== Central Tendency Measures ====================

    /**
     * Calculates the arithmetic mean (average) of a data set.
     * <p>
     * The mean is defined as: mean = (Σ x_i) / n
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the arithmetic mean of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double mean(double[] data) {
        validateData(data);
        return sum(data) / data.length;
    }

    /**
     * Calculates the median (middle value) of a data set.
     * <p>
     * The median is the 50th percentile - the value that separates the higher half from the lower half.
     * For even-sized data sets, it's the average of the two middle values.
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the median of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double median(double[] data) {
        validateData(data);
        return percentile(data, 0.50);
    }

    /**
     * Calculates the geometric mean of a data set.
     * <p>
     * The geometric mean is defined as: GM = (x₁ × x₂ × ... × xₙ)^(1/n)
     * <br>
     * This is useful for averaging ratios, growth rates, or values on different scales.
     * </p>
     * <p>
     * Note: All values must be positive for a real result.
     * This implementation uses logarithms to avoid overflow for large products.
     * </p>
     *
     * @param data the data set, must not be null or empty, all values must be positive
     * @return the geometric mean of the data set
     * @throws IllegalArgumentException if data is null, empty, or contains non-positive values
     */
    public static double geometricMean(double[] data) {
        validateData(data);

        // Use log to avoid overflow: GM = exp((1/n) × Σ ln(x_i))
        double logSum = 0.0;
        for (double value : data) {
            if (value <= 0.0) {
                throw new IllegalArgumentException("Geometric mean requires positive values, got: " + value);
            }
            logSum += Math.log(value);
        }

        return Math.exp(logSum / data.length);
    }

    /**
     * Calculates the harmonic mean of a data set.
     * <p>
     * The harmonic mean is defined as: HM = n / (Σ 1/x_i)
     * <br>
     * This is useful for averaging rates (e.g., speeds, frequencies).
     * </p>
     * <p>
     * Note: All values must be non-zero. Negative values are allowed but may produce unexpected results.
     * </p>
     *
     * @param data the data set, must not be null or empty, values should be non-zero
     * @return the harmonic mean of the data set
     * @throws IllegalArgumentException if data is null or empty
     * @throws ArithmeticException if any value is zero
     */
    public static double harmonicMean(double[] data) {
        validateData(data);

        double sum = 0.0;
        for (double value : data) {
            if (value == 0.0) {
                throw new ArithmeticException("Harmonic mean is undefined when data contains zero");
            }
            sum += 1.0 / value;
        }

        return data.length / sum;
    }

    /**
     * Calculates the weighted mean of a data set.
     * <p>
     * The weighted mean is defined as: WM = (Σ w_i × x_i) / (Σ w_i)
     * <br>
     * Each data point is given a weight - larger weights have more impact on the result.
     * </p>
     *
     * @param data    the data set, must not be null or empty
     * @param weights the weights for each data point, must have the same length as data
     * @return the weighted mean of the data set
     * @throws IllegalArgumentException if data or weights are null, empty, or have different lengths
     */
    public static double weightedMean(double[] data, double[] weights) {
        validateData(data);
        if (weights == null || weights.length == 0) {
            throw new IllegalArgumentException("Weights array cannot be null or empty");
        }
        if (data.length != weights.length) {
            throw new IllegalArgumentException(
                    "Data and weights must have the same length. Data: " + data.length + ", Weights: " + weights.length);
        }

        double numerator = 0.0;
        double denominator = 0.0;

        for (int i = 0; i < data.length; i++) {
            numerator += data[i] * weights[i];
            denominator += weights[i];
        }

        return numerator / denominator;
    }

    /**
     * Calculates the truncated mean (trimmed mean) of a data set.
     * <p>
     * The truncated mean removes a percentage of outliers from both ends before calculating the mean.
     * This makes it more robust to outliers than the standard mean.
     * </p>
     * <p>
     * For example, a 10% truncated mean removes the lowest 5% and highest 5% of values.
     * </p>
     *
     * @param data            the data set, must not be null or empty
     * @param truncPercentage the percentage of values to remove (0-100), split equally between both ends
     * @return the truncated mean of the data set
     * @throws IllegalArgumentException if data is null or empty, or if truncPercentage is not in [0, 100]
     */
    public static double truncatedMean(double[] data, double truncPercentage) {
        validateData(data);
        if (truncPercentage < 0 || truncPercentage > 100) {
            throw new IllegalArgumentException(
                    "Truncation percentage must be between 0 and 100, got: " + truncPercentage);
        }

        // Calculate number of values to remove from each end
        int numRemove = (int) Math.floor(truncPercentage / 100.0 * data.length / 2.0);

        // If we would remove all data, return regular mean
        if (numRemove * 2 >= data.length) {
            return mean(data);
        }

        // Sort the data (on a copy to avoid modifying original)
        double[] sortedData = data.clone();
        Arrays.sort(sortedData);

        // Calculate mean of remaining values
        double sum = 0.0;
        int count = 0;

        for (int i = numRemove; i < sortedData.length - numRemove; i++) {
            sum += sortedData[i];
            count++;
        }

        return sum / count;
    }

    // ==================== Dispersion Measures ====================

    /**
     * Calculates the sample variance of a data set.
     * <p>
     * The sample variance is defined as: s² = Σ(x_i - mean)² / (n - 1)
     * <br>
     * Uses Bessel's correction (n-1) for unbiased estimation of population variance.
     * </p>
     *
     * @param data the data set, must not be null and must have at least 2 elements
     * @return the sample variance of the data set
     * @throws IllegalArgumentException if data is null, empty, or has only one element
     */
    public static double variance(double[] data) {
        validateData(data);
        if (data.length < 2) {
            throw new IllegalArgumentException("Variance requires at least 2 data points, got: " + data.length);
        }

        double mean = mean(data);
        double sumOfSquaredDeviations = 0.0;

        for (double value : data) {
            double deviation = value - mean;
            sumOfSquaredDeviations += deviation * deviation;
        }

        return sumOfSquaredDeviations / (data.length - 1);
    }

    /**
     * Calculates the sample standard deviation of a data set.
     * <p>
     * The standard deviation is the square root of the variance: σ = √variance
     * <br>
     * It measures the spread of data around the mean in the same units as the data.
     * </p>
     *
     * @param data the data set, must not be null and must have at least 2 elements
     * @return the sample standard deviation of the data set
     * @throws IllegalArgumentException if data is null, empty, or has only one element
     */
    public static double standardDeviation(double[] data) {
        return Math.sqrt(variance(data));
    }

    /**
     * Calculates the population variance of a data set.
     * <p>
     * The population variance is defined as: σ² = Σ(x_i - mean)² / n
     * <br>
     * Use this when the data represents the entire population (not a sample).
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the population variance of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double populationVariance(double[] data) {
        validateData(data);

        double mean = mean(data);
        double sumOfSquaredDeviations = 0.0;

        for (double value : data) {
            double deviation = value - mean;
            sumOfSquaredDeviations += deviation * deviation;
        }

        return sumOfSquaredDeviations / data.length;
    }

    /**
     * Calculates the population standard deviation of a data set.
     * <p>
     * The population standard deviation is the square root of the population variance.
     * Use this when the data represents the entire population (not a sample).
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the population standard deviation of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double populationStandardDeviation(double[] data) {
        return Math.sqrt(populationVariance(data));
    }

    /**
     * Calculates the range of a data set.
     * <p>
     * The range is defined as: range = max - min
     * <br>
     * It represents the spread between the largest and smallest values.
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the range of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double range(double[] data) {
        validateData(data);
        return max(data) - min(data);
    }

    /**
     * Calculates the interquartile range (IQR) of a data set.
     * <p>
     * The IQR is defined as: IQR = Q₃ - Q₁ (75th percentile - 25th percentile)
     * <br>
     * It represents the range of the middle 50% of the data, making it robust to outliers.
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the interquartile range of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double interQuartileRange(double[] data) {
        validateData(data);
        return percentile(data, 0.75) - percentile(data, 0.25);
    }

    // ==================== Distribution Shape Measures ====================

    /**
     * Calculates the sample skewness of a data set.
     * <p>
     * Skewness measures the asymmetry of the distribution:
     * </p>
     * <ul>
     *     <li>Skewness = 0: Symmetric distribution</li>
     *     <li>Skewness > 0: Right-skewed (tail extends to the right)</li>
     *     <li>Skewness < 0: Left-skewed (tail extends to the left)</li>
     * </ul>
     * <p>
     * Formula: skew = (1/n) × Σ((x_i - mean)/σ)³
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the sample skewness of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double skewness(double[] data) {
        validateData(data);

        double mean = mean(data);
        double numerator = 0.0;
        double denominator = 0.0;

        for (double value : data) {
            double deviation = value - mean;
            numerator += Math.pow(deviation, 3);
            denominator += Math.pow(deviation, 2);
        }

        numerator /= data.length;
        denominator = Math.pow(denominator / data.length, 1.5);

        return numerator / denominator;
    }

    /**
     * Calculates the population skewness with bias correction (adjusted Fisher-Pearson).
     * <p>
     * This applies a bias correction factor for small sample sizes:
     * <br>
     * Corrected skewness = √(n(n-1)) / (n-2) × skewness
     * </p>
     *
     * @param data the data set, must not be null and must have at least 3 elements
     * @return the population skewness with bias correction
     * @throws IllegalArgumentException if data is null, empty, or has fewer than 3 elements
     */
    public static double populationSkewness(double[] data) {
        validateData(data);
        if (data.length < 3) {
            throw new IllegalArgumentException("Population skewness requires at least 3 data points, got: " + data.length);
        }

        double mean = mean(data);
        double standardDeviation = standardDeviation(data);
        double n = data.length;

        double multiplier = n / ((n - 1) * (n - 2));
        double sum = 0.0;

        for (double value : data) {
            sum += Math.pow((value - mean) / standardDeviation, 3);
        }

        return multiplier * sum;
    }

    /**
     * Calculates the sample excess kurtosis of a data set.
     * <p>
     * Kurtosis measures the "tailedness" of the distribution. Excess kurtosis is relative to a normal distribution:
     * </p>
     * <ul>
     *     <li>Excess kurtosis = 0: Normal distribution (mesokurtic)</li>
     *     <li>Excess kurtosis > 0: Heavy tails, more outliers (leptokurtic)</li>
     *     <li>Excess kurtosis < 0: Light tails, fewer outliers (platykurtic)</li>
     * </ul>
     * <p>
     * Formula: kurtosis = [(1/n) × Σ((x_i - mean)/σ)⁴] - 3
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the sample excess kurtosis of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double kurtosis(double[] data) {
        validateData(data);

        double mean = mean(data);
        double numerator = 0.0;
        double denominator = 0.0;

        for (double value : data) {
            double deviation = value - mean;
            numerator += Math.pow(deviation, 4);
            denominator += Math.pow(deviation, 2);
        }

        numerator /= data.length;
        denominator = Math.pow(denominator / data.length, 2);

        return (numerator / denominator) - 3.0; // Subtract 3 for excess kurtosis
    }

    /**
     * Calculates the population excess kurtosis with bias correction.
     * <p>
     * This applies a bias correction factor for small sample sizes using the formula:
     * <br>
     * Corrected kurtosis = [n(n+1)/((n-1)(n-2)(n-3))] × Σ((x_i - mean)/σ)⁴ - 3(n-1)²/((n-2)(n-3))
     * </p>
     *
     * @param data the data set, must not be null and must have at least 4 elements
     * @return the population excess kurtosis with bias correction
     * @throws IllegalArgumentException if data is null, empty, or has fewer than 4 elements
     */
    public static double populationKurtosis(double[] data) {
        validateData(data);
        if (data.length < 4) {
            throw new IllegalArgumentException("Population kurtosis requires at least 4 data points, got: " + data.length);
        }

        double mean = mean(data);
        double standardDeviation = standardDeviation(data);
        double n = data.length;

        double sum = 0.0;
        double multiplier = n * (n + 1) / ((n - 1) * (n - 2) * (n - 3));
        double subtractor = 3 * Math.pow(n - 1, 2) / ((n - 2) * (n - 3));

        for (double value : data) {
            sum += Math.pow((value - mean) / standardDeviation, 4);
        }

        return multiplier * sum - subtractor;
    }

    // ==================== Correlation and Covariance ====================

    /**
     * Calculates the sample covariance between two data sets.
     * <p>
     * Covariance measures how two variables change together:
     * <br>
     * cov(X,Y) = Σ((x_i - mean_x)(y_i - mean_y)) / (n - 1)
     * </p>
     * <ul>
     *     <li>Positive covariance: Variables tend to increase together</li>
     *     <li>Negative covariance: One increases when the other decreases</li>
     *     <li>Near-zero covariance: Little linear relationship</li>
     * </ul>
     *
     * @param x the first data set, must not be null and must have at least 2 elements
     * @param y the second data set, must have the same length as x
     * @return the sample covariance between x and y
     * @throws IllegalArgumentException if arrays are null, have different lengths, or have fewer than 2 elements
     */
    public static double covariance(double[] x, double[] y) {
        validatePairedData(x, y);
        if (x.length < 2) {
            throw new IllegalArgumentException("Covariance requires at least 2 data points, got: " + x.length);
        }

        double xMean = mean(x);
        double yMean = mean(y);
        double result = 0.0;

        for (int i = 0; i < x.length; i++) {
            result += (x[i] - xMean) * (y[i] - yMean);
        }

        return result / (x.length - 1);
    }

    /**
     * Calculates the population covariance between two data sets.
     * <p>
     * Population covariance uses n instead of (n-1) in the denominator.
     * Use this when the data represents the entire population.
     * </p>
     *
     * @param x the first data set, must not be null or empty
     * @param y the second data set, must have the same length as x
     * @return the population covariance between x and y
     * @throws IllegalArgumentException if arrays are null, empty, or have different lengths
     */
    public static double populationCovariance(double[] x, double[] y) {
        validatePairedData(x, y);

        double xMean = mean(x);
        double yMean = mean(y);
        double result = 0.0;

        for (int i = 0; i < x.length; i++) {
            result += (x[i] - xMean) * (y[i] - yMean);
        }

        return result / x.length;
    }

    /**
     * Calculates the sample Pearson correlation coefficient between two data sets.
     * <p>
     * The sample correlation coefficient measures the strength and direction of the linear relationship:
     * <br>
     * r = sample_cov(X,Y) / (sample_σ_x × sample_σ_y)
     * <br>
     * Uses Bessel's correction (n-1) in covariance and standard deviation calculations.
     * </p>
     * <ul>
     *     <li>r = 1: Perfect positive correlation</li>
     *     <li>r = 0: No linear correlation</li>
     *     <li>r = -1: Perfect negative correlation</li>
     * </ul>
     *
     * @param x the first data set, must not be null and must have at least 2 elements
     * @param y the second data set, must have the same length as x
     * @return the sample correlation coefficient between x and y, in the range [-1, 1]
     * @throws IllegalArgumentException if arrays are null, have different lengths, or have fewer than 2 elements
     */
    public static double correlationCoefficient(double[] x, double[] y) {
        validatePairedData(x, y);
        if (x.length < 2) {
            throw new IllegalArgumentException("Correlation requires at least 2 data points, got: " + x.length);
        }

        double xStdDev = standardDeviation(x);
        double yStdDev = standardDeviation(y);
        double cov = covariance(x, y);

        return cov / (xStdDev * yStdDev);
    }

    /**
     * Calculates the population Pearson correlation coefficient between two data sets.
     * <p>
     * The population correlation coefficient uses population statistics:
     * <br>
     * ρ = population_cov(X,Y) / (population_σ_x × population_σ_y)
     * <br>
     * Use this when the data represents the entire population (not a sample).
     * </p>
     *
     * @param x the first data set, must not be null or empty
     * @param y the second data set, must have the same length as x
     * @return the population correlation coefficient between x and y, in the range [-1, 1]
     * @throws IllegalArgumentException if arrays are null, empty, or have different lengths
     */
    public static double populationCorrelationCoefficient(double[] x, double[] y) {
        validatePairedData(x, y);

        double xStdDev = populationStandardDeviation(x);
        double yStdDev = populationStandardDeviation(y);
        double cov = populationCovariance(x, y);

        return cov / (xStdDev * yStdDev);
    }

    // ==================== Percentiles and Order Statistics ====================

    /**
     * Calculates the specified percentile of a data set using linear interpolation.
     * <p>
     * The percentile is the value below which a given percentage of observations fall.
     * For example:
     * </p>
     * <ul>
     *     <li>0.25 (25th percentile) = First quartile (Q₁)</li>
     *     <li>0.50 (50th percentile) = Median (Q₂)</li>
     *     <li>0.75 (75th percentile) = Third quartile (Q₃)</li>
     * </ul>
     * <p>
     * This method uses linear interpolation between data points when the percentile
     * falls between two values.
     * </p>
     *
     * @param data       the data set, must not be null or empty
     * @param percentile the desired percentile, must be in the range [0, 1]
     * @return the value at the specified percentile
     * @throws IllegalArgumentException if data is null, empty, or percentile is not in [0, 1]
     */
    public static double percentile(double[] data, double percentile) {
        validateData(data);
        if (percentile < 0.0 || percentile > 1.0) {
            throw new IllegalArgumentException(
                    "Percentile must be between 0 and 1 (inclusive), got: " + percentile);
        }

        // Sort data (on a copy to avoid modifying original)
        double[] sortedData = data.clone();
        Arrays.sort(sortedData);

        // Calculate the position in the sorted array
        double position = (sortedData.length - 1) * percentile + 1;

        // Get the integer part and fractional part
        int lowerIndex = (int) Math.floor(position) - 1;
        double fraction = position % 1;

        // Handle edge cases
        if (lowerIndex < 0) {
            return sortedData[0];
        }
        if (lowerIndex >= sortedData.length - 1) {
            return sortedData[sortedData.length - 1];
        }

        // Linear interpolation
        if (fraction == 0.0) {
            return sortedData[lowerIndex];
        } else {
            double lowerValue = sortedData[lowerIndex];
            double upperValue = sortedData[lowerIndex + 1];
            return lowerValue + fraction * (upperValue - lowerValue);
        }
    }

    /**
     * Finds the minimum (smallest) value in a data set.
     *
     * @param data the data set, must not be null or empty
     * @return the minimum value in the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double min(double[] data) {
        validateData(data);

        double min = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] < min) {
                min = data[i];
            }
        }

        return min;
    }

    /**
     * Finds the maximum (largest) value in a data set.
     *
     * @param data the data set, must not be null or empty
     * @return the maximum value in the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double max(double[] data) {
        validateData(data);

        double max = data[0];
        for (int i = 1; i < data.length; i++) {
            if (data[i] > max) {
                max = data[i];
            }
        }

        return max;
    }

    // ==================== Summation Functions ====================

    /**
     * Calculates the sum of all values in a data set.
     * <p>
     * Sum = Σ x_i
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the sum of all values
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double sum(double[] data) {
        validateData(data);

        double sum = 0.0;
        for (double value : data) {
            sum += value;
        }

        return sum;
    }

    /**
     * Calculates the sum of squares of all values in a data set.
     * <p>
     * Sum of squares = Σ x_i²
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the sum of squares of all values
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double sumOfSquares(double[] data) {
        validateData(data);

        double sum = 0.0;
        for (double value : data) {
            sum += value * value;
        }

        return sum;
    }

    /**
     * Calculates the product of all values in a data set.
     * <p>
     * Product = Π x_i = x₁ × x₂ × ... × xₙ
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the product of all values
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double product(double[] data) {
        validateData(data);

        double product = 1.0;
        for (double value : data) {
            product *= value;
        }

        return product;
    }

    /**
     * Calculates the root mean square (RMS) of a data set.
     * <p>
     * RMS is defined as: RMS = √(Σ x_i² / n)
     * <br>
     * It represents the square root of the average of the squared values.
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the root mean square of the data set
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double rootmeanSquare(double[] data) {
        validateData(data);
        return Math.sqrt(sumOfSquares(data) / data.length);
    }

    // ==================== Additional Statistical Measures ====================

    /**
     * Calculates specific quartiles of a data set.
     *
     * @param data     the data set, must not be null or empty
     * @param quartile the quartile number (1, 2, or 3)
     * @return Q1 (25th percentile), Q2 (50th percentile/median), or Q3 (75th percentile)
     * @throws IllegalArgumentException if data is null, empty, or quartile is not 1, 2, or 3
     */
    public static double quartile(double[] data, int quartile) {
        if (quartile < 1 || quartile > 3) {
            throw new IllegalArgumentException("Quartile must be 1, 2, or 3, got: " + quartile);
        }
        return percentile(data, quartile * 0.25);
    }

    /**
     * Calculates the sample coefficient of variation (CV) of a data set.
     * <p>
     * CV is defined as: CV = (sample standard deviation / mean) × 100%
     * <br>
     * It represents the ratio of the sample standard deviation to the mean, expressed as a percentage.
     * Useful for comparing variability across datasets with different units or scales.
     * Uses Bessel's correction (n-1) in the standard deviation calculation.
     * </p>
     *
     * @param data the data set, must not be null and must have at least 2 elements
     * @return the sample coefficient of variation as a percentage
     * @throws IllegalArgumentException if data is null, empty, or has fewer than 2 elements
     * @throws ArithmeticException      if mean is zero
     */
    public static double coefficientOfVariation(double[] data) {
        validateData(data);
        if (data.length < 2) {
            throw new IllegalArgumentException("Coefficient of variation requires at least 2 data points, got: " + data.length);
        }

        double mean = mean(data);
        if (mean == 0.0) {
            throw new ArithmeticException("Coefficient of variation is undefined when mean is zero");
        }

        double stdDev = standardDeviation(data);
        return (stdDev / Math.abs(mean)) * 100.0;
    }

    /**
     * Calculates the population coefficient of variation (CV) of a data set.
     * <p>
     * CV is defined as: CV = (population standard deviation / mean) × 100%
     * <br>
     * Use this when the data represents the entire population (not a sample).
     * Divides by n instead of (n-1) in the standard deviation calculation.
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return the population coefficient of variation as a percentage
     * @throws IllegalArgumentException if data is null or empty
     * @throws ArithmeticException      if mean is zero
     */
    public static double populationCoefficientOfVariation(double[] data) {
        validateData(data);

        double mean = mean(data);
        if (mean == 0.0) {
            throw new ArithmeticException("Population coefficient of variation is undefined when mean is zero");
        }

        double stdDev = populationStandardDeviation(data);
        return (stdDev / Math.abs(mean)) * 100.0;
    }

    /**
     * Calculates the z-score (standard score) for a value within a data set.
     * <p>
     * Z-score is defined as: z = (x - μ) / σ
     * <br>
     * It represents how many standard deviations a value is from the mean.
     * </p>
     *
     * @param value the value to calculate the z-score for
     * @param data the data set, must not be null and must have at least 2 elements
     * @return the z-score of the value
     * @throws IllegalArgumentException if data is null, empty, or has fewer than 2 elements
     */
    public static double zScore(double value, double[] data) {
        validateData(data);
        if (data.length < 2) {
            throw new IllegalArgumentException("Z-score requires at least 2 data points, got: " + data.length);
        }

        double mean = mean(data);
        double stdDev = standardDeviation(data);

        if (stdDev == 0.0) {
            return 0.0; // All values are identical
        }

        return (value - mean) / stdDev;
    }

    /**
     * Normalizes a data set to z-scores (standard scores).
     * <p>
     * Each value is transformed to: z_i = (x_i - μ) / σ
     * <br>
     * The resulting data set has mean = 0 and standard deviation = 1.
     * </p>
     *
     * @param data the data set to normalize, must not be null and must have at least 2 elements
     * @return a new array with normalized z-scores
     * @throws IllegalArgumentException if data is null, empty, or has fewer than 2 elements
     */
    public static double[] normalize(double[] data) {
        validateData(data);
        if (data.length < 2) {
            throw new IllegalArgumentException("Normalization requires at least 2 data points, got: " + data.length);
        }

        double mean = mean(data);
        double stdDev = standardDeviation(data);

        if (stdDev == 0.0) {
            // All values are identical, return array of zeros
            return new double[data.length];
        }

        double[] normalized = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            normalized[i] = (data[i] - mean) / stdDev;
        }

        return normalized;
    }

    /**
     * Calculates the simple moving average with a specified window size.
     * <p>
     * For each position i (where i >= window - 1), the moving average is the mean of
     * the values from (i - window + 1) to i inclusive.
     * </p>
     *
     * @param data   the data set, must not be null or empty
     * @param window the window size, must be > 0 and <= data.length
     * @return an array of moving averages (length = data.length - window + 1)
     * @throws IllegalArgumentException if data is null, empty, or window is invalid
     */
    public static double[] movingAverage(double[] data, int window) {
        validateData(data);
        if (window <= 0 || window > data.length) {
            throw new IllegalArgumentException(
                    "Window must be between 1 and " + data.length + ", got: " + window);
        }

        double[] result = new double[data.length - window + 1];

        // Calculate first window
        double sum = 0.0;
        for (int i = 0; i < window; i++) {
            sum += data[i];
        }
        result[0] = sum / window;

        // Slide window
        for (int i = window; i < data.length; i++) {
            sum = sum - data[i - window] + data[i];
            result[i - window + 1] = sum / window;
        }

        return result;
    }

    /**
     * Calculates the exponential moving average with a specified smoothing factor.
     * <p>
     * EMA_t = α × value_t + (1 - α) × EMA_(t-1)
     * <br>
     * where α is the smoothing factor (typically 2/(window+1) for a given window size).
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @param alpha the smoothing factor, must be in (0, 1]
     * @return an array of exponential moving averages (same length as data)
     * @throws IllegalArgumentException if data is null, empty, or alpha is not in (0, 1]
     */
    public static double[] exponentialMovingAverage(double[] data, double alpha) {
        validateData(data);
        if (alpha <= 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("Alpha must be in (0, 1], got: " + alpha);
        }

        double[] result = new double[data.length];
        result[0] = data[0];

        for (int i = 1; i < data.length; i++) {
            result[i] = alpha * data[i] + (1.0 - alpha) * result[i - 1];
        }

        return result;
    }

    /**
     * Counts the frequency of each unique value in a data set.
     * <p>
     * Returns a 2D array where result[i][0] is a unique value and result[i][1] is its count.
     * Results are sorted by value in ascending order.
     * </p>
     *
     * @param data the data set, must not be null or empty
     * @return a 2D array of [value, count] pairs
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double[][] frequencyCount(double[] data) {
        validateData(data);

        // Sort to group identical values
        double[] sorted = data.clone();
        Arrays.sort(sorted);

        // Count unique values
        int uniqueCount = 1;
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] != sorted[i - 1]) {
                uniqueCount++;
            }
        }

        // Build frequency array
        double[][] frequencies = new double[uniqueCount][2];
        int index = 0;
        frequencies[0][0] = sorted[0];
        frequencies[0][1] = 1;

        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] == sorted[i - 1]) {
                frequencies[index][1]++;
            } else {
                index++;
                frequencies[index][0] = sorted[i];
                frequencies[index][1] = 1;
            }
        }

        return frequencies;
    }

    /**
     * Finds the mode(s) of a data set - the most frequently occurring value(s).
     *
     * @param data the data set, must not be null or empty
     * @return an array of mode values (may have multiple values if there's a tie)
     * @throws IllegalArgumentException if data is null or empty
     */
    public static double[] mode(double[] data) {
        validateData(data);

        double[][] frequencies = frequencyCount(data);

        // Find maximum frequency
        double maxFreq = 0.0;
        for (double[] freq : frequencies) {
            if (freq[1] > maxFreq) {
                maxFreq = freq[1];
            }
        }

        // Count how many values have max frequency
        int modeCount = 0;
        for (double[] freq : frequencies) {
            if (freq[1] == maxFreq) {
                modeCount++;
            }
        }

        // Collect mode values
        double[] modes = new double[modeCount];
        int index = 0;
        for (double[] freq : frequencies) {
            if (freq[1] == maxFreq) {
                modes[index++] = freq[0];
            }
        }

        return modes;
    }

    /**
     * Calculates the Spearman rank correlation coefficient between two data sets.
     * <p>
     * The Spearman correlation is a non-parametric measure of rank correlation.
     * It assesses how well the relationship between two variables can be described using a monotonic function.
     * </p>
     *
     * @param x the first data set, must not be null and must have at least 2 elements
     * @param y the second data set, must have the same length as x
     * @return the Spearman correlation coefficient, in the range [-1, 1]
     * @throws IllegalArgumentException if arrays are null, have different lengths, or have fewer than 2 elements
     */
    public static double spearmanCorrelation(double[] x, double[] y) {
        validatePairedData(x, y);
        if (x.length < 2) {
            throw new IllegalArgumentException("Spearman correlation requires at least 2 data points, got: " + x.length);
        }

        // Convert to ranks
        double[] xRanks = rank(x);
        double[] yRanks = rank(y);

        // Calculate Pearson correlation on ranks
        return correlationCoefficient(xRanks, yRanks);
    }

    /**
     * Converts data values to their ranks (1-based).
     * <p>
     * Equal values receive the average of their ranks (midrank).
     * </p>
     *
     * @param data the data set
     * @return an array of ranks
     */
    private static double[] rank(double[] data) {
        int n = data.length;
        double[] ranks = new double[n];

        // Create index array to track original positions
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }

        // Sort indices by data values
        Arrays.sort(indices, (i, j) -> Double.compare(data[i], data[j]));

        // Assign ranks (handling ties with midrank)
        int i = 0;
        while (i < n) {
            int j = i;
            // Find range of equal values
            while (j < n - 1 && data[indices[j]] == data[indices[j + 1]]) {
                j++;
            }

            // Calculate midrank for ties
            double midrank = (i + j + 2) / 2.0; // +2 for 1-based ranking

            // Assign midrank to all tied values
            for (int k = i; k <= j; k++) {
                ranks[indices[k]] = midrank;
            }

            i = j + 1;
        }

        return ranks;
    }

    // ==================== Validation Helper Methods ====================

    /**
     * Validates that a data array is not null and not empty.
     *
     * @param data the array to validate
     * @throws IllegalArgumentException if data is null or empty
     */
    private static void validateData(double[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data array cannot be null");
        }
        if (data.length == 0) {
            throw new IllegalArgumentException("Data array cannot be empty");
        }
    }

    /**
     * Validates that two data arrays are not null, not empty, and have the same length.
     *
     * @param x the first array to validate
     * @param y the second array to validate
     * @throws IllegalArgumentException if arrays are null, empty, or have different lengths
     */
    private static void validatePairedData(double[] x, double[] y) {
        validateData(x);
        validateData(y);
        if (x.length != y.length) {
            throw new IllegalArgumentException(
                    "Data arrays must have the same length. X: " + x.length + ", Y: " + y.length);
        }
    }
}
