package uk.co.ryanharrison.mathengine.core;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Immutable implementation of an arbitrary-precision rational number.
 * <p>
 * A rational number is a number that can be expressed as the quotient or fraction
 * {@code p/q} of two integers, where {@code p} is the numerator and {@code q} is
 * the non-zero denominator. This class provides exact arithmetic operations without
 * overflow or precision loss.
 * </p>
 * <p>
 * All {@code BigRational} instances are automatically reduced to lowest terms, meaning
 * the greatest common divisor (GCD) of the numerator and denominator is always 1.
 * The sign is always carried by the numerator (the denominator is always positive).
 * </p>
 *
 * <h2>Construction Examples:</h2>
 * <pre>{@code
 * // From integers
 * BigRational half = BigRational.of(1, 2);
 * BigRational three = BigRational.of(3);
 *
 * // From double (exact conversion)
 * BigRational fromDouble = BigRational.of(0.5);
 *
 * // From double with approximation
 * BigRational approx = BigRational.of(Math.PI, 1e-9, 100);
 *
 * // Using constants
 * BigRational zero = BigRational.ZERO;
 * BigRational one = BigRational.ONE;
 * }</pre>
 *
 * <h2>Arithmetic Examples:</h2>
 * <pre>{@code
 * BigRational a = BigRational.of(1, 2);  // 1/2
 * BigRational b = BigRational.of(1, 3);  // 1/3
 *
 * BigRational sum = a.add(b);            // 5/6
 * BigRational diff = a.subtract(b);      // 1/6
 * BigRational product = a.multiply(b);   // 1/6
 * BigRational quotient = a.divide(b);    // 3/2
 * BigRational power = a.pow(2);          // 1/4
 * }</pre>
 *
 * <h2>Key Properties:</h2>
 * <ul>
 *     <li><b>Immutable</b>: All operations return new instances</li>
 *     <li><b>Thread-safe</b>: Can be safely shared between threads</li>
 *     <li><b>Normalized</b>: Always in lowest terms with positive denominator</li>
 *     <li><b>Exact</b>: No rounding errors in arithmetic operations</li>
 * </ul>
 *
 * @see BigInteger
 * @see Number
 */
public final class BigRational extends Number implements Comparable<BigRational>, Serializable {

    // ==================== Common Constants ====================

    /**
     * A rational number representing zero (0/1).
     */
    public static final BigRational ZERO = new BigRational(BigInteger.ZERO, BigInteger.ONE);

    /**
     * A rational number representing one (1/1).
     */
    public static final BigRational ONE = new BigRational(BigInteger.ONE, BigInteger.ONE);

    /**
     * A rational number representing two (2/1).
     */
    public static final BigRational TWO = new BigRational(BigInteger.TWO, BigInteger.ONE);

    /**
     * A rational number representing negative one (-1/1).
     */
    public static final BigRational MINUS_ONE = new BigRational(BigInteger.ONE.negate(), BigInteger.ONE);

    /**
     * A rational number representing one half (1/2).
     */
    public static final BigRational ONE_HALF = new BigRational(BigInteger.ONE, BigInteger.TWO);

    /**
     * A rational number representing one third (1/3).
     */
    public static final BigRational ONE_THIRD = new BigRational(BigInteger.ONE, BigInteger.valueOf(3));

    /**
     * A rational number representing one quarter (1/4).
     */
    public static final BigRational ONE_QUARTER = new BigRational(BigInteger.ONE, BigInteger.valueOf(4));

    /**
     * A rational number representing one fifth (1/5).
     */
    public static final BigRational ONE_FIFTH = new BigRational(BigInteger.ONE, BigInteger.valueOf(5));

    /**
     * A rational number representing two thirds (2/3).
     */
    public static final BigRational TWO_THIRDS = new BigRational(BigInteger.TWO, BigInteger.valueOf(3));

    /**
     * A rational number representing two quarters (1/2, normalized).
     */
    public static final BigRational TWO_QUARTERS = ONE_HALF;

    /**
     * A rational number representing two fifths (2/5).
     */
    public static final BigRational TWO_FIFTHS = new BigRational(BigInteger.TWO, BigInteger.valueOf(5));

    /**
     * A rational number representing three quarters (3/4).
     */
    public static final BigRational THREE_QUARTERS = new BigRational(BigInteger.valueOf(3), BigInteger.valueOf(4));

    /**
     * A rational number representing three fifths (3/5).
     */
    public static final BigRational THREE_FIFTHS = new BigRational(BigInteger.valueOf(3), BigInteger.valueOf(5));

    /**
     * A rational number representing four fifths (4/5).
     */
    public static final BigRational FOUR_FIFTHS = new BigRational(BigInteger.valueOf(4), BigInteger.valueOf(5));

    /**
     * BigInteger representation of 100, used for percentage calculations.
     */
    private static final BigInteger ONE_HUNDRED = BigInteger.valueOf(100);

    // ==================== Fields ====================

    /**
     * The numerator of this rational number (can be negative, zero, or positive).
     */
    private final BigInteger numerator;

    /**
     * The denominator of this rational number (always positive and non-zero).
     */
    private final BigInteger denominator;

    // ==================== Private Constructor ====================

    /**
     * Private constructor that creates a rational number from the given numerator and denominator.
     * The rational is automatically reduced to lowest terms and normalized (sign in numerator).
     *
     * @param numerator   the numerator
     * @param denominator the denominator (must not be zero)
     */
    private BigRational(BigInteger numerator, BigInteger denominator) {
        Objects.requireNonNull(numerator, "Numerator cannot be null");
        Objects.requireNonNull(denominator, "Denominator cannot be null");

        if (denominator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Denominator cannot be zero");
        }

        // Special case for zero
        if (numerator.equals(BigInteger.ZERO)) {
            this.numerator = BigInteger.ZERO;
            this.denominator = BigInteger.ONE;
            return;
        }

        // Reduce to lowest terms
        BigInteger gcd = numerator.gcd(denominator);
        BigInteger reducedNum = numerator.divide(gcd);
        BigInteger reducedDen = denominator.divide(gcd);

        // Normalize sign (move to numerator)
        if (reducedDen.signum() < 0) {
            reducedNum = reducedNum.negate();
            reducedDen = reducedDen.negate();
        }

        this.numerator = reducedNum;
        this.denominator = reducedDen;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a rational number from the given integer value.
     * <p>
     * This is equivalent to {@code value / 1}.
     * </p>
     *
     * @param value the integer value
     * @return a rational number representing the integer
     */
    public static BigRational of(int value) {
        return new BigRational(BigInteger.valueOf(value), BigInteger.ONE);
    }

    /**
     * Creates a rational number from the given long value.
     * <p>
     * This is equivalent to {@code value / 1}.
     * </p>
     *
     * @param value the long value
     * @return a rational number representing the long
     */
    public static BigRational of(long value) {
        return new BigRational(BigInteger.valueOf(value), BigInteger.ONE);
    }

    /**
     * Creates a rational number from the given BigInteger value.
     * <p>
     * This is equivalent to {@code value / 1}.
     * </p>
     *
     * @param value the BigInteger value
     * @return a rational number representing the BigInteger
     * @throws NullPointerException if value is null
     */
    public static BigRational of(BigInteger value) {
        Objects.requireNonNull(value, "Value cannot be null");
        return new BigRational(value, BigInteger.ONE);
    }

    /**
     * Creates a rational number from the given numerator and denominator.
     * <p>
     * The rational is automatically reduced to lowest terms. For example,
     * {@code of(2, 4)} returns {@code 1/2}.
     * </p>
     *
     * @param numerator   the numerator
     * @param denominator the denominator
     * @return a rational number in lowest terms
     * @throws ArithmeticException if denominator is zero
     */
    public static BigRational of(int numerator, int denominator) {
        return new BigRational(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    /**
     * Creates a rational number from the given numerator and denominator.
     * <p>
     * The rational is automatically reduced to lowest terms. For example,
     * {@code of(2L, 4L)} returns {@code 1/2}.
     * </p>
     *
     * @param numerator   the numerator
     * @param denominator the denominator
     * @return a rational number in lowest terms
     * @throws ArithmeticException if denominator is zero
     */
    public static BigRational of(long numerator, long denominator) {
        return new BigRational(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    /**
     * Creates a rational number from the given numerator and denominator.
     * <p>
     * The rational is automatically reduced to lowest terms. For example,
     * {@code of(BigInteger.valueOf(2), BigInteger.valueOf(4))} returns {@code 1/2}.
     * </p>
     *
     * @param numerator   the numerator
     * @param denominator the denominator
     * @return a rational number in lowest terms
     * @throws NullPointerException if numerator or denominator is null
     * @throws ArithmeticException  if denominator is zero
     */
    public static BigRational of(BigInteger numerator, BigInteger denominator) {
        return new BigRational(numerator, denominator);
    }

    /**
     * Creates a rational number from a string representation.
     * <p>
     * The string is parsed as a decimal number and converted to a rational using
     * exact double-to-rational conversion.
     * </p>
     *
     * @param value the string representation of the number (e.g., "3.14159")
     * @return a rational number representing the string value
     * @throws NumberFormatException if the string is not a valid number
     * @throws NullPointerException  if value is null
     */
    public static BigRational of(String value) {
        Objects.requireNonNull(value, "Value cannot be null");
        return of(Double.parseDouble(value));
    }

    /**
     * Creates a rational number from a double value using exact conversion.
     * <p>
     * This method converts the double value exactly by analyzing its IEEE 754
     * bit representation. The conversion is exact but may produce unexpected
     * fractions for decimal values that cannot be represented exactly in binary.
     * </p>
     * <p>
     * For example, {@code of(0.1)} produces {@code 3602879701896397/36028797018963968}
     * rather than {@code 1/10}, because 0.1 cannot be represented exactly as a
     * double. To get simpler fractions, use {@link #of(double, double, int)} with
     * an epsilon tolerance.
     * </p>
     *
     * @param value the double value to convert
     * @return a rational number representing the exact double value
     * @throws IllegalArgumentException if value is NaN or infinite
     */
    public static BigRational of(double value) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Cannot convert NaN to rational");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException("Cannot convert infinity to rational");
        }

        // Exact conversion using IEEE 754 representation: value = m * 2^k
        long bits = Double.doubleToLongBits(value);
        long sign = bits & 0x8000000000000000L;
        long exponent = bits & 0x7ff0000000000000L;
        long mantissa = bits & 0x000fffffffffffffL;

        // Add implicit leading bit for normalized numbers
        if (exponent != 0) {
            mantissa |= 0x0010000000000000L;
        }

        // Apply sign
        if (sign != 0) {
            mantissa = -mantissa;
        }

        // Calculate exponent (biased by 1075 = 1023 + 52)
        int k = ((int) (exponent >> 52)) - 1075;

        // Simplify by removing trailing zeros from mantissa
        while ((mantissa & 0x1) == 0 && mantissa != 0) {
            mantissa >>= 1;
            k++;
        }

        // Construct fraction based on exponent sign
        if (k < 0) {
            // Negative exponent: m / 2^(-k)
            return new BigRational(BigInteger.valueOf(mantissa), BigInteger.ONE.shiftLeft(-k));
        } else {
            // Non-negative exponent: m * 2^k / 1
            return new BigRational(BigInteger.valueOf(mantissa).shiftLeft(k), BigInteger.ONE);
        }
    }

    /**
     * Creates a rational number from a double value with the specified maximum denominator.
     * <p>
     * This method uses a continued fraction algorithm to find the best rational
     * approximation with a denominator no larger than the specified maximum.
     * This is useful for converting decimal values to simple fractions.
     * </p>
     * <p>
     * For example, {@code of(Math.PI, 100)} returns {@code 22/7}.
     * </p>
     *
     * @param value          the double value to approximate
     * @param maxDenominator the maximum allowed denominator (must be positive)
     * @return the best rational approximation within the denominator constraint
     * @throws IllegalArgumentException if value is NaN or infinite, or if maxDenominator is not positive
     */
    public static BigRational of(double value, int maxDenominator) {
        if (maxDenominator <= 0) {
            throw new IllegalArgumentException("Maximum denominator must be positive, got: " + maxDenominator);
        }
        return approximateContinuedFraction(value, 0.0, maxDenominator, 100);
    }

    /**
     * Creates a rational number from a double value with the specified error tolerance.
     * <p>
     * This method uses a continued fraction algorithm to find a rational approximation
     * within the specified epsilon of the target value. The algorithm terminates when
     * either the approximation is within epsilon or the maximum iterations are reached.
     * </p>
     * <p>
     * For example, {@code of(Math.PI, 1e-6, 100)} finds a fraction within 0.000001 of π.
     * </p>
     *
     * @param value         the double value to approximate
     * @param epsilon       the maximum allowed error (must be non-negative)
     * @param maxIterations the maximum number of convergents to compute (must be positive)
     * @return a rational approximation within epsilon of the value
     * @throws IllegalArgumentException if value is NaN or infinite, epsilon is negative, or maxIterations is not positive
     */
    public static BigRational of(double value, double epsilon, int maxIterations) {
        if (epsilon < 0) {
            throw new IllegalArgumentException("Epsilon must be non-negative, got: " + epsilon);
        }
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Maximum iterations must be positive, got: " + maxIterations);
        }
        return approximateContinuedFraction(value, epsilon, Integer.MAX_VALUE, maxIterations);
    }

    /**
     * Internal method for continued fraction approximation.
     * Uses the continued fraction algorithm to approximate a double value as a rational.
     *
     * @param value          the value to approximate
     * @param epsilon        the error tolerance
     * @param maxDenominator the maximum denominator
     * @param maxIterations  the maximum iterations
     * @return the rational approximation
     */
    private static BigRational approximateContinuedFraction(double value, double epsilon,
                                                            int maxDenominator, int maxIterations) {
        if (Double.isNaN(value)) {
            throw new IllegalArgumentException("Cannot convert NaN to rational");
        }
        if (Double.isInfinite(value)) {
            throw new IllegalArgumentException("Cannot convert infinity to rational");
        }

        long overflow = Integer.MAX_VALUE;
        double r0 = value;
        long a0 = (long) Math.floor(r0);

        if (Math.abs(a0) > overflow) {
            throw new ArithmeticException("Value too large to convert: " + value);
        }

        // Check for near-integer values
        if (Math.abs(a0 - value) < epsilon) {
            return new BigRational(BigInteger.valueOf(a0), BigInteger.ONE);
        }

        // Continued fraction convergents
        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        for (int n = 0; n < maxIterations; n++) {
            double r1 = 1.0 / (r0 - a0);
            long a1 = (long) Math.floor(r1);

            long p2 = a1 * p1 + p0;
            long q2 = a1 * q1 + q0;

            if (p2 > overflow || q2 > overflow) {
                // Overflow - return previous convergent
                return new BigRational(BigInteger.valueOf(p1), BigInteger.valueOf(q1));
            }

            double convergent = (double) p2 / (double) q2;

            // Check termination conditions
            if (Math.abs(convergent - value) <= epsilon || q2 >= maxDenominator) {
                if (q2 < maxDenominator) {
                    return new BigRational(BigInteger.valueOf(p2), BigInteger.valueOf(q2));
                } else {
                    return new BigRational(BigInteger.valueOf(p1), BigInteger.valueOf(q1));
                }
            }

            // Update for next iteration
            p0 = p1;
            p1 = p2;
            q0 = q1;
            q1 = q2;
            a0 = a1;
            r0 = r1;
        }

        throw new ArithmeticException("Failed to converge after " + maxIterations + " iterations");
    }

    // ==================== Accessors ====================

    /**
     * Returns the numerator of this rational number.
     * <p>
     * The numerator may be negative, zero, or positive. The fraction is always
     * in lowest terms, so the GCD of the numerator and denominator is 1.
     * </p>
     *
     * @return the numerator
     */
    public BigInteger getNumerator() {
        return numerator;
    }

    /**
     * Returns the denominator of this rational number.
     * <p>
     * The denominator is always positive and non-zero. The fraction is always
     * in lowest terms, so the GCD of the numerator and denominator is 1.
     * </p>
     *
     * @return the denominator (always positive)
     */
    public BigInteger getDenominator() {
        return denominator;
    }

    // ==================== Arithmetic Operations ====================

    /**
     * Returns a rational whose value is {@code (this + value)}.
     *
     * @param value the value to add
     * @return {@code this + value}
     * @throws NullPointerException if value is null
     */
    public BigRational add(BigRational value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value.equals(ZERO)) {
            return this;
        }
        if (this.equals(ZERO)) {
            return value;
        }

        // Optimize for same denominator
        if (denominator.equals(value.denominator)) {
            return new BigRational(numerator.add(value.numerator), denominator);
        }

        // General case: a/b + c/d = (ad + bc) / bd
        BigInteger newNum = numerator.multiply(value.denominator)
                .add(value.numerator.multiply(denominator));
        BigInteger newDen = denominator.multiply(value.denominator);

        return new BigRational(newNum, newDen);
    }

    /**
     * Returns a rational whose value is {@code (this + value)}.
     *
     * @param value the integer value to add
     * @return {@code this + value}
     */
    public BigRational add(int value) {
        return add(BigInteger.valueOf(value));
    }

    /**
     * Returns a rational whose value is {@code (this + value)}.
     *
     * @param value the long value to add
     * @return {@code this + value}
     */
    public BigRational add(long value) {
        return add(BigInteger.valueOf(value));
    }

    /**
     * Returns a rational whose value is {@code (this + value)}.
     *
     * @param value the BigInteger value to add
     * @return {@code this + value}
     * @throws NullPointerException if value is null
     */
    public BigRational add(BigInteger value) {
        Objects.requireNonNull(value, "Value cannot be null");
        return new BigRational(numerator.add(denominator.multiply(value)), denominator);
    }

    /**
     * Returns a rational whose value is {@code (this - value)}.
     *
     * @param value the value to subtract
     * @return {@code this - value}
     * @throws NullPointerException if value is null
     */
    public BigRational subtract(BigRational value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value.equals(ZERO)) {
            return this;
        }

        // Optimize for same denominator
        if (denominator.equals(value.denominator)) {
            return new BigRational(numerator.subtract(value.numerator), denominator);
        }

        // General case: a/b - c/d = (ad - bc) / bd
        BigInteger newNum = numerator.multiply(value.denominator)
                .subtract(value.numerator.multiply(denominator));
        BigInteger newDen = denominator.multiply(value.denominator);

        return new BigRational(newNum, newDen);
    }

    /**
     * Returns a rational whose value is {@code (this - value)}.
     *
     * @param value the integer value to subtract
     * @return {@code this - value}
     */
    public BigRational subtract(int value) {
        return subtract(BigInteger.valueOf(value));
    }

    /**
     * Returns a rational whose value is {@code (this - value)}.
     *
     * @param value the long value to subtract
     * @return {@code this - value}
     */
    public BigRational subtract(long value) {
        return subtract(BigInteger.valueOf(value));
    }

    /**
     * Returns a rational whose value is {@code (this - value)}.
     *
     * @param value the BigInteger value to subtract
     * @return {@code this - value}
     * @throws NullPointerException if value is null
     */
    public BigRational subtract(BigInteger value) {
        Objects.requireNonNull(value, "Value cannot be null");
        return new BigRational(numerator.subtract(denominator.multiply(value)), denominator);
    }

    /**
     * Returns a rational whose value is {@code (this * value)}.
     *
     * @param value the value to multiply by
     * @return {@code this * value}
     * @throws NullPointerException if value is null
     */
    public BigRational multiply(BigRational value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (this.equals(ZERO) || value.equals(ZERO)) {
            return ZERO;
        }
        if (this.equals(ONE)) {
            return value;
        }
        if (value.equals(ONE)) {
            return this;
        }

        // a/b * c/d = ac / bd
        return new BigRational(
                numerator.multiply(value.numerator),
                denominator.multiply(value.denominator));
    }

    /**
     * Returns a rational whose value is {@code (this * value)}.
     *
     * @param value the integer value to multiply by
     * @return {@code this * value}
     */
    public BigRational multiply(int value) {
        return multiply(BigInteger.valueOf(value));
    }

    /**
     * Returns a rational whose value is {@code (this * value)}.
     *
     * @param value the long value to multiply by
     * @return {@code this * value}
     */
    public BigRational multiply(long value) {
        return multiply(BigInteger.valueOf(value));
    }

    /**
     * Returns a rational whose value is {@code (this * value)}.
     *
     * @param value the BigInteger value to multiply by
     * @return {@code this * value}
     * @throws NullPointerException if value is null
     */
    public BigRational multiply(BigInteger value) {
        Objects.requireNonNull(value, "Value cannot be null");
        return new BigRational(numerator.multiply(value), denominator);
    }

    /**
     * Returns a rational whose value is {@code (this / value)}.
     *
     * @param value the value to divide by
     * @return {@code this / value}
     * @throws NullPointerException if value is null
     * @throws ArithmeticException  if value is zero
     */
    public BigRational divide(BigRational value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value.numerator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Division by zero");
        }

        return multiply(value.reciprocal());
    }

    /**
     * Returns a rational whose value is {@code (this / value)}.
     *
     * @param value the integer value to divide by
     * @return {@code this / value}
     * @throws ArithmeticException if value is zero
     */
    public BigRational divide(int value) {
        return divide(BigInteger.valueOf(value));
    }

    /**
     * Returns a rational whose value is {@code (this / value)}.
     *
     * @param value the long value to divide by
     * @return {@code this / value}
     * @throws ArithmeticException if value is zero
     */
    public BigRational divide(long value) {
        return divide(BigInteger.valueOf(value));
    }

    /**
     * Returns a rational whose value is {@code (this / value)}.
     *
     * @param value the BigInteger value to divide by
     * @return {@code this / value}
     * @throws NullPointerException if value is null
     * @throws ArithmeticException  if value is zero
     */
    public BigRational divide(BigInteger value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Division by zero");
        }

        return new BigRational(numerator, denominator.multiply(value));
    }

    /**
     * Returns a rational whose value is {@code this}<sup>{@code exponent}</sup>.
     * <p>
     * For negative exponents, this returns the reciprocal raised to the absolute value.
     * For example, {@code (1/2).pow(-2)} returns {@code 4/1}.
     * </p>
     *
     * @param exponent the exponent
     * @return {@code this}<sup>{@code exponent}</sup>
     * @throws ArithmeticException if this is zero and exponent is negative
     */
    public BigRational pow(int exponent) {
        if (exponent == 0) {
            return ONE;
        }
        if (exponent == 1) {
            return this;
        }
        if (this.equals(ZERO)) {
            if (exponent < 0) {
                throw new ArithmeticException("Cannot raise zero to negative power");
            }
            return ZERO;
        }

        if (exponent < 0) {
            return new BigRational(
                    denominator.pow(-exponent),
                    numerator.pow(-exponent));
        }

        return new BigRational(
                numerator.pow(exponent),
                denominator.pow(exponent));
    }

    /**
     * Returns a rational whose value is {@code this}<sup>{@code exponent}</sup>.
     * <p>
     * For negative exponents, this returns the reciprocal raised to the absolute value.
     * </p>
     *
     * @param exponent the exponent
     * @return {@code this}<sup>{@code exponent}</sup>
     * @throws ArithmeticException if this is zero and exponent is negative
     */
    public BigRational pow(long exponent) {
        if (exponent == 0) {
            return ONE;
        }
        if (exponent == 1) {
            return this;
        }
        if (this.equals(ZERO)) {
            if (exponent < 0) {
                throw new ArithmeticException("Cannot raise zero to negative power");
            }
            return ZERO;
        }

        if (exponent < 0) {
            return new BigRational(
                    powBigInteger(denominator, -exponent),
                    powBigInteger(numerator, -exponent));
        }

        return new BigRational(
                powBigInteger(numerator, exponent),
                powBigInteger(denominator, exponent));
    }

    /**
     * Returns a rational whose value is {@code this}<sup>{@code exponent}</sup>.
     * <p>
     * For negative exponents, this returns the reciprocal raised to the absolute value.
     * </p>
     *
     * @param exponent the exponent
     * @return {@code this}<sup>{@code exponent}</sup>
     * @throws NullPointerException if exponent is null
     * @throws ArithmeticException  if this is zero and exponent is negative
     */
    public BigRational pow(BigInteger exponent) {
        Objects.requireNonNull(exponent, "Exponent cannot be null");

        if (exponent.equals(BigInteger.ZERO)) {
            return ONE;
        }
        if (exponent.equals(BigInteger.ONE)) {
            return this;
        }
        if (this.equals(ZERO)) {
            if (exponent.signum() < 0) {
                throw new ArithmeticException("Cannot raise zero to negative power");
            }
            return ZERO;
        }

        if (exponent.signum() < 0) {
            BigInteger absExponent = exponent.negate();
            return new BigRational(
                    powBigInteger(denominator, absExponent),
                    powBigInteger(numerator, absExponent));
        }

        return new BigRational(
                powBigInteger(numerator, exponent),
                powBigInteger(denominator, exponent));
    }

    /**
     * Returns an approximation of {@code this}<sup>{@code exponent}</sup> as a double.
     * <p>
     * This method is useful for non-integer exponents where an exact rational result
     * is not possible.
     * </p>
     *
     * @param exponent the exponent
     * @return {@code this}<sup>{@code exponent}</sup> as a double
     */
    public double pow(double exponent) {
        return Math.pow(doubleValue(), exponent);
    }

    /**
     * Helper method to compute {@code base}<sup>{@code exponent}</sup> using binary exponentiation.
     *
     * @param base     the base
     * @param exponent the exponent (must be non-negative)
     * @return base raised to the exponent
     */
    private static BigInteger powBigInteger(BigInteger base, long exponent) {
        if (exponent < 0) {
            throw new IllegalArgumentException("Exponent must be non-negative");
        }

        BigInteger result = BigInteger.ONE;
        BigInteger k2p = base;

        while (exponent != 0) {
            if ((exponent & 1) != 0) {
                result = result.multiply(k2p);
            }
            k2p = k2p.multiply(k2p);
            exponent >>= 1;
        }

        return result;
    }

    /**
     * Helper method to compute {@code base}<sup>{@code exponent}</sup> using binary exponentiation.
     *
     * @param base     the base
     * @param exponent the exponent (must be non-negative)
     * @return base raised to the exponent
     */
    private static BigInteger powBigInteger(BigInteger base, BigInteger exponent) {
        if (exponent.signum() < 0) {
            throw new IllegalArgumentException("Exponent must be non-negative");
        }

        BigInteger result = BigInteger.ONE;
        BigInteger k2p = base;
        BigInteger exp = exponent;

        while (!exp.equals(BigInteger.ZERO)) {
            if (exp.testBit(0)) {
                result = result.multiply(k2p);
            }
            k2p = k2p.multiply(k2p);
            exp = exp.shiftRight(1);
        }

        return result;
    }

    // ==================== Other Operations ====================

    /**
     * Returns true if this rational simplifies to an Integer value
     *
     * @return true if this rational is an Integer
     */
    public boolean isInteger() {
        if (denominator.equals(BigInteger.ONE)) {
            return true;
        }
        BigInteger gcd = numerator.gcd(denominator);
        return denominator.divide(gcd).equals(BigInteger.ONE);
    }

    /**
     * Returns the absolute value of this rational number.
     *
     * @return {@code |this|}
     */
    public BigRational abs() {
        return numerator.signum() >= 0 ? this : negate();
    }

    /**
     * Returns the negation of this rational number.
     *
     * @return {@code -this}
     */
    public BigRational negate() {
        return new BigRational(numerator.negate(), denominator);
    }

    /**
     * Returns the reciprocal of this rational number.
     * <p>
     * For example, the reciprocal of {@code 3/4} is {@code 4/3}.
     * </p>
     *
     * @return {@code 1 / this}
     * @throws ArithmeticException if this is zero
     */
    public BigRational reciprocal() {
        if (numerator.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Cannot take reciprocal of zero");
        }
        return new BigRational(denominator, numerator);
    }

    /**
     * Returns this rational reduced to lowest terms.
     * <p>
     * Note: All {@code BigRational} instances are automatically kept in lowest terms,
     * so this method simply returns {@code this}. It is provided for compatibility.
     * </p>
     *
     * @return this rational (already in lowest terms)
     */
    public BigRational reduce() {
        return this;
    }

    /**
     * Returns the signum function of this rational number.
     *
     * @return -1, 0, or 1 as this rational is negative, zero, or positive
     */
    public int signum() {
        return numerator.signum();
    }

    // ==================== Conversions ====================

    /**
     * Converts this rational to a percentage value.
     * <p>
     * This is equivalent to {@code this.multiply(100).doubleValue()}.
     * For example, {@code 1/4} returns {@code 25.0}.
     * </p>
     *
     * @return this rational as a percentage (0-100 scale)
     */
    public double percentageValue() {
        return multiply(ONE_HUNDRED).doubleValue();
    }

    @Override
    public int intValue() {
        return numerator.divide(denominator).intValue();
    }

    @Override
    public long longValue() {
        return numerator.divide(denominator).longValue();
    }

    @Override
    public float floatValue() {
        // Handle potential overflow
        float result = numerator.floatValue() / denominator.floatValue();

        if (Float.isNaN(result)) {
            int shift = Math.max(numerator.bitLength(), denominator.bitLength())
                    - Math.getExponent(Float.MAX_VALUE);
            result = numerator.shiftRight(shift).floatValue()
                    / denominator.shiftRight(shift).floatValue();
        }

        return result;
    }

    @Override
    public double doubleValue() {
        // Handle potential overflow
        double result = numerator.doubleValue() / denominator.doubleValue();

        if (Double.isNaN(result)) {
            int shift = Math.max(numerator.bitLength(), denominator.bitLength())
                    - Math.getExponent(Double.MAX_VALUE);
            result = numerator.shiftRight(shift).doubleValue()
                    / denominator.shiftRight(shift).doubleValue();
        }

        return result;
    }

    // ==================== Comparison ====================

    @Override
    public int compareTo(BigRational other) {
        Objects.requireNonNull(other, "Cannot compare to null");

        // Optimize for same denominator
        if (denominator.equals(other.denominator)) {
            return numerator.compareTo(other.numerator);
        }

        // Cross multiply: a/b <=> c/d iff ad <=> bc
        BigInteger lhs = numerator.multiply(other.denominator);
        BigInteger rhs = other.numerator.multiply(denominator);

        return lhs.compareTo(rhs);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BigRational other)) {
            return false;
        }

        // Since both are always in lowest terms, direct comparison works
        return numerator.equals(other.numerator) &&
                denominator.equals(other.denominator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    @Override
    public String toString() {
        if (denominator.equals(BigInteger.ONE)) {
            return numerator.toString();
        }
        if (numerator.equals(BigInteger.ZERO)) {
            return "0";
        }
        return numerator + "/" + denominator;
    }
}
