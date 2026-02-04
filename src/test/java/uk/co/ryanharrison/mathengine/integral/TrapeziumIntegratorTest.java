package uk.co.ryanharrison.mathengine.integral;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.ryanharrison.mathengine.core.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for {@link TrapeziumIntegrator}.
 */
class TrapeziumIntegratorTest {

    private static final double TOLERANCE = 1e-4;
    private static final int HIGH_ITERATIONS = 10000;

    // ==================== Construction - Factory Methods ====================

    @Test
    void ofWith3ParamsCreatesIntegratorWithDefaultIterations() {
        Function f = new Function("x^2");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 1.0);

        assertThat(integrator.getTargetFunction()).isNotNull();
        assertThat(integrator.getLowerBound()).isEqualTo(0.0);
        assertThat(integrator.getUpperBound()).isEqualTo(1.0);
        assertThat(integrator.getIterations()).isEqualTo(1000);
    }

    @Test
    void ofWith4ParamsCreatesIntegratorWithSpecifiedIterations() {
        Function f = new Function("x^3");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 2.0, 500);

        assertThat(integrator.getTargetFunction()).isNotNull();
        assertThat(integrator.getLowerBound()).isEqualTo(0.0);
        assertThat(integrator.getUpperBound()).isEqualTo(2.0);
        assertThat(integrator.getIterations()).isEqualTo(500);
    }

    @Test
    void ofRejectsNullFunction() {
        assertThatThrownBy(() -> TrapeziumIntegrator.of(null, 0.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");

        assertThatThrownBy(() -> TrapeziumIntegrator.of(null, 0.0, 1.0, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN})
    void ofRejectsNonFiniteLowerBound(double invalidBound) {
        Function f = new Function("x");

        assertThatThrownBy(() -> TrapeziumIntegrator.of(f, invalidBound, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");

        assertThatThrownBy(() -> TrapeziumIntegrator.of(f, invalidBound, 1.0, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN})
    void ofRejectsNonFiniteUpperBound(double invalidBound) {
        Function f = new Function("x");

        assertThatThrownBy(() -> TrapeziumIntegrator.of(f, 0.0, invalidBound))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");

        assertThatThrownBy(() -> TrapeziumIntegrator.of(f, 0.0, invalidBound, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");
    }

    @ParameterizedTest
    @CsvSource({
            "1.0, 1.0",     // equal bounds
            "2.0, 1.0",     // lower > upper
            "5.0, -5.0"     // lower > upper
    })
    void ofRejectsInvalidBoundOrdering(double lower, double upper) {
        Function f = new Function("x");

        assertThatThrownBy(() -> TrapeziumIntegrator.of(f, lower, upper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be less than upper bound");

        assertThatThrownBy(() -> TrapeziumIntegrator.of(f, lower, upper, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be less than upper bound");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void ofRejectsNonPositiveIterations(int invalidIterations) {
        Function f = new Function("x");

        assertThatThrownBy(() -> TrapeziumIntegrator.of(f, 0.0, 1.0, invalidIterations))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Iterations must be positive");
    }

    // ==================== Construction - Builder Pattern ====================

    @Test
    void builderCreatesValidIntegrator() {
        Function f = new Function("x^2");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.builder()
                .function(f)
                .lowerBound(0.0)
                .upperBound(5.0)
                .iterations(500)
                .build();

        assertThat(integrator.getTargetFunction()).isNotNull();
        assertThat(integrator.getLowerBound()).isEqualTo(0.0);
        assertThat(integrator.getUpperBound()).isEqualTo(5.0);
        assertThat(integrator.getIterations()).isEqualTo(500);
    }

    @Test
    void builderUsesDefaultIterationsWhenNotSpecified() {
        Function f = new Function("x");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.builder()
                .function(f)
                .lowerBound(0.0)
                .upperBound(1.0)
                .build();

        assertThat(integrator.getIterations()).isEqualTo(1000);
    }

    @Test
    void builderSupportsMethodChaining() {
        Function f = new Function("x^2");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.builder()
                .function(f)
                .lowerBound(0.0)
                .upperBound(1.0)
                .iterations(200)
                .build();

        assertThat(integrator).isNotNull();
    }

    @Test
    void builderRejectsNullFunction() {
        assertThatThrownBy(() ->
                TrapeziumIntegrator.builder().function(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN})
    void builderRejectsNonFiniteLowerBound(double invalidBound) {
        assertThatThrownBy(() ->
                TrapeziumIntegrator.builder().lowerBound(invalidBound))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN})
    void builderRejectsNonFiniteUpperBound(double invalidBound) {
        assertThatThrownBy(() ->
                TrapeziumIntegrator.builder().upperBound(invalidBound))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void builderRejectsNonPositiveIterations(int invalidIterations) {
        assertThatThrownBy(() ->
                TrapeziumIntegrator.builder().iterations(invalidIterations))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Iterations must be positive");
    }

    @Test
    void builderThrowsWhenFunctionNotSet() {
        assertThatThrownBy(() ->
                TrapeziumIntegrator.builder()
                        .lowerBound(0.0)
                        .upperBound(1.0)
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Function must be set");
    }

    @Test
    void builderThrowsWhenLowerBoundNotSet() {
        Function f = new Function("x");
        assertThatThrownBy(() ->
                TrapeziumIntegrator.builder()
                        .function(f)
                        .upperBound(1.0)
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Lower bound must be set");
    }

    @Test
    void builderThrowsWhenUpperBoundNotSet() {
        Function f = new Function("x");
        assertThatThrownBy(() ->
                TrapeziumIntegrator.builder()
                        .function(f)
                        .lowerBound(0.0)
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Upper bound must be set");
    }

    // ==================== Properties (Getters) ====================

    @Test
    void gettersReturnCorrectValues() {
        Function f = new Function("x^2 + 8*x + 12");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.5, 5.0, 250);

        assertThat(integrator.getTargetFunction().getEquation()).isEqualTo("x^2 + 8*x + 12");
        assertThat(integrator.getLowerBound()).isEqualTo(0.5);
        assertThat(integrator.getUpperBound()).isEqualTo(5.0);
        assertThat(integrator.getIterations()).isEqualTo(250);
    }

    // ==================== Integration Accuracy - Known Integrals ====================

    @Test
    void integrateXSquaredFrom0To1Equals1Over3() {
        // ∫₀¹ x² dx = [x³/3]₀¹ = 1/3 ≈ 0.333333
        Function f = new Function("x^2");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 1.0, HIGH_ITERATIONS);

        double result = integrator.integrate();
        double expected = 1.0 / 3.0;

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateXCubedFrom0To2Equals4() {
        // ∫₀² x³ dx = [x⁴/4]₀² = 16/4 = 4
        Function f = new Function("x^3");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 2.0, HIGH_ITERATIONS);

        double result = integrator.integrate();
        double expected = 4.0;

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateSineFrom0ToPiEquals2() {
        // ∫₀ᵖⁱ sin(x) dx = [-cos(x)]₀ᵖⁱ = -cos(π) + cos(0) = 1 + 1 = 2
        Function f = new Function("sin(x)");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, Math.PI, HIGH_ITERATIONS);

        double result = integrator.integrate();
        double expected = 2.0;

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateQuadraticFrom0To5() {
        // ∫₀⁵ (x² + 8x + 12) dx = [x³/3 + 4x² + 12x]₀⁵
        // = 125/3 + 100 + 60 = 41.667 + 100 + 60 = 201.667
        Function f = new Function("x^2 + 8*x + 12");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 5.0, HIGH_ITERATIONS);

        double result = integrator.integrate();
        double expected = 125.0 / 3.0 + 100.0 + 60.0;

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateConstantFunction() {
        // ∫₂⁷ 5 dx = 5x|₂⁷ = 35 - 10 = 25
        Function f = new Function("5");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 2.0, 7.0, 100);

        double result = integrator.integrate();
        double expected = 25.0;

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateLinearFunction() {
        // ∫₁⁴ 2x dx = [x²]₁⁴ = 16 - 1 = 15
        Function f = new Function("2*x");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 1.0, 4.0, 1000);

        double result = integrator.integrate();
        double expected = 15.0;

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateCosineFunction() {
        // ∫₀^(π/2) cos(x) dx = [sin(x)]₀^(π/2) = 1 - 0 = 1
        Function f = new Function("cos(x)");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, Math.PI / 2.0, HIGH_ITERATIONS);

        double result = integrator.integrate();
        double expected = 1.0;

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integratePolynomialFunction() {
        // ∫₁³ (x^4 - 2x + 1) dx = [x^5/5 - x^2 + x]₁³
        // = (243/5 - 9 + 3) - (1/5 - 1 + 1) = 48.6 - 6 - 0.2 = 42.4
        Function f = new Function("x^4 - 2*x + 1");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 1.0, 3.0, HIGH_ITERATIONS);

        double result = integrator.integrate();
        double expected = (243.0 / 5.0 - 9.0 + 3.0) - (1.0 / 5.0 - 1.0 + 1.0);

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0, 0.333333",      // ∫₀¹ x² dx
            "1.0, 2.0, 2.333333",      // ∫₁² x² dx = [x³/3]₁² = 8/3 - 1/3 = 7/3
            "-1.0, 1.0, 0.666667",     // ∫₋₁¹ x² dx = [x³/3]₋₁¹ = 1/3 - (-1/3) = 2/3
            "0.0, 2.0, 2.666667"       // ∫₀² x² dx = [x³/3]₀² = 8/3
    })
    void integrateXSquaredWithDifferentBounds(double lower, double upper, double expected) {
        Function f = new Function("x^2");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, lower, upper, HIGH_ITERATIONS);

        double result = integrator.integrate();

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    // ==================== Integration Accuracy - Convergence ====================

    @Test
    void higherIterationsImproveAccuracy() {
        Function f = new Function("x^2");
        double expected = 1.0 / 3.0;

        TrapeziumIntegrator lowIter = TrapeziumIntegrator.of(f, 0.0, 1.0, 10);
        TrapeziumIntegrator midIter = TrapeziumIntegrator.of(f, 0.0, 1.0, 100);
        TrapeziumIntegrator highIter = TrapeziumIntegrator.of(f, 0.0, 1.0, 1000);

        double errorLow = Math.abs(lowIter.integrate() - expected);
        double errorMid = Math.abs(midIter.integrate() - expected);
        double errorHigh = Math.abs(highIter.integrate() - expected);

        assertThat(errorMid).isLessThan(errorLow);
        assertThat(errorHigh).isLessThan(errorMid);
    }

    // ==================== Edge Cases ====================

    @Test
    void integrateVerySmallInterval() {
        Function f = new Function("x^2");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 0.001, 100);

        double result = integrator.integrate();

        // Result should be very small but positive
        assertThat(result).isGreaterThan(0.0);
        assertThat(result).isLessThan(0.001);
    }

    @Test
    void integrateVeryLargeInterval() {
        Function f = new Function("x");
        // ∫₀¹⁰⁰⁰ x dx = [x²/2]₀¹⁰⁰⁰ = 500000
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 1000.0, HIGH_ITERATIONS);

        double result = integrator.integrate();
        double expected = 500000.0;

        assertThat(result).isCloseTo(expected, within(1.0));  // Relaxed tolerance for large values
    }

    @Test
    void integrateNegativeInterval() {
        Function f = new Function("x^2");
        // ∫₋₂⁰ x² dx = [x³/3]₋₂⁰ = 0 - (-8/3) = 8/3
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, -2.0, 0.0, HIGH_ITERATIONS);

        double result = integrator.integrate();
        double expected = 8.0 / 3.0;

        assertThat(result).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void integrateFunctionWithNegativeValues() {
        Function f = new Function("x");
        // ∫₋₁¹ x dx = [x²/2]₋₁¹ = 1/2 - 1/2 = 0
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, -1.0, 1.0, HIGH_ITERATIONS);

        double result = integrator.integrate();

        assertThat(result).isCloseTo(0.0, within(TOLERANCE));
    }

    @Test
    void integrateFunctionProducingNaNThrowsException() {
        Function f = new Function("log(x)");  // log of negative is NaN
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, -1.0, 1.0, 100);

        assertThatThrownBy(() -> integrator.integrate())
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Function evaluation produced non-finite value at x = -0.98: NaN");
    }

    @Test
    void integrateFunctionWithNaNAtBoundaryThrowsException() {
        Function f = new Function("log(x)");  // log(0) is -infinity
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 1.0, 10);

        assertThatThrownBy(() -> integrator.integrate())
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Function evaluation at lower bound produced non-finite value: NaN");
    }

    // ==================== Equality and hashCode ====================

    @Test
    void equalIntegratorsAreEqual() {
        Function f1 = new Function("x^2");
        Function f2 = new Function("x^2");

        TrapeziumIntegrator int1 = TrapeziumIntegrator.of(f1, 0.0, 1.0, 100);
        TrapeziumIntegrator int2 = TrapeziumIntegrator.of(f2, 0.0, 1.0, 100);

        assertThat(int1).isEqualTo(int2);
        assertThat(int1.hashCode()).isEqualTo(int2.hashCode());
    }

    @Test
    void differentFunctionsAreNotEqual() {
        Function f1 = new Function("x^2");
        Function f2 = new Function("x^3");

        TrapeziumIntegrator int1 = TrapeziumIntegrator.of(f1, 0.0, 1.0, 100);
        TrapeziumIntegrator int2 = TrapeziumIntegrator.of(f2, 0.0, 1.0, 100);

        assertThat(int1).isNotEqualTo(int2);
    }

    @Test
    void differentBoundsAreNotEqual() {
        Function f = new Function("x^2");

        TrapeziumIntegrator int1 = TrapeziumIntegrator.of(f, 0.0, 1.0, 100);
        TrapeziumIntegrator int2 = TrapeziumIntegrator.of(f, 0.0, 2.0, 100);
        TrapeziumIntegrator int3 = TrapeziumIntegrator.of(f, 1.0, 2.0, 100);

        assertThat(int1).isNotEqualTo(int2);
        assertThat(int1).isNotEqualTo(int3);
        assertThat(int2).isNotEqualTo(int3);
    }

    @Test
    void differentIterationsAreNotEqual() {
        Function f = new Function("x^2");

        TrapeziumIntegrator int1 = TrapeziumIntegrator.of(f, 0.0, 1.0, 100);
        TrapeziumIntegrator int2 = TrapeziumIntegrator.of(f, 0.0, 1.0, 200);

        assertThat(int1).isNotEqualTo(int2);
    }

    @Test
    void equalsHandlesNullAndDifferentTypes() {
        Function f = new Function("x^2");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 1.0, 100);

        assertThat(integrator).isNotEqualTo(null);
        assertThat(integrator).isNotEqualTo("not an integrator");
        assertThat(integrator).isEqualTo(integrator);
    }

    // ==================== toString ====================

    @Test
    void toStringContainsRelevantInformation() {
        Function f = new Function("x^2 + 8*x + 12");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.5, 5.0, 250);

        String str = integrator.toString();

        assertThat(str).contains("TrapeziumIntegrator");
        assertThat(str).contains("x^2 + 8*x + 12");
        assertThat(str).contains("0.5");
        assertThat(str).contains("5.0");
        assertThat(str).contains("250");
    }

    @Test
    void toStringFormattedCorrectly() {
        Function f = new Function("sin(x)");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, Math.PI, 1000);

        String str = integrator.toString();

        assertThat(str).matches(".*TrapeziumIntegrator.*");
        assertThat(str).contains("sin(x)");
        assertThat(str).contains("bounds=");
        assertThat(str).contains("iterations=");
    }

    // ==================== Immutability ====================

    @Test
    void integratorIsImmutable() {
        Function f = new Function("x^2");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 1.0, 100);

        double originalLower = integrator.getLowerBound();
        double originalUpper = integrator.getUpperBound();
        int originalIterations = integrator.getIterations();
        String originalEquation = integrator.getTargetFunction().getEquation();

        // Perform integration multiple times
        integrator.integrate();
        integrator.integrate();
        integrator.integrate();

        // Verify state hasn't changed
        assertThat(integrator.getLowerBound()).isEqualTo(originalLower);
        assertThat(integrator.getUpperBound()).isEqualTo(originalUpper);
        assertThat(integrator.getIterations()).isEqualTo(originalIterations);
        assertThat(integrator.getTargetFunction().getEquation()).isEqualTo(originalEquation);
    }

    @Test
    void integrateProducesSameResultOnRepeatedCalls() {
        Function f = new Function("x^3 + 2*x");
        TrapeziumIntegrator integrator = TrapeziumIntegrator.of(f, 0.0, 5.0, HIGH_ITERATIONS);

        double result1 = integrator.integrate();
        double result2 = integrator.integrate();
        double result3 = integrator.integrate();

        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
    }
}
