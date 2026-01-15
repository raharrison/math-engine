package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.MatrixOperations;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeMatrix;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;

/**
 * Matrix multiplication operator (@).
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
public final class MatrixMultiplyOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final MatrixMultiplyOperator INSTANCE = new MatrixMultiplyOperator();

    private MatrixMultiplyOperator() {
    }

    @Override
    public String symbol() {
        return "@";
    }

    @Override
    public String displayName() {
        return "matrix multiplication";
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
            return vectorMap(vector, func.getFunction(), ctx);
        }

        throw new TypeError("@ operator requires (Matrix @ Matrix), (Vector @ Vector), or (Vector @ Function), got " +
                left.getClass().getSimpleName() + " @ " + right.getClass().getSimpleName());
    }

    /**
     * Applies a function to each element of a vector (map operation).
     * {1,2,3} @ (x -> x*2) → {2,4,6}
     */
    private NodeConstant vectorMap(NodeVector vector, FunctionDefinition func, OperatorContext ctx) {
        // Note: This requires access to the evaluator to call the function.
        // For now, we throw an error. The Evaluator will handle this specially.
        throw new UnsupportedOperationException(
                "Vector @ Function map operation must be handled by the Evaluator");
    }
}

