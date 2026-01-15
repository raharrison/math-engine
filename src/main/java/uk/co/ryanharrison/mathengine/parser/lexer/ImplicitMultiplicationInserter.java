package uk.co.ryanharrison.mathengine.parser.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pass 3 of lexical analysis: Inserts implicit multiplication tokens.
 * <p>
 * Mathematical notation often omits multiplication symbols. This pass
 * inserts explicit MULTIPLY tokens where implicit multiplication is intended.
 *
 * <h2>Implicit Multiplication Rules:</h2>
 * <ul>
 *     <li>{@code 2x} → {@code 2 * x} (number times identifier)</li>
 *     <li>{@code 2(x+1)} → {@code 2 * (x+1)} (number times parenthesized expression)</li>
 *     <li>{@code (a)(b)} → {@code (a) * (b)} (two parenthesized expressions)</li>
 *     <li>{@code (x)y} → {@code (x) * y} (parenthesized times identifier)</li>
 *     <li>{@code pi2} → {@code pi * 2} (identifier times number, after splitting)</li>
 *     <li>{@code xy} → {@code x * y} (identifier times identifier, after splitting)</li>
 * </ul>
 *
 * <h2>Non-Implicit Cases:</h2>
 * <ul>
 *     <li>{@code sin(x)} - function call, not {@code sin * (x)}</li>
 *     <li>{@code 2 + 3} - explicit operator, no insertion needed</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * ImplicitMultiplicationInserter inserter = new ImplicitMultiplicationInserter();
 * List<Token> withMult = inserter.insert(classifiedTokens);
 * }</pre>
 */
public final class ImplicitMultiplicationInserter {

    /**
     * Token types that represent numeric literals.
     */
    private static final Set<TokenType> NUMBER_TYPES = Set.of(
            TokenType.INTEGER,
            TokenType.DECIMAL,
            TokenType.SCIENTIFIC,
            TokenType.RATIONAL
    );

    /**
     * Token types that can appear on the left side of implicit multiplication.
     * <p>
     * Note: RBRACKET is NOT included because `[1,2][0]` is subscripting, not multiplication.
     */
    private static final Set<TokenType> LEFT_MULTIPLICANDS = Set.of(
            TokenType.INTEGER,
            TokenType.DECIMAL,
            TokenType.SCIENTIFIC,
            TokenType.RATIONAL,
            TokenType.RPAREN,
            TokenType.IDENTIFIER,
            TokenType.UNIT,
            TokenType.FACTORIAL,    // After postfix operator
            TokenType.DOUBLE_FACTORIAL,
            TokenType.PERCENT
    );

    /**
     * Token types that can appear on the right side of implicit multiplication.
     * <p>
     * Note: LBRACKET is NOT included because `x[0]` is subscripting, not `x * [0]`.
     */
    private static final Set<TokenType> RIGHT_MULTIPLICANDS = Set.of(
            TokenType.INTEGER,
            TokenType.DECIMAL,
            TokenType.SCIENTIFIC,
            TokenType.RATIONAL,
            TokenType.LPAREN,
            TokenType.IDENTIFIER,
            TokenType.UNIT,
            TokenType.FUNCTION
    );

    /**
     * Creates a new implicit multiplication inserter.
     */
    public ImplicitMultiplicationInserter() {
    }

    /**
     * Inserts implicit multiplication tokens where appropriate.
     *
     * @param tokens the classified tokens from Pass 2
     * @return tokens with implicit multiplication inserted
     */
    public List<Token> insert(List<Token> tokens) {
        List<Token> result = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token current = tokens.get(i);
            Token next = (i + 1 < tokens.size()) ? tokens.get(i + 1) : null;

            result.add(current);

            if (next != null && shouldInsertMultiply(current, next)) {
                // Create virtual multiply token at current position
                Token multiplyToken = new Token(
                        TokenType.MULTIPLY, "*",
                        current.getLine(), current.getColumn()
                );
                result.add(multiplyToken);
            }
        }

        return result;
    }

    /**
     * Determines if implicit multiplication should be inserted between two tokens.
     *
     * @param current the current token
     * @param next    the next token
     * @return true if multiplication should be inserted
     */
    private boolean shouldInsertMultiply(Token current, Token next) {
        // Don't insert implicit multiplication across different lines
        if (current.getLine() != next.getLine()) {
            return false;
        }

        TokenType currType = current.getType();
        TokenType nextType = next.getType();

        // Function call: identifier/function followed by LPAREN is NOT implicit multiplication
        if ((currType == TokenType.FUNCTION) && nextType == TokenType.LPAREN) {
            return false;
        }

        // Number followed by identifier, unit, or function
        if (isNumber(currType) && isRightMultiplicand(nextType)) {
            return true;
        }

        // Number followed by LPAREN: 2(x+1)
        if (isNumber(currType) && nextType == TokenType.LPAREN) {
            return true;
        }

        // RPAREN followed by LPAREN: (a)(b)
        if (currType == TokenType.RPAREN && nextType == TokenType.LPAREN) {
            return true;
        }

        // RPAREN followed by identifier, unit, or number
        if (currType == TokenType.RPAREN && isRightMultiplicand(nextType)) {
            return true;
        }

        // RBRACE (vector close) followed by identifier, unit, or number
        // e.g., {1,2,3} meters -> {1,2,3} * meters (broadcast unit)
        if (currType == TokenType.RBRACE && isRightMultiplicand(nextType)) {
            return true;
        }

        // RBRACE followed by LPAREN: {1,2,3}(x) -> {1,2,3} * (x)
        if (currType == TokenType.RBRACE && nextType == TokenType.LPAREN) {
            return true;
        }

        // RBRACKET (matrix close) followed by identifier, unit, or number
        // e.g., [[1,2],[3,4]] meters -> [[1,2],[3,4]] * meters (broadcast unit)
        // Note: RBRACKET followed by LBRACKET is subscripting (e.g., [1,2][0]),
        // NOT implicit multiplication. LBRACKET is not in RIGHT_MULTIPLICANDS.
        // Note: RBRACKET followed by LPAREN could be a function call (e.g., funcs[0](10)),
        // so we don't insert implicit multiplication there either.
        if (currType == TokenType.RBRACKET && isRightMultiplicand(nextType) && nextType != TokenType.LPAREN) {
            return true;
        }

        // Identifier/Unit followed by number (e.g., pi2 -> pi * 2)
        if (isIdentifierOrUnit(currType) && isNumber(nextType)) {
            return true;
        }

        // Identifier/Unit followed by identifier/unit (e.g., xy -> x * y)
        // Note: Function calls are handled above
        if (isIdentifierOrUnit(currType) && isIdentifierOrUnit(nextType)) {
            return true;
        }

        // Identifier/Unit followed by function (e.g., xsin -> x * sin)
        if (isIdentifierOrUnit(currType) && nextType == TokenType.FUNCTION) {
            return true;
        }

        // Postfix operators followed by values
        if (isPostfixOperator(currType) && isRightMultiplicand(nextType)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the token type is a number literal.
     */
    private boolean isNumber(TokenType type) {
        return NUMBER_TYPES.contains(type);
    }

    /**
     * Checks if the token type can be a right multiplicand.
     */
    private boolean isRightMultiplicand(TokenType type) {
        return RIGHT_MULTIPLICANDS.contains(type);
    }

    /**
     * Checks if the token type is an identifier or unit.
     */
    private boolean isIdentifierOrUnit(TokenType type) {
        return type == TokenType.IDENTIFIER || type == TokenType.UNIT;
    }

    /**
     * Checks if the token type is a postfix operator.
     */
    private boolean isPostfixOperator(TokenType type) {
        return type == TokenType.FACTORIAL ||
                type == TokenType.DOUBLE_FACTORIAL ||
                type == TokenType.PERCENT;
    }
}
