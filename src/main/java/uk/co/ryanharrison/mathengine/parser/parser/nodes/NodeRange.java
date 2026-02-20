package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Node representing a lazy range of numbers (start..end or start..end step increment).
 * Ranges can be iterated without materializing all elements, making them memory-efficient
 * for large ranges.
 */
public final class NodeRange extends NodeConstant {

    private final NodeNumber start;
    private final NodeNumber end;
    private final NodeNumber step;
    private NodeVector cachedVector;

    public NodeRange(NodeNumber start, NodeNumber end, NodeNumber step) {
        this.start = start;
        this.end = end;
        this.step = step != null ? step : new NodeRational(1, 1);
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
        throw new UnsupportedOperationException("Cannot convert range to double");
    }

    /**
     * Estimates the number of elements this range will produce.
     * This is computed without materializing the elements, making it suitable for size validation.
     *
     * @return the estimated number of elements in the range
     */
    public long estimateSize() {
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

        double current = start.doubleValue();
        double endVal = end.doubleValue();
        double stepVal = step.doubleValue();

        if (stepVal == 0) {
            throw new IllegalArgumentException("Step cannot be zero");
        }

        if (stepVal > 0) {
            while (current <= endVal) {
                elements.add(createNumber(current));
                current += stepVal;
            }
        } else {
            while (current >= endVal) {
                elements.add(createNumber(current));
                current += stepVal;
            }
        }

        return new NodeVector(elements);
    }

    /**
     * Get an iterator for lazy iteration over the range.
     */
    public Iterator<NodeConstant> iterator() {
        return new RangeIterator();
    }

    private class RangeIterator implements Iterator<NodeConstant> {
        private double current = start.doubleValue();
        private final double endVal = end.doubleValue();
        private final double stepVal = step.doubleValue();

        @Override
        public boolean hasNext() {
            if (stepVal > 0) {
                return current <= endVal;
            } else if (stepVal < 0) {
                return current >= endVal;
            } else {
                throw new IllegalArgumentException("Step cannot be zero");
            }
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

    // ==================== Universal Arithmetic ====================

    @Override
    public NodeConstant add(NodeConstant other) {
        return toVector().add(other);
    }

    @Override
    public NodeConstant subtract(NodeConstant other) {
        return toVector().subtract(other);
    }

    @Override
    public NodeConstant multiply(NodeConstant other) {
        return toVector().multiply(other);
    }

    @Override
    public NodeConstant divide(NodeConstant other) {
        return toVector().divide(other);
    }

    @Override
    public NodeConstant power(NodeConstant other) {
        return toVector().power(other);
    }

    @Override
    public NodeConstant negate() {
        return toVector().negate();
    }

    @Override
    public int compareTo(NodeConstant other) {
        throw new TypeError("Cannot compare ranges");
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
