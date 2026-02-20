package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

/**
 * Abstract base class for all numeric node types.
 * Numeric nodes can be converted to double values and support arithmetic operations.
 * <p>
 * Sealed to enable exhaustiveness checking in pattern matching.
 */
public abstract sealed class NodeNumber extends NodeConstant permits
        NodeDouble,
        NodeRational,
        NodePercent,
        NodeBoolean {

    @Override
    public String typeName() {
        return "number";
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    /**
     * Get the numeric value as a double.
     */
    @Override
    public abstract double doubleValue();

    /**
     * Negate this number (multiply by -1).
     */
    @Override
    public abstract NodeNumber negate();

    /**
     * Get the absolute value of this number.
     */
    public abstract NodeNumber abs();

    // ==================== Universal Arithmetic ====================

    @Override
    public NodeConstant add(NodeConstant other) {
        if (other instanceof NodeString) {
            return new NodeString(TypeCoercion.toDisplayString(this) + ((NodeString) other).getValue());
        }
        return NumericOperations.applyAdditive(this, other, Double::sum, BigRational::add);
    }

    @Override
    public NodeConstant subtract(NodeConstant other) {
        if (other instanceof NodeString) {
            throw new TypeError("Cannot subtract string from " + typeName());
        }
        return NumericOperations.applyAdditive(this, other, (a, b) -> a - b, BigRational::subtract);
    }

    @Override
    public NodeConstant multiply(NodeConstant other) {
        if (other instanceof NodeString otherStr) {
            return otherStr.repeat(this);
        }
        return NumericOperations.applyMultiplicative(this, other, (a, b) -> a * b, BigRational::multiply, true);
    }

    @Override
    public NodeConstant divide(NodeConstant other) {
        if (other instanceof NodeString) {
            throw new TypeError("Cannot divide " + typeName() + " by string");
        }
        return NumericOperations.applyMultiplicative(this, other, (a, b) -> a / b, BigRational::divide, false);
    }

    @Override
    public NodeConstant power(NodeConstant other) {
        if (other instanceof NodeString) {
            throw new TypeError("Cannot raise " + typeName() + " to string power");
        }
        return NumericOperations.applyPower(this, other, false);
    }

    @Override
    public int compareTo(NodeConstant other) {
        if (other instanceof NodeString) {
            throw new TypeError("Cannot compare " + typeName() + " with string");
        }
        return NumericOperations.compareNumeric(this, other);
    }
}
