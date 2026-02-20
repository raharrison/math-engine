package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;

/**
 * Immutable node representing a numeric value with an associated unit.
 * <p>
 * Examples: 100 meters, 25.5 celsius, 3.14 feet
 * </p>
 * <p>
 * NodeUnit stores a numeric value (as a double) and a unit definition.
 * The value represents the quantity in the given unit.
 * </p>
 *
 * <h2>Arithmetic with Units:</h2>
 * <ul>
 *     <li>Addition/Subtraction: Same unit → result in that unit</li>
 *     <li>Multiplication by scalar: Scales the value, preserves unit</li>
 *     <li>Division by scalar: Scales the value, preserves unit</li>
 *     <li>Unit conversion: Uses UnitDefinition.toBase() and fromBase()</li>
 * </ul>
 */
public final class NodeUnit extends NodeConstant {

    private final double value;
    private final UnitDefinition unit;

    /**
     * Creates a NodeUnit with a numeric value and unit.
     *
     * @param value the numeric value
     * @param unit  the unit definition
     */
    public NodeUnit(double value, UnitDefinition unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * Creates a NodeUnit from a double value and unit.
     *
     * @param value the numeric value
     * @param unit  the unit definition
     */
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
     * Convert this unit value to a different unit.
     *
     * @param targetUnit the target unit definition
     * @return a new NodeUnit with the converted value
     * @throws TypeError if units are incompatible (different types)
     */
    public NodeUnit convertTo(UnitDefinition targetUnit) {
        if (!unit.type().equals(targetUnit.type())) {
            throw new TypeError(
                    "Cannot convert between incompatible unit types: " +
                            unit.type() + " and " + targetUnit.type());
        }

        // If same unit, return this
        if (unit.getName().equals(targetUnit.getName())) {
            return this;
        }

        // Convert: value -> base unit -> target unit
        double valueInBase = unit.toBase(value);
        double valueInTarget = targetUnit.fromBase(valueInBase);

        return NodeUnit.of(valueInTarget, targetUnit);
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
