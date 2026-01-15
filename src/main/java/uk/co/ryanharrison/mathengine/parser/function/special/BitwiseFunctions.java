package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.*;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
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
     * Bitwise AND
     */
    public static final MathFunction BITAND = AggregateFunction.of("bitand", "Bitwise AND", BITWISE, 2, Integer.MAX_VALUE, (args, ctx) -> {
        long result = toLong(ctx, args.get(0));
        for (int i = 1; i < args.size(); i++) result &= toLong(ctx, args.get(i));
        return new NodeRational(result);
    });

    /**
     * Bitwise OR
     */
    public static final MathFunction BITOR = AggregateFunction.of("bitor", "Bitwise OR", BITWISE, 2, Integer.MAX_VALUE, (args, ctx) -> {
        long result = toLong(ctx, args.get(0));
        for (int i = 1; i < args.size(); i++) result |= toLong(ctx, args.get(i));
        return new NodeRational(result);
    });

    /**
     * Bitwise XOR
     */
    public static final MathFunction BITXOR = AggregateFunction.of("bitxor", "Bitwise XOR", BITWISE, 2, Integer.MAX_VALUE, (args, ctx) -> {
        long result = toLong(ctx, args.get(0));
        for (int i = 1; i < args.size(); i++) result ^= toLong(ctx, args.get(i));
        return new NodeRational(result);
    });

    /**
     * Bitwise NOT (complement)
     */
    public static final MathFunction BITNOT = UnaryFunction.of("bitnot", "Bitwise NOT (complement)", BITWISE,
            (arg, ctx) -> new NodeRational(~toLong(ctx, arg)));

    /**
     * Left shift
     */
    public static final MathFunction LSHIFT = BinaryFunction.of("lshift", "Left shift", BITWISE, List.of("shl"),
            (v, s, ctx) -> new NodeRational(toLong(ctx, v) << (int) toLong(ctx, s)));

    /**
     * Right shift (arithmetic)
     */
    public static final MathFunction RSHIFT = BinaryFunction.of("rshift", "Right shift (arithmetic)", BITWISE, List.of("shr"),
            (v, s, ctx) -> new NodeRational(toLong(ctx, v) >> (int) toLong(ctx, s)));

    /**
     * Unsigned right shift
     */
    public static final MathFunction URSHIFT = BinaryFunction.of("urshift", "Unsigned right shift", BITWISE, List.of("ushr"),
            (v, s, ctx) -> new NodeRational(toLong(ctx, v) >>> (int) toLong(ctx, s)));

    /**
     * Count number of 1 bits (popcount)
     */
    public static final MathFunction POPCOUNT = UnaryFunction.of("popcount", "Count number of 1 bits", BITWISE, List.of("bitcount"),
            (arg, ctx) -> new NodeRational(Long.bitCount(toLong(ctx, arg))));

    /**
     * Number of leading zeros
     */
    public static final MathFunction CLZ = UnaryFunction.of("clz", "Count leading zeros", BITWISE,
            (arg, ctx) -> new NodeRational(Long.numberOfLeadingZeros(toLong(ctx, arg))));

    /**
     * Number of trailing zeros
     */
    public static final MathFunction CTZ = UnaryFunction.of("ctz", "Count trailing zeros", BITWISE,
            (arg, ctx) -> new NodeRational(Long.numberOfTrailingZeros(toLong(ctx, arg))));

    /**
     * Rotate left
     */
    public static final MathFunction ROTL = BinaryFunction.of("rotl", "Rotate left", BITWISE,
            (v, s, ctx) -> new NodeRational(Long.rotateLeft(toLong(ctx, v), (int) toLong(ctx, s))));

    /**
     * Rotate right
     */
    public static final MathFunction ROTR = BinaryFunction.of("rotr", "Rotate right", BITWISE,
            (v, s, ctx) -> new NodeRational(Long.rotateRight(toLong(ctx, v), (int) toLong(ctx, s))));

    /**
     * Reverse bits
     */
    public static final MathFunction BITREVERSE = UnaryFunction.of("bitreverse", "Reverse bits", BITWISE,
            (arg, ctx) -> new NodeRational(Long.reverse(toLong(ctx, arg))));

    private static long toLong(FunctionContext ctx, NodeConstant node) {
        return (long) ctx.toNumber(node).doubleValue();
    }

    /**
     * Gets all bitwise functions.
     */
    public static List<MathFunction> all() {
        return List.of(BITAND, BITOR, BITXOR, BITNOT, LSHIFT, RSHIFT, URSHIFT, POPCOUNT, CLZ, CTZ, ROTL, ROTR, BITREVERSE);
    }
}
