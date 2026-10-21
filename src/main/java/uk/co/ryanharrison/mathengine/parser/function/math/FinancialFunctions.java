package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.core.Function;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.ast.NodePercent;
import uk.co.ryanharrison.mathengine.parser.ast.NodeRational;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;

import java.util.Comparator;
import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.FINANCIAL;

/**
 * Loans, savings and cash flows.
 * <p>
 * A rate is the rate per period, written either as a percentage or as a decimal, so
 * {@code 0.5%} and {@code 0.005} mean the same. A monthly payment on an annual rate divides
 * the rate by twelve: {@code pmt(6% / 12, 360, 200000)}. Amounts are combined with the value
 * arithmetic, so a unit on an amount rides through to the answer, and with no interest to
 * compound an exact amount gives an exact answer.
 * <p>
 * Every amount is positive: {@code pmt} is what you pay, not a negative cash flow as a
 * spreadsheet would show it. Only {@code npv} and {@code irr}, which take a series of
 * flows in and out, use the sign.
 *
 * <pre>{@code
 * pmt(6% / 12, 360, 200000)        // 1199.10, the monthly repayment
 * fv(5% / 12, 120, 100)            // 15528.23, saving 100 a month for ten years
 * pv(5% / 12, 120, 100)            // 9428.14, what those payments are worth today
 * npv(10%, {-1000, 500, 500, 500}) // 243.43
 * irr({-1000, 500, 500, 500})      // 23.38%
 * }</pre>
 */
public final class FinancialFunctions {

    /**
     * The search for an internal rate of return: a loss of almost everything per period, up
     * to a thousandfold gain.
     */
    private static final double IRR_LOWEST = -0.9999;
    private static final double IRR_HIGHEST = 1000;

    private FinancialFunctions() {
    }

    /**
     * The repayment per period for a loan
     */
    public static final MathFunction PMT = FunctionBuilder
            .named("pmt")
            .describedAs("Returns the payment per period that repays principal over n periods at the given rate per period")
            .withParams("rate", "n", "principal")
            .inCategory(FINANCIAL)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> {
                double rate = rate(args.get(0), ctx);
                NodeConstant periods = args.get(1);
                NodeConstant principal = args.get(2);
                if (rate == 0) {
                    return principal.divide(periods);
                }
                double n = ctx.toDouble(periods);
                return principal.multiply(new NodeDouble(rate / (1 - Math.pow(1 + rate, -n))));
            });

    /**
     * The value of a series of payments at the end of the term
     */
    public static final MathFunction FV = FunctionBuilder
            .named("fv")
            .describedAs("Returns the value after n periods of paying payment each period at the given rate, plus an optional starting amount")
            .withParams("rate", "n", "payment")
            .withParams("rate", "n", "payment", "present")
            .inCategory(FINANCIAL)
            .takingBetween(3, 4)
            .implementedByAggregate((args, ctx) -> {
                double rate = rate(args.get(0), ctx);
                NodeConstant periods = args.get(1);
                NodeConstant payment = args.get(2);
                NodeConstant present = args.size() > 3 ? args.get(3) : null;
                if (rate == 0) {
                    NodeConstant saved = payment.multiply(periods);
                    return present != null ? saved.add(present) : saved;
                }
                double growth = Math.pow(1 + rate, ctx.toDouble(periods));
                NodeConstant saved = payment.multiply(new NodeDouble((growth - 1) / rate));
                return present != null ? saved.add(present.multiply(new NodeDouble(growth))) : saved;
            });

    /**
     * What a series of payments is worth today
     */
    public static final MathFunction PV = FunctionBuilder
            .named("pv")
            .describedAs("Returns what n payments of payment, one each period, are worth today at the given rate")
            .withParams("rate", "n", "payment")
            .inCategory(FINANCIAL)
            .takingBetween(3, 3)
            .implementedByAggregate((args, ctx) -> {
                double rate = rate(args.get(0), ctx);
                NodeConstant periods = args.get(1);
                NodeConstant payment = args.get(2);
                if (rate == 0) {
                    return payment.multiply(periods);
                }
                double n = ctx.toDouble(periods);
                return payment.multiply(new NodeDouble((1 - Math.pow(1 + rate, -n)) / rate));
            });

    /**
     * Net present value of cash flows, the first one now
     */
    public static final MathFunction NPV = FunctionBuilder
            .named("npv")
            .describedAs("Returns the net present value of the cash flows at the given rate; the first flow is now, each one after a period later")
            .withParams("rate", "flows")
            .inCategory(FINANCIAL)
            .takingVariadic(2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double rate = rate(args.getFirst(), ctx);
                List<NodeConstant> flows = ctx.flattenArguments(args.subList(1, args.size()));
                NodeConstant total = new NodeRational(0);
                for (int t = 0; t < flows.size(); t++) {
                    NodeConstant flow = flows.get(t);
                    total = t == 0 ? flow : total.add(flow.multiply(new NodeDouble(Math.pow(1 + rate, -t))));
                }
                return total;
            });

    /**
     * Internal rate of return: the rate at which the cash flows' net present value is zero
     */
    public static final MathFunction IRR = FunctionBuilder
            .named("irr")
            .describedAs("Returns the rate per period, as a percentage, at which the net present value of the cash flows is zero")
            .withParams("flows")
            .inCategory(FINANCIAL)
            .takingVariadic(1)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                double[] flows = ctx.flattenToDoubles(args);
                boolean anyIn = false;
                boolean anyOut = false;
                for (double flow : flows) {
                    anyIn |= flow > 0;
                    anyOut |= flow < 0;
                }
                if (!anyIn || !anyOut) {
                    throw ctx.error("cash flows need at least one payment in and one out");
                }

                Function netPresentValue = Numerics.realFunction(rate -> {
                    double total = 0;
                    for (int t = 0; t < flows.length; t++) {
                        total += flows[t] / Math.pow(1 + rate, t);
                    }
                    return total;
                }, "npv");
                List<Double> rates = Numerics.run(ctx,
                        () -> Numerics.solver(netPresentValue, IRR_LOWEST, IRR_HIGHEST).solveAll());
                // Unusual flows can have several; the one nearest zero is the meaningful one
                double rate = rates.stream()
                        .min(Comparator.comparingDouble(Math::abs))
                        .orElseThrow(() -> ctx.error("no rate gives these cash flows a net present value of zero"));
                return NodePercent.fromDecimal(rate);
            });

    /**
     * A rate per period as a decimal, from a percentage or a plain number.
     */
    private static double rate(NodeConstant rate, FunctionContext ctx) {
        double value = ctx.toPercentDecimal(rate);
        if (value <= -1) {
            throw ctx.error("rate must be greater than -100%, got: " + value * 100 + "%");
        }
        return value;
    }

    /**
     * Gets all financial functions.
     */
    public static List<MathFunction> all() {
        return List.of(PMT, FV, PV, NPV, IRR);
    }
}
