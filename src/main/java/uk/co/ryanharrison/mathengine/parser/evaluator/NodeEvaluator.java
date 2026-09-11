package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.ast.Node;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;

/**
 * Evaluates a node in an explicit scope. Handlers receive this instead of a
 * back-reference to the evaluator, which keeps the scope an argument rather
 * than shared mutable state.
 */
@FunctionalInterface
public interface NodeEvaluator {

    NodeConstant evaluate(Node node, EvaluationContext context);
}
