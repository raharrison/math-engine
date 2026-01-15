package uk.co.ryanharrison.mathengine.parser.lexer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.FunctionRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.KeywordRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests all lexical features including number scanning, operators, strings, identifiers,
 * token classification, and implicit multiplication.
 */
class LexerTest {

    private Lexer lexer;

    @BeforeEach
    void setUp() {
        // Create lexer with all default registries from config
        MathEngineConfig config = MathEngineConfig.defaults();
        FunctionRegistry functionRegistry = FunctionRegistry.fromFunctions(config.functions());
        UnitRegistry unitRegistry = new UnitRegistry();
        ConstantRegistry constantRegistry = config.constantRegistry();
        KeywordRegistry keywordRegistry = config.keywordRegistry();
        lexer = new Lexer(functionRegistry, unitRegistry, constantRegistry, keywordRegistry, 256, true);
    }

    // ==================== Integer Literals ====================

    @Test
    void tokenizeZero() {
        List<Token> tokens = lexer.tokenize("0");

        assertThat(tokens).hasSize(2); // INTEGER + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.INTEGER);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo("0");
        assertThat(tokens.getFirst().getLiteral()).isEqualTo(0L);
    }

    @ParameterizedTest
    @CsvSource({
            "42, 42",
            "1000000, 1000000",
            "123, 123"
    })
    void tokenizePositiveIntegers(String input, long expected) {
        List<Token> tokens = lexer.tokenize(input);

        assertThat(tokens).hasSize(2); // INTEGER + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.INTEGER);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(input);
        assertThat(tokens.getFirst().getLiteral()).isEqualTo(expected);
    }

    // ==================== Decimal Literals ====================

    @ParameterizedTest
    @CsvSource({
            "3.14, 3.14",
            "0.5, 0.5",
            "0.0, 0.0",
            "1.0, 1.0",
            "0.001, 0.001",
            "123.456, 123.456"
    })
    void tokenizeDecimalNumbers(String input, double expected) {
        List<Token> tokens = lexer.tokenize(input);

        assertThat(tokens).hasSize(2); // DECIMAL + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.DECIMAL);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(input);
        assertThat((Double) tokens.getFirst().getLiteral()).isCloseTo(expected, within(0.000001));
    }

    @Test
    void tokenizeDecimalStartingWithDot() {
        List<Token> tokens = lexer.tokenize(".5");

        assertThat(tokens).hasSize(2); // DECIMAL + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.DECIMAL);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(".5");
        assertThat((Double) tokens.getFirst().getLiteral()).isCloseTo(0.5, within(0.000001));
    }

    // ==================== Scientific Notation ====================

    @ParameterizedTest
    @CsvSource({
            "1e3, 1000.0",
            "2.5E-2, 0.025",
            "1.5e+3, 1500.0",
            "3.2E2, 320.0"
    })
    void tokenizeScientificNotation(String input, double expected) {
        List<Token> tokens = lexer.tokenize(input);

        assertThat(tokens).hasSize(2); // SCIENTIFIC + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.SCIENTIFIC);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(input);
        assertThat((Double) tokens.getFirst().getLiteral()).isCloseTo(expected, within(0.000001));
    }

    // ==================== Rational Literals ====================

    @ParameterizedTest
    @CsvSource({
            "1/2",
            "22/7",
            "3/4",
            "100/25"
    })
    void tokenizeRationalLiterals(String input) {
        List<Token> tokens = lexer.tokenize(input);

        assertThat(tokens).hasSize(2); // RATIONAL + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.RATIONAL);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(input);
    }

    // ==================== Decimal vs Range Disambiguation ====================

    @Test
    void tokenizeDecimalNotRange() {
        List<Token> tokens = lexer.tokenize("1.5");

        assertThat(tokens).hasSize(2); // DECIMAL + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.DECIMAL);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo("1.5");
    }

    @Test
    void tokenizeRangeNotDecimal() {
        List<Token> tokens = lexer.tokenize("1..5");

        assertThat(tokens).hasSize(4); // INTEGER + RANGE + INTEGER + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.INTEGER);
        assertThat(tokens.get(0).getLexeme()).isEqualTo("1");
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.RANGE);
        assertThat(tokens.get(1).getLexeme()).isEqualTo("..");
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.INTEGER);
        assertThat(tokens.get(2).getLexeme()).isEqualTo("5");
    }

    @Test
    void tokenizeRangeWithDecimals() {
        List<Token> tokens = lexer.tokenize("1.0..10.5");

        assertThat(tokens).hasSize(4); // DECIMAL + RANGE + DECIMAL + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.DECIMAL);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.RANGE);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.DECIMAL);
    }

    // ==================== String Literals ====================

    @Test
    void tokenizeDoubleQuotedString() {
        List<Token> tokens = lexer.tokenize("\"hello world\"");

        assertThat(tokens).hasSize(2); // STRING + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.STRING);
        assertThat(tokens.getFirst().getLiteral()).isEqualTo("hello world");
    }

    @Test
    void tokenizeSingleQuotedString() {
        List<Token> tokens = lexer.tokenize("'hello world'");

        assertThat(tokens).hasSize(2); // STRING + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.STRING);
        assertThat(tokens.getFirst().getLiteral()).isEqualTo("hello world");
    }

    @Test
    void tokenizeStringWithEscapes() {
        List<Token> tokens = lexer.tokenize("\"hello\\nworld\\t!\"");

        assertThat(tokens).hasSize(2); // STRING + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.STRING);
        assertThat(tokens.getFirst().getLiteral()).isEqualTo("hello\nworld\t!");
    }

    @Test
    void tokenizeEmptyString() {
        List<Token> tokens = lexer.tokenize("\"\"");

        assertThat(tokens).hasSize(2); // STRING + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.STRING);
        assertThat(tokens.getFirst().getLiteral()).isEqualTo("");
    }

    @Test
    void unteriminatedStringThrowsException() {
        assertThatThrownBy(() -> lexer.tokenize("\"hello"))
                .isInstanceOf(LexerException.class)
                .hasMessageContaining("Unterminated string");
    }

    // ==================== Identifiers ====================

    @ParameterizedTest
    @CsvSource({
            "x",
            "foo",
            "myVariable",
            "x1",
            "variable_name",
            "ABC123"
    })
    void tokenizeIdentifiers(String input) {
        List<Token> tokens = lexer.tokenize(input);

        assertThat(tokens).hasSize(2); // IDENTIFIER + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(input);
    }

    // ==================== Keywords ====================

    @ParameterizedTest
    @CsvSource({
            "for",
            "in",
            "if",
            "step",
            "true",
            "false",
            "to",
            "as"
    })
    void tokenizeKeywords(String input) {
        List<Token> tokens = lexer.tokenize(input);

        assertThat(tokens).hasSize(2); // KEYWORD + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.KEYWORD);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(input);
    }

    @Test
    void tokenizeKeywordOperators() {
        // Test that keyword operators are converted to their proper token types
        List<Token> tokens;

        tokens = lexer.tokenize("and");
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.AND);

        tokens = lexer.tokenize("or");
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.OR);

        tokens = lexer.tokenize("not");
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.NOT);

        tokens = lexer.tokenize("xor");
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.XOR);

        tokens = lexer.tokenize("of");
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.OF);

        tokens = lexer.tokenize("mod");
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.MOD);
    }

    // ==================== Functions ====================

    @ParameterizedTest
    @CsvSource({
            "sin",
            "cos",
            "tan",
            "abs",
            "sqrt",
            "ln",
            "log",
            "exp",
            "sum",
            "det"
    })
    void tokenizeFunctions(String input) {
        List<Token> tokens = lexer.tokenize(input);

        assertThat(tokens).hasSize(2); // FUNCTION + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.FUNCTION);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(input);
    }

    // ==================== Units ====================

    @ParameterizedTest
    @CsvSource({
            "meter",
            "meters",
            "m",
            "kilometer",
            "km",
            "foot",
            "feet",
            "ft"
    })
    void tokenizeUnits(String input) {
        List<Token> tokens = lexer.tokenize(input);

        assertThat(tokens).hasSize(2); // UNIT + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.UNIT);
        assertThat(tokens.getFirst().getLexeme()).isEqualTo(input);
    }

    // ==================== Operators ====================

    @Test
    void tokenizeArithmeticOperators() {
        List<Token> tokens = lexer.tokenize("+ - * / ^ @");

        assertThat(tokens).hasSize(7); // 6 operators + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.PLUS);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.MINUS);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.MULTIPLY);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.DIVIDE);
        assertThat(tokens.get(4).getType()).isEqualTo(TokenType.POWER);
        assertThat(tokens.get(5).getType()).isEqualTo(TokenType.AT);
    }

    @Test
    void tokenizeComparisonOperators() {
        List<Token> tokens = lexer.tokenize("< > <= >= == !=");

        assertThat(tokens).hasSize(7); // 6 operators + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.LT);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.GT);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.LTE);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.GTE);
        assertThat(tokens.get(4).getType()).isEqualTo(TokenType.EQ);
        assertThat(tokens.get(5).getType()).isEqualTo(TokenType.NEQ);
    }

    @Test
    void tokenizeLogicalOperators() {
        List<Token> tokens = lexer.tokenize("&& ||");

        assertThat(tokens).hasSize(3); // AND + OR + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.AND);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.OR);
    }

    @Test
    void tokenizePostfixOperators() {
        List<Token> tokens = lexer.tokenize("! !! %");

        assertThat(tokens).hasSize(4); // 3 operators + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.FACTORIAL);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.DOUBLE_FACTORIAL);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.PERCENT);
    }

    @Test
    void tokenizeAssignmentAndLambda() {
        List<Token> tokens = lexer.tokenize(":= ->");

        assertThat(tokens).hasSize(3); // ASSIGN + LAMBDA + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.ASSIGN);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.LAMBDA);
    }

    @Test
    void tokenizeRangeOperator() {
        List<Token> tokens = lexer.tokenize("..");

        assertThat(tokens).hasSize(2); // RANGE + EOF
        assertThat(tokens.getFirst().getType()).isEqualTo(TokenType.RANGE);
    }

    // ==================== Structural Tokens ====================

    @Test
    void tokenizeStructuralTokens() {
        List<Token> tokens = lexer.tokenize("( ) { } [ ] , ; :");

        assertThat(tokens).hasSize(10); // 9 tokens + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.LPAREN);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.RPAREN);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.LBRACE);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.RBRACE);
        assertThat(tokens.get(4).getType()).isEqualTo(TokenType.LBRACKET);
        assertThat(tokens.get(5).getType()).isEqualTo(TokenType.RBRACKET);
        assertThat(tokens.get(6).getType()).isEqualTo(TokenType.COMMA);
        assertThat(tokens.get(7).getType()).isEqualTo(TokenType.SEMICOLON);
        assertThat(tokens.get(8).getType()).isEqualTo(TokenType.COLON);
    }

    // ==================== Implicit Multiplication ====================

    @Test
    void implicitMultiplicationNumberIdentifier() {
        List<Token> tokens = lexer.tokenize("2x");

        assertThat(tokens).hasSize(4); // INTEGER + MULTIPLY + IDENTIFIER + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.INTEGER);
        assertThat(tokens.get(0).getLexeme()).isEqualTo("2");
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.MULTIPLY);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(tokens.get(2).getLexeme()).isEqualTo("x");
    }

    @Test
    void implicitMultiplicationNumberParenthesis() {
        List<Token> tokens = lexer.tokenize("2(x+1)");

        assertThat(tokens).hasSize(8); // INTEGER + MULTIPLY + LPAREN + IDENTIFIER + PLUS + INTEGER + RPAREN + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.INTEGER);
        assertThat(tokens.get(0).getLexeme()).isEqualTo("2");
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.MULTIPLY);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.LPAREN);
    }

    @Test
    void implicitMultiplicationParenthesisParenthesis() {
        List<Token> tokens = lexer.tokenize("(a)(b)");

        assertThat(tokens).hasSize(8); // LPAREN + IDENTIFIER + RPAREN + MULTIPLY + LPAREN + IDENTIFIER + RPAREN + EOF
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.RPAREN);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.MULTIPLY);
        assertThat(tokens.get(4).getType()).isEqualTo(TokenType.LPAREN);
    }

    @Test
    void implicitMultiplicationNumberConstant() {
        List<Token> tokens = lexer.tokenize("2pi");

        assertThat(tokens).hasSize(4); // INTEGER + MULTIPLY + IDENTIFIER(pi) + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.INTEGER);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.MULTIPLY);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(tokens.get(2).getLexeme()).isEqualTo("pi");
    }

    @Test
    void implicitMultiplicationNumberUnit() {
        List<Token> tokens = lexer.tokenize("100meters");

        assertThat(tokens).hasSize(4); // INTEGER + MULTIPLY + UNIT + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.INTEGER);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.MULTIPLY);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.UNIT);
    }

    @Test
    void implicitMultiplicationParenthesisNumber() {
        List<Token> tokens = lexer.tokenize("(x)2");

        assertThat(tokens).hasSize(6); // LPAREN + IDENTIFIER + RPAREN + MULTIPLY + INTEGER + EOF
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.RPAREN);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.MULTIPLY);
        assertThat(tokens.get(4).getType()).isEqualTo(TokenType.INTEGER);
    }

    @Test
    void noImplicitMultiplicationForFunctionCall() {
        List<Token> tokens = lexer.tokenize("sin(x)");

        assertThat(tokens).hasSize(5); // FUNCTION + LPAREN + IDENTIFIER + RPAREN + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.FUNCTION);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.LPAREN);
        // No MULTIPLY token between FUNCTION and LPAREN
    }

    // ==================== Complex Expressions ====================

    @Test
    void tokenizeComplexArithmeticExpression() {
        List<Token> tokens = lexer.tokenize("2 + 3 * 4");

        assertThat(tokens).hasSize(6); // INTEGER + PLUS + INTEGER + MULTIPLY + INTEGER + EOF
        assertThat(tokens.get(0).getLexeme()).isEqualTo("2");
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.PLUS);
        assertThat(tokens.get(2).getLexeme()).isEqualTo("3");
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.MULTIPLY);
        assertThat(tokens.get(4).getLexeme()).isEqualTo("4");
    }

    @Test
    void tokenizeAssignmentExpression() {
        List<Token> tokens = lexer.tokenize("x := 5");

        assertThat(tokens).hasSize(4); // IDENTIFIER + ASSIGN + INTEGER + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.ASSIGN);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.INTEGER);
    }

    @Test
    void tokenizeLambdaExpression() {
        List<Token> tokens = lexer.tokenize("x -> x^2");

        assertThat(tokens).hasSize(6); // IDENTIFIER + LAMBDA + IDENTIFIER + POWER + INTEGER + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(tokens.get(1).getType()).isEqualTo(TokenType.LAMBDA);
        assertThat(tokens.get(2).getType()).isEqualTo(TokenType.IDENTIFIER);
        assertThat(tokens.get(3).getType()).isEqualTo(TokenType.POWER);
    }

    @Test
    void tokenizeVectorLiteral() {
        List<Token> tokens = lexer.tokenize("{1, 2, 3}");

        assertThat(tokens).hasSize(8); // LBRACE + INTEGER + COMMA + INTEGER + COMMA + INTEGER + RBRACE + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.LBRACE);
        assertThat(tokens.get(6).getType()).isEqualTo(TokenType.RBRACE);
    }

    @Test
    void tokenizeMatrixLiteral() {
        List<Token> tokens = lexer.tokenize("[1, 2; 3, 4]");

        assertThat(tokens).hasSize(10); // LBRACKET + ... + RBRACKET + EOF
        assertThat(tokens.get(0).getType()).isEqualTo(TokenType.LBRACKET);
        assertThat(tokens.get(4).getType()).isEqualTo(TokenType.SEMICOLON);
        assertThat(tokens.get(8).getType()).isEqualTo(TokenType.RBRACKET);
    }

    // ==================== Whitespace Handling ====================

    @Test
    void ignoreWhitespace() {
        List<Token> tokens = lexer.tokenize("  2  +  3  ");

        assertThat(tokens).hasSize(4); // INTEGER + PLUS + INTEGER + EOF (no whitespace tokens)
    }

    @Test
    void handleMultilineInput() {
        List<Token> tokens = lexer.tokenize("x := 5\ny := 10");

        assertThat(tokens).hasSize(7); // IDENTIFIER + ASSIGN + INTEGER + IDENTIFIER + ASSIGN + INTEGER + EOF (no implicit mult across lines)
        assertThat(tokens.get(0).getLine()).isEqualTo(1);
        assertThat(tokens.get(3).getLine()).isEqualTo(2); // y is on line 2
    }

    // ==================== Error Cases ====================

    @Test
    void unexpectedCharacterThrowsException() {
        assertThatThrownBy(() -> lexer.tokenize("#"))
                .isInstanceOf(LexerException.class)
                .hasMessageContaining("Unexpected character");
    }

    @Test
    void singleEqualsThrowsException() {
        assertThatThrownBy(() -> lexer.tokenize("x = 5"))
                .isInstanceOf(LexerException.class)
                .hasMessageContaining("Unexpected character '='");
    }

    @Test
    void singleAmpersandThrowsException() {
        assertThatThrownBy(() -> lexer.tokenize("x & y"))
                .isInstanceOf(LexerException.class)
                .hasMessageContaining("Unexpected character '&'");
    }

    @Test
    void singlePipeThrowsException() {
        assertThatThrownBy(() -> lexer.tokenize("x | y"))
                .isInstanceOf(LexerException.class)
                .hasMessageContaining("Unexpected character '|'");
    }

    @Test
    void invalidScientificNotationThrowsException() {
        assertThatThrownBy(() -> lexer.tokenize("1e"))
                .isInstanceOf(LexerException.class)
                .hasMessageContaining("Expected digit in scientific notation");
    }

    // ==================== Position Tracking ====================

    @Test
    void trackLineAndColumnNumbers() {
        List<Token> tokens = lexer.tokenize("2 + 3");

        assertThat(tokens.get(0).getLine()).isEqualTo(1);
        assertThat(tokens.get(0).getColumn()).isEqualTo(1);
        assertThat(tokens.get(1).getLine()).isEqualTo(1);
        assertThat(tokens.get(1).getColumn()).isEqualTo(3);
        assertThat(tokens.get(2).getLine()).isEqualTo(1);
        assertThat(tokens.get(2).getColumn()).isEqualTo(5);
    }
}
