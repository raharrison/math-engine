package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.function.math.ExponentialFunctions;
import uk.co.ryanharrison.mathengine.parser.function.math.RoundingFunctions;
import uk.co.ryanharrison.mathengine.parser.function.math.UtilityFunctions;
import uk.co.ryanharrison.mathengine.parser.function.special.*;
import uk.co.ryanharrison.mathengine.parser.function.string.StringFunctions;
import uk.co.ryanharrison.mathengine.parser.function.trig.HyperbolicFunctions;
import uk.co.ryanharrison.mathengine.parser.function.trig.TrigonometricFunctions;
import uk.co.ryanharrison.mathengine.parser.function.vector.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides all standard built-in functions as a collection.
 * <p>
 * Use {@link #all()} to get all functions suitable for registration with a
 * {@link FunctionExecutor}.
 *
 * <h2>Included Categories:</h2>
 * <ul>
 *     <li>Trigonometric: sin, cos, tan, sec, csc, cot and their inverses asin, acos,
 *     atan, asec, acsc, acot, plus atan2</li>
 *     <li>Hyperbolic: sinh, cosh, tanh, sech, csch, coth and their inverses asinh,
 *     acosh, atanh, asech, acsch, acoth</li>
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

    private static final List<MathFunction> ALL = buildAll();

    private StandardFunctions() {
    }

    /**
     * Every standard function. The list is built once and shared.
     */
    public static List<MathFunction> all() {
        return ALL;
    }

    private static List<MathFunction> buildAll() {
        var functions = new ArrayList<MathFunction>();

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
     * Gets basic math functions suitable for a standard calculator.
     * Includes trig, hyperbolic, exponential, rounding, utility, conditional, and type functions.
     * Excludes vector, matrix, statistical, string, bitwise, and special functions.
     *
     * @return unmodifiable list of basic math functions
     */
    public static List<MathFunction> basic() {
        var functions = new ArrayList<MathFunction>();
        functions.addAll(TrigonometricFunctions.all());
        functions.addAll(HyperbolicFunctions.all());
        functions.addAll(ExponentialFunctions.all());
        functions.addAll(RoundingFunctions.all());
        functions.addAll(UtilityFunctions.all());
        functions.addAll(ConditionalFunctions.all());
        functions.addAll(TypeFunctions.all());
        return Collections.unmodifiableList(functions);
    }

}
