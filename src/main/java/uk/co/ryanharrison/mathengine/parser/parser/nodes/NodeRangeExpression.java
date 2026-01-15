package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing a range expression before evaluation.
 * This is distinct from NodeRange which holds evaluated constant numbers.
 * <p>
 * Examples: 1..10, 1..10 step 2, x..y step z
 */
public final class NodeRangeExpression extends NodeExpression {

    private final Node start;
    private final Node end;
    private final Node step;  // May be null if no step specified

    public NodeRangeExpression(Node start, Node end, Node step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public Node getStep() {
        return step;
    }

    public boolean hasStep() {
        return step != null;
    }

    @Override
    public String toString() {
        if (step != null) {
            return String.format("%s..%s step %s", start, end, step);
        }
        return String.format("%s..%s", start, end);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeRangeExpression)) return false;
        NodeRangeExpression other = (NodeRangeExpression) obj;

        if (!start.equals(other.start)) return false;
        if (!end.equals(other.end)) return false;
        return step == null ? other.step == null : step.equals(other.step);
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + (step != null ? step.hashCode() : 0);
        return result;
    }
}
