package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.List;

/**
 * Interface for built-in mathematical functions.
 * <p>
 * Functions differ from operators in that they:
 * <ul>
 *     <li>Are called by name (e.g., sin, cos, sqrt)</li>
 *     <li>Take a variable number of arguments</li>
 *     <li>May have complex validation rules</li>
 * </ul>
 *
 * <h2>Implementation Example:</h2>
 * <pre>{@code
 * public class SinFunction implements MathFunction {
 *
 *     @Override
 *     public String name() { return "sin"; }
 *
 *     @Override
 *     public int minArity() { return 1; }
 *
 *     @Override
 *     public int maxArity() { return 1; }
 *
 *     @Override
 *     public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
 *         double angle = ctx.toRadians(args.get(0).doubleValue());
 *         return new NodeDouble(Math.sin(angle));
 *     }
 * }
 * }</pre>
 *
 * @see FunctionContext
 * @see FunctionExecutor
 */
public interface MathFunction {

    /**
     * Gets the function name as it appears in expressions.
     *
     * @return the function name (e.g., "sin", "sqrt", "max")
     */
    String name();

    /**
     * Gets alternate names (aliases) for this function.
     * <p>
     * Aliases allow a function to be called by different names. For example,
     * the "log" function might have an alias "log10" for clarity.
     * <p>
     * By default, functions have no aliases. Override this method to provide aliases.
     *
     * @return unmodifiable list of aliases (empty by default)
     */
    default List<String> aliases() {
        return List.of();
    }

    /**
     * Gets a human-readable description of the function.
     * Used in documentation and error messages.
     *
     * @return description of what the function does
     */
    default String description() {
        return name() + " function";
    }

    /**
     * Gets the minimum number of arguments this function accepts.
     *
     * @return minimum arity (0 or greater)
     */
    int minArity();

    /**
     * Gets the maximum number of arguments this function accepts.
     * Return {@link Integer#MAX_VALUE} for variadic functions.
     *
     * @return maximum arity
     */
    int maxArity();

    /**
     * Applies this function to the given arguments.
     *
     * @param args the evaluated arguments
     * @param ctx  the function context for utilities
     * @return the result of the function
     */
    NodeConstant apply(List<NodeConstant> args, FunctionContext ctx);

    /**
     * Whether this function supports element-wise application to vectors.
     * When true, a single-argument function will be applied to each element
     * of a vector argument automatically.
     *
     * @return true if vector broadcasting is supported
     */
    default boolean supportsVectorBroadcasting() {
        return minArity() == 1 && maxArity() == 1;
    }

    /**
     * Gets the category of this function for documentation purposes.
     *
     * @return the function category
     */
    default Category category() {
        return Category.OTHER;
    }

    /**
     * Function categories for documentation and organization.
     */
    enum Category {
        /**
         * Trigonometric functions: sin, cos, tan, etc.
         */
        TRIGONOMETRIC,
        /**
         * Hyperbolic functions: sinh, cosh, tanh, etc.
         */
        HYPERBOLIC,
        /**
         * Exponential functions: exp, exp2, exp10, etc.
         */
        EXPONENTIAL,
        /**
         * Logarithmic functions: ln, log, log2, etc.
         */
        LOGARITHMIC,
        /**
         * Rounding functions: floor, ceil, round, trunc, etc.
         */
        ROUNDING,
        /**
         * Statistical functions: mean, median, variance, percentile, etc.
         */
        STATISTICAL,
        /**
         * Vector functions: sum, product, min, max, sort, etc.
         */
        VECTOR,
        /**
         * Matrix functions: det, trace, transpose, inverse, etc.
         */
        MATRIX,
        /**
         * Conditional functions: if, clamp, lerp, etc.
         */
        CONDITIONAL,
        /**
         * String manipulation functions: upper, lower, trim, substring, etc.
         */
        STRING,
        /**
         * Percentage functions: percent, percentOf, percentChange, etc.
         */
        PERCENTAGE,
        /**
         * Number theory functions: isprime, factors, gcd, lcm, etc.
         */
        NUMBER_THEORY,
        /**
         * Type checking and conversion: isnan, isinf, typeof, int, float, etc.
         */
        TYPE,
        /**
         * Bitwise operations: bitand, bitor, bitxor, lshift, etc.
         */
        BITWISE,
        /**
         * Utility functions: frac, map, distance, etc.
         */
        UTILITY,
        /**
         * Special mathematical functions: gamma, beta, erf, etc.
         */
        SPECIAL,
        /**
         * Uncategorized or miscellaneous functions
         */
        OTHER
    }
}
