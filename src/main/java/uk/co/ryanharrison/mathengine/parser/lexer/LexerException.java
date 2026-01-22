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

    /**
     * Creates a lexer exception with just a message.
     *
     * @param message the error message
     */
    public LexerException(String message) {
        super(message);
    }

    /**
     * Creates a lexer exception with a token for position info.
     *
     * @param message the error message
     * @param token   the token where the error occurred
     */
    public LexerException(String message, Token token) {
        super(message, token);
    }

    /**
     * Creates a lexer exception with explicit position info.
     * Creates a synthetic ERROR token for the given position.
     *
     * @param message    the error message
     * @param line       the line number (1-based)
     * @param column     the column number (1-based)
     * @param sourceCode the source code being tokenized
     */
    public LexerException(String message, int line, int column, String sourceCode) {
        super(message, new Token(TokenType.ERROR, "", line, column), sourceCode);
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
    }

    // Uses default formatMessage() implementation from MathEngineException
}
