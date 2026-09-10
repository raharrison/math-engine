package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.Sequences;
import uk.co.ryanharrison.mathengine.utils.StatUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Aggregate functions over collections, producing either a scalar (sum, mean)
 * or a transformed collection (sort, reverse).
 * <p>
 * Each one flattens its arguments first, so {@code sum(1, 2, 3)} and
 * {@code sum({1, 2, 3})} agree, and combines them with the shared value
 * arithmetic, so units, percentages and exact rationals survive.
 */
public final class VectorFunctions {

    private VectorFunctions() {
    }

    // ==================== Aggregation Functions ====================

    /**
     * Sum of all values.
     */
    public static final MathFunction SUM = FunctionBuilder
            .named("sum")
            .describedAs("Returns the sum of all values in the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> elements = ctx.flattenArguments(args);
                if (elements.isEmpty()) {
                    return new NodeRational(0);
                }

                return sum(elements);
            });

    /** Product of all values. */
    public static final MathFunction PRODUCT = FunctionBuilder
            .named("product")
            .describedAs("Returns the product of all values in the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> elements = ctx.flattenArguments(args);
                if (elements.isEmpty()) {
                    return new NodeRational(1);
                }

                NodeConstant product = elements.getFirst();
                for (int i = 1; i < elements.size(); i++) {
                    product = product.multiply(elements.get(i));
                }
                return product;
            });

    /** Smallest value, comparing units after conversion. */
    public static final MathFunction MIN = FunctionBuilder
            .named("min")
            .describedAs("Returns the smallest value in the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                return extreme(ctx.flattenArguments(args), "min", -1);
            });

    /** Largest value, comparing units after conversion. */
    public static final MathFunction MAX = FunctionBuilder
            .named("max")
            .describedAs("Returns the largest value in the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                return extreme(ctx.flattenArguments(args), "max", 1);
            });

    // ==================== Statistical Functions ====================

    /** Arithmetic mean. */
    public static final MathFunction MEAN = FunctionBuilder
            .named("mean")
            .describedAs("Returns the arithmetic mean of all values in the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> elements = ctx.flattenArguments(args);
                if (elements.isEmpty()) {
                    throw new TypeError("mean requires at least one element");
                }

                return sum(elements).divide(new NodeRational(elements.size()));
            });

    /** Middle value, averaging the two middle values when the count is even. */
    public static final MathFunction MEDIAN = FunctionBuilder
            .named("median")
            .describedAs("Returns the median of the collection; averages the two middle values for even-length collections")
            .withParams("values")
            .inCategory(MathFunction.Category.STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> elements = ctx.flattenArguments(args);
                if (elements.isEmpty()) {
                    throw new TypeError("median requires at least one element");
                }

                List<NodeConstant> sorted = elements.stream()
                        .sorted(NodeConstant::compareTo)
                        .toList();

                int n = sorted.size();
                if (n % 2 == 1) {
                    return sorted.get(n / 2);
                } else {
                    NodeConstant lo = sorted.get(n / 2 - 1);
                    NodeConstant hi = sorted.get(n / 2);
                    return lo.add(hi).divide(new NodeRational(BigRational.TWO));
                }
            });

    /**
     * Sample variance
     */
    public static final MathFunction VARIANCE = FunctionBuilder
            .named("variance")
            .describedAs("Returns the sample variance of the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> elements = ctx.flattenArguments(args);
                if (elements.size() < 2) {
                    throw new TypeError("variance requires at least two elements");
                }

                double[] values = elements.stream()
                        .mapToDouble(e -> ctx.toDouble(e))
                        .toArray();

                return new NodeDouble(StatUtils.variance(values));
            });

    /**
     * Sample standard deviation
     */
    public static final MathFunction STDDEV = FunctionBuilder
            .named("stddev")
            .describedAs("Returns the sample standard deviation of the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> elements = ctx.flattenArguments(args);
                if (elements.size() < 2) {
                    throw new TypeError("stddev requires at least two elements");
                }

                double[] values = elements.stream()
                        .mapToDouble(e -> ctx.toDouble(e))
                        .toArray();

                return new NodeDouble(StatUtils.standardDeviation(values));
            });

    // ==================== Vector Transformation Functions ====================

    /**
     * Sort vector ascending
     */
    public static final MathFunction SORT = FunctionBuilder
            .named("sort")
            .describedAs("Returns a sorted copy of the vector in ascending order")
            .withParams("vector")
            .inCategory(MathFunction.Category.VECTOR)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                Node[] elements = ctx.requireVector(arg).getElements();
                Arrays.sort(elements, (a, b) -> ((NodeConstant) a).compareTo((NodeConstant) b));
                return new NodeVector(elements);
            });

    /** Reverses a vector or a string. */
    public static final MathFunction REVERSE = FunctionBuilder
            .named("reverse")
            .alias("strreverse")
            .describedAs("Returns the vector or string with its elements in reversed order")
            .withParams("vector")
            .withParams("str")
            .inCategory(MathFunction.Category.VECTOR)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> Sequences.reverse(arg));

    /** Element count of a vector, character count of a string, or [rows, cols] of a matrix. */
    public static final MathFunction LEN = FunctionBuilder
            .named("len")
            .alias("length", "strlen")
            .describedAs("Returns the number of elements in a vector, the number of characters in a string, or [rows, cols] for a matrix")
            .withParams("vector")
            .withParams("str")
            .withParams("matrix")
            .inCategory(MathFunction.Category.VECTOR)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                if (arg instanceof NodeMatrix matrix) {
                    return new NodeVector(new Node[]{
                            new NodeRational(matrix.getRows()),
                            new NodeRational(matrix.getCols())
                    });
                }
                return new NodeRational(Sequences.length(arg));
            });

    /**
     * First element
     */
    public static final MathFunction FIRST = FunctionBuilder
            .named("first")
            .describedAs("Returns the first element of the vector")
            .withParams("vector")
            .inCategory(MathFunction.Category.VECTOR)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                NodeVector vector = ctx.requireVector(arg);
                if (vector.size() == 0) {
                    throw new TypeError("first: vector is empty");
                }
                return (NodeConstant) vector.getElement(0);
            });

    /**
     * Last element
     */
    public static final MathFunction LAST = FunctionBuilder
            .named("last")
            .describedAs("Returns the last element of the vector")
            .withParams("vector")
            .inCategory(MathFunction.Category.VECTOR)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                NodeVector vector = ctx.requireVector(arg);
                if (vector.size() == 0) {
                    throw new TypeError("last: vector is empty");
                }
                return (NodeConstant) vector.getElement(vector.size() - 1);
            });

    // ==================== All Functions ====================

    /**
     * Gets all vector functions.
     */
    private static NodeConstant sum(List<NodeConstant> elements) {
        NodeConstant total = elements.getFirst();
        for (int i = 1; i < elements.size(); i++) {
            total = total.add(elements.get(i));
        }
        return total;
    }

    /**
     * Picks the element that compares furthest in {@code direction}, which is -1 for min and 1 for max.
     */
    private static NodeConstant extreme(List<NodeConstant> elements, String name, int direction) {
        if (elements.isEmpty()) {
            throw new TypeError(name + " requires at least one element");
        }
        NodeConstant best = elements.getFirst();
        for (NodeConstant element : elements.subList(1, elements.size())) {
            if (Integer.signum(element.compareTo(best)) == direction) {
                best = element;
            }
        }
        return best;
    }

    public static List<MathFunction> all() {
        return List.of(SUM, PRODUCT, MIN, MAX, MEAN, MEDIAN, VARIANCE, STDDEV,
                SORT, REVERSE, LEN, FIRST, LAST);
    }

    /**
     * Gets aggregation functions (sum, product, min, max).
     */
    public static List<MathFunction> aggregation() {
        return List.of(SUM, PRODUCT, MIN, MAX);
    }

    /**
     * Gets statistical functions.
     */
    public static List<MathFunction> statistical() {
        return List.of(MEAN, MEDIAN, VARIANCE, STDDEV);
    }
}
