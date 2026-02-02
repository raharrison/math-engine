package uk.co.ryanharrison.mathengine.parser.format;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AsciiMathNodeFormatterTest {

    private final AsciiMathNodeFormatter fmt = AsciiMathNodeFormatter.create();

    // ==================== NodeDouble ====================

    @ParameterizedTest
    @CsvSource({
            "0.0,     0",
            "1.0,     1",
            "-1.0,    -1",
            "3.14,    3.14",
            "100.0,   100"
    })
    void formatsDoubleValues(double value, String expected) {
        assertThat(fmt.format(new NodeDouble(value))).isEqualTo(expected);
    }

    @Test
    void formatsNaN() {
        assertThat(fmt.format(new NodeDouble(Double.NaN))).isEqualTo("\"NaN\"");
    }

    @Test
    void formatsPositiveInfinity() {
        assertThat(fmt.format(new NodeDouble(Double.POSITIVE_INFINITY))).isEqualTo("oo");
    }

    @Test
    void formatsNegativeInfinity() {
        assertThat(fmt.format(new NodeDouble(Double.NEGATIVE_INFINITY))).isEqualTo("-oo");
    }

    @Test
    void formatsDoubleWithDecimalPlaces() {
        var rounded = AsciiMathNodeFormatter.withDecimalPlaces(2);
        assertThat(rounded.format(new NodeDouble(3.14159))).isEqualTo("3.14");
    }

    @Test
    void formatsDoubleWithZeroDecimalPlaces() {
        var rounded = AsciiMathNodeFormatter.withDecimalPlaces(0);
        assertThat(rounded.format(new NodeDouble(3.7))).isEqualTo("4");
    }

    @Test
    void fullPrecisionWithNegativeDecimalPlaces() {
        var full = AsciiMathNodeFormatter.withDecimalPlaces(-1);
        assertThat(full.format(new NodeDouble(3.14159))).isEqualTo("3.14159");
    }

    // ==================== NodeRational ====================

    @Test
    void formatsRationalInteger() {
        assertThat(fmt.format(new NodeRational(5))).isEqualTo("5");
    }

    @Test
    void formatsRationalFractionAsMathFraction() {
        assertThat(fmt.format(new NodeRational(3, 4))).isEqualTo("(3)/(4)");
    }

    @Test
    void formatsRationalNegativeFraction() {
        assertThat(fmt.format(new NodeRational(-1, 3))).isEqualTo("(-1)/(3)");
    }

    // ==================== NodePercent ====================

    @Test
    void formatsPercent() {
        assertThat(fmt.format(new NodePercent(50))).isEqualTo("50%");
    }

    // ==================== NodeBoolean ====================

    @Test
    void formatsTrue() {
        assertThat(fmt.format(NodeBoolean.TRUE)).isEqualTo("\"true\"");
    }

    @Test
    void formatsFalse() {
        assertThat(fmt.format(NodeBoolean.FALSE)).isEqualTo("\"false\"");
    }

    // ==================== NodeString ====================

    @Test
    void formatsString() {
        assertThat(fmt.format(new NodeString("hello"))).isEqualTo("\"hello\"");
    }

    // ==================== NodeVector ====================

    @Test
    void formatsVectorWithRoundBrackets() {
        var vec = new NodeVector(new Node[]{dbl(1), dbl(2), dbl(3)});
        assertThat(fmt.format(vec)).isEqualTo("(1, 2, 3)");
    }

    @Test
    void formatsEmptyVector() {
        var vec = new NodeVector(new Node[]{});
        assertThat(fmt.format(vec)).isEqualTo("()");
    }

    // ==================== NodeMatrix ====================

    @Test
    void formatsMatrixWithStandardNotation() {
        var mat = new NodeMatrix(new Node[][]{
                {dbl(1), dbl(2)},
                {dbl(3), dbl(4)}
        });
        assertThat(fmt.format(mat)).isEqualTo("[[1, 2], [3, 4]]");
    }

    // ==================== NodeUnit ====================

    @Test
    void formatsUnitWithQuotedName() {
        var unitDef = new UnitDefinition("meter", "meters", "length", "meter",
                1.0, 0.0, List.of("m"));
        var unit = NodeUnit.of(5.0, unitDef);
        assertThat(fmt.format(unit)).isEqualTo("5 \"meters\"");
    }

    @Test
    void formatsUnitSingular() {
        var unitDef = new UnitDefinition("meter", "meters", "length", "meter",
                1.0, 0.0, List.of("m"));
        var unit = NodeUnit.of(1.0, unitDef);
        assertThat(fmt.format(unit)).isEqualTo("1 \"meter\"");
    }

    // ==================== NodeRange ====================

    @Test
    void formatsRangeDefaultStep() {
        var range = new NodeRange(new NodeRational(1), new NodeRational(10), null);
        assertThat(fmt.format(range)).isEqualTo("1..10");
    }

    @Test
    void formatsRangeCustomStep() {
        var range = new NodeRange(new NodeRational(0), new NodeRational(100), new NodeRational(5));
        assertThat(fmt.format(range)).isEqualTo("0..100 \"step\" 5");
    }

    // ==================== NodeLambda ====================

    @Test
    void formatsLambdaWithMapsToArrow() {
        var lambda = new NodeLambda(List.of("x"), var("x"));
        assertThat(fmt.format(lambda)).isEqualTo("x |-> x");
    }

    @Test
    void formatsMultiParamLambda() {
        var lambda = new NodeLambda(
                List.of("x", "y"),
                binary(TokenType.PLUS, var("x"), var("y"))
        );
        assertThat(fmt.format(lambda)).isEqualTo("(x, y) |-> x + y");
    }

    // ==================== NodeFunction ====================

    @Test
    void formatsNodeFunctionAsQuotedText() {
        var def = new FunctionDefinition("myFunc", List.of("x"), var("x"), null);
        assertThat(fmt.format(new NodeFunction(def))).isEqualTo("\"myFunc\"");
    }

    // ==================== NodeBinary - Precedence ====================

    @Test
    void formatsSimpleAddition() {
        var node = binary(TokenType.PLUS, dbl(1), dbl(2));
        assertThat(fmt.format(node)).isEqualTo("1 + 2");
    }

    @Test
    void formatsPrecedenceMultiplyInAdd() {
        // 1 + 2 * 3 (no extra parens needed)
        var mult = binary(TokenType.MULTIPLY, dbl(2), dbl(3));
        var add = binary(TokenType.PLUS, dbl(1), mult);
        assertThat(fmt.format(add)).isEqualTo("1 + 2 * 3");
    }

    @Test
    void formatsPrecedenceAddInMultiply() {
        // (1 + 2) * 3 (needs parens around addition)
        var add = binary(TokenType.PLUS, dbl(1), dbl(2));
        var mult = binary(TokenType.MULTIPLY, add, dbl(3));
        assertThat(fmt.format(mult)).isEqualTo("(1 + 2) * 3");
    }

    @Test
    void formatsDivisionAsFraction() {
        var div = binary(TokenType.DIVIDE, var("a"), var("b"));
        assertThat(fmt.format(div)).isEqualTo("a/b");
    }

    @Test
    void formatsFractionWithComplexNumeratorDenominator() {
        var num = binary(TokenType.PLUS, var("a"), dbl(1));
        var den = binary(TokenType.MINUS, var("b"), dbl(2));
        var div = binary(TokenType.DIVIDE, num, den);
        assertThat(fmt.format(div)).isEqualTo("(a + 1)/(b - 2)");
    }

    @Test
    void formatsPowerWithoutSpaces() {
        var pow = binary(TokenType.POWER, var("x"), dbl(2));
        assertThat(fmt.format(pow)).isEqualTo("x^2");
    }

    @Test
    void formatsPowerRightAssociative() {
        // x^y^z = x^(y^z) - no parens needed on right
        var inner = binary(TokenType.POWER, var("y"), var("z"));
        var outer = binary(TokenType.POWER, var("x"), inner);
        assertThat(fmt.format(outer)).isEqualTo("x^y^z");
    }

    @Test
    void formatsPowerLeftAssociativeNeedsParens() {
        // (x^y)^z needs parens
        var inner = binary(TokenType.POWER, var("x"), var("y"));
        var outer = binary(TokenType.POWER, inner, var("z"));
        assertThat(fmt.format(outer)).isEqualTo("(x^y)^z");
    }

    @Test
    void formatsSamePrecSubtractionNeedsRightParens() {
        // a - (b - c) needs parens on right (left-associative)
        var inner = binary(TokenType.MINUS, var("b"), var("c"));
        var outer = binary(TokenType.MINUS, var("a"), inner);
        assertThat(fmt.format(outer)).isEqualTo("a - (b - c)");
    }

    @Test
    void formatsSamePrecSubtractionNoLeftParens() {
        // (a - b) - c doesn't need parens on left (left-associative)
        var inner = binary(TokenType.MINUS, var("a"), var("b"));
        var outer = binary(TokenType.MINUS, inner, var("c"));
        assertThat(fmt.format(outer)).isEqualTo("a - b - c");
    }

    @ParameterizedTest
    @CsvSource({
            "EQ,       =",
            "NEQ,      !=",
            "LT,       <",
            "GT,       >",
            "LTE,      <=",
            "GTE,      >=",
            "AND,      and",
            "OR,       or",
            "MOD,      mod"
    })
    void formatsComparisonAndLogicalOperators(String tokenType, String expectedOp) {
        var node = binary(TokenType.valueOf(tokenType), var("a"), var("b"));
        assertThat(fmt.format(node)).isEqualTo("a " + expectedOp + " b");
    }

    // ==================== NodeUnary ====================

    @Test
    void formatsUnaryMinus() {
        var node = prefixUnary(TokenType.MINUS, "-", dbl(5));
        assertThat(fmt.format(node)).isEqualTo("-5");
    }

    @Test
    void formatsUnaryMinusOfBinaryWrapsParens() {
        var sum = binary(TokenType.PLUS, var("a"), var("b"));
        var neg = prefixUnary(TokenType.MINUS, "-", sum);
        assertThat(fmt.format(neg)).isEqualTo("-(a + b)");
    }

    @Test
    void formatsNotPrefix() {
        var node = prefixUnary(TokenType.NOT, "not", var("x"));
        assertThat(fmt.format(node)).isEqualTo("not x");
    }

    @Test
    void formatsFactorial() {
        var node = postfixUnary(TokenType.FACTORIAL, "!", var("n"));
        assertThat(fmt.format(node)).isEqualTo("n!");
    }

    @Test
    void formatsDoubleFactorial() {
        var node = postfixUnary(TokenType.DOUBLE_FACTORIAL, "!!", dbl(7));
        assertThat(fmt.format(node)).isEqualTo("7!!");
    }

    @Test
    void formatsFactorialOfBinaryWrapsParens() {
        var sum = binary(TokenType.PLUS, var("n"), dbl(1));
        var fact = postfixUnary(TokenType.FACTORIAL, "!", sum);
        assertThat(fmt.format(fact)).isEqualTo("(n + 1)!");
    }

    // ==================== NodeCall - Function Name Mapping ====================

    @Test
    void formatsStandardAsciiMathFunction() {
        var call = new NodeCall(var("sin"), List.of(var("x")));
        assertThat(fmt.format(call)).isEqualTo("sin(x)");
    }

    @Test
    void mapsAsinToArcsin() {
        var call = new NodeCall(var("asin"), List.of(var("x")));
        assertThat(fmt.format(call)).isEqualTo("arcsin(x)");
    }

    @Test
    void mapsAcosToArccos() {
        var call = new NodeCall(var("acos"), List.of(var("x")));
        assertThat(fmt.format(call)).isEqualTo("arccos(x)");
    }

    @Test
    void mapsAtanToArctan() {
        var call = new NodeCall(var("atan"), List.of(var("x")));
        assertThat(fmt.format(call)).isEqualTo("arctan(x)");
    }

    @Test
    void formatsNrootWithRootNotation() {
        var call = new NodeCall(var("nroot"), List.of(dbl(3), var("x")));
        assertThat(fmt.format(call)).isEqualTo("root(3)(x)");
    }

    @Test
    void formatsNonStandardFunctionWithQuotes() {
        var call = new NodeCall(var("cbrt"), List.of(var("x")));
        assertThat(fmt.format(call)).isEqualTo("\"cbrt\"(x)");
    }

    @Test
    void formatsMultiArgNonStandardFunction() {
        var call = new NodeCall(var("clamp"), List.of(var("x"), dbl(0), dbl(1)));
        assertThat(fmt.format(call)).isEqualTo("\"clamp\"(x, 0, 1)");
    }

    @Test
    void formatsLambdaCall() {
        var lambda = new NodeLambda(List.of("x"), binary(TokenType.MULTIPLY, var("x"), dbl(2)));
        var call = new NodeCall(lambda, List.of(dbl(5)));
        assertThat(fmt.format(call)).isEqualTo("(x |-> x * 2)(5)");
    }

    // ==================== NodeVariable ====================

    @Test
    void formatsVariable() {
        assertThat(fmt.format(var("x"))).isEqualTo("x");
    }

    @Test
    void mapsInfinityVariable() {
        assertThat(fmt.format(var("infinity"))).isEqualTo("oo");
        assertThat(fmt.format(var("inf"))).isEqualTo("oo");
    }

    @Test
    void formatsPiVariable() {
        // pi is a standard AsciiMath symbol, so just pass through
        assertThat(fmt.format(var("pi"))).isEqualTo("pi");
    }

    // ==================== NodeAssignment ====================

    @Test
    void formatsAssignmentWithEquals() {
        var assignment = new NodeAssignment("x", dbl(42));
        assertThat(fmt.format(assignment)).isEqualTo("x = 42");
    }

    // ==================== NodeFunctionDef ====================

    @Test
    void formatsFunctionDefWithEquals() {
        var def = new NodeFunctionDef("f", List.of("x"),
                binary(TokenType.POWER, var("x"), dbl(2)));
        assertThat(fmt.format(def)).isEqualTo("f(x) = x^2");
    }

    // ==================== NodeSubscript ====================

    @Test
    void formatsSubscript() {
        var sub = new NodeSubscript(var("v"),
                List.of(new NodeSubscript.SliceArg(dbl(0), null, false)));
        assertThat(fmt.format(sub)).isEqualTo("v[0]");
    }

    @Test
    void formatsSlice() {
        var sub = new NodeSubscript(var("v"),
                List.of(new NodeSubscript.SliceArg(dbl(1), dbl(3))));
        assertThat(fmt.format(sub)).isEqualTo("v[1:3]");
    }

    // ==================== NodeRangeExpression ====================

    @Test
    void formatsRangeExpression() {
        var range = new NodeRangeExpression(dbl(1), dbl(10), null);
        assertThat(fmt.format(range)).isEqualTo("1..10");
    }

    @Test
    void formatsRangeExpressionWithStep() {
        var range = new NodeRangeExpression(dbl(0), dbl(100), dbl(5));
        assertThat(fmt.format(range)).isEqualTo("0..100 \"step\" 5");
    }

    // ==================== NodeUnitConversion ====================

    @Test
    void formatsUnitConversion() {
        var conv = new NodeUnitConversion(dbl(100), "feet");
        assertThat(fmt.format(conv)).isEqualTo("100 \"in\" \"feet\"");
    }

    // ==================== NodeSequence ====================

    @Test
    void formatsSequence() {
        var seq = new NodeSequence(List.of(
                new NodeAssignment("x", dbl(5)),
                binary(TokenType.MULTIPLY, var("x"), dbl(2))
        ));
        assertThat(fmt.format(seq)).isEqualTo("x = 5; x * 2");
    }

    // ==================== NodeComprehension ====================

    @Test
    void formatsComprehensionWithSetBuilderNotation() {
        var comp = new NodeComprehension(
                binary(TokenType.POWER, var("x"), dbl(2)),
                List.of(new NodeComprehension.Iterator("x", new NodeRangeExpression(dbl(1), dbl(10), null))),
                null
        );
        assertThat(fmt.format(comp)).isEqualTo("{x^2 | x in 1..10}");
    }

    @Test
    void formatsComprehensionWithCondition() {
        var comp = new NodeComprehension(
                var("x"),
                List.of(new NodeComprehension.Iterator("x", new NodeRangeExpression(dbl(1), dbl(10), null))),
                binary(TokenType.GT, var("x"), dbl(5))
        );
        assertThat(fmt.format(comp)).isEqualTo("{x | x in 1..10, x > 5}");
    }

    // ==================== Explicit References ====================

    @Test
    void formatsUnitRefAsQuotedText() {
        assertThat(fmt.format(new NodeUnitRef("fahrenheit"))).isEqualTo("\"fahrenheit\"");
    }

    @Test
    void formatsVarRefAsPlainName() {
        assertThat(fmt.format(new NodeVarRef("pi"))).isEqualTo("pi");
    }

    @Test
    void formatsConstRefPi() {
        assertThat(fmt.format(new NodeConstRef("pi"))).isEqualTo("pi");
    }

    @Test
    void formatsConstRefInfinity() {
        assertThat(fmt.format(new NodeConstRef("infinity"))).isEqualTo("oo");
    }

    @Test
    void formatsConstRefPhi() {
        assertThat(fmt.format(new NodeConstRef("phi"))).isEqualTo("phi");
    }

    @Test
    void formatsConstRefEuler() {
        assertThat(fmt.format(new NodeConstRef("e"))).isEqualTo("e");
    }

    @Test
    void formatsUnknownConstRefAsQuotedText() {
        assertThat(fmt.format(new NodeConstRef("tau"))).isEqualTo("tau");
    }

    // ==================== Complex / Deep Nesting ====================

    @Test
    void formatsDeepMathExpression() {
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
                .isEqualTo("sin(x^2 + cos(y * pi))/(1 + abs(z))");
    }

    @Test
    void formatsExpressionWithAllNodeCategories() {
        // f(x) := sqrt(x^2 + 1); f(3) + 50%
        var funcBody = new NodeCall(var("sqrt"), List.of(
                binary(TokenType.PLUS,
                        binary(TokenType.POWER, var("x"), dbl(2)),
                        dbl(1))
        ));
        var funcDef = new NodeFunctionDef("f", List.of("x"), funcBody);
        var funcCall = new NodeCall(var("f"), List.of(dbl(3)));
        var percent = new NodePercent(50);
        var sum = binary(TokenType.PLUS, funcCall, percent);
        var seq = new NodeSequence(List.of(funcDef, sum));

        assertThat(fmt.format(seq))
                .isEqualTo("f(x) = sqrt(x^2 + 1); f(3) + 50%");
    }

    @Test
    void formatsMatrixWithExpressionElements() {
        var mat = new NodeMatrix(new Node[][]{
                {binary(TokenType.PLUS, var("a"), dbl(1)), var("b")},
                {dbl(0), binary(TokenType.MULTIPLY, dbl(2), var("c"))}
        });
        assertThat(fmt.format(mat)).isEqualTo("[[a + 1, b], [0, 2 * c]]");
    }

    @Test
    void formatsNestedFractionsWithParens() {
        // (a/b) / (c/d) - both children are binary, so both get wrapped
        var frac1 = binary(TokenType.DIVIDE, var("a"), var("b"));
        var frac2 = binary(TokenType.DIVIDE, var("c"), var("d"));
        var outer = binary(TokenType.DIVIDE, frac1, frac2);
        assertThat(fmt.format(outer)).isEqualTo("(a/b)/(c/d)");
    }

    @Test
    void formatsSubscriptWithExpressionIndex() {
        // v[i + 1]
        var index = binary(TokenType.PLUS, var("i"), dbl(1));
        var sub = new NodeSubscript(var("v"),
                List.of(new NodeSubscript.SliceArg(index, null, false)));
        assertThat(fmt.format(sub)).isEqualTo("v[i + 1]");
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
        return new NodeBinary(token(type, type.getDisplay()), left, right);
    }

    private static NodeUnary prefixUnary(TokenType type, String lexeme, Node operand) {
        return new NodeUnary(token(type, lexeme), operand, true);
    }

    private static NodeUnary postfixUnary(TokenType type, String lexeme, Node operand) {
        return new NodeUnary(token(type, lexeme), operand, false);
    }
}
