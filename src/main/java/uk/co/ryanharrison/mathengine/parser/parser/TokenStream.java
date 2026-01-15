package uk.co.ryanharrison.mathengine.parser.parser;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;

import java.util.List;

/**
 * Token stream navigator for the parser.
 * <p>
 * Provides methods for navigating through a token stream with support for:
 * <ul>
 *     <li>Position tracking and backtracking (save/restore)</li>
 *     <li>Token matching and consumption</li>
 *     <li>Lookahead and checking</li>
 *     <li>Error creation with position information</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * TokenStream stream = new TokenStream(tokens, sourceCode);
 *
 * while (!stream.isAtEnd()) {
 *     if (stream.match(TokenType.PLUS)) {
 *         // handle plus
 *     } else {
 *         Token token = stream.advance();
 *     }
 * }
 * }</pre>
 */
public final class TokenStream {

    private final List<Token> tokens;
    private final String sourceCode;
    private int current = 0;

    /**
     * Creates a new token stream.
     *
     * @param tokens     the list of tokens to navigate
     * @param sourceCode the original source code (for error messages)
     */
    public TokenStream(List<Token> tokens, String sourceCode) {
        this.tokens = tokens;
        this.sourceCode = sourceCode;
    }

    // ==================== Position Information ====================

    /**
     * Gets the current position in the token stream.
     *
     * @return the current position index
     */
    public int getPosition() {
        return current;
    }

    /**
     * Gets the total number of tokens.
     *
     * @return the token count
     */
    public int size() {
        return tokens.size();
    }

    /**
     * Gets the source code.
     *
     * @return the source code string
     */
    public String getSourceCode() {
        return sourceCode;
    }

    // ==================== Navigation ====================

    /**
     * Gets the current token without consuming it.
     *
     * @return the current token
     */
    public Token peek() {
        return tokens.get(current);
    }

    /**
     * Gets the token at an offset from the current position.
     *
     * @param offset the offset (0 = current position)
     * @return the token at that position, or the EOF token if beyond end
     */
    public Token peek(int offset) {
        int pos = current + offset;
        if (pos >= tokens.size()) {
            return tokens.get(tokens.size() - 1); // Return EOF
        }
        if (pos < 0) {
            return tokens.get(0);
        }
        return tokens.get(pos);
    }

    /**
     * Gets the previous token.
     *
     * @return the previous token
     * @throws IllegalStateException if at the beginning
     */
    public Token previous() {
        if (current == 0) {
            throw new IllegalStateException("Cannot get previous token at position 0");
        }
        return tokens.get(current - 1);
    }

    /**
     * Checks if we've reached the end of the token stream.
     *
     * @return true if at end (current token is EOF)
     */
    public boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    /**
     * Consumes the current token and returns it.
     *
     * @return the consumed token
     */
    public Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    // ==================== Matching ====================

    /**
     * Checks if the current token matches any of the given types.
     *
     * @param types the token types to check
     * @return true if current token matches any type
     */
    public boolean check(TokenType... types) {
        if (isAtEnd()) {
            return false;
        }
        TokenType currentType = peek().getType();
        for (TokenType type : types) {
            if (currentType == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current token is a keyword with any of the given texts.
     *
     * @param keywords the keyword texts to check
     * @return true if current token is a matching keyword
     */
    public boolean checkKeyword(String... keywords) {
        if (!check(TokenType.KEYWORD)) {
            return false;
        }
        String lexeme = peek().getLexeme();
        for (String keyword : keywords) {
            if (lexeme.equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the current token matches any of the given types, consumes it and returns true.
     *
     * @param types the token types to match
     * @return true if matched and consumed
     */
    public boolean match(TokenType... types) {
        if (check(types)) {
            advance();
            return true;
        }
        return false;
    }

    /**
     * Consumes the current token if it matches the expected type, otherwise throws.
     *
     * @param type    the expected token type
     * @param message the error message if not matched
     * @return the consumed token
     * @throws ParseException if the token doesn't match
     */
    public Token expect(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    /**
     * Expects a keyword with specific text.
     *
     * @param keyword the expected keyword text
     * @return the consumed token
     * @throws ParseException if not a matching keyword
     */
    public Token expectKeyword(String keyword) {
        if (checkKeyword(keyword)) {
            return advance();
        }
        throw error(peek(), "Expected keyword '" + keyword + "'");
    }

    // ==================== Backtracking ====================

    /**
     * Saves the current position for potential backtracking.
     *
     * @return the saved position
     */
    public int savePosition() {
        return current;
    }

    /**
     * Restores a previously saved position.
     *
     * @param position the position to restore
     */
    public void restorePosition(int position) {
        this.current = position;
    }

    // ==================== Lookahead ====================

    /**
     * Checks if the token at an offset from current is an identifier-like token.
     * <p>
     * Identifier-like tokens include IDENTIFIER and UNIT (which can be used as
     * parameter names in certain contexts).
     *
     * @param offset the offset from current position
     * @return true if token at offset is identifier-like
     */
    public boolean isIdentifierAt(int offset) {
        if (current + offset >= tokens.size()) {
            return false;
        }
        TokenType type = tokens.get(current + offset).getType();
        return type == TokenType.IDENTIFIER || type == TokenType.UNIT;
    }

    /**
     * Gets the token at an absolute position in the stream.
     *
     * @param position the absolute position
     * @return the token at that position
     * @throws IndexOutOfBoundsException if position is invalid
     */
    public Token getTokenAt(int position) {
        return tokens.get(position);
    }

    // ==================== Error Handling ====================

    /**
     * Creates a parse exception with position information.
     *
     * @param token   the token at which the error occurred
     * @param message the error message
     * @return the parse exception
     */
    public ParseException error(Token token, String message) {
        return new ParseException(message, token, sourceCode);
    }
}
