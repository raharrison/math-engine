package uk.co.ryanharrison.mathengine.parser.parser;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeSequence;

import java.util.ArrayList;
import java.util.List;

/**
 * Recursive descent parser that builds an Abstract Syntax Tree (AST) from a stream of tokens.
 * <p>
 * This is the main entry point for parsing. It coordinates the parsing pipeline using
 * specialized components:
 * <ul>
 *     <li>{@link TokenStream} - Token navigation and utilities</li>
 *     <li>{@link PrecedenceParser} - Expression parsing with proper precedence</li>
 *     <li>{@link CollectionParser} - Vectors, matrices, and comprehensions</li>
 * </ul>
 *
 * <h2>Precedence Chain (lowest to highest):</h2>
 * <ol>
 *     <li>Expression → Assignment</li>
 *     <li>Assignment → Lambda</li>
 *     <li>Lambda → LogicalOr</li>
 *     <li>LogicalOr → LogicalXor</li>
 *     <li>LogicalXor → LogicalAnd</li>
 *     <li>LogicalAnd → Equality</li>
 *     <li>Equality → Range</li>
 *     <li>Range → Relational</li>
 *     <li>Relational → Additive</li>
 *     <li>Additive → Multiplicative</li>
 *     <li>Multiplicative → Unary</li>
 *     <li>Unary → Power</li>
 *     <li>Power → Postfix</li>
 *     <li>Postfix → Call/Subscript</li>
 *     <li>Call/Subscript → Primary</li>
 * </ol>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * List<Token> tokens = lexer.tokenize("2 * x + 3");
 * Parser parser = new Parser(tokens, sourceCode, functionRegistry, unitRegistry);
 * Node ast = parser.parse();
 * }</pre>
 *
 * @see TokenStream
 * @see PrecedenceParser
 * @see CollectionParser
 * @see Node
 */
public final class Parser {

    private final TokenStream stream;
    private final PrecedenceParser precedenceParser;

    /**
     * Creates a new parser with the given tokens, registries, and full configuration.
     *
     * @param tokens                the list of tokens to parse
     * @param sourceCode            the original source code (for error messages)
     * @param maxExpressionDepth    maximum allowed nesting depth for expressions
     * @param forceDoubleArithmetic whether to force double arithmetic for decimal literals
     */
    public Parser(List<Token> tokens, String sourceCode, int maxExpressionDepth, boolean forceDoubleArithmetic) {
        this.stream = new TokenStream(tokens, sourceCode);

        // Use a holder to break the circular initialization dependency
        // The supplier captures 'this' which is safe because it's only called after construction
        var collectionParser = new CollectionParser(stream, this::parseExpressionInternal);

        // Create precedence parser with max expression depth and arithmetic mode
        this.precedenceParser = new PrecedenceParser(stream, collectionParser, maxExpressionDepth, forceDoubleArithmetic);
    }

    /**
     * Internal method for expression parsing, used by CollectionParser.
     */
    private Node parseExpressionInternal() {
        return precedenceParser.parseExpression();
    }

    /**
     * Parses the token stream and returns the root AST node.
     * <p>
     * Supports multiple statements separated by semicolons.
     * If multiple statements are present, returns a {@link NodeSequence}.
     *
     * @return the root node of the AST
     * @throws ParseException if a syntax error occurs
     */
    public Node parse() {
        if (stream.size() == 0 || (stream.size() == 1 && stream.peek().type() == TokenType.EOF)) {
            throw stream.error(stream.peek(), "Empty expression");
        }

        var statements = new ArrayList<Node>();

        // Parse first statement
        statements.add(precedenceParser.parseExpression());

        // Parse additional statements separated by semicolons
        while (stream.match(TokenType.SEMICOLON)) {
            if (stream.isAtEnd()) {
                break;
            }
            statements.add(precedenceParser.parseExpression());
        }

        if (!stream.isAtEnd()) {
            throw stream.error(stream.peek(), "Unexpected token after expression");
        }

        // Return single expression or sequence
        if (statements.size() == 1) {
            return statements.getFirst();
        }
        return new NodeSequence(statements);
    }

    /**
     * Parses a single expression without expecting EOF.
     * <p>
     * Useful for parsing sub-expressions in specific contexts.
     *
     * @return the parsed expression node
     */
    public Node parseExpression() {
        return precedenceParser.parseExpression();
    }

    /**
     * Gets the current position in the token stream.
     * <p>
     * Useful for debugging and error recovery.
     *
     * @return the current position
     */
    public int getCurrentPosition() {
        return stream.getPosition();
    }

    /**
     * Checks if parsing has reached the end of the token stream.
     *
     * @return true if at end
     */
    public boolean isAtEnd() {
        return stream.isAtEnd();
    }

    /**
     * Gets the current token without consuming it.
     * <p>
     * Useful for debugging and error reporting.
     *
     * @return the current token
     */
    public Token peek() {
        return stream.peek();
    }
}
