package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.MatrixOperations;

import java.util.List;

/**
 * The {@code @} operator, for products that are not element-wise:
 * matrix times matrix, vector dot vector, and vector mapped through a function
 * ({@code {1,2,3} @ (x -> x*2)}).
 * <p>
 * For element-wise multiplication use {@link MultiplyOperator}.
 */
public final class AtOperator implements BinaryOperator {

    public static final AtOperator INSTANCE = new AtOperator();

    private AtOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        if (left instanceof NodeMatrix leftMatrix && right instanceof NodeMatrix rightMatrix) {
            return MatrixOperations.multiply(leftMatrix, rightMatrix);
        }
        if (left instanceof NodeVector leftVector && right instanceof NodeVector rightVector) {
            return MatrixOperations.dotProduct(leftVector, rightVector);
        }
        if (left instanceof NodeVector vector && right instanceof NodeFunction function) {
            return map(vector, function, ctx);
        }

        throw new TypeError("@ requires (matrix @ matrix), (vector @ vector) or (vector @ function), got: " +
                left.typeName() + " @ " + right.typeName());
    }

    private NodeConstant map(NodeVector vector, NodeFunction function, OperatorContext ctx) {
        var results = new Node[vector.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = ctx.callFunction(function, List.of((NodeConstant) vector.getElement(i)));
        }
        return new NodeVector(results);
    }
}
