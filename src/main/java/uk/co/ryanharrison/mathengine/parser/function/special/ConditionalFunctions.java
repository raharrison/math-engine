package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.AggregateFunction;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.CONDITIONAL;

/**
 * Collection of conditional and control flow functions.
 */
public final class ConditionalFunctions {

    private ConditionalFunctions() {
    }

    /**
     * Conditional if-then-else function
     */
    public static final MathFunction IF = AggregateFunction.of("if", "Conditional if-then-else", CONDITIONAL, 3, 3,
            (args, ctx) -> ctx.toBoolean(args.getFirst()) ? args.get(1) : args.get(2));

    /**
     * Clamp value to range
     */
    public static final MathFunction CLAMP = AggregateFunction.of("clamp", "Clamp value to range [min, max]", CONDITIONAL, 3, 3, (args, ctx) -> {
        double value = ctx.toNumber(args.get(0)).doubleValue();
        double min = ctx.toNumber(args.get(1)).doubleValue();
        double max = ctx.toNumber(args.get(2)).doubleValue();
        if (value < min) return args.get(1);
        if (value > max) return args.get(2);
        return args.get(0);
    });

    /**
     * Linear interpolation
     */
    public static final MathFunction LERP = AggregateFunction.of("lerp", "Linear interpolation between a and b", CONDITIONAL, 3, 3, (args, ctx) -> {
        double a = ctx.toNumber(args.get(0)).doubleValue();
        double b = ctx.toNumber(args.get(1)).doubleValue();
        double t = ctx.toNumber(args.get(2)).doubleValue();
        return new NodeDouble(a + t * (b - a));
    });

    /**
     * Gets all conditional functions.
     */
    public static List<MathFunction> all() {
        return List.of(IF, CLAMP, LERP);
    }
}
