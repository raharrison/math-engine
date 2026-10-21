package uk.co.ryanharrison.mathengine.parser.syntax;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.function.FunctionExecutor;
import uk.co.ryanharrison.mathengine.parser.lexer.Lexer;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.KeywordRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.List;
import java.util.Set;

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
        Set<String> functionNames = FunctionExecutor.of(config.functions()).getCallableNames();
        UnitRegistry unitRegistry = config.unitRegistry();
        ConstantRegistry constantRegistry = config.constantRegistry();
        KeywordRegistry keywordRegistry = config.keywordRegistry();
        lexer = new Lexer(functionNames, unitRegistry, constantRegistry, keywordRegistry, 256, true);
    }

    private Node parse(String input) {
        List<Token> tokens = lexer.tokenize(input);
        Parser parser = new Parser(tokens, input, 1000, false, 10_000);
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
        Node node = parse("x = 5");
        assertThat(node).isInstanceOf(NodeAssignment.class);
        NodeAssignment assignment = (NodeAssignment) node;
        assertThat(assignment.getIdentifier()).isEqualTo("x");
        assertThat(assignment.getValue()).isInstanceOf(NodeRational.class);
    }

    @Test
    void testElementAssignment() {
        Node node = parse("v[0] = 5");
        assertThat(node).isInstanceOf(NodeElementAssignment.class);
        NodeElementAssignment assignment = (NodeElementAssignment) node;
        assertThat(assignment.getIdentifier()).isEqualTo("v");
        assertThat(assignment.getIndexGroups()).hasSize(1);
        assertThat(assignment.getIndexGroups().getFirst()).hasSize(1);
        assertThat(assignment.getValue()).isInstanceOf(NodeRational.class);
    }

    @Test
    void testMatrixElementAssignment() {
        Node node = parse("m[1, 2] = 9");
        assertThat(node).isInstanceOf(NodeElementAssignment.class);
        NodeElementAssignment assignment = (NodeElementAssignment) node;
        assertThat(assignment.getIndexGroups()).hasSize(1);
        assertThat(assignment.getIndexGroups().getFirst()).hasSize(2);
    }

    @Test
    void testChainedElementAssignment() {
        Node node = parse("m[0][1] = 9");
        assertThat(node).isInstanceOf(NodeElementAssignment.class);
        NodeElementAssignment assignment = (NodeElementAssignment) node;
        assertThat(assignment.getIndexGroups()).hasSize(2);
        assertThat(assignment.getIndexGroups().getFirst()).hasSize(1);
        assertThat(assignment.getIndexGroups().getLast()).hasSize(1);
    }

    @Test
    void testSubscriptWithoutAssignStaysARead() {
        assertThat(parse("v[0] + 1")).isInstanceOf(NodeBinary.class);
        assertThat(parse("v[0]")).isInstanceOf(NodeSubscript.class);
        assertThat(parse("v[0] == 1")).isInstanceOf(NodeBinary.class);
        assertThat(parse("v[0]; x = 1")).isInstanceOf(NodeSequence.class);
    }

    @Test
    void testElementAssignmentTargetMustBeAName() {
        assertThatThrownBy(() -> parse("sin(1)[0] = 5")).isInstanceOf(ParseException.class);
        assertThatThrownBy(() -> parse("{1, 2}[0] = 5")).isInstanceOf(ParseException.class);
    }

    @Test
    void testFunctionDefinition() {
        Node node = parse("f(x) = x^2");
        assertThat(node).isInstanceOf(NodeFunctionDef.class);
        NodeFunctionDef funcDef = (NodeFunctionDef) node;
        assertThat(funcDef.getName()).isEqualTo("f");
        assertThat(funcDef.getParameters()).containsExactly("x");
        assertThat(funcDef.getBody()).isInstanceOf(NodeBinary.class);
    }

    @Test
    void testMultiParameterFunctionDefinition() {
        Node node = parse("add(a, b) = a + b");
        assertThat(node).isInstanceOf(NodeFunctionDef.class);
        NodeFunctionDef funcDef = (NodeFunctionDef) node;
        assertThat(funcDef.getName()).isEqualTo("add");
        assertThat(funcDef.getParameters()).containsExactly("a", "b");
        assertThat(funcDef.getBody()).isInstanceOf(NodeBinary.class);
    }

    // ==================== Assignment Context Tests ====================

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "x = 5         | x := 5",
            "v[0] = 5      | v[0] := 5",
            "m[1, 2] = 9   | m[1, 2] := 9",
            "m[0][1] = 9   | m[0][1] := 9",
            "f(x) = x^2    | f(x) := x^2",
            "f() = 1       | f() := 1",
            "g = x -> x^2  | g := x -> x^2"
    })
    void bothSpellingsBuildTheSameStatement(String equals, String inline) {
        Node withEquals = parse(equals);
        Node withInline = parse(inline);

        assertThat(withEquals).isInstanceOf(withInline.getClass());
        assertThat(withEquals.toString()).isEqualTo(withInline.toString());
    }

    @Test
    void equalsAssignsInEveryStatementOfASequence() {
        Node node = parse("x = 1; y = 2; x + y");

        assertThat(node).isInstanceOf(NodeSequence.class);
        List<Node> statements = ((NodeSequence) node).getStatements();
        assertThat(statements).hasSize(3);
        assertThat(statements.get(0)).isInstanceOf(NodeAssignment.class);
        assertThat(statements.get(1)).isInstanceOf(NodeAssignment.class);
        assertThat(statements.get(2)).isInstanceOf(NodeBinary.class);
    }

    @Test
    void equalsAssignsInsideParentheses() {
        assertThat(parse("(x = 5)")).isInstanceOf(NodeAssignment.class);

        Node group = parse("(x = 2) + 1");
        assertThat(group).isInstanceOf(NodeBinary.class);
        assertThat(((NodeBinary) group).getLeft()).isInstanceOf(NodeAssignment.class);

        Node sequence = parse("(a = 1; b = 2; a + b)");
        assertThat(sequence).isInstanceOf(NodeSequence.class);
        assertThat(((NodeSequence) sequence).getStatements().get(1)).isInstanceOf(NodeAssignment.class);
    }

    @Test
    void parenthesesOptIntoAssignmentInsideAnArgument() {
        Node node = parse("if((x = 5), 1, 2)");

        assertThat(node).isInstanceOf(NodeCall.class);
        assertThat(((NodeCall) node).getArguments().getFirst()).isInstanceOf(NodeAssignment.class);
    }

    @Test
    void functionBodyInParenthesesMayAssign() {
        Node node = parse("f(y) = (x = y + 1; x * 2)");

        assertThat(node).isInstanceOf(NodeFunctionDef.class);
        Node body = ((NodeFunctionDef) node).getBody();
        assertThat(body).isInstanceOf(NodeSequence.class);
        assertThat(((NodeSequence) body).getStatements().getFirst()).isInstanceOf(NodeAssignment.class);
    }

    @Test
    void lambdaBodyInParenthesesMayAssign() {
        Node node = parse("f = i -> (v[i] = 99)");

        assertThat(node).isInstanceOf(NodeAssignment.class);
        Node lambda = ((NodeAssignment) node).getValue();
        assertThat(lambda).isInstanceOf(NodeLambda.class);
        assertThat(((NodeLambda) lambda).getBody()).isInstanceOf(NodeElementAssignment.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "if(x = 5, 1, 2)",
            "sum(a = 3, a)",
            "{a = 1}",
            "{1, a = 2}",
            "[a = 1]",
            "[1, 2; a = 3, 4]",
            "v[i = 1]",
            "v[0][i = 1] = 2",
            "{x for x in v if x = 2}",
            "{y = x for x in v}",
            "x -> y = x",
            "f = x -> y = x",
            "(x, y) -> z = x",
            "1..sum(n = 3)",
            "x = sum(a = 1)"
    })
    void equalsIsRefusedInsideAnExpression(String input) {
        assertThatThrownBy(() -> parse(input))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Use '==' to compare")
                .hasMessageContaining("'=' assigns only as a statement");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "map(f(x) = x, v)       | use ':=' to define a function inside an expression",
            "g(f(x) = 1)            | use ':=' to define a function inside an expression",
            "f = i -> v[i] = 99     | use ':=' to write an element inside an expression",
            "sum(v[0] = 1)          | use ':=' to write an element inside an expression"
    })
    void misplacedDefinitionOrElementWriteSuggestsInlineAssignment(String input, String advice) {
        assertThatThrownBy(() -> parse(input))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("'=' assigns only as a statement")
                .hasMessageContaining(advice)
                .hasMessageNotContaining("==");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sin(x) = 0", "abs(x) = x", "dist(a, b, c, d) = 1", "if(sin(x) = 0, 1, 2)"})
    void equalsDoesNotRedefineABuiltInFunction(String input) {
        String name = input.startsWith("if(") ? "sin" : input.substring(0, input.indexOf('('));

        assertThatThrownBy(() -> parse(input))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("'" + name + "' is a built-in function")
                .hasMessageContaining("use ':=' to redefine it, or '==' to compare");
    }

    @Test
    void inlineAssignmentStillRedefinesABuiltInFunction() {
        Node node = parse("sin(x) := 0");

        assertThat(node).isInstanceOf(NodeFunctionDef.class);
        assertThat(((NodeFunctionDef) node).getName()).isEqualTo("sin");
    }

    @Test
    void equalsMayStillNameAVariableAfterABuiltInFunction() {
        Node node = parse("sum = 5");

        assertThat(node).isInstanceOf(NodeAssignment.class);
        assertThat(((NodeAssignment) node).getIdentifier()).isEqualTo("sum");
    }

    @ParameterizedTest
    @ValueSource(strings = {"f(2) = 4", "sqrt(4) = 2", "f(x, 2) = 1", "f(x + 1) = 2", "f(-x) = 1"})
    void callFollowedByEqualsIsAComparisonNotADefinition(String input) {
        assertThatThrownBy(() -> parse(input))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Cannot assign to this expression")
                .hasMessageContaining("use '==' to compare");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "f() = 1                       | 0",
            "f(x) = x                      | 1",
            "f(x, y, z) = x                | 3",
            "f(m) = m                      | 1",
            "convert(x, from, to) = x      | 3"
    })
    void signatureOfPlainNamesIsADefinition(String input, int parameters) {
        Node node = parse(input);

        assertThat(node).isInstanceOf(NodeFunctionDef.class);
        assertThat(((NodeFunctionDef) node).getParameters()).hasSize(parameters);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "x %= 2   | '%=' is not supported; write 'x = x mod value'",
            "x mod= 2 | 'mod=' is not supported; write 'x = x mod value'"
    })
    void compoundModuloIsRefusedWithAReason(String input, String message) {
        assertThatThrownBy(() -> parse(input))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Compound assignment " + message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"if(x = 5, 1, 2)", "2 + 2 = 4", "x = y = 5", "x += 1"})
    void misplacedEqualsIsReportedAtTheEqualsSign(String input) {
        int column = input.lastIndexOf('=') + 1;

        assertThatThrownBy(() -> parse(input))
                .isInstanceOfSatisfying(ParseException.class,
                        e -> assertThat(e.formatMessage()).contains("line 1, column " + column + ":"));
    }

    @Test
    void inlineAssignmentWorksInsideAnExpression() {
        Node call = parse("if(x := 5, 1, 2)");
        assertThat(((NodeCall) call).getArguments().getFirst()).isInstanceOf(NodeAssignment.class);

        Node vector = parse("{a := 1, a + 1}");
        assertThat(vector).isInstanceOf(NodeVector.class);

        Node comprehension = parse("{x for x in v if x := 2}");
        assertThat(((NodeComprehension) comprehension).getCondition()).isInstanceOf(NodeAssignment.class);

        Node lambda = parse("x -> y := x");
        assertThat(((NodeLambda) lambda).getBody()).isInstanceOf(NodeAssignment.class);

        Node subscript = parse("v[i := 1]");
        assertThat(subscript).isInstanceOf(NodeSubscript.class);

        Node element = parse("f = i -> v[i] := 99");
        assertThat(((NodeLambda) ((NodeAssignment) element).getValue()).getBody())
                .isInstanceOf(NodeElementAssignment.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "x = y = 5",
            "x := y := 5",
            "x = y := 5",
            "x := y = 5",
            "v[0] = w[0] = 1",
            "f(x) = g(x) = 1",
            "(x = y = 5)",
            "sum(a := b := 1)"
    })
    void assignmentsCannotBeChained(String input) {
        assertThatThrownBy(() -> parse(input))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("cannot be chained");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2 + 2 = 4",
            "x^2 = 4",
            "2x = 10",
            "x! = 2",
            "5! = 120",
            "-x = 1",
            "(x) = 1",
            "x + 1 = 2",
            "#pi = 3",
            "$x = 1",
            "@m = 1",
            "\"s\" = 1",
            "in = 3",
            "sin(1)[0] = 5",
            "{1, 2}[0] = 5",
            "x == 1 = 2"
    })
    void onlyANameSubscriptOrSignatureCanBeAssigned(String input) {
        assertThatThrownBy(() -> parse(input))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Cannot assign to this expression")
                .hasMessageContaining("use '==' to compare");
    }

    @Test
    void inlineAssignmentToAnExpressionHasNoComparisonHint() {
        assertThatThrownBy(() -> parse("2 + x := 5"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Cannot assign to this expression")
                .hasMessageNotContaining("==");
    }

    @ParameterizedTest
    @ValueSource(strings = {"+", "-", "*", "/", "^"})
    void compoundAssignmentIsRefusedWithAReason(String operator) {
        assertThatThrownBy(() -> parse("x " + operator + "= 1"))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Compound assignment '" + operator + "=' is not supported");
    }

    @ParameterizedTest
    @ValueSource(strings = {"x = = 5", "x === 5", "x + = 1", "x := = 5", "x; = 5"})
    void strayEqualsWhereAValueBelongsSuggestsComparison(String input) {
        assertThatThrownBy(() -> parse(input))
                .isInstanceOf(ParseException.class)
                .hasMessageContaining("Expected expression, not '='")
                .hasMessageContaining("use '==' to compare");
    }

    @ParameterizedTest
    @ValueSource(strings = {"x =", "= 5", "f(x) ="})
    void missingSideOfAnAssignmentIsAnError(String input) {
        assertThatThrownBy(() -> parse(input)).isInstanceOf(ParseException.class);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "x == 5 | EQ",
            "x != 5 | NEQ",
            "x <= 5 | LTE",
            "x >= 5 | GTE",
            "5!=120 | NEQ"
    })
    void comparisonsAreUnaffectedByAssignment(String input, TokenType operator) {
        Node node = parse(input);

        assertThat(node).isInstanceOf(NodeBinary.class);
        assertThat(((NodeBinary) node).getOperator().type()).isEqualTo(operator);
    }

    @Test
    void assignedValueMayBeAComparison() {
        Node node = parse("x = 5 == 5");

        assertThat(node).isInstanceOf(NodeAssignment.class);
        Node value = ((NodeAssignment) node).getValue();
        assertThat(((NodeBinary) value).getOperator().type()).isEqualTo(TokenType.EQ);
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
        Node node = parse("f(a, b) = a + b");
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
