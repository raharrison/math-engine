package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Node representing a binary operation (operator with two operands).
 * Examples: addition, subtraction, multiplication, comparison, etc.
 */
public final class NodeBinary extends NodeExpression {

    private final Token operator;
    private final Node left;
    private final Node right;

    public NodeBinary(Token operator, Node left, Node right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public Token getOperator() {
        return operator;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", left, operator.getLexeme(), right);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeBinary)) return false;
        NodeBinary other = (NodeBinary) obj;
        return operator.equals(other.operator) && left.equals(other.left) && right.equals(other.right);
    }

    @Override
    public int hashCode() {
        return operator.hashCode() * 31 + left.hashCode() * 17 + right.hashCode();
    }
}
