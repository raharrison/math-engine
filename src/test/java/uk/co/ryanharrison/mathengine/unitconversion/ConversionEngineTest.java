package uk.co.ryanharrison.mathengine.unitconversion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.unitconversion.units.SimpleUnit;
import uk.co.ryanharrison.mathengine.unitconversion.units.SimpleUnitGroup;
import uk.co.ryanharrison.mathengine.unitconversion.units.UnitGroup;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ConversionEngineTest {

    // ==================== Construction ====================

    @Test
    void loadDefaultsCreatesEngineWithUnitGroups() {
        ConversionEngine engine = ConversionEngine.loadDefaults();

        assertThat(engine.getUnitGroupNames()).isNotEmpty();
        assertThat(engine.getAllUnitNames()).isNotEmpty();
    }

    @Test
    void ofCreatesEngineWithSpecifiedGroups() {
        UnitGroup<?> lengthGroup = createLengthGroup();
        UnitGroup<?> massGroup = createMassGroup();

        ConversionEngine engine = ConversionEngine.of(List.of(lengthGroup, massGroup));

        assertThat(engine.getUnitGroupNames()).containsExactlyInAnyOrder("length", "mass");
    }

    @Test
    void ofRejectsNullGroups() {
        assertThatThrownBy(() -> ConversionEngine.of(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Unit groups cannot be null");
    }

    @Test
    void ofRejectsEmptyGroups() {
        assertThatThrownBy(() -> ConversionEngine.of(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one unit group is required");
    }

    @Test
    void builderCreatesEngineWithGroups() {
        ConversionEngine engine = ConversionEngine.builder()
                .addGroup(createLengthGroup())
                .addGroup(createMassGroup())
                .build();

        assertThat(engine.getUnitGroupNames()).containsExactlyInAnyOrder("length", "mass");
    }

    @Test
    void builderLoadDefaultsAddsDefaultGroups() {
        ConversionEngine engine = ConversionEngine.builder()
                .loadDefaults()
                .build();

        assertThat(engine.getUnitGroupNames()).isNotEmpty();
    }

    @Test
    void builderAddGroupsAddsMultipleGroups() {
        ConversionEngine engine = ConversionEngine.builder()
                .addGroups(List.of(createLengthGroup(), createMassGroup()))
                .build();

        assertThat(engine.getUnitGroupNames()).containsExactlyInAnyOrder("length", "mass");
    }

    @Test
    void builderRejectsNullGroup() {
        assertThatThrownBy(() -> ConversionEngine.builder()
                .addGroup(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Unit group cannot be null");
    }

    @Test
    void builderRejectsNullGroups() {
        assertThatThrownBy(() -> ConversionEngine.builder()
                .addGroups(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Unit groups collection cannot be null");
    }

    // ==================== Conversion - Basic ====================

    @Test
    void convertWithDoublePerformsConversion() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        ConversionResult result = engine.convert(100.0, "meters", "feet");

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.result().doubleValue()).isCloseTo(328.084, within(0.01));
    }

    @Test
    void convertWithBigRationalPerformsConversion() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        ConversionResult result = engine.convert(BigRational.of(100), "meters", "feet");

        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.result().doubleValue()).isCloseTo(328.084, within(0.01));
    }

    @Test
    void convertRejectsNullAmount() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.convert(null, "meters", "feet"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Amount cannot be null");
    }

    @Test
    void convertRejectsNullFromUnit() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.convert(BigRational.of(100), null, "feet"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("From unit cannot be null");
    }

    @Test
    void convertRejectsNullToUnit() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.convert(BigRational.of(100), "meters", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("To unit cannot be null");
    }

    @Test
    void convertThrowsWhenUnitsNotFound() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.convert(100.0, "meters", "pounds"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to convert");
    }

    // ==================== Conversion - String Parsing ====================

    @ParameterizedTest
    @CsvSource({
            "100 meters to feet",
            "100 meters in feet",
            "100 meters as feet",
            "100.5 meters to feet",
            "-50 meters to feet"
    })
    void convertFromStringParsesValidFormats(String expression) {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        ConversionResult result = engine.convertFromString(expression);

        assertThat(result.isSuccessful()).isTrue();
    }

    @Test
    void convertFromStringRejectsNullExpression() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.convertFromString(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Conversion expression cannot be null");
    }

    @Test
    void convertFromStringRejectsInvalidFormat() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.convertFromString("invalid format"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid conversion format");
    }

    @Test
    void convertFromStringRejectsInvalidNumber() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.convertFromString("abc meters to feet"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid number format");
    }

    // ==================== Conversion - Convenience Methods ====================

    @Test
    void convertToDoubleReturnsDoubleValue() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        double result = engine.convertToDouble(100.0, "meters", "feet");

        assertThat(result).isCloseTo(328.084, within(0.01));
    }

    @Test
    void convertToDoubleWithDecimalPlacesRoundsResult() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        double result = engine.convertToDouble(100.0, "meters", "feet", 2);

        assertThat(result).isEqualTo(328.08);
    }

    @Test
    void convertToFormattedStringFormatsWithDefaultPrecision() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        String result = engine.convertToFormattedString(100.0, "meters", "feet");

        assertThat(result).contains("100.0");
        assertThat(result).contains("meters");
        assertThat(result).contains("feet");
        assertThat(result).contains("328.0839895");
    }

    @Test
    void convertToFormattedStringFormatsWithSpecifiedPrecision() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        String result = engine.convertToFormattedString(100.0, "meters", "feet", 2);

        assertThat(result).contains("100.0");
        assertThat(result).contains("meters");
        assertThat(result).contains("328.08");
        assertThat(result).contains("feet");
    }

    @Test
    void convertToFormattedStringUsesSingularForOne() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        String result = engine.convertToFormattedString(1.0, "meters", "feet", 2);

        assertThat(result).contains("1.0 meter");
        assertThat(result).contains("feet");
    }

    // ==================== Query Methods ====================

    @Test
    void getUnitGroupNamesReturnsAllGroupNames() {
        ConversionEngine engine = ConversionEngine.of(List.of(
                createLengthGroup(),
                createMassGroup()
        ));

        List<String> groupNames = engine.getUnitGroupNames();

        assertThat(groupNames).containsExactlyInAnyOrder("length", "mass");
    }

    @Test
    void getAllUnitNamesReturnsAllUnits() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        Set<String> unitNames = engine.getAllUnitNames();

        assertThat(unitNames).contains("meters", "feet", "inches");
    }

    @Test
    void getAllAliasesReturnsAllAliases() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        Set<String> aliases = engine.getAllAliases();

        assertThat(aliases).contains("meter", "meters", "m", "foot", "feet", "ft", "inch", "inches", "in");
    }

    @Test
    void getUnitsForGroupReturnsUnitsInGroup() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        Set<String> units = engine.getUnitsForGroup("length");

        assertThat(units).contains("meters", "feet", "inches");
    }

    @Test
    void getUnitsForGroupIsCaseInsensitive() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        Set<String> units1 = engine.getUnitsForGroup("LENGTH");
        Set<String> units2 = engine.getUnitsForGroup("Length");

        assertThat(units1).isEqualTo(units2);
    }

    @Test
    void getUnitsForGroupRejectsNullGroupName() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.getUnitsForGroup(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Group name cannot be null");
    }

    @Test
    void getUnitsForGroupThrowsForUnknownGroup() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.getUnitsForGroup("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown unit group");
    }

    @Test
    void findGroupForUnitReturnsGroupName() {
        SimpleUnit meter = createUnit("meter", "meters", "m", 1.0);
        UnitGroup<?> lengthGroup = SimpleUnitGroup.of("length", List.of(meter));
        ConversionEngine engine = ConversionEngine.of(List.of(lengthGroup));

        Optional<String> groupName = engine.findGroupForUnit(meter);

        assertThat(groupName).contains("length");
    }

    @Test
    void findGroupForUnitReturnsEmptyForUnknownUnit() {
        SimpleUnit meter = createUnit("meter", "meters", "m", 1.0);
        SimpleUnit unknown = createUnit("unknown", "unknowns", "u", 1.0);
        UnitGroup<?> lengthGroup = SimpleUnitGroup.of("length", List.of(meter));
        ConversionEngine engine = ConversionEngine.of(List.of(lengthGroup));

        Optional<String> groupName = engine.findGroupForUnit(unknown);

        assertThat(groupName).isEmpty();
    }

    @Test
    void findGroupForUnitRejectsNullUnit() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        assertThatThrownBy(() -> engine.findGroupForUnit(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Unit cannot be null");
    }

    // ==================== Update Methods ====================

    @Test
    void updateAllCallsUpdateOnAllGroups() {
        ConversionEngine engine = ConversionEngine.of(List.of(
                createLengthGroup(),
                createMassGroup()
        ));

        // Should not throw
        engine.updateAll();
    }

    @Test
    void updateCurrenciesCallsUpdateOnCurrencyGroup() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        // Should not throw even if currency group doesn't exist
        engine.updateCurrencies();
    }

    @Test
    void updateTimeZonesCallsUpdateOnTimeZoneGroup() {
        ConversionEngine engine = ConversionEngine.of(List.of(createLengthGroup()));

        // Should not throw even if timezone group doesn't exist
        engine.updateTimeZones();
    }

    // ==================== toString ====================

    @Test
    void toStringContainsGroupCountAndUnitCount() {
        ConversionEngine engine = ConversionEngine.of(List.of(
                createLengthGroup(),
                createMassGroup()
        ));

        String string = engine.toString();

        assertThat(string).contains("2 groups");
        assertThat(string).contains("units");
    }

    // ==================== Helper Methods ====================

    private SimpleUnitGroup createLengthGroup() {
        return SimpleUnitGroup.of("length", List.of(
                createUnit("meter", "meters", "m", 1.0),
                createUnit("foot", "feet", "ft", 0.3048),
                createUnit("inch", "inches", "in", 0.0254)
        ));
    }

    private SimpleUnitGroup createMassGroup() {
        return SimpleUnitGroup.of("mass", List.of(
                createUnit("kilogram", "kilograms", "kg", 1.0),
                createUnit("pound", "pounds", "lb", 0.453592),
                createUnit("ounce", "ounces", "oz", 0.0283495)
        ));
    }

    private SimpleUnit createUnit(String singular, String plural, String alias, double conversionFactor) {
        return SimpleUnit.builder()
                .singular(singular)
                .plural(plural)
                .alias(alias)
                .conversionFactor(conversionFactor)
                .build();
    }
}
