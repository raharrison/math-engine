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
 */
public final class FunctionDefinition {

    private final String name;
    private final List<String> parameters;
    private final Node body;
    private final EvaluationContext closure;  // null for dynamic scoping, non-null for lexical scoping

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

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public Node getBody() {
        return body;
    }

    public EvaluationContext getClosure() {
        return closure;
    }

    public int getArity() {
        return parameters.size();
    }

    @Override
    public String toString() {
        return name + "(" + String.join(", ", parameters) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FunctionDefinition)) return false;
        FunctionDefinition other = (FunctionDefinition) obj;
        return name.equals(other.name) && parameters.equals(other.parameters) && body.equals(other.body);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + parameters.hashCode() * 17 + body.hashCode();
    }
}
