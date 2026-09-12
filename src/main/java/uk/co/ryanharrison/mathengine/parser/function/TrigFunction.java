package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.ast.NodeDouble;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * Factory for trigonometric functions, which is where angle unit handling lives.
 * <p>
 * Two directions, and the conversion belongs to the direction rather than to the
 * individual function:
 * <ul>
 *     <li>Standard trig (sin, cos, tan): the argument is an angle, read by its own angle
 *     label if it has one, otherwise by the context unit</li>
 *     <li>Inverse trig (asin, acos, atan, atan2): the result is an angle in radians,
 *     expressed in the context unit</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * MathFunction sin = TrigFunction.standard("sin", "Sine function", Math::sin);
 * MathFunction atan = TrigFunction.inverse("atan", "Arctangent function", Math::atan);
 *
 * // An inverse defined only on part of the real line
 * MathFunction asin = TrigFunction.inverse("asin", "Arcsine function", -1.0, 1.0, Math::asin);
 *
 * // An inverse of two arguments
 * MathFunction atan2 = TrigFunction.inverseBinary("atan2", "...", "y", "x", Math::atan2);
 * }</pre>
 *
 * @see FunctionContext#toRadians(double)
 * @see FunctionContext#fromRadians(double)
 */
public final class TrigFunction {

    private TrigFunction() {
    }

    /**
     * Creates a standard trigonometric function, whose argument is an angle.
     * <p>
     * An angle label decides the unit, so {@code sin(90 degrees)} is 1 in either mode.
     *
     * @param fn the math operation, which receives radians
     */
    public static MathFunction standard(String name, String description, DoubleUnaryOperator fn) {
        return builder(name, description, "x")
                .takingUnary()
                .implementedBy((arg, ctx) -> ctx.mapAngle(arg, fn));
    }

    /**
     * Creates an inverse trigonometric function, whose result is an angle.
     *
     * @param fn the math operation, which returns radians
     */
    public static MathFunction inverse(String name, String description, DoubleUnaryOperator fn) {
        return builder(name, description, "x")
                .takingUnary()
                .implementedBy((arg, ctx) -> ctx.mapDouble(arg, value -> ctx.fromRadians(fn.applyAsDouble(value))));
    }

    /**
     * Creates an inverse trigonometric function defined only on {@code [min, max]}, as
     * arcsine and arccosine are. An argument outside the range is a domain error, or NaN
     * under silent validation.
     *
     * @param fn the math operation, which returns radians
     */
    public static MathFunction inverse(String name, String description, double min, double max,
                                       DoubleUnaryOperator fn) {
        return builder(name, description, "x")
                .takingUnary()
                .implementedBy((arg, ctx) -> ctx.mapDouble(arg,
                        value -> ctx.fromRadians(fn.applyAsDouble(ctx.requireInRange(value, min, max)))));
    }

    /**
     * Creates an inverse trigonometric function of two arguments, as arctangent of a
     * point is. The arguments are plain numbers; only the result is an angle.
     *
     * @param fn the math operation, which returns radians
     */
    public static MathFunction inverseBinary(String name, String description, String firstParam,
                                             String secondParam, DoubleBinaryOperator fn) {
        return builder(name, description, firstParam, secondParam)
                .takingBinary()
                .implementedBy((first, second, ctx) -> new NodeDouble(
                        ctx.fromRadians(fn.applyAsDouble(ctx.toDouble(first), ctx.toDouble(second)))));
    }

    private static FunctionBuilder builder(String name, String description, String... params) {
        return FunctionBuilder
                .named(name)
                .describedAs(description)
                .withParams(params)
                .inCategory(MathFunction.Category.TRIGONOMETRIC);
    }
}
