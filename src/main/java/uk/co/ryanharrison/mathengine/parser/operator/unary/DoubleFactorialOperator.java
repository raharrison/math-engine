package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;

/**
 * Double factorial operator (!!).
 * <p>
 * Computes n!! = n * (n-2) * (n-4) * ... * 1 or 2
 * <p>
 * For odd n: n!! = n * (n-2) * ... * 3 * 1
 * For even n: n!! = n * (n-2) * ... * 4 * 2
 * <p>
 * Domain: Non-negative integers only.
 */
public final class DoubleFactorialOperator implements UnaryOperator {

    /**
     * Singleton instance
     */
    public static final DoubleFactorialOperator INSTANCE = new DoubleFactorialOperator();

    private DoubleFactorialOperator() {
    }

    @Override
    public String symbol() {
        return "!!";
    }

    @Override
    public String displayName() {
        return "double factorial";
    }

    @Override
    public Position position() {
        return Position.POSTFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        int n = (int) ctx.toNumber(operand).doubleValue();

        if (n < 0) {
            throw new IllegalArgumentException("Double factorial is not defined for negative numbers: " + n);
        }

        long result = 1;
        for (int i = n; i > 0; i -= 2) {
            result *= i;
        }

        return new NodeRational(result);
    }
}
