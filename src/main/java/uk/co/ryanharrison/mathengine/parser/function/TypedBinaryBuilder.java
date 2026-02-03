package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Builder for binary functions with type-safe parameter extraction.
 * <p>
 * Created via {@link FunctionBuilder#takingTyped(ArgType, ArgType)}.
 * Both argument types are extracted automatically before calling the implementation.
 * Do validation inline in the implementation via {@link FunctionContext} helpers.
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * MathFunction lshift = FunctionBuilder
 *     .named("lshift")
 *     .takingTyped(ArgTypes.longInt(), ArgTypes.longInt())
 *     .implementedBy((value, shift, ctx) -> {
 *         if (shift < 0 || shift > 63) throw ctx.error("shift must be 0-63, got: " + shift);
 *         return new NodeRational(value << shift.intValue());
 *     });
 * }</pre>
 *
 * @param <A> the extracted type of the first argument
 * @param <B> the extracted type of the second argument
 * @see FunctionBuilder#takingTyped(ArgType, ArgType)
 * @see ArgTypes
 */
public final class TypedBinaryBuilder<A, B> {

    private final FunctionBuilder parent;
    private final ArgType<A> arg1Type;
    private final ArgType<B> arg2Type;

    TypedBinaryBuilder(FunctionBuilder parent, ArgType<A> arg1Type, ArgType<B> arg2Type) {
        this.parent = parent;
        this.arg1Type = arg1Type;
        this.arg2Type = arg2Type;
    }

    /**
     * Implements the function with type-safe parameters.
     *
     * @param implementation the function implementation receiving extracted arguments
     * @return the built MathFunction
     */
    public MathFunction implementedBy(TypedBinaryFunction<A, B> implementation) {
        parent.validateMetadata();

        return parent.createMathFunction(() -> (args, ctx) -> {
            A arg1 = arg1Type.extract(args.get(0), ctx);
            B arg2 = arg2Type.extract(args.get(1), ctx);
            return implementation.apply(arg1, arg2, ctx);
        });
    }

    /**
     * Typed function interface for binary functions.
     *
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     */
    @FunctionalInterface
    public interface TypedBinaryFunction<A, B> {
        NodeConstant apply(A arg1, B arg2, FunctionContext ctx);
    }
}
