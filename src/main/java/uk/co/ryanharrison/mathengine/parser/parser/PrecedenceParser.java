package uk.co.ryanharrison.mathengine.parser.parser;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.registry.SymbolRegistry;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Turns a token stream into an expression tree.
 * <p>
 * Assignment and lambda are recognised first, since they need lookahead. Everything
 * from logical OR down to multiplication is one precedence-climbing loop reading the
 * precedence table in {@link SymbolRegistry}. Unary, power, postfix, call and subscript
 * bind tighter still and are parsed by dedicated methods, because each has a shape the
 * table cannot describe.
 *
 * <pre>{@code
 * Node ast = new PrecedenceParser(stream, collectionParser, maxDepth, forceDouble).parseExpression();
 * }</pre>
 */
public final class PrecedenceParser {

    private static final SymbolRegistry SYMBOLS = SymbolRegistry.getDefault();

    /**
     * Loosest binary operator; assignment and lambda are handled above this loop.
     */
    private static final int LOWEST_BINARY_PRECEDENCE = 2;

    /**
     * Unit conversion binds tighter than addition and looser than multiplication.
     */
    private static final int UNIT_CONVERSION_PRECEDENCE = 9;

    private static final int NOT_AN_OPERATOR = Integer.MIN_VALUE;

    private final TokenStream stream;
    private final CollectionParser collectionParser;
    private final int maxExpressionDepth;
    private final boolean forceDoubleArithmetic;
    private final int maxLiteralDigits;
    private int currentDepth;

    /**
     * Creates a new precedence parser with the specified configuration.
     *
     * @param stream                the token stream
     * @param collectionParser      parser for collections (vectors, matrices)
     * @param maxExpressionDepth    maximum allowed nesting depth for expressions
     * @param forceDoubleArithmetic whether to force double arithmetic for decimal literals
     */
    public PrecedenceParser(TokenStream stream, CollectionParser collectionParser, int maxExpressionDepth,
                            boolean forceDoubleArithmetic, int maxLiteralDigits) {
        this.stream = stream;
        this.collectionParser = collectionParser;
        this.maxExpressionDepth = maxExpressionDepth;
        this.forceDoubleArithmetic = forceDoubleArithmetic;
        this.maxLiteralDigits = maxLiteralDigits;
        this.currentDepth = 0;
    }

    /**
     * Refuses a literal longer than the configured digit limit, before expanding it.
     */
    private void requireRepresentable(BigDecimal decimal, Token token) {
        if (maxLiteralDigits < 0) {
            return;
        }
        long digits = (long) decimal.precision() + Math.abs((long) decimal.scale());
        if (digits > maxLiteralDigits) {
            throw stream.error(token, "Number literal needs " + digits +
                    " digits, beyond the limit of " + maxLiteralDigits);
        }
    }

    /**
     * Enters a new expression depth level, checking against the maximum allowed depth.
     * <p>
     * Counted by every construct that makes this parser call itself: a grouping, a vector,
     * a matrix, a call's arguments, a subscript's indices, a prefix operator and the right
     * side of a power. Constructs that only iterate cost nothing.
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

        return parseBinary(LOWEST_BINARY_PRECEDENCE);
    }

    // ==================== Binary Operators ====================

    /**
     * Parses the binary operators by precedence climbing, driven by the precedence
     * recorded in {@link SymbolRegistry}. Registering a new binary operator there and
     * in the operator executor is enough; this loop needs no change.
     * <p>
     * Two levels are not plain infix operators and are handled inline: a range
     * ({@code 1..10 step 2}) takes an optional step, and a unit conversion
     * ({@code 5 km in miles}) takes a unit name rather than an expression.
     *
     * @param minPrecedence the loosest operator this call may consume
     */
    private Node parseBinary(int minPrecedence) {
        Node left = parseUnary();

        while (true) {
            int precedence = infixPrecedence();
            if (precedence < minPrecedence) {
                return left;
            }

            if (precedence == UNIT_CONVERSION_PRECEDENCE) {
                left = parseUnitConversion(left);
            } else if (stream.check(TokenType.RANGE)) {
                left = parseRangeTail(left, precedence);
            } else {
                Token operator = stream.advance();
                left = new NodeBinary(operator, left, parseBinary(precedence + 1));
            }
        }
    }

    /**
     * The precedence of the operator at the cursor, or {@link #NOT_AN_OPERATOR}.
     */
    private int infixPrecedence() {
        if (stream.checkKeyword("in", "to", "as")) {
            return UNIT_CONVERSION_PRECEDENCE;
        }
        TokenType type = stream.peek().type();
        return SYMBOLS.isBinaryOperator(type) ? SYMBOLS.getPrecedence(type) : NOT_AN_OPERATOR;
    }

    /**
     * Parses the rest of {@code start..end} and its optional {@code step}.
     */
    private Node parseRangeTail(Node start, int precedence) {
        stream.advance();
        Node end = parseBinary(precedence + 1);
        Node step = stream.checkKeyword("step") ? consumeStep() : null;
        return new NodeRangeExpression(start, end, step);
    }

    private Node consumeStep() {
        stream.advance();
        return parseUnary();
    }

    /**
     * Parses {@code expr in unit}, and the {@code to} and {@code as} spellings.
     * <p>
     * When the left side is {@code number * identifier}, as in {@code 50m in feet},
     * the identifier is pinned to a unit so that a same-named variable does not win.
     */
    private Node parseUnitConversion(Node left) {
        if (left instanceof NodeBinary binary
                && binary.getOperator().type() == TokenType.MULTIPLY
                && binary.getRight() instanceof NodeVariable variable) {
            left = new NodeBinary(binary.getOperator(), binary.getLeft(), new NodeUnitRef(variable.getName()));
        }

        stream.advance();
        return new NodeUnitConversion(left, expectUnitOrIdentifier().lexeme());
    }

    /**
     * Expects a unit name. An unknown identifier is accepted here and reported at
     * evaluation time, which gives a better message and allows dynamic unit names.
     * Explicit references are accepted too: {@code @fahrenheit} or {@code @"km/h"}.
     * A function name counts, since {@code radians} also spells deg2rad.
     */
    private Token expectUnitOrIdentifier() {
        if (stream.check(TokenType.UNIT) || stream.check(TokenType.IDENTIFIER)
                || stream.check(TokenType.UNIT_REF) || stream.check(TokenType.FUNCTION)) {
            Token token = stream.advance();
            if (token.type() == TokenType.UNIT_REF) {
                return new Token(TokenType.UNIT, (String) token.literal(), token.line(), token.column());
            }
            return token;
        }
        throw stream.error(stream.peek(), "Expected unit name after conversion keyword");
    }

    // ==================== Unary and Power ====================

    /**
     * Parses unary: {@code -expr}, {@code +expr}, {@code not expr}
     */
    Node parseUnary() {
        if (stream.match(TokenType.MINUS, TokenType.PLUS, TokenType.NOT) ||
                stream.checkKeyword("not") && stream.match(TokenType.KEYWORD)) {
            Token op = stream.previous();
            enterDepth();
            try {
                Node operand = parseUnary(); // Right-associative
                return new NodeUnary(op, operand);
            } finally {
                exitDepth();
            }
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
            enterDepth();
            try {
                Node right = parseUnary(); // Right-associative
                return new NodeBinary(op, left, right);
            } finally {
                exitDepth();
            }
        }

        return left;
    }

    // ==================== Postfix and Primary ====================

    /**
     * Parses postfix operators: {@code expr!}, {@code expr!!}, {@code expr%}.
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
                enterDepth();
                List<NodeSubscript.SliceArg> indices;
                try {
                    indices = collectionParser.parseSliceArgs();
                } finally {
                    exitDepth();
                }
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
        enterDepth();
        try {
            var args = new ArrayList<Node>();

            if (!stream.check(TokenType.RPAREN)) {
                do {
                    args.add(parseExpression());
                } while (stream.match(TokenType.COMMA));
            }

            return args;
        } finally {
            exitDepth();
        }
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
            // Double literal: either an explicit 'd'-suffix DOUBLE token, or a computed
            // rational+scientific value (e.g. "1/2E5" → 50000.0 from emitRationalScientific).
            // DOUBLE tokens always go to NodeDouble; others honour the arithmetic mode.
            if (literal instanceof Double doubleVal) {
                if (forceDoubleArithmetic || token.type() == TokenType.DOUBLE) {
                    return new NodeDouble(doubleVal);
                }
                return TypeCoercion.toNumber(doubleVal);
            }
            if (literal instanceof String strVal) {
                if (forceDoubleArithmetic) {
                    return new NodeDouble(Double.parseDouble(strVal));
                }
                try {
                    BigDecimal decimal = new BigDecimal(strVal);
                    requireRepresentable(decimal, token);
                    return new NodeRational(BigRational.of(decimal));
                } catch (NumberFormatException e) {
                    throw stream.error(token, "Invalid decimal literal");
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
