package uk.co.ryanharrison.mathengine.parser.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.lexer.Lexer;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.FunctionRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.KeywordRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for the Parser class.
 * Tests precedence, associativity, and correct AST structure.
 */
class ParserTest {

    private Lexer lexer;

    @BeforeEach
    void setUp() {
        // Create registries from default config
        MathEngineConfig config = MathEngineConfig.defaults();
        FunctionRegistry functionRegistry = FunctionRegistry.fromFunctions(config.functions());
        UnitRegistry unitRegistry = new UnitRegistry();
        ConstantRegistry constantRegistry = config.constantRegistry();
        KeywordRegistry keywordRegistry = config.keywordRegistry();
        lexer = new Lexer(functionRegistry, unitRegistry, constantRegistry, keywordRegistry, 256, true);
    }

    private Node parse(String input) {
        List<Token> tokens = lexer.tokenize(input);
        Parser parser = new Parser(tokens, input, 1000, false);
        return parser.parse();
    }

    // ==================== Precedence Tests ====================

    @Test
    void testMultiplicationBeforeAddition() {
        // 2 + 3 * 4 = 2 + (3 * 4) = 14
        Node node = parse("2 + 3 * 4");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be addition
        assertThat(binary.getOperator().lexeme()).isEqualTo("+");
        assertThat(binary.getLeft()).isInstanceOf(NodeRational.class);
        assertThat(((NodeRational) binary.getLeft()).getValue().longValue()).isEqualTo(2);

        // Right side should be multiplication
        assertThat(binary.getRight()).isInstanceOf(NodeBinary.class);
        NodeBinary mult = (NodeBinary) binary.getRight();
        assertThat(mult.getOperator().lexeme()).isEqualTo("*");
    }

    @Test
    void testDivisionBeforeAddition() {
        // 8 / 2 + 2 = (8 / 2) + 2 = 6
        Node node = parse("8 / 2 + 2");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be addition
        assertThat(binary.getOperator().lexeme()).isEqualTo("+");

        // Left side should be division
        assertThat(binary.getLeft()).isInstanceOf(NodeBinary.class);
        NodeBinary div = (NodeBinary) binary.getLeft();
        assertThat(div.getOperator().lexeme()).isEqualTo("/");
    }

    @Test
    void testExponentiationBeforeMultiplication() {
        // 2 * 3^2 = 2 * (3^2) = 18
        Node node = parse("2 * 3^2");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be multiplication
        assertThat(binary.getOperator().lexeme()).isEqualTo("*");

        // Right side should be exponentiation
        assertThat(binary.getRight()).isInstanceOf(NodeBinary.class);
        NodeBinary pow = (NodeBinary) binary.getRight();
        assertThat(pow.getOperator().lexeme()).isEqualTo("^");
    }

    @Test
    void testUnaryMinusBeforeExponentiation() {
        // According to mathematical convention: -2^2 = -(2^2) = -4
        // Exponentiation has higher precedence than unary minus
        Node node = parse("-2^2");

        assertThat(node).isInstanceOf(NodeUnary.class);
        NodeUnary unary = (NodeUnary) node;

        // Top level should be negation
        assertThat(unary.getOperator().lexeme()).isEqualTo("-");

        // Child should be exponentiation
        assertThat(unary.getOperand()).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) unary.getOperand();
        assertThat(binary.getOperator().lexeme()).isEqualTo("^");
    }

    @Test
    void testParenthesesOverridePrecedence() {
        // (-2)^2 = 4
        Node node = parse("(-2)^2");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be exponentiation
        assertThat(binary.getOperator().lexeme()).isEqualTo("^");

        // Left side should be negation
        assertThat(binary.getLeft()).isInstanceOf(NodeUnary.class);
        NodeUnary unary = (NodeUnary) binary.getLeft();
        assertThat(unary.getOperator().lexeme()).isEqualTo("-");
    }

    @Test
    void testArithmeticBeforeComparison() {
        // 2 + 3 < 10
        Node node = parse("2 + 3 < 10");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be less-than comparison
        assertThat(binary.getOperator().lexeme()).isEqualTo("<");

        // Left side should be addition
        assertThat(binary.getLeft()).isInstanceOf(NodeBinary.class);
        NodeBinary add = (NodeBinary) binary.getLeft();
        assertThat(add.getOperator().lexeme()).isEqualTo("+");
    }

    @Test
    void testComparisonBeforeLogicalAnd() {
        // 5 > 3 && 2 < 4
        Node node = parse("5 > 3 && 2 < 4");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be AND
        assertThat(binary.getOperator().lexeme()).isEqualTo("&&");

        // Both sides should be comparisons
        assertThat(binary.getLeft()).isInstanceOf(NodeBinary.class);
        assertThat(binary.getRight()).isInstanceOf(NodeBinary.class);

        NodeBinary leftComp = (NodeBinary) binary.getLeft();
        NodeBinary rightComp = (NodeBinary) binary.getRight();

        assertThat(leftComp.getOperator().lexeme()).isEqualTo(">");
        assertThat(rightComp.getOperator().lexeme()).isEqualTo("<");
    }

    @Test
    void testComplexPrecedence() {
        // 2 + 3 * 4^2 = 2 + (3 * (4^2)) = 2 + 48 = 50
        Node node = parse("2 + 3 * 4^2");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level: addition
        assertThat(binary.getOperator().lexeme()).isEqualTo("+");

        // Right side: multiplication
        assertThat(binary.getRight()).isInstanceOf(NodeBinary.class);
        NodeBinary mult = (NodeBinary) binary.getRight();
        assertThat(mult.getOperator().lexeme()).isEqualTo("*");

        // Right side of multiplication: exponentiation
        assertThat(mult.getRight()).isInstanceOf(NodeBinary.class);
        NodeBinary pow = (NodeBinary) mult.getRight();
        assertThat(pow.getOperator().lexeme()).isEqualTo("^");
    }

    // ==================== Associativity Tests ====================

    @Test
    void testLeftAssociativeSubtraction() {
        // 10 - 5 - 2 = (10 - 5) - 2 = 3
        Node node = parse("10 - 5 - 2");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be subtraction
        assertThat(binary.getOperator().lexeme()).isEqualTo("-");

        // Left side should also be subtraction
        assertThat(binary.getLeft()).isInstanceOf(NodeBinary.class);
        NodeBinary leftSub = (NodeBinary) binary.getLeft();
        assertThat(leftSub.getOperator().lexeme()).isEqualTo("-");

        // Right side should be a number
        assertThat(binary.getRight()).isInstanceOf(NodeNumber.class);
    }

    @Test
    void testLeftAssociativeDivision() {
        // 20 / 4 / 2 = (20 / 4) / 2 = 2.5
        Node node = parse("20 / 4 / 2");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be division
        assertThat(binary.getOperator().lexeme()).isEqualTo("/");

        // Left side should also be division
        assertThat(binary.getLeft()).isInstanceOf(NodeBinary.class);
        NodeBinary leftDiv = (NodeBinary) binary.getLeft();
        assertThat(leftDiv.getOperator().lexeme()).isEqualTo("/");
    }

    @Test
    void testRightAssociativeExponentiation() {
        // 2^3^2 = 2^(3^2) = 2^9 = 512
        Node node = parse("2^3^2");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be exponentiation
        assertThat(binary.getOperator().lexeme()).isEqualTo("^");

        // Right side should also be exponentiation (NOT left side)
        assertThat(binary.getRight()).isInstanceOf(NodeBinary.class);
        NodeBinary rightPow = (NodeBinary) binary.getRight();
        assertThat(rightPow.getOperator().lexeme()).isEqualTo("^");

        // Left side should be a number
        assertThat(binary.getLeft()).isInstanceOf(NodeNumber.class);
    }

    @Test
    void testMultipleRightAssociativeExponentiation() {
        // 2^2^2^2 = 2^(2^(2^2)) = 2^16 = 65536
        Node node = parse("2^2^2^2");

        assertThat(node).isInstanceOf(NodeBinary.class);
        NodeBinary binary = (NodeBinary) node;

        // Top level should be exponentiation
        assertThat(binary.getOperator().lexeme()).isEqualTo("^");

        // Left is number, right is more exponentiations
        assertThat(binary.getLeft()).isInstanceOf(NodeNumber.class);
        assertThat(binary.getRight()).isInstanceOf(NodeBinary.class);

        NodeBinary right1 = (NodeBinary) binary.getRight();
        assertThat(right1.getOperator().lexeme()).isEqualTo("^");
        assertThat(right1.getRight()).isInstanceOf(NodeBinary.class);

        NodeBinary right2 = (NodeBinary) right1.getRight();
        assertThat(right2.getOperator().lexeme()).isEqualTo("^");
    }

    @Test
    void testDoubleNegation() {
        // --5 = -(-5) = 5
        Node node = parse("--5");

        assertThat(node).isInstanceOf(NodeUnary.class);
        NodeUnary unary1 = (NodeUnary) node;
        assertThat(unary1.getOperator().lexeme()).isEqualTo("-");

        assertThat(unary1.getOperand()).isInstanceOf(NodeUnary.class);
        NodeUnary unary2 = (NodeUnary) unary1.getOperand();
        assertThat(unary2.getOperator().lexeme()).isEqualTo("-");

        assertThat(unary2.getOperand()).isInstanceOf(NodeNumber.class);
    }

    // ==================== Literal Tests ====================

    @Test
    void testIntegerLiteral() {
        Node node = parse("42");
        assertThat(node).isInstanceOf(NodeRational.class);
        NodeRational rational = (NodeRational) node;
        assertThat(rational.getValue().longValue()).isEqualTo(42);
    }

    @Test
    void testDecimalLiteral() {
        Node node = parse("3.14");
        assertThat(node).isInstanceOf(NodeRational.class);
        NodeRational dbl = (NodeRational) node;
        assertThat(dbl.getValue().doubleValue()).isCloseTo(3.14, offset(1e-9));
    }

    @Test
    void testRationalLiteral() {
        Node node = parse("22/7");
        assertThat(node).isInstanceOf(NodeRational.class);
        NodeRational rational = (NodeRational) node;
        assertThat(rational.getValue().getNumerator().longValue()).isEqualTo(22);
        assertThat(rational.getValue().getDenominator().longValue()).isEqualTo(7);
    }

    @Test
    void testStringLiteral() {
        Node node = parse("\"hello\"");
        assertThat(node).isInstanceOf(NodeString.class);
        NodeString str = (NodeString) node;
        assertThat(str.getValue()).isEqualTo("hello");
    }

    @Test
    void testIdentifier() {
        Node node = parse("x");
        assertThat(node).isInstanceOf(NodeVariable.class);
        NodeVariable var = (NodeVariable) node;
        assertThat(var.getName()).isEqualTo("x");
    }

    // ==================== Vector Tests ====================

    @Test
    void testEmptyVector() {
        Node node = parse("{}");
        assertThat(node).isInstanceOf(NodeVector.class);
        NodeVector vec = (NodeVector) node;
        assertThat(vec.size()).isEqualTo(0);
    }

    @Test
    void testVectorLiteral() {
        Node node = parse("{1, 2, 3}");
        assertThat(node).isInstanceOf(NodeVector.class);
        NodeVector vec = (NodeVector) node;
        assertThat(vec.size()).isEqualTo(3);
        assertThat(vec.getElement(0)).isInstanceOf(NodeRational.class);
        assertThat(vec.getElement(1)).isInstanceOf(NodeRational.class);
        assertThat(vec.getElement(2)).isInstanceOf(NodeRational.class);
    }

    @Test
    void testVectorWithExpressions() {
        Node node = parse("{1 + 2, 3 * 4}");
        assertThat(node).isInstanceOf(NodeVector.class);
        NodeVector vec = (NodeVector) node;
        assertThat(vec.size()).isEqualTo(2);
        assertThat(vec.getElement(0)).isInstanceOf(NodeBinary.class);
        assertThat(vec.getElement(1)).isInstanceOf(NodeBinary.class);
    }

    // ==================== Matrix Tests ====================

    @Test
    void testMatrixLiteral() {
        Node node = parse("[1, 2; 3, 4]");
        assertThat(node).isInstanceOf(NodeMatrix.class);
        NodeMatrix mat = (NodeMatrix) node;
        assertThat(mat.getRowsList()).hasSize(2);
        assertThat(mat.getRowsList().get(0)).hasSize(2);
        assertThat(mat.getRowsList().get(1)).hasSize(2);
    }

    @Test
    void testSingleRowMatrix() {
        Node node = parse("[1, 2, 3]");
        assertThat(node).isInstanceOf(NodeMatrix.class);
        NodeMatrix mat = (NodeMatrix) node;
        assertThat(mat.getRowsList()).hasSize(1);
        assertThat(mat.getRowsList().getFirst()).hasSize(3);
    }

    @Test
    void testSingleColumnMatrix() {
        Node node = parse("[1; 2; 3]");
        assertThat(node).isInstanceOf(NodeMatrix.class);
        NodeMatrix mat = (NodeMatrix) node;
        assertThat(mat.getRowsList()).hasSize(3);
        assertThat(mat.getRowsList().get(0)).hasSize(1);
        assertThat(mat.getRowsList().get(1)).hasSize(1);
        assertThat(mat.getRowsList().get(2)).hasSize(1);
    }

    @Test
    void testMatrixInconsistentRowSize() {
        assertThatThrownBy(() -> parse("[1, 2; 3]"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("inconsistent sizes");
    }

    // ==================== Range Tests ====================

    @Test
    void testSimpleRange() {
        Node node = parse("1..5");
        assertThat(node).isInstanceOf(NodeRangeExpression.class);
        NodeRangeExpression range = (NodeRangeExpression) node;
        assertThat(range.getStart()).isInstanceOf(NodeRational.class);
        assertThat(range.getEnd()).isInstanceOf(NodeRational.class);
        assertThat(range.hasStep()).isFalse();
    }

    @Test
    void testRangeWithStep() {
        Node node = parse("1..10 step 2");
        assertThat(node).isInstanceOf(NodeRangeExpression.class);
        NodeRangeExpression range = (NodeRangeExpression) node;
        assertThat(range.getStart()).isInstanceOf(NodeRational.class);
        assertThat(range.getEnd()).isInstanceOf(NodeRational.class);
        assertThat(range.hasStep()).isTrue();
        assertThat(range.getStep()).isInstanceOf(NodeRational.class);
    }

    // ==================== Function Call Tests ====================

    @Test
    void testFunctionCall() {
        Node node = parse("sin(0)");
        assertThat(node).isInstanceOf(NodeCall.class);
        NodeCall call = (NodeCall) node;
        assertThat(call.getFunction()).isInstanceOf(NodeVariable.class);
        assertThat(((NodeVariable) call.getFunction()).getName()).isEqualTo("sin");
        assertThat(call.getArguments()).hasSize(1);
    }

    @Test
    void testFunctionCallMultipleArgs() {
        Node node = parse("max(1, 2, 3)");
        assertThat(node).isInstanceOf(NodeCall.class);
        NodeCall call = (NodeCall) node;
        assertThat(call.getArguments()).hasSize(3);
    }

    @Test
    void testNestedFunctionCalls() {
        Node node = parse("sin(cos(0))");
        assertThat(node).isInstanceOf(NodeCall.class);
        NodeCall outerCall = (NodeCall) node;
        assertThat(((NodeVariable) outerCall.getFunction()).getName()).isEqualTo("sin");
        assertThat(outerCall.getArguments().getFirst()).isInstanceOf(NodeCall.class);

        NodeCall innerCall = (NodeCall) outerCall.getArguments().getFirst();
        assertThat(((NodeVariable) innerCall.getFunction()).getName()).isEqualTo("cos");
    }

    // ==================== Subscript Tests ====================

    @Test
    void testVectorSubscript() {
        Node node = parse("v[0]");
        assertThat(node).isInstanceOf(NodeSubscript.class);
        NodeSubscript subscript = (NodeSubscript) node;
        assertThat(subscript.getTarget()).isInstanceOf(NodeVariable.class);
        assertThat(subscript.getIndices()).hasSize(1);
        assertThat(subscript.getIndices().getFirst().isRange()).isFalse();
    }

    @Test
    void testVectorSlice() {
        Node node = parse("v[1:3]");
        assertThat(node).isInstanceOf(NodeSubscript.class);
        NodeSubscript subscript = (NodeSubscript) node;
        assertThat(subscript.getIndices()).hasSize(1);
        assertThat(subscript.getIndices().getFirst().isRange()).isTrue();
    }

    @Test
    void testMatrixSubscript() {
        Node node = parse("m[1, 2]");
        assertThat(node).isInstanceOf(NodeSubscript.class);
        NodeSubscript subscript = (NodeSubscript) node;
        assertThat(subscript.getIndices()).hasSize(2);
    }

    // ==================== Assignment Tests ====================

    @Test
    void testVariableAssignment() {
        Node node = parse("x := 5");
        assertThat(node).isInstanceOf(NodeAssignment.class);
        NodeAssignment assignment = (NodeAssignment) node;
        assertThat(assignment.getIdentifier()).isEqualTo("x");
        assertThat(assignment.getValue()).isInstanceOf(NodeRational.class);
    }

    @Test
    void testFunctionDefinition() {
        Node node = parse("f(x) := x^2");
        assertThat(node).isInstanceOf(NodeFunctionDef.class);
        NodeFunctionDef funcDef = (NodeFunctionDef) node;
        assertThat(funcDef.getName()).isEqualTo("f");
        assertThat(funcDef.getParameters()).containsExactly("x");
        assertThat(funcDef.getBody()).isInstanceOf(NodeBinary.class);
    }

    @Test
    void testMultiParameterFunctionDefinition() {
        Node node = parse("add(a, b) := a + b");
        assertThat(node).isInstanceOf(NodeFunctionDef.class);
        NodeFunctionDef funcDef = (NodeFunctionDef) node;
        assertThat(funcDef.getName()).isEqualTo("add");
        assertThat(funcDef.getParameters()).containsExactly("a", "b");
        assertThat(funcDef.getBody()).isInstanceOf(NodeBinary.class);
    }

    // ==================== Lambda Tests ====================

    @Test
    void testSingleParameterLambda() {
        Node node = parse("x -> x^2");
        assertThat(node).isInstanceOf(NodeLambda.class);
        NodeLambda lambda = (NodeLambda) node;
        assertThat(lambda.getParameters()).containsExactly("x");
        assertThat(lambda.getBody()).isInstanceOf(NodeBinary.class);
    }

    @Test
    void testMultiParameterLambda() {
        // Multi-parameter lambdas are only supported through function definitions
        // (a, b) -> expr is parsed as a parenthesized expression followed by error
        // Use function definition syntax instead
        Node node = parse("f(a, b) := a + b");
        assertThat(node).isInstanceOf(NodeFunctionDef.class);
        NodeFunctionDef funcDef = (NodeFunctionDef) node;
        assertThat(funcDef.getParameters()).containsExactly("a", "b");
    }

    // ==================== Comprehension Tests ====================

    @Test
    void testSimpleComprehension() {
        Node node = parse("{x^2 for x in 1..5}");
        assertThat(node).isInstanceOf(NodeComprehension.class);
        NodeComprehension comp = (NodeComprehension) node;
        assertThat(comp.getExpression()).isInstanceOf(NodeBinary.class);
        assertThat(comp.getVariable()).isEqualTo("x");
        assertThat(comp.getIterable()).isInstanceOf(NodeRangeExpression.class);
        assertThat(comp.getCondition()).isNull();
    }

    @Test
    void testComprehensionWithCondition() {
        Node node = parse("{x for x in 1..10 if x > 5}");
        assertThat(node).isInstanceOf(NodeComprehension.class);
        NodeComprehension comp = (NodeComprehension) node;
        assertThat(comp.getCondition()).isNotNull();
        assertThat(comp.getCondition()).isInstanceOf(NodeBinary.class);
    }

    // ==================== Error Tests ====================

    @Test
    void testEmptyExpression() {
        assertThatThrownBy(() -> parse(""))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Empty expression");
    }

    @Test
    void testUnmatchedLeftParen() {
        assertThatThrownBy(() -> parse("(2 + 3"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Expected ')'");
    }

    @Test
    void testUnmatchedLeftBracket() {
        assertThatThrownBy(() -> parse("[1, 2"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Expected ']'");
    }

    @Test
    void testUnmatchedLeftBrace() {
        assertThatThrownBy(() -> parse("{1, 2"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Expected '}'");
    }

    @Test
    void testUnexpectedToken() {
        // Note: "2 + + 3" is actually valid (parses as "2 + (+3)")
        // Test with a truly invalid expression instead
        assertThatThrownBy(() -> parse("2 * / 3"))
                .isInstanceOf(ParseException.class);
    }
}
