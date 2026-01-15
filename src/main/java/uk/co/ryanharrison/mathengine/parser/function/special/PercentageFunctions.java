package uk.co.ryanharrison.mathengine.parser.function.special;

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
    public static final MathFunction PERCENT = new MathFunction() {
        @Override
        public String name() {
            return "percent";
        }

        @Override
        public List<String> aliases() {
            return List.of("pct");
        }

        @Override
        public String description() {
            return "Convert number to percentage (50 -> 50%)";
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
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            double value = ctx.toNumber(args.getFirst()).doubleValue();
            return new NodePercent(value);  // NodePercent constructor divides by 100
        }
    };

    /**
     * Convert percentage to decimal (50% -> 0.5)
     */
    public static final MathFunction TOPERCENT = new MathFunction() {
        @Override
        public String name() {
            return "topercent";
        }

        @Override
        public String description() {
            return "Get decimal value of percentage";
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
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            double value = ctx.toPercentDecimal(args.getFirst());
            return new NodeDouble(value);
        }
    };

    /**
     * Get percentage value (50% -> 50)
     */
    public static final MathFunction PERCENTVALUE = new MathFunction() {
        @Override
        public String name() {
            return "percentvalue";
        }

        @Override
        public List<String> aliases() {
            return List.of("pctvalue");
        }

        @Override
        public String description() {
            return "Get percentage value (50% -> 50)";
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
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant arg = args.getFirst();
            if (arg instanceof NodePercent pct) {
                return new NodeDouble(pct.getPercentValue());
            }
            return new NodeDouble(ctx.toNumber(arg).doubleValue() * 100);
        }
    };

    // ==================== Percentage Calculations ====================

    /**
     * Calculate percentage of a value (what is 20% of 50?)
     */
    public static final MathFunction PERCENTOF = new MathFunction() {
        @Override
        public String name() {
            return "percentof";
        }

        @Override
        public List<String> aliases() {
            return List.of("pctof");
        }

        @Override
        public String description() {
            return "Calculate percentage of value";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
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
            double percent = ctx.toPercentDecimal(args.get(0));
            double value = ctx.toNumber(args.get(1)).doubleValue();
            return new NodeDouble(percent * value);
        }
    };

    /**
     * What percent is X of Y? (10 is what % of 50? = 20%)
     */
    public static final MathFunction WHATPERCENT = new MathFunction() {
        @Override
        public String name() {
            return "whatpercent";
        }

        @Override
        public List<String> aliases() {
            return List.of("whatpct");
        }

        @Override
        public String description() {
            return "Calculate what percent X is of Y";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
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
            double part = ctx.toNumber(args.get(0)).doubleValue();
            double whole = ctx.toNumber(args.get(1)).doubleValue();
            if (whole == 0) {
                throw new IllegalArgumentException("whatpercent: division by zero");
            }
            return new NodePercent((part / whole) * 100);
        }
    };

    /**
     * Percentage change from old to new value
     */
    public static final MathFunction PERCENTCHANGE = new MathFunction() {
        @Override
        public String name() {
            return "percentchange";
        }

        @Override
        public List<String> aliases() {
            return List.of("pctchange", "pctdiff");
        }

        @Override
        public String description() {
            return "Percentage change from old to new";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
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
            double oldValue = ctx.toNumber(args.get(0)).doubleValue();
            double newValue = ctx.toNumber(args.get(1)).doubleValue();
            if (oldValue == 0) {
                throw new IllegalArgumentException("percentchange: old value cannot be zero");
            }
            double change = ((newValue - oldValue) / oldValue) * 100;
            return new NodePercent(change);
        }
    };

    /**
     * Add percentage to value (100 + 20% = 120)
     */
    public static final MathFunction ADDPERCENT = new MathFunction() {
        @Override
        public String name() {
            return "addpercent";
        }

        @Override
        public List<String> aliases() {
            return List.of("addpct", "markup");
        }

        @Override
        public String description() {
            return "Add percentage to value";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
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
            double value = ctx.toNumber(args.get(0)).doubleValue();
            double percent = ctx.toPercentDecimal(args.get(1));
            return new NodeDouble(value * (1 + percent));
        }
    };

    /**
     * Subtract percentage from value (100 - 20% = 80)
     */
    public static final MathFunction SUBTRACTPERCENT = new MathFunction() {
        @Override
        public String name() {
            return "subtractpercent";
        }

        @Override
        public List<String> aliases() {
            return List.of("subpct", "discount");
        }

        @Override
        public String description() {
            return "Subtract percentage from value";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
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
            double value = ctx.toNumber(args.get(0)).doubleValue();
            double percent = ctx.toPercentDecimal(args.get(1));
            return new NodeDouble(value * (1 - percent));
        }
    };

    // ==================== Percentage Inverse Operations ====================

    /**
     * Find original value before percentage was added (120 with 20% markup was what?)
     */
    public static final MathFunction REVERSEPERCENT = new MathFunction() {
        @Override
        public String name() {
            return "reversepercent";
        }

        @Override
        public List<String> aliases() {
            return List.of("reversepct", "unmarkup");
        }

        @Override
        public String description() {
            return "Find original value before percentage increase";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
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
            double current = ctx.toNumber(args.get(0)).doubleValue();
            double percent = ctx.toPercentDecimal(args.get(1));
            if (percent == -1) {
                throw new IllegalArgumentException("reversepercent: cannot reverse 100% decrease");
            }
            return new NodeDouble(current / (1 + percent));
        }
    };

    /**
     * Calculate percentage from ratio (1.2 -> 20% increase)
     */
    public static final MathFunction RATIOTOPERCENT = new MathFunction() {
        @Override
        public String name() {
            return "ratiotopercent";
        }

        @Override
        public List<String> aliases() {
            return List.of("ratiopct");
        }

        @Override
        public String description() {
            return "Convert ratio to percentage change";
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
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            double ratio = ctx.toNumber(args.getFirst()).doubleValue();
            return new NodePercent((ratio - 1) * 100);
        }
    };

    /**
     * Calculate ratio from percentage (20% -> 1.2)
     */
    public static final MathFunction PERCENTTORATIO = new MathFunction() {
        @Override
        public String name() {
            return "percenttoratio";
        }

        @Override
        public List<String> aliases() {
            return List.of("pctratio");
        }

        @Override
        public String description() {
            return "Convert percentage change to ratio";
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
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            double percent = ctx.toPercentDecimal(args.getFirst());
            return new NodeDouble(1 + percent);
        }
    };

    // ==================== Percentage Points ====================

    /**
     * Difference in percentage points (30% to 50% = 20pp)
     */
    public static final MathFunction PERCENTPOINTS = new MathFunction() {
        @Override
        public String name() {
            return "percentpoints";
        }

        @Override
        public List<String> aliases() {
            return List.of("pp");
        }

        @Override
        public String description() {
            return "Difference in percentage points";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
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
            double pct1 = ctx.toPercentDecimal(args.get(0)) * 100;
            double pct2 = ctx.toPercentDecimal(args.get(1)) * 100;
            return new NodeDouble(pct2 - pct1);
        }
    };

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
            return "Check if value is a percentage";
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
