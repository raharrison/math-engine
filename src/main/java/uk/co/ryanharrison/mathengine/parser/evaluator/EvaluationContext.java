package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A single variable/function scope, linked to its parent to form a scope chain.
 * <p>
 * Configuration settings (angle unit, arithmetic mode, ...) live in the shared
 * {@link MathEngineConfig} rather than being duplicated per scope.
 */
public final class EvaluationContext {

    private final MathEngineConfig config;
    private final Map<String, NodeConstant> variables;
    private final EvaluationContext parent;
    private final RecursionTracker recursionTracker;

    /**
     * Created on first use. A call scope almost never defines a function, so leaving
     * this null keeps the walk in {@link #resolveFunction} a pointer chase rather than
     * a hash lookup per level, and saves a map per call.
     */
    private Map<String, FunctionDefinition> functions;

    /**
     * Cached operator context; created on first use and reused for this scope.
     */
    private OperatorContext operatorContext;

    public EvaluationContext(MathEngineConfig config, RecursionTracker recursionTracker) {
        this(config, new HashMap<>(), null, null, recursionTracker);
    }

    private EvaluationContext(
            MathEngineConfig config,
            Map<String, NodeConstant> variables,
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
     * Creates a child scope holding the given bindings, with this context as its parent.
     * Used for function calls, comprehension iterations and compiled-expression bindings.
     */
    public EvaluationContext withBindings(Map<String, NodeConstant> bindings) {
        return new EvaluationContext(config, new HashMap<>(bindings), null, this, recursionTracker);
    }

    /**
     * Flattens the whole scope chain into a detached root scope.
     * Used to capture a lambda's defining environment for lexical scoping.
     */
    public EvaluationContext snapshot() {
        var allVariables = new HashMap<String, NodeConstant>();
        var allFunctions = new HashMap<String, FunctionDefinition>();
        collectInto(allVariables, allFunctions);
        return new EvaluationContext(config, allVariables,
                allFunctions.isEmpty() ? null : allFunctions, null, recursionTracker);
    }

    private void collectInto(Map<String, NodeConstant> targetVars, Map<String, FunctionDefinition> targetFuncs) {
        if (parent != null) {
            parent.collectInto(targetVars, targetFuncs);
        }
        targetVars.putAll(variables);
        if (functions != null) {
            targetFuncs.putAll(functions);
        }
    }

    // ==================== Variables ====================

    /** Defines a variable in this scope, shadowing any parent binding. */
    public void define(String name, NodeConstant value) {
        variables.put(name, value);
    }

    /** Removes a variable from this scope. Parent bindings are unaffected. */
    public void removeVariable(String name) {
        variables.remove(name);
    }

    /**
     * Assigns a variable in the scope that already defines it, so closures can
     * mutate variables from their defining scope. Defines locally if unknown.
     */
    public void assign(String name, NodeConstant value) {
        for (EvaluationContext ctx = this; ctx != null; ctx = ctx.parent) {
            if (ctx.variables.containsKey(name)) {
                ctx.variables.put(name, value);
                return;
            }
        }
        variables.put(name, value);
    }

    /** Resolves a variable up the scope chain, falling back to the constant registry. */
    public Optional<NodeConstant> resolve(String name) {
        for (EvaluationContext ctx = this; ctx != null; ctx = ctx.parent) {
            NodeConstant value = ctx.variables.get(name);
            if (value != null) {
                return Optional.of(value);
            }
        }
        return config.constantRegistry().getValue(name);
    }

    // ==================== User functions ====================

    public void defineFunction(String name, FunctionDefinition function) {
        if (functions == null) {
            functions = new HashMap<>();
        }
        functions.put(name, function);
    }

    public Optional<FunctionDefinition> resolveFunction(String name) {
        for (EvaluationContext ctx = this; ctx != null; ctx = ctx.parent) {
            if (ctx.functions != null) {
                FunctionDefinition function = ctx.functions.get(name);
                if (function != null) {
                    return Optional.of(function);
                }
            }
        }
        return Optional.empty();
    }

    // ==================== Recursion ====================

    public void enterFunction(String functionName) {
        recursionTracker.enterFunction(functionName);
    }

    public void exitFunction() {
        recursionTracker.exitFunction();
    }

    // ==================== Config ====================

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

    /**
     * Decimal places for displayed output, or negative for full precision.
     */
    public int getDecimalPlaces() {
        return config.decimalPlaces();
    }

    public boolean isSilentValidation() {
        return config.silentValidation();
    }

    /**
     * Returns the operator context for this scope, creating it on first use.
     * The caller is fixed for the lifetime of an engine, so one instance per scope suffices.
     */
    public OperatorContext operatorContext(FunctionCaller functionCaller) {
        if (operatorContext == null) {
            operatorContext = new OperatorContext(this, functionCaller);
        }
        return operatorContext;
    }

    // ==================== Introspection ====================

    /** Variables defined directly in this scope, excluding parents. */
    public Map<String, NodeConstant> getLocalVariables() {
        return Map.copyOf(variables);
    }

    /** Functions defined directly in this scope, excluding parents. */
    public Map<String, FunctionDefinition> getLocalFunctions() {
        return functions == null ? Map.of() : Map.copyOf(functions);
    }
}
