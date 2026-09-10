package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Multiplication ({@code *}), element-wise over collections.
 * <p>
 * Scales units and percentages, and repeats strings ({@code "ab" * 3}).
 * For true matrix multiplication use {@link AtOperator}.
 */
public final class MultiplyOperator implements BinaryOperator {

    public static final MultiplyOperator INSTANCE = new MultiplyOperator();

    private MultiplyOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return left.multiply(right);
    }
}
