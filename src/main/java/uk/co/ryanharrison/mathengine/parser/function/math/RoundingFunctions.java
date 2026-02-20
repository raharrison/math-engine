package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.ROUNDING;
import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.UTILITY;

/**
 * Collection of rounding and basic mathematical functions.
 */
public final class RoundingFunctions {

    private RoundingFunctions() {
    }

    // ==================== Rounding Functions ====================

    /**
     * Floor function (round down)
     */
    public static final MathFunction FLOOR = FunctionBuilder
            .named("floor")
            .describedAs("Returns the largest integer less than or equal to x")
            .withParams("x")
            .inCategory(ROUNDING)
            .takingUnary()
            .implementedByDouble(Math::floor);

    /**
     * Ceiling function (round up)
     */
    public static final MathFunction CEIL = FunctionBuilder
            .named("ceil")
            .describedAs("Returns the smallest integer greater than or equal to x")
            .withParams("x")
            .inCategory(ROUNDING)
            .takingUnary()
            .implementedByDouble(Math::ceil);

    /**
     * Round to nearest integer
     */
    public static final MathFunction ROUND = FunctionBuilder
            .named("round")
            .describedAs("Rounds x to the nearest integer")
            .withParams("x")
            .inCategory(ROUNDING)
            .takingUnary()
            .implementedByDouble(x -> (double) Math.round(x));

    /**
     * Truncate toward zero
     */
    public static final MathFunction TRUNC = FunctionBuilder
            .named("trunc")
            .describedAs("Truncates x toward zero (removes fractional part)")
            .withParams("x")
            .inCategory(ROUNDING)
            .takingUnary()
            .implementedByDouble(x -> x < 0 ? Math.ceil(x) : Math.floor(x));

    /**
     * Round to specified decimal places
     */
    public static final MathFunction ROUNDN = FunctionBuilder
            .named("roundn")
            .describedAs("Rounds x to the specified number of decimal places")
            .withParams("x", "places")
            .inCategory(ROUNDING)
            .takingBinary()
            .implementedBy((x, n, ctx) -> {
                double xVal = ctx.toNumber(x).doubleValue();
                int places = (int) ctx.toNumber(n).doubleValue();
                double factor = Math.pow(10, places);
                return new NodeDouble(Math.round(xVal * factor) / factor);
            });

    // ==================== Sign and Absolute Value ====================

    /**
     * Absolute value
     */
    public static final MathFunction ABS = FunctionBuilder
            .named("abs")
            .describedAs("Returns the absolute value of x")
            .withParams("x")
            .inCategory(UTILITY)
            .takingUnary()
            .noBroadcasting() // broadcasts internally via ctx.applyWithTypePreservation()
            .implementedBy((arg, ctx) -> ctx.applyWithTypePreservation(arg, r -> r.abs(), Math::abs));

    /**
     * Sign function (-1, 0, or 1)
     */
    public static final MathFunction SIGN = FunctionBuilder
            .named("sign")
            .describedAs("Returns -1, 0, or 1 depending on the sign of x")
            .withParams("x")
            .inCategory(UTILITY)
            .takingUnary()
            .implementedBy((arg, ctx) -> {
                double x = ctx.toNumber(arg).doubleValue();
                int sign = x < 0 ? -1 : x > 0 ? 1 : 0;
                return new NodeRational(sign);
            });

    /**
     * Copy sign from y to x
     */
    public static final MathFunction COPYSIGN = FunctionBuilder
            .named("copysign")
            .describedAs("Returns x with the sign of y")
            .withParams("x", "y")
            .inCategory(UTILITY)
            .takingBinary()
            .implementedByDouble(Math::copySign);

    // ==================== Modulo Functions ====================

    /**
     * Floating-point modulo
     */
    public static final MathFunction FMOD = FunctionBuilder
            .named("fmod")
            .describedAs("Returns the floating-point remainder of x divided by y")
            .withParams("x", "y")
            .inCategory(UTILITY)
            .takingBinary()
            .implementedBy((x, y, ctx) -> {
                double xVal = ctx.toNumber(x).doubleValue();
                double yVal = ctx.toNumber(y).doubleValue();
                return new NodeDouble(xVal % yVal);
            });

    /**
     * IEEE remainder
     */
    public static final MathFunction REMAINDER = FunctionBuilder
            .named("remainder")
            .describedAs("Returns the IEEE remainder of x divided by y")
            .withParams("x", "y")
            .inCategory(UTILITY)
            .takingBinary()
            .implementedBy((x, y, ctx) -> {
                double xVal = ctx.toNumber(x).doubleValue();
                double yVal = ctx.toNumber(y).doubleValue();
                return new NodeDouble(Math.IEEEremainder(xVal, yVal));
            });

    // ==================== Hypotenuse ====================

    /**
     * Hypotenuse (sqrt(x^2 + y^2))
     */
    public static final MathFunction HYPOT = FunctionBuilder
            .named("hypot")
            .describedAs("Returns sqrt(x² + y²) without intermediate overflow")
            .withParams("x", "y")
            .inCategory(UTILITY)
            .takingBinary()
            .implementedBy((x, y, ctx) -> {
                double xVal = ctx.toNumber(x).doubleValue();
                double yVal = ctx.toNumber(y).doubleValue();
                return new NodeDouble(Math.hypot(xVal, yVal));
            });

    // ==================== All Functions ====================

    /**
     * Gets all rounding and basic math functions.
     */
    public static List<MathFunction> all() {
        return List.of(FLOOR, CEIL, ROUND, TRUNC, ROUNDN, ABS, SIGN, COPYSIGN, FMOD, REMAINDER, HYPOT);
    }

    /**
     * Gets only rounding functions.
     */
    public static List<MathFunction> rounding() {
        return List.of(FLOOR, CEIL, ROUND, TRUNC, ROUNDN);
    }
}
