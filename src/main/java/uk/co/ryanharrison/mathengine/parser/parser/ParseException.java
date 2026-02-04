package uk.co.ryanharrison.mathengine.parser.parser;

import uk.co.ryanharrison.mathengine.parser.MathEngineException;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.registry.SymbolRegistry;

/**
 * Exception thrown during parsing.
 * <p>
 * Indicates syntax problems such as:
 * <ul>
 *     <li>Unexpected tokens</li>
 *     <li>Missing tokens (unclosed parentheses, brackets)</li>
 *     <li>Malformed syntax (invalid function definitions, etc.)</li>
 * </ul>
 *
 * <h2>Example Messages:</h2>
 * <pre>
 * Parse error at line 1, column 5: Expected ')' but found '+'
 *      1 | 2 * (3 + 4
 *        |          ^
 *
 * Parse error at line 2, column 1: Unexpected token 'then'
 *      2 | if x > 0 then y
 *        | ^
 * </pre>
 */
public class ParseException extends MathEngineException {

    private final TokenType expected;
    private final TokenType actual;

    /**
     * Creates a parse exception with just a message.
     *
     * @param message the error message
     */
    public ParseException(String message) {
        super(message);
        this.expected = null;
        this.actual = null;
    }

    /**
     * Creates a parse exception with token position info.
     *
     * @param message the error message
     * @param token   the token where the error occurred
     */
    public ParseException(String message, Token token) {
        super(message, token);
        this.expected = null;
        this.actual = null;
    }

    /**
     * Creates a parse exception with token and source code.
     *
     * @param message    the error message
     * @param token      the token where the error occurred
     * @param sourceCode the source code being parsed
     */
    public ParseException(String message, Token token, String sourceCode) {
        super(message, token, sourceCode);
        this.expected = null;
        this.actual = null;
    }

    /**
     * Creates a parse exception with expected/actual token types.
     *
     * @param message    the error message
     * @param expected   the expected token type
     * @param actual     the actual token found
     * @param sourceCode the source code being parsed
     */
    public ParseException(String message, TokenType expected, Token actual, String sourceCode) {
        super(message, actual, sourceCode);
        this.expected = expected;
        this.actual = actual != null ? actual.type() : null;
    }

    /**
     * Gets the expected token type, if known.
     *
     * @return the expected type, or null
     */
    public TokenType getExpected() {
        return expected;
    }

    /**
     * Gets the actual token type found.
     *
     * @return the actual type, or null
     */
    public TokenType getActual() {
        return actual;
    }

    @Override
    public String formatMessage() {
        var sb = new StringBuilder();

        // Main error message with expected/actual token info if available
        if (token != null) {
            if (expected != null && actual != null) {
                sb.append(String.format("Parse error at line %d, column %d: Expected %s but found %s",
                        token.line(), token.column(),
                        formatTokenType(expected),
                        formatTokenType(actual)));
            } else {
                sb.append(String.format("Parse error at line %d, column %d: %s",
                        token.line(), token.column(), getMessage()));
            }
        } else {
            sb.append(String.format("Parse error: %s", getMessage()));
        }

        // Add source context if available (using base class method)
        if (sourceCode != null && token != null) {
            sb.append("\n").append(formatSourceContext(sourceCode, token.line(), token.column()));
        }

        // Add helpful suggestions
        String suggestion = getSuggestion();
        if (suggestion != null) {
            sb.append("\n\nHint: ").append(suggestion);
        }

        return sb.toString();
    }

    /**
     * Formats a token type for human-readable output.
     */
    private String formatTokenType(TokenType type) {
        if (type == null) {
            return "unknown";
        }

        return SymbolRegistry.getDefault()
                .getMetadata(type)
                .map(meta -> {
                    String symbol = meta.getInputSymbol();
                    return meta.isKeyword() ? symbol : "'" + symbol + "'";
                })
                .orElse(type.name().toLowerCase());
    }

    /**
     * Provides helpful suggestions based on the error context.
     */
    private String getSuggestion() {
        if (expected == null || actual == null) {
            return null;
        }

        // Unclosed parenthesis
        if (expected == TokenType.RPAREN) {
            return "Check for missing closing parenthesis ')'";
        }
        if (expected == TokenType.RBRACE) {
            return "Check for missing closing brace '}'";
        }
        if (expected == TokenType.RBRACKET) {
            return "Check for missing closing bracket ']'";
        }

        // Common mistakes
        if (actual == TokenType.ASSIGN && expected != TokenType.ASSIGN) {
            return "Did you mean '==' for comparison? ':=' is used for assignment.";
        }

        return null;
    }
}
