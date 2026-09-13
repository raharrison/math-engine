package uk.co.ryanharrison.mathengine.parser.lexer;

/**
 * Represents a single token produced by the lexer.
 * Contains the token type, the original text (lexeme), any parsed literal value,
 * and position information for error reporting.
 *
 * @param implicit true for an operator the lexer supplied, as in {@code 45 degrees}
 */
public record Token(TokenType type, String lexeme, Object literal, int line, int column, boolean implicit) {

    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this(type, lexeme, literal, line, column, false);
    }

    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, null, line, column, false);
    }

    /**
     * An operator the lexer supplied between two adjacent operands.
     */
    public static Token implied(TokenType type, String lexeme, int line, int column) {
        return new Token(type, lexeme, null, line, column, true);
    }

    /**
     * Create a new token with a different type (used for token classification).
     */
    public Token withType(TokenType newType) {
        return new Token(newType, lexeme, literal, line, column, implicit);
    }

    @Override
    public String toString() {
        if (literal != null) {
            return String.format("Token{%s, '%s', %s, %d:%d}", type, lexeme, literal, line, column);
        }
        return String.format("Token{%s, '%s', %d:%d}", type, lexeme, line, column);
    }
}
