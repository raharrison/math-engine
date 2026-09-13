package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeFunction;
import uk.co.ryanharrison.mathengine.parser.ast.NodeMatrix;
import uk.co.ryanharrison.mathengine.parser.ast.NodeVector;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;

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

    private static final ArgType<Double> NUMBER = (node, ctx) -> ctx.toDouble(node);
    private static final ArgType<Integer> INTEGER = (node, ctx) -> ctx.requireInteger(node);
    private static final ArgType<Long> LONG = (node, ctx) -> ctx.requireLong(node);
    private static final ArgType<NodeVector> VECTOR = (node, ctx) -> ctx.requireVector(node);
    private static final ArgType<NodeMatrix> MATRIX = (node, ctx) -> ctx.requireMatrix(node);
    private static final ArgType<NodeConstant> ANY = (node, ctx) -> node;

    private static final ArgType<NodeFunction> FUNCTION = (node, ctx) -> {
        if (node instanceof NodeFunction function) {
            return function;
        }
        throw new TypeError("Function '" + ctx.functionName() + "' requires a function argument, got: " +
                node.typeName());
    };

    // ==================== Scalar Types ====================

    /**
     * Extracts a double value from any numeric node.
     * Handles NodeNumber, NodeBoolean (0/1), NodeUnit (strips unit), and NodePercent.
     */
    public static ArgType<Double> number() {
        return NUMBER;
    }

    /**
     * Extracts an integer value, validating there is no fractional part.
     *
     * @throws TypeError if the value is not an integer
     */
    public static ArgType<Integer> integer() {
        return INTEGER;
    }

    /**
     * Extracts a long integer value, validating there is no fractional part.
     *
     * @throws TypeError if the value is not an integer
     */
    public static ArgType<Long> longInt() {
        return LONG;
    }

    // ==================== Collection Types ====================

    /**
     * Extracts a NodeVector, requiring the node to be a vector.
     *
     * @throws TypeError if the value is not a vector
     */
    public static ArgType<NodeVector> vector() {
        return VECTOR;
    }

    /**
     * Extracts a NodeMatrix, requiring the node to be a matrix.
     *
     * @throws TypeError if the value is not a matrix
     */
    public static ArgType<NodeMatrix> matrix() {
        return MATRIX;
    }

    // ==================== Special Types ====================

    /**
     * Extracts a NodeFunction, requiring the node to be a function/lambda.
     *
     * @throws TypeError if the value is not a function
     */
    public static ArgType<NodeFunction> function() {
        return FUNCTION;
    }

    // ==================== Flexible Types ====================

    /**
     * Accepts any NodeConstant without conversion.
     * Use when the function needs to inspect or preserve the exact type.
     */
    public static ArgType<NodeConstant> any() {
        return ANY;
    }

}
