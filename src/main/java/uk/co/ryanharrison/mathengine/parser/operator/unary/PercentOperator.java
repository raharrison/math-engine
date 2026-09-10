package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodePercent;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

/**
 * Postfix percent ({@code 50%}), reading the number as hundredths.
 * Pairs with the {@code of} operator: {@code 50% of 200} is 100.
 */
public final class PercentOperator implements UnaryOperator {

    public static final PercentOperator INSTANCE = new PercentOperator();

    private PercentOperator() {
    }

    @Override
    public Position position() {
        return Position.POSTFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        return BroadcastingEngine.applyUnary(operand, value -> new NodePercent(TypeCoercion.toDouble(value)));
    }
}
