package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.utils.StatUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Collection of vector and aggregate functions.
 * <p>
 * These functions operate on vectors (collections of values) and produce
 * either scalar results (sum, mean) or transformed vectors (sort, reverse).
 */
public final class VectorFunctions {

    private VectorFunctions() {
    }

    // ==================== Aggregation Functions ====================

    /**
     * Sum of all elements
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

                NodeConstant sum = elements.getFirst();
                for (int i = 1; i < elements.size(); i++) {
                    sum = ctx.applyNumericBinary(sum, elements.get(i),
                            BigRational::add, Double::sum);
                }
                return sum;
            });

    /**
     * Product of all elements
     */
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
                    product = ctx.applyNumericBinary(product, elements.get(i),
                            BigRational::multiply, (a, b) -> a * b);
                }
                return product;
            });

    /**
     * Minimum value
     */
    public static final MathFunction MIN = FunctionBuilder
            .named("min")
            .describedAs("Returns the smallest value in the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> elements = ctx.flattenArguments(args);
                if (elements.isEmpty()) {
                    throw new TypeError("min requires at least one element");
                }

                double min = Double.POSITIVE_INFINITY;
                NodeConstant minNode = null;

                for (NodeConstant elem : elements) {
                    double val = ctx.toNumber(elem).doubleValue();
                    if (val < min) {
                        min = val;
                        minNode = elem;
                    }
                }
                return minNode;
            });

    /**
     * Maximum value
     */
    public static final MathFunction MAX = FunctionBuilder
            .named("max")
            .describedAs("Returns the largest value in the collection")
            .withParams("values")
            .inCategory(MathFunction.Category.VECTOR)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> elements = ctx.flattenArguments(args);
                if (elements.isEmpty()) {
                    throw new TypeError("max requires at least one element");
                }

                double max = Double.NEGATIVE_INFINITY;
                NodeConstant maxNode = null;

                for (NodeConstant elem : elements) {
                    double val = ctx.toNumber(elem).doubleValue();
                    if (val > max) {
                        max = val;
                        maxNode = elem;
                    }
                }
                return maxNode;
            });

    // ==================== Statistical Functions ====================

    /**
     * Arithmetic mean
     */
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

                // Sum all elements (preserves units via applyNumericBinary)
                NodeConstant sum = elements.getFirst();
                for (int i = 1; i < elements.size(); i++) {
                    sum = ctx.applyNumericBinary(sum, elements.get(i),
                            BigRational::add, Double::sum);
                }

                // Divide by count (preserves units via applyMultiplicativeBinary)
                return ctx.applyMultiplicativeBinary(sum, new NodeRational(elements.size()),
                        BigRational::divide, (a, b) -> a / b, false);
            });

    /**
     * Median value
     */
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

                double[] values = elements.stream()
                        .mapToDouble(e -> ctx.toNumber(e).doubleValue())
                        .sorted()
                        .toArray();

                int n = values.length;
                if (n % 2 == 0) {
                    return new NodeDouble((values[n / 2 - 1] + values[n / 2]) / 2.0);
                } else {
                    return new NodeDouble(values[n / 2]);
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
                        .mapToDouble(e -> ctx.toNumber(e).doubleValue())
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
                        .mapToDouble(e -> ctx.toNumber(e).doubleValue())
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
                NodeVector vector = ctx.requireVector(arg);
                Node[] elements = vector.getElements().clone();

                Arrays.sort(elements, Comparator.comparingDouble(n -> ((NodeConstant) n).doubleValue()));

                return new NodeVector(elements);
            });

    /**
     * Reverse vector
     */
    public static final MathFunction REVERSE = FunctionBuilder
            .named("reverse")
            .describedAs("Returns the vector with elements in reversed order")
            .withParams("vector")
            .inCategory(MathFunction.Category.VECTOR)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                NodeVector vector = ctx.requireVector(arg);
                Node[] elements = vector.getElements();
                Node[] reversed = new Node[elements.length];

                for (int i = 0; i < elements.length; i++) {
                    reversed[i] = elements[elements.length - 1 - i];
                }

                return new NodeVector(reversed);
            });

    /**
     * Length/size of vector
     */
    public static final MathFunction LEN = FunctionBuilder
            .named("len")
            .alias("length")
            .describedAs("Returns the number of elements in a vector, or [rows, cols] for a matrix")
            .withParams("vector")
            .withParams("matrix")
            .inCategory(MathFunction.Category.VECTOR)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                if (arg instanceof NodeVector vector) {
                    return new NodeRational(vector.size());
                }
                if (arg instanceof NodeMatrix matrix) {
                    // Return [rows, cols] as a vector for matrix dimensions
                    return new NodeVector(new Node[]{
                            new NodeRational(matrix.getRows()),
                            new NodeRational(matrix.getCols())
                    });
                }
                return new NodeRational(1); // Scalar has length 1
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
