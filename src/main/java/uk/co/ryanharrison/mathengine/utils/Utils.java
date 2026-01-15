package uk.co.ryanharrison.mathengine.utils;

/**
 * Utility methods for string manipulation and validation used throughout the Math Engine library.
 * <p>
 * This class provides various helper methods for:
 * </p>
 * <ul>
 *     <li>Numeric string validation</li>
 *     <li>Parenthesis matching and manipulation</li>
 *     <li>String standardization and formatting</li>
 * </ul>
 * <p>
 * All methods are static and the class cannot be instantiated.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Check if a string is numeric
 * boolean isNum = Utils.isNumeric("123.45");  // true
 * boolean isNotNum = Utils.isNumeric("abc");  // false
 *
 * // Remove outer parenthesis
 * String expr = Utils.removeOuterParenthesis("(2+3)");  // "2+3"
 *
 * // Standardize a string (remove whitespace, lowercase)
 * String std = Utils.standardiseString("  Hello World  ");  // "helloworld"
 *
 * // Convert to title case
 * String title = Utils.toTitleCase("hello world");  // "Hello World"
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class Utils {

    /**
     * Not permitted to create an instance of this class.
     */
    private Utils() {
    }

    /**
     * Determines whether a string can be considered to be numeric.
     * <p>
     * A string is considered numeric if it can be parsed as a valid number, including:
     * </p>
     * <ul>
     *     <li>Integers: "123", "-456"</li>
     *     <li>Decimals: "123.45", "-67.89"</li>
     *     <li>Scientific notation: "1.23e10", "-4.5E-6"</li>
     *     <li>Hexadecimal: "0x1A2B"</li>
     * </ul>
     * <p>
     * Whitespace is automatically removed before validation, so " 1 2 3 " is considered numeric.
     * </p>
     *
     * @param string the string to test, may be null
     * @return {@code true} if the string is numeric, {@code false} otherwise (including null input)
     *
     * @see #standardiseString(String)
     */
    public static boolean isNumeric(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }

        // Remove all whitespace
        String str = deleteWhitespace(string);

        if (str.isEmpty()) {
            return false;
        }

        // Handle hexadecimal
        if (str.startsWith("0x") || str.startsWith("0X") ||
                str.startsWith("-0x") || str.startsWith("-0X")) {
            try {
                String hexPart = str.startsWith("-") ? str.substring(3) : str.substring(2);
                Long.parseLong(hexPart, 16);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // Try to parse as double (handles integers, decimals, scientific notation)
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Finds the index of the matching end character corresponding to a begin character.
     * <p>
     * This method is useful for finding matching parentheses, brackets, or braces in an expression.
     * It properly handles nested pairs by counting open and close characters.
     * </p>
     * <p>
     * For example, in the expression {@code "((a+b)*(c+d))"}, starting at index 0 with begin='('
     * and end=')', this method returns 13 (the index of the final closing parenthesis).
     * </p>
     *
     * @param expression the string to search in, must not be null
     * @param index      the starting index (typically the index of a begin character)
     * @param begin      the opening/beginning character (e.g., '(', '[', '{')
     * @param end        the closing/ending character (e.g., ')', ']', '}')
     * @return the index of the matching end character, or {@code index} if no match is found
     * @throws NullPointerException if expression is null
     * @throws StringIndexOutOfBoundsException if index is negative or >= expression.length()
     */
    public static int matchingCharacterIndex(String expression, int index, char begin, char end) {
        if (expression == null) {
            throw new NullPointerException("Expression cannot be null");
        }
        if (index < 0 || index >= expression.length()) {
            throw new StringIndexOutOfBoundsException("Index out of bounds: " + index);
        }

        int len = expression.length();
        int i = index;
        int count = 0;

        while (i < len) {
            if (expression.charAt(i) == begin) {
                count++;
            } else if (expression.charAt(i) == end) {
                count--;
            }

            if (count == 0) {
                return i;
            }

            i++;
        }

        return index;
    }

    /**
     * Removes outer parentheses from a string if they wrap the entire expression.
     * <p>
     * This method only removes parentheses that completely enclose the expression.
     * It will repeatedly remove outer pairs until no more can be removed.
     * </p>
     * <p>
     * Examples:
     * </p>
     * <ul>
     *     <li>{@code "(2+3)" → "2+3"}</li>
     *     <li>{@code "((2+3))" → "2+3"}</li>
     *     <li>{@code "(2+3)+(4+5)" → "(2+3)+(4+5)"} (not removed - doesn't wrap entire expression)</li>
     *     <li>{@code "2+3" → "2+3"} (no parentheses to remove)</li>
     * </ul>
     *
     * @param source the string to process, must not be null
     * @return the source string with outer parentheses removed if present
     * @throws NullPointerException if source is null
     */
    public static String removeOuterParenthesis(String source) {
        if (source == null) {
            throw new NullPointerException("Source cannot be null");
        }

        if (!source.startsWith("(")) {
            return source;
        }

        int i;
        while ((i = matchingCharacterIndex(source, 0, '(', ')')) == source.length() - 1) {
            if (source.charAt(0) == '(' && i == source.length() - 1) {
                source = source.substring(1, source.length() - 1);
            }

            if (!source.startsWith("(")) {
                break;
            }
        }

        return source;
    }

    /**
     * Removes redundant nested double parentheses from a string.
     * <p>
     * This method recursively removes layers of double parentheses "((...))" when they exist
     * at the beginning and end, and the outer pair is redundant (i.e., the parentheses depth
     * never goes negative when scanning from position 1 to length-2). It continues removing
     * layers until only a single layer of parentheses remains.
     * </p>
     * <p>
     * Examples:
     * </p>
     * <ul>
     *     <li>{@code "((x))" → "(x)"} (removes one redundant layer)</li>
     *     <li>{@code "((a+b))" → "(a+b)"}</li>
     *     <li>{@code "((((x))))" → "(x)"} (removes until single layer remains)</li>
     *     <li>{@code "((a)+(b))" → "(a)+(b)"} (removes redundant outer layer)</li>
     *     <li>{@code "(a)+(b)" → "(a)+(b)"} (no change - doesn't start/end with double parens)</li>
     *     <li>{@code "(x)" → "(x)"} (no change - only single layer)</li>
     *     <li>{@code "x" → "x"} (no change - no parentheses)</li>
     * </ul>
     *
     * @param s the string to process, must not be null
     * @return the string with redundant nested parentheses removed (down to single layer)
     * @throws NullPointerException if s is null
     */
    public static String removeRedundantParentheses(String s) {
        if (s == null) {
            throw new NullPointerException("String cannot be null");
        }

        if (s.startsWith("((") && s.endsWith("))")) {
            // Check if outer parens are redundant
            int depth = 0;
            boolean canRemove = true;
            for (int i = 1; i < s.length() - 1; i++) {
                if (s.charAt(i) == '(') depth++;
                if (s.charAt(i) == ')') depth--;
                if (depth < 0) {
                    canRemove = false;
                    break;
                }
            }
            if (canRemove) {
                return removeRedundantParentheses(s.substring(1, s.length() - 1));
            }
        }
        return s;
    }

    /**
     * Standardizes a string by removing all whitespace and converting to lowercase.
     * <p>
     * This is useful for normalizing user input, comparing strings in a case-insensitive
     * manner, or preparing strings for parsing.
     * </p>
     * <p>
     * Examples:
     * </p>
     * <ul>
     *     <li>{@code "  Hello World  " → "helloworld"}</li>
     *     <li>{@code "A B C" → "abc"}</li>
     *     <li>{@code null → null}</li>
     * </ul>
     *
     * @param string the string to standardize, may be null
     * @return the standardized string with no whitespace and in lowercase, or null if input is null
     *
     * @see #deleteWhitespace(String)
     */
    public static String standardiseString(String string) {
        if (string == null) {
            return null;
        }
        return deleteWhitespace(string).toLowerCase();
    }

    /**
     * Converts a string to title case where the first letter of each word is capitalized.
     * <p>
     * Words are separated by spaces. The first character after a space is converted to
     * title case using {@link Character#toTitleCase(char)}.
     * </p>
     * <p>
     * Examples:
     * </p>
     * <ul>
     *     <li>{@code "hello world" → "Hello World"}</li>
     *     <li>{@code "the quick brown fox" → "The Quick Brown Fox"}</li>
     *     <li>{@code "already Title Case" → "Already Title Case"}</li>
     * </ul>
     *
     * @param input the string to convert, must not be null
     * @return the input string in title case
     * @throws NullPointerException if input is null
     */
    public static String toTitleCase(String input) {
        if (input == null) {
            throw new NullPointerException("Input cannot be null");
        }

        StringBuilder titleCase = new StringBuilder(input.length());
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }

    // ==================== Additional String Utility Methods ====================

    /**
     * Checks if a string is null or empty.
     * <p>
     * A string is considered empty if it is null or has length 0.
     * </p>
     *
     * @param string the string to check, may be null
     * @return {@code true} if the string is null or empty, {@code false} otherwise
     */
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Finds the index of the first occurrence of any character from a search set within a string.
     * <p>
     * This is similar to String.indexOf() but searches for any of multiple characters.
     * Returns -1 if none of the search characters are found.
     * </p>
     *
     * @param string      the string to search, may be null
     * @param searchChars the characters to search for
     * @return the index of the first occurrence of any search character, or -1 if not found
     */
    public static int indexOfAny(String string, char... searchChars) {
        if (string == null || string.isEmpty() || searchChars == null || searchChars.length == 0) {
            return -1;
        }

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            for (char searchChar : searchChars) {
                if (c == searchChar) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Strips trailing characters from the end of a string.
     * <p>
     * All occurrences of the specified character are removed from the end of the string.
     * </p>
     *
     * @param string    the string to strip, may be null
     * @param stripChar the character to remove from the end
     * @return the string with trailing characters removed, or null if input is null
     */
    public static String stripEnd(String string, String stripChar) {
        if (string == null || string.isEmpty() || stripChar == null || stripChar.isEmpty()) {
            return string;
        }

        char strip = stripChar.charAt(0);
        int end = string.length();

        while (end > 0 && string.charAt(end - 1) == strip) {
            end--;
        }

        return string.substring(0, end);
    }

    /**
     * Joins an array of strings with a delimiter.
     * <p>
     * Concatenates the string representations of the array elements with the specified delimiter
     * between each element.
     * </p>
     *
     * @param array     the array of objects to join, may be null or empty
     * @param delimiter the delimiter to use between elements, may be null (treated as empty string)
     * @return the joined string, or empty string if array is null or empty
     */
    public static String join(Object[] array, String delimiter) {
        if (array == null || array.length == 0) {
            return "";
        }

        if (delimiter == null) {
            delimiter = "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append(array[i]);
        }

        return result.toString();
    }

    // ==================== Number Formatting and Parsing ====================

    /**
     * Formats a double to a string with a specified number of decimal places.
     * <p>
     * Trailing zeros are removed unless decimalPlaces is 0.
     * </p>
     *
     * @param value         the value to format
     * @param decimalPlaces the maximum number of decimal places (0-10)
     * @return the formatted string
     * @throws IllegalArgumentException if decimalPlaces is negative or > 10
     */
    public static String formatDecimal(double value, int decimalPlaces) {
        if (decimalPlaces < 0 || decimalPlaces > 10) {
            throw new IllegalArgumentException(
                    "Decimal places must be between 0 and 10, got: " + decimalPlaces);
        }

        String format = "%." + decimalPlaces + "f";
        String result = String.format(format, value);

        // Remove trailing zeros (but keep at least one digit after decimal)
        if (decimalPlaces > 0 && result.contains(".")) {
            result = result.replaceAll("0+$", "").replaceAll("\\.$", "");
        }

        return result;
    }

    /**
     * Parses a string to a double, returning a default value if parsing fails.
     * <p>
     * This is useful for safe parsing without exception handling.
     * </p>
     *
     * @param string       the string to parse, may be null
     * @param defaultValue the value to return if parsing fails
     * @return the parsed double, or defaultValue if parsing fails
     */
    public static double parseDouble(String string, double defaultValue) {
        if (string == null || string.isEmpty()) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(deleteWhitespace(string));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Parses a string to an integer, returning a default value if parsing fails.
     *
     * @param string       the string to parse, may be null
     * @param defaultValue the value to return if parsing fails
     * @return the parsed integer, or defaultValue if parsing fails
     */
    public static int parseInt(String string, int defaultValue) {
        if (string == null || string.isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(deleteWhitespace(string));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== Array Utilities ====================

    /**
     * Reverses an array in place.
     * <p>
     * The original array is modified.
     * </p>
     *
     * @param array the array to reverse, must not be null
     * @throws NullPointerException if array is null
     */
    public static void reverse(double[] array) {
        if (array == null) {
            throw new NullPointerException("Array cannot be null");
        }

        int left = 0;
        int right = array.length - 1;

        while (left < right) {
            double temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    /**
     * Checks if an array contains a specific value.
     *
     * @param array the array to search, must not be null
     * @param value the value to search for
     * @return {@code true} if the array contains the value, {@code false} otherwise
     * @throws NullPointerException if array is null
     */
    public static boolean contains(double[] array, double value) {
        if (array == null) {
            throw new NullPointerException("Array cannot be null");
        }

        for (double element : array) {
            if (Double.compare(element, value) == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds the index of the first occurrence of a value in an array.
     *
     * @param array the array to search, must not be null
     * @param value the value to search for
     * @return the index of the first occurrence, or -1 if not found
     * @throws NullPointerException if array is null
     */
    public static int indexOf(double[] array, double value) {
        if (array == null) {
            throw new NullPointerException("Array cannot be null");
        }

        for (int i = 0; i < array.length; i++) {
            if (Double.compare(array[i], value) == 0) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Creates a copy of an array with all elements within a specified range.
     *
     * @param array     the source array, must not be null
     * @param fromIndex the starting index (inclusive)
     * @param toIndex   the ending index (exclusive)
     * @return a new array containing elements from fromIndex to toIndex-1
     * @throws NullPointerException     if array is null
     * @throws IllegalArgumentException if fromIndex > toIndex or indices are out of bounds
     */
    public static double[] copyOfRange(double[] array, int fromIndex, int toIndex) {
        if (array == null) {
            throw new NullPointerException("Array cannot be null");
        }
        if (fromIndex < 0 || toIndex > array.length || fromIndex > toIndex) {
            throw new IllegalArgumentException(
                    "Invalid range: fromIndex=" + fromIndex + ", toIndex=" + toIndex + ", length=" + array.length);
        }

        int length = toIndex - fromIndex;
        double[] result = new double[length];
        System.arraycopy(array, fromIndex, result, 0, length);
        return result;
    }

    // ==================== Additional String Utilities ====================

    /**
     * Repeats a string a specified number of times.
     *
     * @param string the string to repeat, must not be null
     * @param count  the number of times to repeat (must be non-negative)
     * @return the repeated string
     * @throws NullPointerException     if string is null
     * @throws IllegalArgumentException if count is negative
     */
    public static String repeat(String string, int count) {
        if (string == null) {
            throw new NullPointerException("String cannot be null");
        }
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative, got: " + count);
        }

        if (count == 0 || string.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder(string.length() * count);
        for (int i = 0; i < count; i++) {
            result.append(string);
        }

        return result.toString();
    }

    /**
     * Pads a string to a specified length by adding characters to the left.
     *
     * @param string  the string to pad, must not be null
     * @param length  the target length
     * @param padChar the character to use for padding
     * @return the padded string (or original if already >= length)
     * @throws NullPointerException if string is null
     */
    public static String padLeft(String string, int length, char padChar) {
        if (string == null) {
            throw new NullPointerException("String cannot be null");
        }

        if (string.length() >= length) {
            return string;
        }

        int padCount = length - string.length();
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < padCount; i++) {
            result.append(padChar);
        }
        result.append(string);

        return result.toString();
    }

    /**
     * Pads a string to a specified length by adding characters to the right.
     *
     * @param string  the string to pad, must not be null
     * @param length  the target length
     * @param padChar the character to use for padding
     * @return the padded string (or original if already >= length)
     * @throws NullPointerException if string is null
     */
    public static String padRight(String string, int length, char padChar) {
        if (string == null) {
            throw new NullPointerException("String cannot be null");
        }

        if (string.length() >= length) {
            return string;
        }

        int padCount = length - string.length();
        StringBuilder result = new StringBuilder(string);
        for (int i = 0; i < padCount; i++) {
            result.append(padChar);
        }

        return result.toString();
    }

    /**
     * Removes all whitespace characters from a string.
     * <p>
     * This method removes spaces, tabs, newlines, and all other characters that
     * {@link Character#isWhitespace(char)} considers to be whitespace.
     * </p>
     *
     * @param string the string to process, may be null
     * @return the string with all whitespace removed, or null if input is null
     */
    private static String deleteWhitespace(String string) {
        if (string == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!Character.isWhitespace(c)) {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}
