package uk.co.ryanharrison.mathengine.parser.function.string;

import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    public static final MathFunction STRLEN = new MathFunction() {
        @Override
        public String name() {
            return "strlen";
        }

        @Override
        public String description() {
            return "String length";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            return new NodeRational(s.length());
        }
    };

    /**
     * Convert to uppercase
     */
    public static final MathFunction UPPER = new MathFunction() {
        @Override
        public String name() {
            return "upper";
        }

        @Override
        public List<String> aliases() {
            return List.of("uppercase", "toupper");
        }

        @Override
        public String description() {
            return "Convert to uppercase";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            return new NodeString(s.toUpperCase());
        }
    };

    /**
     * Convert to lowercase
     */
    public static final MathFunction LOWER = new MathFunction() {
        @Override
        public String name() {
            return "lower";
        }

        @Override
        public List<String> aliases() {
            return List.of("lowercase", "tolower");
        }

        @Override
        public String description() {
            return "Convert to lowercase";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            return new NodeString(s.toLowerCase());
        }
    };

    /**
     * Trim whitespace
     */
    public static final MathFunction TRIM = new MathFunction() {
        @Override
        public String name() {
            return "trim";
        }

        @Override
        public String description() {
            return "Trim leading and trailing whitespace";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            return new NodeString(s.strip());
        }
    };

    /**
     * Trim leading whitespace
     */
    public static final MathFunction LTRIM = new MathFunction() {
        @Override
        public String name() {
            return "ltrim";
        }

        @Override
        public List<String> aliases() {
            return List.of("trimleft");
        }

        @Override
        public String description() {
            return "Trim leading whitespace";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            return new NodeString(s.stripLeading());
        }
    };

    /**
     * Trim trailing whitespace
     */
    public static final MathFunction RTRIM = new MathFunction() {
        @Override
        public String name() {
            return "rtrim";
        }

        @Override
        public List<String> aliases() {
            return List.of("trimright");
        }

        @Override
        public String description() {
            return "Trim trailing whitespace";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            return new NodeString(s.stripTrailing());
        }
    };

    // ==================== Substring and Slicing ====================

    /**
     * Extract substring
     */
    public static final MathFunction SUBSTRING = new MathFunction() {
        @Override
        public String name() {
            return "substring";
        }

        @Override
        public List<String> aliases() {
            return List.of("substr", "mid");
        }

        @Override
        public String description() {
            return "Extract substring (start, length)";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
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
        }
    };

    /**
     * Get left portion of string
     */
    public static final MathFunction LEFT = new MathFunction() {
        @Override
        public String name() {
            return "left";
        }

        @Override
        public String description() {
            return "Get leftmost n characters";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            int n = ctx.toInt(args.get(1));
            n = Math.max(0, Math.min(n, s.length()));
            return new NodeString(s.substring(0, n));
        }
    };

    /**
     * Get right portion of string
     */
    public static final MathFunction RIGHT = new MathFunction() {
        @Override
        public String name() {
            return "right";
        }

        @Override
        public String description() {
            return "Get rightmost n characters";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            int n = ctx.toInt(args.get(1));
            n = Math.max(0, Math.min(n, s.length()));
            return new NodeString(s.substring(s.length() - n));
        }
    };

    /**
     * Get character at index
     */
    public static final MathFunction CHARAT = new MathFunction() {
        @Override
        public String name() {
            return "charat";
        }

        @Override
        public List<String> aliases() {
            return List.of("char");
        }

        @Override
        public String description() {
            return "Get character at index";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            int index = ctx.toInt(args.get(1));

            // Handle negative index
            if (index < 0) index = s.length() + index;

            if (index < 0 || index >= s.length()) {
                throw new IllegalArgumentException("charat: index " + index + " out of bounds");
            }
            return new NodeString(String.valueOf(s.charAt(index)));
        }
    };

    // ==================== Search Operations ====================

    /**
     * Find index of substring
     */
    public static final MathFunction STRINDEXOF = new MathFunction() {
        @Override
        public String name() {
            return "strindexof";
        }

        @Override
        public List<String> aliases() {
            return List.of("strfind");
        }

        @Override
        public String description() {
            return "Find first index of substring (-1 if not found)";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            String needle = ctx.toStringValue(args.get(1));
            int start = args.size() > 2 ? ctx.toInt(args.get(2)) : 0;
            return new NodeRational(s.indexOf(needle, start));
        }
    };

    /**
     * Find last index of substring
     */
    public static final MathFunction STRLASTINDEXOF = new MathFunction() {
        @Override
        public String name() {
            return "strlastindexof";
        }

        @Override
        public List<String> aliases() {
            return List.of("strrfind");
        }

        @Override
        public String description() {
            return "Find last index of substring";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            String needle = ctx.toStringValue(args.get(1));
            return new NodeRational(s.lastIndexOf(needle));
        }
    };

    /**
     * Check if string contains substring
     */
    public static final MathFunction STRCONTAINS = new MathFunction() {
        @Override
        public String name() {
            return "strcontains";
        }

        @Override
        public String description() {
            return "Check if string contains substring";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            String needle = ctx.toStringValue(args.get(1));
            return new NodeBoolean(s.contains(needle));
        }
    };

    /**
     * Check if string starts with prefix
     */
    public static final MathFunction STARTSWITH = new MathFunction() {
        @Override
        public String name() {
            return "startswith";
        }

        @Override
        public String description() {
            return "Check if string starts with prefix";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            String prefix = ctx.toStringValue(args.get(1));
            return new NodeBoolean(s.startsWith(prefix));
        }
    };

    /**
     * Check if string ends with suffix
     */
    public static final MathFunction ENDSWITH = new MathFunction() {
        @Override
        public String name() {
            return "endswith";
        }

        @Override
        public String description() {
            return "Check if string ends with suffix";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            String suffix = ctx.toStringValue(args.get(1));
            return new NodeBoolean(s.endsWith(suffix));
        }
    };

    // ==================== Replace and Transform ====================

    /**
     * Replace all occurrences
     */
    public static final MathFunction REPLACE = new MathFunction() {
        @Override
        public String name() {
            return "replace";
        }

        @Override
        public String description() {
            return "Replace all occurrences of pattern";
        }

        @Override
        public int minArity() {
            return 3;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            String pattern = ctx.toStringValue(args.get(1));
            String replacement = ctx.toStringValue(args.get(2));
            return new NodeString(s.replace(pattern, replacement));
        }
    };

    /**
     * Replace first occurrence
     */
    public static final MathFunction REPLACEFIRST = new MathFunction() {
        @Override
        public String name() {
            return "replacefirst";
        }

        @Override
        public String description() {
            return "Replace first occurrence";
        }

        @Override
        public int minArity() {
            return 3;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            String pattern = ctx.toStringValue(args.get(1));
            String replacement = ctx.toStringValue(args.get(2));

            try {
                return new NodeString(s.replaceFirst(Pattern.quote(pattern), replacement));
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("replacefirst: invalid pattern");
            }
        }
    };

    /**
     * Reverse a string
     */
    public static final MathFunction STRREVERSE = new MathFunction() {
        @Override
        public String name() {
            return "strreverse";
        }

        @Override
        public String description() {
            return "Reverse a string";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            return new NodeString(new StringBuilder(s).reverse().toString());
        }
    };

    /**
     * Repeat string n times
     */
    public static final MathFunction STRREPEAT = new MathFunction() {
        @Override
        public String name() {
            return "strrepeat";
        }

        @Override
        public String description() {
            return "Repeat string n times";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.get(0));
            int n = ctx.toInt(args.get(1));
            if (n < 0) n = 0;
            return new NodeString(s.repeat(n));
        }
    };

    /**
     * Pad string on left
     */
    public static final MathFunction PADLEFT = new MathFunction() {
        @Override
        public String name() {
            return "padleft";
        }

        @Override
        public List<String> aliases() {
            return List.of("lpad");
        }

        @Override
        public String description() {
            return "Pad string on left to length";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
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
        }
    };

    /**
     * Pad string on right
     */
    public static final MathFunction PADRIGHT = new MathFunction() {
        @Override
        public String name() {
            return "padright";
        }

        @Override
        public List<String> aliases() {
            return List.of("rpad");
        }

        @Override
        public String description() {
            return "Pad string on right to length";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
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
        }
    };

    // ==================== Conversion Functions ====================

    /**
     * Convert to string
     */
    public static final MathFunction STR = new MathFunction() {
        @Override
        public String name() {
            return "str";
        }

        @Override
        public List<String> aliases() {
            return List.of("tostring");
        }

        @Override
        public String description() {
            return "Convert value to string";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            return ctx.asString(args.getFirst());
        }
    };

    /**
     * Split string into vector
     */
    public static final MathFunction SPLIT = new MathFunction() {
        @Override
        public String name() {
            return "split";
        }

        @Override
        public String description() {
            return "Split string by delimiter";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
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
        }
    };

    /**
     * Join vector elements into string
     */
    public static final MathFunction JOIN = new MathFunction() {
        @Override
        public String name() {
            return "join";
        }

        @Override
        public String description() {
            return "Join vector elements with delimiter";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "join");
            String delimiter = args.size() > 1 ? ctx.toStringValue(args.get(1)) : "";

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < vector.size(); i++) {
                if (i > 0) sb.append(delimiter);
                sb.append(ctx.toStringValue((NodeConstant) vector.getElement(i)));
            }
            return new NodeString(sb.toString());
        }
    };

    /**
     * ASCII code of character
     */
    public static final MathFunction ORD = new MathFunction() {
        @Override
        public String name() {
            return "ord";
        }

        @Override
        public List<String> aliases() {
            return List.of("ascii");
        }

        @Override
        public String description() {
            return "Get ASCII/Unicode code of first character";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            if (s.isEmpty()) {
                throw new IllegalArgumentException("ord: empty string");
            }
            return new NodeRational(s.codePointAt(0));
        }
    };

    /**
     * Character from ASCII/Unicode code
     */
    public static final MathFunction CHR = new MathFunction() {
        @Override
        public String name() {
            return "chr";
        }

        @Override
        public String description() {
            return "Get character from ASCII/Unicode code";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            int code = ctx.toInt(args.getFirst());
            if (code < 0 || code > 0x10FFFF) {
                throw new IllegalArgumentException("chr: code point out of range");
            }
            return new NodeString(new String(Character.toChars(code)));
        }
    };

    /**
     * Check if string is empty
     */
    public static final MathFunction ISEMPTY = new MathFunction() {
        @Override
        public String name() {
            return "isempty";
        }

        @Override
        public List<String> aliases() {
            return List.of("isblank");
        }

        @Override
        public String description() {
            return "Check if string is empty or blank";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            String s = ctx.toStringValue(args.getFirst());
            return new NodeBoolean(s.isBlank());
        }
    };

    /**
     * Format number as string
     */
    public static final MathFunction FORMAT = new MathFunction() {
        @Override
        public String name() {
            return "format";
        }

        @Override
        public String description() {
            return "Format number with decimal places";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.STRING;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            double value = ctx.toNumber(args.get(0)).doubleValue();
            int decimals = ctx.toInt(args.get(1));
            decimals = Math.max(0, Math.min(decimals, 15));
            return new NodeString(String.format("%." + decimals + "f", value));
        }
    };

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
