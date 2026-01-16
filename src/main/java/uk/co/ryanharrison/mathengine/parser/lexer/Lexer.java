package uk.co.ryanharrison.mathengine.parser.lexer;

import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.FunctionRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.KeywordRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.List;

/**
 * Lexical analyzer (lexer/scanner) for the Math Engine.
 * <p>
 * Converts input text into a stream of tokens using a multi-pass pipeline:
 * <ol>
 *     <li><b>Pass 1:</b> {@link TokenScanner} - Scan characters and produce raw tokens</li>
 *     <li><b>Pass 1.5:</b> {@link IdentifierSplitter} - Split compound identifiers (e.g., "pi2e")</li>
 *     <li><b>Pass 2:</b> {@link TokenClassifier} - Classify identifiers (keywords, units, functions)</li>
 *     <li><b>Pass 3:</b> {@link ImplicitMultiplicationInserter} - Insert implicit multiplication tokens</li>
 * </ol>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *     <li>Handles decimal vs range disambiguation (1.5 vs 1..5)</li>
 *     <li>Extensible registries for operators, functions, units, constants, and keywords</li>
 *     <li>Position tracking for error reporting</li>
 *     <li>Support for implicit multiplication (2x, 2(x+1))</li>
 *     <li>Compound identifier splitting (pi2e → pi * 2 * e)</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // With all registries
 * Lexer lexer = new Lexer(
 *     functionRegistry,
 *     unitRegistry,
 *     constantRegistry,
 *     keywordRegistry);
 * List<Token> tokens = lexer.tokenize("sin(x) + cos(y)");
 * }</pre>
 *
 * <h2>Architecture:</h2>
 * <p>
 * The Lexer is a facade that orchestrates the lexical analysis pipeline.
 * Each pass is handled by a dedicated, focused component:
 * </p>
 * <ul>
 *     <li>{@link TokenScanner} - Character-level scanning</li>
 *     <li>{@link IdentifierSplitter} - Token splitting</li>
 *     <li>{@link TokenClassifier} - Token classification</li>
 *     <li>{@link ImplicitMultiplicationInserter} - Implicit multiplication</li>
 * </ul>
 *
 * @see Token
 * @see TokenType
 * @see LexerException
 */
public final class Lexer {

    private final TokenScanner tokenScanner;
    private final IdentifierSplitter identifierSplitter;
    private final TokenClassifier tokenClassifier;
    private final ImplicitMultiplicationInserter implicitMultiplicationInserter;
    private final boolean implicitMultiplicationEnabled;

    /**
     * Creates a new lexer with full configuration.
     *
     * @param functionRegistry              registry of known functions
     * @param unitRegistry                  registry of known units
     * @param constantRegistry              registry of known constants
     * @param keywordRegistry               registry of known keywords
     * @param maxIdentifierLength           maximum allowed length for identifiers
     * @param implicitMultiplicationEnabled whether to insert implicit multiplication tokens
     */
    public Lexer(FunctionRegistry functionRegistry,
                 UnitRegistry unitRegistry,
                 ConstantRegistry constantRegistry,
                 KeywordRegistry keywordRegistry,
                 int maxIdentifierLength,
                 boolean implicitMultiplicationEnabled) {
        this.tokenScanner = new TokenScanner(maxIdentifierLength);
        this.identifierSplitter = new IdentifierSplitter(functionRegistry, unitRegistry, constantRegistry);
        this.tokenClassifier = new TokenClassifier(functionRegistry, keywordRegistry);
        this.implicitMultiplicationInserter = new ImplicitMultiplicationInserter();
        this.implicitMultiplicationEnabled = implicitMultiplicationEnabled;
    }

    /**
     * Tokenizes the input source code.
     * <p>
     * Runs the complete lexical analysis pipeline:
     * <ol>
     *     <li>Scan characters into raw tokens</li>
     *     <li>Split compound identifiers</li>
     *     <li>Classify identifiers</li>
     *     <li>Insert implicit multiplication</li>
     * </ol>
     *
     * @param source the source code to tokenize
     * @return list of tokens ready for parsing
     * @throws LexerException if a lexical error occurs
     */
    public List<Token> tokenize(String source) {
        // Pass 1: Scan characters and produce raw tokens
        List<Token> rawTokens = tokenScanner.scan(source);

        // Pass 1.5: Split compound identifiers (e.g., "pi2e" -> "pi", "2", "e")
        List<Token> splitTokens = identifierSplitter.split(rawTokens);

        // Pass 2: Classify identifiers (keywords, units, functions)
        List<Token> classifiedTokens = tokenClassifier.classify(splitTokens);

        // Pass 3: Insert implicit multiplication (e.g., "2x" -> "2 * x") if enabled
        if (implicitMultiplicationEnabled) {
            return implicitMultiplicationInserter.insert(classifiedTokens);
        }
        return classifiedTokens;
    }
}
