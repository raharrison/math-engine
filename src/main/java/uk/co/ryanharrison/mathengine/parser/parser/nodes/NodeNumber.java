package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Abstract base class for all numeric node types.
 * Numeric nodes can be converted to double values and support arithmetic operations.
 * <p>
 * Sealed to enable exhaustiveness checking in pattern matching.
 */
public abstract sealed class NodeNumber extends NodeConstant permits
        NodeDouble,
        NodeRational,
        NodePercent,
        NodeBoolean {

    @Override
    public String typeName() {
        return "number";
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    /**
     * Get the numeric value as a double.
     */
    @Override
    public abstract double doubleValue();

    /**
     * Negate this number (multiply by -1).
     */
    public abstract NodeNumber negate();

    /**
     * Get the absolute value of this number.
     */
    public abstract NodeNumber abs();
}
