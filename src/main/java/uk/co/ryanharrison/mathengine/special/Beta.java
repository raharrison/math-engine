package uk.co.ryanharrison.mathengine.special;

/**
 * Utility class providing the mathematical Beta function.
 * <p>
 * The Beta function, also known as the Euler integral of the first kind, is a special
 * function defined by the integral:
 * <br>
 * B(z, w) = ∫₀¹ t^(z-1) * (1-t)^(w-1) dt
 * </p>
 * <p>
 * It can be expressed in terms of the Gamma function as:
 * <br>
 * <b>B(z, w) = Γ(z) * Γ(w) / Γ(z + w)</b>
 * </p>
 * <p>
 * The Beta function has the following key properties:
 * </p>
 * <ul>
 *     <li><b>Symmetry</b>: B(z, w) = B(w, z)</li>
 *     <li><b>Domain</b>: Both z and w must be positive real numbers</li>
 *     <li><b>Special values</b>:
 *         <ul>
 *             <li>B(1, 1) = 1</li>
 *             <li>B(z, 1) = 1/z</li>
 *             <li>B(0.5, 0.5) = π</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Calculate B(2, 3)
 * double result = Beta.beta(2.0, 3.0);
 * // result ≈ 0.0833 (which equals 1/12)
 *
 * // Calculate B(0.5, 0.5)
 * double piValue = Beta.beta(0.5, 0.5);
 * // piValue ≈ 3.14159... (equals π)
 *
 * // Verify symmetry property
 * double b1 = Beta.beta(3.0, 5.0);
 * double b2 = Beta.beta(5.0, 3.0);
 * // b1 == b2
 * }</pre>
 *
 * <p>
 * This implementation computes the Beta function using the logarithmic form to avoid
 * numerical overflow for large parameter values:
 * <br>
 * B(z, w) = exp(ln Γ(z) + ln Γ(w) - ln Γ(z + w))
 * </p>
 *
 * @author Ryan Harrison
 * @see Gamma
 */
public final class Beta {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Beta() {
    }

    /**
     * Calculates the Beta function B(z, w).
     * <p>
     * The Beta function is computed using the relationship with the Gamma function:
     * <br>
     * B(z, w) = Γ(z) * Γ(w) / Γ(z + w)
     * </p>
     * <p>
     * The implementation uses logarithmic computation to prevent numerical overflow:
     * <br>
     * B(z, w) = exp(ln Γ(z) + ln Γ(w) - ln Γ(z + w))
     * </p>
     *
     * @param z the first parameter of the Beta function, must be positive
     * @param w the second parameter of the Beta function, must be positive
     * @return the value of B(z, w)
     * @throws IllegalArgumentException if {@code z <= 0} or {@code w <= 0}
     */
    public static double beta(final double z, final double w) {
        if (z <= 0.0) {
            throw new IllegalArgumentException(
                    "First parameter must be positive, got: " + z);
        }
        if (w <= 0.0) {
            throw new IllegalArgumentException(
                    "Second parameter must be positive, got: " + w);
        }

        return Math.exp(Gamma.gammaLn(z) + Gamma.gammaLn(w) - Gamma.gammaLn(z + w));
    }

    /**
     * Calculates the regularized incomplete beta function I_x(a, b).
     * <p>
     * The regularized incomplete beta function is the cumulative distribution function of
     * the Beta(a, b) distribution, and from it follow those of Student's t and the F
     * distribution:
     * <pre>
     * I_x(a, b) = B(x; a, b) / B(a, b) = (1 / B(a, b)) ∫₀ˣ t^(a-1) (1-t)^(b-1) dt
     * </pre>
     * It is evaluated by its continued fraction, using the symmetry
     * I_x(a, b) = 1 - I_(1-x)(b, a) on whichever side the fraction converges faster.
     * </p>
     *
     * @param x the upper limit of integration, in [0, 1]
     * @param a the first shape parameter, positive
     * @param b the second shape parameter, positive
     * @return I_x(a, b), in [0, 1]
     * @throws IllegalArgumentException if x is outside [0, 1] or either parameter is not positive
     */
    public static double regularizedIncomplete(double x, double a, double b) {
        if (!(x >= 0.0 && x <= 1.0)) {
            throw new IllegalArgumentException("x must be in [0, 1], got: " + x);
        }
        if (!(a > 0.0)) {
            throw new IllegalArgumentException("First parameter must be positive, got: " + a);
        }
        if (!(b > 0.0)) {
            throw new IllegalArgumentException("Second parameter must be positive, got: " + b);
        }
        if (x == 0.0 || x == 1.0) {
            return x;
        }

        // x^a (1-x)^b / B(a, b), the factor in front of the continued fraction
        double front = Math.exp(Gamma.gammaLn(a + b) - Gamma.gammaLn(a) - Gamma.gammaLn(b)
                + a * Math.log(x) + b * Math.log1p(-x));

        if (x < (a + 1.0) / (a + b + 2.0)) {
            return front * continuedFraction(x, a, b) / a;
        }
        return 1.0 - front * continuedFraction(1.0 - x, b, a) / b;
    }

    /**
     * The continued fraction for the incomplete beta function, by Lentz's method.
     */
    private static double continuedFraction(double x, double a, double b) {
        final double epsilon = 1e-16;
        final double tiny = 1e-300;
        final int maxIterations = 10_000;

        double sum = a + b;
        double aPlusOne = a + 1.0;
        double aMinusOne = a - 1.0;

        double c = 1.0;
        double d = 1.0 - sum * x / aPlusOne;
        if (Math.abs(d) < tiny) {
            d = tiny;
        }
        d = 1.0 / d;
        double result = d;

        for (int m = 1; m <= maxIterations; m++) {
            int twoM = 2 * m;

            // Even step
            double coefficient = m * (b - m) * x / ((aMinusOne + twoM) * (a + twoM));
            d = 1.0 + coefficient * d;
            if (Math.abs(d) < tiny) {
                d = tiny;
            }
            c = 1.0 + coefficient / c;
            if (Math.abs(c) < tiny) {
                c = tiny;
            }
            d = 1.0 / d;
            result *= d * c;

            // Odd step
            coefficient = -(a + m) * (sum + m) * x / ((a + twoM) * (aPlusOne + twoM));
            d = 1.0 + coefficient * d;
            if (Math.abs(d) < tiny) {
                d = tiny;
            }
            c = 1.0 + coefficient / c;
            if (Math.abs(c) < tiny) {
                c = tiny;
            }
            d = 1.0 / d;
            double delta = d * c;
            result *= delta;

            if (Math.abs(delta - 1.0) < epsilon) {
                break;
            }
        }
        return result;
    }

}
