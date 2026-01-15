package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Internal wrapper for simple double-to-double functions.
 * <p>
 * This is the most common function type: takes a numeric value, converts to double,
 * applies an operation, and returns a NodeDouble.
 *
 * @see UnaryFunction#ofDouble(String, String, MathFunction.Category, DoubleUnaryOperator)
 */
final class DoubleUnaryFunctionWrapper implements MathFunction {

    private final String funcName;
    private final String funcDescription;
    private final Category funcCategory;
    private final List<String> funcAliases;
    private final DoubleUnaryOperator function;

    DoubleUnaryFunctionWrapper(String name, String description, Category category,
                               List<String> aliases, DoubleUnaryOperator function) {
        this.funcName = name;
        this.funcDescription = description;
        this.funcCategory = category;
        this.funcAliases = aliases != null ? List.copyOf(aliases) : List.of();
        this.function = function;
    }

    @Override
    public String name() {
        return funcName;
    }

    @Override
    public String description() {
        return funcDescription;
    }

    @Override
    public List<String> aliases() {
        return funcAliases;
    }

    @Override
    public int minArity() {
        return 1;
    }

    @Override
    public int maxArity() {
        return 1;
    }

    @Override
    public Category category() {
        return funcCategory;
    }

    @Override
    public boolean supportsVectorBroadcasting() {
        return true;
    }

    @Override
    public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
        double x = ctx.toNumber(args.getFirst()).doubleValue();
        return new NodeDouble(function.applyAsDouble(x));
    }
}
