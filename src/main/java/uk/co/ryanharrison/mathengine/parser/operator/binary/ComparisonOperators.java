package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.ast.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;

import java.util.function.IntPredicate;

/**
 * The comparison operators.
 * <p>
 * Ordering ({@code < > <= >=}) works on numbers, unit quantities of the same kind
 * and strings, but not on collections. Equality ({@code == !=}) works on every type:
 * units are converted before comparing, and collections compare structurally.
 * The underlying rules live in {@link NodeConstant#compareTo} and
 * {@link NodeConstant#equalTo}.
 */
public final class ComparisonOperators {

    private ComparisonOperators() {
    }

    public static final BinaryOperator LESS_THAN = ordering("<", c -> c < 0);
    public static final BinaryOperator GREATER_THAN = ordering(">", c -> c > 0);
    public static final BinaryOperator LESS_THAN_OR_EQUAL = ordering("<=", c -> c <= 0);
    public static final BinaryOperator GREATER_THAN_OR_EQUAL = ordering(">=", c -> c >= 0);

    public static final BinaryOperator EQUAL = (left, right, ctx) -> NodeBoolean.of(left.equalTo(right));
    public static final BinaryOperator NOT_EQUAL = (left, right, ctx) -> NodeBoolean.of(!left.equalTo(right));

    private static BinaryOperator ordering(String symbol, IntPredicate accepts) {
        return (left, right, ctx) -> {
            if (left.isVector() || left.isMatrix() || right.isVector() || right.isMatrix()) {
                throw new TypeError("Cannot use '" + symbol +
                        "' on collections. Use '==' or '!=' to compare them.");
            }
            return NodeBoolean.of(accepts.test(left.compareTo(right)));
        };
    }
}
