package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.registry.SymbolRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pass 1 of lexical analysis: Scans characters and produces raw tokens.
 * <p>
 * Handles character-by-character scanning of source text to produce
 * tokens for operators, literals, identifiers, and structural elements.
 *
 * <h2>Responsibilities:</h2>
 * <ul>
 *     <li>Scanning number literals (integer, decimal, scientific, rational)</li>
 *     <li>Scanning string literals with escape sequences</li>
 *     <li>Scanning identifiers (keywords, variables, functions)</li>
 *     <li>Recognizing operators and punctuation</li>
 *     <li>Disambiguating decimal (1.5) vs range (1..5)</li>
 *     <li>Tracking line/column positions for error reporting</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * TokenScanner scanner = new TokenScanner(256);
 * List<Token> tokens = scanner.scan("2 + 3 * x");
 * }</pre>
 */
public final class TokenScanner {

    private final int maxIdentifierLength;
    private final SymbolRegistry registry = SymbolRegistry.getDefault();
    private CharacterScanner scanner;
    private int start;
    private int tokenStartLine;
    private int tokenStartColumn;
    private List<Token> tokens;

    /**
     * Creates a new token scanner with the specified maximum identifier length.
     *
     * @param maxIdentifierLength maximum allowed length for identifiers
     */
    public TokenScanner(int maxIdentifierLength) {
        this.maxIdentifierLength = maxIdentifierLength;
    }

    /**
     * Scans the source text and produces raw tokens.
     * <p>
     * This is Pass 1 of the lexer pipeline. The resulting tokens
     * may need further processing (identifier classification,
     * implicit multiplication insertion).
     *
     * @param source the source text to scan
     * @return list of raw tokens
     * @throws LexerException if a lexical error occurs
     */
    public List<Token> scan(String source) {
        this.scanner = new CharacterScanner(source != null ? source : "");
        this.tokens = new ArrayList<>();
        this.start = 0;

        while (!scanner.isAtEnd()) {
            start = scanner.getPosition();
            tokenStartLine = scanner.getLine();
            tokenStartColumn = scanner.getColumn();
            scanToken();
        }

        // Add EOF token
        tokens.add(new Token(TokenType.EOF, "", scanner.getLine(), scanner.getColumn()));

        return tokens;
    }

    /**
     * Scans a single token from the current position.
     */
    private void scanToken() {
        char c = scanner.advance();

        switch (c) {
            // Whitespace
            case ' ', '\r', '\t' -> {
                // Ignore whitespace
            }

            case '\n' -> scanner.newLine();

            // Structural tokens (not operators)
            case '(' -> addToken(TokenType.LPAREN, "(");
            case ')' -> addToken(TokenType.RPAREN, ")");
            case '{' -> addToken(TokenType.LBRACE, "{");
            case '}' -> addToken(TokenType.RBRACE, "}");
            case '[' -> addToken(TokenType.LBRACKET, "[");
            case ']' -> addToken(TokenType.RBRACKET, "]");
            case ',' -> addToken(TokenType.COMMA, ",");
            case ';' -> addToken(TokenType.SEMICOLON, ";");

            // Multi-character tokens (need lookahead)
            case ':' -> scanColon();
            case '!' -> scanBang();
            case '=' -> scanEquals();
            case '<' -> scanLessThan();
            case '>' -> scanGreaterThan();
            case '&' -> scanAmpersand();
            case '|' -> scanPipe();
            case '-' -> scanMinus();
            case '@' -> scanAtSign();
            case '$' -> scanDollarSign();
            case '#' -> scanHashSign();
            case '.' -> scanDot();
            case '"' -> scanString('"');
            case '\'' -> scanString('\'');

            default -> {
                // Try registry lookup for simple single-char operators
                Optional<TokenType> type = registry.findByInputSymbol(String.valueOf(c));
                if (type.isPresent()) {
                    addToken(type.get(), String.valueOf(c));
                    return;
                }

                // Check for numbers and identifiers
                if (CharacterScanner.isDigit(c)) {
                    scanNumber();
                } else if (CharacterScanner.isAlpha(c)) {
                    scanIdentifier();
                } else {
                    throw scanner.error("Unexpected character: '" + c + "'");
                }
            }
        }
    }

    // ==================== Operator Scanning ====================

    private void scanColon() {
        if (scanner.match('=')) {
            addToken(TokenType.ASSIGN, ":=");
        } else {
            addToken(TokenType.COLON, ":");
        }
    }

    private void scanBang() {
        if (scanner.match('!')) {
            addToken(TokenType.DOUBLE_FACTORIAL, "!!");
        } else if (scanner.match('=')) {
            addToken(TokenType.NEQ, "!=");
        } else {
            addToken(TokenType.FACTORIAL, "!");
        }
    }

    private void scanEquals() {
        if (scanner.match('=')) {
            addToken(TokenType.EQ, "==");
        } else {
            throw scanner.error("Unexpected character '=' (use '==' for equality or ':=' for assignment)");
        }
    }

    private void scanLessThan() {
        if (scanner.match('=')) {
            addToken(TokenType.LTE, "<=");
        } else {
            addToken(TokenType.LT, "<");
        }
    }

    private void scanGreaterThan() {
        if (scanner.match('=')) {
            addToken(TokenType.GTE, ">=");
        } else {
            addToken(TokenType.GT, ">");
        }
    }

    private void scanAmpersand() {
        if (scanner.match('&')) {
            addToken(TokenType.AND, "&&");
        } else {
            throw scanner.error("Unexpected character '&' (use '&&' for logical AND)");
        }
    }

    private void scanPipe() {
        if (scanner.match('|')) {
            addToken(TokenType.OR, "||");
        } else {
            throw scanner.error("Unexpected character '|' (use '||' for logical OR)");
        }
    }

    private void scanMinus() {
        if (scanner.match('>')) {
            addToken(TokenType.LAMBDA, "->");
        } else {
            // Minus operator; parser will handle unary minus
            addToken(TokenType.MINUS, "-");
        }
    }

    private void scanDot() {
        if (scanner.match('.')) {
            addToken(TokenType.RANGE, "..");
        } else if (CharacterScanner.isDigit(scanner.peek())) {
            // Decimal starting with .
            scanDecimalPart();
        } else {
            throw scanner.error("Unexpected character '.'");
        }
    }

    /**
     * Scans @ character - either matrix multiplication or explicit unit reference.
     * <ul>
     *     <li>{@code @fahrenheit} - unit reference with identifier</li>
     *     <li>{@code @"km/h"} - unit reference with quoted string (for complex units)</li>
     *     <li>{@code @} alone - at operator</li>
     * </ul>
     */
    private void scanAtSign() {
        if (CharacterScanner.isAlpha(scanner.peek())) {
            // Explicit unit reference: @fahrenheit
            String unitName = extractIdentifierName();
            addToken(TokenType.UNIT_REF, "@" + unitName, unitName);
        } else if (scanner.peek() == '"' || scanner.peek() == '\'') {
            // Quoted unit reference: @"km/h" or @'km/h'
            char quote = scanner.advance();
            int unitStart = scanner.getPosition();
            scanner.consumeWhile(c -> c != quote && !CharacterScanner.isNewline((char) c));
            if (scanner.isAtEnd() || scanner.peek() != quote) {
                throw scanner.error("Unterminated unit reference string");
            }
            String unitName = scanner.substring(unitStart, scanner.getPosition());
            scanner.advance(); // consume closing quote
            String text = scanner.substring(start, scanner.getPosition());
            addToken(TokenType.UNIT_REF, text, unitName);
        } else {
            addToken(TokenType.AT, "@");
        }
    }

    /**
     * Scans $ character - explicit variable reference.
     * Must be followed by an identifier (letter).
     */
    private void scanDollarSign() {
        if (!CharacterScanner.isAlpha(scanner.peek())) {
            throw scanner.error("Expected identifier after '$' (use $variable for explicit variable reference)");
        }
        String varName = extractIdentifierName();
        addToken(TokenType.VAR_REF, "$" + varName);
    }

    /**
     * Scans # character - explicit constant reference.
     * Must be followed by an identifier (letter).
     */
    private void scanHashSign() {
        if (!CharacterScanner.isAlpha(scanner.peek())) {
            throw scanner.error("Expected identifier after '#' (use #constant for explicit constant reference)");
        }
        String constName = extractIdentifierName();
        addToken(TokenType.CONST_REF, "#" + constName);
    }

    // ==================== Number Scanning ====================

    /**
     * Scans a number literal.
     * Handles decimal vs range disambiguation (1.5 vs 1..5).
     */
    private void scanNumber() {
        // Consume digits
        scanner.consumeWhile(CharacterScanner::isDigit);

        // Check for decimal, range, or rational
        if (scanner.peek() == '.' && scanner.peek(1) == '.') {
            // Range: "1..5"
            emitInteger();
            return;
        } else if (scanner.peek() == '.' && CharacterScanner.isDigit(scanner.peek(1))) {
            // Decimal: "1.5"
            scanner.advance(); // consume '.'
            scanDecimalPart();
            return;
        } else if (scanner.peek() == '/' && CharacterScanner.isDigit(scanner.peek(1))) {
            // Rational: "1/2" - only if followed by a digit
            scanRational();
            return;
        }

        // Check for scientific notation
        if (isExponentStart()) {
            scanScientific();
            return;
        }

        // Plain integer
        emitInteger();
    }

    /**
     * Scans the decimal part of a number (after the decimal point).
     */
    private void scanDecimalPart() {
        // Consume fractional digits
        scanner.consumeWhile(CharacterScanner::isDigit);

        // Check for invalid number format: 1.2.3 (decimal followed by another decimal point)
        if (scanner.peek() == '.' && CharacterScanner.isDigit(scanner.peek(1))) {
            throw scanner.error("Invalid number format: too many decimal points");
        }

        // Check for scientific notation
        if (isExponentStart()) {
            scanScientific();
            return;
        }

        // Emit decimal token
        String text = scanner.substring(start, scanner.getPosition());
        double value = Double.parseDouble(text);
        addToken(TokenType.DECIMAL, text, value);
    }

    /**
     * Scans a rational literal (e.g., "1/2").
     */
    private void scanRational() {
        scanner.advance(); // consume '/'

        if (!CharacterScanner.isDigit(scanner.peek())) {
            throw scanner.error("Expected digit after '/' in rational literal");
        }

        scanner.consumeWhile(CharacterScanner::isDigit);

        String text = scanner.substring(start, scanner.getPosition());
        addToken(TokenType.RATIONAL, text);
    }

    /**
     * Scans scientific notation (e.g., "1e10", "2.5E-3").
     */
    private void scanScientific() {
        scanner.advance(); // consume 'e' or 'E'

        // Optional sign
        if (scanner.peek() == '+' || scanner.peek() == '-') {
            scanner.advance();
        }

        // Exponent digits (required)
        if (!CharacterScanner.isDigit(scanner.peek())) {
            throw scanner.error("Expected digit in scientific notation exponent");
        }

        scanner.consumeWhile(CharacterScanner::isDigit);

        String text = scanner.substring(start, scanner.getPosition());
        double value = Double.parseDouble(text);
        addToken(TokenType.SCIENTIFIC, text, value);
    }

    /**
     * Emits an integer token for the scanned digits.
     */
    private void emitInteger() {
        String text = scanner.substring(start, scanner.getPosition());
        long value = Long.parseLong(text);
        addToken(TokenType.INTEGER, text, value);
    }

    /**
     * Checks if the current position starts an exponent (e/E).
     */
    private boolean isExponentStart() {
        return scanner.peek() == 'e' || scanner.peek() == 'E';
    }

    // ==================== String Scanning ====================

    /**
     * Scans a string literal with escape sequence support.
     */
    private void scanString(char quote) {
        var value = new StringBuilder();

        while (!scanner.isAtEnd() && scanner.peek() != quote) {
            if (CharacterScanner.isNewline(scanner.peek())) {
                scanner.advance();
                scanner.newLine();
            } else if (scanner.peek() == '\\' && scanner.peek(1) != '\0') {
                // Escape sequence
                scanner.advance(); // consume '\'
                char escaped = scanner.advance();
                switch (escaped) {
                    case 'n' -> value.append('\n');
                    case 't' -> value.append('\t');
                    case 'r' -> value.append('\r');
                    case '\\' -> value.append('\\');
                    case '"' -> value.append('"');
                    case '\'' -> value.append('\'');
                    default -> value.append(escaped);
                }
            } else {
                value.append(scanner.advance());
            }
        }

        if (scanner.isAtEnd()) {
            throw scanner.error("Unterminated string literal");
        }

        // Consume closing quote
        scanner.advance();

        String text = scanner.substring(start, scanner.getPosition());
        addToken(TokenType.STRING, text, value.toString());
    }

    // ==================== Identifier Scanning ====================

    /**
     * Scans an identifier (keyword, variable, function name).
     */
    private void scanIdentifier() {
        scanner.consumeWhile(CharacterScanner::isAlphaNumeric);

        String text = scanner.substring(start, scanner.getPosition());

        // Validate identifier length against configuration limit
        if (text.length() > maxIdentifierLength) {
            throw scanner.error("Identifier '" + text.substring(0, 20) + "...' exceeds maximum allowed length of " + maxIdentifierLength);
        }

        // Don't classify here - just emit as IDENTIFIER
        // Classification happens in TokenProcessor
        addToken(TokenType.IDENTIFIER, text);
    }

    /**
     * Extracts an identifier name without creating a token.
     * Used for explicit references (@unit, $var, #const).
     *
     * @return the identifier text
     */
    private String extractIdentifierName() {
        int startPos = scanner.getPosition();
        scanner.consumeWhile(CharacterScanner::isAlphaNumeric);
        String text = scanner.substring(startPos, scanner.getPosition());

        // Validate identifier length
        if (text.length() > maxIdentifierLength) {
            throw scanner.error("Identifier '" + text.substring(0, 20) + "...' exceeds maximum allowed length of " + maxIdentifierLength);
        }

        return text;
    }

    // ==================== Token Creation ====================

    private void addToken(TokenType type, String lexeme) {
        tokens.add(new Token(type, lexeme, tokenStartLine, tokenStartColumn));
    }

    private void addToken(TokenType type, String lexeme, Object literal) {
        tokens.add(new Token(type, lexeme, literal, tokenStartLine, tokenStartColumn));
    }
}
