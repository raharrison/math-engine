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
     *     <li>Keyword operators: and, or, xor, not, mod, of</li>
     * </ul>
     *
     * @return registry with standard keywords
     */
    public static KeywordRegistry withDefaults() {
        return builder()
                // Reserved keywords
                .keyword("for")
                .keyword("in")
                .keyword("if")
                .keyword("step")
                .keyword("to")
                .keyword("as")
                .keyword("true")
                .keyword("false")
                // Keyword operators
                .keywordOperator("and", TokenType.AND)
                .keywordOperator("or", TokenType.OR)
                .keywordOperator("xor", TokenType.XOR)
                .keywordOperator("not", TokenType.NOT)
                .keywordOperator("mod", TokenType.MOD)
                .keywordOperator("of", TokenType.OF)
                .build();
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
     * Checks if a word is a keyword operator.
     *
     * @param word the word to check (case-insensitive)
     * @return true if the word is a keyword operator
     */
    public boolean isKeywordOperator(String word) {
        return keywordOperators.containsKey(word.toLowerCase());
    }

    /**
     * Checks if a word is any type of keyword (reserved or operator).
     *
     * @param word the word to check (case-insensitive)
     * @return true if the word is any type of keyword
     */
    public boolean isAnyKeyword(String word) {
        String lower = word.toLowerCase();
        return reservedKeywords.contains(lower) || keywordOperators.containsKey(lower);
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

    /**
     * Gets all reserved keywords.
     *
     * @return unmodifiable set of reserved keywords (lowercase)
     */
    public Set<String> getReservedKeywords() {
        return reservedKeywords;
    }

    /**
     * Gets all keyword operators.
     *
     * @return unmodifiable map of keyword operators to token types
     */
    public Map<String, TokenType> getKeywordOperators() {
        return keywordOperators;
    }

    /**
     * Gets all keywords (reserved + operators).
     *
     * @return unmodifiable set of all keywords (lowercase)
     */
    public Set<String> getAllKeywords() {
        var all = new HashSet<>(reservedKeywords);
        all.addAll(keywordOperators.keySet());
        return Set.copyOf(all);
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
