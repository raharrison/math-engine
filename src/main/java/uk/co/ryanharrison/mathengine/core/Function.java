package uk.co.ryanharrison.mathengine.core;

import uk.co.ryanharrison.mathengine.parser.CompiledExpression;
import uk.co.ryanharrison.mathengine.parser.MathEngine;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.ast.Node;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

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
 *
 * // Backed by code rather than an equation, for the numerical methods
 * Function h = Function.of(x -> x * x, "x^2");
 * double area = SimpsonIntegrator.of(h, 0, 1).integrate();  // 1/3
 * }</pre>
 * <p>
 * A function made by {@link #of} has no expression tree, so it suits the numerical
 * methods, which only evaluate it, and not the symbolic ones.
 *
 * @author Ryan Harrison
 */
public final class Function {

    private final String equation;
    private final String variable;
    private final AngleUnit angleUnit;

    /**
     * The evaluation, when this function is backed by code rather than an equation.
     */
    private final DoubleUnaryOperator evaluator;

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
        this(equation, variable, angleUnit, null);
    }

    private Function(String equation, String variable, AngleUnit angleUnit, DoubleUnaryOperator evaluator) {
        this.equation = equation;
        this.variable = variable;
        this.angleUnit = angleUnit;
        this.evaluator = evaluator;
    }

    /**
     * Creates a function evaluated by code, so the numerical methods can integrate,
     * differentiate or solve something that is not written as an equation, such as a
     * lambda from the expression parser.
     *
     * @param evaluator   computes the function at a point
     * @param description how the function reads, used by {@link #getEquation()} and
     *                    {@link #toString()}
     * @return a function that evaluates through {@code evaluator}
     * @throws NullPointerException if either argument is null
     */
    public static Function of(DoubleUnaryOperator evaluator, String description) {
        Objects.requireNonNull(evaluator, "Evaluator cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        return new Function(description, "x", AngleUnit.RADIANS, evaluator);
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
        if (evaluator != null) {
            return evaluator.applyAsDouble(at);
        }
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
        if (evaluator != null) {
            return evaluator.applyAsDouble(MathEngine.create().evaluateDouble(at));
        }
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
     * @throws UnsupportedOperationException if this function was made by {@link #of}, and
     *                                       so has no equation to compile
     */
    public Node getCompiledExpression() {
        if (evaluator != null) {
            throw new UnsupportedOperationException(
                    "Function '" + equation + "' is evaluated by code and has no expression tree");
        }
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
