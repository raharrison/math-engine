package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeString;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

/**
 * Addition operator (+).
 * <p>
 * Supports:
 * <ul>
 *     <li>Scalar addition with type promotion</li>
 *     <li>Vector element-wise addition with broadcasting</li>
 *     <li>Matrix element-wise addition with broadcasting</li>
 *     <li>Broadcasting: {@code {1,2,3} + 10} → {@code {11, 12, 13}}</li>
 *     <li>Percent arithmetic: {@code number + percent%} computes {@code number + (percent% of number)}</li>
 * </ul>
 * <p>
 * Percent arithmetic examples:
 * <ul>
 *     <li>{@code 100 + 10%} = {@code 100 + (10% of 100)} = {@code 110}</li>
 *     <li>{@code 10% + 20%} = {@code 30%}</li>
 *     <li>{@code 10% + 5} = {@code 0.1 + 5} = {@code 5.1}</li>
 * </ul>
 */
public final class PlusOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final PlusOperator INSTANCE = new PlusOperator();

    private PlusOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return BroadcastingEngine.applyBinary(left, right, (l, r) -> {
            if (l instanceof NodeString || r instanceof NodeString) {
                return l.add(r);
            }
            return ctx.applyAdditive(l, r, BigRational::add, Double::sum, true);
        });
    }
}
