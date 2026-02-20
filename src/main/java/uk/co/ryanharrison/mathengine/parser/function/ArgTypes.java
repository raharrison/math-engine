package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

/**
 * Factory for common {@link ArgType} extractors.
 * <p>
 * Provides predefined type extractors for use with {@link FunctionBuilder#takingTyped}.
 * Each extractor handles type coercion and validation, providing clear error messages
 * when extraction fails. The function name is obtained from the {@link FunctionContext}.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Extract a vector and an integer
 * FunctionBuilder.named("take")
 *     .takingTyped(ArgTypes.vector(), ArgTypes.integer())
 *     .implementedBy((vector, n, ctx) -> { ... });
 *
 * // Extract a matrix
 * FunctionBuilder.named("det")
 *     .takingTyped(ArgTypes.matrix())
 *     .implementedBy((matrix, ctx) -> { ... });
 *
 * // Accept any type without conversion
 * FunctionBuilder.named("typeof")
 *     .takingTyped(ArgTypes.any())
 *     .implementedBy((arg, ctx) -> { ... });
 * }</pre>
 *
 * @see ArgType
 * @see FunctionBuilder
 */
public final class ArgTypes {

    private ArgTypes() {
    }

    // ==================== Scalar Types ====================

    /**
     * Extracts a double value from any numeric node.
     * Handles NodeNumber, NodeBoolean (0/1), NodeUnit (strips unit), and NodePercent.
     */
    public static ArgType<Double> number() {
        return (node, ctx) -> ctx.toNumber(node).doubleValue();
    }

    /**
     * Extracts an integer value, validating there is no fractional part.
     *
     * @throws TypeError if the value is not an integer
     */
    public static ArgType<Integer> integer() {
        return (node, ctx) -> ctx.requireInteger(node);
    }

    /**
     * Extracts a long integer value, validating there is no fractional part.
     *
     * @throws TypeError if the value is not an integer
     */
    public static ArgType<Long> longInt() {
        return (node, ctx) -> ctx.requireLong(node);
    }

    /**
     * Extracts a boolean value. Numbers are truthy if non-zero.
     */
    public static ArgType<Boolean> bool() {
        return (node, ctx) -> ctx.toBoolean(node);
    }

    /**
     * Extracts a string value, requiring the node to be a NodeString.
     *
     * @throws TypeError if the value is not a string
     */
    public static ArgType<String> string() {
        return (node, ctx) -> ctx.requireString(node).getValue();
    }

    // ==================== Collection Types ====================

    /**
     * Extracts a NodeVector, requiring the node to be a vector.
     *
     * @throws TypeError if the value is not a vector
     */
    public static ArgType<NodeVector> vector() {
        return (node, ctx) -> ctx.requireVector(node);
    }

    /**
     * Extracts a NodeMatrix, requiring the node to be a matrix.
     *
     * @throws TypeError if the value is not a matrix
     */
    public static ArgType<NodeMatrix> matrix() {
        return (node, ctx) -> ctx.requireMatrix(node);
    }

    /**
     * Extracts a double array from a vector.
     *
     * @throws TypeError if the value is not a vector
     */
    public static ArgType<double[]> doubleArray() {
        return (node, ctx) -> {
            NodeVector vec = ctx.requireVector(node);
            return ctx.toDoubleArray(vec);
        };
    }

    // ==================== Special Types ====================

    /**
     * Extracts a NodeFunction, requiring the node to be a function/lambda.
     *
     * @throws TypeError if the value is not a function
     */
    public static ArgType<NodeFunction> function() {
        return (node, ctx) -> {
            if (node instanceof NodeFunction func) return func;
            throw new TypeError("Function '" + ctx.functionName() + "' requires a function argument, got: " +
                    node.typeName());
        };
    }

    // ==================== Flexible Types ====================

    /**
     * Accepts any NodeConstant without conversion.
     * Use when the function needs to inspect or preserve the exact type.
     */
    public static ArgType<NodeConstant> any() {
        return (node, ctx) -> node;
    }

    /**
     * Accepts a vector OR wraps a scalar in a single-element vector.
     */
    public static ArgType<NodeVector> vectorOrScalar() {
        return (node, ctx) -> {
            if (node instanceof NodeVector vec) return vec;
            return new NodeVector(new Node[]{node});
        };
    }
}
