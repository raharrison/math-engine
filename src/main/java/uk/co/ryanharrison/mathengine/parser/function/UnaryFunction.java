package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

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

    // Note: Factory methods have been removed. Use FunctionBuilder instead:
    //
    // Old:  UnaryFunction.ofDouble("abs", "Absolute value", UTILITY, Math::abs)
    // New:  FunctionBuilder.named("abs")
    //                      .describedAs("Absolute value")
    //                      .inCategory(UTILITY)
    //                      .takingUnary()
    //                      .implementedByDouble(Math::abs)
    //
    // See FunctionBuilder for the fluent API with support for aliases, validation, and broadcasting.
}
