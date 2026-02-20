package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

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
    public String typeName() {
        return "function";
    }

    // ==================== Universal Arithmetic ====================

    @Override
    public NodeConstant add(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant subtract(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant multiply(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant divide(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant power(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant negate() {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public int compareTo(NodeConstant other) {
        throw new TypeError("Cannot compare functions");
    }

    @Override
    public String toString() {
        if (parameters.size() == 1) {
            return parameters.getFirst() + " -> " + body;
        }
        return "(" + String.join(", ", parameters) + ") -> " + body;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeLambda other)) return false;
        return parameters.equals(other.parameters) && body.equals(other.body);
    }

    @Override
    public int hashCode() {
        return parameters.hashCode() * 31 + body.hashCode();
    }
}
