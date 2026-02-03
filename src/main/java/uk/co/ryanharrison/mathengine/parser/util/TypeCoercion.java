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

    private static final int MAX_DENOMINATOR = 10_000;

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
        return value instanceof NodeNumber || value instanceof NodeUnit;
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

    // ==================== Type Conversion ====================

    public static NodeNumber toNumber(double value) {
        if (!Double.isFinite(value)) {
            return new NodeDouble(value);
        }

        // Integers are always rational
        if (value == Math.floor(value) && Math.abs(value) <= Long.MAX_VALUE) {
            return new NodeRational((long) value, 1L);
        }

        // Find best rational approximation with a small denominator.
        // Accept it only if it round-trips exactly to the same double.
        try {
            BigRational approx = BigRational.of(value, MAX_DENOMINATOR);
            if (approx.doubleValue() == value) {
                return new NodeRational(approx);
            }
        } catch (ArithmeticException ignored) {
        }

        return new NodeDouble(value);
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

}
