package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

/**
 * Node representing a string literal.
 * Strings are distinct from identifiers and cannot be implicitly converted to numbers.
 * <p>
 * Supports concatenation via {@link #add} and repetition via {@link #multiply}.
 * Lexicographic comparison is supported via {@link #compareTo} for string-to-string comparisons.
 */
public final class NodeString extends NodeConstant {

    private final String value;

    public NodeString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException("Cannot convert string to double");
    }

    @Override
    public String typeName() {
        return "string";
    }

    // ==================== Universal Arithmetic ====================

    @Override
    public NodeConstant add(NodeConstant other) {
        return new NodeString(this.value + TypeCoercion.toDisplayString(other));
    }

    @Override
    public NodeConstant subtract(NodeConstant other) {
        throw new TypeError("Cannot subtract from string");
    }

    @Override
    public NodeConstant multiply(NodeConstant other) {
        if (other.isNumeric()) {
            return repeat(other);
        }
        throw new TypeError("Cannot multiply string by " + other.typeName());
    }

    @Override
    public NodeConstant divide(NodeConstant other) {
        throw new TypeError("Cannot divide string");
    }

    @Override
    public NodeConstant power(NodeConstant other) {
        throw new TypeError("Cannot raise string to a power");
    }

    @Override
    public NodeConstant negate() {
        throw new TypeError("Cannot negate string");
    }

    @Override
    public int compareTo(NodeConstant other) {
        if (other instanceof NodeString otherStr) {
            return this.value.compareTo(otherStr.getValue());
        }
        throw new TypeError("Cannot compare string with " + other.typeName());
    }

    /**
     * Repeats this string the specified number of times.
     *
     * @param times a numeric constant specifying the repeat count
     * @return a new NodeString with the repeated value
     * @throws TypeError if the count is negative
     */
    public NodeConstant repeat(NodeConstant times) {
        int n = (int) NumericOperations.toDoubleValue(times);
        if (n < 0) {
            throw new TypeError("Cannot repeat string negative times: " + n);
        }
        return new NodeString(n == 0 ? "" : this.value.repeat(n));
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeString other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
