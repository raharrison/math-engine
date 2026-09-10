package uk.co.ryanharrison.mathengine.unitconversion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionEngineDigitalStorageTest extends ConversionEngineBaseTest {

    @Test
    public void testDigitalStorage() {
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Bit", "Bits"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Bit", "Kilobits", PLACES), 0.0);
        assertEquals(2.86E-6, engine.convertToDouble(AMOUNT, "Bit", "Megabits"), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Bit", "Gigabits", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Bit", "Terabits", PLACES), 0.0);
        assertEquals(0.3575, engine.convertToDouble(AMOUNT, "Bit", "Bytes"), 0.0);
        assertEquals(0.0003575, engine.convertToDouble(AMOUNT, "Bit", "Kilobytes", PLACES), 0.0);
        assertEquals(3.575E-7, engine.convertToDouble(AMOUNT, "Bit", "Megabytes", PLACES + 3), 0.0);
        assertEquals(3.575E-10, engine.convertToDouble(AMOUNT, "Bit", "Gigabytes"), 0.0);
        assertEquals(3.575E-13, engine.convertToDouble(AMOUNT, "Bit", "Terabytes"), 0.0);
        assertEquals(2860, engine.convertToDouble(AMOUNT, "Kilobit", "Bits"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Kilobit", "Kilobits"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Kilobit", "Megabits", PLACES), 0.0);
        assertEquals(2.86E-6, engine.convertToDouble(AMOUNT, "Kilobit", "Gigabits"), 0.0);
        assertEquals(2.86E-9, engine.convertToDouble(AMOUNT, "Kilobit", "Terabits"), 0.0);
        assertEquals(357.5, engine.convertToDouble(AMOUNT, "Kilobit", "Bytes"), 0.0);
        assertEquals(0.3575, engine.convertToDouble(AMOUNT, "Kilobit", "Kilobytes"), 0.0);
        assertEquals(3.575E-4, engine.convertToDouble(AMOUNT, "Kilobit", "Megabytes"), 0.0);
        assertEquals(3.575E-7, engine.convertToDouble(AMOUNT, "Kilobit", "Gigabytes"), 0.0);
        assertEquals(3.575E-10, engine.convertToDouble(AMOUNT, "Kilobit", "Terabytes"), 0.0);
        assertEquals(2860000.0, engine.convertToDouble(AMOUNT, "Megabit", "Bits"), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Megabit", "Kilobits"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Megabit", "Megabits"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Megabit", "Gigabits"), 0.0);
        assertEquals(2.86E-6, engine.convertToDouble(AMOUNT, "Megabit", "Terabits"), 0.0);
        assertEquals(357500.0, engine.convertToDouble(AMOUNT, "Megabit", "Bytes"), 0.0);
        assertEquals(357.5, engine.convertToDouble(AMOUNT, "Megabit", "Kilobytes"), 0.0);
        assertEquals(0.3575, engine.convertToDouble(AMOUNT, "Megabit", "Megabytes"), 0.0);
        assertEquals(3.575E-4, engine.convertToDouble(AMOUNT, "Megabit", "Gigabytes"), 0.0);
        assertEquals(3.575E-7, engine.convertToDouble(AMOUNT, "Megabit", "Terabytes"), 0.0);
        assertEquals(2.86E9, engine.convertToDouble(AMOUNT, "Gigabit", "Bits"), 0.0);
        assertEquals(2860000, engine.convertToDouble(AMOUNT, "Gigabit", "Kilobits"), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Gigabit", "Megabits"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Gigabit", "Gigabits"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Gigabit", "Terabits"), 0.0);
        assertEquals(3.575E8, engine.convertToDouble(AMOUNT, "Gigabit", "Bytes"), 0.0);
        assertEquals(357500.0, engine.convertToDouble(AMOUNT, "Gigabit", "Kilobytes"), 0.0);
        assertEquals(357.5, engine.convertToDouble(AMOUNT, "Gigabit", "Megabytes"), 0.0);
        assertEquals(0.3575, engine.convertToDouble(AMOUNT, "Gigabit", "Gigabytes"), 0.0);
        assertEquals(3.575E-4, engine.convertToDouble(AMOUNT, "Gigabit", "Terabytes"), 0.0);
        assertEquals(2.86E12, engine.convertToDouble(AMOUNT, "Terabit", "Bits"), 0.0);
        assertEquals(2.86E9, engine.convertToDouble(AMOUNT, "Terabit", "Kilobits"), 0.0);
        assertEquals(2860000.0, engine.convertToDouble(AMOUNT, "Terabit", "Megabits"), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Terabit", "Gigabits"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Terabit", "Terabits"), 0.0);
        assertEquals(3.575E11, engine.convertToDouble(AMOUNT, "Terabit", "Bytes"), 0.0);
        assertEquals(3.575E8, engine.convertToDouble(AMOUNT, "Terabit", "Kilobytes"), 0.0);
        assertEquals(357500.0, engine.convertToDouble(AMOUNT, "Terabit", "Megabytes"), 0.0);
        assertEquals(357.5, engine.convertToDouble(AMOUNT, "Terabit", "Gigabytes"), 0.0);
        assertEquals(0.3575, engine.convertToDouble(AMOUNT, "Terabit", "Terabytes"), 0.0);
        assertEquals(22.88, engine.convertToDouble(AMOUNT, "Byte", "Bits"), 0.0);
        assertEquals(0.02288, engine.convertToDouble(AMOUNT, "Byte", "Kilobits"), 0.0);
        assertEquals(2.288E-5, engine.convertToDouble(AMOUNT, "Byte", "Megabits"), 0.0);
        assertEquals(2.288E-8, engine.convertToDouble(AMOUNT, "Byte", "Gigabits"), 0.0);
        assertEquals(2.288E-11, engine.convertToDouble(AMOUNT, "Byte", "Terabits"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Byte", "Bytes"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Byte", "Kilobytes"), 0.0);
        assertEquals(2.86E-6, engine.convertToDouble(AMOUNT, "Byte", "Megabytes"), 0.0);
        assertEquals(2.86E-9, engine.convertToDouble(AMOUNT, "Byte", "Gigabytes"), 0.0);
        assertEquals(2.86E-12, engine.convertToDouble(AMOUNT, "Byte", "Terabytes"), 0.0);
        assertEquals(22880.0, engine.convertToDouble(AMOUNT, "Kilobyte", "Bits"), 0.0);
        assertEquals(22.88, engine.convertToDouble(AMOUNT, "Kilobyte", "Kilobits"), 0.0);
        assertEquals(0.02288, engine.convertToDouble(AMOUNT, "Kilobyte", "Megabits"), 0.0);
        assertEquals(2.288E-5, engine.convertToDouble(AMOUNT, "Kilobyte", "Gigabits"), 0.0);
        assertEquals(2.288E-8, engine.convertToDouble(AMOUNT, "Kilobyte", "Terabits"), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Kilobyte", "Bytes", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Kilobyte", "Kilobytes"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Kilobyte", "Megabytes"), 0.0);
        assertEquals(2.86E-6, engine.convertToDouble(AMOUNT, "Kilobyte", "Gigabytes"), 0.0);
        assertEquals(2.86E-9, engine.convertToDouble(AMOUNT, "Kilobyte", "Terabytes"), 0.0);
        assertEquals(2.288E7, engine.convertToDouble(AMOUNT, "Megabyte", "Bits"), 0.0);
        assertEquals(22880.0, engine.convertToDouble(AMOUNT, "Megabyte", "Kilobits"), 0.0);
        assertEquals(22.88, engine.convertToDouble(AMOUNT, "Megabyte", "Megabits"), 0.0);
        assertEquals(0.02288, engine.convertToDouble(AMOUNT, "Megabyte", "Gigabits"), 0.0);
        assertEquals(2.288E-5, engine.convertToDouble(AMOUNT, "Megabyte", "Terabits"), 0.0);
        assertEquals(2860000.0, engine.convertToDouble(AMOUNT, "Megabyte", "Bytes"), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Megabyte", "Kilobytes"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Megabyte", "Megabytes"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Megabyte", "Gigabytes", PLACES), 0.0);
        assertEquals(2.9E-6, engine.convertToDouble(AMOUNT, "Megabyte", "Terabytes", PLACES), 0.0);
        assertEquals(2.288E10, engine.convertToDouble(AMOUNT, "Gigabyte", "Bits"), 0.0);
        assertEquals(2.288E7, engine.convertToDouble(AMOUNT, "Gigabyte", "Kilobits"), 0.0);
        assertEquals(22880.0, engine.convertToDouble(AMOUNT, "Gigabyte", "Megabits"), 0.0);
        assertEquals(22.88, engine.convertToDouble(AMOUNT, "Gigabyte", "Gigabits"), 0.0);
        assertEquals(0.02288, engine.convertToDouble(AMOUNT, "Gigabyte", "Terabits", PLACES), 0.0);
        assertEquals(2.86E9, engine.convertToDouble(AMOUNT, "Gigabyte", "Bytes"), 0.0);
        assertEquals(2860000.0, engine.convertToDouble(AMOUNT, "Gigabyte", "Kilobytes"), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Gigabyte", "Megabytes"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Gigabyte", "Gigabytes"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Gigabyte", "Terabytes", PLACES), 0.0);
        assertEquals(2.288E13, engine.convertToDouble(AMOUNT, "Terabyte", "Bits"), 0.0);
        assertEquals(2.288E10, engine.convertToDouble(AMOUNT, "Terabyte", "Kilobits"), 0.0);
        assertEquals(2.288E7, engine.convertToDouble(AMOUNT, "Terabyte", "Megabits"), 0.0);
        assertEquals(22880.0, engine.convertToDouble(AMOUNT, "Terabyte", "Gigabits"), 0.0);
        assertEquals(22.88, engine.convertToDouble(AMOUNT, "Terabyte", "Terabits"), 0.0);
        assertEquals(2.86E12, engine.convertToDouble(AMOUNT, "Terabyte", "Bytes"), 0.0);
        assertEquals(2.86E9, engine.convertToDouble(AMOUNT, "Terabyte", "Kilobytes"), 0.0);
        assertEquals(2860000.0, engine.convertToDouble(AMOUNT, "Terabyte", "Megabytes"), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Terabyte", "Gigabytes"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Terabyte", "Terabytes"), 0.0);
    }

    @Test
    public void testAliases() {
        assertEquals(2.86e-9, engine.convertToDouble(AMOUNT, "b", "gb"), 0.0);
        assertEquals(3.575E-7, engine.convertToDouble(AMOUNT, "bts", "mbs"), 0.0);
        assertEquals(2.288e+7, engine.convertToDouble(AMOUNT, "mb", "bt"), 0.0);
        assertEquals(2.86E-6, engine.convertToDouble(AMOUNT, "kbs", "gb"), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "gbs", "tbs"), 0.0);
        assertEquals(2.86e-9, engine.convertToDouble(AMOUNT, "kb", "tb"), 0.0);
    }

    @Test
    public void testUnitGroup() {
        ConversionResult conversion = engine.convert(AMOUNT, "gb", "mb");
        assertEquals("digital storage", conversion.groupName());
    }

    @Test
    public void testInvalidConversion() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.convertToDouble(AMOUNT, "gigabytes", "kikobits");
        });
    }
}