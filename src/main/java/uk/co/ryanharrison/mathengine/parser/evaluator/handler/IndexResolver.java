package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.ast.Node;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.evaluator.NodeEvaluator;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

/**
 * Turns an index expression into a position in a collection. Shared by
 * {@link SubscriptHandler} and {@link ElementAssignmentHandler}, so reading and writing
 * agree on negative indices and on bounds.
 */
final class IndexResolver {

    private final NodeEvaluator evaluator;

    IndexResolver(NodeEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Resolves an index that must land on an existing element.
     * <p>
     * The failure quotes the index as it was written, so {@code v[-4]} reports -4 rather
     * than the position it was translated to.
     *
     * @throws EvaluationException if the index is outside the collection
     */
    int requireIndex(Node indexNode, int size, String description, EvaluationContext context) {
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
    int resolveIndex(Node indexNode, int size, String description, EvaluationContext context) {
        int index = readIndex(indexNode, description, context);
        return index < 0 ? size + index : index;
    }

    /**
     * Evaluates an index expression to the integer the user wrote.
     */
    int readIndex(Node indexNode, String description, EvaluationContext context) {
        NodeConstant indexValue = evaluator.evaluate(indexNode, context);
        if (!TypeCoercion.isNumeric(indexValue)) {
            throw new TypeError(description + " must be a number");
        }
        double index = TypeCoercion.toDouble(indexValue);
        if (index != Math.floor(index) || Double.isInfinite(index)) {
            throw new TypeError(description + " must be a whole number, got: " + index);
        }
        return (int) index;
    }

    /**
     * Clamps a value between min (inclusive) and max (inclusive).
     */
    int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
