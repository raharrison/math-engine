package uk.co.ryanharrison.mathengine.parser.registry;

import uk.co.ryanharrison.mathengine.parser.function.FunctionExecutor;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Registry for function names used during tokenization.
 * <p>
 * The lexer uses this registry to classify identifiers as function names.
 * This registry can be synchronized with a {@link FunctionExecutor} to
 * automatically include all registered function names.
 * <p>
 * Note: The set of available functions is configured through
 * {@link uk.co.ryanharrison.mathengine.parser.MathEngineConfig#functions()}.
 * The FunctionRegistry is typically created automatically from the FunctionExecutor.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Create from FunctionExecutor (preferred)
 * FunctionExecutor executor = new FunctionExecutor();
 * executor.registerAll(config.functions());
 * FunctionRegistry registry = FunctionRegistry.fromExecutor(executor);
 *
 * // Or create from a list of functions
 * FunctionRegistry registry = FunctionRegistry.fromFunctions(StandardFunctions.all());
 *
 * // Or create empty and add names manually
 * FunctionRegistry registry = new FunctionRegistry();
 * registry.register("customFunc");
 * }</pre>
 */
public final class FunctionRegistry {

    private final Set<String> functionNames;

    /**
     * Creates an empty function registry.
     */
    public FunctionRegistry() {
        this.functionNames = new HashSet<>();
    }

    /**
     * Creates a function registry populated from a FunctionExecutor.
     * <p>
     * This method retrieves all function names from the executor, including
     * aliases that were registered.
     *
     * @param executor the function executor to get names from
     * @return new registry with all function names from the executor
     */
    public static FunctionRegistry fromExecutor(FunctionExecutor executor) {
        FunctionRegistry registry = new FunctionRegistry();
        registry.functionNames.addAll(executor.getFunctionNames());
        return registry;
    }

    /**
     * Creates a function registry populated from a collection of functions.
     * <p>
     * This method registers all function names and their aliases.
     *
     * @param functions the functions to register
     * @return new registry with all function names
     */
    public static FunctionRegistry fromFunctions(Collection<MathFunction> functions) {
        FunctionRegistry registry = new FunctionRegistry();
        for (MathFunction func : functions) {
            registry.register(func.name());
            for (String alias : func.aliases()) {
                registry.register(alias);
            }
        }
        return registry;
    }

    /**
     * Synchronizes this registry with a FunctionExecutor.
     * Adds all function names from the executor to this registry.
     *
     * @param executor the function executor to sync with
     */
    public void syncWith(FunctionExecutor executor) {
        functionNames.addAll(executor.getFunctionNames());
    }

    /**
     * Register a function name.
     *
     * @param name the function name (case-insensitive)
     */
    public void register(String name) {
        functionNames.add(name.toLowerCase());
    }

    /**
     * Unregister a function name.
     *
     * @param name the function name to remove
     * @return true if the name was removed
     */
    public boolean unregister(String name) {
        return functionNames.remove(name.toLowerCase());
    }

    /**
     * Check if a name is a registered function.
     *
     * @param name the name to check (case-insensitive)
     * @return true if the name is registered
     */
    public boolean isFunction(String name) {
        return functionNames.contains(name.toLowerCase());
    }

    /**
     * Get all registered function names.
     *
     * @return unmodifiable set of function names
     */
    public Set<String> getFunctionNames() {
        return Set.copyOf(functionNames);
    }

    /**
     * Clear all registered function names.
     */
    public void clear() {
        functionNames.clear();
    }
}
