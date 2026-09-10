package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests the type rules of the shared value arithmetic: units, percentages, rounding,
 * modulo and exact powers.
 * <p>
 * This is the single implementation behind both the operators and the built-in
 * functions, so anything asserted here holds for {@code 100 + 10%} and
 * {@code sum(100, 10%)} alike.
 */
class NodeArithmeticRulesTest {

    private static final double TOLERANCE = 1e-9;

    private static UnitDefinition lengthUnit(String name, double toBase) {
        return new UnitDefinition(name, name + "s", "length", name, toBase, 0.0, List.of());
    }

    @Nested
    class Units {

        private final UnitDefinition metre = lengthUnit("meter", 1.0);
        private final UnitDefinition kilometre = lengthUnit("kilometer", 1000.0);

        @Test
        void addingLikeUnitsConvertsToTheLeftUnit() {
            NodeConstant result = NodeUnit.of(1, kilometre).add(NodeUnit.of(500, metre));

            assertThat(result).isInstanceOf(NodeUnit.class);
            assertThat(((NodeUnit) result).getUnit().getName()).isEqualTo("kilometer");
            assertThat(result.doubleValue()).isCloseTo(1.5, within(TOLERANCE));
        }

        @Test
        void addingUnlikeUnitsThrows() {
            UnitDefinition second = new UnitDefinition("second", "seconds", "time", "second", 1.0, 0.0, List.of());

            assertThatThrownBy(() -> NodeUnit.of(1, metre).add(NodeUnit.of(1, second)))
                    .isInstanceOf(TypeError.class)
                    .hasMessageContaining("different types");
        }

        @Test
        void dividingLikeUnitsCancelsThem() {
            NodeConstant result = NodeUnit.of(1, kilometre).divide(NodeUnit.of(500, metre));

            assertThat(result).isInstanceOf(NodeDouble.class);
            assertThat(result.doubleValue()).isCloseTo(2.0, within(TOLERANCE));
        }

        @Test
        void scalingKeepsTheUnit() {
            NodeConstant result = NodeUnit.of(3, metre).multiply(new NodeRational(4));

            assertThat(result).isInstanceOf(NodeUnit.class);
            assertThat(result.doubleValue()).isCloseTo(12.0, within(TOLERANCE));
        }

        @Test
        void moduloKeepsTheUnit() {
            NodeConstant result = NodeUnit.of(10, metre).modulo(new NodeRational(3));

            assertThat(result).isInstanceOf(NodeUnit.class);
            assertThat(result.doubleValue()).isCloseTo(1.0, within(TOLERANCE));
        }

        @Test
        void orderingConvertsFirst() {
            assertThat(NodeUnit.of(1, kilometre).compareTo(NodeUnit.of(500, metre))).isPositive();
            assertThat(NodeUnit.of(500, metre).compareTo(NodeUnit.of(1, kilometre))).isNegative();
        }

        @Test
        void equalityConvertsFirst() {
            assertThat(NodeUnit.of(1, kilometre).equalTo(NodeUnit.of(1000, metre))).isTrue();
            assertThat(NodeUnit.of(1, kilometre).equalTo(NodeUnit.of(999, metre))).isFalse();
        }
    }

    @Nested
    class Percentages {

        @Test
        void addingAPercentTakesThatShareOfTheNumber() {
            assertThat(new NodeRational(100).add(new NodePercent(10)).doubleValue())
                    .isCloseTo(110.0, within(TOLERANCE));
        }

        @Test
        void subtractingAPercentTakesThatShareOfTheNumber() {
            assertThat(new NodeRational(200).subtract(new NodePercent(25)).doubleValue())
                    .isCloseTo(150.0, within(TOLERANCE));
        }

        @Test
        void aRatioOfPercentsIsAPlainNumber() {
            NodeConstant result = new NodePercent(20).divide(new NodePercent(10));

            assertThat(result).isInstanceOf(NodeDouble.class);
            assertThat(result.doubleValue()).isCloseTo(2.0, within(TOLERANCE));
        }

        @Test
        void scalingAPercentKeepsItAPercent() {
            NodeConstant result = new NodePercent(10).multiply(new NodeRational(5));

            assertThat(result).isInstanceOf(NodePercent.class);
            assertThat(((NodePercent) result).getPercentValue()).isCloseTo(50.0, within(TOLERANCE));
        }
    }

    @Nested
    class Rounding {

        @ParameterizedTest
        @CsvSource({
                "-7, 2, -4, -3, -3, -3",
                "7, 2, 3, 4, 4, 3"
        })
        void roundingARationalStaysExact(int numerator, int denominator,
                                         int floor, int ceil, int nearest, int towardsZero) {
            NodeRational value = new NodeRational(numerator, denominator);

            assertThat(value.floor()).isInstanceOf(NodeRational.class);
            assertThat(value.floor().doubleValue()).isCloseTo(floor, within(TOLERANCE));
            assertThat(value.ceil().doubleValue()).isCloseTo(ceil, within(TOLERANCE));
            assertThat(value.round().doubleValue()).isCloseTo(nearest, within(TOLERANCE));
            assertThat(value.truncate().doubleValue()).isCloseTo(towardsZero, within(TOLERANCE));
        }

        @Test
        void roundingBroadcastsOverAVector() {
            NodeVector vector = new NodeVector(new Node[]{new NodeDouble(1.4), new NodeDouble(1.6)});

            NodeConstant result = vector.floor();

            assertThat(result).isInstanceOf(NodeVector.class);
            assertThat(((NodeVector) result).getElement(0).toString()).isEqualTo("1");
            assertThat(((NodeVector) result).getElement(1).toString()).isEqualTo("1");
        }

        @Test
        void roundingKeepsAUnit() {
            NodeConstant result = NodeUnit.of(2.7, lengthUnit("meter", 1.0)).floor();

            assertThat(result).isInstanceOf(NodeUnit.class);
            assertThat(result.doubleValue()).isCloseTo(2.0, within(TOLERANCE));
        }

        @Test
        void absKeepsARationalExact() {
            NodeConstant result = new NodeRational(-1, 3).abs();

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.toString()).isEqualTo("1/3");
        }
    }

    @Nested
    class Modulo {

        @ParameterizedTest
        @CsvSource({
                "7, 3, 1",
                "-7, 3, 2",
                "10, 5, 0",
                "-1, 3, 2"
        })
        void flooredModuloFollowsTheSignOfTheDivisor(int left, int right, int expected) {
            NodeConstant result = new NodeRational(left).modulo(new NodeRational(right));

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.doubleValue()).isCloseTo(expected, within(TOLERANCE));
        }
    }

    @Nested
    class ExactPower {

        @Test
        void anIntegerPowerStaysExact() {
            NodeConstant result = new NodeRational(2).power(new NodeRational(100));

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.toString()).isEqualTo("1267650600228229401496703205376");
        }

        @Test
        void aFractionalPowerFallsBackToDouble() {
            NodeConstant result = new NodeRational(4).power(new NodeDouble(0.5));

            assertThat(result).isInstanceOf(NodeDouble.class);
            assertThat(result.doubleValue()).isCloseTo(2.0, within(TOLERANCE));
        }

        @Test
        void aHugeExponentIsRefusedRatherThanExhaustingMemory() {
            assertThatThrownBy(() -> new NodeRational(2).power(new NodeRational(1_000_000)))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("beyond the exact arithmetic limit");
        }

        @Test
        void aHugeExponentOfOneIsStillCheap() {
            NodeConstant result = new NodeRational(1).power(new NodeRational(1_000_000));

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.doubleValue()).isEqualTo(1.0);
        }
    }
}
