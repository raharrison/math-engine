package uk.co.ryanharrison.mathengine.parser.parser;

import uk.co.ryanharrison.mathengine.parser.MathEngineException;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;

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
        this.actual = actual != null ? actual.getType() : null;
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
        StringBuilder sb = new StringBuilder();

        // Main error message
        if (token != null) {
            if (expected != null && actual != null) {
                sb.append(String.format("Parse error at line %d, column %d: Expected %s but found %s",
                        token.getLine(), token.getColumn(),
                        formatTokenType(expected),
                        formatTokenType(actual)));
            } else {
                sb.append(String.format("Parse error at line %d, column %d: %s",
                        token.getLine(), token.getColumn(), getMessage()));
            }
        } else {
            sb.append(String.format("Parse error: %s", getMessage()));
        }

        // Add source context if available
        if (sourceCode != null && token != null) {
            sb.append("\n").append(formatSourceContext());
        }

        // Add helpful suggestions
        String suggestion = getSuggestion();
        if (suggestion != null) {
            sb.append("\n\nHint: ").append(suggestion);
        }

        return sb.toString();
    }

    /**
     * Formats source code context with a caret pointing to the error.
     */
    private String formatSourceContext() {
        if (sourceCode == null || token == null) {
            return "";
        }

        String[] lines = sourceCode.split("\n", -1);
        int line = token.getLine();
        int column = token.getColumn();

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

    /**
     * Formats a token type for human-readable output.
     */
    private String formatTokenType(TokenType type) {
        if (type == null) return "unknown";

        return switch (type.getName()) {
            case "LPAREN" -> "'('";
            case "RPAREN" -> "')'";
            case "LBRACE" -> "'{'";
            case "RBRACE" -> "'}'";
            case "LBRACKET" -> "'['";
            case "RBRACKET" -> "']'";
            case "COMMA" -> "','";
            case "SEMICOLON" -> "';'";
            case "COLON" -> "':'";
            case "PLUS" -> "'+'";
            case "MINUS" -> "'-'";
            case "MULTIPLY" -> "'*'";
            case "DIVIDE" -> "'/'";
            case "POWER" -> "'^'";
            case "ASSIGN" -> "':='";
            case "EQ" -> "'=='";
            case "NEQ" -> "'!='";
            case "LT" -> "'<'";
            case "GT" -> "'>'";
            case "LTE" -> "'<='";
            case "GTE" -> "'>='";
            case "EOF" -> "end of expression";
            case "INTEGER", "DECIMAL", "SCIENTIFIC" -> "number";
            case "IDENTIFIER" -> "identifier";
            case "FUNCTION" -> "function";
            case "KEYWORD" -> "keyword";
            default -> type.getName().toLowerCase();
        };
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
