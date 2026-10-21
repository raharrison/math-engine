package uk.co.ryanharrison.mathengine.parser.function.math;

import uk.co.ryanharrison.mathengine.core.Function;
import uk.co.ryanharrison.mathengine.parser.ast.NodeDouble;
import uk.co.ryanharrison.mathengine.parser.ast.NodeFunction;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.solvers.BrentSolver;
import uk.co.ryanharrison.mathengine.solvers.SolverException;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

/**
 * The bridge from the built-in functions to the library's numerical methods: a function
 * value made callable as a {@link Function}, and the methods' own failures reported as
 * errors of the built-in that called them.
 */
final class Numerics {

    /**
     * How close a root must be pinned down. Far tighter than the solvers' default, since
     * the answer is shown to the user rather than fed into a further approximation.
     */
    static final double ROOT_TOLERANCE = 1e-12;

    /**
     * Iterations for one root, and the number of slices an interval is scanned in for
     * sign changes when looking for every root.
     */
    static final int ROOT_ITERATIONS = 1000;

    private Numerics() {
    }

    /**
     * A function value as a function of one real variable.
     *
     * @throws TypeError if the function does not take exactly one argument
     */
    static Function realFunction(NodeFunction function, FunctionContext ctx) {
        int arity = function.getFunction().getArity();
        if (arity != 1) {
            throw new TypeError(ctx.functionName() + ": function must accept exactly 1 parameter, got " + arity);
        }
        return realFunction(x -> ctx.toDouble(ctx.callFunction(function, List.of(new NodeDouble(x)))),
                function.toString());
    }

    static Function realFunction(DoubleUnaryOperator evaluator, String description) {
        return Function.of(evaluator, description);
    }

    /**
     * Brent's method on {@code [lower, upper]}, at the tolerance above.
     */
    static BrentSolver solver(Function function, double lower, double upper) {
        return BrentSolver.builder()
                .targetFunction(function)
                .lowerBound(lower)
                .upperBound(upper)
                .tolerance(ROOT_TOLERANCE)
                .iterations(ROOT_ITERATIONS)
                .build();
    }

    /**
     * Runs a numerical method, reporting its complaint as this function's domain error:
     * bounds that bracket no root, a method that does not converge, or a non-finite value
     * such as a pole inside an integral.
     * Under silent validation the answer is NaN instead.
     */
    static double run(FunctionContext ctx, DoubleSupplier method) {
        try {
            return method.getAsDouble();
        } catch (SolverException | IllegalArgumentException | ArithmeticException e) {
            if (ctx.isSilentValidation()) {
                return Double.NaN;
            }
            throw ctx.error(e.getMessage());
        }
    }

    /**
     * As {@link #run(FunctionContext, DoubleSupplier)}, for a method with a richer answer.
     */
    static <T> T run(FunctionContext ctx, Supplier<T> method) {
        try {
            return method.get();
        } catch (SolverException | IllegalArgumentException | ArithmeticException e) {
            throw ctx.error(e.getMessage());
        }
    }
}
