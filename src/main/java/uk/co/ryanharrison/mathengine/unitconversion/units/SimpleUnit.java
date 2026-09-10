package uk.co.ryanharrison.mathengine.unitconversion.units;

import uk.co.ryanharrison.mathengine.core.BigRational;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An immutable unit with a linear conversion factor.
 * <p>
 * Simple units convert to other units through multiplication and division
 * of conversion factors. For example, converting meters to feet:
 * </p>
 * <pre>
 * result = amount * (meterFactor / footFactor)
 * </pre>
 * <p>
 * This is the most common unit type, suitable for length, mass, volume,
 * and other units with linear relationships.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * SimpleUnit meter = SimpleUnit.builder()
 *     .singular("meter")
 *     .plural("meters")
 *     .alias("m")
 *     .conversionFactor(1.0)  // Base unit
 *     .build();
 *
 * SimpleUnit foot = SimpleUnit.builder()
 *     .singular("foot")
 *     .plural("feet")
 *     .alias("ft")
 *     .conversionFactor(0.3048)  // 1 foot = 0.3048 meters
 *     .build();
 * }</pre>
 *
 * @see ComplexUnit
 * @see SimpleUnitGroup
 */
public final class SimpleUnit extends BaseUnit {
    private final BigRational conversionFactor;

    private SimpleUnit(String singular, String plural, List<String> aliases, BigRational conversionFactor) {
        super(singular, plural, aliases);
        Objects.requireNonNull(conversionFactor, "Conversion factor cannot be null");
        this.conversionFactor = conversionFactor;
    }

    /**
     * Returns the conversion factor for this unit.
     * <p>
     * The conversion factor represents the unit's value relative to the base unit
     * of its group. For example, if meters are the base (factor = 1.0), then
     * feet would have factor ≈ 0.3048.
     * </p>
     *
     * @return the conversion factor
     */
    public BigRational getConversionFactor() {
        return conversionFactor;
    }

    /**
     * Creates a copy of this unit with a different conversion factor.
     * <p>
     * This is useful for dynamic units like currencies where rates change.
     * </p>
     *
     * @param newFactor the new conversion factor
     * @return a new unit with the updated factor
     */
    public SimpleUnit withConversionFactor(BigRational newFactor) {
        return new SimpleUnit(getSingular(), getPlural(), new ArrayList<>(getAllAliases()), newFactor);
    }

    /**
     * Creates a copy of this unit with a different conversion factor.
     *
     * @param newFactor the new conversion factor
     * @return a new unit with the updated factor
     */
    public SimpleUnit withConversionFactor(double newFactor) {
        return withConversionFactor(BigRational.ofDecimal(newFactor));
    }

    /**
     * Creates a new builder for constructing simple units.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleUnit that)) return false;
        if (!super.equals(o)) return false;
        return conversionFactor.equals(that.conversionFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), conversionFactor);
    }

    /**
     * Builder for constructing {@link SimpleUnit} instances.
     */
    public static final class Builder {
        private String singular;
        private String plural;
        private final List<String> aliases = new ArrayList<>();
        private BigRational conversionFactor = BigRational.ONE;

        private Builder() {
        }

        /**
         * Sets the singular form of the unit.
         *
         * @param singular the singular form (e.g., "meter")
         * @return this builder
         */
        public Builder singular(String singular) {
            this.singular = singular;
            return this;
        }

        /**
         * Sets the plural form of the unit.
         *
         * @param plural the plural form (e.g., "meters")
         * @return this builder
         */
        public Builder plural(String plural) {
            this.plural = plural;
            return this;
        }

        /**
         * Adds an alias for this unit.
         *
         * @param alias the alias to add (e.g., "m")
         * @return this builder
         */
        public Builder alias(String alias) {
            if (alias != null && !alias.isEmpty()) {
                this.aliases.add(alias.toLowerCase());
            }
            return this;
        }

        /**
         * Sets the conversion factor for this unit.
         *
         * @param conversionFactor the conversion factor
         * @return this builder
         */
        public Builder conversionFactor(BigRational conversionFactor) {
            this.conversionFactor = conversionFactor;
            return this;
        }

        /**
         * Sets the conversion factor for this unit.
         *
         * @param conversionFactor the conversion factor
         * @return this builder
         */
        public Builder conversionFactor(double conversionFactor) {
            return conversionFactor(BigRational.ofDecimal(conversionFactor));
        }

        /**
         * Builds the simple unit.
         *
         * @return a new simple unit
         * @throws IllegalArgumentException if singular form is not set
         */
        public SimpleUnit build() {
            if (singular == null || singular.isEmpty()) {
                throw new IllegalArgumentException("Singular form is required");
            }
            if (plural == null || plural.isEmpty()) {
                plural = singular;
            }
            return new SimpleUnit(singular, plural, aliases, conversionFactor);
        }
    }
}
