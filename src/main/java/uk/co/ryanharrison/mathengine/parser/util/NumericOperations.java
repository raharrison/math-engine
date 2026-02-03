package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Shared utility for type-preserving numeric operations.
 * <p>
 * This class provides consistent handling of ALL first-class numeric types:
 * units, percents, rationals, and doubles across both operator and function contexts.
 * </p>
 * <p>
 * Type preservation rules (in priority order):
 * <ul>
 *     <li><b>Units</b>: Preserved through +, -, *, /, ^ when combined with scalars</li>
 *     <li><b>Percents</b>: Preserved through +, -, *, /, ^ when combined with scalars</li>
 *     <li><b>Rationals</b>: Preserved when both operands are rational (exact arithmetic)</li>
 *     <li><b>Doubles</b>: Used when any operand is double (floating-point arithmetic)</li>
 * </ul>
 */
public final class NumericOperations {

    private NumericOperations() {
    }

    // ==================== Additive Operations (+ -) ====================

    /**
     * Applies an additive operation (+ or -) with full type preservation.
     */
    public static NodeConstant applyAdditive(NodeConstant left, NodeConstant right,
                                             DoubleBinaryOperator doubleOp,
                                             BinaryOperator<BigRational> rationalOp) {
        return applyAdditive(left, right, doubleOp, rationalOp, false);
    }

    /**
     * Applies an additive operation (+ or -) with type preservation.
     * <p>
     * Type preservation rules:
     * <ul>
     *     <li>unit + unit (same type) → unit (converts to first's unit)</li>
     *     <li>unit + scalar → unit</li>
     *     <li>percent + percent → percent</li>
     *     <li>rational + rational → rational (unless forceDouble is true)</li>
     *     <li>otherwise → double</li>
     * </ul>
     *
     * @param left        the left operand
     * @param right       the right operand
     * @param doubleOp    the operation for double operands
     * @param rationalOp  the operation for rational operands
     * @param forceDouble if true, skip rational arithmetic and use doubles
     * @return the result with preserved type where applicable
     */
    public static NodeConstant applyAdditive(NodeConstant left, NodeConstant right,
                                             DoubleBinaryOperator doubleOp,
                                             BinaryOperator<BigRational> rationalOp,
                                             boolean forceDouble) {
        // Unit operations
        if (left instanceof NodeUnit || right instanceof NodeUnit) {
            return applyUnitAdditive(left, right, doubleOp);
        }

        // Percent + Percent → Percent
        if (left instanceof NodePercent leftPct && right instanceof NodePercent rightPct) {
            double result = doubleOp.applyAsDouble(leftPct.getValue(), rightPct.getValue());
            return NodePercent.fromDecimal(result);
        }

        // Default numeric operation
        return applyNumeric(left, right, doubleOp, rationalOp, forceDouble);
    }

    /**
     * Applies an additive operation specifically for units.
     */
    private static NodeConstant applyUnitAdditive(NodeConstant left, NodeConstant right,
                                                  DoubleBinaryOperator doubleOp) {
        // unit + unit (same type) → unit
        if (left instanceof NodeUnit leftUnit && right instanceof NodeUnit rightUnit) {
            if (!leftUnit.getUnit().type().equals(rightUnit.getUnit().type())) {
                throw new TypeError("Cannot combine units of different types: " +
                        leftUnit.getUnit().type() + " and " + rightUnit.getUnit().type());
            }
            NodeUnit convertedRight = rightUnit.convertTo(leftUnit.getUnit());
            double result = doubleOp.applyAsDouble(leftUnit.getValue(), convertedRight.getValue());
            return NodeUnit.of(result, leftUnit.getUnit());
        }

        // unit + scalar → unit
        if (left instanceof NodeUnit leftUnit) {
            double scalar = toDoubleValue(right);
            double result = doubleOp.applyAsDouble(leftUnit.getValue(), scalar);
            return NodeUnit.of(result, leftUnit.getUnit());
        }

        // scalar + unit → unit
        if (right instanceof NodeUnit rightUnit) {
            double scalar = toDoubleValue(left);
            double result = doubleOp.applyAsDouble(scalar, rightUnit.getValue());
            return NodeUnit.of(result, rightUnit.getUnit());
        }

        throw new IllegalStateException("Expected at least one unit operand");
    }

    // ==================== Multiplicative Operations (* /) ====================

    /**
     * Applies a multiplicative operation (* or /) with full type preservation.
     */
    public static NodeConstant applyMultiplicative(NodeConstant left, NodeConstant right,
                                                   DoubleBinaryOperator doubleOp,
                                                   BinaryOperator<BigRational> rationalOp,
                                                   boolean isMultiply) {
        return applyMultiplicative(left, right, doubleOp, rationalOp, isMultiply, false);
    }

    /**
     * Applies a multiplicative operation (* or /) with type preservation.
     * <p>
     * Type preservation rules for multiplication:
     * <ul>
     *     <li>unit * scalar → unit</li>
     *     <li>scalar * unit → unit</li>
     *     <li>percent * scalar → percent</li>
     *     <li>scalar * percent → percent</li>
     *     <li>percent * percent → percent (product of fractions)</li>
     *     <li>rational * rational → rational (unless forceDouble is true)</li>
     * </ul>
     * <p>
     * Type preservation rules for division:
     * <ul>
     *     <li>unit / scalar → unit</li>
     *     <li>unit / unit (same type) → scalar (ratio)</li>
     *     <li>percent / scalar → percent</li>
     *     <li>percent / percent → scalar (ratio)</li>
     *     <li>rational / rational → rational (unless forceDouble is true)</li>
     * </ul>
     *
     * @param left        the left operand
     * @param right       the right operand
     * @param doubleOp    the operation for double operands
     * @param rationalOp  the operation for rational operands
     * @param isMultiply  true for multiplication, false for division
     * @param forceDouble if true, skip rational arithmetic and use doubles
     * @return the result with preserved type where applicable
     */
    public static NodeConstant applyMultiplicative(NodeConstant left, NodeConstant right,
                                                   DoubleBinaryOperator doubleOp,
                                                   BinaryOperator<BigRational> rationalOp,
                                                   boolean isMultiply,
                                                   boolean forceDouble) {
        // Unit operations
        if (left instanceof NodeUnit || right instanceof NodeUnit) {
            return applyUnitMultiplicative(left, right, isMultiply);
        }

        // Percent operations
        if (left instanceof NodePercent || right instanceof NodePercent) {
            return applyPercentMultiplicative(left, right, isMultiply);
        }

        // Default numeric operation
        return applyNumeric(left, right, doubleOp, rationalOp, forceDouble);
    }

    /**
     * Applies multiplicative operation for units.
     */
    private static NodeConstant applyUnitMultiplicative(NodeConstant left, NodeConstant right,
                                                        boolean isMultiply) {
        if (isMultiply) {
            // scalar * unit → unit
            if (!(left instanceof NodeUnit) && right instanceof NodeUnit rightUnit) {
                double scalar = toDoubleValue(left);
                return NodeUnit.of(scalar * rightUnit.getValue(), rightUnit.getUnit());
            }
            // unit * scalar → unit
            if (left instanceof NodeUnit leftUnit && !(right instanceof NodeUnit)) {
                double scalar = toDoubleValue(right);
                return NodeUnit.of(leftUnit.getValue() * scalar, leftUnit.getUnit());
            }
            // unit * unit → not typically supported, fall through to error
        } else {
            // unit / scalar → unit
            if (left instanceof NodeUnit leftUnit && !(right instanceof NodeUnit)) {
                double scalar = toDoubleValue(right);
                return NodeUnit.of(leftUnit.getValue() / scalar, leftUnit.getUnit());
            }
            // unit / unit (same type) → ratio
            if (left instanceof NodeUnit leftUnit) {
                NodeUnit rightUnit = (NodeUnit) right;
                if (!leftUnit.getUnit().type().equals(rightUnit.getUnit().type())) {
                    throw new TypeError("Cannot divide units of different types: " +
                            leftUnit.getUnit().type() + " and " + rightUnit.getUnit().type());
                }
                NodeUnit convertedRight = rightUnit.convertTo(leftUnit.getUnit());
                return new NodeDouble(leftUnit.getValue() / convertedRight.getValue());
            }
            // scalar / unit → returns scalar (loses unit)
            if (right instanceof NodeUnit rightUnit) {
                double scalar = toDoubleValue(left);
                return new NodeDouble(scalar / rightUnit.getValue());
            }
        }

        throw new TypeError("Unsupported unit operation");
    }

    /**
     * Applies multiplicative operation for percents.
     */
    private static NodeConstant applyPercentMultiplicative(NodeConstant left, NodeConstant right,
                                                           boolean isMultiply) {
        if (isMultiply) {
            // percent * percent → percent (product of fractions)
            if (left instanceof NodePercent leftPct && right instanceof NodePercent rightPct) {
                double result = leftPct.getValue() * rightPct.getValue();
                return NodePercent.fromDecimal(result);
            }
            // percent * scalar → percent
            if (left instanceof NodePercent leftPct) {
                double scalar = toDoubleValue(right);
                return NodePercent.fromDecimal(leftPct.getValue() * scalar);
            }
            // scalar * percent → percent
            if (right instanceof NodePercent rightPct) {
                double scalar = toDoubleValue(left);
                return NodePercent.fromDecimal(scalar * rightPct.getValue());
            }
        } else {
            // percent / percent → ratio (scalar)
            if (left instanceof NodePercent leftPct && right instanceof NodePercent rightPct) {
                return new NodeDouble(leftPct.getValue() / rightPct.getValue());
            }
            // percent / scalar → percent
            if (left instanceof NodePercent leftPct) {
                double scalar = toDoubleValue(right);
                return NodePercent.fromDecimal(leftPct.getValue() / scalar);
            }
            // scalar / percent → scalar
            if (right instanceof NodePercent rightPct) {
                double scalar = toDoubleValue(left);
                return new NodeDouble(scalar / rightPct.getValue());
            }
        }

        throw new IllegalStateException("Expected at least one percent operand");
    }

    // ==================== Power Operation (^) ====================

    /**
     * Applies power/exponentiation with type preservation.
     * <p>
     * Type preservation rules:
     * <ul>
     *     <li>unit ^ scalar → unit (value raised to power)</li>
     *     <li>percent ^ scalar → percent (value raised to power)</li>
     *     <li>rational ^ integer → rational (exact, unless forceDouble)</li>
     *     <li>otherwise → double</li>
     * </ul>
     *
     * @param base        the base operand
     * @param exponent    the exponent operand
     * @param forceDouble if true, skip rational arithmetic and use doubles
     * @return the result with preserved type where applicable
     */
    public static NodeConstant applyPower(NodeConstant base, NodeConstant exponent, boolean forceDouble) {
        double exp = toDoubleValue(exponent);

        // Unit ^ scalar → unit
        if (base instanceof NodeUnit baseUnit) {
            double result = Math.pow(baseUnit.getValue(), exp);
            return NodeUnit.of(result, baseUnit.getUnit());
        }

        // Percent ^ scalar → percent
        if (base instanceof NodePercent basePct) {
            double result = Math.pow(basePct.getValue(), exp);
            return NodePercent.fromDecimal(result);
        }

        // Rational ^ integer → rational (exact, unless forceDouble)
        if (!forceDouble && base instanceof NodeRational baseRat) {
            if (exp == Math.floor(exp) && !Double.isInfinite(exp)) {
                long longExp = (long) exp;
                return new NodeRational(baseRat.getValue().pow(longExp));
            }
        }

        // Fall back to double
        double baseVal = toDoubleValue(base);
        return new NodeDouble(Math.pow(baseVal, exp));
    }

    // ==================== Unary Operations ====================

    /**
     * Applies a unary operation with type preservation.
     */
    public static NodeConstant applyUnary(NodeConstant operand,
                                          UnaryOperator<BigRational> rationalOp,
                                          DoubleUnaryOperator doubleOp) {
        return applyUnary(operand, rationalOp, doubleOp, false);
    }

    /**
     * Applies a unary operation with type preservation.
     * <p>
     * Type preservation rules:
     * <ul>
     *     <li>unit → unit (operation applied to value)</li>
     *     <li>percent → percent (operation applied to value)</li>
     *     <li>rational → rational (if rationalOp provided and succeeds, unless forceDouble)</li>
     *     <li>otherwise → double</li>
     * </ul>
     *
     * @param operand     the operand
     * @param rationalOp  the operation for rational operands (may be null)
     * @param doubleOp    the operation for double operands
     * @param forceDouble if true, skip rational arithmetic and use doubles
     * @return the result with preserved type where applicable
     */
    public static NodeConstant applyUnary(NodeConstant operand,
                                          UnaryOperator<BigRational> rationalOp,
                                          DoubleUnaryOperator doubleOp,
                                          boolean forceDouble) {
        // Unit → unit
        if (operand instanceof NodeUnit unit) {
            double result = doubleOp.applyAsDouble(unit.getValue());
            return NodeUnit.of(result, unit.getUnit());
        }

        // Percent → percent
        if (operand instanceof NodePercent pct) {
            double result = doubleOp.applyAsDouble(pct.getValue());
            return NodePercent.fromDecimal(result);
        }

        // Rational → rational (if rationalOp provided and forceDouble is false)
        if (!forceDouble && operand instanceof NodeRational rat && rationalOp != null) {
            try {
                return new NodeRational(rationalOp.apply(rat.getValue()));
            } catch (ArithmeticException e) {
                // Fall through to double
            }
        }

        // Fall back to double
        double value = toDoubleValue(operand);
        return new NodeDouble(doubleOp.applyAsDouble(value));
    }

    // ==================== Pure Numeric Operations ====================

    /**
     * Applies a pure numeric operation with optional rational preservation.
     * Does not handle units or percents - use specific methods for those.
     *
     * @param left        the left operand
     * @param right       the right operand
     * @param doubleOp    the operation for double operands
     * @param rationalOp  the operation for rational operands
     * @param forceDouble if true, always produce NodeDouble instead of NodeRational
     * @return the result
     */
    public static NodeConstant applyNumeric(NodeConstant left, NodeConstant right,
                                            DoubleBinaryOperator doubleOp,
                                            BinaryOperator<BigRational> rationalOp,
                                            boolean forceDouble) {
        // If forceDouble is set, skip rational preservation
        if (!forceDouble && left instanceof NodeRational leftRat && right instanceof NodeRational rightRat) {
            try {
                return new NodeRational(rationalOp.apply(leftRat.getValue(), rightRat.getValue()));
            } catch (ArithmeticException e) {
                // Fall through to double (e.g., division by zero)
            }
        }

        // Fall back to double
        double l = toDoubleValue(left);
        double r = toDoubleValue(right);
        return new NodeDouble(doubleOp.applyAsDouble(l, r));
    }

    // ==================== Type Checking Utilities ====================

    /**
     * Converts a node to a double value, extracting from any wrapper type.
     */
    public static double toDoubleValue(NodeConstant node) {
        return switch (node) {
            case NodeUnit unit -> unit.getValue();
            case NodePercent pct -> pct.getValue();
            case NodeBoolean bool -> bool.getValue() ? 1.0 : 0.0;
            case NodeNumber num -> num.doubleValue();
            default -> throw new TypeError("Cannot convert to number: " + node.getClass().getSimpleName());
        };
    }

    // ==================== Comparison Operations ====================

    /**
     * Tolerance for unit conversion precision errors.
     */
    private static final double UNIT_COMPARISON_TOLERANCE = 1e-10;

    /**
     * Compares two numeric values with proper type handling.
     */
    public static int compareNumeric(NodeConstant left, NodeConstant right) {
        // Unit vs Unit comparison
        if (left instanceof NodeUnit leftUnit && right instanceof NodeUnit rightUnit) {
            if (!leftUnit.getUnit().type().equals(rightUnit.getUnit().type())) {
                return Double.compare(leftUnit.getValue(), rightUnit.getValue());
            }
            NodeUnit convertedRight = rightUnit.convertTo(leftUnit.getUnit());
            return Double.compare(leftUnit.getValue(), convertedRight.getValue());
        }

        // Unit vs scalar
        if (left instanceof NodeUnit || right instanceof NodeUnit) {
            return Double.compare(toDoubleValue(left), toDoubleValue(right));
        }

        // Both NodeRational - exact comparison
        if (left instanceof NodeRational leftRat && right instanceof NodeRational rightRat) {
            return leftRat.getValue().compareTo(rightRat.getValue());
        }

        // Fall back to double comparison
        return Double.compare(toDoubleValue(left), toDoubleValue(right));
    }

    /**
     * Tests equality with proper type handling.
     */
    public static boolean areEqual(NodeConstant left, NodeConstant right) {
        double lVal = toDoubleValue(left);
        double rVal = toDoubleValue(right);

        if (Double.isNaN(lVal) || Double.isNaN(rVal)) {
            return false;
        }
        if (Double.isInfinite(lVal) || Double.isInfinite(rVal)) {
            return lVal == rVal;
        }

        // Unit vs Unit
        if (left instanceof NodeUnit leftUnit && right instanceof NodeUnit rightUnit) {
            if (!leftUnit.getUnit().type().equals(rightUnit.getUnit().type())) {
                return false;
            }
            NodeUnit convertedRight = rightUnit.convertTo(leftUnit.getUnit());
            return Math.abs(leftUnit.getValue() - convertedRight.getValue()) < UNIT_COMPARISON_TOLERANCE;
        }

        // Unit vs scalar
        if (left instanceof NodeUnit || right instanceof NodeUnit) {
            return Math.abs(lVal - rVal) < UNIT_COMPARISON_TOLERANCE;
        }

        // Both rational - exact comparison
        if (left instanceof NodeRational leftRat && right instanceof NodeRational rightRat) {
            return leftRat.getValue().equals(rightRat.getValue());
        }

        // Double involved - exact comparison (floating-point errors visible)
        return lVal == rVal;
    }

    public static boolean isLessThan(NodeConstant left, NodeConstant right) {
        return compareNumeric(left, right) < 0;
    }

    public static boolean isGreaterThan(NodeConstant left, NodeConstant right) {
        return compareNumeric(left, right) > 0;
    }

    public static boolean isLessThanOrEqual(NodeConstant left, NodeConstant right) {
        return compareNumeric(left, right) <= 0;
    }

    public static boolean isGreaterThanOrEqual(NodeConstant left, NodeConstant right) {
        return compareNumeric(left, right) >= 0;
    }
}
