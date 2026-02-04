package uk.co.ryanharrison.mathengine.parser.format;

import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.registry.SymbolRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Formats AST nodes into valid <a href="http://asciimath.org/">AsciiMath</a> syntax.
 * <p>
 * AsciiMath is a lightweight markup language for mathematical expressions that
 * can be rendered by libraries such as MathJax. This formatter translates the
 * full AST node hierarchy into AsciiMath, handling:
 * </p>
 * <ul>
 *     <li>Standard mathematical operators with correct precedence-based parenthesisation</li>
 *     <li>Built-in AsciiMath functions ({@code sin}, {@code cos}, {@code sqrt}, etc.)
 *         and automatic name mapping ({@code asin} &rarr; {@code arcsin})</li>
 *     <li>Fractions via {@code /}, exponents via {@code ^}, nth roots via
 *         {@code root(n)(x)}</li>
 *     <li>Matrices ({@code [[a,b],[c,d]]}), vectors, ranges, comprehensions</li>
 *     <li>Custom (non-AsciiMath) functions rendered with quoted text notation</li>
 *     <li>Special values: {@code oo} for infinity, {@code pi}, {@code phi}, etc.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * NodeFormatter fmt = AsciiMathNodeFormatter.create();
 * String asciiMath = fmt.format(node);
 *
 * // With rounding
 * NodeFormatter rounded = AsciiMathNodeFormatter.withDecimalPlaces(4);
 * String asciiMath = rounded.format(node);
 * }</pre>
 *
 * <p>This formatter never delegates to {@code Node.toString()}; all formatting
 * logic is self-contained.</p>
 */
public final class AsciiMathNodeFormatter implements NodeFormatter {

    /**
     * Functions recognised natively by AsciiMath renderers.
     * These are emitted verbatim and rendered in upright (roman) font.
     */
    private static final Set<String> ASCIIMATH_FUNCTIONS = Set.of(
            "sin", "cos", "tan", "sec", "csc", "cot",
            "arcsin", "arccos", "arctan",
            "sinh", "cosh", "tanh", "sech", "csch", "coth",
            "exp", "log", "ln",
            "det", "dim", "mod", "gcd", "lcm",
            "abs", "norm", "floor", "ceil",
            "min", "max", "sqrt"
    );

    /**
     * Maps engine function names to their AsciiMath equivalents where they differ.
     */
    private static final Map<String, String> FUNCTION_NAME_MAP = Map.of(
            "asin", "arcsin",
            "acos", "arccos",
            "atan", "arctan"
    );

    private final int decimalPlaces;
    private final SymbolRegistry registry = SymbolRegistry.getDefault();

    private AsciiMathNodeFormatter(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    /**
     * Creates a new AsciiMath formatter with full numeric precision.
     *
     * @return an AsciiMath formatter instance
     */
    public static AsciiMathNodeFormatter create() {
        return new AsciiMathNodeFormatter(-1);
    }

    /**
     * Creates a formatter that rounds numeric values to the given number of
     * decimal places using {@link java.math.RoundingMode#HALF_UP}.
     * A negative value means full precision (equivalent to {@link #create()}).
     *
     * @param decimalPlaces the number of decimal places, or negative for full precision
     * @return an AsciiMath formatter
     */
    public static AsciiMathNodeFormatter withDecimalPlaces(int decimalPlaces) {
        return new AsciiMathNodeFormatter(decimalPlaces);
    }

    @Override
    public String format(Node node) {
        return switch (node) {
            // --- NodeNumber subtypes ---
            case NodeDouble n -> formatDouble(n.getValue());
            case NodeRational n -> formatRational(n);
            case NodePercent n -> formatDouble(n.getPercentValue()) + "%";
            case NodeBoolean n -> "\"" + n.getValue() + "\"";

            // --- Other NodeConstant subtypes ---
            case NodeString n -> "\"" + n.getValue() + "\"";
            case NodeVector n -> formatVector(n);
            case NodeMatrix n -> formatMatrix(n);
            case NodeUnit n -> formatUnit(n);
            case NodeRange n -> formatRange(n);
            case NodeLambda n -> formatLambda(n);
            case NodeFunction n -> "\"" + n.getFunction().name() + "\"";

            // --- NodeExpression subtypes ---
            case NodeBinary n -> formatBinary(n);
            case NodeUnary n -> formatUnary(n);
            case NodeCall n -> formatCall(n);
            case NodeVariable n -> mapVariable(n.getName());
            case NodeAssignment n -> n.getIdentifier() + " = " + format(n.getValue());
            case NodeFunctionDef n -> formatFunctionDef(n);
            case NodeSubscript n -> FormatUtils.formatSubscript(n, this);
            case NodeRangeExpression n -> formatRangeExpr(n);
            case NodeUnitConversion n -> format(n.getValue()) + " \"in\" \"" + n.getTargetUnit() + "\"";
            case NodeSequence n -> FormatUtils.formatSequence(n, this);
            case NodeComprehension n -> formatComprehension(n);
            case NodeUnitRef n -> "\"" + n.getUnitName() + "\"";
            case NodeVarRef n -> n.getVarName();
            case NodeConstRef n -> formatConstRef(n);
        };
    }

    // ==================== Numeric Helpers ====================

    private String formatDouble(double value) {
        if (Double.isNaN(value)) {
            return "\"NaN\"";
        }
        if (value == Double.POSITIVE_INFINITY) {
            return "oo";
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return "-oo";
        }
        return FormatUtils.formatFiniteDouble(value, decimalPlaces);
    }

    private String formatRational(NodeRational node) {
        var rational = node.getValue();
        if (rational.isInteger()) {
            return rational.getNumerator().toString();
        }
        // AsciiMath fraction notation: (num)/(den) renders as proper fraction
        return "(" + rational.getNumerator() + ")/(" + rational.getDenominator() + ")";
    }

    // ==================== Constant Helpers ====================

    private String formatVector(NodeVector vector) {
        var sb = new StringBuilder("(");
        FormatUtils.appendJoined(sb, java.util.List.of(vector.getElements()), this);
        sb.append(")");
        return sb.toString();
    }

    private String formatMatrix(NodeMatrix matrix) {
        Node[][] elements = matrix.getElements();
        var sb = new StringBuilder("[");
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) sb.append("], [");
            else sb.append("[");
            for (int j = 0; j < elements[i].length; j++) {
                if (j > 0) sb.append(", ");
                sb.append(format(elements[i][j]));
            }
        }
        sb.append("]]");
        return sb.toString();
    }

    private String formatUnit(NodeUnit unit) {
        String formattedValue = formatDouble(unit.getValue());
        String unitName = unit.getUnit().getDisplayName(unit.getValue());
        return formattedValue + " \"" + unitName + "\"";
    }

    private String formatRange(NodeRange range) {
        String start = format(range.getStart());
        String end = format(range.getEnd());
        if (range.getStep().doubleValue() == 1.0) {
            return start + ".." + end;
        }
        return start + ".." + end + " \"step\" " + format(range.getStep());
    }

    private String formatLambda(NodeLambda lambda) {
        var params = lambda.getParameters();
        String body = format(lambda.getBody());
        if (params.size() == 1) {
            return params.getFirst() + " |-> " + body;
        }
        return "(" + String.join(", ", params) + ") |-> " + body;
    }

    // ==================== Expression Helpers ====================

    private String formatBinary(NodeBinary binary) {
        TokenType opType = binary.getOperator().type();

        // Division: AsciiMath renders / as a fraction, so always wrap binary
        // children in parens to ensure clean numerator/denominator grouping.
        if (opType == TokenType.DIVIDE) {
            String left = binary.getLeft() instanceof NodeBinary
                    ? "(" + format(binary.getLeft()) + ")"
                    : format(binary.getLeft());
            String right = binary.getRight() instanceof NodeBinary
                    ? "(" + format(binary.getRight()) + ")"
                    : format(binary.getRight());
            return left + "/" + right;
        }

        int prec = precedence(opType);
        boolean rightAssoc = opType == TokenType.POWER;

        String left = formatBinaryChild(binary.getLeft(), prec, false, rightAssoc);
        String right = formatBinaryChild(binary.getRight(), prec, true, rightAssoc);

        String op = asciiMathBinaryOp(opType);

        if (opType == TokenType.POWER) {
            return left + "^" + right;
        }
        return left + " " + op + " " + right;
    }

    /**
     * Wraps a child node of a binary expression in parentheses when required by
     * operator precedence.
     */
    private String formatBinaryChild(Node child, int parentPrec, boolean isRight,
                                     boolean parentIsRightAssoc) {
        String formatted = format(child);

        if (child instanceof NodeBinary childBinary) {
            int childPrec = precedence(childBinary.getOperator().type());
            boolean needsParens;
            if (isRight) {
                needsParens = childPrec < parentPrec
                        || (childPrec == parentPrec && !parentIsRightAssoc);
            } else {
                needsParens = childPrec < parentPrec
                        || (childPrec == parentPrec && parentIsRightAssoc);
            }
            if (needsParens) {
                return "(" + formatted + ")";
            }
        }

        if (isRight && child instanceof NodeUnary unary
                && unary.isPrefix() && unary.getOperator().type() == TokenType.MINUS) {
            return "(" + formatted + ")";
        }

        return formatted;
    }

    private String formatUnary(NodeUnary unary) {
        String operand = format(unary.getOperand());
        boolean operandIsCompound = unary.getOperand() instanceof NodeBinary;

        if (unary.isPrefix()) {
            String op = switch (unary.getOperator().type()) {
                case MINUS -> "-";
                case PLUS -> "+";
                case NOT -> "not ";
                default -> unary.getOperator().lexeme() + " ";
            };
            if (operandIsCompound) {
                return op + "(" + operand + ")";
            }
            return op + operand;
        }

        String op = switch (unary.getOperator().type()) {
            case FACTORIAL -> "!";
            case DOUBLE_FACTORIAL -> "!!";
            case PERCENT -> "%";
            default -> unary.getOperator().lexeme();
        };
        if (operandIsCompound) {
            return "(" + operand + ")" + op;
        }
        return operand + op;
    }

    private String formatCall(NodeCall call) {
        List<Node> args = call.getArguments();

        if (call.getFunction() instanceof NodeVariable funcVar) {
            String name = funcVar.getName();
            String mapped = FUNCTION_NAME_MAP.getOrDefault(name, name);

            // Special: nroot(n, x) -> root(n)(x)
            if (name.equals("nroot") && args.size() == 2) {
                return "root(" + format(args.get(0)) + ")(" + format(args.get(1)) + ")";
            }

            String formattedArgs = FormatUtils.joinFormatted(args, this);

            if (ASCIIMATH_FUNCTIONS.contains(mapped)) {
                return mapped + "(" + formattedArgs + ")";
            }

            // Single-letter names render correctly as italic variable + parens.
            // Multi-letter names must be quoted so AsciiMath doesn't split them
            // into separate italic characters (e.g. cbrt -> c*b*r*t).
            if (mapped.length() <= 1) {
                return mapped + "(" + formattedArgs + ")";
            }
            return "\"" + mapped + "\"(" + formattedArgs + ")";
        }

        // Lambda or other callable expression
        return "(" + format(call.getFunction()) + ")(" + FormatUtils.joinFormatted(args, this) + ")";
    }

    private String formatFunctionDef(NodeFunctionDef def) {
        return def.getName() + "(" + String.join(", ", def.getParameters())
                + ") = " + format(def.getBody());
    }

    private String formatRangeExpr(NodeRangeExpression range) {
        String start = format(range.getStart());
        String end = format(range.getEnd());
        if (range.hasStep()) {
            return start + ".." + end + " \"step\" " + format(range.getStep());
        }
        return start + ".." + end;
    }

    private String formatComprehension(NodeComprehension comp) {
        var sb = new StringBuilder("{");
        sb.append(format(comp.getExpression()));
        for (var iter : comp.getIterators()) {
            sb.append(" | ").append(iter.variable())
                    .append(" in ").append(format(iter.iterable()));
        }
        if (comp.hasCondition()) {
            sb.append(", ").append(format(comp.getCondition()));
        }
        sb.append("}");
        return sb.toString();
    }

    // ==================== Name / Symbol Helpers ====================

    private static String mapVariable(String name) {
        return switch (name) {
            case "infinity", "inf" -> "oo";
            default -> name;
        };
    }

    private String formatConstRef(NodeConstRef ref) {
        return switch (ref.getConstName()) {
            case "pi" -> "pi";
            case "e", "euler" -> "e";
            case "phi", "goldenratio" -> "phi";
            case "tau" -> "tau";
            case "infinity", "inf" -> "oo";
            case "nan" -> "\"NaN\"";
            case "true" -> "\"true\"";
            case "false" -> "\"false\"";
            default -> "\"" + ref.getConstName() + "\"";
        };
    }

    private String asciiMathBinaryOp(TokenType type) {
        return registry.getAsciiMathFormat(type);
    }

    private int precedence(TokenType type) {
        return registry.getPrecedence(type);
    }
}
