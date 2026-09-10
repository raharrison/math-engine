package uk.co.ryanharrison.mathengine.unitconversion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionEngineEnergyTest extends ConversionEngineBaseTest {

    @Test
    public void testEnergy() {
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "attojoule", "joule", PLACES), 0.0);
        assertEquals(10296000.0, engine.convertToDouble(AMOUNT, "Board of Trade unit", "joule", PLACES), 0.0);
        assertEquals(3017.459874, engine.convertToDouble(AMOUNT, "Btu", "joule", PLACES), 0.0);
        assertEquals(11.974248, engine.convertToDouble(AMOUNT, "calories", "joule", PLACES), 0.0);
        assertEquals(5431.42743, engine.convertToDouble(AMOUNT, "celsius heat unit", "joule", PLACES), 0.0);
        assertEquals(0.0286, engine.convertToDouble(AMOUNT, "centijoule", "joule", PLACES), 0.0);
        assertEquals(7572695.13, engine.convertToDouble(AMOUNT, "cheval vapeur heure", "joule", PLACES), 0.0);
        assertEquals(0.286, engine.convertToDouble(AMOUNT, "decijoule", "joule", PLACES), 0.0);
        assertEquals(28.6, engine.convertToDouble(AMOUNT, "dekajoule", "joule", PLACES), 0.0);
        assertEquals(102960.0, engine.convertToDouble(AMOUNT, "dekawatt hour", "joule", PLACES), 0.0);
        assertEquals(3017463020.0, engine.convertToDouble(AMOUNT, "dekatherm", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "electronvolt", "joule", PLACES), 0.0);
        assertEquals(3e-07, engine.convertToDouble(AMOUNT, "erg", "joule", PLACES), 0.0);
        assertEquals(2.86e+18, engine.convertToDouble(AMOUNT, "exajoule", "joule", PLACES), 0.0);
        assertEquals(1.0296e+22, engine.convertToDouble(AMOUNT, "exawatt hour", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "femtojoule", "joule", PLACES), 0.0);
        assertEquals(3.8776392, engine.convertToDouble(AMOUNT, "foot pound", "joule", PLACES), 0.0);
        assertEquals(0.1205207, engine.convertToDouble(AMOUNT, "foot poundal", "joule", PLACES), 0.0);
        assertEquals(376833600.0, engine.convertToDouble(AMOUNT, "gallons", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "gigaelectronvolt", "joule", PLACES), 0.0);
        assertEquals(11974248000.0, engine.convertToDouble(AMOUNT, "gigacalorie", "joule", PLACES), 0.0);
        assertEquals(2860000000.0, engine.convertToDouble(AMOUNT, "gigajoule", "joule", PLACES), 0.0);
        assertEquals(10296000000000.0, engine.convertToDouble(AMOUNT, "gigawatt hour", "joule", PLACES), 0.0);
        assertEquals(11.971388, engine.convertToDouble(AMOUNT, "gram calorie", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "hartree", "joule", PLACES), 0.0);
        assertEquals(286.0, engine.convertToDouble(AMOUNT, "hectojoule", "joule", PLACES), 0.0);
        assertEquals(1029600.0, engine.convertToDouble(AMOUNT, "hectowatt hour", "joule", PLACES), 0.0);
        assertEquals(7677725.77, engine.convertToDouble(AMOUNT, "horsepower hour", "joule", PLACES), 0.0);
        assertEquals(310939200.0, engine.convertToDouble(AMOUNT, "hundred cubic foot of natural gas", "joule", PLACES), 0.0);
        assertEquals(0.020196, engine.convertToDouble(AMOUNT, "inch ounce", "joule", PLACES), 0.0);
        assertEquals(0.3231366, engine.convertToDouble(AMOUNT, "inch pound", "joule", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "joule", "joule", PLACES), 0.0);
        assertEquals(11974.248, engine.convertToDouble(AMOUNT, "kilocalorie", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "kiloelectronvolt", "joule", PLACES), 0.0);
        assertEquals(11971.388, engine.convertToDouble(AMOUNT, "kilogram calorie", "joule", PLACES), 0.0);
        assertEquals(28.047019, engine.convertToDouble(AMOUNT, "kilogram force meter", "joule", PLACES), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "kilojoule", "joule", PLACES), 0.0);
        assertEquals(28.047019, engine.convertToDouble(AMOUNT, "kilopond meter", "joule", PLACES), 0.0);
        assertEquals(11966240000000.0, engine.convertToDouble(AMOUNT, "kiloton", "joule", PLACES), 0.0);
        assertEquals(10296000.0, engine.convertToDouble(AMOUNT, "kilowatt hour", "joule", PLACES), 0.0);
        assertEquals(289.7895, engine.convertToDouble(AMOUNT, "liter atmosphere", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "megaelectronvolt", "joule", PLACES), 0.0);
        assertEquals(11974248.0, engine.convertToDouble(AMOUNT, "megacalorie", "joule", PLACES), 0.0);
        assertEquals(2860000.0, engine.convertToDouble(AMOUNT, "megajoule", "joule", PLACES), 0.0);
        assertEquals(0.286, engine.convertToDouble(AMOUNT, "megalerg", "joule", PLACES), 0.0);
        assertEquals(1.196624e+16, engine.convertToDouble(AMOUNT, "megaton", "joule", PLACES), 0.0);
        assertEquals(10296000000.0, engine.convertToDouble(AMOUNT, "megawatthour", "joule", PLACES), 0.0);
        assertEquals(28.047019, engine.convertToDouble(AMOUNT, "meter kilogram force", "joule", PLACES), 0.0);
        assertEquals(2.9e-06, engine.convertToDouble(AMOUNT, "microjoule", "joule", PLACES), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "millijoule", "joule", PLACES), 0.0);
        assertEquals(102960000.0, engine.convertToDouble(AMOUNT, "myriawatt hour", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "nanojoule", "joule", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "newton meter", "joule", PLACES), 0.0);
        assertEquals(2860000000000000.0, engine.convertToDouble(AMOUNT, "petajoule", "joule", PLACES), 0.0);
        assertEquals(1.0296e+19, engine.convertToDouble(AMOUNT, "petawatthour", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "picojoule", "joule", PLACES), 0.0);
        assertEquals(3.017459874E21, engine.convertToDouble(AMOUNT, "Q unit", "joule", PLACES), 0.0);
        assertEquals(3.017459874e+18, engine.convertToDouble(AMOUNT, "quad", "joule", PLACES), 0.0);
        assertEquals(5e-07, engine.convertToDouble(AMOUNT, "teraelectronvolt", "joule", PLACES), 0.0);
        assertEquals(2860000000000.0, engine.convertToDouble(AMOUNT, "terajoule", "joule", PLACES), 0.0);
        assertEquals(1.0296e+16, engine.convertToDouble(AMOUNT, "terawatthour", "joule", PLACES), 0.0);
        assertEquals(11971388.0, engine.convertToDouble(AMOUNT, "thermie", "joule", PLACES), 0.0);
        assertEquals(11966240000.0, engine.convertToDouble(AMOUNT, "ton", "joule", PLACES), 0.0);
        assertEquals(83819736000.0, engine.convertToDouble(AMOUNT, "tonne of coal", "joule", PLACES), 0.0);
        assertEquals(119742480000.0, engine.convertToDouble(AMOUNT, "tonne of oil", "joule", PLACES), 0.0);
        assertEquals(10296.0, engine.convertToDouble(AMOUNT, "watthour", "joule", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "wattsecond", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "yoctojoule", "joule", PLACES), 0.0);
        assertEquals(2.86e+24, engine.convertToDouble(AMOUNT, "yottajoule", "joule", PLACES), 0.0);
        assertEquals(1.0296E28, engine.convertToDouble(AMOUNT, "yottawatthour", "joule", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "zeptojoule", "joule", PLACES), 0.0);
        assertEquals(2.86e+21, engine.convertToDouble(AMOUNT, "zettajoule", "joule", PLACES), 0.0);
        assertEquals(1.0296e+25, engine.convertToDouble(AMOUNT, "zettawatthour", "joule", PLACES), 0.0);
    }

    @Test
    public void testAliases() {
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "wh", "mlwh", PLACES), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "j", "mlj", PLACES), 0.0);
        assertEquals(1.0296E19, engine.convertToDouble(AMOUNT, "mwh", "nj", PLACES), 0.0);
        assertEquals(2.9E-6, engine.convertToDouble(AMOUNT, "nm", "mj", PLACES), 0.0);
    }

    @Test
    public void testUnitGroup() {
        ConversionResult conversion = engine.convert(AMOUNT, "calories", "joules");
        assertEquals("energy", conversion.groupName());
    }

    @Test
    public void testInvalidConversion() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.convertToDouble(AMOUNT, "gigajoule", "watts");
        });
    }
}