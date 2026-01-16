package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeFunction;

import java.util.List;

/**
 * Callback interface for applying functions to arguments.
 */
@FunctionalInterface
public interface FunctionCaller {
    NodeConstant call(NodeFunction function, List<NodeConstant> args, EvaluationContext context);
}
