package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Unary plus operator (+).
 * <p>
 * A no-op that returns the operand unchanged. Supports all node types
 * including vectors and matrices.
 */
public final class UnaryPlusOperator implements UnaryOperator {

    /**
     * Singleton instance
     */
    public static final UnaryPlusOperator INSTANCE = new UnaryPlusOperator();

    private UnaryPlusOperator() {
    }

    @Override
    public Position position() {
        return Position.PREFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        // Unary plus is a no-op, return operand unchanged
        return operand;
    }
}
