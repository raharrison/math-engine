package uk.co.ryanharrison.mathengine.distributions;

import uk.co.ryanharrison.mathengine.special.Erf;

import java.util.Objects;

/**
 * Immutable implementation of the Normal (Gaussian) {@link ContinuousProbabilityDistribution}.
 * <p>
 * The normal distribution is a continuous probability distribution characterized by
 * its bell-shaped curve. It is defined by two parameters:
 * </p>
 * <ul>
 *     <li><b>μ (mu)</b>: the mean, which determines the center of the distribution</li>
 *     <li><b>σ (sigma)</b>: the standard deviation, which determines the spread</li>
 * </ul>
 * <p>
 * The probability density function is:
 * <br>
 * f(x) = (1 / (σ√(2π))) * exp(-((x-μ)² / (2σ²)))
 * </p>
 * <p>
 * The standard normal distribution has μ = 0 and σ = 1.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Standard normal distribution (mean=0, stddev=1)
 * NormalDistribution standard = NormalDistribution.standard();
 *
 * // Custom distribution using builder
 * NormalDistribution custom = NormalDistribution.builder()
 *     .mean(100)
 *     .standardDeviation(15)
 *     .build();
 *
 * // Calculate probability density
 * double pdf = custom.density(115);
 *
 * // Calculate cumulative probability
 * double cdf = custom.cumulative(115);
 *
 * // Find quantile (inverse CDF)
 * double quantile = custom.inverseCumulative(0.95);
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class NormalDistribution implements ContinuousProbabilityDistribution {

    /**
     * The square root of one half (1/√2).
     */
    private static final double SQRT_ONE_HALF = 0.707106781186547524;

    /**
     * One over the square root of 2π (1/√(2π)).
     */
    private static final double ONE_OVER_SQRT_2PI = 0.398942280401432678;

    /**
     * The mean (μ) of the distribution.
     */
    private final double mean;

    /**
     * The standard deviation (σ) of the distribution.
     */
    private final double standardDeviation;

    /**
     * Private constructor to enforce builder usage for complex configurations.
     * Use {@link #standard()} for the standard normal distribution.
     *
     * @param mean the mean (μ) of the distribution
     * @param standardDeviation the standard deviation (σ) of the distribution
     */
    private NormalDistribution(double mean, double standardDeviation) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /**
     * Creates a standard normal distribution with mean = 0 and standard deviation = 1.
     *
     * @return a standard normal distribution
     */
    public static NormalDistribution standard() {
        return new NormalDistribution(0.0, 1.0);
    }

    /**
     * Creates a normal distribution with the specified mean and standard deviation.
     *
     * @param mean              the mean (μ) of the distribution
     * @param standardDeviation the standard deviation (σ) of the distribution, must be positive
     * @return a normal distribution with the given parameters
     * @throws IllegalArgumentException if standard deviation is not positive
     */
    public static NormalDistribution of(double mean, double standardDeviation) {
        if (standardDeviation <= 0.0) {
            throw new IllegalArgumentException("Standard deviation must be positive, got: " + standardDeviation);
        }
        return new NormalDistribution(mean, standardDeviation);
    }

    /**
     * Creates a new builder for constructing a {@link NormalDistribution}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the mean (μ) of this distribution.
     *
     * @return the mean
     */
    public double getMean() {
        return mean;
    }

    /**
     * Gets the standard deviation (σ) of this distribution.
     *
     * @return the standard deviation
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Gets the variance (σ²) of this distribution.
     *
     * @return the variance
     */
    public double getVariance() {
        return standardDeviation * standardDeviation;
    }

    @Override
    public double density(double x) {
        double z = (x - mean) / standardDeviation;
        return ONE_OVER_SQRT_2PI / standardDeviation * Math.exp(-0.5 * z * z);
    }

    @Override
    public double cumulative(double x) {
        return 0.5 * Erf.erfc(-SQRT_ONE_HALF * (x - mean) / standardDeviation);
    }

    @Override
    public double inverseCumulative(double p) {
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("Probability must be in (0, 1), got: " + p);
        }

        // Transform the standard quantile to this distribution: x = μ + σz
        return mean + standardDeviation * standardQuantile(p);
    }

    /**
     * The quantile of the standard normal distribution, to near machine precision.
     * <p>
     * The rational approximation below starts within 4.5 × 10⁻⁴, and Newton's method on
     * the cumulative distribution function, which converges quadratically, finishes it in a
     * few steps. The upper half is found by symmetry, so the iteration always works in the
     * lower tail, where the cumulative probability keeps its relative accuracy.
     * </p>
     *
     * @param p the probability, in (0, 1)
     * @return z such that Φ(z) = p
     */
    static double standardQuantile(double p) {
        if (p > 0.5) {
            return -standardQuantile(1.0 - p);
        }
        double z = approximateStandardNormalQuantile(p);
        for (int i = 0; i < 50; i++) {
            double error = 0.5 * Erf.erfc(-SQRT_ONE_HALF * z) - p;
            double step = error / (ONE_OVER_SQRT_2PI * Math.exp(-0.5 * z * z));
            z -= step;
            if (Math.abs(step) <= 1e-15 * Math.max(1.0, Math.abs(z))) {
                break;
            }
        }
        return z;
    }

    /**
     * Approximates the quantile function of the standard normal distribution
     * using the Abramowitz and Stegun formula 26.2.23, as the starting point for
     * {@link #standardQuantile(double)}.
     * <p>
     * The absolute error is less than 4.5 × 10⁻⁴.
     * </p>
     *
     * @param p the probability
     * @return the approximate quantile of the standard normal distribution
     * @see <a href="http://www.johndcook.com/csharp_phi_inverse.html">
     * John D. Cook - Normal Quantile Function</a>
     */
    private static double approximateStandardNormalQuantile(double p) {
        if (p < 0.5) {
            // For p < 0.5: Φ⁻¹(p) = -G⁻¹(p)
            double t = Math.sqrt(-2.0 * Math.log(p));
            return -rationalApproximation(t);
        } else {
            // For p ≥ 0.5: Φ⁻¹(p) = G⁻¹(1-p)
            double t = Math.sqrt(-2.0 * Math.log(1.0 - p));
            return rationalApproximation(t);
        }
    }

    /**
     * Rational approximation helper for the quantile function.
     * Uses the Abramowitz and Stegun formula.
     *
     * @param t the transformed value
     * @return the approximated value
     */
    private static double rationalApproximation(double t) {
        // Abramowitz and Stegun formula 26.2.23
        double[] c = {2.515517, 0.802853, 0.010328};
        double[] d = {1.432788, 0.189269, 0.001308};

        double numerator = c[0] + t * (c[1] + t * c[2]);
        double denominator = 1.0 + t * (d[0] + t * (d[1] + t * d[2]));

        return t - numerator / denominator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NormalDistribution that)) return false;
        return Double.compare(that.mean, mean) == 0 &&
                Double.compare(that.standardDeviation, standardDeviation) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mean, standardDeviation);
    }

    @Override
    public String toString() {
        return String.format("NormalDistribution(μ=%.4f, σ=%.4f)", mean, standardDeviation);
    }

    /**
     * Builder for creating {@link NormalDistribution} instances.
     * <p>
     * Provides a fluent API for constructing distributions with named parameters.
     * Defaults to standard normal distribution (mean=0, stddev=1) if not specified.
     * </p>
     */
    public static final class Builder {
        private double mean = 0.0;
        private double standardDeviation = 1.0;

        private Builder() {
        }

        /**
         * Sets the mean (μ) of the distribution.
         *
         * @param mean the mean
         * @return this builder
         */
        public Builder mean(double mean) {
            this.mean = mean;
            return this;
        }

        /**
         * Sets the standard deviation (σ) of the distribution.
         *
         * @param standardDeviation the standard deviation, must be positive
         * @return this builder
         * @throws IllegalArgumentException if standard deviation is not positive
         */
        public Builder standardDeviation(double standardDeviation) {
            if (standardDeviation <= 0.0) {
                throw new IllegalArgumentException(
                        "Standard deviation must be positive, got: " + standardDeviation);
            }
            this.standardDeviation = standardDeviation;
            return this;
        }

        /**
         * Sets the variance (σ²) of the distribution.
         *
         * @param variance the variance, must be positive
         * @return this builder
         * @throws IllegalArgumentException if variance is not positive
         */
        public Builder variance(double variance) {
            if (variance <= 0.0) {
                throw new IllegalArgumentException("Variance must be positive, got: " + variance);
            }
            this.standardDeviation = Math.sqrt(variance);
            return this;
        }

        /**
         * Builds the {@link NormalDistribution} instance.
         *
         * @return a new immutable normal distribution
         */
        public NormalDistribution build() {
            return new NormalDistribution(mean, standardDeviation);
        }
    }
}
