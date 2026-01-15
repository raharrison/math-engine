package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.parser.function.BinaryFunction;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.UnaryFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;

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
    public static final MathFunction EXP = UnaryFunction.ofDouble("exp", "Natural exponential (e^x)", EXPONENTIAL, Math::exp);

    /**
     * Power of 2 function (2^x)
     */
    public static final MathFunction EXP2 = UnaryFunction.ofDouble("exp2", "Power of 2 (2^x)", EXPONENTIAL, x -> Math.pow(2, x));

    /**
     * Power of 10 function (10^x)
     */
    public static final MathFunction EXP10 = UnaryFunction.ofDouble("exp10", "Power of 10 (10^x)", EXPONENTIAL, x -> Math.pow(10, x));

    /**
     * exp(x) - 1, accurate for small x
     */
    public static final MathFunction EXPM1 = UnaryFunction.ofDouble("expm1", "exp(x) - 1", EXPONENTIAL, Math::expm1);

    // ==================== Logarithmic Functions ====================

    /**
     * Natural logarithm (ln, base e)
     */
    public static final MathFunction LN = UnaryFunction.of("ln", "Natural logarithm (base e)", LOGARITHMIC, (arg, ctx) -> {
        double x = ctx.toNumber(arg).doubleValue();
        if (x <= 0) {
            throw new IllegalArgumentException("ln is not defined for values <= 0, got: " + x);
        }
        return new NodeDouble(Math.log(x));
    });

    /**
     * Common logarithm (log10, base 10)
     */
    public static final MathFunction LOG = UnaryFunction.of("log", "Common logarithm (base 10)", LOGARITHMIC, List.of("log10"), (arg, ctx) -> {
        double x = ctx.toNumber(arg).doubleValue();
        if (x <= 0) {
            throw new IllegalArgumentException("log is not defined for values <= 0, got: " + x);
        }
        return new NodeDouble(Math.log10(x));
    });

    private static final double LOG_2 = Math.log(2);

    /**
     * Binary logarithm (log2, base 2)
     */
    public static final MathFunction LOG2 = UnaryFunction.of("log2", "Binary logarithm (base 2)", LOGARITHMIC, (arg, ctx) -> {
        double x = ctx.toNumber(arg).doubleValue();
        if (x <= 0) {
            throw new IllegalArgumentException("log2 is not defined for values <= 0, got: " + x);
        }
        return new NodeDouble(Math.log(x) / LOG_2);
    });

    /**
     * Logarithm with arbitrary base
     */
    public static final MathFunction LOGN = BinaryFunction.of("logn", "Logarithm with arbitrary base", LOGARITHMIC, (x, base, ctx) -> {
        double xVal = ctx.toNumber(x).doubleValue();
        double baseVal = ctx.toNumber(base).doubleValue();
        if (xVal <= 0) {
            throw new IllegalArgumentException("logn is not defined for values <= 0, got: " + xVal);
        }
        if (baseVal <= 0 || baseVal == 1) {
            throw new IllegalArgumentException("logn base must be positive and not 1, got: " + baseVal);
        }
        return new NodeDouble(Math.log(xVal) / Math.log(baseVal));
    });

    /**
     * ln(1 + x), accurate for small x
     */
    public static final MathFunction LOG1P = UnaryFunction.of("log1p", "ln(1 + x)", LOGARITHMIC, (arg, ctx) -> {
        double x = ctx.toNumber(arg).doubleValue();
        if (x <= -1) {
            throw new IllegalArgumentException("log1p is not defined for values <= -1, got: " + x);
        }
        return new NodeDouble(Math.log1p(x));
    });

    // ==================== Power and Root Functions ====================

    /**
     * Square root
     */
    public static final MathFunction SQRT = UnaryFunction.of("sqrt", "Square root", EXPONENTIAL, (arg, ctx) -> {
        double x = ctx.toNumber(arg).doubleValue();
        if (x < 0) {
            throw new IllegalArgumentException("sqrt is not defined for negative values, got: " + x);
        }
        return new NodeDouble(Math.sqrt(x));
    });

    /**
     * Cube root
     */
    public static final MathFunction CBRT = UnaryFunction.ofDouble("cbrt", "Cube root", EXPONENTIAL, Math::cbrt);

    /**
     * nth root
     */
    public static final MathFunction NROOT = BinaryFunction.of("nroot", "nth root", EXPONENTIAL, (x, n, ctx) -> {
        double xVal = ctx.toNumber(x).doubleValue();
        double nVal = ctx.toNumber(n).doubleValue();

        if (nVal == 0) {
            throw new IllegalArgumentException("nroot: n cannot be 0");
        }

        // Check for even root of negative number
        if (xVal < 0 && nVal == Math.floor(nVal) && ((long) nVal) % 2 == 0) {
            throw new IllegalArgumentException("nroot: cannot take even root of negative number (x=" + xVal + ", n=" + nVal + ")");
        }

        return new NodeDouble(Math.pow(xVal, 1.0 / nVal));
    });

    /**
     * Power function
     */
    public static final MathFunction POW = BinaryFunction.of("pow", "Power function", EXPONENTIAL, (base, exp, ctx) -> {
        double baseVal = ctx.toNumber(base).doubleValue();
        double expVal = ctx.toNumber(exp).doubleValue();

        // Preserve rational precision for integer exponents
        if (base instanceof NodeRational baseRat) {
            if (expVal == Math.floor(expVal) && !Double.isInfinite(expVal)) {
                int intExp = (int) expVal;
                return new NodeRational(baseRat.getValue().pow(intExp));
            }
        }

        return new NodeDouble(Math.pow(baseVal, expVal));
    });

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
