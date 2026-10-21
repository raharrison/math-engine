package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.core.Function;
import uk.co.ryanharrison.mathengine.differential.DifferencesDirection;
import uk.co.ryanharrison.mathengine.differential.NumericalDifferentiationMethod;
import uk.co.ryanharrison.mathengine.differential.RichardsonExtrapolationMethod;
import uk.co.ryanharrison.mathengine.integral.SimpsonIntegrator;
import uk.co.ryanharrison.mathengine.parser.ast.Node;
import uk.co.ryanharrison.mathengine.parser.ast.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.ast.NodeVector;
import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.CALCULUS;

/**
 * Numerical calculus on a function of one variable, given as a lambda or a user function.
 * <p>
 * These hand the function to the library's numerical methods: Simpson's rule for
 * {@code integrate}, Richardson extrapolation for {@code deriv} and Brent's method for
 * {@code solve} and {@code roots}. Every answer is an approximation, so it is a double.
 * The function is evaluated in the session, so a trigonometric function inside it follows
 * the configured angle unit.
 *
 * <pre>{@code
 * integrate(x -> x^2, 0, 3)        // 9
 * deriv(x -> x^3, 2)               // 12
 * deriv(x -> x^3, 2, 2)            // 12, the second derivative
 * solve(x -> x^2 - 2, 0, 2)        // 1.41421356...
 * roots(x -> x^3 - x, -2, 2)       // {-1, 0, 1}
 * }</pre>
 */
public final class CalculusFunctions {

    /**
     * Slices for Simpson's rule. Its error falls with the fourth power of the slice width,
     * so this is accurate to around 1e-12 for a smooth function on a short interval.
     */
    private static final int INTEGRATION_SLICES = 1000;

    /**
     * Step size by order of derivative, from the first. A higher order divides by a higher
     * power of the step, so it needs a wider one to keep rounding error down.
     */
    private static final double[] DERIVATIVE_STEPS = {1e-3, 1e-3, 1e-2, 1e-2};

    private CalculusFunctions() {
    }

    /**
     * Definite integral
     */
    public static final MathFunction INTEGRATE = FunctionBuilder
            .named("integrate")
            .describedAs("Returns the definite integral of f from a to b, by Simpson's rule")
            .withParams("f", "a", "b")
            .inCategory(CALCULUS)
            .takingTyped(ArgTypes.function(), ArgTypes.number(), ArgTypes.number())
            .implementedBy((f, a, b, ctx) -> {
                Function function = Numerics.realFunction(f, ctx);
                if (a.equals(b)) {
                    return new NodeDouble(0);
                }
                // The integrator wants its bounds in order; reversing them negates the integral
                double lower = Math.min(a, b);
                double upper = Math.max(a, b);
                double area = Numerics.run(ctx, () ->
                        SimpsonIntegrator.of(function, lower, upper, INTEGRATION_SLICES).integrate());
                return new NodeDouble(a < b ? area : -area);
            });

    /**
     * Derivative at a point
     */
    public static final MathFunction DERIV = FunctionBuilder
            .named("deriv")
            .describedAs("Returns the derivative of f at x, or the given order of derivative (1 to 4)")
            .withParams("f", "x")
            .withParams("f", "x", "order")
            .inCategory(CALCULUS)
            .takingBetween(2, 3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                Function function = Numerics.realFunction(ArgTypes.function().extract(args.get(0), ctx), ctx);
                double x = ctx.toDouble(args.get(1));
                int order = args.size() > 2 ? ctx.requireInteger(args.get(2)) : 1;
                if (order < 1 || order > DERIVATIVE_STEPS.length) {
                    throw ctx.error("order must be between 1 and " + DERIVATIVE_STEPS.length + ", got: " + order);
                }
                return new NodeDouble(Numerics.run(ctx, () -> {
                    NumericalDifferentiationMethod method = RichardsonExtrapolationMethod.builder()
                            .targetFunction(function)
                            .targetPoint(x)
                            .stepSize(DERIVATIVE_STEPS[order - 1])
                            .direction(DifferencesDirection.Central)
                            .build();
                    return switch (order) {
                        case 1 -> method.deriveFirst();
                        case 2 -> method.deriveSecond();
                        case 3 -> method.deriveThird();
                        default -> method.deriveFourth();
                    };
                }));
            });

    /**
     * One root in an interval
     */
    public static final MathFunction SOLVE = FunctionBuilder
            .named("solve")
            .describedAs("Returns a root of f between a and b, where f(a) and f(b) differ in sign, by Brent's method")
            .withParams("f", "a", "b")
            .inCategory(CALCULUS)
            .takingTyped(ArgTypes.function(), ArgTypes.number(), ArgTypes.number())
            .implementedBy((f, a, b, ctx) -> {
                Function function = Numerics.realFunction(f, ctx);
                double lower = Math.min(a, b);
                double upper = Math.max(a, b);
                return new NodeDouble(Numerics.run(ctx, () -> Numerics.solver(function, lower, upper).solve()));
            });

    /**
     * Every root found in an interval
     */
    public static final MathFunction ROOTS = FunctionBuilder
            .named("roots")
            .describedAs("Returns the roots of f between a and b, in ascending order, found where f changes sign")
            .withParams("f", "a", "b")
            .inCategory(CALCULUS)
            .takingTyped(ArgTypes.function(), ArgTypes.number(), ArgTypes.number())
            .implementedBy((f, a, b, ctx) -> {
                Function function = Numerics.realFunction(f, ctx);
                double lower = Math.min(a, b);
                double upper = Math.max(a, b);
                List<Double> roots = Numerics.run(ctx, () -> Numerics.solver(function, lower, upper).solveAll());
                return new NodeVector(roots.stream().sorted().map(NodeDouble::new).toArray(Node[]::new));
            });

    /**
     * Gets all calculus functions.
     */
    public static List<MathFunction> all() {
        return List.of(INTEGRATE, DERIV, SOLVE, ROOTS);
    }
}
