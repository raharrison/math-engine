package uk.co.ryanharrison.mathengine.unitconversion.units;

import java.util.*;

/**
 * An immutable unit with formula-based conversions to other units.
 * <p>
 * Complex units use mathematical equations for conversions instead of simple
 * multiplication factors. This is necessary for units with non-linear relationships,
 * such as temperature scales (Celsius, Fahrenheit, Kelvin).
 * </p>
 * <p>
 * Each complex unit stores conversion equations to target units. The equations
 * are evaluated using the expression syntax, with the source value bound to
 * a variable (typically "x").
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ComplexUnit celsius = ComplexUnit.builder()
 *     .singular("celsius")
 *     .plural("celsius")
 *     .alias("c")
 *     .variable("c")
 *     .conversionEquation("fahrenheit", "c * 9/5 + 32")
 *     .conversionEquation("kelvin", "c + 273.15")
 *     .build();
 *
 * ComplexUnit fahrenheit = ComplexUnit.builder()
 *     .singular("fahrenheit")
 *     .plural("fahrenheit")
 *     .alias("f")
 *     .variable("f")
 *     .conversionEquation("celsius", "(f - 32) * 5/9")
 *     .conversionEquation("kelvin", "(f - 32) * 5/9 + 273.15")
 *     .build();
 * }</pre>
 *
 * @see SimpleUnit
 * @see ComplexUnitGroup
 */
public final class ComplexUnit extends BaseUnit {
    private final Map<String, String> conversionEquations;
    private final String variable;

    private ComplexUnit(String singular, String plural, List<String> aliases,
                        Map<String, String> conversionEquations, String variable) {
        super(singular, plural, aliases);
        Objects.requireNonNull(conversionEquations, "Conversion equations cannot be null");
        Objects.requireNonNull(variable, "Variable cannot be null");
        this.conversionEquations = Map.copyOf(conversionEquations);
        this.variable = variable;
    }

    /**
     * Returns the conversion equation to convert to the specified target unit.
     * <p>
     * The equation is looked up by matching against all aliases of the target unit.
     * If no equation is found, an empty Optional is returned.
     * </p>
     *
     * @param targetUnit the unit to convert to
     * @return optional containing the conversion equation, or empty if no equation exists
     */
    public Optional<String> getConversionEquationFor(ComplexUnit targetUnit) {
        for (String alias : targetUnit.getAllAliases()) {
            String equation = conversionEquations.get(alias.toLowerCase());
            if (equation != null) {
                return Optional.of(equation);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the variable name used in conversion equations.
     * <p>
     * This variable represents the source value in equations. For example,
     * if variable is "c" and the equation is "c * 9/5 + 32", then "c" will
     * be replaced with the actual Celsius value during conversion.
     * </p>
     *
     * @return the variable name (e.g., "x", "c", "f")
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Creates a new builder for constructing complex units.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplexUnit that)) return false;
        if (!super.equals(o)) return false;
        return conversionEquations.equals(that.conversionEquations)
                && variable.equals(that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), conversionEquations, variable);
    }

    /**
     * Builder for constructing {@link ComplexUnit} instances.
     */
    public static final class Builder {
        private String singular;
        private String plural;
        private final List<String> aliases = new ArrayList<>();
        private final Map<String, String> conversionEquations = new HashMap<>();
        private String variable = "x";

        private Builder() {
        }

        /**
         * Sets the singular form of the unit.
         *
         * @param singular the singular form (e.g., "celsius")
         * @return this builder
         */
        public Builder singular(String singular) {
            this.singular = singular;
            return this;
        }

        /**
         * Sets the plural form of the unit.
         *
         * @param plural the plural form (e.g., "celsius")
         * @return this builder
         */
        public Builder plural(String plural) {
            this.plural = plural;
            return this;
        }

        /**
         * Adds an alias for this unit.
         *
         * @param alias the alias to add (e.g., "c")
         * @return this builder
         */
        public Builder alias(String alias) {
            if (alias != null && !alias.isEmpty()) {
                this.aliases.add(alias.toLowerCase());
            }
            return this;
        }

        /**
         * Adds a conversion equation to a target unit.
         * <p>
         * The equation should use the variable specified by {@link #variable(String)}.
         * For example: "c * 9/5 + 32" converts Celsius to Fahrenheit where "c" is the variable.
         * </p>
         *
         * @param targetUnit the name of the target unit
         * @param equation   the conversion equation
         * @return this builder
         */
        public Builder conversionEquation(String targetUnit, String equation) {
            if (targetUnit != null && !targetUnit.isEmpty() && equation != null && !equation.isEmpty()) {
                this.conversionEquations.put(targetUnit.toLowerCase(), equation);
            }
            return this;
        }

        /**
         * Sets the variable name used in conversion equations.
         * <p>
         * Defaults to "x" if not specified.
         * </p>
         *
         * @param variable the variable name (e.g., "c", "f", "k")
         * @return this builder
         */
        public Builder variable(String variable) {
            if (variable != null && !variable.isEmpty()) {
                this.variable = variable;
            }
            return this;
        }

        /**
         * Builds the complex unit.
         *
         * @return a new complex unit
         * @throws IllegalArgumentException if singular form is not set
         */
        public ComplexUnit build() {
            if (singular == null || singular.isEmpty()) {
                throw new IllegalArgumentException("Singular form is required");
            }
            if (plural == null || plural.isEmpty()) {
                plural = singular;
            }
            return new ComplexUnit(singular, plural, aliases, conversionEquations, variable);
        }
    }
}
