package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.EXPONENTIAL;
import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.LOGARITHMIC;

/**
 * Collection of exponential and logarithmic functions.
 */
public final class ExponentialFunctions {

    private ExponentialFunctions() {
    }

    // ==================== Exponential Functions ====================

    /**
     * Natural exponential function (e^x)
     */
    public static final MathFunction EXP = FunctionBuilder
            .named("exp")
            .describedAs("Returns e raised to the power x")
            .withParams("x")
            .inCategory(EXPONENTIAL)
            .takingUnary()
            .implementedByDouble(Math::exp);

    /**
     * Power of 2 function (2^x)
     */
    public static final MathFunction EXP2 = FunctionBuilder
            .named("exp2")
            .describedAs("Returns 2 raised to the power x")
            .withParams("x")
            .inCategory(EXPONENTIAL)
            .takingUnary()
            .implementedByDouble(x -> Math.pow(2, x));

    /**
     * Power of 10 function (10^x)
     */
    public static final MathFunction EXP10 = FunctionBuilder
            .named("exp10")
            .describedAs("Returns 10 raised to the power x")
            .withParams("x")
            .inCategory(EXPONENTIAL)
            .takingUnary()
            .implementedByDouble(x -> Math.pow(10, x));

    /**
     * exp(x) - 1, accurate for small x
     */
    public static final MathFunction EXPM1 = FunctionBuilder
            .named("expm1")
            .describedAs("Returns e^x - 1, numerically stable for small x (avoids cancellation error)")
            .withParams("x")
            .inCategory(EXPONENTIAL)
            .takingUnary()
            .implementedByDouble(Math::expm1);

    // ==================== Logarithmic Functions ====================

    /**
     * Natural logarithm (ln, base e)
     */
    public static final MathFunction LN = FunctionBuilder
            .named("ln")
            .describedAs("Returns the natural logarithm (base e) of x")
            .withParams("x")
            .inCategory(LOGARITHMIC)
            .takingUnary()
            .implementedBy((arg, ctx) -> ctx.applyWithBroadcasting(arg, value -> Math.log(ctx.requirePositive(value))));

    /**
     * Common logarithm (log10, base 10)
     */
    public static final MathFunction LOG = FunctionBuilder
            .named("log")
            .alias("log10")
            .describedAs("Returns the base-10 logarithm of x")
            .withParams("x")
            .inCategory(LOGARITHMIC)
            .takingUnary()
            .implementedBy((arg, ctx) ->
                    ctx.applyWithBroadcasting(arg, value -> Math.log10(ctx.requirePositive(value))));

    private static final double LOG_2 = Math.log(2);

    /**
     * Binary logarithm (log2, base 2)
     */
    public static final MathFunction LOG2 = FunctionBuilder
            .named("log2")
            .describedAs("Returns the base-2 logarithm of x")
            .withParams("x")
            .inCategory(LOGARITHMIC)
            .takingUnary()
            .implementedBy((arg, ctx) ->
                    ctx.applyWithBroadcasting(arg, value -> Math.log(ctx.requirePositive(value)) / LOG_2));

    /**
     * Logarithm with arbitrary base
     */
    public static final MathFunction LOGN = FunctionBuilder
            .named("logn")
            .describedAs("Returns the logarithm of x to the given base (log_base(x))")
            .withParams("x", "base")
            .inCategory(LOGARITHMIC)
            .takingBinary()
            .withBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double x = ctx.toNumber(first).doubleValue();
                double base = ctx.toNumber(second).doubleValue();
                double validX = ctx.requirePositive(x);
                double validBase = ctx.requirePositive(base);

                // Check base != 1 (needs special handling for silent mode)
                if (validBase == 1.0) {
                    if (ctx.isSilentValidation()) {
                        return new NodeDouble(Double.NaN);
                    }
                    throw ctx.error("base cannot be 1");
                }

                return new NodeDouble(Math.log(validX) / Math.log(validBase));
            });

    /**
     * ln(1 + x), accurate for small x
     */
    public static final MathFunction LOG1P = FunctionBuilder
            .named("log1p")
            .describedAs("Returns ln(1 + x), numerically stable for small x (avoids cancellation error)")
            .withParams("x")
            .inCategory(LOGARITHMIC)
            .takingUnary()
            .implementedBy((arg, ctx) ->
                    ctx.applyWithBroadcasting(arg, value -> Math.log1p(ctx.requireInRange(value, -0.999999999, Double.MAX_VALUE))));

    // ==================== Power and Root Functions ====================

    /**
     * Square root
     */
    public static final MathFunction SQRT = FunctionBuilder
            .named("sqrt")
            .describedAs("Returns the square root of x")
            .withParams("x")
            .inCategory(EXPONENTIAL)
            .takingUnary()
            .implementedBy((arg, ctx) ->
                    ctx.applyWithBroadcasting(arg, value -> Math.sqrt(ctx.requireNonNegative(value))));

    /**
     * Cube root
     */
    public static final MathFunction CBRT = FunctionBuilder
            .named("cbrt")
            .describedAs("Returns the cube root of x")
            .withParams("x")
            .inCategory(EXPONENTIAL)
            .takingUnary()
            .implementedByDouble(Math::cbrt);

    /**
     * nth root
     */
    public static final MathFunction NROOT = FunctionBuilder
            .named("nroot")
            .describedAs("Returns the nth root of x, equivalent to x^(1/n)")
            .withParams("x", "n")
            .inCategory(EXPONENTIAL)
            .takingBinary()
            .withBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double x = ctx.toNumber(first).doubleValue();
                double n = ctx.toNumber(second).doubleValue();
                // Prevent even roots of negative numbers
                if (x < 0 && n == Math.floor(n) && !Double.isInfinite(n)) {
                    long nInt = (long) n;
                    if (nInt % 2 == 0) {
                        if (ctx.isSilentValidation()) {
                            return new NodeDouble(Double.NaN);
                        }
                        throw ctx.error("cannot take even root of negative number (x=" + x + ", n=" + n + ")");
                    }
                }
                double validN = ctx.requireNonZero(n);
                return new NodeDouble(Math.pow(x, 1.0 / validN));
            });

    /**
     * Power function
     */
    public static final MathFunction POW = FunctionBuilder
            .named("pow")
            .describedAs("Returns base raised to the power exp")
            .withParams("base", "exp")
            .inCategory(EXPONENTIAL)
            .takingBinary()
            .withBroadcasting()
            .implementedBy((base, exp, ctx) -> NumericOperations.applyPower(base, exp, false));

    // ==================== All Functions ====================

    /**
     * Gets all exponential and logarithmic functions.
     *
     * @return list of all functions
     */
    public static List<MathFunction> all() {
        return List.of(EXP, EXP2, EXP10, EXPM1, LN, LOG, LOG2, LOGN, LOG1P, SQRT, CBRT, NROOT, POW);
    }

    /**
     * Gets only exponential functions.
     */
    public static List<MathFunction> exponential() {
        return List.of(EXP, EXP2, EXP10, EXPM1);
    }

    /**
     * Gets only logarithmic functions.
     */
    public static List<MathFunction> logarithmic() {
        return List.of(LN, LOG, LOG2, LOGN, LOG1P);
    }

    /**
     * Gets power/root functions.
     */
    public static List<MathFunction> powerAndRoot() {
        return List.of(SQRT, CBRT, NROOT, POW);
    }
}
