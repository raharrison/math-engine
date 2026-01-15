package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.MathEngineException;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Exception thrown during expression evaluation.
 * <p>
 * Indicates runtime errors such as:
 * <ul>
 *     <li>Division by zero</li>
 *     <li>Domain errors (e.g., sqrt of negative number)</li>
 *     <li>Type mismatches</li>
 *     <li>Feature disabled in configuration</li>
 * </ul>
 *
 * <h2>Example Messages:</h2>
 * <pre>
 * Evaluation error: Division by zero
 * Evaluation error: Cannot take square root of negative number: -4
 * Evaluation error: Vectors are disabled in current configuration
 * </pre>
 */
public class EvaluationException extends MathEngineException {

    /**
     * Creates an evaluation exception with just a message.
     *
     * @param message the error message
     */
    public EvaluationException(String message) {
        super(message);
    }

    /**
     * Creates an evaluation exception with token position info.
     *
     * @param message the error message
     * @param token   the token where the error occurred
     */
    public EvaluationException(String message, Token token) {
        super(message, token);
    }

    /**
     * Creates an evaluation exception with token and source code.
     *
     * @param message    the error message
     * @param token      the token where the error occurred
     * @param sourceCode the source code being evaluated
     */
    public EvaluationException(String message, Token token, String sourceCode) {
        super(message, token, sourceCode);
    }

    /**
     * Creates an evaluation exception wrapping another exception.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String formatMessage() {
        StringBuilder sb = new StringBuilder();

        // Main error message
        if (token != null) {
            sb.append(String.format("Evaluation error at line %d, column %d: %s",
                    token.getLine(), token.getColumn(), getMessage()));
        } else {
            sb.append(String.format("Evaluation error: %s", getMessage()));
        }

        // Add source context if available
        if (sourceCode != null && token != null) {
            sb.append("\n").append(formatSourceContext());
        }

        // Add cause info if present
        Throwable cause = getCause();
        if (cause != null && cause.getMessage() != null) {
            sb.append("\nCaused by: ").append(cause.getMessage());
        }

        return sb.toString();
    }

    /**
     * Formats source code context with a caret pointing to the error.
     */
    private String formatSourceContext() {
        if (sourceCode == null || token == null) {
            return "";
        }

        String[] lines = sourceCode.split("\n", -1);
        int line = token.getLine();
        int column = token.getColumn();

        if (line <= 0 || line > lines.length) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String sourceLine = lines[line - 1];

        // Show the line with error
        sb.append(String.format("  %4d | %s\n", line, sourceLine));

        // Show caret pointing to error position
        sb.append("       | ");
        int caretPos = Math.max(0, column - 1);
        sb.append(" ".repeat(caretPos));
        sb.append("^");

        return sb.toString();
    }
}
