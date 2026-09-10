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
     * Only the taken branch is evaluated, so recursive base cases terminate.
     */
    public static final MathFunction IF = FunctionBuilder
            .named("if")
            .describedAs("Returns 'then' if condition is true, otherwise returns 'else'")
            .withParams("condition", "then", "else")
            .inCategory(CONDITIONAL)
            .takingExactly(3)
            .implementedByLazy((args, ctx, evaluate) ->
                    ctx.toBoolean(evaluate.evaluate(args.getFirst()))
                            ? evaluate.evaluate(args.get(1))
                            : evaluate.evaluate(args.get(2)));

    /** Clamps a value into [min, max], broadcasting over collections. */
    public static final MathFunction CLAMP = FunctionBuilder
            .named("clamp")
            .describedAs("Returns value clamped to the range [min, max]")
            .withParams("value", "min", "max")
            .inCategory(CONDITIONAL)
            .takingExactly(3)
            .implementedByAggregate((args, ctx) -> {
                NodeConstant value = args.get(0), min = args.get(1), max = args.get(2);
                return BroadcastingEngine.applyBinary(
                        BroadcastingEngine.applyBinary(value, min,
                                (v, lo) -> v.compareTo(lo) < 0 ? lo : v),
                        max,
                        (v, hi) -> v.compareTo(hi) > 0 ? hi : v);
            });

    /** Linear interpolation: a + (b - a) * t. */
    public static final MathFunction LERP = FunctionBuilder
            .named("lerp")
            .describedAs("Linearly interpolates between a and b by factor t (0=a, 1=b)")
            .withParams("a", "b", "t")
            .inCategory(CONDITIONAL)
            .takingExactly(3)
            .implementedByAggregate((args, ctx) ->
                    args.get(0).add(args.get(1).subtract(args.get(0)).multiply(args.get(2))));

    public static List<MathFunction> all() {
        return List.of(IF, CLAMP, LERP);
    }
}
