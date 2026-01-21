package uk.co.ryanharrison.mathengine.parser.lexer;

/**
 * Represents a single token produced by the lexer.
 * Contains the token type, the original text (lexeme), any parsed literal value,
 * and position information for error reporting.
 */
public record Token(TokenType type, String lexeme, Object literal, int line, int column) {

    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, null, line, column);
    }

    /**
     * Create a new token with a different type (used for token classification).
     */
    public Token withType(TokenType newType) {
        return new Token(newType, lexeme, literal, line, column);
    }

    @Override
    public String toString() {
        if (literal != null) {
            return String.format("Token{%s, '%s', %s, %d:%d}", type, lexeme, literal, line, column);
        }
        return String.format("Token{%s, '%s', %d:%d}", type, lexeme, line, column);
    }
}
