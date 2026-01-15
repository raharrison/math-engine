package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Abstract base class for all numeric node types.
 * Numeric nodes can be converted to double values and support arithmetic operations.
 */
public abstract class NodeNumber extends NodeConstant {

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
