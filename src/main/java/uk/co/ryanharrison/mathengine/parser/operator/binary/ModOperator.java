package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.BroadcastingDispatcher;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;

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
    public String symbol() {
        return "mod";
    }

    @Override
    public String displayName() {
        return "modulo";
    }

    @Override
    public int precedence() {
        return 7;
    }

    @Override
    public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
        return BroadcastingDispatcher.dispatch(left, right, ctx, (l, r) -> {
            double lVal = ctx.toNumber(l).doubleValue();
            double rVal = ctx.toNumber(r).doubleValue();

            // Use floor modulo: a mod b = a - b * floor(a / b)
            // This differs from Java's % which is remainder
            double result = lVal - rVal * Math.floor(lVal / rVal);
            return new NodeDouble(result);
        });
    }
}
