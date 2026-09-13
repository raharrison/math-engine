package uk.co.ryanharrison.mathengine.parser.format;

import uk.co.ryanharrison.mathengine.parser.ast.Node;
import uk.co.ryanharrison.mathengine.parser.ast.NodeBinary;
import uk.co.ryanharrison.mathengine.parser.ast.NodeSequence;
import uk.co.ryanharrison.mathengine.parser.ast.NodeSubscript;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Shared formatting utilities used by both {@link StringNodeFormatter} and
 * {@link AsciiMathNodeFormatter}.
 */
final class FormatUtils {

    private FormatUtils() {
    }

    /**
     * Formats a finite (non-NaN, non-Infinite) double value with optional
     * decimal-place rounding.
     *
     * @param value         a finite double
     * @param decimalPlaces number of decimal places, or negative for full precision
     */
    static String formatFiniteDouble(double value, int decimalPlaces) {
        if (decimalPlaces < 0) {
            if (value == Math.floor(value) && Math.abs(value) < 1e15) {
                return String.valueOf((long) value);
            }
            return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        }

        if (decimalPlaces == 0) {
            return String.valueOf(Math.round(value));
        }

        try {
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
            return bd.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return String.valueOf(value);
        }
    }

    static String formatSubscript(NodeSubscript subscript, NodeFormatter fmt) {
        return target(subscript.getTarget(), fmt) +
                formatIndexGroups(List.of(subscript.getIndices()), fmt);
    }

    /**
     * A subscript binds tighter than multiplication, so {@code (2 m)[0]} keeps its parentheses.
     */
    private static String target(Node target, NodeFormatter fmt) {
        String text = fmt.format(target);
        if (target instanceof NodeBinary binary && binary.getOperator().implicit()) {
            return "(" + text + ")";
        }
        return text;
    }

    /**
     * The {@code [...]} groups of a subscript or an element assignment target.
     */
    static String formatIndexGroups(List<List<NodeSubscript.SliceArg>> groups, NodeFormatter fmt) {
        var sb = new StringBuilder();
        for (List<NodeSubscript.SliceArg> group : groups) {
            sb.append("[");
            for (int i = 0; i < group.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatSliceArg(group.get(i), fmt));
            }
            sb.append("]");
        }
        return sb.toString();
    }

    static String formatSliceArg(NodeSubscript.SliceArg arg, NodeFormatter fmt) {
        if (arg.isRange()) {
            String start = arg.getStart() != null ? fmt.format(arg.getStart()) : "";
            String end = arg.getEnd() != null ? fmt.format(arg.getEnd()) : "";
            return start + ":" + end;
        }
        return fmt.format(arg.getStart());
    }

    static String formatSequence(NodeSequence seq, NodeFormatter fmt) {
        var sb = new StringBuilder();
        var stmts = seq.getStatements();
        for (int i = 0; i < stmts.size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(fmt.format(stmts.get(i)));
        }
        return sb.toString();
    }

    static void appendJoined(StringBuilder sb, List<Node> nodes, NodeFormatter fmt) {
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(fmt.format(nodes.get(i)));
        }
    }

    static String joinFormatted(List<Node> nodes, NodeFormatter fmt) {
        var sb = new StringBuilder();
        appendJoined(sb, nodes, fmt);
        return sb.toString();
    }
}
