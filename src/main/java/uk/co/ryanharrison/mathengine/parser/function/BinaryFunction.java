package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

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

    // Note: Factory methods have been removed. Use FunctionBuilder instead:
    //
    // Old:  BinaryFunction.of("pow", "Power function", EXPONENTIAL, (base, exp, ctx) -> ...)
    // New:  FunctionBuilder.named("pow")
    //                      .describedAs("Power function")
    //                      .inCategory(EXPONENTIAL)
    //                      .takingBinary()
    //                      .implementedBy((base, exp, ctx) -> ...)
    //
    // See FunctionBuilder for the fluent API with support for aliases, validation, and broadcasting.
}
