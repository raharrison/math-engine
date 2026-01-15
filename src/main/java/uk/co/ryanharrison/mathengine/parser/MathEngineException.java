package uk.co.ryanharrison.mathengine.parser;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Base exception for all math engine errors.
 * Provides location information and source code context for error reporting.
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
     */
    public abstract String formatMessage();
}
