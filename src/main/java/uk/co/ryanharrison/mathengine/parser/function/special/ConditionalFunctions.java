package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;

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
    public static final MathFunction IF = FunctionBuilder
            .named("if")
            .describedAs("Returns 'then' if condition is true, otherwise returns 'else'")
            .withParams("condition", "then", "else")
            .inCategory(CONDITIONAL)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> ctx.toBoolean(args.getFirst()) ? args.get(1) : args.get(2));

    /**
     * Clamp value to range
     */
    public static final MathFunction CLAMP = FunctionBuilder
            .named("clamp")
            .describedAs("Returns value clamped to the range [min, max]")
            .withParams("value", "min", "max")
            .inCategory(CONDITIONAL)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant value = args.get(0), min = args.get(1), max = args.get(2);
                return BroadcastingEngine.applyBinary(
                        BroadcastingEngine.applyBinary(value, min,
                                (v, lo) -> v.compareTo(lo) < 0 ? lo : v),
                        max,
                        (v, hi) -> v.compareTo(hi) > 0 ? hi : v);
            });

    /**
     * Linear interpolation
     */
    public static final MathFunction LERP = FunctionBuilder
            .named("lerp")
            .describedAs("Linearly interpolates between a and b by factor t (0=a, 1=b)")
            .withParams("a", "b", "t")
            .inCategory(CONDITIONAL)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant a = args.get(0), b = args.get(1), t = args.get(2);
                // lerp = a + (b - a) * t
                return a.add(b.subtract(a).multiply(t));
            });

    /**
     * Gets all conditional functions.
     */
    public static List<MathFunction> all() {
        return List.of(IF, CLAMP, LERP);
    }
}
