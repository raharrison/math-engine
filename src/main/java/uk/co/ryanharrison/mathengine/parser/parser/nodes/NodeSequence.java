package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import java.util.List;

/**
 * Node representing a sequence of statements separated by semicolons.
 * When evaluated, each statement is evaluated in order, and the last result is returned.
 */
public final class NodeSequence extends NodeExpression {

    private final List<Node> statements;

    public NodeSequence(List<Node> statements) {
        this.statements = List.copyOf(statements);
    }

    public List<Node> getStatements() {
        return statements;
    }

    @Override
    public String typeName() {
        return "statement sequence";
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        for (int i = 0; i < statements.size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(statements.get(i));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeSequence other)) return false;
        return statements.equals(other.statements);
    }

    @Override
    public int hashCode() {
        return statements.hashCode();
    }
}
