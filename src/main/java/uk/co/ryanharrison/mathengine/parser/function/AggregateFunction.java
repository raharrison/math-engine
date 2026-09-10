package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;

/**
 * The body of a function that sees all of its arguments at once, such as
 * {@code sum}, {@code max} or {@code concat}. No broadcasting is applied.
 */
@FunctionalInterface
public interface AggregateFunction {

    NodeConstant apply(List<NodeConstant> args, FunctionContext ctx);
}
