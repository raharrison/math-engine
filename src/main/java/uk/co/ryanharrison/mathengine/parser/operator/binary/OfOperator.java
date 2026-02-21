package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;

/**
 * Percentage "of" operator.
 * <p>
 * Used for percent calculations: "50% of 200" = 100
 * The left operand is a percent (inherently double), right operand is the base value.
 * Since the left side is always a percent (double-valued), the result is always double.
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
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return BroadcastingEngine.applyBinary(left, right, (l, r) ->
                new NodeDouble(NumericOperations.toDoubleValue(l) * NumericOperations.toDoubleValue(r)));
    }
}
