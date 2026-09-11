package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.parser.ast.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.ast.NodeRational;
import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.special.Gamma;
import uk.co.ryanharrison.mathengine.utils.MathUtils;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.UTILITY;

/**
 * Collection of general-purpose utility functions.
 * <p>
 * These combine their arguments with {@link NodeConstant} arithmetic rather than in
 * doubles, so a unit rides along and an exact input gives an exact answer:
 * {@code frac(3/2)} is 1/2 and {@code distance(0, 0, 3 m, 4 m)} is 5 meters. Only the ones
 * that genuinely need a root or a gamma end in a double, and even those keep the label.
 */
public final class UtilityFunctions {

    private UtilityFunctions() {
    }

    // ==================== Fractional Functions ====================

    /**
     * Fractional part of a number (x - floor(x))
     */
    public static final MathFunction FRAC = FunctionBuilder
            .named("frac")
            .alias("fpart", "fractional")
            .describedAs("Returns the fractional part of x (x - floor(x))")
            .withParams("x")
            .inCategory(UTILITY)
            .takingUnary()
            .implementedBy((x, ctx) -> x.subtract(x.floor()));

    // ==================== Combinatorial Functions ====================

    /**
     * Permutation: n! / (n-r)!
     */
    public static final MathFunction PERMUTATION = FunctionBuilder
            .named("permutation")
            .alias("perm", "npr")
            .describedAs("Returns the number of ordered arrangements of r items from n (nPr)")
            .withParams("n", "r")
            .inCategory(UTILITY)
            .takingBinary()
            .implementedBy((n, r, ctx) -> {
                double nVal = ctx.toDouble(n);
                double rVal = ctx.toDouble(r);
                if (rVal < 0 || rVal > nVal) {
                    return new NodeRational(0);
                }
                // Whole arguments have an exact answer, as factorial and binomial already give
                if (isWhole(nVal) && isWhole(rVal) && rVal <= MathUtils.MAX_EXACT_FACTORIAL) {
                    NodeConstant arrangements = new NodeRational(1);
                    for (long i = 0; i < (long) rVal; i++) {
                        arrangements = arrangements.multiply(new NodeRational((long) nVal - i));
                    }
                    return arrangements;
                }
                return new NodeDouble(Gamma.gamma(nVal + 1) / Gamma.gamma(nVal - rVal + 1));
            });

    /**
     * Double factorial: n!!
     */
    public static final MathFunction DOUBLEFACTORIAL = FunctionBuilder
            .named("doublefactorial")
            .alias("dfact")
            .describedAs("Returns n!! (product of every other integer from n down to 1 or 2)")
            .withParams("n")
            .inCategory(UTILITY)
            .takingUnary()
            .implementedByDouble(MathUtils::doubleFactorial);

    // ==================== Interpolation Functions ====================

    /**
     * Inverse linear interpolation
     */
    public static final MathFunction INVERSELERP = FunctionBuilder
            .named("inverselerp")
            .alias("ilerp")
            .describedAs("Returns the t value such that lerp(start, end, t) = value")
            .withParams("start", "end", "value")
            .inCategory(UTILITY)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant start = args.get(0);
                NodeConstant end = args.get(1);
                NodeConstant value = args.get(2);
                if (start.equalTo(end)) {
                    throw new DomainException("inverselerp: start and end cannot be equal");
                }
                // Two like quantities divide into a plain fraction, which is what t is
                return value.subtract(start).divide(end.subtract(start));
            });

    /**
     * Map/remap value from one range to another
     */
    public static final MathFunction REMAP = FunctionBuilder
            .named("remap")
            .describedAs("Remaps value from [fromMin, fromMax] to [toMin, toMax]")
            .withParams("value", "fromMin", "fromMax", "toMin", "toMax")
            .inCategory(UTILITY)
            .takingBetween(5, 5)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant value = args.get(0);
                NodeConstant fromMin = args.get(1);
                NodeConstant fromMax = args.get(2);
                NodeConstant toMin = args.get(3);
                NodeConstant toMax = args.get(4);
                if (fromMin.equalTo(fromMax)) {
                    throw new DomainException("map: source range cannot have zero width");
                }
                NodeConstant t = value.subtract(fromMin).divide(fromMax.subtract(fromMin));
                return toMin.add(t.multiply(toMax.subtract(toMin)));
            });

    /**
     * Smooth step interpolation (cubic Hermite)
     */
    public static final MathFunction SMOOTHSTEP = FunctionBuilder
            .named("smoothstep")
            .describedAs("Returns a smooth cubic interpolation of x between edge0 and edge1")
            .withParams("edge0", "edge1", "x")
            .inCategory(UTILITY)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant edge0 = args.get(0);
                NodeConstant edge1 = args.get(1);
                NodeConstant x = args.get(2);

                NodeConstant zero = new NodeRational(0);
                NodeConstant one = new NodeRational(1);
                NodeConstant t = x.subtract(edge0).divide(edge1.subtract(edge0));
                if (t.compareTo(zero) < 0) {
                    t = zero;
                } else if (t.compareTo(one) > 0) {
                    t = one;
                }
                NodeConstant slope = new NodeRational(3).subtract(new NodeRational(2).multiply(t));
                return t.multiply(t).multiply(slope);
            });

    // ==================== Distance Functions ====================

    /**
     * 2D Euclidean distance
     */
    public static final MathFunction DISTANCE2D = FunctionBuilder
            .named("distance")
            .alias("dist")
            .describedAs("Returns the Euclidean distance between points (x1,y1) and (x2,y2)")
            .withParams("x1", "y1", "x2", "y2")
            .inCategory(UTILITY)
            .takingBetween(4, 4)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant dx = args.get(2).subtract(args.get(0));
                NodeConstant dy = args.get(3).subtract(args.get(1));
                return squared(dx).add(squared(dy)).mapMagnitude(Math::sqrt);
            });

    /**
     * 3D Euclidean distance
     */
    public static final MathFunction DISTANCE3D = FunctionBuilder
            .named("distance3d")
            .alias("dist3d")
            .describedAs("Returns the Euclidean distance between points (x1,y1,z1) and (x2,y2,z2)")
            .withParams("x1", "y1", "z1", "x2", "y2", "z2")
            .inCategory(UTILITY)
            .takingBetween(6, 6)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant dx = args.get(3).subtract(args.get(0));
                NodeConstant dy = args.get(4).subtract(args.get(1));
                NodeConstant dz = args.get(5).subtract(args.get(2));
                return squared(dx).add(squared(dy)).add(squared(dz)).mapMagnitude(Math::sqrt);
            });

    /**
     * Manhattan distance
     */
    public static final MathFunction MANHATTAN = FunctionBuilder
            .named("manhattan")
            .describedAs("Returns the Manhattan (taxicab) distance between (x1,y1) and (x2,y2)")
            .withParams("x1", "y1", "x2", "y2")
            .inCategory(UTILITY)
            .takingBetween(4, 4)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant dx = args.get(2).subtract(args.get(0)).abs();
                NodeConstant dy = args.get(3).subtract(args.get(1)).abs();
                return dx.add(dy);
            });

    // ==================== Comparison Functions ====================

    /**
     * Approximately equal within tolerance
     */
    public static final MathFunction APPROXEQ = FunctionBuilder
            .named("approxeq")
            .alias("approx", "isclose")
            .describedAs("Returns true if a and b are within the given tolerance of each other")
            .withParams("a", "b")
            .withParams("a", "b", "tolerance")
            .inCategory(UTILITY)
            .takingBetween(2, 3)
            .implementedByAggregate((args, ctx) -> {
                // Subtracting rather than converting to doubles is what lets
                // approxeq(1 m, 100 cm) see one value in two spellings
                NodeConstant gap = args.get(0).subtract(args.get(1)).abs();
                NodeConstant tolerance = args.size() > 2 ? args.get(2).abs() : new NodeDouble(1e-9);
                return new NodeBoolean(gap.compareTo(tolerance) <= 0);
            });

    /**
     * Compare (returns -1, 0, or 1)
     */
    public static final MathFunction COMPARE = FunctionBuilder
            .named("compare")
            .alias("cmp")
            .describedAs("Returns -1, 0, or 1 depending on whether a is less than, equal to, or greater than b")
            .withParams("a", "b")
            .inCategory(UTILITY)
            .takingBinary()
            .implementedBy((a, b, ctx) -> {
                int cmp = a.compareTo(b);
                return new NodeRational(Integer.signum(cmp));
            });

    // ==================== Interest and Financial ====================

    /**
     * Compound interest
     */
    public static final MathFunction COMPOUNDINTEREST = FunctionBuilder
            .named("compound")
            .describedAs("Returns the final amount after compound interest over time periods")
            .withParams("principal", "rate", "time")
            .withParams("principal", "rate", "time", "n")
            .inCategory(UTILITY)
            .takingBetween(3, 4)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant principal = args.get(0);
                NodeConstant rate = args.get(1);
                NodeConstant time = args.get(2);
                NodeConstant periods = args.size() > 3 ? args.get(3) : new NodeRational(1);

                NodeConstant perPeriod = rate.divide(new NodeRational(100).multiply(periods));
                NodeConstant growth = new NodeRational(1).add(perPeriod).power(periods.multiply(time));
                return principal.multiply(growth);
            });

    // ==================== Wrap and Normalize ====================

    /**
     * Wrap value to [min, max) range
     */
    public static final MathFunction WRAP = FunctionBuilder
            .named("wrap")
            .describedAs("Wraps value into the range [min, max) by repeating cyclically")
            .withParams("value", "min", "max")
            .inCategory(UTILITY)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant value = args.get(0);
                NodeConstant min = args.get(1);
                NodeConstant max = args.get(2);
                if (min.compareTo(max) >= 0) {
                    throw new DomainException("wrap: min must be less than max");
                }
                NodeConstant range = max.subtract(min);
                NodeConstant offset = value.subtract(min).modulo(range).add(range).modulo(range);
                return offset.add(min);
            });

    /**
     * Normalize value to [0, 1] range
     */
    public static final MathFunction NORMALIZE = FunctionBuilder
            .named("normalize")
            .describedAs("Normalizes value to the [0, 1] range given min and max")
            .withParams("value", "min", "max")
            .inCategory(UTILITY)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant value = args.get(0);
                NodeConstant min = args.get(1);
                NodeConstant max = args.get(2);
                if (min.equalTo(max)) {
                    throw new DomainException("normalize: min and max cannot be equal");
                }
                return value.subtract(min).divide(max.subtract(min));
            });

    /**
     * Degrees to radians
     */
    public static final MathFunction DEG2RAD = FunctionBuilder
            .named("deg2rad")
            .alias("radians")
            .describedAs("Converts x from degrees to radians")
            .withParams("x")
            .inCategory(UTILITY)
            .takingUnary()
            .implementedByDouble(Math::toRadians);

    /**
     * Radians to degrees
     */
    public static final MathFunction RAD2DEG = FunctionBuilder
            .named("rad2deg")
            .alias("degrees")
            .describedAs("Converts x from radians to degrees")
            .withParams("x")
            .inCategory(UTILITY)
            .takingUnary()
            .implementedByDouble(Math::toDegrees);

    // ==================== Helpers ====================

    private static boolean isWhole(double value) {
        return Double.isFinite(value) && value == Math.floor(value);
    }

    private static NodeConstant squared(NodeConstant value) {
        return value.multiply(value);
    }

    /**
     * Gets all utility functions.
     */
    public static List<MathFunction> all() {
        return List.of(
                FRAC, PERMUTATION, DOUBLEFACTORIAL,
                INVERSELERP, REMAP, SMOOTHSTEP,
                DISTANCE2D, DISTANCE3D, MANHATTAN,
                APPROXEQ, COMPARE,
                COMPOUNDINTEREST,
                WRAP, NORMALIZE, DEG2RAD, RAD2DEG
        );
    }
}
