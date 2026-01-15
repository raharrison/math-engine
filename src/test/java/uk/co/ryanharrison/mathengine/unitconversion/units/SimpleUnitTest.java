package uk.co.ryanharrison.mathengine.unitconversion.units;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.core.BigRational;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleUnitTest {

    // ==================== Construction ====================

    @Test
    void builderCreatesUnitWithAllProperties() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .plural("meters")
                .alias("m")
                .alias("mtr")
                .conversionFactor(1.0)
                .build();

        assertThat(unit.getSingular()).isEqualTo("meter");
        assertThat(unit.getPlural()).isEqualTo("meters");
        assertThat(unit.getConversionFactor()).isEqualTo(BigRational.of(1.0));
        assertThat(unit.getAllAliases()).contains("meter", "meters", "m", "mtr");
    }

    @Test
    void builderDefaultsPluralToSingularIfNotSet() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("foot")
                .build();

        assertThat(unit.getSingular()).isEqualTo("foot");
        assertThat(unit.getPlural()).isEqualTo("foot");
    }

    @Test
    void builderDefaultsConversionFactorToOne() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .build();

        assertThat(unit.getConversionFactor()).isEqualTo(BigRational.ONE);
    }

    @Test
    void builderRejectsMissingSingular() {
        assertThatThrownBy(() -> SimpleUnit.builder().build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Singular form is required");
    }

    @Test
    void builderRejectsNullConversionFactor() {
        assertThatThrownBy(() -> SimpleUnit.builder()
                .singular("meter")
                .conversionFactor(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Conversion factor");
    }

    @Test
    void builderAcceptsBigRationalConversionFactor() {
        BigRational factor = BigRational.of(0.3048);
        SimpleUnit unit = SimpleUnit.builder()
                .singular("foot")
                .conversionFactor(factor)
                .build();

        assertThat(unit.getConversionFactor()).isEqualTo(factor);
    }

    @Test
    void builderAcceptsDoubleConversionFactor() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("foot")
                .conversionFactor(0.3048)
                .build();

        assertThat(unit.getConversionFactor()).isEqualTo(BigRational.of(0.3048));
    }

    @Test
    void builderIgnoresNullAndEmptyAliases() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .alias(null)
                .alias("")
                .alias("m")
                .build();

        assertThat(unit.getAllAliases()).contains("meter", "m");
        assertThat(unit.getAllAliases().size()).isEqualTo(2);
    }

    @Test
    void builderNormalizesAliasesToLowercase() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .alias("M")
                .alias("MTR")
                .build();

        assertThat(unit.matches("m")).isTrue();
        assertThat(unit.matches("mtr")).isTrue();
        assertThat(unit.matches("MTR")).isTrue();
    }

    // ==================== Getters and Basic Properties ====================

    @Test
    void getSingularReturnsCorrectValue() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .plural("meters")
                .build();

        assertThat(unit.getSingular()).isEqualTo("meter");
    }

    @Test
    void getPluralReturnsCorrectValue() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .plural("meters")
                .build();

        assertThat(unit.getPlural()).isEqualTo("meters");
    }

    @Test
    void getConversionFactorReturnsCorrectValue() {
        BigRational factor = BigRational.of(0.3048);
        SimpleUnit unit = SimpleUnit.builder()
                .singular("foot")
                .conversionFactor(factor)
                .build();

        assertThat(unit.getConversionFactor()).isEqualTo(factor);
    }

    @Test
    void getAllAliasesIncludesSingularPluralAndCustomAliases() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .plural("meters")
                .alias("m")
                .alias("mtr")
                .build();

        assertThat(unit.getAllAliases()).containsExactlyInAnyOrder("meter", "meters", "m", "mtr");
    }

    @Test
    void getAllAliasesDoesNotDuplicateSingularWhenEqualToPlural() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("celsius")
                .plural("celsius")
                .build();

        assertThat(unit.getAllAliases()).containsExactly("celsius");
    }

    // ==================== Matching ====================

    @ParameterizedTest
    @CsvSource({
            "meter, meter",
            "meter, METER",
            "meter, Meter",
            "meter, meters",
            "meter, METERS",
            "meter, m",
            "meter, M"
    })
    void matchesIsCaseInsensitive(String input, String toMatch) {
        SimpleUnit unit = SimpleUnit.builder()
                .singular(input)
                .plural(input + "s")
                .alias(input.substring(0, 1))
                .build();

        assertThat(unit.matches(toMatch)).isTrue();
    }

    @Test
    void matchesReturnsFalseForNonMatchingInput() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .plural("meters")
                .alias("m")
                .build();

        assertThat(unit.matches("foot")).isFalse();
        assertThat(unit.matches("feet")).isFalse();
        assertThat(unit.matches("km")).isFalse();
    }

    @Test
    void matchesReturnsFalseForNull() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .build();

        assertThat(unit.matches(null)).isFalse();
    }

    // ==================== withConversionFactor ====================

    @Test
    void withConversionFactorBigRationalCreatesNewUnitWithUpdatedFactor() {
        SimpleUnit original = SimpleUnit.builder()
                .singular("USD")
                .plural("USD")
                .conversionFactor(1.0)
                .build();

        BigRational newFactor = BigRational.of(1.2);
        SimpleUnit updated = original.withConversionFactor(newFactor);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.getConversionFactor()).isEqualTo(newFactor);
        assertThat(updated.getSingular()).isEqualTo(original.getSingular());
        assertThat(updated.getPlural()).isEqualTo(original.getPlural());
    }

    @Test
    void withConversionFactorDoubleCreatesNewUnitWithUpdatedFactor() {
        SimpleUnit original = SimpleUnit.builder()
                .singular("USD")
                .plural("USD")
                .conversionFactor(1.0)
                .build();

        SimpleUnit updated = original.withConversionFactor(1.2);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.getConversionFactor()).isEqualTo(BigRational.of(1.2));
    }

    @Test
    void withConversionFactorPreservesOriginalUnit() {
        SimpleUnit original = SimpleUnit.builder()
                .singular("USD")
                .plural("USD")
                .conversionFactor(1.0)
                .build();

        original.withConversionFactor(1.2);

        assertThat(original.getConversionFactor()).isEqualTo(BigRational.of(1.0));
    }

    // ==================== Equality and HashCode ====================

    @Test
    void equalsReturnsTrueForIdenticalUnits() {
        SimpleUnit unit1 = SimpleUnit.builder()
                .singular("meter")
                .plural("meters")
                .alias("m")
                .conversionFactor(1.0)
                .build();

        SimpleUnit unit2 = SimpleUnit.builder()
                .singular("meter")
                .plural("meters")
                .alias("m")
                .conversionFactor(1.0)
                .build();

        assertThat(unit1).isEqualTo(unit2);
        assertThat(unit1.hashCode()).isEqualTo(unit2.hashCode());
    }

    @Test
    void equalsReturnsFalseForDifferentConversionFactors() {
        SimpleUnit unit1 = SimpleUnit.builder()
                .singular("meter")
                .conversionFactor(1.0)
                .build();

        SimpleUnit unit2 = SimpleUnit.builder()
                .singular("meter")
                .conversionFactor(2.0)
                .build();

        assertThat(unit1).isNotEqualTo(unit2);
    }

    @Test
    void equalsReturnsFalseForDifferentSingulars() {
        SimpleUnit unit1 = SimpleUnit.builder()
                .singular("meter")
                .build();

        SimpleUnit unit2 = SimpleUnit.builder()
                .singular("foot")
                .build();

        assertThat(unit1).isNotEqualTo(unit2);
    }

    // ==================== toString ====================

    @Test
    void toStringReturnsPluralForm() {
        SimpleUnit unit = SimpleUnit.builder()
                .singular("meter")
                .plural("meters")
                .build();

        assertThat(unit.toString()).isEqualTo("meters");
    }
}
