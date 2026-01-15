package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.List;
import java.util.function.Function;

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
    private final Function<Node, NodeConstant> evaluator;

    /**
     * Creates a new subscript handler.
     *
     * @param config    the engine configuration
     * @param evaluator function to evaluate nodes (typically Evaluator::evaluate)
     */
    public SubscriptHandler(MathEngineConfig config, Function<Node, NodeConstant> evaluator) {
        this.config = config;
        this.evaluator = evaluator;
    }

    /**
     * Evaluates a subscript operation.
     *
     * @param subscript the subscript node
     * @return the result of the subscript operation
     * @throws TypeError if the target cannot be subscripted
     */
    public NodeConstant evaluate(NodeSubscript subscript) {
        NodeConstant target = evaluator.apply(subscript.getTarget());

        if (target instanceof NodeVector vector) {
            if (!config.vectorsEnabled()) {
                throw new EvaluationException("Vectors are disabled in current configuration");
            }
            return evaluateVectorSubscript(vector, subscript.getIndices());
        }

        if (target instanceof NodeMatrix matrix) {
            if (!config.matricesEnabled()) {
                throw new EvaluationException("Matrices are disabled in current configuration");
            }
            return evaluateMatrixSubscript(matrix, subscript.getIndices());
        }

        throw new TypeError("Cannot subscript " + TypeCoercion.typeName(target));
    }

    /**
     * Evaluates vector subscript: v[i] or v[start:end].
     */
    private NodeConstant evaluateVectorSubscript(NodeVector vector, List<NodeSubscript.SliceArg> indices) {
        if (indices.size() != 1) {
            throw new TypeError("Vector subscript requires exactly one index, got " + indices.size());
        }

        NodeSubscript.SliceArg arg = indices.getFirst();

        if (arg.isRange() || arg.getStart() == null) {
            return evaluateVectorSlice(vector, arg);
        } else {
            return evaluateVectorIndex(vector, arg);
        }
    }

    /**
     * Evaluates a vector slice operation.
     */
    private NodeConstant evaluateVectorSlice(NodeVector vector, NodeSubscript.SliceArg arg) {
        int start = 0;
        int end = vector.size();

        if (arg.getStart() != null) {
            start = resolveIndex(arg.getStart(), vector.size(), "start");
        }

        if (arg.getEnd() != null) {
            end = resolveIndex(arg.getEnd(), vector.size(), "end");
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
    private NodeConstant evaluateVectorIndex(NodeVector vector, NodeSubscript.SliceArg arg) {
        int index = resolveIndex(arg.getStart(), vector.size(), "index");

        // Handle negative indices
        if (index < 0) index = vector.size() + index;

        if (index < 0 || index >= vector.size()) {
            throw new EvaluationException("Vector index out of bounds: " + index +
                    " (size: " + vector.size() + ")");
        }

        Node element = vector.getElement(index);
        if (element instanceof NodeConstant constant) {
            return constant;
        } else {
            return evaluator.apply(element);
        }
    }

    /**
     * Evaluates matrix subscript.
     */
    private NodeConstant evaluateMatrixSubscript(NodeMatrix matrix, List<NodeSubscript.SliceArg> indices) {
        if (indices.isEmpty() || indices.size() > 2) {
            throw new TypeError("Matrix subscript requires 1 or 2 indices, got " + indices.size());
        }

        NodeSubscript.SliceArg rowArg = indices.get(0);
        NodeSubscript.SliceArg colArg = indices.size() > 1 ? indices.get(1) : null;

        // Single index: row selector
        if (colArg == null && !rowArg.isRange() && rowArg.getStart() != null) {
            return evaluateMatrixRowAccess(matrix, rowArg);
        }

        return evaluateMatrixSlice(matrix, rowArg, colArg);
    }

    /**
     * Evaluates matrix row access.
     */
    private NodeConstant evaluateMatrixRowAccess(NodeMatrix matrix, NodeSubscript.SliceArg rowArg) {
        int rowIndex = resolveIndex(rowArg.getStart(), matrix.getRows(), "row index");
        if (rowIndex < 0) rowIndex = matrix.getRows() + rowIndex;

        if (rowIndex < 0 || rowIndex >= matrix.getRows()) {
            throw new EvaluationException("Matrix row index out of bounds: " + rowIndex);
        }

        Node[][] elements = matrix.getElements();
        return new NodeVector(elements[rowIndex]);
    }

    /**
     * Evaluates a matrix slice or element access.
     */
    private NodeConstant evaluateMatrixSlice(NodeMatrix matrix, NodeSubscript.SliceArg rowArg, NodeSubscript.SliceArg colArg) {
        // Extract row range
        int startRow = 0, endRow = matrix.getRows();
        boolean singleRow = false;

        if (rowArg.getStart() != null && !rowArg.isRange()) {
            startRow = resolveIndex(rowArg.getStart(), matrix.getRows(), "row");
            if (startRow < 0) startRow = matrix.getRows() + startRow;
            endRow = startRow + 1;
            singleRow = true;
        } else if (rowArg.isRange()) {
            if (rowArg.getStart() != null) {
                startRow = resolveIndex(rowArg.getStart(), matrix.getRows(), "row start");
                if (startRow < 0) startRow = matrix.getRows() + startRow;
            }
            if (rowArg.getEnd() != null) {
                endRow = resolveIndex(rowArg.getEnd(), matrix.getRows(), "row end");
                if (endRow < 0) endRow = matrix.getRows() + endRow;
            }
        }

        // Extract column range
        int startCol = 0, endCol = matrix.getCols();
        boolean singleCol = false;

        if (colArg != null) {
            if (colArg.getStart() != null && !colArg.isRange()) {
                startCol = resolveIndex(colArg.getStart(), matrix.getCols(), "column");
                if (startCol < 0) startCol = matrix.getCols() + startCol;
                endCol = startCol + 1;
                singleCol = true;
            } else if (colArg.isRange()) {
                if (colArg.getStart() != null) {
                    startCol = resolveIndex(colArg.getStart(), matrix.getCols(), "column start");
                    if (startCol < 0) startCol = matrix.getCols() + startCol;
                }
                if (colArg.getEnd() != null) {
                    endCol = resolveIndex(colArg.getEnd(), matrix.getCols(), "column end");
                    if (endCol < 0) endCol = matrix.getCols() + endCol;
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
     * Resolves an index expression to an integer value.
     */
    private int resolveIndex(Node indexNode, int size, String description) {
        NodeConstant indexValue = evaluator.apply(indexNode);
        if (!TypeCoercion.isNumeric(indexValue)) {
            throw new TypeError("Slice " + description + " must be a number");
        }
        int index = TypeCoercion.toInt(indexValue);
        if (index < 0) {
            index = size + index;
        }
        return index;
    }

    /**
     * Clamps a value between min (inclusive) and max (inclusive).
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
