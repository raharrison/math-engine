package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.util.Sequences;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Collection of vector manipulation and transformation functions.
 */
public final class VectorManipulationFunctions {

    private VectorManipulationFunctions() {
    }

    // ==================== Slicing Functions ====================

    /**
     * Take first n elements
     */
    public static final MathFunction TAKE = FunctionBuilder
            .named("take")
            .describedAs("Returns the first n elements of the vector")
            .withParams("vector", "n")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.vector(), ArgTypes.integer())
            .implementedBy((vector, n, ctx) -> {
                if (n < 0) n = 0;
                n = Math.min(n, vector.size());

                Node[] result = new Node[n];
                for (int i = 0; i < n; i++) {
                    result[i] = vector.getElement(i);
                }
                return new NodeVector(result);
            });

    /**
     * Drop first n elements
     */
    public static final MathFunction DROP = FunctionBuilder
            .named("drop")
            .describedAs("Returns the vector with its first n elements removed")
            .withParams("vector", "n")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.vector(), ArgTypes.integer())
            .implementedBy((vector, n, ctx) -> {
                if (n < 0) n = 0;
                if (n >= vector.size()) {
                    return new NodeVector(new Node[0]);
                }

                int newLen = vector.size() - n;
                Node[] result = new Node[newLen];
                for (int i = 0; i < newLen; i++) {
                    result[i] = vector.getElement(n + i);
                }
                return new NodeVector(result);
            });

    /**
     * Slice vector [start, end) with optional step
     */
    public static final MathFunction SLICE = FunctionBuilder
            .named("slice")
            .describedAs("Returns a sub-vector from start (inclusive) to end (exclusive), with optional step")
            .withParams("vector", "start")
            .withParams("vector", "start", "end")
            .withParams("vector", "start", "end", "step")
            .inCategory(MathFunction.Category.VECTOR)
            .takingBetween(2, 4)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                NodeVector vector = ctx.requireVector(args.get(0));
                int start = ctx.requireInteger(args.get(1));
                int end = args.size() > 2
                        ? ctx.requireInteger(args.get(2))
                        : vector.size();
                int step = args.size() > 3
                        ? ctx.requireInteger(args.get(3))
                        : 1;

                if (step == 0) {
                    throw new TypeError("slice: step cannot be 0");
                }

                // Handle negative indices
                if (start < 0) start = Math.max(0, vector.size() + start);
                if (end < 0) end = vector.size() + end;

                start = Math.max(0, Math.min(start, vector.size()));
                end = Math.max(0, Math.min(end, vector.size()));

                List<Node> result = new ArrayList<>();
                if (step > 0) {
                    for (int i = start; i < end; i += step) {
                        result.add(vector.getElement(i));
                    }
                } else {
                    for (int i = start; i > end; i += step) {
                        result.add(vector.getElement(i));
                    }
                }
                return new NodeVector(result.toArray(Node[]::new));
            });

    // ==================== Element Access Functions ====================

    /**
     * Get element at index
     */
    public static final MathFunction GET = FunctionBuilder
            .named("get")
            .alias("at", "nth")
            .describedAs("Returns the element at index (0-based; negative indices count from end)")
            .withParams("vector", "index")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.vector(), ArgTypes.integer())
            .implementedBy((vector, index, ctx) -> {
                // Handle negative indices
                if (index < 0) index = vector.size() + index;

                if (index < 0 || index >= vector.size()) {
                    throw new TypeError("get: index " + index + " out of bounds for vector of size " + vector.size());
                }
                return (NodeConstant) vector.getElement(index);
            });

    /**
     * First position of a value in a vector, or of a substring in a string.
     */
    public static final MathFunction INDEXOF = FunctionBuilder
            .named("indexof")
            .alias("find", "strindexof", "strfind")
            .describedAs("Returns the first index of value in a vector, of an element matching a predicate, or of a substring in a string; -1 if absent")
            .withParams("vector", "value")
            .withParams("vector", "predicate")
            .withParams("str", "substr")
            .withParams("str", "substr", "start")
            .inCategory(MathFunction.Category.VECTOR)
            .takingBetween(2, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                int from = args.size() > 2 ? ctx.toInt(args.get(2)) : 0;
                if (Callbacks.isFunction(args.get(1))) {
                    return new NodeRational(indexMatching(args.get(0), args.get(1), from, ctx, "indexof"));
                }
                return new NodeRational(Sequences.indexOf(args.get(0), args.get(1), from));
            });

    /** Membership in a vector, or substring containment in a string. */
    public static final MathFunction CONTAINS = FunctionBuilder
            .named("contains")
            .alias("includes", "strcontains")
            .describedAs("Returns true if the value appears in the vector, an element matches the predicate, or the substring appears in the string")
            .withParams("vector", "value")
            .withParams("vector", "predicate")
            .withParams("str", "substr")
            .inCategory(MathFunction.Category.VECTOR)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((sequence, target, ctx) -> NodeBoolean.of(Callbacks.isFunction(target)
                    ? indexMatching(sequence, target, 0, ctx, "contains") >= 0
                    : Sequences.contains(sequence, target)));

    // ==================== Transformation Functions ====================

    /**
     * Remove duplicates
     */
    public static final MathFunction UNIQUE = FunctionBuilder
            .named("unique")
            .alias("distinct")
            .describedAs("Returns the vector with duplicates removed, preserving order; a key function decides what counts as a duplicate")
            .withParams("vector")
            .withParams("vector", "key")
            .inCategory(MathFunction.Category.VECTOR)
            .takingBetween(1, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                NodeVector vector = ctx.requireVector(args.getFirst());
                NodeFunction key = args.size() > 1
                        ? Callbacks.requireArity(args.get(1), 1, "unique")
                        : null;

                Set<Double> seen = new LinkedHashSet<>();
                List<Node> result = new ArrayList<>();
                for (NodeConstant element : Callbacks.elements(vector, ctx)) {
                    NodeConstant identity = key == null ? element : Callbacks.call(ctx, key, element);
                    if (seen.add(ctx.toDouble(identity))) {
                        result.add(element);
                    }
                }
                return new NodeVector(result.toArray(Node[]::new));
            });

    /**
     * The first index at or after {@code from} whose element the predicate accepts, or -1.
     */
    private static int indexMatching(NodeConstant sequence, NodeConstant predicate, int from,
                                     FunctionContext ctx, String name) {
        NodeFunction test = Callbacks.requireArity(predicate, 1, name);
        List<NodeConstant> elements = Callbacks.elements(sequence, ctx);
        for (int i = Math.max(from, 0); i < elements.size(); i++) {
            if (Callbacks.test(ctx, test, elements.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Concatenate vectors
     */
    public static final MathFunction CONCAT = FunctionBuilder
            .named("concat")
            .alias("append")
            .describedAs("Returns a new vector with all input vectors concatenated; scalars are treated as single-element vectors")
            .withParams("a", "b")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<Node> result = new ArrayList<>();

                for (NodeConstant arg : args) {
                    if (arg instanceof NodeVector vector) {
                        for (int i = 0; i < vector.size(); i++) {
                            result.add(vector.getElement(i));
                        }
                    } else {
                        result.add(arg);
                    }
                }
                return new NodeVector(result.toArray(Node[]::new));
            });

    /**
     * Flatten nested vectors
     */
    public static final MathFunction FLATTEN = FunctionBuilder
            .named("flatten")
            .describedAs("Recursively flattens nested vectors or a matrix into a single flat vector")
            .withParams("vector")
            .withParams("matrix")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.any())
            .implementedBy((arg, ctx) -> {
                List<Node> result = new ArrayList<>();
                flattenHelper(arg, result);
                return new NodeVector(result.toArray(Node[]::new));
            });

    private static void flattenHelper(NodeConstant node, List<Node> result) {
        if (node instanceof NodeVector vector) {
            for (int i = 0; i < vector.size(); i++) {
                flattenHelper((NodeConstant) vector.getElement(i), result);
            }
        } else if (node instanceof NodeMatrix matrix) {
            for (int i = 0; i < matrix.getRows(); i++) {
                for (int j = 0; j < matrix.getCols(); j++) {
                    flattenHelper((NodeConstant) matrix.getElements()[i][j], result);
                }
            }
        } else {
            result.add(node);
        }
    }

    /**
     * Zip two vectors into pairs
     */
    public static final MathFunction ZIP = FunctionBuilder
            .named("zip")
            .describedAs("Zips two or more vectors into a matrix of rows, truncated to the shortest")
            .withParams("a", "b")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                // Get all vectors
                List<NodeVector> vectors = new ArrayList<>();
                for (NodeConstant arg : args) {
                    vectors.add(ctx.requireVector(arg));
                }

                // Find minimum length
                int minLen = vectors.stream().mapToInt(NodeVector::size).min().orElse(0);

                // Create result matrix
                Node[][] result = new Node[minLen][vectors.size()];
                for (int i = 0; i < minLen; i++) {
                    for (int j = 0; j < vectors.size(); j++) {
                        result[i][j] = vectors.get(j).getElement(i);
                    }
                }
                return new NodeMatrix(result);
            });

    /**
     * Repeat vector n times
     */
    public static final MathFunction REPEAT = FunctionBuilder
            .named("repeat")
            .alias("replicate", "strrepeat")
            .describedAs("Returns the value repeated n times: a longer vector, or a longer string")
            .withParams("value", "n")
            .withParams("vector", "n")
            .withParams("str", "n")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.any(), ArgTypes.integer())
            .implementedBy((input, n, ctx) -> Sequences.repeat(input, n));

    // ==================== Predicate Functions ====================

    /**
     * Count elements equal to value
     */
    public static final MathFunction COUNT = FunctionBuilder
            .named("count")
            .describedAs("Returns how many elements equal the given value, or match the given predicate")
            .withParams("vector", "value")
            .withParams("vector", "predicate")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.vector(), ArgTypes.any())
            .implementedBy((vector, target, ctx) -> {
                boolean byPredicate = Callbacks.isFunction(target);
                int count = 0;
                for (NodeConstant element : Callbacks.elements(vector, ctx)) {
                    // Value matching goes through equalTo, as indexof and contains do, so
                    // count({1 m, 100 cm}, 1 m) sees one value in two spellings
                    boolean matches = byPredicate
                            ? Callbacks.test(ctx, (NodeFunction) target, element)
                            : element.equalTo(target);
                    if (matches) {
                        count++;
                    }
                }
                return new NodeRational(count);
            });

    /**
     * Check if any element is truthy (non-zero)
     */
    public static final MathFunction ANY = FunctionBuilder
            .named("any")
            .describedAs("Returns true if any element in the collection is truthy (non-zero)")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                for (NodeConstant arg : args) {
                    if (arg instanceof NodeVector vector) {
                        for (int i = 0; i < vector.size(); i++) {
                            if (ctx.toBoolean((NodeConstant) vector.getElement(i))) {
                                return new NodeBoolean(true);
                            }
                        }
                    } else {
                        if (ctx.toBoolean(arg)) {
                            return new NodeBoolean(true);
                        }
                    }
                }
                return new NodeBoolean(false);
            });

    /**
     * Check if all elements are truthy (non-zero)
     */
    public static final MathFunction ALL = FunctionBuilder
            .named("all")
            .describedAs("Returns true if all elements in the collection are truthy (non-zero)")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                for (NodeConstant arg : args) {
                    if (arg instanceof NodeVector vector) {
                        for (int i = 0; i < vector.size(); i++) {
                            if (!ctx.toBoolean((NodeConstant) vector.getElement(i))) {
                                return new NodeBoolean(false);
                            }
                        }
                    } else {
                        if (!ctx.toBoolean(arg)) {
                            return new NodeBoolean(false);
                        }
                    }
                }
                return new NodeBoolean(true);
            });

    /**
     * Check if no elements are truthy
     */
    public static final MathFunction NONE = FunctionBuilder
            .named("none")
            .describedAs("Returns true if no elements in the collection are truthy")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                for (NodeConstant arg : args) {
                    if (arg instanceof NodeVector vector) {
                        for (int i = 0; i < vector.size(); i++) {
                            if (ctx.toBoolean((NodeConstant) vector.getElement(i))) {
                                return new NodeBoolean(false);
                            }
                        }
                    } else {
                        if (ctx.toBoolean(arg)) {
                            return new NodeBoolean(false);
                        }
                    }
                }
                return new NodeBoolean(true);
            });

    // ==================== Generator Functions ====================

    /**
     * Generate range [start, end] with optional step
     */
    public static final MathFunction RANGEGEN = FunctionBuilder
            .named("seq")
            .alias("sequence", "arange")
            .describedAs("Generates a sequence of numbers from start to end (inclusive) with the given step")
            .withParams("end")
            .withParams("start", "end")
            .withParams("start", "end", "step")
            .inCategory(MathFunction.Category.VECTOR)
            .takingBetween(1, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double start, end, step;

                if (args.size() == 1) {
                    start = 0;
                    end = ctx.toDouble(args.getFirst());
                    step = 1;
                } else if (args.size() == 2) {
                    start = ctx.toDouble(args.get(0));
                    end = ctx.toDouble(args.get(1));
                    step = 1;
                } else {
                    start = ctx.toDouble(args.get(0));
                    end = ctx.toDouble(args.get(1));
                    step = ctx.toDouble(args.get(2));
                }

                if (step == 0) {
                    throw new TypeError("seq: step cannot be 0");
                }

                List<Node> result = new ArrayList<>();
                if (step > 0) {
                    for (double i = start; i <= end; i += step) {
                        result.add(new NodeDouble(i));
                    }
                } else {
                    for (double i = start; i >= end; i += step) {
                        result.add(new NodeDouble(i));
                    }
                }
                return new NodeVector(result.toArray(Node[]::new));
            });

    /**
     * Generate n evenly spaced values between start and end
     */
    public static final MathFunction LINSPACE = FunctionBuilder
            .named("linspace")
            .describedAs("Generates n evenly spaced values between start and end (inclusive)")
            .withParams("start", "end", "n")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.number(), ArgTypes.number(), ArgTypes.integer())
            .implementedBy((start, end, n, ctx) -> {
                if (n < 1) {
                    throw new TypeError("linspace: n must be at least 1");
                }

                if (n == 1) {
                    return new NodeVector(new Node[]{new NodeDouble(start)});
                }

                Node[] result = new Node[n];
                double step = (end - start) / (n - 1);
                for (int i = 0; i < n; i++) {
                    result[i] = new NodeDouble(start + i * step);
                }
                return new NodeVector(result);
            });

    /**
     * Fill with value
     */
    public static final MathFunction FILL = FunctionBuilder
            .named("fill")
            .describedAs("Creates a vector of n copies of value")
            .withParams("value", "n")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.any(), ArgTypes.integer())
            .implementedBy((value, n, ctx) -> {
                if (n < 0) n = 0;

                Node[] result = new Node[n];
                for (int i = 0; i < n; i++) {
                    result[i] = value;
                }
                return new NodeVector(result);
            });

    /**
     * Gets all vector manipulation functions.
     */
    public static List<MathFunction> all() {
        return List.of(
                TAKE, DROP, SLICE,
                GET, INDEXOF, CONTAINS,
                UNIQUE, CONCAT, FLATTEN, ZIP, REPEAT,
                COUNT, ANY, ALL, NONE,
                RANGEGEN, LINSPACE, FILL
        );
    }
}
