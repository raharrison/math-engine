package uk.co.ryanharrison.mathengine.parser.util;

import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A vector and a string are both sequences, so {@code len}, {@code reverse} and the
 * search operations share one implementation rather than needing {@code str}-prefixed twins.
 */
class SequencesTest {

    private static NodeVector vector(int... values) {
        Node[] elements = new Node[values.length];
        for (int i = 0; i < values.length; i++) {
            elements[i] = new NodeRational(values[i]);
        }
        return new NodeVector(elements);
    }

    @Test
    void lengthCountsElementsOrCharacters() {
        assertThat(Sequences.length(vector(1, 2, 3))).isEqualTo(3);
        assertThat(Sequences.length(new NodeString("hello"))).isEqualTo(5);
        assertThat(Sequences.length(new NodeRational(7))).isEqualTo(1);
    }

    @Test
    void reverseHandlesBothKinds() {
        assertThat(Sequences.reverse(new NodeString("abc"))).isEqualTo(new NodeString("cba"));
        assertThat(Sequences.reverse(vector(1, 2, 3))).isEqualTo(vector(3, 2, 1));
    }

    @Test
    void reverseRejectsAScalar() {
        assertThatThrownBy(() -> Sequences.reverse(new NodeRational(1)))
                .isInstanceOf(TypeError.class);
    }

    @Test
    void repeatHandlesBothKinds() {
        assertThat(Sequences.repeat(new NodeString("ab"), 3)).isEqualTo(new NodeString("ababab"));
        assertThat(Sequences.repeat(vector(1, 2), 2)).isEqualTo(vector(1, 2, 1, 2));
        assertThat(Sequences.repeat(new NodeRational(5), 3)).isEqualTo(vector(5, 5, 5));
    }

    @Test
    void aNegativeRepeatCountGivesAnEmptyResult() {
        assertThat(Sequences.length(Sequences.repeat(vector(1, 2), -1))).isZero();
        assertThat(Sequences.repeat(new NodeString("ab"), -1)).isEqualTo(new NodeString(""));
    }

    @Test
    void indexOfMatchesElementsByValueAndStringsBySubstring() {
        assertThat(Sequences.indexOf(vector(10, 20, 30), new NodeDouble(20), 0)).isEqualTo(1);
        assertThat(Sequences.indexOf(vector(10, 20, 30), new NodeRational(99), 0)).isEqualTo(-1);
        assertThat(Sequences.indexOf(new NodeString("banana"), new NodeString("na"), 0)).isEqualTo(2);
        assertThat(Sequences.indexOf(new NodeString("banana"), new NodeString("na"), 3)).isEqualTo(4);
    }

    @Test
    void lastIndexOfSearchesFromTheEnd() {
        assertThat(Sequences.lastIndexOf(vector(1, 2, 1), new NodeRational(1))).isEqualTo(2);
        assertThat(Sequences.lastIndexOf(new NodeString("banana"), new NodeString("na"))).isEqualTo(4);
    }

    @Test
    void containsFollowsIndexOf() {
        assertThat(Sequences.contains(vector(1, 2, 3), new NodeRational(2))).isTrue();
        assertThat(Sequences.contains(vector(1, 2, 3), new NodeRational(4))).isFalse();
        assertThat(Sequences.contains(new NodeString("hello"), new NodeString("ell"))).isTrue();
    }
}
