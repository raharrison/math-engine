package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;

import java.math.BigInteger;

/**
 * Modulo operator (mod).
 * <p>
 * Returns the floor modulo (not Java's remainder operator %).
 * Floor modulo always returns non-negative results for positive divisors.
 * </p>
 * <p>
 * Formula: a mod b = a - b * floor(a / b)
 * </p>
 * <p>
 * Examples:
 * <ul>
 *     <li>7 mod 3 = 1</li>
 *     <li>-7 mod 3 = 2 (not -1 like Java's %)</li>
 *     <li>10 mod 5 = 0</li>
 * </ul>
 * </p>
 * <p>
 * Supports broadcasting over vectors and matrices.
 * </p>
 */
public final class ModOperator implements BinaryOperator {

    /**
     * Singleton instance
     */
    public static final ModOperator INSTANCE = new ModOperator();

    private ModOperator() {
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return BroadcastingEngine.applyBinary(left, right, (l, r) ->
                NumericOperations.applyNumeric(l, r,
                        (a, b) -> a - b * Math.floor(a / b),
                        ModOperator::floorMod,
                        false));
    }

    /**
     * Floor modulo for BigRational: {@code a - b * floor(a/b)}.
     */
    private static BigRational floorMod(BigRational a, BigRational b) {
        BigRational quotient = a.divide(b);
        BigInteger floor = floorBigRational(quotient);
        return a.subtract(b.multiply(BigRational.of(floor)));
    }

    /**
     * Floor of a BigRational — largest integer <= the value.
     */
    private static BigInteger floorBigRational(BigRational r) {
        BigInteger[] divRem = r.getNumerator().divideAndRemainder(r.getDenominator());
        // BigInteger division truncates toward zero; adjust for negative non-integers
        if (divRem[1].signum() < 0) {
            return divRem[0].subtract(BigInteger.ONE);
        }
        return divRem[0];
    }
}
