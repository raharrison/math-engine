package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.function.math.ExponentialFunctions;
import uk.co.ryanharrison.mathengine.parser.function.math.RoundingFunctions;
import uk.co.ryanharrison.mathengine.parser.function.math.UtilityFunctions;
import uk.co.ryanharrison.mathengine.parser.function.special.*;
import uk.co.ryanharrison.mathengine.parser.function.string.StringFunctions;
import uk.co.ryanharrison.mathengine.parser.function.trig.HyperbolicFunctions;
import uk.co.ryanharrison.mathengine.parser.function.trig.TrigonometricFunctions;
import uk.co.ryanharrison.mathengine.parser.function.vector.*;

import java.util.*;

/**
 * Provides all standard built-in functions as a collection.
 * <p>
 * Use {@link #all()} to get all functions suitable for registration with a
 * {@link FunctionExecutor}.
 *
 * <h2>Included Categories:</h2>
 * <ul>
 *     <li>Trigonometric: sin, cos, tan, asin, acos, atan, atan2, sec, csc, cot</li>
 *     <li>Hyperbolic: sinh, cosh, tanh, asinh, acosh, atanh, sech, csch, coth</li>
 *     <li>Exponential: exp, exp2, exp10, expm1</li>
 *     <li>Logarithmic: ln, log, log2, logn, log1p</li>
 *     <li>Power/Root: sqrt, cbrt, nroot, pow</li>
 *     <li>Rounding: floor, ceil, round, trunc, roundn</li>
 *     <li>Basic: abs, sign, copysign, fmod, remainder, hypot</li>
 *     <li>Vector: sum, product, min, max, mean, median, sort, reverse, len, first, last</li>
 *     <li>Vector Manipulation: take, drop, slice, get, indexof, contains, unique, concat,
 *         flatten, zip, repeat, count, any, all, none, seq, linspace, fill</li>
 *     <li>Statistical: range, percentile, iqr, gmean, hmean, rms, skewness, kurtosis,
 *         covariance, correlation, mode, quartile</li>
 *     <li>Matrix: det, trace, transpose, identity, zeros, ones, diag, inverse, rank,
 *         norm, row, col, reshape, minor, cofactor, adjugate</li>
 *     <li>Conditional: if, clamp, lerp</li>
 *     <li>Special: gamma, lgamma, factorial, gcd, lcm, binomial, random, randint,
 *         beta, erf, erfc, digamma</li>
 *     <li>Number Theory: isprime, nextprime, prevprime, factors, distinctfactors,
 *         divisorcount, divisorsum, modpow, iscoprime</li>
 *     <li>Type: isnan, isinf, isfinite, isint, iseven, isodd, ispositive, isnegative,
 *         iszero, int, float, bool, numerator, denominator, typeof, isnumber, isvector,
 *         ismatrix, isboolean</li>
 *     <li>Bitwise: bitand, bitor, bitxor, bitnot, lshift, rshift, urshift, popcount,
 *         clz, ctz, rotl, rotr, bitreverse</li>
 *     <li>Utility: frac, mod, permutation, doublefactorial, inverselerp, map, smoothstep,
 *         distance, distance3d, manhattan, approxeq, compare, compound, wrap, normalize,
 *         deg2rad, rad2deg</li>
 *     <li>String: len, upper, lower, trim, ltrim, rtrim, substring, left, right, concat,
 *         replace, replaceall, indexof, lastindexof, contains, startswith, endswith,
 *         split, join, reverse, repeat, padleft, padright, char, ord, format, isempty,
 *         isblank, regex</li>
 *     <li>Percentage: percent, topercent, percentvalue, percentof, whatpercent, percentchange,
 *         addpercent, subtractpercent, reversepercent, ratiotopercent, percenttoratio,
 *         percentpoints, ispercent</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * FunctionExecutor executor = new FunctionExecutor();
 * executor.registerAll(StandardFunctions.all());
 * }</pre>
 */
public final class StandardFunctions {

    private StandardFunctions() {
    }

    /**
     * Gets all standard built-in functions.
     *
     * @return unmodifiable list of all functions
     */
    public static List<MathFunction> all() {
        List<MathFunction> functions = new ArrayList<>();

        // Trigonometric and hyperbolic
        functions.addAll(TrigonometricFunctions.all());
        functions.addAll(HyperbolicFunctions.all());

        // Exponential and logarithmic
        functions.addAll(ExponentialFunctions.all());

        // Rounding and basic math
        functions.addAll(RoundingFunctions.all());

        // Utility functions
        functions.addAll(UtilityFunctions.all());

        // Vector and matrix
        functions.addAll(VectorFunctions.all());
        functions.addAll(VectorManipulationFunctions.all());
        functions.addAll(StatisticalFunctions.all());
        functions.addAll(MatrixFunctions.all());
        functions.addAll(HigherOrderFunctions.all());

        // Conditional
        functions.addAll(ConditionalFunctions.all());

        // Special functions
        functions.addAll(SpecialFunctions.all());
        functions.addAll(NumberTheoryFunctions.all());

        // Type checking and conversion
        functions.addAll(TypeFunctions.all());

        // Bitwise operations
        functions.addAll(BitwiseFunctions.all());

        // String functions
        functions.addAll(StringFunctions.all());

        // Percentage functions
        functions.addAll(PercentageFunctions.all());

        return Collections.unmodifiableList(functions);
    }

    /**
     * Gets all functions as a map keyed by name.
     *
     * @return map of function name to function
     */
    public static Map<String, MathFunction> asMap() {
        Map<String, MathFunction> map = new HashMap<>();
        for (MathFunction func : all()) {
            map.put(func.name().toLowerCase(), func);
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Gets functions by category.
     *
     * @param category the category to filter
     * @return list of functions in the category
     */
    public static List<MathFunction> byCategory(MathFunction.Category category) {
        return all().stream()
                .filter(f -> f.category() == category)
                .toList();
    }

    /**
     * Gets basic math functions suitable for a standard calculator.
     * Includes trig, hyperbolic, exponential, rounding, utility, conditional, and type functions.
     * Excludes vector, matrix, statistical, string, bitwise, and special functions.
     *
     * @return unmodifiable list of basic math functions
     */
    public static List<MathFunction> basic() {
        List<MathFunction> functions = new ArrayList<>();
        functions.addAll(TrigonometricFunctions.all());
        functions.addAll(HyperbolicFunctions.all());
        functions.addAll(ExponentialFunctions.all());
        functions.addAll(RoundingFunctions.all());
        functions.addAll(UtilityFunctions.all());
        functions.addAll(ConditionalFunctions.all());
        functions.addAll(TypeFunctions.all());
        return Collections.unmodifiableList(functions);
    }

    /**
     * Gets only trigonometric functions.
     */
    public static List<MathFunction> trigonometric() {
        return TrigonometricFunctions.all();
    }

    /**
     * Gets only hyperbolic functions.
     */
    public static List<MathFunction> hyperbolic() {
        return HyperbolicFunctions.all();
    }

    /**
     * Gets only exponential functions.
     */
    public static List<MathFunction> exponential() {
        return ExponentialFunctions.exponential();
    }

    /**
     * Gets only logarithmic functions.
     */
    public static List<MathFunction> logarithmic() {
        return ExponentialFunctions.logarithmic();
    }

    /**
     * Gets only rounding functions.
     */
    public static List<MathFunction> rounding() {
        return RoundingFunctions.rounding();
    }

    /**
     * Gets only vector functions.
     */
    public static List<MathFunction> vector() {
        return VectorFunctions.all();
    }

    /**
     * Gets only matrix functions.
     */
    public static List<MathFunction> matrix() {
        return MatrixFunctions.all();
    }

    /**
     * Gets only statistical functions.
     */
    public static List<MathFunction> statistical() {
        return StatisticalFunctions.all();
    }

    /**
     * Gets only number theory functions.
     */
    public static List<MathFunction> numberTheory() {
        return NumberTheoryFunctions.all();
    }

    /**
     * Gets only type functions.
     */
    public static List<MathFunction> type() {
        return TypeFunctions.all();
    }

    /**
     * Gets only bitwise functions.
     */
    public static List<MathFunction> bitwise() {
        return BitwiseFunctions.all();
    }

    /**
     * Gets only utility functions.
     */
    public static List<MathFunction> utility() {
        return UtilityFunctions.all();
    }

    /**
     * Gets only string functions.
     */
    public static List<MathFunction> string() {
        return StringFunctions.all();
    }

    /**
     * Gets only percentage functions.
     */
    public static List<MathFunction> percentage() {
        return PercentageFunctions.all();
    }

    /**
     * Gets function names as a set for quick lookup.
     */
    public static Set<String> names() {
        Set<String> names = new HashSet<>();
        for (MathFunction func : all()) {
            names.add(func.name().toLowerCase());
        }
        return Collections.unmodifiableSet(names);
    }

    /**
     * Counts the total number of standard functions.
     */
    public static int count() {
        return all().size();
    }
}
