package uk.co.ryanharrison.mathengine.parser.ast;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

/**
 * A string value. Strings never coerce to numbers, but {@code +} concatenates,
 * {@code *} repeats and comparisons are lexicographic. See {@link NodeArithmetic}.
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
    public boolean isNumeric() {
        return false;
    }

    @Override
    public double doubleValue() {
        throw new TypeError("Cannot convert a string to a number: " + this);
    }

    @Override
    public String typeName() {
        return "string";
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
