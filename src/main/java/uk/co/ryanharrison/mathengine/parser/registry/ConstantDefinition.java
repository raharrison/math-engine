package uk.co.ryanharrison.mathengine.parser.registry;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;
import java.util.Objects;

/**
 * Immutable definition of a mathematical constant.
 * <p>
 * Constants have a primary name, optional aliases, and a value. They are used
 * by the lexer to recognize constant names and by the evaluator to provide
 * predefined values.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Simple constant
 * ConstantDefinition pi = ConstantDefinition.of("pi", new NodeDouble(Math.PI));
 *
 * // Constant with aliases
 * ConstantDefinition e = ConstantDefinition.of("e", new NodeDouble(Math.E), "euler");
 *
 * // Using builder for more control
 * ConstantDefinition phi = ConstantDefinition.builder()
 *     .name("goldenratio")
 *     .aliases("phi", "φ")
 *     .value(new NodeDouble(1.618033988749895))
 *     .description("The golden ratio")
 *     .build();
 * }</pre>
 */
public final class ConstantDefinition {

    private final String name;
    private final List<String> aliases;
    private final NodeConstant value;
    private final String description;

    private ConstantDefinition(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Constant name cannot be null");
        this.aliases = builder.aliases == null ? List.of() : List.copyOf(builder.aliases);
        this.value = Objects.requireNonNull(builder.value, "Constant value cannot be null");
        this.description = builder.description != null ? builder.description : name + " constant";
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a constant definition with no aliases.
     *
     * @param name  the constant name
     * @param value the constant value
     * @return new constant definition
     */
    public static ConstantDefinition of(String name, NodeConstant value) {
        return builder().name(name).value(value).build();
    }

    /**
     * Creates a constant definition with aliases.
     *
     * @param name    the primary constant name
     * @param value   the constant value
     * @param aliases optional aliases for the constant
     * @return new constant definition
     */
    public static ConstantDefinition of(String name, NodeConstant value, String... aliases) {
        return builder().name(name).value(value).aliases(aliases).build();
    }

    /**
     * Creates a new builder for constructing constant definitions.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Accessors ====================

    /**
     * Gets the primary name of the constant.
     *
     * @return the constant name
     */
    public String name() {
        return name;
    }

    /**
     * Gets the aliases for this constant.
     *
     * @return unmodifiable list of aliases (may be empty)
     */
    public List<String> aliases() {
        return aliases;
    }

    /**
     * Gets all names for this constant (primary name + aliases).
     *
     * @return unmodifiable list of all names
     */
    public List<String> allNames() {
        if (aliases.isEmpty()) {
            return List.of(name);
        }
        var names = new java.util.ArrayList<String>(aliases.size() + 1);
        names.add(name);
        names.addAll(aliases);
        return List.copyOf(names);
    }

    /**
     * Gets the value of the constant.
     *
     * @return the constant value
     */
    public NodeConstant value() {
        return value;
    }

    /**
     * Gets the description of the constant.
     *
     * @return the constant description
     */
    public String description() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstantDefinition that)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ConstantDefinition{name='" + name + "', value=" + value + "}";
    }

    // ==================== Builder ====================

    /**
     * Builder for constructing {@link ConstantDefinition} instances.
     */
    public static final class Builder {
        private String name;
        private List<String> aliases;
        private NodeConstant value;
        private String description;

        private Builder() {
        }

        /**
         * Sets the primary name of the constant.
         *
         * @param name the constant name
         * @return this builder
         */
        public Builder name(String name) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Constant name cannot be null or blank");
            }
            this.name = name;
            return this;
        }

        /**
         * Sets the aliases for the constant.
         *
         * @param aliases the constant aliases
         * @return this builder
         */
        public Builder aliases(String... aliases) {
            this.aliases = List.of(aliases);
            return this;
        }

        /**
         * Sets the aliases for the constant.
         *
         * @param aliases the constant aliases
         * @return this builder
         */
        public Builder aliases(List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        /**
         * Sets the value of the constant.
         *
         * @param value the constant value
         * @return this builder
         */
        public Builder value(NodeConstant value) {
            if (value == null) {
                throw new IllegalArgumentException("Constant value cannot be null");
            }
            this.value = value;
            return this;
        }

        /**
         * Sets the description of the constant.
         *
         * @param description the constant description
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Builds the constant definition.
         *
         * @return new constant definition
         */
        public ConstantDefinition build() {
            return new ConstantDefinition(this);
        }
    }
}
