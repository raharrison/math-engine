package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;
import uk.co.ryanharrison.mathengine.utils.StatUtils;

import java.util.ArrayList;
import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.STATISTICAL;

/**
 * Advanced statistics, computed on the values rather than on a {@code double[]}.
 * <p>
 * A function that selects one of its inputs returns that input, and one whose answer is a
 * sum, difference, product or quotient of its inputs is written that way, so units,
 * percentages and exact rationals carry through. See {@link Statistics}.
 * <p>
 * Three answers are ratios by definition and so wear no label at all: {@code skewness},
 * {@code kurtosis} and {@code correlation}. Those still go through {@link StatUtils},
 * because a plain double really is what they are.
 */
public final class StatisticalFunctions {

    private StatisticalFunctions() {
    }

    /**
     * Range (max - min)
     */
    public static final MathFunction RANGE = FunctionBuilder
            .named("range")
            .describedAs("Returns the range (max - min) of the values")
            .withParams("values")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> sorted = Statistics.sorted(elements(args, ctx, 1, "range"));
                return sorted.getLast().subtract(sorted.getFirst());
            });

    /**
     * Percentile (0-100 or 0-1)
     */
    public static final MathFunction PERCENTILE = FunctionBuilder
            .named("percentile")
            .describedAs("Returns the p-th percentile of the values (p in 0-100 or 0-1)")
            .withParams("values", "p")
            .inCategory(STATISTICAL)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((vector, pValue, ctx) -> {
                NodeVector vec = ctx.requireVector(vector);
                double p = ctx.toDouble(pValue);

                // Allow both 0-100 and 0-1 formats
                if (p > 1) {
                    p = p / 100.0;
                }

                if (p < 0 || p > 1) {
                    throw new DomainException("percentile must be between 0 and 1 (or 0 and 100)");
                }

                return Statistics.percentile(sortedVector(vec, ctx, 1, "percentile"), p);
            });

    /**
     * Interquartile range (Q3 - Q1)
     */
    public static final MathFunction IQR = FunctionBuilder
            .named("iqr")
            .describedAs("Returns the interquartile range (Q3 - Q1) of the values")
            .withParams("values")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> sorted = Statistics.sorted(elements(args, ctx, 4, "iqr"));
                return Statistics.percentile(sorted, 0.75).subtract(Statistics.percentile(sorted, 0.25));
            });

    /**
     * Geometric mean
     */
    public static final MathFunction GMEAN = FunctionBuilder
            .named("gmean")
            .alias("geometricmean")
            .describedAs("Returns the geometric mean of the values")
            .withParams("values")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> values = elements(args, ctx, 1, "gmean");
                // Computed through logarithms, which is what keeps a long product from
                // overflowing, so the magnitude is mapped back onto one of the inputs
                double result = StatUtils.geometricMean(magnitudes(values, ctx));
                return labelledLike(values.getFirst(), result);
            });

    /**
     * Harmonic mean
     */
    public static final MathFunction HMEAN = FunctionBuilder
            .named("hmean")
            .alias("harmonicmean")
            .describedAs("Returns the harmonic mean of the values")
            .withParams("values")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> values = elements(args, ctx, 1, "hmean");

                var reciprocals = new ArrayList<NodeConstant>(values.size());
                for (NodeConstant value : values) {
                    if (ctx.toDouble(value) == 0.0) {
                        throw new ArithmeticException("Harmonic mean is undefined when data contains zero");
                    }
                    reciprocals.add(new NodeRational(1).divide(value));
                }
                return new NodeRational(values.size()).divide(Statistics.sum(reciprocals));
            });

    /**
     * Root mean square
     */
    public static final MathFunction RMS = FunctionBuilder
            .named("rms")
            .alias("rootmeansquare")
            .describedAs("Returns the root mean square (RMS) of the values")
            .withParams("values")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> values = elements(args, ctx, 1, "rms");

                var squares = new ArrayList<NodeConstant>(values.size());
                for (NodeConstant value : values) {
                    squares.add(value.multiply(value));
                }
                return Statistics.mean(squares).mapMagnitude(Math::sqrt);
            });

    /**
     * Skewness (measure of asymmetry)
     */
    public static final MathFunction SKEWNESS = FunctionBuilder
            .named("skewness")
            .describedAs("Returns the skewness (asymmetry measure) of the values; a ratio, so it carries no unit")
            .withParams("values")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] values = ctx.flattenToDoubles(args);
                ctx.requireMinSize(values, 3);
                return new NodeDouble(StatUtils.skewness(values));
            });

    /**
     * Kurtosis (measure of tailedness)
     */
    public static final MathFunction KURTOSIS = FunctionBuilder
            .named("kurtosis")
            .describedAs("Returns the excess kurtosis (tailedness relative to normal) of the values; a ratio, so it carries no unit")
            .withParams("values")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] values = ctx.flattenToDoubles(args);
                ctx.requireMinSize(values, 4);
                return new NodeDouble(StatUtils.kurtosis(values));
            });

    /**
     * Covariance between two vectors
     */
    public static final MathFunction COVARIANCE = FunctionBuilder
            .named("covariance")
            .alias("cov")
            .describedAs("Returns the sample covariance between two vectors xs and ys")
            .withParams("xs", "ys")
            .inCategory(STATISTICAL)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((v1, v2, ctx) -> covariance(v1, v2, ctx, "covariance"));

    /**
     * Correlation coefficient between two vectors
     */
    public static final MathFunction CORRELATION = FunctionBuilder
            .named("correlation")
            .alias("corr")
            .describedAs("Returns the Pearson correlation coefficient between vectors xs and ys; a ratio, so it carries no unit")
            .withParams("xs", "ys")
            .inCategory(STATISTICAL)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((v1, v2, ctx) -> {
                NodeConstant covariance = covariance(v1, v2, ctx, "correlation");
                NodeConstant spread = Statistics.standardDeviation(ctx.requireVector(v1).toList())
                        .multiply(Statistics.standardDeviation(ctx.requireVector(v2).toList()));
                // Two like quantities divide into a plain ratio, which is what a
                // correlation is; no case is needed to strip the label
                return covariance.divide(spread);
            });

    /**
     * Mode (most frequent value)
     */
    public static final MathFunction MODE = FunctionBuilder
            .named("mode")
            .describedAs("Returns the most frequently occurring value in the collection; returns the smallest if there are multiple modes")
            .withParams("values")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                List<NodeConstant> sorted = Statistics.sorted(elements(args, ctx, 1, "mode"));

                // Equal values are adjacent once sorted, so the longest run is the mode and
                // its first member is the smallest. Equality converts units, so 1 m and
                // 100 cm count as the same value and come back in the spelling seen first
                NodeConstant mode = sorted.getFirst();
                int best = 0;
                int run = 0;
                for (int i = 0; i < sorted.size(); i++) {
                    run = i > 0 && sorted.get(i).equalTo(sorted.get(i - 1)) ? run + 1 : 1;
                    if (run > best) {
                        best = run;
                        mode = sorted.get(i - run + 1);
                    }
                }
                return mode;
            });

    /**
     * Quartile (1, 2, or 3)
     */
    public static final MathFunction QUARTILE = FunctionBuilder
            .named("quartile")
            .describedAs("Returns the q-th quartile of the values (q = 1, 2, or 3)")
            .withParams("values", "q")
            .inCategory(STATISTICAL)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((vector, qValue, ctx) -> {
                NodeVector vec = ctx.requireVector(vector);
                int q = (int) ctx.toDouble(qValue);

                if (q < 1 || q > 3) {
                    throw new DomainException("quartile must be 1, 2, or 3");
                }

                return Statistics.percentile(sortedVector(vec, ctx, 1, "quartile"), q * 0.25);
            });

    // ==================== Helpers ====================

    /**
     * The arguments flattened to values, checked for size.
     */
    private static List<NodeConstant> elements(List<NodeConstant> args, FunctionContext ctx,
                                               int minimum, String name) {
        List<NodeConstant> elements = ctx.flattenArguments(args);
        if (elements.size() < minimum) {
            throw new TypeError(name + " requires at least " + minimum +
                    (minimum == 1 ? " element" : " elements"));
        }
        return elements;
    }

    private static List<NodeConstant> sortedVector(NodeVector vector, FunctionContext ctx,
                                                   int minimum, String name) {
        return Statistics.sorted(elements(List.of(vector), ctx, minimum, name));
    }

    private static double[] magnitudes(List<NodeConstant> values, FunctionContext ctx) {
        double[] magnitudes = new double[values.size()];
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = ctx.toDouble(values.get(i));
        }
        return magnitudes;
    }

    /**
     * Puts a magnitude computed in doubles back under the label the inputs were wearing.
     */
    private static NodeConstant labelledLike(NodeConstant sample, double magnitude) {
        return sample.mapMagnitude(ignored -> magnitude);
    }

    private static NodeConstant covariance(NodeConstant v1, NodeConstant v2,
                                           FunctionContext ctx, String name) {
        List<NodeConstant> xs = ctx.requireVector(v1).toList();
        List<NodeConstant> ys = ctx.requireVector(v2).toList();

        if (xs.size() != ys.size()) {
            throw new DomainException(name + " requires vectors of equal length");
        }
        if (xs.size() < 2) {
            throw new TypeError(name + " requires at least 2 elements");
        }

        NodeConstant xMean = Statistics.mean(xs);
        NodeConstant yMean = Statistics.mean(ys);
        NodeConstant total = null;
        for (int i = 0; i < xs.size(); i++) {
            NodeConstant product = xs.get(i).subtract(xMean).multiply(ys.get(i).subtract(yMean));
            total = total == null ? product : total.add(product);
        }
        return total.divide(new NodeRational(xs.size() - 1));
    }

    /**
     * Gets all statistical functions.
     */
    public static List<MathFunction> all() {
        return List.of(RANGE, PERCENTILE, IQR, GMEAN, HMEAN, RMS,
                SKEWNESS, KURTOSIS, COVARIANCE, CORRELATION, MODE, QUARTILE);
    }
}
