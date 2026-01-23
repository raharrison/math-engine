package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for extracting children from AST nodes for tree visualization.
 * <p>
 * This visitor is used by GUI components to build visual tree representations
 * of the AST by recursively extracting child nodes from expression nodes.
 * </p>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * AstTreeBuilder builder = new AstTreeBuilder();
 * List<Node> children = node.accept(builder);
 * }</pre>
 */
public final class AstTreeBuilder implements NodeVisitor<List<Node>> {

    @Override
    public List<Node> visitConstant(NodeConstant node) {
        // Constants are leaf nodes - handle special cases for composite constants
        return switch (node) {
            case NodeVector vector -> List.of(vector.getElements());
            case NodeMatrix matrix -> {
                List<Node> children = new ArrayList<>();
                for (Node[] row : matrix.getElements()) {
                    children.addAll(List.of(row));
                }
                yield children;
            }
            default -> List.of(); // Other constants are true leaf nodes
        };
    }

    @Override
    public List<Node> visitExpression(NodeExpression node) {
        // Extract children based on expression node type
        return switch (node) {
            case NodeBinary binary -> List.of(binary.getLeft(), binary.getRight());
            case NodeUnary unary -> List.of(unary.getOperand());
            case NodeCall call -> {
                List<Node> children = new ArrayList<>();
                children.add(call.getFunction());
                children.addAll(call.getArguments());
                yield children;
            }
            case NodeSubscript subscript -> {
                List<Node> children = new ArrayList<>();
                children.add(subscript.getTarget());
                // Extract nodes from SliceArg objects
                for (NodeSubscript.SliceArg arg : subscript.getIndices()) {
                    if (arg.getStart() != null) {
                        children.add(arg.getStart());
                    }
                    if (arg.getEnd() != null) {
                        children.add(arg.getEnd());
                    }
                }
                yield children;
            }
            case NodeAssignment assignment -> List.of(assignment.getValue());
            case NodeFunctionDef funcDef -> List.of(funcDef.getBody());
            case NodeRangeExpression range -> {
                List<Node> children = new ArrayList<>();
                children.add(range.getStart());
                children.add(range.getEnd());
                if (range.getStep() != null) {
                    children.add(range.getStep());
                }
                yield children;
            }
            case NodeUnitConversion conversion -> List.of(conversion.getValue());
            case NodeComprehension comp -> {
                List<Node> children = new ArrayList<>();
                children.add(comp.getExpression());
                children.add(comp.getIterable());
                if (comp.getCondition() != null) {
                    children.add(comp.getCondition());
                }
                yield children;
            }
            case NodeSequence sequence -> sequence.getStatements();
            case NodeVariable _ -> List.of(); // Variables are leaf nodes
            default -> List.of(); // Unknown expression types have no children
        };
    }
}
