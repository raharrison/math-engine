package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Functional interface for unary (single-argument) math functions.
 * <p>
 * This interface represents the core operation of a unary function: taking a single
 * {@link NodeConstant} and returning a result. The {@link FunctionExecutor} handles
 * all broadcasting logic (applying to vectors, matrices, nested structures).
 *
 * <h2>Usage Patterns:</h2>
 *
 * <h3>1. Simple double operations (most common):</h3>
 * <pre>{@code
 * // Using the factory method for double->double functions
 * MathFunction exp = UnaryFunction.ofDouble("exp", "Natural exponential", Category.EXPONENTIAL, Math::exp);
 * MathFunction sqrt = UnaryFunction.ofDouble("sqrt", "Square root", Category.EXPONENTIAL, Math::sqrt);
 * }</pre>
 *
 * <h3>2. Type-preserving operations:</h3>
 * <pre>{@code
 * // Absolute value that preserves rationals
 * MathFunction abs = UnaryFunction.of("abs", "Absolute value", Category.UTILITY, (arg, ctx) -> {
 *     if (arg instanceof NodeRational rat) {
 *         return new NodeRational(rat.getValue().abs());
 *     }
 *     return new NodeDouble(Math.abs(ctx.toNumber(arg).doubleValue()));
 * });
 * }</pre>
 *
 * <h3>3. Functions with domain validation:</h3>
 * <pre>{@code
 * MathFunction ln = UnaryFunction.of("ln", "Natural logarithm", Category.LOGARITHMIC, (arg, ctx) -> {
 *     double x = ctx.toNumber(arg).doubleValue();
 *     if (x <= 0) throw new IllegalArgumentException("ln requires positive input");
 *     return new NodeDouble(Math.log(x));
 * });
 * }</pre>
 *
 * @see MathFunction
 * @see FunctionExecutor
 */
@FunctionalInterface
public interface UnaryFunction {

    /**
     * Applies this function to a single argument.
     *
     * @param arg the input constant
     * @param ctx the function context for utilities
     * @return the result
     */
    NodeConstant apply(NodeConstant arg, FunctionContext ctx);

    // ==================== Factory Methods ====================

    /**
     * Creates a unary function from a lambda.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param fn          the function implementation
     * @return a MathFunction wrapping the unary function
     */
    static MathFunction of(String name, String description, MathFunction.Category category,
                           UnaryFunction fn) {
        return new UnaryFunctionWrapper(name, description, category, List.of(), fn, true);
    }

    /**
     * Creates a unary function with aliases.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param aliases     alternate names
     * @param fn          the function implementation
     * @return a MathFunction wrapping the unary function
     */
    static MathFunction of(String name, String description, MathFunction.Category category,
                           List<String> aliases, UnaryFunction fn) {
        return new UnaryFunctionWrapper(name, description, category, aliases, fn, true);
    }

    /**
     * Creates a unary function from a lambda with broadcasting control.
     *
     * @param name                 function name
     * @param description          function description
     * @param category             function category
     * @param fn                   the function implementation
     * @param supportsBroadcasting whether to broadcast over vectors/matrices
     * @return a MathFunction wrapping the unary function
     */
    static MathFunction of(String name, String description, MathFunction.Category category,
                           UnaryFunction fn, boolean supportsBroadcasting) {
        return new UnaryFunctionWrapper(name, description, category, List.of(), fn, supportsBroadcasting);
    }

    /**
     * Creates a unary function with aliases and broadcasting control.
     *
     * @param name                 function name
     * @param description          function description
     * @param category             function category
     * @param aliases              alternate names
     * @param fn                   the function implementation
     * @param supportsBroadcasting whether to broadcast over vectors/matrices
     * @return a MathFunction wrapping the unary function
     */
    static MathFunction of(String name, String description, MathFunction.Category category,
                           List<String> aliases, UnaryFunction fn, boolean supportsBroadcasting) {
        return new UnaryFunctionWrapper(name, description, category, aliases, fn, supportsBroadcasting);
    }

    /**
     * Creates a unary function from a simple double operation.
     * <p>
     * This is the most common case: a function that takes a double and returns a double.
     * The wrapper handles converting any numeric node to double.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param fn          the double operation
     * @return a MathFunction wrapping the operation
     */
    static MathFunction ofDouble(String name, String description, MathFunction.Category category,
                                 DoubleUnaryOperator fn) {
        return new DoubleUnaryFunctionWrapper(name, description, category, List.of(), fn);
    }

    /**
     * Creates a unary function from a double operation with aliases.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param aliases     alternate names
     * @param fn          the double operation
     * @return a MathFunction wrapping the operation
     */
    static MathFunction ofDouble(String name, String description, MathFunction.Category category,
                                 List<String> aliases, DoubleUnaryOperator fn) {
        return new DoubleUnaryFunctionWrapper(name, description, category, aliases, fn);
    }
}
