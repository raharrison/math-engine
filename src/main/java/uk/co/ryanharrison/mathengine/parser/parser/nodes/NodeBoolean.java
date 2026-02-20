package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing a boolean value (true or false).
 * Booleans can be coerced to numbers: true = 1.0, false = 0.0.
 */
public final class NodeBoolean extends NodeNumber {

    private final boolean value;

    public NodeBoolean(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public double doubleValue() {
        return value ? 1.0 : 0.0;
    }

    @Override
    public NodeNumber negate() {
        return new NodeDouble(value ? -1.0 : 0.0);
    }

    @Override
    public NodeNumber abs() {
        return new NodeDouble(value ? 1.0 : 0.0);
    }

    /**
     * Logical NOT operation.
     */
    public NodeBoolean not() {
        return new NodeBoolean(!value);
    }

    @Override
    public String typeName() {
        return "boolean";
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeBoolean other)) return false;
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    // Commonly used instances
    public static final NodeBoolean TRUE = new NodeBoolean(true);
    public static final NodeBoolean FALSE = new NodeBoolean(false);

    public static NodeBoolean of(boolean value) {
        return value ? TRUE : FALSE;
    }
}
