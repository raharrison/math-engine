package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.MathEngineException;

/**
 * Exception thrown during lexical analysis (tokenization).
 * <p>
 * Indicates problems such as:
 * <ul>
 *     <li>Unexpected characters in input</li>
 *     <li>Malformed numbers (e.g., "1.2.3")</li>
 *     <li>Unterminated strings</li>
 *     <li>Invalid escape sequences</li>
 * </ul>
 *
 * <h2>Example Messages:</h2>
 * <pre>
 * Lexer error at line 1, column 5: Unexpected character '@'
 * Lexer error at line 2, column 10: Unterminated string literal
 * </pre>
 */
public class LexerException extends MathEngineException {

    private final int lineNumber;
    private final int columnNumber;
    private final String source;

    /**
     * Creates a lexer exception with just a message.
     *
     * @param message the error message
     */
    public LexerException(String message) {
        super(message);
        this.lineNumber = -1;
        this.columnNumber = -1;
        this.source = null;
    }

    /**
     * Creates a lexer exception with a token for position info.
     *
     * @param message the error message
     * @param token   the token where the error occurred
     */
    public LexerException(String message, Token token) {
        super(message, token);
        this.lineNumber = token != null ? token.getLine() : -1;
        this.columnNumber = token != null ? token.getColumn() : -1;
        this.source = null;
    }

    /**
     * Creates a lexer exception with explicit position info.
     *
     * @param message    the error message
     * @param line       the line number (1-based)
     * @param column     the column number (1-based)
     * @param sourceCode the source code being tokenized
     */
    public LexerException(String message, int line, int column, String sourceCode) {
        super(message, null, sourceCode);
        this.lineNumber = line;
        this.columnNumber = column;
        this.source = sourceCode;
    }

    /**
     * Creates a lexer exception with token and source code.
     *
     * @param message    the error message
     * @param token      the token where the error occurred
     * @param sourceCode the source code being tokenized
     */
    public LexerException(String message, Token token, String sourceCode) {
        super(message, token, sourceCode);
        this.lineNumber = token != null ? token.getLine() : -1;
        this.columnNumber = token != null ? token.getColumn() : -1;
        this.source = sourceCode;
    }

    /**
     * Gets the line number where the error occurred.
     *
     * @return the line number (1-based), or -1 if unknown
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Gets the column number where the error occurred.
     *
     * @return the column number (1-based), or -1 if unknown
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    @Override
    public String formatMessage() {
        StringBuilder sb = new StringBuilder();

        // Main error message with position
        if (token != null) {
            sb.append(String.format("Lexer error at line %d, column %d: %s",
                    token.getLine(), token.getColumn(), getMessage()));
        } else if (lineNumber > 0) {
            sb.append(String.format("Lexer error at line %d, column %d: %s",
                    lineNumber, columnNumber, getMessage()));
        } else {
            sb.append(String.format("Lexer error: %s", getMessage()));
        }

        // Add source context if available
        String src = source != null ? source : getSourceCode();
        int line = token != null ? token.getLine() : lineNumber;
        int col = token != null ? token.getColumn() : columnNumber;

        if (src != null && line > 0) {
            sb.append("\n").append(formatSourceContext(src, line, col));
        }

        return sb.toString();
    }

    /**
     * Formats source code context with a caret pointing to the error.
     */
    private String formatSourceContext(String sourceCode, int line, int column) {
        String[] lines = sourceCode.split("\n", -1);
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
