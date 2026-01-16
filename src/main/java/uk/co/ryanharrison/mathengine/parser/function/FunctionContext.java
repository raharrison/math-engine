package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.ArityException;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Provides context and utility methods for function implementations.
 * <p>
 * This class handles:
 * <ul>
 *     <li>Argument validation and arity checking</li>
 *     <li>Type coercion and conversion</li>
 *     <li>Angle unit conversions for trigonometric functions</li>
 *     <li>Vector broadcasting for single-argument functions</li>
 * </ul>
 *
 * <h2>Usage in Functions:</h2>
 * <pre>{@code
 * public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
 *     ctx.requireArity(args, 1, "sin");
 *     double angle = ctx.toRadians(args.get(0).doubleValue());
 *     return new NodeDouble(Math.sin(angle));
 * }
 * }</pre>
 */
public final class FunctionContext {

    private final EvaluationContext evaluationContext;
    private final FunctionCaller functionCaller;

    /**
     * Creates a function context with function calling capability.
     *
     * @param evaluationContext the evaluation context
     * @param functionCaller    callback for calling user functions/lambdas
     */
    public FunctionContext(EvaluationContext evaluationContext, FunctionCaller functionCaller) {
        this.evaluationContext = evaluationContext;
        this.functionCaller = functionCaller;
    }

    /**
     * Gets the underlying evaluation context.
     */
    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
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

    // ==================== Arity Validation ====================

    /**
     * Requires exactly the specified number of arguments.
     *
     * @param args         the arguments list
     * @param expected     the expected count
     * @param functionName the function name for error messages
     * @throws ArityException if count doesn't match
     */
    public void requireArity(List<NodeConstant> args, int expected, String functionName) {
        if (args.size() != expected) {
            throw new ArityException("Function '" + functionName + "' expects " +
                    expected + " argument(s), got " + args.size());
        }
    }

    /**
     * Requires at least the specified number of arguments.
     *
     * @param args         the arguments list
     * @param minimum      the minimum count
     * @param functionName the function name for error messages
     * @throws ArityException if count is less than minimum
     */
    public void requireMinArity(List<NodeConstant> args, int minimum, String functionName) {
        if (args.size() < minimum) {
            throw new ArityException("Function '" + functionName + "' requires at least " +
                    minimum + " argument(s), got " + args.size());
        }
    }

    /**
     * Requires arguments within a range.
     *
     * @param args         the arguments list
     * @param min          the minimum count
     * @param max          the maximum count
     * @param functionName the function name for error messages
     * @throws ArityException if count is outside range
     */
    public void requireArityRange(List<NodeConstant> args, int min, int max, String functionName) {
        if (args.size() < min || args.size() > max) {
            throw new ArityException("Function '" + functionName + "' expects " +
                    min + " to " + max + " argument(s), got " + args.size());
        }
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
     * @param value        the value to check
     * @param functionName the function name for error messages
     * @return the value as a NodeVector
     * @throws TypeError if the value is not a vector
     */
    public NodeVector requireVector(NodeConstant value, String functionName) {
        if (value instanceof NodeVector vector) {
            return vector;
        }
        throw new TypeError("Function '" + functionName + "' requires a vector, got " +
                value.getClass().getSimpleName());
    }

    /**
     * Requires the argument to be a matrix.
     *
     * @param value        the value to check
     * @param functionName the function name for error messages
     * @return the value as a NodeMatrix
     * @throws TypeError if the value is not a matrix
     */
    public NodeMatrix requireMatrix(NodeConstant value, String functionName) {
        if (value instanceof NodeMatrix matrix) {
            return matrix;
        }
        throw new TypeError("Function '" + functionName + "' requires a matrix, got " +
                value.getClass().getSimpleName());
    }

    /**
     * Requires the matrix to be square.
     *
     * @param matrix       the matrix to check
     * @param functionName the function name for error messages
     * @throws TypeError if the matrix is not square
     */
    public void requireSquareMatrix(NodeMatrix matrix, String functionName) {
        if (matrix.getRows() != matrix.getCols()) {
            throw new IllegalArgumentException("Function '" + functionName + "' requires a square matrix, got " +
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
     * Applies a unary function to a value, broadcasting over vectors.
     *
     * @param value    the value (scalar or vector)
     * @param doubleOp the operation for double values
     * @return the result
     */
    public NodeConstant applyWithBroadcasting(NodeConstant value, DoubleUnaryOperator doubleOp) {
        if (value instanceof NodeVector vector) {
            Node[] elements = vector.getElements();
            Node[] result = new Node[elements.length];
            for (int i = 0; i < elements.length; i++) {
                NodeConstant elem = (NodeConstant) elements[i];
                result[i] = applyWithBroadcasting(elem, doubleOp);
            }
            return new NodeVector(result);
        }

        double val = toNumber(value).doubleValue();
        return new NodeDouble(doubleOp.applyAsDouble(val));
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
        if (value instanceof NodeVector vector) {
            Node[] elements = vector.getElements();
            Node[] result = new Node[elements.length];
            for (int i = 0; i < elements.length; i++) {
                NodeConstant elem = (NodeConstant) elements[i];
                result[i] = applyWithTypePreservation(elem, rationalOp, doubleOp);
            }
            return new NodeVector(result);
        }

        // Delegate to NumericOperations for consistent type preservation
        return NumericOperations.applyUnary(value, rationalOp, doubleOp);
    }

    // ==================== Domain Validation ====================

    /**
     * Ensures a value is positive, throwing IllegalArgumentException if not.
     *
     * @param value        the value to check
     * @param functionName the function name for error messages
     */
    public void requirePositive(double value, String functionName) {
        if (value <= 0) {
            throw new IllegalArgumentException(functionName + " requires positive value, got: " + value);
        }
    }

    /**
     * Ensures a value is non-negative, throwing IllegalArgumentException if not.
     *
     * @param value        the value to check
     * @param functionName the function name for error messages
     */
    public void requireNonNegative(double value, String functionName) {
        if (value < 0) {
            throw new IllegalArgumentException(functionName + " requires non-negative value, got: " + value);
        }
    }

    /**
     * Ensures a value is within a range, throwing IllegalArgumentException if not.
     *
     * @param value        the value to check
     * @param min          the minimum allowed value (inclusive)
     * @param max          the maximum allowed value (inclusive)
     * @param functionName the function name for error messages
     */
    public void requireInRange(double value, double min, double max, String functionName) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(functionName + " requires value in range [" +
                    min + ", " + max + "], got: " + value);
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
     * @param value        the value to check
     * @param functionName the function name for error messages
     * @return the value as a NodeString
     * @throws TypeError if the value is not a string
     */
    public NodeString requireString(NodeConstant value, String functionName) {
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
     * Creates a NodePercent from a decimal value.
     *
     * @param decimal the decimal value (0.5 for 50%)
     * @return a NodePercent
     */
    public NodePercent asPercent(double decimal) {
        return NodePercent.fromDecimal(decimal);
    }

    // ==================== Integer Operations ====================

    /**
     * Converts a value to a long integer.
     *
     * @param value the value to convert
     * @return the long value (truncated)
     */
    public long toLong(NodeConstant value) {
        return (long) toNumber(value).doubleValue();
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
     * @param value        the value to convert
     * @param functionName the name of the calling function for error messages
     * @return the integer value
     * @throws TypeError if the value is not an integer
     */
    public int requireInteger(NodeConstant value, String functionName) {
        double d = toNumber(value).doubleValue();
        if (d != Math.floor(d)) {
            throw new TypeError(functionName + " requires integer arguments, got: " + d);
        }
        return (int) d;
    }

    /**
     * Converts a value to a long integer, validating it has no fractional part.
     *
     * @param value        the value to convert
     * @param functionName the name of the calling function for error messages
     * @return the long integer value
     * @throws TypeError if the value is not an integer
     */
    public long requireLong(NodeConstant value, String functionName) {
        double d = toNumber(value).doubleValue();
        if (d != Math.floor(d)) {
            throw new TypeError(functionName + " requires integer arguments, got: " + d);
        }
        return (long) d;
    }

    // ==================== Collection Operations ====================

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
    public uk.co.ryanharrison.mathengine.linearalgebra.Matrix toMatrix(NodeMatrix matrix) {
        double[][] data = new double[matrix.getRows()][matrix.getCols()];
        Node[][] elements = matrix.getElements();
        for (int i = 0; i < matrix.getRows(); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                data[i][j] = ((NodeConstant) elements[i][j]).doubleValue();
            }
        }
        return uk.co.ryanharrison.mathengine.linearalgebra.Matrix.of(data);
    }

    /**
     * Converts a linearalgebra Matrix to a NodeMatrix.
     *
     * @param matrix the linearalgebra Matrix
     * @return the NodeMatrix
     */
    public NodeMatrix fromMatrix(uk.co.ryanharrison.mathengine.linearalgebra.Matrix matrix) {
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

    // ==================== Type Checking ====================

    /**
     * Checks if a value is a string.
     */
    public boolean isString(NodeConstant value) {
        return value instanceof NodeString;
    }

    /**
     * Checks if a value is numeric (number or boolean).
     */
    public boolean isNumeric(NodeConstant value) {
        return value instanceof NodeNumber;
    }

    /**
     * Checks if a value is a collection (vector or matrix).
     */
    public boolean isCollection(NodeConstant value) {
        return value instanceof NodeVector || value instanceof NodeMatrix;
    }
}
