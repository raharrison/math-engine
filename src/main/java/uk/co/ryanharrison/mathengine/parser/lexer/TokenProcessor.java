package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.KeywordRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Single-pass token processor that handles identifier splitting, classification,
 * and implicit multiplication insertion.
 * <p>
 * This class consolidates the functionality of three previous passes into one:
 * <ul>
 *     <li>Identifier splitting (e.g., "pi2e" → "pi", "2", "e")</li>
 *     <li>Token classification (IDENTIFIER → FUNCTION, KEYWORD, etc.)</li>
 *     <li>Implicit multiplication insertion (e.g., "2x" → "2 * x")</li>
 * </ul>
 *
 * <h2>Processing Rules:</h2>
 * <ul>
 *     <li>Assignment targets ({@code name :=}) are never split</li>
 *     <li>Function definitions ({@code name(...) :=}) are never split</li>
 *     <li>Digit boundaries always trigger splits ({@code pi2e})</li>
 *     <li>Function suffixes split only for single-char/constant/unit prefixes</li>
 *     <li>Implicit multiplication inserted between compatible adjacent tokens</li>
 * </ul>
 */
public final class TokenProcessor {

    private static final Set<TokenType> NUMBER_TYPES = Set.of(
            TokenType.INTEGER, TokenType.DECIMAL, TokenType.SCIENTIFIC, TokenType.RATIONAL
    );

    private static final Set<TokenType> RIGHT_MULTIPLICANDS = Set.of(
            TokenType.INTEGER, TokenType.DECIMAL, TokenType.SCIENTIFIC, TokenType.RATIONAL,
            TokenType.LPAREN, TokenType.IDENTIFIER, TokenType.UNIT,
            TokenType.UNIT_REF, TokenType.VAR_REF, TokenType.CONST_REF, TokenType.FUNCTION
    );

    private final Set<String> functionNames;
    private final UnitRegistry unitRegistry;
    private final ConstantRegistry constantRegistry;
    private final KeywordRegistry keywordRegistry;
    private final boolean implicitMultiplicationEnabled;

    public TokenProcessor(Set<String> functionNames,
                          UnitRegistry unitRegistry,
                          ConstantRegistry constantRegistry,
                          KeywordRegistry keywordRegistry,
                          boolean implicitMultiplicationEnabled) {
        this.functionNames = functionNames;
        this.unitRegistry = unitRegistry;
        this.constantRegistry = constantRegistry;
        this.keywordRegistry = keywordRegistry;
        this.implicitMultiplicationEnabled = implicitMultiplicationEnabled;
    }

    /**
     * Processes raw tokens in a single pass: splits, classifies, and inserts implicit multiplication.
     *
     * @param rawTokens tokens from TokenScanner
     * @return fully processed tokens ready for parsing
     */
    public List<Token> process(List<Token> rawTokens) {
        var result = new ArrayList<Token>(rawTokens.size() * 2); // Pre-size for potential expansions
        Token prev = null;

        for (int i = 0; i < rawTokens.size(); i++) {
            Token token = rawTokens.get(i);

            // Process token (may produce multiple tokens if split)
            List<Token> processed = processToken(token, rawTokens, i);

            // Add each processed token, inserting implicit multiplication as needed
            for (Token t : processed) {
                if (implicitMultiplicationEnabled && prev != null && shouldInsertMultiply(prev, t)) {
                    result.add(new Token(TokenType.MULTIPLY, "*", prev.line(), prev.column()));
                }
                result.add(t);
                prev = t;
            }
        }

        return result;
    }

    // ==================== Token Processing ====================

    /**
     * Processes a single token: splits if identifier, then classifies.
     */
    private List<Token> processToken(Token token, List<Token> allTokens, int index) {
        if (token.type() != TokenType.IDENTIFIER) {
            return List.of(token);
        }

        // Don't split definition targets
        if (isDefinitionTarget(allTokens, index)) {
            return List.of(classifyIdentifier(token));
        }

        // Try to split, then classify each part
        return splitAndClassify(token);
    }

    /**
     * Splits an identifier if needed, then classifies all resulting tokens.
     */
    private List<Token> splitAndClassify(Token token) {
        String text = token.lexeme();

        // If it's already a known entity, just classify
        if (isKnownIdentifier(text)) {
            return List.of(classifyIdentifier(token));
        }

        // Try digit split first (e.g., "pi2e")
        List<Token> digitSplit = trySplitAtDigits(token);
        if (digitSplit != null) {
            return classifyAll(digitSplit);
        }

        // Try function suffix split (e.g., "xsqrt")
        List<Token> funcSplit = trySplitAtFunctionSuffix(token);
        if (funcSplit != null) {
            return classifyAll(funcSplit);
        }

        // No split, just classify
        return List.of(classifyIdentifier(token));
    }

    private List<Token> classifyAll(List<Token> tokens) {
        var result = new ArrayList<Token>(tokens.size());
        for (Token t : tokens) {
            if (t.type() == TokenType.IDENTIFIER) {
                result.add(classifyIdentifier(t));
            } else {
                result.add(t);
            }
        }
        return result;
    }

    // ==================== Classification ====================

    /**
     * Classifies an identifier token into its specific type.
     */
    private Token classifyIdentifier(Token token) {
        String text = token.lexeme();

        // Keyword operators (and, or, xor, not, mod, of)
        Optional<TokenType> operatorType = keywordRegistry.getKeywordOperatorType(text);
        if (operatorType.isPresent()) {
            return token.withType(operatorType.get());
        }

        // Reserved keywords (for, in, if, step, true, false, to, as)
        if (keywordRegistry.isKeyword(text)) {
            return token.withType(TokenType.KEYWORD);
        }

        // Functions
        if (isFunction(text)) {
            return token.withType(TokenType.FUNCTION);
        }

        // Units resolved at evaluation time for context-aware resolution
        return token;
    }

    // ==================== Definition Target Detection ====================

    /**
     * Checks if identifier is an assignment target (name :=) or function definition (name(...) :=).
     */
    private boolean isDefinitionTarget(List<Token> tokens, int i) {
        if (i + 1 >= tokens.size()) {
            return false;
        }

        Token next = tokens.get(i + 1);

        // Direct assignment: name :=
        if (next.type() == TokenType.ASSIGN) {
            return true;
        }

        // Function definition: name(...) :=
        if (next.type() == TokenType.LPAREN) {
            return isFunctionDefinition(tokens, i);
        }

        return false;
    }

    private boolean isFunctionDefinition(List<Token> tokens, int i) {
        // Find matching RPAREN, then check for :=
        int depth = 1;
        int j = i + 2;

        while (j < tokens.size() && depth > 0) {
            TokenType type = tokens.get(j).type();
            if (type == TokenType.LPAREN) depth++;
            else if (type == TokenType.RPAREN) depth--;
            j++;
        }

        return depth == 0 && j < tokens.size() && tokens.get(j).type() == TokenType.ASSIGN;
    }

    // ==================== Identifier Splitting ====================

    private boolean isFunction(String text) {
        return functionNames.contains(text.toLowerCase());
    }

    private boolean isKnownIdentifier(String text) {
        return isFunction(text) ||
                unitRegistry.isUnit(text) ||
                constantRegistry.isConstant(text);
    }

    /**
     * Splits at digit boundaries: "pi2e" → ["pi", "2", "e"]
     */
    private List<Token> trySplitAtDigits(Token token) {
        String text = token.lexeme();
        int digitStart = findFirstDigit(text);

        if (digitStart <= 0) {
            return null;
        }

        String prefix = text.substring(0, digitStart);
        if (!isKnownIdentifier(prefix)) {
            return null;
        }

        int line = token.line();
        int col = token.column();

        // Find end of digit sequence
        int digitEnd = digitStart;
        while (digitEnd < text.length() && Character.isDigit(text.charAt(digitEnd))) {
            digitEnd++;
        }

        var result = new ArrayList<Token>();
        result.add(new Token(TokenType.IDENTIFIER, prefix, line, col));

        String digits = text.substring(digitStart, digitEnd);
        result.add(new Token(TokenType.INTEGER, digits, Long.parseLong(digits), line, col + digitStart));

        // Recursively process suffix
        if (digitEnd < text.length()) {
            String suffix = text.substring(digitEnd);
            var suffixToken = new Token(TokenType.IDENTIFIER, suffix, line, col + digitEnd);
            List<Token> suffixResult = splitAndClassify(suffixToken);
            // Un-classify for now (we'll classify all at once)
            for (Token t : suffixResult) {
                if (t.type() == TokenType.FUNCTION) {
                    result.add(new Token(TokenType.IDENTIFIER, t.lexeme(), t.line(), t.column()));
                } else {
                    result.add(t);
                }
            }
        }

        return result;
    }

    private int findFirstDigit(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Splits at function suffix: "xsqrt" → ["x", "sqrt"]
     * Only splits for single-char, constant, or unit prefixes (not function prefixes).
     */
    private List<Token> trySplitAtFunctionSuffix(Token token) {
        String text = token.lexeme();
        int line = token.line();
        int col = token.column();

        for (int i = 1; i < text.length(); i++) {
            String prefix = text.substring(0, i);
            String suffix = text.substring(i);

            if (isFunction(suffix) && shouldSplitForPrefix(prefix)) {
                var result = new ArrayList<Token>(2);
                result.add(new Token(TokenType.IDENTIFIER, prefix, line, col));
                result.add(new Token(TokenType.IDENTIFIER, suffix, line, col + i));
                return result;
            }
        }

        return null;
    }

    /**
     * Determines if splitting is appropriate for the given prefix.
     * Split for: single-char, constants, units. Don't split for: functions.
     */
    private boolean shouldSplitForPrefix(String prefix) {
        return prefix.length() == 1 ||
                constantRegistry.isConstant(prefix) ||
                unitRegistry.isUnit(prefix);
    }

    // ==================== Implicit Multiplication ====================

    /**
     * Determines if implicit multiplication should be inserted between two tokens.
     */
    private boolean shouldInsertMultiply(Token prev, Token next) {
        // Don't insert across different lines
        if (prev.line() != next.line()) {
            return false;
        }

        TokenType prevType = prev.type();
        TokenType nextType = next.type();

        // Function call: FUNCTION followed by LPAREN is NOT implicit multiplication
        if (prevType == TokenType.FUNCTION && nextType == TokenType.LPAREN) {
            return false;
        }

        // Number followed by right multiplicand or LPAREN
        if (isNumber(prevType)) {
            return isRightMultiplicand(nextType) || nextType == TokenType.LPAREN;
        }

        // RPAREN followed by LPAREN or right multiplicand
        if (prevType == TokenType.RPAREN) {
            return nextType == TokenType.LPAREN || isRightMultiplicand(nextType);
        }

        // RBRACE followed by right multiplicand or LPAREN
        if (prevType == TokenType.RBRACE) {
            return isRightMultiplicand(nextType) || nextType == TokenType.LPAREN;
        }

        // RBRACKET followed by right multiplicand (but not LPAREN - could be function call from array)
        if (prevType == TokenType.RBRACKET) {
            return isRightMultiplicand(nextType) && nextType != TokenType.LPAREN;
        }

        // Identifier/Unit followed by number, identifier, unit, or function
        if (isIdentifierOrUnit(prevType)) {
            return isNumber(nextType) || isIdentifierOrUnit(nextType) || nextType == TokenType.FUNCTION;
        }

        // Postfix operators followed by right multiplicand
        if (isPostfixOperator(prevType)) {
            return isRightMultiplicand(nextType);
        }

        return false;
    }

    private boolean isNumber(TokenType type) {
        return NUMBER_TYPES.contains(type);
    }

    private boolean isRightMultiplicand(TokenType type) {
        return RIGHT_MULTIPLICANDS.contains(type);
    }

    private boolean isIdentifierOrUnit(TokenType type) {
        return type == TokenType.IDENTIFIER || type == TokenType.UNIT ||
                type == TokenType.UNIT_REF || type == TokenType.VAR_REF || type == TokenType.CONST_REF;
    }

    private boolean isPostfixOperator(TokenType type) {
        return type == TokenType.FACTORIAL || type == TokenType.DOUBLE_FACTORIAL || type == TokenType.PERCENT;
    }
}
