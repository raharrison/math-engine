package uk.co.ryanharrison.mathengine.parser.registry;

import uk.co.ryanharrison.mathengine.core.BigRational;

import java.util.List;

/**
 * A unit of measurement and the affine rule that converts it to its base unit:
 * {@code base = (value - offset) * multiplier}.
 * <p>
 * Supports singular and plural forms for proper grammatical output.
 */
public record UnitDefinition(String singularName, String pluralName, String type, String baseUnit,
                             Factor multiplier, Factor offset, List<String> aliases) {

    /**
     * @param singularName the singular form (e.g., "meter")
     * @param pluralName   the plural form (e.g., "meters")
     * @param type         the unit type/category (e.g., "length")
     * @param baseUnit     the base unit for this type
     * @param multiplier   conversion multiplier to the base unit
     * @param offset       conversion offset, for affine units like temperature
     * @param aliases      additional names/symbols for this unit
     */
    public UnitDefinition {
        aliases = List.copyOf(aliases);
    }

    /**
     * A unit that is a plain multiple of its base.
     */
    public static UnitDefinition of(String singularName, String pluralName, String type,
                                    String baseUnit, Factor multiplier, List<String> aliases) {
        return new UnitDefinition(singularName, pluralName, type, baseUnit,
                multiplier, Factor.ZERO, aliases);
    }

    /**
     * Returns the canonical name (singular form).
     */
    public String getName() {
        return singularName;
    }

    /**
     * Returns the appropriate display name based on the value.
     * Uses singular form for exactly 1.0, plural otherwise.
     *
     * @param value the numeric value
     * @return singular name if value is 1.0, plural name otherwise
     */
    public String getDisplayName(double value) {
        return value == 1.0 ? singularName : pluralName;
    }

    /**
     * Whether both factors are exact, and so whether a conversion can be.
     */
    public boolean isExact() {
        return multiplier.exact() && offset.exact();
    }

    /**
     * {@code (value - offset) * multiplier}
     */
    public BigRational toBase(BigRational value) {
        return value.subtract(offset.value()).multiply(multiplier.value());
    }

    /**
     * The inverse of {@link #toBase}.
     */
    public BigRational fromBase(BigRational baseValue) {
        return baseValue.divide(multiplier.value()).add(offset.value());
    }

    /** As {@link #toBase(BigRational)}, rounding once at the end. */
    public double toBase(double value) {
        return toBase(BigRational.of(value)).doubleValue();
    }

    /** As {@link #fromBase(BigRational)}, rounding once at the end. */
    public double fromBase(double baseValue) {
        return fromBase(BigRational.of(baseValue)).doubleValue();
    }

    @Override
    public String toString() {
        return singularName;
    }
}
