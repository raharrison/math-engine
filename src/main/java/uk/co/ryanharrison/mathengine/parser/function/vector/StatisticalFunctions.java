package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;
import uk.co.ryanharrison.mathengine.utils.StatUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Collection of advanced statistical functions.
 * <p>
 * These functions delegate to {@link StatUtils} for their implementations where possible.
 * </p>
 */
public final class StatisticalFunctions {

    private StatisticalFunctions() {
    }

    /**
     * Range (max - min)
     */
    public static final MathFunction RANGE = new MathFunction() {
        @Override
        public String name() {
            return "range";
        }

        @Override
        public String description() {
            return "Range (max - min)";
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
            double[] values = flattenToDoubles(args, ctx);
            if (values.length == 0) {
                throw new TypeError("range requires at least one element");
            }
            return new NodeDouble(StatUtils.range(values));
        }
    };

    /**
     * Percentile (0-100 or 0-1)
     */
    public static final MathFunction PERCENTILE = new MathFunction() {
        @Override
        public String name() {
            return "percentile";
        }

        @Override
        public String description() {
            return "Calculate percentile";
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
            return Category.STATISTICAL;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "percentile");
            double p = ctx.toNumber(args.get(1)).doubleValue();

            // Allow both 0-100 and 0-1 formats
            if (p > 1) {
                p = p / 100.0;
            }

            if (p < 0 || p > 1) {
                throw new IllegalArgumentException("percentile must be between 0 and 1 (or 0 and 100)");
            }

            double[] values = vectorToDoubles(vector, ctx);
            if (values.length == 0) {
                throw new TypeError("percentile requires non-empty vector");
            }

            return new NodeDouble(StatUtils.percentile(values, p));
        }
    };

    /**
     * Interquartile range (Q3 - Q1)
     */
    public static final MathFunction IQR = new MathFunction() {
        @Override
        public String name() {
            return "iqr";
        }

        @Override
        public String description() {
            return "Interquartile range (Q3 - Q1)";
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
            double[] values = flattenToDoubles(args, ctx);
            if (values.length < 4) {
                throw new TypeError("iqr requires at least 4 elements");
            }

            return new NodeDouble(StatUtils.interQuartileRange(values));
        }
    };

    /**
     * Geometric mean
     */
    public static final MathFunction GMEAN = new MathFunction() {
        @Override
        public String name() {
            return "gmean";
        }

        @Override
        public List<String> aliases() {
            return List.of("geometricmean");
        }

        @Override
        public String description() {
            return "Geometric mean";
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
            double[] values = flattenToDoubles(args, ctx);
            if (values.length == 0) {
                throw new TypeError("gmean requires at least one element");
            }
            return new NodeDouble(StatUtils.geometricMean(values));
        }
    };

    /**
     * Harmonic mean
     */
    public static final MathFunction HMEAN = new MathFunction() {
        @Override
        public String name() {
            return "hmean";
        }

        @Override
        public List<String> aliases() {
            return List.of("harmonicmean");
        }

        @Override
        public String description() {
            return "Harmonic mean";
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
            double[] values = flattenToDoubles(args, ctx);
            if (values.length == 0) {
                throw new TypeError("hmean requires at least one element");
            }
            return new NodeDouble(StatUtils.harmonicMean(values));
        }
    };

    /**
     * Root mean square
     */
    public static final MathFunction RMS = new MathFunction() {
        @Override
        public String name() {
            return "rms";
        }

        @Override
        public List<String> aliases() {
            return List.of("rootmeansquare");
        }

        @Override
        public String description() {
            return "Root mean square";
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
            double[] values = flattenToDoubles(args, ctx);
            if (values.length == 0) {
                throw new TypeError("rms requires at least one element");
            }

            return new NodeDouble(StatUtils.rootmeanSquare(values));
        }
    };

    /**
     * Skewness (measure of asymmetry)
     */
    public static final MathFunction SKEWNESS = new MathFunction() {
        @Override
        public String name() {
            return "skewness";
        }

        @Override
        public String description() {
            return "Skewness (asymmetry measure)";
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
            double[] values = flattenToDoubles(args, ctx);
            if (values.length < 3) {
                throw new TypeError("skewness requires at least 3 elements");
            }

            return new NodeDouble(StatUtils.skewness(values));
        }
    };

    /**
     * Kurtosis (measure of tailedness)
     */
    public static final MathFunction KURTOSIS = new MathFunction() {
        @Override
        public String name() {
            return "kurtosis";
        }

        @Override
        public String description() {
            return "Kurtosis (excess, relative to normal)";
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
            double[] values = flattenToDoubles(args, ctx);
            if (values.length < 4) {
                throw new TypeError("kurtosis requires at least 4 elements");
            }

            return new NodeDouble(StatUtils.kurtosis(values));
        }
    };

    /**
     * Covariance between two vectors
     */
    public static final MathFunction COVARIANCE = new MathFunction() {
        @Override
        public String name() {
            return "covariance";
        }

        @Override
        public List<String> aliases() {
            return List.of("cov");
        }

        @Override
        public String description() {
            return "Covariance between two vectors";
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
            return Category.STATISTICAL;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector v1 = ctx.requireVector(args.get(0), "covariance");
            NodeVector v2 = ctx.requireVector(args.get(1), "covariance");

            if (v1.size() != v2.size()) {
                throw new IllegalArgumentException("covariance requires vectors of equal length");
            }
            if (v1.size() < 2) {
                throw new TypeError("covariance requires at least 2 elements");
            }

            double[] x = vectorToDoubles(v1, ctx);
            double[] y = vectorToDoubles(v2, ctx);

            return new NodeDouble(StatUtils.covariance(x, y));
        }
    };

    /**
     * Correlation coefficient between two vectors
     */
    public static final MathFunction CORRELATION = new MathFunction() {
        @Override
        public String name() {
            return "correlation";
        }

        @Override
        public List<String> aliases() {
            return List.of("corr");
        }

        @Override
        public String description() {
            return "Pearson correlation coefficient";
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
            return Category.STATISTICAL;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector v1 = ctx.requireVector(args.get(0), "correlation");
            NodeVector v2 = ctx.requireVector(args.get(1), "correlation");

            if (v1.size() != v2.size()) {
                throw new IllegalArgumentException("correlation requires vectors of equal length");
            }
            if (v1.size() < 2) {
                throw new TypeError("correlation requires at least 2 elements");
            }

            double[] x = vectorToDoubles(v1, ctx);
            double[] y = vectorToDoubles(v2, ctx);

            return new NodeDouble(StatUtils.correlationCoefficient(x, y));
        }
    };

    /**
     * Mode (most frequent value)
     */
    public static final MathFunction MODE = new MathFunction() {
        @Override
        public String name() {
            return "mode";
        }

        @Override
        public String description() {
            return "Most frequent value";
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
            double[] values = flattenToDoubles(args, ctx);
            if (values.length == 0) {
                throw new TypeError("mode requires at least one element");
            }

            // StatUtils.mode returns all modes; return the first one
            double[] modes = StatUtils.mode(values);
            return new NodeDouble(modes[0]);
        }
    };

    /**
     * Quartile (1, 2, or 3)
     */
    public static final MathFunction QUARTILE = new MathFunction() {
        @Override
        public String name() {
            return "quartile";
        }

        @Override
        public String description() {
            return "Calculate quartile (1, 2, or 3)";
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
            return Category.STATISTICAL;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "quartile");
            int q = (int) ctx.toNumber(args.get(1)).doubleValue();

            if (q < 1 || q > 3) {
                throw new IllegalArgumentException("quartile must be 1, 2, or 3");
            }

            double[] values = vectorToDoubles(vector, ctx);
            if (values.length == 0) {
                throw new TypeError("quartile requires non-empty vector");
            }

            return new NodeDouble(StatUtils.quartile(values, q));
        }
    };

    // ==================== Helper Methods ====================

    private static double[] flattenToDoubles(List<NodeConstant> args, FunctionContext ctx) {
        return args.stream()
                .flatMap(arg -> {
                    if (arg instanceof NodeVector vector) {
                        return Arrays.stream(vector.getElements())
                                .map(n -> (NodeConstant) n);
                    }
                    return java.util.stream.Stream.of(arg);
                })
                .mapToDouble(n -> ctx.toNumber(n).doubleValue())
                .toArray();
    }

    private static double[] vectorToDoubles(NodeVector vector, FunctionContext ctx) {
        return Arrays.stream(vector.getElements())
                .mapToDouble(n -> ctx.toNumber((NodeConstant) n).doubleValue())
                .toArray();
    }


    /**
     * Gets all statistical functions.
     */
    public static List<MathFunction> all() {
        return List.of(RANGE, PERCENTILE, IQR, GMEAN, HMEAN, RMS,
                SKEWNESS, KURTOSIS, COVARIANCE, CORRELATION, MODE, QUARTILE);
    }
}
