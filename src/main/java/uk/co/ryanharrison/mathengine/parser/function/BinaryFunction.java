package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * The body of a two-argument function. Broadcasting over vectors and matrices
 * is applied by {@link FunctionBuilder}, so implementations only handle scalars.
 */
@FunctionalInterface
public interface BinaryFunction {

    NodeConstant apply(NodeConstant first, NodeConstant second, FunctionContext ctx);
}
