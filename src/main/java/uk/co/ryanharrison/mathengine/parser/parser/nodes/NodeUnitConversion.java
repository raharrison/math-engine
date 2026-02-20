package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing a unit conversion (value in/to/as unit).
 * Example: 100 meters in feet
 */
public final class NodeUnitConversion extends NodeExpression {

    private final Node value;
    private final String targetUnit;

    public NodeUnitConversion(Node value, String targetUnit) {
        this.value = value;
        this.targetUnit = targetUnit;
    }

    public Node getValue() {
        return value;
    }

    public String getTargetUnit() {
        return targetUnit;
    }

    @Override
    public String typeName() {
        return "unit conversion";
    }

    @Override
    public String toString() {
        return value + " in " + targetUnit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeUnitConversion other)) return false;
        return value.equals(other.value) && targetUnit.equals(other.targetUnit);
    }

    @Override
    public int hashCode() {
        return value.hashCode() * 31 + targetUnit.hashCode();
    }
}
