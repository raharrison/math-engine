package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Builder for unary functions with type-safe parameter extraction.
 * <p>
 * Created via {@link FunctionBuilder#takingTyped(ArgType)}.
 * The argument type is extracted automatically before calling the implementation.
 * Do validation inline in the implementation via {@link FunctionContext} helpers.
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * MathFunction factorial = FunctionBuilder
 *     .named("factorial")
 *     .takingTyped(ArgTypes.longInt())
 *     .implementedBy((n, ctx) -> {
 *         if (n < 0) throw ctx.error("requires non-negative integer");
 *         return new NodeRational(MathUtils.factorial(n));
 *     });
 * }</pre>
 *
 * @param <A> the extracted type of the argument
 * @see FunctionBuilder#takingTyped(ArgType)
 * @see ArgTypes
 */
public final class TypedUnaryBuilder<A> {

    private final FunctionBuilder parent;
    private final ArgType<A> argType;

    TypedUnaryBuilder(FunctionBuilder parent, ArgType<A> argType) {
        this.parent = parent;
        this.argType = argType;
    }

    /**
     * Implements the function with a type-safe parameter.
     *
     * @param implementation the function implementation receiving the extracted argument
     * @return the built MathFunction
     */
    public MathFunction implementedBy(TypedUnaryFunction<A> implementation) {
        parent.validateMetadata();

        return parent.createMathFunction(() -> (args, ctx) -> {
            A arg = argType.extract(args.get(0), ctx);
            return implementation.apply(arg, ctx);
        });
    }

    /**
     * Typed function interface for unary functions.
     *
     * @param <A> the type of the argument
     */
    @FunctionalInterface
    public interface TypedUnaryFunction<A> {
        NodeConstant apply(A arg, FunctionContext ctx);
    }
}
