package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.*;

import java.util.List;
import java.util.function.Supplier;

/**
 * Writes one element of a collection: {@code v[0] := 5}, {@code m[1, 2] := 9},
 * {@code m[0] := {7, 8}}, {@code m[0][1] := 9}, {@code s[0] := "H"}.
 * <p>
 * Values are immutable, so the write rebuilds the target and rebinds the name through
 * {@link EvaluationContext#assign}, which finds the scope that defines it.
 *
 * @see SubscriptHandler
 */
public final class ElementAssignmentHandler {

    private final MathEngineConfig config;
    private final NodeEvaluator evaluator;
    private final IndexResolver indexResolver;

    public ElementAssignmentHandler(MathEngineConfig config, NodeEvaluator evaluator) {
        this.config = config;
        this.evaluator = evaluator;
        this.indexResolver = new IndexResolver(evaluator);
    }

    /**
     * @return the value assigned, so {@code v[0] := 5} yields 5 as {@code x := 5} does
     * @throws UndefinedVariableException if the target name has no value
     * @throws TypeError                  if the target, or a step of the chain, holds no elements
     * @throws EvaluationException        if an index is outside the collection
     */
    public NodeConstant evaluate(NodeElementAssignment node, EvaluationContext context) {
        if (!config.userDefinedVariablesEnabled()) {
            throw new EvaluationException("User-defined variables are disabled in current configuration");
        }

        String name = node.getIdentifier();
        NodeConstant target = context.resolve(name)
                .orElseThrow(() -> new UndefinedVariableException(name));

        NodeConstant value = evaluator.evaluate(node.getValue(), context);
        context.assign(name, update(target, node.getIndexGroups(), 0, value, context));
        return value;
    }

    /**
     * Applies the groups from {@code groupIndex} onwards, answering the rebuilt container.
     * One rule covers every shape, so a chain costs no case of its own.
     */
    private NodeConstant update(NodeConstant container, List<List<NodeSubscript.SliceArg>> groups,
                                int groupIndex, NodeConstant value, EvaluationContext context) {
        if (groupIndex == groups.size()) {
            return value;
        }

        List<NodeSubscript.SliceArg> group = groups.get(groupIndex);

        return switch (container) {
            case NodeVector vector -> updateVector(vector, group, groups, groupIndex, value, context);
            case NodeMatrix matrix -> updateMatrix(matrix, group, groups, groupIndex, value, context);
            case NodeString text -> updateString(text, group, groups, groupIndex, value, context);
            default -> throw new TypeError("Cannot assign into a " + container.typeName());
        };
    }

    /**
     * The right side when this was the last group, otherwise the result of writing
     * further into the element. The element is a supplier because the last group
     * overwrites it unread.
     */
    private NodeConstant replacementFor(List<List<NodeSubscript.SliceArg>> groups, int groupIndex,
                                        Supplier<NodeConstant> current, NodeConstant value,
                                        EvaluationContext context) {
        if (groupIndex + 1 == groups.size()) {
            return value;
        }
        return update(current.get(), groups, groupIndex + 1, value, context);
    }

    // ==================== Vectors ====================

    private NodeConstant updateVector(NodeVector vector, List<NodeSubscript.SliceArg> group,
                                      List<List<NodeSubscript.SliceArg>> groups, int groupIndex,
                                      NodeConstant value, EvaluationContext context) {
        if (!config.vectorsEnabled()) {
            throw new EvaluationException("Vectors are disabled in current configuration");
        }

        Node indexNode = singleIndex(group, "Vector");
        int index = indexResolver.requireIndex(indexNode, vector.size(), "Vector index", context);

        NodeConstant replacement = replacementFor(groups, groupIndex,
                () -> constant(vector.getElement(index), context), value, context);

        Node[] elements = vector.getElements();
        elements[index] = replacement;
        return new NodeVector(elements);
    }

    // ==================== Matrices ====================

    private NodeConstant updateMatrix(NodeMatrix matrix, List<NodeSubscript.SliceArg> group,
                                      List<List<NodeSubscript.SliceArg>> groups, int groupIndex,
                                      NodeConstant value, EvaluationContext context) {
        if (!config.matricesEnabled()) {
            throw new EvaluationException("Matrices are disabled in current configuration");
        }
        if (group.isEmpty() || group.size() > 2) {
            throw new TypeError("Matrix subscript requires 1 or 2 indices, got " + group.size());
        }

        if (group.size() == 1) {
            return updateMatrixRow(matrix, group.getFirst(), groups, groupIndex, value, context);
        }
        return updateMatrixElement(matrix, group, groups, groupIndex, value, context);
    }

    /**
     * Writes a whole row, {@code m[0] := {7, 8}}. The row keeps its width.
     */
    private NodeConstant updateMatrixRow(NodeMatrix matrix, NodeSubscript.SliceArg rowArg,
                                         List<List<NodeSubscript.SliceArg>> groups, int groupIndex,
                                         NodeConstant value, EvaluationContext context) {
        requireSingleElement(rowArg);
        int row = indexResolver.requireIndex(rowArg.getStart(), matrix.getRows(), "Matrix row index", context);

        NodeConstant replacement = replacementFor(groups, groupIndex,
                () -> new NodeVector(matrix.getElements()[row]), value, context);

        if (!(replacement instanceof NodeVector rowVector)) {
            throw new TypeError("A matrix row must be assigned a vector, got a " + replacement.typeName());
        }
        if (rowVector.size() != matrix.getCols()) {
            throw new EvaluationException("Row length " + rowVector.size() +
                    " does not match matrix width " + matrix.getCols());
        }

        Node[][] elements = matrix.getElements();
        elements[row] = rowVector.getElements();
        return new NodeMatrix(elements);
    }

    private NodeConstant updateMatrixElement(NodeMatrix matrix, List<NodeSubscript.SliceArg> group,
                                             List<List<NodeSubscript.SliceArg>> groups, int groupIndex,
                                             NodeConstant value, EvaluationContext context) {
        NodeSubscript.SliceArg rowArg = group.get(0);
        NodeSubscript.SliceArg colArg = group.get(1);
        requireSingleElement(rowArg);
        requireSingleElement(colArg);

        int row = indexResolver.requireIndex(rowArg.getStart(), matrix.getRows(), "Matrix row index", context);
        int col = indexResolver.requireIndex(colArg.getStart(), matrix.getCols(), "Matrix column index", context);

        NodeConstant replacement = replacementFor(groups, groupIndex,
                () -> constant(matrix.getElement(row, col), context), value, context);

        Node[][] elements = matrix.getElements();
        elements[row][col] = replacement;
        return new NodeMatrix(elements);
    }

    // ==================== Strings ====================

    /**
     * Writes one character, {@code s[0] := "H"}. The right side is a single-character
     * string, which is what {@code s[0]} reads back, so the length is preserved.
     */
    private NodeConstant updateString(NodeString text, List<NodeSubscript.SliceArg> group,
                                      List<List<NodeSubscript.SliceArg>> groups, int groupIndex,
                                      NodeConstant value, EvaluationContext context) {
        String current = text.getValue();
        Node indexNode = singleIndex(group, "String");
        int index = indexResolver.requireIndex(indexNode, current.length(), "String index", context);

        NodeConstant replacement = replacementFor(groups, groupIndex,
                () -> new NodeString(String.valueOf(current.charAt(index))), value, context);

        if (!(replacement instanceof NodeString character)) {
            throw new TypeError("A string element must be assigned a string, got a " + replacement.typeName());
        }
        if (character.getValue().length() != 1) {
            throw new TypeError("A string element must be assigned a single character, got: \"" +
                    character.getValue() + "\"");
        }

        return new NodeString(current.substring(0, index) + character.getValue() + current.substring(index + 1));
    }

    // ==================== Index groups ====================

    /**
     * @param what the collection being written, for the message
     */
    private Node singleIndex(List<NodeSubscript.SliceArg> group, String what) {
        if (group.size() != 1) {
            throw new TypeError(what + " subscript requires exactly one index, got " + group.size());
        }
        NodeSubscript.SliceArg arg = group.getFirst();
        requireSingleElement(arg);
        return arg.getStart();
    }

    /**
     * Slices are refused: filling and length matching are a separate design.
     */
    private void requireSingleElement(NodeSubscript.SliceArg arg) {
        if (arg.isRange() || arg.getStart() == null) {
            throw new EvaluationException(
                    "Slice assignment is not supported: assign to a single element instead");
        }
    }

    /**
     * An element as a value, evaluating it if it was left unevaluated.
     */
    private NodeConstant constant(Node element, EvaluationContext context) {
        return element instanceof NodeConstant value ? value : evaluator.evaluate(element, context);
    }
}
