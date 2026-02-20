package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

/**
 * Node representing a user-defined function as a first-class value.
 * Functions can be assigned to variables and passed as arguments.
 */
public final class NodeFunction extends NodeConstant {

    private final FunctionDefinition function;

    public NodeFunction(FunctionDefinition function) {
        this.function = function;
    }

    public FunctionDefinition getFunction() {
        return function;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public double doubleValue() {
        throw new UnsupportedOperationException("Cannot convert function to double");
    }

    @Override
    public String typeName() {
        return "function";
    }

    // ==================== Universal Arithmetic ====================

    @Override
    public NodeConstant add(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant subtract(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant multiply(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant divide(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant power(NodeConstant other) {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public NodeConstant negate() {
        throw new TypeError("Cannot perform arithmetic on function");
    }

    @Override
    public int compareTo(NodeConstant other) {
        throw new TypeError("Cannot compare functions");
    }

    @Override
    public String toString() {
        return "<function:" + function.name() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeFunction other)) return false;
        return function.equals(other.function);
    }

    @Override
    public int hashCode() {
        return function.hashCode();
    }
}
