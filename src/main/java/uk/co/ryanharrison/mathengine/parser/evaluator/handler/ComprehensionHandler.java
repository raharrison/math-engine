package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.evaluator.NodeEvaluator;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates list comprehensions such as {@code {x^2 for x in 1..10 if x % 2 == 0}}.
 * Multiple {@code for} clauses nest, with later clauses seeing earlier bindings.
 */
public final class ComprehensionHandler {

    private final MathEngineConfig config;
    private final NodeEvaluator evaluator;

    public ComprehensionHandler(MathEngineConfig config, NodeEvaluator evaluator) {
        this.config = config;
        this.evaluator = evaluator;
    }

    /**
     * @throws EvaluationException if comprehensions are disabled or the result is too large
     * @throws TypeError           if an iterable is not iterable
     */
    public NodeConstant evaluate(NodeComprehension node, EvaluationContext context) {
        if (!config.comprehensionsEnabled()) {
            throw new EvaluationException("List comprehensions are disabled in current configuration");
        }

        var results = new ArrayList<Node>();
        evaluateIterators(node, context, new HashMap<>(), 0, results);

        if (results.size() > config.maxVectorSize()) {
            throw new EvaluationException("Comprehension produced " + results.size() +
                    " elements, exceeding maximum allowed size of " + config.maxVectorSize());
        }
        return new NodeVector(results.toArray(new Node[0]));
    }

    private void evaluateIterators(NodeComprehension node, EvaluationContext context,
                                   Map<String, NodeConstant> bindings, int iteratorIndex, List<Node> results) {

        List<NodeComprehension.Iterator> iterators = node.getIterators();

        if (iteratorIndex >= iterators.size()) {
            EvaluationContext scope = context.withBindings(bindings);
            if (node.hasCondition() && !TypeCoercion.toBoolean(evaluator.evaluate(node.getCondition(), scope))) {
                return;
            }
            results.add(evaluator.evaluate(node.getExpression(), scope));
            return;
        }

        NodeComprehension.Iterator iterator = iterators.get(iteratorIndex);
        EvaluationContext scope = context.withBindings(bindings);
        NodeConstant iterableValue = evaluator.evaluate(iterator.iterable(), scope);

        for (NodeConstant item : toIterable(iterableValue, scope)) {
            var nested = new HashMap<>(bindings);
            nested.put(iterator.variable(), item);
            evaluateIterators(node, context, nested, iteratorIndex + 1, results);
        }
    }

    private Iterable<NodeConstant> toIterable(NodeConstant value, EvaluationContext context) {
        if (value instanceof NodeVector vector) {
            var items = new ArrayList<NodeConstant>(vector.size());
            for (int i = 0; i < vector.size(); i++) {
                items.add(evaluator.evaluate(vector.getElement(i), context));
            }
            return items;
        }
        if (value instanceof NodeRange range) {
            return range::iterator;
        }
        throw new TypeError("Cannot iterate over: " + value.typeName());
    }
}
