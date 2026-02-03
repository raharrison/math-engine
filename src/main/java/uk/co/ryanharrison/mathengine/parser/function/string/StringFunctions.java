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
            .describedAs("String length")
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
            .describedAs("Convert to uppercase")
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
            .describedAs("Convert to lowercase")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> new NodeString(ctx.toStringValue(arg).toLowerCase()));

    /**
     * Trim whitespace
     */
    public static final MathFunction TRIM = FunctionBuilder
            .named("trim")
            .describedAs("Trim leading and trailing whitespace")
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
            .describedAs("Trim leading whitespace")
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
            .describedAs("Trim trailing whitespace")
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
            .describedAs("Extract substring (start, length)")
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
            .describedAs("Get leftmost n characters")
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
            .describedAs("Get rightmost n characters")
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
            .describedAs("Get character at index")
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
            .describedAs("Find first index of substring (-1 if not found)")
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
            .describedAs("Find last index of substring")
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
            .describedAs("Check if string contains substring")
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
            .describedAs("Check if string starts with prefix")
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
            .describedAs("Check if string ends with suffix")
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
            .describedAs("Replace all occurrences of pattern")
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
            .describedAs("Replace first occurrence")
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
            .describedAs("Reverse a string")
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
            .describedAs("Repeat string n times")
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
            .describedAs("Pad string on left to length")
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
            .describedAs("Pad string on right to length")
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
            .describedAs("Convert value to string")
            .inCategory(STRING)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> ctx.asString(arg));

    /**
     * Split string into vector
     */
    public static final MathFunction SPLIT = FunctionBuilder
            .named("split")
            .describedAs("Split string by delimiter")
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
            .describedAs("Join vector elements with delimiter")
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
            .describedAs("Get ASCII/Unicode code of first character")
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
            .describedAs("Get character from ASCII/Unicode code")
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
            .describedAs("Check if string is empty or blank")
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
            .describedAs("Format number with decimal places")
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
