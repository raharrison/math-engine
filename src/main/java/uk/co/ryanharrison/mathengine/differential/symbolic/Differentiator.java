package uk.co.ryanharrison.mathengine.differential.symbolic;

import uk.co.ryanharrison.mathengine.core.Function;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.symbolic.ExpressionItem;
import uk.co.ryanharrison.mathengine.parser.symbolic.TreeToStack;
import uk.co.ryanharrison.mathengine.utils.Utils;

import java.util.Deque;

public class Differentiator {

    static void main() {
        System.out.println(new Differentiator().differentiate(new Function("sin(x)"), true));
    }

    public Function differentiate(Function equation, boolean optimize) {
        Node node = equation.getCompiledExpression();
        Deque<ExpressionItem> stack = TreeToStack.treeToStack(node);
        String result = differentiateStack(stack);

        if (optimize)
            result = optimize(result.replace(" ", ""));

        return new Function(result);
    }

    /**
     * Differentiates a string expression by parsing it and differentiating the AST.
     * Used as a callback for recursive differentiation of sub-expressions.
     * Always optimizes the result to match original behavior.
     *
     * @param expression the expression string to differentiate
     * @return the derivative as a string expression (optimized)
     */
    private String differentiateExpression(String expression) {
        Node node = new Function(expression).getCompiledExpression();
        Deque<ExpressionItem> stack = TreeToStack.treeToStack(node);
        String result = differentiateStack(stack);
        // Always optimize recursive calls (matches original behavior)
        return optimize(result.replace(" ", ""));
    }

    /**
     * Differentiates a stack of expression items.
     * <p>
     * Uses postfix (reverse Polish) notation. The stack is processed recursively,
     * applying differentiation rules for operators and functions.
     * </p>
     *
     * @param stack the expression stack in postfix order
     * @return the derivative as a string expression
     */
    private String differentiateStack(Deque<ExpressionItem> stack) {
        ExpressionItem item = stack.pop();

        if (item.isOperator()) {
            // Binary operator: apply product rule, quotient rule, chain rule, etc.
            return differentiateBinaryOperator(item, stack);
        } else if (item.isFunction()) {
            // Function: apply chain rule
            String u = ExpressionItem.extractFunctionArgument(item.input);
            String result = DifferentiationRules.differentiate(item.function, u, this::differentiateExpression);
            return (item.sign == -1 ? "-" : "") + result;
        } else {
            // Variable or constant
            return differentiateVariable(item.input);
        }
    }

    /**
     * Applies differentiation rules for binary operators.
     *
     * @param item  the operator item
     * @param stack the remaining expression stack
     * @return the derivative expression
     */
    private String differentiateBinaryOperator(ExpressionItem item, Deque<ExpressionItem> stack) {
        // Get left operand and its derivative
        String u = stack.getFirst().input;
        String du = differentiateStack(stack);

        // Get right operand and its derivative
        String v = stack.getFirst().input;
        String dv = differentiateStack(stack);

        // Apply differentiation rule based on operator
        if (du.equals("0")) {
            // Left operand is constant
            return applyConstantLeftRule(item.operator, u, v, dv);
        } else if (dv.equals("0")) {
            // Right operand is constant
            return applyConstantRightRule(item.operator, u, v, du);
        } else {
            // Both operands are variable
            return applyGeneralRule(item.operator, u, v, du, dv);
        }
    }

    /**
     * Applies differentiation rule when left operand is constant.
     */
    private String applyConstantLeftRule(char op, String u, String v, String dv) {
        return switch (op) {
            case '+' -> dv;                                          // d(c+v) = dv
            case '-' -> "(-" + dv + ')';                             // d(c-v) = -dv
            case '*' -> u + '*' + dv;                                // d(c*v) = c*dv
            case '/' -> "(-" + u + '*' + dv + ")/(" + v + ")^2";     // d(c/v) = -c*dv/v²
            case '^' -> dv + "*" + u + "^" + v +                     // d(c^v) = dv*c^v*ln(c)
                    (u.equals("e") ? "" : "*ln(" + u + ")");
            default -> throw new UnsupportedOperationException("Unknown operator: " + op);
        };
    }

    /**
     * Applies differentiation rule when right operand is constant.
     */
    private String applyConstantRightRule(char op, String u, String v, String du) {
        return switch (op) {
            case '+', '-' -> du;                                     // d(u±c) = du
            case '*' -> du + '*' + v;                                // d(u*c) = du*c
            case '/' -> '(' + du + ")/" + v;                         // d(u/c) = du/c
            case '^' -> v + "*" + u + "^" + trim(Double.parseDouble(v) - 1) + "*" + du; // d(u^c) = c*u^(c-1)*du
            default -> throw new UnsupportedOperationException("Unknown operator: " + op);
        };
    }

    /**
     * Applies general differentiation rule when both operands are variable.
     */
    private String applyGeneralRule(char op, String u, String v, String du, String dv) {
        return switch (op) {
            case '+', '-' -> '(' + du + op + dv + ')';               // d(u±v) = du±dv
            case '*' -> '(' + u + '*' + dv + '+' + du + '*' + v + ')'; // d(u*v) = u*dv + du*v (product rule)
            case '/' -> '(' + du + '*' + v + '-' + u + '*' + dv + ")/(" + v + ")^2"; // d(u/v) = (du*v - u*dv)/v² (quotient rule)
            case '^' -> '(' + v + '*' + u + "^(" + v + "-1)*" + du + // d(u^v) = v*u^(v-1)*du + u^v*ln(u)*dv (general power rule)
                    '+' + u + '^' + v + "*ln(" + u + ")*" + dv + ')';
            default -> throw new UnsupportedOperationException("Unknown operator: " + op);
        };
    }

    /**
     * Differentiates a variable or constant with respect to x.
     */
    private static String differentiateVariable(String var) {
        // d/dx[x] = 1
        if (var.equals("x") || var.equals("+x")) {
            return "1";
        }
        // d/dx[-x] = -1
        else if (var.equals("-x")) {
            return "-1";
        }
        // d/dx[c] = 0 for constants
        else if (Utils.isNumeric(var) || var.equals("pi") || var.equals("e")) {
            return "0";
        }
        // d/dx[f(x)] where f is unknown
        else {
            return 'd' + var + "/dx";
        }
    }

    private static String optimize(String s) {
        int nLength = s.length();

        // Replace "-0" with "0" (handle negative zero from constant differentiation)
        if (s.equals("-0")) {
            return "0";
        }

        s = optimizeSign(s);
        StringBuilder str = new StringBuilder(s);

        int nIndex = -1;

        // replace "((....))" with "(....)"
        while ((nIndex = str.indexOf("((", nIndex + 1)) != -1) {
            int pClose = Utils.matchingCharacterIndex(str.toString(), nIndex + 1, '(', ')') + 1;
            if (str.charAt(pClose) == ')') {
                str = str.delete(pClose, pClose + 1);
                str = str.delete(nIndex, nIndex + 1);
            }
        }

        nIndex = -1;
        // remove any 1*
        while ((nIndex = str.indexOf("1*", nIndex + 1)) != -1) {
            if (nIndex == 0 || "+-*/(".indexOf(str.charAt(nIndex - 1)) != -1)
                str = str.delete(nIndex, nIndex + 2);

            if (nIndex + 1 > str.length())
                break;
        }

        nIndex = -1;
        // remove any *1
        while ((nIndex = str.indexOf("*1", nIndex + 1)) != -1) {
            if (nIndex + 2 == str.length() || "+-*/()".indexOf(str.charAt(nIndex + 2)) != -1)
                str = str.delete(nIndex, nIndex + 2);

            if (nIndex + 1 > str.length())
                break;
        }

        nIndex = -1;
        // remove any exponent equal 1
        while ((nIndex = str.indexOf("^1", nIndex + 1)) != -1) {
            if (nIndex + 2 == str.length() || "+-*/()".indexOf(str.charAt(nIndex + 2)) != -1)
                str = str.delete(nIndex, nIndex + 2);

            if (nIndex + 1 > str.length())
                break;
        }

        nIndex = 0;
        // remove unneeded parentheses
        while ((nIndex = str.indexOf("(", nIndex)) != -1) {
            // "nscthgp0" is the end characters of all supported functions
            if (nIndex > 0 && "nscthgp0".indexOf(str.charAt(nIndex - 1)) != -1) {
                nIndex++;
                continue;
            }
            // find the parenthesis close
            int pClose = Utils.matchingCharacterIndex(str.toString(), nIndex, '(', ')') + 1;
            if (pClose == nIndex)
                return str.toString();
            // get the index of the close char
            int nCloseIndex = pClose - 1;
            // check if the parentheses in the start and the end of the string
            if ((nIndex == 0 && nCloseIndex == str.length() - 1) || nCloseIndex == nIndex + 2 ||
                    // check if the string doesn't include any operator
                    Utils.isNumeric(str.substring(nIndex + 1, nCloseIndex))) {
                // delete the far index of ')'
                str = str.delete(nCloseIndex, nCloseIndex + 1);
                // delete the near index of '('
                str = str.delete(nIndex, nIndex + 1);
            } else
                nIndex++;
        }

        if (nLength != str.length())
            str = new StringBuilder(optimize(str.toString()));

        return str.toString();
    }

    private static String optimizeSign(String s) {
        int index = 0;

        StringBuilder str = new StringBuilder(s);
        // replace "--" with "" or "+"
        while ((index = str.indexOf("--", index)) != -1)
            if (index == 0
                    || Utils.indexOfAny(str.substring(index - 1), '(', '+', '-', '/',
                    '*', '^') != -1) {
                str = str.delete(index, index + 2);
            } else {
                str = str.delete(index, index + 1);
                str = str.insert(index, "+");
            }

        // replace "+-" with "-"
        return str.toString().replace("+-", "-");
    }

    private static String trim(double d) {
        String str = Double.toString(d);
        if (str.indexOf('.') != -1)
            str = Utils.stripEnd(str, "0");
        return Utils.stripEnd(str, ".");
    }
}
