package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

/**
 * Multiplication operator (*).
 * <p>
 * Supports:
 * <ul>
 *     <li>Scalar multiplication with type promotion</li>
 *     <li>Vector element-wise multiplication with broadcasting</li>
 *     <li>Matrix element-wise multiplication with broadcasting</li>
 *     <li>Broadcasting: {@code {1,2,3} * 5} → {@code {5, 10, 15}}</li>
 *     <li>Percent arithmetic: {@code percent% * percent%} → {@code percent%}</li>
 *     <li>Unit arithmetic: {@code 5 * meters} → {@code 5 meters}, {@code 10 meters * 2} → {@code 20 meters}</li>
 * </ul>
 * <p>
 * Percent arithmetic examples:
 * <ul>
 *     <li>{@code 10% * 20%} = {@code 0.1 * 0.2} = {@code 2%}</li>
 *     <li>{@code 10% * 5} = {@code 0.1 * 5} = {@code 0.5}</li>
 *     <li>{@code 5 * 10%} = {@code 5 * 0.1} = {@code 0.5}</li>
 * </ul>
 * <p>
 * Note: For true matrix multiplication, use the {@link AtOperator} (@).
 */
public final class MultiplyOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final MultiplyOperator INSTANCE = new MultiplyOperator();

    private MultiplyOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return BroadcastingEngine.applyBinary(left, right, (l, r) ->
                ctx.applyMultiplicative(l, r, BigRational::multiply, (a, b) -> a * b, true)
        );
    }
}
