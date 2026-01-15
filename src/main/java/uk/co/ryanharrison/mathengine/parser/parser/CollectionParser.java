package uk.co.ryanharrison.mathengine.parser.parser;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Parser component for collection literals and comprehensions.
 * <p>
 * Handles parsing of:
 * <ul>
 *     <li>Vector literals: {@code {1, 2, 3}}</li>
 *     <li>Matrix literals: {@code [1, 2; 3, 4]} or {@code [[1, 2], [3, 4]]}</li>
 *     <li>List comprehensions: {@code {x*2 for x in 1..10}}</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * CollectionParser collectionParser = new CollectionParser(stream, () -> parseExpression());
 * Node vector = collectionParser.parseVector();
 * Node matrix = collectionParser.parseMatrix();
 * }</pre>
 */
public final class CollectionParser {

    private final TokenStream stream;
    private final Supplier<Node> expressionParser;

    /**
     * Creates a new collection parser.
     *
     * @param stream           the token stream
     * @param expressionParser supplier for parsing sub-expressions
     */
    public CollectionParser(TokenStream stream, Supplier<Node> expressionParser) {
        this.stream = stream;
        this.expressionParser = expressionParser;
    }

    // ==================== Vector Parsing ====================

    /**
     * Parses a vector literal or comprehension.
     * <p>
     * Disambiguates between:
     * <ul>
     *     <li>Vector literal: {@code {1, 2, 3}}</li>
     *     <li>Comprehension: {@code {x*2 for x in 1..10}}</li>
     * </ul>
     * <p>
     * Assumes the opening brace '{' has already been consumed.
     *
     * @return the parsed vector or comprehension node
     */
    public Node parseVectorOrComprehension() {
        // Empty vector
        if (stream.check(TokenType.RBRACE)) {
            stream.advance();
            return new NodeVector(new ArrayList<>());
        }

        // Parse first expression
        int savepoint = stream.savePosition();
        Node firstExpr = expressionParser.get();

        // Check if this is a comprehension (has "for" keyword)
        if (stream.checkKeyword("for")) {
            // Backtrack and parse as comprehension
            stream.restorePosition(savepoint);
            return parseComprehension();
        }

        // It's a vector literal
        List<Node> elements = new ArrayList<>();
        elements.add(firstExpr);

        while (stream.match(TokenType.COMMA)) {
            elements.add(expressionParser.get());
        }

        stream.expect(TokenType.RBRACE, "Expected '}' after vector elements");
        return new NodeVector(elements);
    }

    /**
     * Parses a list comprehension.
     * <p>
     * Syntax: {@code { expression for variable in iterable [for variable in iterable]* [if condition] }}
     * <p>
     * Supports nested iterations:
     * <ul>
     *     <li>{@code {x*y for x in 1..3 for y in 1..3}}</li>
     *     <li>{@code {{x, y} for x in 1..2 for y in 1..2}}</li>
     * </ul>
     *
     * @return the comprehension node
     */
    private Node parseComprehension() {
        Node expr = expressionParser.get();

        List<NodeComprehension.Iterator> iterators = new ArrayList<>();

        // Parse all 'for variable in iterable' clauses
        while (stream.checkKeyword("for")) {
            stream.advance();

            // Accept IDENTIFIER or UNIT tokens as variable names (e.g., k for kelvin)
            Token var;
            if (stream.check(TokenType.IDENTIFIER) || stream.check(TokenType.UNIT)) {
                var = stream.advance();
            } else {
                throw stream.error(stream.peek(), "Expected variable name after 'for'");
            }

            stream.expectKeyword("in");
            Node iterable = expressionParser.get();
            iterators.add(new NodeComprehension.Iterator(var.getLexeme(), iterable));
        }

        Node condition = null;
        if (stream.checkKeyword("if")) {
            stream.advance();
            condition = expressionParser.get();
        }

        stream.expect(TokenType.RBRACE, "Expected '}' after comprehension");

        return new NodeComprehension(expr, iterators, condition);
    }

    // ==================== Matrix Parsing ====================

    /**
     * Parses a matrix literal.
     * <p>
     * Supports two syntaxes:
     * <ul>
     *     <li>Traditional: {@code [1, 2; 3, 4]}</li>
     *     <li>Nested: {@code [[1, 2], [3, 4]]}</li>
     *     <li>Empty: {@code []} creates 0×0 matrix, {@code [[]]} creates 1×0 matrix</li>
     * </ul>
     * <p>
     * Assumes the opening bracket '[' has already been consumed.
     *
     * @return the parsed matrix node
     */
    public Node parseMatrix() {
        // Empty matrix [] creates 0×0 matrix (consistent with {} empty vector)
        if (stream.check(TokenType.RBRACKET)) {
            stream.advance();
            return new NodeMatrix(new ArrayList<>());
        }

        List<List<Node>> rows = new ArrayList<>();

        // Parse first element/expression
        Node firstExpr = expressionParser.get();

        // Determine syntax mode based on first expression and next token
        boolean isNestedSyntax = isNestedMatrixElement(firstExpr) &&
                (stream.check(TokenType.COMMA) || stream.check(TokenType.SEMICOLON) ||
                        stream.check(TokenType.RBRACKET));

        if (isNestedSyntax) {
            return parseNestedMatrix(firstExpr, rows);
        } else {
            return parseTraditionalMatrix(firstExpr, rows);
        }
    }

    /**
     * Checks if a node represents a nested matrix element (vector or matrix).
     */
    private boolean isNestedMatrixElement(Node node) {
        return node instanceof NodeVector || node instanceof NodeMatrix;
    }

    /**
     * Parses a matrix in nested syntax: [[1, 2], [3, 4]].
     */
    private Node parseNestedMatrix(Node firstExpr, List<List<Node>> rows) {
        List<Node> rowNodes = new ArrayList<>();
        rowNodes.add(firstExpr);

        // Parse remaining rows (separated by comma or semicolon)
        while (stream.match(TokenType.COMMA) || stream.match(TokenType.SEMICOLON)) {
            rowNodes.add(expressionParser.get());
        }

        stream.expect(TokenType.RBRACKET, "Expected ']' after matrix");

        // Convert each row node to a list of elements
        for (Node rowNode : rowNodes) {
            List<Node> rowElements = extractRowElements(rowNode);

            // Validate consistent row sizes
            if (!rows.isEmpty() && rowElements.size() != rows.get(0).size()) {
                throw stream.error(stream.previous(), "Matrix rows have inconsistent sizes: expected " +
                        rows.get(0).size() + " elements, got " + rowElements.size());
            }

            rows.add(rowElements);
        }

        return new NodeMatrix(rows);
    }

    /**
     * Extracts row elements from a vector or matrix node.
     */
    private List<Node> extractRowElements(Node rowNode) {
        List<Node> rowElements = new ArrayList<>();

        if (rowNode instanceof NodeVector vec) {
            for (int i = 0; i < vec.size(); i++) {
                rowElements.add(vec.getElement(i));
            }
        } else if (rowNode instanceof NodeMatrix mat) {
            // Empty 0×0 matrix represents an empty row (0 columns)
            if (mat.getRows() == 0 && mat.getCols() == 0) {
                return rowElements; // Empty list
            }
            // Use first row if it's a matrix
            if (mat.getRows() != 1) {
                throw stream.error(stream.previous(), "Nested matrix must be a single row (1xN matrix)");
            }
            for (int j = 0; j < mat.getCols(); j++) {
                rowElements.add(mat.getElement(0, j));
            }
        } else {
            throw stream.error(stream.previous(), "In nested matrix syntax, each element must be a vector or matrix");
        }

        return rowElements;
    }

    /**
     * Parses a matrix in traditional syntax: [1, 2; 3, 4].
     */
    private Node parseTraditionalMatrix(Node firstExpr, List<List<Node>> rows) {
        // Build first row
        List<Node> firstRow = new ArrayList<>();
        firstRow.add(firstExpr);

        while (stream.match(TokenType.COMMA)) {
            firstRow.add(expressionParser.get());
        }
        rows.add(firstRow);

        // Parse additional rows (separated by semicolon)
        while (stream.match(TokenType.SEMICOLON)) {
            List<Node> row = new ArrayList<>();
            row.add(expressionParser.get());

            while (stream.match(TokenType.COMMA)) {
                row.add(expressionParser.get());
            }

            // Validate row size
            if (row.size() != firstRow.size()) {
                throw stream.error(stream.previous(), "Matrix rows have inconsistent sizes: expected " +
                        firstRow.size() + " elements, got " + row.size());
            }

            rows.add(row);
        }

        stream.expect(TokenType.RBRACKET, "Expected ']' after matrix");
        return new NodeMatrix(rows);
    }

    // ==================== Slice Parsing ====================

    /**
     * Parses slice arguments for subscript operations.
     * <p>
     * Supports:
     * <ul>
     *     <li>Single index: {@code [5]}</li>
     *     <li>Slice: {@code [1:5]}</li>
     *     <li>Open-ended slice: {@code [:5]}, {@code [1:]}, {@code [:]}</li>
     *     <li>Multi-dimensional: {@code [1, 2]}</li>
     * </ul>
     *
     * @return list of slice arguments
     */
    public List<NodeSubscript.SliceArg> parseSliceArgs() {
        List<NodeSubscript.SliceArg> args = new ArrayList<>();

        do {
            Node start = null, end = null;
            boolean isSlice = false;

            // Check if we have a start index/expression
            if (!stream.check(TokenType.COLON)) {
                start = expressionParser.get();
            }

            // Check if this is a slice (has colon)
            if (stream.match(TokenType.COLON)) {
                isSlice = true;
                // Check if we have an end index/expression
                if (!stream.check(TokenType.COMMA) && !stream.check(TokenType.RBRACKET)) {
                    end = expressionParser.get();
                }
            }

            args.add(new NodeSubscript.SliceArg(start, end, isSlice));

        } while (stream.match(TokenType.COMMA));

        return args;
    }
}
