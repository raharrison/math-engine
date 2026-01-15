package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;

/**
 * Functional interface for aggregate/variadic functions that operate on all arguments.
 * <p>
 * Unlike {@link UnaryFunction} and {@link BinaryFunction}, aggregate functions receive
 * all arguments at once and decide how to process them. This is used for functions like:
 * <ul>
 *     <li>sum(1, 2, 3) - aggregates all values</li>
 *     <li>max(a, b, c) - finds maximum across all values</li>
 *     <li>concat([1,2], [3,4]) - combines vectors</li>
 *     <li>if(cond, then, else) - conditional evaluation</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Sum of all arguments (flattens vectors)
 * MathFunction sum = AggregateFunction.of("sum", "Sum all values", Category.VECTOR, 1, Integer.MAX_VALUE,
 *     (args, ctx) -> {
 *         double total = 0;
 *         for (NodeConstant arg : ctx.flattenToNumbers(args)) {
 *             total += ctx.toNumber(arg).doubleValue();
 *         }
 *         return new NodeDouble(total);
 *     }
 * );
 *
 * // Conditional with exactly 3 args
 * MathFunction ifFunc = AggregateFunction.of("if", "Conditional", Category.CONDITIONAL, 3, 3,
 *     (args, ctx) -> ctx.toBoolean(args.get(0)) ? args.get(1) : args.get(2)
 * );
 * }</pre>
 *
 * @see MathFunction
 * @see UnaryFunction
 * @see BinaryFunction
 */
@FunctionalInterface
public interface AggregateFunction {

    /**
     * Applies this function to all arguments.
     *
     * @param args all arguments as evaluated NodeConstants
     * @param ctx  the function context for utilities
     * @return the result
     */
    NodeConstant apply(List<NodeConstant> args, FunctionContext ctx);

    // ==================== Factory Methods ====================

    /**
     * Creates an aggregate function with specified arity.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param minArity    minimum number of arguments
     * @param maxArity    maximum number of arguments (use Integer.MAX_VALUE for variadic)
     * @param fn          the function implementation
     * @return a MathFunction wrapping the aggregate function
     */
    static MathFunction of(String name, String description, MathFunction.Category category,
                           int minArity, int maxArity, AggregateFunction fn) {
        return new AggregateFunctionWrapper(name, description, category, List.of(), minArity, maxArity, fn);
    }

    /**
     * Creates an aggregate function with aliases.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param aliases     alternate names
     * @param minArity    minimum number of arguments
     * @param maxArity    maximum number of arguments
     * @param fn          the function implementation
     * @return a MathFunction wrapping the aggregate function
     */
    static MathFunction of(String name, String description, MathFunction.Category category,
                           List<String> aliases, int minArity, int maxArity, AggregateFunction fn) {
        return new AggregateFunctionWrapper(name, description, category, aliases, minArity, maxArity, fn);
    }

    /**
     * Creates a variadic aggregate function (1 to unlimited arguments).
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param fn          the function implementation
     * @return a MathFunction wrapping the aggregate function
     */
    static MathFunction variadic(String name, String description, MathFunction.Category category,
                                 AggregateFunction fn) {
        return new AggregateFunctionWrapper(name, description, category, List.of(), 1, Integer.MAX_VALUE, fn);
    }

    /**
     * Creates a variadic aggregate function with aliases.
     *
     * @param name        function name
     * @param description function description
     * @param category    function category
     * @param aliases     alternate names
     * @param fn          the function implementation
     * @return a MathFunction wrapping the aggregate function
     */
    static MathFunction variadic(String name, String description, MathFunction.Category category,
                                 List<String> aliases, AggregateFunction fn) {
        return new AggregateFunctionWrapper(name, description, category, aliases, 1, Integer.MAX_VALUE, fn);
    }
}
