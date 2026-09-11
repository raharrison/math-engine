package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.ast.NodeRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;
import uk.co.ryanharrison.mathengine.utils.MathUtils;

/**
 * Factorial ({@code n!}), element-wise over collections. Integers give an exact
 * result; other values go through the gamma function.
 */
public final class FactorialOperator implements UnaryOperator {

    public static final FactorialOperator INSTANCE = new FactorialOperator();

    private FactorialOperator() {
    }

    @Override
    public Position position() {
        return Position.POSTFIX;
    }

    @Override
    public NodeConstant apply(NodeConstant operand, OperatorContext ctx) {
        return BroadcastingEngine.applyUnary(operand, value -> {
            double n = TypeCoercion.toDouble(value);
            if (n < 0) {
                throw new DomainException("Factorial is not defined for negative numbers: " + n);
            }
            if (Math.floor(n) != n || Double.isInfinite(n)) {
                return new NodeDouble(MathUtils.factorial(n));
            }
            if (n > MathUtils.MAX_EXACT_FACTORIAL) {
                throw new DomainException("Factorial of " + (long) n +
                        " is beyond the exact arithmetic limit of " + MathUtils.MAX_EXACT_FACTORIAL);
            }
            return new NodeRational(BigRational.of(MathUtils.factorialExact((long) n)));
        });
    }
}
