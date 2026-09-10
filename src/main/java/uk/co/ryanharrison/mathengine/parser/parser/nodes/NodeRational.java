package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.format.RationalDisplay;

/**
 * An exact rational number, of arbitrary precision. Integers and fractions are both held
 * this way, so arithmetic between them never rounds.
 * <p>
 * Exactness is not the same thing as fraction notation: {@code 2.5} is held as 5/2 and
 * shown as {@code 2.5}, while a third has no decimal and is shown as {@code 1/3}.
 * {@link RationalDisplay} decides which, for {@link #toString()} and for the formatters
 * alike.
 */
public final class NodeRational extends NodeNumber {

    private final BigRational value;

    public NodeRational(BigRational value) {
        this.value = value;
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
        return RationalDisplay.asDecimal(value, -1)
                .orElseGet(() -> value.getNumerator() + "/" + value.getDenominator());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeRational other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
