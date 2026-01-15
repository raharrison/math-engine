package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.parser.function.AggregateFunction;
import uk.co.ryanharrison.mathengine.parser.function.BinaryFunction;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.UnaryFunction;
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
    public static final MathFunction FRAC = UnaryFunction.ofDouble("frac", "Fractional part (x - floor(x))", UTILITY,
            List.of("fpart", "fractional"), MathUtils::fPart);

    // ==================== Combinatorial Functions ====================

    /**
     * Permutation: n! / (n-r)!
     */
    public static final MathFunction PERMUTATION = BinaryFunction.of("permutation", "Permutation: n! / (n-r)!", UTILITY,
            List.of("perm", "npr"), (n, r, ctx) -> {
                double nVal = ctx.toNumber(n).doubleValue();
                double rVal = ctx.toNumber(r).doubleValue();
                if (rVal < 0 || rVal > nVal) {
                    return new NodeDouble(0);
                }
                return new NodeDouble(Gamma.gamma(nVal + 1) / Gamma.gamma(nVal - rVal + 1));
            });

    /**
     * Double factorial: n!!
     */
    public static final MathFunction DOUBLEFACTORIAL = UnaryFunction.ofDouble("doublefactorial", "Double factorial: n!!", UTILITY,
            List.of("dfact"), MathUtils::doubleFactorial);

    // ==================== Interpolation Functions ====================

    /**
     * Inverse linear interpolation
     */
    public static final MathFunction INVERSELERP = AggregateFunction.of("inverselerp", "Inverse linear interpolation", UTILITY,
            List.of("ilerp"), 3, 3, (args, ctx) -> {
                double start = ctx.toNumber(args.get(0)).doubleValue();
                double end = ctx.toNumber(args.get(1)).doubleValue();
                double value = ctx.toNumber(args.get(2)).doubleValue();
                if (start == end) {
                    throw new IllegalArgumentException("inverselerp: start and end cannot be equal");
                }
                return new NodeDouble((value - start) / (end - start));
            });

    /**
     * Map/remap value from one range to another
     */
    public static final MathFunction REMAP = AggregateFunction.of("remap", "Map value from one range to another", UTILITY,
            List.of(), 5, 5, (args, ctx) -> {
                double value = ctx.toNumber(args.get(0)).doubleValue();
                double fromMin = ctx.toNumber(args.get(1)).doubleValue();
                double fromMax = ctx.toNumber(args.get(2)).doubleValue();
                double toMin = ctx.toNumber(args.get(3)).doubleValue();
                double toMax = ctx.toNumber(args.get(4)).doubleValue();
                if (fromMin == fromMax) {
                    throw new IllegalArgumentException("map: source range cannot have zero width");
                }
                double t = (value - fromMin) / (fromMax - fromMin);
                return new NodeDouble(toMin + t * (toMax - toMin));
            });

    /**
     * Smooth step interpolation (cubic Hermite)
     */
    public static final MathFunction SMOOTHSTEP = AggregateFunction.of("smoothstep", "Smooth step interpolation", UTILITY, 3, 3, (args, ctx) -> {
        double edge0 = ctx.toNumber(args.get(0)).doubleValue();
        double edge1 = ctx.toNumber(args.get(1)).doubleValue();
        double x = ctx.toNumber(args.get(2)).doubleValue();
        double t = (x - edge0) / (edge1 - edge0);
        t = Math.max(0, Math.min(1, t));
        return new NodeDouble(t * t * (3 - 2 * t));
    });

    // ==================== Distance Functions ====================

    /**
     * 2D Euclidean distance
     */
    public static final MathFunction DISTANCE2D = AggregateFunction.of("distance", "2D Euclidean distance", UTILITY,
            List.of("dist"), 4, 4, (args, ctx) -> {
                double x1 = ctx.toNumber(args.get(0)).doubleValue();
                double y1 = ctx.toNumber(args.get(1)).doubleValue();
                double x2 = ctx.toNumber(args.get(2)).doubleValue();
                double y2 = ctx.toNumber(args.get(3)).doubleValue();
                return new NodeDouble(Math.hypot(x2 - x1, y2 - y1));
            });

    /**
     * 3D Euclidean distance
     */
    public static final MathFunction DISTANCE3D = AggregateFunction.of("distance3d", "3D Euclidean distance", UTILITY,
            List.of("dist3d"), 6, 6, (args, ctx) -> {
                double x1 = ctx.toNumber(args.get(0)).doubleValue();
                double y1 = ctx.toNumber(args.get(1)).doubleValue();
                double z1 = ctx.toNumber(args.get(2)).doubleValue();
                double x2 = ctx.toNumber(args.get(3)).doubleValue();
                double y2 = ctx.toNumber(args.get(4)).doubleValue();
                double z2 = ctx.toNumber(args.get(5)).doubleValue();
                double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
                return new NodeDouble(Math.sqrt(dx * dx + dy * dy + dz * dz));
            });

    /**
     * Manhattan distance
     */
    public static final MathFunction MANHATTAN = AggregateFunction.of("manhattan", "Manhattan (taxicab) distance", UTILITY, 4, 4, (args, ctx) -> {
        double x1 = ctx.toNumber(args.get(0)).doubleValue();
        double y1 = ctx.toNumber(args.get(1)).doubleValue();
        double x2 = ctx.toNumber(args.get(2)).doubleValue();
        double y2 = ctx.toNumber(args.get(3)).doubleValue();
        return new NodeDouble(Math.abs(x2 - x1) + Math.abs(y2 - y1));
    });

    // ==================== Comparison Functions ====================

    /**
     * Approximately equal within tolerance
     */
    public static final MathFunction APPROXEQ = AggregateFunction.of("approxeq", "Approximately equal within tolerance", UTILITY,
            List.of("approx", "isclose"), 2, 3, (args, ctx) -> {
                double a = ctx.toNumber(args.get(0)).doubleValue();
                double b = ctx.toNumber(args.get(1)).doubleValue();
                double tolerance = args.size() > 2 ? ctx.toNumber(args.get(2)).doubleValue() : 1e-9;
                return new NodeBoolean(Math.abs(a - b) <= Math.abs(tolerance));
            });

    /**
     * Compare (returns -1, 0, or 1)
     */
    public static final MathFunction COMPARE = BinaryFunction.of("compare", "Compare: returns -1, 0, or 1", UTILITY,
            List.of("cmp"), (a, b, ctx) -> new NodeRational(Double.compare(ctx.toNumber(a).doubleValue(), ctx.toNumber(b).doubleValue())));

    // ==================== Interest and Financial ====================

    /**
     * Compound interest
     */
    public static final MathFunction COMPOUNDINTEREST = AggregateFunction.of("compound", "Compound interest", UTILITY, 3, 4, (args, ctx) -> {
        double principal = ctx.toNumber(args.get(0)).doubleValue();
        double rate = ctx.toNumber(args.get(1)).doubleValue();
        double time = ctx.toNumber(args.get(2)).doubleValue();
        double n = args.size() > 3 ? ctx.toNumber(args.get(3)).doubleValue() : 1;
        return new NodeDouble(principal * Math.pow(1 + rate / (100 * n), n * time));
    });

    // ==================== Wrap and Normalize ====================

    /**
     * Wrap value to [min, max) range
     */
    public static final MathFunction WRAP = AggregateFunction.of("wrap", "Wrap value to [min, max) range", UTILITY, 3, 3, (args, ctx) -> {
        double value = ctx.toNumber(args.get(0)).doubleValue();
        double min = ctx.toNumber(args.get(1)).doubleValue();
        double max = ctx.toNumber(args.get(2)).doubleValue();
        if (min >= max) {
            throw new IllegalArgumentException("wrap: min must be less than max");
        }
        double range = max - min;
        return new NodeDouble(((value - min) % range + range) % range + min);
    });

    /**
     * Normalize value to [0, 1] range
     */
    public static final MathFunction NORMALIZE = AggregateFunction.of("normalize", "Normalize value to [0, 1] range", UTILITY, 3, 3, (args, ctx) -> {
        double value = ctx.toNumber(args.get(0)).doubleValue();
        double min = ctx.toNumber(args.get(1)).doubleValue();
        double max = ctx.toNumber(args.get(2)).doubleValue();
        if (min == max) {
            throw new IllegalArgumentException("normalize: min and max cannot be equal");
        }
        return new NodeDouble((value - min) / (max - min));
    });

    /**
     * Degrees to radians
     */
    public static final MathFunction DEG2RAD = UnaryFunction.ofDouble("deg2rad", "Convert degrees to radians", UTILITY,
            List.of("radians"), Math::toRadians);

    /**
     * Radians to degrees
     */
    public static final MathFunction RAD2DEG = UnaryFunction.ofDouble("rad2deg", "Convert radians to degrees", UTILITY,
            List.of("degrees"), Math::toDegrees);

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
