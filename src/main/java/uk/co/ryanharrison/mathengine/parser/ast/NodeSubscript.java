package uk.co.ryanharrison.mathengine.parser.ast;

import java.util.List;
import java.util.Objects;

/**
 * Node representing subscript/indexing operations.
 * Supports both single indexing (v[0]) and slicing (v[1:3]).
 */
public final class NodeSubscript extends NodeExpression {

    private final Node target;
    private final List<SliceArg> indices;

    public NodeSubscript(Node target, List<SliceArg> indices) {
        this.target = target;
        this.indices = List.copyOf(indices);
    }

    public Node getTarget() {
        return target;
    }

    public List<SliceArg> getIndices() {
        return indices;
    }

    @Override
    public String typeName() {
        return "subscript";
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(target).append("[");
        for (int i = 0; i < indices.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(indices.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeSubscript other)) return false;
        return target.equals(other.target) && indices.equals(other.indices);
    }

    @Override
    public int hashCode() {
        return target.hashCode() * 31 + indices.hashCode();
    }

    /**
     * Represents a single index or slice argument in a subscript operation.
     */
    public static final class SliceArg {
        private final Node start;
        private final Node end;
        private final boolean isSlice;  // True if colon was present

        public SliceArg(Node start, Node end) {
            this(start, end, end != null || start == null);
        }

        public SliceArg(Node start, Node end, boolean isSlice) {
            this.start = start;
            this.end = end;
            this.isSlice = isSlice;
        }

        public Node getStart() {
            return start;
        }

        public Node getEnd() {
            return end;
        }

        public boolean isRange() {
            return isSlice;
        }

        @Override
        public String toString() {
            if (start == null && end == null) {
                return ":";
            } else if (start == null) {
                return ":" + end;
            } else if (end == null) {
                return start + ":";
            } else if (!isRange()) {
                return start.toString();
            } else {
                return start + ":" + end;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SliceArg other)) return false;
            return isSlice == other.isSlice
                    && Objects.equals(start, other.start)
                    && Objects.equals(end, other.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end, isSlice);
        }
    }
}
