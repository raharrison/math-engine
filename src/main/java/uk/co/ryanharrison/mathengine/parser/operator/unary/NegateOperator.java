package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Unary negation operator (-).
 * <p>
 * Negates a numeric value. Supports broadcasting over vectors and matrices.
 */
public final class NegateOperator implements UnaryOperator {

    /**
     * Singleton instance
     */
    public static final NegateOperator INSTANCE = new NegateOperator();

    private NegateOperator() {
    }

    @Override
    public String symbol() {
        return "-";
    }

    @Override
    public String displayName() {
        return "negation";
    }

    @Override
    public Position position() {
        return Position.PREFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        return ctx.dispatchUnary(operand, BigRational::negate, v -> -v);
    }
}
