package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.BroadcastingDispatcher;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Subtraction operator (-).
 * <p>
 * Supports:
 * <ul>
 *     <li>Scalar subtraction with type promotion</li>
 *     <li>Vector element-wise subtraction with broadcasting</li>
 *     <li>Matrix element-wise subtraction with broadcasting</li>
 *     <li>Broadcasting: {@code {10,20,30} - 5} → {@code {5, 15, 25}}</li>
 *     <li>Percent arithmetic: {@code number - percent%} computes {@code number - (percent% of number)}</li>
 * </ul>
 * <p>
 * Percent arithmetic examples:
 * <ul>
 *     <li>{@code 100 - 10%} = {@code 100 - (10% of 100)} = {@code 90}</li>
 *     <li>{@code 30% - 10%} = {@code 20%}</li>
 *     <li>{@code 10% - 5} = {@code 0.1 - 5} = {@code -4.9}</li>
 * </ul>
 */
public final class MinusOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final MinusOperator INSTANCE = new MinusOperator();

    private MinusOperator() {
    }

    @Override
    public String symbol() {
        return "-";
    }

    @Override
    public String displayName() {
        return "subtraction";
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return BroadcastingDispatcher.dispatch(left, right, ctx, (l, r) ->
                ctx.applyAdditive(l, r, BigRational::subtract, (a, b) -> a - b, false)
        );
    }
}
