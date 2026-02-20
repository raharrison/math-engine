package uk.co.ryanharrison.mathengine.parser.function;

import java.util.function.DoubleUnaryOperator;

/**
 * Factory for creating trigonometric functions with automatic angle unit handling.
 * <p>
 * This abstraction handles the common patterns in trigonometric functions:
 * <ul>
 *     <li>Standard trig (sin, cos, tan): input angle is converted from context unit to radians</li>
 *     <li>Inverse trig (asin, acos, atan): output radians are converted to context unit</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Standard trig function - input is an angle
 * MathFunction sin = TrigFunction.standard("sin", "Sine function", Math::sin);
 * MathFunction cos = TrigFunction.standard("cos", "Cosine function", Math::cos);
 *
 * // Inverse trig function - output is an angle
 * MathFunction asin = TrigFunction.inverse("asin", "Arcsine function", Math::asin);
 * MathFunction atan = TrigFunction.inverse("atan", "Arctangent function", Math::atan);
 * }</pre>
 *
 * @see UnaryFunction
 * @see FunctionContext#toRadians(double)
 * @see FunctionContext#fromRadians(double)
 */
public final class TrigFunction {

    private TrigFunction() {
    }

    /**
     * Creates a standard trigonometric function where input is an angle.
     * <p>
     * The input value is converted from the context's angle unit (degrees or radians)
     * to radians before applying the function.
     *
     * @param name        function name
     * @param description function description
     * @param fn          the math operation (receives radians)
     * @return a MathFunction that handles angle conversion
     */
    public static MathFunction standard(String name, String description, DoubleUnaryOperator fn) {
        return FunctionBuilder
                .named(name)
                .describedAs(description)
                .withParams("x")
                .inCategory(MathFunction.Category.TRIGONOMETRIC)
                .takingUnary()
                .noBroadcasting() // broadcasts internally via ctx.applyWithBroadcasting()
                .implementedBy((arg, ctx) ->
                        ctx.applyWithBroadcasting(arg, value ->
                                fn.applyAsDouble(ctx.toRadians(value))));
    }

    /**
     * Creates an inverse trigonometric function where output is an angle.
     * <p>
     * The function computes a result in radians, which is then converted to
     * the context's angle unit (degrees or radians).
     *
     * @param name        function name
     * @param description function description
     * @param fn          the math operation (returns radians)
     * @return a MathFunction that handles angle conversion
     */
    public static MathFunction inverse(String name, String description, DoubleUnaryOperator fn) {
        return FunctionBuilder
                .named(name)
                .describedAs(description)
                .withParams("x")
                .inCategory(MathFunction.Category.TRIGONOMETRIC)
                .takingUnary()
                .noBroadcasting() // broadcasts internally via ctx.applyWithBroadcasting()
                .implementedBy((arg, ctx) ->
                        ctx.applyWithBroadcasting(arg, value ->
                                ctx.fromRadians(fn.applyAsDouble(value))));
    }
}
