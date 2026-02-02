package uk.co.ryanharrison.mathengine.parser.format;

import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

/**
 * Formats AST nodes into human-readable plain-text strings.
 * <p>
 * Produces unambiguous output where binary expressions are always parenthesised
 * to reflect the tree structure. Optionally rounds numeric values to a configured
 * number of decimal places.
 * </p>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Full precision (no rounding)
 * NodeFormatter fmt = StringNodeFormatter.fullPrecision();
 * String text = fmt.format(node);
 *
 * // Round numeric output to 4 decimal places
 * NodeFormatter rounded = StringNodeFormatter.withDecimalPlaces(4);
 * String text = rounded.format(node);
 * }</pre>
 *
 * <p>This formatter never delegates to {@code Node.toString()}; all formatting
 * logic is self-contained.</p>
 */
public final class StringNodeFormatter implements NodeFormatter {

    private final int decimalPlaces;

    private StringNodeFormatter(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    /**
     * Creates a formatter that preserves full numeric precision.
     *
     * @return a full-precision string formatter
     */
    public static StringNodeFormatter fullPrecision() {
        return new StringNodeFormatter(-1);
    }

    /**
     * Creates a formatter that rounds numeric values to the given number of
     * decimal places using {@link java.math.RoundingMode#HALF_UP}.
     * A negative value means full precision (equivalent to {@link #fullPrecision()}).
     *
     * @param decimalPlaces the number of decimal places, or negative for full precision
     * @return a string formatter
     */
    public static StringNodeFormatter withDecimalPlaces(int decimalPlaces) {
        return new StringNodeFormatter(decimalPlaces);
    }

    @Override
    public String format(Node node) {
        return switch (node) {
            // --- NodeNumber subtypes ---
            case NodeDouble n -> formatDouble(n.getValue());
            case NodeRational n -> formatRational(n);
            case NodePercent n -> formatDouble(n.getPercentValue()) + "%";
            case NodeBoolean n -> String.valueOf(n.getValue());

            // --- Other NodeConstant subtypes ---
            case NodeString n -> "\"" + n.getValue() + "\"";
            case NodeVector n -> formatVector(n);
            case NodeMatrix n -> formatMatrix(n);
            case NodeUnit n -> formatUnit(n);
            case NodeRange n -> formatRange(n);
            case NodeLambda n -> formatLambda(n);
            case NodeFunction n -> "<function:" + n.getFunction().name() + ">";

            // --- NodeExpression subtypes ---
            case NodeBinary n -> formatBinary(n);
            case NodeUnary n -> formatUnary(n);
            case NodeCall n -> formatCall(n);
            case NodeVariable n -> n.getName();
            case NodeAssignment n -> n.getIdentifier() + " := " + format(n.getValue());
            case NodeFunctionDef n -> formatFunctionDef(n);
            case NodeSubscript n -> FormatUtils.formatSubscript(n, this);
            case NodeRangeExpression n -> formatRangeExpr(n);
            case NodeUnitConversion n -> format(n.getValue()) + " in " + n.getTargetUnit();
            case NodeSequence n -> FormatUtils.formatSequence(n, this);
            case NodeComprehension n -> formatComprehension(n);
            case NodeUnitRef n -> "@" + n.getUnitName();
            case NodeVarRef n -> "$" + n.getVarName();
            case NodeConstRef n -> "#" + n.getConstName();
        };
    }

    // ==================== Numeric Helpers ====================

    private String formatDouble(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Infinity" : "-Infinity";
        }
        return FormatUtils.formatFiniteDouble(value, decimalPlaces);
    }

    private String formatRational(NodeRational node) {
        var rational = node.getValue();
        if (rational.isInteger()) {
            return rational.getNumerator().toString();
        }
        return rational.getNumerator() + "/" + rational.getDenominator();
    }

    // ==================== Constant Helpers ====================

    private String formatVector(NodeVector vector) {
        var sb = new StringBuilder("{");
        FormatUtils.appendJoined(sb, java.util.List.of(vector.getElements()), this);
        sb.append("}");
        return sb.toString();
    }

    private String formatMatrix(NodeMatrix matrix) {
        Node[][] elements = matrix.getElements();
        var sb = new StringBuilder("[");
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) sb.append("; ");
            for (int j = 0; j < elements[i].length; j++) {
                if (j > 0) sb.append(", ");
                sb.append(format(elements[i][j]));
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String formatUnit(NodeUnit unit) {
        String formattedValue = formatDouble(unit.getValue());
        String unitName = unit.getUnit().getDisplayName(unit.getValue());
        return formattedValue + " " + unitName;
    }

    private String formatRange(NodeRange range) {
        String start = format(range.getStart());
        String end = format(range.getEnd());
        if (range.getStep().doubleValue() == 1.0) {
            return start + ".." + end;
        }
        return start + ".." + end + " step " + format(range.getStep());
    }

    private String formatLambda(NodeLambda lambda) {
        var params = lambda.getParameters();
        String body = format(lambda.getBody());
        if (params.size() == 1) {
            return params.getFirst() + " -> " + body;
        }
        return "(" + String.join(", ", params) + ") -> " + body;
    }

    // ==================== Expression Helpers ====================

    private String formatBinary(NodeBinary binary) {
        String left = format(binary.getLeft());
        String right = format(binary.getRight());
        String op = binaryOperatorSymbol(binary.getOperator().type());
        return "(" + left + " " + op + " " + right + ")";
    }

    private String formatUnary(NodeUnary unary) {
        String operand = format(unary.getOperand());
        if (unary.isPrefix()) {
            String op = prefixOperatorSymbol(unary.getOperator().type());
            return op + operand;
        }
        String op = postfixOperatorSymbol(unary.getOperator().type());
        return operand + op;
    }

    private String formatCall(NodeCall call) {
        String func = format(call.getFunction());
        var sb = new StringBuilder(func);
        sb.append("(");
        FormatUtils.appendJoined(sb, call.getArguments(), this);
        sb.append(")");
        return sb.toString();
    }

    private String formatFunctionDef(NodeFunctionDef def) {
        return def.getName() + "(" + String.join(", ", def.getParameters())
                + ") := " + format(def.getBody());
    }

    private String formatRangeExpr(NodeRangeExpression range) {
        String start = format(range.getStart());
        String end = format(range.getEnd());
        if (range.hasStep()) {
            return start + ".." + end + " step " + format(range.getStep());
        }
        return start + ".." + end;
    }

    private String formatComprehension(NodeComprehension comp) {
        var sb = new StringBuilder("{");
        sb.append(format(comp.getExpression()));
        for (var iter : comp.getIterators()) {
            sb.append(" for ").append(iter.variable())
                    .append(" in ").append(format(iter.iterable()));
        }
        if (comp.hasCondition()) {
            sb.append(" if ").append(format(comp.getCondition()));
        }
        sb.append("}");
        return sb.toString();
    }

    // ==================== Operator Symbol Helpers ====================

    private static String binaryOperatorSymbol(TokenType type) {
        return switch (type) {
            case PLUS -> "+";
            case MINUS -> "-";
            case MULTIPLY -> "*";
            case DIVIDE -> "/";
            case POWER -> "^";
            case MOD -> "%";
            case EQ -> "==";
            case NEQ -> "!=";
            case LT -> "<";
            case GT -> ">";
            case LTE -> "<=";
            case GTE -> ">=";
            case AND -> "and";
            case OR -> "or";
            case XOR -> "xor";
            case OF -> "of";
            case AT -> "@";
            case ASSIGN -> ":=";
            case LAMBDA -> "->";
            case RANGE -> "..";
            default -> type.name().toLowerCase();
        };
    }

    private static String prefixOperatorSymbol(TokenType type) {
        return switch (type) {
            case MINUS -> "-";
            case PLUS -> "+";
            case NOT -> "not ";
            default -> type.name().toLowerCase() + " ";
        };
    }

    private static String postfixOperatorSymbol(TokenType type) {
        return switch (type) {
            case FACTORIAL -> "!";
            case DOUBLE_FACTORIAL -> "!!";
            case PERCENT -> "%";
            default -> type.name().toLowerCase();
        };
    }
}
