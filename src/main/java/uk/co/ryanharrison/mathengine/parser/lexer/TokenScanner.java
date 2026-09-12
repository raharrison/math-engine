package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.registry.SymbolRegistry;

import java.math.BigInteger;
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
            case '/' -> scanSlash();
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

    /**
     * Scans a slash, which starts a comment when doubled or followed by a star,
     * and is the division operator otherwise. A line comment runs to the end of
     * the line; the newline itself is left for the main loop to count.
     */
    private void scanSlash() {
        if (scanner.match('/')) {
            scanner.consumeWhile(c -> c != '\n');
        } else if (scanner.match('*')) {
            scanBlockComment();
        } else {
            addToken(TokenType.DIVIDE, "/");
        }
    }

    private void scanBlockComment() {
        while (!scanner.isAtEnd()) {
            char c = scanner.advance();
            if (c == '\n') {
                scanner.newLine();
            } else if (c == '*' && scanner.match('/')) {
                return;
            }
        }
        throw scanner.error("Unterminated block comment (expected '*/')");
    }

    private void scanMinus() {
        if (scanner.match('>')) {
            addToken(TokenType.LAMBDA, "->");
        } else {
            // Minus operator; syntax will handle unary minus
            addToken(TokenType.MINUS, "-");
        }
    }

    private void scanDot() {
        if (scanner.match('.')) {
            addToken(TokenType.RANGE, "..");
        } else if (CharacterScanner.isDigit(scanner.peek())) {
            // Decimal starting with . (e.g., ".5")
            consumeDigits();

            // Check for invalid format: .5.3
            if (scanner.peek() == '.' && CharacterScanner.isDigit(scanner.peek(1))) {
                throw scanner.error("Invalid number format: too many decimal points");
            }

            // Check for scientific notation
            boolean hasExponent = isExponentStart();
            if (hasExponent) {
                scanExponent();
            }

            // Check for 'd' suffix
            if (isDoubleSuffix()) {
                scanner.advance();
                emitDouble();
            } else if (hasExponent) {
                emitScientific();
            } else {
                emitDecimal();
            }
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
     * <p>
     * Handles integers, decimals, rationals, scientific notation, and 'd' suffix for forced doubles.
     * Digits may be grouped with '_', and an integer may be written in another base as 0x1f,
     * 0o17 or 0b1011.
     * </p>
     */
    private void scanNumber() {
        if (startsBasedInteger()) {
            scanBasedInteger();
            return;
        }

        // Consume integer part
        consumeDigits();

        // Check what comes next
        if (scanner.peek() == '.' && scanner.peek(1) == '.') {
            // Range: "1..5"
            emitInteger();
            return;
        } else if (scanner.peek() == '.' && CharacterScanner.isDigit(scanner.peek(1))) {
            // Decimal: "1.5"
            scanner.advance(); // consume '.'
            consumeDigits();

            // Check for invalid format: 1.2.3
            if (scanner.peek() == '.' && CharacterScanner.isDigit(scanner.peek(1))) {
                throw scanner.error("Invalid number format: too many decimal points");
            }

            // Check for scientific notation
            boolean hasExponent = isExponentStart();
            if (hasExponent) {
                scanExponent();
            }

            // Check for 'd' suffix
            if (isDoubleSuffix()) {
                scanner.advance();
                emitDouble();
            } else if (hasExponent) {
                emitScientific();
            } else {
                emitDecimal();
            }
            return;
        } else if (scanner.peek() == '/' && CharacterScanner.isDigit(scanner.peek(1))) {
            // Rational: "1/2"
            scanner.advance(); // consume '/'
            consumeDigits();
            // Check for scientific notation after rational
            if (isExponentStart()) {
                scanExponent();
                emitRationalScientific();
                return;
            }
            // Check for 'd' suffix
            if (isDoubleSuffix()) {
                scanner.advance();
                emitRationalAsDouble();
                return;
            }
            emitRational();
            return;
        }

        // Check for scientific notation
        if (isExponentStart()) {
            scanExponent();
            if (isDoubleSuffix()) {
                scanner.advance();
                emitDouble();
            } else {
                emitScientific();
            }
            return;
        }

        // Check for 'd' suffix on plain integer
        if (isDoubleSuffix()) {
            scanner.advance();
            emitDouble();
            return;
        }

        // Plain integer
        emitInteger();
    }

    /**
     * True when the number starting here is a based integer: a leading zero, then a base
     * prefix, then a digit. The leading digit has already been consumed, so it is read
     * back through the scanner.
     * <p>
     * Any decimal digit after the prefix starts a based literal, valid for that base or
     * not, so that {@code 0b12} is reported rather than read as two tokens. A prefix with
     * no digit after it is not a literal at all, which keeps {@code 0x} as a zero times a
     * variable named x.
     */
    private boolean startsBasedInteger() {
        if (scanner.peek(-1) != '0') {
            return false;
        }
        int radix = radixOf(scanner.peek());
        char next = scanner.peek(1);
        return radix != 0 && (CharacterScanner.isDigit(next) || isDigitOfRadix(next, radix));
    }

    /**
     * Scans a based integer such as {@code 0x1f}, {@code 0o17} or {@code 0b1011}, digit
     * separators included.
     * <p>
     * A letter that is not a digit of the base ends the literal, so {@code 0b10x} is two
     * times x, the same way {@code 2x} is two times x. A decimal digit that is not a digit
     * of the base is a typo rather than a product, and is rejected.
     */
    private void scanBasedInteger() {
        int radix = radixOf(scanner.advance());
        int digitsStart = scanner.getPosition();
        consumeDigits(radix);

        if (CharacterScanner.isDigit(scanner.peek())) {
            throw scanner.error("Invalid digit '" + scanner.peek() + "' in a base " + radix + " literal");
        }

        String digits = withoutSeparators(scanner.substring(digitsStart, scanner.getPosition()));
        String text = numberText();
        try {
            addToken(TokenType.INTEGER, text, Long.parseLong(digits, radix));
        } catch (NumberFormatException e) {
            // Too large for a long, the same fallback a plain integer literal takes
            addToken(TokenType.INTEGER, text, new BigInteger(digits, radix));
        }
    }

    /**
     * The base a prefix character stands for, or 0 when it is not a base prefix.
     */
    private static int radixOf(char prefix) {
        return switch (prefix) {
            case 'x', 'X' -> 16;
            case 'o', 'O' -> 8;
            case 'b', 'B' -> 2;
            default -> 0;
        };
    }

    /**
     * Consumes a run of decimal digits, allowing '_' between two of them.
     */
    private void consumeDigits() {
        consumeDigits(10);
    }

    /**
     * Consumes a run of digits of the given base, allowing '_' between two of them.
     * A separator with no digit after it is left where it is, so the number ends there
     * and the separator starts an identifier: {@code 1_} is one times a name.
     */
    private void consumeDigits(int radix) {
        scanner.consumeWhile(c -> isDigitOfRadix((char) c, radix));
        while (scanner.peek() == '_' && isDigitOfRadix(scanner.peek(1), radix)) {
            scanner.advance();
            scanner.consumeWhile(c -> isDigitOfRadix((char) c, radix));
        }
    }

    /**
     * Whether a character is a digit of the given base. ASCII only, matching the rest of
     * the scanner, so a Unicode digit is never read as part of a number.
     */
    private static boolean isDigitOfRadix(char c, int radix) {
        int digit;
        if (CharacterScanner.isDigit(c)) {
            digit = c - '0';
        } else {
            char lower = Character.toLowerCase(c);
            digit = lower >= 'a' && lower <= 'z' ? lower - 'a' + 10 : -1;
        }
        return digit >= 0 && digit < radix;
    }

    /**
     * The text of the number just scanned, with its digit separators removed so that
     * everything downstream sees a literal it can parse.
     */
    private String numberText() {
        return withoutSeparators(scanner.substring(start, scanner.getPosition()));
    }

    private static String withoutSeparators(String text) {
        return text.indexOf('_') < 0 ? text : text.replace("_", "");
    }

    /**
     * Scans the exponent part of scientific notation (e.g., "e10", "E-3").
     */
    private void scanExponent() {
        scanner.advance(); // consume 'e' or 'E'

        // Optional sign
        if (scanner.peek() == '+' || scanner.peek() == '-') {
            scanner.advance();
        }

        // Exponent digits (required)
        if (!CharacterScanner.isDigit(scanner.peek())) {
            throw scanner.error("Expected digit in scientific notation exponent");
        }

        consumeDigits();
    }

    /**
     * Emits an integer token.
     */
    private void emitInteger() {
        String text = numberText();
        try {
            long value = Long.parseLong(text);
            addToken(TokenType.INTEGER, text, value);
        } catch (NumberFormatException e) {
            // Number too large for long — store as BigInteger, syntax handles it
            addToken(TokenType.INTEGER, text, new BigInteger(text));
        }
    }

    /**
     * Emits a decimal token.
     */
    private void emitDecimal() {
        String text = numberText();
        addToken(TokenType.DECIMAL, text, text);
    }

    /**
     * Emits a double token (with 'd' suffix).
     */
    private void emitDouble() {
        String text = numberText();
        // Remove 'd' or 'D' suffix for parsing
        String numericPart = text.substring(0, text.length() - 1);
        double value = Double.parseDouble(numericPart);
        addToken(TokenType.DOUBLE, text, value);
    }

    /**
     * Emits a rational token.
     */
    private void emitRational() {
        String text = numberText();
        addToken(TokenType.RATIONAL, text);
    }

    /**
     * Emits a rational as a double (with 'd' suffix).
     */
    private void emitRationalAsDouble() {
        String text = numberText();
        // Remove 'd' suffix and evaluate rational
        String rationalPart = text.substring(0, text.length() - 1);
        int slashIndex = rationalPart.indexOf('/');
        BigInteger numerator = new BigInteger(rationalPart.substring(0, slashIndex));
        BigInteger denominator = new BigInteger(rationalPart.substring(slashIndex + 1));

        if (denominator.signum() == 0) {
            throw scanner.error("Division by zero in rational literal");
        }

        double value = numerator.doubleValue() / denominator.doubleValue();
        addToken(TokenType.DOUBLE, text, value);
    }

    /**
     * Emits a scientific notation token.
     */
    private void emitScientific() {
        String text = numberText();
        addToken(TokenType.SCIENTIFIC, text, text);
    }

    /**
     * Emits a rational + scientific notation as SCIENTIFIC token.
     * E.g., "1/2E5" = 0.5 * 10^5 = 50000.0
     */
    private void emitRationalScientific() {
        String text = numberText();

        // Find the slash and E/e to split rational and exponent parts
        int slashIndex = text.indexOf('/');
        int eIndex = text.toLowerCase().indexOf('e', slashIndex);

        // Parse rational part
        BigInteger numerator = new BigInteger(text.substring(0, slashIndex));
        BigInteger denominator = new BigInteger(text.substring(slashIndex + 1, eIndex));

        if (denominator.signum() == 0) {
            throw scanner.error("Division by zero in rational literal");
        }

        double rationalValue = numerator.doubleValue() / denominator.doubleValue();

        // Parse exponent
        int exponent = Integer.parseInt(text.substring(eIndex + 1));
        double value = rationalValue * Math.pow(10, exponent);

        addToken(TokenType.SCIENTIFIC, text, value);
    }

    /**
     * Checks if the current position starts an exponent (e/E).
     */
    private boolean isExponentStart() {
        char c = scanner.peek();
        if (c != 'e' && c != 'E') {
            return false;
        }
        // Make sure next char is digit or +/-
        char next = scanner.peek(1);
        return CharacterScanner.isDigit(next) || next == '+' || next == '-';
    }

    /**
     * Checks if the current position has a 'd' or 'D' suffix (forces double type).
     */
    private boolean isDoubleSuffix() {
        char c = scanner.peek();
        if (c != 'd' && c != 'D') {
            return false;
        }
        // Make sure it's not followed by a letter (e.g., "data" should not match)
        char next = scanner.peek(1);
        return !CharacterScanner.isAlpha(next);
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
     * Reports an over-long identifier, abbreviating it only when there is something to
     * abbreviate. Cutting blindly at twenty characters used to fail on a short name that
     * was still over a short configured limit.
     */
    private String tooLongMessage(String text) {
        String shown = text.length() > 20 ? text.substring(0, 20) + "..." : text;
        return "Identifier '" + shown + "' exceeds maximum allowed length of " + maxIdentifierLength;
    }

    /**
     * Scans an identifier (keyword, variable, function name).
     */
    private void scanIdentifier() {
        scanner.consumeWhile(CharacterScanner::isAlphaNumeric);

        String text = scanner.substring(start, scanner.getPosition());

        // Validate identifier length against configuration limit
        if (text.length() > maxIdentifierLength) {
            throw scanner.error(tooLongMessage(text));
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
            throw scanner.error(tooLongMessage(text));
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
