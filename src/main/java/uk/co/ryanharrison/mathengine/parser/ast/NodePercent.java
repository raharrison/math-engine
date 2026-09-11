package uk.co.ryanharrison.mathengine.parser.ast;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

/**
 * A percentage, held as the {@link NodeNumber} fraction it stands for: 50% is one half.
 * Holding a number rather than a double is what makes {@code 1/2 + 25%} exact.
 * <p>
 * A percentage built from a {@code double} reads it through
 * {@link TypeCoercion#toNumber(double)}, the rule every other edge of the engine uses, so
 * the same figure cannot be exact as a number and inexact as a percentage.
 */
public final class NodePercent extends NodeNumber {

    private static final BigRational HUNDRED = BigRational.of(100);

    private final NodeNumber fraction;

    /**
     * From the number in front of the sign: {@code new NodePercent(50)} is 50%.
     */
    public NodePercent(double percentValue) {
        this(divideByHundred(TypeCoercion.toNumber(percentValue)));
    }

    private NodePercent(NodeNumber fraction) {
        this.fraction = fraction;
    }

    /** From the fraction it stands for: {@code fromDecimal(0.5)} is 50%. */
    public static NodePercent fromDecimal(double decimalValue) {
        return new NodePercent(TypeCoercion.toNumber(decimalValue));
    }

    /**
     * From the fraction it stands for: {@code fromFraction(1/2)} is 50%.
     */
    public static NodePercent fromFraction(NodeNumber fraction) {
        return new NodePercent(fraction);
    }

    /**
     * From the number in front of the sign: {@code ofPercentValue(50)} is 50%.
     */
    public static NodePercent ofPercentValue(NodeNumber percentValue) {
        return new NodePercent(divideByHundred(percentValue));
    }

    /**
     * The fraction it stands for: 50% gives one half. Exact where the percentage is.
     * {@link #getValue()} is the same thing as a {@code double}.
     */
    public NodeNumber getFraction() {
        return fraction;
    }

    /**
     * The number in front of the sign: 50% gives 50. Exact where the percentage is, so a
     * third of one percent gives 100/3 rather than 33.333333333333336. This is what a
     * formatter prints, so a percentage is spelled by the rules any other number is.
     * {@link #getPercentValue()} is the same thing as a {@code double}.
     */
    public NodeNumber getPercent() {
        if (fraction instanceof NodeRational rational) {
            return new NodeRational(rational.getValue().multiply(HUNDRED));
        }
        return new NodeDouble(fraction.doubleValue() * 100.0);
    }

    /** {@link #getFraction()} as a {@code double}: 50% gives 0.5. */
    public double getValue() {
        return fraction.doubleValue();
    }

    /** {@link #getPercent()} as a {@code double}: 50% gives 50. */
    public double getPercentValue() {
        return fraction.doubleValue() * 100.0;
    }

    @Override
    public double doubleValue() {
        return fraction.doubleValue();
    }

    @Override
    public NodeNumber negate() {
        return new NodePercent(fraction.negate());
    }

    @Override
    public NodeNumber abs() {
        return new NodePercent(fraction.abs());
    }

    @Override
    public String typeName() {
        return "percentage";
    }

    /** The percentage as written. A formatter is what a caller should use to show one. */
    @Override
    public String toString() {
        return getPercent() + "%";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodePercent other)) return false;
        return Double.compare(getValue(), other.getValue()) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(getValue());
    }

    // ==================== Helpers ====================

    private static NodeNumber divideByHundred(NodeNumber value) {
        if (value instanceof NodeRational rational) {
            return new NodeRational(rational.getValue().divide(HUNDRED));
        }
        return new NodeDouble(value.doubleValue() / 100.0);
    }

}
