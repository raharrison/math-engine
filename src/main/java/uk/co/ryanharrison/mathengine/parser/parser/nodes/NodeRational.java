package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.core.BigRational;

/**
 * Node representing an exact rational number (arbitrary precision).
 * Used for integers and fractions to maintain exact arithmetic without rounding errors.
 */
public final class NodeRational extends NodeNumber {

    private static final int maxIterations = 150;
    private static final double epsilon = 1E-15;
    private final BigRational value;

    public NodeRational(BigRational value) {
        this.value = value;
    }

    public NodeRational(double value) {
        this.value = BigRational.of(value, epsilon, maxIterations);
    }

    public NodeRational(long numerator, long denominator) {
        this.value = BigRational.of(numerator, denominator);
    }

    public NodeRational(long numerator) {
        this.value = BigRational.of(numerator);
    }

    public BigRational getValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    @Override
    public NodeNumber negate() {
        return new NodeRational(value.negate());
    }

    @Override
    public NodeNumber abs() {
        return new NodeRational(value.abs());
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeRational)) return false;
        NodeRational other = (NodeRational) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
