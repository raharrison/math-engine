package uk.co.ryanharrison.mathengine.integral.symbolic;

/**
 * Database of symbolic integration rules for common mathematical functions.
 * <p>
 * Each rule applies the antiderivative formula with chain rule support.
 * If the function has an argument u (not just x), the rule returns the integral
 * multiplied by the notation indicating where the inner derivative du would go.
 *
 * <h2>Rule Format:</h2>
 * For ∫f(u) du, returns the antiderivative formula where:
 * <ul>
 *     <li>u is the function argument (e.g., "x^2" in sin(x^2))</li>
 *     <li>The result implicitly needs multiplication by du/dx</li>
 * </ul>
 *
 * <h2>Integration Rules Catalog:</h2>
 * <pre>
 * Trigonometric:
 *   ∫sin(u) du = -cos(u) + C
 *   ∫cos(u) du = sin(u) + C
 *   ∫tan(u) du = -ln(|cos(u)|) + C
 *   ∫sec(u) du = ln(|sec(u) + tan(u)|) + C
 *   ∫cosec(u) du = -ln(|cosec(u) + cot(u)|) + C
 *   ∫cot(u) du = ln(|sin(u)|) + C
 *
 * Inverse Trigonometric:
 *   ∫asin(u) du = u*asin(u) + sqrt(1-u²) + C
 *   ∫acos(u) du = u*acos(u) - sqrt(1-u²) + C
 *   ∫atan(u) du = u*atan(u) - ln(1+u²)/2 + C
 *
 * Hyperbolic:
 *   ∫sinh(u) du = cosh(u) + C
 *   ∫cosh(u) du = sinh(u) + C
 *   ∫tanh(u) du = ln(cosh(u)) + C
 *   ∫sech(u) du = atan(sinh(u)) + C
 *   ∫cosech(u) du = ln(|tanh(u/2)|) + C
 *   ∫coth(u) du = ln(|sinh(u)|) + C
 *
 * Exponential and Logarithmic:
 *   ∫exp(u) du = exp(u) + C
 *   ∫ln(u) du = u*ln(u) - u + C
 *   ∫log10(u) du = u*log10(u) - u/ln(10) + C
 *
 * Algebraic:
 *   ∫sqrt(u) du = (2/3)*u^(3/2) + C
 *   ∫1/sqrt(u) du = 2*sqrt(u) + C
 *   ∫abs(u) du = (u*abs(u))/2 + C
 * </pre>
 *
 * @see Integrator
 */
public final class IntegrationRules {

    private IntegrationRules() {
        // Utility class - no instantiation
    }

    /**
     * Looks up the integration rule for a given function.
     *
     * @param function the function name (e.g., "sin", "cos", "ln")
     * @param argument the function argument as a string (e.g., "x", "x^2")
     * @return the antiderivative formula as a string
     * @throws UnsupportedOperationException if no rule exists for the function
     */
    public static String integrate(String function, String argument) {
        if (function == null || function.isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        if (argument == null || argument.isEmpty()) {
            throw new IllegalArgumentException("Argument cannot be null or empty");
        }

        String u = argument;

        return switch (function.toLowerCase()) {
            // ==================== Trigonometric ====================
            case "sin", "sine" -> "(-cos(" + u + "))";
            case "cos", "cosine" -> "sin(" + u + ")";
            case "tan", "tangent" -> "(-ln(abs(cos(" + u + "))))";
            case "sec", "secant" -> "ln(abs(sec(" + u + ")+tan(" + u + ")))";
            case "cosec", "csc", "cosecant" -> "(-ln(abs(csc(" + u + ")+cot(" + u + "))))";
            case "cot", "cotangent" -> "ln(abs(sin(" + u + ")))";

            // ==================== Inverse Trigonometric ====================
            case "asin", "arcsin" -> u + "*asin(" + u + ")+sqrt(1-(" + u + ")^2)";
            case "acos", "arccos" -> u + "*acos(" + u + ")-sqrt(1-(" + u + ")^2)";
            case "atan", "arctan" -> u + "*atan(" + u + ")-ln(1+(" + u + ")^2)/2";
            case "asec", "arcsec" -> u + "*asec(" + u + ")-ln(abs(" + u + "+sqrt((" + u + ")^2-1)))";
            case "acosec", "acsc", "arccsc" -> u + "*acsc(" + u + ")+ln(abs(" + u + "+sqrt((" + u + ")^2-1)))";
            case "acot", "arccot" -> u + "*acot(" + u + ")+ln(1+(" + u + ")^2)/2";

            // ==================== Hyperbolic ====================
            case "sinh" -> "cosh(" + u + ")";
            case "cosh" -> "sinh(" + u + ")";
            case "tanh" -> "ln(cosh(" + u + "))";
            case "sech" -> "atan(sinh(" + u + "))";
            case "cosech", "csch" -> "ln(abs(tanh((" + u + ")/2)))";
            case "coth" -> "ln(abs(sinh(" + u + ")))";

            // ==================== Inverse Hyperbolic ====================
            case "asinh", "arcsinh" -> u + "*asinh(" + u + ")-sqrt((" + u + ")^2+1)";
            case "acosh", "arccosh" -> u + "*acosh(" + u + ")-sqrt((" + u + ")^2-1)";
            case "atanh", "arctanh" -> u + "*atanh(" + u + ")+ln(1-(" + u + ")^2)/2";
            case "asech", "arcsech" -> u + "*asech(" + u + ")+asin(" + u + ")";
            case "acosech", "acsch", "arccsch" -> u + "*acsch(" + u + ")+asinh(abs(" + u + "))";
            case "acoth", "arccoth" -> u + "*acoth(" + u + ")+ln((" + u + ")^2-1)/2";

            // ==================== Exponential and Logarithmic ====================
            case "exp" -> "exp(" + u + ")";
            case "ln", "log" -> u + "*ln(" + u + ")-(" + u + ")";
            case "log10" -> u + "*log10(" + u + ")-(" + u + ")/ln(10)";
            case "log2" -> u + "*log2(" + u + ")-(" + u + ")/ln(2)";

            // ==================== Algebraic ====================
            case "sqrt" -> "(2/3)*(" + u + ")^(3/2)";
            case "abs" -> "(" + u + "*abs(" + u + "))/2";

            // ==================== Special Cases ====================
            case "sign" -> throw new UnsupportedOperationException(
                    "Integration of sign(x) is not defined (discontinuous)");

            // Empty string (just parentheses) - identity
            case "" -> u;

            default -> throw new UnsupportedOperationException(
                    "No integration rule for function: " + function);
        };
    }
}
