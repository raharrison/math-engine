package uk.co.ryanharrison.mathengine.parser.registry;

import java.util.*;

/**
 * Immutable registry for unit definitions.
 * <p>
 * Manages unit definitions and provides lookup by name or alias.
 * Once created, the registry cannot be modified, making it thread-safe.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Use default units
 * UnitRegistry registry = UnitRegistry.withDefaults();
 *
 * // Create custom registry
 * UnitRegistry custom = UnitRegistry.builder()
 *     .add(UnitDefinition.of("meter", "meters", "length", "meter", Factor.ONE, List.of("m")))
 *     .add(UnitDefinition.of("foot", "feet", "length", "meter", Factor.of("0.3048"), List.of("ft")))
 *     .build();
 *
 * // Empty registry (no units)
 * UnitRegistry empty = UnitRegistry.empty();
 * }</pre>
 */
public final class UnitRegistry {

    private static final UnitRegistry STANDARD = buildDefaults();

    private final Map<String, UnitDefinition> units;
    private final List<UnitDefinition> allUnits;

    private UnitRegistry(List<UnitDefinition> unitList) {
        var unitsMap = new HashMap<String, UnitDefinition>();
        var uniqueUnits = new ArrayList<UnitDefinition>();

        for (UnitDefinition unit : unitList) {
            // Track unique units
            if (!unitsMap.containsKey(unit.singularName().toLowerCase())) {
                uniqueUnits.add(unit);
            }

            // Register singular name
            unitsMap.put(unit.singularName().toLowerCase(), unit);

            // Register plural name
            unitsMap.put(unit.pluralName().toLowerCase(), unit);

            // Register aliases
            for (String alias : unit.aliases()) {
                unitsMap.put(alias.toLowerCase(), unit);
            }
        }

        this.units = Map.copyOf(unitsMap);
        this.allUnits = List.copyOf(uniqueUnits);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an empty unit registry.
     *
     * @return new empty registry
     */
    public static UnitRegistry empty() {
        return new UnitRegistry(List.of());
    }

    /**
     * The standard registry. Immutable, so every engine shares one instance.
     */
    public static UnitRegistry withDefaults() {
        return STANDARD;
    }

    /**
     * Creates a registry with standard unit definitions.
     *
     * @return registry with default units
     */
    private static UnitRegistry buildDefaults() {
        return builder()
                .addAll(lengthUnits())
                .addAll(massUnits())
                .addAll(volumeUnits())
                .addAll(timeUnits())
                .addAll(temperatureUnits())
                .addAll(areaUnits())
                .addAll(speedUnits())
                .addAll(pressureUnits())
                .addAll(energyUnits())
                .addAll(powerUnits())
                .addAll(angleUnits())
                .addAll(dataUnits())
                .build();
    }

    /**
     * Creates a new builder for constructing unit registries.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Query Methods ====================

    /**
     * Check if a unit is registered.
     *
     * @param name the unit name to check (case-insensitive)
     * @return true if the unit is registered
     */
    public boolean isUnit(String name) {
        return units.containsKey(name.toLowerCase());
    }

    /**
     * Get a unit definition by name.
     *
     * @param name the unit name (case-insensitive)
     * @return the unit definition, or empty if not found
     */
    public Optional<UnitDefinition> getUnit(String name) {
        return Optional.ofNullable(units.get(name.toLowerCase()));
    }

    /**
     * Get all registered units (deduplicated by primary singular name).
     *
     * @return unmodifiable collection of all unique unit definitions
     */
    public Collection<UnitDefinition> getAllUnits() {
        return allUnits;
    }

    /**
     * Get the number of registered units (distinct units, not counting aliases).
     *
     * @return the number of unique units
     */
    public int size() {
        return allUnits.size();
    }

    // ==================== Standard Unit Sets ====================

    /**
     * Length units (base: meter).
     */
    public static List<UnitDefinition> lengthUnits() {
        return List.of(
                UnitDefinition.of("meter", "meters", "length", "meter", Factor.ONE, List.of("m")),
                UnitDefinition.of("kilometer", "kilometers", "length", "meter", Factor.of("1000"), List.of("km")),
                UnitDefinition.of("centimeter", "centimeters", "length", "meter", Factor.of("0.01"), List.of("cm")),
                UnitDefinition.of("millimeter", "millimeters", "length", "meter", Factor.of("0.001"), List.of("mm")),
                UnitDefinition.of("micrometer", "micrometers", "length", "meter", Factor.of("0.000001"), List.of("micron", "microns", "μm")),
                UnitDefinition.of("nanometer", "nanometers", "length", "meter", Factor.of("0.000000001"), List.of("nm")),
                UnitDefinition.of("foot", "feet", "length", "meter", Factor.of("0.3048"), List.of("ft")),
                UnitDefinition.of("inch", "inches", "length", "meter", Factor.of("0.0254"), List.of("in")),
                UnitDefinition.of("yard", "yards", "length", "meter", Factor.of("0.9144"), List.of("yd")),
                UnitDefinition.of("mile", "miles", "length", "meter", Factor.of("1609.344"), List.of("mi")),
                UnitDefinition.of("nautical_mile", "nautical_miles", "length", "meter", Factor.of("1852"), List.of("nmi"))
        );
    }

    /**
     * Mass units (base: kilogram).
     */
    public static List<UnitDefinition> massUnits() {
        return List.of(
                UnitDefinition.of("kilogram", "kilograms", "mass", "kilogram", Factor.ONE, List.of("kg")),
                UnitDefinition.of("gram", "grams", "mass", "kilogram", Factor.of("0.001"), List.of("g")),
                UnitDefinition.of("milligram", "milligrams", "mass", "kilogram", Factor.of("0.000001"), List.of("mg")),
                UnitDefinition.of("microgram", "micrograms", "mass", "kilogram", Factor.of("0.000000001"), List.of("μg")),
                UnitDefinition.of("tonne", "tonnes", "mass", "kilogram", Factor.of("1000"), List.of("metric_ton", "metric_tons", "t")),
                UnitDefinition.of("pound", "pounds", "mass", "kilogram", Factor.of("0.45359237"), List.of("lb", "lbs")),
                UnitDefinition.of("ounce", "ounces", "mass", "kilogram", Factor.of("0.028349523125"), List.of("oz")),
                UnitDefinition.of("ton", "tons", "mass", "kilogram", Factor.of("907.18474"), List.of("imperial_ton")),
                UnitDefinition.of("stone", "stones", "mass", "kilogram", Factor.of("6.35029318"), List.of("st"))
        );
    }

    /**
     * Volume units (base: liter).
     */
    public static List<UnitDefinition> volumeUnits() {
        return List.of(
                UnitDefinition.of("liter", "liters", "volume", "liter", Factor.ONE, List.of("l", "L")),
                UnitDefinition.of("milliliter", "milliliters", "volume", "liter", Factor.of("0.001"), List.of("ml", "mL")),
                UnitDefinition.of("cubic_meter", "cubic_meters", "volume", "liter", Factor.of("1000"), List.of("m3")),
                UnitDefinition.of("cubic_centimeter", "cubic_centimeters", "volume", "liter", Factor.of("0.001"), List.of("cc", "cm3")),
                UnitDefinition.of("gallon", "gallons", "volume", "liter", Factor.of("3.785411784"), List.of("gal")),
                UnitDefinition.of("quart", "quarts", "volume", "liter", Factor.of("0.946352946"), List.of("qt")),
                UnitDefinition.of("pint", "pints", "volume", "liter", Factor.of("0.473176473"), List.of("pt")),
                UnitDefinition.of("cup", "cups", "volume", "liter", Factor.of("0.2365882365"), List.of()),
                UnitDefinition.of("fluid_ounce", "fluid_ounces", "volume", "liter", Factor.of("0.0295735296"), List.of("fl_oz", "floz")),
                UnitDefinition.of("tablespoon", "tablespoons", "volume", "liter", Factor.of("0.01478676478"), List.of("tbsp")),
                UnitDefinition.of("teaspoon", "teaspoons", "volume", "liter", Factor.of("0.00492892159"), List.of("tsp"))
        );
    }

    /**
     * Time units (base: second).
     */
    public static List<UnitDefinition> timeUnits() {
        return List.of(
                UnitDefinition.of("second", "seconds", "time", "second", Factor.ONE, List.of("s", "sec")),
                UnitDefinition.of("minute", "minutes", "time", "second", Factor.of("60"), List.of("min")),
                UnitDefinition.of("hour", "hours", "time", "second", Factor.of("3600"), List.of("hr", "h")),
                UnitDefinition.of("day", "days", "time", "second", Factor.of("86400"), List.of("d")),
                UnitDefinition.of("week", "weeks", "time", "second", Factor.of("604800"), List.of("wk")),
                UnitDefinition.of("year", "years", "time", "second", Factor.of("31557600"), List.of("yr")),
                UnitDefinition.of("millisecond", "milliseconds", "time", "second", Factor.of("0.001"), List.of("ms")),
                UnitDefinition.of("microsecond", "microseconds", "time", "second", Factor.of("0.000001"), List.of("μs")),
                UnitDefinition.of("nanosecond", "nanoseconds", "time", "second", Factor.of("0.000000001"), List.of("ns"))
        );
    }

    /**
     * Temperature units (base: kelvin).
     */
    public static List<UnitDefinition> temperatureUnits() {
        return List.of(
                UnitDefinition.of("kelvin", "kelvin", "temperature", "kelvin", Factor.ONE, List.of("K")),
                new UnitDefinition("celsius", "celsius", "temperature", "kelvin", Factor.ONE, Factor.of("-273.15"), List.of("C")),
                new UnitDefinition("fahrenheit", "fahrenheit", "temperature", "kelvin", Factor.ratio(5, 9), Factor.of("-459.67"), List.of("F"))
        );
    }

    /**
     * Area units (base: square meter).
     */
    public static List<UnitDefinition> areaUnits() {
        return List.of(
                UnitDefinition.of("square_meter", "square_meters", "area", "square_meter", Factor.ONE, List.of("m2", "sq_m")),
                UnitDefinition.of("square_kilometer", "square_kilometers", "area", "square_meter", Factor.of("1000000"), List.of("km2", "sq_km")),
                UnitDefinition.of("square_centimeter", "square_centimeters", "area", "square_meter", Factor.of("0.0001"), List.of("cm2", "sq_cm")),
                UnitDefinition.of("square_foot", "square_feet", "area", "square_meter", Factor.of("0.09290304"), List.of("ft2", "sq_ft")),
                UnitDefinition.of("square_inch", "square_inches", "area", "square_meter", Factor.of("0.00064516"), List.of("in2", "sq_in")),
                UnitDefinition.of("square_mile", "square_miles", "area", "square_meter", Factor.of("2589988.110336"), List.of("mi2", "sq_mi")),
                UnitDefinition.of("acre", "acres", "area", "square_meter", Factor.of("4046.8564224"), List.of()),
                UnitDefinition.of("hectare", "hectares", "area", "square_meter", Factor.of("10000"), List.of("ha"))
        );
    }

    /**
     * Speed units (base: meters per second).
     */
    public static List<UnitDefinition> speedUnits() {
        return List.of(
                UnitDefinition.of("meters_per_second", "meters_per_second", "speed", "meters_per_second", Factor.ONE, List.of("m/s", "mps")),
                UnitDefinition.of("kilometers_per_hour", "kilometers_per_hour", "speed", "meters_per_second", Factor.ratio(1000, 3600), List.of("km/h", "kph", "kmph")),
                UnitDefinition.of("miles_per_hour", "miles_per_hour", "speed", "meters_per_second", Factor.of("0.44704"), List.of("mph")),
                UnitDefinition.of("knot", "knots", "speed", "meters_per_second", Factor.ratio(1852, 3600), List.of("kt", "kts")),
                UnitDefinition.of("feet_per_second", "feet_per_second", "speed", "meters_per_second", Factor.of("0.3048"), List.of("ft/s", "fps"))
        );
    }

    /**
     * Pressure units (base: pascal).
     */
    public static List<UnitDefinition> pressureUnits() {
        return List.of(
                UnitDefinition.of("pascal", "pascals", "pressure", "pascal", Factor.ONE, List.of("Pa")),
                UnitDefinition.of("kilopascal", "kilopascals", "pressure", "pascal", Factor.of("1000"), List.of("kPa")),
                UnitDefinition.of("bar", "bars", "pressure", "pascal", Factor.of("100000"), List.of()),
                UnitDefinition.of("atmosphere", "atmospheres", "pressure", "pascal", Factor.of("101325"), List.of("atm")),
                UnitDefinition.of("psi", "psi", "pressure", "pascal", Factor.of("6894.757293168361"), List.of("pounds_per_square_inch")),
                UnitDefinition.of("torr", "torr", "pressure", "pascal", Factor.ratio(101325, 760), List.of("mmHg"))
        );
    }

    /**
     * Energy units (base: joule).
     */
    public static List<UnitDefinition> energyUnits() {
        return List.of(
                UnitDefinition.of("joule", "joules", "energy", "joule", Factor.ONE, List.of("J")),
                UnitDefinition.of("kilojoule", "kilojoules", "energy", "joule", Factor.of("1000"), List.of("kJ")),
                UnitDefinition.of("calorie", "calories", "energy", "joule", Factor.of("4.184"), List.of("cal")),
                UnitDefinition.of("kilocalorie", "kilocalories", "energy", "joule", Factor.of("4184"), List.of("kcal", "Cal")),
                UnitDefinition.of("watt_hour", "watt_hours", "energy", "joule", Factor.of("3600"), List.of("Wh")),
                UnitDefinition.of("kilowatt_hour", "kilowatt_hours", "energy", "joule", Factor.of("3600000"), List.of("kWh")),
                UnitDefinition.of("electronvolt", "electronvolts", "energy", "joule", Factor.of("0.0000000000000000001602176634"), List.of("eV"))
        );
    }

    /**
     * Power units (base: watt).
     */
    public static List<UnitDefinition> powerUnits() {
        return List.of(
                UnitDefinition.of("watt", "watts", "power", "watt", Factor.ONE, List.of("W")),
                UnitDefinition.of("kilowatt", "kilowatts", "power", "watt", Factor.of("1000"), List.of("kW")),
                UnitDefinition.of("megawatt", "megawatts", "power", "watt", Factor.of("1000000"), List.of("MW")),
                UnitDefinition.of("horsepower", "horsepower", "power", "watt", Factor.of("745.69987158227022"), List.of("hp"))
        );
    }

    /**
     * Angle units (base: radian).
     */
    public static List<UnitDefinition> angleUnits() {
        return List.of(
                UnitDefinition.of("radian", "radians", "angle", "radian", Factor.ONE, List.of("rad")),
                UnitDefinition.of("degree", "degrees", "angle", "radian", Factor.approx(Math.PI / 180.0), List.of("deg", "°")),
                UnitDefinition.of("gradian", "gradians", "angle", "radian", Factor.approx(Math.PI / 200.0), List.of("grad"))
        );
    }

    /**
     * Data units (base: byte).
     */
    public static List<UnitDefinition> dataUnits() {
        return List.of(
                UnitDefinition.of("byte", "bytes", "data", "byte", Factor.ONE, List.of("B")),
                UnitDefinition.of("kilobyte", "kilobytes", "data", "byte", Factor.of("1024"), List.of("KB", "kB")),
                UnitDefinition.of("megabyte", "megabytes", "data", "byte", Factor.of("1048576"), List.of("MB")),
                UnitDefinition.of("gigabyte", "gigabytes", "data", "byte", Factor.of("1073741824"), List.of("GB")),
                UnitDefinition.of("terabyte", "terabytes", "data", "byte", Factor.of("1099511627776"), List.of("TB")),
                UnitDefinition.of("petabyte", "petabytes", "data", "byte", Factor.of("1125899906842624"), List.of("PB")),
                UnitDefinition.of("bit", "bits", "data", "byte", Factor.of("0.125"), List.of("b")),
                UnitDefinition.of("kilobit", "kilobits", "data", "byte", Factor.of("128"), List.of("Kb", "kb")),
                UnitDefinition.of("megabit", "megabits", "data", "byte", Factor.of("131072"), List.of("Mb")),
                UnitDefinition.of("gigabit", "gigabits", "data", "byte", Factor.of("134217728"), List.of("Gb"))
        );
    }

    // ==================== Builder ====================

    /**
     * Builder for constructing {@link UnitRegistry} instances.
     */
    public static final class Builder {
        private final List<UnitDefinition> units = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds a unit definition.
         *
         * @param unit the unit to add
         * @return this builder
         */
        public Builder add(UnitDefinition unit) {
            if (unit == null) {
                throw new IllegalArgumentException("Unit definition cannot be null");
            }
            units.add(unit);
            return this;
        }

        /**
         * Adds multiple unit definitions.
         *
         * @param unitDefs the units to add
         * @return this builder
         */
        public Builder addAll(Collection<UnitDefinition> unitDefs) {
            for (UnitDefinition unit : unitDefs) {
                add(unit);
            }
            return this;
        }

        /**
         * Builds the unit registry.
         *
         * @return new unit registry
         */
        public UnitRegistry build() {
            return new UnitRegistry(units);
        }
    }
}
