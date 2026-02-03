package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeNumber;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

/**
 * Percentage "of" operator.
 * <p>
 * Used for percent calculations: "50% of 200" = 100
 * The left operand should be a percent, right operand is the base value.
 * Supports broadcasting: "50% of {1, 2, 3}" = {0.5, 1.0, 1.5}
 */
public final class OfOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final OfOperator INSTANCE = new OfOperator();

    private OfOperator() {
    }

    @Override
    public String symbol() {
        return "of";
    }

    @Override
    public String displayName() {
        return "percent of";
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        // Use broadcasting to support vectors/matrices
        return BroadcastingEngine.applyBinary(left, right, (l, r) -> {
            NodeNumber leftNum = ctx.toNumber(l);
            NodeNumber rightNum = ctx.toNumber(r);

            // Multiply: percent (as decimal) * value
            double result = leftNum.doubleValue() * rightNum.doubleValue();
            return new NodeDouble(result);
        });
    }
}
