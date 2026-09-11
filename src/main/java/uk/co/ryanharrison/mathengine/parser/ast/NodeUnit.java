package uk.co.ryanharrison.mathengine.parser.ast;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;

/**
 * A quantity with a unit, such as {@code 100 meters} or {@code 25.5 celsius}.
 * The magnitude is a {@link NodeNumber}, so a quantity is as exact as the number that made
 * it. Arithmetic rules live in {@link NodeArithmetic}.
 */
public final class NodeUnit extends NodeConstant {

    private final NodeNumber magnitude;
    private final UnitDefinition unit;

    public NodeUnit(NodeNumber magnitude, UnitDefinition unit) {
        this.magnitude = magnitude;
        this.unit = unit;
    }

    public NodeUnit(double value, UnitDefinition unit) {
        this(new NodeDouble(value), unit);
    }

    public static NodeUnit of(NodeNumber magnitude, UnitDefinition unit) {
        return new NodeUnit(magnitude, unit);
    }

    public static NodeUnit of(double value, UnitDefinition unit) {
        return new NodeUnit(new NodeDouble(value), unit);
    }

    /**
     * The size of the quantity, with its exactness intact.
     */
    public NodeNumber getMagnitude() {
        return magnitude;
    }

    public double getValue() {
        return magnitude.doubleValue();
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
        return NodeUnit.of(convert(magnitude, unit, targetUnit), targetUnit);
    }

    /**
     * Goes to the base unit and out again in one step. Rounding at the halfway point is
     * what made {@code 0 celsius in fahrenheit} answer 31.999999999999943.
     */
    private static NodeNumber convert(NodeNumber magnitude, UnitDefinition from, UnitDefinition to) {
        double approximate = magnitude.doubleValue();
        if (!Double.isFinite(approximate)) {
            return new NodeDouble(to.fromBase(from.toBase(approximate)));
        }

        boolean exact = magnitude instanceof NodeRational && from.isExact() && to.isExact();
        BigRational value = magnitude instanceof NodeRational rational
                ? rational.getValue()
                : BigRational.of(approximate);
        BigRational converted = to.fromBase(from.toBase(value));

        return exact ? new NodeRational(converted) : new NodeDouble(converted.doubleValue());
    }

    @Override
    public double doubleValue() {
        return magnitude.doubleValue();
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
        return magnitude + " " + unit.getDisplayName(getValue());
    }

    /** Compares the magnitude by size, so an exact 4 and a 4.0 are one quantity. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeUnit other)) return false;
        return Double.compare(getValue(), other.getValue()) == 0 &&
                unit.getName().equals(other.unit.getName());
    }

    @Override
    public int hashCode() {
        return Double.hashCode(getValue()) * 31 + unit.getName().hashCode();
    }
}
