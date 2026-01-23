package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing a variable assignment (identifier := expression).
 * Evaluating an assignment stores the value in the evaluation context and returns it.
 */
public final class NodeAssignment extends NodeExpression {

    private final String identifier;
    private final Node value;

    public NodeAssignment(String identifier, Node value) {
        this.identifier = identifier;
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Node getValue() {
        return value;
    }

    @Override
    public String toString() {
        return identifier + " := " + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeAssignment other)) return false;
        return identifier.equals(other.identifier) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode() * 31 + value.hashCode();
    }
}
