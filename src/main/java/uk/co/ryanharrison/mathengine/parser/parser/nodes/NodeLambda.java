package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import java.util.List;

/**
 * Node representing an anonymous function (lambda).
 * Lambdas are first-class values that can be passed as arguments or assigned to variables.
 */
public final class NodeLambda extends NodeConstant {

    private final List<String> parameters;
    private final Node body;

    public NodeLambda(List<String> parameters, Node body) {
        this.parameters = List.copyOf(parameters);
        this.body = body;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public Node getBody() {
        return body;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException("Cannot convert lambda to double");
    }

    @Override
    public String toString() {
        if (parameters.size() == 1) {
            return parameters.get(0) + " -> " + body;
        }
        return "(" + String.join(", ", parameters) + ") -> " + body;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeLambda)) return false;
        NodeLambda other = (NodeLambda) obj;
        return parameters.equals(other.parameters) && body.equals(other.body);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode() * 31 + body.hashCode();
    }
}
