package uk.co.ryanharrison.mathengine.parser.function.trig;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.TrigFunction;
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
    public static final MathFunction SIN = TrigFunction.standard("sin", "Returns the sine of x", Math::sin);

    /**
     * Cosine function
     */
    public static final MathFunction COS = TrigFunction.standard("cos", "Returns the cosine of x", Math::cos);

    /**
     * Tangent function
     */
    public static final MathFunction TAN = TrigFunction.standard("tan", "Returns the tangent of x", Math::tan);

    // ==================== Inverse Trig Functions ====================

    /**
     * Arcsine function
     */
    public static final MathFunction ASIN = FunctionBuilder
            .named("asin")
            .describedAs("Returns the arcsine of x (inverse sine)")
            .withParams("x")
            .inCategory(MathFunction.Category.TRIGONOMETRIC)
            .takingUnary()
            .noBroadcasting() // broadcasts internally via ctx.applyWithBroadcasting()
            .implementedBy((arg, ctx) ->
                    ctx.applyWithBroadcasting(arg, value -> {
                        return ctx.fromRadians(Math.asin(ctx.requireInRange(value, -1.0, 1.0)));
                    }));

    /**
     * Arccosine function
     */
    public static final MathFunction ACOS = FunctionBuilder
            .named("acos")
            .describedAs("Returns the arccosine of x (inverse cosine)")
            .withParams("x")
            .inCategory(MathFunction.Category.TRIGONOMETRIC)
            .takingUnary()
            .noBroadcasting() // broadcasts internally via ctx.applyWithBroadcasting()
            .implementedBy((arg, ctx) ->
                    ctx.applyWithBroadcasting(arg, value -> {
                        return ctx.fromRadians(Math.acos(ctx.requireInRange(value, -1.0, 1.0)));
                    }));

    /**
     * Arctangent function
     */
    public static final MathFunction ATAN = TrigFunction.inverse("atan", "Returns the arctangent of x (inverse tangent)", Math::atan);

    /**
     * Two-argument arctangent function
     */
    public static final MathFunction ATAN2 = FunctionBuilder
            .named("atan2")
            .describedAs("Returns the angle to point (x, y) in current angle units; takes y first, then x (atan2 convention)")
            .withParams("y", "x")
            .inCategory(MathFunction.Category.TRIGONOMETRIC)
            .takingBinary()
            .implementedBy((y, x, ctx) -> {
                double yVal = ctx.toNumber(y).doubleValue();
                double xVal = ctx.toNumber(x).doubleValue();
                double radians = Math.atan2(yVal, xVal);
                return new NodeDouble(ctx.fromRadians(radians));
            });

    // ==================== Reciprocal Trig Functions ====================

    /**
     * Secant function (1/cos)
     */
    public static final MathFunction SEC = TrigFunction.standard("sec", "Returns the secant of x (1/cos(x))", TrigUtils::sec);

    /**
     * Cosecant function (1/sin)
     */
    public static final MathFunction CSC = TrigFunction.standard("csc", "Returns the cosecant of x (1/sin(x))", TrigUtils::cosec);

    /**
     * Cotangent function (1/tan)
     */
    public static final MathFunction COT = TrigFunction.standard("cot", "Returns the cotangent of x (1/tan(x))", TrigUtils::cot);

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
