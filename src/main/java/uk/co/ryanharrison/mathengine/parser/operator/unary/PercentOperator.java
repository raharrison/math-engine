package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeNumber;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodePercent;

/**
 * Percent operator (%).
 * <p>
 * Converts a number to a percentage (divides by 100).
 * For example, 50% = 0.5
 * <p>
 * Can be used with the "of" operator: 50% of 200 = 100
 */
public final class PercentOperator implements UnaryOperator {

    /**
     * Singleton instance
     */
    public static final PercentOperator INSTANCE = new PercentOperator();

    private PercentOperator() {
    }

    @Override
    public Position position() {
        return Position.POSTFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        NodeNumber num = ctx.toNumber(operand);
        return new NodePercent(num.doubleValue());
    }
}
