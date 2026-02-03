package uk.co.ryanharrison.mathengine.parser.symbolic;

/**
 * Represents an expression item in symbolic computation stacks.
 * <p>
 * An item can be:
 * <ul>
 *     <li>A constant or variable (just input)</li>
 *     <li>A binary operator (has operator field)</li>
 *     <li>A function application (has function field)</li>
 * </ul>
 * </p>
 * <p>
 * This is a data structure used for converting AST nodes to postfix notation.
 * Processing logic is handled by Differentiator and Integrator classes.
 * </p>
 */
public record ExpressionItem(String input, char operator, String function, int sign) {

    public ExpressionItem(String input) {
        this(input, (char) 0, null, 1);
    }

    /**
     * Creates an operator item.
     */
    public static ExpressionItem operator(String input, char operator) {
        return new ExpressionItem(input, operator, null, 1);
    }

    /**
     * Creates a function item.
     */
    public static ExpressionItem function(String input, String functionName) {
        return new ExpressionItem(input, (char) 0, functionName, 1);
    }

    /**
     * Returns true if this item represents an operator.
     */
    public boolean isOperator() {
        return operator != 0;
    }

    /**
     * Returns true if this item represents a function call.
     */
    public boolean isFunction() {
        return function != null;
    }

    /**
     * Extracts the argument from a function call string.
     * Example: "sin(x^2)" → "x^2"
     */
    public static String extractFunctionArgument(String functionCall) {
        int openParen = functionCall.indexOf('(');
        int closeParen = functionCall.lastIndexOf(')');
        if (openParen == -1 || closeParen == -1) {
            return functionCall;
        }
        return functionCall.substring(openParen + 1, closeParen);
    }
}
