package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.MathEngineException;

/**
 * Exception thrown during lexical analysis: an unexpected character, a malformed number,
 * an unterminated string or an invalid escape.
 * <pre>
 * Lexer error at line 1, column 5: Unexpected character '@'
 * </pre>
 */
public class LexerException extends MathEngineException {

    /**
     * @param line   the line number (1-based)
     * @param column the column number (1-based)
     */
    public LexerException(String message, int line, int column, String sourceCode) {
        super(message, new Token(TokenType.ERROR, "", line, column), sourceCode);
    }
}
