package uk.co.ryanharrison.mathengine.parser.format;

import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;

/**
 * Formats AST {@link Node} instances into string representations.
 * <p>
 * Implementations handle both unevaluated expressions ({@code NodeExpression} subtypes)
 * and evaluated constants ({@code NodeConstant} subtypes), making them suitable for
 * formatting both the input and output of a parser request.
 * </p>
 * <p>
 * Formatters do not rely on any {@code Node.toString()} method; all formatting
 * logic is self-contained within the implementation.
 * </p>
 *
 * <h2>Available Implementations:</h2>
 * <ul>
 *     <li>{@link StringNodeFormatter} - human-readable plain-text output with optional
 *         decimal-place rounding for numeric values</li>
 *     <li>{@link AsciiMathNodeFormatter} - valid AsciiMath syntax suitable for rendering
 *         with MathJax or similar libraries</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * NodeFormatter fmt = StringFormatter.fullPrecision();
 * String text = fmt.format(someNode);
 *
 * NodeFormatter ascii = AsciiMathFormatter.create();
 * String math = ascii.format(someNode);
 * }</pre>
 */
public interface NodeFormatter {

    /**
     * Formats the given node into a string representation.
     *
     * @param node the AST node to format (expression or constant)
     * @return the formatted string, never {@code null}
     */
    String format(Node node);
}
