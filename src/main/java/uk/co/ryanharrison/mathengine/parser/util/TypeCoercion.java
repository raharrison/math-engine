package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

/**
 * Utility class for type coercion and conversion operations.
 * <p>
 * Provides static methods for converting between node types and
 * determining type compatibility for operations.
 *
 * <h2>Type Hierarchy:</h2>
 * <ul>
 *     <li>NodeBoolean - can be coerced to number (true=1, false=0)</li>
 *     <li>NodeRational - exact rational arithmetic</li>
 *     <li>NodeDouble - IEEE 754 double-precision floating point</li>
 *     <li>NodePercent - percentage (stored as fraction, e.g., 50% = 0.5)</li>
 *     <li>NodeVector - collection of values</li>
 *     <li>NodeMatrix - 2D collection of values</li>
 * </ul>
 *
 * <h2>Type Promotion Rules:</h2>
 * <ul>
 *     <li>Rational + Rational = Rational</li>
 *     <li>Rational + Double = Double</li>
 *     <li>Any + Percent = Double</li>
 *     <li>Boolean is coerced to Rational (1 or 0)</li>
 * </ul>
 */
public final class TypeCoercion {

    private TypeCoercion() {
    }

    // ==================== Type Checking ====================

    /**
     * Checks if the value is a numeric type.
     *
     * @param value the value to check
     * @return true if numeric (NodeNumber, NodeBoolean, or NodeUnit)
     */
    public static boolean isNumeric(NodeConstant value) {
        return value instanceof NodeNumber || value instanceof NodeBoolean || value instanceof NodeUnit;
    }

    /**
     * Checks if the value is a collection type.
     *
     * @param value the value to check
     * @return true if vector or matrix
     */
    public static boolean isCollection(NodeConstant value) {
        return value instanceof NodeVector || value instanceof NodeMatrix;
    }

    /**
     * Checks if the value is numeric or a collection (can participate in multiplication).
     *
     * @param value the value to check
     * @return true if numeric, vector, or matrix
     */
    public static boolean isNumericOrCollection(NodeConstant value) {
        return isNumeric(value) || isCollection(value);
    }

    /**
     * Checks if two values require double arithmetic when combined.
     *
     * @param a first value
     * @param b second value
     * @return true if result should be double
     */
    public static boolean requiresDouble(NodeConstant a, NodeConstant b) {
        return a instanceof NodeDouble || b instanceof NodeDouble ||
                a instanceof NodePercent || b instanceof NodePercent;
    }

    // ==================== Type Conversion ====================

    public static NodeNumber toNumber(double value) {
        double absValue = Math.abs(value);

        // 1. If the value is too large to fit in a rational representation, return NodeDouble.
        if (absValue > Integer.MAX_VALUE) {
            return new NodeDouble(value);
        }

        // 2. If the value is too small (close to zero), return NodeDouble.
        if (absValue < 1.0 / Integer.MAX_VALUE && absValue != 0) {
            return new NodeDouble(value);
        }

        // 3. If the number has too many decimal places (arbitrary threshold of 5 decimals), return NodeDouble.
        if (Double.toString(absValue).split("\\.")[1].length() > 5) {
            return new NodeDouble(value);
        }

        // 4. Try to convert to NodeRational.
        try {
            return new NodeRational(value);  // Try to get fraction approximation
        } catch (IllegalArgumentException e) {
            return new NodeDouble(value);  // If it fails, fallback to NodeDouble.
        }
    }

    /**
     * Converts a constant to a NodeNumber, coercing booleans to numbers
     * and extracting values from units.
     *
     * @param value the value to convert
     * @return the value as a NodeNumber (units are stripped to just their numeric value)
     * @throws TypeError if the value cannot be converted
     */
    public static NodeNumber toNumber(NodeConstant value) {
        return switch (value) {
            case NodeBoolean bool -> new NodeRational(bool.getValue() ? 1 : 0);
            case NodeUnit unit -> new NodeDouble(unit.getValue());
            case NodeNumber num -> num;
            default -> throw new TypeError("Cannot convert " + typeName(value) + " to number");
        };
    }

    /**
     * Converts a constant to a double value.
     *
     * @param value the value to convert
     * @return the double value
     * @throws TypeError if the value cannot be converted
     */
    public static double toDouble(NodeConstant value) {
        return toNumber(value).doubleValue();
    }

    /**
     * Converts a constant to an integer value.
     *
     * @param value the value to convert
     * @return the integer value
     * @throws TypeError if the value cannot be converted
     */
    public static int toInt(NodeConstant value) {
        return (int) toDouble(value);
    }

    /**
     * Converts a constant to a long value.
     *
     * @param value the value to convert
     * @return the long value
     * @throws TypeError if the value cannot be converted
     */
    public static long toLong(NodeConstant value) {
        return (long) toDouble(value);
    }

    /**
     * Converts a constant to a boolean value.
     * Numbers are truthy if non-zero.
     *
     * @param value the value to convert
     * @return the boolean value
     * @throws TypeError if the value cannot be converted
     */
    public static boolean toBoolean(NodeConstant value) {
        return switch (value) {
            case NodeBoolean bool -> bool.getValue();
            case NodeNumber num -> num.doubleValue() != 0.0;
            default -> throw new TypeError("Cannot convert " + typeName(value) + " to boolean");
        };
    }

    /**
     * Converts a constant to a NodeRational.
     *
     * @param value the value to convert
     * @return the value as NodeRational
     */
    public static NodeRational toRational(NodeConstant value) {
        return switch (value) {
            case NodeRational rat -> rat;
            case NodeBoolean bool -> new NodeRational(bool.getValue() ? 1 : 0);
            // Convert double to rational (may lose precision)
            default -> new NodeRational(BigRational.of(toDouble(value)));
        };
    }

    /**
     * Converts a constant to a NodeDouble.
     *
     * @param value the value to convert
     * @return the value as NodeDouble
     */
    public static NodeDouble toNodeDouble(NodeConstant value) {
        return switch (value) {
            case NodeDouble d -> d;
            default -> new NodeDouble(toDouble(value));
        };
    }

    // ==================== Type Promotion ====================

    /**
     * Promotes two numeric values to a common type and applies an operation.
     * <p>
     * Delegates to {@link NumericOperations#applyNumeric} for consistent
     * type-preserving behavior. Note: This method does not preserve units
     * or percents - use {@link NumericOperations#applyAdditive} or
     * {@link NumericOperations#applyMultiplicative} for full type preservation.
     *
     * @param a          first operand
     * @param b          second operand
     * @param rationalOp operation for rational arguments
     * @param doubleOp   operation for double arguments
     * @return the result
     */
    public static NodeConstant promoteAndApply(NodeConstant a, NodeConstant b,
                                               java.util.function.BinaryOperator<BigRational> rationalOp,
                                               java.util.function.DoubleBinaryOperator doubleOp) {
        // Delegate to NumericOperations for consistent behavior
        return NumericOperations.applyNumeric(a, b, doubleOp, rationalOp);
    }

    /**
     * Applies a unary operation with type preservation.
     * <p>
     * Delegates to {@link NumericOperations#applyUnary} for consistent
     * type-preserving behavior across all first-class types (units, percents, rationals).
     *
     * @param value      the operand
     * @param rationalOp operation for rational argument
     * @param doubleOp   operation for double argument
     * @return the result
     */
    public static NodeConstant applyUnary(NodeConstant value,
                                          java.util.function.UnaryOperator<BigRational> rationalOp,
                                          java.util.function.DoubleUnaryOperator doubleOp) {
        // Delegate to NumericOperations for consistent type preservation
        return NumericOperations.applyUnary(value, rationalOp, doubleOp);
    }

    // ==================== Utility Methods ====================

    /**
     * Gets a human-readable type name for a node.
     *
     * @param value the value
     * @return type name string
     */
    public static String typeName(NodeConstant value) {
        if (value == null) return "null";
        return value.getClass().getSimpleName().replace("Node", "").toLowerCase();
    }

    /**
     * Gets a human-readable type name for a class.
     *
     * @param clazz the class
     * @return type name string
     */
    public static String typeName(Class<? extends NodeConstant> clazz) {
        return clazz.getSimpleName().replace("Node", "").toLowerCase();
    }

    /**
     * Checks if two values have compatible types for a binary operation.
     *
     * @param a first value
     * @param b second value
     * @return true if types are compatible
     */
    public static boolean areCompatible(NodeConstant a, NodeConstant b) {
        // Numeric types are compatible with each other
        if (isNumeric(a) && isNumeric(b)) {
            return true;
        }

        // Vectors are compatible with scalars (broadcasting)
        if (isNumeric(a) && b instanceof NodeVector) return true;
        if (a instanceof NodeVector && isNumeric(b)) return true;
        if (a instanceof NodeVector && b instanceof NodeVector) return true;

        // Matrices are compatible with scalars (broadcasting)
        if (isNumeric(a) && b instanceof NodeMatrix) return true;
        if (a instanceof NodeMatrix && isNumeric(b)) return true;
        if (a instanceof NodeMatrix && b instanceof NodeMatrix) return true;

        return false;
    }

    /**
     * Creates a type error message for incompatible operands.
     *
     * @param operation the operation name
     * @param a         first operand
     * @param b         second operand
     * @return error message
     */
    public static String typeErrorMessage(String operation, NodeConstant a, NodeConstant b) {
        return "Cannot perform " + operation + " on " + typeName(a) + " and " + typeName(b);
    }
}
