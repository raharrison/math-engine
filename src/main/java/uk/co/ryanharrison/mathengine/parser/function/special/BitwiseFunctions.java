package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.BITWISE;

/**
 * Collection of bitwise operation functions.
 * <p>
 * All operations work on 64-bit signed integers (long). Input values are
 * truncated to integers before the bitwise operation is performed.
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

    /**
     * Gets all bitwise functions.
     */
    public static List<MathFunction> all() {
        return List.of(BITAND, BITOR, BITXOR, BITNOT, LSHIFT, RSHIFT, URSHIFT, POPCOUNT, CLZ, CTZ, ROTL, ROTR, BITREVERSE);
    }
}
