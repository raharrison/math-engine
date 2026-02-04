package uk.co.ryanharrison.mathengine.parser.registry;

import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;

import java.util.*;

/**
 * Registry for keywords and keyword operators used by the lexer.
 * <p>
 * Keywords are reserved words that have special meaning in the grammar:
 * <ul>
 *     <li><b>Reserved keywords:</b> for, in, if, step, to, as - used in control flow and comprehensions</li>
 *     <li><b>Keyword operators:</b> and, or, xor, not, mod, of - parsed as operators</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Use standard keywords
 * KeywordRegistry registry = KeywordRegistry.withDefaults();
 *
 * // Check if a word is a keyword
 * boolean isKeyword = registry.isKeyword("for"); // true
 *
 * // Check if it's a keyword operator
 * Optional<TokenType> op = registry.getKeywordOperatorType("and"); // Optional[AND]
 *
 * // Custom registry
 * KeywordRegistry custom = KeywordRegistry.builder()
 *     .keyword("for")
 *     .keyword("in")
 *     .keywordOperator("and", TokenType.AND)
 *     .build();
 * }</pre>
 */
public final class KeywordRegistry {

    private final Set<String> reservedKeywords;
    private final Map<String, TokenType> keywordOperators;

    private KeywordRegistry(Set<String> reservedKeywords, Map<String, TokenType> keywordOperators) {
        this.reservedKeywords = Set.copyOf(reservedKeywords);
        this.keywordOperators = Map.copyOf(keywordOperators);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an empty keyword registry.
     *
     * @return new empty registry
     */
    public static KeywordRegistry empty() {
        return new KeywordRegistry(Set.of(), Map.of());
    }

    /**
     * Creates a registry with the standard keywords.
     * <p>
     * Includes:
     * <ul>
     *     <li>Reserved keywords: for, in, if, step, to, as, true, false</li>
     *     <li>Keyword operators: automatically populated from SymbolRegistry</li>
     * </ul>
     *
     * @return registry with standard keywords
     */
    public static KeywordRegistry withDefaults() {
        Builder builder = builder();

        SymbolRegistry symbolRegistry = SymbolRegistry.getDefault();
        for (TokenType type : TokenType.values()) {
            symbolRegistry.getMetadata(type).ifPresent(meta -> {
                if (meta.isKeyword()) {
                    builder.keywordOperator(meta.getInputSymbol(), type);
                }
            });
        }

        // Reserved keywords (non-operators)
        builder.keywords("for", "in", "if", "step", "to", "as", "true", "false");

        return builder.build();
    }

    /**
     * Creates a new builder for constructing keyword registries.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Query Methods ====================

    /**
     * Checks if a word is a reserved keyword (not an operator).
     *
     * @param word the word to check (case-insensitive)
     * @return true if the word is a reserved keyword
     */
    public boolean isKeyword(String word) {
        return reservedKeywords.contains(word.toLowerCase());
    }

    /**
     * Gets the token type for a keyword operator.
     *
     * @param word the word to check (case-insensitive)
     * @return the token type, or empty if not a keyword operator
     */
    public Optional<TokenType> getKeywordOperatorType(String word) {
        return Optional.ofNullable(keywordOperators.get(word.toLowerCase()));
    }

    // ==================== Builder ====================

    /**
     * Builder for constructing {@link KeywordRegistry} instances.
     */
    public static final class Builder {
        private final Set<String> reservedKeywords = new HashSet<>();
        private final Map<String, TokenType> keywordOperators = new HashMap<>();

        private Builder() {
        }

        /**
         * Adds a reserved keyword.
         *
         * @param keyword the keyword to add
         * @return this builder
         */
        public Builder keyword(String keyword) {
            if (keyword == null || keyword.isBlank()) {
                throw new IllegalArgumentException("Keyword cannot be null or blank");
            }
            reservedKeywords.add(keyword.toLowerCase());
            return this;
        }

        /**
         * Adds multiple reserved keywords.
         *
         * @param keywords the keywords to add
         * @return this builder
         */
        public Builder keywords(String... keywords) {
            for (String kw : keywords) {
                keyword(kw);
            }
            return this;
        }

        /**
         * Adds a keyword operator.
         *
         * @param keyword   the keyword
         * @param tokenType the token type to map to
         * @return this builder
         */
        public Builder keywordOperator(String keyword, TokenType tokenType) {
            if (keyword == null || keyword.isBlank()) {
                throw new IllegalArgumentException("Keyword cannot be null or blank");
            }
            if (tokenType == null) {
                throw new IllegalArgumentException("Token type cannot be null");
            }
            keywordOperators.put(keyword.toLowerCase(), tokenType);
            return this;
        }

        /**
         * Builds the keyword registry.
         *
         * @return new keyword registry
         */
        public KeywordRegistry build() {
            return new KeywordRegistry(reservedKeywords, keywordOperators);
        }
    }
}
