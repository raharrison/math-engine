package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Interface for unary operators that take a single operand.
 * <p>
 * Unary operators include:
 * <ul>
 *     <li>Prefix operators: -, +, not</li>
 *     <li>Postfix operators: !, !!, %</li>
 * </ul>
 *
 * <h2>Implementation Example:</h2>
 * <pre>{@code
 * public class NegateOperator implements UnaryOperator {
 *
 *     @Override
 *     public String symbol() { return "-"; }
 *
 *     @Override
 *     public String displayName() { return "negation"; }
 *
 *     @Override
 *     public Position position() { return Position.PREFIX; }
 *
 *     @Override
 *     public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
 *         return ctx.dispatchUnary(operand,
 *             BigRational::negate,
 *             v -> -v);
 *     }
 * }
 * }</pre>
 *
 * @see BinaryOperator
 * @see OperatorContext
 */
public interface UnaryOperator extends Operator {

    /**
     * The position of a unary operator relative to its operand.
     */
    enum Position {
        /**
         * Operator appears before the operand (e.g., -x, not x)
         */
        PREFIX,

        /**
         * Operator appears after the operand (e.g., x!, x%)
         */
        POSTFIX
    }

    /**
     * Applies this operator to the given operand.
     *
     * @param operand the operand
     * @param ctx     the operator context for type coercion and utility methods
     * @return the result of the operation
     */
    NodeConstant apply(NodeConstant operand, OperatorContext ctx);

    /**
     * Gets the position of this operator relative to its operand.
     *
     * @return PREFIX for operators like -x, POSTFIX for operators like x!
     */
    Position position();
}
