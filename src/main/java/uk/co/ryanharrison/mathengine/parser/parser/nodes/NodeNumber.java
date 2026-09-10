package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * A scalar number: an exact rational, a double, a percentage or a boolean.
 * Arithmetic is inherited from {@link NodeConstant}.
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

    @Override
    public abstract double doubleValue();

    @Override
    public abstract NodeNumber negate();

    public abstract NodeNumber abs();

}
