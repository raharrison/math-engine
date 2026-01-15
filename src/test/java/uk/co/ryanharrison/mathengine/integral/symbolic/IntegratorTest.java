package uk.co.ryanharrison.mathengine.integral.symbolic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.core.Function;
import uk.co.ryanharrison.mathengine.differential.symbolic.Differentiator;
import uk.co.ryanharrison.mathengine.integral.TrapeziumIntegrator;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for symbolic integration.
 * <p>
 * Tests verify mathematical correctness by:
 * 1. Differentiating the result and comparing to the original function
 * 2. Evaluating the result at specific points and verifying correctness
 * </p>
 */
class IntegratorTest {

    private static final double TOLERANCE = 1e-6;
    private static final double NUMERICAL_TOLERANCE = 1e-4;  // More relaxed for numerical integration
    private Integrator integrator;
    private Differentiator differentiator;

    @BeforeEach
    void setUp() {
        integrator = new Integrator();
        differentiator = new Differentiator();
    }

    /**
     * Helper method to integrate an expression string.
     */
    private String integrate(String expression) {
        return integrator.integrate(expression);
    }

    /**
     * Helper method to integrate with optimization off for exact checking.
     */
    private String integrateNoOptimize(String expression) {
        Function f = new Function(expression);
        return integrator.integrate(f, false);
    }

    /**
     * Verifies integration correctness by differentiating the result.
     * The derivative of the integral should equal the original function.
     * <p>
     * Note: Uses numerical evaluation rather than symbolic comparison due to
     * lack of algebraic simplification in the differentiator.
     * </p>
     */
    private void verifyIntegrationByDifferentiation(String original, String antiderivative) {
        // Remove +C from antiderivative
        String antiderivativeWithoutC = antiderivative.replace("+C", "").replace("+ C", "").trim();

        // Differentiate the antiderivative
        Function f = new Function(antiderivativeWithoutC);
        Function derivative = differentiator.differentiate(f, true);

        // Evaluate both at several points to verify equality
        double[] testPoints = {0.5, 1.0, 2.0, 3.0};
        Function originalFunc = new Function(original);

        for (double x : testPoints) {
            double expected = originalFunc.evaluateAt(x);
            double actual = derivative.evaluateAt(x);

            assertThat(actual)
                    .as("At x=" + x + ": d/dx[" + antiderivativeWithoutC + "] should equal " + original)
                    .isCloseTo(expected, within(TOLERANCE));
        }
    }

    /**
     * Verifies integration by evaluating definite integrals.
     * Compares F(b) - F(a) with numerical integration using TrapeziumIntegrator.
     */
    private void verifyDefiniteIntegral(String original, String antiderivative, double a, double b) {
        String antiderivativeWithoutC = antiderivative.replace("+C", "").replace("+ C", "").trim();
        Function F = new Function(antiderivativeWithoutC);

        // Evaluate at boundaries
        double Fb = F.evaluateAt(b);
        double Fa = F.evaluateAt(a);
        double definiteIntegral = Fb - Fa;

        // Numerical integration using TrapeziumIntegrator
        Function f = new Function(original);
        TrapeziumIntegrator numericalIntegrator = TrapeziumIntegrator.of(f, a, b, 1500);
        double numericalIntegral = numericalIntegrator.integrate();

        assertThat(definiteIntegral)
                .as("Definite integral from " + a + " to " + b)
                .isCloseTo(numericalIntegral, within(NUMERICAL_TOLERANCE));
    }

    // ==================== Constants and Basic Rules ====================

    @Nested
    class ConstantsTest {

        @Test
        void integrateConstant() {
            // ∫5 dx = 5x + C
            String result = integrate("5");
            verifyIntegrationByDifferentiation("5", result);
        }

        @Test
        void integrateZero() {
            // ∫0 dx = C
            String result = integrate("0");
            verifyIntegrationByDifferentiation("0", result);
        }

        @Test
        void integratePi() {
            // ∫π dx = πx + C
            String result = integrate("pi");
            verifyIntegrationByDifferentiation("pi", result);
        }

        @Test
        void integrateEuler() {
            // ∫e dx = ex + C (e as constant, not exp(x))
            String result = integrate("e");
            verifyIntegrationByDifferentiation("e", result);
        }
    }

    @Nested
    class BasicArithmeticTest {

        @Test
        void integrateVariable() {
            // ∫x dx = x²/2 + C
            String result = integrate("x");
            verifyDefiniteIntegral("x", result, 1.0, 3.0);
        }

        @Test
        void integrateNegativeVariable() {
            // ∫-x dx = -x²/2 + C
            String result = integrate("-x");
            verifyDefiniteIntegral("-x", result, 1.0, 3.0);
        }

        @Test
        void integrateSumOfConstants() {
            // ∫(3 + 5) dx = 8x + C
            String result = integrate("3+5");
            verifyIntegrationByDifferentiation("3+5", result);
        }

        @Test
        void integrateDifference() {
            // ∫(x - 3) dx = x²/2 - 3x + C
            String result = integrate("x-3");
            verifyDefiniteIntegral("x-3", result, 1.0, 3.0);
        }

        @Test
        void integrateConstantMultiple() {
            // ∫3x dx = 3*x²/2 + C
            String result = integrate("3*x");
            verifyDefiniteIntegral("3*x", result, 1.0, 3.0);
        }

        @Test
        void integrateSum() {
            // ∫(x + x) dx = x² + C
            String result = integrate("x+x");
            verifyDefiniteIntegral("x+x", result, 1.0, 3.0);
        }
    }

    // ==================== Power Rule ====================

    @Nested
    class PowerRuleTest {

        @Test
        void integrateXSquared() {
            // ∫x² dx = x³/3 + C
            String result = integrate("x^2");
            verifyDefiniteIntegral("x^2", result, 1.0, 3.0);
        }

        @Test
        void integrateXCubed() {
            // ∫x³ dx = x⁴/4 + C
            String result = integrate("x^3");
            verifyDefiniteIntegral("x^3", result, 1.0, 3.0);
        }

        @Test
        void integrateConstantPower() {
            // ∫x^5 dx = x^6/6 + C
            String result = integrate("x^5");
            verifyDefiniteIntegral("x^5", result, 1.0, 3.0);
        }

        @Test
        void integrateReciprocalX() {
            // ∫x^(-1) dx = ∫1/x dx = ln(|x|) + C
            String result = integrate("x^-1");
            // Test on positive values only (to avoid ln of negative)
            verifyDefiniteIntegral("x^-1", result, 1.0, 3.0);
        }
    }

    // ==================== Trigonometric Functions ====================

    @Nested
    class TrigonometricTest {

        @Test
        void integrateSine() {
            // ∫sin(x) dx = -cos(x) + C
            String result = integrate("sin(x)");
            verifyIntegrationByDifferentiation("sin(x)", result);
        }

        @Test
        void integrateCosine() {
            // ∫cos(x) dx = sin(x) + C
            String result = integrate("cos(x)");
            verifyIntegrationByDifferentiation("cos(x)", result);
        }

        @Test
        void integrateTangent() {
            // ∫tan(x) dx = -ln(|cos(x)|) + C
            String result = integrate("tan(x)");
            verifyDefiniteIntegral("tan(x)", result, 0.1, 1.0);
        }

        @Test
        void integrateSecant() {
            // ∫sec(x) dx = ln(|sec(x) + tan(x)|) + C
            String result = integrate("sec(x)");
            verifyDefiniteIntegral("sec(x)", result, 0.1, 1.0);
        }

        @Test
        void integrateCotangent() {
            // ∫cot(x) dx = ln(|sin(x)|) + C
            String result = integrate("cot(x)");
            verifyDefiniteIntegral("cot(x)", result, 0.5, 2.0);
        }
    }

    // ==================== Exponential and Logarithmic ====================

    @Nested
    class ExponentialAndLogarithmicTest {

        @Test
        void integrateExponential() {
            // ∫exp(x) dx = exp(x) + C
            String result = integrate("exp(x)");
            verifyIntegrationByDifferentiation("exp(x)", result);
        }

        @Test
        void integrateNaturalLog() {
            // ∫ln(x) dx = x*ln(x) - x + C
            String result = integrate("ln(x)");
            verifyDefiniteIntegral("ln(x)", result, 1.0, 5.0);
        }

        @Test
        void integrateLog10() {
            // ∫log10(x) dx = x*log10(x) - x/ln(10) + C
            String result = integrate("log10(x)");
            verifyDefiniteIntegral("log10(x)", result, 1.0, 5.0);
        }
    }

    // ==================== Hyperbolic Functions ====================

    @Nested
    class HyperbolicTest {

        @Test
        void integrateSinh() {
            // ∫sinh(x) dx = cosh(x) + C
            String result = integrate("sinh(x)");
            verifyIntegrationByDifferentiation("sinh(x)", result);
        }

        @Test
        void integrateCosh() {
            // ∫cosh(x) dx = sinh(x) + C
            String result = integrate("cosh(x)");
            verifyIntegrationByDifferentiation("cosh(x)", result);
        }

        @Test
        void integrateTanh() {
            // ∫tanh(x) dx = ln(cosh(x)) + C
            String result = integrate("tanh(x)");
            verifyDefiniteIntegral("tanh(x)", result, 0.1, 2.0);
        }

        @Test
        void integrateSech() {
            // ∫sech(x) dx = atan(sinh(x)) + C
            String result = integrate("sech(x)");
            verifyDefiniteIntegral("sech(x)", result, 0.1, 2.0);
        }
    }

    // ==================== Inverse Trigonometric ====================

    @Nested
    class InverseTrigonometricTest {

        @Test
        void integrateArcsin() {
            // ∫asin(x) dx = x*asin(x) + sqrt(1-x²) + C
            String result = integrate("asin(x)");
            verifyDefiniteIntegral("asin(x)", result, 0.1, 0.9);
        }

        @Test
        void integrateArccos() {
            // ∫acos(x) dx = x*acos(x) - sqrt(1-x²) + C
            String result = integrate("acos(x)");
            verifyDefiniteIntegral("acos(x)", result, 0.1, 0.9);
        }

        @Test
        void integrateArctan() {
            // ∫atan(x) dx = x*atan(x) - ln(1+x²)/2 + C
            String result = integrate("atan(x)");
            verifyIntegrationByDifferentiation("atan(x)", result);
        }
    }

    // ==================== Algebraic Functions ====================

    @Nested
    class AlgebraicTest {

        @Test
        void integrateSqrt() {
            // ∫√x dx = (2/3)x^(3/2) + C
            String result = integrate("sqrt(x)");
            verifyDefiniteIntegral("sqrt(x)", result, 1.0, 4.0);
        }

        @Test
        void integrateAbsoluteValue() {
            // ∫|x| dx = (x*|x|)/2 + C
            String result = integrate("abs(x)");
            verifyDefiniteIntegral("abs(x)", result, 0.1, 3.0);
        }
    }

    // ==================== Combined Operations ====================

    @Nested
    class CombinedOperationsTest {

        @Test
        void integrateSumOfFunctions() {
            // ∫(sin(x) + cos(x)) dx = -cos(x) + sin(x) + C
            String result = integrate("sin(x)+cos(x)");
            verifyIntegrationByDifferentiation("sin(x)+cos(x)", result);
        }

        @Test
        void integrateDifferenceOfFunctions() {
            // ∫(exp(x) - ln(x)) dx = exp(x) - (x*ln(x) - x) + C
            String result = integrate("exp(x)-ln(x)");
            verifyDefiniteIntegral("exp(x)-ln(x)", result, 1.0, 3.0);
        }

        @Test
        void integrateConstantTimesFunction() {
            // ∫5*sin(x) dx = 5*(-cos(x)) + C
            String result = integrate("5*sin(x)");
            verifyIntegrationByDifferentiation("5*sin(x)", result);
        }

        @Test
        void integratePolynomial() {
            // ∫(x² + 3x + 2) dx = x³/3 + 3x²/2 + 2x + C
            String result = integrate("x^2+3*x+2");
            verifyIntegrationByDifferentiation("x^2+3*x+2", result);
        }
    }

    // ==================== Integration by Parts ====================

    @Nested
    class IntegrationByPartsTest {

        @Test
        void integrateXTimesSine() {
            // ∫x*sin(x) dx = -x*cos(x) + sin(x) + C
            String result = integrate("x*sin(x)");
            verifyDefiniteIntegral("x*sin(x)", result, 0.1, 3.0);
        }

        @Test
        void integrateXTimesCosine() {
            // ∫x*cos(x) dx = x*sin(x) + cos(x) + C
            String result = integrate("x*cos(x)");
            verifyDefiniteIntegral("x*cos(x)", result, 0.1, 3.0);
        }

        @Test
        void integrateXTimesExp() {
            // ∫x*exp(x) dx = (x-1)*exp(x) + C
            String result = integrate("x*exp(x)");
            verifyDefiniteIntegral("x*exp(x)", result, 0.1, 2.0);
        }

        @Test
        void integrateXSquaredTimesExp() {
            // ∫x²*exp(x) dx - complex but should work with repeated integration by parts
            // NOTE: Using (x^2)*exp(x) to work around parser precedence bug
            String result = integrate("(x^2)*exp(x)");
            verifyDefiniteIntegral("(x^2)*exp(x)", result, 0.1, 2.0);
        }
    }

    // ==================== Quotient Integration ====================

    @Nested
    class QuotientIntegrationTest {

        @Test
        void integrateLogarithmicDerivative() {
            // ∫(2*x)/(x^2) dx = ln|x^2| + C = 2*ln|x| + C
            String result = integrate("(2*x)/(x^2)");
            verifyDefiniteIntegral("(2*x)/(x^2)", result, 1.0, 3.0);
        }

        @Test
        void integrateTangent() {
            // ∫sin(x)/cos(x) dx = -ln|cos(x)| + C (tan(x))
            // This works as -sin(x) is derivative of cos(x)
            String result = integrate("sin(x)/cos(x)");
            verifyDefiniteIntegral("sin(x)/cos(x)", result, 0.1, 1.0);
        }
    }

    // ==================== Unsupported Operations ====================

    @Nested
    class UnsupportedOperationsTest {

        @Test
        void integrateComplexProductPattern() {
            // ∫sin(x)*cos(x) dx requires special techniques (trig substitution)
            assertThatThrownBy(() -> integrate("sin(x)*cos(x)"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("Integration by parts");
        }

        @Test
        void integrateComplexQuotientPattern() {
            // ∫cos(x)/sin(x) dx when cos(x) is NOT derivative of sin(x) in the right way
            // This might actually work with the derivative check, so let's use a different example
            assertThatThrownBy(() -> integrate("x/(x^3+1)"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("Quotient");
        }

        @Test
        void integratePowerOfVariableFunctions() {
            // ∫x^(sin(x)) dx has no elementary form
            assertThatThrownBy(() -> integrate("x^sin(x)"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("power integration");
        }

        @Test
        void integrateSignFunction() {
            // ∫sign(x) dx is not defined (discontinuous)
            assertThatThrownBy(() -> integrate("sign(x)"))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("sign");
        }
    }

    // ==================== Optimization ====================

    @Nested
    class OptimizationTest {

        @Test
        void optimizationRemovesRedundantMultiplication() {
            // Optimization should clean up 1*x and x*1
            String result = integrate("x");
            assertThat(result).doesNotContain("1*");
        }

        @Test
        void optimizationRemovesUnnecessaryParentheses() {
            // Check that optimization simplifies nested parentheses
            String result = integrate("x+3");
            // Should not have excessive parentheses
            int openCount = result.length() - result.replace("(", "").length();
            int closeCount = result.length() - result.replace(")", "").length();
            assertThat(openCount).isEqualTo(closeCount); // Balanced
        }

        @Test
        void optimizationSimplifiessZeroOperations() {
            // +0 and -0 should be removed
            String result = integrate("x-x+x");
            assertThat(result).doesNotContain("+0").doesNotContain("-0");
        }
    }

    // ==================== Different Variables ====================

    @Nested
    class DifferentVariablesTest {

        @Test
        void integrateWithRespectToT() {
            Integrator tIntegrator = new Integrator("t");
            Function f = new Function("t^2", "t");
            String result = tIntegrator.integrate(f);
            assertThat(result).contains("t^3").contains("+C");
        }

        @Test
        void integrateWithRespectToY() {
            Integrator yIntegrator = new Integrator("y");
            Function f = new Function("sin(y)", "y");
            String result = yIntegrator.integrate(f);
            assertThat(result).contains("cos(y)").contains("+C");
        }
    }
}
