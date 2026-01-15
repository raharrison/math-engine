package uk.co.ryanharrison.mathengine.parser.lexer;

/**
 * Low-level character scanning utilities for the lexer.
 * <p>
 * Handles character-by-character traversal of input text with position tracking.
 * Provides methods for lookahead, matching, and character classification.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * CharacterScanner scanner = new CharacterScanner("2 + 3");
 * while (!scanner.isAtEnd()) {
 *     char c = scanner.advance();
 *     // process character
 * }
 * }</pre>
 */
public final class CharacterScanner {

    private final String source;
    private int current;
    private int line;
    private int column;

    /**
     * Creates a new character scanner for the given source text.
     *
     * @param source the source text to scan
     */
    public CharacterScanner(String source) {
        this.source = source != null ? source : "";
        this.current = 0;
        this.line = 1;
        this.column = 1;
    }

    // ==================== Position Information ====================

    /**
     * Gets the current position in the source.
     */
    public int getPosition() {
        return current;
    }

    /**
     * Gets the current line number (1-based).
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the current column number (1-based).
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the source text.
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets a substring from the source.
     *
     * @param start start position (inclusive)
     * @param end   end position (exclusive)
     * @return the substring
     */
    public String substring(int start, int end) {
        return source.substring(start, end);
    }

    // ==================== Navigation ====================

    /**
     * Checks if we've reached the end of the source.
     *
     * @return true if at end
     */
    public boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Advances to the next character and returns the current character.
     *
     * @return the character at the current position before advancing
     */
    public char advance() {
        if (isAtEnd()) {
            return '\0';
        }
        char c = source.charAt(current++);
        column++;
        return c;
    }

    /**
     * Returns the current character without advancing.
     *
     * @return the current character, or '\0' if at end
     */
    public char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Returns the character at the given offset from current position.
     *
     * @param offset the offset (0 = current position)
     * @return the character at that position, or '\0' if beyond end
     */
    public char peek(int offset) {
        int pos = current + offset;
        if (pos >= source.length() || pos < 0) return '\0';
        return source.charAt(pos);
    }

    /**
     * Advances if the current character matches the expected character.
     *
     * @param expected the expected character
     * @return true if matched and advanced, false otherwise
     */
    public boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        column++;
        return true;
    }

    /**
     * Consumes characters while the predicate is true.
     *
     * @param predicate the predicate to test each character
     * @return the number of characters consumed
     */
    public int consumeWhile(java.util.function.IntPredicate predicate) {
        int count = 0;
        while (!isAtEnd() && predicate.test(peek())) {
            advance();
            count++;
        }
        return count;
    }

    // ==================== Line Tracking ====================

    /**
     * Increments the line counter and resets column.
     * Call this when a newline is encountered.
     */
    public void newLine() {
        line++;
        column = 1;
    }

    /**
     * Sets the column to a specific value.
     * Useful when resetting after consuming certain characters.
     *
     * @param col the new column value
     */
    public void setColumn(int col) {
        this.column = col;
    }

    // ==================== Character Classification ====================

    /**
     * Checks if a character is a digit (0-9).
     *
     * @param c the character to check
     * @return true if digit
     */
    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks if a character (as int) is a digit (0-9).
     * <p>
     * This overload is compatible with {@link java.util.function.IntPredicate}
     * for use with {@link #consumeWhile(java.util.function.IntPredicate)}.
     *
     * @param c the character as int
     * @return true if digit
     */
    public static boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks if a character is alphabetic (a-z, A-Z, _).
     *
     * @param c the character to check
     * @return true if alphabetic
     */
    public static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * Checks if a character (as int) is alphabetic (a-z, A-Z, _).
     * <p>
     * This overload is compatible with {@link java.util.function.IntPredicate}
     * for use with {@link #consumeWhile(java.util.function.IntPredicate)}.
     *
     * @param c the character as int
     * @return true if alphabetic
     */
    public static boolean isAlpha(int c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * Checks if a character is alphanumeric.
     *
     * @param c the character to check
     * @return true if alphanumeric
     */
    public static boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Checks if a character (as int) is alphanumeric.
     * <p>
     * This overload is compatible with {@link java.util.function.IntPredicate}
     * for use with {@link #consumeWhile(java.util.function.IntPredicate)}.
     *
     * @param c the character as int
     * @return true if alphanumeric
     */
    public static boolean isAlphaNumeric(int c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Checks if a character is whitespace (space, tab, carriage return).
     * Note: newline is not included as it may need special handling.
     *
     * @param c the character to check
     * @return true if whitespace
     */
    public static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r';
    }

    /**
     * Checks if a character is a newline.
     *
     * @param c the character to check
     * @return true if newline
     */
    public static boolean isNewline(char c) {
        return c == '\n';
    }

    // ==================== Error Utilities ====================

    /**
     * Creates a lexer exception at the current position.
     *
     * @param message the error message
     * @return the exception
     */
    public LexerException error(String message) {
        return new LexerException(message, line, column, source);
    }

    /**
     * Creates a lexer exception at a specific position.
     *
     * @param message the error message
     * @param atLine  the line number
     * @param atCol   the column number
     * @return the exception
     */
    public LexerException error(String message, int atLine, int atCol) {
        return new LexerException(message, atLine, atCol, source);
    }
}
