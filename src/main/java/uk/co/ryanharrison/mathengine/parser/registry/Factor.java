package uk.co.ryanharrison.mathengine.parser.registry;

import uk.co.ryanharrison.mathengine.core.BigRational;

import java.math.BigDecimal;

/**
 * One number in a unit's conversion rule, and whether it is exact.
 *
 * <pre>{@code
 * Factor.of("0.3048")          // a foot
 * Factor.ratio(5, 9)           // a fahrenheit degree
 * Factor.approx(Math.PI / 180) // a degree of arc, which no ratio can state
 * }</pre>
 */
public record Factor(BigRational value, boolean exact) {

    public static final Factor ZERO = new Factor(BigRational.ZERO, true);
    public static final Factor ONE = new Factor(BigRational.ONE, true);

    /**
     * @param decimal read as written, so no double sees it
     */
    public static Factor of(String decimal) {
        return new Factor(BigRational.of(new BigDecimal(decimal)), true);
    }

    /**
     * A ratio that is not a terminating decimal, such as five ninths.
     */
    public static Factor ratio(long numerator, long denominator) {
        return new Factor(BigRational.of(numerator, denominator), true);
    }

    /**
     * A factor no ratio can state. Conversions through it stay inexact.
     */
    public static Factor approx(double value) {
        return new Factor(BigRational.of(value), false);
    }

    public double doubleValue() {
        return value.doubleValue();
    }

    public boolean isZero() {
        return value.getNumerator().signum() == 0;
    }

    @Override
    public String toString() {
        return exact ? value.toString() : "~" + doubleValue();
    }
}
