package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;

/**
 * A function that receives its arguments unevaluated, so it can decide which ones
 * to evaluate. Used for control flow such as {@code if}, where evaluating the
 * untaken branch would be wasteful or would raise an error.
 * <p>
 * Arity is still validated before {@link #applyLazy} is called.
 */
public interface LazyFunction extends MathFunction {

    /**
     * @param args     the unevaluated argument nodes
     * @param ctx      the function context
     * @param evaluate evaluates an argument in the caller's scope
     */
    NodeConstant applyLazy(List<Node> args, FunctionContext ctx, ArgumentEvaluator evaluate);

    /**
     * Lazy functions are always invoked through {@link #applyLazy}.
     */
    @Override
    default NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
        throw new UnsupportedOperationException(name() + " must be called with unevaluated arguments");
    }

    /**
     * Lazy functions never broadcast: their arguments are nodes, not values.
     */
    @Override
    default boolean supportsVectorBroadcasting() {
        return false;
    }

    /**
     * Evaluates a single argument node in the calling scope.
     */
    @FunctionalInterface
    interface ArgumentEvaluator {
        NodeConstant evaluate(Node node);
    }

    /**
     * The body of a lazy function, as supplied to {@code FunctionBuilder.implementedByLazy}.
     */
    @FunctionalInterface
    interface Body {
        NodeConstant apply(List<Node> args, FunctionContext ctx, ArgumentEvaluator evaluate);
    }
}
