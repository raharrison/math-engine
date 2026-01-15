package uk.co.ryanharrison.mathengine.utils;

import uk.co.ryanharrison.mathengine.special.Gamma;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Comprehensive collection of mathematical utility functions and constants.
 * <p>
 * This class provides implementations of various mathematical functions including:
 * </p>
 * <ul>
 *     <li>Factorial and combinatorial functions</li>
 *     <li>Number theory functions (GCD, LCM)</li>
 *     <li>Utility functions (rounding, hypotenuse, clamping, interpolation)</li>
 *     <li>Angle conversion and distance functions</li>
 * </ul>
 * <p>
 * All methods are static and the class cannot be instantiated.
 * </p>
 * <p>
 * <b>Note:</b> Trigonometric and hyperbolic functions have been moved to {@link TrigUtils}.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Factorials and combinations
 * long fact = MathUtils.factorial(5);  // 120
 * double comb = MathUtils.combination(10, 3);  // 120
 *
 * // Number theory
 * int gcd = MathUtils.gcd(48, 18);  // 6
 *
 * // Utility functions
 * double rounded = MathUtils.round(3.14159, 2);  // 3.14
 * double clamped = MathUtils.clamp(15, 0, 10);  // 10
 *
 * // Distance calculations
 * double dist = MathUtils.distance(0, 0, 3, 4);  // 5.0
 * }</pre>
 *
 * @author Ryan Harrison
 * @see TrigUtils for trigonometric and hyperbolic functions
 */
public final class MathUtils {

    /**
     * The square root of π (pi).
     * <br>
     * √π ≈ 1.772453850905516027298
     * <p>
     * Note: The value in the original code (2.506628...) was actually √(2π), not √π.
     * Retained for backward compatibility.
     * </p>
     */
    public static final double SQTPI = 2.50662827463100050242E0;

    /**
     * The natural logarithm of π (pi).
     * <br>
     * ln(π) ≈ 1.14472988584940017414
     */
    public static final double LOGPI = 1.14472988584940017414;

    /**
     * Not permitted to make an instance of this class.
     */
    private MathUtils() {
    }

    // ==================== Factorial Functions ====================

    /**
     * Calculates the factorial of a non-negative integer.
     * <p>
     * The factorial is defined as: n! = n × (n-1) × (n-2) × ... × 2 × 1
     * <br>
     * By convention, 0! = 1
     * </p>
     *
     * @param num the non-negative integer
     * @return the factorial of num
     * @throws IllegalArgumentException if num is negative
     */
    public static long factorial(long num) {
        if (num < 0) {
            throw new IllegalArgumentException("Factorial requires non-negative integer, got: " + num);
        }

        long answer = 1;
        for (long i = 2; i <= num; i++) {
            answer *= i;
        }

        return answer;
    }

    /**
     * Calculates the factorial of a real number using the Gamma function.
     * <p>
     * For real numbers, the factorial is defined as: x! = Γ(x + 1)
     * <br>
     * For integers, this delegates to {@link #factorial(long)} for exact computation.
     * </p>
     *
     * @param x the value
     * @return the factorial of x, computed as Γ(x + 1)
     * @see Gamma#gamma(double)
     */
    public static double factorial(double x) {
        // For integers, use the exact integer factorial
        if (Math.floor(x) == x && x >= 0) {
            return factorial((long) x);
        }
        return Gamma.gamma(x + 1.0);
    }

    /**
     * Calculates the double factorial of a non-negative integer.
     * <p>
     * The double factorial is defined as:
     * </p>
     * <ul>
     *     <li>n!! = n × (n-2) × (n-4) × ... × 2 (for even n)</li>
     *     <li>n!! = n × (n-2) × (n-4) × ... × 1 (for odd n)</li>
     *     <li>0!! = 1!! = 1 (by convention)</li>
     * </ul>
     *
     * @param x the non-negative integer
     * @return the double factorial of x
     * @throws IllegalArgumentException if x is negative
     */
    public static double doubleFactorial(long x) {
        if (x < 0) {
            throw new IllegalArgumentException("Double factorial requires non-negative integer, got: " + x);
        }

        if (x == 0 || x == 1) {
            return 1;
        }

        double result = 1;
        for (long i = x; i > 1; i -= 2) {
            result *= i;
        }

        return result;
    }

    /**
     * Calculates the double factorial of a real number using the Gamma function.
     * <p>
     * For real numbers, the double factorial is defined using:
     * <br>
     * x!! = Γ(x/2 + 1) × 2^(x/2) × (π/2)^((cos(πx) - 1) / 4)
     * <br>
     * For integers, this delegates to {@link #doubleFactorial(long)} for exact computation.
     * </p>
     *
     * @param x the value
     * @return the double factorial of x
     */
    public static double doubleFactorial(double x) {
        // For non-negative integers, use the exact integer double factorial
        if (Math.floor(x) == x && x >= 0) {
            return doubleFactorial((long) x);
        }

        return Gamma.gamma((x / 2.0) + 1.0) * Math.pow(2, x / 2.0)
                * Math.pow(Math.PI / 2, 0.25 * (-1 + Math.cos(x * Math.PI)));
    }

    // ==================== Combinatorial Functions ====================

    /**
     * Calculates the binomial coefficient "n choose k" — the number of ways to choose k items from n items.
     * <p>
     * Supports integer and real n/k, including:
     * <ul>
     *     <li>Positive integers n and k</li>
     *     <li>Negative integer n</li>
     *     <li>Real-valued n and k using the Gamma function</li>
     * </ul>
     * </p>
     *
     * <p>
     * Edge cases:
     * <ul>
     *     <li>If k < 0, returns 0</li>
     *     <li>If integer n and k > n, returns 0</li>
     *     <li>If negative integer n and integer k ≥ 0, uses generalized formula: C(-n,k) = (-1)^k × C(n+k-1,k)</li>
     * </ul>
     * </p>
     *
     * @param n the size of the set (can be real or integer, positive or negative integer)
     * @param k the size of the subsets to count (can be real or integer)
     * @return the binomial coefficient C(n, k), or POSITIVE_INFINITY/NaN if undefined
     */
    public static double combination(double n, double k) {
        // Edge case: k < 0 means 0
        if (k < 0) return 0;

        // INTEGER BRANCH: both n and k are integers
        if (n % 1 == 0 && k % 1 == 0) {
            long K = (long) k;

            // Handle negative integer n using generalized formula
            if (n < 0) {
                long N = (long) (-n);
                return ((K % 2 == 0) ? 1 : -1) * combination(N + K - 1, K);
            }

            // n is non-negative integer
            long N = (long) n;
            if (K > N) return 0;          // can't choose more than available
            K = Math.min(K, N - K);       // symmetry: C(n,k) = C(n,n-k)
            double result = 1.0;

            // Multiplicative formula avoids factorial overflow
            for (long i = 1; i <= K; i++) {
                result *= (N - (K - i));
                result /= i;
            }
            return result;
        }

        // Avoid gamma poles for real n/k
        if ((k <= 0 && k != Math.floor(k)) || ((n - k) <= 0 && (n - k) != Math.floor(n - k))) {
            return Double.NaN;
        }

        // Gamma function formula
        return Math.exp(
                Gamma.gammaLn(n + 1)
                        - Gamma.gammaLn(k + 1)
                        - Gamma.gammaLn(n - k + 1)
        );
    }

    /**
     * Calculates the number of permutations - the number of ways to arrange r items from n items.
     * <p>
     * The permutation is defined as:
     * <br>
     * P(n, r) = n! / (n - r)!
     * </p>
     *
     * @param n the size of the set
     * @param r the number of items to arrange
     * @return the number of permutations P(n, r)
     */
    public static double permutation(double n, double r) {
        return factorial(n) / factorial(n - r);
    }

    // ==================== Number Theory Functions ====================

    /**
     * Calculates the greatest common divisor (GCD) of two integers using Euclid's algorithm.
     * <p>
     * The GCD is the largest positive integer that divides both numbers without remainder.
     * </p>
     *
     * @param xval the first integer
     * @param yval the second integer
     * @return the greatest common divisor of xval and yval
     */
    public static int gcd(int xval, int yval) {
        xval = Math.abs(xval);
        yval = Math.abs(yval);

        while (yval != 0) {
            int temp = yval;
            yval = xval % yval;
            xval = temp;
        }

        return xval;
    }

    /**
     * Calculates the greatest common factor (GCF) of an array of integers.
     * <p>
     * The GCF is the largest positive integer that divides all numbers in the array without remainder.
     * </p>
     *
     * @param data the array of integers, must not be null or empty
     * @return the greatest common factor of all values in the array
     * @throws IllegalArgumentException if data is null or empty
     */
    public static long greatestCommonFactor(int[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data array cannot be null or empty");
        }

        if (data.length == 1) {
            return Math.abs(data[0]);
        }

        // Use iterative GCD approach
        int result = Math.abs(data[0]);
        for (int i = 1; i < data.length; i++) {
            result = gcd(result, data[i]);
            if (result == 1) {
                return 1; // Early termination
            }
        }

        return result;
    }

    /**
     * Calculates the least common multiple (LCM) of an array of integers.
     * <p>
     * The LCM is the smallest positive integer that is divisible by all numbers in the array.
     * </p>
     *
     * @param data the array of integers, must not be null or empty
     * @return the least common multiple of all values in the array
     * @throws IllegalArgumentException if data is null or empty
     */
    public static long lowestCommonMultiple(int[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data array cannot be null or empty");
        }

        if (data.length == 1) {
            return Math.abs(data[0]);
        }

        // Use iterative LCM approach: lcm(a,b) = |a*b| / gcd(a,b)
        long result = Math.abs(data[0]);
        for (int i = 1; i < data.length; i++) {
            result = lcm(result, Math.abs(data[i]));
        }

        return result;
    }

    /**
     * Calculates the least common multiple of two integers.
     *
     * @param a the first integer
     * @param b the second integer
     * @return the least common multiple of a and b
     */
    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return Math.abs(a * b) / gcd((int) a, (int) b);
    }

    // ==================== Utility Functions ====================

    /**
     * Calculates the fractional part of a number.
     * <p>
     * The fractional part is defined as: frac(x) = x - floor(x)
     * <br>
     * This always returns a non-negative value in the range [0, 1).
     * </p>
     * <p>
     * Examples:
     * </p>
     * <ul>
     *     <li>fPart(3.7) = 0.7</li>
     *     <li>fPart(-2.3) = 0.7</li>
     *     <li>fPart(5.0) = 0.0</li>
     * </ul>
     *
     * @param x the number
     * @return the fractional part of x, always in [0, 1)
     */
    public static double fPart(double x) {
        return x - Math.floor(x);
    }

    /**
     * Calculates the hypotenuse of a right triangle with sides a and b.
     * <p>
     * This method uses a numerically stable algorithm to compute √(a² + b²) without overflow or underflow.
     * </p>
     *
     * @param a the first side
     * @param b the second side
     * @return the length of the hypotenuse √(a² + b²)
     */
    public static double hypot(double a, double b) {
        // Use Math.hypot which implements a numerically stable algorithm
        return Math.hypot(a, b);
    }

    /**
     * Calculates the nth root of a number.
     * <p>
     * The nth root is defined as: root(x, n) = x^(1/n)
     * </p>
     *
     * @param num the number
     * @param n   the root degree
     * @return the nth root of num
     */
    public static double root(double num, double n) {
        return Math.pow(num, 1.0 / n);
    }

    /**
     * Rounds a number to a specified number of decimal places using HALF_UP rounding mode.
     * <p>
     * This uses {@link BigDecimal} for precise decimal rounding.
     * </p>
     *
     * @param number the number to round
     * @param places the number of decimal places (negative values are treated as absolute value)
     * @return the number rounded to the specified number of decimal places
     */
    public static double round(double number, int places) {
        places = Math.abs(places);
        return BigDecimal.valueOf(number)
                .setScale(places, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Returns the sign of a number.
     * <p>
     * Returns:
     * </p>
     * <ul>
     *     <li>-1.0 if x < 0</li>
     *     <li>1.0 if x >= 0</li>
     * </ul>
     *
     * @param x the number
     * @return the sign of x (-1.0 or 1.0)
     */
    public static double sign(double x) {
        return x < 0.0 ? -1.0 : 1.0;
    }

    /**
     * Calculates compound interest.
     * <p>
     * The compound interest formula is: A = P × (1 + r/100)^t
     * <br>
     * where A is the final amount, P is the principal, r is the rate, and t is time.
     * </p>
     *
     * @param principal    the initial amount
     * @param interestRate the interest rate as a percentage (e.g., 5 for 5%)
     * @param years        the number of years
     * @return the final amount after applying compound interest
     */
    public static double compoundInterest(double principal, double interestRate, double years) {
        return principal * Math.pow(1.0 + interestRate / 100.0, years);
    }

    /**
     * Determines whether two numbers are equal within a specified absolute tolerance.
     * <p>
     * Returns true if |x - y| ≤ |tolerance|
     * </p>
     *
     * @param x         the first number
     * @param y         the second number
     * @param tolerance the absolute tolerance
     * @return {@code true} if the numbers are equal within the specified tolerance, {@code false} otherwise
     */
    public static boolean isEqualWithinLimits(double x, double y, double tolerance) {
        return Math.abs(x - y) <= Math.abs(tolerance);
    }

    /**
     * Determines whether two numbers are equal within a specified percentage tolerance.
     * <p>
     * The tolerance is calculated as a percentage of the average of the two numbers:
     * <br>
     * tolerance_value = |(x + y) × percentage / 200|
     * </p>
     *
     * @param x          the first number
     * @param y          the second number
     * @param percentage the percentage tolerance (e.g., 5 for 5%)
     * @return {@code true} if the numbers are equal within the specified percentage, {@code false} otherwise
     */
    public static boolean isEqualWithinPerCent(double x, double y, double percentage) {
        double limit = Math.abs((x + y) * percentage / 200.0);
        return Math.abs(x - y) <= limit;
    }

    // ==================== Value Clamping and Range Functions ====================

    /**
     * Clamps a value to be within a specified range.
     * <p>
     * If value < min, returns min.
     * If value > max, returns max.
     * Otherwise, returns value.
     * </p>
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     * @throws IllegalArgumentException if min > max
     */
    public static double clamp(double value, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("Min cannot be greater than max. Min: " + min + ", Max: " + max);
        }
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Linear interpolation between two values.
     * <p>
     * Calculates: start + t × (end - start)
     * <br>
     * When t = 0, returns start. When t = 1, returns end.
     * Values of t outside [0, 1] result in extrapolation.
     * </p>
     *
     * @param start the starting value
     * @param end   the ending value
     * @param t     the interpolation parameter (typically in [0, 1])
     * @return the interpolated value
     */
    public static double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    /**
     * Inverse linear interpolation - finds the parameter t for a given value.
     * <p>
     * Given value = lerp(start, end, t), solves for t:
     * <br>
     * t = (value - start) / (end - start)
     * </p>
     *
     * @param start the starting value
     * @param end   the ending value
     * @param value the value to find the parameter for
     * @return the parameter t such that lerp(start, end, t) = value
     * @throws IllegalArgumentException if start == end
     */
    public static double inverseLerp(double start, double end, double value) {
        if (start == end) {
            throw new IllegalArgumentException("Start and end cannot be equal");
        }
        return (value - start) / (end - start);
    }

    /**
     * Maps a value from one range to another.
     * <p>
     * Converts a value in the range [fromMin, fromMax] to the equivalent value in [toMin, toMax].
     * </p>
     *
     * @param value   the value to map
     * @param fromMin the minimum of the source range
     * @param fromMax the maximum of the source range
     * @param toMin   the minimum of the target range
     * @param toMax   the maximum of the target range
     * @return the mapped value
     * @throws IllegalArgumentException if fromMin == fromMax
     */
    public static double remap(double value, double fromMin, double fromMax, double toMin, double toMax) {
        if (fromMin == fromMax) {
            throw new IllegalArgumentException("Source range cannot have zero width");
        }
        double t = inverseLerp(fromMin, fromMax, value);
        return lerp(toMin, toMax, t);
    }

    // ==================== Angle Conversion Functions ====================

    /**
     * Normalizes an angle in degrees to the range [0, 360).
     *
     * @param degrees the angle in degrees
     * @return the normalized angle in [0, 360)
     */
    public static double normalizeDegrees(double degrees) {
        degrees = degrees % 360.0;
        if (degrees < 0) {
            degrees += 360.0;
        }
        return degrees;
    }

    /**
     * Normalizes an angle in radians to the range [0, 2π).
     *
     * @param radians the angle in radians
     * @return the normalized angle in [0, 2π)
     */
    public static double normalizeRadians(double radians) {
        radians = radians % (2.0 * Math.PI);
        if (radians < 0) {
            radians += 2.0 * Math.PI;
        }
        return radians;
    }

    // ==================== Distance Functions ====================

    /**
     * Calculates the Euclidean distance between two 2D points.
     * <p>
     * distance = √((x2 - x1)² + (y2 - y1)²)
     * </p>
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @return the Euclidean distance between the points
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.hypot(dx, dy);
    }

    /**
     * Calculates the Euclidean distance between two 3D points.
     * <p>
     * distance = √((x2 - x1)² + (y2 - y1)² + (z2 - z1)²)
     * </p>
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param z1 the z-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @param z2 the z-coordinate of the second point
     * @return the Euclidean distance between the points
     */
    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculates the Manhattan distance (taxicab distance) between two 2D points.
     * <p>
     * distance = |x2 - x1| + |y2 - y1|
     * </p>
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @return the Manhattan distance between the points
     */
    public static double manhattanDistance(double x1, double y1, double x2, double y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }

    /**
     * Calculates the logarithm of a number with a specified base.
     * <p>
     * log_base(x) = ln(x) / ln(base)
     * </p>
     *
     * @param value the value
     * @param base the logarithm base
     * @return the logarithm of value with the specified base
     * @throws IllegalArgumentException if value <= 0 or base <= 0 or base == 1
     */
    public static double logBase(double value, double base) {
        if (value <= 0) {
            throw new IllegalArgumentException("Value must be positive, got: " + value);
        }
        if (base <= 0 || base == 1) {
            throw new IllegalArgumentException("Base must be positive and not equal to 1, got: " + base);
        }
        return Math.log(value) / Math.log(base);
    }
}
