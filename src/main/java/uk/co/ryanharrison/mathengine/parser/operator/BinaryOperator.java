package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

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
 *     public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
 *         // Use BroadcastingEngine for element-wise operations with full broadcasting
 *         return BroadcastingEngine.applyBinary(left, right, (l, r) -> {
 *             return ctx.applyNumericBinary(l, r, BigRational::add, Double::sum);
 *         });
 *     }
 * }
 * }</pre>
 *
 * <h2>Broadcasting:</h2>
 * <p>Use {@link BroadcastingEngine} for automatic broadcasting over vectors and matrices.
 * For operators with special semantics (like matrix multiplication), implement custom logic.</p>
 *
 * @see UnaryOperator
 * @see OperatorContext
 * @see uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine
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
