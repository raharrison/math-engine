package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.KeywordRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.List;
import java.util.Set;

/**
 * Lexical analyzer (lexer/scanner) for the Math Engine.
 * <p>
 * Converts input text into a stream of tokens using a two-pass pipeline:
 * <ol>
 *     <li><b>Pass 1:</b> {@link TokenScanner} - Scan characters and produce raw tokens</li>
 *     <li><b>Pass 2:</b> {@link TokenProcessor} - Split, classify, and insert implicit multiplication</li>
 * </ol>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *     <li>Handles decimal vs range disambiguation (1.5 vs 1..5)</li>
 *     <li>Position tracking for error reporting</li>
 *     <li>Support for implicit multiplication (2x, 2(x+1))</li>
 *     <li>Compound identifier splitting (pi2e → pi * 2 * e)</li>
 *     <li>Assignment target protection (sumrange := 100 works)</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * Lexer lexer = new Lexer(
 *     functionNames,
 *     unitRegistry,
 *     constantRegistry,
 *     keywordRegistry,
 *     256,
 *     true);
 * List<Token> tokens = lexer.tokenize("sin(x) + cos(y)");
 * }</pre>
 *
 * @see Token
 * @see TokenType
 * @see LexerException
 */
public final class Lexer {

    private final TokenScanner tokenScanner;
    private final TokenProcessor tokenProcessor;

    /**
     * Creates a new lexer with full configuration.
     *
     * @param functionNames                 set of known function names (lowercase)
     * @param unitRegistry                  registry of known units
     * @param constantRegistry              registry of known constants
     * @param keywordRegistry               registry of known keywords
     * @param maxIdentifierLength           maximum allowed length for identifiers
     * @param implicitMultiplicationEnabled whether to insert implicit multiplication tokens
     */
    public Lexer(Set<String> functionNames,
                 UnitRegistry unitRegistry,
                 ConstantRegistry constantRegistry,
                 KeywordRegistry keywordRegistry,
                 int maxIdentifierLength,
                 boolean implicitMultiplicationEnabled) {
        this.tokenScanner = new TokenScanner(maxIdentifierLength);
        this.tokenProcessor = new TokenProcessor(
                functionNames,
                unitRegistry,
                constantRegistry,
                keywordRegistry,
                implicitMultiplicationEnabled
        );
    }

    /**
     * Tokenizes the input source code.
     * <p>
     * Runs the lexical analysis pipeline:
     * <ol>
     *     <li>Scan characters into raw tokens</li>
     *     <li>Process tokens (split, classify, insert implicit multiplication)</li>
     * </ol>
     *
     * @param source the source code to tokenize
     * @return list of tokens ready for parsing
     * @throws LexerException if a lexical error occurs
     */
    public List<Token> tokenize(String source) {
        List<Token> rawTokens = tokenScanner.scan(source);
        return tokenProcessor.process(rawTokens);
    }
}
