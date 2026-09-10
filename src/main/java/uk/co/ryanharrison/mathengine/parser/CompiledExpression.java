package uk.co.ryanharrison.mathengine.parser;

import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.HashMap;
import java.util.Map;

/**
 * An expression parsed once and evaluated many times, typically with different
 * variable values. Used by plotting, integration, differentiation and root finding.
 *
 * <pre>{@code
 * CompiledExpression expr = engine.compile("x^2 + 2*x + 1");
 * for (double x = 0; x <= 10; x += 0.1) {
 *     double y = expr.evaluateDouble("x", x);
 * }
 * }</pre>
 *
 * <p>Bindings live in a child scope created per evaluation, so they never disturb
 * the engine session. Like {@link MathEngine}, instances are not thread-safe.
 *
 * @see MathEngine#compile(String)
 */
public final class CompiledExpression {

    private final String expression;
    private final Node ast;
    private final MathEngine engine;

    CompiledExpression(String expression, Node ast, MathEngine engine) {
        this.expression = expression;
        this.ast = ast;
        this.engine = engine;
    }

    /**
     * Evaluates with the given variable bindings, which shadow session variables
     * of the same name for the duration of the call.
     *
     * @throws MathEngineException if evaluation fails
     */
    public NodeConstant evaluate(Map<String, ? extends Number> bindings) {
        if (bindings.isEmpty()) {
            return evaluate();
        }

        var values = new HashMap<String, NodeConstant>(bindings.size() * 2);
        bindings.forEach((name, value) -> values.put(name, toNodeConstant(value)));

        EvaluationContext scope = engine.getContext().withBindings(values);
        return engine.evaluate(ast, scope);
    }

    public NodeConstant evaluate(String variable, Number value) {
        return evaluate(Map.of(variable, value));
    }

    /**
     * Evaluates using only the variables already defined in the engine session.
     */
    public NodeConstant evaluate() {
        return engine.evaluate(ast);
    }

    public double evaluateDouble(Map<String, ? extends Number> bindings) {
        return evaluate(bindings).doubleValue();
    }

    public double evaluateDouble(String variable, double value) {
        return evaluate(variable, value).doubleValue();
    }

    public String getExpression() {
        return expression;
    }

    /** The parsed tree. Treat it as read-only; it is shared with every evaluation. */
    public Node getAst() {
        return ast;
    }

    private NodeConstant toNodeConstant(Number value) {
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return new NodeRational(value.longValue());
        }
        if (engine.getConfig().forceDoubleArithmetic()) {
            return new NodeDouble(value.doubleValue());
        }
        return TypeCoercion.toNumber(value.doubleValue());
    }

    @Override
    public String toString() {
        return "CompiledExpression{" + expression + "}";
    }
}
