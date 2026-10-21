package uk.co.ryanharrison.mathengine.distributions;

import uk.co.ryanharrison.mathengine.special.Beta;
import uk.co.ryanharrison.mathengine.special.Gamma;

import java.util.Objects;

/**
 * Immutable implementation of the Student's t {@link ContinuousProbabilityDistribution}.
 * <p>
 * The Student's t-distribution arises when estimating the mean of a normally distributed
 * population when the sample size is small and the population standard deviation is unknown.
 * It is characterized by three parameters:
 * </p>
 * <ul>
 *     <li><b>ν (nu)</b>: degrees of freedom, which determines the shape</li>
 *     <li><b>μ (mu)</b>: location parameter (optional, defaults to 0)</li>
 *     <li><b>σ (sigma)</b>: scale parameter (optional, defaults to 1)</li>
 * </ul>
 * <p>
 * The probability density function is:
 * <br>
 * f(x) = (Γ((ν+1)/2) / (sqrt(νπ) * Γ(ν/2))) * (1 + ((x-μ)/σ)²/ν)^(-(ν+1)/2) / σ
 * </p>
 * <p>
 * As ν → ∞, the t-distribution approaches the normal distribution.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Standard t-distribution with 10 degrees of freedom
 * StudentTDistribution standard = StudentTDistribution.of(10.0);
 *
 * // Custom t-distribution
 * StudentTDistribution custom = StudentTDistribution.builder()
 *     .degreesOfFreedom(5.0)
 *     .location(100.0)
 *     .scale(15.0)
 *     .build();
 *
 * // Calculate probability density
 * double pdf = custom.density(115.0);
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class StudentTDistribution implements ContinuousProbabilityDistribution {

    /**
     * The degrees of freedom (ν).
     */
    private final double degreesOfFreedom;

    /**
     * The location parameter (μ).
     */
    private final double location;

    /**
     * The scale parameter (σ).
     */
    private final double scale;

    /**
     * Pre-computed factor (ν+1)/2 for density calculations.
     */
    private final double halfDegreesPlus1;

    /**
     * Pre-computed logarithm of normalization factor.
     * Equals ln(Γ((ν+1)/2)) - ln(Γ(ν/2))
     */
    private final double logNormalizationFactor;

    /**
     * Private constructor to enforce factory method usage.
     *
     * @param degreesOfFreedom the degrees of freedom (ν)
     * @param location         the location parameter (μ)
     * @param scale            the scale parameter (σ)
     */
    private StudentTDistribution(double degreesOfFreedom, double location, double scale) {
        this.degreesOfFreedom = degreesOfFreedom;
        this.location = location;
        this.scale = scale;
        this.halfDegreesPlus1 = 0.5 * (degreesOfFreedom + 1.0);
        this.logNormalizationFactor = Gamma.gammaLn(halfDegreesPlus1)
                - Gamma.gammaLn(0.5 * degreesOfFreedom);
    }

    /**
     * Creates a standard Student's t-distribution with the specified degrees of freedom.
     * <p>
     * Location = 0, scale = 1.
     * </p>
     *
     * @param degreesOfFreedom the degrees of freedom (ν), must be positive
     * @return a standard t-distribution
     * @throws IllegalArgumentException if degrees of freedom is not positive
     */
    public static StudentTDistribution of(double degreesOfFreedom) {
        return of(degreesOfFreedom, 0.0, 1.0);
    }

    /**
     * Creates a Student's t-distribution with the specified parameters.
     *
     * @param degreesOfFreedom the degrees of freedom (ν), must be positive
     * @param location the location parameter (μ)
     * @param scale the scale parameter (σ), must be positive
     * @return a t-distribution with the given parameters
     * @throws IllegalArgumentException if degrees of freedom or scale are not positive
     */
    public static StudentTDistribution of(double degreesOfFreedom, double location, double scale) {
        if (degreesOfFreedom <= 0.0) {
            throw new IllegalArgumentException(
                    "Degrees of freedom must be positive, got: " + degreesOfFreedom);
        }
        if (scale <= 0.0) {
            throw new IllegalArgumentException("Scale must be positive, got: " + scale);
        }
        return new StudentTDistribution(degreesOfFreedom, location, scale);
    }

    /**
     * Creates a new builder for constructing a {@link StudentTDistribution}.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the degrees of freedom (ν) of this distribution.
     *
     * @return the degrees of freedom
     */
    public double getDegreesOfFreedom() {
        return degreesOfFreedom;
    }

    /**
     * Gets the location parameter (μ) of this distribution.
     *
     * @return the location parameter
     */
    public double getLocation() {
        return location;
    }

    /**
     * Gets the scale parameter (σ) of this distribution.
     *
     * @return the scale parameter
     */
    public double getScale() {
        return scale;
    }

    /**
     * Gets the mean of this distribution.
     * <p>
     * The mean is defined for ν > 1 and equals μ.
     * </p>
     *
     * @return the mean
     * @throws ArithmeticException if ν ≤ 1 (mean is undefined)
     */
    public double getMean() {
        if (degreesOfFreedom <= 1.0) {
            throw new ArithmeticException("Mean is undefined for ν ≤ 1");
        }
        return location;
    }

    /**
     * Gets the variance of this distribution.
     * <p>
     * The variance is defined for ν > 2 and equals σ² * ν/(ν-2).
     * </p>
     *
     * @return the variance
     * @throws ArithmeticException if ν ≤ 2 (variance is undefined)
     */
    public double getVariance() {
        if (degreesOfFreedom <= 2.0) {
            throw new ArithmeticException("Variance is undefined for ν ≤ 2");
        }
        return scale * scale * degreesOfFreedom / (degreesOfFreedom - 2.0);
    }

    @Override
    public double density(double x) {
        double z = (x - location) / scale;
        double zSquared = z * z;

        // f(x) = exp(-((ν+1)/2) * ln(1 + z²/ν) + logNormFactor) / (sqrt(νπ) * σ)
        return Math.exp(-halfDegreesPlus1 * Math.log(1.0 + zSquared / degreesOfFreedom)
                + logNormalizationFactor)
                / (Math.sqrt(Math.PI * degreesOfFreedom) * scale);
    }

    /**
     * {@inheritDoc}
     * <p>
     * For the standardised value z = (x - μ)/σ, the tail beyond |z| is
     * ½ I_(ν/(ν+z²))(ν/2, ½), from the regularized incomplete beta function.
     * </p>
     */
    @Override
    public double cumulative(double x) {
        double z = (x - location) / scale;
        if (Double.isInfinite(z)) {
            return z > 0 ? 1.0 : 0.0;
        }
        double tail = 0.5 * Beta.regularizedIncomplete(
                degreesOfFreedom / (degreesOfFreedom + z * z), 0.5 * degreesOfFreedom, 0.5);
        return z > 0 ? 1.0 - tail : tail;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Found by Newton's method on {@link #cumulative(double)} from the normal quantile,
     * falling back to bisection within a bracket whenever a step would leave it, which the
     * heavy tails of a few degrees of freedom can otherwise cause.
     * </p>
     */
    @Override
    public double inverseCumulative(double p) {
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("Probability must be in (0, 1), got: " + p);
        }
        if (p > 0.5) {
            return 2.0 * location - inverseCumulative(1.0 - p);
        }

        // Work on the standardised variable, in the lower tail, where z <= 0
        StudentTDistribution standard = location == 0.0 && scale == 1.0 ? this : of(degreesOfFreedom);
        double lower = -1.0;
        while (standard.cumulative(lower) > p) {
            lower *= 2.0;
        }
        double upper = 0.0;
        double z = Math.max(lower, Math.min(upper, NormalDistribution.standardQuantile(p)));

        for (int i = 0; i < 200; i++) {
            double error = standard.cumulative(z) - p;
            if (error > 0) {
                upper = z;
            } else {
                lower = z;
            }
            double next = z - error / standard.density(z);
            if (!(next > lower && next < upper)) {
                next = 0.5 * (lower + upper);
            }
            if (Math.abs(next - z) <= 1e-15 * Math.max(1.0, Math.abs(z))) {
                z = next;
                break;
            }
            z = next;
        }
        return location + scale * z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentTDistribution)) return false;
        StudentTDistribution that = (StudentTDistribution) o;
        return Double.compare(that.degreesOfFreedom, degreesOfFreedom) == 0 &&
                Double.compare(that.location, location) == 0 &&
                Double.compare(that.scale, scale) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(degreesOfFreedom, location, scale);
    }

    @Override
    public String toString() {
        return String.format("StudentTDistribution(ν=%.4f, μ=%.4f, σ=%.4f)",
                degreesOfFreedom, location, scale);
    }

    /**
     * Builder for creating {@link StudentTDistribution} instances.
     * <p>
     * Provides a fluent API for constructing distributions with named parameters.
     * Defaults to standard t-distribution (location=0, scale=1).
     * </p>
     */
    public static final class Builder {
        private double degreesOfFreedom;
        private double location = 0.0;
        private double scale = 1.0;
        private boolean degreesSet = false;

        private Builder() {
        }

        /**
         * Sets the degrees of freedom (ν) of the distribution.
         *
         * @param degreesOfFreedom the degrees of freedom, must be positive
         * @return this builder
         * @throws IllegalArgumentException if degrees of freedom is not positive
         */
        public Builder degreesOfFreedom(double degreesOfFreedom) {
            if (degreesOfFreedom <= 0.0) {
                throw new IllegalArgumentException(
                        "Degrees of freedom must be positive, got: " + degreesOfFreedom);
            }
            this.degreesOfFreedom = degreesOfFreedom;
            this.degreesSet = true;
            return this;
        }

        /**
         * Sets the location parameter (μ) of the distribution.
         *
         * @param location the location parameter
         * @return this builder
         */
        public Builder location(double location) {
            this.location = location;
            return this;
        }

        /**
         * Sets the scale parameter (σ) of the distribution.
         *
         * @param scale the scale parameter, must be positive
         * @return this builder
         * @throws IllegalArgumentException if scale is not positive
         */
        public Builder scale(double scale) {
            if (scale <= 0.0) {
                throw new IllegalArgumentException("Scale must be positive, got: " + scale);
            }
            this.scale = scale;
            return this;
        }

        /**
         * Builds the {@link StudentTDistribution} instance.
         *
         * @return a new immutable Student's t-distribution
         * @throws IllegalStateException if degrees of freedom has not been set
         */
        public StudentTDistribution build() {
            if (!degreesSet) {
                throw new IllegalStateException("Degrees of freedom must be set");
            }
            return new StudentTDistribution(degreesOfFreedom, location, scale);
        }
    }
}
