package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;

/**
 * Unary minus, element-wise over collections.
 */
public final class NegateOperator implements UnaryOperator {

    public static final NegateOperator INSTANCE = new NegateOperator();

    private NegateOperator() {
    }

    @Override
    public Position position() {
        return Position.PREFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        return operand.negate();
    }
}
