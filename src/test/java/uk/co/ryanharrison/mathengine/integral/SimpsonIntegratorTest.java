package uk.co.ryanharrison.mathengine.integral;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.core.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for {@link SimpsonIntegrator}.
 */
class SimpsonIntegratorTest {

    private static final double TOLERANCE = 1e-5;
    private static final double EXACT_TOLERANCE = 1e-9;

    // ==================== Construction Tests ====================

    @Test
    void ofCreatesIntegratorWithDefaultIterations() {
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 5.0);

        assertThat(integrator.getTargetFunction()).isEqualTo(f);
        assertThat(integrator.getLowerBound()).isEqualTo(0.0);
        assertThat(integrator.getUpperBound()).isEqualTo(5.0);
        assertThat(integrator.getIterations()).isEqualTo(1000);
    }

    @Test
    void ofWithIterationsCreatesIntegratorWithSpecifiedIterations() {
        Function f = new Function("x^3");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 1.0, 10.0, 500);

        assertThat(integrator.getTargetFunction()).isEqualTo(f);
        assertThat(integrator.getLowerBound()).isEqualTo(1.0);
        assertThat(integrator.getUpperBound()).isEqualTo(10.0);
        assertThat(integrator.getIterations()).isEqualTo(500);
    }

    @Test
    void ofRejectsNullFunction() {
        assertThatThrownBy(() -> SimpsonIntegrator.of(null, 0.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void ofRejectsInfiniteLowerBound() {
        Function f = new Function("x");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, Double.POSITIVE_INFINITY, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, Double.NEGATIVE_INFINITY, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, Double.NaN, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");
    }

    @Test
    void ofRejectsInfiniteUpperBound() {
        Function f = new Function("x");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, 0.0, Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, 0.0, Double.NEGATIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, 0.0, Double.NaN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");
    }

    @Test
    void ofRejectsLowerBoundGreaterThanOrEqualToUpperBound() {
        Function f = new Function("x");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, 5.0, 5.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be less than upper bound");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, 10.0, 5.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be less than upper bound");
    }

    @Test
    void ofRejectsNonPositiveIterations() {
        Function f = new Function("x");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, 0.0, 1.0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Iterations must be positive");

        assertThatThrownBy(() -> SimpsonIntegrator.of(f, 0.0, 1.0, -10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Iterations must be positive");
    }

    @Test
    void builderCreatesIntegratorWithDefaultIterations() {
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.builder()
                .function(f)
                .lowerBound(0.0)
                .upperBound(5.0)
                .build();

        assertThat(integrator.getTargetFunction()).isEqualTo(f);
        assertThat(integrator.getLowerBound()).isEqualTo(0.0);
        assertThat(integrator.getUpperBound()).isEqualTo(5.0);
        assertThat(integrator.getIterations()).isEqualTo(1000);
    }

    @Test
    void builderAllowsSettingIterations() {
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.builder()
                .function(f)
                .lowerBound(0.0)
                .upperBound(5.0)
                .iterations(250)
                .build();

        assertThat(integrator.getIterations()).isEqualTo(250);
    }

    @Test
    void builderRejectsNullFunction() {
        assertThatThrownBy(() ->
                SimpsonIntegrator.builder()
                        .function(null)
                        .lowerBound(0.0)
                        .upperBound(1.0)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void builderRejectsInfiniteBounds() {
        Function f = new Function("x");

        assertThatThrownBy(() ->
                SimpsonIntegrator.builder()
                        .function(f)
                        .lowerBound(Double.POSITIVE_INFINITY)
                        .upperBound(1.0)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");

        assertThatThrownBy(() ->
                SimpsonIntegrator.builder()
                        .function(f)
                        .lowerBound(0.0)
                        .upperBound(Double.NEGATIVE_INFINITY)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");
    }

    @Test
    void builderRejectsNonPositiveIterations() {
        assertThatThrownBy(() ->
                SimpsonIntegrator.builder()
                        .function(new Function("x"))
                        .lowerBound(0.0)
                        .upperBound(1.0)
                        .iterations(0)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Iterations must be positive");
    }

    @Test
    void builderRequiresFunction() {
        assertThatThrownBy(() ->
                SimpsonIntegrator.builder()
                        .lowerBound(0.0)
                        .upperBound(1.0)
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Function must be set");
    }

    @Test
    void builderRequiresLowerBound() {
        assertThatThrownBy(() ->
                SimpsonIntegrator.builder()
                        .function(new Function("x"))
                        .upperBound(1.0)
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Lower bound must be set");
    }

    @Test
    void builderRequiresUpperBound() {
        assertThatThrownBy(() ->
                SimpsonIntegrator.builder()
                        .function(new Function("x"))
                        .lowerBound(0.0)
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Upper bound must be set");
    }

    // ==================== Property Tests ====================

    @Test
    void getTargetFunctionReturnsCorrectFunction() {
        Function f1 = new Function("x^2");
        Function f2 = new Function("sin(x)");

        SimpsonIntegrator integrator1 = SimpsonIntegrator.of(f1, 0.0, 1.0);
        SimpsonIntegrator integrator2 = SimpsonIntegrator.of(f2, 0.0, Math.PI);

        assertThat(integrator1.getTargetFunction().getEquation()).isEqualTo("x^2");
        assertThat(integrator2.getTargetFunction().getEquation()).isEqualTo("sin(x)");
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0",
            "-5.0, 5.0",
            "0.0, 10.0",
            "-2.5, 7.3"
    })
    void getBoundsReturnCorrectValues(double lower, double upper) {
        Function f = new Function("x");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, lower, upper);

        assertThat(integrator.getLowerBound()).isEqualTo(lower);
        assertThat(integrator.getUpperBound()).isEqualTo(upper);
    }

    @ParameterizedTest
    @CsvSource({
            "10",
            "100",
            "1000",
            "5000"
    })
    void getIterationsReturnsCorrectValue(int iterations) {
        Function f = new Function("x");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 1.0, iterations);

        assertThat(integrator.getIterations()).isEqualTo(iterations);
    }

    // ==================== Integration Accuracy Tests ====================

    @Test
    void integrateXSquaredFrom0To1Equals1Third() {
        // ∫₀¹ x² dx = [x³/3]₀¹ = 1/3 ≈ 0.333333
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 1.0, 100);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(1.0 / 3.0, within(TOLERANCE));
    }

    @Test
    void integrateXCubedFrom0To2Equals4() {
        // ∫₀² x³ dx = [x⁴/4]₀² = 16/4 = 4
        Function f = new Function("x^3");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 2.0, 100);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(4.0, within(TOLERANCE));
    }

    @Test
    void integrateSineFrom0ToPiEquals2() {
        // ∫₀ᵖⁱ sin(x) dx = [-cos(x)]₀ᵖⁱ = -cos(π) + cos(0) = 1 + 1 = 2
        Function f = new Function("sin(x)");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, Math.PI, 100);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void integratePolynomialFrom0To5() {
        // ∫₀⁵ (x² + 8x + 12) dx = [x³/3 + 4x² + 12x]₀⁵
        // = 125/3 + 100 + 60 = 41.667 + 100 + 60 = 201.667 (approximately)
        // Exact: 125/3 + 100 + 60 = 125/3 + 160 = (125 + 480)/3 = 605/3 ≈ 201.6667
        Function f = new Function("x^2 + 8*x + 12");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 5.0, 100);

        double result = integrator.integrate();
        double expected = 605.0 / 3.0;
        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateConstantFunction() {
        // ∫₁⁵ 7 dx = 7x|₁⁵ = 35 - 7 = 28
        Function f = new Function("7");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 1.0, 5.0, 50);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(28.0, within(TOLERANCE));
    }

    @Test
    void integrateLinearFunction() {
        // ∫₀¹⁰ 3x dx = [3x²/2]₀¹⁰ = 150
        Function f = new Function("3*x");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 10.0, 50);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(150.0, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0, 0.333333333",    // ∫₀¹ x² dx = 1/3
            "0.0, 2.0, 2.666666667",    // ∫₀² x² dx = 8/3
            "1.0, 3.0, 8.666666667",    // ∫₁³ x² dx = 26/3
            "-1.0, 1.0, 0.666666667"    // ∫₋₁¹ x² dx = 2/3
    })
    void integrateXSquaredOverDifferentRanges(double lower, double upper, double expected) {
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, lower, upper, 100);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void simpsonsRuleIsExactForLinearFunctions() {
        // Simpson's rule should be exact for polynomials up to degree 3
        // Linear: ∫₀⁵ (2x + 3) dx = [x² + 3x]₀⁵ = 25 + 15 = 40
        Function f = new Function("2*x + 3");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 5.0, 10);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(40.0, within(EXACT_TOLERANCE));
    }

    @Test
    void simpsonsRuleIsExactForQuadraticFunctions() {
        // Quadratic: ∫₁⁴ (x² - 2x + 1) dx = [x³/3 - x² + x]₁⁴
        // = (64/3 - 16 + 4) - (1/3 - 1 + 1) = 64/3 - 12 - 1/3 = 63/3 - 12 = 21 - 12 = 9
        Function f = new Function("x^2 - 2*x + 1");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 1.0, 4.0, 10);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(9.0, within(EXACT_TOLERANCE));
    }

    @Test
    void simpsonsRuleIsExactForCubicFunctions() {
        // Cubic: ∫₀² (x³ - 3x² + 2x + 1) dx = [x⁴/4 - x³ + x² + x]₀²
        // = (16/4 - 8 + 4 + 2) - 0 = 4 - 8 + 4 + 2 = 2
        Function f = new Function("x^3 - 3*x^2 + 2*x + 1");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 2.0, 10);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(2.0, within(EXACT_TOLERANCE));
    }

    @Test
    void integrateExponentialFunction() {
        // ∫₀¹ e^x dx = [e^x]₀¹ = e - 1 ≈ 1.71828
        // Using euler constant from evaluator
        Function f = new Function("euler^x");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 1.0, 200);

        double result = integrator.integrate();
        double expected = Math.E - 1.0;
        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateCosineFunction() {
        // ∫₀ᵖⁱ/² cos(x) dx = [sin(x)]₀ᵖⁱ/² = sin(π/2) - sin(0) = 1 - 0 = 1
        Function f = new Function("cos(x)");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, Math.PI / 2, 100);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(1.0, within(TOLERANCE));
    }

    @Test
    void integrateSquareRootFunction() {
        // ∫₀⁴ √x dx = [2x^(3/2)/3]₀⁴ = 2*8/3 = 16/3 ≈ 5.333
        // Using x^0.5 instead of sqrt(x)
        // Requires more iterations for good accuracy due to singularity at x=0
        Function f = new Function("x^0.5");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 4.0, 500);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(16.0 / 3.0, within(1e-4));
    }

    @Test
    void higherIterationsImproveAccuracy() {
        Function f = new Function("sin(x) * cos(x)");  // More complex function
        double lower = 0.0;
        double upper = Math.PI;

        // Integrate with different iteration counts
        SimpsonIntegrator integrator10 = SimpsonIntegrator.of(f, lower, upper, 10);
        SimpsonIntegrator integrator100 = SimpsonIntegrator.of(f, lower, upper, 100);
        SimpsonIntegrator integrator1000 = SimpsonIntegrator.of(f, lower, upper, 1000);

        double result10 = integrator10.integrate();
        double result100 = integrator100.integrate();
        double result1000 = integrator1000.integrate();

        // Results should converge (exact value is 0 for this integral)
        assertThat(Math.abs(result1000)).isLessThan(Math.abs(result10));
        assertThat(result1000).isCloseTo(0.0, within(1e-4));
    }

    @Test
    void integrateNegativeInterval() {
        // ∫₋₂⁰ x² dx = [x³/3]₋₂⁰ = 0 - (-8/3) = 8/3 ≈ 2.667
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, -2.0, 0.0, 100);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(8.0 / 3.0, within(TOLERANCE));
    }

    @Test
    void integrateNegativeFunctionValues() {
        // ∫₀ᵖⁱ -sin(x) dx = [cos(x)]₀ᵖⁱ = cos(π) - cos(0) = -1 - 1 = -2
        Function f = new Function("-sin(x)");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, Math.PI, 100);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(-2.0, within(TOLERANCE));
    }

    @Test
    void integrateMixedSignFunction() {
        // ∫₋₁¹ x dx = [x²/2]₋₁¹ = 1/2 - 1/2 = 0
        Function f = new Function("x");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, -1.0, 1.0, 50);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(0.0, within(TOLERANCE));
    }

    // ==================== Edge Case Tests ====================

    @Test
    void integrateVerySmallInterval() {
        // ∫₀⁰·⁰¹ x² dx = [x³/3]₀⁰·⁰¹ = 0.000001/3 ≈ 3.333e-7
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 0.01, 10);

        double result = integrator.integrate();
        double expected = 0.000001 / 3.0;
        assertThat(result).isCloseTo(expected, within(1e-9));
    }

    @Test
    void integrateVeryLargeInterval() {
        // ∫₀¹⁰⁰ x dx = [x²/2]₀¹⁰⁰ = 10000/2 = 5000
        Function f = new Function("x");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 100.0, 500);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(5000.0, within(0.01));
    }

    @Test
    void integrateFunctionWithZeroResult() {
        // ∫₀²ᵖⁱ sin(x) dx = [-cos(x)]₀²ᵖⁱ = -cos(2π) + cos(0) = -1 + 1 = 0
        Function f = new Function("sin(x)");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 2 * Math.PI, 100);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void integrateWithFewIterations() {
        // Even with just 1 iteration, should produce some result
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 1.0, 1);

        double result = integrator.integrate();
        // May not be very accurate but should be finite and positive
        assertThat(Double.isFinite(result)).isTrue();
        assertThat(result).isPositive();
    }

    @Test
    void integrateFunctionProducingInfinityThrowsException() {
        // 1/x produces division by zero at x=0
        // The rational arithmetic will throw IllegalArgumentException for zero denominator
        Function f = new Function("1/x");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 1.0, 100);

        assertThatThrownBy(integrator::integrate)
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Function evaluation at lower bound produced non-finite value: Infinity");
    }

    @Test
    void integrateFunctionProducingNaNThrowsException() {
        // x^0.5 when x is negative produces NaN
        Function f = new Function("x^0.5");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, -1.0, 1.0, 100);

        assertThatThrownBy(integrator::integrate)
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("non-finite");
    }

    // ==================== Immutability Tests ====================

    @Test
    void integratorIsImmutable() {
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 5.0, 100);

        double originalLower = integrator.getLowerBound();
        double originalUpper = integrator.getUpperBound();
        int originalIterations = integrator.getIterations();

        // Perform integration multiple times
        integrator.integrate();
        integrator.integrate();
        integrator.integrate();

        // Verify state hasn't changed
        assertThat(integrator.getLowerBound()).isEqualTo(originalLower);
        assertThat(integrator.getUpperBound()).isEqualTo(originalUpper);
        assertThat(integrator.getIterations()).isEqualTo(originalIterations);
    }

    @Test
    void multipleIntegrationsProduceSameResult() {
        Function f = new Function("sin(x)");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, Math.PI, 100);

        double result1 = integrator.integrate();
        double result2 = integrator.integrate();
        double result3 = integrator.integrate();

        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
    }

    // ==================== Equality and HashCode Tests ====================

    @Test
    void equalIntegratorsAreEqual() {
        Function f1 = new Function("x^2");
        Function f2 = new Function("x^2");

        SimpsonIntegrator integrator1 = SimpsonIntegrator.of(f1, 0.0, 5.0, 100);
        SimpsonIntegrator integrator2 = SimpsonIntegrator.of(f2, 0.0, 5.0, 100);

        assertThat(integrator1).isEqualTo(integrator2);
        assertThat(integrator1.hashCode()).isEqualTo(integrator2.hashCode());
    }

    @Test
    void integratorsWithDifferentFunctionsAreNotEqual() {
        Function f1 = new Function("x^2");
        Function f2 = new Function("x^3");

        SimpsonIntegrator integrator1 = SimpsonIntegrator.of(f1, 0.0, 5.0, 100);
        SimpsonIntegrator integrator2 = SimpsonIntegrator.of(f2, 0.0, 5.0, 100);

        assertThat(integrator1).isNotEqualTo(integrator2);
    }

    @Test
    void integratorsWithDifferentBoundsAreNotEqual() {
        Function f = new Function("x^2");

        SimpsonIntegrator integrator1 = SimpsonIntegrator.of(f, 0.0, 5.0, 100);
        SimpsonIntegrator integrator2 = SimpsonIntegrator.of(f, 0.0, 10.0, 100);
        SimpsonIntegrator integrator3 = SimpsonIntegrator.of(f, 1.0, 5.0, 100);

        assertThat(integrator1).isNotEqualTo(integrator2);
        assertThat(integrator1).isNotEqualTo(integrator3);
    }

    @Test
    void integratorsWithDifferentIterationsAreNotEqual() {
        Function f = new Function("x^2");

        SimpsonIntegrator integrator1 = SimpsonIntegrator.of(f, 0.0, 5.0, 100);
        SimpsonIntegrator integrator2 = SimpsonIntegrator.of(f, 0.0, 5.0, 200);

        assertThat(integrator1).isNotEqualTo(integrator2);
    }

    @Test
    void equalsHandlesNullAndDifferentTypes() {
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 5.0, 100);

        assertThat(integrator).isNotEqualTo(null);
        assertThat(integrator).isNotEqualTo("not an integrator");
        assertThat(integrator).isEqualTo(integrator);
    }

    @Test
    void hashCodeIsConsistent() {
        Function f = new Function("x^2");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 5.0, 100);

        int hash1 = integrator.hashCode();
        int hash2 = integrator.hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }

    // ==================== toString Tests ====================

    @Test
    void toStringContainsRelevantInformation() {
        Function f = new Function("x^2 + 3*x");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, 0.0, 5.0, 500);

        String str = integrator.toString();

        assertThat(str).contains("SimpsonIntegrator");
        assertThat(str).contains("x^2 + 3*x");
        assertThat(str).contains("0.0");
        assertThat(str).contains("5.0");
        assertThat(str).contains("500");
    }

    @Test
    void toStringShowsBounds() {
        Function f = new Function("sin(x)");
        SimpsonIntegrator integrator = SimpsonIntegrator.of(f, -2.5, 7.3, 100);

        String str = integrator.toString();

        assertThat(str).contains("-2.5");
        assertThat(str).contains("7.3");
    }
}
