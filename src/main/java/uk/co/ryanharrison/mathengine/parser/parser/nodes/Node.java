package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Base class for all nodes in the Abstract Syntax Tree (AST).
 * <p>
 * The AST is built by the parser and represents the structure of an expression.
 * Nodes can be either constants (already evaluated) or expressions (need evaluation).
 */
public abstract class Node {

    /**
     * Accept a visitor for the visitor pattern (optional, for future use).
     */
    public abstract <T> T accept(NodeVisitor<T> visitor);

    /**
     * Get a string representation of this node for debugging.
     */
    @Override
    public abstract String toString();
}
