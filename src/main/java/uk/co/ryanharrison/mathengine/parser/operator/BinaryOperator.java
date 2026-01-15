package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Interface for binary operators that take two operands.
 * <p>
 * Binary operators include arithmetic (+, -, *, /, ^), comparison (<, >, ==),
 * and logical (and, or, xor) operators.
 *
 * <h2>Implementation Example:</h2>
 * <pre>{@code
 * public class PlusOperator implements BinaryOperator {
 *
 *     @Override
 *     public String symbol() { return "+"; }
 *
 *     @Override
 *     public String displayName() { return "addition"; }
 *
 *     @Override
 *     public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
 *         // Use BroadcastingDispatcher for element-wise operations with full broadcasting
 *         return BroadcastingDispatcher.dispatch(left, right, ctx, (l, r) -> {
 *             return ctx.applyNumericBinary(l, r, BigRational::add, Double::sum);
 *         });
 *     }
 * }
 * }</pre>
 *
 * <h2>Broadcasting:</h2>
 * <p>Use {@link BroadcastingDispatcher} for automatic broadcasting over vectors and matrices.
 * For operators with special semantics (like matrix multiplication), implement custom logic.</p>
 *
 * @see UnaryOperator
 * @see OperatorContext
 * @see BroadcastingDispatcher
 */
public interface BinaryOperator extends Operator {

    /**
     * Applies this operator to the given operands.
     *
     * @param left  the left operand
     * @param right the right operand
     * @param ctx   the operator context for type coercion and utility methods
     * @return the result of the operation
     */
    NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx);

    /**
     * Gets the precedence of this operator.
     * Lower values indicate higher precedence.
     * <p>
     * Typical precedence levels:
     * <ul>
     *     <li>6: Power (^)</li>
     *     <li>7: Multiplicative (*, /, mod)</li>
     *     <li>8: Additive (+, -)</li>
     *     <li>10: Relational (<, >, <=, >=)</li>
     *     <li>11: Equality (==, !=)</li>
     *     <li>12-14: Logical (and, xor, or)</li>
     *     <li>15: Assignment (:=)</li>
     * </ul>
     *
     * @return the precedence level (lower = higher priority)
     */
    default int precedence() {
        return 10; // Default to relational precedence
    }

    /**
     * Whether this operator is right-associative.
     * Most operators are left-associative (a + b + c = (a + b) + c).
     * Power and assignment are right-associative (a ^ b ^ c = a ^ (b ^ c)).
     *
     * @return true if right-associative, false if left-associative
     */
    default boolean isRightAssociative() {
        return false;
    }

    /**
     * Whether this operator requires short-circuit evaluation.
     * Logical AND and OR operators should return true to avoid evaluating
     * the right operand when unnecessary.
     *
     * @return true if short-circuit evaluation should be used
     */
    default boolean requiresShortCircuit() {
        return false;
    }

    /**
     * For short-circuit operators, determines if the right operand should be skipped.
     * Called only when {@link #requiresShortCircuit()} returns true.
     *
     * @param leftValue the evaluated left operand
     * @param ctx       the operator context
     * @return the result if short-circuit applies, or null to continue evaluation
     */
    default NodeConstant shortCircuitResult(NodeConstant leftValue, OperatorContext ctx) {
        return null;
    }
}
