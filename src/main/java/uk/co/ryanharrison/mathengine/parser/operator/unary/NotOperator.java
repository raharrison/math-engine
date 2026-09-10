package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

/**
 * Logical negation ({@code not}, {@code !}). Numbers count as true when non-zero.
 */
public final class NotOperator implements UnaryOperator {

    public static final NotOperator INSTANCE = new NotOperator();

    private NotOperator() {
    }

    @Override
    public Position position() {
        return Position.PREFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        return NodeBoolean.of(!TypeCoercion.toBoolean(operand));
    }
}
