package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Exception thrown when attempting to access an undefined variable or function.
 * <p>
 * Provides the variable name that couldn't be resolved for better error messages.
 * </p>
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

    @Override
    public String formatMessage() {
        String baseMessage = super.formatMessage();

        // Add helpful hint if variable name looks like it could be a typo
        var hint = new StringBuilder();
        if (variableName.length() > 1) {
            hint.append("\n\nHint: Variable '").append(variableName).append("' is not defined.");
            hint.append("\n  - Check spelling and case sensitivity");
            hint.append("\n  - Use ':=' to assign variables (e.g., '").append(variableName).append(" := 5')");
        }

        return baseMessage + (!hint.isEmpty() ? hint.toString() : "");
    }
}
