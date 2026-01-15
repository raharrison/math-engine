package uk.co.ryanharrison.mathengine.parser;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.Evaluator;
import uk.co.ryanharrison.mathengine.parser.evaluator.RecursionTracker;
import uk.co.ryanharrison.mathengine.parser.function.FunctionExecutor;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.lexer.Lexer;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorExecutor;
import uk.co.ryanharrison.mathengine.parser.parser.Parser;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.registry.*;
import uk.co.ryanharrison.mathengine.parser.util.ResultFormatter;

import java.util.*;

/**
 * Main entry point for the Math Engine parser and evaluator.
 * <p>
 * MathEngine is the central configuration point for the parser system.
 * It creates and manages all components:
 * <ul>
 *     <li>{@link OperatorExecutor} - handles operator implementations</li>
 *     <li>{@link FunctionExecutor} - handles built-in function implementations</li>
 *     <li>{@link FunctionRegistry} - provides function names for lexer</li>
 *     <li>{@link Evaluator} - evaluates parsed expressions</li>
 * </ul>
 *
 * <h2>Quick Start:</h2>
 * <pre>{@code
 * // Simple evaluation
 * MathEngine engine = MathEngine.create();
 * NodeConstant result = engine.evaluate("2 + 3 * 4");
 * System.out.println(result); // 14
 *
 * // With custom configuration
 * MathEngine engine = MathEngine.builder()
 *     .angleUnit(AngleUnit.DEGREES)
 *     .build();
 * NodeConstant result = engine.evaluate("sin(90)");
 * System.out.println(result); // 1.0
 * }</pre>
 *
 * <h2>Session Support:</h2>
 * <pre>{@code
 * MathEngine engine = MathEngine.create();
 *
 * // Define variables
 * engine.evaluate("x := 10");
 * engine.evaluate("y := 20");
 *
 * // Use them in subsequent evaluations
 * NodeConstant result = engine.evaluate("x + y"); // 30
 *
 * // Define custom functions
 * engine.evaluate("square(n) := n^2");
 * engine.evaluate("square(5)"); // 25
 * }</pre>
 *
 * <h2>Custom Functions:</h2>
 * <pre>{@code
 * MathEngine engine = MathEngine.create();
 *
 * // Register a custom built-in function using MathFunction
 * engine.registerFunction(new MathFunction() {
 *     public String name() { return "double"; }
 *     public int minArity() { return 1; }
 *     public int maxArity() { return 1; }
 *     public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
 *         return new NodeDouble(args.get(0).doubleValue() * 2);
 *     }
 * });
 *
 * engine.evaluate("double(21)"); // 42
 * }</pre>
 */
public final class MathEngine {

    private final MathEngineConfig config;
    private final FunctionExecutor functionExecutor;
    private final FunctionRegistry functionRegistry;
    private final UnitRegistry unitRegistry;
    private final ConstantRegistry constantRegistry;
    private final EvaluationContext context;
    private final Evaluator evaluator;
    private final Lexer lexer;

    private MathEngine(Builder builder) {
        this.config = builder.config;

        // Create executors
        OperatorExecutor operatorExecutor = new OperatorExecutor();
        operatorExecutor.registerBinaryOperators(config.binaryOperators());
        operatorExecutor.registerUnaryOperators(config.unaryOperators());

        this.functionExecutor = new FunctionExecutor();
        this.functionExecutor.registerAll(config.functions());

        // Registries
        this.constantRegistry = config.constantRegistry();
        this.functionRegistry = builder.functionRegistry != null
                ? builder.functionRegistry
                : FunctionRegistry.fromExecutor(functionExecutor);
        this.unitRegistry = config.unitRegistry();

        // Evaluation context
        this.context = new EvaluationContext(config, new RecursionTracker(config.maxRecursionDepth()));
        initializePredefinedConstants();

        // Core components
        this.evaluator = new Evaluator(config, context, operatorExecutor, functionExecutor);
        this.lexer = new Lexer(functionRegistry, unitRegistry, constantRegistry,
                config.keywordRegistry(), config.maxIdentifierLength(), config.implicitMultiplication());
    }

    private void initializePredefinedConstants() {
        for (ConstantDefinition def : constantRegistry.getDefinitions()) {
            context.define(def.name(), def.value());
            for (String alias : def.aliases()) {
                context.define(alias, def.value());
            }
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a new MathEngine with default configuration.
     *
     * @return new MathEngine instance
     */
    public static MathEngine create() {
        return builder().build();
    }

    /**
     * Creates a new MathEngine with the specified configuration.
     *
     * @param config the configuration to use
     * @return new MathEngine instance
     */
    public static MathEngine create(MathEngineConfig config) {
        return builder().config(config).build();
    }

    /**
     * Creates a new builder for constructing a customized MathEngine.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a minimal arithmetic-only engine for maximum performance.
     * <p>
     * Supports only basic operators (+, -, *, /, ^, %) with no functions,
     * no vectors/matrices, no units, and double arithmetic enabled.
     *
     * @return arithmetic-only engine
     * @see MathEngineConfig#arithmetic()
     */
    public static MathEngine arithmetic() {
        return create(MathEngineConfig.arithmetic());
    }

    /**
     * Creates a basic calculator engine with core math functions.
     * <p>
     * Includes trig, hyperbolic, exponential, logarithmic, rounding, and utility functions.
     * Excludes vectors, matrices, lambdas, comprehensions, and specialized functions.
     *
     * @return basic calculator engine
     * @see MathEngineConfig#basic()
     */
    public static MathEngine basic() {
        return create(MathEngineConfig.basic());
    }

    /**
     * Creates a full-featured engine with all capabilities enabled.
     * <p>
     * Equivalent to {@link #create()} - includes vectors, matrices, user-defined functions,
     * lambdas, comprehensions, and all built-in functions.
     *
     * @return full-featured engine
     */
    public static MathEngine full() {
        return create();
    }

    // ==================== Configuration Updates ====================

    /**
     * Creates a new engine with updated config, preserving session state.
     * <p>
     * Transfers all variables and user-defined functions to the new engine.
     * The original engine remains unchanged.
     *
     * @param newConfig the new configuration to use
     * @return new MathEngine with updated config and preserved session state
     */
    public MathEngine withConfig(MathEngineConfig newConfig) {
        MathEngine newEngine = MathEngine.create(newConfig);
        context.getLocalVariables().forEach(newEngine.context::define);
        context.getLocalFunctions().forEach(newEngine.context::defineFunction);
        return newEngine;
    }

    // ==================== Compilation & Evaluation ====================

    /**
     * Compiles an expression for repeated evaluation.
     * <p>
     * Parse once, evaluate many times with different variable values.
     * More efficient than calling {@link #evaluate(String)} repeatedly.
     *
     * @param expression the expression to compile
     * @return compiled expression ready for repeated evaluation
     * @throws MathEngineException if parsing fails
     */
    public CompiledExpression compile(String expression) {
        return new CompiledExpression(expression, parse(expression), this);
    }

    /**
     * Evaluates a mathematical expression and returns the result.
     *
     * @param expression the expression to evaluate
     * @return the result as a NodeConstant
     * @throws MathEngineException if parsing or evaluation fails
     */
    public NodeConstant evaluate(String expression) {
        return evaluator.evaluate(parse(expression));
    }

    private Node parse(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression cannot be null or empty");
        }
        List<Token> tokens = lexer.tokenize(expression);
        return new Parser(tokens, expression,
                config.maxExpressionDepth(), config.forceDoubleArithmetic()).parse();
    }

    /**
     * Evaluates an expression and returns the result as a double.
     * Convenience method for numeric results.
     *
     * @param expression the expression to evaluate
     * @return the result as a double
     * @throws MathEngineException if parsing or evaluation fails
     */
    public double evaluateDouble(String expression) {
        return evaluate(expression).doubleValue();
    }

    // ==================== Formatting ====================

    /**
     * Formats a result according to the engine's decimalPlaces setting.
     * (-1 = full precision, 0 = integer, n = n decimal places)
     *
     * @param result the result to format
     * @return the formatted string representation
     */
    public String format(NodeConstant result) {
        return ResultFormatter.format(result, config);
    }

    /**
     * Evaluates an expression and returns the formatted result string.
     *
     * @param expression the expression to evaluate
     * @return the formatted result string
     * @throws MathEngineException if parsing or evaluation fails
     */
    public String evaluateAndFormat(String expression) {
        return format(evaluate(expression));
    }

    // ==================== Variable/Function Definition ====================

    /**
     * Defines a variable in the current session.
     */
    public void defineVariable(String name, double value) {
        evaluate(name + " := " + value);
    }

    /**
     * Defines a variable in the current session.
     */
    public void defineVariable(String name, NodeConstant value) {
        context.define(name, value);
    }

    /**
     * Defines a function in the current session (e.g., "f(x) := x^2").
     */
    public void defineFunction(String definition) {
        evaluate(definition);
    }

    /**
     * Registers a custom built-in function.
     * The function is registered for both execution and lexer recognition.
     *
     * @param function the function implementation
     */
    public void registerFunction(MathFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function cannot be null");
        }
        if (function.name() == null || function.name().isBlank()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        functionExecutor.register(function);
        functionRegistry.register(function.name());
        for (String alias : function.aliases()) {
            functionRegistry.register(alias);
        }
    }

    // ==================== Accessors ====================

    /**
     * Returns the current engine configuration.
     */
    public MathEngineConfig getConfig() {
        return config;
    }

    /**
     * Returns the evaluation context (for advanced use cases).
     */
    public EvaluationContext getContext() {
        return context;
    }

    /**
     * Returns the evaluator (for advanced use cases).
     */
    public Evaluator getEvaluator() {
        return evaluator;
    }

    // ==================== Query Methods ====================

    /**
     * Returns all registered function names (lowercase, including aliases).
     */
    public Set<String> getAllFunctionNames() {
        return functionExecutor.getFunctionNames();
    }

    /**
     * Returns all registered functions grouped by category.
     */
    public Map<MathFunction.Category, List<MathFunction>> getFunctionsByCategory() {
        Map<MathFunction.Category, List<MathFunction>> result = new EnumMap<>(MathFunction.Category.class);
        for (MathFunction.Category category : MathFunction.Category.values()) {
            List<MathFunction> funcs = functionExecutor.getFunctionsByCategory(category);
            if (!funcs.isEmpty()) {
                result.put(category, funcs);
            }
        }
        return result;
    }

    /**
     * Returns all constant definitions.
     */
    public List<ConstantDefinition> getAllConstants() {
        return constantRegistry.getDefinitions();
    }

    /**
     * Returns all registered units.
     */
    public Collection<UnitDefinition> getAllUnits() {
        return unitRegistry.getAllUnits();
    }

    /**
     * Tokenizes an expression (for debugging/display).
     */
    public List<Token> tokenize(String expression) {
        return lexer.tokenize(expression);
    }

    // ==================== Builder ====================

    /**
     * Builder for constructing customized {@link MathEngine} instances.
     */
    public static final class Builder {
        private MathEngineConfig config = MathEngineConfig.defaults();
        private FunctionRegistry functionRegistry;

        private Builder() {
        }

        /**
         * Sets the configuration to use.
         */
        public Builder config(MathEngineConfig config) {
            if (config == null) throw new IllegalArgumentException("Config cannot be null");
            this.config = config;
            return this;
        }

        /**
         * Sets the angle unit (convenience method that updates the config).
         */
        public Builder angleUnit(AngleUnit angleUnit) {
            this.config = config.toBuilder().angleUnit(angleUnit).build();
            return this;
        }

        /**
         * Sets a custom function registry (overrides auto-generated from config functions).
         */
        public Builder functionRegistry(FunctionRegistry functionRegistry) {
            this.functionRegistry = functionRegistry;
            return this;
        }

        /**
         * Builds the MathEngine instance.
         */
        public MathEngine build() {
            return new MathEngine(this);
        }
    }
}
