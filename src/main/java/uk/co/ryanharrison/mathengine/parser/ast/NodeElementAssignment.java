package uk.co.ryanharrison.mathengine.parser.ast;

import java.util.List;

/**
 * Node representing a write into one element of a collection: {@code v[0] := 5},
 * {@code m[1, 2] := 9}, {@code m[0][1] := 9}, {@code s[0] := "H"}.
 * <p>
 * Each {@code [...]} group is held separately, so {@code m[0][1]} and {@code m[0, 1]}
 * stay distinguishable.
 *
 * @see NodeAssignment
 * @see NodeSubscript
 */
public final class NodeElementAssignment extends NodeExpression {

    private final String identifier;
    private final List<List<NodeSubscript.SliceArg>> indexGroups;
    private final Node value;

    public NodeElementAssignment(String identifier, List<List<NodeSubscript.SliceArg>> indexGroups, Node value) {
        this.identifier = identifier;
        this.indexGroups = indexGroups.stream().map(List::copyOf).toList();
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * The {@code [...]} groups, in the order they were written.
     */
    public List<List<NodeSubscript.SliceArg>> getIndexGroups() {
        return indexGroups;
    }

    public Node getValue() {
        return value;
    }

    @Override
    public String typeName() {
        return "element assignment";
    }

    @Override
    public String toString() {
        var sb = new StringBuilder(identifier);
        for (List<NodeSubscript.SliceArg> group : indexGroups) {
            sb.append("[");
            for (int i = 0; i < group.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(group.get(i));
            }
            sb.append("]");
        }
        return sb.append(" := ").append(value).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeElementAssignment other)) return false;
        return identifier.equals(other.identifier)
                && indexGroups.equals(other.indexGroups)
                && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        int hash = identifier.hashCode();
        hash = hash * 31 + indexGroups.hashCode();
        return hash * 31 + value.hashCode();
    }
}
