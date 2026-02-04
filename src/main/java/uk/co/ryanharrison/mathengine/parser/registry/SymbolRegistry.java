package uk.co.ryanharrison.mathengine.parser.registry;

import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Single source of truth for all symbol metadata in the parser.
 * <p>
 * This registry centralizes information about operators, their symbols, formats,
 * precedence, and classification. It eliminates duplication across TokenType,
 * Operators, TokenScanner, and NodeFormatters.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Use the default registry
 * SymbolRegistry registry = SymbolRegistry.getDefault();
 *
 * // Look up by input symbol
 * Optional<TokenType> type = registry.findByInputSymbol("+");  // PLUS
 *
 * // Look up by keyword
 * Optional<TokenType> type = registry.findByKeyword("mod");    // MOD
 *
 * // Get formatting info
 * String display = registry.getStringFormat(TokenType.PLUS);   // "+"
 * String ascii = registry.getAsciiMathFormat(TokenType.EQ);    // "="
 *
 * // Get precedence
 * int prec = registry.getPrecedence(TokenType.MULTIPLY);       // 9
 * }</pre>
 */
public final class SymbolRegistry {

    private static final SymbolRegistry DEFAULT_INSTANCE = withDefaults();

    private final Map<TokenType, SymbolMetadata> metadataMap;
    private final Map<String, TokenType> inputSymbolMap;
    private final Map<String, TokenType> keywordMap;

    private SymbolRegistry(Map<TokenType, SymbolMetadata> metadataMap) {
        this.metadataMap = Map.copyOf(metadataMap);

        // Build reverse lookup maps
        Map<String, TokenType> inputMap = new HashMap<>();
        Map<String, TokenType> kwMap = new HashMap<>();

        for (Map.Entry<TokenType, SymbolMetadata> entry : metadataMap.entrySet()) {
            SymbolMetadata meta = entry.getValue();
            if (meta.inputSymbol != null && !meta.inputSymbol.isEmpty()) {
                inputMap.put(meta.inputSymbol, entry.getKey());
            }
            if (meta.isKeyword && meta.inputSymbol != null) {
                kwMap.put(meta.inputSymbol.toLowerCase(), entry.getKey());
            }
        }

        this.inputSymbolMap = Map.copyOf(inputMap);
        this.keywordMap = Map.copyOf(kwMap);
    }

    // ==================== Factory Methods ====================

    /**
     * Gets the default singleton instance with standard symbols.
     *
     * @return the default registry
     */
    public static SymbolRegistry getDefault() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Creates a registry with all default symbols populated.
     *
     * @return new registry with standard symbols
     */
    public static SymbolRegistry withDefaults() {
        Map<TokenType, SymbolMetadata> metadata = new HashMap<>();

        // Binary operators
        metadata.put(TokenType.PLUS, SymbolMetadata.builder()
                .tokenType(TokenType.PLUS)
                .inputSymbol("+")
                .displayName("addition")
                .stringFormat("+")
                .asciiMathFormat("+")
                .precedence(8)
                .isBinaryOperator(true)
                .isUnaryOperator(true)
                .unaryPosition(UnaryOperator.Position.PREFIX)
                .build());

        metadata.put(TokenType.MINUS, SymbolMetadata.builder()
                .tokenType(TokenType.MINUS)
                .inputSymbol("-")
                .displayName("subtraction")
                .stringFormat("-")
                .asciiMathFormat("-")
                .precedence(8)
                .isBinaryOperator(true)
                .isUnaryOperator(true)
                .unaryPosition(UnaryOperator.Position.PREFIX)
                .build());

        metadata.put(TokenType.MULTIPLY, SymbolMetadata.builder()
                .tokenType(TokenType.MULTIPLY)
                .inputSymbol("*")
                .displayName("multiplication")
                .stringFormat("*")
                .asciiMathFormat("*")
                .precedence(9)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.DIVIDE, SymbolMetadata.builder()
                .tokenType(TokenType.DIVIDE)
                .inputSymbol("/")
                .displayName("division")
                .stringFormat("/")
                .asciiMathFormat("/")
                .precedence(9)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.POWER, SymbolMetadata.builder()
                .tokenType(TokenType.POWER)
                .inputSymbol("^")
                .displayName("exponentiation")
                .stringFormat("^")
                .asciiMathFormat("^")
                .precedence(10)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.MOD, SymbolMetadata.builder()
                .tokenType(TokenType.MOD)
                .inputSymbol("mod")
                .displayName("modulo")
                .stringFormat("%")
                .asciiMathFormat("mod")
                .precedence(9)
                .isKeyword(true)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.EQ, SymbolMetadata.builder()
                .tokenType(TokenType.EQ)
                .inputSymbol("==")
                .displayName("equality")
                .stringFormat("==")
                .asciiMathFormat("=")
                .precedence(5)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.NEQ, SymbolMetadata.builder()
                .tokenType(TokenType.NEQ)
                .inputSymbol("!=")
                .displayName("inequality")
                .stringFormat("!=")
                .asciiMathFormat("!=")
                .precedence(5)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.LT, SymbolMetadata.builder()
                .tokenType(TokenType.LT)
                .inputSymbol("<")
                .displayName("less than")
                .stringFormat("<")
                .asciiMathFormat("<")
                .precedence(6)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.GT, SymbolMetadata.builder()
                .tokenType(TokenType.GT)
                .inputSymbol(">")
                .displayName("greater than")
                .stringFormat(">")
                .asciiMathFormat(">")
                .precedence(6)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.LTE, SymbolMetadata.builder()
                .tokenType(TokenType.LTE)
                .inputSymbol("<=")
                .displayName("less than or equal")
                .stringFormat("<=")
                .asciiMathFormat("<=")
                .precedence(6)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.GTE, SymbolMetadata.builder()
                .tokenType(TokenType.GTE)
                .inputSymbol(">=")
                .displayName("greater than or equal")
                .stringFormat(">=")
                .asciiMathFormat(">=")
                .precedence(6)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.AND, SymbolMetadata.builder()
                .tokenType(TokenType.AND)
                .inputSymbol("and")
                .displayName("logical AND")
                .stringFormat("and")
                .asciiMathFormat("and")
                .precedence(4)
                .isKeyword(true)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.OR, SymbolMetadata.builder()
                .tokenType(TokenType.OR)
                .inputSymbol("or")
                .displayName("logical OR")
                .stringFormat("or")
                .asciiMathFormat("or")
                .precedence(2)
                .isKeyword(true)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.XOR, SymbolMetadata.builder()
                .tokenType(TokenType.XOR)
                .inputSymbol("xor")
                .displayName("logical XOR")
                .stringFormat("xor")
                .asciiMathFormat("\"xor\"")
                .precedence(3)
                .isKeyword(true)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.OF, SymbolMetadata.builder()
                .tokenType(TokenType.OF)
                .inputSymbol("of")
                .displayName("of operator")
                .stringFormat("of")
                .asciiMathFormat("\"of\"")
                .precedence(9)
                .isKeyword(true)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.AT, SymbolMetadata.builder()
                .tokenType(TokenType.AT)
                .inputSymbol("@")
                .displayName("at operator")
                .stringFormat("@")
                .asciiMathFormat("@")
                .precedence(9)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.ASSIGN, SymbolMetadata.builder()
                .tokenType(TokenType.ASSIGN)
                .inputSymbol(":=")
                .displayName("assignment")
                .stringFormat(":=")
                .asciiMathFormat("=")
                .precedence(1)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.LAMBDA, SymbolMetadata.builder()
                .tokenType(TokenType.LAMBDA)
                .inputSymbol("->")
                .displayName("lambda")
                .stringFormat("->")
                .asciiMathFormat("->")
                .precedence(1)
                .isBinaryOperator(true)
                .build());

        metadata.put(TokenType.RANGE, SymbolMetadata.builder()
                .tokenType(TokenType.RANGE)
                .inputSymbol("..")
                .displayName("range")
                .stringFormat("..")
                .asciiMathFormat("..")
                .precedence(7)
                .isBinaryOperator(true)
                .build());

        // Unary operators
        metadata.put(TokenType.NOT, SymbolMetadata.builder()
                .tokenType(TokenType.NOT)
                .inputSymbol("not")
                .displayName("logical NOT")
                .stringFormat("not ")
                .asciiMathFormat("not ")
                .isKeyword(true)
                .isUnaryOperator(true)
                .unaryPosition(UnaryOperator.Position.PREFIX)
                .build());

        metadata.put(TokenType.FACTORIAL, SymbolMetadata.builder()
                .tokenType(TokenType.FACTORIAL)
                .inputSymbol("!")
                .displayName("factorial")
                .stringFormat("!")
                .asciiMathFormat("!")
                .isUnaryOperator(true)
                .unaryPosition(UnaryOperator.Position.POSTFIX)
                .build());

        metadata.put(TokenType.DOUBLE_FACTORIAL, SymbolMetadata.builder()
                .tokenType(TokenType.DOUBLE_FACTORIAL)
                .inputSymbol("!!")
                .displayName("double factorial")
                .stringFormat("!!")
                .asciiMathFormat("!!")
                .isUnaryOperator(true)
                .unaryPosition(UnaryOperator.Position.POSTFIX)
                .build());

        metadata.put(TokenType.PERCENT, SymbolMetadata.builder()
                .tokenType(TokenType.PERCENT)
                .inputSymbol("%")
                .displayName("percent")
                .stringFormat("%")
                .asciiMathFormat("%")
                .isUnaryOperator(true)
                .unaryPosition(UnaryOperator.Position.POSTFIX)
                .build());

        return new SymbolRegistry(metadata);
    }

    // ==================== Query Methods ====================

    /**
     * Gets the metadata for a given token type.
     *
     * @param type the token type
     * @return the metadata, or empty if not found
     */
    public Optional<SymbolMetadata> getMetadata(TokenType type) {
        return Optional.ofNullable(metadataMap.get(type));
    }

    /**
     * Finds a token type by its input symbol (what users type).
     *
     * @param symbol the input symbol (e.g., "+", "mod", "==")
     * @return the token type, or empty if not found
     */
    public Optional<TokenType> findByInputSymbol(String symbol) {
        return Optional.ofNullable(inputSymbolMap.get(symbol));
    }

    /**
     * Finds a token type by its keyword (case-insensitive).
     *
     * @param keyword the keyword (e.g., "and", "or", "not")
     * @return the token type, or empty if not a keyword operator
     */
    public Optional<TokenType> findByKeyword(String keyword) {
        return Optional.ofNullable(keywordMap.get(keyword.toLowerCase()));
    }

    /**
     * Gets the precedence for a token type.
     *
     * @param type the token type
     * @return the precedence (higher number = lower precedence), or 0 if not an operator
     */
    public int getPrecedence(TokenType type) {
        return getMetadata(type)
                .map(m -> m.precedence)
                .orElse(0);
    }

    /**
     * Gets the string format for a token type (used by StringNodeFormatter).
     *
     * @param type the token type
     * @return the string format, or the token name in lowercase if not found
     */
    public String getStringFormat(TokenType type) {
        return getMetadata(type)
                .map(m -> m.stringFormat)
                .orElse(type.name().toLowerCase());
    }

    /**
     * Gets the AsciiMath format for a token type (used by AsciiMathNodeFormatter).
     *
     * @param type the token type
     * @return the AsciiMath format, or the token name in lowercase if not found
     */
    public String getAsciiMathFormat(TokenType type) {
        return getMetadata(type)
                .map(m -> m.asciiMathFormat)
                .orElse(type.name().toLowerCase());
    }

    /**
     * Checks if a token type is a keyword operator.
     *
     * @param type the token type
     * @return true if the token is a keyword operator (e.g., "and", "mod")
     */
    public boolean isKeyword(TokenType type) {
        return getMetadata(type)
                .map(m -> m.isKeyword)
                .orElse(false);
    }

    /**
     * Checks if a token type is a binary operator.
     *
     * @param type the token type
     * @return true if the token is a binary operator
     */
    public boolean isBinaryOperator(TokenType type) {
        return getMetadata(type)
                .map(m -> m.isBinaryOperator)
                .orElse(false);
    }

    /**
     * Checks if a token type is a unary operator.
     *
     * @param type the token type
     * @return true if the token is a unary operator
     */
    public boolean isUnaryOperator(TokenType type) {
        return getMetadata(type)
                .map(m -> m.isUnaryOperator)
                .orElse(false);
    }

    /**
     * Gets the position of a unary operator.
     *
     * @param type the token type
     * @return the position (PREFIX or POSTFIX), or empty if not a unary operator
     */
    public Optional<UnaryOperator.Position> getUnaryPosition(TokenType type) {
        return getMetadata(type)
                .flatMap(m -> Optional.ofNullable(m.unaryPosition));
    }

    // ==================== Symbol Metadata ====================

    /**
     * Metadata for a symbol/token type.
     * <p>
     * Contains all information about how a symbol is represented, parsed, and formatted.
     * </p>
     */
    public static final class SymbolMetadata {
        private final TokenType tokenType;
        private final String inputSymbol;
        private final String displayName;
        private final String stringFormat;
        private final String asciiMathFormat;
        private final int precedence;
        private final boolean isKeyword;
        private final boolean isBinaryOperator;
        private final boolean isUnaryOperator;
        private final UnaryOperator.Position unaryPosition;

        private SymbolMetadata(Builder builder) {
            this.tokenType = builder.tokenType;
            this.inputSymbol = builder.inputSymbol;
            this.displayName = builder.displayName;
            this.stringFormat = builder.stringFormat;
            this.asciiMathFormat = builder.asciiMathFormat;
            this.precedence = builder.precedence;
            this.isKeyword = builder.isKeyword;
            this.isBinaryOperator = builder.isBinaryOperator;
            this.isUnaryOperator = builder.isUnaryOperator;
            this.unaryPosition = builder.unaryPosition;
        }

        public TokenType getTokenType() {
            return tokenType;
        }

        public String getInputSymbol() {
            return inputSymbol;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getStringFormat() {
            return stringFormat;
        }

        public String getAsciiMathFormat() {
            return asciiMathFormat;
        }

        public int getPrecedence() {
            return precedence;
        }

        public boolean isKeyword() {
            return isKeyword;
        }

        public boolean isBinaryOperator() {
            return isBinaryOperator;
        }

        public boolean isUnaryOperator() {
            return isUnaryOperator;
        }

        public Optional<UnaryOperator.Position> getUnaryPosition() {
            return Optional.ofNullable(unaryPosition);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private TokenType tokenType;
            private String inputSymbol;
            private String displayName;
            private String stringFormat;
            private String asciiMathFormat;
            private int precedence = 0;
            private boolean isKeyword = false;
            private boolean isBinaryOperator = false;
            private boolean isUnaryOperator = false;
            private UnaryOperator.Position unaryPosition;

            public Builder tokenType(TokenType tokenType) {
                this.tokenType = tokenType;
                return this;
            }

            public Builder inputSymbol(String inputSymbol) {
                this.inputSymbol = inputSymbol;
                return this;
            }

            public Builder displayName(String displayName) {
                this.displayName = displayName;
                return this;
            }

            public Builder stringFormat(String stringFormat) {
                this.stringFormat = stringFormat;
                return this;
            }

            public Builder asciiMathFormat(String asciiMathFormat) {
                this.asciiMathFormat = asciiMathFormat;
                return this;
            }

            public Builder precedence(int precedence) {
                this.precedence = precedence;
                return this;
            }

            public Builder isKeyword(boolean isKeyword) {
                this.isKeyword = isKeyword;
                return this;
            }

            public Builder isBinaryOperator(boolean isBinaryOperator) {
                this.isBinaryOperator = isBinaryOperator;
                return this;
            }

            public Builder isUnaryOperator(boolean isUnaryOperator) {
                this.isUnaryOperator = isUnaryOperator;
                return this;
            }

            public Builder unaryPosition(UnaryOperator.Position unaryPosition) {
                this.unaryPosition = unaryPosition;
                return this;
            }

            public SymbolMetadata build() {
                if (tokenType == null) {
                    throw new IllegalArgumentException("tokenType is required");
                }
                return new SymbolMetadata(this);
            }
        }
    }
}
