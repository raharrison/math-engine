package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Base class for nodes representing constant values that have already been evaluated.
 * <p>
 * NodeConstant instances are the final results of evaluation and don't require
 * further computation. They include numbers, booleans, strings, vectors, matrices, etc.
 * <p>
 * Sealed to enable exhaustiveness checking in pattern matching.
 * <p>
 * Provides universal arithmetic dispatch via instance methods ({@link #add}, {@link #subtract},
 * {@link #multiply}, {@link #divide}, {@link #power}, {@link #negate}, {@link #compareTo}).
 * This allows any code to perform {@code left.add(right)} with correct type-preserving dispatch,
 * including string concatenation/repetition and lexicographic comparison.
 */
public abstract sealed class NodeConstant extends Node permits
        NodeNumber,
        NodeString,
        NodeVector,
        NodeMatrix,
        NodeUnit,
        NodeRange,
        NodeLambda,
        NodeFunction {

    /**
     * Convert this constant to a double value for numeric operations.
     * Throws an exception if this constant is not numeric.
     */
    public abstract double doubleValue();

    /**
     * Check if this constant represents a numeric value.
     */
    public abstract boolean isNumeric();

    /**
     * Check if this constant represents a boolean value.
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * Check if this constant represents a string value.
     */
    public boolean isString() {
        return false;
    }

    /**
     * Check if this constant represents a vector.
     */
    public boolean isVector() {
        return false;
    }

    /**
     * Check if this constant represents a matrix.
     */
    public boolean isMatrix() {
        return false;
    }

    // ==================== Universal Arithmetic Dispatch ====================

    /**
     * Adds this constant to another constant.
     * <p>
     * Type dispatch rules:
     * <ul>
     *     <li>Numeric types delegate to {@link uk.co.ryanharrison.mathengine.parser.util.NumericOperations}</li>
     *     <li>String + anything = string concatenation</li>
     *     <li>Vectors/matrices broadcast element-wise</li>
     * </ul>
     *
     * @param other the right-hand operand
     * @return the result of the addition
     */
    public abstract NodeConstant add(NodeConstant other);

    /**
     * Subtracts another constant from this constant.
     *
     * @param other the right-hand operand
     * @return the result of the subtraction
     */
    public abstract NodeConstant subtract(NodeConstant other);

    /**
     * Multiplies this constant by another constant.
     * <p>
     * Supports string repetition: {@code "ab" * 3} = {@code "ababab"}.
     *
     * @param other the right-hand operand
     * @return the result of the multiplication
     */
    public abstract NodeConstant multiply(NodeConstant other);

    /**
     * Divides this constant by another constant.
     *
     * @param other the right-hand operand
     * @return the result of the division
     */
    public abstract NodeConstant divide(NodeConstant other);

    /**
     * Raises this constant to the power of another constant.
     *
     * @param other the exponent
     * @return the result of the exponentiation
     */
    public abstract NodeConstant power(NodeConstant other);

    /**
     * Negates this constant (unary minus).
     *
     * @return the negated value
     */
    public abstract NodeConstant negate();

    /**
     * Compares this constant to another constant for ordering.
     * <p>
     * Supports numeric comparison and lexicographic string comparison.
     *
     * @param other the other constant to compare to
     * @return negative if this &lt; other, zero if equal, positive if this &gt; other
     */
    public abstract int compareTo(NodeConstant other);

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitConstant(this);
    }
}
