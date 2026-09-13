package uk.co.ryanharrison.mathengine.parser.function.trig;

import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.TrigFunction;
import uk.co.ryanharrison.mathengine.utils.TrigUtils;

import java.util.List;

/**
 * Collection of trigonometric functions.
 * <p>
 * Every one is built by {@link TrigFunction}, which is where the angle unit is applied:
 * to the argument of a standard function, and to the result of an inverse one.
 */
public final class TrigonometricFunctions {

    private TrigonometricFunctions() {
    }

    // ==================== Standard Trig Functions ====================

    /**
     * Sine function.
     */
    public static final MathFunction SIN = TrigFunction.standard(
            "sin", "Returns the sine of x", Math::sin);

    /**
     * Cosine function.
     */
    public static final MathFunction COS = TrigFunction.standard(
            "cos", "Returns the cosine of x", Math::cos);

    /**
     * Tangent function.
     */
    public static final MathFunction TAN = TrigFunction.standard(
            "tan", "Returns the tangent of x", Math::tan);

    // ==================== Reciprocal Trig Functions ====================

    /**
     * Secant function (1/cos).
     */
    public static final MathFunction SEC = TrigFunction.standard(
            "sec", "Returns the secant of x (1/cos(x))", TrigUtils::sec);

    /**
     * Cosecant function (1/sin).
     */
    public static final MathFunction CSC = TrigFunction.standard(
            "csc", "Returns the cosecant of x (1/sin(x))", TrigUtils::cosec);

    /**
     * Cotangent function (1/tan).
     */
    public static final MathFunction COT = TrigFunction.standard(
            "cot", "Returns the cotangent of x (1/tan(x))", TrigUtils::cot);

    // ==================== Inverse Trig Functions ====================

    /** Arcsine function, defined on [-1, 1]. */
    public static final MathFunction ASIN = TrigFunction.inverse(
            "asin", "Returns the arcsine of x (inverse sine)", TrigUtils::asin);

    /** Arccosine function, defined on [-1, 1]. */
    public static final MathFunction ACOS = TrigFunction.inverse(
            "acos", "Returns the arccosine of x (inverse cosine)", TrigUtils::acos);

    /** Arctangent function. */
    public static final MathFunction ATAN = TrigFunction.inverse(
            "atan", "Returns the arctangent of x (inverse tangent)", Math::atan);

    // ==================== Inverse Reciprocal Trig Functions ====================

    /**
     * Arcsecant function, acos(1/x), defined for |x| >= 1.
     */
    public static final MathFunction ASEC = TrigFunction.inverse(
            "asec", "Returns the arcsecant of x (inverse secant), acos(1/x)", TrigUtils::asec);

    /**
     * Arccosecant function, asin(1/x), defined for |x| >= 1.
     */
    public static final MathFunction ACSC = TrigFunction.inverse(
            "acsc", "Returns the arccosecant of x (inverse cosecant), asin(1/x)", TrigUtils::acosec);

    /**
     * Arccotangent function, atan(1/x), which is a quarter turn at zero.
     */
    public static final MathFunction ACOT = TrigFunction.inverse(
            "acot", "Returns the arccotangent of x (inverse cotangent), atan(1/x)", TrigUtils::acot);

    // ==================== Two-Argument Inverse ====================

    /** Two-argument arctangent function. */
    public static final MathFunction ATAN2 = TrigFunction.inverseBinary("atan2",
            "Returns the angle to point (x, y) in current angle units; takes y first, then x (atan2 convention)",
            "y", "x", Math::atan2);

    // ==================== All Functions ====================

    /**
     * Gets all trigonometric functions.
     *
     * @return list of all trig functions
     */
    public static List<MathFunction> all() {
        return List.of(SIN, COS, TAN, SEC, CSC, COT, ASIN, ACOS, ATAN, ASEC, ACSC, ACOT, ATAN2);
    }

}
