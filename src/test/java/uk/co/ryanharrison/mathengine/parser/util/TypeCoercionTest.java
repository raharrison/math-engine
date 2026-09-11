package uk.co.ryanharrison.mathengine.parser.util;

import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.ast.*;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How much exactness a {@code double} arriving from outside is allowed to claim.
 * Every edge of the engine reads doubles through the same method, so this is where the
 * answer is agreed once rather than per caller.
 */
class TypeCoercionTest {

    // ==================== toNumber(double) ====================

    @Test
    void readsAnIntegralDoubleExactly() {
        assertThat(TypeCoercion.toNumber(3.0)).isEqualTo(new NodeRational(3));
        assertThat(TypeCoercion.toNumber(-7.0)).isEqualTo(new NodeRational(-7));
        assertThat(TypeCoercion.toNumber(0.0)).isEqualTo(new NodeRational(0));
    }

    @Test
    void readsAnIntegralDoubleTooBigForALongExactly() {
        // Two to the sixty-third is one past Long.MAX_VALUE, and the cast used to land on
        // Long.MAX_VALUE instead, quietly answering one short of the value it was given.
        double twoToThe63 = 0x1p63;
        BigInteger expected = BigInteger.TWO.pow(63);

        assertThat(TypeCoercion.toNumber(twoToThe63))
                .isEqualTo(new NodeRational(BigRational.of(expected)));
        assertThat(TypeCoercion.toNumber(-twoToThe63))
                .isEqualTo(new NodeRational(BigRational.of(expected.negate())));
    }

    @Test
    void readsADoubleThatAShortDecimalNames() {
        assertThat(TypeCoercion.toNumber(0.1)).isEqualTo(new NodeRational(1, 10));
        assertThat(TypeCoercion.toNumber(0.5)).isEqualTo(new NodeRational(1, 2));
        assertThat(TypeCoercion.toNumber(12.345)).isEqualTo(new NodeRational(2469, 200));
    }

    @Test
    void readsADecimalTooFineForASmallDenominator() {
        // Asking for the closest fraction under a million answers 37/300813 here, which is
        // nearer than 123/1000000 without being equal to it, so the exact value was lost.
        // The shortest decimal that rounds to the double finds it every time.
        assertThat(TypeCoercion.toNumber(0.000123)).isEqualTo(new NodeRational(123, 1000000));
        assertThat(TypeCoercion.toNumber(1e-5)).isEqualTo(new NodeRational(1, 100000));
    }

    @Test
    void readsADoubleThatOnlyAShortFractionNames() {
        // A third needs sixteen digits as a decimal, so only the fraction branch finds it
        assertThat(TypeCoercion.toNumber(1.0 / 3.0)).isEqualTo(new NodeRational(1, 3));
        assertThat(TypeCoercion.toNumber(2.0 / 7.0)).isEqualTo(new NodeRational(2, 7));
    }

    @Test
    void leavesADoubleNoFractionNames() {
        assertThat(TypeCoercion.toNumber(Math.sqrt(2))).isEqualTo(new NodeDouble(Math.sqrt(2)));
        assertThat(TypeCoercion.toNumber(Math.PI)).isEqualTo(new NodeDouble(Math.PI));
        // The classic sum is not a tenth plus a fifth, and must not be dressed up as one
        assertThat(TypeCoercion.toNumber(0.1 + 0.2)).isEqualTo(new NodeDouble(0.1 + 0.2));
    }

    @Test
    void leavesAValueNoRationalHolds() {
        assertThat(TypeCoercion.toNumber(Double.NaN).doubleValue()).isNaN();
        assertThat(TypeCoercion.toNumber(Double.POSITIVE_INFINITY))
                .isEqualTo(new NodeDouble(Double.POSITIVE_INFINITY));
    }

    @Test
    void aPercentageReadsADoubleTheSameWayAPlainNumberDoes() {
        // The two used to answer with different denominator limits
        assertThat(NodePercent.fromDecimal(0.1).getFraction())
                .isEqualTo(TypeCoercion.toNumber(0.1));
        assertThat(NodePercent.fromDecimal(0.000123).getFraction())
                .isEqualTo(TypeCoercion.toNumber(0.000123));
    }

    @Test
    void claimsNoExactnessForAComputedValue() {
        // The guard that stops the shortest-decimal rule swallowing everything: a value
        // needing sixteen or seventeen digits to name itself was calculated, not written.
        for (double computed : new double[]{Math.PI, Math.E, Math.sqrt(2), Math.sqrt(3),
                Math.log(2), Math.sin(1), Math.cbrt(7), Math.exp(1.5), Math.atan(1)}) {
            assertThat(TypeCoercion.toNumber(computed))
                    .as("%s was calculated and must stay a double", computed)
                    .isInstanceOf(NodeDouble.class);
        }
    }

    // ==================== toDisplayString ====================

    @Test
    void showsANumberTheWayAFormatterWould() {
        // str(2.5) answered "5/2" while 2.5 on its own answered 2.5
        assertThat(TypeCoercion.toDisplayString(new NodeRational(5, 2))).isEqualTo("2.5");
        assertThat(TypeCoercion.toDisplayString(new NodeRational(1, 3))).isEqualTo("1/3");
        assertThat(TypeCoercion.toDisplayString(new NodeDouble(1.5))).isEqualTo("1.5");
    }

    @Test
    void showsAStringAndABooleanWithoutDecoration() {
        assertThat(TypeCoercion.toDisplayString(new NodeString("hi"))).isEqualTo("hi");
        assertThat(TypeCoercion.toDisplayString(new NodeBoolean(true))).isEqualTo("true");
    }
}
