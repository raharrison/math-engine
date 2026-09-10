package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.math.BigInteger;
import java.util.function.DoubleUnaryOperator;

/**
 * The single implementation of arithmetic and ordering over values.
 * <p>
 * Reached through {@link NodeConstant#add}, {@link NodeConstant#compareTo} and friends,
 * so operators, functions and library code all get identical semantics.
 *
 * <h2>Dispatch order</h2>
 * Each binary operation walks this cascade and stops at the first match:
 * <ol>
 *     <li><b>Collections</b>: vectors and matrices broadcast element-wise, recursing here per element</li>
 *     <li><b>Strings</b>: concatenation for {@code +}, repetition for {@code *}</li>
 *     <li><b>Units</b>: the label rides the magnitude, cancelling only when like meets like</li>
 *     <li><b>Percents</b>: {@code 100 + 10%} is 110, while {@code 10% + 20%} is 30%</li>
 *     <li><b>Plain numbers</b>: exact when both sides are rational, double otherwise</li>
 * </ol>
 */
final class NodeArithmetic {

    /**
     * Units are compared after conversion, which is not exact in binary floating point.
     */
    private static final double UNIT_TOLERANCE = 1e-10;

    /**
     * Beyond this, an exact result would need more memory than it is worth.
     */
    private static final long MAX_EXACT_POWER = 10_000;

    private NodeArithmetic() {
    }

    /**
     * Each operation carries both its double and its exact implementation.
     */
    enum Op {
        ADD("+", Double::sum, BigRational::add),
        SUBTRACT("-", (a, b) -> a - b, BigRational::subtract),
        MULTIPLY("*", (a, b) -> a * b, BigRational::multiply),
        DIVIDE("/", (a, b) -> a / b, NodeArithmetic::exactDivide),
        POWER("^", Math::pow, null),
        MODULO("mod", (a, b) -> a - b * Math.floor(a / b), NodeArithmetic::exactFloorMod);

        private final String symbol;
        private final java.util.function.DoubleBinaryOperator doubles;
        private final java.util.function.BinaryOperator<BigRational> exact;

        Op(String symbol,
           java.util.function.DoubleBinaryOperator doubles,
           java.util.function.BinaryOperator<BigRational> exact) {
            this.symbol = symbol;
            this.doubles = doubles;
            this.exact = exact;
        }

        double apply(double left, double right) {
            return doubles.applyAsDouble(left, right);
        }

        boolean isAdditive() {
            return this == ADD || this == SUBTRACT;
        }
    }

    // ==================== Entry point ====================

    static NodeConstant apply(Op op, NodeConstant left, NodeConstant right) {
        left = materialise(left);
        right = materialise(right);

        if (isCollection(left) || isCollection(right)) {
            return BroadcastingEngine.applyBinary(left, right, (l, r) -> apply(op, l, r));
        }
        if (left instanceof NodeString || right instanceof NodeString) {
            return strings(op, left, right);
        }
        if (left instanceof NodeUnit || right instanceof NodeUnit) {
            return units(op, left, right);
        }
        if (left instanceof NodePercent || right instanceof NodePercent) {
            return percents(op, left, right);
        }
        return numbers(op, requireNumber(op, left), requireNumber(op, right));
    }

    static NodeConstant negate(NodeConstant value) {
        return switch (materialise(value)) {
            case NodeNumber number -> number.negate();
            case NodeUnit unit -> NodeUnit.of(-unit.getValue(), unit.getUnit());
            case NodeVector vector -> BroadcastingEngine.applyUnary(vector, NodeArithmetic::negate);
            case NodeMatrix matrix -> BroadcastingEngine.applyUnary(matrix, NodeArithmetic::negate);
            default -> throw new TypeError("Cannot negate " + value.typeName());
        };
    }

    // ==================== Rounding and magnitude ====================

    /**
     * How a value is moved to a whole number.
     */
    enum Rounding {
        FLOOR, CEIL, NEAREST, TOWARDS_ZERO
    }

    static NodeConstant round(NodeConstant value, Rounding mode) {
        return mapScalar(value, number -> {
            if (number instanceof NodeRational rational) {
                return new NodeRational(BigRational.of(round(rational.getValue(), mode)));
            }
            return new NodeDouble(round(number.doubleValue(), mode));
        });
    }

    static NodeConstant abs(NodeConstant value) {
        return mapScalar(value, NodeNumber::abs);
    }

    /**
     * Applies a real-valued transform to the magnitude, keeping the label.
     */
    static NodeConstant mapMagnitude(NodeConstant value, DoubleUnaryOperator op) {
        return mapScalar(value, number -> new NodeDouble(op.applyAsDouble(number.doubleValue())));
    }

    private static double round(double value, Rounding mode) {
        return switch (mode) {
            case FLOOR -> Math.floor(value);
            case CEIL -> Math.ceil(value);
            case NEAREST -> Math.round(value);
            case TOWARDS_ZERO -> value < 0 ? Math.ceil(value) : Math.floor(value);
        };
    }

    private static BigInteger round(BigRational value, Rounding mode) {
        BigInteger[] divRem = value.getNumerator().divideAndRemainder(value.getDenominator());
        BigInteger truncated = divRem[0];
        if (divRem[1].signum() == 0) {
            return truncated;
        }
        boolean negative = value.getNumerator().signum() < 0;
        return switch (mode) {
            case FLOOR -> negative ? truncated.subtract(BigInteger.ONE) : truncated;
            case CEIL -> negative ? truncated : truncated.add(BigInteger.ONE);
            case TOWARDS_ZERO -> truncated;
            // Math.round rounds halves up, towards positive infinity
            case NEAREST -> BigInteger.valueOf(Math.round(value.doubleValue()));
        };
    }

    /**
     * Applies a numeric transform to a value, keeping its shape: collections stay
     * collections, and units and percentages keep their wrapper.
     */
    private static NodeConstant mapScalar(NodeConstant value, java.util.function.UnaryOperator<NodeNumber> map) {
        return switch (materialise(value)) {
            case NodePercent percent -> NodePercent.fromDecimal(
                    map.apply(new NodeDouble(percent.getValue())).doubleValue());
            case NodeNumber number -> map.apply(number);
            case NodeUnit unit -> NodeUnit.of(map.apply(new NodeDouble(unit.getValue())).doubleValue(), unit.getUnit());
            case NodeVector vector -> BroadcastingEngine.applyUnary(vector, v -> mapScalar(v, map));
            case NodeMatrix matrix -> BroadcastingEngine.applyUnary(matrix, v -> mapScalar(v, map));
            default -> throw new TypeError("Expected a number, got " + value.typeName());
        };
    }

    // ==================== Strings ====================

    private static NodeConstant strings(Op op, NodeConstant left, NodeConstant right) {
        if (op == Op.ADD) {
            return new NodeString(TypeCoercion.toDisplayString(left) + TypeCoercion.toDisplayString(right));
        }
        if (op == Op.MULTIPLY) {
            // A plain count only: repeating a string "4 metres" times means nothing
            if (left instanceof NodeString text && right instanceof NodeNumber count) {
                return repeat(text, count);
            }
            if (right instanceof NodeString text && left instanceof NodeNumber count) {
                return repeat(text, count);
            }
        }
        throw new TypeError("Cannot apply '" + op.symbol + "' to " +
                left.typeName() + " and " + right.typeName());
    }

    private static NodeConstant repeat(NodeString text, NodeNumber count) {
        double times = count.doubleValue();
        if (times != Math.floor(times) || Double.isInfinite(times)) {
            // Rounding here would quietly turn "ab" * 2.5 into "abab", and 20% * "ab" into ""
            throw new TypeError("Cannot repeat a string a fractional number of times: " + times);
        }
        if (times < 0) {
            throw new TypeError("Cannot repeat a string a negative number of times: " + (long) times);
        }
        return new NodeString(text.getValue().repeat((int) times));
    }

    // ==================== Units ====================

    private static NodeConstant units(Op op, NodeConstant left, NodeConstant right) {
        boolean leftIsUnit = left instanceof NodeUnit;
        boolean rightIsUnit = right instanceof NodeUnit;

        if (leftIsUnit && rightIsUnit) {
            return bothUnits(op, (NodeUnit) left, (NodeUnit) right);
        }

        // One side carries a label: the arithmetic happens on the magnitude, the label rides
        NodeUnit unit = (NodeUnit) (leftIsUnit ? left : right);
        double scalar = requireNumber(op, leftIsUnit ? right : left).doubleValue();
        double magnitude = leftIsUnit
                ? op.apply(unit.getValue(), scalar)
                : op.apply(scalar, unit.getValue());
        return NodeUnit.of(magnitude, unit.getUnit());
    }

    private static NodeConstant bothUnits(Op op, NodeUnit left, NodeUnit right) {
        if (!left.getUnit().type().equals(right.getUnit().type())) {
            throw new TypeError("Cannot combine units of different types: " +
                    left.getUnit().type() + " and " + right.getUnit().type());
        }
        double rightValue = right.convertTo(left.getUnit()).getValue();

        if (op.isAdditive() || op == Op.MODULO) {
            // Two lengths add, subtract and divide into one another without changing dimension
            return NodeUnit.of(op.apply(left.getValue(), rightValue), left.getUnit());
        }
        if (op == Op.DIVIDE) {
            // Like quantities cancel, leaving a plain ratio
            return new NodeDouble(left.getValue() / rightValue);
        }
        throw new TypeError("Cannot apply '" + op.symbol + "' to two unit values");
    }

    // ==================== Percents ====================

    private static NodeConstant percents(Op op, NodeConstant left, NodeConstant right) {
        boolean leftIsPercent = left instanceof NodePercent;
        boolean rightIsPercent = right instanceof NodePercent;
        double leftValue = requireNumber(op, left).doubleValue();
        double rightValue = requireNumber(op, right).doubleValue();

        if (leftIsPercent && rightIsPercent) {
            // A ratio of two percents is a plain number; everything else stays a percent
            return op == Op.DIVIDE
                    ? new NodeDouble(leftValue / rightValue)
                    : NodePercent.fromDecimal(op.apply(leftValue, rightValue));
        }

        if (rightIsPercent) {
            // "100 + 10%" reads as "100 plus 10% of 100", the usual calculator convention
            if (op.isAdditive()) {
                double delta = leftValue * rightValue;
                return new NodeDouble(op == Op.ADD ? leftValue + delta : leftValue - delta);
            }
            if (op == Op.MULTIPLY) {
                return NodePercent.fromDecimal(leftValue * rightValue);
            }
            return new NodeDouble(op.apply(leftValue, rightValue));
        }

        // Percent on the left of a plain number: scaling keeps it a percent, anything else measures it
        return op == Op.MULTIPLY || op == Op.DIVIDE || op == Op.POWER
                ? NodePercent.fromDecimal(op.apply(leftValue, rightValue))
                : new NodeDouble(op.apply(leftValue, rightValue));
    }

    // ==================== Plain numbers ====================

    private static NodeConstant numbers(Op op, NodeNumber left, NodeNumber right) {
        BigRational leftExact = exactValue(left);
        BigRational rightExact = exactValue(right);

        if (leftExact != null && rightExact != null) {
            try {
                return new NodeRational(op == Op.POWER
                        ? exactPower(leftExact, rightExact)
                        : op.exact.apply(leftExact, rightExact));
            } catch (ArithmeticException notRepresentable) {
                // Division by zero, or a non-integer exponent: fall through to doubles
            }
        }
        return new NodeDouble(op.apply(left.doubleValue(), right.doubleValue()));
    }

    /**
     * The exact value of a number, or null if it can only be represented as a double.
     */
    private static BigRational exactValue(NodeNumber number) {
        return switch (number) {
            case NodeRational rational -> rational.getValue();
            case NodeBoolean bool -> BigRational.of(bool.getValue() ? 1 : 0);
            default -> null;
        };
    }

    private static BigRational exactDivide(BigRational left, BigRational right) {
        if (right.getNumerator().signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return left.divide(right);
    }

    /**
     * Floor modulo, so {@code -7 mod 3} is 2 rather than Java's -1.
     */
    private static BigRational exactFloorMod(BigRational left, BigRational right) {
        if (right.getNumerator().signum() == 0) {
            throw new ArithmeticException("Modulo by zero");
        }
        BigRational quotient = left.divide(right);
        BigInteger[] divRem = quotient.getNumerator().divideAndRemainder(quotient.getDenominator());
        BigInteger floor = divRem[1].signum() < 0 ? divRem[0].subtract(BigInteger.ONE) : divRem[0];
        return left.subtract(right.multiply(BigRational.of(floor)));
    }

    private static BigRational exactPower(BigRational base, BigRational exponent) {
        if (!exponent.getDenominator().equals(BigInteger.ONE)) {
            throw new ArithmeticException("Fractional exponent");
        }
        long power = exponent.getNumerator().longValueExact();
        if (Math.abs(power) > MAX_EXACT_POWER) {
            throw new ArithmeticException("Exponent too large for exact arithmetic");
        }
        return base.pow(power);
    }

    // ==================== Ordering ====================

    static int compare(NodeConstant left, NodeConstant right) {
        if (left instanceof NodeString leftText && right instanceof NodeString rightText) {
            return leftText.getValue().compareTo(rightText.getValue());
        }
        if (!left.isNumeric() || !right.isNumeric()) {
            throw new TypeError("Cannot compare " + left.typeName() + " and " + right.typeName());
        }

        if (left instanceof NodeUnit leftUnit && right instanceof NodeUnit rightUnit
                && leftUnit.getUnit().type().equals(rightUnit.getUnit().type())) {
            return Double.compare(leftUnit.getValue(), rightUnit.convertTo(leftUnit.getUnit()).getValue());
        }
        if (left instanceof NodeNumber leftNumber && right instanceof NodeNumber rightNumber) {
            BigRational leftExact = exactValue(leftNumber);
            BigRational rightExact = exactValue(rightNumber);
            if (leftExact != null && rightExact != null) {
                return leftExact.compareTo(rightExact);
            }
        }
        return Double.compare(left.doubleValue(), right.doubleValue());
    }

    static boolean equalTo(NodeConstant left, NodeConstant right) {
        if (isCollection(left) || isCollection(right) || !left.isNumeric() || !right.isNumeric()) {
            return left.equals(right);
        }

        double leftValue = left.doubleValue();
        double rightValue = right.doubleValue();
        if (Double.isNaN(leftValue) || Double.isNaN(rightValue)) {
            return false;
        }

        // Converted units carry rounding error, so they need a tolerance; plain doubles do not
        if (left instanceof NodeUnit || right instanceof NodeUnit) {
            if (left instanceof NodeUnit leftUnit && right instanceof NodeUnit rightUnit) {
                if (!leftUnit.getUnit().type().equals(rightUnit.getUnit().type())) {
                    return false;
                }
                rightValue = rightUnit.convertTo(leftUnit.getUnit()).getValue();
            }
            return Double.isInfinite(leftValue) || Double.isInfinite(rightValue)
                    ? leftValue == rightValue
                    : Math.abs(leftValue - rightValue) < UNIT_TOLERANCE;
        }

        return leftValue == rightValue;
    }

    // ==================== Helpers ====================

    private static boolean isCollection(NodeConstant value) {
        return value instanceof NodeVector || value instanceof NodeMatrix;
    }

    /**
     * A range behaves as the vector it stands for.
     */
    private static NodeConstant materialise(NodeConstant value) {
        return value instanceof NodeRange range ? range.toVector() : value;
    }

    private static NodeNumber requireNumber(Op op, NodeConstant value) {
        if (value instanceof NodeNumber number) {
            return number;
        }
        throw new TypeError("Cannot apply arithmetic '" + op.symbol + "' to " + value.typeName());
    }
}
