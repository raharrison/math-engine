package uk.co.ryanharrison.mathengine.special;

/**
 * Utility class providing static methods for computing the Error function and its complement.
 * <p>
 * The <b>Error function</b> (erf) is a special function of sigmoid shape that occurs in probability,
 * statistics, and partial differential equations. It is defined as:
 * </p>
 * <pre>
 * erf(x) = (2/√π) ∫₀ˣ e^(-t²) dt
 * </pre>
 * <p>
 * The <b>Complementary error function</b> (erfc) is defined as:
 * </p>
 * <pre>
 * erfc(x) = 1 - erf(x) = (2/√π) ∫ₓ^∞ e^(-t²) dt
 * </pre>
 * <p>
 * <b>Properties:</b>
 * </p>
 * <ul>
 *     <li>erf(x) is an odd function: erf(-x) = -erf(x)</li>
 *     <li>erf(0) = 0</li>
 *     <li>erf(∞) = 1, erf(-∞) = -1</li>
 *     <li>Range of erf: [-1, 1]</li>
 *     <li>Range of erfc: [0, 2]</li>
 *     <li>erfc(x) + erf(x) = 1 for all x</li>
 * </ul>
 * <p>
 * <b>Implementation:</b> near zero, the series
 * erf(x) = (2/√π) e^(-x²) Σ 2ⁿ x^(2n+1) / (1·3·5···(2n+1)), whose terms are all positive and
 * so lose nothing to cancellation; further out, the continued fraction for erfc, which
 * converges quickly there and keeps its relative accuracy far into the tail. Both are
 * accurate to around 1e-15, which the normal distribution's probabilities depend on.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Calculate error function at x = 1
 * double erfValue = Erf.erf(1.0);  // ≈ 0.8427
 *
 * // Calculate complementary error function
 * double erfcValue = Erf.erfc(1.0);  // ≈ 0.1573
 *
 * // Verify the relationship: erf(x) + erfc(x) = 1
 * double x = 2.5;
 * double sum = Erf.erf(x) + Erf.erfc(x);  // = 1.0
 *
 * // Use symmetry property: erf(-x) = -erf(x)
 * double symmetric = Erf.erf(-1.0);  // ≈ -0.8427
 * }</pre>
 *
 * @author Ryan Harrison
 * @see <a href="https://en.wikipedia.org/wiki/Error_function">Error function - Wikipedia</a>
 */
public final class Erf {

    /**
     * Where the series hands over to the continued fraction. Below it the series needs few
     * terms; above it the continued fraction does.
     */
    private static final double SERIES_LIMIT = 2.5;

    /**
     * Relative size of the last term or correction at which a sum is taken as converged.
     */
    private static final double EPSILON = 1e-16;

    /**
     * Guard on the number of terms, far beyond what convergence needs.
     */
    private static final int MAX_TERMS = 1000;

    /**
     * Stands in for a zero denominator in the continued fraction.
     */
    private static final double TINY = 1e-300;

    private static final double TWO_OVER_SQRT_PI = 2.0 / Math.sqrt(Math.PI);
    private static final double ONE_OVER_SQRT_PI = 1.0 / Math.sqrt(Math.PI);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Erf() {
    }

    /**
     * Calculates the Error function at the given point.
     * <p>
     * The error function is defined as:
     * <pre>
     * erf(x) = (2/√π) ∫₀ˣ e^(-t²) dt
     * </pre>
     * accurate to around 1e-15.
     * </p>
     * <p>
     * The error function has the following properties:
     * </p>
     * <ul>
     *     <li>erf(0) = 0</li>
     *     <li>erf(-x) = -erf(x) (odd function)</li>
     *     <li>erf(∞) = 1</li>
     *     <li>Range: [-1, 1]</li>
     * </ul>
     *
     * @param x the point at which to evaluate the error function
     * @return the value of erf(x), in the range [-1, 1]
     */
    public static double erf(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        double magnitude = Math.abs(x);
        double value = magnitude < SERIES_LIMIT ? series(magnitude) : 1.0 - continuedFraction(magnitude);
        return Math.copySign(value, x);
    }

    /**
     * Calculates the complementary Error function at the given point.
     * <p>
     * The complementary error function is defined as:
     * <pre>
     * erfc(x) = 1 - erf(x) = (2/√π) ∫ₓ^∞ e^(-t²) dt
     * </pre>
     * This is useful for computing probabilities in the tail of the normal distribution:
     * for large x it keeps its relative accuracy, where {@code 1 - erf(x)} would round to zero.
     * </p>
     * <p>
     * Properties:
     * </p>
     * <ul>
     *     <li>erfc(0) = 1</li>
     *     <li>erfc(∞) = 0</li>
     *     <li>erfc(x) + erf(x) = 1 for all x</li>
     *     <li>Range: [0, 2]</li>
     * </ul>
     *
     * @param x the point at which to evaluate the complementary error function
     * @return the value of erfc(x) = 1 - erf(x), in the range [0, 2]
     * @see #erf(double)
     */
    public static double erfc(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        if (x < 0) {
            return 2.0 - erfc(-x);
        }
        return x < SERIES_LIMIT ? 1.0 - series(x) : continuedFraction(x);
    }

    /**
     * erf(x) for x &ge; 0 by the series (2/√π) e^(-x²) Σ 2ⁿ x^(2n+1) / (2n+1)!!.
     */
    private static double series(double x) {
        double twoXSquared = 2.0 * x * x;
        double term = x;
        double sum = x;
        for (int n = 1; n < MAX_TERMS; n++) {
            term *= twoXSquared / (2 * n + 1);
            sum += term;
            if (term < EPSILON * sum) {
                break;
            }
        }
        return TWO_OVER_SQRT_PI * Math.exp(-x * x) * sum;
    }

    /**
     * erfc(x) for x &gt; 0 by the continued fraction
     * (e^(-x²)/√π) / (x + (1/2) / (x + 1 / (x + (3/2) / (x + ...)))), evaluated by Lentz's method.
     */
    private static double continuedFraction(double x) {
        if (Double.isInfinite(x)) {
            return 0.0;
        }
        double f = x;
        double c = x;
        double d = 0.0;
        for (int n = 1; n < MAX_TERMS; n++) {
            double a = n / 2.0;
            d = x + a * d;
            if (d == 0.0) {
                d = TINY;
            }
            c = x + a / c;
            if (c == 0.0) {
                c = TINY;
            }
            d = 1.0 / d;
            double delta = c * d;
            f *= delta;
            if (Math.abs(delta - 1.0) < EPSILON) {
                break;
            }
        }
        return ONE_OVER_SQRT_PI * Math.exp(-x * x) / f;
    }
}
