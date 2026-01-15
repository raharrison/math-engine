package uk.co.ryanharrison.mathengine.integral.symbolic;

import uk.co.ryanharrison.mathengine.core.Function;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.symbolic.ExpressionItem;
import uk.co.ryanharrison.mathengine.parser.symbolic.TreeToStack;
import uk.co.ryanharrison.mathengine.utils.Utils;

import java.util.Deque;

/**
 * Symbolic integration engine for mathematical expressions.
 * <p>
 * Performs symbolic integration using a rule-based approach similar to the symbolic
 * differentiator. Converts expressions to postfix notation, applies integration rules,
 * and returns the antiderivative as a string expression.
 *
 * <h2>Architecture:</h2>
 * <pre>
 * Expression String → AST → Postfix Stack → Integration Rules → Result + C
 * </pre>
 *
 * <h2>Supported Operations:</h2>
 * <ul>
 *     <li>Power rule: ∫x^n dx = x^(n+1)/(n+1) + C (n ≠ -1)</li>
 *     <li>Special case: ∫1/x dx = ln(|x|) + C</li>
 *     <li>Exponential: ∫e^x dx = e^x + C</li>
 *     <li>Trigonometric: sin, cos, tan, sec, cosec, cot</li>
 *     <li>Inverse trig: asin, acos, atan</li>
 *     <li>Hyperbolic: sinh, cosh, tanh</li>
 *     <li>Logarithmic: ∫ln(x) dx = x*ln(x) - x + C</li>
 *     <li>Sum/difference: ∫(u ± v) dx = ∫u dx ± ∫v dx</li>
 *     <li>Constant multiple: ∫c*u dx = c*∫u dx</li>
 *     <li>Integration by parts: Simple polynomial × transcendental (e.g., x·sin(x), x²·exp(x))</li>
 *     <li>Logarithmic derivative: ∫f'(x)/f(x) dx = ln|f(x)| + C</li>
 * </ul>
 *
 * <h2>Limitations:</h2>
 * <ul>
 *     <li>Integration by parts: Only simple polynomial × transcendental patterns</li>
 *     <li>Quotient rule: Only ∫f'(x)/f(x) pattern implemented</li>
 *     <li>Substitution: Not implemented (would require pattern matching)</li>
 *     <li>Partial fractions: Not implemented</li>
 *     <li>Returns unsimplified expressions (algebraic simplification not implemented)</li>
 *     <li>Some integrals have no elementary closed form</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Integrator integrator = new Integrator();
 * Function f = new Function("x^2 + 3*x");
 * String antiderivative = integrator.integrate(f);
 * // Result: "x^3/3+3*x^2/2+C"
 * }</pre>
 *
 * @see uk.co.ryanharrison.mathengine.differential.symbolic.Differentiator
 */
public final class Integrator {

    private final String variable;
    private final uk.co.ryanharrison.mathengine.differential.symbolic.Differentiator differentiator;

    /**
     * Creates an integrator for the variable 'x'.
     */
    public Integrator() {
        this("x");
    }

    /**
     * Creates an integrator for the specified variable.
     *
     * @param variable the integration variable (e.g., "x", "t")
     */
    public Integrator(String variable) {
        if (variable == null || variable.isEmpty()) {
            throw new IllegalArgumentException("Variable cannot be null or empty");
        }
        this.variable = variable;
        this.differentiator = new uk.co.ryanharrison.mathengine.differential.symbolic.Differentiator();
    }

    /**
     * Integrates a function symbolically.
     *
     * @param equation the function to integrate
     * @return the antiderivative as a string expression with constant of integration "+C"
     */
    public String integrate(Function equation) {
        return integrate(equation, true);
    }

    /**
     * Integrates a function symbolically with optional optimization.
     *
     * @param equation the function to integrate
     * @param optimize whether to apply post-processing optimization
     * @return the antiderivative as a string expression
     */
    public String integrate(Function equation, boolean optimize) {
        Node tree = equation.getCompiledExpression();
        Deque<ExpressionItem> stack = TreeToStack.treeToStack(tree);

        String result = integrateStack(stack);

        if (optimize) {
            result = optimize(result);
        }

        // Add constant of integration if not already present
        if (!result.endsWith("+C") && !result.endsWith("+ C")) {
            result = result + "+C";
        }

        return result;
    }

    /**
     * Integrates an expression string directly.
     *
     * @param expression the expression to integrate
     * @return the antiderivative
     */
    public String integrate(String expression) {
        Function f = new Function(expression, variable);
        return integrate(f);
    }

    /**
     * Core integration algorithm - processes postfix stack.
     *
     * @param stack the expression in postfix notation
     * @return the integral as a string expression
     */
    private String integrateStack(Deque<ExpressionItem> stack) {
        ExpressionItem item = stack.pop();

        // Binary operator case
        if (item.isOperator()) {
            return integrateBinaryOperator(item, stack);
        }

        // Function case
        if (item.isFunction()) {
            String u = ExpressionItem.extractFunctionArgument(item.input);
            String result = IntegrationRules.integrate(item.function, u);
            return (item.sign == -1 ? "-" : "") + result;
        }

        // Variable or constant case
        return integrateVariable(item.input);
    }

    /**
     * Integrates a variable or constant with respect to the integration variable.
     */
    private String integrateVariable(String var) {
        // ∫x dx = x²/2
        if (var.equals(variable) || var.equals("+" + variable)) {
            return "(" + variable + "^2)/2";
        }
        // ∫-x dx = -x²/2
        else if (var.equals("-" + variable)) {
            return "-((" + variable + "^2)/2)";
        }
        // ∫c dx = c*x for constants
        else if (!var.contains(variable)) {
            return var + "*" + variable;
        }
        // ∫f(x) dx where f is unknown
        else {
            return "integral(" + var + "," + variable + ")";
        }
    }

    /**
     * Integrates a binary operator expression.
     * <p>
     * Key insight: We need to get the ORIGINAL operands (not their integrals),
     * check which are constants, then apply the appropriate integration rule.
     * </p>
     */
    private String integrateBinaryOperator(ExpressionItem item, Deque<ExpressionItem> stack) {
        // Get operands - use getFirst to peek at the string, then integrateStack to process
        // This matches what the differentiator does
        String u = stack.getFirst().input;
        String intU = integrateStack(stack);  // This recursively processes and removes left operand
        boolean uIsConstant = isConstant(u);

        String v = stack.getFirst().input;
        String intV = integrateStack(stack);  // This recursively processes and removes right operand
        boolean vIsConstant = isConstant(v);

        char operator = item.operator;

        // Apply integration rules based on which operands are constant
        if (uIsConstant && vIsConstant) {
            // Both constants: ∫(c1 op c2) dx = (c1 op c2)*x
            String constant = "(" + u + operator + v + ")";
            return constant + "*" + variable;
        } else if (uIsConstant && !vIsConstant) {
            // Left is constant: ∫c op v dx
            return applyConstantLeftIntegration(u, operator, intV);
        } else if (!uIsConstant && vIsConstant) {
            // Right is constant: ∫u op c dx
            return applyConstantRightIntegration(u, v, operator, intU);
        } else {
            // Both contain variable: ∫u op v dx
            return applyGeneralIntegration(operator, u, v, intU, intV);
        }
    }

    /**
     * Checks if an expression is constant (doesn't contain the integration variable).
     */
    private boolean isConstant(String expression) {
        return !expression.contains(variable);
    }

    /**
     * Simplifies n+1 for simple numeric values.
     * <p>
     * For example: "2" becomes "3", "5" becomes "6", "2.5" becomes "3.5".
     * Returns "(n+1)" for complex expressions.
     * </p>
     */
    private String simplifyAddOne(String n) {
        if (!Utils.isNumeric(n)) {
            // Not a simple number, return symbolic form
            return "(" + n + "+1)";
        }

        try {
            // Try to parse as integer first
            int value = Integer.parseInt(n);
            return String.valueOf(value + 1);
        } catch (NumberFormatException e1) {
            try {
                // Parse as double
                double value = Double.parseDouble(n);
                double result = value + 1.0;
                // Return integer string if result is whole number
                if (result == Math.floor(result)) {
                    return String.valueOf((int) result);
                }
                return String.valueOf(result);
            } catch (NumberFormatException e2) {
                // Fallback (shouldn't reach here if isNumeric works correctly)
                return "(" + n + "+1)";
            }
        }
    }

    /**
     * Integrates when left operand is constant: ∫c op v dx.
     */
    private String applyConstantLeftIntegration(String c, char operator, String intV) {
        return switch (operator) {
            case '+' -> c + "*" + variable + "+" + intV;        // ∫(c+v) dx = c*x + ∫v dx
            case '-' -> c + "*" + variable + "-(" + intV + ")"; // ∫(c-v) dx = c*x - ∫v dx
            case '*' -> c + "*(" + intV + ")";                  // ∫(c*v) dx = c*∫v dx
            case '/' -> throw new UnsupportedOperationException(
                    String.format("Integration of ∫(%s/%s) d%s not supported - would require substitution u=%s",
                            c, intV, variable, intV));
            case '^' -> throw new UnsupportedOperationException(
                    String.format("Integration of ∫(%s^%s) d%s not supported - exponential substitution required",
                            c, intV, variable));
            default -> throw new UnsupportedOperationException(
                    "Unknown operator: " + operator);
        };
    }

    /**
     * Integrates when right operand is constant: ∫u op c dx.
     */
    private String applyConstantRightIntegration(String u, String c, char operator, String intU) {
        return switch (operator) {
            case '+' -> intU + "+" + c + "*" + variable;    // ∫(u+c) dx = ∫u dx + c*x
            case '-' -> intU + "-" + c + "*" + variable;    // ∫(u-c) dx = ∫u dx - c*x
            case '*' -> c + "*(" + intU + ")";              // ∫(u*c) dx = c*∫u dx
            case '/' -> "(" + intU + ")/" + c;              // ∫(u/c) dx = (1/c)*∫u dx
            case '^' -> {
                // ∫u^n dx (power rule)
                if (c.equals("-1") || c.equals("(-1)")) {
                    yield "ln(abs(" + u + "))";  // ∫x^(-1) dx = ln(|x|)
                } else if (u.equals(variable)) {
                    // ∫x^n dx = x^(n+1)/(n+1)
                    String exponentPlusOne = simplifyAddOne(c);
                    yield "(" + variable + "^" + exponentPlusOne + ")/" + exponentPlusOne;
                } else {
                    throw new UnsupportedOperationException(
                            String.format("Integration of ∫(%s)^%s d%s requires chain rule with substitution u=%s (not implemented)",
                                    u, c, variable, u));
                }
            }
            default -> throw new UnsupportedOperationException("Unknown operator: " + operator);
        };
    }

    /**
     * Integrates when both operands contain variables: ∫u op v dx.
     *
     * @param operator the binary operator
     * @param u        the left operand (original expression)
     * @param v        the right operand (original expression)
     * @param intU     the integral of u (pre-computed for sum/difference)
     * @param intV     the integral of v (pre-computed for sum/difference)
     */
    private String applyGeneralIntegration(char operator, String u, String v, String intU, String intV) {
        return switch (operator) {
            case '+' -> intU + "+" + intV;              // ∫(u+v) dx = ∫u dx + ∫v dx
            case '-' -> intU + "-(" + intV + ")";       // ∫(u-v) dx = ∫u dx - ∫v dx
            case '*' -> tryIntegrationByParts(u, v);    // Try integration by parts
            case '/' -> tryQuotientIntegration(u, v);   // Try logarithmic derivative pattern
            case '^' -> throw new UnsupportedOperationException(
                    "General power integration not implemented (∫u^v dx)");
            default -> throw new UnsupportedOperationException(
                    "Unknown operator: " + operator);
        };
    }

    /**
     * Attempts integration by parts: ∫u*v dx = u∫v dx - ∫(u'∫v dx) dx
     * <p>
     * Handles simple cases like:
     * - Polynomial × transcendental (x·sin(x), x²·exp(x))
     * - x^n × ln(x)
     * </p>
     * <p>
     * Strategy: Polynomial should be u (differentiate), transcendental should be v (integrate)
     * LIATE order: Logarithmic, Inverse trig, Algebraic, Trigonometric, Exponential
     * </p>
     *
     * @param left  the left operand
     * @param right the right operand
     */
    private String tryIntegrationByParts(String left, String right) {
        // Check if both are polynomials
        boolean leftIsPolynomial = isSimplePolynomial(left);
        boolean rightIsPolynomial = isSimplePolynomial(right);

        if (leftIsPolynomial && rightIsPolynomial) {
            throw new UnsupportedOperationException(
                    String.format("Integration by parts not implemented for polynomial product: ∫(%s)*(%s) d%s " +
                                    "(both operands are polynomials - this might indicate a parsing issue)",
                            left, right, variable));
        }

        boolean leftIsTranscendental = isTranscendental(left);
        boolean rightIsTranscendental = isTranscendental(right);

        // Pattern: polynomial * transcendental
        if (leftIsPolynomial && rightIsTranscendental) {
            return integrateByParts(left, right);
        } else if (rightIsPolynomial && leftIsTranscendental) {
            return integrateByParts(right, left);
        }

        // Pattern: logarithmic * algebraic (LIATE rule: logarithm should be u)
        boolean leftIsLog = left.startsWith("ln(") || left.startsWith("log(");
        boolean rightIsLog = right.startsWith("ln(") || right.startsWith("log(");

        if (leftIsLog && (rightIsPolynomial || right.equals(variable))) {
            return integrateByParts(left, right); // ln(x) is u
        } else if (rightIsLog && (leftIsPolynomial || left.equals(variable))) {
            return integrateByParts(right, left); // ln(x) is u
        }

        // No pattern matched
        throw new UnsupportedOperationException(
                String.format("Integration by parts not implemented for: ∫(%s)*(%s) d%s " +
                                "(no recognizable pattern: polynomial*transcendental or variable*logarithm)",
                        left, right, variable));
    }

    /**
     * Performs integration by parts on simple cases.
     *
     * @param u the part to differentiate
     * @param v the part to integrate
     */
    private String integrateByParts(String u, String v) {
        try {
            // Differentiate u
            String du = differentiateExpression(u);

            // Integrate v
            String vIntegral = integrateExpression(v);

            // Compute ∫u'·∫v dx
            String innerIntegrand = du + "*(" + vIntegral + ")";
            String innerIntegral = integrateExpression(innerIntegrand);

            // Result: u·∫v dx - ∫(u'·∫v dx) dx
            return u + "*(" + vIntegral + ")-(" + innerIntegral + ")";
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                    "Integration by parts failed for ∫(" + u + ")*(" + v + ") dx: " + e.getMessage());
        }
    }

    /**
     * Attempts quotient integration for special patterns:
     * 1. ∫f'(x)/f(x) dx = ln|f(x)| + C
     * 2. ∫polynomial/polynomial dx (partial fractions - limited support)
     *
     * @param numerator   the numerator expression
     * @param denominator the denominator expression
     */
    private String tryQuotientIntegration(String numerator, String denominator) {
        // Special case: Check if numerator is derivative of denominator
        try {
            String denominatorDerivative = differentiateExpression(denominator);

            // Simplify comparison by removing spaces and outer parentheses
            String numSimplified = simplifyForComparison(numerator);
            String derivSimplified = simplifyForComparison(denominatorDerivative);

            // Check if they're equal or scalar multiples
            if (numSimplified.equals(derivSimplified)) {
                // ∫f'(x)/f(x) dx = ln|f(x)| + C
                return "ln(abs(" + denominator + "))";
            }

            // Check if numerator is negative of derivative (e.g., tan(x) = sin(x)/cos(x) where d/dx[cos(x)] = -sin(x))
            if (numSimplified.equals("-" + derivSimplified) || ("-" + numSimplified).equals(derivSimplified)) {
                // ∫-f'(x)/f(x) dx = -ln|f(x)| + C
                return "-ln(abs(" + denominator + "))";
            }

            // Check if numerator is a constant multiple of the derivative
            if (isConstantMultiple(numSimplified, derivSimplified)) {
                double constant = extractConstantMultiple(numSimplified, derivSimplified);
                if (constant == 1.0) {
                    return "ln(abs(" + denominator + "))";
                }
                return constant + "*ln(abs(" + denominator + "))";
            }
        } catch (Exception e) {
            // Derivative check failed, continue to other patterns
        }

        // No pattern matched
        throw new UnsupportedOperationException(
                String.format("Quotient integration not implemented for: ∫(%s)/(%s) d%s " +
                                "(numerator is not the derivative of denominator - would require advanced techniques like substitution or partial fractions)",
                        numerator, denominator, variable));
    }

    /**
     * Checks if an expression is a simple polynomial in the variable.
     * <p>
     * Handles parentheses and spaces: x, (x), x^2, (x^2), (x ^ 2), 2*x, 3*x^2, etc.
     * A simple polynomial is a power of the variable, optionally multiplied by a numeric constant.
     * </p>
     */
    private boolean isSimplePolynomial(String expr) {
        // Remove spaces and outer parentheses for checking
        String normalized = simplifyForComparison(expr);

        // Remove numeric coefficient if present (e.g., "2*x" -> "x")
        String withoutCoeff = normalized;
        if (normalized.matches("-?\\d+(\\.\\d+)?\\*.*")) {
            int starIndex = normalized.indexOf('*');
            withoutCoeff = normalized.substring(starIndex + 1);
        }

        // Just the variable: x
        if (withoutCoeff.equals(variable)) {
            return true;
        }

        // Power of variable: x^2, x^3, etc.
        if (withoutCoeff.startsWith(variable + "^")) {
            String exponent = withoutCoeff.substring(variable.length() + 1);
            // Check if exponent is a number (integer or decimal)
            return Utils.isNumeric(exponent);
        }

        return false;
    }

    /**
     * Checks if an expression is a transcendental function (trig, exp, log, etc.).
     * <p>
     * Transcendental functions are non-algebraic functions like trigonometric,
     * exponential, logarithmic, and hyperbolic functions.
     * </p>
     */
    private boolean isTranscendental(String expr) {
        // Trigonometric functions
        if (expr.contains("sin(") || expr.contains("cos(") || expr.contains("tan(") ||
                expr.contains("sec(") || expr.contains("csc(") || expr.contains("cot(")) {
            return true;
        }

        // Inverse trigonometric functions
        if (expr.contains("asin(") || expr.contains("acos(") || expr.contains("atan(") ||
                expr.contains("asec(") || expr.contains("acsc(") || expr.contains("acot(")) {
            return true;
        }

        // Hyperbolic functions
        if (expr.contains("sinh(") || expr.contains("cosh(") || expr.contains("tanh(") ||
                expr.contains("sech(") || expr.contains("csch(") || expr.contains("coth(")) {
            return true;
        }

        // Exponential and logarithmic
        if (expr.contains("exp(") || expr.contains("ln(") || expr.contains("log(")) {
            return true;
        }

        return false;
    }

    /**
     * Differentiates an expression by creating a Function and using the differentiator.
     */
    private String differentiateExpression(String expression) {
        try {
            Function f = new Function(expression, variable);
            Function result = differentiator.differentiate(f, true);
            return result.getEquation();
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                    "Cannot differentiate expression: " + expression, e);
        }
    }

    /**
     * Recursively integrates an expression by parsing it as a new Function.
     */
    private String integrateExpression(String expression) {
        try {
            Function f = new Function(expression, variable);
            // Don't add +C for intermediate integrals
            return new Integrator(variable).integrate(f, true).replace("+C", "");
        } catch (Exception e) {
            throw new UnsupportedOperationException(
                    "Cannot integrate expression: " + expression, e);
        }
    }

    /**
     * Simplifies an expression for comparison by removing spaces, outer parentheses, and leading 1*.
     * <p>
     * This is a workaround for comparing expressions without full algebraic simplification.
     * It handles common cases like "(x)" vs "x" and "1*-sin(x)" vs "-sin(x)".
     * </p>
     */
    private String simplifyForComparison(String expr) {
        if (expr == null || expr.isEmpty()) {
            return expr;
        }

        String simplified = expr.replace(" ", "");

        // Remove leading 1* (handles cases like "1*-sin(x)")
        while (simplified.startsWith("1*")) {
            simplified = simplified.substring(2);
        }

        // Remove outer parentheses using Utils method
        simplified = Utils.removeOuterParenthesis(simplified);

        return simplified;
    }

    /**
     * Checks if expr1 is a constant multiple of expr2.
     * <p>
     * Handles patterns like "2*sin(x)" vs "sin(x)" or "-3*x" vs "x".
     * Returns true if one expression is a numeric constant times the other.
     * </p>
     */
    private boolean isConstantMultiple(String expr1, String expr2) {
        try {
            String simplified1 = simplifyForComparison(expr1);
            String simplified2 = simplifyForComparison(expr2);

            // Check if expr1 = constant * expr2
            if (simplified1.matches("-?\\d+(\\.\\d+)?\\*.*")) {
                int starIndex = simplified1.indexOf('*');
                String rest = simplified1.substring(starIndex + 1);
                if (rest.equals(simplified2) || simplifyForComparison(rest).equals(simplified2)) {
                    return true;
                }
            }

            // Check if expr2 = constant * expr1
            if (simplified2.matches("-?\\d+(\\.\\d+)?\\*.*")) {
                int starIndex = simplified2.indexOf('*');
                String rest = simplified2.substring(starIndex + 1);
                if (rest.equals(simplified1) || simplifyForComparison(rest).equals(simplified1)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the constant multiple between two expressions.
     * <p>
     * If expr1 = k*expr2, returns k.
     * If expr2 = k*expr1, returns 1/k.
     * Otherwise returns 1.0.
     * </p>
     */
    private double extractConstantMultiple(String expr1, String expr2) {
        try {
            String simplified1 = simplifyForComparison(expr1);
            String simplified2 = simplifyForComparison(expr2);

            // Check if expr1 = constant * ...
            if (simplified1.matches("-?\\d+(\\.\\d+)?\\*.*")) {
                int starIndex = simplified1.indexOf('*');
                return Double.parseDouble(simplified1.substring(0, starIndex));
            }

            // Check if expr2 = constant * ...
            if (simplified2.matches("-?\\d+(\\.\\d+)?\\*.*")) {
                int starIndex = simplified2.indexOf('*');
                return 1.0 / Double.parseDouble(simplified2.substring(0, starIndex));
            }

            return 1.0;
        } catch (Exception e) {
            return 1.0;
        }
    }

    /**
     * Optimizes the result by simplifying redundant operations.
     * <p>
     * Note: We intentionally do NOT remove "1*" patterns as this breaks
     * function calls like "1*exp(x)" → "expx".
     * </p>
     */
    private String optimize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        String result = s;

        // Remove x^1 → x
        result = result.replace(variable + "^1", variable);

        // Simplify zero operations
        result = result.replace("+0", "");     // x+0 → x
        result = result.replace("-0", "");     // x-0 → x
        result = result.replace("*0", "0");    // x*0 → 0

        // Clean up double signs
        result = result.replace("--", "+");    // x--y → x+y
        result = result.replace("+-", "-");    // x+-y → x-y

        // Remove redundant nested parentheses using Utils method
        result = Utils.removeRedundantParentheses(result);

        // Recursively optimize if changes were made (limit recursion depth)
        if (!result.equals(s) && !result.isEmpty()) {
            result = optimize(result);
        }

        return result;
    }
}
