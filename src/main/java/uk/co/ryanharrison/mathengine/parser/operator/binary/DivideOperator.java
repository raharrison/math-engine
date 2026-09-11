package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;

/**
 * Division ({@code /}), element-wise over collections.
 * <p>
 * Stays exact while both sides are rational. Division by zero falls back to
 * IEEE 754 semantics, giving infinity or NaN rather than an error.
 * Dividing like units cancels them, leaving a plain ratio.
 */
public final class DivideOperator implements BinaryOperator {

    public static final DivideOperator INSTANCE = new DivideOperator();

    private DivideOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return left.divide(right);
    }
}
