package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import java.util.Arrays;
import java.util.List;

/**
 * Node representing a two-dimensional matrix of values.
 * Matrices support both element-wise and true matrix operations.
 */
public final class NodeMatrix extends NodeConstant {

    private final Node[][] elements;

    public NodeMatrix(Node[][] elements) {
        // Deep clone the 2D array
        this.elements = new Node[elements.length][];
        for (int i = 0; i < elements.length; i++) {
            this.elements[i] = elements[i].clone();
        }
    }

    public NodeMatrix(List<List<Node>> rows) {
        this.elements = new Node[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            List<Node> row = rows.get(i);
            this.elements[i] = row.toArray(new Node[0]);
        }
    }

    public Node[][] getElements() {
        // Deep clone for return
        Node[][] copy = new Node[elements.length][];
        for (int i = 0; i < elements.length; i++) {
            copy[i] = elements[i].clone();
        }
        return copy;
    }

    public Node getElement(int row, int col) {
        return elements[row][col];
    }

    public int getRows() {
        return elements.length;
    }

    public int getCols() {
        return elements.length > 0 ? elements[0].length : 0;
    }

    /**
     * Get rows as a list of lists (useful for testing and iteration).
     */
    public java.util.List<java.util.List<Node>> getRowsList() {
        java.util.List<java.util.List<Node>> rows = new java.util.ArrayList<>();
        for (Node[] row : elements) {
            rows.add(java.util.Arrays.asList(row));
        }
        return rows;
    }

    @Override
    public boolean isMatrix() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException("Cannot convert matrix to double");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append("[");
            for (int j = 0; j < elements[i].length; j++) {
                if (j > 0) sb.append(", ");
                sb.append(elements[i][j].toString());
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeMatrix)) return false;
        NodeMatrix other = (NodeMatrix) obj;
        return Arrays.deepEquals(elements, other.elements);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(elements);
    }
}
