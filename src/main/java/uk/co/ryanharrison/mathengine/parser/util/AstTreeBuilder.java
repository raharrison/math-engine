package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists the direct children of an AST node, for tools that draw the tree.
 *
 * <pre>{@code
 * List<Node> children = AstTreeBuilder.childrenOf(node);
 * }</pre>
 */
public final class AstTreeBuilder {

    private AstTreeBuilder() {
    }

    /**
     * The node's children in source order, or an empty list for a leaf.
     */
    public static List<Node> childrenOf(Node node) {
        return switch (node) {
            case NodeVector vector -> List.of(vector.getElements());
            case NodeMatrix matrix -> {
                var children = new ArrayList<Node>();
                for (Node[] row : matrix.getElements()) {
                    children.addAll(List.of(row));
                }
                yield children;
            }
            case NodeBinary binary -> List.of(binary.getLeft(), binary.getRight());
            case NodeUnary unary -> List.of(unary.getOperand());
            case NodeCall call -> {
                var children = new ArrayList<Node>();
                children.add(call.getFunction());
                children.addAll(call.getArguments());
                yield children;
            }
            case NodeSubscript subscript -> {
                var children = new ArrayList<Node>();
                children.add(subscript.getTarget());
                for (NodeSubscript.SliceArg arg : subscript.getIndices()) {
                    addIfPresent(children, arg.getStart());
                    addIfPresent(children, arg.getEnd());
                }
                yield children;
            }
            case NodeAssignment assignment -> List.of(assignment.getValue());
            case NodeElementAssignment assignment -> {
                var children = new ArrayList<Node>();
                for (List<NodeSubscript.SliceArg> group : assignment.getIndexGroups()) {
                    for (NodeSubscript.SliceArg arg : group) {
                        addIfPresent(children, arg.getStart());
                        addIfPresent(children, arg.getEnd());
                    }
                }
                children.add(assignment.getValue());
                yield children;
            }
            case NodeFunctionDef definition -> List.of(definition.getBody());
            case NodeRangeExpression range -> {
                var children = new ArrayList<Node>(List.of(range.getStart(), range.getEnd()));
                addIfPresent(children, range.getStep());
                yield children;
            }
            case NodeUnitConversion conversion -> List.of(conversion.getValue());
            case NodeComprehension comprehension -> {
                var children = new ArrayList<Node>(
                        List.of(comprehension.getExpression(), comprehension.getIterable()));
                addIfPresent(children, comprehension.getCondition());
                yield children;
            }
            case NodeSequence sequence -> sequence.getStatements();
            default -> List.of();
        };
    }

    private static void addIfPresent(List<Node> children, Node node) {
        if (node != null) {
            children.add(node);
        }
    }
}
