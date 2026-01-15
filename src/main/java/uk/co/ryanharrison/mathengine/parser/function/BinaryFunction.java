package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

/**
 * Functional interface for binary (two-argument) math functions.
 * <p>
 * This interface represents the core operation of a binary function: taking two
 * {@link NodeConstant} arguments and returning a result.
 *
 * <h2>Usage Patterns:</h2>
 *
 * <h3>1. Simple double operations:</h3>
 * <pre>{@code
 * MathFunction pow = BinaryFunction.ofDouble("pow", "Power function", Category.EXPONENTIAL, Math::pow);
 * MathFunction hypot = BinaryFunction.ofDouble("hypot", "Hypotenuse", Category.UTILITY, Math::hypot);
 * }</pre>
 *
 * <h3>2. Type-aware operations:</h3>
 * <pre>{@code
 * // Division that preserves rationals
 * MathFunction div = BinaryFunction.of("div", "Integer division", Category.UTILITY, (a, b, ctx) -> {
 *     if (a instanceof NodeRational r1 && b instanceof NodeRational r2) {
 *         return new NodeRational(r1.getValue().divide(r2.getValue()));
 *     }
 *     return new NodeDouble(ctx.toNumber(a).doubleValue() / ctx.toNumber(b).doubleValue());
 * });
 * }</pre>
 *
 * @see MathFunction
 * @see FunctionExecutor
 */
@FunctionalInterface
public interface BinaryFunction {

    /**
     * Applies this function to two arguments.
     *
     * @param first  the first argument
     * @param second the second argument
     * @param ctx    the function context for utilities
     * @return the result
     */
    NodeConstant apply(NodeConstant first, NodeConstant second, FunctionContext ctx);

    // ==================== Factory Methods ====================

    /**
     * Creates a binary function from a lambda.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param fn          the function implementation
     * @return a MathFunction wrapping the binary function
     */
    static MathFunction of(String name, String description, MathFunction.Category category,
                           BinaryFunction fn) {
        return new BinaryFunctionWrapper(name, description, category, List.of(), fn);
    }

    /**
     * Creates a binary function with aliases.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param aliases     alternate names
     * @param fn          the function implementation
     * @return a MathFunction wrapping the binary function
     */
    static MathFunction of(String name, String description, MathFunction.Category category,
                           List<String> aliases, BinaryFunction fn) {
        return new BinaryFunctionWrapper(name, description, category, aliases, fn);
    }

    /**
     * Creates a binary function from a simple double operation.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param fn          the double operation
     * @return a MathFunction wrapping the operation
     */
    static MathFunction ofDouble(String name, String description, MathFunction.Category category,
                                 DoubleBinaryOperator fn) {
        return new BinaryFunctionWrapper(name, description, category, List.of(), (a, b, ctx) -> {
            double x = ctx.toNumber(a).doubleValue();
            double y = ctx.toNumber(b).doubleValue();
            return new NodeDouble(fn.applyAsDouble(x, y));
        });
    }

    /**
     * Creates a binary function from a double operation with aliases.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param aliases     alternate names
     * @param fn          the double operation
     * @return a MathFunction wrapping the operation
     */
    static MathFunction ofDouble(String name, String description, MathFunction.Category category,
                                 List<String> aliases, DoubleBinaryOperator fn) {
        return new BinaryFunctionWrapper(name, description, category, aliases, (a, b, ctx) -> {
            double x = ctx.toNumber(a).doubleValue();
            double y = ctx.toNumber(b).doubleValue();
            return new NodeDouble(fn.applyAsDouble(x, y));
        });
    }
}
