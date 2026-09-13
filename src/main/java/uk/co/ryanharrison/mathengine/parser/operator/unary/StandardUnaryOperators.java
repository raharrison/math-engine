package uk.co.ryanharrison.mathengine.parser.operator.unary;

import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;

import java.util.Map;

/**
 * Provides all standard unary operators as a map keyed by {@link TokenType}.
 * <p>
 * Use {@link #all()} to get a map suitable for registration with an
 * {@link uk.co.ryanharrison.mathengine.parser.operator.OperatorExecutor}.
 *
 * <h2>Included Operators:</h2>
 * <ul>
 *     <li>Prefix: -, +, not</li>
 *     <li>Postfix: !, !!, %</li>
 * </ul>
 */
public final class StandardUnaryOperators {

    private StandardUnaryOperators() {
    }

    /**
     * Gets all standard unary operators as a map.
     *
     * @return immutable map of token types to operators
     */
    public static Map<TokenType, UnaryOperator> all() {
        return ALL;
    }

    private static final Map<TokenType, UnaryOperator> ALL = buildAll();

    private static Map<TokenType, UnaryOperator> buildAll() {
        return Map.of(
                TokenType.MINUS, NegateOperator.INSTANCE,
                TokenType.PLUS, UnaryPlusOperator.INSTANCE,
                TokenType.NOT, NotOperator.INSTANCE,
                TokenType.FACTORIAL, FactorialOperator.INSTANCE,
                TokenType.DOUBLE_FACTORIAL, DoubleFactorialOperator.INSTANCE,
                TokenType.PERCENT, PercentOperator.INSTANCE
        );
    }

}
