package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;
import uk.co.ryanharrison.mathengine.parser.util.PersistentHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Runtime evaluation context storing variables, user-defined functions, and scope chain.
 * <p>
 * Configuration settings (angleUnit, forceDoubleArithmetic, etc.) are accessed from
 * the shared {@link MathEngineConfig} - not duplicated here.
 * <p>
 * Supports lexical scoping through parent contexts for function calls.
 * <p>
 * Uses {@link PersistentHashMap} for variables to enable efficient structural sharing
 * when creating child contexts or snapshots for closures.
 */
public final class EvaluationContext {

    private final MathEngineConfig config;
    private PersistentHashMap<String, NodeConstant> variables;
    private final Map<String, FunctionDefinition> functions;
    private final EvaluationContext parent;
    private final RecursionTracker recursionTracker;

    /**
     * Create a root context with config and recursion tracker.
     */
    public EvaluationContext(MathEngineConfig config, RecursionTracker recursionTracker) {
        this(config, PersistentHashMap.empty(), new HashMap<>(), null, recursionTracker);
    }

    private EvaluationContext(
            MathEngineConfig config,
            PersistentHashMap<String, NodeConstant> variables,
            Map<String, FunctionDefinition> functions,
            EvaluationContext parent,
            RecursionTracker recursionTracker) {
        this.config = config;
        this.variables = variables;
        this.functions = functions;
        this.parent = parent;
        this.recursionTracker = recursionTracker;
    }

    /**
     * Create a child context with variable bindings (for function calls).
     * Shares config and recursion tracker with this context.
     * <p>
     * Each child has its own independent persistent map containing only its local bindings.
     * The EvaluationContext.parent chain is used for scope lookup, allowing assign()
     * to properly update variables in their defining scope (important for closures).
     */
    public EvaluationContext withBindings(Map<String, NodeConstant> bindings) {
        // Create independent map for child - don't chain persistent maps
        // This ensures assign() can correctly find where variables are defined via ctx.parent
        PersistentHashMap<String, NodeConstant> childVars = PersistentHashMap.from(bindings);
        return new EvaluationContext(this.config, childVars, new HashMap<>(), this, this.recursionTracker);
    }

    /**
     * Create a snapshot for closure capture - copies all variables from scope chain.
     * Ensures lexical scoping works correctly even if original context is modified later.
     */
    public EvaluationContext snapshot() {
        var allVariables = new HashMap<String, NodeConstant>();
        collectVariables(allVariables);
        PersistentHashMap<String, NodeConstant> snapshotVars = PersistentHashMap.from(allVariables);

        var snapshotFunctions = new HashMap<String, FunctionDefinition>();
        collectFunctions(snapshotFunctions);

        return new EvaluationContext(this.config, snapshotVars, snapshotFunctions, null, this.recursionTracker);
    }

    private void collectVariables(Map<String, NodeConstant> target) {
        if (parent != null) {
            parent.collectVariables(target);
        }
        target.putAll(this.variables.toMap());
    }

    private void collectFunctions(Map<String, FunctionDefinition> target) {
        if (parent != null) {
            parent.collectFunctions(target);
        }
        target.putAll(this.functions);
    }

    // ==================== Variable Management ====================

    /**
     * Define a variable in this context (local scope).
     * Creates a new layer in the persistent map.
     */
    public void define(String name, NodeConstant value) {
        variables = variables.assoc(name, value);
    }

    /**
     * Remove a variable from this context (local scope).
     * Creates a new layer that shadows the variable with removal.
     */
    public void removeVariable(String name) {
        variables = variables.dissoc(name);
    }

    /**
     * Assign a value to a variable, updating where it exists in the scope chain.
     * If not found anywhere, creates in current context.
     * <p>
     * This preserves closure semantics - closures can modify variables from
     * their defining scope.
     */
    public void assign(String name, NodeConstant value) {
        EvaluationContext ctx = this;
        while (ctx != null) {
            if (ctx.variables.containsKey(name)) {
                ctx.variables = ctx.variables.assoc(name, value);
                return;
            }
            ctx = ctx.parent;
        }
        // Not found anywhere, define in current scope
        variables = variables.assoc(name, value);
    }

    /**
     * Resolve a variable by name, searching up the scope chain and constant registry.
     *
     * @param name the variable name to resolve
     * @return the variable value, or empty if not found anywhere
     */
    public Optional<NodeConstant> resolve(String name) {
        if (variables.containsKey(name)) {
            return Optional.of(variables.get(name));
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        return config.constantRegistry().getValue(name);
    }

    // ==================== Function Management ====================

    /**
     * Define a user function in this context.
     */
    public void defineFunction(String name, FunctionDefinition function) {
        functions.put(name, function);
    }

    /**
     * Resolve a user function by name, searching up the scope chain.
     *
     * @param name the function name to resolve
     * @return the function definition, or empty if not found
     */
    public Optional<FunctionDefinition> resolveFunction(String name) {
        if (functions.containsKey(name)) {
            return Optional.of(functions.get(name));
        }
        if (parent != null) {
            return parent.resolveFunction(name);
        }
        return Optional.empty();
    }

    // ==================== Recursion Tracking ====================

    public void enterFunction(String functionName) {
        recursionTracker.enterFunction(functionName);
    }

    public void exitFunction() {
        recursionTracker.exitFunction();
    }

    // ==================== Config Accessors ====================

    public Optional<NodeConstant> resolveConstant(String name) {
        return config.constantRegistry().getValue(name);
    }

    public Optional<UnitDefinition> resolveUnit(String name) {
        return config.unitRegistry().getUnit(name);
    }

    public AngleUnit getAngleUnit() {
        return config.angleUnit();
    }

    public boolean isForceDoubleArithmetic() {
        return config.forceDoubleArithmetic();
    }

    public boolean isSilentValidation() {
        return config.silentValidation();
    }

    // ==================== Local State Accessors ====================

    /**
     * Gets all variables visible in this context (including parent scopes).
     * Returns an immutable snapshot.
     */
    public Map<String, NodeConstant> getLocalVariables() {
        return Map.copyOf(variables.toMap());
    }

    /**
     * Gets functions defined in this context only (not parent scopes).
     */
    public Map<String, FunctionDefinition> getLocalFunctions() {
        return Map.copyOf(functions);
    }
}
