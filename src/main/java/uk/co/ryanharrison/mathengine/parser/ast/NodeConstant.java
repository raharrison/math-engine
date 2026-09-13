package uk.co.ryanharrison.mathengine.parser.ast;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

import java.util.function.DoubleUnaryOperator;

/**
 * A value: the result of evaluating an expression, needing no further computation.
 * <p>
 * Arithmetic and ordering are defined once here for every value type, so operators,
 * built-in functions and library callers all share the same semantics. See
 * {@link NodeArithmetic} for the type rules, which cover string concatenation,
 * unit conversion, percentages, exact rationals and element-wise broadcasting.
 * <p>
 * Sealed to give pattern switches exhaustiveness checking.
 */
public abstract sealed class NodeConstant extends Node permits
        NodeNumber,
        NodeString,
        NodeVector,
        NodeMatrix,
        NodeUnit,
        NodeRange,
        NodeLambda,
        NodeFunction {

    /**
     * This value as a double.
     *
     * @throws TypeError if the value is not numeric
     */
    public abstract double doubleValue();

    public abstract boolean isNumeric();

    public boolean isVector() {
        return false;
    }

    public boolean isMatrix() {
        return false;
    }

    // ==================== Arithmetic ====================

    public NodeConstant add(NodeConstant other) {
        return NodeArithmetic.apply(NodeArithmetic.Op.ADD, this, other);
    }

    public NodeConstant subtract(NodeConstant other) {
        return NodeArithmetic.apply(NodeArithmetic.Op.SUBTRACT, this, other);
    }

    public NodeConstant multiply(NodeConstant other) {
        return NodeArithmetic.apply(NodeArithmetic.Op.MULTIPLY, this, other);
    }

    public NodeConstant divide(NodeConstant other) {
        return NodeArithmetic.apply(NodeArithmetic.Op.DIVIDE, this, other);
    }

    public NodeConstant power(NodeConstant other) {
        return NodeArithmetic.apply(NodeArithmetic.Op.POWER, this, other);
    }

    /**
     * Floor modulo, so {@code -7 mod 3} is 2 rather than -1.
     */
    public NodeConstant modulo(NodeConstant other) {
        return NodeArithmetic.apply(NodeArithmetic.Op.MODULO, this, other);
    }

    public NodeConstant negate() {
        return NodeArithmetic.negate(this);
    }

    // ==================== Rounding ====================

    /**
     * Largest whole number no greater than this value.
     */
    public NodeConstant floor() {
        return NodeArithmetic.round(this, NodeArithmetic.Rounding.FLOOR);
    }

    /**
     * Smallest whole number no less than this value.
     */
    public NodeConstant ceil() {
        return NodeArithmetic.round(this, NodeArithmetic.Rounding.CEIL);
    }

    /**
     * Nearest whole number, with halves going up.
     */
    public NodeConstant round() {
        return NodeArithmetic.round(this, NodeArithmetic.Rounding.NEAREST);
    }

    /**
     * Drops the fractional part, moving towards zero.
     */
    public NodeConstant truncate() {
        return NodeArithmetic.round(this, NodeArithmetic.Rounding.TOWARDS_ZERO);
    }

    public NodeConstant abs() {
        return NodeArithmetic.abs(this);
    }

    /**
     * Transforms the magnitude, keeping the marker: {@code sqrt(100 meters)} is 10 meters.
     *
     * @throws TypeError if the value is not numeric
     */
    public NodeConstant mapMagnitude(DoubleUnaryOperator op) {
        return NodeArithmetic.mapMagnitude(this, op);
    }

    // ==================== Ordering ====================

    /**
     * Orders this value against another, converting units and comparing strings
     * lexicographically.
     *
     * @return negative, zero or positive as this value is less than, equal to or greater than {@code other}
     * @throws TypeError if the two values have no ordering
     */
    public int compareTo(NodeConstant other) {
        return NodeArithmetic.compare(this, other);
    }

    /**
     * Value equality as the {@code ==} operator sees it: units are converted before
     * comparing, collections compare structurally, and NaN equals nothing.
     */
    public boolean equalTo(NodeConstant other) {
        return NodeArithmetic.equalTo(this, other);
    }

}
