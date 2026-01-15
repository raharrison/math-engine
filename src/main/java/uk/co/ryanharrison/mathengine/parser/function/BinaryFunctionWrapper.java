package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;

/**
 * Internal wrapper that adapts a {@link BinaryFunction} to the {@link MathFunction} interface.
 *
 * @see BinaryFunction#of(String, String, MathFunction.Category, BinaryFunction)
 */
final class BinaryFunctionWrapper implements MathFunction {

    private final String funcName;
    private final String funcDescription;
    private final Category funcCategory;
    private final List<String> funcAliases;
    private final BinaryFunction function;

    BinaryFunctionWrapper(String name, String description, Category category,
                          List<String> aliases, BinaryFunction function) {
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
        return 2;
    }

    @Override
    public int maxArity() {
        return 2;
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
        return function.apply(args.get(0), args.get(1), ctx);
    }
}
