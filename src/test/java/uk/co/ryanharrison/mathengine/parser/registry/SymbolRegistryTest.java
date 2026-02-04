package uk.co.ryanharrison.mathengine.parser.registry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SymbolRegistry}.
 */
class SymbolRegistryTest {

    private final SymbolRegistry registry = SymbolRegistry.getDefault();

    // ==================== Singleton ====================

    @Test
    void getDefaultReturnsSameInstance() {
        SymbolRegistry instance1 = SymbolRegistry.getDefault();
        SymbolRegistry instance2 = SymbolRegistry.getDefault();
        assertThat(instance1).isSameAs(instance2);
    }

    // ==================== Input Symbol Lookup ====================

    @ParameterizedTest
    @CsvSource({
            "+, PLUS",
            "-, MINUS",
            "*, MULTIPLY",
            "/, DIVIDE",
            "^, POWER",
            "mod, MOD",
            "==, EQ",
            "!=, NEQ",
            "<, LT",
            ">, GT",
            "<=, LTE",
            ">=, GTE",
            "and, AND",
            "or, OR",
            "xor, XOR",
            "of, OF",
            "@, AT",
            ":=, ASSIGN",
            "->, LAMBDA",
            ".., RANGE",
            "!, FACTORIAL",
            "!!, DOUBLE_FACTORIAL",
            "%, PERCENT",
            "not, NOT"
    })
    void findByInputSymbolReturnsCorrectTokenType(String symbol, TokenType expectedType) {
        Optional<TokenType> result = registry.findByInputSymbol(symbol);
        assertThat(result).hasValue(expectedType);
    }

    @Test
    void findByInputSymbolReturnsEmptyForUnknownSymbol() {
        Optional<TokenType> result = registry.findByInputSymbol("unknown");
        assertThat(result).isEmpty();
    }

    // ==================== Keyword Lookup ====================

    @ParameterizedTest
    @CsvSource({
            "and, AND",
            "or, OR",
            "xor, XOR",
            "not, NOT",
            "mod, MOD",
            "of, OF"
    })
    void findByKeywordReturnsCorrectTokenType(String keyword, TokenType expectedType) {
        Optional<TokenType> result = registry.findByKeyword(keyword);
        assertThat(result).hasValue(expectedType);
    }

    @Test
    void findByKeywordIsCaseInsensitive() {
        assertThat(registry.findByKeyword("AND")).hasValue(TokenType.AND);
        assertThat(registry.findByKeyword("And")).hasValue(TokenType.AND);
        assertThat(registry.findByKeyword("and")).hasValue(TokenType.AND);
    }

    @Test
    void findByKeywordReturnsEmptyForNonKeyword() {
        assertThat(registry.findByKeyword("+")).isEmpty();
        assertThat(registry.findByKeyword("*")).isEmpty();
    }

    // ==================== String Format ====================

    @ParameterizedTest
    @CsvSource({
            "PLUS, '+'",
            "MINUS, '-'",
            "MULTIPLY, '*'",
            "DIVIDE, '/'",
            "POWER, '^'",
            "MOD, '%'",
            "EQ, '=='",
            "NEQ, '!='",
            "LT, '<'",
            "GT, '>'",
            "LTE, '<='",
            "GTE, '>='",
            "AND, 'and'",
            "OR, 'or'",
            "XOR, 'xor'",
            "OF, 'of'",
            "AT, '@'",
            "ASSIGN, ':='",
            "LAMBDA, '->'",
            "RANGE, '..'",
            "FACTORIAL, '!'",
            "DOUBLE_FACTORIAL, '!!'",
            "PERCENT, '%'",
            "NOT, 'not '"
    })
    void getStringFormatReturnsCorrectFormat(TokenType type, String expectedFormat) {
        String format = registry.getStringFormat(type);
        assertThat(format).isEqualTo(expectedFormat);
    }

    // ==================== AsciiMath Format ====================

    @ParameterizedTest
    @CsvSource({
            "PLUS, '+'",
            "MINUS, '-'",
            "MULTIPLY, '*'",
            "DIVIDE, '/'",
            "POWER, '^'",
            "MOD, 'mod'",
            "EQ, '='",
            "NEQ, '!='",
            "LT, '<'",
            "GT, '>'",
            "LTE, '<='",
            "GTE, '>='",
            "AND, 'and'",
            "OR, 'or'",
            "XOR, '\"xor\"'",
            "OF, '\"of\"'",
            "AT, '@'",
            "ASSIGN, '='",
            "LAMBDA, '->'",
            "RANGE, '..'",
            "FACTORIAL, '!'",
            "DOUBLE_FACTORIAL, '!!'",
            "PERCENT, '%'",
            "NOT, 'not '"
    })
    void getAsciiMathFormatReturnsCorrectFormat(TokenType type, String expectedFormat) {
        String format = registry.getAsciiMathFormat(type);
        assertThat(format).isEqualTo(expectedFormat);
    }

    // ==================== Precedence ====================

    @ParameterizedTest
    @CsvSource({
            "ASSIGN, 1",
            "LAMBDA, 1",
            "OR, 2",
            "XOR, 3",
            "AND, 4",
            "EQ, 5",
            "NEQ, 5",
            "LT, 6",
            "GT, 6",
            "LTE, 6",
            "GTE, 6",
            "RANGE, 7",
            "PLUS, 8",
            "MINUS, 8",
            "MULTIPLY, 9",
            "DIVIDE, 9",
            "MOD, 9",
            "OF, 9",
            "AT, 9",
            "POWER, 10"
    })
    void getPrecedenceReturnsCorrectValue(TokenType type, int expectedPrecedence) {
        int precedence = registry.getPrecedence(type);
        assertThat(precedence).isEqualTo(expectedPrecedence);
    }

    @Test
    void getPrecedenceReturnsZeroForNonOperator() {
        assertThat(registry.getPrecedence(TokenType.LPAREN)).isEqualTo(0);
        assertThat(registry.getPrecedence(TokenType.INTEGER)).isEqualTo(0);
        assertThat(registry.getPrecedence(TokenType.IDENTIFIER)).isEqualTo(0);
    }

    @Test
    void unaryOperatorsHaveNoPrecedence() {
        assertThat(registry.getPrecedence(TokenType.FACTORIAL)).isEqualTo(0);
        assertThat(registry.getPrecedence(TokenType.PERCENT)).isEqualTo(0);
        assertThat(registry.getPrecedence(TokenType.NOT)).isEqualTo(0);
    }

    // ==================== Operator Classification ====================

    @Test
    void isBinaryOperatorIdentifiesCorrectly() {
        // Binary operators
        assertThat(registry.isBinaryOperator(TokenType.PLUS)).isTrue();
        assertThat(registry.isBinaryOperator(TokenType.MULTIPLY)).isTrue();
        assertThat(registry.isBinaryOperator(TokenType.POWER)).isTrue();
        assertThat(registry.isBinaryOperator(TokenType.AND)).isTrue();

        // Not binary operators
        assertThat(registry.isBinaryOperator(TokenType.FACTORIAL)).isFalse();
        assertThat(registry.isBinaryOperator(TokenType.LPAREN)).isFalse();
        assertThat(registry.isBinaryOperator(TokenType.INTEGER)).isFalse();
    }

    @Test
    void isUnaryOperatorIdentifiesCorrectly() {
        // Unary operators
        assertThat(registry.isUnaryOperator(TokenType.MINUS)).isTrue();
        assertThat(registry.isUnaryOperator(TokenType.PLUS)).isTrue();
        assertThat(registry.isUnaryOperator(TokenType.NOT)).isTrue();
        assertThat(registry.isUnaryOperator(TokenType.FACTORIAL)).isTrue();
        assertThat(registry.isUnaryOperator(TokenType.DOUBLE_FACTORIAL)).isTrue();
        assertThat(registry.isUnaryOperator(TokenType.PERCENT)).isTrue();

        // Not unary operators
        assertThat(registry.isUnaryOperator(TokenType.MULTIPLY)).isFalse();
        assertThat(registry.isUnaryOperator(TokenType.DIVIDE)).isFalse();
        assertThat(registry.isUnaryOperator(TokenType.LPAREN)).isFalse();
    }

    @Test
    void plusAndMinusAreBothBinaryAndUnary() {
        assertThat(registry.isBinaryOperator(TokenType.PLUS)).isTrue();
        assertThat(registry.isUnaryOperator(TokenType.PLUS)).isTrue();

        assertThat(registry.isBinaryOperator(TokenType.MINUS)).isTrue();
        assertThat(registry.isUnaryOperator(TokenType.MINUS)).isTrue();
    }

    // ==================== Unary Position ====================

    @Test
    void getUnaryPositionReturnsCorrectPosition() {
        assertThat(registry.getUnaryPosition(TokenType.MINUS))
                .hasValue(UnaryOperator.Position.PREFIX);
        assertThat(registry.getUnaryPosition(TokenType.PLUS))
                .hasValue(UnaryOperator.Position.PREFIX);
        assertThat(registry.getUnaryPosition(TokenType.NOT))
                .hasValue(UnaryOperator.Position.PREFIX);

        assertThat(registry.getUnaryPosition(TokenType.FACTORIAL))
                .hasValue(UnaryOperator.Position.POSTFIX);
        assertThat(registry.getUnaryPosition(TokenType.DOUBLE_FACTORIAL))
                .hasValue(UnaryOperator.Position.POSTFIX);
        assertThat(registry.getUnaryPosition(TokenType.PERCENT))
                .hasValue(UnaryOperator.Position.POSTFIX);
    }

    @Test
    void getUnaryPositionReturnsEmptyForNonUnaryOperator() {
        assertThat(registry.getUnaryPosition(TokenType.MULTIPLY)).isEmpty();
        assertThat(registry.getUnaryPosition(TokenType.DIVIDE)).isEmpty();
        assertThat(registry.getUnaryPosition(TokenType.LPAREN)).isEmpty();
    }

    // ==================== Keyword Identification ====================

    @Test
    void isKeywordIdentifiesKeywordOperators() {
        assertThat(registry.isKeyword(TokenType.AND)).isTrue();
        assertThat(registry.isKeyword(TokenType.OR)).isTrue();
        assertThat(registry.isKeyword(TokenType.XOR)).isTrue();
        assertThat(registry.isKeyword(TokenType.NOT)).isTrue();
        assertThat(registry.isKeyword(TokenType.MOD)).isTrue();
        assertThat(registry.isKeyword(TokenType.OF)).isTrue();
    }

    @Test
    void isKeywordReturnsFalseForSymbolOperators() {
        assertThat(registry.isKeyword(TokenType.PLUS)).isFalse();
        assertThat(registry.isKeyword(TokenType.MULTIPLY)).isFalse();
        assertThat(registry.isKeyword(TokenType.POWER)).isFalse();
    }

    // ==================== Metadata Retrieval ====================

    @Test
    void getMetadataReturnsCompleteMetadataForOperators() {
        Optional<SymbolRegistry.SymbolMetadata> meta = registry.getMetadata(TokenType.PLUS);
        assertThat(meta).isPresent();

        SymbolRegistry.SymbolMetadata m = meta.get();
        assertThat(m.getTokenType()).isEqualTo(TokenType.PLUS);
        assertThat(m.getInputSymbol()).isEqualTo("+");
        assertThat(m.getDisplayName()).isEqualTo("addition");
        assertThat(m.getStringFormat()).isEqualTo("+");
        assertThat(m.getAsciiMathFormat()).isEqualTo("+");
        assertThat(m.getPrecedence()).isEqualTo(8);
        assertThat(m.isKeyword()).isFalse();
        assertThat(m.isBinaryOperator()).isTrue();
        assertThat(m.isUnaryOperator()).isTrue();
        assertThat(m.getUnaryPosition()).hasValue(UnaryOperator.Position.PREFIX);
    }

    @Test
    void getMetadataReturnsEmptyForNonOperatorTokens() {
        assertThat(registry.getMetadata(TokenType.LPAREN)).isEmpty();
        assertThat(registry.getMetadata(TokenType.INTEGER)).isEmpty();
        assertThat(registry.getMetadata(TokenType.IDENTIFIER)).isEmpty();
    }

    // ==================== Coverage ====================

    @Test
    void allOperatorTokensHaveMetadata() {
        // Binary operators
        assertThat(registry.getMetadata(TokenType.PLUS)).isPresent();
        assertThat(registry.getMetadata(TokenType.MINUS)).isPresent();
        assertThat(registry.getMetadata(TokenType.MULTIPLY)).isPresent();
        assertThat(registry.getMetadata(TokenType.DIVIDE)).isPresent();
        assertThat(registry.getMetadata(TokenType.POWER)).isPresent();
        assertThat(registry.getMetadata(TokenType.MOD)).isPresent();
        assertThat(registry.getMetadata(TokenType.EQ)).isPresent();
        assertThat(registry.getMetadata(TokenType.NEQ)).isPresent();
        assertThat(registry.getMetadata(TokenType.LT)).isPresent();
        assertThat(registry.getMetadata(TokenType.GT)).isPresent();
        assertThat(registry.getMetadata(TokenType.LTE)).isPresent();
        assertThat(registry.getMetadata(TokenType.GTE)).isPresent();
        assertThat(registry.getMetadata(TokenType.AND)).isPresent();
        assertThat(registry.getMetadata(TokenType.OR)).isPresent();
        assertThat(registry.getMetadata(TokenType.XOR)).isPresent();
        assertThat(registry.getMetadata(TokenType.OF)).isPresent();
        assertThat(registry.getMetadata(TokenType.AT)).isPresent();
        assertThat(registry.getMetadata(TokenType.ASSIGN)).isPresent();
        assertThat(registry.getMetadata(TokenType.LAMBDA)).isPresent();
        assertThat(registry.getMetadata(TokenType.RANGE)).isPresent();

        // Unary operators
        assertThat(registry.getMetadata(TokenType.NOT)).isPresent();
        assertThat(registry.getMetadata(TokenType.FACTORIAL)).isPresent();
        assertThat(registry.getMetadata(TokenType.DOUBLE_FACTORIAL)).isPresent();
        assertThat(registry.getMetadata(TokenType.PERCENT)).isPresent();
    }
}
