package uk.co.ryanharrison.mathengine.parser.format;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.registry.SymbolRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StringNodeFormatterTest {

    private final StringNodeFormatter fmt = StringNodeFormatter.fullPrecision();

    // ==================== Factory Methods ====================

    @Test
    void fullPrecisionFactoryReturnsInstance() {
        assertThat(StringNodeFormatter.fullPrecision()).isNotNull();
    }

    // ==================== NodeDouble ====================

    @ParameterizedTest
    @CsvSource({
            "0.0,     0",
            "1.0,     1",
            "-1.0,    -1",
            "3.14,    3.14",
            "100.0,   100",
            "-42.5,   -42.5"
    })
    void formatsDoubleValues(double value, String expected) {
        assertThat(fmt.format(new NodeDouble(value))).isEqualTo(expected);
    }

    @Test
    void formatsNaN() {
        assertThat(fmt.format(new NodeDouble(Double.NaN))).isEqualTo("NaN");
    }

    @Test
    void formatsPositiveInfinity() {
        assertThat(fmt.format(new NodeDouble(Double.POSITIVE_INFINITY))).isEqualTo("Infinity");
    }

    @Test
    void formatsNegativeInfinity() {
        assertThat(fmt.format(new NodeDouble(Double.NEGATIVE_INFINITY))).isEqualTo("-Infinity");
    }

    @Test
    void formatsDoubleWithDecimalPlaces() {
        var rounded = StringNodeFormatter.withDecimalPlaces(2);
        assertThat(rounded.format(new NodeDouble(3.14159))).isEqualTo("3.14");
    }

    @Test
    void formatsDoubleWithZeroDecimalPlaces() {
        var rounded = StringNodeFormatter.withDecimalPlaces(0);
        assertThat(rounded.format(new NodeDouble(3.7))).isEqualTo("4");
    }

    // ==================== NodeRational ====================

    @Test
    void formatsRationalInteger() {
        assertThat(fmt.format(new NodeRational(5))).isEqualTo("5");
    }

    @Test
    void formatsRationalFraction() {
        assertThat(fmt.format(new NodeRational(3, 4))).isEqualTo("3/4");
    }

    @Test
    void formatsRationalFractionIgnoresDecimalPlaces() {
        var rounded = StringNodeFormatter.withDecimalPlaces(3);
        // Rationals always display as exact fractions, never lossy decimals
        assertThat(rounded.format(new NodeRational(1, 3))).isEqualTo("1/3");
    }

    // ==================== NodePercent ====================

    @Test
    void formatsPercent() {
        // NodePercent(50) stores as 0.5, getPercentValue() returns 50.0
        assertThat(fmt.format(new NodePercent(50))).isEqualTo("50%");
    }

    @Test
    void formatsPercentFractional() {
        assertThat(fmt.format(new NodePercent(33.33))).isEqualTo("33.33%");
    }

    // ==================== NodeBoolean ====================

    @Test
    void formatsTrue() {
        assertThat(fmt.format(NodeBoolean.TRUE)).isEqualTo("true");
    }

    @Test
    void formatsFalse() {
        assertThat(fmt.format(NodeBoolean.FALSE)).isEqualTo("false");
    }

    // ==================== NodeString ====================

    @Test
    void formatsString() {
        assertThat(fmt.format(new NodeString("hello"))).isEqualTo("\"hello\"");
    }

    @Test
    void formatsEmptyString() {
        assertThat(fmt.format(new NodeString(""))).isEqualTo("\"\"");
    }

    // ==================== NodeVector ====================

    @Test
    void formatsVector() {
        var vec = new NodeVector(new Node[]{
                new NodeDouble(1), new NodeDouble(2), new NodeDouble(3)
        });
        assertThat(fmt.format(vec)).isEqualTo("{1, 2, 3}");
    }

    @Test
    void formatsEmptyVector() {
        var vec = new NodeVector(new Node[]{});
        assertThat(fmt.format(vec)).isEqualTo("{}");
    }

    @Test
    void formatsNestedVector() {
        var inner = new NodeVector(new Node[]{new NodeDouble(1), new NodeDouble(2)});
        var outer = new NodeVector(new Node[]{inner, new NodeDouble(3)});
        assertThat(fmt.format(outer)).isEqualTo("{{1, 2}, 3}");
    }

    // ==================== NodeMatrix ====================

    @Test
    void formatsMatrix() {
        var mat = new NodeMatrix(new Node[][]{
                {new NodeDouble(1), new NodeDouble(2)},
                {new NodeDouble(3), new NodeDouble(4)}
        });
        assertThat(fmt.format(mat)).isEqualTo("[1, 2; 3, 4]");
    }

    @Test
    void formatsSingleRowMatrix() {
        var mat = new NodeMatrix(new Node[][]{
                {new NodeDouble(1), new NodeDouble(2), new NodeDouble(3)}
        });
        assertThat(fmt.format(mat)).isEqualTo("[1, 2, 3]");
    }

    // ==================== NodeUnit ====================

    @Test
    void formatsUnitSingular() {
        var unitDef = new UnitDefinition("meter", "meters", "length", "meter",
                1.0, 0.0, List.of("m"));
        var unit = NodeUnit.of(1.0, unitDef);
        assertThat(fmt.format(unit)).isEqualTo("1 meter");
    }

    @Test
    void formatsUnitPlural() {
        var unitDef = new UnitDefinition("meter", "meters", "length", "meter",
                1.0, 0.0, List.of("m"));
        var unit = NodeUnit.of(5.0, unitDef);
        assertThat(fmt.format(unit)).isEqualTo("5 meters");
    }

    // ==================== NodeRange ====================

    @Test
    void formatsRangeDefaultStep() {
        var range = new NodeRange(new NodeRational(1), new NodeRational(10), null);
        assertThat(fmt.format(range)).isEqualTo("1..10");
    }

    @Test
    void formatsRangeCustomStep() {
        var range = new NodeRange(new NodeRational(1), new NodeRational(10), new NodeRational(2));
        assertThat(fmt.format(range)).isEqualTo("1..10 step 2");
    }

    // ==================== NodeLambda ====================

    @Test
    void formatsSingleParamLambda() {
        var lambda = new NodeLambda(
                List.of("x"),
                binary(TokenType.MULTIPLY, var("x"), var("x"))
        );
        assertThat(fmt.format(lambda)).isEqualTo("x -> (x * x)");
    }

    @Test
    void formatsMultiParamLambda() {
        var lambda = new NodeLambda(
                List.of("x", "y"),
                binary(TokenType.PLUS, var("x"), var("y"))
        );
        assertThat(fmt.format(lambda)).isEqualTo("(x, y) -> (x + y)");
    }

    // ==================== NodeFunction ====================

    @Test
    void formatsFunction() {
        var def = new FunctionDefinition("myFunc", List.of("x", "y"),
                binary(TokenType.PLUS, var("x"), var("y")), null);
        assertThat(fmt.format(new NodeFunction(def))).isEqualTo("<function:myFunc>");
    }

    // ==================== NodeBinary ====================

    @Test
    void formatsAddition() {
        var node = binary(TokenType.PLUS, dbl(1), dbl(2));
        assertThat(fmt.format(node)).isEqualTo("(1 + 2)");
    }

    @Test
    void formatsNestedBinary() {
        var inner = binary(TokenType.MULTIPLY, dbl(2), dbl(3));
        var outer = binary(TokenType.PLUS, dbl(1), inner);
        assertThat(fmt.format(outer)).isEqualTo("(1 + (2 * 3))");
    }

    @ParameterizedTest
    @CsvSource({
            "PLUS,     +",
            "MINUS,    -",
            "MULTIPLY, *",
            "DIVIDE,   /",
            "POWER,    ^",
            "MOD,      %",
            "EQ,       ==",
            "NEQ,      !=",
            "LT,       <",
            "GT,       >",
            "LTE,      <=",
            "GTE,      >=",
            "AND,      and",
            "OR,       or",
            "XOR,      xor"
    })
    void formatsBinaryOperators(String tokenType, String expectedOp) {
        var node = binary(TokenType.valueOf(tokenType), var("a"), var("b"));
        assertThat(fmt.format(node)).isEqualTo("(a " + expectedOp + " b)");
    }

    // ==================== NodeUnary ====================

    @Test
    void formatsUnaryMinus() {
        var node = prefixUnary(TokenType.MINUS, "-", dbl(5));
        assertThat(fmt.format(node)).isEqualTo("-5");
    }

    @Test
    void formatsUnaryNot() {
        var node = prefixUnary(TokenType.NOT, "not", var("x"));
        assertThat(fmt.format(node)).isEqualTo("not x");
    }

    @Test
    void formatsFactorial() {
        var node = postfixUnary(TokenType.FACTORIAL, "!", dbl(5));
        assertThat(fmt.format(node)).isEqualTo("5!");
    }

    @Test
    void formatsDoubleFactorial() {
        var node = postfixUnary(TokenType.DOUBLE_FACTORIAL, "!!", dbl(7));
        assertThat(fmt.format(node)).isEqualTo("7!!");
    }

    @Test
    void formatsPostfixPercent() {
        var node = postfixUnary(TokenType.PERCENT, "%", dbl(50));
        assertThat(fmt.format(node)).isEqualTo("50%");
    }

    // ==================== NodeCall ====================

    @Test
    void formatsSimpleFunctionCall() {
        var call = new NodeCall(var("sin"), List.of(var("x")));
        assertThat(fmt.format(call)).isEqualTo("sin(x)");
    }

    @Test
    void formatsMultiArgFunctionCall() {
        var call = new NodeCall(var("max"), List.of(dbl(1), dbl(2), dbl(3)));
        assertThat(fmt.format(call)).isEqualTo("max(1, 2, 3)");
    }

    @Test
    void formatsNestedFunctionCall() {
        var inner = new NodeCall(var("cos"), List.of(var("x")));
        var outer = new NodeCall(var("sin"), List.of(inner));
        assertThat(fmt.format(outer)).isEqualTo("sin(cos(x))");
    }

    // ==================== NodeVariable ====================

    @Test
    void formatsVariable() {
        assertThat(fmt.format(var("myVar"))).isEqualTo("myVar");
    }

    // ==================== NodeAssignment ====================

    @Test
    void formatsAssignment() {
        var assignment = new NodeAssignment("x", dbl(42));
        assertThat(fmt.format(assignment)).isEqualTo("x := 42");
    }

    // ==================== NodeFunctionDef ====================

    @Test
    void formatsFunctionDef() {
        var def = new NodeFunctionDef("f", List.of("x", "y"),
                binary(TokenType.PLUS, var("x"), var("y")));
        assertThat(fmt.format(def)).isEqualTo("f(x, y) := (x + y)");
    }

    // ==================== NodeSubscript ====================

    @Test
    void formatsSimpleSubscript() {
        var sub = new NodeSubscript(var("v"),
                List.of(new NodeSubscript.SliceArg(dbl(0), null, false)));
        assertThat(fmt.format(sub)).isEqualTo("v[0]");
    }

    @Test
    void formatsSliceSubscript() {
        var sub = new NodeSubscript(var("v"),
                List.of(new NodeSubscript.SliceArg(dbl(1), dbl(3))));
        assertThat(fmt.format(sub)).isEqualTo("v[1:3]");
    }

    @Test
    void formatsOpenEndSlice() {
        var sub = new NodeSubscript(var("v"),
                List.of(new NodeSubscript.SliceArg(dbl(2), null, true)));
        assertThat(fmt.format(sub)).isEqualTo("v[2:]");
    }

    @Test
    void formatsOpenStartSlice() {
        var sub = new NodeSubscript(var("v"),
                List.of(new NodeSubscript.SliceArg(null, dbl(5))));
        assertThat(fmt.format(sub)).isEqualTo("v[:5]");
    }

    // ==================== NodeRangeExpression ====================

    @Test
    void formatsRangeExpressionNoStep() {
        var range = new NodeRangeExpression(dbl(1), dbl(10), null);
        assertThat(fmt.format(range)).isEqualTo("1..10");
    }

    @Test
    void formatsRangeExpressionWithStep() {
        var range = new NodeRangeExpression(dbl(0), dbl(100), dbl(5));
        assertThat(fmt.format(range)).isEqualTo("0..100 step 5");
    }

    // ==================== NodeUnitConversion ====================

    @Test
    void formatsUnitConversion() {
        var conv = new NodeUnitConversion(dbl(100), "feet");
        assertThat(fmt.format(conv)).isEqualTo("100 in feet");
    }

    // ==================== NodeSequence ====================

    @Test
    void formatsSequence() {
        var seq = new NodeSequence(List.of(
                new NodeAssignment("x", dbl(5)),
                new NodeAssignment("y", dbl(10)),
                binary(TokenType.PLUS, var("x"), var("y"))
        ));
        assertThat(fmt.format(seq)).isEqualTo("x := 5; y := 10; (x + y)");
    }

    // ==================== NodeComprehension ====================

    @Test
    void formatsSimpleComprehension() {
        var comp = new NodeComprehension(
                binary(TokenType.POWER, var("x"), dbl(2)),
                List.of(new NodeComprehension.Iterator("x", new NodeRangeExpression(dbl(1), dbl(10), null))),
                null
        );
        assertThat(fmt.format(comp)).isEqualTo("{(x ^ 2) for x in 1..10}");
    }

    @Test
    void formatsComprehensionWithCondition() {
        var comp = new NodeComprehension(
                var("x"),
                List.of(new NodeComprehension.Iterator("x", new NodeRangeExpression(dbl(1), dbl(10), null))),
                binary(TokenType.GT, var("x"), dbl(5))
        );
        assertThat(fmt.format(comp)).isEqualTo("{x for x in 1..10 if (x > 5)}");
    }

    @Test
    void formatsNestedComprehension() {
        var comp = new NodeComprehension(
                binary(TokenType.MULTIPLY, var("x"), var("y")),
                List.of(
                        new NodeComprehension.Iterator("x", new NodeRangeExpression(dbl(1), dbl(3), null)),
                        new NodeComprehension.Iterator("y", new NodeRangeExpression(dbl(1), dbl(3), null))
                ),
                null
        );
        assertThat(fmt.format(comp)).isEqualTo("{(x * y) for x in 1..3 for y in 1..3}");
    }

    // ==================== Explicit References ====================

    @Test
    void formatsUnitRef() {
        assertThat(fmt.format(new NodeUnitRef("fahrenheit"))).isEqualTo("@fahrenheit");
    }

    @Test
    void formatsVarRef() {
        assertThat(fmt.format(new NodeVarRef("pi"))).isEqualTo("$pi");
    }

    @Test
    void formatsConstRef() {
        assertThat(fmt.format(new NodeConstRef("pi"))).isEqualTo("#pi");
    }

    // ==================== Complex / Deep Nesting ====================

    @Test
    void formatsDeepExpression() {
        // sin(x^2 + cos(y * pi)) / (1 + abs(z))
        var sinArg = binary(TokenType.PLUS,
                binary(TokenType.POWER, var("x"), dbl(2)),
                new NodeCall(var("cos"), List.of(
                        binary(TokenType.MULTIPLY, var("y"), var("pi"))
                ))
        );
        var sinCall = new NodeCall(var("sin"), List.of(sinArg));
        var absCall = new NodeCall(var("abs"), List.of(var("z")));
        var denominator = binary(TokenType.PLUS, dbl(1), absCall);
        var division = binary(TokenType.DIVIDE, sinCall, denominator);

        assertThat(fmt.format(division))
                .isEqualTo("(sin(((x ^ 2) + cos((y * pi)))) / (1 + abs(z)))");
    }

    @Test
    void formatsExpressionWithMixedTypes() {
        // {sum(v) for v in {1..3, 4..6} if len(v) > 0}
        var rangeExpr1 = new NodeRangeExpression(dbl(1), dbl(3), null);
        var rangeExpr2 = new NodeRangeExpression(dbl(4), dbl(6), null);
        var vectorExpr = new NodeVector(new Node[]{rangeExpr1, rangeExpr2});
        var sumCall = new NodeCall(var("sum"), List.of(var("v")));
        var lenCall = new NodeCall(var("len"), List.of(var("v")));
        var condition = binary(TokenType.GT, lenCall, dbl(0));

        var comp = new NodeComprehension(
                sumCall,
                List.of(new NodeComprehension.Iterator("v", vectorExpr)),
                condition
        );

        assertThat(fmt.format(comp))
                .isEqualTo("{sum(v) for v in {1..3, 4..6} if (len(v) > 0)}");
    }

    @Test
    void formatsLambdaWithComplexBody() {
        // (x, y) -> x^2 + y^2
        var body = binary(TokenType.PLUS,
                binary(TokenType.POWER, var("x"), dbl(2)),
                binary(TokenType.POWER, var("y"), dbl(2))
        );
        var lambda = new NodeLambda(List.of("x", "y"), body);
        assertThat(fmt.format(lambda)).isEqualTo("(x, y) -> ((x ^ 2) + (y ^ 2))");
    }

    @Test
    void formatsSequenceWithFunctionDefAndCall() {
        // f(x) := x^2; f(5)
        var funcDef = new NodeFunctionDef("f", List.of("x"),
                binary(TokenType.POWER, var("x"), dbl(2)));
        var funcCall = new NodeCall(var("f"), List.of(dbl(5)));
        var seq = new NodeSequence(List.of(funcDef, funcCall));

        assertThat(fmt.format(seq)).isEqualTo("f(x) := (x ^ 2); f(5)");
    }

    // ==================== Test Helpers ====================

    private static NodeDouble dbl(double value) {
        return new NodeDouble(value);
    }

    private static NodeVariable var(String name) {
        return new NodeVariable(name);
    }

    private static Token token(TokenType type, String lexeme) {
        return new Token(type, lexeme, 0, 0);
    }

    private static NodeBinary binary(TokenType type, Node left, Node right) {
        String lexeme = SymbolRegistry.getDefault()
                .getMetadata(type)
                .map(SymbolRegistry.SymbolMetadata::getInputSymbol)
                .orElse(type.name());
        return new NodeBinary(token(type, lexeme), left, right);
    }

    private static NodeUnary prefixUnary(TokenType type, String lexeme, Node operand) {
        return new NodeUnary(token(type, lexeme), operand, true);
    }

    private static NodeUnary postfixUnary(TokenType type, String lexeme, Node operand) {
        return new NodeUnary(token(type, lexeme), operand, false);
    }
}
