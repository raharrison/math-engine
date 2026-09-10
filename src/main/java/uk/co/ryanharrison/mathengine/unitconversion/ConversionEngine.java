package uk.co.ryanharrison.mathengine.unitconversion;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.unitconversion.units.Unit;
import uk.co.ryanharrison.mathengine.unitconversion.units.UnitGroup;
import uk.co.ryanharrison.mathengine.unitconversion.units.UnitGroupLoader;
import uk.co.ryanharrison.mathengine.utils.MathUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Central facade for unit conversion operations across multiple unit groups.
 * <p>
 * The ConversionEngine manages a collection of {@link UnitGroup}s and provides
 * a unified interface for converting between units. Conversions can be performed
 * programmatically or by parsing natural language expressions.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Load default unit groups from classpath
 * ConversionEngine engine = ConversionEngine.loadDefaults();
 *
 * // Convert programmatically
 * ConversionResult result = engine.convert(100, "meters", "feet");
 * System.out.println(result.result()); // BigRational result
 *
 * // Convert from string expression
 * ConversionResult result2 = engine.convertFromString("100 meters to feet");
 *
 * // Convert to formatted string
 * String formatted = engine.convertToFormattedString(100, "meters", "feet", 2);
 * // "100.0 meters = 328.08 feet"
 *
 * // Build custom engine
 * ConversionEngine custom = ConversionEngine.builder()
 *     .addGroup(SimpleUnitGroup.of("length", units))
 *     .build();
 * }</pre>
 *
 * @see UnitGroup
 * @see ConversionResult
 */
public final class ConversionEngine {
    /**
     * Unit group name for currency conversions
     */
    private static final String CURRENCY_GROUP_NAME = "currency";

    /**
     * Unit group name for timezone conversions
     */
    private static final String TIMEZONE_GROUP_NAME = "time zones";

    /**
     * Regex pattern for parsing conversion expressions.
     * Matches formats like: "100 meters to feet", "5.5 kg in pounds"
     */
    private static final Pattern CONVERSION_PATTERN = Pattern.compile(
            "(-?\\d*\\.?\\d*)\\s*(.+?)\\s+(?:in|to|as)\\s+(.+)",
            Pattern.CASE_INSENSITIVE
    );

    private final List<UnitGroup<?>> unitGroups;
    private final Set<String> allAliases;

    private ConversionEngine(List<UnitGroup<?>> unitGroups) {
        Objects.requireNonNull(unitGroups, "Unit groups cannot be null");
        if (unitGroups.isEmpty()) {
            throw new IllegalArgumentException("At least one unit group is required");
        }
        this.unitGroups = List.copyOf(unitGroups);
        this.allAliases = computeAllAliases();
    }

    /**
     * Creates a conversion engine with default unit groups loaded from classpath.
     * <p>
     * Default groups are loaded from the {@code units.xml} resource file.
     * </p>
     *
     * @return a new conversion engine with default unit groups
     * @see UnitGroupLoader#loadFromClasspath()
     */
    public static ConversionEngine loadDefaults() {
        List<UnitGroup<?>> groups = UnitGroupLoader.loadFromClasspath();
        return new ConversionEngine(groups);
    }

    /**
     * Creates a conversion engine with the specified unit groups.
     *
     * @param unitGroups the unit groups to include
     * @return a new conversion engine
     * @throws NullPointerException     if unitGroups is null
     * @throws IllegalArgumentException if unitGroups is empty
     */
    public static ConversionEngine of(List<UnitGroup<?>> unitGroups) {
        return new ConversionEngine(unitGroups);
    }

    /**
     * Creates a new builder for constructing a conversion engine.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Converts an amount from one unit to another.
     *
     * @param amount   the amount to convert
     * @param fromUnit the source unit name (case-insensitive)
     * @param toUnit   the target unit name (case-insensitive)
     * @return the conversion result
     * @throws IllegalArgumentException if units are not found or incompatible
     */
    public ConversionResult convert(double amount, String fromUnit, String toUnit) {
        return convert(BigRational.ofDecimal(amount), fromUnit, toUnit);
    }

    /**
     * Converts an amount from one unit to another using arbitrary precision.
     *
     * @param amount   the amount to convert (arbitrary precision)
     * @param fromUnit the source unit name (case-insensitive)
     * @param toUnit   the target unit name (case-insensitive)
     * @return the conversion result
     * @throws NullPointerException     if any parameter is null
     * @throws IllegalArgumentException if units are not found or incompatible
     */
    public ConversionResult convert(BigRational amount, String fromUnit, String toUnit) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(fromUnit, "From unit cannot be null");
        Objects.requireNonNull(toUnit, "To unit cannot be null");

        String normalizedFrom = fromUnit.toLowerCase().trim();
        String normalizedTo = toUnit.toLowerCase().trim();

        for (UnitGroup<?> group : unitGroups) {
            ConversionResult result = group.convert(amount, normalizedFrom, normalizedTo);
            if (result.isSuccessful()) {
                return result;
            }
        }

        throw new IllegalArgumentException(
                String.format("Unable to convert from '%s' to '%s'", fromUnit, toUnit)
        );
    }

    /**
     * Parses and executes a conversion expression in natural language format.
     * <p>
     * Supported formats:
     * </p>
     * <ul>
     *     <li>"100 meters to feet"</li>
     *     <li>"5.5 kg in pounds"</li>
     *     <li>"32 fahrenheit as celsius"</li>
     * </ul>
     *
     * @param conversionExpression the natural language conversion expression
     * @return the conversion result
     * @throws NullPointerException     if conversionExpression is null
     * @throws IllegalArgumentException if expression format is invalid or conversion fails
     */
    public ConversionResult convertFromString(String conversionExpression) {
        Objects.requireNonNull(conversionExpression, "Conversion expression cannot be null");

        Matcher matcher = CONVERSION_PATTERN.matcher(conversionExpression.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid conversion format. Expected: '<amount> <unit> to <unit>'. Got: " + conversionExpression
            );
        }

        try {
            double amount = Double.parseDouble(matcher.group(1).trim());
            String fromUnit = matcher.group(2).trim();
            String toUnit = matcher.group(3).trim();
            return convert(amount, fromUnit, toUnit);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid number format in conversion expression: " + conversionExpression, e
            );
        }
    }

    /**
     * Converts and returns the result as a double.
     *
     * @param amount   the amount to convert
     * @param fromUnit the source unit
     * @param toUnit   the target unit
     * @return the conversion result as a double
     */
    public double convertToDouble(double amount, String fromUnit, String toUnit) {
        return convert(amount, fromUnit, toUnit).result().doubleValue();
    }

    /**
     * Converts and returns the result as a rounded double.
     *
     * @param amount        the amount to convert
     * @param fromUnit      the source unit
     * @param toUnit        the target unit
     * @param decimalPlaces number of decimal places to round to
     * @return the conversion result rounded to specified decimal places
     */
    public double convertToDouble(double amount, String fromUnit, String toUnit, int decimalPlaces) {
        double result = convertToDouble(amount, fromUnit, toUnit);
        return MathUtils.round(result, decimalPlaces);
    }

    /**
     * Converts and returns a formatted string with default precision (7 decimal places).
     *
     * @param amount   the amount to convert
     * @param fromUnit the source unit
     * @param toUnit   the target unit
     * @return formatted string like "100.0 meters = 328.0839895 feet"
     */
    public String convertToFormattedString(double amount, String fromUnit, String toUnit) {
        return convertToFormattedString(amount, fromUnit, toUnit, 7);
    }

    /**
     * Converts and returns a formatted string with proper plural/singular unit names.
     * <p>
     * Example output: "1.0 meter = 3.28 feet" (uses singular for 1.0, plural otherwise)
     * </p>
     *
     * @param amount        the amount to convert
     * @param fromUnit      the source unit
     * @param toUnit        the target unit
     * @param decimalPlaces number of decimal places to display
     * @return formatted conversion string
     */
    public String convertToFormattedString(double amount, String fromUnit, String toUnit, int decimalPlaces) {
        ConversionResult result = convert(amount, fromUnit, toUnit);
        double roundedResult = MathUtils.round(result.result().doubleValue(), decimalPlaces);

        String fromForm = Math.abs(amount) == 1.0
                ? result.fromUnit().getSingular()
                : result.fromUnit().getPlural();
        String toForm = Math.abs(roundedResult) == 1.0
                ? result.toUnit().getSingular()
                : result.toUnit().getPlural();

        return String.format("%s %s = %s %s", amount, fromForm, roundedResult, toForm);
    }

    /**
     * Returns the names of all loaded unit groups.
     *
     * @return list of unit group names (e.g., "length", "mass", "currency")
     */
    public List<String> getUnitGroupNames() {
        return unitGroups.stream()
                .map(UnitGroup::getName)
                .toList();
    }

    /**
     * Returns all unique unit names (plural forms) across all groups.
     *
     * @return unmodifiable set of all unit names
     */
    public Set<String> getAllUnitNames() {
        return unitGroups.stream()
                .flatMap(group -> group.getAllUnitNames().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns all unit aliases (singular, plural, and custom aliases) across all groups.
     *
     * @return unmodifiable set of all unit aliases
     */
    public Set<String> getAllAliases() {
        return allAliases;
    }

    /**
     * Returns all unit names for a specific unit group.
     *
     * @param groupName the name of the unit group (case-insensitive)
     * @return set of unit names in the specified group
     * @throws NullPointerException     if groupName is null
     * @throws IllegalArgumentException if group is not found
     */
    public Set<String> getUnitsForGroup(String groupName) {
        Objects.requireNonNull(groupName, "Group name cannot be null");

        return unitGroups.stream()
                .filter(group -> group.getName().equalsIgnoreCase(groupName))
                .findFirst()
                .map(UnitGroup::getAllUnitNames)
                .orElseThrow(() -> new IllegalArgumentException("Unknown unit group: " + groupName));
    }

    /**
     * Finds the unit group that contains the specified unit.
     *
     * @param unit the unit to search for
     * @return optional containing the group name if found
     * @throws NullPointerException if unit is null
     */
    public Optional<String> findGroupForUnit(Unit unit) {
        Objects.requireNonNull(unit, "Unit cannot be null");

        for (UnitGroup<?> group : unitGroups) {
            if (group.getUnits().contains(unit)) {
                return Optional.of(group.getName());
            }
        }

        return Optional.empty();
    }

    /**
     * Updates all unit groups that support dynamic updates (e.g., currency rates, timezones).
     */
    public void updateAll() {
        unitGroups.forEach(UnitGroup::update);
    }

    /**
     * Updates currency exchange rates from configured providers.
     * <p>
     * This method only updates the currency group if it exists in the engine.
     * Does nothing if no currency group is present.
     * </p>
     *
     * @see uk.co.ryanharrison.mathengine.unitconversion.units.currency.Currency#update()
     */
    public void updateCurrencies() {
        unitGroups.stream()
                .filter(group -> CURRENCY_GROUP_NAME.equalsIgnoreCase(group.getName()))
                .findFirst()
                .ifPresent(UnitGroup::update);
    }

    /**
     * Updates timezone data from configured providers.
     * <p>
     * This method only updates the timezone group if it exists in the engine.
     * Does nothing if no timezone group is present.
     * </p>
     *
     * @see uk.co.ryanharrison.mathengine.unitconversion.units.timezones.TimeZones#update()
     */
    public void updateTimeZones() {
        unitGroups.stream()
                .filter(group -> TIMEZONE_GROUP_NAME.equalsIgnoreCase(group.getName()))
                .findFirst()
                .ifPresent(UnitGroup::update);
    }

    private Set<String> computeAllAliases() {
        return unitGroups.stream()
                .flatMap(group -> group.getAllAliases().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String toString() {
        return String.format("ConversionEngine(%d groups, %d total units)",
                unitGroups.size(), getAllUnitNames().size());
    }

    /**
     * Builder for constructing {@link ConversionEngine} instances with custom configurations.
     *
     * <h2>Usage Example:</h2>
     * <pre>{@code
     * ConversionEngine engine = ConversionEngine.builder()
     *     .loadDefaults()
     *     .addGroup(customGroup)
     *     .build();
     * }</pre>
     */
    public static final class Builder {
        private final List<UnitGroup<?>> unitGroups = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds a single unit group to the builder.
         *
         * @param group the unit group to add
         * @return this builder for method chaining
         * @throws NullPointerException if group is null
         */
        public Builder addGroup(UnitGroup<?> group) {
            Objects.requireNonNull(group, "Unit group cannot be null");
            this.unitGroups.add(group);
            return this;
        }

        /**
         * Adds multiple unit groups to the builder.
         *
         * @param groups the collection of unit groups to add
         * @return this builder for method chaining
         * @throws NullPointerException if groups is null
         */
        public Builder addGroups(Collection<UnitGroup<?>> groups) {
            Objects.requireNonNull(groups, "Unit groups collection cannot be null");
            this.unitGroups.addAll(groups);
            return this;
        }

        /**
         * Loads and adds default unit groups from classpath resources.
         *
         * @return this builder for method chaining
         */
        public Builder loadDefaults() {
            this.unitGroups.addAll(UnitGroupLoader.loadFromClasspath());
            return this;
        }

        /**
         * Builds the conversion engine with the configured unit groups.
         *
         * @return a new conversion engine instance
         * @throws IllegalArgumentException if no unit groups have been added
         */
        public ConversionEngine build() {
            return new ConversionEngine(unitGroups);
        }
    }
}
