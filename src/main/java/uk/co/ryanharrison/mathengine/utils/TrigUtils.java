package uk.co.ryanharrison.mathengine.utils;

/**
 * Comprehensive collection of trigonometric and hyperbolic mathematical functions.
 * <p>
 * This class provides implementations of:
 * </p>
 * <ul>
 *     <li>Trigonometric functions: sec, csc, cot</li>
 *     <li>Inverse trigonometric functions: asin, acos, asec, acosec, acot</li>
 *     <li>Hyperbolic functions: sinh, cosh, tanh, sech, cosech, coth</li>
 *     <li>Inverse hyperbolic functions: asinh, acosh, atanh, asech, acosech, acoth</li>
 * </ul>
 * <p>
 * All methods are static and the class cannot be instantiated.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Trigonometric functions
 * double secant = TrigUtils.sec(Math.PI / 4);
 * double cosecant = TrigUtils.cosec(Math.PI / 6);
 * double cotangent = TrigUtils.cot(Math.PI / 3);
 *
 * // Inverse trigonometric functions
 * double arcsec = TrigUtils.asec(2.0);
 * double arccot = TrigUtils.acot(1.5);
 *
 * // Hyperbolic functions
 * double sinhValue = TrigUtils.sinh(2.0);
 * double coshValue = TrigUtils.cosh(1.5);
 *
 * // Inverse hyperbolic functions
 * double atanhValue = TrigUtils.atanh(0.5);
 * double asinhValue = TrigUtils.asinh(2.0);
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class TrigUtils {

    /**
     * Not permitted to make an instance of this class.
     */
    private TrigUtils() {
    }

    // ==================== Inverse Trigonometric Functions ====================

    /**
     * Calculates the inverse sine (arcsine) of a value.
     *
     * @param a the value, must satisfy |a| ≤ 1
     * @return the inverse sine of the value in radians, in the range [-π/2, π/2]
     * @throws IllegalArgumentException if |a| > 1
     */
    public static double asin(double a) {
        if (Math.abs(a) > 1.0) {
            throw new IllegalArgumentException("Arcsine requires |x| <= 1, got: " + a);
        }
        return Math.asin(a);
    }

    /**
     * Calculates the inverse cosine (arccosine) of a value.
     *
     * @param a the value, must satisfy |a| ≤ 1
     * @return the inverse cosine of the value in radians, in the range [0, π]
     * @throws IllegalArgumentException if |a| > 1
     */
    public static double acos(double a) {
        if (Math.abs(a) > 1.0) {
            throw new IllegalArgumentException("Arccosine requires |x| <= 1, got: " + a);
        }
        return Math.acos(a);
    }

    /**
     * Calculates the inverse cosecant (arccosecant) of a value.
     * <p>
     * The inverse cosecant is defined as: arccsc(x) = arcsin(1/x)
     * </p>
     *
     * @param a the value, must satisfy |a| ≥ 1
     * @return the inverse cosecant of the value in radians, in the range [-π/2, π/2] excluding 0
     * @throws IllegalArgumentException if |a| < 1
     */
    public static double acosec(double a) {
        if (Math.abs(a) < 1.0) {
            throw new IllegalArgumentException("Arccosecant requires |x| >= 1, got: " + a);
        }
        return Math.asin(1.0 / a);
    }

    /**
     * Calculates the inverse secant (arcsecant) of a value.
     * <p>
     * The inverse secant is defined as: arcsec(x) = arccos(1/x)
     * </p>
     *
     * @param a the value, must satisfy |a| ≥ 1
     * @return the inverse secant of the value in radians, in the range [0, π] excluding π/2
     * @throws IllegalArgumentException if |a| < 1
     */
    public static double asec(double a) {
        if (Math.abs(a) < 1.0) {
            throw new IllegalArgumentException("Arcsecant requires |x| >= 1, got: " + a);
        }
        return Math.acos(1.0 / a);
    }

    /**
     * Calculates the inverse cotangent (arccotangent) of a value.
     * <p>
     * The inverse cotangent is defined as: arccot(x) = arctan(1/x)
     * </p>
     *
     * @param a the value
     * @return the inverse cotangent of the value in radians, in the range
     *         [-π/2, π/2] excluding 0, taking the value π/2 at zero
     */
    public static double acot(double a) {
        return Math.atan(1.0 / a);
    }

    // ==================== Inverse Hyperbolic Functions ====================

    /**
     * Calculates the inverse hyperbolic cosecant (arccosech) of a value.
     * <p>
     * The inverse hyperbolic cosecant is defined as:
     * <br>
     * arccsch(x) = ln(1/x + √(1/x² + 1))
     * </p>
     *
     * @param a the value, must not be zero
     * @return the inverse hyperbolic cosecant of the value
     * @throws IllegalArgumentException if a is zero
     */
    public static double acosech(double a) {
        if (a == 0.0) {
            throw new IllegalArgumentException("Arccosech is undefined for x = 0");
        }

        double sgn = Math.signum(a);
        a = Math.abs(a);
        return sgn * Math.log(1.0 / a + Math.sqrt(1.0 / (a * a) + 1.0));
    }

    /**
     * Calculates the inverse hyperbolic cosine (arccosh) of a value.
     * <p>
     * The inverse hyperbolic cosine is defined as:
     * <br>
     * arccosh(x) = ln(x + √(x² - 1))
     * </p>
     *
     * @param a the value, must be ≥ 1
     * @return the inverse hyperbolic cosine of the value
     * @throws IllegalArgumentException if a < 1
     */
    public static double acosh(double a) {
        if (a < 1.0) {
            throw new IllegalArgumentException("Arccosh requires x >= 1, got: " + a);
        }
        return Math.log(a + Math.sqrt(a * a - 1.0));
    }

    /**
     * Calculates the inverse hyperbolic cotangent (arccoth) of a value.
     * <p>
     * The inverse hyperbolic cotangent is defined as:
     * <br>
     * arccoth(x) = (1/2) * ln((x + 1) / (x - 1))
     * </p>
     *
     * @param a the value, must satisfy |a| > 1
     * @return the inverse hyperbolic cotangent of the value
     * @throws IllegalArgumentException if |a| ≤ 1
     */
    public static double acoth(double a) {
        if (Math.abs(a) <= 1.0) {
            throw new IllegalArgumentException("Arccoth requires |x| > 1, got: " + a);
        }

        double sgn = Math.signum(a);
        a = Math.abs(a);
        return 0.5 * sgn * (Math.log(a + 1.0) - Math.log(a - 1.0));
    }

    /**
     * Calculates the inverse hyperbolic secant (arcsech) of a value.
     * <p>
     * The inverse hyperbolic secant is defined as:
     * <br>
     * arcsech(x) = ln((√(1 - x²) + 1) / x)
     * </p>
     *
     * @param a the value, must be in the range (0, 1]
     * @return the inverse hyperbolic secant of the value
     * @throws IllegalArgumentException if a ≤ 0 or a > 1
     */
    public static double asech(double a) {
        if (a <= 0.0 || a > 1.0) {
            throw new IllegalArgumentException("Arcsech requires 0 < x <= 1, got: " + a);
        }
        return Math.log((Math.sqrt(1.0 - a * a) + 1.0) / a);
    }

    /**
     * Calculates the inverse hyperbolic sine (arcsinh) of a value.
     * <p>
     * The inverse hyperbolic sine is defined as:
     * <br>
     * arcsinh(x) = ln(x + √(x² + 1))
     * </p>
     *
     * @param a the value
     * @return the inverse hyperbolic sine of the value
     */
    public static double asinh(double a) {
        double sgn = Math.signum(a);
        a = Math.abs(a);
        return sgn * Math.log(a + Math.sqrt(a * a + 1.0));
    }

    /**
     * Calculates the inverse hyperbolic tangent (arctanh) of a value.
     * <p>
     * The inverse hyperbolic tangent is defined as:
     * <br>
     * arctanh(x) = (1/2) * ln((1 + x) / (1 - x))
     * </p>
     *
     * @param a the value, must satisfy |a| < 1
     * @return the inverse hyperbolic tangent of the value
     * @throws IllegalArgumentException if |a| ≥ 1
     */
    public static double atanh(double a) {
        if (Math.abs(a) >= 1.0) {
            throw new IllegalArgumentException("Arctanh requires |x| < 1, got: " + a);
        }

        double sgn = Math.signum(a);
        a = Math.abs(a);
        return 0.5 * sgn * (Math.log(1.0 + a) - Math.log(1.0 - a));
    }

    // ==================== Trigonometric Functions ====================

    /**
     * Calculates the cosecant of an angle.
     * <p>
     * The cosecant is defined as: csc(x) = 1 / sin(x)
     * </p>
     *
     * @param a the angle in radians
     * @return the cosecant of the angle
     * @throws IllegalArgumentException if sin(a) is zero (when a is a multiple of π)
     */
    public static double cosec(double a) {
        double sinValue = Math.sin(a);
        if (sinValue == 0.0) {
            throw new IllegalArgumentException("Cosecant is undefined when sin(x) = 0");
        }
        return 1.0 / sinValue;
    }

    /**
     * Calculates the secant of an angle.
     * <p>
     * The secant is defined as: sec(x) = 1 / cos(x)
     * </p>
     *
     * @param a the angle in radians
     * @return the secant of the angle
     * @throws IllegalArgumentException if cos(a) is zero (when a is an odd multiple of π/2)
     */
    public static double sec(double a) {
        double cosValue = Math.cos(a);
        if (cosValue == 0.0) {
            throw new IllegalArgumentException("Secant is undefined when cos(x) = 0");
        }
        return 1.0 / cosValue;
    }

    /**
     * Calculates the cotangent of an angle.
     * <p>
     * The cotangent is defined as: cot(x) = 1 / tan(x) = cos(x) / sin(x)
     * </p>
     *
     * @param a the angle in radians
     * @return the cotangent of the angle
     * @throws IllegalArgumentException if tan(a) is zero (when a is a multiple of π)
     */
    public static double cot(double a) {
        double tanValue = Math.tan(a);
        if (tanValue == 0.0) {
            throw new IllegalArgumentException("Cotangent is undefined when tan(x) = 0");
        }
        return 1.0 / tanValue;
    }

    // ==================== Hyperbolic Functions ====================

    /**
     * Calculates the hyperbolic sine of a value.
     * <p>
     * The hyperbolic sine is defined as: sinh(x) = (e^x - e^(-x)) / 2
     * </p>
     *
     * @param a the value
     * @return the hyperbolic sine of the value
     */
    public static double sinh(double a) {
        return 0.5 * (Math.exp(a) - Math.exp(-a));
    }

    /**
     * Calculates the hyperbolic cosine of a value.
     * <p>
     * The hyperbolic cosine is defined as: cosh(x) = (e^x + e^(-x)) / 2
     * </p>
     *
     * @param a the value
     * @return the hyperbolic cosine of the value
     */
    public static double cosh(double a) {
        return 0.5 * (Math.exp(a) + Math.exp(-a));
    }

    /**
     * Calculates the hyperbolic tangent of a value.
     * <p>
     * The hyperbolic tangent is defined as: tanh(x) = sinh(x) / cosh(x)
     * </p>
     *
     * @param a the value
     * @return the hyperbolic tangent of the value
     */
    public static double tanh(double a) {
        return sinh(a) / cosh(a);
    }

    /**
     * Calculates the hyperbolic cosecant of a value.
     * <p>
     * The hyperbolic cosecant is defined as: csch(x) = 1 / sinh(x)
     * </p>
     *
     * @param a the value, must not be zero
     * @return the hyperbolic cosecant of the value
     * @throws IllegalArgumentException if a is zero
     */
    public static double cosech(double a) {
        if (a == 0.0) {
            throw new IllegalArgumentException("Cosech is undefined for x = 0");
        }
        return 1.0 / sinh(a);
    }

    /**
     * Calculates the hyperbolic secant of a value.
     * <p>
     * The hyperbolic secant is defined as: sech(x) = 1 / cosh(x)
     * </p>
     *
     * @param a the value
     * @return the hyperbolic secant of the value
     */
    public static double sech(double a) {
        return 1.0 / cosh(a);
    }

    /**
     * Calculates the hyperbolic cotangent of a value.
     * <p>
     * The hyperbolic cotangent is defined as: coth(x) = 1 / tanh(x) = cosh(x) / sinh(x)
     * </p>
     *
     * @param a the value, must not be zero
     * @return the hyperbolic cotangent of the value
     * @throws IllegalArgumentException if a is zero
     */
    public static double coth(double a) {
        if (a == 0.0) {
            throw new IllegalArgumentException("Coth is undefined for x = 0");
        }
        return 1.0 / tanh(a);
    }
}
