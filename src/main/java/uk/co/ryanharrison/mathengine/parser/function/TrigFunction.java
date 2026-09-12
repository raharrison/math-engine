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
 *     <li>Standard trig (sin, cos, tan, sec, csc, cot): the argument is an angle, read by
 *     its own angle label if it has one, otherwise by the context unit</li>
 *     <li>Inverse trig (asin, acos, atan, asec, acsc, acot, atan2): the result is an
 *     angle in radians, expressed in the context unit</li>
 * </ul>
 * <p>
 * A restricted domain belongs to the maths, not to this factory: the routines in
 * {@link uk.co.ryanharrison.mathengine.utils.TrigUtils} police their own arguments, and
 * {@link FunctionContext#checkingDomain} reports that as the engine's own domain error.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * MathFunction sin = TrigFunction.standard("sin", "Sine function", Math::sin);
 * MathFunction atan = TrigFunction.inverse("atan", "Arctangent function", Math::atan);
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
                .implementedBy((arg, ctx) -> ctx.mapAngle(arg, ctx.checkingDomain(fn)));
    }

    /**
     * Creates an inverse trigonometric function, whose result is an angle.
     *
     * @param fn the math operation, which returns radians
     */
    public static MathFunction inverse(String name, String description, DoubleUnaryOperator fn) {
        return builder(name, description, "x")
                .takingUnary()
                .implementedBy((arg, ctx) -> ctx.mapDouble(arg,
                        ctx.checkingDomain(fn).andThen(ctx::fromRadians)));
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
