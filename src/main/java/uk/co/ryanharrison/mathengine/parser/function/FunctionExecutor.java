package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.evaluator.ArityException;
import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;

import java.util.*;

/**
 * Immutable, thread-safe registry and dispatcher for built-in functions.
 *
 * <pre>{@code
 * FunctionExecutor executor = FunctionExecutor.of(StandardFunctions.all());
 * NodeConstant result = executor.execute("sin", List.of(new NodeDouble(0.5)), context, caller);
 * }</pre>
 */
public final class FunctionExecutor {

    /**
     * Keyed by lowercase name and by every alias.
     */
    private final Map<String, MathFunction> byName;

    private FunctionExecutor(Map<String, MathFunction> byName) {
        this.byName = Map.copyOf(byName);
    }

    public static FunctionExecutor empty() {
        return new FunctionExecutor(Map.of());
    }

    public static FunctionExecutor of(Collection<MathFunction> functions) {
        return builder().addAll(functions).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // ==================== Query ====================

    public boolean hasFunction(String name) {
        return byName.containsKey(name.toLowerCase());
    }

    /**
     * @throws EvaluationException if no function is registered under that name
     */
    public MathFunction lookup(String name) {
        MathFunction function = byName.get(name.toLowerCase());
        if (function == null) {
            throw new EvaluationException("Unknown function: " + name);
        }
        return function;
    }

    /**
     * Every name a function can be called by, including aliases. Used to seed the lexer.
     */
    public Set<String> getCallableNames() {
        return byName.keySet();
    }

    public List<MathFunction> getFunctionsByCategory(MathFunction.Category category) {
        return byName.values().stream()
                .filter(f -> f.category() == category)
                .distinct()
                .toList();
    }

    // ==================== Execution ====================

    /**
     * Looks up a function by name and applies it to already-evaluated arguments.
     *
     * @throws EvaluationException if the function is not registered
     * @throws ArityException      if the argument count is invalid
     */
    public NodeConstant execute(String name, List<NodeConstant> args,
                                EvaluationContext context, FunctionCaller functionCaller) {
        return execute(lookup(name), args, context, functionCaller);
    }

    /**
     * Applies a function to already-evaluated arguments.
     * <p>
     * A unary broadcasting function called with several arguments treats them as a
     * vector, so {@code sqrt(4, 9, 16)} means {@code sqrt([4, 9, 16])}.
     */
    public NodeConstant execute(MathFunction function, List<NodeConstant> args,
                                EvaluationContext context, FunctionCaller functionCaller) {
        List<NodeConstant> normalised = normaliseArguments(function, args);
        validateArity(function, normalised.size());

        var ctx = new FunctionContext(function.name(), context, functionCaller);
        try {
            return function.apply(normalised, ctx);
        } catch (DomainException | ArithmeticException e) {
            if (context.isSilentValidation()) {
                return new NodeDouble(Double.NaN);
            }
            throw e;
        }
    }

    /**
     * Applies a lazy function to unevaluated argument nodes.
     *
     * @throws ArityException if the argument count is invalid
     */
    public NodeConstant executeLazy(LazyFunction function, List<Node> args, EvaluationContext context,
                                    FunctionCaller functionCaller, LazyFunction.ArgumentEvaluator evaluate) {
        validateArity(function, args.size());
        return function.applyLazy(args, new FunctionContext(function.name(), context, functionCaller), evaluate);
    }

    private List<NodeConstant> normaliseArguments(MathFunction function, List<NodeConstant> args) {
        boolean unary = function.minArity() == 1 && function.maxArity() == 1;
        if (unary && args.size() > 1 && function.supportsVectorBroadcasting()) {
            return List.of(new NodeVector(args.toArray(new Node[0])));
        }
        return args;
    }

    private void validateArity(MathFunction function, int argCount) {
        if (argCount < function.minArity()) {
            throw new ArityException("Function '" + function.name() + "' requires at least " +
                    function.minArity() + " argument(s), got " + argCount);
        }
        if (argCount > function.maxArity()) {
            throw new ArityException("Function '" + function.name() + "' accepts at most " +
                    function.maxArity() + " argument(s), got " + argCount);
        }
    }

    // ==================== Builder ====================

    public static final class Builder {

        private final Map<String, MathFunction> byName = new HashMap<>();

        private Builder() {
        }

        /**
         * Registers a function under its name and all of its aliases.
         *
         * @throws IllegalArgumentException if any of those names is already taken
         */
        public Builder add(MathFunction function) {
            Objects.requireNonNull(function, "function");
            register(function.name(), function);
            for (String alias : function.aliases()) {
                register(alias, function);
            }
            return this;
        }

        public Builder addAll(Collection<MathFunction> functions) {
            functions.forEach(this::add);
            return this;
        }

        private void register(String name, MathFunction function) {
            MathFunction existing = byName.putIfAbsent(name.toLowerCase(), function);
            if (existing != null && existing != function) {
                throw new IllegalArgumentException("Function name '" + name + "' is already registered to " +
                        existing.name() + "; cannot also register " + function.name());
            }
        }

        public FunctionExecutor build() {
            return new FunctionExecutor(byName);
        }
    }
}
