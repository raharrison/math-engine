package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.ArrayList;
import java.util.List;

/**
 * Higher-order functions for functional programming.
 * <p>
 * These functions accept other functions (lambdas) as arguments and apply them
 * to collections.
 * <ul>
 *     <li>{@code map(f, collection)} - apply function to each element</li>
 *     <li>{@code filter(predicate, collection)} - filter elements</li>
 *     <li>{@code reduce(f, initial, collection)} - reduce to single value</li>
 * </ul>
 */
public final class HigherOrderFunctions {

    private HigherOrderFunctions() {
    }

    // ==================== Map Function ====================

    /**
     * Map function: applies a function to each element of a collection.
     * <p>
     * Syntax: {@code map(f, collection)}
     * <p>
     * Examples:
     * <ul>
     *     <li>{@code map(x -> x * 2, {1, 2, 3})} → {@code {2, 4, 6}}</li>
     *     <li>{@code map(x -> x^2, 1..5)} → {@code {1, 4, 9, 16, 25}}</li>
     * </ul>
     */
    public static final MathFunction MAP = new MathFunction() {
        @Override
        public String name() {
            return "map";
        }

        @Override
        public String description() {
            return "Apply function to each element of a collection";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant function = args.get(0);
            NodeConstant collection = args.get(1);

            if (!(function instanceof NodeFunction func)) {
                throw new TypeError("map: first argument must be a function, got " +
                        TypeCoercion.typeName(function));
            }

            List<NodeConstant> elements = toList(collection, ctx);
            List<Node> results = new ArrayList<>();

            for (NodeConstant element : elements) {
                NodeConstant result = ctx.callFunction(func, List.of(element));
                results.add(result);
            }

            return new NodeVector(results.toArray(new Node[0]));
        }
    };

    // ==================== Filter Function ====================

    /**
     * Filter function: filters elements based on a predicate.
     * <p>
     * Syntax: {@code filter(predicate, collection)}
     * <p>
     * Examples:
     * <ul>
     *     <li>{@code filter(x -> x > 3, {1, 2, 3, 4, 5})} → {@code {4, 5}}</li>
     *     <li>{@code filter(x -> x mod 2 == 0, 1..10)} → {@code {2, 4, 6, 8, 10}}</li>
     * </ul>
     */
    public static final MathFunction FILTER = new MathFunction() {
        @Override
        public String name() {
            return "filter";
        }

        @Override
        public String description() {
            return "Filter elements based on predicate";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant function = args.get(0);
            NodeConstant collection = args.get(1);

            if (!(function instanceof NodeFunction func)) {
                throw new TypeError("filter: first argument must be a function, got " +
                        TypeCoercion.typeName(function));
            }

            List<NodeConstant> elements = toList(collection, ctx);
            List<Node> results = new ArrayList<>();

            for (NodeConstant element : elements) {
                NodeConstant result = ctx.callFunction(func, List.of(element));
                if (TypeCoercion.toBoolean(result)) {
                    results.add(element);
                }
            }

            return new NodeVector(results.toArray(new Node[0]));
        }
    };

    // ==================== Reduce Function ====================

    /**
     * Reduce function: reduces a collection to a single value using a binary function.
     * <p>
     * Syntax: {@code reduce(f, initial, collection)}
     * <p>
     * Examples:
     * <ul>
     *     <li>{@code reduce((acc, x) -> acc + x, 0, {1, 2, 3, 4, 5})} → {@code 15}</li>
     *     <li>{@code reduce((acc, x) -> acc * x, 1, {1, 2, 3, 4, 5})} → {@code 120}</li>
     * </ul>
     */
    public static final MathFunction REDUCE = new MathFunction() {
        @Override
        public String name() {
            return "reduce";
        }

        @Override
        public String description() {
            return "Reduce collection to single value using binary function";
        }

        @Override
        public int minArity() {
            return 3;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant function = args.get(0);
            NodeConstant initial = args.get(1);
            NodeConstant collection = args.get(2);

            if (!(function instanceof NodeFunction func)) {
                throw new TypeError("reduce: first argument must be a function, got " +
                        TypeCoercion.typeName(function));
            }

            FunctionDefinition funcDef = func.getFunction();
            if (funcDef.getArity() != 2) {
                throw new TypeError("reduce: function must accept exactly 2 parameters (accumulator, element), got " +
                        funcDef.getArity());
            }

            List<NodeConstant> elements = toList(collection, ctx);
            NodeConstant accumulator = initial;

            for (NodeConstant element : elements) {
                accumulator = ctx.callFunction(func, List.of(accumulator, element));
            }

            return accumulator;
        }
    };

    // ==================== Helper Methods ====================

    /**
     * Converts a node to a list of elements.
     */
    private static List<NodeConstant> toList(NodeConstant node, FunctionContext ctx) {
        if (node instanceof NodeVector vector) {
            Node[] elements = vector.getElements();
            List<NodeConstant> result = new ArrayList<>();
            for (Node element : elements) {
                if (element instanceof NodeConstant constant) {
                    result.add(constant);
                } else {
                    throw new EvaluationException("Vector contains non-constant element: " + element);
                }
            }
            return result;
        }

        if (node instanceof NodeRange range) {
            List<NodeConstant> result = new ArrayList<>();
            java.util.Iterator<NodeConstant> iterator = range.iterator();
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
            return result;
        }

        throw new TypeError("Cannot iterate over " + TypeCoercion.typeName(node) +
                " in higher-order function");
    }

    /**
     * Gets all higher-order functions.
     */
    public static List<MathFunction> all() {
        return List.of(MAP, FILTER, REDUCE);
    }
}
