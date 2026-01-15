package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Visitor interface for traversing the AST using the visitor pattern.
 * This is optional and can be used for operations like printing, optimization, etc.
 */
public interface NodeVisitor<T> {

    T visitConstant(NodeConstant node);

    T visitExpression(NodeExpression node);
}
