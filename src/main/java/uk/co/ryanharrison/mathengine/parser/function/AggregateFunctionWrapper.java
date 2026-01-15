package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;

/**
 * Internal wrapper that adapts an {@link AggregateFunction} to the {@link MathFunction} interface.
 *
 * @see AggregateFunction#of(String, String, MathFunction.Category, int, int, AggregateFunction)
 */
final class AggregateFunctionWrapper implements MathFunction {

    private final String funcName;
    private final String funcDescription;
    private final Category funcCategory;
    private final List<String> funcAliases;
    private final int funcMinArity;
    private final int funcMaxArity;
    private final AggregateFunction function;

    AggregateFunctionWrapper(String name, String description, Category category,
                             List<String> aliases, int minArity, int maxArity,
                             AggregateFunction function) {
        this.funcName = name;
        this.funcDescription = description;
        this.funcCategory = category;
        this.funcAliases = aliases != null ? List.copyOf(aliases) : List.of();
        this.funcMinArity = minArity;
        this.funcMaxArity = maxArity;
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
        return funcMinArity;
    }

    @Override
    public int maxArity() {
        return funcMaxArity;
    }

    @Override
    public Category category() {
        return funcCategory;
    }

    @Override
    public boolean supportsVectorBroadcasting() {
        return false;
    }

    @Override
    public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
        return function.apply(args, ctx);
    }
}
