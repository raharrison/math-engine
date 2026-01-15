package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.parser.function.BinaryFunction;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.UnaryFunction;
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
    public static final MathFunction FLOOR = UnaryFunction.ofDouble("floor", "Floor (round down)", ROUNDING, Math::floor);

    /**
     * Ceiling function (round up)
     */
    public static final MathFunction CEIL = UnaryFunction.ofDouble("ceil", "Ceiling (round up)", ROUNDING, Math::ceil);

    /**
     * Round to nearest integer
     */
    public static final MathFunction ROUND = UnaryFunction.ofDouble("round", "Round to nearest integer", ROUNDING, x -> (double) Math.round(x));

    /**
     * Truncate toward zero
     */
    public static final MathFunction TRUNC = UnaryFunction.ofDouble("trunc", "Truncate toward zero", ROUNDING, x -> x < 0 ? Math.ceil(x) : Math.floor(x));

    /**
     * Round to specified decimal places
     */
    public static final MathFunction ROUNDN = BinaryFunction.of("roundn", "Round to n decimal places", ROUNDING, (x, n, ctx) -> {
        double xVal = ctx.toNumber(x).doubleValue();
        int places = (int) ctx.toNumber(n).doubleValue();
        double factor = Math.pow(10, places);
        return new NodeDouble(Math.round(xVal * factor) / factor);
    });

    // ==================== Sign and Absolute Value ====================

    /**
     * Absolute value
     */
    public static final MathFunction ABS = UnaryFunction.of("abs", "Absolute value", UTILITY, (arg, ctx) ->
            ctx.applyWithTypePreservation(arg, r -> r.abs(), Math::abs));

    /**
     * Sign function (-1, 0, or 1)
     */
    public static final MathFunction SIGN = UnaryFunction.of("sign", "Sign function", UTILITY, (arg, ctx) -> {
        double x = ctx.toNumber(arg).doubleValue();
        int sign = x < 0 ? -1 : x > 0 ? 1 : 0;
        return new NodeRational(sign);
    });

    /**
     * Copy sign from y to x
     */
    public static final MathFunction COPYSIGN = BinaryFunction.ofDouble("copysign", "Copy sign from y to x", UTILITY, Math::copySign);

    // ==================== Modulo Functions ====================

    /**
     * Floating-point modulo
     */
    public static final MathFunction FMOD = BinaryFunction.ofDouble("fmod", "Floating-point modulo", UTILITY, (x, y) -> x % y);

    /**
     * IEEE remainder
     */
    public static final MathFunction REMAINDER = BinaryFunction.ofDouble("remainder", "IEEE remainder", UTILITY, Math::IEEEremainder);

    // ==================== Hypotenuse ====================

    /**
     * Hypotenuse (sqrt(x^2 + y^2))
     */
    public static final MathFunction HYPOT = BinaryFunction.ofDouble("hypot", "Hypotenuse", UTILITY, Math::hypot);

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
