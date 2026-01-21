package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Exception thrown when an operation is applied to incompatible types.
 * For example, trying to add a string to a number.
 */
public class TypeError extends EvaluationException {

    private final NodeConstant leftOperand;
    private final NodeConstant rightOperand;
    private final Token operator;

    public TypeError(String message) {
        super(message);
        this.leftOperand = null;
        this.rightOperand = null;
        this.operator = null;
    }

    public TypeError(String message, Token operator, NodeConstant left, NodeConstant right) {
        super(message, operator);
        this.leftOperand = left;
        this.rightOperand = right;
        this.operator = operator;
    }

    public NodeConstant getLeftOperand() {
        return leftOperand;
    }

    public NodeConstant getRightOperand() {
        return rightOperand;
    }

    public Token getOperator() {
        return operator;
    }

    @Override
    public String formatMessage() {
        if (operator != null && leftOperand != null && rightOperand != null) {
            return String.format("Type error at line %d, column %d: Cannot apply %s to %s and %s",
                    operator.line(), operator.column(),
                    operator.lexeme(),
                    leftOperand.getClass().getSimpleName(),
                    rightOperand.getClass().getSimpleName());
        }
        return String.format("Type error: %s", getMessage());
    }
}
