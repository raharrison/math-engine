package uk.co.ryanharrison.mathengine.parser.function.trig;

import uk.co.ryanharrison.mathengine.parser.function.BinaryFunction;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.TrigFunction;
import uk.co.ryanharrison.mathengine.parser.function.UnaryFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.utils.TrigUtils;

import java.util.List;

/**
 * Collection of trigonometric functions.
 * <p>
 * All functions respect the angle unit setting in the context
 * (radians or degrees). Input conversion happens automatically.
 */
public final class TrigonometricFunctions {

    private TrigonometricFunctions() {
    }

    // ==================== Standard Trig Functions ====================

    /**
     * Sine function
     */
    public static final MathFunction SIN = TrigFunction.standard("sin", "Sine", Math::sin);

    /**
     * Cosine function
     */
    public static final MathFunction COS = TrigFunction.standard("cos", "Cosine", Math::cos);

    /**
     * Tangent function
     */
    public static final MathFunction TAN = TrigFunction.standard("tan", "Tangent", Math::tan);

    // ==================== Inverse Trig Functions ====================

    /**
     * Arcsine function
     */
    public static final MathFunction ASIN = UnaryFunction.of("asin", "Arcsine", MathFunction.Category.TRIGONOMETRIC,
            (arg, ctx) -> {
                double value = ctx.toNumber(arg).doubleValue();
                if (value < -1.0 || value > 1.0) {
                    throw new IllegalArgumentException("asin requires input in range [-1, 1], got: " + value);
                }
                double radians = Math.asin(value);
                return new NodeDouble(ctx.fromRadians(radians));
            });

    /**
     * Arccosine function
     */
    public static final MathFunction ACOS = UnaryFunction.of("acos", "Arccosine", MathFunction.Category.TRIGONOMETRIC,
            (arg, ctx) -> {
                double value = ctx.toNumber(arg).doubleValue();
                if (value < -1.0 || value > 1.0) {
                    throw new IllegalArgumentException("acos requires input in range [-1, 1], got: " + value);
                }
                double radians = Math.acos(value);
                return new NodeDouble(ctx.fromRadians(radians));
            });

    /**
     * Arctangent function
     */
    public static final MathFunction ATAN = TrigFunction.inverse("atan", "Arctangent", Math::atan);

    /**
     * Two-argument arctangent function
     */
    public static final MathFunction ATAN2 = BinaryFunction.of(
            "atan2", "Two-argument arctangent", MathFunction.Category.TRIGONOMETRIC,
            (y, x, ctx) -> {
                double yVal = ctx.toNumber(y).doubleValue();
                double xVal = ctx.toNumber(x).doubleValue();
                double radians = Math.atan2(yVal, xVal);
                return new NodeDouble(ctx.fromRadians(radians));
            }
    );

    // ==================== Reciprocal Trig Functions ====================

    /**
     * Secant function (1/cos)
     */
    public static final MathFunction SEC = TrigFunction.standard("sec", "Secant", TrigUtils::sec);

    /**
     * Cosecant function (1/sin)
     */
    public static final MathFunction CSC = TrigFunction.standard("csc", "Cosecant", TrigUtils::cosec);

    /**
     * Cotangent function (1/tan)
     */
    public static final MathFunction COT = TrigFunction.standard("cot", "Cotangent", TrigUtils::cot);

    // ==================== All Functions ====================

    /**
     * Gets all trigonometric functions.
     *
     * @return list of all trig functions
     */
    public static List<MathFunction> all() {
        return List.of(SIN, COS, TAN, ASIN, ACOS, ATAN, ATAN2, SEC, CSC, COT);
    }

    /**
     * Gets standard trig functions (sin, cos, tan).
     *
     * @return list of standard trig functions
     */
    public static List<MathFunction> standard() {
        return List.of(SIN, COS, TAN);
    }

    /**
     * Gets inverse trig functions (asin, acos, atan).
     *
     * @return list of inverse trig functions
     */
    public static List<MathFunction> inverse() {
        return List.of(ASIN, ACOS, ATAN, ATAN2);
    }
}
