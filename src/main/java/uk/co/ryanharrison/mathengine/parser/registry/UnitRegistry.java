package uk.co.ryanharrison.mathengine.parser.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for unit definitions.
 * Manages unit definitions and provides lookup by name or alias.
 * This is a skeleton implementation for Phase 0.
 */
public final class UnitRegistry {

    private final Map<String, UnitDefinition> units;

    public UnitRegistry() {
        this.units = new HashMap<>();
        initializeDefaultUnits();
    }

    /**
     * Initialize default units across common measurement categories.
     */
    private void initializeDefaultUnits() {
        // ==================== Length (base: meter) ====================
        register(new UnitDefinition("meter", "meters", "length", "meter", 1.0, 0.0, List.of("m")));
        register(new UnitDefinition("kilometer", "kilometers", "length", "meter", 1000.0, 0.0, List.of("km")));
        register(new UnitDefinition("centimeter", "centimeters", "length", "meter", 0.01, 0.0, List.of("cm")));
        register(new UnitDefinition("millimeter", "millimeters", "length", "meter", 0.001, 0.0, List.of("mm")));
        register(new UnitDefinition("micrometer", "micrometers", "length", "meter", 1e-6, 0.0, List.of("micron", "microns", "μm")));
        register(new UnitDefinition("nanometer", "nanometers", "length", "meter", 1e-9, 0.0, List.of("nm")));
        register(new UnitDefinition("foot", "feet", "length", "meter", 0.3048, 0.0, List.of("ft")));
        register(new UnitDefinition("inch", "inches", "length", "meter", 0.0254, 0.0, List.of("in")));
        register(new UnitDefinition("yard", "yards", "length", "meter", 0.9144, 0.0, List.of("yd")));
        register(new UnitDefinition("mile", "miles", "length", "meter", 1609.344, 0.0, List.of("mi")));
        register(new UnitDefinition("nautical_mile", "nautical_miles", "length", "meter", 1852.0, 0.0, List.of("nmi")));

        // ==================== Mass (base: kilogram) ====================
        register(new UnitDefinition("kilogram", "kilograms", "mass", "kilogram", 1.0, 0.0, List.of("kg")));
        register(new UnitDefinition("gram", "grams", "mass", "kilogram", 0.001, 0.0, List.of("g")));
        register(new UnitDefinition("milligram", "milligrams", "mass", "kilogram", 1e-6, 0.0, List.of("mg")));
        register(new UnitDefinition("microgram", "micrograms", "mass", "kilogram", 1e-9, 0.0, List.of("μg")));
        register(new UnitDefinition("tonne", "tonnes", "mass", "kilogram", 1000.0, 0.0, List.of("metric_ton", "metric_tons", "t")));
        register(new UnitDefinition("pound", "pounds", "mass", "kilogram", 0.45359237, 0.0, List.of("lb", "lbs")));
        register(new UnitDefinition("ounce", "ounces", "mass", "kilogram", 0.028349523125, 0.0, List.of("oz")));
        register(new UnitDefinition("ton", "tons", "mass", "kilogram", 907.18474, 0.0, List.of("imperial_ton")));
        register(new UnitDefinition("stone", "stones", "mass", "kilogram", 6.35029318, 0.0, List.of("st")));

        // ==================== Volume (base: liter) ====================
        register(new UnitDefinition("liter", "liters", "volume", "liter", 1.0, 0.0, List.of("l", "L")));
        register(new UnitDefinition("milliliter", "milliliters", "volume", "liter", 0.001, 0.0, List.of("ml", "mL")));
        register(new UnitDefinition("cubic_meter", "cubic_meters", "volume", "liter", 1000.0, 0.0, List.of("m3")));
        register(new UnitDefinition("cubic_centimeter", "cubic_centimeters", "volume", "liter", 0.001, 0.0, List.of("cc", "cm3")));
        register(new UnitDefinition("gallon", "gallons", "volume", "liter", 3.785411784, 0.0, List.of("gal")));
        register(new UnitDefinition("quart", "quarts", "volume", "liter", 0.946352946, 0.0, List.of("qt")));
        register(new UnitDefinition("pint", "pints", "volume", "liter", 0.473176473, 0.0, List.of("pt")));
        register(new UnitDefinition("cup", "cups", "volume", "liter", 0.2365882365, 0.0, List.of()));
        register(new UnitDefinition("fluid_ounce", "fluid_ounces", "volume", "liter", 0.0295735296, 0.0, List.of("fl_oz", "floz")));
        register(new UnitDefinition("tablespoon", "tablespoons", "volume", "liter", 0.01478676478, 0.0, List.of("tbsp")));
        register(new UnitDefinition("teaspoon", "teaspoons", "volume", "liter", 0.00492892159, 0.0, List.of("tsp")));

        // ==================== Time (base: second) ====================
        register(new UnitDefinition("second", "seconds", "time", "second", 1.0, 0.0, List.of("s", "sec")));
        register(new UnitDefinition("minute", "minutes", "time", "second", 60.0, 0.0, List.of("min")));
        register(new UnitDefinition("hour", "hours", "time", "second", 3600.0, 0.0, List.of("hr", "h")));
        register(new UnitDefinition("day", "days", "time", "second", 86400.0, 0.0, List.of("d")));
        register(new UnitDefinition("week", "weeks", "time", "second", 604800.0, 0.0, List.of("wk")));
        register(new UnitDefinition("year", "years", "time", "second", 31557600.0, 0.0, List.of("yr")));
        register(new UnitDefinition("millisecond", "milliseconds", "time", "second", 0.001, 0.0, List.of("ms")));
        register(new UnitDefinition("microsecond", "microseconds", "time", "second", 1e-6, 0.0, List.of("μs")));
        register(new UnitDefinition("nanosecond", "nanoseconds", "time", "second", 1e-9, 0.0, List.of("ns")));

        // ==================== Temperature (base: kelvin) ====================
        // Formula: toBase = (value - offset) / multiplier
        //          fromBase = value * multiplier + offset
        // Celsius: K = (C - (-273.15)) / 1.0 = C + 273.15 → offset=-273.15, multiplier=1.0
        // Fahrenheit: K = (F - (-459.67)) / (5/9) = (F + 459.67) / (5/9) → offset=-459.67, multiplier=5/9
        // Note: Temperature units don't have plural forms (use same name for both)
        register(new UnitDefinition("kelvin", "kelvin", "temperature", "kelvin", 1.0, 0.0, List.of("K")));
        register(new UnitDefinition("celsius", "celsius", "temperature", "kelvin", 1.0, -273.15, List.of("C")));
        register(new UnitDefinition("fahrenheit", "fahrenheit", "temperature", "kelvin", 5.0 / 9.0, -459.67, List.of("F")));

        // ==================== Area (base: square meter) ====================
        register(new UnitDefinition("square_meter", "square_meters", "area", "square_meter", 1.0, 0.0, List.of("m2", "sq_m")));
        register(new UnitDefinition("square_kilometer", "square_kilometers", "area", "square_meter", 1e6, 0.0, List.of("km2", "sq_km")));
        register(new UnitDefinition("square_centimeter", "square_centimeters", "area", "square_meter", 1e-4, 0.0, List.of("cm2", "sq_cm")));
        register(new UnitDefinition("square_foot", "square_feet", "area", "square_meter", 0.09290304, 0.0, List.of("ft2", "sq_ft")));
        register(new UnitDefinition("square_inch", "square_inches", "area", "square_meter", 0.00064516, 0.0, List.of("in2", "sq_in")));
        register(new UnitDefinition("square_mile", "square_miles", "area", "square_meter", 2589988.110336, 0.0, List.of("mi2", "sq_mi")));
        register(new UnitDefinition("acre", "acres", "area", "square_meter", 4046.8564224, 0.0, List.of()));
        register(new UnitDefinition("hectare", "hectares", "area", "square_meter", 10000.0, 0.0, List.of("ha")));

        // ==================== Speed (base: meters per second) ====================
        // Note: Speed units typically don't pluralize (same form for singular/plural)
        register(new UnitDefinition("meters_per_second", "meters_per_second", "speed", "meters_per_second", 1.0, 0.0, List.of("m/s", "mps")));
        register(new UnitDefinition("kilometers_per_hour", "kilometers_per_hour", "speed", "meters_per_second", 0.277778, 0.0, List.of("km/h", "kph", "kmph")));
        register(new UnitDefinition("miles_per_hour", "miles_per_hour", "speed", "meters_per_second", 0.44704, 0.0, List.of("mph")));
        register(new UnitDefinition("knot", "knots", "speed", "meters_per_second", 0.514444, 0.0, List.of("kt", "kts")));
        register(new UnitDefinition("feet_per_second", "feet_per_second", "speed", "meters_per_second", 0.3048, 0.0, List.of("ft/s", "fps")));

        // ==================== Pressure (base: pascal) ====================
        register(new UnitDefinition("pascal", "pascals", "pressure", "pascal", 1.0, 0.0, List.of("Pa")));
        register(new UnitDefinition("kilopascal", "kilopascals", "pressure", "pascal", 1000.0, 0.0, List.of("kPa")));
        register(new UnitDefinition("bar", "bars", "pressure", "pascal", 100000.0, 0.0, List.of()));
        register(new UnitDefinition("atmosphere", "atmospheres", "pressure", "pascal", 101325.0, 0.0, List.of("atm")));
        register(new UnitDefinition("psi", "psi", "pressure", "pascal", 6894.757, 0.0, List.of("pounds_per_square_inch")));
        register(new UnitDefinition("torr", "torr", "pressure", "pascal", 133.322, 0.0, List.of("mmHg")));

        // ==================== Energy (base: joule) ====================
        register(new UnitDefinition("joule", "joules", "energy", "joule", 1.0, 0.0, List.of("J")));
        register(new UnitDefinition("kilojoule", "kilojoules", "energy", "joule", 1000.0, 0.0, List.of("kJ")));
        register(new UnitDefinition("calorie", "calories", "energy", "joule", 4.184, 0.0, List.of("cal")));
        register(new UnitDefinition("kilocalorie", "kilocalories", "energy", "joule", 4184.0, 0.0, List.of("kcal", "Cal")));
        register(new UnitDefinition("watt_hour", "watt_hours", "energy", "joule", 3600.0, 0.0, List.of("Wh")));
        register(new UnitDefinition("kilowatt_hour", "kilowatt_hours", "energy", "joule", 3.6e6, 0.0, List.of("kWh")));
        register(new UnitDefinition("electronvolt", "electronvolts", "energy", "joule", 1.602176634e-19, 0.0, List.of("eV")));

        // ==================== Power (base: watt) ====================
        register(new UnitDefinition("watt", "watts", "power", "watt", 1.0, 0.0, List.of("W")));
        register(new UnitDefinition("kilowatt", "kilowatts", "power", "watt", 1000.0, 0.0, List.of("kW")));
        register(new UnitDefinition("megawatt", "megawatts", "power", "watt", 1e6, 0.0, List.of("MW")));
        register(new UnitDefinition("horsepower", "horsepower", "power", "watt", 745.699872, 0.0, List.of("hp")));

        // ==================== Angle (base: radian) ====================
        register(new UnitDefinition("radian", "radians", "angle", "radian", 1.0, 0.0, List.of("rad")));
        register(new UnitDefinition("degree", "degrees", "angle", "radian", Math.PI / 180.0, 0.0, List.of("deg", "°")));
        register(new UnitDefinition("gradian", "gradians", "angle", "radian", Math.PI / 200.0, 0.0, List.of("grad")));

        // ==================== Data (base: byte) ====================
        register(new UnitDefinition("byte", "bytes", "data", "byte", 1.0, 0.0, List.of("B")));
        register(new UnitDefinition("kilobyte", "kilobytes", "data", "byte", 1024.0, 0.0, List.of("KB", "kB")));
        register(new UnitDefinition("megabyte", "megabytes", "data", "byte", 1048576.0, 0.0, List.of("MB")));
        register(new UnitDefinition("gigabyte", "gigabytes", "data", "byte", 1.073741824e9, 0.0, List.of("GB")));
        register(new UnitDefinition("terabyte", "terabytes", "data", "byte", 1.099511627776e12, 0.0, List.of("TB")));
        register(new UnitDefinition("petabyte", "petabytes", "data", "byte", 1.125899906842624e15, 0.0, List.of("PB")));
        register(new UnitDefinition("bit", "bits", "data", "byte", 0.125, 0.0, List.of("b")));
        register(new UnitDefinition("kilobit", "kilobits", "data", "byte", 128.0, 0.0, List.of("Kb", "kb")));
        register(new UnitDefinition("megabit", "megabits", "data", "byte", 131072.0, 0.0, List.of("Mb")));
        register(new UnitDefinition("gigabit", "gigabits", "data", "byte", 1.34217728e8, 0.0, List.of("Gb")));
    }

    /**
     * Register a unit definition with its singular name, plural name, and all aliases.
     */
    public void register(UnitDefinition unit) {
        // Register singular name
        units.put(unit.getSingularName().toLowerCase(), unit);

        // Register plural name
        units.put(unit.getPluralName().toLowerCase(), unit);

        // Register aliases
        for (String alias : unit.getAliases()) {
            units.put(alias.toLowerCase(), unit);
        }
    }

    /**
     * Check if a unit is registered.
     */
    public boolean isUnit(String name) {
        return units.containsKey(name.toLowerCase());
    }

    /**
     * Get a unit definition by name.
     */
    public UnitDefinition get(String name) {
        return units.get(name.toLowerCase());
    }

    /**
     * Get all registered units (deduplicated by primary singular name).
     * Since units are stored by all their names (singular, plural, aliases),
     * this returns a distinct set based on the singular name.
     */
    public java.util.Collection<UnitDefinition> getAllUnits() {
        // Units map contains the same UnitDefinition under multiple keys (singular, plural, aliases)
        // So we need to deduplicate based on singular name
        Map<String, UnitDefinition> uniqueUnits = new java.util.LinkedHashMap<>();
        for (UnitDefinition unit : units.values()) {
            uniqueUnits.putIfAbsent(unit.getSingularName(), unit);
        }
        return uniqueUnits.values();
    }

    /**
     * Get the number of registered units (distinct units, not counting aliases).
     */
    public int size() {
        return getAllUnits().size();
    }
}
