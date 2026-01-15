package uk.co.ryanharrison.mathengine.special;

/**
 * Utility class providing static methods for computing the Gamma function and its logarithm.
 * <p>
 * The Gamma function Γ(x) is a continuous extension of the factorial function to real and complex numbers.
 * For positive integers n, it satisfies the relationship:
 * <br>
 * Γ(n) = (n-1)!
 * </p>
 * <p>
 * The Gamma function is defined by the integral:
 * <br>
 * Γ(x) = ∫₀^∞ t^(x-1) e^(-t) dt
 * </p>
 * <p>
 * Key properties:
 * </p>
 * <ul>
 *     <li><b>Recurrence relation:</b> Γ(x+1) = x·Γ(x)</li>
 *     <li><b>Γ(1) = 1</b> (base case)</li>
 *     <li><b>Γ(0.5) = √π</b></li>
 *     <li><b>Γ(n) = (n-1)!</b> for positive integers n</li>
 * </ul>
 *
 * <h2>Numerical Stability:</h2>
 * <p>
 * For large values of x, Γ(x) grows extremely rapidly and can overflow. The {@link #gammaLn(double)}
 * method provides the natural logarithm of the Gamma function, which remains numerically stable
 * for much larger arguments. When possible, use {@code gammaLn} for calculations and only
 * exponentiate the result when necessary.
 * </p>
 *
 * <h2>Approximation Method:</h2>
 * <p>
 * This implementation uses the asymptotic series from Abramowitz and Stegun 6.1.41,
 * which provides accuracy to at least 11-12 significant figures. The approximation
 * is based on Stirling's formula with correction terms.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Compute Γ(5) = 4! = 24
 * double result = Gamma.gamma(5.0);  // Returns 24.0
 *
 * // Compute Γ(0.5) = √π
 * double halfGamma = Gamma.gamma(0.5);  // Returns approximately 1.772453850905516
 *
 * // For large values, use gammaLn to avoid overflow
 * double logGamma = Gamma.gammaLn(100.0);  // Returns ln(Γ(100))
 * // Equivalent to ln(99!) ≈ 359.13
 *
 * // Compute Γ(x) for large x using logarithm
 * double largeX = 200.0;
 * double logResult = Gamma.gammaLn(largeX);
 * // To get Γ(200), use exp(logResult), but beware of overflow
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class Gamma {
    /**
     * Precomputed value of ln(2π)/2, used in the Stirling approximation.
     * <p>
     * This constant appears in the asymptotic expansion of the Gamma function:
     * <br>
     * ln(Γ(x)) ≈ (x - 0.5)·ln(x) - x + ln(2π)/2 + correction_terms
     * </p>
     * Value: ln(2π)/2 = ln(√(2π)) ≈ 0.91893853320467274178
     */
    private static final double HALF_LOG_TWOPI = 0.91893853320467274178032973640562;

    /**
     * Coefficients for the Abramowitz and Stegun 6.1.41 asymptotic series.
     * <p>
     * These coefficients are used in the correction terms of Stirling's approximation
     * to achieve 11-12 digit accuracy. The series is:
     * <br>
     * correction = (c[0] + c[1]·z + c[2]·z² + ... + c[7]·z⁷) / x
     * <br>
     * where z = 1/x²
     * </p>
     * <p>
     * Reference: Abramowitz and Stegun, "Handbook of Mathematical Functions" (1964),
     * formula 6.1.41. For error analysis, see Whittaker and Watson,
     * "A Course in Modern Analysis" (1927), page 252.
     * </p>
     */
    private static final double[] STIRLING_COEFFICIENTS = {
            1.0 / 12.0,
            -1.0 / 360.0,
            1.0 / 1260.0,
            -1.0 / 1680.0,
            1.0 / 1188.0,
            -691.0 / 360360.0,
            1.0 / 156.0,
            -3617.0 / 122400.0
    };

    /**
     * Not permitted to create an instance of this class
     */
    private Gamma() {
    }

    /**
     * Calculates the value of the Gamma function at the given point.
     * <p>
     * Computes Γ(x) using the relationship Γ(x) = e^(ln(Γ(x))). For large values of x,
     * this method may overflow. In such cases, use {@link #gammaLn(double)} to compute
     * the natural logarithm directly, which remains numerically stable.
     * </p>
     *
     * @param x the point at which to evaluate the Gamma function; must be positive
     * @return the value of Γ(x)
     * @throws IllegalArgumentException if {@code x <= 0}
     */
    public static double gamma(double x) {
        return Math.exp(gammaLn(x));
    }

    /**
     * Calculates the natural logarithm of the Gamma function at the given point.
     * <p>
     * Computes ln(Γ(x)) using the asymptotic expansion from Abramowitz and Stegun 6.1.41,
     * based on Stirling's approximation with correction terms. This method is numerically
     * stable for large values of x where Γ(x) itself would overflow.
     * </p>
     * <p>
     * For small values of x (x < 10), this method uses the recurrence relation
     * Γ(x+1) = x·Γ(x) to shift x into a range where Stirling's approximation is accurate,
     * then adjusts the result accordingly.
     * </p>
     * <p>
     * The approximation achieves accuracy to at least 11-12 significant figures.
     * </p>
     *
     * @param x the point at which to evaluate ln(Γ(x)); must be positive
     * @return the value of ln(Γ(x))
     * @throws IllegalArgumentException if {@code x <= 0}
     * @see <a href="http://www.johndcook.com/Gamma.cs">http://www.johndcook.com/Gamma.cs</a>
     */
    public static double gammaLn(final double x) {
        if (x <= 0.0) {
            String msg = String.format("Invalid input argument %s. Argument must be positive.", x);
            throw new IllegalArgumentException(msg);
        }

        // For small x, use recurrence relation to shift into range where Stirling works well
        // Γ(x+1) = x·Γ(x)  =>  ln(Γ(x)) = ln(Γ(x+n)) - ln(x) - ln(x+1) - ... - ln(x+n-1)
        double tmp = x;

        // Shift x into range where Stirling's approximation is accurate (x >= 10)
        while (tmp < 10.0) {
            tmp += 1.0;
        }

        // Apply Stirling's approximation with correction terms
        double z = 1.0 / (tmp * tmp);
        double sum = STIRLING_COEFFICIENTS[7];
        for (int i = 6; i >= 0; i--) {
            sum *= z;
            sum += STIRLING_COEFFICIENTS[i];
        }
        double series = sum / tmp;

        double result = (tmp - 0.5) * Math.log(tmp) - tmp + HALF_LOG_TWOPI + series;

        // If we shifted x, adjust the result using the recurrence relation
        // ln(Γ(x)) = ln(Γ(x+n)) - ln(x) - ln(x+1) - ... - ln(x+n-1)
        while (tmp > x) {
            tmp -= 1.0;
            result -= Math.log(tmp);
        }

        return result;
    }

    /**
     * Calculates the digamma function ψ(x), also known as the psi function.
     * <p>
     * The digamma function is the logarithmic derivative of the Gamma function:
     * <br>
     * ψ(x) = d/dx ln(Γ(x)) = Γ'(x) / Γ(x)
     * </p>
     * <p>
     * Key properties:
     * </p>
     * <ul>
     *     <li><b>Recurrence relation:</b> ψ(x + 1) = ψ(x) + 1/x</li>
     *     <li><b>Reflection formula:</b> ψ(1 - x) = ψ(x) + π·cot(πx)</li>
     *     <li><b>Special values:</b> ψ(1) = -γ (negative Euler-Mascheroni constant)</li>
     * </ul>
     * <p>
     * This implementation uses the asymptotic series approximation for large x,
     * combined with the recurrence relation to handle small values.
     * </p>
     *
     * @param x the point at which to evaluate ψ(x)
     * @return the value of ψ(x), or {@code Double.NaN} if x is a non-positive integer
     */
    public static double digamma(double x) {
        // For non-positive integers, digamma is undefined
        if (x <= 0 && x == Math.floor(x)) {
            return Double.NaN;
        }

        // Use reflection formula for negative values
        if (x < 0) {
            return digamma(1 - x) - Math.PI / Math.tan(Math.PI * x);
        }

        // Use recurrence relation to shift x into range where asymptotic series works well
        double result = 0;
        while (x < 6) {
            result -= 1 / x;
            x += 1;
        }

        // Apply asymptotic series for large x
        double x2 = 1 / (x * x);
        return result + Math.log(x) - 0.5 / x - x2 * (1.0 / 12 - x2 * (1.0 / 120 - x2 / 252));
    }
}