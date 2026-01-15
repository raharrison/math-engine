package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Provides context and utility methods for operator implementations.
 * <p>
 * This class handles:
 * <ul>
 *     <li>Type coercion between number types</li>
 *     <li>Angle unit conversions</li>
 *     <li>Common numeric operations</li>
 *     <li>Validation utilities</li>
 * </ul>
 *
 * <h2>Usage in Operators:</h2>
 * <pre>{@code
 * public class PlusOperator implements BinaryOperator {
 *     public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
 *         // Use BroadcastingDispatcher for automatic broadcasting
 *         return BroadcastingDispatcher.dispatch(left, right, ctx, (l, r) -> {
 *             return ctx.applyNumericBinary(l, r, BigRational::add, Double::sum);
 *         });
 *     }
 * }
 * }</pre>
 *
 * @see BroadcastingDispatcher
 */
public final class OperatorContext {

    private final EvaluationContext evaluationContext;

    /**
     * Creates an operator context wrapping the given evaluation context.
     *
     * @param evaluationContext the evaluation context
     */
    public OperatorContext(EvaluationContext evaluationContext) {
        this.evaluationContext = evaluationContext;
    }

    /**
     * Gets the current angle unit setting.
     */
    public AngleUnit getAngleUnit() {
        return evaluationContext.getAngleUnit();
    }

    /**
     * Returns whether double arithmetic is forced (bypassing rational arithmetic).
     */
    public boolean forceDoubleArithmetic() {
        return evaluationContext.isForceDoubleArithmetic();
    }

    // ==================== Type Coercion ====================

    /**
     * Converts a constant to a NodeNumber, handling boolean and unit coercion.
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

    // ==================== Binary Operations ====================

    /**
     * Applies a binary operation with type promotion and type preservation.
     * <p>
     * Handles all numeric types with intelligent type preservation:
     * <ul>
     *     <li>NodeUnit + scalar → NodeUnit (preserves unit)</li>
     *     <li>NodeRational + NodeRational → NodeRational (preserves exactness)</li>
     *     <li>NodeDouble or NodePercent → NodeDouble (promotes to double)</li>
     *     <li>NodeBoolean → treated as 0/1</li>
     * </ul>
     *
     * @param left       the left operand
     * @param right      the right operand
     * @param rationalOp the operation for rational operands
     * @param doubleOp   the operation for double operands
     * @return the result
     */
    public NodeConstant applyNumericBinary(NodeConstant left, NodeConstant right,
                                           BinaryOperator<BigRational> rationalOp,
                                           DoubleBinaryOperator doubleOp) {
        // NodeUnit preservation: unit op scalar → unit, scalar op unit → unit
        // This handles power, modulo, and other generic operations on units
        if (left instanceof NodeUnit unit && !(right instanceof NodeUnit)) {
            double result = doubleOp.applyAsDouble(unit.getValue(), toNumber(right).doubleValue());
            return NodeUnit.of(result, unit.getUnit());
        }
        if (right instanceof NodeUnit unit && !(left instanceof NodeUnit)) {
            double result = doubleOp.applyAsDouble(toNumber(left).doubleValue(), unit.getValue());
            return NodeUnit.of(result, unit.getUnit());
        }

        // Convert to numbers for the rest of the logic
        NodeNumber leftNum = toNumber(left);
        NodeNumber rightNum = toNumber(right);

        // Type promotion: if either is double/percent, result is double
        if (leftNum instanceof NodeDouble || rightNum instanceof NodeDouble ||
                leftNum instanceof NodePercent || rightNum instanceof NodePercent) {
            double l = leftNum.doubleValue();
            double r = rightNum.doubleValue();
            return new NodeDouble(doubleOp.applyAsDouble(l, r));
        }

        // Both are rational - preserve exactness (unless forceDoubleArithmetic is set)
        if (!forceDoubleArithmetic() && leftNum instanceof NodeRational leftRat && rightNum instanceof NodeRational rightRat) {
            BigRational l = leftRat.getValue();
            BigRational r = rightRat.getValue();
            try {
                return new NodeRational(rationalOp.apply(l, r));
            } catch (ArithmeticException e) {
                // Fall back to double for operations that can't be represented as rational
                // (e.g., division by zero which should return infinity/NaN per IEEE 754)
                double ld = l.doubleValue();
                double rd = r.doubleValue();
                return new NodeDouble(doubleOp.applyAsDouble(ld, rd));
            }
        }

        // Fallback to double for remaining cases (rationals with forceDouble, booleans, etc.)
        double l = leftNum.doubleValue();
        double r = rightNum.doubleValue();
        return new NodeDouble(doubleOp.applyAsDouble(l, r));
    }

    /**
     * Applies a binary operation that always produces a double result.
     *
     * @param left     the left operand
     * @param right    the right operand
     * @param doubleOp the operation
     * @return the result as NodeDouble
     */
    public NodeConstant applyDoubleBinary(NodeConstant left, NodeConstant right,
                                          DoubleBinaryOperator doubleOp) {
        double l = toNumber(left).doubleValue();
        double r = toNumber(right).doubleValue();
        return new NodeDouble(doubleOp.applyAsDouble(l, r));
    }

    // ==================== Multiplicative Operations (* /) ====================

    /**
     * Applies multiplicative operations (* or /) with full type preservation.
     * <p>
     * Delegates to {@link NumericOperations#applyMultiplicative} for consistent
     * type-preserving behavior across all first-class types (units, percents, rationals).
     * <p>
     * Multiplication rules:
     * <ul>
     *     <li>unit * scalar → unit</li>
     *     <li>scalar * unit → unit</li>
     *     <li>percent * scalar → percent</li>
     *     <li>scalar * percent → percent</li>
     *     <li>percent * percent → percent</li>
     *     <li>rational * rational → rational</li>
     * </ul>
     * <p>
     * Division rules:
     * <ul>
     *     <li>unit / scalar → unit</li>
     *     <li>unit / unit (same type) → ratio (number)</li>
     *     <li>percent / scalar → percent</li>
     *     <li>percent / percent → ratio (number)</li>
     *     <li>rational / rational → rational</li>
     * </ul>
     * <p>
     * Respects {@link #forceDoubleArithmetic()} setting - when true, skips rational
     * arithmetic entirely for better performance.
     */
    public NodeConstant applyMultiplicative(NodeConstant left, NodeConstant right,
                                            BinaryOperator<BigRational> rationalOp,
                                            DoubleBinaryOperator doubleOp,
                                            boolean isMultiply) {
        return NumericOperations.applyMultiplicative(left, right, doubleOp, rationalOp, isMultiply, forceDoubleArithmetic());
    }

    // ==================== Additive Operations (+ -) ====================

    /**
     * Applies additive operations (+ or -) with full type preservation.
     * <p>
     * This method has special operator semantics for "number + percent" which
     * applies the percent OF the number (e.g., 100 + 10% = 110).
     * <p>
     * Addition/Subtraction rules:
     * <ul>
     *     <li>unit + unit (same type) → unit (converts to left's unit)</li>
     *     <li>unit + scalar → unit</li>
     *     <li>percent + percent → percent</li>
     *     <li>number + percent → number ± (percent of number) [special operator semantic]</li>
     *     <li>rational + rational → rational</li>
     *     <li>otherwise → double</li>
     * </ul>
     */
    public NodeConstant applyAdditive(NodeConstant left, NodeConstant right,
                                      BinaryOperator<BigRational> rationalOp,
                                      DoubleBinaryOperator doubleOp,
                                      boolean isAddition) {
        // Special operator semantic: number + percent → number ± (percent of number)
        // This is unique to operators (e.g., 100 + 10% = 110, 100 - 10% = 90)
        // Excludes NodeBoolean, NodeUnit, and NodePercent on the left
        if (!(left instanceof NodePercent) && !(left instanceof NodeBoolean) && !(left instanceof NodeUnit) &&
                left instanceof NodeNumber && right instanceof NodePercent) {
            double base = left.doubleValue();
            double percentValue = ((NodePercent) right).getValue();
            double result = isAddition ?
                    base + (base * percentValue) :
                    base - (base * percentValue);
            return new NodeDouble(result);
        }

        // Delegate all other type-preserving operations to NumericOperations
        return NumericOperations.applyAdditive(left, right, doubleOp, rationalOp, forceDoubleArithmetic());
    }

    // ==================== Unary Operations ====================

    /**
     * Applies a unary operation with full type preservation.
     * <p>
     * Delegates to {@link NumericOperations#applyUnary} for consistent
     * type-preserving behavior across all first-class types (units, percents, rationals).
     * <ul>
     *     <li>NodeUnit → NodeUnit (preserves unit)</li>
     *     <li>NodePercent → NodePercent (preserves percent)</li>
     *     <li>NodeRational → NodeRational (preserves exactness where possible)</li>
     *     <li>Others → NodeDouble</li>
     * </ul>
     *
     * @param operand    the operand
     * @param rationalOp the operation for rational operands
     * @param doubleOp   the operation for double operands
     * @return the result
     */
    public NodeConstant applyNumericUnary(NodeConstant operand,
                                          UnaryOperator<BigRational> rationalOp,
                                          DoubleUnaryOperator doubleOp) {
        // Delegate to NumericOperations for consistent type preservation
        return NumericOperations.applyUnary(operand, rationalOp, doubleOp, forceDoubleArithmetic());
    }

    /**
     * Applies a unary operation that always produces a double result.
     *
     * @param operand  the operand
     * @param doubleOp the operation
     * @return the result as NodeDouble
     */
    public NodeConstant applyDoubleUnary(NodeConstant operand, DoubleUnaryOperator doubleOp) {
        double value = toNumber(operand).doubleValue();
        return new NodeDouble(doubleOp.applyAsDouble(value));
    }

    // ==================== Unary Broadcasting ====================

    /**
     * Applies a unary operation with vector/matrix broadcasting.
     *
     * @param operand    the operand
     * @param rationalOp the operation for rational operands
     * @param doubleOp   the operation for double operands
     * @return the result
     */
    public NodeConstant dispatchUnary(NodeConstant operand,
                                      UnaryOperator<BigRational> rationalOp,
                                      DoubleUnaryOperator doubleOp) {
        if (operand instanceof NodeVector vector) {
            Node[] elements = vector.getElements();
            Node[] result = new Node[elements.length];
            for (int i = 0; i < elements.length; i++) {
                result[i] = dispatchUnary((NodeConstant) elements[i], rationalOp, doubleOp);
            }
            return new NodeVector(result);
        }

        if (operand instanceof NodeMatrix matrix) {
            Node[][] elements = matrix.getElements();
            Node[][] result = new Node[matrix.getRows()][matrix.getCols()];
            for (int i = 0; i < matrix.getRows(); i++) {
                for (int j = 0; j < matrix.getCols(); j++) {
                    result[i][j] = dispatchUnary((NodeConstant) elements[i][j], rationalOp, doubleOp);
                }
            }
            return new NodeMatrix(result);
        }

        return applyNumericUnary(operand, rationalOp, doubleOp);
    }

    // ==================== Validation ====================

    /**
     * Ensures a value is positive, throwing IllegalArgumentException if not.
     *
     * @param value the value to check
     * @param name  the name of the parameter for error messages
     */
    public void requirePositive(double value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive, got: " + value);
        }
    }

    /**
     * Ensures a value is non-negative, throwing IllegalArgumentException if not.
     *
     * @param value the value to check
     * @param name  the name of the parameter for error messages
     */
    public void requireNonNegative(double value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be non-negative, got: " + value);
        }
    }

    /**
     * Ensures a value is within a range, throwing IllegalArgumentException if not.
     *
     * @param value the value to check
     * @param min   the minimum allowed value (inclusive)
     * @param max   the maximum allowed value (inclusive)
     * @param name  the name of the parameter for error messages
     */
    public void requireInRange(double value, double min, double max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be in range [" + min + ", " + max + "], got: " + value);
        }
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
}
