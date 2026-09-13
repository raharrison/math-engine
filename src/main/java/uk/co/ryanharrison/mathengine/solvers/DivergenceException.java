package uk.co.ryanharrison.mathengine.solvers;

/**
 * Exception thrown when an iterative root-finding algorithm diverges.
 * <p>
 * Divergence occurs when the algorithm produces increasingly worse approximations instead
 * of converging to a root. This is most common in polishing methods like Newton-Raphson
 * when:
 * </p>
 * <ul>
 *     <li>The initial guess is far from the actual root</li>
 *     <li>The derivative is zero or very small at the current estimate</li>
 *     <li>The function has a local minimum/maximum near the root</li>
 *     <li>Numerical overflow occurs during iteration</li>
 * </ul>
 *
 * <h2>Example:</h2>
 * <pre>{@code
 * Function f = new Function("1/x"); // Has no roots
 * NewtonRaphsonSolver solver = NewtonRaphsonSolver.builder()
 *     .targetFunction(f)
 *     .initialGuess(1.0)
 *     .build();
 *
 * // Throws DivergenceException as the algorithm produces infinite values
 * solver.solve();
 * }</pre>
 *
 * @see SolverException
 */
public class DivergenceException extends SolverException {

    /**
     * Constructs a new divergence exception with detailed context.
     *
     * @param message         the detail message explaining the divergence
     * @param iteration       the iteration number at which divergence was detected
     * @param lastFiniteValue the last finite value before divergence (may be NaN or Infinity)
     */
    public DivergenceException(String message, int iteration, double lastFiniteValue) {
        super(String.format("%s (iteration: %d, last finite value: %.10g)",
                message, iteration, lastFiniteValue));
    }

}
