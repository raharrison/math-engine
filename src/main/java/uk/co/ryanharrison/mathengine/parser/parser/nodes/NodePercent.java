package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing a percentage value.
 * Percentages are stored as their decimal equivalent (e.g., 50% = 0.5).
 */
public final class NodePercent extends NodeNumber {

    private final double value;

    /**
     * Create a percent node from a percentage value.
     * For example, NodePercent(50) represents 50% (0.5 as a decimal).
     */
    public NodePercent(double percentValue) {
        this.value = percentValue / 100.0;
    }

    /**
     * Create a percent node from an already-converted decimal value.
     */
    public static NodePercent fromDecimal(double decimalValue) {
        return new NodePercent(decimalValue * 100);
    }

    public double getValue() {
        return value;
    }

    /**
     * Get the percentage value (e.g., 50 for 50%).
     */
    public double getPercentValue() {
        return value * 100.0;
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
        return String.format("%.2f%%", value * 100);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodePercent)) return false;
        NodePercent other = (NodePercent) obj;
        return Double.compare(value, other.value) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
}
