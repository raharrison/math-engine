package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeString;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;

/**
 * Operations that read the same on a vector and on a string, so {@code len},
 * {@code reverse}, {@code repeat}, {@code contains} and {@code indexof} each need
 * one implementation rather than one per type.
 * <p>
 * A vector is a sequence of values; a string is a sequence of characters.
 * Anything else is a sequence of one.
 */
public final class Sequences {

    private Sequences() {
    }

    public static int length(NodeConstant value) {
        return switch (value) {
            case NodeVector vector -> vector.size();
            case NodeString text -> text.getValue().length();
            default -> 1;
        };
    }

    public static NodeConstant reverse(NodeConstant value) {
        return switch (value) {
            case NodeVector vector -> {
                var reversed = new Node[vector.size()];
                for (int i = 0; i < reversed.length; i++) {
                    reversed[i] = vector.getElement(reversed.length - 1 - i);
                }
                yield new NodeVector(reversed);
            }
            case NodeString text -> new NodeString(new StringBuilder(text.getValue()).reverse().toString());
            default -> throw new TypeError("Cannot reverse a " + value.typeName());
        };
    }

    /**
     * A negative count is treated as zero, giving an empty result.
     */
    public static NodeConstant repeat(NodeConstant value, int count) {
        int times = Math.max(count, 0);
        if (value instanceof NodeString text) {
            return new NodeString(text.getValue().repeat(times));
        }

        int unit = value instanceof NodeVector vector ? vector.size() : 1;
        var result = new Node[unit * times];
        for (int i = 0; i < result.length; i++) {
            result[i] = value instanceof NodeVector vector ? vector.getElement(i % unit) : value;
        }
        return new NodeVector(result);
    }

    /**
     * The first position of {@code target} at or after {@code from}, or -1.
     * Vector elements are matched by value, strings by substring.
     */
    public static int indexOf(NodeConstant sequence, NodeConstant target, int from) {
        if (sequence instanceof NodeString text) {
            return text.getValue().indexOf(TypeCoercion.toDisplayString(target), Math.max(from, 0));
        }
        if (sequence instanceof NodeVector vector) {
            for (int i = Math.max(from, 0); i < vector.size(); i++) {
                if (((NodeConstant) vector.getElement(i)).equalTo(target)) {
                    return i;
                }
            }
            return -1;
        }
        throw new TypeError("Cannot search a " + sequence.typeName());
    }

    public static int lastIndexOf(NodeConstant sequence, NodeConstant target) {
        if (sequence instanceof NodeString text) {
            return text.getValue().lastIndexOf(TypeCoercion.toDisplayString(target));
        }
        if (sequence instanceof NodeVector vector) {
            for (int i = vector.size() - 1; i >= 0; i--) {
                if (((NodeConstant) vector.getElement(i)).equalTo(target)) {
                    return i;
                }
            }
            return -1;
        }
        throw new TypeError("Cannot search a " + sequence.typeName());
    }

    public static boolean contains(NodeConstant sequence, NodeConstant target) {
        return indexOf(sequence, target, 0) >= 0;
    }
}
