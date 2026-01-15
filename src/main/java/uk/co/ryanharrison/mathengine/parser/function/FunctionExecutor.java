package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.evaluator.ArityException;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;

import java.util.*;

/**
 * Executor for built-in functions that manages registration and execution.
 * <p>
 * This class serves as the central registry for all built-in functions and handles
 * dispatching function calls to the appropriate implementation.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * FunctionExecutor executor = new FunctionExecutor();
 *
 * // Register standard functions
 * executor.registerAll(StandardFunctions.all());
 *
 * // Execute a function
 * List<NodeConstant> args = List.of(new NodeDouble(0.5));
 * NodeConstant result = executor.execute("sin", args, context);
 * }</pre>
 */
public final class FunctionExecutor {

    private final Map<String, MathFunction> functions;
    private FunctionContext.FunctionCaller functionCaller;

    /**
     * Creates a new function executor with no registered functions.
     */
    public FunctionExecutor() {
        this.functions = new HashMap<>();
        this.functionCaller = null;
    }

    /**
     * Sets the function caller for higher-order functions (map, filter, reduce).
     *
     * @param caller the callback for calling user-defined functions
     */
    public void setFunctionCaller(FunctionContext.FunctionCaller caller) {
        this.functionCaller = caller;
    }

    // ==================== Registration ====================

    /**
     * Registers a function with its primary name and all aliases.
     * <p>
     * The function is registered under its primary name (from {@link MathFunction#name()})
     * and all aliases (from {@link MathFunction#aliases()}). This allows the function
     * to be called by any of its names.
     *
     * @param function the function to register
     * @return this executor for method chaining
     */
    public FunctionExecutor register(MathFunction function) {
        // Register under primary name
        functions.put(function.name().toLowerCase(), function);
        // Register under all aliases
        for (String alias : function.aliases()) {
            functions.put(alias.toLowerCase(), function);
        }
        return this;
    }

    /**
     * Registers a function with an additional alias beyond those defined in the function itself.
     * <p>
     * This method first registers the function normally (including its built-in aliases),
     * then adds the specified additional alias.
     *
     * @param function the function to register
     * @param alias    the additional alias name
     * @return this executor for method chaining
     */
    public FunctionExecutor registerWithAlias(MathFunction function, String alias) {
        register(function);
        functions.put(alias.toLowerCase(), function);
        return this;
    }

    /**
     * Registers multiple functions.
     *
     * @param funcs collection of functions to register
     * @return this executor for method chaining
     */
    public FunctionExecutor registerAll(Collection<MathFunction> funcs) {
        for (MathFunction func : funcs) {
            register(func);
        }
        return this;
    }

    /**
     * Registers multiple functions from a map (name -> function).
     *
     * @param funcs map of names to functions
     * @return this executor for method chaining
     */
    public FunctionExecutor registerAll(Map<String, MathFunction> funcs) {
        for (Map.Entry<String, MathFunction> entry : funcs.entrySet()) {
            functions.put(entry.getKey().toLowerCase(), entry.getValue());
        }
        return this;
    }

    // ==================== Query ====================

    /**
     * Gets a function by name.
     *
     * @param name the function name (case-insensitive)
     * @return the function, or empty if not registered
     */
    public Optional<MathFunction> getFunction(String name) {
        return Optional.ofNullable(functions.get(name.toLowerCase()));
    }

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
     * @return set of function names (lowercase)
     */
    public Set<String> getFunctionNames() {
        return Set.copyOf(functions.keySet());
    }

    /**
     * Gets all registered functions.
     *
     * @return unmodifiable map of functions
     */
    public Map<String, MathFunction> getAllFunctions() {
        return Collections.unmodifiableMap(functions);
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
     * @param name    the function name (case-insensitive)
     * @param args    the evaluated arguments
     * @param context the evaluation context
     * @return the result of the function
     * @throws EvaluationException if the function is not registered
     * @throws ArityException      if the argument count is invalid
     */
    public NodeConstant execute(String name, List<NodeConstant> args, EvaluationContext context) {
        MathFunction function = functions.get(name.toLowerCase());
        if (function == null) {
            throw new EvaluationException("Unknown function: " + name);
        }

        // Validate arity
        validateArity(function, args.size());

        // Check for vector broadcasting
        if (function.supportsVectorBroadcasting() && args.size() == 1 &&
                args.getFirst() instanceof NodeVector vector) {
            return applyToVector(function, vector, context);
        }

        // Execute the function
        FunctionContext ctx = new FunctionContext(context, functionCaller);
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
                                       EvaluationContext context) {
        FunctionContext ctx = new FunctionContext(context, functionCaller);
        Node[] elements = vector.getElements();
        Node[] results = new Node[elements.length];

        for (int i = 0; i < elements.length; i++) {
            NodeConstant elem = (NodeConstant) elements[i];
            results[i] = function.apply(List.of(elem), ctx);
        }

        return new NodeVector(results);
    }

    // ==================== Unregistration ====================

    /**
     * Unregisters a function by name.
     *
     * @param name the function name to unregister
     * @return true if a function was unregistered
     */
    public boolean unregister(String name) {
        return functions.remove(name.toLowerCase()) != null;
    }

    /**
     * Clears all registered functions.
     */
    public void clear() {
        functions.clear();
    }
}
