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
            .takingTyped(ArgTypes.number(), ArgTypes.number(), ArgTypes.number())
            .implementedBy((value, min, max, ctx) -> new NodeDouble(Math.max(min, Math.min(max, value))));

    /**
     * Linear interpolation
     */
    public static final MathFunction LERP = FunctionBuilder
            .named("lerp")
            .describedAs("Linearly interpolates between a and b by factor t (0=a, 1=b)")
            .withParams("a", "b", "t")
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
