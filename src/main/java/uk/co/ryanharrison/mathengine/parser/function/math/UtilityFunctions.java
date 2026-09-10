package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;
import uk.co.ryanharrison.mathengine.special.Gamma;
import uk.co.ryanharrison.mathengine.utils.MathUtils;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.UTILITY;

/**
 * Collection of general-purpose utility functions.
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
            .implementedByMagnitude(MathUtils::fPart);

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
                    return new NodeDouble(0);
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
                double start = ctx.toDouble(args.get(0));
                double end = ctx.toDouble(args.get(1));
                double value = ctx.toDouble(args.get(2));
                if (start == end) {
                    throw new DomainException("inverselerp: start and end cannot be equal");
                }
                return new NodeDouble((value - start) / (end - start));
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
                double value = ctx.toDouble(args.get(0));
                double fromMin = ctx.toDouble(args.get(1));
                double fromMax = ctx.toDouble(args.get(2));
                double toMin = ctx.toDouble(args.get(3));
                double toMax = ctx.toDouble(args.get(4));
                if (fromMin == fromMax) {
                    throw new DomainException("map: source range cannot have zero width");
                }
                double t = (value - fromMin) / (fromMax - fromMin);
                return new NodeDouble(toMin + t * (toMax - toMin));
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
                double edge0 = ctx.toDouble(args.get(0));
                double edge1 = ctx.toDouble(args.get(1));
                double x = ctx.toDouble(args.get(2));
                double t = (x - edge0) / (edge1 - edge0);
                t = Math.max(0, Math.min(1, t));
                return new NodeDouble(t * t * (3 - 2 * t));
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
                double x1 = ctx.toDouble(args.get(0));
                double y1 = ctx.toDouble(args.get(1));
                double x2 = ctx.toDouble(args.get(2));
                double y2 = ctx.toDouble(args.get(3));
                return new NodeDouble(Math.hypot(x2 - x1, y2 - y1));
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
                double x1 = ctx.toDouble(args.get(0));
                double y1 = ctx.toDouble(args.get(1));
                double z1 = ctx.toDouble(args.get(2));
                double x2 = ctx.toDouble(args.get(3));
                double y2 = ctx.toDouble(args.get(4));
                double z2 = ctx.toDouble(args.get(5));
                double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
                return new NodeDouble(Math.sqrt(dx * dx + dy * dy + dz * dz));
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
                double x1 = ctx.toDouble(args.get(0));
                double y1 = ctx.toDouble(args.get(1));
                double x2 = ctx.toDouble(args.get(2));
                double y2 = ctx.toDouble(args.get(3));
                return new NodeDouble(Math.abs(x2 - x1) + Math.abs(y2 - y1));
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
                double a = ctx.toDouble(args.get(0));
                double b = ctx.toDouble(args.get(1));
                double tolerance = args.size() > 2 ? ctx.toDouble(args.get(2)) : 1e-9;
                return new NodeBoolean(Math.abs(a - b) <= Math.abs(tolerance));
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
                double principal = ctx.toDouble(args.get(0));
                double rate = ctx.toDouble(args.get(1));
                double time = ctx.toDouble(args.get(2));
                double n = args.size() > 3 ? ctx.toDouble(args.get(3)) : 1;
                return new NodeDouble(principal * Math.pow(1 + rate / (100 * n), n * time));
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
                double value = ctx.toDouble(args.get(0));
                double min = ctx.toDouble(args.get(1));
                double max = ctx.toDouble(args.get(2));
                if (min >= max) {
                    throw new DomainException("wrap: min must be less than max");
                }
                double range = max - min;
                return new NodeDouble(((value - min) % range + range) % range + min);
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
                double value = ctx.toDouble(args.get(0));
                double min = ctx.toDouble(args.get(1));
                double max = ctx.toDouble(args.get(2));
                if (min == max) {
                    throw new DomainException("normalize: min and max cannot be equal");
                }
                return new NodeDouble((value - min) / (max - min));
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
