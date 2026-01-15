package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;

import java.util.List;
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
        return standard(name, description, List.of(), fn);
    }

    /**
     * Creates a standard trigonometric function with aliases.
     *
     * @param name        function name
     * @param description function description
     * @param aliases     alternate names
     * @param fn          the math operation (receives radians)
     * @return a MathFunction that handles angle conversion
     */
    public static MathFunction standard(String name, String description, List<String> aliases,
                                        DoubleUnaryOperator fn) {
        return UnaryFunction.of(name, description, MathFunction.Category.TRIGONOMETRIC, aliases,
                (arg, ctx) -> {
                    double angle = ctx.toNumber(arg).doubleValue();
                    double radians = ctx.toRadians(angle);
                    return new NodeDouble(fn.applyAsDouble(radians));
                });
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
        return inverse(name, description, List.of(), fn);
    }

    /**
     * Creates an inverse trigonometric function with aliases.
     *
     * @param name        function name
     * @param description function description
     * @param aliases     alternate names
     * @param fn          the math operation (returns radians)
     * @return a MathFunction that handles angle conversion
     */
    public static MathFunction inverse(String name, String description, List<String> aliases,
                                       DoubleUnaryOperator fn) {
        return UnaryFunction.of(name, description, MathFunction.Category.TRIGONOMETRIC, aliases,
                (arg, ctx) -> {
                    double value = ctx.toNumber(arg).doubleValue();
                    double radians = fn.applyAsDouble(value);
                    return new NodeDouble(ctx.fromRadians(radians));
                });
    }
}
