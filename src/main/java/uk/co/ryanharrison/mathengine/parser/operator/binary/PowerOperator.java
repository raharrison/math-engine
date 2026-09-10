package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeMatrix;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeNumber;
import uk.co.ryanharrison.mathengine.parser.util.MatrixOperations;

/**
 * Exponentiation ({@code ^}), right-associative.
 * <p>
 * A matrix raised to an integer power means repeated matrix multiplication;
 * every other case is element-wise. Stays exact for a rational base and an
 * integer exponent.
 */
public final class PowerOperator implements BinaryOperator {

    public static final PowerOperator INSTANCE = new PowerOperator();

    private PowerOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        if (left instanceof NodeMatrix matrix && right instanceof NodeNumber) {
            double exponent = right.doubleValue();
            if (exponent == Math.floor(exponent) && !Double.isInfinite(exponent)) {
                return MatrixOperations.power(matrix, (int) exponent);
            }
        }
        return left.power(right);
    }
}
