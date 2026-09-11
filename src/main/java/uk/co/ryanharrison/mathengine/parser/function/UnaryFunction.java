package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;

/**
 * The body of a single-argument function. Broadcasting over vectors and matrices
 * is applied by {@link FunctionBuilder}, so implementations only handle scalars.
 *
 * <pre>{@code
 * FunctionBuilder.named("ln")
 *     .inCategory(LOGARITHMIC)
 *     .takingUnary()
 *     .implementedBy((arg, ctx) -> new NodeDouble(Math.log(ctx.requirePositive(ctx.toDouble(arg)))));
 * }</pre>
 */
@FunctionalInterface
public interface UnaryFunction {

    NodeConstant apply(NodeConstant arg, FunctionContext ctx);
}
