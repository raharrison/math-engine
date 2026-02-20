package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for universal arithmetic dispatch on NodeConstant.
 * <p>
 * Verifies that {@code left.add(right)}, {@code left.subtract(right)}, etc.
 * produce correct results across all type combinations including string
 * concatenation/repetition and lexicographic comparison.
 */
class NodeConstantArithmeticTest {

    private static final double TOLERANCE = 1e-9;

    // ==================== Numeric Type Preservation ====================

    @Nested
    class NumericTypePreservation {

        @Test
        void rationalPlusRationalIsRational() {
            NodeConstant left = new NodeRational(1, 2);
            NodeConstant right = new NodeRational(1, 3);
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.doubleValue()).isCloseTo(5.0 / 6.0, within(TOLERANCE));
        }

        @Test
        void rationalTimesRationalIsRational() {
            NodeConstant left = new NodeRational(2, 3);
            NodeConstant right = new NodeRational(3, 4);
            NodeConstant result = left.multiply(right);

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.doubleValue()).isCloseTo(0.5, within(TOLERANCE));
        }

        @Test
        void rationalMinusRationalIsRational() {
            NodeConstant left = new NodeRational(3, 4);
            NodeConstant right = new NodeRational(1, 4);
            NodeConstant result = left.subtract(right);

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.doubleValue()).isCloseTo(0.5, within(TOLERANCE));
        }

        @Test
        void rationalDividedByRationalIsRational() {
            NodeConstant left = new NodeRational(1, 2);
            NodeConstant right = new NodeRational(1, 3);
            NodeConstant result = left.divide(right);

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.doubleValue()).isCloseTo(1.5, within(TOLERANCE));
        }

        @Test
        void rationalToIntegerPowerIsRational() {
            NodeConstant base = new NodeRational(2, 3);
            NodeConstant exp = new NodeRational(3, 1);
            NodeConstant result = base.power(exp);

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.doubleValue()).isCloseTo(8.0 / 27.0, within(TOLERANCE));
        }

        @Test
        void doublePlusRationalIsDouble() {
            NodeConstant left = new NodeDouble(1.5);
            NodeConstant right = new NodeRational(1, 2);
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeDouble.class);
            assertThat(result.doubleValue()).isCloseTo(2.0, within(TOLERANCE));
        }

        @Test
        void percentPlusPercentIsPercent() {
            NodeConstant left = new NodePercent(10); // 10%
            NodeConstant right = new NodePercent(20); // 20%
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodePercent.class);
            assertThat(result.doubleValue()).isCloseTo(0.3, within(TOLERANCE));
        }

        @Test
        void percentTimesPercentIsPercent() {
            NodeConstant left = new NodePercent(50); // 50% = 0.5
            NodeConstant right = new NodePercent(50); // 50% = 0.5
            NodeConstant result = left.multiply(right);

            assertThat(result).isInstanceOf(NodePercent.class);
            assertThat(result.doubleValue()).isCloseTo(0.25, within(TOLERANCE));
        }

        @Test
        void booleanPlusBoolean() {
            NodeConstant left = NodeBoolean.TRUE;
            NodeConstant right = NodeBoolean.TRUE;
            NodeConstant result = left.add(right);

            assertThat(result.isNumeric()).isTrue();
            assertThat(result.doubleValue()).isCloseTo(2.0, within(TOLERANCE));
        }

        @Test
        void negateDouble() {
            NodeConstant val = new NodeDouble(5.0);
            NodeConstant result = val.negate();

            assertThat(result).isInstanceOf(NodeDouble.class);
            assertThat(result.doubleValue()).isCloseTo(-5.0, within(TOLERANCE));
        }

        @Test
        void negateRational() {
            NodeConstant val = new NodeRational(3, 4);
            NodeConstant result = val.negate();

            assertThat(result).isInstanceOf(NodeRational.class);
            assertThat(result.doubleValue()).isCloseTo(-0.75, within(TOLERANCE));
        }
    }

    // ==================== String Concatenation ====================

    @Nested
    class StringConcatenation {

        @Test
        void stringPlusString() {
            NodeConstant left = new NodeString("hello");
            NodeConstant right = new NodeString(" world");
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("hello world");
        }

        @Test
        void stringPlusNumber() {
            NodeConstant left = new NodeString("abc");
            NodeConstant right = new NodeDouble(3.0);
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("abc3");
        }

        @Test
        void numberPlusString() {
            NodeConstant left = new NodeDouble(5.0);
            NodeConstant right = new NodeString("hello");
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("5hello");
        }

        @Test
        void stringPlusBoolean() {
            NodeConstant left = new NodeString("val=");
            NodeConstant right = NodeBoolean.TRUE;
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("val=true");
        }

        @Test
        void rationalPlusString() {
            NodeConstant left = new NodeRational(1, 2);
            NodeConstant right = new NodeString(" is a half");
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("1/2 is a half");
        }

        @Test
        void stringPlusEmptyString() {
            NodeConstant left = new NodeString("test");
            NodeConstant right = new NodeString("");
            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("test");
        }
    }

    // ==================== String Repetition ====================

    @Nested
    class StringRepetition {

        @Test
        void stringTimesNumber() {
            NodeConstant left = new NodeString("ab");
            NodeConstant right = new NodeDouble(3.0);
            NodeConstant result = left.multiply(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("ababab");
        }

        @Test
        void numberTimesString() {
            NodeConstant left = new NodeDouble(3.0);
            NodeConstant right = new NodeString("ab");
            NodeConstant result = left.multiply(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("ababab");
        }

        @Test
        void stringTimesZero() {
            NodeConstant left = new NodeString("x");
            NodeConstant right = new NodeDouble(0.0);
            NodeConstant result = left.multiply(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("");
        }

        @Test
        void stringTimesOne() {
            NodeConstant left = new NodeString("abc");
            NodeConstant right = new NodeRational(1, 1);
            NodeConstant result = left.multiply(right);

            assertThat(result).isInstanceOf(NodeString.class);
            assertThat(((NodeString) result).getValue()).isEqualTo("abc");
        }

        @Test
        void stringTimesNegativeThrows() {
            NodeConstant left = new NodeString("x");
            NodeConstant right = new NodeDouble(-1.0);

            assertThatThrownBy(() -> left.multiply(right))
                    .isInstanceOf(TypeError.class)
                    .hasMessageContaining("negative");
        }
    }

    // ==================== String Comparison ====================

    @Nested
    class StringComparison {

        @ParameterizedTest
        @CsvSource({
                "a, b, -1",
                "b, a, 1",
                "abc, abc, 0",
                "abc, abd, -1",
                "z, a, 1"
        })
        void compareToStrings(String left, String right, int expectedSign) {
            NodeConstant l = new NodeString(left);
            NodeConstant r = new NodeString(right);
            int result = l.compareTo(r);

            if (expectedSign < 0) {
                assertThat(result).isNegative();
            } else if (expectedSign > 0) {
                assertThat(result).isPositive();
            } else {
                assertThat(result).isZero();
            }
        }

        @Test
        void compareStringWithNumberThrows() {
            NodeConstant left = new NodeString("abc");
            NodeConstant right = new NodeDouble(5.0);

            assertThatThrownBy(() -> left.compareTo(right))
                    .isInstanceOf(TypeError.class);
        }

        @Test
        void compareNumberWithStringThrows() {
            NodeConstant left = new NodeDouble(5.0);
            NodeConstant right = new NodeString("abc");

            assertThatThrownBy(() -> left.compareTo(right))
                    .isInstanceOf(TypeError.class);
        }
    }

    // ==================== String TypeError Stubs ====================

    @Nested
    class StringTypeErrors {

        @Test
        void subtractFromStringThrows() {
            NodeConstant left = new NodeString("hello");
            NodeConstant right = new NodeDouble(5.0);

            assertThatThrownBy(() -> left.subtract(right))
                    .isInstanceOf(TypeError.class);
        }

        @Test
        void subtractStringFromNumberThrows() {
            NodeConstant left = new NodeDouble(5.0);
            NodeConstant right = new NodeString("hello");

            assertThatThrownBy(() -> left.subtract(right))
                    .isInstanceOf(TypeError.class);
        }

        @Test
        void divideStringThrows() {
            NodeConstant left = new NodeString("hello");
            NodeConstant right = new NodeDouble(2.0);

            assertThatThrownBy(() -> left.divide(right))
                    .isInstanceOf(TypeError.class);
        }

        @Test
        void powerStringThrows() {
            NodeConstant left = new NodeString("hello");
            NodeConstant right = new NodeDouble(2.0);

            assertThatThrownBy(() -> left.power(right))
                    .isInstanceOf(TypeError.class);
        }

        @Test
        void negateStringThrows() {
            NodeConstant val = new NodeString("hello");

            assertThatThrownBy(val::negate)
                    .isInstanceOf(TypeError.class);
        }

        @Test
        void multiplyStringByStringThrows() {
            NodeConstant left = new NodeString("ab");
            NodeConstant right = new NodeString("cd");

            assertThatThrownBy(() -> left.multiply(right))
                    .isInstanceOf(TypeError.class);
        }
    }

    // ==================== Vector Broadcasting ====================

    @Nested
    class VectorBroadcasting {

        @Test
        void vectorPlusScalar() {
            NodeVector vec = new NodeVector(new Node[]{
                    new NodeDouble(1.0), new NodeDouble(2.0), new NodeDouble(3.0)
            });
            NodeConstant scalar = new NodeDouble(10.0);

            NodeConstant result = vec.add(scalar);

            assertThat(result).isInstanceOf(NodeVector.class);
            NodeVector resultVec = (NodeVector) result;
            assertThat(resultVec.size()).isEqualTo(3);
            assertThat(((NodeConstant) resultVec.getElement(0)).doubleValue()).isCloseTo(11.0, within(TOLERANCE));
            assertThat(((NodeConstant) resultVec.getElement(1)).doubleValue()).isCloseTo(12.0, within(TOLERANCE));
            assertThat(((NodeConstant) resultVec.getElement(2)).doubleValue()).isCloseTo(13.0, within(TOLERANCE));
        }

        @Test
        void vectorPlusVector() {
            NodeVector left = new NodeVector(new Node[]{
                    new NodeRational(1, 2), new NodeRational(1, 3)
            });
            NodeVector right = new NodeVector(new Node[]{
                    new NodeRational(1, 4), new NodeRational(1, 5)
            });

            NodeConstant result = left.add(right);

            assertThat(result).isInstanceOf(NodeVector.class);
            NodeVector resultVec = (NodeVector) result;
            assertThat(resultVec.size()).isEqualTo(2);
            // 1/2 + 1/4 = 3/4
            assertThat(resultVec.getElement(0)).isInstanceOf(NodeRational.class);
            assertThat(((NodeConstant) resultVec.getElement(0)).doubleValue()).isCloseTo(0.75, within(TOLERANCE));
            // 1/3 + 1/5 = 8/15
            assertThat(resultVec.getElement(1)).isInstanceOf(NodeRational.class);
            assertThat(((NodeConstant) resultVec.getElement(1)).doubleValue()).isCloseTo(8.0 / 15.0, within(TOLERANCE));
        }

        @Test
        void vectorNegate() {
            NodeVector vec = new NodeVector(new Node[]{
                    new NodeDouble(1.0), new NodeDouble(-2.0), new NodeDouble(3.0)
            });

            NodeConstant result = vec.negate();

            assertThat(result).isInstanceOf(NodeVector.class);
            NodeVector resultVec = (NodeVector) result;
            assertThat(((NodeConstant) resultVec.getElement(0)).doubleValue()).isCloseTo(-1.0, within(TOLERANCE));
            assertThat(((NodeConstant) resultVec.getElement(1)).doubleValue()).isCloseTo(2.0, within(TOLERANCE));
            assertThat(((NodeConstant) resultVec.getElement(2)).doubleValue()).isCloseTo(-3.0, within(TOLERANCE));
        }

        @Test
        void vectorCompareToThrows() {
            NodeVector vec = new NodeVector(new Node[]{new NodeDouble(1.0)});
            NodeConstant scalar = new NodeDouble(1.0);

            assertThatThrownBy(() -> vec.compareTo(scalar))
                    .isInstanceOf(TypeError.class);
        }
    }

    // ==================== Matrix Broadcasting ====================

    @Nested
    class MatrixBroadcasting {

        @Test
        void matrixPlusScalar() {
            NodeMatrix mat = new NodeMatrix(new Node[][]{
                    {new NodeDouble(1.0), new NodeDouble(2.0)},
                    {new NodeDouble(3.0), new NodeDouble(4.0)}
            });
            NodeConstant scalar = new NodeDouble(10.0);

            NodeConstant result = mat.add(scalar);

            assertThat(result).isInstanceOf(NodeMatrix.class);
            NodeMatrix resultMat = (NodeMatrix) result;
            assertThat(((NodeConstant) resultMat.getElement(0, 0)).doubleValue()).isCloseTo(11.0, within(TOLERANCE));
            assertThat(((NodeConstant) resultMat.getElement(1, 1)).doubleValue()).isCloseTo(14.0, within(TOLERANCE));
        }

        @Test
        void matrixNegate() {
            NodeMatrix mat = new NodeMatrix(new Node[][]{
                    {new NodeDouble(1.0), new NodeDouble(-2.0)},
                    {new NodeDouble(3.0), new NodeDouble(-4.0)}
            });

            NodeConstant result = mat.negate();

            assertThat(result).isInstanceOf(NodeMatrix.class);
            NodeMatrix resultMat = (NodeMatrix) result;
            assertThat(((NodeConstant) resultMat.getElement(0, 0)).doubleValue()).isCloseTo(-1.0, within(TOLERANCE));
            assertThat(((NodeConstant) resultMat.getElement(0, 1)).doubleValue()).isCloseTo(2.0, within(TOLERANCE));
            assertThat(((NodeConstant) resultMat.getElement(1, 0)).doubleValue()).isCloseTo(-3.0, within(TOLERANCE));
            assertThat(((NodeConstant) resultMat.getElement(1, 1)).doubleValue()).isCloseTo(4.0, within(TOLERANCE));
        }

        @Test
        void matrixCompareToThrows() {
            NodeMatrix mat = new NodeMatrix(new Node[][]{{new NodeDouble(1.0)}});
            NodeConstant scalar = new NodeDouble(1.0);

            assertThatThrownBy(() -> mat.compareTo(scalar))
                    .isInstanceOf(TypeError.class);
        }
    }

    // ==================== Range Materialization ====================

    @Nested
    class RangeMaterialization {

        @Test
        void rangePlusScalar() {
            NodeRange range = new NodeRange(
                    new NodeRational(1, 1),
                    new NodeRational(3, 1),
                    new NodeRational(1, 1)
            );
            NodeConstant scalar = new NodeDouble(10.0);

            NodeConstant result = range.add(scalar);

            assertThat(result).isInstanceOf(NodeVector.class);
            NodeVector vec = (NodeVector) result;
            assertThat(vec.size()).isEqualTo(3);
            assertThat(((NodeConstant) vec.getElement(0)).doubleValue()).isCloseTo(11.0, within(TOLERANCE));
            assertThat(((NodeConstant) vec.getElement(1)).doubleValue()).isCloseTo(12.0, within(TOLERANCE));
            assertThat(((NodeConstant) vec.getElement(2)).doubleValue()).isCloseTo(13.0, within(TOLERANCE));
        }

        @Test
        void rangeCompareToThrows() {
            NodeRange range = new NodeRange(
                    new NodeRational(1, 1),
                    new NodeRational(3, 1),
                    null
            );

            assertThatThrownBy(() -> range.compareTo(new NodeDouble(1.0)))
                    .isInstanceOf(TypeError.class);
        }
    }

    // ==================== Lambda/Function TypeError ====================

    @Nested
    class FunctionTypeErrors {

        @Test
        void lambdaAddThrows() {
            NodeLambda lambda = new NodeLambda(java.util.List.of("x"), new NodeDouble(1.0));

            assertThatThrownBy(() -> lambda.add(new NodeDouble(1.0)))
                    .isInstanceOf(TypeError.class)
                    .hasMessageContaining("arithmetic");
        }

        @Test
        void lambdaSubtractThrows() {
            NodeLambda lambda = new NodeLambda(java.util.List.of("x"), new NodeDouble(1.0));

            assertThatThrownBy(() -> lambda.subtract(new NodeDouble(1.0)))
                    .isInstanceOf(TypeError.class);
        }

        @Test
        void lambdaNegateThrows() {
            NodeLambda lambda = new NodeLambda(java.util.List.of("x"), new NodeDouble(1.0));

            assertThatThrownBy(lambda::negate)
                    .isInstanceOf(TypeError.class);
        }

        @Test
        void lambdaCompareToThrows() {
            NodeLambda lambda = new NodeLambda(java.util.List.of("x"), new NodeDouble(1.0));

            assertThatThrownBy(() -> lambda.compareTo(new NodeDouble(1.0)))
                    .isInstanceOf(TypeError.class)
                    .hasMessageContaining("compare");
        }
    }

    // ==================== Numeric Comparison ====================

    @Nested
    class NumericComparison {

        @Test
        void compareDoubles() {
            NodeConstant left = new NodeDouble(3.0);
            NodeConstant right = new NodeDouble(5.0);

            assertThat(left.compareTo(right)).isNegative();
            assertThat(right.compareTo(left)).isPositive();
            assertThat(left.compareTo(new NodeDouble(3.0))).isZero();
        }

        @Test
        void compareRationals() {
            NodeConstant left = new NodeRational(1, 3);
            NodeConstant right = new NodeRational(1, 2);

            assertThat(left.compareTo(right)).isNegative();
            assertThat(right.compareTo(left)).isPositive();
        }

        @Test
        void compareMixedNumeric() {
            NodeConstant rational = new NodeRational(1, 2);
            NodeConstant dbl = new NodeDouble(0.75);

            assertThat(rational.compareTo(dbl)).isNegative();
        }
    }

    // ==================== Unit Arithmetic ====================

    @Nested
    class UnitArithmetic {

        @Test
        void unitNegate() {
            // Create a simple unit for testing
            NodeUnit unit = NodeUnit.of(5.0, createTestUnit("meter"));

            NodeConstant result = unit.negate();

            assertThat(result).isInstanceOf(NodeUnit.class);
            assertThat(((NodeUnit) result).getValue()).isCloseTo(-5.0, within(TOLERANCE));
        }

        @Test
        void unitPlusString() {
            NodeUnit unit = NodeUnit.of(5.0, createTestUnit("meter"));
            NodeConstant str = new NodeString(" is the value");

            NodeConstant result = unit.add(str);

            assertThat(result).isInstanceOf(NodeString.class);
        }

        @Test
        void unitSubtractStringThrows() {
            NodeUnit unit = NodeUnit.of(5.0, createTestUnit("meter"));
            NodeConstant str = new NodeString("test");

            assertThatThrownBy(() -> unit.subtract(str))
                    .isInstanceOf(TypeError.class);
        }

        private uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition createTestUnit(String name) {
            return new uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition(
                    name, name + "s", "length", name,
                    1.0, 0.0, java.util.List.of()
            );
        }
    }
}
