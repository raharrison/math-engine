package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Exception thrown when attempting to access an undefined variable or function.
 */
public class UndefinedVariableException extends EvaluationException {

    private final String variableName;

    public UndefinedVariableException(String variableName) {
        super(String.format("No value associated with '%s'", variableName));
        this.variableName = variableName;
    }

    public UndefinedVariableException(String variableName, Token token) {
        super(String.format("No value associated with '%s'", variableName), token);
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }
}
