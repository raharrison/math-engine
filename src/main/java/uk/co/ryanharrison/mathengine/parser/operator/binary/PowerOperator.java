package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.MatrixOperations;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeMatrix;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeNumber;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;

/**
 * Power/exponentiation operator (^).
 * <p>
 * Supports full type preservation for all first-class numeric types:
 * <ul>
 *     <li>Unit ^ scalar → unit (e.g., (10 meters)^2 = 100 meters)</li>
 *     <li>Percent ^ scalar → percent (e.g., 50%^2 = 25%)</li>
 *     <li>Rational ^ integer → rational (preserves exactness)</li>
 *     <li>Vector element-wise exponentiation with broadcasting</li>
 *     <li>Matrix power for integer exponents (A^n = A * A * ... * A)</li>
 *     <li>Matrix element-wise power for non-integer exponents</li>
 * </ul>
 * <p>
 * This operator is right-associative: a^b^c = a^(b^c)
 */
public final class PowerOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final PowerOperator INSTANCE = new PowerOperator();

    private PowerOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        // Special case: Matrix ^ Integer = Matrix exponentiation (repeated multiplication)
        if (left instanceof NodeMatrix matrix && right instanceof NodeNumber) {
            double exp = right.doubleValue();

            // For integer exponents, use matrix power (A^n = A * A * ... * A)
            if (exp == Math.floor(exp) && !Double.isInfinite(exp)) {
                return MatrixOperations.power(matrix, (int) exp, ctx);
            }

            // For non-integer exponents, fall through to element-wise broadcasting
        }

        // Default: Use broadcasting dispatcher for element-wise power with type preservation
        return BroadcastingEngine.applyBinary(left, right,
                (l, r) -> NumericOperations.applyPower(l, r, ctx.forceDoubleArithmetic()));
    }
}
