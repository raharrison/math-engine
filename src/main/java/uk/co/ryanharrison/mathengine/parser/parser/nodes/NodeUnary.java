package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Node representing a unary operation (operator with one operand).
 * Examples: negation, logical NOT, factorial, percent.
 */
public final class NodeUnary extends NodeExpression {

    private final Token operator;
    private final Node operand;
    private final boolean isPrefix;

    public NodeUnary(Token operator, Node operand, boolean isPrefix) {
        this.operator = operator;
        this.operand = operand;
        this.isPrefix = isPrefix;
    }

    public NodeUnary(Token operator, Node operand) {
        this(operator, operand, true);
    }

    public Token getOperator() {
        return operator;
    }

    public Node getOperand() {
        return operand;
    }

    public boolean isPrefix() {
        return isPrefix;
    }

    @Override
    public String typeName() {
        return "unary expression";
    }

    @Override
    public String toString() {
        if (isPrefix) {
            return String.format("(%s%s)", operator.lexeme(), operand);
        } else {
            return String.format("(%s%s)", operand, operator.lexeme());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeUnary other)) return false;
        return operator.equals(other.operator) && operand.equals(other.operand) && isPrefix == other.isPrefix;
    }

    @Override
    public int hashCode() {
        return operator.hashCode() * 31 + operand.hashCode() * 17 + Boolean.hashCode(isPrefix);
    }
}
