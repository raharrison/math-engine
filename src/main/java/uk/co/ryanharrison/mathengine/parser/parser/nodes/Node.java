package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * A node in the expression tree: either a {@link NodeConstant}, which is already a
 * value, or a {@link NodeExpression}, which still needs evaluating.
 * <p>
 * Sealed, so a pattern switch over nodes is checked for exhaustiveness.
 */
public abstract sealed class Node permits NodeConstant, NodeExpression {

    /**
     * Returns a human-readable type name for use in error messages and diagnostics.
     * Examples: {@code "number"}, {@code "string"}, {@code "vector"}, {@code "identifier"}
     */
    public abstract String typeName();

    /**
     * A readable form of this node, for debugging. Use a formatter for user-facing output.
     */
    @Override
    public abstract String toString();
}
