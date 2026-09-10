package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Unary plus, which leaves its operand alone.
 */
public final class UnaryPlusOperator implements UnaryOperator {

    public static final UnaryPlusOperator INSTANCE = new UnaryPlusOperator();

    private UnaryPlusOperator() {
    }

    @Override
    public Position position() {
        return Position.PREFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        return operand;
    }
}
