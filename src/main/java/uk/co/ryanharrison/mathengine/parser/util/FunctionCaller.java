package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeFunction;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;

import java.util.List;

/**
 * Callback interface for applying functions to arguments.
 */
@FunctionalInterface
public interface FunctionCaller {
    NodeConstant call(NodeFunction function, List<NodeConstant> args, EvaluationContext context);
}
