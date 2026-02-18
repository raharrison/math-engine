package uk.co.ryanharrison.mathengine.parser.parser;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Precedence chain parser for mathematical expressions.
 * <p>
 * Implements the complete precedence chain from lowest to highest:
 * <ol>
 *     <li>Expression (top level)</li>
 *     <li>Assignment</li>
 *     <li>Lambda</li>
 *     <li>Logical OR</li>
 *     <li>Logical XOR</li>
 *     <li>Logical AND</li>
 *     <li>Equality</li>
 *     <li>Range</li>
 *     <li>Relational</li>
 *     <li>Additive</li>
 *     <li>Multiplicative</li>
 *     <li>Unary</li>
 *     <li>Power</li>
 *     <li>Postfix</li>
 *     <li>Call/Subscript</li>
 *     <li>Primary (literals, identifiers, groupings)</li>
 * </ol>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * PrecedenceParser parser = new PrecedenceParser(stream, collectionParser, functionRegistry, unitRegistry);
 * Node ast = parser.parseExpression();
 * }</pre>
 */
public final class PrecedenceParser {

    private final TokenStream stream;
    private final CollectionParser collectionParser;
    private final int maxExpressionDepth;
    private final boolean forceDoubleArithmetic;
    private int currentDepth;

    /**
     * Creates a new precedence parser with the specified configuration.
     *
     * @param stream                the token stream
     * @param collectionParser      parser for collections (vectors, matrices)
     * @param maxExpressionDepth    maximum allowed nesting depth for expressions
     * @param forceDoubleArithmetic whether to force double arithmetic for decimal literals
     */
    public PrecedenceParser(TokenStream stream, CollectionParser collectionParser, int maxExpressionDepth, boolean forceDoubleArithmetic) {
        this.stream = stream;
        this.collectionParser = collectionParser;
        this.maxExpressionDepth = maxExpressionDepth;
        this.forceDoubleArithmetic = forceDoubleArithmetic;
        this.currentDepth = 0;
    }

    /**
     * Enters a new expression depth level, checking against the maximum allowed depth.
     *
     * @throws ParseException if maximum expression depth is exceeded
     */
    private void enterDepth() {
        currentDepth++;
        if (currentDepth > maxExpressionDepth) {
            throw stream.error(stream.peek(), "Expression depth exceeds maximum allowed depth of " + maxExpressionDepth);
        }
    }

    /**
     * Exits the current expression depth level.
     */
    private void exitDepth() {
        currentDepth--;
    }

    // ==================== Top-Level Expression ====================

    /**
     * Parses an expression (top level of precedence chain).
     *
     * @return the parsed expression node
     */
    public Node parseExpression() {
        return parseAssignment();
    }

    // ==================== Assignment ====================

    /**
     * Parses assignment expressions.
     * <p>
     * Syntax:
     * <ul>
     *     <li>Variable assignment: {@code x := 5}</li>
     *     <li>Function definition: {@code f(x) := x + 1}</li>
     * </ul>
     * <p>
     * Accepts IDENTIFIER, UNIT, and FUNCTION tokens as names.
     * FUNCTION tokens allow shadowing of built-in functions.
     */
    private Node parseAssignment() {
        int savepoint = stream.savePosition();

        if (stream.check(TokenType.IDENTIFIER) || stream.check(TokenType.UNIT) || stream.check(TokenType.FUNCTION)) {
            Token id = stream.advance();

            // Check for function parameters
            List<String> params = null;
            if (stream.check(TokenType.LPAREN)) {
                if (isFunctionDefinition()) {
                    stream.advance(); // consume LPAREN
                    params = parseParamList();
                    stream.expect(TokenType.RPAREN, "Expected ')' after parameter list");
                } else {
                    stream.restorePosition(savepoint);
                    return parseLambda();
                }
            }

            if (stream.check(TokenType.ASSIGN)) {
                stream.advance(); // consume ASSIGN
                Node value = parseLambda();

                if (params != null) {
                    return new NodeFunctionDef(id.lexeme(), params, value);
                } else {
                    return new NodeAssignment(id.lexeme(), value);
                }
            }

            stream.restorePosition(savepoint);
        }

        return parseLambda();
    }

    /**
     * Checks if current position is at a function definition.
     * <p>
     * Looks for pattern: identifier(params) :=
     */
    private boolean isFunctionDefinition() {
        int depth = 0;
        int lookAhead = 0;

        while (stream.getPosition() + lookAhead < stream.size()) {
            Token token = stream.getTokenAt(stream.getPosition() + lookAhead);
            TokenType type = token.type();

            if (type == TokenType.LPAREN) {
                depth++;
            } else if (type == TokenType.RPAREN) {
                depth--;
                if (depth == 0) {
                    if (stream.getPosition() + lookAhead + 1 < stream.size()) {
                        Token next = stream.getTokenAt(stream.getPosition() + lookAhead + 1);
                        return next.type() == TokenType.ASSIGN;
                    }
                    return false;
                }
            } else if (type == TokenType.EOF) {
                return false;
            }

            lookAhead++;
        }

        return false;
    }

    /**
     * Parses a parameter list for function definitions.
     */
    private List<String> parseParamList() {
        var params = new ArrayList<String>();

        if (!stream.check(TokenType.RPAREN)) {
            do {
                Token param = expectParameterName();
                params.add(param.lexeme());
            } while (stream.match(TokenType.COMMA));
        }

        return params;
    }

    /**
     * Expects a parameter name (IDENTIFIER, UNIT, or KEYWORD token).
     * <p>
     * Keywords are allowed as parameter names in function definitions to support
     * cases like {@code convert(x, from, to) := x from in to}.
     * </p>
     */
    private Token expectParameterName() {
        if (stream.check(TokenType.IDENTIFIER) || stream.check(TokenType.UNIT) || stream.check(TokenType.KEYWORD)) {
            return stream.advance();
        }
        throw stream.error(stream.peek(), "Expected parameter name");
    }

    // ==================== Lambda ====================

    /**
     * Parses lambda expressions.
     * <p>
     * Syntax: {@code x -> expr}
     * <p>
     * Multi-parameter lambdas {@code (x, y) -> expr} are handled in parenthesized expressions.
     */
    private Node parseLambda() {
        if (stream.check(TokenType.IDENTIFIER)) {
            int savepoint = stream.savePosition();
            Token param = stream.advance();

            if (stream.match(TokenType.LAMBDA)) {
                var params = new ArrayList<String>();
                params.add(param.lexeme());
                Node body = parseExpression();
                return new NodeLambda(params, body);
            }

            stream.restorePosition(savepoint);
        }

        return parseLogicalOr();
    }

    // ==================== Logical Operators ====================

    /**
     * Parses logical OR: {@code expr || expr} or {@code expr or expr}
     */
    private Node parseLogicalOr() {
        Node left = parseLogicalXor();

        while (stream.match(TokenType.OR) || stream.checkKeyword("or") && stream.match(TokenType.KEYWORD)) {
            Token op = stream.previous();
            Node right = parseLogicalXor();
            left = new NodeBinary(op, left, right);
        }

        return left;
    }

    /**
     * Parses logical XOR: {@code expr xor expr}
     */
    private Node parseLogicalXor() {
        Node left = parseLogicalAnd();

        while (stream.match(TokenType.XOR) || stream.checkKeyword("xor") && stream.match(TokenType.KEYWORD)) {
            Token op = stream.previous();
            Node right = parseLogicalAnd();
            left = new NodeBinary(op, left, right);
        }

        return left;
    }

    /**
     * Parses logical AND: {@code expr && expr} or {@code expr and expr}
     */
    private Node parseLogicalAnd() {
        Node left = parseEquality();

        while (stream.match(TokenType.AND) || stream.checkKeyword("and") && stream.match(TokenType.KEYWORD)) {
            Token op = stream.previous();
            Node right = parseEquality();
            left = new NodeBinary(op, left, right);
        }

        return left;
    }

    // ==================== Comparison Operators ====================

    /**
     * Parses equality: {@code expr == expr} or {@code expr != expr}
     */
    private Node parseEquality() {
        Node left = parseRange();

        while (stream.match(TokenType.EQ, TokenType.NEQ)) {
            Token op = stream.previous();
            Node right = parseRange();
            left = new NodeBinary(op, left, right);
        }

        return left;
    }

    /**
     * Parses range expressions: {@code start..end} or {@code start..end step s}
     */
    private Node parseRange() {
        Node left = parseRelational();

        if (stream.match(TokenType.RANGE)) {
            Node end = parseRelational(); // Parse at same level to support -10..-5
            Node step = null;

            if (stream.checkKeyword("step")) {
                stream.advance();
                step = parseUnary();
            }

            return new NodeRangeExpression(left, end, step);
        }

        return left;
    }

    /**
     * Parses relational: {@code expr < expr}, {@code expr > expr}, etc.
     */
    private Node parseRelational() {
        Node left = parseAdditive();

        while (stream.match(TokenType.LT, TokenType.GT, TokenType.LTE, TokenType.GTE)) {
            Token op = stream.previous();
            Node right = parseAdditive();
            left = new NodeBinary(op, left, right);
        }

        return left;
    }

    // ==================== Arithmetic Operators ====================

    /**
     * Parses additive: {@code expr + expr} or {@code expr - expr}
     */
    private Node parseAdditive() {
        Node left = parseUnitConversion();

        while (stream.match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = stream.previous();
            Node right = parseUnitConversion();
            left = new NodeBinary(op, left, right);
        }

        return left;
    }

    /**
     * Parses unit conversions: {@code expr in unit}, {@code expr to unit}, {@code expr as unit}.
     * Binds tighter than addition but looser than multiplication.
     * <p>
     * Accepts both UNIT and IDENTIFIER tokens as target units. Unknown units (identifiers that
     * aren't registered units) will cause a TypeError at evaluation time, not parse time.
     * This allows for better error messages and supports dynamic unit names.
     */
    private Node parseUnitConversion() {
        Node left = parseMultiplicative();

        while (stream.checkKeyword("in", "to", "as")) {
            // Special case: if left is (number * identifier) and we're about to convert,
            // replace the identifier with an explicit unit reference to force unit interpretation
            // e.g., "50m in feet" where m is a variable → treat m as meter unit in this context
            if (left instanceof NodeBinary binary &&
                    binary.getOperator().type() == TokenType.MULTIPLY &&
                    binary.getRight() instanceof NodeVariable variable) {
                // Replace the variable with an explicit unit reference
                // This forces the identifier to be treated as a unit, not a variable
                var unitRef = new NodeUnitRef(variable.getName());
                left = new NodeBinary(binary.getOperator(), binary.getLeft(), unitRef);
            }

            stream.advance();
            // Accept both UNIT and IDENTIFIER tokens - unknown units will be caught during evaluation
            Token unitToken = expectUnitOrIdentifier();
            left = new NodeUnitConversion(left, unitToken.lexeme());
        }

        return left;
    }

    /**
     * Expects a unit name token (UNIT, IDENTIFIER, or UNIT_REF).
     * <p>
     * Allows unknown identifiers to be used as unit names, with validation
     * deferred to evaluation time. This provides better error messages and
     * supports dynamic unit resolution.
     * <p>
     * Accepts explicit unit references: {@code @fahrenheit} or {@code @"km/h"}.
     * The quoted form allows complex unit names containing operators.
     */
    private Token expectUnitOrIdentifier() {
        if (stream.check(TokenType.UNIT) || stream.check(TokenType.IDENTIFIER) || stream.check(TokenType.UNIT_REF)) {
            Token token = stream.advance();
            // For UNIT_REF tokens, extract the unit name from the literal
            if (token.type() == TokenType.UNIT_REF) {
                String unitName = (String) token.literal();
                return new Token(TokenType.UNIT, unitName, token.line(), token.column());
            }
            return token;
        }
        throw stream.error(stream.peek(), "Expected unit name after conversion keyword");
    }

    /**
     * Parses multiplicative: {@code expr * expr}, {@code expr / expr}, etc.
     */
    private Node parseMultiplicative() {
        Node left = parseUnary();

        while (stream.match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.AT, TokenType.MOD, TokenType.OF) ||
                stream.checkKeyword("mod", "of") && stream.match(TokenType.KEYWORD)) {
            Token op = stream.previous();
            Node right = parseUnary();
            left = new NodeBinary(op, left, right);
        }

        return left;
    }

    // ==================== Unary and Power ====================

    /**
     * Parses unary: {@code -expr}, {@code +expr}, {@code not expr}
     */
    Node parseUnary() {
        if (stream.match(TokenType.MINUS, TokenType.PLUS, TokenType.NOT) ||
                stream.checkKeyword("not") && stream.match(TokenType.KEYWORD)) {
            Token op = stream.previous();
            Node operand = parseUnary(); // Right-associative
            return new NodeUnary(op, operand);
        }

        return parsePower();
    }

    /**
     * Parses power: {@code base ^ exponent}
     * <p>
     * Right-associative: {@code 2^3^2 = 2^(3^2) = 512}
     */
    private Node parsePower() {
        Node left = parsePostfix();

        if (stream.match(TokenType.POWER)) {
            Token op = stream.previous();
            Node right = parseUnary(); // Right-associative
            return new NodeBinary(op, left, right);
        }

        return left;
    }

    // ==================== Postfix and Primary ====================

    /**
     * Parses postfix operators: {@code expr!}, {@code expr!!}, {@code expr%}.
     * Note: unit conversions are now handled in parseMultiplicative.
     */
    Node parsePostfix() {
        Node expr = parseCallAndSubscript();

        while (true) {
            if (stream.match(TokenType.FACTORIAL, TokenType.DOUBLE_FACTORIAL, TokenType.PERCENT)) {
                Token op = stream.previous();
                expr = new NodeUnary(op, expr, false);
            } else {
                break;
            }
        }

        return expr;
    }

    /**
     * Parses function calls and subscript operations.
     * <p>
     * Handles chained operations: {@code funcs[0](10)}, {@code matrix[0][1]}
     */
    private Node parseCallAndSubscript() {
        Node expr = parsePrimary();

        while (true) {
            if (stream.match(TokenType.LPAREN)) {
                List<Node> args = parseArguments();
                stream.expect(TokenType.RPAREN, "Expected ')' after function arguments");
                expr = new NodeCall(expr, args);
            } else if (stream.match(TokenType.LBRACKET)) {
                List<NodeSubscript.SliceArg> indices = collectionParser.parseSliceArgs();
                stream.expect(TokenType.RBRACKET, "Expected ']' after subscript");
                expr = new NodeSubscript(expr, indices);
            } else if (stream.check(TokenType.MULTIPLY) && isCallableOrParenthesized(expr)) {
                // Potential call disguised as multiplication from implicit mult
                int savePos = stream.savePosition();
                stream.advance();
                if (stream.check(TokenType.LPAREN)) {
                    stream.advance();
                    List<Node> args = parseArguments();
                    stream.expect(TokenType.RPAREN, "Expected ')' after arguments");
                    expr = new NodeCall(expr, args);
                } else {
                    stream.restorePosition(savePos);
                    break;
                }
            } else {
                break;
            }
        }

        return expr;
    }

    /**
     * Checks if a node could potentially be callable.
     */
    private boolean isCallableOrParenthesized(Node node) {
        return node instanceof NodeLambda ||
                node instanceof NodeCall;
        // Only lambdas and function call results are callable via implicit multiplication.
        // Variables, constants, binary/unary expressions are NOT callable this way to avoid
        // parsing bugs like: P * (1 + r) being treated as P(1 + r) where P is a variable.
    }

    /**
     * Parses function arguments.
     */
    private List<Node> parseArguments() {
        var args = new ArrayList<Node>();

        if (!stream.check(TokenType.RPAREN)) {
            do {
                args.add(parseExpression());
            } while (stream.match(TokenType.COMMA));
        }

        return args;
    }

    // ==================== Primary Expressions ====================

    /**
     * Parses primary expressions (literals, identifiers, groupings).
     */
    private Node parsePrimary() {
        // Integer literal
        if (stream.match(TokenType.INTEGER)) {
            Token token = stream.previous();
            Object literal = token.literal();
            if (literal instanceof Long longVal) {
                if (forceDoubleArithmetic) {
                    return new NodeDouble(longVal.doubleValue());
                }
                return new NodeRational(longVal, 1L);
            } else if (literal instanceof Integer intVal) {
                if (forceDoubleArithmetic) {
                    return new NodeDouble(intVal.doubleValue());
                }
                return new NodeRational(intVal.longValue(), 1L);
            } else if (literal instanceof BigInteger bigInt) {
                if (forceDoubleArithmetic) {
                    return new NodeDouble(bigInt.doubleValue());
                }
                return new NodeRational(BigRational.of(bigInt));
            }
            throw stream.error(token, "Invalid integer literal");
        }

        // Decimal/scientific/double literal
        if (stream.match(TokenType.DECIMAL, TokenType.SCIENTIFIC, TokenType.DOUBLE)) {
            Token token = stream.previous();
            Object literal = token.literal();
            if (literal instanceof Double doubleVal) {
                // DOUBLE token always returns NodeDouble (never converts to rational)
                if (forceDoubleArithmetic || token.type() == TokenType.DOUBLE) {
                    return new NodeDouble(doubleVal);
                } else {
                    return TypeCoercion.toNumber(doubleVal);
                }
            }
            throw stream.error(token, "Invalid decimal literal");
        }

        // Rational literal
        if (stream.match(TokenType.RATIONAL)) {
            return parseRationalLiteral();
        }

        // String literal
        if (stream.match(TokenType.STRING)) {
            Token token = stream.previous();
            Object literal = token.literal();
            if (literal instanceof String strVal) {
                return new NodeString(strVal);
            }
            throw stream.error(token, "Invalid string literal");
        }

        // Identifiers (variables, constants, functions)
        if (stream.match(TokenType.IDENTIFIER, TokenType.KEYWORD, TokenType.UNIT, TokenType.FUNCTION)) {
            return new NodeVariable(stream.previous().lexeme());
        }

        // Explicit unit reference (@unit or @"km/h")
        if (stream.match(TokenType.UNIT_REF)) {
            String unitName = (String) stream.previous().literal();
            return new NodeUnitRef(unitName);
        }

        // Explicit variable reference ($var)
        if (stream.match(TokenType.VAR_REF)) {
            String lexeme = stream.previous().lexeme();
            // Remove $ prefix to get variable name
            String varName = lexeme.substring(1);
            return new NodeVarRef(varName);
        }

        // Explicit constant reference (#const)
        if (stream.match(TokenType.CONST_REF)) {
            String lexeme = stream.previous().lexeme();
            // Remove # prefix to get constant name
            String constName = lexeme.substring(1);
            return new NodeConstRef(constName);
        }

        // Parenthesized expression
        if (stream.match(TokenType.LPAREN)) {
            return parseParenthesizedContent();
        }

        // Vector or comprehension
        if (stream.match(TokenType.LBRACE)) {
            enterDepth();
            try {
                return collectionParser.parseVectorOrComprehension();
            } finally {
                exitDepth();
            }
        }

        // Matrix
        if (stream.match(TokenType.LBRACKET)) {
            enterDepth();
            try {
                return collectionParser.parseMatrix();
            } finally {
                exitDepth();
            }
        }

        throw stream.error(stream.peek(), "Expected expression");
    }

    /**
     * Parses a rational literal like "1/2".
     */
    private Node parseRationalLiteral() {
        Token token = stream.previous();
        String text = token.lexeme();
        String[] parts = text.split("/");

        if (parts.length == 2) {
            try {
                BigInteger numerator = new BigInteger(parts[0]);
                BigInteger denominator = new BigInteger(parts[1]);

                if (denominator.signum() == 0) {
                    // Lazy evaluation for division by zero
                    var left = forceDoubleArithmetic ?
                            new NodeDouble(numerator.doubleValue()) : new NodeRational(BigRational.of(numerator));
                    var right = forceDoubleArithmetic ?
                            new NodeDouble(0) : new NodeRational(0);
                    var divideToken = new Token(TokenType.DIVIDE, "/", null, token.line(), token.column());
                    return new NodeBinary(divideToken, left, right);
                }

                if (forceDoubleArithmetic) {
                    return new NodeDouble(numerator.doubleValue() / denominator.doubleValue());
                }
                return new NodeRational(BigRational.of(numerator, denominator));
            } catch (NumberFormatException e) {
                throw stream.error(token, "Invalid rational literal");
            }
        }
        throw stream.error(token, "Invalid rational literal format");
    }

    /**
     * Parses content inside parentheses.
     * <p>
     * Handles:
     * <ul>
     *     <li>Parenthesized lambda: {@code (x) -> expr} or {@code (x, y) -> expr}</li>
     *     <li>Statement sequence: {@code (stmt1; stmt2; expr)}</li>
     *     <li>Regular expression: {@code (expr)}</li>
     * </ul>
     */
    private Node parseParenthesizedContent() {
        enterDepth();
        try {
            // Check for parenthesized lambda (single or multi-parameter)
            if (isMultiParamLambda()) {
                return parseParenthesizedLambda();
            }

            // Parse first expression
            Node firstExpr = parseExpression();

            // Check for statement sequence
            if (stream.match(TokenType.SEMICOLON)) {
                var statements = new ArrayList<Node>();
                statements.add(firstExpr);

                while (!stream.check(TokenType.RPAREN) && !stream.isAtEnd()) {
                    statements.add(parseExpression());
                    if (!stream.match(TokenType.SEMICOLON)) {
                        break;
                    }
                }

                stream.expect(TokenType.RPAREN, "Expected ')' after statement sequence");
                return new NodeSequence(statements);
            }

            // Regular parenthesized expression
            stream.expect(TokenType.RPAREN, "Expected ')' after expression");
            return firstExpr;
        } finally {
            exitDepth();
        }
    }

    /**
     * Checks if current position starts a parenthesized lambda.
     * <p>
     * Supports both single and multi-parameter lambdas:
     * <ul>
     *     <li>Single parameter: {@code (x) -> expr}</li>
     *     <li>Multiple parameters: {@code (x, y, z) -> expr}</li>
     * </ul>
     * Pattern: (id [, id]*) ->
     */
    private boolean isMultiParamLambda() {
        int lookAhead = 0;

        if (!stream.isIdentifierAt(lookAhead)) {
            return false;
        }
        lookAhead++;

        while (stream.getPosition() + lookAhead < stream.size()) {
            Token token = stream.getTokenAt(stream.getPosition() + lookAhead);

            if (token.type() == TokenType.COMMA) {
                lookAhead++;
                if (!stream.isIdentifierAt(lookAhead)) {
                    return false;
                }
                lookAhead++;
            } else if (token.type() == TokenType.RPAREN) {
                // Found closing paren, check if followed by LAMBDA
                lookAhead++;
                if (stream.getPosition() + lookAhead < stream.size()) {
                    Token next = stream.getTokenAt(stream.getPosition() + lookAhead);
                    return next.type() == TokenType.LAMBDA;
                }
                return false;
            } else {
                return false;
            }
        }

        return false;
    }

    /**
     * Parses a parenthesized lambda (single or multi-parameter).
     * Pattern: x [, y, ...] ) -> body
     * Note: Opening paren already consumed by caller
     */
    private Node parseParenthesizedLambda() {
        var params = new ArrayList<String>();

        Token firstParam = expectParameterName();
        params.add(firstParam.lexeme());

        while (stream.match(TokenType.COMMA)) {
            Token param = expectParameterName();
            params.add(param.lexeme());
        }

        stream.expect(TokenType.RPAREN, "Expected ')' after lambda parameters");
        stream.expect(TokenType.LAMBDA, "Expected '->' after lambda parameters");
        Node body = parseExpression();

        return new NodeLambda(params, body);
    }
}
