package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Base class for nodes representing constant values that have already been evaluated.
 * <p>
 * NodeConstant instances are the final results of evaluation and don't require
 * further computation. They include numbers, booleans, strings, vectors, matrices, etc.
 */
public abstract class NodeConstant extends Node {

    /**
     * Convert this constant to a double value for numeric operations.
     * Throws an exception if this constant is not numeric.
     */
    public abstract double doubleValue();

    /**
     * Check if this constant represents a numeric value.
     */
    public abstract boolean isNumeric();

    /**
     * Check if this constant represents a boolean value.
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * Check if this constant represents a string value.
     */
    public boolean isString() {
        return false;
    }

    /**
     * Check if this constant represents a vector.
     */
    public boolean isVector() {
        return false;
    }

    /**
     * Check if this constant represents a matrix.
     */
    public boolean isMatrix() {
        return false;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitConstant(this);
    }
}
