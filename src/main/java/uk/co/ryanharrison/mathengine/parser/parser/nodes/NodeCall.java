package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import java.util.List;

/**
 * Node representing a function call.
 * The function can be a name (identifier), a lambda, or any expression that evaluates to a callable.
 */
public final class NodeCall extends NodeExpression {

    private final Node function;
    private final List<Node> arguments;

    public NodeCall(Node function, List<Node> arguments) {
        this.function = function;
        this.arguments = List.copyOf(arguments);
    }

    public Node getFunction() {
        return function;
    }

    public List<Node> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(function).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeCall)) return false;
        NodeCall other = (NodeCall) obj;
        return function.equals(other.function) && arguments.equals(other.arguments);
    }

    @Override
    public int hashCode() {
        return function.hashCode() * 31 + arguments.hashCode();
    }
}
