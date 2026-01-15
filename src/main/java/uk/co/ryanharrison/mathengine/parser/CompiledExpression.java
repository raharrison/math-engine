package uk.co.ryanharrison.mathengine.parser;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.HashMap;
import java.util.Map;

/**
 * A pre-compiled expression that can be evaluated multiple times efficiently.
 * <p>
 * CompiledExpression separates parsing from evaluation, allowing the same parsed
 * AST to be evaluated multiple times with different variable values. This is useful
 * for:
 * <ul>
 *     <li>Plotting functions (evaluating y = f(x) for many x values)</li>
 *     <li>Numerical integration and differentiation</li>
 *     <li>Root finding algorithms</li>
 *     <li>Any scenario where the same expression is evaluated repeatedly</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * MathEngine engine = MathEngine.create();
 *
 * // Compile the expression once
 * CompiledExpression expr = engine.compile("x^2 + 2*x + 1");
 *
 * // Evaluate many times with different x values
 * for (double x = 0; x <= 10; x += 0.1) {
 *     NodeConstant result = expr.evaluate(Map.of("x", x));
 *     System.out.println(x + " -> " + result.doubleValue());
 * }
 *
 * // Or use the convenience method for single variable
 * double y = expr.evaluateDouble("x", 5.0);
 * }</pre>
 *
 * <h2>Thread Safety:</h2>
 * <p>
 * CompiledExpression instances are NOT thread-safe due to shared mutable state
 * in the evaluation context. Create separate MathEngine instances for concurrent
 * evaluation.
 * </p>
 *
 * @see MathEngine#compile(String)
 */
public final class CompiledExpression {

    private final String expression;
    private final Node ast;
    private final MathEngine engine;

    /**
     * Creates a compiled expression.
     * <p>
     * This constructor is package-private. Use {@link MathEngine#compile(String)}
     * to create compiled expressions.
     *
     * @param expression the original expression string
     * @param ast        the parsed AST
     * @param engine     the engine used for evaluation
     */
    CompiledExpression(String expression, Node ast, MathEngine engine) {
        this.expression = expression;
        this.ast = ast;
        this.engine = engine;
    }

    /**
     * Evaluates the expression with the given variable bindings.
     * <p>
     * The bindings temporarily override any existing variables in the engine's
     * context. After evaluation, the original variable values are restored.
     *
     * @param bindings map of variable names to values
     * @return the result of evaluation
     * @throws MathEngineException if evaluation fails
     */
    public NodeConstant evaluate(Map<String, ? extends Number> bindings) {
        // Save current variable values and set new ones
        var context = engine.getContext();
        var savedValues = new HashMap<String, NodeConstant>();

        for (var entry : bindings.entrySet()) {
            String name = entry.getKey();
            if (context.isDefined(name)) {
                savedValues.put(name, context.resolve(name));
            }
            context.define(name, toNodeConstant(entry.getValue()));
        }

        try {
            return engine.getEvaluator().evaluate(ast);
        } finally {
            // Restore original values
            for (var entry : savedValues.entrySet()) {
                context.define(entry.getKey(), entry.getValue());
            }
            // Remove newly added variables
            for (String name : bindings.keySet()) {
                if (!savedValues.containsKey(name)) {
                    context.removeVariable(name);
                }
            }
        }
    }

    /**
     * Evaluates the expression with a single variable binding.
     *
     * @param variable the variable name
     * @param value    the variable value
     * @return the result of evaluation
     */
    public NodeConstant evaluate(String variable, Number value) {
        return evaluate(Map.of(variable, value));
    }

    /**
     * Evaluates the expression and returns the result as a double.
     *
     * @param bindings map of variable names to values
     * @return the result as a double
     */
    public double evaluateDouble(Map<String, ? extends Number> bindings) {
        return evaluate(bindings).doubleValue();
    }

    /**
     * Evaluates the expression with a single variable and returns the result as a double.
     *
     * @param variable the variable name
     * @param value    the variable value
     * @return the result as a double
     */
    public double evaluateDouble(String variable, double value) {
        return evaluate(variable, value).doubleValue();
    }

    /**
     * Evaluates the expression without any variable bindings.
     * <p>
     * Uses whatever variables are already defined in the engine's context.
     *
     * @return the result of evaluation
     */
    public NodeConstant evaluate() {
        return engine.getEvaluator().evaluate(ast);
    }

    /**
     * Gets the original expression string.
     *
     * @return the expression string
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Gets the parsed AST.
     * <p>
     * Note: Modifying the returned AST may cause undefined behavior.
     *
     * @return the parsed AST
     */
    public Node getAst() {
        return ast;
    }

    /**
     * Converts a Number to a NodeConstant.
     */
    private NodeConstant toNodeConstant(Number value) {
        if (value instanceof Double || value instanceof Float) {
            if (engine.getConfig().forceDoubleArithmetic()) {
                return new NodeDouble(value.doubleValue());
            } else {
                return TypeCoercion.toNumber(value.doubleValue());
            }
        } else if (value instanceof Long || value instanceof Integer ||
                value instanceof Short || value instanceof Byte) {
            return new NodeRational(value.longValue());
        } else {
            return new NodeDouble(value.doubleValue());
        }
    }

    @Override
    public String toString() {
        return "CompiledExpression{" + expression + "}";
    }
}
