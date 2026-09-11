package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;

/**
 * Subtraction ({@code -}).
 * <p>
 * Subtracts like units, reads {@code 100 - 10%} as "100 less 10% of 100", and
 * works element-wise over vectors and matrices.
 */
public final class MinusOperator implements BinaryOperator {

    public static final MinusOperator INSTANCE = new MinusOperator();

    private MinusOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return left.subtract(right);
    }
}
