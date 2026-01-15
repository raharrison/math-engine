package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.AggregateFunction;
import uk.co.ryanharrison.mathengine.parser.function.BinaryFunction;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.UnaryFunction;
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
    public static final MathFunction GAMMA = UnaryFunction.ofDouble("gamma", "Gamma function", SPECIAL, Gamma::gamma);

    /**
     * Natural log of gamma function
     */
    public static final MathFunction LGAMMA = UnaryFunction.ofDouble("lgamma", "Log of gamma function", SPECIAL, Gamma::gammaLn);

    /**
     * Factorial
     */
    public static final MathFunction FACTORIAL = UnaryFunction.of("factorial", "Factorial function", SPECIAL, (arg, ctx) -> {
        double d = ctx.toNumber(arg).doubleValue();
        if (Math.floor(d) == d && d >= 0) {
            return new NodeRational(MathUtils.factorial((long) d));
        }
        return new NodeDouble(MathUtils.factorial(d));
    });

    /**
     * Greatest common divisor
     */
    public static final MathFunction GCD = AggregateFunction.of("gcd", "Greatest common divisor", SPECIAL, 2, Integer.MAX_VALUE, (args, ctx) -> {
        int result = ctx.requireInteger(args.get(0), "gcd");
        for (int i = 1; i < args.size(); i++) {
            result = MathUtils.gcd(result, ctx.requireInteger(args.get(i), "gcd"));
        }
        return new NodeRational(result);
    });

    /**
     * Least common multiple
     */
    public static final MathFunction LCM = AggregateFunction.of("lcm", "Least common multiple", SPECIAL, 2, Integer.MAX_VALUE, (args, ctx) -> {
        long result = ctx.requireLong(args.get(0), "lcm");
        for (int i = 1; i < args.size(); i++) {
            result = MathUtils.lcm(result, ctx.requireLong(args.get(i), "lcm"));
        }
        return new NodeRational(result);
    });

    /**
     * Binomial coefficient
     */
    public static final MathFunction BINOMIAL = BinaryFunction.of("binomial", "Binomial coefficient (n choose k)", SPECIAL, (n, k, ctx) -> {
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
    public static final MathFunction RANDOM = AggregateFunction.of("random", "Random number in [0, 1)", SPECIAL, 0, 0,
            (args, ctx) -> new NodeDouble(Math.random()));

    /**
     * Random integer in range [min, max]
     */
    public static final MathFunction RANDINT = BinaryFunction.of("randint", "Random integer in [min, max]", SPECIAL, (min, max, ctx) -> {
        int minVal = (int) ctx.toNumber(min).doubleValue();
        int maxVal = (int) ctx.toNumber(max).doubleValue();
        return new NodeRational(minVal + (int) (Math.random() * (maxVal - minVal + 1)));
    });

    /**
     * Beta function B(z, w)
     */
    public static final MathFunction BETA = BinaryFunction.of("beta", "Beta function B(z, w)", SPECIAL, (z, w, ctx) -> {
        double zVal = ctx.toNumber(z).doubleValue();
        double wVal = ctx.toNumber(w).doubleValue();
        return new NodeDouble(Beta.beta(zVal, wVal));
    });

    /**
     * Error function
     */
    public static final MathFunction ERF = UnaryFunction.ofDouble("erf", "Error function", SPECIAL, Erf::erf);

    /**
     * Complementary error function
     */
    public static final MathFunction ERFC = UnaryFunction.ofDouble("erfc", "Complementary error function", SPECIAL, Erf::erfc);

    /**
     * Digamma function (psi) - logarithmic derivative of gamma
     */
    public static final MathFunction DIGAMMA = UnaryFunction.of("digamma", "Digamma function (logarithmic derivative of gamma)", SPECIAL,
            List.of("psi"), (arg, ctx) -> new NodeDouble(Gamma.digamma(ctx.toNumber(arg).doubleValue())));

    /**
     * Gets all special functions.
     */
    public static List<MathFunction> all() {
        return List.of(GAMMA, LGAMMA, FACTORIAL, GCD, LCM, BINOMIAL, RANDOM, RANDINT, BETA, ERF, ERFC, DIGAMMA);
    }
}
