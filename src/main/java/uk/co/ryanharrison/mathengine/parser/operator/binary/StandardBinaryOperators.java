package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;

import java.util.Map;

/**
 * Provides all standard binary operators as a map keyed by {@link TokenType}.
 * <p>
 * Use {@link #all()} to get a map suitable for registration with an
 * {@link uk.co.ryanharrison.mathengine.parser.operator.OperatorExecutor}.
 *
 * <h2>Included Operators:</h2>
 * <ul>
 *     <li>Arithmetic: +, -, *, /, ^, mod</li>
 *     <li>Comparison: <, >, <=, >=, ==, !=</li>
 *     <li>Logical: &&, ||, xor</li>
 *     <li>Special: of, @</li>
 * </ul>
 */
public final class StandardBinaryOperators {

    private StandardBinaryOperators() {
    }

    /**
     * Gets all standard binary operators as a map.
     *
     * @return immutable map of token types to operators
     */
    public static Map<TokenType, BinaryOperator> all() {
        return Map.ofEntries(
                // Arithmetic
                Map.entry(TokenType.PLUS, PlusOperator.INSTANCE),
                Map.entry(TokenType.MINUS, MinusOperator.INSTANCE),
                Map.entry(TokenType.MULTIPLY, MultiplyOperator.INSTANCE),
                Map.entry(TokenType.DIVIDE, DivideOperator.INSTANCE),
                Map.entry(TokenType.POWER, PowerOperator.INSTANCE),
                Map.entry(TokenType.MOD, ModOperator.INSTANCE),

                // Comparison
                Map.entry(TokenType.LT, ComparisonOperators.LESS_THAN),
                Map.entry(TokenType.GT, ComparisonOperators.GREATER_THAN),
                Map.entry(TokenType.LTE, ComparisonOperators.LESS_THAN_OR_EQUAL),
                Map.entry(TokenType.GTE, ComparisonOperators.GREATER_THAN_OR_EQUAL),
                Map.entry(TokenType.EQ, ComparisonOperators.EQUAL),
                Map.entry(TokenType.NEQ, ComparisonOperators.NOT_EQUAL),

                // Logical
                Map.entry(TokenType.AND, LogicalOperators.AND),
                Map.entry(TokenType.OR, LogicalOperators.OR),
                Map.entry(TokenType.XOR, LogicalOperators.XOR),

                // Special
                Map.entry(TokenType.OF, OfOperator.INSTANCE),
                Map.entry(TokenType.AT, AtOperator.INSTANCE)
        );
    }

    /**
     * Gets only arithmetic operators.
     *
     * @return map of arithmetic operators
     */
    public static Map<TokenType, BinaryOperator> arithmetic() {
        return Map.of(
                TokenType.PLUS, PlusOperator.INSTANCE,
                TokenType.MINUS, MinusOperator.INSTANCE,
                TokenType.MULTIPLY, MultiplyOperator.INSTANCE,
                TokenType.DIVIDE, DivideOperator.INSTANCE,
                TokenType.POWER, PowerOperator.INSTANCE,
                TokenType.MOD, ModOperator.INSTANCE
        );
    }

    /**
     * Gets only comparison operators.
     *
     * @return map of comparison operators
     */
    public static Map<TokenType, BinaryOperator> comparison() {
        return Map.of(
                TokenType.LT, ComparisonOperators.LESS_THAN,
                TokenType.GT, ComparisonOperators.GREATER_THAN,
                TokenType.LTE, ComparisonOperators.LESS_THAN_OR_EQUAL,
                TokenType.GTE, ComparisonOperators.GREATER_THAN_OR_EQUAL,
                TokenType.EQ, ComparisonOperators.EQUAL,
                TokenType.NEQ, ComparisonOperators.NOT_EQUAL
        );
    }

    /**
     * Gets only logical operators.
     *
     * @return map of logical operators
     */
    public static Map<TokenType, BinaryOperator> logical() {
        return Map.of(
                TokenType.AND, LogicalOperators.AND,
                TokenType.OR, LogicalOperators.OR,
                TokenType.XOR, LogicalOperators.XOR
        );
    }
}
