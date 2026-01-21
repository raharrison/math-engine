package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Node representing a list comprehension ({ expr for var in iterable if? condition }).
 * Comprehensions generate vectors by iterating over an iterable and evaluating an expression.
 * Supports nested iterations: {x*y for x in 1..3 for y in 1..3}
 */
public final class NodeComprehension extends NodeExpression {

    /**
     * Represents a single iterator in a comprehension (variable name and iterable).
     */
    public static final class Iterator {
        private final String variable;
        private final Node iterable;

        public Iterator(String variable, Node iterable) {
            this.variable = variable;
            this.iterable = iterable;
        }

        public String getVariable() {
            return variable;
        }

        public Node getIterable() {
            return iterable;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Iterator)) return false;
            Iterator other = (Iterator) obj;
            return variable.equals(other.variable) && iterable.equals(other.iterable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variable, iterable);
        }

        @Override
        public String toString() {
            return variable + " in " + iterable;
        }
    }

    private final Node expression;
    private final List<Iterator> iterators;
    private final Node condition;

    public NodeComprehension(Node expression, String variable, Node iterable, Node condition) {
        this.expression = expression;
        this.iterators = Collections.singletonList(new Iterator(variable, iterable));
        this.condition = condition;
    }

    public NodeComprehension(Node expression, List<Iterator> iterators, Node condition) {
        this.expression = expression;
        this.iterators = new ArrayList<>(iterators);
        this.condition = condition;
    }

    public Node getExpression() {
        return expression;
    }

    public String getVariable() {
        return iterators.get(0).variable;
    }

    public Node getIterable() {
        return iterators.get(0).iterable;
    }

    public List<Iterator> getIterators() {
        return Collections.unmodifiableList(iterators);
    }

    public Node getCondition() {
        return condition;
    }

    public boolean hasCondition() {
        return condition != null;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("{").append(expression);
        for (Iterator iter : iterators) {
            sb.append(" for ").append(iter);
        }
        if (condition != null) {
            sb.append(" if ").append(condition);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeComprehension)) return false;
        NodeComprehension other = (NodeComprehension) obj;
        boolean condEq = (condition == null) ? (other.condition == null) : condition.equals(other.condition);
        return expression.equals(other.expression) && iterators.equals(other.iterators) && condEq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, iterators, condition);
    }
}
