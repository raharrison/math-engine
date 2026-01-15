package uk.co.ryanharrison.mathengine.differential.symbolic;

import java.util.function.Function;

/**
 * Symbolic differentiation rules for standard mathematical functions.
 * <p>
 * Each method applies the chain rule to differentiate a function composition.
 * The argument {@code u} is the inner function, and {@code du} is its derivative.
 * </p>
 */
final class DifferentiationRules {

    private DifferentiationRules() {
        throw new AssertionError("Utility class");
    }

    /**
     * Applies the appropriate differentiation rule based on the function name.
     *
     * @param functionName   the function to differentiate (e.g., "sin", "cos", "ln")
     * @param u              the argument expression as a string
     * @param differentiator function to differentiate sub-expressions
     * @return the derivative expression as a string
     */
    static String differentiate(String functionName, String u, Function<String, String> differentiator) {
        String du = differentiator.apply(u);

        return switch (functionName) {
            // Trigonometric functions
            case "sin" -> du + "*cos(" + u + ")";                              // d/dx[sin(u)] = du*cos(u)
            case "cos" -> du + "*-sin(" + u + ")";                             // d/dx[cos(u)] = -du*sin(u)
            case "tan" -> du + "*sec(" + u + ")^2";                            // d/dx[tan(u)] = du*sec²(u)
            case "sec" -> du + "*sec(" + u + ")*tan(" + u + ")";               // d/dx[sec(u)] = du*sec(u)*tan(u)
            case "cosec", "csc" -> du + "*-cosec(" + u + ")*cot(" + u + ")";   // d/dx[csc(u)] = -du*csc(u)*cot(u)
            case "cot" -> du + "*-cosec(" + u + ")^2";                         // d/dx[cot(u)] = -du*csc²(u)

            // Hyperbolic functions
            case "sinh" -> du + "*cosh(" + u + ")";                            // d/dx[sinh(u)] = du*cosh(u)
            case "cosh" -> du + "*sinh(" + u + ")";                            // d/dx[cosh(u)] = du*sinh(u)
            case "tanh" -> du + "*sech(" + u + ")^2";                          // d/dx[tanh(u)] = du*sech²(u)
            case "sech" -> du + "*sech(" + u + ")*tanh(" + u + ")";            // d/dx[sech(u)] = du*sech(u)*tanh(u)
            case "cosech", "csch" -> du + "*-cosech(" + u + ")*coth(" + u + ")"; // d/dx[csch(u)] = -du*csch(u)*coth(u)
            case "coth" -> du + "*-cosech(" + u + ")^2";                       // d/dx[coth(u)] = -du*csch²(u)

            // Inverse trigonometric functions
            case "asin" -> du + "/sqrt(1-(" + u + ")^2)";                      // d/dx[asin(u)] = du/√(1-u²)
            case "acos" -> "(-" + du + ")/sqrt(1-(" + u + ")^2)";              // d/dx[acos(u)] = -du/√(1-u²)
            case "atan" -> du + "/(1+(" + u + ")^2)";                          // d/dx[atan(u)] = du/(1+u²)
            case "asec" -> du + "/(abs(" + u + ")*sqrt((" + u + ")^2-1))";     // d/dx[asec(u)] = du/(|u|√(u²-1))
            case "acosec", "acsc" -> "(-" + du + ")/(abs(" + u + ")*sqrt((" + u + ")^2-1))"; // d/dx[acsc(u)] = -du/(|u|√(u²-1))
            case "acot" -> "(-" + du + ")/(1+(" + u + ")^2)";                  // d/dx[acot(u)] = -du/(1+u²)

            // Inverse hyperbolic functions
            case "asinh" -> du + "/sqrt((" + u + ")^2+1)";                     // d/dx[asinh(u)] = du/√(u²+1)
            case "acosh" -> du + ")/sqrt((" + u + ")^2-1)";                    // d/dx[acosh(u)] = du/√(u²-1)
            case "atanh" -> du + "/(1-(" + u + ")^2)";                         // d/dx[atanh(u)] = du/(1-u²)
            case "asech" -> "(-" + du + ")/((" + u + ")*sqrt(1-(" + u + ")^2))"; // d/dx[asech(u)] = -du/(u√(1-u²))
            case "acosech", "acsch" -> "(-" + du + ")/((" + u + ")*sqrt(1+(" + u + ")^2))"; // d/dx[acsch(u)] = -du/(u√(1+u²))
            case "acoth" -> du + "/(1-(" + u + ")^2)";                         // d/dx[acoth(u)] = du/(1-u²)

            // Exponential and logarithmic functions
            case "exp" -> du + "*exp(" + u + ")";                              // d/dx[exp(u)] = du*exp(u)
            case "sqrt" -> du + "/(2*sqrt(" + u + "))";                        // d/dx[√u] = du/(2√u)
            case "log10" -> du + "/((" + u + ")*log(10))";                     // d/dx[log₁₀(u)] = du/(u*ln(10))
            case "log", "ln" -> du + "/(" + u + ")";                           // d/dx[ln(u)] = du/u
            case "abs" -> du + "*(" + u + ")/abs(" + u + ")";                  // d/dx[|u|] = du*u/|u|
            case "sign" -> "0";                                                // d/dx[sign(u)] = 0

            // Parenthesized expression
            case "" -> du;

            default -> throw new UnsupportedOperationException(
                    "Differentiation rule not defined for function: " + functionName);
        };
    }
}
