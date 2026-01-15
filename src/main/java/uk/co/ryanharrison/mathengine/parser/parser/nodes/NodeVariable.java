package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing a variable reference or identifier.
 * During evaluation, the variable name is looked up in the evaluation context.
 */
public final class NodeVariable extends NodeExpression {

    private final String name;

    public NodeVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeVariable)) return false;
        NodeVariable other = (NodeVariable) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
