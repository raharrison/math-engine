package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodePercent;

import java.util.List;

/**
 * Collection of percentage-related functions.
 * <p>
 * Percentages are represented as decimal values internally (50% = 0.5).
 * These functions help with common percentage calculations.
 */
public final class PercentageFunctions {

    private PercentageFunctions() {
    }

    // ==================== Conversion Functions ====================

    /**
     * Convert number to percentage (50 -> 50%)
     */
    public static final MathFunction PERCENT = FunctionBuilder
            .named("percent")
            .alias("pct")
            .describedAs("Wraps x as a percentage value (50 → 50%)")
            .withParams("x")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                double value = ctx.toNumber(arg).doubleValue();
                return new NodePercent(value);  // NodePercent constructor divides by 100
            });

    /**
     * Convert percentage to decimal (50% -> 0.5)
     */
    public static final MathFunction TOPERCENT = FunctionBuilder
            .named("topercent")
            .describedAs("Returns the decimal value of a percentage (50% → 0.5)")
            .withParams("x")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                double value = ctx.toPercentDecimal(arg);
                return new NodeDouble(value);
            });

    /**
     * Get percentage value (50% -> 50)
     */
    public static final MathFunction PERCENTVALUE = FunctionBuilder
            .named("percentvalue")
            .alias("pctvalue")
            .describedAs("Returns the numeric value of a percentage (50% → 50)")
            .withParams("x")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                if (arg instanceof NodePercent pct) {
                    return new NodeDouble(pct.getPercentValue());
                }
                return new NodeDouble(ctx.toNumber(arg).doubleValue() * 100);
            });

    // ==================== Percentage Calculations ====================

    /**
     * Calculate percentage of a value (what is 20% of 50?)
     */
    public static final MathFunction PERCENTOF = FunctionBuilder
            .named("percentof")
            .alias("pctof")
            .describedAs("Returns what percent of total equals (e.g. percentof(20%, 50) → 10)")
            .withParams("percent", "total")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double percent = ctx.toPercentDecimal(first);
                double value = ctx.toNumber(second).doubleValue();
                return new NodeDouble(percent * value);
            });

    /**
     * What percent is X of Y? (10 is what % of 50? = 20%)
     */
    public static final MathFunction WHATPERCENT = FunctionBuilder
            .named("whatpercent")
            .alias("whatpct")
            .describedAs("Returns what percentage value is of total (e.g. whatpercent(10, 50) → 20%)")
            .withParams("value", "total")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double part = ctx.toNumber(first).doubleValue();
                double whole = ctx.toNumber(second).doubleValue();
                if (whole == 0) {
                    throw new IllegalArgumentException("whatpercent: division by zero");
                }
                return new NodePercent((part / whole) * 100);
            });

    /**
     * Percentage change from old to new value
     */
    public static final MathFunction PERCENTCHANGE = FunctionBuilder
            .named("percentchange")
            .alias("pctchange", "pctdiff")
            .describedAs("Returns the percentage change from 'from' to 'to'")
            .withParams("from", "to")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double oldValue = ctx.toNumber(first).doubleValue();
                double newValue = ctx.toNumber(second).doubleValue();
                if (oldValue == 0) {
                    throw new IllegalArgumentException("percentchange: old value cannot be zero");
                }
                double change = ((newValue - oldValue) / oldValue) * 100;
                return new NodePercent(change);
            });

    /**
     * Add percentage to value (100 + 20% = 120)
     */
    public static final MathFunction ADDPERCENT = FunctionBuilder
            .named("addpercent")
            .alias("addpct", "markup")
            .describedAs("Returns value increased by percent (e.g. addpercent(100, 20%) → 120)")
            .withParams("value", "percent")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double value = ctx.toNumber(first).doubleValue();
                double percent = ctx.toPercentDecimal(second);
                return new NodeDouble(value * (1 + percent));
            });

    /**
     * Subtract percentage from value (100 - 20% = 80)
     */
    public static final MathFunction SUBTRACTPERCENT = FunctionBuilder
            .named("subtractpercent")
            .alias("subpct", "discount")
            .describedAs("Returns value decreased by percent (e.g. subtractpercent(100, 20%) → 80)")
            .withParams("value", "percent")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double value = ctx.toNumber(first).doubleValue();
                double percent = ctx.toPercentDecimal(second);
                return new NodeDouble(value * (1 - percent));
            });

    // ==================== Percentage Inverse Operations ====================

    /**
     * Find original value before percentage was added (120 with 20% markup was what?)
     */
    public static final MathFunction REVERSEPERCENT = FunctionBuilder
            .named("reversepercent")
            .alias("reversepct", "unmarkup")
            .describedAs("Returns the original value before a percentage was applied")
            .withParams("result", "percent")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double current = ctx.toNumber(first).doubleValue();
                double percent = ctx.toPercentDecimal(second);
                if (percent == -1) {
                    throw new IllegalArgumentException("reversepercent: cannot reverse 100% decrease");
                }
                return new NodeDouble(current / (1 + percent));
            });

    /**
     * Calculate percentage from ratio (1.2 -> 20% increase)
     */
    public static final MathFunction RATIOTOPERCENT = FunctionBuilder
            .named("ratiotopercent")
            .alias("ratiopct")
            .describedAs("Converts a ratio to a percentage change (1.2 → 20%)")
            .withParams("ratio")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                double ratio = ctx.toNumber(arg).doubleValue();
                return new NodePercent((ratio - 1) * 100);
            });

    /**
     * Calculate ratio from percentage (20% -> 1.2)
     */
    public static final MathFunction PERCENTTORATIO = FunctionBuilder
            .named("percenttoratio")
            .alias("pctratio")
            .describedAs("Converts a percentage change to a ratio (20% → 1.2)")
            .withParams("percent")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                double percent = ctx.toPercentDecimal(arg);
                return new NodeDouble(1 + percent);
            });

    // ==================== Percentage Points ====================

    /**
     * Difference in percentage points (30% to 50% = 20pp)
     */
    public static final MathFunction PERCENTPOINTS = FunctionBuilder
            .named("percentpoints")
            .alias("pp")
            .describedAs("Returns the difference in percentage points between a and b")
            .withParams("a", "b")
            .inCategory(MathFunction.Category.PERCENTAGE)
            .takingBinary()
            .noBroadcasting()
            .implementedBy((first, second, ctx) -> {
                double pct1 = ctx.toPercentDecimal(first) * 100;
                double pct2 = ctx.toPercentDecimal(second) * 100;
                return new NodeDouble(pct2 - pct1);
            });

    /**
     * Check if value is a percentage type
     */
    public static final MathFunction ISPERCENT = new MathFunction() {
        @Override
        public String name() {
            return "ispercent";
        }

        @Override
        public String description() {
            return "Returns true if x is a percentage value";
        }

        @Override
        public List<List<String>> parameterSets() {
            return List.of(List.of("x"));
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.PERCENTAGE;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            return new NodeBoolean(args.getFirst() instanceof NodePercent);
        }
    };

    /**
     * Gets all percentage functions.
     */
    public static List<MathFunction> all() {
        return List.of(
                // Conversion
                PERCENT, TOPERCENT, PERCENTVALUE,
                // Calculations
                PERCENTOF, WHATPERCENT, PERCENTCHANGE,
                ADDPERCENT, SUBTRACTPERCENT,
                // Inverse operations
                REVERSEPERCENT, RATIOTOPERCENT, PERCENTTORATIO,
                // Percentage points
                PERCENTPOINTS, ISPERCENT
        );
    }
}
