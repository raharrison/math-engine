package uk.co.ryanharrison.mathengine.parser;

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
import uk.co.ryanharrison.mathengine.parser.registry.ConstantDefinition;
import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.*;

/**
 * Main entry point for the Math Engine parser and evaluator.
 * <p>
 * MathEngine is an immutable, thread-safe facade for the parser system.
 * All configuration is provided via {@link MathEngineConfig}.
 *
 * <h2>Quick Start:</h2>
 * <pre>{@code
 * // Simple evaluation with defaults
 * MathEngine engine = MathEngine.create();
 * NodeConstant result = engine.evaluate("2 + 3 * 4");
 * System.out.println(result); // 14
 *
 * // With custom configuration
 * MathEngineConfig config = MathEngineConfig.builder()
 *     .angleUnit(AngleUnit.DEGREES)
 *     .build();
 * MathEngine engine = MathEngine.create(config);
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
 * <h2>Custom Built-in Functions:</h2>
 * <pre>{@code
 * // Option 1: Configure in MathEngineConfig (preferred)
 * var functions = new ArrayList<>(StandardFunctions.all());
 * functions.add(myCustomFunction);
 * MathEngineConfig config = MathEngineConfig.builder()
 *     .functions(functions)
 *     .build();
 * MathEngine engine = MathEngine.create(config);
 *
 * // Option 2: Use withFunction (creates new engine)
 * MathEngine engine = MathEngine.create();
 * engine = engine.withFunction(myCustomFunction);
 * }</pre>
 */
public final class MathEngine {

    private final MathEngineConfig config;
    private final FunctionExecutor functionExecutor;
    private final UnitRegistry unitRegistry;
    private final ConstantRegistry constantRegistry;
    private final EvaluationContext context;
    private final Evaluator evaluator;
    private final Lexer lexer;

    private MathEngine(MathEngineConfig config) {
        this.config = config;

        // Create immutable executors
        var operatorExecutor = OperatorExecutor.of(config.binaryOperators(), config.unaryOperators());
        this.functionExecutor = FunctionExecutor.of(config.functions());

        // Immutable registries
        this.constantRegistry = config.constantRegistry();
        this.unitRegistry = config.unitRegistry();

        // Evaluation context (mutable for user-defined variables/functions)
        this.context = new EvaluationContext(config, new RecursionTracker(config.maxRecursionDepth()));
        initializePredefinedConstants();

        // Core components
        this.evaluator = new Evaluator(config, context, operatorExecutor, functionExecutor);
        this.lexer = new Lexer(
                functionExecutor.getFunctionNames(),
                unitRegistry,
                constantRegistry,
                config.keywordRegistry(),
                config.maxIdentifierLength(),
                config.implicitMultiplication()
        );
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
        return new MathEngine(MathEngineConfig.defaults());
    }

    /**
     * Creates a new MathEngine with the specified configuration.
     *
     * @param config the configuration to use
     * @return new MathEngine instance
     */
    public static MathEngine create(MathEngineConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        return new MathEngine(config);
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
        return new MathEngine(MathEngineConfig.arithmetic());
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
        return new MathEngine(MathEngineConfig.basic());
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

    /**
     * Creates a new engine with an additional custom built-in function.
     * <p>
     * This method returns a new MathEngine instance with the function added.
     * The original engine remains unchanged. Session state (variables, user functions)
     * is preserved in the new engine.
     *
     * <h2>Usage:</h2>
     * <pre>{@code
     * MathEngine engine = MathEngine.create();
     * engine = engine.withFunction(myCustomFunction);
     * engine.evaluate("myFunction(21)");
     * }</pre>
     *
     * <p>For defining functions at configuration time, prefer using
     * {@link MathEngineConfig.Builder#functions(List)}.
     *
     * @param function the function implementation
     * @return new engine with the function added
     */
    public MathEngine withFunction(MathFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function cannot be null");
        }
        if (function.name() == null || function.name().isBlank()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }

        // Create new config with additional function
        var newFunctions = new ArrayList<>(config.functions());
        newFunctions.add(function);
        MathEngineConfig newConfig = config.toBuilder().functions(newFunctions).build();

        // Create new engine and transfer session state
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
        var result = new EnumMap<MathFunction.Category, List<MathFunction>>(MathFunction.Category.class);
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
}
