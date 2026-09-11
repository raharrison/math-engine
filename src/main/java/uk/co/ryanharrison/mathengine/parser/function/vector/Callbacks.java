package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Calling a caller-supplied function over a collection.
 * <p>
 * Two shapes of higher-order function use this. A dedicated one takes the function first,
 * as {@code map}, {@code filter} and {@code reduce} do. An existing function that already
 * took a value instead accepts a function in that value's place, so {@code count} keeps
 * its own argument order and gains {@code count(vector, x -> x > 3)}.
 */
final class Callbacks {

    private Callbacks() {
    }

    /**
     * Whether an argument is a function, which is how the value form and the callback form
     * of the same function are told apart.
     */
    static boolean isFunction(NodeConstant value) {
        return value instanceof NodeFunction;
    }

    static NodeFunction requireFunction(NodeConstant value, String name) {
        if (value instanceof NodeFunction function) {
            return function;
        }
        throw new TypeError(name + ": expected a function, got " + value.typeName());
    }

    /**
     * @throws TypeError if the function does not take the number of arguments it is about to be given
     */
    static NodeFunction requireArity(NodeConstant value, int arity, String name) {
        NodeFunction function = requireFunction(value, name);
        int actual = function.getFunction().getArity();
        if (actual != arity) {
            throw new TypeError(name + ": function must accept exactly " + arity +
                    (arity == 1 ? " parameter" : " parameters") + ", got " + actual);
        }
        return function;
    }

    static NodeConstant call(FunctionContext ctx, NodeFunction function, NodeConstant argument) {
        return ctx.callFunction(function, List.of(argument));
    }

    static boolean test(FunctionContext ctx, NodeFunction predicate, NodeConstant element) {
        return TypeCoercion.toBoolean(call(ctx, predicate, element));
    }

    /**
     * The elements of a vector or a range, as values.
     */
    static List<NodeConstant> elements(NodeConstant collection, FunctionContext ctx) {
        if (collection instanceof NodeVector vector) {
            var result = new ArrayList<NodeConstant>(vector.size());
            for (Node element : vector.getElements()) {
                if (element instanceof NodeConstant constant) {
                    result.add(constant);
                } else {
                    throw new EvaluationException("Vector contains non-constant element: " + element);
                }
            }
            return result;
        }

        if (collection instanceof NodeRange range) {
            var result = new ArrayList<NodeConstant>();
            Iterator<NodeConstant> iterator = range.iterator();
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
            return result;
        }

        throw new TypeError("Cannot iterate over a " + collection.typeName() +
                " in higher-order function");
    }

    static NodeVector vectorOf(List<NodeConstant> elements) {
        return new NodeVector(elements.toArray(Node[]::new));
    }
}
