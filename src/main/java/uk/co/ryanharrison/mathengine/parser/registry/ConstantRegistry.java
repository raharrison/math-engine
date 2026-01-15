package uk.co.ryanharrison.mathengine.parser.registry;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;

import java.util.*;

/**
 * Registry for mathematical constants used by the engine.
 * <p>
 * The registry provides a centralized place to define constants that will be:
 * <ul>
 *     <li>Recognized by the lexer for identifier splitting</li>
 *     <li>Defined in the evaluation context as predefined variables</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Use standard constants
 * ConstantRegistry registry = ConstantRegistry.withDefaults();
 *
 * // Create custom registry
 * ConstantRegistry registry = ConstantRegistry.builder()
 *     .add(ConstantDefinition.of("pi", new NodeDouble(Math.PI)))
 *     .add(ConstantDefinition.of("e", new NodeDouble(Math.E), "euler"))
 *     .build();
 *
 * // Check if a name is a constant
 * boolean isPi = registry.isConstant("pi"); // true
 *
 * // Get the value
 * Optional<NodeConstant> value = registry.getValue("pi");
 * }</pre>
 */
public final class ConstantRegistry {

    private final List<ConstantDefinition> definitions;
    private final Map<String, ConstantDefinition> nameToDefinition;

    private ConstantRegistry(List<ConstantDefinition> definitions) {
        this.definitions = List.copyOf(definitions);
        this.nameToDefinition = new HashMap<>();

        for (ConstantDefinition def : definitions) {
            for (String name : def.allNames()) {
                nameToDefinition.put(name.toLowerCase(), def);
            }
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an empty constant registry.
     *
     * @return new empty registry
     */
    public static ConstantRegistry empty() {
        return new ConstantRegistry(List.of());
    }

    /**
     * Creates a registry with the standard mathematical constants.
     * <p>
     * Includes:
     * <ul>
     *     <li>Mathematical: pi, e/euler, goldenratio</li>
     *     <li>Boolean: true, false</li>
     *     <li>Special numeric: infinity, nan</li>
     *     <li>Numeric words: zero through ten, hundred, thousand, million, billion</li>
     * </ul>
     *
     * @return registry with standard constants
     */
    public static ConstantRegistry withDefaults() {
        return builder()
                .addAll(mathematicalConstants())
                .addAll(booleanConstants())
                .addAll(specialNumericConstants())
                .addAll(numericWordConstants())
                .build();
    }

    /**
     * Creates a registry with only mathematical constants (pi, e, goldenratio).
     *
     * @return registry with mathematical constants only
     */
    public static ConstantRegistry mathematicalOnly() {
        return builder()
                .addAll(mathematicalConstants())
                .build();
    }

    /**
     * Creates a new builder for constructing constant registries.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Standard Constant Sets ====================

    /**
     * Gets the standard mathematical constants.
     *
     * @return list of mathematical constants (pi, e, goldenratio)
     */
    public static List<ConstantDefinition> mathematicalConstants() {
        return List.of(
                ConstantDefinition.builder()
                        .name("pi")
                        .value(new NodeDouble(Math.PI))
                        .description("The ratio of a circle's circumference to its diameter")
                        .build(),
                ConstantDefinition.builder()
                        .name("tau")
                        .value(new NodeDouble(Math.PI * 2))
                        .description("The ratio of a circle's circumference to its radius")
                        .build(),
                ConstantDefinition.builder()
                        .name("e")
                        .aliases("euler")
                        .value(new NodeDouble(Math.E))
                        .description("Euler's number, the base of natural logarithms")
                        .build(),
                ConstantDefinition.builder()
                        .name("goldenratio")
                        .aliases("phi")
                        .value(new NodeDouble(1.618033988749895))
                        .description("The golden ratio")
                        .build()
        );
    }

    /**
     * Gets the boolean constants.
     *
     * @return list of boolean constants (true, false)
     */
    public static List<ConstantDefinition> booleanConstants() {
        return List.of(
                ConstantDefinition.builder()
                        .name("true")
                        .value(NodeBoolean.TRUE)
                        .description("Boolean true value")
                        .build(),
                ConstantDefinition.builder()
                        .name("false")
                        .value(NodeBoolean.FALSE)
                        .description("Boolean false value")
                        .build()
        );
    }

    /**
     * Gets the special numeric constants.
     *
     * @return list of special numeric constants (infinity, nan)
     */
    public static List<ConstantDefinition> specialNumericConstants() {
        return List.of(
                ConstantDefinition.builder()
                        .name("infinity")
                        .aliases("inf")
                        .value(new NodeDouble(Double.POSITIVE_INFINITY))
                        .description("Positive infinity")
                        .build(),
                ConstantDefinition.builder()
                        .name("nan")
                        .value(new NodeDouble(Double.NaN))
                        .description("Not a number")
                        .build()
        );
    }

    /**
     * Gets the numeric word constants.
     *
     * @return list of numeric word constants (zero through billion)
     */
    public static List<ConstantDefinition> numericWordConstants() {
        return List.of(
                ConstantDefinition.of("zero", new NodeRational(0)),
                ConstantDefinition.of("one", new NodeRational(1)),
                ConstantDefinition.of("two", new NodeRational(2)),
                ConstantDefinition.of("three", new NodeRational(3)),
                ConstantDefinition.of("four", new NodeRational(4)),
                ConstantDefinition.of("five", new NodeRational(5)),
                ConstantDefinition.of("six", new NodeRational(6)),
                ConstantDefinition.of("seven", new NodeRational(7)),
                ConstantDefinition.of("eight", new NodeRational(8)),
                ConstantDefinition.of("nine", new NodeRational(9)),
                ConstantDefinition.of("ten", new NodeRational(10)),
                ConstantDefinition.of("hundred", new NodeRational(100)),
                ConstantDefinition.of("thousand", new NodeRational(1000)),
                ConstantDefinition.of("million", new NodeRational(1000000)),
                ConstantDefinition.of("billion", new NodeRational(1000000000L))
        );
    }

    // ==================== Query Methods ====================

    /**
     * Checks if a name is a registered constant.
     *
     * @param name the name to check (case-insensitive)
     * @return true if the name is a constant
     */
    public boolean isConstant(String name) {
        return nameToDefinition.containsKey(name.toLowerCase());
    }

    /**
     * Gets the value of a constant by name.
     *
     * @param name the constant name (case-insensitive)
     * @return the constant value, or empty if not found
     */
    public Optional<NodeConstant> getValue(String name) {
        ConstantDefinition def = nameToDefinition.get(name.toLowerCase());
        return def != null ? Optional.of(def.value()) : Optional.empty();
    }

    /**
     * Gets a constant definition by name.
     *
     * @param name the constant name (case-insensitive)
     * @return the constant definition, or empty if not found
     */
    public Optional<ConstantDefinition> getDefinition(String name) {
        return Optional.ofNullable(nameToDefinition.get(name.toLowerCase()));
    }

    /**
     * Gets all constant definitions.
     *
     * @return unmodifiable list of all definitions
     */
    public List<ConstantDefinition> getDefinitions() {
        return definitions;
    }

    /**
     * Gets all constant names (including aliases).
     *
     * @return unmodifiable set of all constant names (lowercase)
     */
    public Set<String> getConstantNames() {
        return Set.copyOf(nameToDefinition.keySet());
    }

    /**
     * Gets the number of constant definitions.
     *
     * @return the number of definitions
     */
    public int size() {
        return definitions.size();
    }

    /**
     * Checks if the registry is empty.
     *
     * @return true if no constants are registered
     */
    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    // ==================== Builder ====================

    /**
     * Builder for constructing {@link ConstantRegistry} instances.
     */
    public static final class Builder {
        private final List<ConstantDefinition> definitions = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds a constant definition.
         *
         * @param definition the constant definition
         * @return this builder
         */
        public Builder add(ConstantDefinition definition) {
            if (definition == null) {
                throw new IllegalArgumentException("Constant definition cannot be null");
            }
            definitions.add(definition);
            return this;
        }

        /**
         * Adds multiple constant definitions.
         *
         * @param defs the constant definitions
         * @return this builder
         */
        public Builder addAll(Collection<ConstantDefinition> defs) {
            for (ConstantDefinition def : defs) {
                add(def);
            }
            return this;
        }

        /**
         * Adds a simple constant with no aliases.
         *
         * @param name  the constant name
         * @param value the constant value
         * @return this builder
         */
        public Builder add(String name, NodeConstant value) {
            return add(ConstantDefinition.of(name, value));
        }

        /**
         * Adds a constant with aliases.
         *
         * @param name    the primary constant name
         * @param value   the constant value
         * @param aliases the aliases
         * @return this builder
         */
        public Builder add(String name, NodeConstant value, String... aliases) {
            return add(ConstantDefinition.of(name, value, aliases));
        }

        /**
         * Builds the constant registry.
         *
         * @return new constant registry
         */
        public ConstantRegistry build() {
            return new ConstantRegistry(definitions);
        }
    }
}
