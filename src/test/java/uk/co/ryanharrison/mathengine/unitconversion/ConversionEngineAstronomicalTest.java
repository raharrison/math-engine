package uk.co.ryanharrison.mathengine.unitconversion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionEngineAstronomicalTest extends ConversionEngineBaseTest {

    @Test
    public void testAstronomical() {
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "astronomical unit", "astronomical unit", PLACES), 0.0);
        assertEquals(4.2784991017626E8, engine.convertToDouble(AMOUNT, "astronomical unit", "kilometer", PLACES), 0.0);
        assertEquals(1427.1536817, engine.convertToDouble(AMOUNT, "astronomical unit", "light second", PLACES), 0.0);
        assertEquals(23.7858947, engine.convertToDouble(AMOUNT, "astronomical unit", "light minute", PLACES), 0.0);
        assertEquals(0.3964316, engine.convertToDouble(AMOUNT, "astronomical unit", "light hour", PLACES), 0.0);
        assertEquals(0.016518, engine.convertToDouble(AMOUNT, "astronomical unit", "light day", PLACES), 0.0);
        assertEquals(4.52e-05, engine.convertToDouble(AMOUNT, "astronomical unit", "light year", PLACES), 0.0);
        assertEquals(1.39e-05, engine.convertToDouble(AMOUNT, "astronomical unit", "parsec", PLACES), 0.0);
        assertEquals(427849910176.26, engine.convertToDouble(AMOUNT, "astronomical unit", "meter", PLACES), 0.0);
        assertEquals(265853608.7848589, engine.convertToDouble(AMOUNT, "astronomical unit", "mile", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "kilometer", "astronomical unit", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "kilometer", "kilometer", PLACES), 0.0);
        assertEquals(9.5e-06, engine.convertToDouble(AMOUNT, "kilometer", "light second", PLACES), 0.0);
        assertEquals(2e-07, engine.convertToDouble(AMOUNT, "kilometer", "light minute", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "kilometer", "light hour", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "kilometer", "light day", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "kilometer", "light year", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "kilometer", "parsec", PLACES), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "kilometer", "meter", PLACES), 0.0);
        assertEquals(1.7771216, engine.convertToDouble(AMOUNT, "kilometer", "mile", PLACES), 0.0);
        assertEquals(0.0057314, engine.convertToDouble(AMOUNT, "light second", "astronomical unit", PLACES), 0.0);
        assertEquals(857406.42988, engine.convertToDouble(AMOUNT, "light second", "kilometer", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "light second", "light second", PLACES), 0.0);
        assertEquals(0.0476667, engine.convertToDouble(AMOUNT, "light second", "light minute", PLACES), 0.0);
        assertEquals(0.0007944, engine.convertToDouble(AMOUNT, "light second", "light hour", PLACES), 0.0);
        assertEquals(3.31e-05, engine.convertToDouble(AMOUNT, "light second", "light day", PLACES), 0.0);
        assertEquals(1e-07, engine.convertToDouble(AMOUNT, "light second", "light year", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "light second", "parsec", PLACES), 0.0);
        assertEquals(857406429.88, engine.convertToDouble(AMOUNT, "light second", "meter", PLACES), 0.0);
        assertEquals(532767.6555665, engine.convertToDouble(AMOUNT, "light second", "mile", PLACES), 0.0);
        assertEquals(0.3438845, engine.convertToDouble(AMOUNT, "light minute", "astronomical unit", PLACES), 0.0);
        assertEquals(51444385.7928, engine.convertToDouble(AMOUNT, "light minute", "kilometer", PLACES), 0.0);
        assertEquals(171.6, engine.convertToDouble(AMOUNT, "light minute", "light second", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "light minute", "light minute", PLACES), 0.0);
        assertEquals(0.0476667, engine.convertToDouble(AMOUNT, "light minute", "light hour", PLACES), 0.0);
        assertEquals(0.0019861, engine.convertToDouble(AMOUNT, "light minute", "light day", PLACES), 0.0);
        assertEquals(5.4e-06, engine.convertToDouble(AMOUNT, "light minute", "light year", PLACES), 0.0);
        assertEquals(1.7e-06, engine.convertToDouble(AMOUNT, "light minute", "parsec", PLACES), 0.0);
        assertEquals(5.14443857928E10, engine.convertToDouble(AMOUNT, "light minute", "meter", PLACES), 0.0);
        assertEquals(31966059.3339895, engine.convertToDouble(AMOUNT, "light minute", "mile", PLACES), 0.0);
        assertEquals(20.6330687, engine.convertToDouble(AMOUNT, "light hour", "astronomical unit", PLACES), 0.0);
        assertEquals(3086663147.568, engine.convertToDouble(AMOUNT, "light hour", "kilometer", PLACES), 0.0);
        assertEquals(10296.0, engine.convertToDouble(AMOUNT, "light hour", "light second", PLACES), 0.0);
        assertEquals(171.6, engine.convertToDouble(AMOUNT, "light hour", "light minute", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "light hour", "light hour", PLACES), 0.0);
        assertEquals(0.1191667, engine.convertToDouble(AMOUNT, "light hour", "light day", PLACES), 0.0);
        assertEquals(0.0003263, engine.convertToDouble(AMOUNT, "light hour", "light year", PLACES), 0.0);
        assertEquals(0.0001, engine.convertToDouble(AMOUNT, "light hour", "parsec", PLACES), 0.0);
        assertEquals(3086663147568.0, engine.convertToDouble(AMOUNT, "light hour", "meter", PLACES), 0.0);
        assertEquals(1917963560.03937, engine.convertToDouble(AMOUNT, "light hour", "mile", PLACES), 0.0);
        assertEquals(495.1936495, engine.convertToDouble(AMOUNT, "light day", "astronomical unit", PLACES), 0.0);
        assertEquals(7.4079915541632E10, engine.convertToDouble(AMOUNT, "light day", "kilometer", PLACES), 0.0);
        assertEquals(247104.0, engine.convertToDouble(AMOUNT, "light day", "light second", PLACES), 0.0);
        assertEquals(4118.4, engine.convertToDouble(AMOUNT, "light day", "light minute", PLACES), 0.0);
        assertEquals(68.64, engine.convertToDouble(AMOUNT, "light day", "light hour", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "light day", "light day", PLACES), 0.0);
        assertEquals(0.0078303, engine.convertToDouble(AMOUNT, "light day", "light year", PLACES), 0.0);
        assertEquals(0.0024008, engine.convertToDouble(AMOUNT, "light day", "parsec", PLACES), 0.0);
        assertEquals(74079915541632.0, engine.convertToDouble(AMOUNT, "light day", "meter", PLACES), 0.0);
        assertEquals(4.6031125440944885E10, engine.convertToDouble(AMOUNT, "light day", "mile", PLACES), 0.0);
        assertEquals(180869.4804719, engine.convertToDouble(AMOUNT, "light year", "astronomical unit", PLACES), 0.0);
        assertEquals(2.705768915158109E13, engine.convertToDouble(AMOUNT, "light year", "kilometer", PLACES), 0.0);
        assertEquals(90254736.0, engine.convertToDouble(AMOUNT, "light year", "light second", PLACES), 0.0);
        assertEquals(1504245.6, engine.convertToDouble(AMOUNT, "light year", "light minute", PLACES), 0.0);
        assertEquals(25070.76, engine.convertToDouble(AMOUNT, "light year", "light hour", PLACES), 0.0);
        assertEquals(1044.615, engine.convertToDouble(AMOUNT, "light year", "light day", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "light year", "light year", PLACES), 0.0);
        assertEquals(0.87688, engine.convertToDouble(AMOUNT, "light year", "parsec", PLACES), 0.0);
        assertEquals(2.705768915158109e+16, engine.convertToDouble(AMOUNT, "light year", "meter", PLACES), 0.0);
        assertEquals(16812868567305.117, engine.convertToDouble(AMOUNT, "light year", "mile", PLACES), 0.0);
        assertEquals(589917.328, engine.convertToDouble(AMOUNT, "parsec", "astronomical unit", PLACES), 0.0);
        assertEquals(88250376152524.22, engine.convertToDouble(AMOUNT, "parsec", "kilometer", PLACES), 0.0);
        assertEquals(294371568.7221332, engine.convertToDouble(AMOUNT, "parsec", "light second", PLACES), 0.0);
        assertEquals(4906192.8120356, engine.convertToDouble(AMOUNT, "parsec", "light minute", PLACES), 0.0);
        assertEquals(81769.8802006, engine.convertToDouble(AMOUNT, "parsec", "light hour", PLACES), 0.0);
        assertEquals(3407.0783417, engine.convertToDouble(AMOUNT, "parsec", "light day", PLACES), 0.0);
        assertEquals(9.3280721, engine.convertToDouble(AMOUNT, "parsec", "light year", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "parsec", "parsec", PLACES), 0.0);
        assertEquals(8.825037615252422e+16, engine.convertToDouble(AMOUNT, "parsec", "meter", PLACES), 0.0);
        assertEquals(54836241445287.164, engine.convertToDouble(AMOUNT, "parsec", "mile", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "meter", "astronomical unit", PLACES), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "meter", "kilometer", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "meter", "light second", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "meter", "light minute", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "meter", "light hour", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "meter", "light day", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "meter", "light year", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "meter", "parsec", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "meter", "meter", PLACES), 0.0);
        assertEquals(0.0017771, engine.convertToDouble(AMOUNT, "meter", "mile", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "mile", "astronomical unit", PLACES), 0.0);
        assertEquals(4.6027238, engine.convertToDouble(AMOUNT, "mile", "kilometer", PLACES), 0.0);
        assertEquals(1.54e-05, engine.convertToDouble(AMOUNT, "mile", "light second", PLACES), 0.0);
        assertEquals(3e-07, engine.convertToDouble(AMOUNT, "mile", "light minute", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "mile", "light hour", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "mile", "light day", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "mile", "light year", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "mile", "parsec", PLACES), 0.0);
        assertEquals(4602.72384, engine.convertToDouble(AMOUNT, "mile", "meter", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "mile", "mile", PLACES), 0.0);
    }

    @Test
    public void testAliases() {
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "km", "m", PLACES), 0.0);
        assertEquals(2.943715687221332E8, engine.convertToDouble(AMOUNT, "psec", "light sec", PLACES), 0.0);
        assertEquals(1504245.6, engine.convertToDouble(AMOUNT, "ly", "light min", PLACES), 0.0);
    }

    @Test
    public void testUnitGroup() {
        ConversionResult conversion = engine.convert(AMOUNT, "parsec", "miles");
        assertEquals("astronomical", conversion.groupName());
    }

    @Test
    public void testInvalidConversion() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.convertToDouble(AMOUNT, "light second", "feet");
        });
    }
}