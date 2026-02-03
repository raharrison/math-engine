package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;
import uk.co.ryanharrison.mathengine.special.Beta;
import uk.co.ryanharrison.mathengine.special.Erf;
import uk.co.ryanharrison.mathengine.special.Gamma;
import uk.co.ryanharrison.mathengine.utils.MathUtils;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.SPECIAL;

/**
 * Collection of special mathematical functions (gamma, beta, etc.).
 */
public final class SpecialFunctions {

    private SpecialFunctions() {
    }

    /**
     * Gamma function
     */
    public static final MathFunction GAMMA = FunctionBuilder
            .named("gamma")
            .describedAs("Gamma function")
            .inCategory(SPECIAL)
            .takingUnary()
            .implementedByDouble(Gamma::gamma);

    /**
     * Natural log of gamma function
     */
    public static final MathFunction LGAMMA = FunctionBuilder
            .named("lgamma")
            .describedAs("Log of gamma function")
            .inCategory(SPECIAL)
            .takingUnary()
            .implementedByDouble(Gamma::gammaLn);

    /**
     * Factorial
     */
    public static final MathFunction FACTORIAL = FunctionBuilder
            .named("factorial")
            .describedAs("Factorial function")
            .inCategory(SPECIAL)
            .takingUnary()
            .implementedBy((arg, ctx) -> {
                double d = ctx.toNumber(arg).doubleValue();
                if (Math.floor(d) == d && d >= 0) {
                    return new NodeRational(MathUtils.factorial((long) d));
                }
                return new NodeDouble(MathUtils.factorial(d));
            });

    /**
     * Greatest common divisor
     */
    public static final MathFunction GCD = FunctionBuilder
            .named("gcd")
            .describedAs("Greatest common divisor")
            .inCategory(SPECIAL)
            .takingVariadic(2)
            .implementedByAggregate((args, ctx) -> {
                int result = ctx.requireInteger(args.getFirst());
                for (int i = 1; i < args.size(); i++) {
                    result = MathUtils.gcd(result, ctx.requireInteger(args.get(i)));
                }
                return new NodeRational(result);
            });

    /**
     * Least common multiple
     */
    public static final MathFunction LCM = FunctionBuilder
            .named("lcm")
            .describedAs("Least common multiple")
            .inCategory(SPECIAL)
            .takingVariadic(2)
            .implementedByAggregate((args, ctx) -> {
                long result = ctx.requireLong(args.getFirst());
                for (int i = 1; i < args.size(); i++) {
                    result = MathUtils.lcm(result, ctx.requireLong(args.get(i)));
                }
                return new NodeRational(result);
            });

    /**
     * Binomial coefficient
     */
    public static final MathFunction BINOMIAL = FunctionBuilder
            .named("binomial")
            .describedAs("Binomial coefficient (n choose k)")
            .inCategory(SPECIAL)
            .takingBinary()
            .implementedBy((n, k, ctx) -> {
                double nVal = ctx.toNumber(n).doubleValue();
                double kVal = ctx.toNumber(k).doubleValue();
                double result = MathUtils.combination(nVal, kVal);

                // Return as integer if possible
                if (result == Math.floor(result) && !Double.isInfinite(result)) {
                    return new NodeRational((long) result);
                }
                return new NodeDouble(result);
            });

    /**
     * Random number in [0, 1)
     */
    public static final MathFunction RANDOM = FunctionBuilder
            .named("random")
            .describedAs("Random number in [0, 1)")
            .inCategory(SPECIAL)
            .takingBetween(0, 0)
            .implementedByAggregate((args, ctx) -> new NodeDouble(Math.random()));

    /**
     * Random integer in range [min, max]
     */
    public static final MathFunction RANDINT = FunctionBuilder
            .named("randint")
            .describedAs("Random integer in [min, max]")
            .inCategory(SPECIAL)
            .takingBinary()
            .implementedBy((min, max, ctx) -> {
                int minVal = (int) ctx.toNumber(min).doubleValue();
                int maxVal = (int) ctx.toNumber(max).doubleValue();
                return new NodeRational(minVal + (int) (Math.random() * (maxVal - minVal + 1)));
            });

    /**
     * Beta function B(z, w)
     */
    public static final MathFunction BETA = FunctionBuilder
            .named("beta")
            .describedAs("Beta function B(z, w)")
            .inCategory(SPECIAL)
            .takingBinary()
            .implementedBy((z, w, ctx) -> {
                double zVal = ctx.toNumber(z).doubleValue();
                double wVal = ctx.toNumber(w).doubleValue();
                return new NodeDouble(Beta.beta(zVal, wVal));
            });

    /**
     * Error function
     */
    public static final MathFunction ERF = FunctionBuilder
            .named("erf")
            .describedAs("Error function")
            .inCategory(SPECIAL)
            .takingUnary()
            .implementedByDouble(Erf::erf);

    /**
     * Complementary error function
     */
    public static final MathFunction ERFC = FunctionBuilder
            .named("erfc")
            .describedAs("Complementary error function")
            .inCategory(SPECIAL)
            .takingUnary()
            .implementedByDouble(Erf::erfc);

    /**
     * Digamma function (psi) - logarithmic derivative of gamma
     */
    public static final MathFunction DIGAMMA = FunctionBuilder
            .named("digamma")
            .alias("psi")
            .describedAs("Digamma function (logarithmic derivative of gamma)")
            .inCategory(SPECIAL)
            .takingUnary()
            .implementedBy((arg, ctx) -> new NodeDouble(Gamma.digamma(ctx.toNumber(arg).doubleValue())));

    /**
     * Gets all special functions.
     */
    public static List<MathFunction> all() {
        return List.of(GAMMA, LGAMMA, FACTORIAL, GCD, LCM, BINOMIAL, RANDOM, RANDINT, BETA, ERF, ERFC, DIGAMMA);
    }
}
