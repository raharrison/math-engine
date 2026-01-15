package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;

/**
 * Internal wrapper that adapts a {@link UnaryFunction} to the {@link MathFunction} interface.
 * <p>
 * This class handles the boilerplate of implementing MathFunction while delegating
 * the actual computation to the wrapped UnaryFunction.
 *
 * @see UnaryFunction#of(String, String, MathFunction.Category, UnaryFunction)
 */
final class UnaryFunctionWrapper implements MathFunction {

    private final String funcName;
    private final String funcDescription;
    private final Category funcCategory;
    private final List<String> funcAliases;
    private final UnaryFunction function;
    private final boolean supportsBroadcasting;

    UnaryFunctionWrapper(String name, String description, Category category,
                         List<String> aliases, UnaryFunction function, boolean supportsBroadcasting) {
        this.funcName = name;
        this.funcDescription = description;
        this.funcCategory = category;
        this.funcAliases = aliases != null ? List.copyOf(aliases) : List.of();
        this.function = function;
        this.supportsBroadcasting = supportsBroadcasting;
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
        return supportsBroadcasting;
    }

    @Override
    public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
        return function.apply(args.getFirst(), ctx);
    }
}
