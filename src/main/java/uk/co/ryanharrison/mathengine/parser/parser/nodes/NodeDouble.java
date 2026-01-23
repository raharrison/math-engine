package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing a double-precision floating-point number (IEEE 754).
 * Used for decimal numbers, scientific notation, and results of operations that require floating-point precision.
 */
public final class NodeDouble extends NodeNumber {

    private final double value;

    public NodeDouble(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public NodeNumber negate() {
        return new NodeDouble(-value);
    }

    @Override
    public NodeNumber abs() {
        return new NodeDouble(Math.abs(value));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeDouble other)) return false;
        return Double.compare(value, other.value) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
}
