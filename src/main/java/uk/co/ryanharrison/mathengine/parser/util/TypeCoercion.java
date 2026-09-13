package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.format.StringNodeFormatter;

import java.math.BigDecimal;

/**
 * Conversions between node types, for the edges of the engine where a value arrives as
 * something other than a node or has to leave as one.
 * <p>
 * What happens when two nodes meet in an operation is not here: that is
 * {@code NodeArithmetic}, reached through {@link NodeConstant#add} and friends.
 */
public final class TypeCoercion {

    /**
     * The most significant digits a written decimal is expected to have. A double needing
     * more than this to name itself was computed rather than typed: a third needs sixteen
     * and the classic {@code 0.1 + 0.2} needs seventeen, where every decimal anybody writes
     * needs a handful.
     */
    private static final int MAX_WRITTEN_DIGITS = 12;

    /**
     * The largest denominator a {@code double} is allowed to name as a fraction.
     */
    private static final int MAX_DENOMINATOR = 10_000;

    /**
     * Doubles below this in size are integers a {@code long} holds exactly.
     */
    private static final double LONG_EXACT_LIMIT = 0x1p63;

    private static final StringNodeFormatter DISPLAY = StringNodeFormatter.fullPrecision();

    private TypeCoercion() {
    }

    // ==================== String Conversion ====================

    /**
     * Converts a constant to its display string representation.
     * <p>
     * Used for string concatenation and string coercion. Rules:
     * <ul>
     *     <li>NodeString: raw value (no quotes)</li>
     *     <li>NodeBoolean: "true" or "false"</li>
     *     <li>NodeNumber and NodeUnit: whatever a formatter would show, so that
     *         {@code str(2.5)} and {@code 2.5} agree</li>
     *     <li>Others: toString()</li>
     * </ul>
     *
     * @param node the constant to convert
     * @return the display string
     */
    public static String toDisplayString(NodeConstant node) {
        if (node instanceof NodeString str) {
            return str.getValue();
        }
        if (node instanceof NodeBoolean bool) {
            return bool.getValue() ? "true" : "false";
        }
        if (node instanceof NodeNumber || node instanceof NodeUnit) {
            return DISPLAY.format(node);
        }
        return node.toString();
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

    /**
     * Turns a {@code double} into the number it stands for.
     * <p>
     * A double is a binary fraction, so most decimals are not in it exactly. The engine
     * still has to take doubles at its edges: a Java caller binding a variable, a literal
     * that was computed before it was read, an interpolation weight. This is the single
     * place that decides how much exactness such a value may claim, so that every edge
     * claims the same amount.
     * <ul>
     *     <li>Not finite: stays a double, since no rational is infinite.</li>
     *     <li>Integral: exact, however large, matching the literal {@code 1e20}.</li>
     *     <li>Named by a short decimal: that decimal. {@code 0.1} is a tenth and
     *         {@code 0.000123} is 123 millionths, because those are the shortest decimals
     *         that round to those doubles.</li>
     *     <li>Named by a fraction with a small denominator: that fraction. This is what
     *         reads {@code 1.0 / 3.0} back as a third rather than as sixteen threes.</li>
     *     <li>Otherwise it came out of a calculation and is left as it came, so
     *         {@code sqrt(2)} and {@code 0.1 + 0.2} stay doubles.</li>
     * </ul>
     * Both middle cases are round trips, which is what keeps this honest: an exact answer
     * is one the double could only have meant, never a nearby value that looks tidier.
     *
     * @param value the double to read
     * @return the value as a rational where that is exact, otherwise as a double
     */
    public static NodeNumber toNumber(double value) {
        if (!Double.isFinite(value)) {
            return new NodeDouble(value);
        }

        // An integral double is exactly an integer, so it always reads back exactly.
        // Below 2^63 a long holds it; above, only a BigInteger does, and casting to a
        // long there used to land one short of 2^63 rather than on it.
        if (value == Math.floor(value)) {
            return Math.abs(value) < LONG_EXACT_LIMIT
                    ? new NodeRational((long) value, 1L)
                    : new NodeRational(BigRational.of(new BigDecimal(value).toBigIntegerExact()));
        }

        // The shortest decimal that rounds to this double. Asking for the closest fraction
        // within a denominator instead used to hide exact answers behind better
        // approximations: 0.000123 is exactly 123/1000000, but the closest fraction under a
        // million is 37/300813, which is nearer without being equal, so the value was lost.
        BigDecimal shortest = BigDecimal.valueOf(value).stripTrailingZeros();
        if (shortest.precision() <= MAX_WRITTEN_DIGITS) {
            return new NodeRational(BigRational.of(shortest));
        }

        // No short decimal, so try a short fraction: this is the branch that reads
        // 1.0 / 3.0 back as a third.
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
            case NodeUnit unit -> unit.getMagnitude();
            case NodeNumber num -> num;
            default -> throw new TypeError("Cannot convert " + value.typeName() + " to number");
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
            default -> throw new TypeError("Cannot convert " + value.typeName() + " to boolean");
        };
    }

}
