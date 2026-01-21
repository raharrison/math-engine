package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;

import java.util.List;

/**
 * Represents a user-defined function with a name, parameters, body, and optional closure.
 * <p>
 * Two scoping modes are supported:
 * <ul>
 *     <li><b>Dynamic scoping</b> (closure = null): For regular function definitions (f(x) := ...).
 *         Free variables are looked up in the global context at call time.</li>
 *     <li><b>Lexical scoping</b> (closure != null): For lambdas (x -> ...).
 *         Free variables are captured at definition time.</li>
 * </ul>
 *
 * @param closure null for dynamic scoping, non-null for lexical scoping
 */
public record FunctionDefinition(String name, List<String> parameters, Node body, EvaluationContext closure) {

    public FunctionDefinition(String name, List<String> parameters, Node body, EvaluationContext closure) {
        this.name = name;
        this.parameters = List.copyOf(parameters);
        this.body = body;
        this.closure = closure;
    }

    /**
     * Check if this function uses lexical scoping (has a captured closure).
     */
    public boolean hasLexicalScope() {
        return closure != null;
    }

    public int getArity() {
        return parameters.size();
    }

    @Override
    public String toString() {
        return name + "(" + String.join(", ", parameters) + ")";
    }
}
