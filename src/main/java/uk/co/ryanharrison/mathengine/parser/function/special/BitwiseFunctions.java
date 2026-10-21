package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeRational;
import uk.co.ryanharrison.mathengine.parser.ast.NodeString;
import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;

import java.math.BigInteger;
import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.BITWISE;

/**
 * Collection of bitwise operation functions.
 * <p>
 * The bitwise operations work on 64-bit signed integers (long). Input values are
 * truncated to integers before the bitwise operation is performed.
 * <p>
 * The radix functions write an integer of any size in another base, and read one back:
 * {@code hex(255)} is {@code "0xff"}, which can be typed back in as a literal, and
 * {@code frombase("ff", 16)} is 255.
 */
public final class BitwiseFunctions {

    private BitwiseFunctions() {
    }

    /**
     * Bitwise AND (variadic — folds over 2+ arguments)
     */
    public static final MathFunction BITAND = FunctionBuilder
            .named("bitand")
            .describedAs("Returns the bitwise AND of two or more integers")
            .withParams("a", "b")
            .inCategory(BITWISE)
            .takingVariadic(2)
            .implementedByAggregate((args, ctx) -> {
                long result = ctx.requireLong(args.getFirst());
                for (int i = 1; i < args.size(); i++) result &= ctx.requireLong(args.get(i));
                return new NodeRational(result);
            });

    /**
     * Bitwise OR (variadic — folds over 2+ arguments)
     */
    public static final MathFunction BITOR = FunctionBuilder
            .named("bitor")
            .describedAs("Returns the bitwise OR of two or more integers")
            .withParams("a", "b")
            .inCategory(BITWISE)
            .takingVariadic(2)
            .implementedByAggregate((args, ctx) -> {
                long result = ctx.requireLong(args.getFirst());
                for (int i = 1; i < args.size(); i++) result |= ctx.requireLong(args.get(i));
                return new NodeRational(result);
            });

    /**
     * Bitwise XOR (variadic — folds over 2+ arguments)
     */
    public static final MathFunction BITXOR = FunctionBuilder
            .named("bitxor")
            .describedAs("Returns the bitwise XOR of two or more integers")
            .withParams("a", "b")
            .inCategory(BITWISE)
            .takingVariadic(2)
            .implementedByAggregate((args, ctx) -> {
                long result = ctx.requireLong(args.getFirst());
                for (int i = 1; i < args.size(); i++) result ^= ctx.requireLong(args.get(i));
                return new NodeRational(result);
            });

    /**
     * Bitwise NOT (complement)
     */
    public static final MathFunction BITNOT = FunctionBuilder
            .named("bitnot")
            .describedAs("Returns the bitwise NOT (one's complement) of x")
            .withParams("x")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((value, ctx) -> new NodeRational(~value));

    /**
     * Left shift
     */
    public static final MathFunction LSHIFT = FunctionBuilder
            .named("lshift")
            .alias("shl")
            .describedAs("Returns value shifted left by shift bits")
            .withParams("value", "shift")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((v, s, ctx) -> new NodeRational(v << s.intValue()));

    /**
     * Right shift (arithmetic)
     */
    public static final MathFunction RSHIFT = FunctionBuilder
            .named("rshift")
            .alias("shr")
            .describedAs("Returns value shifted right by shift bits (sign-preserving)")
            .withParams("value", "shift")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((v, s, ctx) -> new NodeRational(v >> s.intValue()));

    /**
     * Unsigned right shift
     */
    public static final MathFunction URSHIFT = FunctionBuilder
            .named("urshift")
            .alias("ushr")
            .describedAs("Returns value shifted right by shift bits (zero-fill, unsigned)")
            .withParams("value", "shift")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((v, s, ctx) -> new NodeRational(v >>> s.intValue()));

    /**
     * Count number of 1 bits (popcount)
     */
    public static final MathFunction POPCOUNT = FunctionBuilder
            .named("popcount")
            .alias("bitcount")
            .describedAs("Returns the number of 1-bits in the binary representation of x")
            .withParams("x")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((value, ctx) -> new NodeRational(Long.bitCount(value)));

    /**
     * Number of leading zeros
     */
    public static final MathFunction CLZ = FunctionBuilder
            .named("clz")
            .describedAs("Returns the number of leading zero bits in x (treating x as a 64-bit integer)")
            .withParams("x")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((value, ctx) -> new NodeRational(Long.numberOfLeadingZeros(value)));

    /**
     * Number of trailing zeros
     */
    public static final MathFunction CTZ = FunctionBuilder
            .named("ctz")
            .describedAs("Returns the number of trailing zero bits in x (treating x as a 64-bit integer)")
            .withParams("x")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((value, ctx) -> new NodeRational(Long.numberOfTrailingZeros(value)));

    /**
     * Rotate left
     */
    public static final MathFunction ROTL = FunctionBuilder
            .named("rotl")
            .describedAs("Returns value rotated left by shift bit positions within a 64-bit boundary")
            .withParams("value", "shift")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((v, s, ctx) -> new NodeRational(Long.rotateLeft(v, s.intValue())));

    /**
     * Rotate right
     */
    public static final MathFunction ROTR = FunctionBuilder
            .named("rotr")
            .describedAs("Returns value rotated right by shift bit positions within a 64-bit boundary")
            .withParams("value", "shift")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((v, s, ctx) -> new NodeRational(Long.rotateRight(v, s.intValue())));

    /**
     * Reverse bits
     */
    public static final MathFunction BITREVERSE = FunctionBuilder
            .named("bitreverse")
            .describedAs("Returns x with its 64-bit binary representation reversed")
            .withParams("x")
            .inCategory(BITWISE)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((value, ctx) -> new NodeRational(Long.reverse(value)));

    // ==================== Radix ====================

    private static final int MIN_RADIX = Character.MIN_RADIX;
    private static final int MAX_RADIX = Character.MAX_RADIX;

    /**
     * Hexadecimal, with the {@code 0x} prefix the lexer reads back
     */
    public static final MathFunction HEX = FunctionBuilder
            .named("hex")
            .describedAs("Returns integer n written in hexadecimal, as a string such as \"0xff\"")
            .withParams("n")
            .inCategory(BITWISE)
            .takingUnary()
            .implementedBy((n, ctx) -> prefixed(ctx.requireBigInteger(n), 16, "0x"));

    /**
     * Binary, with the {@code 0b} prefix the lexer reads back
     */
    public static final MathFunction BIN = FunctionBuilder
            .named("bin")
            .describedAs("Returns integer n written in binary, as a string such as \"0b1010\"")
            .withParams("n")
            .inCategory(BITWISE)
            .takingUnary()
            .implementedBy((n, ctx) -> prefixed(ctx.requireBigInteger(n), 2, "0b"));

    /**
     * Octal, with the {@code 0o} prefix the lexer reads back
     */
    public static final MathFunction OCT = FunctionBuilder
            .named("oct")
            .describedAs("Returns integer n written in octal, as a string such as \"0o17\"")
            .withParams("n")
            .inCategory(BITWISE)
            .takingUnary()
            .implementedBy((n, ctx) -> prefixed(ctx.requireBigInteger(n), 8, "0o"));

    /**
     * Any base from 2 to 36, without a prefix
     */
    public static final MathFunction TOBASE = FunctionBuilder
            .named("tobase")
            .describedAs("Returns integer n written in the given base (2 to 36) as a string, without a prefix")
            .withParams("n", "base")
            .inCategory(BITWISE)
            .takingBinary()
            .implementedBy((n, base, ctx) ->
                    new NodeString(ctx.requireBigInteger(n).toString(requireRadix(base, ctx))));

    /**
     * Reads digits written in a base from 2 to 36
     */
    public static final MathFunction FROMBASE = FunctionBuilder
            .named("frombase")
            .describedAs("Returns the integer that string s denotes in the given base (2 to 36)")
            .withParams("s", "base")
            .inCategory(BITWISE)
            .takingBinary()
            .implementedBy((s, base, ctx) -> {
                int radix = requireRadix(base, ctx);
                String text = ctx.requireString(s).getValue().strip().replace("_", "");
                return new NodeRational(BigRational.of(parseDigits(text, radix, ctx)));
            });

    private static NodeString prefixed(BigInteger value, int radix, String prefix) {
        String digits = value.abs().toString(radix);
        return new NodeString((value.signum() < 0 ? "-" : "") + prefix + digits);
    }

    private static int requireRadix(NodeConstant base, FunctionContext ctx) {
        int radix = ctx.requireInteger(base);
        if (radix < MIN_RADIX || radix > MAX_RADIX) {
            throw ctx.error("base must be between " + MIN_RADIX + " and " + MAX_RADIX + ", got: " + radix);
        }
        return radix;
    }

    /**
     * Parses a sign, then the prefix that matches the base if there is one, then digits.
     */
    private static BigInteger parseDigits(String text, int radix, FunctionContext ctx) {
        boolean negative = text.startsWith("-");
        String digits = negative || text.startsWith("+") ? text.substring(1) : text;
        String prefix = switch (radix) {
            case 16 -> "0x";
            case 8 -> "0o";
            case 2 -> "0b";
            default -> null;
        };
        if (prefix != null && digits.toLowerCase().startsWith(prefix)) {
            digits = digits.substring(prefix.length());
        }
        try {
            BigInteger value = new BigInteger(digits, radix);
            if (digits.startsWith("-") || digits.startsWith("+")) {
                throw new NumberFormatException();
            }
            return negative ? value.negate() : value;
        } catch (NumberFormatException e) {
            throw ctx.error("'" + text + "' is not a base " + radix + " integer");
        }
    }

    /**
     * Gets all bitwise functions.
     */
    public static List<MathFunction> all() {
        return List.of(BITAND, BITOR, BITXOR, BITNOT, LSHIFT, RSHIFT, URSHIFT, POPCOUNT, CLZ, CTZ, ROTL, ROTR, BITREVERSE,
                HEX, BIN, OCT, TOBASE, FROMBASE);
    }
}
