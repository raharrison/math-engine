package uk.co.ryanharrison.mathengine.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive test suite for {@link Utils} utility class.
 * <p>
 * Tests cover:
 * <ul>
 *     <li>Numeric validation with various number formats</li>
 *     <li>Parenthesis matching with nested cases</li>
 *     <li>String manipulation and standardization</li>
 *     <li>Null handling and exception cases</li>
 * </ul>
 */
class UtilsTest {

    // ==================== isNumeric() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "123",              // positive integer
            "-456",             // negative integer
            "0",                // zero
            "123.45",           // positive decimal
            "-67.89",           // negative decimal
            "0.123",            // decimal starting with 0
            ".5",               // decimal without leading zero
            "-.5",              // negative decimal without leading zero
    })
    void isNumericReturnsTrueForValidIntegers(String input) {
        assertThat(Utils.isNumeric(input)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "1.23e10",          // positive exponent
            "1.23E10",          // uppercase E
            "-4.5e-6",          // negative number and exponent
            "1E5",              // integer with exponent
            "2.5e+3",           // explicit positive exponent
            "-1.5E-10",         // negative with uppercase E
    })
    void isNumericReturnsTrueForScientificNotation(String input) {
        assertThat(Utils.isNumeric(input)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "0x1A2B",           // lowercase x
            "0X1A2B",           // uppercase X
            "-0x1A2B",          // negative hex
            "-0X1A2B",          // negative hex uppercase X
            "0xFF",             // common hex value
            "0x0",              // hex zero
    })
    void isNumericReturnsTrueForHexadecimal(String input) {
        assertThat(Utils.isNumeric(input)).isTrue();
    }

    @Test
    void isNumericRemovesWhitespaceBeforeValidation() {
        assertThat(Utils.isNumeric(" 123 ")).isTrue();        // spaces around number
        assertThat(Utils.isNumeric("1 2 3")).isTrue();        // spaces within number
        assertThat(Utils.isNumeric("\t456\t")).isTrue();      // tabs
        assertThat(Utils.isNumeric("\n789\n")).isTrue();      // newlines
        assertThat(Utils.isNumeric(" 1.23e10 ")).isTrue();    // scientific notation with spaces
        assertThat(Utils.isNumeric(" 0x1A2B ")).isTrue();     // hex with spaces
    }

    @Test
    void isNumericReturnsFalseForNullEmptyOrWhitespace() {
        assertThat(Utils.isNumeric(null)).isFalse();          // null
        assertThat(Utils.isNumeric("")).isFalse();            // empty
        assertThat(Utils.isNumeric("   ")).isFalse();         // only spaces
        assertThat(Utils.isNumeric("\t\t")).isFalse();        // only tabs
        assertThat(Utils.isNumeric("\n\n")).isFalse();        // only newlines
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abc",              // letters
            "12a34",            // mixed letters and numbers
            "1.2.3",            // multiple decimal points
            "1e2e3",            // multiple exponents
            "0xGHIJ",           // invalid hex characters
            "-",                // just minus sign
            ".",                // just decimal point
            "e5",               // exponent without base
            "--5",              // double minus
            "1.2.3.4",          // multiple dots
    })
    void isNumericReturnsFalseForInvalidFormats(String input) {
        assertThat(Utils.isNumeric(input)).isFalse();
    }

    @Test
    void isNumericAcceptsSpecialDoubleValues() {
        // NaN and Infinity are valid double values in Java
        assertThat(Utils.isNumeric("NaN")).isTrue();
        assertThat(Utils.isNumeric("Infinity")).isTrue();
        assertThat(Utils.isNumeric("-Infinity")).isTrue();
    }

    // ==================== matchingCharacterIndex() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "(simple), 0, 7",           // simple parenthesis
            "((nested)), 0, 9",         // nested parenthesis starting at 0
            "((nested)), 1, 8",         // nested parenthesis starting at 1
            "(a(b(c)d)e), 0, 10",       // multiple nesting levels
            "(a(b(c)d)e), 2, 8",        // starting at inner parenthesis
            "((a+b)*(c+d)), 0, 12",     // complex expression
    })
    void matchingCharacterIndexFindsMatchingPair(String expression, int startIndex, int expectedIndex) {
        assertThat(Utils.matchingCharacterIndex(expression, startIndex, '(', ')'))
                .isEqualTo(expectedIndex);
    }

    @Test
    void matchingCharacterIndexWorksWithDifferentBracketTypes() {
        String expr = "[a[b[c]d]e]";
        assertThat(Utils.matchingCharacterIndex(expr, 0, '[', ']')).isEqualTo(10);

        expr = "{a{b{c}d}e}";
        assertThat(Utils.matchingCharacterIndex(expr, 0, '{', '}')).isEqualTo(10);
    }

    @Test
    void matchingCharacterIndexReturnsStartIndexWhenNoMatchFound() {
        String expr = "(abc";
        assertThat(Utils.matchingCharacterIndex(expr, 0, '(', ')')).isEqualTo(0);

        expr = "((abc)";
        assertThat(Utils.matchingCharacterIndex(expr, 0, '(', ')')).isEqualTo(0);
    }

    @Test
    void matchingCharacterIndexHandlesEmptyParenthesis() {
        String expr = "()";
        assertThat(Utils.matchingCharacterIndex(expr, 0, '(', ')')).isEqualTo(1);
    }

    @Test
    void matchingCharacterIndexThrowsOnNullExpression() {
        assertThatThrownBy(() -> Utils.matchingCharacterIndex(null, 0, '(', ')'))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Expression cannot be null");
    }

    @Test
    void matchingCharacterIndexThrowsOnNegativeIndex() {
        assertThatThrownBy(() -> Utils.matchingCharacterIndex("(abc)", -1, '(', ')'))
                .isInstanceOf(StringIndexOutOfBoundsException.class)
                .hasMessageContaining("Index out of bounds");
    }

    @Test
    void matchingCharacterIndexThrowsOnIndexOutOfBounds() {
        String expr = "(abc)";
        assertThatThrownBy(() -> Utils.matchingCharacterIndex(expr, 10, '(', ')'))
                .isInstanceOf(StringIndexOutOfBoundsException.class)
                .hasMessageContaining("Index out of bounds");
    }

    @Test
    void matchingCharacterIndexThrowsOnIndexEqualToLength() {
        String expr = "(abc)";
        assertThatThrownBy(() -> Utils.matchingCharacterIndex(expr, expr.length(), '(', ')'))
                .isInstanceOf(StringIndexOutOfBoundsException.class)
                .hasMessageContaining("Index out of bounds");
    }

    // ==================== removeOuterParenthesis() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "(2+3), 2+3",                   // simple case
            "((2+3)), 2+3",                 // double nested
            "(((2+3))), 2+3",               // triple nested
            "(a), a",                       // single character
            "(), ''",                       // empty parenthesis
    })
    void removeOuterParenthesisRemovesWrapperParenthesis(String input, String expected) {
        assertThat(Utils.removeOuterParenthesis(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "(2+3)+(4+5), (2+3)+(4+5)",     // not wrapping - has operator outside
            "(2+3)*(4+5), (2+3)*(4+5)",     // not wrapping - multiplication
            "2+3, 2+3",                     // no parenthesis
            "a+b, a+b",                     // no parenthesis
    })
    void removeOuterParenthesisDoesNotRemoveNonWrapperParenthesis(String input, String expected) {
        assertThat(Utils.removeOuterParenthesis(input)).isEqualTo(expected);
    }

    @Test
    void removeOuterParenthesisHandlesComplexNestedExpressions() {
        assertThat(Utils.removeOuterParenthesis("2+3")).isEqualTo("2+3");
        assertThat(Utils.removeOuterParenthesis("(2+3)")).isEqualTo("2+3");
        assertThat(Utils.removeOuterParenthesis("2^3 + (2 + 3)")).isEqualTo("2^3 + (2 + 3)");
        assertThat(Utils.removeOuterParenthesis("(2^3) + (2 + 3)")).isEqualTo("(2^3) + (2 + 3)");
        assertThat(Utils.removeOuterParenthesis("(2^3 + (2 + 3))")).isEqualTo("2^3 + (2 + 3)");
        assertThat(Utils.removeOuterParenthesis("((2^3) + (2 + 3))")).isEqualTo("(2^3) + (2 + 3)");

        String input = "((a+b)*(c+d))";
        String expected = "(a+b)*(c+d)";
        assertThat(Utils.removeOuterParenthesis(input)).isEqualTo(expected);
    }

    @Test
    void removeOuterParenthesisHandlesStringWithoutLeadingParenthesis() {
        String input = "abc)";
        assertThat(Utils.removeOuterParenthesis(input)).isEqualTo(input);

        input = "123+456";
        assertThat(Utils.removeOuterParenthesis(input)).isEqualTo(input);
    }

    @Test
    void removeOuterParenthesisHandlesUnmatchedParenthesis() {
        String input = "(abc";
        assertThat(Utils.removeOuterParenthesis(input)).isEqualTo(input);
    }

    @Test
    void removeOuterParenthesisThrowsOnNull() {
        assertThatThrownBy(() -> Utils.removeOuterParenthesis(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Source cannot be null");
    }

    @Test
    void removeOuterParenthesisHandlesEmptyString() {
        assertThat(Utils.removeOuterParenthesis("")).isEqualTo("");
    }

    // ==================== removeRedundantParentheses() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "((x)), (x)",                    // double nested, single var - removes one layer
            "((a+b)), (a+b)",                // double nested, expression - removes one layer
            "(((x))), (x)",                  // triple nested - removes to single
            "((((x)))), (x)",                // quad nested - removes to single
            "((2+3)), (2+3)",                // double nested numeric - removes one layer
    })
    void removeRedundantParenthesesRemovesDoubleNesting(String input, String expected) {
        assertThat(Utils.removeRedundantParentheses(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "x, x",                          // no parentheses
            "(x), (x)",                      // single parentheses - not removed
            "(a+b), (a+b)",                  // single parentheses with expression
            "(a)+(b), (a)+(b)",              // multiple single parentheses
            "((a)+(b)), (a)+(b)",            // double at ends - depth goes negative, so outer layer removed
    })
    void removeRedundantParenthesesDoesNotRemoveSingleOrNonRedundant(String input, String expected) {
        assertThat(Utils.removeRedundantParentheses(input)).isEqualTo(expected);
    }

    @Test
    void removeRedundantParenthesesHandlesComplexExpressions() {
        // Expression with nested parts but not redundant doubles
        assertThat(Utils.removeRedundantParentheses("(a+(b+c))")).isEqualTo("(a+(b+c))");

        // Outer layer is redundant, so it gets removed
        assertThat(Utils.removeRedundantParentheses("((a+b)+(c+d))")).isEqualTo("(a+b)+(c+d)");

        // Recursively removes multiple layers until only single parens remain
        assertThat(Utils.removeRedundantParentheses("((((a+b))))")).isEqualTo("(a+b)");

        // Only outer doubles removed
        assertThat(Utils.removeRedundantParentheses("((a+(b+c)))")).isEqualTo("(a+(b+c))");
    }

    @Test
    void removeRedundantParenthesesHandlesEdgeCases() {
        // Empty string
        assertThat(Utils.removeRedundantParentheses("")).isEqualTo("");

        // Just parentheses - removes down to single pair
        assertThat(Utils.removeRedundantParentheses("(())")).isEqualTo("()");
        assertThat(Utils.removeRedundantParentheses("((()))")).isEqualTo("()");

        // Single char
        assertThat(Utils.removeRedundantParentheses("a")).isEqualTo("a");
    }

    @Test
    void removeRedundantParenthesesThrowsOnNull() {
        assertThatThrownBy(() -> Utils.removeRedundantParentheses(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void removeRedundantParenthesesDoesNotAffectUnmatchedParentheses() {
        // If there are unmatched parens, should just return as-is
        assertThat(Utils.removeRedundantParentheses("((a+b)")).isEqualTo("((a+b)");
        assertThat(Utils.removeRedundantParentheses("(a+b))")).isEqualTo("(a+b))");
    }

    // ==================== standardiseString() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "Hello World, helloworld",          // spaces between words
            "  Hello World  , helloworld",      // leading and trailing spaces
            "A B C, abc",                       // single character words
            "UPPERCASE, uppercase",             // all uppercase
            "lowercase, lowercase",             // already lowercase
            "MiXeD CaSe, mixedcase",            // mixed case
    })
    void standardiseStringRemovesWhitespaceAndLowercases(String input, String expected) {
        assertThat(Utils.standardiseString(input)).isEqualTo(expected);
    }

    @Test
    void standardiseStringRemovesAllTypesOfWhitespace() {
        assertThat(Utils.standardiseString("a\tb\tc")).isEqualTo("abc");       // tabs
        assertThat(Utils.standardiseString("a\nb\nc")).isEqualTo("abc");       // newlines
        assertThat(Utils.standardiseString("a\rb\rc")).isEqualTo("abc");       // carriage returns
        assertThat(Utils.standardiseString("a \t\n\r b")).isEqualTo("ab");     // mixed whitespace
    }

    @Test
    void standardiseStringReturnsNullForNull() {
        assertThat(Utils.standardiseString(null)).isNull();
    }

    @Test
    void standardiseStringHandlesEmptyString() {
        assertThat(Utils.standardiseString("")).isEqualTo("");
    }

    @Test
    void standardiseStringHandlesWhitespaceOnly() {
        assertThat(Utils.standardiseString("   ")).isEqualTo("");
        assertThat(Utils.standardiseString("\t\t")).isEqualTo("");
        assertThat(Utils.standardiseString("\n\n")).isEqualTo("");
    }

    @Test
    void standardiseStringPreservesSpecialCharacters() {
        assertThat(Utils.standardiseString("a+b-c*d/e")).isEqualTo("a+b-c*d/e");
        assertThat(Utils.standardiseString("!@#$%^&*()")).isEqualTo("!@#$%^&*()");
    }

    // ==================== toTitleCase() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "hello world, Hello World",                     // simple case
            "the quick brown fox, The Quick Brown Fox",     // multiple words
            "already Title Case, Already Title Case",       // already title case
            "UPPERCASE TEXT, UPPERCASE TEXT",               // all uppercase (only first char capitalized)
            "lowercase text, Lowercase Text",               // all lowercase
    })
    void toTitleCaseCapitalizesFirstLetterOfEachWord(String input, String expected) {
        assertThat(Utils.toTitleCase(input)).isEqualTo(expected);
    }

    @Test
    void toTitleCaseHandlesSingleWord() {
        assertThat(Utils.toTitleCase("hello")).isEqualTo("Hello");
        assertThat(Utils.toTitleCase("HELLO")).isEqualTo("HELLO");
        assertThat(Utils.toTitleCase("h")).isEqualTo("H");
    }

    @Test
    void toTitleCaseHandlesMultipleSpaces() {
        assertThat(Utils.toTitleCase("hello  world")).isEqualTo("Hello  World");
        assertThat(Utils.toTitleCase("a   b   c")).isEqualTo("A   B   C");
    }

    @Test
    void toTitleCaseHandlesLeadingAndTrailingSpaces() {
        assertThat(Utils.toTitleCase(" hello world ")).isEqualTo(" Hello World ");
        assertThat(Utils.toTitleCase("   hello   ")).isEqualTo("   Hello   ");
    }

    @Test
    void toTitleCaseHandlesEmptyString() {
        assertThat(Utils.toTitleCase("")).isEqualTo("");
    }

    @Test
    void toTitleCaseHandlesOnlySpaces() {
        assertThat(Utils.toTitleCase("   ")).isEqualTo("   ");
    }

    @Test
    void toTitleCaseThrowsOnNull() {
        assertThatThrownBy(() -> Utils.toTitleCase(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Input cannot be null");
    }

    @Test
    void toTitleCaseHandlesSpecialCharacters() {
        // Special characters are not treated as word boundaries
        assertThat(Utils.toTitleCase("hello-world")).isEqualTo("Hello-world");
        assertThat(Utils.toTitleCase("test_case")).isEqualTo("Test_case");
    }

    @Test
    void toTitleCaseUsesCharacterToTitleCase() {
        // Test with characters where title case differs from uppercase
        // For most characters, title case == uppercase, but for some ligatures it differs
        assertThat(Utils.toTitleCase("abc def")).isEqualTo("Abc Def");
    }

    @Test
    void toTitleCaseHandlesNumbersAndMixedContent() {
        assertThat(Utils.toTitleCase("hello 123 world")).isEqualTo("Hello 123 World");
        assertThat(Utils.toTitleCase("test 1 test 2")).isEqualTo("Test 1 Test 2");
    }

    // ==================== Integration Tests ====================

    @Test
    void isNumericAndStandardiseStringWorkTogether() {
        // standardiseString removes whitespace, which isNumeric also does internally
        String input = "  1 2 3  ";
        String standardised = Utils.standardiseString(input);
        assertThat(Utils.isNumeric(input)).isTrue();
        assertThat(Utils.isNumeric(standardised)).isTrue();
    }

    @Test
    void matchingCharacterIndexAndRemoveOuterParenthesisWorkTogether() {
        // removeOuterParenthesis uses matchingCharacterIndex internally
        String expr = "((2+3))";
        int matchIndex = Utils.matchingCharacterIndex(expr, 0, '(', ')');
        assertThat(matchIndex).isEqualTo(expr.length() - 1);

        String withoutOuter = Utils.removeOuterParenthesis(expr);
        assertThat(withoutOuter).isEqualTo("2+3");
    }

    // ==================== formatDecimal() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "123.456, 2, 123.46",           // rounding up
            "123.454, 2, 123.45",           // rounding down
            "100.0, 2, 100",                // trailing zeros removed
            "100.10, 2, 100.1",             // partial trailing zeros removed
            "100.00, 2, 100",               // all trailing zeros removed
            "0.5, 1, 0.5",                  // single decimal place
            "0.123456789, 5, 0.12346",      // more decimals than needed
    })
    void formatDecimalFormatsWithSpecifiedDecimals(double value, int decimals, String expected) {
        assertThat(Utils.formatDecimal(value, decimals)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "123.456, 0, 123",              // zero decimal places
            "123.956, 0, 124",              // zero decimals with rounding
            "0.5, 0, 1",                    // fractional rounded to zero decimals (HALF_UP: 0.5 -> 1)
    })
    void formatDecimalHandlesZeroDecimalPlaces(double value, int decimals, String expected) {
        assertThat(Utils.formatDecimal(value, decimals)).isEqualTo(expected);
    }

    @Test
    void formatDecimalHandlesNegativeNumbers() {
        assertThat(Utils.formatDecimal(-123.456, 2)).isEqualTo("-123.46");
        assertThat(Utils.formatDecimal(-100.0, 2)).isEqualTo("-100");
        assertThat(Utils.formatDecimal(-0.5, 1)).isEqualTo("-0.5");
    }

    @Test
    void formatDecimalHandlesMaximumDecimalPlaces() {
        assertThat(Utils.formatDecimal(1.123456789012345, 10)).isEqualTo("1.123456789");
    }

    @Test
    void formatDecimalHandlesVerySmallNumbers() {
        assertThat(Utils.formatDecimal(0.000001, 6)).isEqualTo("0.000001");
        assertThat(Utils.formatDecimal(0.0000001, 7)).isEqualTo("0.0000001");
    }

    @Test
    void formatDecimalHandlesZero() {
        assertThat(Utils.formatDecimal(0.0, 2)).isEqualTo("0");
        assertThat(Utils.formatDecimal(0.0, 0)).isEqualTo("0");
    }

    @Test
    void formatDecimalThrowsOnNegativeDecimalPlaces() {
        assertThatThrownBy(() -> Utils.formatDecimal(123.456, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Decimal places must be between 0 and 10")
                .hasMessageContaining("-1");
    }

    @Test
    void formatDecimalThrowsOnTooManyDecimalPlaces() {
        assertThatThrownBy(() -> Utils.formatDecimal(123.456, 11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Decimal places must be between 0 and 10")
                .hasMessageContaining("11");
    }

    @Test
    void formatDecimalHandlesSpecialDoubleValues() {
        // Special values should format without throwing
        String nanResult = Utils.formatDecimal(Double.NaN, 2);
        assertThat(nanResult).containsIgnoringCase("nan");

        String infResult = Utils.formatDecimal(Double.POSITIVE_INFINITY, 2);
        assertThat(infResult).containsIgnoringCase("infinity");

        String negInfResult = Utils.formatDecimal(Double.NEGATIVE_INFINITY, 2);
        assertThat(negInfResult).containsIgnoringCase("infinity");
    }

    // ==================== parseDouble() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "123.45, 0.0, 123.45",          // valid positive decimal
            "-67.89, 0.0, -67.89",          // valid negative decimal
            "0, 1.0, 0.0",                  // zero
            ".5, 0.0, 0.5",                 // decimal without leading zero
            "1e10, 0.0, 1.0E10",            // scientific notation
            "-1.5E-10, 0.0, -1.5E-10",      // negative scientific notation
    })
    void parseDoubleReturnsValueForValidInput(String input, double defaultValue, double expected) {
        assertThat(Utils.parseDouble(input, defaultValue)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "abc, 99.9, 99.9",              // letters - returns default
            "12a34, 99.9, 99.9",            // mixed - returns default
            "not a number, 50.0, 50.0",     // words - returns default
            "'', 42.0, 42.0",               // empty string - returns default
    })
    void parseDoubleReturnsDefaultForInvalidInput(String input, double defaultValue, double expected) {
        assertThat(Utils.parseDouble(input, defaultValue)).isEqualTo(expected);
    }

    @Test
    void parseDoubleReturnsDefaultForNull() {
        assertThat(Utils.parseDouble(null, 100.0)).isEqualTo(100.0);
        assertThat(Utils.parseDouble(null, -50.5)).isEqualTo(-50.5);
    }

    @Test
    void parseDoubleHandlesWhitespace() {
        assertThat(Utils.parseDouble(" 123.45 ", 0.0)).isEqualTo(123.45);
        assertThat(Utils.parseDouble("1 2 3", 0.0)).isEqualTo(123.0);
        assertThat(Utils.parseDouble("\t456.78\t", 0.0)).isEqualTo(456.78);
        assertThat(Utils.parseDouble("\n789\n", 0.0)).isEqualTo(789.0);
    }

    @Test
    void parseDoubleHandlesNegativeDefaults() {
        assertThat(Utils.parseDouble("invalid", -1.0)).isEqualTo(-1.0);
        assertThat(Utils.parseDouble(null, -999.99)).isEqualTo(-999.99);
    }

    @Test
    void parseDoubleHandlesZeroDefault() {
        assertThat(Utils.parseDouble("invalid", 0.0)).isEqualTo(0.0);
        assertThat(Utils.parseDouble("", 0.0)).isEqualTo(0.0);
    }

    @Test
    void parseDoubleHandlesSpecialDoubleValues() {
        assertThat(Utils.parseDouble("NaN", 0.0)).isNaN();
        assertThat(Utils.parseDouble("Infinity", 0.0)).isEqualTo(Double.POSITIVE_INFINITY);
        assertThat(Utils.parseDouble("-Infinity", 0.0)).isEqualTo(Double.NEGATIVE_INFINITY);
    }

    // ==================== parseInt() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "123, 0, 123",                  // valid positive integer
            "-456, 0, -456",                // valid negative integer
            "0, 1, 0",                      // zero
            "2147483647, 0, 2147483647",    // max int
            "-2147483648, 0, -2147483648",  // min int
    })
    void parseIntReturnsValueForValidInput(String input, int defaultValue, int expected) {
        assertThat(Utils.parseInt(input, defaultValue)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "abc, 99, 99",                  // letters - returns default
            "12a34, 99, 99",                // mixed - returns default
            "123.45, 50, 50",               // decimal - returns default
            "not a number, 42, 42",         // words - returns default
            "'', 10, 10",                   // empty string - returns default
    })
    void parseIntReturnsDefaultForInvalidInput(String input, int defaultValue, int expected) {
        assertThat(Utils.parseInt(input, defaultValue)).isEqualTo(expected);
    }

    @Test
    void parseIntReturnsDefaultForNull() {
        assertThat(Utils.parseInt(null, 100)).isEqualTo(100);
        assertThat(Utils.parseInt(null, -50)).isEqualTo(-50);
    }

    @Test
    void parseIntHandlesWhitespace() {
        assertThat(Utils.parseInt(" 123 ", 0)).isEqualTo(123);
        assertThat(Utils.parseInt("1 2 3", 0)).isEqualTo(123);
        assertThat(Utils.parseInt("\t456\t", 0)).isEqualTo(456);
        assertThat(Utils.parseInt("\n789\n", 0)).isEqualTo(789);
    }

    @Test
    void parseIntReturnsDefaultForOverflow() {
        String tooLarge = "9999999999999999";  // Exceeds Integer.MAX_VALUE
        assertThat(Utils.parseInt(tooLarge, -1)).isEqualTo(-1);

        String tooSmall = "-9999999999999999";  // Below Integer.MIN_VALUE
        assertThat(Utils.parseInt(tooSmall, -1)).isEqualTo(-1);
    }

    @Test
    void parseIntHandlesNegativeDefaults() {
        assertThat(Utils.parseInt("invalid", -1)).isEqualTo(-1);
        assertThat(Utils.parseInt(null, -999)).isEqualTo(-999);
    }

    @Test
    void parseIntHandlesZeroDefault() {
        assertThat(Utils.parseInt("invalid", 0)).isEqualTo(0);
        assertThat(Utils.parseInt("", 0)).isEqualTo(0);
    }

    // ==================== reverse() Tests ====================

    @Test
    void reverseReversesArrayInPlace() {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        Utils.reverse(array);
        assertThat(array).containsExactly(5.0, 4.0, 3.0, 2.0, 1.0);
    }

    @Test
    void reverseHandlesEvenLengthArray() {
        double[] array = {1.0, 2.0, 3.0, 4.0};
        Utils.reverse(array);
        assertThat(array).containsExactly(4.0, 3.0, 2.0, 1.0);
    }

    @Test
    void reverseHandlesOddLengthArray() {
        double[] array = {1.0, 2.0, 3.0};
        Utils.reverse(array);
        assertThat(array).containsExactly(3.0, 2.0, 1.0);
    }

    @Test
    void reverseHandlesSingleElementArray() {
        double[] array = {42.0};
        Utils.reverse(array);
        assertThat(array).containsExactly(42.0);
    }

    @Test
    void reverseHandlesEmptyArray() {
        double[] array = {};
        Utils.reverse(array);
        assertThat(array).isEmpty();
    }

    @Test
    void reverseHandlesTwoElementArray() {
        double[] array = {1.0, 2.0};
        Utils.reverse(array);
        assertThat(array).containsExactly(2.0, 1.0);
    }

    @Test
    void reverseHandlesNegativeNumbers() {
        double[] array = {-1.0, -2.0, -3.0};
        Utils.reverse(array);
        assertThat(array).containsExactly(-3.0, -2.0, -1.0);
    }

    @Test
    void reverseHandlesDecimalNumbers() {
        double[] array = {1.1, 2.2, 3.3};
        Utils.reverse(array);
        assertThat(array).containsExactly(3.3, 2.2, 1.1);
    }

    @Test
    void reverseThrowsOnNull() {
        assertThatThrownBy(() -> Utils.reverse(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Array cannot be null");
    }

    @Test
    void reverseIsIdempotentWhenAppliedTwice() {
        double[] original = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        Utils.reverse(array);
        Utils.reverse(array);
        assertThat(array).containsExactly(original);
    }

    // ==================== contains() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1.0",      // first element
            "3.0",      // middle element
            "5.0",      // last element
            "2.5",      // decimal in array
    })
    void containsReturnsTrueWhenValueExists(double value) {
        double[] array = {1.0, 2.0, 3.0, 2.5, 5.0};
        assertThat(Utils.contains(array, value)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "0.0",      // before range
            "6.0",      // after range
            "2.1",      // not in array
            "99.9",     // far out of range
    })
    void containsReturnsFalseWhenValueDoesNotExist(double value) {
        double[] array = {1.0, 2.0, 3.0, 5.0};
        assertThat(Utils.contains(array, value)).isFalse();
    }

    @Test
    void containsHandlesEmptyArray() {
        double[] array = {};
        assertThat(Utils.contains(array, 1.0)).isFalse();
    }

    @Test
    void containsHandlesSingleElementArray() {
        double[] array = {42.0};
        assertThat(Utils.contains(array, 42.0)).isTrue();
        assertThat(Utils.contains(array, 43.0)).isFalse();
    }

    @Test
    void containsHandlesNegativeNumbers() {
        double[] array = {-1.0, -2.0, -3.0};
        assertThat(Utils.contains(array, -2.0)).isTrue();
        assertThat(Utils.contains(array, -4.0)).isFalse();
    }

    @Test
    void containsHandlesZero() {
        double[] array = {-1.0, 0.0, 1.0};
        assertThat(Utils.contains(array, 0.0)).isTrue();
    }

    @Test
    void containsHandlesDuplicates() {
        double[] array = {1.0, 2.0, 2.0, 3.0};
        assertThat(Utils.contains(array, 2.0)).isTrue();
    }

    @Test
    void containsUsesDoubleCompare() {
        // Ensures proper double comparison (not ==)
        double[] array = {0.1 + 0.2};  // This equals 0.30000000000000004 due to floating point
        assertThat(Utils.contains(array, 0.3)).isFalse();
        assertThat(Utils.contains(array, 0.1 + 0.2)).isTrue();
    }

    @Test
    void containsHandlesSpecialDoubleValues() {
        double[] array = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        assertThat(Utils.contains(array, Double.POSITIVE_INFINITY)).isTrue();
        assertThat(Utils.contains(array, Double.NEGATIVE_INFINITY)).isTrue();
        assertThat(Utils.contains(array, Double.NaN)).isTrue();
    }

    @Test
    void containsThrowsOnNull() {
        assertThatThrownBy(() -> Utils.contains(null, 1.0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Array cannot be null");
    }

    // ==================== indexOf() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1.0, 0",       // first element
            "3.0, 2",       // middle element
            "5.0, 4",       // last element
            "2.0, 1",       // second element
    })
    void indexOfReturnsIndexWhenValueExists(double value, int expectedIndex) {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(Utils.indexOf(array, value)).isEqualTo(expectedIndex);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0",      // before range
            "6.0",      // after range
            "2.5",      // not in array
            "99.9",     // far out of range
    })
    void indexOfReturnsMinusOneWhenValueDoesNotExist(double value) {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        assertThat(Utils.indexOf(array, value)).isEqualTo(-1);
    }

    @Test
    void indexOfReturnsFirstOccurrenceOfDuplicates() {
        double[] array = {1.0, 2.0, 2.0, 3.0, 2.0};
        assertThat(Utils.indexOf(array, 2.0)).isEqualTo(1);  // First occurrence at index 1
    }

    @Test
    void indexOfHandlesEmptyArray() {
        double[] array = {};
        assertThat(Utils.indexOf(array, 1.0)).isEqualTo(-1);
    }

    @Test
    void indexOfHandlesSingleElementArray() {
        double[] array = {42.0};
        assertThat(Utils.indexOf(array, 42.0)).isEqualTo(0);
        assertThat(Utils.indexOf(array, 43.0)).isEqualTo(-1);
    }

    @Test
    void indexOfHandlesNegativeNumbers() {
        double[] array = {-3.0, -2.0, -1.0};
        assertThat(Utils.indexOf(array, -2.0)).isEqualTo(1);
        assertThat(Utils.indexOf(array, -4.0)).isEqualTo(-1);
    }

    @Test
    void indexOfHandlesZero() {
        double[] array = {-1.0, 0.0, 1.0};
        assertThat(Utils.indexOf(array, 0.0)).isEqualTo(1);
    }

    @Test
    void indexOfUsesDoubleCompare() {
        double[] array = {0.1, 0.2, 0.1 + 0.2};
        assertThat(Utils.indexOf(array, 0.1 + 0.2)).isEqualTo(2);
        assertThat(Utils.indexOf(array, 0.3)).isEqualTo(-1);
    }

    @Test
    void indexOfHandlesSpecialDoubleValues() {
        double[] array = {1.0, Double.POSITIVE_INFINITY, -1.0, Double.NEGATIVE_INFINITY};
        assertThat(Utils.indexOf(array, Double.POSITIVE_INFINITY)).isEqualTo(1);
        assertThat(Utils.indexOf(array, Double.NEGATIVE_INFINITY)).isEqualTo(3);
        double[] nanArray = {1.0, Double.NaN, 2.0};
        assertThat(Utils.indexOf(nanArray, Double.NaN)).isEqualTo(1);
    }

    @Test
    void indexOfThrowsOnNull() {
        assertThatThrownBy(() -> Utils.indexOf(null, 1.0))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Array cannot be null");
    }

    // ==================== copyOfRange() Tests ====================

    @Test
    void copyOfRangeCopiesEntireArray() {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] copy = Utils.copyOfRange(array, 0, 5);
        assertThat(copy).containsExactly(1.0, 2.0, 3.0, 4.0, 5.0);
        // Verify it's a copy, not the same reference
        assertThat(copy).isNotSameAs(array);
    }

    @Test
    void copyOfRangeCopiesSubrange() {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] copy = Utils.copyOfRange(array, 1, 4);
        assertThat(copy).containsExactly(2.0, 3.0, 4.0);
    }

    @Test
    void copyOfRangeCopiesSingleElement() {
        double[] array = {1.0, 2.0, 3.0};
        double[] copy = Utils.copyOfRange(array, 1, 2);
        assertThat(copy).containsExactly(2.0);
    }

    @Test
    void copyOfRangeCreatesEmptyArrayWhenFromEqualsTo() {
        double[] array = {1.0, 2.0, 3.0};
        double[] copy = Utils.copyOfRange(array, 2, 2);
        assertThat(copy).isEmpty();
    }

    @Test
    void copyOfRangeCopiesFromStart() {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] copy = Utils.copyOfRange(array, 0, 3);
        assertThat(copy).containsExactly(1.0, 2.0, 3.0);
    }

    @Test
    void copyOfRangeCopiesToEnd() {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] copy = Utils.copyOfRange(array, 3, 5);
        assertThat(copy).containsExactly(4.0, 5.0);
    }

    @Test
    void copyOfRangeDoesNotModifyOriginal() {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] copy = Utils.copyOfRange(array, 1, 4);
        copy[0] = 999.0;
        assertThat(array[1]).isEqualTo(2.0);  // Original unchanged
    }

    @Test
    void copyOfRangeThrowsOnNull() {
        assertThatThrownBy(() -> Utils.copyOfRange(null, 0, 5))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Array cannot be null");
    }

    @Test
    void copyOfRangeThrowsOnNegativeFromIndex() {
        double[] array = {1.0, 2.0, 3.0};
        assertThatThrownBy(() -> Utils.copyOfRange(array, -1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid range")
                .hasMessageContaining("fromIndex=-1");
    }

    @Test
    void copyOfRangeThrowsOnToIndexGreaterThanLength() {
        double[] array = {1.0, 2.0, 3.0};
        assertThatThrownBy(() -> Utils.copyOfRange(array, 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid range")
                .hasMessageContaining("toIndex=10")
                .hasMessageContaining("length=3");
    }

    @Test
    void copyOfRangeThrowsOnFromIndexGreaterThanToIndex() {
        double[] array = {1.0, 2.0, 3.0};
        assertThatThrownBy(() -> Utils.copyOfRange(array, 2, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid range")
                .hasMessageContaining("fromIndex=2")
                .hasMessageContaining("toIndex=1");
    }

    @Test
    void copyOfRangeHandlesEmptyArray() {
        double[] array = {};
        double[] copy = Utils.copyOfRange(array, 0, 0);
        assertThat(copy).isEmpty();
    }

    // ==================== repeat() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "abc, 3, abcabcabc",            // simple repeat
            "x, 5, xxxxx",                  // single character
            "12, 4, 12121212",              // numbers
            "hello, 1, hello",              // repeat once (no change)
            "test, 0, ''",                  // repeat zero times (empty)
    })
    void repeatRepeatsStringCorrectly(String input, int count, String expected) {
        assertThat(Utils.repeat(input, count)).isEqualTo(expected);
    }

    @Test
    void repeatHandlesEmptyString() {
        assertThat(Utils.repeat("", 5)).isEqualTo("");
        assertThat(Utils.repeat("", 0)).isEqualTo("");
    }

    @Test
    void repeatHandlesCountOfOne() {
        assertThat(Utils.repeat("hello", 1)).isEqualTo("hello");
    }

    @Test
    void repeatHandlesCountOfZero() {
        assertThat(Utils.repeat("hello", 0)).isEqualTo("");
    }

    @Test
    void repeatHandlesLongStrings() {
        String result = Utils.repeat("abc", 10);
        assertThat(result).hasSize(30);
        assertThat(result).startsWith("abcabcabc");
        assertThat(result).endsWith("abcabcabc");
    }

    @Test
    void repeatHandlesSpecialCharacters() {
        assertThat(Utils.repeat("!@#", 3)).isEqualTo("!@#!@#!@#");
        assertThat(Utils.repeat("\n", 3)).isEqualTo("\n\n\n");
        assertThat(Utils.repeat("\t", 2)).isEqualTo("\t\t");
    }

    @Test
    void repeatThrowsOnNull() {
        assertThatThrownBy(() -> Utils.repeat(null, 3))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("String cannot be null");
    }

    @Test
    void repeatThrowsOnNegativeCount() {
        assertThatThrownBy(() -> Utils.repeat("test", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Count cannot be negative")
                .hasMessageContaining("-1");
    }

    @Test
    void repeatHandlesMultipleWords() {
        assertThat(Utils.repeat("hello world ", 2)).isEqualTo("hello world hello world ");
    }

    // ==================== padLeft() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "123, 5, 0, 00123",             // pad with zeros
            "abc, 5, ' ', '  abc'",         // pad with spaces
            "x, 3, *, **x",                 // pad with asterisks
            "hello, 10, -, -----hello",     // longer padding
    })
    void padLeftPadsCorrectly(String input, int length, char padChar, String expected) {
        assertThat(Utils.padLeft(input, length, padChar)).isEqualTo(expected);
    }

    @Test
    void padLeftReturnsOriginalWhenAlreadyLongEnough() {
        assertThat(Utils.padLeft("hello", 5, ' ')).isEqualTo("hello");
        assertThat(Utils.padLeft("hello", 3, ' ')).isEqualTo("hello");
    }

    @Test
    void padLeftHandlesEmptyString() {
        assertThat(Utils.padLeft("", 5, 'x')).isEqualTo("xxxxx");
    }

    @Test
    void padLeftHandlesSingleCharacter() {
        assertThat(Utils.padLeft("a", 5, 'b')).isEqualTo("bbbba");
    }

    @Test
    void padLeftHandlesTargetLengthZero() {
        assertThat(Utils.padLeft("hello", 0, ' ')).isEqualTo("hello");
    }

    @Test
    void padLeftHandlesTargetLengthOne() {
        assertThat(Utils.padLeft("a", 1, ' ')).isEqualTo("a");
        assertThat(Utils.padLeft("", 1, 'x')).isEqualTo("x");
    }

    @Test
    void padLeftHandlesSpecialCharacters() {
        assertThat(Utils.padLeft("test", 8, '\t')).isEqualTo("\t\t\t\ttest");
        assertThat(Utils.padLeft("x", 3, '\n')).isEqualTo("\n\nx");
    }

    @Test
    void padLeftThrowsOnNull() {
        assertThatThrownBy(() -> Utils.padLeft(null, 5, ' '))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("String cannot be null");
    }

    @Test
    void padLeftPreservesOriginalString() {
        String original = "hello";
        String padded = Utils.padLeft(original, 10, ' ');
        assertThat(original).isEqualTo("hello");  // Original unchanged
        assertThat(padded).isNotSameAs(original);
    }

    // ==================== padRight() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "123, 5, 0, 12300",             // pad with zeros
            "abc, 5, ' ', 'abc  '",         // pad with spaces
            "x, 3, *, x**",                 // pad with asterisks
            "hello, 10, -, hello-----",     // longer padding
    })
    void padRightPadsCorrectly(String input, int length, char padChar, String expected) {
        assertThat(Utils.padRight(input, length, padChar)).isEqualTo(expected);
    }

    @Test
    void padRightReturnsOriginalWhenAlreadyLongEnough() {
        assertThat(Utils.padRight("hello", 5, ' ')).isEqualTo("hello");
        assertThat(Utils.padRight("hello", 3, ' ')).isEqualTo("hello");
    }

    @Test
    void padRightHandlesEmptyString() {
        assertThat(Utils.padRight("", 5, 'x')).isEqualTo("xxxxx");
    }

    @Test
    void padRightHandlesSingleCharacter() {
        assertThat(Utils.padRight("a", 5, 'b')).isEqualTo("abbbb");
    }

    @Test
    void padRightHandlesTargetLengthZero() {
        assertThat(Utils.padRight("hello", 0, ' ')).isEqualTo("hello");
    }

    @Test
    void padRightHandlesTargetLengthOne() {
        assertThat(Utils.padRight("a", 1, ' ')).isEqualTo("a");
        assertThat(Utils.padRight("", 1, 'x')).isEqualTo("x");
    }

    @Test
    void padRightHandlesSpecialCharacters() {
        assertThat(Utils.padRight("test", 8, '\t')).isEqualTo("test\t\t\t\t");
        assertThat(Utils.padRight("x", 3, '\n')).isEqualTo("x\n\n");
    }

    @Test
    void padRightThrowsOnNull() {
        assertThatThrownBy(() -> Utils.padRight(null, 5, ' '))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("String cannot be null");
    }

    @Test
    void padRightPreservesOriginalString() {
        String original = "hello";
        String padded = Utils.padRight(original, 10, ' ');
        assertThat(original).isEqualTo("hello");  // Original unchanged
        assertThat(padded).isNotSameAs(original);
    }

    @Test
    void padLeftAndPadRightWorkTogether() {
        String input = "test";
        String leftPadded = Utils.padLeft(input, 8, ' ');
        String bothPadded = Utils.padRight(leftPadded, 12, ' ');
        assertThat(bothPadded).isEqualTo("    test    ");
    }

    // ==================== isEmpty() Tests ====================

    @Test
    void isEmptyReturnsTrueForNullOrEmpty() {
        assertThat(Utils.isEmpty(null)).isTrue();
        assertThat(Utils.isEmpty("")).isTrue();
    }

    @Test
    void isEmptyReturnsFalseForNonEmpty() {
        assertThat(Utils.isEmpty("hello")).isFalse();
        assertThat(Utils.isEmpty("a")).isFalse();
        assertThat(Utils.isEmpty(" ")).isFalse();  // Space is not empty
        assertThat(Utils.isEmpty("   ")).isFalse();  // Whitespace is not empty
    }

    // ==================== indexOfAny() Tests ====================

    @Test
    void indexOfAnyFindsFirstOccurrence() {
        assertThat(Utils.indexOfAny("hello", 'h')).isEqualTo(0);      // first character
        assertThat(Utils.indexOfAny("hello", 'e')).isEqualTo(1);      // second character
        assertThat(Utils.indexOfAny("hello", 'o')).isEqualTo(4);      // last character
        assertThat(Utils.indexOfAny("hello", 'l')).isEqualTo(2);      // first occurrence of 'l'
        assertThat(Utils.indexOfAny("abc", 'a', 'b', 'c')).isEqualTo(0);  // multiple search chars - finds first
    }

    @Test
    void indexOfAnyFindsFirstOfMultipleChars() {
        assertThat(Utils.indexOfAny("hello world", 'w', 'o')).isEqualTo(4);  // 'o' at index 4
        assertThat(Utils.indexOfAny("hello world", 'x', 'e')).isEqualTo(1);  // 'e' at index 1
        assertThat(Utils.indexOfAny("test", 't', 'e', 's')).isEqualTo(0);    // 't' at index 0
    }

    @Test
    void indexOfAnyReturnsMinusOneWhenNotFound() {
        assertThat(Utils.indexOfAny("hello", 'x', 'y', 'z')).isEqualTo(-1);
        assertThat(Utils.indexOfAny("abc", 'd')).isEqualTo(-1);
    }

    @Test
    void indexOfAnyHandlesNullString() {
        assertThat(Utils.indexOfAny(null, 'a', 'b')).isEqualTo(-1);
    }

    @Test
    void indexOfAnyHandlesEmptyString() {
        assertThat(Utils.indexOfAny("", 'a', 'b')).isEqualTo(-1);
    }

    @Test
    void indexOfAnyHandlesNullSearchChars() {
        assertThat(Utils.indexOfAny("hello", (char[]) null)).isEqualTo(-1);
    }

    @Test
    void indexOfAnyHandlesEmptySearchChars() {
        assertThat(Utils.indexOfAny("hello")).isEqualTo(-1);
    }

    @Test
    void indexOfAnyHandlesSingleSearchChar() {
        assertThat(Utils.indexOfAny("hello", 'l')).isEqualTo(2);
        assertThat(Utils.indexOfAny("hello", 'x')).isEqualTo(-1);
    }

    @Test
    void indexOfAnyHandlesSpecialCharacters() {
        assertThat(Utils.indexOfAny("hello!world", '!', '?')).isEqualTo(5);
        assertThat(Utils.indexOfAny("a\tb\nc", '\t', '\n')).isEqualTo(1);
    }

    // ==================== stripEnd() Tests ====================

    @ParameterizedTest
    @CsvSource({
            "hello000, 0, hello",       // strip trailing zeros
            "test..., ., test",         // strip trailing dots
            "abc   , ' ', abc",         // strip trailing spaces
            "xyz###, #, xyz",           // strip trailing hashes
            "aaa, a, ''",               // strip all characters
    })
    void stripEndRemovesTrailingCharacters(String input, String stripChar, String expected) {
        assertThat(Utils.stripEnd(input, stripChar)).isEqualTo(expected);
    }

    @Test
    void stripEndDoesNotRemoveNonTrailingChars() {
        assertThat(Utils.stripEnd("0hello0", "0")).isEqualTo("0hello");
        assertThat(Utils.stripEnd("xteXtxxx", "x")).isEqualTo("xteXt");
    }

    @Test
    void stripEndHandlesStringWithoutTrailingChars() {
        assertThat(Utils.stripEnd("hello", "x")).isEqualTo("hello");
        assertThat(Utils.stripEnd("test", "a")).isEqualTo("test");
    }

    @Test
    void stripEndHandlesEmptyString() {
        assertThat(Utils.stripEnd("", "x")).isEqualTo("");
    }

    @Test
    void stripEndHandlesNullString() {
        assertThat(Utils.stripEnd(null, "x")).isNull();
    }

    @Test
    void stripEndHandlesNullStripChar() {
        assertThat(Utils.stripEnd("hello", null)).isEqualTo("hello");
    }

    @Test
    void stripEndHandlesEmptyStripChar() {
        assertThat(Utils.stripEnd("hello", "")).isEqualTo("hello");
    }

    @Test
    void stripEndUsesOnlyFirstCharOfStripString() {
        // If stripChar has multiple characters, only the first is used
        assertThat(Utils.stripEnd("hello000", "0x")).isEqualTo("hello");
        assertThat(Utils.stripEnd("testabc", "abc")).isEqualTo("testabc");  // Strips only 'a', but 'testabc' doesn't end with 'a'
    }

    @Test
    void stripEndHandlesAllSameCharacter() {
        assertThat(Utils.stripEnd("aaaa", "a")).isEqualTo("");
    }

    @Test
    void stripEndHandlesSingleCharString() {
        assertThat(Utils.stripEnd("x", "x")).isEqualTo("");
        assertThat(Utils.stripEnd("x", "y")).isEqualTo("x");
    }

    // ==================== join() Tests ====================

    @Test
    void joinJoinsArrayWithDelimiter() {
        Object[] array = {"a", "b", "c"};
        assertThat(Utils.join(array, ", ")).isEqualTo("a, b, c");
        assertThat(Utils.join(array, "-")).isEqualTo("a-b-c");
        assertThat(Utils.join(array, "")).isEqualTo("abc");
    }

    @Test
    void joinHandlesNullDelimiter() {
        Object[] array = {"a", "b", "c"};
        assertThat(Utils.join(array, null)).isEqualTo("abc");  // Treated as empty string
    }

    @Test
    void joinHandlesNullArray() {
        assertThat(Utils.join(null, ",")).isEqualTo("");
    }

    @Test
    void joinHandlesEmptyArray() {
        assertThat(Utils.join(new Object[0], ",")).isEqualTo("");
    }

    @Test
    void joinHandlesSingleElement() {
        Object[] array = {"hello"};
        assertThat(Utils.join(array, ", ")).isEqualTo("hello");
    }

    @Test
    void joinHandlesTwoElements() {
        Object[] array = {"a", "b"};
        assertThat(Utils.join(array, " and ")).isEqualTo("a and b");
    }

    @Test
    void joinHandlesNonStringObjects() {
        Object[] array = {1, 2, 3, 4.5};
        assertThat(Utils.join(array, ", ")).isEqualTo("1, 2, 3, 4.5");
    }

    @Test
    void joinHandlesMixedTypes() {
        Object[] array = {"hello", 123, true, 45.67};
        assertThat(Utils.join(array, " | ")).isEqualTo("hello | 123 | true | 45.67");
    }

    @Test
    void joinHandlesNullElements() {
        Object[] array = {"a", null, "c"};
        assertThat(Utils.join(array, ", ")).isEqualTo("a, null, c");
    }

    @Test
    void joinHandlesEmptyStrings() {
        Object[] array = {"", "a", "", "b", ""};
        assertThat(Utils.join(array, ",")).isEqualTo(",a,,b,");
    }

    @Test
    void joinHandlesMultiCharacterDelimiter() {
        Object[] array = {"x", "y", "z"};
        assertThat(Utils.join(array, " --> ")).isEqualTo("x --> y --> z");
    }

    @Test
    void joinHandlesSpecialCharacterDelimiter() {
        Object[] array = {"a", "b", "c"};
        assertThat(Utils.join(array, "\n")).isEqualTo("a\nb\nc");
        assertThat(Utils.join(array, "\t")).isEqualTo("a\tb\tc");
    }
}
