package uk.co.ryanharrison.mathengine.parser.format;

import uk.co.ryanharrison.mathengine.core.BigRational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * Decides whether an exact rational reads better as a decimal or as a fraction.
 * <p>
 * Exactness belongs to the value, fraction notation to the display. Every value that can be
 * exact is held as a {@link BigRational}, so a decimal literal, a unit conversion and a
 * percentage all arrive here as fractions although nobody wrote one; printing {@code n/d}
 * unconditionally is what showed {@code 2.5} as {@code 5/2} and {@code 100 m in feet} as
 * {@code 125000/381 feet}.
 *
 * <h2>The rule</h2>
 * <ol>
 *     <li>An integer prints as its digits, however many, so a factorial stays exact.</li>
 *     <li>A denominator of only twos and fives gives a finite decimal expansion, and that
 *         decimal <em>is</em> the value: {@code 5/2} is {@code 2.5}, {@code 493/5} is
 *         {@code 98.6}.</li>
 *     <li>Otherwise no decimal equals the value, and a short ratio says so: {@code 1/3},
 *         {@code 22/7}.</li>
 *     <li>A ratio too wide to read is noise rather than information, so it rounds:
 *         {@code 125000/381} is {@code 328.0839895013123}.</li>
 * </ol>
 *
 * <p>Rounding uses {@link FormatUtils#formatFiniteDouble}, so a rational and a double of
 * equal value print alike. Both formatters and string coercion read this class, so one
 * value cannot be spelled two ways.
 */
public final class RationalDisplay {

    /**
     * The largest numerator and denominator still worth reading as a fraction. Above this a
     * ratio carries no meaning a decimal does not carry better.
     */
    private static final BigInteger FRACTION_LIMIT = BigInteger.valueOf(10_000);

    /**
     * The longest exact decimal worth printing, in significant digits and in scale.
     * {@code 1/2^80} terminates, but forty digits of it is not a number anybody wanted.
     */
    private static final int MAX_EXACT_DIGITS = 20;

    private static final BigInteger FIVE = BigInteger.valueOf(5);

    private RationalDisplay() {
    }

    /**
     * The decimal text for a rational, or empty when it reads better as a fraction.
     * <p>
     * A caller that gets an empty result renders {@link BigRational#getNumerator()} over
     * {@link BigRational#getDenominator()} in whatever notation it uses.
     *
     * @param value         the rational to display
     * @param decimalPlaces places to round to, or negative for full precision. A caller
     *                      that asked for a fixed number of places asked for decimals, so
     *                      every value answers as one.
     * @return the decimal text, or empty to ask for a fraction
     */
    public static Optional<String> asDecimal(BigRational value, int decimalPlaces) {
        BigInteger numerator = value.getNumerator();
        BigInteger denominator = value.getDenominator();

        if (denominator.equals(BigInteger.ONE)) {
            return Optional.of(numerator.toString());
        }
        if (decimalPlaces >= 0) {
            return Optional.of(FormatUtils.formatFiniteDouble(value.doubleValue(), decimalPlaces));
        }

        Optional<String> exact = exactDecimal(numerator, denominator);
        if (exact.isPresent()) {
            return exact;
        }
        if (numerator.abs().compareTo(FRACTION_LIMIT) <= 0
                && denominator.compareTo(FRACTION_LIMIT) <= 0) {
            return Optional.empty();
        }
        return Optional.of(FormatUtils.formatFiniteDouble(value.doubleValue(), -1));
    }

    /**
     * The exact decimal expansion, when there is one and it is short enough to print.
     * A denominator of only twos and fives is what makes the expansion terminate.
     */
    private static Optional<String> exactDecimal(BigInteger numerator, BigInteger denominator) {
        BigInteger remaining = denominator;
        while (remaining.mod(BigInteger.TWO).signum() == 0) {
            remaining = remaining.divide(BigInteger.TWO);
        }
        while (remaining.mod(FIVE).signum() == 0) {
            remaining = remaining.divide(FIVE);
        }
        if (!remaining.equals(BigInteger.ONE)) {
            return Optional.empty();
        }

        BigDecimal decimal = new BigDecimal(numerator)
                .divide(new BigDecimal(denominator))
                .stripTrailingZeros();
        if (decimal.precision() > MAX_EXACT_DIGITS || decimal.scale() > MAX_EXACT_DIGITS) {
            return Optional.empty();
        }
        return Optional.of(decimal.toPlainString());
    }
}
