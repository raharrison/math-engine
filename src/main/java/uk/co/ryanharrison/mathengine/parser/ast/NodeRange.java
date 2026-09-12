package uk.co.ryanharrison.mathengine.parser.ast;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Node representing a lazy range of numbers (start..end or start..end step increment).
 * Ranges can be iterated without materializing all elements, making them memory-efficient
 * for large ranges.
 * <p>
 * When all three bounds are exact, the elements are exact too, and each one is computed
 * from its index rather than by adding the step repeatedly. That is what keeps
 * {@code 0..1 step 0.1} a run of tenths instead of reaching 0.30000000000000004 at the
 * third element and stopping short of 1. A bound that is already a double keeps the whole
 * range on double arithmetic, because there is no exact value left to preserve.
 */
public final class NodeRange extends NodeConstant {

    /**
     * The largest magnitude a bound may have before long stepping could overflow.
     */
    private static final BigInteger HALF_OF_LONG = BigInteger.valueOf(Long.MAX_VALUE / 2);

    private final NodeNumber start;
    private final NodeNumber end;
    private final NodeNumber step;

    /**
     * The bounds as exact values, each null when that bound is not exact.
     */
    private final BigRational exactStart;
    private final BigRational exactEnd;
    private final BigRational exactStep;

    private NodeVector cachedVector;

    public NodeRange(NodeNumber start, NodeNumber end, NodeNumber step) {
        this.start = start;
        this.end = end;
        this.step = step != null ? step : new NodeRational(1, 1);
        this.exactStart = exactValue(this.start);
        this.exactEnd = exactValue(this.end);
        this.exactStep = exactValue(this.step);
    }

    public NodeNumber getStart() {
        return start;
    }

    public NodeNumber getEnd() {
        return end;
    }

    public NodeNumber getStep() {
        return step;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public double doubleValue() {
        throw new TypeError("Cannot use a range as a number");
    }

    private static BigRational exactValue(NodeNumber number) {
        return number instanceof NodeRational rational ? rational.getValue() : null;
    }

    /**
     * Whether every bound is exact, so the elements can be computed without rounding.
     */
    private boolean isExact() {
        return exactStart != null && exactEnd != null && exactStep != null;
    }

    /**
     * Estimates the number of elements this range will produce.
     * This is computed without materializing the elements, making it suitable for size validation.
     * <p>
     * For an exact range it is not an estimate: it is the count the iterator will produce.
     *
     * @return the estimated number of elements in the range
     */
    public long estimateSize() {
        if (isExact()) {
            return exactSize();
        }

        double startVal = start.doubleValue();
        double endVal = end.doubleValue();
        double stepVal = step.doubleValue();

        if (stepVal == 0) {
            return Long.MAX_VALUE; // Infinite range
        }

        if (stepVal > 0) {
            if (startVal > endVal) {
                return 0;
            }
            return (long) Math.floor((endVal - startVal) / stepVal) + 1;
        } else {
            if (startVal < endVal) {
                return 0;
            }
            return (long) Math.floor((startVal - endVal) / (-stepVal)) + 1;
        }
    }

    /**
     * The element count of an exact range, counted in exact arithmetic so that it agrees
     * with the elements the iterator goes on to produce.
     */
    private long exactSize() {
        if (exactStep.signum() == 0) {
            return Long.MAX_VALUE; // Infinite range
        }

        BigRational span = exactEnd.subtract(exactStart);
        if (span.signum() != 0 && span.signum() != exactStep.signum()) {
            return 0;
        }

        // Both signs agree, so the quotient is positive and truncation is a floor
        BigRational steps = span.divide(exactStep);
        BigInteger count = steps.getNumerator().divide(steps.getDenominator()).add(BigInteger.ONE);
        return count.bitLength() < Long.SIZE ? count.longValue() : Long.MAX_VALUE;
    }

    /**
     * Convert the range to a vector, materializing all elements.
     * This is cached so subsequent calls return the same vector.
     */
    public NodeVector toVector() {
        if (cachedVector == null) {
            cachedVector = computeVector();
        }
        return cachedVector;
    }

    private NodeVector computeVector() {
        List<Node> elements = new ArrayList<>();
        Iterator<NodeConstant> iterator = iterator();
        while (iterator.hasNext()) {
            elements.add(iterator.next());
        }
        return new NodeVector(elements);
    }

    /**
     * Get an iterator for lazy iteration over the range.
     */
    public Iterator<NodeConstant> iterator() {
        if (step.doubleValue() == 0) {
            throw new IllegalArgumentException("Step cannot be zero");
        }
        return isExact() ? new ExactRangeIterator() : new DoubleRangeIterator();
    }

    /**
     * Walks an exact range by index, so no rounding error can build up along the way.
     * <p>
     * An integer range steps in {@code long}, which is the common case and is much cheaper
     * than rational arithmetic. Anything else steps in {@link BigRational}.
     */
    private final class ExactRangeIterator implements Iterator<NodeConstant> {

        private final long size = exactSize();
        private final boolean stepsInLong = stepsInLong();
        private final long longStart = stepsInLong ? exactStart.getNumerator().longValue() : 0;
        private final long longStep = stepsInLong ? exactStep.getNumerator().longValue() : 0;

        private long index;

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public NodeConstant next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            long i = index++;
            if (stepsInLong) {
                return new NodeRational(longStart + i * longStep, 1);
            }
            return new NodeRational(exactStart.add(exactStep.multiply(BigRational.of(i))));
        }
    }

    /**
     * Whether the elements can be counted out in {@code long}: a whole start and step, and
     * bounds small enough that neither the multiplication nor the addition can overflow.
     */
    private boolean stepsInLong() {
        return exactStart.isInteger() && exactStep.isInteger()
                && withinHalfOfLong(exactStart) && withinHalfOfLong(exactEnd);
    }

    /**
     * Whether a value is at most half of {@link Long#MAX_VALUE}, which bounds both the
     * distance between the ends and every element between them.
     */
    private static boolean withinHalfOfLong(BigRational value) {
        return value.getNumerator().abs()
                .compareTo(HALF_OF_LONG.multiply(value.getDenominator().abs())) <= 0;
    }

    /**
     * Walks a range that has a double for one of its bounds, where the step has to be
     * added as it goes because there is no exact value to multiply up from.
     */
    private final class DoubleRangeIterator implements Iterator<NodeConstant> {

        private final double endVal = end.doubleValue();
        private final double stepVal = step.doubleValue();
        private double current = start.doubleValue();

        @Override
        public boolean hasNext() {
            return stepVal > 0 ? current <= endVal : current >= endVal;
        }

        @Override
        public NodeConstant next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            NodeConstant result = createNumber(current);
            current += stepVal;
            return result;
        }
    }

    private NodeNumber createNumber(double value) {
        // If the value is an integer and within range, create a rational
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return new NodeRational((long) value, 1);
        }
        return new NodeDouble(value);
    }

    @Override
    public String typeName() {
        return "range";
    }

    @Override
    public String toString() {
        if (step.doubleValue() == 1.0) {
            return String.format("%s..%s", start, end);
        }
        return String.format("%s..%s step %s", start, end, step);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeRange other)) return false;
        return start.equals(other.start) && end.equals(other.end) && step.equals(other.step);
    }

    @Override
    public int hashCode() {
        return start.hashCode() * 31 + end.hashCode() * 17 + step.hashCode();
    }
}
