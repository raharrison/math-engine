package uk.co.ryanharrison.mathengine.unitconversion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionEngineAngleTest extends ConversionEngineBaseTest {

    @Test
    public void testAngles() {
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "degree", "degree"), 0.0);
        assertEquals(3.1777777777777776, engine.convertToDouble(AMOUNT, "degree", "gradian"), 0.0);
        assertEquals(0.0499164166072, engine.convertToDouble(AMOUNT, "degree", "radian"), 0.0);
        assertEquals(0.007944444444470256, engine.convertToDouble(AMOUNT, "degree", "rotation"), 0.0);
        assertEquals(2.574, engine.convertToDouble(AMOUNT, "gradian", "degree"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "gradian", "gradian"), 0.0);
        assertEquals(0.04492477494648, engine.convertToDouble(AMOUNT, "gradian", "radian"), 0.0);
        assertEquals(0.007150000000023231, engine.convertToDouble(AMOUNT, "gradian", "rotation"), 0.0);
        assertEquals(163.86592940688305, engine.convertToDouble(AMOUNT, "radian", "degree"), 0.0);
        assertEquals(182.07325489653672, engine.convertToDouble(AMOUNT, "radian", "gradian"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "radian", "radian"), 0.0);
        assertEquals(0.45518313724282067, engine.convertToDouble(AMOUNT, "radian", "rotation"), 0.0);
        assertEquals(1029.5999999966548, engine.convertToDouble(AMOUNT, "rotation", "degree"), 0.0);
        assertEquals(1143.9999999962831, engine.convertToDouble(AMOUNT, "rotation", "gradian"), 0.0);
        assertEquals(17.969909978533618, engine.convertToDouble(AMOUNT, "rotation", "radian"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "rotation", "rotation"), 0.0);
    }

    @Test
    public void testAliases() {
        assertEquals(182.07325489653672, engine.convertToDouble(AMOUNT, "rads", "grads"), 0.0);
        assertEquals(3.1777777777777776, engine.convertToDouble(AMOUNT, "degs", "g"), 0.0);
        assertEquals(17.969909978533618, engine.convertToDouble(AMOUNT, "circles", "rads"), 0.0);
        assertEquals(182.07325489653672, engine.convertToDouble(AMOUNT, "rad", "grads"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "deg", "d"), 0.0);
        assertEquals(0.45518313724282067, engine.convertToDouble(AMOUNT, "r", "fc"), 0.0);
    }

    @Test
    public void testUnitGroup() {
        ConversionResult conversion = engine.convert(AMOUNT, "radians", "degrees");
        assertEquals("angles", conversion.groupName());
    }

    @Test
    public void testInvalidConversion() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.convertToDouble(AMOUNT, "radians", "bits");
        });
    }
}