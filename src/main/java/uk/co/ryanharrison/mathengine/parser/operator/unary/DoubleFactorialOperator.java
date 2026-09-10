package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

/**
 * Double factorial ({@code n!!}): the product of every other integer down from n,
 * so {@code 7!!} is 7*5*3*1. Element-wise over collections.
 */
public final class DoubleFactorialOperator implements UnaryOperator {

    public static final DoubleFactorialOperator INSTANCE = new DoubleFactorialOperator();

    private DoubleFactorialOperator() {
    }

    @Override
    public Position position() {
        return Position.POSTFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        return BroadcastingEngine.applyUnary(operand, value -> {
            int n = (int) TypeCoercion.toDouble(value);
            if (n < 0) {
                throw new DomainException("Double factorial is not defined for negative numbers: " + n);
            }
            long result = 1;
            for (int i = n; i > 0; i -= 2) {
                result *= i;
            }
            return new NodeRational(result);
        });
    }
}
