package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Modulo ({@code mod}), using floor semantics rather than Java's remainder:
 * {@code -7 mod 3} is 2, not -1. Element-wise over collections.
 */
public final class ModOperator implements BinaryOperator {

    public static final ModOperator INSTANCE = new ModOperator();

    private ModOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return left.modulo(right);
    }
}
