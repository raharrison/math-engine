package uk.co.ryanharrison.mathengine.parser.ast;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.core.BigRational;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the two things a range promises: that its count agrees with the elements it goes
 * on to produce, and that exact bounds are stepped exactly rather than accumulated in
 * double, which used to turn a tenth into 0.30000000000000004 by the third element.
 */
class NodeRangeTest {

    private static NodeRational rational(String value) {
        return new NodeRational(BigRational.of(new BigDecimal(value)));
    }

    private static NodeRange range(String start, String end, String step) {
        return new NodeRange(rational(start), rational(end), rational(step));
    }

    private static List<NodeConstant> elementsOf(NodeRange range) {
        var elements = new ArrayList<NodeConstant>();
        Iterator<NodeConstant> iterator = range.iterator();
        while (iterator.hasNext()) {
            elements.add(iterator.next());
        }
        return elements;
    }

    // ==================== Size ====================

    @ParameterizedTest
    @CsvSource({
            "0, 1, 0.1, 11",        // start, end, step, element count
            "0, 0.3, 0.1, 4",
            "1, 10, 1, 10",
            "5, 1, -1, 5",
            "5, 5, 1, 1",
            "2, 1, 1, 0",
            "0, 1, 0.25, 5"
    })
    void sizeAgreesWithTheElementsProduced(String start, String end, String step, int expected) {
        NodeRange range = range(start, end, step);

        assertThat(range.estimateSize()).isEqualTo(expected);
        assertThat(elementsOf(range)).hasSize(expected);
        assertThat(range.toVector().size()).isEqualTo(expected);
    }

    @Test
    void zeroStepCountsAsUnbounded() {
        NodeRange range = range("1", "5", "0");

        assertThat(range.estimateSize()).isEqualTo(Long.MAX_VALUE);
        assertThatThrownBy(range::iterator)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Step cannot be zero");
    }

    // ==================== Exact stepping ====================

    @Test
    void fractionalStepStaysExact() {
        List<NodeConstant> elements = elementsOf(range("0", "1", "0.1"));

        assertThat(elements.get(3)).isEqualTo(new NodeRational(BigRational.of(3, 10)));
        assertThat(elements.get(8)).isEqualTo(new NodeRational(BigRational.of(4, 5)));
    }

    @Test
    void exactStepReachesTheEnd() {
        List<NodeConstant> elements = elementsOf(range("0", "1", "0.1"));

        assertThat(elements.getLast()).isEqualTo(new NodeRational(1));
    }

    @Test
    void stepWithNoDecimalFormStaysExact() {
        NodeRange thirds = new NodeRange(new NodeRational(0), new NodeRational(1),
                new NodeRational(BigRational.of(1, 3)));

        assertThat(elementsOf(thirds)).containsExactly(
                new NodeRational(BigRational.of(0, 1)),
                new NodeRational(BigRational.of(1, 3)),
                new NodeRational(BigRational.of(2, 3)),
                new NodeRational(1));
    }

    @Test
    void integerRangeProducesRationals() {
        assertThat(elementsOf(range("1", "4", "1")))
                .containsExactly(new NodeRational(1), new NodeRational(2),
                        new NodeRational(3), new NodeRational(4));
    }

    @Test
    void boundBeyondLongStaysExact() {
        BigRational huge = BigRational.of(new BigDecimal("100000000000000000000"));
        NodeRange range = new NodeRange(new NodeRational(huge),
                new NodeRational(huge.add(BigRational.of(2))), new NodeRational(1));

        assertThat(elementsOf(range)).containsExactly(
                new NodeRational(huge),
                new NodeRational(huge.add(BigRational.of(1))),
                new NodeRational(huge.add(BigRational.of(2))));
    }

    // ==================== Double bounds ====================

    @Test
    void aDoubleBoundKeepsTheRangeOnDoubleArithmetic() {
        NodeRange range = new NodeRange(new NodeRational(0), new NodeDouble(1.0), rational("0.1"));

        List<NodeConstant> elements = elementsOf(range);

        assertThat(elements).hasSize(11);
        assertThat(elements.get(3)).isInstanceOf(NodeDouble.class);
        assertThat(((NodeDouble) elements.get(3)).doubleValue()).isNotEqualTo(0.3);
    }

    // ==================== Materialisation ====================

    @Test
    void toVectorIsCached() {
        NodeRange range = range("1", "5", "1");

        assertThat(range.toVector()).isSameAs(range.toVector());
    }

    @Test
    void iteratorRefusesToRunPastTheEnd() {
        Iterator<NodeConstant> iterator = range("1", "2", "1").iterator();
        iterator.next();
        iterator.next();

        assertThat(iterator.hasNext()).isFalse();
        assertThatThrownBy(iterator::next).isInstanceOf(java.util.NoSuchElementException.class);
    }
}
