package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime evaluation context storing variables, user-defined functions, and scope chain.
 * <p>
 * Configuration settings (angleUnit, forceDoubleArithmetic, etc.) are accessed from
 * the shared {@link MathEngineConfig} - not duplicated here.
 * <p>
 * Supports lexical scoping through parent contexts for function calls.
 */
public final class EvaluationContext {

    private final MathEngineConfig config;
    private final Map<String, NodeConstant> variables;
    private final Map<String, FunctionDefinition> functions;
    private final EvaluationContext parent;
    private final RecursionTracker recursionTracker;

    /**
     * Create a root context with config and recursion tracker.
     */
    public EvaluationContext(MathEngineConfig config, RecursionTracker recursionTracker) {
        this(config, null, recursionTracker);
    }

    private EvaluationContext(MathEngineConfig config, EvaluationContext parent, RecursionTracker recursionTracker) {
        this.config = config;
        this.variables = new HashMap<>();
        this.functions = new HashMap<>();
        this.parent = parent;
        this.recursionTracker = recursionTracker;
    }

    /**
     * Create a child context with variable bindings (for function calls).
     * Shares config and recursion tracker with this context.
     */
    public EvaluationContext withBindings(Map<String, NodeConstant> bindings) {
        EvaluationContext child = new EvaluationContext(this.config, this, this.recursionTracker);
        child.variables.putAll(bindings);
        return child;
    }

    /**
     * Create a snapshot for closure capture - copies all variables from scope chain.
     * Ensures lexical scoping works correctly even if original context is modified later.
     */
    public EvaluationContext snapshot() {
        EvaluationContext snapshot = new EvaluationContext(this.config, null, this.recursionTracker);
        collectVariables(snapshot.variables);
        collectFunctions(snapshot.functions);
        return snapshot;
    }

    private void collectVariables(Map<String, NodeConstant> target) {
        if (parent != null) {
            parent.collectVariables(target);
        }
        target.putAll(this.variables);
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
     */
    public void define(String name, NodeConstant value) {
        variables.put(name, value);
    }

    /**
     * Remove a variable from this context (local scope).
     */
    public void removeVariable(String name) {
        variables.remove(name);
    }

    /**
     * Assign a value to a variable, updating where it exists in the scope chain.
     * If not found anywhere, creates in current context.
     */
    public void assign(String name, NodeConstant value) {
        EvaluationContext ctx = this;
        while (ctx != null) {
            if (ctx.variables.containsKey(name)) {
                ctx.variables.put(name, value);
                return;
            }
            ctx = ctx.parent;
        }
        variables.put(name, value);
    }

    /**
     * Resolve a variable by name, searching up the scope chain.
     */
    public NodeConstant resolve(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        if (parent != null) {
            return parent.resolve(name);
        }
        throw new UndefinedVariableException(name);
    }

    /**
     * Check if a variable is defined in this context or any parent.
     */
    public boolean isDefined(String name) {
        if (variables.containsKey(name)) {
            return true;
        }
        return parent != null && parent.isDefined(name);
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
     */
    public FunctionDefinition resolveFunction(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        }
        if (parent != null) {
            return parent.resolveFunction(name);
        }
        return null;
    }

    /**
     * Check if a user function is defined in this context or any parent.
     */
    public boolean isFunctionDefined(String name) {
        if (functions.containsKey(name)) {
            return true;
        }
        return parent != null && parent.isFunctionDefined(name);
    }

    // ==================== Recursion Tracking ====================

    public void enterFunction(String functionName) {
        recursionTracker.enterFunction(functionName);
    }

    public void exitFunction() {
        recursionTracker.exitFunction();
    }

    // ==================== Config Accessors (delegated to config) ====================

    public AngleUnit getAngleUnit() {
        return config.angleUnit();
    }

    public boolean isForceDoubleArithmetic() {
        return config.forceDoubleArithmetic();
    }

    public UnitRegistry getUnitRegistry() {
        return config.unitRegistry();
    }

    public MathEngineConfig getConfig() {
        return config;
    }

    // ==================== Local State Accessors ====================

    /**
     * Gets variables defined in this context only (not parent scopes).
     */
    public Map<String, NodeConstant> getLocalVariables() {
        return Map.copyOf(variables);
    }

    /**
     * Gets functions defined in this context only (not parent scopes).
     */
    public Map<String, FunctionDefinition> getLocalFunctions() {
        return Map.copyOf(functions);
    }
}
