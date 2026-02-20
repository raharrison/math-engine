package uk.co.ryanharrison.mathengine.parser.function.string;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.STRING;

/**
 * Collection of string manipulation functions.
 */
public final class StringFunctions {

    private StringFunctions() {
    }

    // ==================== Basic String Operations ====================

    /**
     * String length
     */
    public static final MathFunction STRLEN = FunctionBuilder
            .named("strlen")
            .describedAs("Returns the number of characters in string str")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> new NodeRational(ctx.toStringValue(arg).length()));

    /**
     * Convert to uppercase
     */
    public static final MathFunction UPPER = FunctionBuilder
            .named("upper")
            .alias("uppercase", "toupper")
            .describedAs("Returns string str converted to uppercase")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> new NodeString(ctx.toStringValue(arg).toUpperCase()));

    /**
     * Convert to lowercase
     */
    public static final MathFunction LOWER = FunctionBuilder
            .named("lower")
            .alias("lowercase", "tolower")
            .describedAs("Returns string str converted to lowercase")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> new NodeString(ctx.toStringValue(arg).toLowerCase()));

    /**
     * Trim whitespace
     */
    public static final MathFunction TRIM = FunctionBuilder
            .named("trim")
            .describedAs("Returns string str with leading and trailing whitespace removed")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> new NodeString(ctx.toStringValue(arg).strip()));

    /**
     * Trim leading whitespace
     */
    public static final MathFunction LTRIM = FunctionBuilder
            .named("ltrim")
            .alias("trimleft")
            .describedAs("Returns string str with leading whitespace removed")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> new NodeString(ctx.toStringValue(arg).stripLeading()));

    /**
     * Trim trailing whitespace
     */
    public static final MathFunction RTRIM = FunctionBuilder
            .named("rtrim")
            .alias("trimright")
            .describedAs("Returns string str with trailing whitespace removed")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> new NodeString(ctx.toStringValue(arg).stripTrailing()));

    // ==================== Substring and Slicing ====================

    /**
     * Extract substring
     */
    public static final MathFunction SUBSTRING = FunctionBuilder
            .named("substring")
            .alias("substr", "mid")
            .describedAs("Returns a substring of string str from start, optionally limited to length characters")
            .withParams("str", "start")
            .withParams("str", "start", "length")
            .inCategory(STRING)
            .takingBetween(2, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                String s = ctx.toStringValue(args.get(0));
                int start = ctx.toInt(args.get(1));

                // Handle negative start index
                if (start < 0) start = Math.max(0, s.length() + start);
                start = Math.min(start, s.length());

                if (args.size() == 2) {
                    return new NodeString(s.substring(start));
                }

                int length = ctx.toInt(args.get(2));
                int end = Math.min(start + length, s.length());
                return new NodeString(s.substring(start, end));
            });

    /**
     * Get left portion of string
     */
    public static final MathFunction LEFT = FunctionBuilder
            .named("left")
            .describedAs("Returns the leftmost count characters of string str")
            .withParams("str", "count")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((s, n, ctx) -> {
                String str = ctx.toStringValue(s);
                int count = ctx.toInt(n);
                count = Math.max(0, Math.min(count, str.length()));
                return new NodeString(str.substring(0, count));
            });

    /**
     * Get right portion of string
     */
    public static final MathFunction RIGHT = FunctionBuilder
            .named("right")
            .describedAs("Returns the rightmost count characters of string str")
            .withParams("str", "count")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((s, n, ctx) -> {
                String str = ctx.toStringValue(s);
                int count = ctx.toInt(n);
                count = Math.max(0, Math.min(count, str.length()));
                return new NodeString(str.substring(str.length() - count));
            });

    /**
     * Get character at index
     */
    public static final MathFunction CHARAT = FunctionBuilder
            .named("charat")
            .alias("char")
            .describedAs("Returns the character of string str at the given index (0-based)")
            .withParams("str", "index")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((s, idx, ctx) -> {
                String str = ctx.toStringValue(s);
                int index = ctx.toInt(idx);

                // Handle negative index
                if (index < 0) index = str.length() + index;

                if (index < 0 || index >= str.length()) {
                    throw new IllegalArgumentException("charat: index " + index + " out of bounds");
                }
                return new NodeString(String.valueOf(str.charAt(index)));
            });

    // ==================== Search Operations ====================

    /**
     * Find index of substring
     */
    public static final MathFunction STRINDEXOF = FunctionBuilder
            .named("strindexof")
            .alias("strfind")
            .describedAs("Returns the first index of substr in string str (-1 if not found)")
            .withParams("str", "substr")
            .withParams("str", "substr", "start")
            .inCategory(STRING)
            .takingBetween(2, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                String s = ctx.toStringValue(args.get(0));
                String needle = ctx.toStringValue(args.get(1));
                int start = args.size() > 2 ? ctx.toInt(args.get(2)) : 0;
                return new NodeRational(s.indexOf(needle, start));
            });

    /**
     * Find last index of substring
     */
    public static final MathFunction STRLASTINDEXOF = FunctionBuilder
            .named("strlastindexof")
            .alias("strrfind")
            .describedAs("Returns the last index of substr in string str (-1 if not found)")
            .withParams("str", "substr")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((s, needle, ctx) -> {
                String str = ctx.toStringValue(s);
                String search = ctx.toStringValue(needle);
                return new NodeRational(str.lastIndexOf(search));
            });

    /**
     * Check if string contains substring
     */
    public static final MathFunction STRCONTAINS = FunctionBuilder
            .named("strcontains")
            .describedAs("Returns true if string str contains substr")
            .withParams("str", "substr")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((s, needle, ctx) -> {
                String str = ctx.toStringValue(s);
                String search = ctx.toStringValue(needle);
                return new NodeBoolean(str.contains(search));
            });

    /**
     * Check if string starts with prefix
     */
    public static final MathFunction STARTSWITH = FunctionBuilder
            .named("startswith")
            .describedAs("Returns true if string str starts with the given prefix")
            .withParams("str", "prefix")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((s, prefix, ctx) -> {
                String str = ctx.toStringValue(s);
                String pre = ctx.toStringValue(prefix);
                return new NodeBoolean(str.startsWith(pre));
            });

    /**
     * Check if string ends with suffix
     */
    public static final MathFunction ENDSWITH = FunctionBuilder
            .named("endswith")
            .describedAs("Returns true if string str ends with the given suffix")
            .withParams("str", "suffix")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((s, suffix, ctx) -> {
                String str = ctx.toStringValue(s);
                String suf = ctx.toStringValue(suffix);
                return new NodeBoolean(str.endsWith(suf));
            });

    // ==================== Replace and Transform ====================

    /**
     * Replace all occurrences
     */
    public static final MathFunction REPLACE = FunctionBuilder
            .named("replace")
            .describedAs("Returns string str with all occurrences of target replaced by replacement")
            .withParams("str", "target", "replacement")
            .inCategory(STRING)
            .takingExactly(3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                String str = ctx.toStringValue(args.get(0));
                String pat = ctx.toStringValue(args.get(1));
                String rep = ctx.toStringValue(args.get(2));
                return new NodeString(str.replace(pat, rep));
            });

    /**
     * Replace first occurrence
     */
    public static final MathFunction REPLACEFIRST = FunctionBuilder
            .named("replacefirst")
            .describedAs("Returns string str with the first occurrence of target replaced by replacement")
            .withParams("str", "target", "replacement")
            .inCategory(STRING)
            .takingExactly(3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                String str = ctx.toStringValue(args.get(0));
                String pat = ctx.toStringValue(args.get(1));
                String rep = ctx.toStringValue(args.get(2));

                try {
                    return new NodeString(str.replaceFirst(Pattern.quote(pat), rep));
                } catch (PatternSyntaxException e) {
                    throw new IllegalArgumentException("replacefirst: invalid pattern");
                }
            });

    /**
     * Reverse a string
     */
    public static final MathFunction STRREVERSE = FunctionBuilder
            .named("strreverse")
            .describedAs("Returns string str with its characters in reversed order")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                String s = ctx.toStringValue(arg);
                return new NodeString(new StringBuilder(s).reverse().toString());
            });

    /**
     * Repeat string n times
     */
    public static final MathFunction STRREPEAT = FunctionBuilder
            .named("strrepeat")
            .describedAs("Returns string str repeated count times")
            .withParams("str", "count")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((s, n, ctx) -> {
                String str = ctx.toStringValue(s);
                int count = ctx.toInt(n);
                if (count < 0) count = 0;
                return new NodeString(str.repeat(count));
            });

    /**
     * Pad string on left
     */
    public static final MathFunction PADLEFT = FunctionBuilder
            .named("padleft")
            .alias("lpad")
            .describedAs("Pads string str on the left with spaces (or pad char) to reach the given total length")
            .withParams("str", "length")
            .withParams("str", "length", "pad")
            .inCategory(STRING)
            .takingBetween(2, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                String s = ctx.toStringValue(args.get(0));
                int length = ctx.toInt(args.get(1));
                String padChar = args.size() > 2 ? ctx.toStringValue(args.get(2)) : " ";

                if (padChar.isEmpty()) padChar = " ";
                char pad = padChar.charAt(0);

                if (s.length() >= length) return new NodeString(s);

                StringBuilder sb = new StringBuilder();
                while (sb.length() + s.length() < length) {
                    sb.append(pad);
                }
                sb.append(s);
                return new NodeString(sb.toString());
            });

    /**
     * Pad string on right
     */
    public static final MathFunction PADRIGHT = FunctionBuilder
            .named("padright")
            .alias("rpad")
            .describedAs("Pads string str on the right with spaces (or pad char) to reach the given total length")
            .withParams("str", "length")
            .withParams("str", "length", "pad")
            .inCategory(STRING)
            .takingBetween(2, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                String s = ctx.toStringValue(args.get(0));
                int length = ctx.toInt(args.get(1));
                String padChar = args.size() > 2 ? ctx.toStringValue(args.get(2)) : " ";

                if (padChar.isEmpty()) padChar = " ";
                char pad = padChar.charAt(0);

                if (s.length() >= length) return new NodeString(s);

                StringBuilder sb = new StringBuilder(s);
                while (sb.length() < length) {
                    sb.append(pad);
                }
                return new NodeString(sb.toString());
            });

    // ==================== Conversion Functions ====================

    /**
     * Convert to string
     */
    public static final MathFunction STR = FunctionBuilder
            .named("str")
            .alias("tostring")
            .describedAs("Converts x to its string representation")
            .withParams("x")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> ctx.asString(arg));

    /**
     * Split string into vector
     */
    public static final MathFunction SPLIT = FunctionBuilder
            .named("split")
            .describedAs("Splits string str by delimiter into a vector of strings (no delimiter = individual chars)")
            .withParams("str")
            .withParams("str", "delimiter")
            .inCategory(STRING)
            .takingBetween(1, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                String s = ctx.toStringValue(args.get(0));
                String delimiter = args.size() > 1 ? ctx.toStringValue(args.get(1)) : "";

                String[] parts;
                if (delimiter.isEmpty()) {
                    // Split into individual characters
                    parts = s.split("");
                } else {
                    parts = s.split(Pattern.quote(delimiter), -1);
                }

                Node[] result = new Node[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    result[i] = new NodeString(parts[i]);
                }
                return new NodeVector(result);
            });

    /**
     * Join vector elements into string
     */
    public static final MathFunction JOIN = FunctionBuilder
            .named("join")
            .describedAs("Joins the elements of a vector into a string, separated by delimiter")
            .withParams("vector")
            .withParams("vector", "delimiter")
            .inCategory(STRING)
            .takingBetween(1, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                NodeVector vector = ctx.requireVector(args.get(0));
                String delimiter = args.size() > 1 ? ctx.toStringValue(args.get(1)) : "";

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < vector.size(); i++) {
                    if (i > 0) sb.append(delimiter);
                    sb.append(ctx.toStringValue((NodeConstant) vector.getElement(i)));
                }
                return new NodeString(sb.toString());
            });

    /**
     * ASCII code of character
     */
    public static final MathFunction ORD = FunctionBuilder
            .named("ord")
            .alias("ascii")
            .describedAs("Returns the Unicode code point of the first character of string str")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                String s = ctx.toStringValue(arg);
                if (s.isEmpty()) {
                    throw new IllegalArgumentException("ord: empty string");
                }
                return new NodeRational(s.codePointAt(0));
            });

    /**
     * Character from ASCII/Unicode code
     */
    public static final MathFunction CHR = FunctionBuilder
            .named("chr")
            .describedAs("Returns the character corresponding to the Unicode code point n")
            .withParams("n")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                int code = ctx.toInt(arg);
                if (code < 0 || code > 0x10FFFF) {
                    throw new IllegalArgumentException("chr: code point out of range");
                }
                return new NodeString(new String(Character.toChars(code)));
            });

    /**
     * Check if string is empty
     */
    public static final MathFunction ISEMPTY = FunctionBuilder
            .named("isempty")
            .alias("isblank")
            .describedAs("Returns true if string str is empty or contains only whitespace")
            .withParams("str")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                String s = ctx.toStringValue(arg);
                return new NodeBoolean(s.isBlank());
            });

    /**
     * Format number as string
     */
    public static final MathFunction FORMAT = FunctionBuilder
            .named("format")
            .describedAs("Formats value as a string with the specified number of decimal places")
            .withParams("value", "places")
            .inCategory(STRING)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((value, decimals, ctx) -> {
                double val = ctx.toNumber(value).doubleValue();
                int dec = ctx.toInt(decimals);
                dec = Math.max(0, Math.min(dec, 15));
                return new NodeString(String.format("%." + dec + "f", val));
            });

    /**
     * Gets all string functions.
     */
    public static List<MathFunction> all() {
        return List.of(
                // Basic operations
                STRLEN, UPPER, LOWER, TRIM, LTRIM, RTRIM,
                // Substring operations
                SUBSTRING, LEFT, RIGHT, CHARAT,
                // Search operations
                STRINDEXOF, STRLASTINDEXOF, STRCONTAINS, STARTSWITH, ENDSWITH,
                // Transform operations
                REPLACE, REPLACEFIRST, STRREVERSE, STRREPEAT, PADLEFT, PADRIGHT,
                // Conversion
                STR, SPLIT, JOIN, ORD, CHR, ISEMPTY, FORMAT
        );
    }
}
