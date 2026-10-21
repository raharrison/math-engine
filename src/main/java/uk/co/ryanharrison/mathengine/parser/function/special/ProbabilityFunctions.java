package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.distributions.BinomialDistribution;
import uk.co.ryanharrison.mathengine.distributions.ContinuousProbabilityDistribution;
import uk.co.ryanharrison.mathengine.distributions.NormalDistribution;
import uk.co.ryanharrison.mathengine.distributions.StudentTDistribution;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.function.Supplier;
import java.util.function.ToDoubleBiFunction;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.PROBABILITY;

/**
 * Density, cumulative probability and quantile of the common distributions, from the
 * library's {@code distributions} package.
 * <p>
 * The point or probability, always the first argument but for the binomial's {@code k},
 * broadcasts, so {@code normcdf({-1, 0, 1})} gives three probabilities. The naming follows
 * the usual calculator convention: {@code pdf} the density, {@code cdf} the probability of
 * a value at most x, and {@code inv} the value with a given cumulative probability.
 *
 * <pre>{@code
 * normcdf(1.96)                // 0.975, the standard normal
 * normcdf(130, 100, 15)        // 0.977, an IQ of 130 or less
 * norminv(0.95)                // 1.645
 * binompdf(10, 0.5, 5)         // 0.246, five heads in ten tosses
 * binomcdf(10, 0.5, 5)         // 0.623, at most five heads
 * tinv(0.975, 10)              // 2.228, the two-sided 95% critical value
 * }</pre>
 */
public final class ProbabilityFunctions {

    private ProbabilityFunctions() {
    }

    // ==================== Normal ====================

    /**
     * Normal density
     */
    public static final MathFunction NORMPDF = FunctionBuilder
            .named("normpdf")
            .describedAs("Returns the normal probability density at x, standard unless a mean and standard deviation are given")
            .withParams("x")
            .withParams("x", "mean", "sd")
            .inCategory(PROBABILITY)
            .takingBetween(1, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> normal(args, ctx, ContinuousProbabilityDistribution::density));

    /**
     * Normal cumulative probability
     */
    public static final MathFunction NORMCDF = FunctionBuilder
            .named("normcdf")
            .describedAs("Returns the probability that a normal variable is at most x, standard unless a mean and standard deviation are given")
            .withParams("x")
            .withParams("x", "mean", "sd")
            .inCategory(PROBABILITY)
            .takingBetween(1, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> normal(args, ctx, ContinuousProbabilityDistribution::cumulative));

    /**
     * Normal quantile
     */
    public static final MathFunction NORMINV = FunctionBuilder
            .named("norminv")
            .describedAs("Returns the value a normal variable is at most with probability p, standard unless a mean and standard deviation are given")
            .withParams("p")
            .withParams("p", "mean", "sd")
            .inCategory(PROBABILITY)
            .takingBetween(1, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> normal(args, ctx, ContinuousProbabilityDistribution::inverseCumulative));

    // ==================== Binomial ====================

    /**
     * Binomial probability of exactly k successes
     */
    public static final MathFunction BINOMPDF = FunctionBuilder
            .named("binompdf")
            .describedAs("Returns the probability of exactly k successes in n trials, each succeeding with probability p")
            .withParams("n", "p", "k")
            .inCategory(PROBABILITY)
            .takingBetween(3, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                BinomialDistribution distribution = binomial(args, ctx);
                return binomialAt(args.get(2), ctx, k -> k < 0 ? 0 : distribution.density(k));
            });

    /**
     * Binomial probability of at most k successes
     */
    public static final MathFunction BINOMCDF = FunctionBuilder
            .named("binomcdf")
            .describedAs("Returns the probability of at most k successes in n trials, each succeeding with probability p")
            .withParams("n", "p", "k")
            .inCategory(PROBABILITY)
            .takingBetween(3, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                BinomialDistribution distribution = binomial(args, ctx);
                return binomialAt(args.get(2), ctx, k -> k < 0 ? 0 : distribution.cumulative(k));
            });

    // ==================== Student's t ====================

    /**
     * Student's t density
     */
    public static final MathFunction TPDF = FunctionBuilder
            .named("tpdf")
            .describedAs("Returns the Student's t probability density at x with the given degrees of freedom")
            .withParams("x", "df")
            .inCategory(PROBABILITY)
            .takingBetween(2, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> studentT(args, ctx, ContinuousProbabilityDistribution::density));

    /**
     * Student's t cumulative probability
     */
    public static final MathFunction TCDF = FunctionBuilder
            .named("tcdf")
            .describedAs("Returns the probability that a Student's t variable with the given degrees of freedom is at most x")
            .withParams("x", "df")
            .inCategory(PROBABILITY)
            .takingBetween(2, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> studentT(args, ctx, ContinuousProbabilityDistribution::cumulative));

    /**
     * Student's t quantile
     */
    public static final MathFunction TINV = FunctionBuilder
            .named("tinv")
            .describedAs("Returns the value a Student's t variable with the given degrees of freedom is at most with probability p")
            .withParams("p", "df")
            .inCategory(PROBABILITY)
            .takingBetween(2, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> studentT(args, ctx, ContinuousProbabilityDistribution::inverseCumulative));

    // ==================== Helpers ====================

    private static NodeConstant normal(List<NodeConstant> args, FunctionContext ctx,
                                       ToDoubleBiFunction<ContinuousProbabilityDistribution, Double> measure) {
        if (args.size() == 2) {
            throw ctx.error("give both a mean and a standard deviation, or neither");
        }
        ContinuousProbabilityDistribution distribution = args.size() == 3
                ? create(ctx, () -> NormalDistribution.of(ctx.toDouble(args.get(1)), ctx.toDouble(args.get(2))))
                : NormalDistribution.standard();
        return at(args.getFirst(), ctx, distribution, measure);
    }

    private static NodeConstant studentT(List<NodeConstant> args, FunctionContext ctx,
                                         ToDoubleBiFunction<ContinuousProbabilityDistribution, Double> measure) {
        ContinuousProbabilityDistribution distribution =
                create(ctx, () -> StudentTDistribution.of(ctx.toDouble(args.get(1))));
        return at(args.getFirst(), ctx, distribution, measure);
    }

    /**
     * The measure at each point of {@code points}, which may be a single number or a collection.
     */
    private static NodeConstant at(NodeConstant points, FunctionContext ctx,
                                   ContinuousProbabilityDistribution distribution,
                                   ToDoubleBiFunction<ContinuousProbabilityDistribution, Double> measure) {
        return ctx.mapDouble(points, ctx.checkingDomain(x -> measure.applyAsDouble(distribution, x)));
    }

    private static BinomialDistribution binomial(List<NodeConstant> args, FunctionContext ctx) {
        int trials = ctx.requireInteger(args.get(0));
        double probability = ctx.toPercentDecimal(args.get(1));
        return create(ctx, () -> BinomialDistribution.of(trials, probability));
    }

    private static NodeConstant binomialAt(NodeConstant counts, FunctionContext ctx, IntToDoubleFunction measure) {
        return BroadcastingEngine.applyUnary(counts,
                k -> new NodeDouble(measure.applyAsDouble(ctx.requireInteger(k))));
    }

    /**
     * Builds a distribution, reporting parameters it refuses as this function's domain error.
     */
    private static <T> T create(FunctionContext ctx, Supplier<T> factory) {
        try {
            return factory.get();
        } catch (IllegalArgumentException e) {
            throw ctx.error(e.getMessage());
        }
    }

    /**
     * Gets all probability functions.
     */
    public static List<MathFunction> all() {
        return List.of(NORMPDF, NORMCDF, NORMINV, BINOMPDF, BINOMCDF, TPDF, TCDF, TINV);
    }
}
