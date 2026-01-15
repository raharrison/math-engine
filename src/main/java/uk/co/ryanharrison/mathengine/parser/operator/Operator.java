package uk.co.ryanharrison.mathengine.parser.operator;

/**
 * Base interface for all operators in the Math Engine.
 * <p>
 * Operators are categorized by their arity:
 * <ul>
 *     <li>{@link BinaryOperator} - Two operands (e.g., +, -, *, /)</li>
 *     <li>{@link UnaryOperator} - One operand (e.g., -, !, not)</li>
 * </ul>
 *
 * <p>Each operator has:
 * <ul>
 *     <li>A unique symbol (e.g., "+", "*", "mod")</li>
 *     <li>A display name for error messages</li>
 *     <li>Whether it supports vector/matrix broadcasting</li>
 * </ul>
 *
 * @see BinaryOperator
 * @see UnaryOperator
 */
public interface Operator {

    /**
     * Gets the operator symbol as it appears in expressions.
     *
     * @return the operator symbol (e.g., "+", "-", "mod", "not")
     */
    String symbol();

    /**
     * Gets a human-readable name for the operator.
     * Used in error messages.
     *
     * @return display name (e.g., "addition", "multiplication", "logical not")
     */
    String displayName();

    /**
     * Whether this operator supports broadcasting over vectors and matrices.
     * When true, the operator will be applied element-wise to collection types.
     *
     * @return true if broadcasting is supported
     */
    default boolean supportsBroadcasting() {
        return true;
    }
}
