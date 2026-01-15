package uk.co.ryanharrison.mathengine.integral.symbolic;

import uk.co.ryanharrison.mathengine.core.Function;
import uk.co.ryanharrison.mathengine.differential.symbolic.Differentiator;

public class test_integration_fixes {
    public static void main(String[] args) {
        Integrator integrator = new Integrator();
        Differentiator diff = new Differentiator();

        // Test 1: exp(x) - should not produce expx
        System.out.println("Test 1: exp(x)");
        String result1 = integrator.integrate("exp(x)");
        System.out.println("  Result: " + result1);
        Function f1 = new Function(result1.replace("+C", ""));
        Function d1 = diff.differentiate(f1, true);
        System.out.println("  Derivative: " + d1.getEquation());
        try {
            double val = f1.evaluateAt(1.0);
            System.out.println("  Evaluates at x=1: " + val);
        } catch (Exception e) {
            System.out.println("  ERROR: " + e.getMessage());
        }

        // Test 2: tangent sin(x)/cos(x)
        System.out.println("\nTest 2: sin(x)/cos(x)");
        String result2 = integrator.integrate("sin(x)/cos(x)");
        System.out.println("  Result: " + result2);

        // Test 3: logarithmic derivative (2*x)/(x^2)
        System.out.println("\nTest 3: (2*x)/(x^2)");
        String result3 = integrator.integrate("(2*x)/(x^2)");
        System.out.println("  Result: " + result3);

        // Test 4: x^2*exp(x)
        System.out.println("\nTest 4: x^2*exp(x)");
        try {
            String result4 = integrator.integrate("x^2*exp(x)");
            System.out.println("  Result: " + result4);
        } catch (Exception e) {
            System.out.println("  ERROR: " + e.getMessage());
        }
    }
}
