package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
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
    public static final MathFunction IF = FunctionBuilder
            .named("if")
            .describedAs("Conditional if-then-else")
            .inCategory(CONDITIONAL)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> ctx.toBoolean(args.getFirst()) ? args.get(1) : args.get(2));

    /**
     * Clamp value to range
     */
    public static final MathFunction CLAMP = FunctionBuilder
            .named("clamp")
            .describedAs("Clamp value to range [min, max]")
            .inCategory(CONDITIONAL)
            .takingTyped(ArgTypes.number(), ArgTypes.number(), ArgTypes.number())
            .implementedBy((value, min, max, ctx) -> new NodeDouble(Math.max(min, Math.min(max, value))));

    /**
     * Linear interpolation
     */
    public static final MathFunction LERP = FunctionBuilder
            .named("lerp")
            .describedAs("Linear interpolation between a and b")
            .inCategory(CONDITIONAL)
            .takingTyped(ArgTypes.number(), ArgTypes.number(), ArgTypes.number())
            .implementedBy((a, b, t, ctx) -> new NodeDouble(a + t * (b - a)));

    /**
     * Gets all conditional functions.
     */
    public static List<MathFunction> all() {
        return List.of(IF, CLAMP, LERP);
    }
}
