package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeString;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

import java.math.BigInteger;

/**
 * Division operator (/).
 * <p>
 * Supports:
 * <ul>
 *     <li>Rational division (preserves exactness when possible)</li>
 *     <li>Vector element-wise division with broadcasting</li>
 *     <li>Matrix element-wise division with broadcasting</li>
 *     <li>Broadcasting: {@code {10,20,30} / 2} → {@code {5, 10, 15}}</li>
 *     <li>Percent arithmetic: {@code percent% / percent%} → {@code number} (ratio)</li>
 * </ul>
 * <p>
 * Division by zero follows IEEE 754 semantics:
 * <ul>
 *     <li>x / 0 = +infinity (for x > 0)</li>
 *     <li>x / 0 = -infinity (for x < 0)</li>
 *     <li>0 / 0 = NaN</li>
 * </ul>
 * <p>
 * Percent arithmetic examples:
 * <ul>
 *     <li>{@code 20% / 10%} = {@code 0.2 / 0.1} = {@code 2} (ratio, not percent)</li>
 *     <li>{@code 10% / 2} = {@code 5%}</li>
 *     <li>{@code 10 / 10%} = {@code 10 / 0.1} = {@code 100}</li>
 * </ul>
 */
public final class DivideOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final DivideOperator INSTANCE = new DivideOperator();

    private DivideOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return BroadcastingEngine.applyBinary(left, right, (l, r) -> {
            if (l instanceof NodeString || r instanceof NodeString) {
                throw new TypeError("Cannot divide strings");
            }
            return ctx.applyMultiplicative(l, r, this::divideRationals, this::divideDoubles, false);
        });
    }

    /**
     * Divides two BigRationals with division by zero handling.
     */
    private BigRational divideRationals(BigRational left, BigRational right) {
        if (right.getNumerator().equals(BigInteger.ZERO)) {
            // Division by zero - throw to trigger fallback to double
            throw new ArithmeticException("Division by zero");
        }
        return left.divide(right);
    }

    /**
     * Divides two doubles (handles infinity/NaN per IEEE 754).
     */
    private double divideDoubles(double left, double right) {
        return left / right;  // IEEE 754 handles division by zero correctly
    }
}
