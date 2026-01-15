package uk.co.ryanharrison.mathengine.parser.lexer;

/**
 * Represents a single token produced by the lexer.
 * Contains the token type, the original text (lexeme), any parsed literal value,
 * and position information for error reporting.
 */
public final class Token {

    private final TokenType type;
    private final String lexeme;
    private final Object literal;
    private final int line;
    private final int column;

    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, null, line, column);
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
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
