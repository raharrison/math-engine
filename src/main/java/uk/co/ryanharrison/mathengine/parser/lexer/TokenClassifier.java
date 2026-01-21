package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.registry.FunctionRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.KeywordRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pass 2 of lexical analysis: Classifies identifier tokens.
 * <p>
 * Transforms generic IDENTIFIER tokens into more specific types:
 * <ul>
 *     <li>KEYWORD - reserved words (for, in, if, step, true, false)</li>
 *     <li>AND, OR, XOR, NOT, MOD, OF - keyword operators</li>
 *     <li>FUNCTION - function names from the function registry</li>
 *     <li>IDENTIFIER - remains as generic identifier (variables, etc.)</li>
 * </ul>
 *
 * <h2>Classification Priority:</h2>
 * <ol>
 *     <li>Keyword operators (and, or, xor, not, mod, of)</li>
 *     <li>Reserved keywords (for, in, if, step, true, false, to, as)</li>
 *     <li>Functions (from FunctionRegistry)</li>
 *     <li>Generic identifier (default)</li>
 * </ol>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * TokenClassifier classifier = new TokenClassifier(
 *     functionRegistry, keywordRegistry);
 * List<Token> classified = classifier.classify(splitTokens);
 * }</pre>
 */
public final class TokenClassifier {

    private final FunctionRegistry functionRegistry;
    private final KeywordRegistry keywordRegistry;

    /**
     * Creates a new token classifier with the given registries.
     *
     * @param functionRegistry registry of known functions
     * @param keywordRegistry  registry of known keywords
     */
    public TokenClassifier(FunctionRegistry functionRegistry,
                           KeywordRegistry keywordRegistry) {
        this.functionRegistry = functionRegistry;
        this.keywordRegistry = keywordRegistry;
    }

    /**
     * Classifies identifier tokens in the token list.
     *
     * @param tokens the tokens from Pass 1.5 (with split identifiers)
     * @return tokens with identifiers classified
     */
    public List<Token> classify(List<Token> tokens) {
        var classified = new ArrayList<Token>();

        for (Token token : tokens) {
            if (token.type() == TokenType.IDENTIFIER) {
                classified.add(classifyIdentifier(token));
            } else {
                classified.add(token);
            }
        }

        return classified;
    }

    /**
     * Classifies a single identifier token.
     *
     * @param token the identifier token
     * @return the classified token (may have different type)
     */
    private Token classifyIdentifier(Token token) {
        String text = token.lexeme();

        // Check if it's a keyword operator (convert to proper token type)
        Optional<TokenType> operatorType = keywordRegistry.getKeywordOperatorType(text);
        if (operatorType.isPresent()) {
            return token.withType(operatorType.get());
        }

        // Check if it's a non-operator keyword
        if (keywordRegistry.isKeyword(text)) {
            return token.withType(TokenType.KEYWORD);
        }

        // Check if it's a function
        if (functionRegistry.isFunction(text)) {
            return token.withType(TokenType.FUNCTION);
        }

        // Don't classify as UNIT here - units are resolved at evaluation time
        // based on context (POSTFIX_UNIT vs GENERAL) by VariableResolver.
        // This allows variables to shadow unit names and provides proper
        // context-aware resolution.

        // Keep as identifier - will be resolved by VariableResolver based on context
        return token;
    }
}
