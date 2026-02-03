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

    // Note: Factory methods have been removed. Use FunctionBuilder instead:
    //
    // Old:  AggregateFunction.of("sum", "Sum values", VECTOR, 1, Integer.MAX_VALUE, (args, ctx) -> ...)
    // New:  FunctionBuilder.named("sum")
    //                      .describedAs("Sum values")
    //                      .inCategory(VECTOR)
    //                      .takingVariadic(1)
    //                      .implementedByAggregate((args, ctx) -> ...)
    //
    // See FunctionBuilder for the fluent API with support for aliases, validation, and broadcasting.
}
