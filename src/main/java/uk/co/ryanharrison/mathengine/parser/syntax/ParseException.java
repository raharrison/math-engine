package uk.co.ryanharrison.mathengine.parser.syntax;

import uk.co.ryanharrison.mathengine.parser.MathEngineException;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Exception thrown during parsing: an unexpected token, a missing one, or malformed syntax.
 * <p>
 * The message is formatted by {@link MathEngineException#formatMessage()}:
 * <pre>
 * Parse error at line 1, column 11: Expected ')' after expression
 *      1 | 2 * (3 + 4
 *        |          ^
 * </pre>
 */
public class ParseException extends MathEngineException {

    public ParseException(String message, Token token, String sourceCode) {
        super(message, token, sourceCode);
    }
}
