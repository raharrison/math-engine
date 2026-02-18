package uk.co.ryanharrison.mathengine.core;

import uk.co.ryanharrison.mathengine.parser.CompiledExpression;
import uk.co.ryanharrison.mathengine.parser.MathEngine;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;

/**
 * Class representing a function of one variable that can be evaluated.
 * <p>
 * This class uses a {@link CompiledExpression} internally for efficient repeated
 * evaluation. The expression is parsed once on first use and then evaluated many
 * times with different variable values.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Create a function
 * Function f = new Function("x^2 + 2*x + 1");
 *
 * // Evaluate at multiple points
 * double y1 = f.evaluateAt(0);   // 1.0
 * double y2 = f.evaluateAt(1);   // 4.0
 * double y3 = f.evaluateAt(2);   // 9.0
 *
 * // With custom variable name
 * Function g = new Function("t^2", "t");
 * double result = g.evaluateAt(5);  // 25.0
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class Function {

    private final String equation;
    private final String variable;
    private final AngleUnit angleUnit;

    // Lazily initialized engine and compiled expression for efficient evaluation
    private MathEngine engine;
    private CompiledExpression compiledExpression;

    /**
     * Construct a new function with the specified equation.
     * Uses "x" as the variable and radians for angle measurements.
     *
     * @param equation the equation
     */
    public Function(String equation) {
        this(equation, "x", AngleUnit.RADIANS);
    }

    /**
     * Construct a new function with the specified equation and angle unit.
     * Uses "x" as the variable.
     *
     * @param equation  the equation
     * @param angleUnit the angle unit for trigonometric functions
     */
    public Function(String equation, AngleUnit angleUnit) {
        this(equation, "x", angleUnit);
    }

    /**
     * Construct a new function with specified equation and variable.
     * Uses radians for angle measurements.
     *
     * @param equation the equation
     * @param variable the variable name (e.g., "x", "t")
     */
    public Function(String equation, String variable) {
        this(equation, variable, AngleUnit.RADIANS);
    }

    /**
     * Construct a new function with specified equation, variable and angle unit.
     *
     * @param equation  the equation
     * @param variable  the variable name
     * @param angleUnit the angle unit for trigonometric functions
     */
    public Function(String equation, String variable, AngleUnit angleUnit) {
        this.equation = equation;
        this.variable = variable;
        this.angleUnit = angleUnit;
    }

    /**
     * Initializes the engine and compiles the expression.
     * This is called lazily on first evaluation.
     */
    private void initEngine() {
        MathEngineConfig config = MathEngineConfig.builder()
                .vectorsEnabled(false)
                .matricesEnabled(false)
                .comprehensionsEnabled(false)
                .lambdasEnabled(false)
                .unitsEnabled(false)
                .userDefinedVariablesEnabled(false)
                .userDefinedFunctionsEnabled(false)
                .forceDoubleArithmetic(true)
                .angleUnit(this.angleUnit)
                .silentValidation(true)
                .build();
        this.engine = MathEngine.create(config);
        this.compiledExpression = engine.compile(equation);
    }

    /**
     * Evaluate the function at a specified point.
     * <p>
     * The variable of this function will be set to the given value during evaluation.
     *
     * @param at the point to evaluate at
     * @return the function evaluated at the specified point
     */
    public double evaluateAt(double at) {
        if (compiledExpression == null) {
            initEngine();
        }
        return compiledExpression.evaluateDouble(variable, at);
    }

    /**
     * Evaluate the function at a specified point given as a string expression.
     * <p>
     * The string can itself be an expression that will be evaluated first.
     *
     * @param at the point to evaluate at (can be an expression)
     * @return the function evaluated at the specified point
     */
    public double evaluateAt(String at) {
        if (compiledExpression == null) {
            initEngine();
        }
        // First evaluate the 'at' expression to get the numeric value
        double value = engine.evaluateDouble(at);
        return compiledExpression.evaluateDouble(variable, value);
    }

    /**
     * Get the equation of this function.
     *
     * @return this function's equation
     */
    public String getEquation() {
        return this.equation;
    }

    /**
     * Get the variable of this function.
     *
     * @return this function's variable name
     */
    public String getVariable() {
        return this.variable;
    }

    /**
     * Get this function compiled into a Node expression tree.
     *
     * @return an expression tree of nodes representing this function
     */
    public Node getCompiledExpression() {
        if (compiledExpression == null) {
            initEngine();
        }
        return compiledExpression.getAst();
    }

    /**
     * Convert this function into a String representation of the form
     * f(variable) = equation.
     */
    @Override
    public String toString() {
        return String.format("f(%s) = %s", this.variable, this.equation);
    }
}
