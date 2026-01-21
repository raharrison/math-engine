package uk.co.ryanharrison.mathengine.parser.parser.nodes;

import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;

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
    public String toString() {
        return "<function:" + function.name() + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeFunction)) return false;
        NodeFunction other = (NodeFunction) obj;
        return function.equals(other.function);
    }

    @Override
    public int hashCode() {
        return function.hashCode();
    }
}
