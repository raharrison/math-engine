package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing a string literal.
 * Strings are distinct from identifiers and cannot be implicitly converted to numbers.
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
