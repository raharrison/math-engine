package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

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
            .describedAs("Take first n elements")
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
            .describedAs("Drop first n elements")
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
            .describedAs("Slice vector [start, end) with optional step")
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
            .describedAs("Get element at index (0-based)")
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
     * Find index of element
     */
    public static final MathFunction INDEXOF = FunctionBuilder
            .named("indexof")
            .alias("find")
            .describedAs("Find first index of element (-1 if not found)")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.vector(), ArgTypes.number())
            .implementedBy((vector, target, ctx) -> {
                for (int i = 0; i < vector.size(); i++) {
                    double val = ctx.toNumber((NodeConstant) vector.getElement(i)).doubleValue();
                    if (val == target) {
                        return new NodeRational(i);
                    }
                }
                return new NodeRational(-1);
            });

    /**
     * Check if vector contains element
     */
    public static final MathFunction CONTAINS = FunctionBuilder
            .named("contains")
            .alias("includes")
            .describedAs("Check if vector contains element")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.vector(), ArgTypes.number())
            .implementedBy((vector, target, ctx) -> {
                for (int i = 0; i < vector.size(); i++) {
                    double val = ctx.toNumber((NodeConstant) vector.getElement(i)).doubleValue();
                    if (val == target) {
                        return new NodeBoolean(true);
                    }
                }
                return new NodeBoolean(false);
            });

    // ==================== Transformation Functions ====================

    /**
     * Remove duplicates
     */
    public static final MathFunction UNIQUE = FunctionBuilder
            .named("unique")
            .alias("distinct")
            .describedAs("Remove duplicate elements")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.vector())
            .implementedBy((vector, ctx) -> {
                Set<Double> seen = new LinkedHashSet<>();
                List<Node> result = new ArrayList<>();

                for (int i = 0; i < vector.size(); i++) {
                    double val = ctx.toNumber((NodeConstant) vector.getElement(i)).doubleValue();
                    if (seen.add(val)) {
                        result.add(vector.getElement(i));
                    }
                }
                return new NodeVector(result.toArray(Node[]::new));
            });

    /**
     * Concatenate vectors
     */
    public static final MathFunction CONCAT = FunctionBuilder
            .named("concat")
            .alias("append")
            .describedAs("Concatenate vectors")
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
            .describedAs("Flatten nested vectors")
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
            .describedAs("Zip vectors into matrix of pairs")
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
            .alias("replicate")
            .describedAs("Repeat value or vector n times")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.any(), ArgTypes.integer())
            .implementedBy((input, n, ctx) -> {
                if (n < 0) n = 0;

                List<Node> result = new ArrayList<>();

                // Handle both scalar and vector inputs
                if (input instanceof NodeVector vector) {
                    for (int rep = 0; rep < n; rep++) {
                        for (int i = 0; i < vector.size(); i++) {
                            result.add(vector.getElement(i));
                        }
                    }
                } else {
                    // Scalar: repeat the value n times
                    for (int rep = 0; rep < n; rep++) {
                        result.add(input);
                    }
                }

                return new NodeVector(result.toArray(Node[]::new));
            });

    // ==================== Predicate Functions ====================

    /**
     * Count elements equal to value
     */
    public static final MathFunction COUNT = FunctionBuilder
            .named("count")
            .describedAs("Count occurrences of value")
            .inCategory(MathFunction.Category.VECTOR)
            .takingTyped(ArgTypes.vector(), ArgTypes.number())
            .implementedBy((vector, target, ctx) -> {
                int count = 0;
                for (int i = 0; i < vector.size(); i++) {
                    double val = ctx.toNumber((NodeConstant) vector.getElement(i)).doubleValue();
                    if (val == target) count++;
                }
                return new NodeRational(count);
            });

    /**
     * Check if any element is truthy (non-zero)
     */
    public static final MathFunction ANY = FunctionBuilder
            .named("any")
            .describedAs("Check if any element is truthy")
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
            .describedAs("Check if all elements are truthy")
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
            .describedAs("Check if no elements are truthy")
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
     * Generate range [start, end) with optional step
     */
    public static final MathFunction RANGEGEN = FunctionBuilder
            .named("seq")
            .alias("sequence", "arange")
            .describedAs("Generate sequence [start, end] with step")
            .inCategory(MathFunction.Category.VECTOR)
            .takingBetween(1, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double start, end, step;

                if (args.size() == 1) {
                    start = 0;
                    end = ctx.toNumber(args.getFirst()).doubleValue();
                    step = 1;
                } else if (args.size() == 2) {
                    start = ctx.toNumber(args.get(0)).doubleValue();
                    end = ctx.toNumber(args.get(1)).doubleValue();
                    step = 1;
                } else {
                    start = ctx.toNumber(args.get(0)).doubleValue();
                    end = ctx.toNumber(args.get(1)).doubleValue();
                    step = ctx.toNumber(args.get(2)).doubleValue();
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
            .describedAs("Generate n evenly spaced values")
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
            .describedAs("Create vector filled with value")
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
