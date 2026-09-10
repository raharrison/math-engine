package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodePercent;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

/**
 * The {@code of} operator, which takes a share of a quantity: {@code 50% of 200} is 100.
 * <p>
 * It is multiplication with the percentage spent, so it is implemented as multiplication
 * and cannot drift away from {@code *}. Everything multiplication supports it supports:
 * {@code 50% of 100 meters} is 50 meters, {@code 2 of 3} is an exact 6, and a collection
 * on either side broadcasts. The one difference is the result type, because a share of
 * something is a plain amount rather than another percentage.
 */
public final class OfOperator implements BinaryOperator {

    public static final OfOperator INSTANCE = new OfOperator();

    private OfOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return BroadcastingEngine.applyBinary(left, right,
                (fraction, value) -> spendPercentage(fraction.multiply(value)));
    }

    private static NodeConstant spendPercentage(NodeConstant product) {
        return product instanceof NodePercent percent
                ? new NodeDouble(percent.getValue())
                : product;
    }
}
