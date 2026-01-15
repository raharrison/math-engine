package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import java.util.List;

/**
 * Node representing a function definition (name(params) := body).
 * Evaluating a function definition stores the function in the evaluation context.
 */
public final class NodeFunctionDef extends NodeExpression {

    private final String name;
    private final List<String> parameters;
    private final Node body;

    public NodeFunctionDef(String name, List<String> parameters, Node body) {
        this.name = name;
        this.parameters = List.copyOf(parameters);
        this.body = body;
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

    @Override
    public String toString() {
        return name + "(" + String.join(", ", parameters) + ") := " + body;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeFunctionDef)) return false;
        NodeFunctionDef other = (NodeFunctionDef) obj;
        return name.equals(other.name) && parameters.equals(other.parameters) && body.equals(other.body);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + parameters.hashCode() * 17 + body.hashCode();
    }
}
