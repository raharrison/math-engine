package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.linearalgebra.Matrix;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Provides context and utility methods for function implementations.
 * <p>
 * Every {@code FunctionContext} knows the name of the function it is serving,
 * so validation helpers and error methods automatically include it in messages.
 * <p>
 * This class handles:
 * <ul>
 *     <li>Central error reporting via {@link #error(String)}</li>
 *     <li>Argument validation and arity checking</li>
 *     <li>Domain validation (positive, non-negative, range, etc.)</li>
 *     <li>Type coercion and conversion</li>
 *     <li>Angle unit conversions for trigonometric functions</li>
 *     <li>Vector broadcasting for single-argument functions</li>
 * </ul>
 *
 * <h2>Usage in Functions:</h2>
 * <pre>{@code
 * // ctx already knows the function name
 * public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
 *     double value = ctx.toNumber(args.get(0)).doubleValue();
 *     ctx.requirePositive(value);
 *     return new NodeDouble(Math.log(value));
 * }
 * }</pre>
 */
public final class FunctionContext {

    private final String functionName;
    private final EvaluationContext evaluationContext;
    private final FunctionCaller functionCaller;

    /**
     * Creates a function context with function calling capability.
     *
     * @param functionName      the name of the function being executed
     * @param evaluationContext the evaluation context
     * @param functionCaller    callback for calling user functions/lambdas
     */
    public FunctionContext(String functionName, EvaluationContext evaluationContext, FunctionCaller functionCaller) {
        this.functionName = functionName;
        this.evaluationContext = evaluationContext;
        this.functionCaller = functionCaller;
    }

    /**
     * Gets the name of the function this context is serving.
     */
    public String functionName() {
        return functionName;
    }

    /**
     * Calls a user-defined function or lambda with the given arguments.
     *
     * @param function the function to call
     * @param args     the arguments to pass
     * @return the result of calling the function
     * @throws IllegalStateException if function calling is not supported in this context
     */
    public NodeConstant callFunction(NodeFunction function, List<NodeConstant> args) {
        if (functionCaller == null) {
            throw new IllegalStateException("Function calling is not supported in this context");
        }
        return functionCaller.call(function, args, evaluationContext);
    }

    /**
     * Gets the current angle unit setting.
     */
    public AngleUnit getAngleUnit() {
        return evaluationContext.getAngleUnit();
    }

    /**
     * Gets the silent validation mode setting.
     * <p>
     * When silent validation is enabled, validation failures return NaN
     * instead of throwing exceptions.
     *
     * @return true if silent validation is enabled
     */
    public boolean isSilentValidation() {
        return evaluationContext.getConfig().silentValidation();
    }

    // ==================== Error Reporting ====================

    /**
     * Creates an {@link IllegalArgumentException} with the function name prepended.
     * <p>
     * This is the central error-reporting method. All function errors should go through here
     * to ensure consistent formatting.
     *
     * <h3>Usage:</h3>
     * <pre>{@code
     * if (value <= 0) {
     *     throw ctx.error("requires positive value, got: " + value);
     * }
     * }</pre>
     *
     * @param message the error message (function name is prepended automatically)
     * @return the exception (caller must throw it)
     */
    public IllegalArgumentException error(String message) {
        return new IllegalArgumentException(functionName + ": " + message);
    }

    // ==================== Domain Validation ====================

    /**
     * Requires the value to be strictly positive ({@code > 0}).
     * <p>
     * In silent validation mode, returns NaN for invalid values instead of throwing.
     *
     * @param value the value to check
     * @return the value if valid, NaN if invalid and silent mode enabled
     * @throws IllegalArgumentException if the value is not positive and silent mode disabled
     */
    public double requirePositive(double value) {
        if (value <= 0) {
            if (isSilentValidation()) {
                return Double.NaN;
            }
            throw error("requires positive value, got: " + value);
        }
        return value;
    }

    /**
     * Requires the value to be non-negative ({@code >= 0}).
     * <p>
     * In silent validation mode, returns NaN for invalid values instead of throwing.
     *
     * @param value the value to check
     * @return the value if valid, NaN if invalid and silent mode enabled
     * @throws IllegalArgumentException if the value is negative and silent mode disabled
     */
    public double requireNonNegative(double value) {
        if (value < 0) {
            if (isSilentValidation()) {
                return Double.NaN;
            }
            throw error("requires non-negative value, got: " + value);
        }
        return value;
    }

    /**
     * Requires the value to be non-zero.
     * <p>
     * In silent validation mode, returns NaN for invalid values instead of throwing.
     *
     * @param value the value to check
     * @return the value if valid, NaN if invalid and silent mode enabled
     * @throws IllegalArgumentException if the value is zero and silent mode disabled
     */
    public double requireNonZero(double value) {
        if (value == 0) {
            if (isSilentValidation()) {
                return Double.NaN;
            }
            throw error("requires non-zero value");
        }
        return value;
    }

    /**
     * Requires the value to be within a specified range (inclusive).
     * <p>
     * In silent validation mode, returns NaN for invalid values instead of throwing.
     *
     * @param value the value to check
     * @param min   the minimum allowed value (inclusive)
     * @param max   the maximum allowed value (inclusive)
     * @return the value if valid, NaN if invalid and silent mode enabled
     * @throws IllegalArgumentException if the value is outside the range and silent mode disabled
     */
    public double requireInRange(double value, double min, double max) {
        if (value < min || value > max) {
            if (isSilentValidation()) {
                return Double.NaN;
            }
            throw error("requires value in range [" + min + ", " + max + "], got: " + value);
        }
        return value;
    }

    // ==================== Type Coercion ====================

    /**
     * Converts a constant to a NodeNumber, handling boolean and unit coercion.
     * <p>
     * For NodeUnit, extracts the numeric value (strips the unit).
     * Use this for functions where units don't apply (sqrt, trig functions, etc.).
     * Delegates to {@link TypeCoercion#toNumber(NodeConstant)}.
     *
     * @param value the value to convert
     * @return the value as a NodeNumber
     * @throws TypeError if the value cannot be converted to a number
     */
    public NodeNumber toNumber(NodeConstant value) {
        return TypeCoercion.toNumber(value);
    }

    /**
     * Converts a constant to a boolean value.
     * Numbers are truthy if non-zero.
     * Delegates to {@link TypeCoercion#toBoolean(NodeConstant)}.
     *
     * @param value the value to convert
     * @return the boolean value
     * @throws TypeError if the value cannot be converted to a boolean
     */
    public boolean toBoolean(NodeConstant value) {
        return TypeCoercion.toBoolean(value);
    }

    /**
     * Requires the argument to be a vector.
     *
     * @param value the value to check
     * @return the value as a NodeVector
     * @throws TypeError if the value is not a vector
     */
    public NodeVector requireVector(NodeConstant value) {
        if (value instanceof NodeVector vector) {
            return vector;
        }
        throw new TypeError("Function '" + functionName + "' requires a vector, got " +
                value.getClass().getSimpleName());
    }

    /**
     * Requires the argument to be a matrix.
     *
     * @param value the value to check
     * @return the value as a NodeMatrix
     * @throws TypeError if the value is not a matrix
     */
    public NodeMatrix requireMatrix(NodeConstant value) {
        if (value instanceof NodeMatrix matrix) {
            return matrix;
        }
        throw new TypeError("Function '" + functionName + "' requires a matrix, got " +
                value.getClass().getSimpleName());
    }

    /**
     * Requires the matrix to be square.
     *
     * @param matrix the matrix to check
     * @throws IllegalArgumentException if the matrix is not square
     */
    public void requireSquareMatrix(NodeMatrix matrix) {
        if (matrix.getRows() != matrix.getCols()) {
            throw error("requires a square matrix, got " +
                    matrix.getRows() + "x" + matrix.getCols());
        }
    }

    // ==================== Angle Conversion ====================

    /**
     * Converts an angle to radians based on the current angle unit setting.
     *
     * @param angle the angle in the current unit
     * @return the angle in radians
     */
    public double toRadians(double angle) {
        return getAngleUnit() == AngleUnit.DEGREES
                ? Math.toRadians(angle)
                : angle;
    }

    /**
     * Converts an angle from radians based on the current angle unit setting.
     *
     * @param radians the angle in radians
     * @return the angle in the current unit
     */
    public double fromRadians(double radians) {
        return getAngleUnit() == AngleUnit.DEGREES
                ? Math.toDegrees(radians)
                : radians;
    }

    // ==================== Vector Broadcasting ====================

    /**
     * Applies a unary function to a value, automatically broadcasting over vectors.
     * <p>
     * This is the primary broadcasting mechanism for unary functions. When the value
     * is a vector, the function is recursively applied to each element.
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * // Scalar input
     * applyWithBroadcasting(NodeDouble(4.0), Math::sqrt)
     *   -> NodeDouble(2.0)
     *
     * // Vector input (broadcasts)
     * applyWithBroadcasting(NodeVector([1,4,9]), Math::sqrt)
     *   -> NodeVector([1.0, 2.0, 3.0])
     *
     * // With inline validation
     * applyWithBroadcasting(arg, value -> {
     *     ctx.requirePositive(value);
     *     return Math.log(value);
     * })
     * }</pre>
     *
     * @param value    the value (scalar or vector)
     * @param doubleOp the operation for double values
     * @return the result (scalar or vector, matching input structure)
     */
    public NodeConstant applyWithBroadcasting(NodeConstant value, DoubleUnaryOperator doubleOp) {
        return BroadcastingEngine.applyUnary(value, v -> {
            double val = toNumber(v).doubleValue();
            return new NodeDouble(doubleOp.applyAsDouble(val));
        });
    }

    /**
     * Applies a unary function with type preservation, broadcasting over vectors.
     * <p>
     * Preserves all first-class types (units, percents, rationals) by delegating
     * to {@link NumericOperations#applyUnary}.
     *
     * @param value      the value
     * @param rationalOp the operation for rational values
     * @param doubleOp   the operation for double values
     * @return the result with preserved type where applicable
     */
    public NodeConstant applyWithTypePreservation(NodeConstant value,
                                                  UnaryOperator<BigRational> rationalOp,
                                                  DoubleUnaryOperator doubleOp) {
        return BroadcastingEngine.applyUnary(value, v ->
                NumericOperations.applyUnary(v, rationalOp, doubleOp));
    }

    // ==================== Array Validation ====================

    /**
     * Ensures an array is non-empty.
     *
     * @param values the array to check
     * @throws TypeError if the array is empty
     */
    public void requireNonEmpty(double[] values) {
        if (values.length == 0) {
            throw new TypeError(functionName + " requires at least one element");
        }
    }

    /**
     * Ensures an array has at least the minimum size.
     *
     * @param values the array to check
     * @param min    the minimum size
     * @throws TypeError if the array is smaller than minimum
     */
    public void requireMinSize(double[] values, int min) {
        if (values.length < min) {
            throw new TypeError(functionName + " requires at least " + min + " element(s), got " + values.length);
        }
    }

    // ==================== Numeric Operations ====================

    /**
     * Applies an additive binary operation (+ or -) with full unit support.
     * Uses shared NumericOperations utility for consistent unit handling.
     *
     * @param left       the left operand
     * @param right      the right operand
     * @param rationalOp the operation for rational operands
     * @param doubleOp   the operation for double operands
     * @return the result, preserving units if applicable
     */
    public NodeConstant applyNumericBinary(NodeConstant left, NodeConstant right,
                                           java.util.function.BinaryOperator<BigRational> rationalOp,
                                           java.util.function.DoubleBinaryOperator doubleOp) {
        return NumericOperations.applyAdditive(left, right, doubleOp, rationalOp);
    }

    /**
     * Applies a multiplicative binary operation (* or /) with full unit support.
     *
     * @param left       the left operand
     * @param right      the right operand
     * @param rationalOp the operation for rational operands
     * @param doubleOp   the operation for double operands
     * @param isMultiply true for multiplication, false for division
     * @return the result, preserving units if applicable
     */
    public NodeConstant applyMultiplicativeBinary(NodeConstant left, NodeConstant right,
                                                  java.util.function.BinaryOperator<BigRational> rationalOp,
                                                  java.util.function.DoubleBinaryOperator doubleOp,
                                                  boolean isMultiply) {
        return NumericOperations.applyMultiplicative(left, right, doubleOp, rationalOp, isMultiply);
    }

    // ==================== String Operations ====================

    /**
     * Converts a constant to a string value.
     *
     * @param value the value to convert
     * @return the string representation
     */
    public String toStringValue(NodeConstant value) {
        if (value instanceof NodeString str) {
            return str.getValue();
        }
        if (value instanceof NodeBoolean bool) {
            return bool.getValue() ? "true" : "false";
        }
        if (value instanceof NodeNumber num) {
            double d = num.doubleValue();
            if (d == Math.floor(d) && Double.isFinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        // For vectors/matrices, use toString representation
        return value.toString();
    }

    /**
     * Requires the argument to be a string.
     *
     * @param value the value to check
     * @return the value as a NodeString
     * @throws TypeError if the value is not a string
     */
    public NodeString requireString(NodeConstant value) {
        if (value instanceof NodeString str) {
            return str;
        }
        throw new TypeError("Function '" + functionName + "' requires a string, got " +
                value.getClass().getSimpleName());
    }

    /**
     * Gets a string value, converting if necessary.
     *
     * @param value the value
     * @return a NodeString (original or converted)
     */
    public NodeString asString(NodeConstant value) {
        if (value instanceof NodeString str) {
            return str;
        }
        return new NodeString(toStringValue(value));
    }

    // ==================== Percentage Operations ====================

    /**
     * Converts a constant to a percentage value (0.5 for 50%).
     *
     * @param value the value to convert
     * @return the decimal value of the percentage
     */
    public double toPercentDecimal(NodeConstant value) {
        if (value instanceof NodePercent pct) {
            return pct.getValue();
        }
        return toNumber(value).doubleValue();
    }

    /**
     * Converts a value to an int.
     *
     * @param value the value to convert
     * @return the int value (truncated)
     */
    public int toInt(NodeConstant value) {
        return (int) toNumber(value).doubleValue();
    }

    /**
     * Converts a value to an integer, validating it has no fractional part.
     *
     * @param value the value to convert
     * @return the integer value
     * @throws TypeError if the value is not an integer
     */
    public int requireInteger(NodeConstant value) {
        double d = toNumber(value).doubleValue();
        if (d != Math.floor(d)) {
            throw new TypeError(functionName + " requires integer arguments, got: " + d);
        }
        return (int) d;
    }

    /**
     * Converts a value to a long integer, validating it has no fractional part.
     *
     * @param value the value to convert
     * @return the long integer value
     * @throws TypeError if the value is not an integer
     */
    public long requireLong(NodeConstant value) {
        double d = toNumber(value).doubleValue();
        if (d != Math.floor(d)) {
            throw new TypeError(functionName + " requires integer arguments, got: " + d);
        }
        return (long) d;
    }

    // ==================== Collection Operations ====================

    /**
     * Flattens arguments to a list of elements, handling both scalars and vectors.
     * <p>
     * This is useful for aggregate functions that accept either form:
     * </p>
     * <ul>
     *     <li>{@code sum(1, 2, 3)} -> {@code [1, 2, 3]}</li>
     *     <li>{@code sum([1, 2, 3])} -> {@code [1, 2, 3]}</li>
     *     <li>{@code sum(1, [2, 3], 4)} -> {@code [1, 2, 3, 4]}</li>
     * </ul>
     *
     * @param args the arguments list (may contain scalars and/or vectors)
     * @return flattened list of all scalar elements
     */
    public List<NodeConstant> flattenArguments(List<NodeConstant> args) {
        return args.stream()
                .flatMap(arg -> {
                    if (arg instanceof NodeVector vector) {
                        return Arrays.stream(vector.getElements())
                                .map(n -> (NodeConstant) n);
                    }
                    return Stream.of(arg);
                })
                .toList();
    }

    /**
     * Converts a vector to a double array.
     *
     * @param vector the vector
     * @return array of double values
     */
    public double[] toDoubleArray(NodeVector vector) {
        double[] result = new double[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            result[i] = toNumber((NodeConstant) vector.getElement(i)).doubleValue();
        }
        return result;
    }

    /**
     * Flattens arguments to a double array.
     * Handles both scalars and vectors.
     *
     * @param args the arguments
     * @return flattened array of doubles
     */
    public double[] flattenToDoubles(List<NodeConstant> args) {
        java.util.List<Double> values = new java.util.ArrayList<>();
        for (NodeConstant arg : args) {
            if (arg instanceof NodeVector vector) {
                for (int i = 0; i < vector.size(); i++) {
                    values.add(toNumber((NodeConstant) vector.getElement(i)).doubleValue());
                }
            } else {
                values.add(toNumber(arg).doubleValue());
            }
        }
        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Converts a matrix to a linearalgebra Matrix.
     *
     * @param matrix the node matrix
     * @return the linearalgebra Matrix
     */
    public Matrix toMatrix(NodeMatrix matrix) {
        double[][] data = new double[matrix.getRows()][matrix.getCols()];
        Node[][] elements = matrix.getElements();
        for (int i = 0; i < matrix.getRows(); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                data[i][j] = ((NodeConstant) elements[i][j]).doubleValue();
            }
        }
        return Matrix.of(data);
    }

    /**
     * Converts a linearalgebra Matrix to a NodeMatrix.
     *
     * @param matrix the linearalgebra Matrix
     * @return the NodeMatrix
     */
    public NodeMatrix fromMatrix(Matrix matrix) {
        int rows = matrix.getRowCount();
        int cols = matrix.getColumnCount();
        Node[][] result = new Node[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = new NodeDouble(matrix.get(i, j));
            }
        }
        return new NodeMatrix(result);
    }
}
