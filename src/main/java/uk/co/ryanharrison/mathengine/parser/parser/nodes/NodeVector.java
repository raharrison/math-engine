package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Node representing a one-dimensional vector (array) of values.
 * Vectors support element-wise operations and can contain any type of node.
 */
public final class NodeVector extends NodeConstant {

    private Node[] elements;

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
        throw new UnsupportedOperationException("Cannot convert vector to double");
    }

    /**
     * Pad this vector to the specified size by adding zero elements.
     * Modifies this vector in place.
     */
    public void padToSize(int newSize) {
        if (newSize <= elements.length) {
            return;
        }

        Node[] newElements = new Node[newSize];
        System.arraycopy(elements, 0, newElements, 0, elements.length);

        // Fill remaining with zero
        for (int i = elements.length; i < newSize; i++) {
            newElements[i] = new NodeRational(0, 1);
        }

        elements = newElements;
    }

    /**
     * Convert to a list for iteration.
     */
    public List<NodeConstant> toList() {
        List<NodeConstant> list = new ArrayList<>(elements.length);
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
    public String toString() {
        return "{" + String.join(", ", Arrays.stream(elements)
                .map(Object::toString)
                .toArray(String[]::new)) + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeVector)) return false;
        NodeVector other = (NodeVector) obj;
        return Arrays.equals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(elements);
    }
}
