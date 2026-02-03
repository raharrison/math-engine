package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

/**
 * Describes how to extract a typed value from a {@link NodeConstant}.
 * <p>
 * Used with {@link FunctionBuilder#takingTyped} for type-safe function parameter extraction
 * without reflection. Implementations convert or validate node types and return the
 * extracted value, throwing {@link TypeError} if extraction fails.
 * <p>
 * The function name is available via {@link FunctionContext#functionName()} for error messages.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Use predefined extractors from ArgTypes
 * FunctionBuilder.named("take")
 *     .takingTyped(ArgTypes.vector(), ArgTypes.integer())
 *     .implementedBy((vector, n, ctx) -> { ... });
 *
 * // Custom extractor
 * ArgType<double[]> sorted = (node, ctx) -> {
 *     double[] arr = ctx.toDoubleArray(ctx.requireVector(node));
 *     Arrays.sort(arr);
 *     return arr;
 * };
 * }</pre>
 *
 * @param <T> the type to extract
 * @see ArgTypes
 * @see FunctionBuilder
 */
@FunctionalInterface
public interface ArgType<T> {

    /**
     * Extracts a value of type {@code T} from the given node.
     *
     * @param node the node to extract from
     * @param ctx  the function context (provides conversion utilities and function name)
     * @return the extracted value
     * @throws TypeError if extraction fails
     */
    T extract(NodeConstant node, FunctionContext ctx);
}
