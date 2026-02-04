package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Logical NOT operator (not, !).
 * <p>
 * Returns the logical negation of a boolean value.
 * Numbers are truthy if non-zero.
 */
public final class NotOperator implements UnaryOperator {

    /**
     * Singleton instance
     */
    public static final NotOperator INSTANCE = new NotOperator();

    private NotOperator() {
    }

    @Override
    public Position position() {
        return Position.PREFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        boolean value = ctx.toBoolean(operand);
        return NodeBoolean.of(!value);
    }
}
