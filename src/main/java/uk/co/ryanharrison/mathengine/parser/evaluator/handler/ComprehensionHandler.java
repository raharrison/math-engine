package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Handles list comprehension evaluation.
 * <p>
 * List comprehensions provide a concise way to create vectors by applying
 * an expression to each element of an iterable, optionally filtering with
 * a condition.
 *
 * <h2>Syntax:</h2>
 * <pre>
 * {expression for variable in iterable}
 * {expression for variable in iterable if condition}
 * </pre>
 *
 * <h2>Examples:</h2>
 * <ul>
 *     <li>{@code {x^2 for x in 1..10}} - squares of 1 to 10</li>
 *     <li>{@code {x for x in 1..20 if x % 2 == 0}} - even numbers from 1 to 20</li>
 *     <li>{@code {x*2 for x in {1, 3, 5}}} - double each element</li>
 * </ul>
 */
public final class ComprehensionHandler {

    private final MathEngineConfig config;
    private final Function<Node, NodeConstant> evaluator;
    private final Function<EvaluationContext, EvaluationContext> contextPusher;
    private final BiConsumer<EvaluationContext, EvaluationContext> contextPopper;

    /**
     * Creates a new comprehension handler.
     *
     * @param config        the engine configuration
     * @param evaluator     function to evaluate nodes
     * @param contextPusher function to push a new context and return the old one
     * @param contextPopper consumer to pop/restore context (new, old)
     */
    public ComprehensionHandler(
            MathEngineConfig config,
            Function<Node, NodeConstant> evaluator,
            Function<EvaluationContext, EvaluationContext> contextPusher,
            BiConsumer<EvaluationContext, EvaluationContext> contextPopper) {
        this.config = config;
        this.evaluator = evaluator;
        this.contextPusher = contextPusher;
        this.contextPopper = contextPopper;
    }

    /**
     * Evaluates a list comprehension.
     * <p>
     * Supports nested iterations: {@code {x*y for x in 1..3 for y in 1..3}}
     *
     * @param node    the comprehension node
     * @param context the evaluation context
     * @return a NodeVector containing the comprehension results
     * @throws EvaluationException if comprehensions are disabled
     * @throws TypeError           if the iterable is not iterable
     */
    public NodeConstant evaluate(NodeComprehension node, EvaluationContext context) {
        if (!config.comprehensionsEnabled()) {
            throw new EvaluationException("List comprehensions are disabled in current configuration");
        }

        var results = new ArrayList<Node>();
        var bindings = new HashMap<String, NodeConstant>();

        // Use recursive helper to handle nested iterations
        evaluateIterators(node, context, bindings, 0, results);

        // Validate result size against configuration limit
        if (results.size() > config.maxVectorSize()) {
            throw new EvaluationException("Comprehension produced " + results.size() +
                    " elements, exceeding maximum allowed size of " + config.maxVectorSize());
        }

        return new NodeVector(results.toArray(new Node[0]));
    }

    /**
     * Recursively evaluates nested iterators in a comprehension.
     *
     * @param node          the comprehension node
     * @param context       the evaluation context
     * @param bindings      accumulated variable bindings
     * @param iteratorIndex current iterator index
     * @param results       list to accumulate results
     */
    private void evaluateIterators(
            NodeComprehension node,
            EvaluationContext context,
            Map<String, NodeConstant> bindings,
            int iteratorIndex,
            List<Node> results) {

        List<NodeComprehension.Iterator> iterators = node.getIterators();

        // Base case: all iterators processed, evaluate expression
        if (iteratorIndex >= iterators.size()) {
            EvaluationContext loopContext = context.withBindings(bindings);
            EvaluationContext oldContext = contextPusher.apply(loopContext);
            try {
                // Check condition if present
                if (node.hasCondition()) {
                    NodeConstant conditionResult = evaluator.apply(node.getCondition());
                    if (!TypeCoercion.toBoolean(conditionResult)) {
                        return;
                    }
                }

                // Evaluate the expression for this iteration
                NodeConstant result = evaluator.apply(node.getExpression());
                results.add(result);
            } finally {
                contextPopper.accept(loopContext, oldContext);
            }
            return;
        }

        // Get current iterator
        NodeComprehension.Iterator iterator = iterators.get(iteratorIndex);

        // Evaluate iterable in current context (with bindings from outer loops)
        EvaluationContext iterableContext = context.withBindings(bindings);
        EvaluationContext oldIterableContext = contextPusher.apply(iterableContext);
        NodeConstant iterableValue;
        try {
            iterableValue = evaluator.apply(iterator.iterable());
        } finally {
            contextPopper.accept(iterableContext, oldIterableContext);
        }

        Iterable<NodeConstant> items = toIterable(iterableValue);

        // Iterate over items and recurse to next iterator
        for (NodeConstant item : items) {
            // Create new bindings map with current variable
            var newBindings = new HashMap<>(bindings);
            newBindings.put(iterator.variable(), item);

            // Recurse to next iterator
            evaluateIterators(node, context, newBindings, iteratorIndex + 1, results);
        }
    }

    /**
     * Converts a constant to an iterable.
     *
     * @param value the value to convert
     * @return an iterable of constants
     * @throws TypeError if the value cannot be iterated
     */
    private Iterable<NodeConstant> toIterable(NodeConstant value) {
        if (value instanceof NodeVector vector) {
            Node[] elements = vector.getElements();
            var evaluatedElements = new ArrayList<NodeConstant>();
            for (Node element : elements) {
                evaluatedElements.add(evaluator.apply(element));
            }
            return evaluatedElements;
        }

        if (value instanceof NodeRange range) {
            return range::iterator;
        }

        throw new TypeError("Cannot iterate over " + TypeCoercion.typeName(value));
    }
}
