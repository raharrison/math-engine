package uk.co.ryanharrison.mathengine.parser.registry;

import java.util.List;

/**
 * Represents a unit of measurement with conversion factors.
 * Units can be converted to and from a base unit of the same type.
 * <p>
 * Supports singular and plural forms for proper grammatical output.
 * </p>
 */
public record UnitDefinition(String singularName, String pluralName, String type, String baseUnit, double multiplier,
                             double offset, List<String> aliases) {

    /**
     * Creates a unit definition with singular and plural names.
     *
     * @param singularName the singular form (e.g., "meter")
     * @param pluralName   the plural form (e.g., "meters")
     * @param type         the unit type/category (e.g., "length")
     * @param baseUnit     the base unit for this type
     * @param multiplier   conversion multiplier to base unit
     * @param offset       conversion offset (for affine units like temperature)
     * @param aliases      additional names/symbols for this unit
     */
    public UnitDefinition(String singularName, String pluralName, String type, String baseUnit,
                          double multiplier, double offset, List<String> aliases) {
        this.singularName = singularName;
        this.pluralName = pluralName;
        this.type = type;
        this.baseUnit = baseUnit;
        this.multiplier = multiplier;
        this.offset = offset;
        this.aliases = List.copyOf(aliases);
    }

    /**
     * Returns the canonical name (singular form).
     */
    public String getName() {
        return singularName;
    }

    /**
     * Returns the singular form of the unit name.
     */
    @Override
    public String singularName() {
        return singularName;
    }

    /**
     * Returns the plural form of the unit name.
     */
    @Override
    public String pluralName() {
        return pluralName;
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
     * Convert a value in this unit to the base unit.
     * <p>
     * For affine transformations (like temperature), this applies:
     * base = (value - offset) * multiplier
     * </p>
     * <p>
     * This is the inverse of fromBase.
     * </p>
     */
    public double toBase(double value) {
        return (value - offset) * multiplier;
    }

    /**
     * Convert a value from the base unit to this unit.
     * <p>
     * For affine transformations (like temperature), this applies:
     * value = (base / multiplier) + offset
     * </p>
     */
    public double fromBase(double baseValue) {
        return (baseValue / multiplier) + offset;
    }

    @Override
    public String toString() {
        return singularName;
    }
}
