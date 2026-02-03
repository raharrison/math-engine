package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Builder for ternary functions with type-safe parameter extraction.
 * <p>
 * Created via {@link FunctionBuilder#takingTyped(ArgType, ArgType, ArgType)}.
 * All three argument types are extracted automatically before calling the implementation.
 * Do validation inline in the implementation via {@link FunctionContext} helpers.
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * MathFunction modpow = FunctionBuilder
 *     .named("modpow")
 *     .takingTyped(ArgTypes.longInt(), ArgTypes.longInt(), ArgTypes.longInt())
 *     .implementedBy((base, exp, mod, ctx) -> {
 *         if (mod <= 0) throw ctx.error("modulus must be positive");
 *         if (exp < 0) throw ctx.error("exponent must be non-negative");
 *         return ...;
 *     });
 * }</pre>
 *
 * @param <A> the extracted type of the first argument
 * @param <B> the extracted type of the second argument
 * @param <C> the extracted type of the third argument
 * @see FunctionBuilder#takingTyped(ArgType, ArgType, ArgType)
 * @see ArgTypes
 */
public final class TypedTernaryBuilder<A, B, C> {

    private final FunctionBuilder parent;
    private final ArgType<A> arg1Type;
    private final ArgType<B> arg2Type;
    private final ArgType<C> arg3Type;

    TypedTernaryBuilder(FunctionBuilder parent, ArgType<A> arg1Type, ArgType<B> arg2Type, ArgType<C> arg3Type) {
        this.parent = parent;
        this.arg1Type = arg1Type;
        this.arg2Type = arg2Type;
        this.arg3Type = arg3Type;
    }

    /**
     * Implements the function with type-safe parameters.
     *
     * @param implementation the function implementation receiving extracted arguments
     * @return the built MathFunction
     */
    public MathFunction implementedBy(TypedTernaryFunction<A, B, C> implementation) {
        parent.validateMetadata();

        return parent.createMathFunction(() -> (args, ctx) -> {
            A arg1 = arg1Type.extract(args.get(0), ctx);
            B arg2 = arg2Type.extract(args.get(1), ctx);
            C arg3 = arg3Type.extract(args.get(2), ctx);
            return implementation.apply(arg1, arg2, arg3, ctx);
        });
    }

    /**
     * Typed function interface for ternary functions.
     *
     * @param <A> the type of the first argument
     * @param <B> the type of the second argument
     * @param <C> the type of the third argument
     */
    @FunctionalInterface
    public interface TypedTernaryFunction<A, B, C> {
        NodeConstant apply(A arg1, B arg2, C arg3, FunctionContext ctx);
    }
}
