package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;
import uk.co.ryanharrison.mathengine.utils.StatUtils;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.STATISTICAL;

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
    public static final MathFunction RANGE = FunctionBuilder
            .named("range")
            .describedAs("Range (max - min)")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] values = ctx.flattenToDoubles(args);
                ctx.requireNonEmpty(values);
                return new NodeDouble(StatUtils.range(values));
            });

    /**
     * Percentile (0-100 or 0-1)
     */
    public static final MathFunction PERCENTILE = FunctionBuilder
            .named("percentile")
            .describedAs("Calculate percentile")
            .inCategory(STATISTICAL)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((vector, pValue, ctx) -> {
                NodeVector vec = ctx.requireVector(vector);
                double p = ctx.toNumber(pValue).doubleValue();

                // Allow both 0-100 and 0-1 formats
                if (p > 1) {
                    p = p / 100.0;
                }

                if (p < 0 || p > 1) {
                    throw new IllegalArgumentException("percentile must be between 0 and 1 (or 0 and 100)");
                }

                double[] values = ctx.toDoubleArray(vec);
                ctx.requireNonEmpty(values);

                return new NodeDouble(StatUtils.percentile(values, p));
            });

    /**
     * Interquartile range (Q3 - Q1)
     */
    public static final MathFunction IQR = FunctionBuilder
            .named("iqr")
            .describedAs("Interquartile range (Q3 - Q1)")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] values = ctx.flattenToDoubles(args);
                ctx.requireMinSize(values, 4);
                return new NodeDouble(StatUtils.interQuartileRange(values));
            });

    /**
     * Geometric mean
     */
    public static final MathFunction GMEAN = FunctionBuilder
            .named("gmean")
            .alias("geometricmean")
            .describedAs("Geometric mean")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] values = ctx.flattenToDoubles(args);
                ctx.requireNonEmpty(values);
                return new NodeDouble(StatUtils.geometricMean(values));
            });

    /**
     * Harmonic mean
     */
    public static final MathFunction HMEAN = FunctionBuilder
            .named("hmean")
            .alias("harmonicmean")
            .describedAs("Harmonic mean")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] values = ctx.flattenToDoubles(args);
                ctx.requireNonEmpty(values);
                return new NodeDouble(StatUtils.harmonicMean(values));
            });

    /**
     * Root mean square
     */
    public static final MathFunction RMS = FunctionBuilder
            .named("rms")
            .alias("rootmeansquare")
            .describedAs("Root mean square")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] values = ctx.flattenToDoubles(args);
                ctx.requireNonEmpty(values);
                return new NodeDouble(StatUtils.rootmeanSquare(values));
            });

    /**
     * Skewness (measure of asymmetry)
     */
    public static final MathFunction SKEWNESS = FunctionBuilder
            .named("skewness")
            .describedAs("Skewness (asymmetry measure)")
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
            .describedAs("Kurtosis (excess, relative to normal)")
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
            .describedAs("Covariance between two vectors")
            .inCategory(STATISTICAL)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((v1, v2, ctx) -> {
                NodeVector vec1 = ctx.requireVector(v1);
                NodeVector vec2 = ctx.requireVector(v2);

                if (vec1.size() != vec2.size()) {
                    throw new IllegalArgumentException("covariance requires vectors of equal length");
                }
                if (vec1.size() < 2) {
                    throw new TypeError("covariance requires at least 2 elements");
                }

                double[] x = ctx.toDoubleArray(vec1);
                double[] y = ctx.toDoubleArray(vec2);

                return new NodeDouble(StatUtils.covariance(x, y));
            });

    /**
     * Correlation coefficient between two vectors
     */
    public static final MathFunction CORRELATION = FunctionBuilder
            .named("correlation")
            .alias("corr")
            .describedAs("Pearson correlation coefficient")
            .inCategory(STATISTICAL)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((v1, v2, ctx) -> {
                NodeVector vec1 = ctx.requireVector(v1);
                NodeVector vec2 = ctx.requireVector(v2);

                if (vec1.size() != vec2.size()) {
                    throw new IllegalArgumentException("correlation requires vectors of equal length");
                }
                if (vec1.size() < 2) {
                    throw new TypeError("correlation requires at least 2 elements");
                }

                double[] x = ctx.toDoubleArray(vec1);
                double[] y = ctx.toDoubleArray(vec2);

                return new NodeDouble(StatUtils.correlationCoefficient(x, y));
            });

    /**
     * Mode (most frequent value)
     */
    public static final MathFunction MODE = FunctionBuilder
            .named("mode")
            .describedAs("Most frequent value")
            .inCategory(STATISTICAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] values = ctx.flattenToDoubles(args);
                ctx.requireNonEmpty(values);
                // StatUtils.mode returns all modes; return the first one
                double[] modes = StatUtils.mode(values);
                return new NodeDouble(modes[0]);
            });

    /**
     * Quartile (1, 2, or 3)
     */
    public static final MathFunction QUARTILE = FunctionBuilder
            .named("quartile")
            .describedAs("Calculate quartile (1, 2, or 3)")
            .inCategory(STATISTICAL)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((vector, qValue, ctx) -> {
                NodeVector vec = ctx.requireVector(vector);
                int q = (int) ctx.toNumber(qValue).doubleValue();

                if (q < 1 || q > 3) {
                    throw new IllegalArgumentException("quartile must be 1, 2, or 3");
                }

                double[] values = ctx.toDoubleArray(vec);
                ctx.requireNonEmpty(values);

                return new NodeDouble(StatUtils.quartile(values, q));
            });

    /**
     * Gets all statistical functions.
     */
    public static List<MathFunction> all() {
        return List.of(RANGE, PERCENTILE, IQR, GMEAN, HMEAN, RMS,
                SKEWNESS, KURTOSIS, COVARIANCE, CORRELATION, MODE, QUARTILE);
    }
}
