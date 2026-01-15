package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeExpression;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVisitor;
import uk.co.ryanharrison.mathengine.utils.Utils;

/**
 * Visitor for formatting nodes into human-readable strings.
 * <p>
 * This visitor provides clean, formatted output suitable for display in GUIs
 * and REPLs. Constants are formatted using {@link ResultFormatter} to respect
 * decimal place configuration, while expressions are formatted with cleaned-up
 * parentheses.
 * </p>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Static convenience method
 * String formatted = NodeFormatter.format(node, config);
 *
 * // Or use visitor directly
 * NodeFormatter formatter = new NodeFormatter(config);
 * String formatted = node.accept(formatter);
 * }</pre>
 */
public final class NodeFormatter implements NodeVisitor<String> {

    private final MathEngineConfig config;

    public NodeFormatter(MathEngineConfig config) {
        this.config = config;
    }

    /**
     * Convenience method to format a node with the given configuration.
     *
     * @param node   the node to format
     * @param config the configuration (for decimal places, etc.)
     * @return formatted string representation
     */
    public static String format(Node node, MathEngineConfig config) {
        NodeFormatter formatter = new NodeFormatter(config);
        String result = node.accept(formatter);
        // Remove outer parentheses from the final result
        return Utils.removeOuterParenthesis(result);
    }

    @Override
    public String visitConstant(NodeConstant node) {
        // Use ResultFormatter for constants (respects decimal places)
        return ResultFormatter.format(node, config);
    }

    @Override
    public String visitExpression(NodeExpression node) {
        // For expressions, use default toString
        return node.toString();
    }
}
