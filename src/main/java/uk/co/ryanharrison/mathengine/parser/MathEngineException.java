package uk.co.ryanharrison.mathengine.parser;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Base exception for all math engine errors.
 * Provides location information and source code context for error reporting.
 * <p>
 * All subclasses can override {@link #formatMessage()} to provide custom formatting,
 * or use the default implementation which includes position info and source context.
 * </p>
 */
public abstract class MathEngineException extends RuntimeException {

    protected final Token token;
    protected final String sourceCode;

    public MathEngineException(String message) {
        super(message);
        this.token = null;
        this.sourceCode = null;
    }

    public MathEngineException(String message, Token token) {
        super(message);
        this.token = token;
        this.sourceCode = null;
    }

    public MathEngineException(String message, Token token, String sourceCode) {
        super(message);
        this.token = token;
        this.sourceCode = sourceCode;
    }

    public MathEngineException(String message, Throwable cause) {
        super(message, cause);
        this.token = null;
        this.sourceCode = null;
    }

    public Token getToken() {
        return token;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    /**
     * Format a detailed error message including position and source context.
     * <p>
     * Default implementation includes:
     * <ul>
     *     <li>Error type and message with position (if token available)</li>
     *     <li>Source code context with caret (if source available)</li>
     *     <li>Cause information (if exception was chained)</li>
     * </ul>
     * Subclasses can override to provide custom formatting.
     * </p>
     *
     * @return formatted error message suitable for display to users
     */
    public String formatMessage() {
        var sb = new StringBuilder();

        // Main error message with position
        String errorType = getErrorType();
        if (token != null) {
            sb.append(String.format("%s at line %d, column %d: %s",
                    errorType, token.line(), token.column(), getMessage()));
        } else {
            sb.append(String.format("%s: %s", errorType, getMessage()));
        }

        // Add source context if available
        if (sourceCode != null && token != null) {
            sb.append("\n").append(formatSourceContext(sourceCode, token.line(), token.column()));
        }

        // Add cause info if present
        Throwable cause = getCause();
        if (cause != null && cause.getMessage() != null) {
            sb.append("\nCaused by: ").append(cause.getMessage());
        }

        return sb.toString();
    }

    /**
     * Gets the error type name for display.
     * Subclasses can override to customize the error type label.
     *
     * @return the error type (e.g., "Parse error", "Evaluation error")
     */
    protected String getErrorType() {
        String className = getClass().getSimpleName();
        // Convert "ParseException" to "Parse error"
        if (className.endsWith("Exception")) {
            className = className.substring(0, className.length() - 9);
        }
        return className + " error";
    }

    /**
     * Formats source code context with a caret pointing to the error position.
     * <p>
     * Example output:
     * <pre>
     *   1 | 2 * (3 + 4
     *     |          ^
     * </pre>
     * </p>
     *
     * @param sourceCode the source code
     * @param line       the line number (1-based)
     * @param column     the column number (1-based)
     * @return formatted source context, or empty string if invalid
     */
    protected final String formatSourceContext(String sourceCode, int line, int column) {
        if (sourceCode == null || line <= 0) {
            return "";
        }

        String[] lines = sourceCode.split("\n", -1);
        if (line > lines.length) {
            return "";
        }

        var sb = new StringBuilder();
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
