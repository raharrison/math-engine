package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.MatrixOperations;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.List;

/**
 * At (@).
 * <p>
 * Supports:
 * <ul>
 *     <li>Matrix @ Matrix: True matrix multiplication (not element-wise)</li>
 *     <li>Vector @ Vector: Dot product</li>
 *     <li>Vector @ Function: Map operation (apply function to each element)</li>
 * </ul>
 * <p>
 * This is NOT element-wise multiplication. For element-wise, use {@link MultiplyOperator} (*).
 */
public final class AtOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final AtOperator INSTANCE = new AtOperator();

    private AtOperator() {
    }

    @Override
    public String symbol() {
        return "@";
    }

    @Override
    public String displayName() {
        return "at";
    }

    @Override
    public int precedence() {
        return 7;
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        // Matrix @ Matrix: true matrix multiplication
        if (left instanceof NodeMatrix leftMatrix && right instanceof NodeMatrix rightMatrix) {
            return MatrixOperations.multiply(leftMatrix, rightMatrix, ctx);
        }

        // Vector @ Vector: dot product
        if (left instanceof NodeVector leftVec && right instanceof NodeVector rightVec) {
            return MatrixOperations.dotProduct(leftVec, rightVec, ctx);
        }

        // Vector @ Function: map operation
        if (left instanceof NodeVector vector && right instanceof NodeFunction func) {
            return vectorMap(vector, func, ctx);
        }

        throw new TypeError("@ operator requires (Matrix @ Matrix), (Vector @ Vector), or (Vector @ Function), got " +
                left.getClass().getSimpleName() + " @ " + right.getClass().getSimpleName());
    }

    /**
     * Applies a function to each element of a vector (map operation).
     * <p>
     * Example: {1,2,3} @ (x -> x*2) → {2,4,6}
     *
     * @param vector the input vector
     * @param func   the function to apply to each element
     * @param ctx    the operator context (must have function calling capability)
     * @return a new vector with the function applied to each element
     */
    private NodeConstant vectorMap(NodeVector vector, NodeFunction func, OperatorContext ctx) {
        Node[] elements = vector.getElements();
        var results = new Node[elements.length];

        for (int i = 0; i < elements.length; i++) {
            NodeConstant element = (NodeConstant) elements[i];
            results[i] = ctx.callFunction(func, List.of(element));
        }

        return new NodeVector(results);
    }
}

