package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Base class for nodes representing expressions that need to be evaluated.
 * <p>
 * NodeExpression instances are part of the AST and represent operations,
 * function calls, variable references, etc. They must be evaluated to produce
 * a NodeConstant result.
 */
public abstract class NodeExpression extends Node {

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitExpression(this);
    }
}
