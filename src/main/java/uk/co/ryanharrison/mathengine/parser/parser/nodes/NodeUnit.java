package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;

/**
 * A quantity with a unit, such as {@code 100 meters} or {@code 25.5 celsius}.
 * Arithmetic rules live in {@link NodeArithmetic}.
 */
public final class NodeUnit extends NodeConstant {

    private final double value;
    private final UnitDefinition unit;

    public NodeUnit(double value, UnitDefinition unit) {
        this.value = value;
        this.unit = unit;
    }

    public static NodeUnit of(double value, UnitDefinition unit) {
        return new NodeUnit(value, unit);
    }

    public double getValue() {
        return value;
    }

    public UnitDefinition getUnit() {
        return unit;
    }

    /**
     * Converts this quantity to another unit of the same type.
     *
     * @throws TypeError if the units measure different things
     */
    public NodeUnit convertTo(UnitDefinition targetUnit) {
        if (!unit.type().equals(targetUnit.type())) {
            throw new TypeError(
                    "Cannot convert between incompatible unit types: " +
                            unit.type() + " and " + targetUnit.type());
        }

        if (unit.getName().equals(targetUnit.getName())) {
            return this;
        }
        return NodeUnit.of(targetUnit.fromBase(unit.toBase(value)), targetUnit);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public String typeName() {
        return "unit value";
    }

    @Override
    public String toString() {
        return value + " " + unit.getDisplayName(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeUnit other)) return false;
        return Double.compare(value, other.value) == 0 &&
                unit.getName().equals(other.unit.getName());
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value) * 31 + unit.getName().hashCode();
    }
}
