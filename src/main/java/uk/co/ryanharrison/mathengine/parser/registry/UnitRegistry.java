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
 *     .add(new UnitDefinition("meter", "meters", "length", "meter", 1.0, 0.0, List.of("m")))
 *     .add(new UnitDefinition("foot", "feet", "length", "meter", 0.3048, 0.0, List.of("ft")))
 *     .build();
 *
 * // Empty registry (no units)
 * UnitRegistry empty = UnitRegistry.empty();
 * }</pre>
 */
public final class UnitRegistry {

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
     * Creates a registry with standard unit definitions.
     *
     * @return registry with default units
     */
    public static UnitRegistry withDefaults() {
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
     * @return the unit definition, or null if not found
     */
    public UnitDefinition get(String name) {
        return units.get(name.toLowerCase());
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
                new UnitDefinition("meter", "meters", "length", "meter", 1.0, 0.0, List.of("m")),
                new UnitDefinition("kilometer", "kilometers", "length", "meter", 1000.0, 0.0, List.of("km")),
                new UnitDefinition("centimeter", "centimeters", "length", "meter", 0.01, 0.0, List.of("cm")),
                new UnitDefinition("millimeter", "millimeters", "length", "meter", 0.001, 0.0, List.of("mm")),
                new UnitDefinition("micrometer", "micrometers", "length", "meter", 1e-6, 0.0, List.of("micron", "microns", "μm")),
                new UnitDefinition("nanometer", "nanometers", "length", "meter", 1e-9, 0.0, List.of("nm")),
                new UnitDefinition("foot", "feet", "length", "meter", 0.3048, 0.0, List.of("ft")),
                new UnitDefinition("inch", "inches", "length", "meter", 0.0254, 0.0, List.of("in")),
                new UnitDefinition("yard", "yards", "length", "meter", 0.9144, 0.0, List.of("yd")),
                new UnitDefinition("mile", "miles", "length", "meter", 1609.344, 0.0, List.of("mi")),
                new UnitDefinition("nautical_mile", "nautical_miles", "length", "meter", 1852.0, 0.0, List.of("nmi"))
        );
    }

    /**
     * Mass units (base: kilogram).
     */
    public static List<UnitDefinition> massUnits() {
        return List.of(
                new UnitDefinition("kilogram", "kilograms", "mass", "kilogram", 1.0, 0.0, List.of("kg")),
                new UnitDefinition("gram", "grams", "mass", "kilogram", 0.001, 0.0, List.of("g")),
                new UnitDefinition("milligram", "milligrams", "mass", "kilogram", 1e-6, 0.0, List.of("mg")),
                new UnitDefinition("microgram", "micrograms", "mass", "kilogram", 1e-9, 0.0, List.of("μg")),
                new UnitDefinition("tonne", "tonnes", "mass", "kilogram", 1000.0, 0.0, List.of("metric_ton", "metric_tons", "t")),
                new UnitDefinition("pound", "pounds", "mass", "kilogram", 0.45359237, 0.0, List.of("lb", "lbs")),
                new UnitDefinition("ounce", "ounces", "mass", "kilogram", 0.028349523125, 0.0, List.of("oz")),
                new UnitDefinition("ton", "tons", "mass", "kilogram", 907.18474, 0.0, List.of("imperial_ton")),
                new UnitDefinition("stone", "stones", "mass", "kilogram", 6.35029318, 0.0, List.of("st"))
        );
    }

    /**
     * Volume units (base: liter).
     */
    public static List<UnitDefinition> volumeUnits() {
        return List.of(
                new UnitDefinition("liter", "liters", "volume", "liter", 1.0, 0.0, List.of("l", "L")),
                new UnitDefinition("milliliter", "milliliters", "volume", "liter", 0.001, 0.0, List.of("ml", "mL")),
                new UnitDefinition("cubic_meter", "cubic_meters", "volume", "liter", 1000.0, 0.0, List.of("m3")),
                new UnitDefinition("cubic_centimeter", "cubic_centimeters", "volume", "liter", 0.001, 0.0, List.of("cc", "cm3")),
                new UnitDefinition("gallon", "gallons", "volume", "liter", 3.785411784, 0.0, List.of("gal")),
                new UnitDefinition("quart", "quarts", "volume", "liter", 0.946352946, 0.0, List.of("qt")),
                new UnitDefinition("pint", "pints", "volume", "liter", 0.473176473, 0.0, List.of("pt")),
                new UnitDefinition("cup", "cups", "volume", "liter", 0.2365882365, 0.0, List.of()),
                new UnitDefinition("fluid_ounce", "fluid_ounces", "volume", "liter", 0.0295735296, 0.0, List.of("fl_oz", "floz")),
                new UnitDefinition("tablespoon", "tablespoons", "volume", "liter", 0.01478676478, 0.0, List.of("tbsp")),
                new UnitDefinition("teaspoon", "teaspoons", "volume", "liter", 0.00492892159, 0.0, List.of("tsp"))
        );
    }

    /**
     * Time units (base: second).
     */
    public static List<UnitDefinition> timeUnits() {
        return List.of(
                new UnitDefinition("second", "seconds", "time", "second", 1.0, 0.0, List.of("s", "sec")),
                new UnitDefinition("minute", "minutes", "time", "second", 60.0, 0.0, List.of("min")),
                new UnitDefinition("hour", "hours", "time", "second", 3600.0, 0.0, List.of("hr", "h")),
                new UnitDefinition("day", "days", "time", "second", 86400.0, 0.0, List.of("d")),
                new UnitDefinition("week", "weeks", "time", "second", 604800.0, 0.0, List.of("wk")),
                new UnitDefinition("year", "years", "time", "second", 31557600.0, 0.0, List.of("yr")),
                new UnitDefinition("millisecond", "milliseconds", "time", "second", 0.001, 0.0, List.of("ms")),
                new UnitDefinition("microsecond", "microseconds", "time", "second", 1e-6, 0.0, List.of("μs")),
                new UnitDefinition("nanosecond", "nanoseconds", "time", "second", 1e-9, 0.0, List.of("ns"))
        );
    }

    /**
     * Temperature units (base: kelvin).
     */
    public static List<UnitDefinition> temperatureUnits() {
        return List.of(
                new UnitDefinition("kelvin", "kelvin", "temperature", "kelvin", 1.0, 0.0, List.of("K")),
                new UnitDefinition("celsius", "celsius", "temperature", "kelvin", 1.0, -273.15, List.of("C")),
                new UnitDefinition("fahrenheit", "fahrenheit", "temperature", "kelvin", 5.0 / 9.0, -459.67, List.of("F"))
        );
    }

    /**
     * Area units (base: square meter).
     */
    public static List<UnitDefinition> areaUnits() {
        return List.of(
                new UnitDefinition("square_meter", "square_meters", "area", "square_meter", 1.0, 0.0, List.of("m2", "sq_m")),
                new UnitDefinition("square_kilometer", "square_kilometers", "area", "square_meter", 1e6, 0.0, List.of("km2", "sq_km")),
                new UnitDefinition("square_centimeter", "square_centimeters", "area", "square_meter", 1e-4, 0.0, List.of("cm2", "sq_cm")),
                new UnitDefinition("square_foot", "square_feet", "area", "square_meter", 0.09290304, 0.0, List.of("ft2", "sq_ft")),
                new UnitDefinition("square_inch", "square_inches", "area", "square_meter", 0.00064516, 0.0, List.of("in2", "sq_in")),
                new UnitDefinition("square_mile", "square_miles", "area", "square_meter", 2589988.110336, 0.0, List.of("mi2", "sq_mi")),
                new UnitDefinition("acre", "acres", "area", "square_meter", 4046.8564224, 0.0, List.of()),
                new UnitDefinition("hectare", "hectares", "area", "square_meter", 10000.0, 0.0, List.of("ha"))
        );
    }

    /**
     * Speed units (base: meters per second).
     */
    public static List<UnitDefinition> speedUnits() {
        return List.of(
                new UnitDefinition("meters_per_second", "meters_per_second", "speed", "meters_per_second", 1.0, 0.0, List.of("m/s", "mps")),
                new UnitDefinition("kilometers_per_hour", "kilometers_per_hour", "speed", "meters_per_second", 0.277778, 0.0, List.of("km/h", "kph", "kmph")),
                new UnitDefinition("miles_per_hour", "miles_per_hour", "speed", "meters_per_second", 0.44704, 0.0, List.of("mph")),
                new UnitDefinition("knot", "knots", "speed", "meters_per_second", 0.514444, 0.0, List.of("kt", "kts")),
                new UnitDefinition("feet_per_second", "feet_per_second", "speed", "meters_per_second", 0.3048, 0.0, List.of("ft/s", "fps"))
        );
    }

    /**
     * Pressure units (base: pascal).
     */
    public static List<UnitDefinition> pressureUnits() {
        return List.of(
                new UnitDefinition("pascal", "pascals", "pressure", "pascal", 1.0, 0.0, List.of("Pa")),
                new UnitDefinition("kilopascal", "kilopascals", "pressure", "pascal", 1000.0, 0.0, List.of("kPa")),
                new UnitDefinition("bar", "bars", "pressure", "pascal", 100000.0, 0.0, List.of()),
                new UnitDefinition("atmosphere", "atmospheres", "pressure", "pascal", 101325.0, 0.0, List.of("atm")),
                new UnitDefinition("psi", "psi", "pressure", "pascal", 6894.757, 0.0, List.of("pounds_per_square_inch")),
                new UnitDefinition("torr", "torr", "pressure", "pascal", 133.322, 0.0, List.of("mmHg"))
        );
    }

    /**
     * Energy units (base: joule).
     */
    public static List<UnitDefinition> energyUnits() {
        return List.of(
                new UnitDefinition("joule", "joules", "energy", "joule", 1.0, 0.0, List.of("J")),
                new UnitDefinition("kilojoule", "kilojoules", "energy", "joule", 1000.0, 0.0, List.of("kJ")),
                new UnitDefinition("calorie", "calories", "energy", "joule", 4.184, 0.0, List.of("cal")),
                new UnitDefinition("kilocalorie", "kilocalories", "energy", "joule", 4184.0, 0.0, List.of("kcal", "Cal")),
                new UnitDefinition("watt_hour", "watt_hours", "energy", "joule", 3600.0, 0.0, List.of("Wh")),
                new UnitDefinition("kilowatt_hour", "kilowatt_hours", "energy", "joule", 3.6e6, 0.0, List.of("kWh")),
                new UnitDefinition("electronvolt", "electronvolts", "energy", "joule", 1.602176634e-19, 0.0, List.of("eV"))
        );
    }

    /**
     * Power units (base: watt).
     */
    public static List<UnitDefinition> powerUnits() {
        return List.of(
                new UnitDefinition("watt", "watts", "power", "watt", 1.0, 0.0, List.of("W")),
                new UnitDefinition("kilowatt", "kilowatts", "power", "watt", 1000.0, 0.0, List.of("kW")),
                new UnitDefinition("megawatt", "megawatts", "power", "watt", 1e6, 0.0, List.of("MW")),
                new UnitDefinition("horsepower", "horsepower", "power", "watt", 745.699872, 0.0, List.of("hp"))
        );
    }

    /**
     * Angle units (base: radian).
     */
    public static List<UnitDefinition> angleUnits() {
        return List.of(
                new UnitDefinition("radian", "radians", "angle", "radian", 1.0, 0.0, List.of("rad")),
                new UnitDefinition("degree", "degrees", "angle", "radian", Math.PI / 180.0, 0.0, List.of("deg", "°")),
                new UnitDefinition("gradian", "gradians", "angle", "radian", Math.PI / 200.0, 0.0, List.of("grad"))
        );
    }

    /**
     * Data units (base: byte).
     */
    public static List<UnitDefinition> dataUnits() {
        return List.of(
                new UnitDefinition("byte", "bytes", "data", "byte", 1.0, 0.0, List.of("B")),
                new UnitDefinition("kilobyte", "kilobytes", "data", "byte", 1024.0, 0.0, List.of("KB", "kB")),
                new UnitDefinition("megabyte", "megabytes", "data", "byte", 1048576.0, 0.0, List.of("MB")),
                new UnitDefinition("gigabyte", "gigabytes", "data", "byte", 1.073741824e9, 0.0, List.of("GB")),
                new UnitDefinition("terabyte", "terabytes", "data", "byte", 1.099511627776e12, 0.0, List.of("TB")),
                new UnitDefinition("petabyte", "petabytes", "data", "byte", 1.125899906842624e15, 0.0, List.of("PB")),
                new UnitDefinition("bit", "bits", "data", "byte", 0.125, 0.0, List.of("b")),
                new UnitDefinition("kilobit", "kilobits", "data", "byte", 128.0, 0.0, List.of("Kb", "kb")),
                new UnitDefinition("megabit", "megabits", "data", "byte", 131072.0, 0.0, List.of("Mb")),
                new UnitDefinition("gigabit", "gigabits", "data", "byte", 1.34217728e8, 0.0, List.of("Gb"))
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
