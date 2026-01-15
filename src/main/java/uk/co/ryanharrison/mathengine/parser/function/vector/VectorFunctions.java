package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
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
    public static final MathFunction SUM = new MathFunction() {
        @Override
        public String name() {
            return "sum";
        }

        @Override
        public String description() {
            return "Sum of all elements";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
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
            List<NodeConstant> elements = flattenToElements(args);
            if (elements.isEmpty()) {
                return new NodeRational(0);
            }

            NodeConstant sum = elements.getFirst();
            for (int i = 1; i < elements.size(); i++) {
                sum = ctx.applyNumericBinary(sum, elements.get(i),
                        BigRational::add, Double::sum);
            }
            return sum;
        }
    };

    /**
     * Product of all elements
     */
    public static final MathFunction PRODUCT = new MathFunction() {
        @Override
        public String name() {
            return "product";
        }

        @Override
        public String description() {
            return "Product of all elements";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
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
            List<NodeConstant> elements = flattenToElements(args);
            if (elements.isEmpty()) {
                return new NodeRational(1);
            }

            NodeConstant product = elements.getFirst();
            for (int i = 1; i < elements.size(); i++) {
                product = ctx.applyNumericBinary(product, elements.get(i),
                        BigRational::multiply, (a, b) -> a * b);
            }
            return product;
        }
    };

    /**
     * Minimum value
     */
    public static final MathFunction MIN = new MathFunction() {
        @Override
        public String name() {
            return "min";
        }

        @Override
        public String description() {
            return "Minimum value";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
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
            List<NodeConstant> elements = flattenToElements(args);
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
        }
    };

    /**
     * Maximum value
     */
    public static final MathFunction MAX = new MathFunction() {
        @Override
        public String name() {
            return "max";
        }

        @Override
        public String description() {
            return "Maximum value";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
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
            List<NodeConstant> elements = flattenToElements(args);
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
        }
    };

    // ==================== Statistical Functions ====================

    /**
     * Arithmetic mean
     */
    public static final MathFunction MEAN = new MathFunction() {
        @Override
        public String name() {
            return "mean";
        }

        @Override
        public String description() {
            return "Arithmetic mean";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.STATISTICAL;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            List<NodeConstant> elements = flattenToElements(args);
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
        }
    };

    /**
     * Median value
     */
    public static final MathFunction MEDIAN = new MathFunction() {
        @Override
        public String name() {
            return "median";
        }

        @Override
        public String description() {
            return "Median value";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.STATISTICAL;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            List<NodeConstant> elements = flattenToElements(args);
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
        }
    };

    /**
     * Sample variance
     */
    public static final MathFunction VARIANCE = new MathFunction() {
        @Override
        public String name() {
            return "variance";
        }

        @Override
        public String description() {
            return "Sample variance";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.STATISTICAL;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            List<NodeConstant> elements = flattenToElements(args);
            if (elements.size() < 2) {
                throw new TypeError("variance requires at least two elements");
            }

            double[] values = elements.stream()
                    .mapToDouble(e -> ctx.toNumber(e).doubleValue())
                    .toArray();

            return new NodeDouble(StatUtils.variance(values));
        }
    };

    /**
     * Sample standard deviation
     */
    public static final MathFunction STDDEV = new MathFunction() {
        @Override
        public String name() {
            return "stddev";
        }

        @Override
        public String description() {
            return "Sample standard deviation";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.STATISTICAL;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            List<NodeConstant> elements = flattenToElements(args);
            if (elements.size() < 2) {
                throw new TypeError("stddev requires at least two elements");
            }

            double[] values = elements.stream()
                    .mapToDouble(e -> ctx.toNumber(e).doubleValue())
                    .toArray();

            return new NodeDouble(StatUtils.standardDeviation(values));
        }
    };

    // ==================== Vector Transformation Functions ====================

    /**
     * Sort vector ascending
     */
    public static final MathFunction SORT = new MathFunction() {
        @Override
        public String name() {
            return "sort";
        }

        @Override
        public String description() {
            return "Sort vector ascending";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.getFirst(), "sort");
            Node[] elements = vector.getElements().clone();

            Arrays.sort(elements, Comparator.comparingDouble(n -> ((NodeConstant) n).doubleValue()));

            return new NodeVector(elements);
        }
    };

    /**
     * Reverse vector
     */
    public static final MathFunction REVERSE = new MathFunction() {
        @Override
        public String name() {
            return "reverse";
        }

        @Override
        public String description() {
            return "Reverse vector";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.getFirst(), "reverse");
            Node[] elements = vector.getElements();
            Node[] reversed = new Node[elements.length];

            for (int i = 0; i < elements.length; i++) {
                reversed[i] = elements[elements.length - 1 - i];
            }

            return new NodeVector(reversed);
        }
    };

    /**
     * Length/size of vector
     */
    public static final MathFunction LEN = new MathFunction() {
        @Override
        public String name() {
            return "len";
        }

        @Override
        public List<String> aliases() {
            return List.of("length");
        }

        @Override
        public String description() {
            return "Length of vector";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant arg = args.getFirst();
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
        }
    };

    /**
     * First element
     */
    public static final MathFunction FIRST = new MathFunction() {
        @Override
        public String name() {
            return "first";
        }

        @Override
        public String description() {
            return "First element of vector";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.getFirst(), "first");
            if (vector.size() == 0) {
                throw new TypeError("first: vector is empty");
            }
            return (NodeConstant) vector.getElement(0);
        }
    };

    /**
     * Last element
     */
    public static final MathFunction LAST = new MathFunction() {
        @Override
        public String name() {
            return "last";
        }

        @Override
        public String description() {
            return "Last element of vector";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.getFirst(), "last");
            if (vector.size() == 0) {
                throw new TypeError("last: vector is empty");
            }
            return (NodeConstant) vector.getElement(vector.size() - 1);
        }
    };

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

    // ==================== Helper Methods ====================

    /**
     * Flattens arguments (which may include vectors) to a list of scalar elements.
     */
    private static List<NodeConstant> flattenToElements(List<NodeConstant> args) {
        return args.stream()
                .flatMap(arg -> {
                    if (arg instanceof NodeVector vector) {
                        return Arrays.stream(vector.getElements())
                                .map(n -> (NodeConstant) n);
                    }
                    return java.util.stream.Stream.of(arg);
                })
                .toList();
    }
}
