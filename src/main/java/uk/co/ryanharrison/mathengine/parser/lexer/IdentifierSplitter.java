package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.FunctionRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Pass 1.5 of lexical analysis: Splits compound identifiers into separate tokens.
 * <p>
 * Handles cases where multiple logical tokens are written without spaces:
 * <ul>
 *     <li>{@code "pi2e"} → {@code "pi"}, {@code "2"}, {@code "e"} (constant, number, constant)</li>
 *     <li>{@code "xsqrt"} → {@code "x"}, {@code "sqrt"} (identifier, function)</li>
 *     <li>{@code "2sin"} → already split by Pass 1 (number then identifier)</li>
 * </ul>
 *
 * <h2>Split Rules:</h2>
 * <ol>
 *     <li>If the identifier is a known function/unit/constant, don't split</li>
 *     <li>Split at digit boundaries when prefix is a known identifier</li>
 *     <li>Split at function suffix when a known function appears at the end</li>
 * </ol>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * IdentifierSplitter splitter = new IdentifierSplitter(
 *     functionRegistry, unitRegistry, constantRegistry);
 * List<Token> split = splitter.split(rawTokens);
 * }</pre>
 */
public final class IdentifierSplitter {

    private final FunctionRegistry functionRegistry;
    private final UnitRegistry unitRegistry;
    private final ConstantRegistry constantRegistry;

    /**
     * Creates a new identifier splitter with the given registries.
     *
     * @param functionRegistry registry of known functions
     * @param unitRegistry     registry of known units
     * @param constantRegistry registry of known constants
     */
    public IdentifierSplitter(FunctionRegistry functionRegistry,
                              UnitRegistry unitRegistry,
                              ConstantRegistry constantRegistry) {
        this.functionRegistry = functionRegistry;
        this.unitRegistry = unitRegistry;
        this.constantRegistry = constantRegistry;
    }

    /**
     * Splits compound identifiers in the token list.
     *
     * @param tokens the raw tokens from Pass 1
     * @return tokens with compound identifiers split
     */
    public List<Token> split(List<Token> tokens) {
        List<Token> result = new ArrayList<>();

        for (Token token : tokens) {
            if (token.type() == TokenType.IDENTIFIER) {
                List<Token> splitTokens = trySplitIdentifier(token);
                result.addAll(splitTokens);
            } else {
                result.add(token);
            }
        }

        return result;
    }

    /**
     * Attempts to split a compound identifier into multiple tokens.
     *
     * @param token the identifier token to potentially split
     * @return list containing split tokens, or just the original token if no split
     */
    private List<Token> trySplitIdentifier(Token token) {
        String text = token.lexeme();

        // If it's a known function/unit/constant as-is, don't split
        if (isKnownIdentifier(text)) {
            return List.of(token);
        }

        // Try to split at digit boundaries (e.g., "pi2e" -> "pi", "2", "e")
        List<Token> digitSplit = trySplitAtDigits(token);
        if (digitSplit != null) {
            return digitSplit;
        }

        // Try to split at function suffix (e.g., "xsqrt" -> "x", "sqrt")
        List<Token> funcSplit = trySplitAtFunctionSuffix(token);
        if (funcSplit != null) {
            return funcSplit;
        }

        // No split possible
        return List.of(token);
    }

    /**
     * Checks if the identifier is known (function, unit, or constant).
     *
     * @param text the identifier text
     * @return true if known
     */
    private boolean isKnownIdentifier(String text) {
        return functionRegistry.isFunction(text) ||
                unitRegistry.isUnit(text) ||
                constantRegistry.isConstant(text);
    }

    /**
     * Attempts to split an identifier at digit boundaries.
     * <p>
     * Example: {@code "pi2e"} → {@code ["pi", "2", "e"]}
     *
     * @param token the token to split
     * @return split tokens, or null if no split possible
     */
    private List<Token> trySplitAtDigits(Token token) {
        String text = token.lexeme();
        int line = token.line();
        int col = token.column();

        // Find first digit
        int digitStart = findFirstDigit(text);

        // No digits in identifier
        if (digitStart == -1) {
            return null;
        }

        // Must have a prefix before the digit
        if (digitStart == 0) {
            return null;
        }

        String prefix = text.substring(0, digitStart);

        // Check if prefix is a valid known identifier (constant or function)
        if (!isKnownIdentifier(prefix)) {
            return null;  // Prefix is not known, don't split
        }

        // Find end of digit sequence
        int digitEnd = digitStart;
        while (digitEnd < text.length() && Character.isDigit(text.charAt(digitEnd))) {
            digitEnd++;
        }

        String digits = text.substring(digitStart, digitEnd);
        String suffix = text.substring(digitEnd);

        List<Token> result = new ArrayList<>();

        // Add prefix as identifier
        result.add(new Token(TokenType.IDENTIFIER, prefix, line, col));

        // Add digits as integer
        long numberValue = Long.parseLong(digits);
        result.add(new Token(TokenType.INTEGER, digits, numberValue, line, col + digitStart));

        // If there's a suffix, recursively process it
        if (!suffix.isEmpty()) {
            Token suffixToken = new Token(TokenType.IDENTIFIER, suffix, line, col + digitEnd);
            List<Token> suffixTokens = trySplitIdentifier(suffixToken);
            result.addAll(suffixTokens);
        }

        return result;
    }

    /**
     * Finds the index of the first digit in the string.
     *
     * @param text the string to search
     * @return index of first digit, or -1 if none
     */
    private int findFirstDigit(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Attempts to split an identifier at a known function suffix.
     * <p>
     * Example: {@code "xsqrt"} → {@code ["x", "sqrt"]}
     * <p>
     * Only splits when the prefix is a known identifier or a single character,
     * preventing legitimate compound identifiers like "sumrange" from being
     * incorrectly split.
     *
     * @param token the token to split
     * @return split tokens, or null if no split possible
     */
    private List<Token> trySplitAtFunctionSuffix(Token token) {
        String text = token.lexeme();
        int line = token.line();
        int col = token.column();

        // Try progressively shorter prefixes to find the longest matching suffix
        for (int i = 1; i < text.length(); i++) {
            String prefix = text.substring(0, i);
            String suffix = text.substring(i);

            // Check if suffix is a known function
            if (functionRegistry.isFunction(suffix)) {
                // Only split if prefix is a known identifier or single character
                // This prevents "sumrange" from splitting while allowing "xsin" or "pilog"
                if (isKnownIdentifier(prefix) || prefix.length() == 1) {
                    List<Token> result = new ArrayList<>();
                    result.add(new Token(TokenType.IDENTIFIER, prefix, line, col));
                    result.add(new Token(TokenType.IDENTIFIER, suffix, line, col + i));
                    return result;
                }
            }
        }

        return null;
    }
}
