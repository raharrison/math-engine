package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Higher-order functions for functional programming.
 * <p>
 * These take the function first, as {@code map(f, collection)} does. A function that
 * already existed with a value argument instead keeps its own order and accepts a function
 * where the value went, so {@code sort(vector, comparator)} and
 * {@code count(vector, predicate)} read as extensions rather than as new spellings.
 * <ul>
 *     <li>{@code map(f, collection)} - apply f to each element</li>
 *     <li>{@code flatmap(f, collection)} - apply f to each element, then flatten one level</li>
 *     <li>{@code filter(predicate, collection)} - keep the elements that match</li>
 *     <li>{@code partition(predicate, collection)} - the matches and the rest, as two vectors</li>
 *     <li>{@code takewhile(predicate, collection)} - the leading run that matches</li>
 *     <li>{@code dropwhile(predicate, collection)} - everything after that run</li>
 *     <li>{@code reduce(f, collection, initial)} - fold to a single value</li>
 *     <li>{@code scan(f, collection, initial)} - the same fold, keeping every step</li>
 *     <li>{@code sortby(key, collection)} - sort by a computed key</li>
 *     <li>{@code zipwith(f, a, b)} - combine two collections element by element</li>
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
    public static final MathFunction MAP = FunctionBuilder
            .named("map")
            .describedAs("Applies function f to each element of collection and returns the results")
            .withParams("f", "collection")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any())
            .implementedBy((func, collection, ctx) -> {
                var results = new ArrayList<NodeConstant>();
                for (NodeConstant element : Callbacks.elements(collection, ctx)) {
                    results.add(Callbacks.call(ctx, func, element));
                }
                return Callbacks.vectorOf(results);
            });

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
    public static final MathFunction FILTER = FunctionBuilder
            .named("filter")
            .describedAs("Returns elements of collection for which predicate returns true")
            .withParams("predicate", "collection")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any())
            .implementedBy((func, collection, ctx) -> {
                var results = new ArrayList<NodeConstant>();
                for (NodeConstant element : Callbacks.elements(collection, ctx)) {
                    if (Callbacks.test(ctx, func, element)) {
                        results.add(element);
                    }
                }
                return Callbacks.vectorOf(results);
            });

    // ==================== Reduce Function ====================

    /**
     * Reduce function: reduces a collection to a single value using a binary function.
     * <p>
     * Syntax: {@code reduce(f, collection, initial)}
     * <p>
     * Examples:
     * <ul>
     *     <li>{@code reduce((acc, x) -> acc + x, {1, 2, 3, 4, 5}, 0)} → {@code 15}</li>
     *     <li>{@code reduce((acc, x) -> acc * x, {1, 2, 3, 4, 5}, 1)} → {@code 120}</li>
     * </ul>
     */
    public static final MathFunction REDUCE = FunctionBuilder
            .named("reduce")
            .describedAs("Reduces collection to a single value by applying f(accumulator, element) from left to right")
            .withParams("f", "collection", "initial")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any(), ArgTypes.any())
            .implementedBy((func, collection, initial, ctx) -> {
                Callbacks.requireArity(func, 2, "reduce");

                NodeConstant accumulator = initial;
                for (NodeConstant element : Callbacks.elements(collection, ctx)) {
                    accumulator = ctx.callFunction(func, List.of(accumulator, element));
                }
                return accumulator;
            });

    // ==================== Flat Map ====================

    /**
     * Applies a function to each element and flattens one level of the results, so a
     * function returning a collection does not leave a collection of collections.
     * <p>
     * Example: {@code flatmap(x -> {x, x}, {1, 2})} is {@code {1, 1, 2, 2}}
     */
    public static final MathFunction FLATMAP = FunctionBuilder
            .named("flatmap")
            .alias("concatmap")
            .describedAs("Applies f to each element and concatenates the results into one vector")
            .withParams("f", "collection")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any())
            .implementedBy((func, collection, ctx) -> {
                var results = new ArrayList<NodeConstant>();
                for (NodeConstant element : Callbacks.elements(collection, ctx)) {
                    NodeConstant mapped = Callbacks.call(ctx, func, element);
                    if (mapped instanceof NodeVector nested) {
                        results.addAll(Callbacks.elements(nested, ctx));
                    } else {
                        results.add(mapped);
                    }
                }
                return Callbacks.vectorOf(results);
            });

    // ==================== Partition, Take While, Drop While ====================

    /**
     * Splits a collection into the elements that match and the elements that do not.
     * <p>
     * Example: {@code partition(x -> x > 2, {1, 2, 3, 4})} is <code>{{3, 4}, {1, 2}}</code>
     */
    public static final MathFunction PARTITION = FunctionBuilder
            .named("partition")
            .describedAs("Returns two vectors: the elements matching the predicate, then the rest")
            .withParams("predicate", "collection")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any())
            .implementedBy((func, collection, ctx) -> {
                var matching = new ArrayList<NodeConstant>();
                var rest = new ArrayList<NodeConstant>();
                for (NodeConstant element : Callbacks.elements(collection, ctx)) {
                    (Callbacks.test(ctx, func, element) ? matching : rest).add(element);
                }
                return new NodeVector(new Node[]{
                        Callbacks.vectorOf(matching), Callbacks.vectorOf(rest)});
            });

    /**
     * The longest leading run of elements matching the predicate.
     * <p>
     * Example: {@code takewhile(x -> x < 3, {1, 2, 3, 1})} is {@code {1, 2}}
     */
    public static final MathFunction TAKEWHILE = FunctionBuilder
            .named("takewhile")
            .describedAs("Returns the leading elements of the collection while the predicate holds")
            .withParams("predicate", "collection")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any())
            .implementedBy((func, collection, ctx) -> {
                var results = new ArrayList<NodeConstant>();
                for (NodeConstant element : Callbacks.elements(collection, ctx)) {
                    if (!Callbacks.test(ctx, func, element)) {
                        break;
                    }
                    results.add(element);
                }
                return Callbacks.vectorOf(results);
            });

    /**
     * Everything after the leading run that matches, which is what {@code takewhile} left.
     * <p>
     * Example: {@code dropwhile(x -> x < 3, {1, 2, 3, 1})} is {@code {3, 1}}
     */
    public static final MathFunction DROPWHILE = FunctionBuilder
            .named("dropwhile")
            .describedAs("Returns the collection with its leading matching elements removed")
            .withParams("predicate", "collection")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any())
            .implementedBy((func, collection, ctx) -> {
                List<NodeConstant> elements = Callbacks.elements(collection, ctx);
                int start = 0;
                while (start < elements.size() && Callbacks.test(ctx, func, elements.get(start))) {
                    start++;
                }
                return Callbacks.vectorOf(elements.subList(start, elements.size()));
            });

    // ==================== Scan ====================

    /**
     * The same fold as {@code reduce}, but keeping every intermediate value, which is how
     * a running total is written.
     * <p>
     * Example: {@code scan((a, x) -> a + x, {1, 2, 3}, 0)} is {@code {0, 1, 3, 6}}
     */
    public static final MathFunction SCAN = FunctionBuilder
            .named("scan")
            .alias("accumulate")
            .describedAs("Reduces the collection like reduce, but returns every intermediate accumulator")
            .withParams("f", "collection", "initial")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any(), ArgTypes.any())
            .implementedBy((func, collection, initial, ctx) -> {
                Callbacks.requireArity(func, 2, "scan");

                var steps = new ArrayList<NodeConstant>();
                NodeConstant accumulator = initial;
                steps.add(accumulator);
                for (NodeConstant element : Callbacks.elements(collection, ctx)) {
                    accumulator = ctx.callFunction(func, List.of(accumulator, element));
                    steps.add(accumulator);
                }
                return Callbacks.vectorOf(steps);
            });

    // ==================== Sort By ====================

    /**
     * Sorts by a computed key rather than by the elements themselves, which is usually
     * what is wanted when {@code sort} alone will not do.
     * <p>
     * Example: {@code sortby(x -> -x, {1, 3, 2})} is {@code {3, 2, 1}}
     */
    public static final MathFunction SORTBY = FunctionBuilder
            .named("sortby")
            .describedAs("Returns the collection sorted by the value the key function gives for each element")
            .withParams("key", "collection")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any())
            .implementedBy((func, collection, ctx) -> {
                List<NodeConstant> elements = Callbacks.elements(collection, ctx);

                // Keys are computed once per element rather than once per comparison, so a
                // costly key function does not turn the sort quadratic in calls
                var keyed = new ArrayList<NodeConstant[]>(elements.size());
                for (NodeConstant element : elements) {
                    keyed.add(new NodeConstant[]{Callbacks.call(ctx, func, element), element});
                }
                keyed.sort((a, b) -> a[0].compareTo(b[0]));

                var sorted = new ArrayList<NodeConstant>(keyed.size());
                for (NodeConstant[] pair : keyed) {
                    sorted.add(pair[1]);
                }
                return Callbacks.vectorOf(sorted);
            });

    // ==================== Zip With ====================

    /**
     * Combines two collections element by element, stopping at the shorter one. Where
     * {@code zip} pairs them up, this applies a function instead.
     * <p>
     * Example: {@code zipwith((a, b) -> a * b, {1, 2}, {3, 4})} is {@code {3, 8}}
     */
    public static final MathFunction ZIPWITH = FunctionBuilder
            .named("zipwith")
            .describedAs("Combines two collections element by element with f, stopping at the shorter")
            .withParams("f", "a", "b")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.function(), ArgTypes.any(), ArgTypes.any())
            .implementedBy((func, first, second, ctx) -> {
                Callbacks.requireArity(func, 2, "zipwith");

                List<NodeConstant> left = Callbacks.elements(first, ctx);
                List<NodeConstant> right = Callbacks.elements(second, ctx);
                int size = Math.min(left.size(), right.size());

                var results = new ArrayList<NodeConstant>(size);
                for (int i = 0; i < size; i++) {
                    results.add(ctx.callFunction(func, List.of(left.get(i), right.get(i))));
                }
                return Callbacks.vectorOf(results);
            });

    /**
     * Gets all higher-order functions.
     */
    public static List<MathFunction> all() {
        return List.of(MAP, FLATMAP, FILTER, PARTITION, TAKEWHILE, DROPWHILE,
                REDUCE, SCAN, SORTBY, ZIPWITH);
    }
}
