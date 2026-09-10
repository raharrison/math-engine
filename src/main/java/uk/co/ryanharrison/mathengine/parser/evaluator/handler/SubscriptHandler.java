package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.evaluator.NodeEvaluator;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.List;

/**
 * Handles subscript operations (indexing and slicing) for vectors and matrices.
 * <p>
 * Supports:
 * <ul>
 *     <li>Vector indexing: v[0], v[-1]</li>
 *     <li>Vector slicing: v[1:3], v[:5], v[2:]</li>
 *     <li>Matrix row access: m[0]</li>
 *     <li>Matrix element access: m[0, 1]</li>
 *     <li>Matrix slicing: m[1:3, 0:2]</li>
 * </ul>
 * <p>
 * Negative indices are supported and wrap from the end.
 */
public final class SubscriptHandler {

    private final MathEngineConfig config;
    private final NodeEvaluator evaluator;

    public SubscriptHandler(MathEngineConfig config, NodeEvaluator evaluator) {
        this.config = config;
        this.evaluator = evaluator;
    }

    /**
     * @throws TypeError if the target cannot be subscripted
     */
    public NodeConstant evaluate(NodeSubscript subscript, EvaluationContext context) {
        NodeConstant target = evaluator.evaluate(subscript.getTarget(), context);

        return switch (target) {
            case NodeVector vector -> {
                if (!config.vectorsEnabled()) {
                    throw new EvaluationException("Vectors are disabled in current configuration");
                }
                yield evaluateVectorSubscript(vector, subscript.getIndices(), context);
            }
            case NodeMatrix matrix -> {
                if (!config.matricesEnabled()) {
                    throw new EvaluationException("Matrices are disabled in current configuration");
                }
                yield evaluateMatrixSubscript(matrix, subscript.getIndices(), context);
            }
            default -> throw new TypeError("Cannot subscript a " + target.typeName());
        };
    }

    /**
     * Evaluates vector subscript: v[i] or v[start:end].
     */
    private NodeConstant evaluateVectorSubscript(NodeVector vector, List<NodeSubscript.SliceArg> indices, EvaluationContext context) {
        if (indices.size() != 1) {
            throw new TypeError("Vector subscript requires exactly one index, got " + indices.size());
        }

        NodeSubscript.SliceArg arg = indices.getFirst();

        if (arg.isRange() || arg.getStart() == null) {
            return evaluateVectorSlice(vector, arg, context);
        } else {
            return evaluateVectorIndex(vector, arg, context);
        }
    }

    /**
     * Evaluates a vector slice operation.
     */
    private NodeConstant evaluateVectorSlice(NodeVector vector, NodeSubscript.SliceArg arg, EvaluationContext context) {
        int start = 0;
        int end = vector.size();

        if (arg.getStart() != null) {
            start = resolveIndex(arg.getStart(), vector.size(), "start", context);
        }

        if (arg.getEnd() != null) {
            end = resolveIndex(arg.getEnd(), vector.size(), "end", context);
        }

        // Clamp bounds
        start = clamp(start, 0, vector.size());
        end = clamp(end, 0, vector.size());
        if (start > end) start = end;

        Node[] elements = vector.getElements();
        Node[] sliceElements = new Node[end - start];
        System.arraycopy(elements, start, sliceElements, 0, end - start);

        return new NodeVector(sliceElements);
    }

    /**
     * Evaluates a single vector index access.
     */
    private NodeConstant evaluateVectorIndex(NodeVector vector, NodeSubscript.SliceArg arg, EvaluationContext context) {
        int index = requireIndex(arg.getStart(), vector.size(), "Vector index", context);

        Node element = vector.getElement(index);
        if (element instanceof NodeConstant constant) {
            return constant;
        } else {
            return evaluator.evaluate(element, context);
        }
    }

    /**
     * Evaluates matrix subscript.
     */
    private NodeConstant evaluateMatrixSubscript(NodeMatrix matrix, List<NodeSubscript.SliceArg> indices, EvaluationContext context) {
        if (indices.isEmpty() || indices.size() > 2) {
            throw new TypeError("Matrix subscript requires 1 or 2 indices, got " + indices.size());
        }

        NodeSubscript.SliceArg rowArg = indices.get(0);
        NodeSubscript.SliceArg colArg = indices.size() > 1 ? indices.get(1) : null;

        // Single index: row selector
        if (colArg == null && !rowArg.isRange() && rowArg.getStart() != null) {
            return evaluateMatrixRowAccess(matrix, rowArg, context);
        }

        return evaluateMatrixSlice(matrix, rowArg, colArg, context);
    }

    /**
     * Evaluates matrix row access.
     */
    private NodeConstant evaluateMatrixRowAccess(NodeMatrix matrix, NodeSubscript.SliceArg rowArg, EvaluationContext context) {
        int rowIndex = requireIndex(rowArg.getStart(), matrix.getRows(), "Matrix row index", context);

        Node[][] elements = matrix.getElements();
        return new NodeVector(elements[rowIndex]);
    }

    /**
     * Evaluates a matrix slice or element access.
     */
    private NodeConstant evaluateMatrixSlice(NodeMatrix matrix, NodeSubscript.SliceArg rowArg, NodeSubscript.SliceArg colArg, EvaluationContext context) {
        // Extract row range
        int startRow = 0, endRow = matrix.getRows();
        boolean singleRow = false;

        if (rowArg.getStart() != null && !rowArg.isRange()) {
            startRow = resolveIndex(rowArg.getStart(), matrix.getRows(), "row", context);
            endRow = startRow + 1;
            singleRow = true;
        } else if (rowArg.isRange()) {
            if (rowArg.getStart() != null) {
                startRow = resolveIndex(rowArg.getStart(), matrix.getRows(), "row start", context);
            }
            if (rowArg.getEnd() != null) {
                endRow = resolveIndex(rowArg.getEnd(), matrix.getRows(), "row end", context);
            }
        }

        // Extract column range
        int startCol = 0, endCol = matrix.getCols();
        boolean singleCol = false;

        if (colArg != null) {
            if (colArg.getStart() != null && !colArg.isRange()) {
                startCol = resolveIndex(colArg.getStart(), matrix.getCols(), "column", context);
                endCol = startCol + 1;
                singleCol = true;
            } else if (colArg.isRange()) {
                if (colArg.getStart() != null) {
                    startCol = resolveIndex(colArg.getStart(), matrix.getCols(), "column start", context);
                }
                if (colArg.getEnd() != null) {
                    endCol = resolveIndex(colArg.getEnd(), matrix.getCols(), "column end", context);
                }
            }
        }

        // Clamp bounds
        startRow = clamp(startRow, 0, matrix.getRows());
        endRow = clamp(endRow, 0, matrix.getRows());
        startCol = clamp(startCol, 0, matrix.getCols());
        endCol = clamp(endCol, 0, matrix.getCols());

        Node[][] elements = matrix.getElements();

        if (singleRow && singleCol) {
            return (NodeConstant) elements[startRow][startCol];
        } else if (singleRow) {
            Node[] rowElements = new Node[endCol - startCol];
            System.arraycopy(elements[startRow], startCol, rowElements, 0, endCol - startCol);
            return new NodeVector(rowElements);
        } else if (singleCol) {
            Node[] colElements = new Node[endRow - startRow];
            for (int i = startRow; i < endRow; i++) {
                colElements[i - startRow] = elements[i][startCol];
            }
            return new NodeVector(colElements);
        } else {
            Node[][] subElements = new Node[endRow - startRow][endCol - startCol];
            for (int i = startRow; i < endRow; i++) {
                System.arraycopy(elements[i], startCol, subElements[i - startRow], 0, endCol - startCol);
            }
            return new NodeMatrix(subElements);
        }
    }

    /**
     * Resolves an index that must land on an existing element.
     * <p>
     * The failure quotes the index as it was written, so {@code v[-4]} reports -4 rather
     * than the position it was translated to.
     *
     * @throws EvaluationException if the index is outside the collection
     */
    private int requireIndex(Node indexNode, int size, String description, EvaluationContext context) {
        int written = readIndex(indexNode, description, context);
        int index = written < 0 ? size + written : written;
        if (index < 0 || index >= size) {
            throw new EvaluationException(description + " out of bounds: " + written +
                    " (size: " + size + ")");
        }
        return index;
    }

    /**
     * Resolves an index expression to a position from the start of the collection.
     * <p>
     * This is the one place a negative index is turned into a position, so {@code v[-1]}
     * is the last element and {@code v[-4]} of a three-element vector is out of range
     * rather than wrapping around a second time.
     *
     * @param size the length of the dimension being indexed
     * @return a position, which may still be out of range and is checked by the caller
     */
    private int resolveIndex(Node indexNode, int size, String description, EvaluationContext context) {
        int index = readIndex(indexNode, description, context);
        return index < 0 ? size + index : index;
    }

    /**
     * Evaluates an index expression to the integer the user wrote.
     */
    private int readIndex(Node indexNode, String description, EvaluationContext context) {
        NodeConstant indexValue = evaluator.evaluate(indexNode, context);
        if (!TypeCoercion.isNumeric(indexValue)) {
            throw new TypeError(description + " must be a number");
        }
        return TypeCoercion.toInt(indexValue);
    }

    /**
     * Clamps a value between min (inclusive) and max (inclusive).
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
