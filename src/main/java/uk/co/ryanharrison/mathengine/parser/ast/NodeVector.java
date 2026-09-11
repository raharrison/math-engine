package uk.co.ryanharrison.mathengine.parser.ast;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A one-dimensional vector. Arithmetic broadcasts element-wise; see {@link NodeArithmetic}.
 */
public final class NodeVector extends NodeConstant {

    private final Node[] elements;

    public NodeVector(Node[] elements) {
        this.elements = elements.clone();
    }

    public NodeVector(List<Node> elements) {
        this.elements = elements.toArray(new Node[0]);
    }

    public Node[] getElements() {
        return elements.clone();
    }

    public Node getElement(int index) {
        return elements[index];
    }

    public int size() {
        return elements.length;
    }

    @Override
    public boolean isVector() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public double doubleValue() {
        throw new TypeError("Cannot use a vector as a number");
    }

    /**
     * The elements as values, for iteration.
     */
    public List<NodeConstant> toList() {
        var list = new ArrayList<NodeConstant>(elements.length);
        for (Node element : elements) {
            if (element instanceof NodeConstant) {
                list.add((NodeConstant) element);
            } else {
                throw new IllegalStateException("Vector contains unevaluated expression");
            }
        }
        return list;
    }

    @Override
    public String typeName() {
        return "vector";
    }

    @Override
    public String toString() {
        return "{" + String.join(", ", Arrays.stream(elements)
                .map(Object::toString)
                .toArray(String[]::new)) + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeVector other)) return false;
        return Arrays.equals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(elements);
    }
}
