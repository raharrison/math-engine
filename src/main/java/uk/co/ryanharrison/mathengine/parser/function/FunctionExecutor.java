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
     * Executes a function by name with automatic arity validation and input normalization.
     *
     * <h3>Execution Flow:</h3>
     * <ol>
     *   <li>Look up function by name (case-insensitive)</li>
     *   <li>Normalize inputs if needed (variadic → vector for broadcasting functions)</li>
     *   <li>Validate argument count against function's arity constraints</li>
     *   <li>Create FunctionContext with evaluation context and function caller</li>
     *   <li>Call {@link MathFunction#apply(List, FunctionContext)}</li>
     *   <li>Return result</li>
     * </ol>
     *
     * <h3>Input Normalization:</h3>
     * <p>
     * For functions that support broadcasting with arity [1,1], multiple arguments
     * are automatically normalized to a vector:
     * <pre>{@code
     * sqrt(4, 9, 16) → sqrt([4, 9, 16]) → [2, 3, 4]
     * }</pre>
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

        // Normalize inputs: if function expects 1 arg but got multiple, and supports broadcasting,
        // wrap multiple args as a vector for automatic broadcasting
        List<NodeConstant> normalizedArgs = normalizeInputs(function, args);

        // Validate arity
        validateArity(function, normalizedArgs.size());

        // Execute the function (broadcasting handled by function implementation)
        var ctx = new FunctionContext(function.name(), context, functionCaller);
        return function.apply(normalizedArgs, ctx);
    }

    /**
     * Normalizes inputs for functions that support broadcasting.
     * <p>
     * If a function expects exactly 1 argument (unary) but receives multiple arguments,
     * and it supports broadcasting, the arguments are wrapped into a vector.
     * This allows natural syntax like {@code sqrt(4, 9, 16)} instead of {@code sqrt([4, 9, 16])}.
     *
     * @param function the function to execute
     * @param args     the original arguments
     * @return normalized arguments (either original or wrapped in vector)
     */
    private List<NodeConstant> normalizeInputs(MathFunction function, List<NodeConstant> args) {
        // If function expects exactly 1 arg, got multiple, and supports broadcasting:
        // wrap args in a vector
        if (function.minArity() == 1 && function.maxArity() == 1 &&
                args.size() > 1 && function.supportsVectorBroadcasting()) {

            Node[] elements = args.toArray(new Node[0]);
            return List.of(new NodeVector(elements));
        }

        return args;
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
