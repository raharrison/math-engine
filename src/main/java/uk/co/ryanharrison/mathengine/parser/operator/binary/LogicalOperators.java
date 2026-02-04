package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeMatrix;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;

/**
 * Collection of logical operators.
 * <p>
 * Includes AND, OR, and XOR operators with broadcasting support.
 * </p>
 * <ul>
 *     <li>Scalar operations: {@code true && false} → {@code false}</li>
 *     <li>Vector operations: {@code {true, false} && {true, true}} → {@code {true, false}}</li>
 *     <li>Broadcasting: {@code {true, false, true} && true} → {@code {true, false, true}}</li>
 * </ul>
 * <p>
 * Note: Short-circuit evaluation only applies to scalar operations.
 * </p>
 */
public final class LogicalOperators {

    private LogicalOperators() {
    }

    /**
     * Logical AND operator (&&, and).
     * Supports short-circuit evaluation for scalars and broadcasting for vectors.
     */
    public static final BinaryOperator AND = new BinaryOperator() {
        @Override
        public boolean requiresShortCircuit() {
            return true;
        }

        @Override
        public NodeConstant shortCircuitResult(NodeConstant leftValue, OperatorContext ctx) {
            // Short-circuit only for scalar values
            if (leftValue instanceof NodeBoolean bool && !bool.getValue()) {
                return NodeBoolean.FALSE;
            }
            return null; // Continue evaluation
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            // Logical operators only work on scalars
            if (left instanceof NodeVector || left instanceof NodeMatrix ||
                    right instanceof NodeVector || right instanceof NodeMatrix) {
                throw new uk.co.ryanharrison.mathengine.parser.evaluator.TypeError(
                        "Logical AND (&&) does not work on containers");
            }

            boolean lVal = ctx.toBoolean(left);
            boolean rVal = ctx.toBoolean(right);
            return NodeBoolean.of(lVal && rVal);
        }
    };

    /**
     * Logical OR operator (||, or).
     * Supports short-circuit evaluation for scalars and broadcasting for vectors.
     */
    public static final BinaryOperator OR = new BinaryOperator() {
        @Override
        public boolean requiresShortCircuit() {
            return true;
        }

        @Override
        public NodeConstant shortCircuitResult(NodeConstant leftValue, OperatorContext ctx) {
            // Short-circuit only for scalar values
            if (leftValue instanceof NodeBoolean bool && bool.getValue()) {
                return NodeBoolean.TRUE;
            }
            return null; // Continue evaluation
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            // Logical operators only work on scalars
            if (left instanceof NodeVector || left instanceof NodeMatrix ||
                    right instanceof NodeVector || right instanceof NodeMatrix) {
                throw new uk.co.ryanharrison.mathengine.parser.evaluator.TypeError(
                        "Logical OR (||) does not work on containers");
            }

            boolean lVal = ctx.toBoolean(left);
            boolean rVal = ctx.toBoolean(right);
            return NodeBoolean.of(lVal || rVal);
        }
    };

    /**
     * Logical XOR operator (xor).
     * Supports broadcasting but not short-circuit evaluation.
     */
    public static final BinaryOperator XOR = (left, right, ctx) -> {
        // Logical operators only work on scalars
        if (left instanceof NodeVector || left instanceof NodeMatrix ||
                right instanceof NodeVector || right instanceof NodeMatrix) {
            throw new uk.co.ryanharrison.mathengine.parser.evaluator.TypeError(
                    "Logical XOR (xor) does not work on containers");
        }

        boolean lVal = ctx.toBoolean(left);
        boolean rVal = ctx.toBoolean(right);
        return NodeBoolean.of(lVal ^ rVal);
    };
}
