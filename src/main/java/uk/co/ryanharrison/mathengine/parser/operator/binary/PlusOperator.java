package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;

/**
 * Addition ({@code +}).
 * <p>
 * Also concatenates strings, adds like units, and reads {@code 100 + 10%} as
 * "100 plus 10% of 100". Vectors and matrices add element-wise.
 */
public final class PlusOperator implements BinaryOperator {

    public static final PlusOperator INSTANCE = new PlusOperator();

    private PlusOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return left.add(right);
    }
}
