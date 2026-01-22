package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.evaluator.ArityException;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;

import java.util.*;

/**
 * Immutable executor for built-in functions.
 * <p>
 * Once created, the executor cannot be modified, making it thread-safe.
 * All functions must be provided at construction time.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Create with standard functions
 * FunctionExecutor executor = FunctionExecutor.of(StandardFunctions.all());
 *
 * // Create with custom functions
 * FunctionExecutor executor = FunctionExecutor.builder()
 *     .add(myCustomFunction)
 *     .addAll(StandardFunctions.basic())
 *     .build();
 *
 * // Execute a function
 * List<NodeConstant> args = List.of(new NodeDouble(0.5));
 * NodeConstant result = executor.execute("sin", args, context, functionCaller);
 * }</pre>
 */
public final class FunctionExecutor {

    private final Map<String, MathFunction> functions;

    private FunctionExecutor(Map<String, MathFunction> functions) {
        this.functions = Map.copyOf(functions);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an empty function executor.
     *
     * @return new empty executor
     */
    public static FunctionExecutor empty() {
        return new FunctionExecutor(Map.of());
    }

    /**
     * Creates a function executor with the given functions.
     *
     * @param functions the functions to register
     * @return new executor with all functions registered
     */
    public static FunctionExecutor of(Collection<MathFunction> functions) {
        return builder().addAll(functions).build();
    }

    /**
     * Creates a new builder for constructing function executors.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Query ====================

    /**
     * Checks if a function is registered.
     *
     * @param name the function name (case-insensitive)
     * @return true if the function is registered
     */
    public boolean hasFunction(String name) {
        return functions.containsKey(name.toLowerCase());
    }

    /**
     * Gets all registered function names.
     *
     * @return unmodifiable set of function names (lowercase)
     */
    public Set<String> getFunctionNames() {
        return functions.keySet();
    }

    /**
     * Gets functions by category.
     *
     * @param category the category to filter by
     * @return list of functions in the category
     */
    public List<MathFunction> getFunctionsByCategory(MathFunction.Category category) {
        return functions.values().stream()
                .filter(f -> f.category() == category)
                .distinct()
                .toList();
    }

    // ==================== Execution ====================

    /**
     * Executes a function by name.
     *
     * @param name           the function name (case-insensitive)
     * @param args           the evaluated arguments
     * @param context        the evaluation context
     * @param functionCaller callback for higher-order functions to call user functions
     * @return the result of the function
     * @throws EvaluationException if the function is not registered
     * @throws ArityException      if the argument count is invalid
     */
    public NodeConstant execute(String name, List<NodeConstant> args,
                                EvaluationContext context, FunctionCaller functionCaller) {
        MathFunction function = functions.get(name.toLowerCase());
        if (function == null) {
            throw new EvaluationException("Unknown function: " + name);
        }

        // Validate arity
        validateArity(function, args.size());

        // Check for vector broadcasting
        if (function.supportsVectorBroadcasting() && args.size() == 1 &&
                args.getFirst() instanceof NodeVector vector) {
            return applyToVector(function, vector, context, functionCaller);
        }

        // Execute the function
        var ctx = new FunctionContext(context, functionCaller);
        return function.apply(args, ctx);
    }

    /**
     * Validates the argument count against the function's arity constraints.
     */
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

    /**
     * Applies a single-argument function to each element of a vector.
     */
    private NodeConstant applyToVector(MathFunction function, NodeVector vector,
                                       EvaluationContext context, FunctionCaller functionCaller) {
        var ctx = new FunctionContext(context, functionCaller);
        Node[] elements = vector.getElements();
        Node[] results = new Node[elements.length];

        for (int i = 0; i < elements.length; i++) {
            NodeConstant elem = (NodeConstant) elements[i];
            results[i] = function.apply(List.of(elem), ctx);
        }

        return new NodeVector(results);
    }

    // ==================== Builder ====================

    /**
     * Builder for constructing {@link FunctionExecutor} instances.
     */
    public static final class Builder {
        private final Map<String, MathFunction> functions = new HashMap<>();

        private Builder() {
        }

        /**
         * Adds a function with its primary name and all aliases.
         *
         * @param function the function to add
         * @return this builder
         */
        public Builder add(MathFunction function) {
            if (function == null) {
                throw new IllegalArgumentException("Function cannot be null");
            }
            // Register under primary name
            functions.put(function.name().toLowerCase(), function);
            // Register under all aliases
            for (String alias : function.aliases()) {
                functions.put(alias.toLowerCase(), function);
            }
            return this;
        }

        /**
         * Adds multiple functions.
         *
         * @param funcs collection of functions to add
         * @return this builder
         */
        public Builder addAll(Collection<MathFunction> funcs) {
            for (MathFunction func : funcs) {
                add(func);
            }
            return this;
        }

        /**
         * Builds the function executor.
         *
         * @return new function executor
         */
        public FunctionExecutor build() {
            return new FunctionExecutor(functions);
        }
    }
}
