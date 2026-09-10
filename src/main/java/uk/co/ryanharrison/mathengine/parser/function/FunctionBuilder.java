package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * Fluent builder for creating {@link MathFunction} definitions with minimal boilerplate.
 * <p>
 * Provides a clean DSL for defining functions with automatic broadcasting and type conversion.
 * All function implementations receive a {@link FunctionContext} which knows the function name
 * and provides validation helpers like {@link FunctionContext#requirePositive(double)},
 * {@link FunctionContext#requireInRange(double, double, double)}, etc.
 *
 * <h2>Validation:</h2>
 * <p>
 * Do validation inline in the function implementation via {@link FunctionContext} helpers:
 * <pre>{@code
 * // Unary with validation
 * MathFunction sqrt = FunctionBuilder
 *     .named("sqrt")
 *     .takingUnary()
 *     .implementedBy((arg, ctx) -> {
 *         double value = ctx.toDouble(arg);
 *         ctx.requireNonNegative(value);
 *         return new NodeDouble(Math.sqrt(value));
 *     });
 *
 * // Simple double operation (no validation needed)
 * MathFunction exp = FunctionBuilder
 *     .named("exp")
 *     .takingUnary()
 *     .implementedByDouble(Math::exp);
 * }</pre>
 *
 * @see MathFunction
 * @see FunctionContext
 */
public final class FunctionBuilder {

    private String name;
    private String description;
    private MathFunction.Category category = MathFunction.Category.OTHER;
    private final List<String> aliases = new ArrayList<>();
    private final List<List<String>> parameterSets = new ArrayList<>();
    private int minArity;
    private int maxArity;
    private boolean supportsBroadcasting = true;

    private FunctionBuilder() {
    }

    /**
     * Returns the function name. Package-private for use by typed builders.
     */
    String getName() {
        return name;
    }

    // ==================== Entry Point ====================

    /**
     * Creates a new function builder with the specified name.
     */
    public static FunctionBuilder named(String name) {
        FunctionBuilder builder = new FunctionBuilder();
        builder.name = name;
        return builder;
    }

    // ==================== Metadata ====================

    public FunctionBuilder describedAs(String description) {
        this.description = description;
        return this;
    }

    public FunctionBuilder inCategory(MathFunction.Category category) {
        this.category = category;
        return this;
    }

    public FunctionBuilder alias(String... aliases) {
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public FunctionBuilder withParams(String... params) {
        this.parameterSets.add(List.copyOf(Arrays.asList(params)));
        return this;
    }

    // ==================== Arity ====================

    public FunctionBuilder takingUnary() {
        this.minArity = 1;
        this.maxArity = 1;
        return this;
    }

    public FunctionBuilder takingBinary() {
        this.minArity = 2;
        this.maxArity = 2;
        return this;
    }

    public FunctionBuilder takingExactly(int arity) {
        if (arity < 0) {
            throw new IllegalArgumentException("Arity must be non-negative, got: " + arity);
        }
        this.minArity = arity;
        this.maxArity = arity;
        return this;
    }

    public FunctionBuilder takingVariadic(int min) {
        if (min < 1) {
            throw new IllegalArgumentException("Minimum arity must be at least 1, got: " + min);
        }
        this.minArity = min;
        this.maxArity = Integer.MAX_VALUE;
        return this;
    }

    public FunctionBuilder takingBetween(int min, int max) {
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("Invalid arity range: [" + min + ", " + max + "]");
        }
        this.minArity = min;
        this.maxArity = max;
        return this;
    }

    // ==================== Broadcasting ====================

    public FunctionBuilder withBroadcasting() {
        this.supportsBroadcasting = true;
        return this;
    }

    public FunctionBuilder noBroadcasting() {
        this.supportsBroadcasting = false;
        return this;
    }

    // ==================== Implementation - Unary ====================

    /**
     * Creates a unary function from a simple double operation with automatic broadcasting.
     * <p>
     * For functions that need validation, use {@link #implementedBy(UnaryFunction)} instead
     * and validate via {@link FunctionContext} helpers.
     */
    public MathFunction implementedByDouble(DoubleUnaryOperator op) {
        validateUnaryArity();

        return implementedByAggregate((args, ctx) -> {
            // Safe: arity validated by FunctionExecutor before calling apply()
            NodeConstant arg = args.getFirst();
            if (supportsBroadcasting) {
                return ctx.mapDouble(arg, op);
            } else {
                double value = ctx.toDouble(arg);
                return new NodeDouble(op.applyAsDouble(value));
            }
        });
    }

    /**
     * As {@link #implementedByDouble(DoubleUnaryOperator)}, but the answer keeps the
     * argument's marker: use it for a root, not for a logarithm.
     */
    public MathFunction implementedByMagnitude(DoubleUnaryOperator op) {
        validateUnaryArity();

        return implementedByAggregate((args, ctx) -> {
            // Safe: arity validated by FunctionExecutor before calling apply()
            return ctx.mapMagnitude(args.getFirst(), op);
        });
    }

    /**
     * Creates a unary function from a lambda (full control over types).
     * <p>
     * When broadcasting is enabled (default), the function is automatically applied
     * element-wise to vectors and matrices via {@link BroadcastingEngine#applyUnary}.
     * Functions that already broadcast internally should call {@link #noBroadcasting()}
     * to avoid double-wrapping.
     * <p>
     * The {@link FunctionContext} is available for validation and type conversion.
     */
    public MathFunction implementedBy(UnaryFunction fn) {
        validateUnaryArity();

        // Safe: arity validated by FunctionExecutor before calling apply()
        return implementedByAggregate((args, ctx) -> {
            if (supportsBroadcasting) {
                return BroadcastingEngine.applyUnary(args.getFirst(), v -> fn.apply(v, ctx));
            }
            return fn.apply(args.getFirst(), ctx);
        });
    }

    // ==================== Implementation - Binary ====================

    /**
     * Creates a binary function from a simple double operation with automatic type conversion.
     * <p>
     * For functions that need validation, use {@link #implementedBy(BinaryFunction)} instead
     * and validate via {@link FunctionContext} helpers.
     */
    public MathFunction implementedByDouble(DoubleBinaryOperator op) {
        validateBinaryArity();

        BinaryFunction fn = (first, second, ctx) -> {
            double a = ctx.toDouble(first);
            double b = ctx.toDouble(second);
            return new NodeDouble(op.applyAsDouble(a, b));
        };

        return createBroadcastingBinaryFunction(fn);
    }

    /**
     * Creates a binary function from a lambda (full control over types).
     * <p>
     * The {@link FunctionContext} is available for validation and type conversion.
     */
    public MathFunction implementedBy(BinaryFunction fn) {
        validateBinaryArity();
        return createBroadcastingBinaryFunction(fn);
    }

    private MathFunction createBroadcastingBinaryFunction(BinaryFunction fn) {
        return implementedByAggregate((args, ctx) -> {
            // Safe: arity validated by FunctionExecutor before calling apply()
            if (supportsBroadcasting) {
                return BroadcastingEngine.applyBinary(args.get(0), args.get(1),
                        (left, right) -> fn.apply(left, right, ctx));
            } else {
                return fn.apply(args.get(0), args.get(1), ctx);
            }
        });
    }

    // ==================== Typed Parameter Builders ====================

    /**
     * Creates a unary function with type-safe parameter extraction.
     *
     * @param argType the type descriptor for the argument
     * @param <A>     the extracted type
     * @return typed builder for implementation
     */
    public <A> TypedUnaryBuilder<A> takingTyped(ArgType<A> argType) {
        this.minArity = 1;
        this.maxArity = 1;
        this.supportsBroadcasting = false;
        return new TypedUnaryBuilder<>(this, argType);
    }

    /**
     * Creates a binary function with type-safe parameter extraction.
     *
     * @param arg1Type type descriptor for first argument
     * @param arg2Type type descriptor for second argument
     * @param <A>      first extracted type
     * @param <B>      second extracted type
     * @return typed builder for implementation
     */
    public <A, B> TypedBinaryBuilder<A, B> takingTyped(ArgType<A> arg1Type, ArgType<B> arg2Type) {
        this.minArity = 2;
        this.maxArity = 2;
        this.supportsBroadcasting = false;
        return new TypedBinaryBuilder<>(this, arg1Type, arg2Type);
    }

    /**
     * Creates a ternary function with type-safe parameter extraction.
     *
     * @param arg1Type type descriptor for first argument
     * @param arg2Type type descriptor for second argument
     * @param arg3Type type descriptor for third argument
     * @param <A>      first extracted type
     * @param <B>      second extracted type
     * @param <C>      third extracted type
     * @return typed builder for implementation
     */
    public <A, B, C> TypedTernaryBuilder<A, B, C> takingTyped(
            ArgType<A> arg1Type, ArgType<B> arg2Type, ArgType<C> arg3Type) {
        this.minArity = 3;
        this.maxArity = 3;
        this.supportsBroadcasting = false;
        return new TypedTernaryBuilder<>(this, arg1Type, arg2Type, arg3Type);
    }

    // ==================== Assembly ====================

    /**
     * Creates a function whose arguments are evaluated before the body runs.
     */
    public MathFunction implementedByAggregate(AggregateFunction fn) {
        return BuiltinFunction.eager(metadata(), fn);
    }

    /**
     * Creates a function that receives unevaluated argument nodes plus an evaluator,
     * so it can skip arguments it does not need. See {@link LazyFunction}.
     */
    public MathFunction implementedByLazy(LazyFunction.Body fn) {
        return BuiltinFunction.lazy(metadata(), fn);
    }

    BuiltinFunction.Metadata metadata() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Function name is required");
        }
        return new BuiltinFunction.Metadata(
                name,
                List.copyOf(aliases),
                description != null ? description : name + " function",
                List.copyOf(parameterSets),
                category,
                minArity,
                maxArity,
                supportsBroadcasting);
    }

    // ==================== Validation ====================

    private void validateUnaryArity() {
        if (minArity != 1 || maxArity != 1) {
            throw new IllegalStateException("Unary functions must have arity [1,1], got [" + minArity + "," + maxArity + "]");
        }
    }

    private void validateBinaryArity() {
        if (minArity != 2 || maxArity != 2) {
            throw new IllegalStateException("Binary functions must have arity [2,2], got [" + minArity + "," + maxArity + "]");
        }
    }
}
