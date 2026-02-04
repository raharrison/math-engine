package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;
import uk.co.ryanharrison.mathengine.utils.MathUtils;

/**
 * Factorial operator (!).
 * <p>
 * Computes n! = n * (n-1) * (n-2) * ... * 1
 * <p>
 * Domain: Non-negative integers only.
 */
public final class FactorialOperator implements UnaryOperator {

    /**
     * Singleton instance
     */
    public static final FactorialOperator INSTANCE = new FactorialOperator();

    private FactorialOperator() {
    }

    @Override
    public Position position() {
        return Position.POSTFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        double d = ctx.toNumber(operand).doubleValue();
        if (Math.floor(d) == d) {
            return new NodeRational(MathUtils.factorial((long) d));
        }
        return new NodeDouble(MathUtils.factorial(d));
    }
}
