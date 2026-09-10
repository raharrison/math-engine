package uk.co.ryanharrison.mathengine.unitconversion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionEngineTimeTest extends ConversionEngineBaseTest {

    @Test
    public void testTimes() {
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "century", "century", PLACES), 0.0);
        assertEquals(104390.0, engine.convertToDouble(AMOUNT, "century", "day", PLACES), 0.0);
        assertEquals(28.6, engine.convertToDouble(AMOUNT, "century", "decade", PLACES), 0.0);
        assertEquals(7456.4285714, engine.convertToDouble(AMOUNT, "century", "fortnight", PLACES), 0.0);
        assertEquals(2505360.0, engine.convertToDouble(AMOUNT, "century", "hour", PLACES), 0.0);
        assertEquals(1.503216E8, engine.convertToDouble(AMOUNT, "century", "minute", PLACES), 0.0);
        assertEquals(9.019296E9, engine.convertToDouble(AMOUNT, "century", "second", PLACES), 0.0);
        assertEquals(14912.8571429, engine.convertToDouble(AMOUNT, "century", "week", PLACES), 0.0);
        assertEquals(286, engine.convertToDouble(AMOUNT, "century", "year", PLACES), 0.0);
        assertEquals(7.835616438356165E-5, engine.convertToDouble(AMOUNT, "day", "century"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "day", "day", PLACES), 0.0);
        assertEquals(7.835616438356164E-4, engine.convertToDouble(AMOUNT, "day", "decade"), 0.0);
        assertEquals(0.2042857, engine.convertToDouble(AMOUNT, "day", "fortnight", PLACES), 0.0);
        assertEquals(68.64, engine.convertToDouble(AMOUNT, "day", "hour", PLACES), 0.0);
        assertEquals(4118.4, engine.convertToDouble(AMOUNT, "day", "minute", PLACES), 0.0);
        assertEquals(247104, engine.convertToDouble(AMOUNT, "day", "second", PLACES), 0.0);
        assertEquals(0.4085714, engine.convertToDouble(AMOUNT, "day", "week", PLACES), 0.0);
        assertEquals(0.0078356, engine.convertToDouble(AMOUNT, "day", "year", PLACES), 0.0);
        assertEquals(0.286, engine.convertToDouble(AMOUNT, "decade", "century", PLACES), 0.0);
        assertEquals(10439.0, engine.convertToDouble(AMOUNT, "decade", "day", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "decade", "decade", PLACES), 0.0);
        assertEquals(745.6428571, engine.convertToDouble(AMOUNT, "decade", "fortnight", PLACES), 0.0);
        assertEquals(250536.0, engine.convertToDouble(AMOUNT, "decade", "hour", PLACES), 0.0);
        assertEquals(1.503216E7, engine.convertToDouble(AMOUNT, "decade", "minute", PLACES), 0.0);
        assertEquals(9.019296E8, engine.convertToDouble(AMOUNT, "decade", "second", PLACES), 0.0);
        assertEquals(1491.2857143, engine.convertToDouble(AMOUNT, "decade", "week", PLACES), 0.0);
        assertEquals(28.6, engine.convertToDouble(AMOUNT, "decade", "year", PLACES), 0.0);
        assertEquals(0.001097, engine.convertToDouble(AMOUNT, "fortnight", "century", PLACES), 0.0);
        assertEquals(40.04, engine.convertToDouble(AMOUNT, "fortnight", "day", PLACES), 0.0);
        assertEquals(0.0109699, engine.convertToDouble(AMOUNT, "fortnight", "decade", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "fortnight", "fortnight", PLACES), 0.0);
        assertEquals(960.96, engine.convertToDouble(AMOUNT, "fortnight", "hour", PLACES), 0.0);
        assertEquals(57657.6, engine.convertToDouble(AMOUNT, "fortnight", "minute", PLACES), 0.0);
        assertEquals(3459456, engine.convertToDouble(AMOUNT, "fortnight", "second", PLACES), 0.0);
        assertEquals(5.72, engine.convertToDouble(AMOUNT, "fortnight", "week", PLACES), 0.0);
        assertEquals(0.1096986, engine.convertToDouble(AMOUNT, "fortnight", "year", PLACES), 0.0);
        assertEquals(3.3E-6, engine.convertToDouble(AMOUNT, "hour", "century", PLACES), 0.0);
        assertEquals(0.11916666666666667, engine.convertToDouble(AMOUNT, "hour", "day"), 0.0);
        assertEquals(3.26E-5, engine.convertToDouble(AMOUNT, "hour", "decade", PLACES), 0.0);
        assertEquals(0.0085119, engine.convertToDouble(AMOUNT, "hour", "fortnight", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "hour", "hour", PLACES), 0.0);
        assertEquals(171.6, engine.convertToDouble(AMOUNT, "hour", "minute", PLACES), 0.0);
        assertEquals(10296, engine.convertToDouble(AMOUNT, "hour", "second", PLACES), 0.0);
        assertEquals(0.0170238, engine.convertToDouble(AMOUNT, "hour", "week", PLACES), 0.0);
        assertEquals(3.264840182648402E-4, engine.convertToDouble(AMOUNT, "hour", "year"), 0.0);
        assertEquals(5.441400304414003E-8, engine.convertToDouble(AMOUNT, "minute", "century"), 0.0);
        assertEquals(0.0019861111111111112, engine.convertToDouble(AMOUNT, "minute", "day"), 0.0);
        assertEquals(5.441400304414003E-7, engine.convertToDouble(AMOUNT, "minute", "decade"), 0.0);
        assertEquals(0.00014186507936507937, engine.convertToDouble(AMOUNT, "minute", "fortnight"), 0.0);
        assertEquals(0.04766666666666667, engine.convertToDouble(AMOUNT, "minute", "hour"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "minute", "minute", PLACES), 0.0);
        assertEquals(171.6, engine.convertToDouble(AMOUNT, "minute", "second", PLACES), 0.0);
        assertEquals(0.00028373015873015873, engine.convertToDouble(AMOUNT, "minute", "week"), 0.0);
        assertEquals(5.441400304414003E-6, engine.convertToDouble(AMOUNT, "minute", "year"), 0.0);
        assertEquals(9.069000507356672E-10, engine.convertToDouble(AMOUNT, "second", "century"), 0.0);
        assertEquals(3.3101851851851855E-5, engine.convertToDouble(AMOUNT, "second", "day"), 0.0);
        assertEquals(9.069000507356672E-9, engine.convertToDouble(AMOUNT, "second", "decade"), 0.0);
        assertEquals(0.0000023644179894179893, engine.convertToDouble(AMOUNT, "second", "fortnight"), 0.0);
        assertEquals(0.0007944444444444444, engine.convertToDouble(AMOUNT, "second", "hour"), 0.0);
        assertEquals(0.04766666666666667, engine.convertToDouble(AMOUNT, "second", "minute"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "second", "second", PLACES), 0.0);
        assertEquals(0.0000047288359788359785, engine.convertToDouble(AMOUNT, "second", "week"), 0.0);
        assertEquals(9.069000507356672E-8, engine.convertToDouble(AMOUNT, "second", "year"), 0.0);
        assertEquals(5.484931506849315E-4, engine.convertToDouble(AMOUNT, "week", "century"), 0.0);
        assertEquals(20.02, engine.convertToDouble(AMOUNT, "week", "day", PLACES), 0.0);
        assertEquals(0.005484931506849315, engine.convertToDouble(AMOUNT, "week", "decade"), 0.0);
        assertEquals(1.43, engine.convertToDouble(AMOUNT, "week", "fortnight"), 0.0);
        assertEquals(480.48, engine.convertToDouble(AMOUNT, "week", "hour", PLACES), 0.0);
        assertEquals(28828.8, engine.convertToDouble(AMOUNT, "week", "minute", PLACES), 0.0);
        assertEquals(1729728, engine.convertToDouble(AMOUNT, "week", "second", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "week", "week", PLACES), 0.0);
        assertEquals(0.05484931506849315, engine.convertToDouble(AMOUNT, "week", "year"), 0.0);
        assertEquals(0.0286, engine.convertToDouble(AMOUNT, "year", "century"), 0.0);
        assertEquals(1043.9, engine.convertToDouble(AMOUNT, "year", "day", PLACES), 0.0);
        assertEquals(0.286, engine.convertToDouble(AMOUNT, "year", "decade"), 0.0);
        assertEquals(74.56428571428572, engine.convertToDouble(AMOUNT, "year", "fortnight"), 0.0);
        assertEquals(25053.6, engine.convertToDouble(AMOUNT, "year", "hour"), 0.0);
        assertEquals(1503216.0, engine.convertToDouble(AMOUNT, "year", "minute"), 0.0);
        assertEquals(9.019296E7, engine.convertToDouble(AMOUNT, "year", "second"), 0.0);
        assertEquals(149.12857142857143, engine.convertToDouble(AMOUNT, "year", "week"), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "year", "year"), 0.0);

        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Nanosecond", "Nanoseconds", PLACES), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Nanosecond", "Microseconds", PLACES), 0.0);
        assertEquals(0.00000286, engine.convertToDouble(AMOUNT, "Nanosecond", "Milliseconds"), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Nanosecond", "Seconds", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Nanosecond", "Minutes", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Nanosecond", "Hours", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Nanosecond", "Days", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Nanosecond", "Weeks", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Nanosecond", "Months", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Nanosecond", "Years", PLACES), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Microsecond", "Nanoseconds", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Microsecond", "Microseconds", PLACES), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Microsecond", "Milliseconds", PLACES), 0.0);
        assertEquals(2.86E-6, engine.convertToDouble(AMOUNT, "Microsecond", "Seconds"), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Microsecond", "Minutes", PLACES), 0.0);
        assertEquals(7.944444444444445E-10, engine.convertToDouble(AMOUNT, "Microsecond", "Hours"), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Microsecond", "Days", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Microsecond", "Weeks", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Microsecond", "Months", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Microsecond", "Years", PLACES), 0.0);
        assertEquals(2860000, engine.convertToDouble(AMOUNT, "Millisecond", "Nanoseconds", PLACES), 0.0);
        assertEquals(2860.0, engine.convertToDouble(AMOUNT, "Millisecond", "Microseconds", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Millisecond", "Milliseconds", PLACES), 0.0);
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "Millisecond", "Seconds", PLACES), 0.0);
        assertEquals(0.00004766666666666667, engine.convertToDouble(AMOUNT, "Millisecond", "Minutes"), 0.0);
        assertEquals(7.944444444444445e-7, engine.convertToDouble(AMOUNT, "Millisecond", "Hours"), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Millisecond", "Days", PLACES), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Millisecond", "Weeks", PLACES), 0.0);
        assertEquals(1.0882800608828006E-9, engine.convertToDouble(AMOUNT, "Millisecond", "Months"), 0.0);
        assertEquals(0.0, engine.convertToDouble(AMOUNT, "Millisecond", "Years", PLACES), 0.0);
        assertEquals(2.86E9, engine.convertToDouble(AMOUNT, "Second", "Nanoseconds", PLACES), 0.0);
        assertEquals(2860000, engine.convertToDouble(AMOUNT, "Second", "Microseconds", PLACES), 0.0);
        assertEquals(2860, engine.convertToDouble(AMOUNT, "Second", "Milliseconds", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Second", "Seconds", PLACES), 0.0);
        assertEquals(0.0476667, engine.convertToDouble(AMOUNT, "Second", "Minutes", PLACES), 0.0);
        assertEquals(0.0007944444444444444, engine.convertToDouble(AMOUNT, "Second", "Hours"), 0.0);
        assertEquals(3.3101851851851855E-5, engine.convertToDouble(AMOUNT, "Second", "Days"), 0.0);
        assertEquals(0.0000047288359788359785, engine.convertToDouble(AMOUNT, "Second", "Weeks"), 0.0);
        assertEquals(1.0882800608828005E-6, engine.convertToDouble(AMOUNT, "Second", "Months"), 0.0);
        assertEquals(1.0E-7, engine.convertToDouble(AMOUNT, "Second", "Years", PLACES), 0.0);
        assertEquals(1.716E11, engine.convertToDouble(AMOUNT, "Minute", "Nanoseconds", PLACES), 0.0);
        assertEquals(171600000, engine.convertToDouble(AMOUNT, "Minute", "Microseconds", PLACES), 0.0);
        assertEquals(171600, engine.convertToDouble(AMOUNT, "Minute", "Milliseconds", PLACES), 0.0);
        assertEquals(171.6, engine.convertToDouble(AMOUNT, "Minute", "Seconds", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Minute", "Minutes", PLACES), 0.0);
        assertEquals(0.04766666666666667, engine.convertToDouble(AMOUNT, "Minute", "Hours"), 0.0);
        assertEquals(0.0019861111111111112, engine.convertToDouble(AMOUNT, "Minute", "Days"), 0.0);
        assertEquals(0.00028373015873015873, engine.convertToDouble(AMOUNT, "Minute", "Weeks"), 0.0);
        assertEquals(6.529680365296803E-5, engine.convertToDouble(AMOUNT, "Minute", "Months"), 0.0);
        assertEquals(5.441400304414003E-6, engine.convertToDouble(AMOUNT, "Minute", "Years"), 0.0);
        assertEquals(10296000000000.0, engine.convertToDouble(AMOUNT, "Hour", "Nanoseconds", PLACES), 0.0);
        assertEquals(10296000000.0, engine.convertToDouble(AMOUNT, "Hour", "Microseconds", PLACES), 0.0);
        assertEquals(10296000, engine.convertToDouble(AMOUNT, "Hour", "Milliseconds", PLACES), 0.0);
        assertEquals(10296, engine.convertToDouble(AMOUNT, "Hour", "Seconds", PLACES), 0.0);
        assertEquals(171.6, engine.convertToDouble(AMOUNT, "Hour", "Minutes", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Hour", "Hours", PLACES), 0.0);
        assertEquals(0.1191667, engine.convertToDouble(AMOUNT, "Hour", "Days", PLACES), 0.0);
        assertEquals(0.017023809523809524, engine.convertToDouble(AMOUNT, "Hour", "Weeks"), 0.0);
        assertEquals(0.003917808219178082, engine.convertToDouble(AMOUNT, "Hour", "Months"), 0.0);
        assertEquals(3.264840182648402E-4, engine.convertToDouble(AMOUNT, "Hour", "Years"), 0.0);
        assertEquals(247104000000000.0, engine.convertToDouble(AMOUNT, "Day", "Nanoseconds", PLACES), 0.0);
        assertEquals(247104000000.0, engine.convertToDouble(AMOUNT, "Day", "Microseconds", PLACES), 0.0);
        assertEquals(247104000, engine.convertToDouble(AMOUNT, "Day", "Milliseconds", PLACES), 0.0);
        assertEquals(247104, engine.convertToDouble(AMOUNT, "Day", "Seconds", PLACES), 0.0);
        assertEquals(4118.4, engine.convertToDouble(AMOUNT, "Day", "Minutes", PLACES), 0.0);
        assertEquals(68.64, engine.convertToDouble(AMOUNT, "Day", "Hours", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Day", "Days", PLACES), 0.0);
        assertEquals(0.4085714285714286, engine.convertToDouble(AMOUNT, "Day", "Weeks"), 0.0);
        assertEquals(0.0940274, engine.convertToDouble(AMOUNT, "Day", "Months", PLACES), 0.0);
        assertEquals(0.007835616438356164, engine.convertToDouble(AMOUNT, "Day", "Years"), 0.0);
        assertEquals(1729728000000000.0, engine.convertToDouble(AMOUNT, "Week", "Nanoseconds", PLACES), 0.0);
        assertEquals(1729728000000.0, engine.convertToDouble(AMOUNT, "Week", "Microseconds", PLACES), 0.0);
        assertEquals(1729728000, engine.convertToDouble(AMOUNT, "Week", "Milliseconds", PLACES), 0.0);
        assertEquals(1729728, engine.convertToDouble(AMOUNT, "Week", "Seconds", PLACES), 0.0);
        assertEquals(28828.8, engine.convertToDouble(AMOUNT, "Week", "Minutes", PLACES), 0.0);
        assertEquals(480.48, engine.convertToDouble(AMOUNT, "Week", "Hours", PLACES), 0.0);
        assertEquals(20.02, engine.convertToDouble(AMOUNT, "Week", "Days", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Week", "Weeks", PLACES), 0.0);
        assertEquals(0.6581917808219178, engine.convertToDouble(AMOUNT, "Week", "Months"), 0.0);
        assertEquals(0.0548493, engine.convertToDouble(AMOUNT, "Week", "Years", PLACES), 0.0);
        assertEquals(7.51608E15, engine.convertToDouble(AMOUNT, "Month", "Nanoseconds", PLACES), 0.0);
        assertEquals(7.51608E12, engine.convertToDouble(AMOUNT, "Month", "Microseconds", PLACES), 0.0);
        assertEquals(7.51608E9, engine.convertToDouble(AMOUNT, "Month", "Milliseconds", PLACES), 0.0);
        assertEquals(7516080.0, engine.convertToDouble(AMOUNT, "Month", "Seconds", PLACES), 0.0);
        assertEquals(125268.0, engine.convertToDouble(AMOUNT, "Month", "Minutes", PLACES), 0.0);
        assertEquals(2087.8, engine.convertToDouble(AMOUNT, "Month", "Hours", PLACES), 0.0);
        assertEquals(86.9916667, engine.convertToDouble(AMOUNT, "Month", "Days", PLACES), 0.0);
        assertEquals(12.427381, engine.convertToDouble(AMOUNT, "Month", "Weeks", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Month", "Months", PLACES), 0.0);
        assertEquals(0.2383333, engine.convertToDouble(AMOUNT, "Month", "Years", PLACES), 0.0);
        assertEquals(9.019296E16, engine.convertToDouble(AMOUNT, "Year", "Nanoseconds", PLACES), 0.0);
        assertEquals(9.019296E13, engine.convertToDouble(AMOUNT, "Year", "Microseconds", PLACES), 0.0);
        assertEquals(9.019296E10, engine.convertToDouble(AMOUNT, "Year", "Milliseconds", PLACES), 0.0);
        assertEquals(9.019296E7, engine.convertToDouble(AMOUNT, "Year", "Seconds", PLACES), 0.0);
        assertEquals(1503216.0, engine.convertToDouble(AMOUNT, "Year", "Minutes", PLACES), 0.0);
        assertEquals(25053.6, engine.convertToDouble(AMOUNT, "Year", "Hours", PLACES), 0.0);
        assertEquals(1043.9, engine.convertToDouble(AMOUNT, "Year", "Days", PLACES), 0.0);
        assertEquals(149.1285714, engine.convertToDouble(AMOUNT, "Year", "Weeks", PLACES), 0.0);
        assertEquals(34.32, engine.convertToDouble(AMOUNT, "Year", "Months", PLACES), 0.0);
        assertEquals(2.86, engine.convertToDouble(AMOUNT, "Year", "Years", PLACES), 0.0);
    }

    @Test
    public void testAliases() {
        assertEquals(0.00286, engine.convertToDouble(AMOUNT, "ms", "secs", PLACES), 0.0);
        assertEquals(0.0476667, engine.convertToDouble(AMOUNT, "min", "hrs", PLACES), 0.0);
        assertEquals(10296.0, engine.convertToDouble(AMOUNT, "hr", "s"), 0.0);
        assertEquals(2.86E-6, engine.convertToDouble(AMOUNT, "ns", "ms"), 0.0);
        assertEquals(5.441400304414003E-6, engine.convertToDouble(AMOUNT, "mins", "yrs"), 0.0);
        assertEquals(34.32, engine.convertToDouble(AMOUNT, "yr", "months"), 0.0);
        assertEquals(20.02, engine.convertToDouble(AMOUNT, "wks", "days"), 0.0);
    }

    @Test
    public void testUnitGroup() {
        ConversionResult conversion = engine.convert(AMOUNT, "minutes", "hours");
        assertEquals("time", conversion.groupName());
    }

    @Test
    public void testInvalidConversion() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.convertToDouble(AMOUNT, "minutes", "meter");
        });
    }
}