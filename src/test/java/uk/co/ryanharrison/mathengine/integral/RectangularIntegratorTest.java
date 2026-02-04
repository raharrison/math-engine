package uk.co.ryanharrison.mathengine.integral;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.core.Function;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive test suite for {@link RectangularIntegrator}.
 * <p>
 * Tests all factory methods, builder pattern, integration accuracy for each position type,
 * validation behavior, immutability, and object methods (equals, hashCode, toString).
 * </p>
 */
class RectangularIntegratorTest {

    private static final double TOLERANCE = 1e-4;
    private static final double STRICT_TOLERANCE = 1e-6;
    private static final double RELAXED_TOLERANCE = 1e-3;  // For LEFT/RIGHT with fewer iterations

    // ==================== Construction (Factory Methods) ====================

    @Test
    void ofWithThreeParametersUsesDefaultsCorrectly() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0);

        assertThat(integrator.getTargetFunction()).isNotNull();
        assertThat(integrator.getLowerBound()).isEqualTo(0.0);
        assertThat(integrator.getUpperBound()).isEqualTo(1.0);
        assertThat(integrator.getIterations()).isEqualTo(1000); // DEFAULT_ITERATIONS
        assertThat(integrator.getPosition()).isEqualTo(RectanglePosition.MIDPOINT);
    }

    @Test
    void ofWithFourParametersUsesSpecifiedPosition() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, RectanglePosition.LEFT);

        assertThat(integrator.getPosition()).isEqualTo(RectanglePosition.LEFT);
        assertThat(integrator.getIterations()).isEqualTo(1000);
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT",
            "MIDPOINT",
            "RIGHT"
    })
    void ofWithFiveParametersUsesAllSpecified(RectanglePosition position) {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 500, position);

        assertThat(integrator.getIterations()).isEqualTo(500);
        assertThat(integrator.getPosition()).isEqualTo(position);
        assertThat(integrator.getLowerBound()).isEqualTo(0.0);
        assertThat(integrator.getUpperBound()).isEqualTo(1.0);
    }

    // ==================== Construction (Builder) ====================

    @Test
    void builderCreatesIntegratorWithAllParameters() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.builder()
                .function(f)
                .lowerBound(0.0)
                .upperBound(2.0)
                .iterations(500)
                .position(RectanglePosition.RIGHT)
                .build();

        assertThat(integrator.getLowerBound()).isEqualTo(0.0);
        assertThat(integrator.getUpperBound()).isEqualTo(2.0);
        assertThat(integrator.getIterations()).isEqualTo(500);
        assertThat(integrator.getPosition()).isEqualTo(RectanglePosition.RIGHT);
    }

    @Test
    void builderUsesDefaultsForOptionalParameters() {
        Function f = new Function("x");
        RectangularIntegrator integrator = RectangularIntegrator.builder()
                .function(f)
                .lowerBound(0.0)
                .upperBound(1.0)
                .build();

        assertThat(integrator.getIterations()).isEqualTo(1000);
        assertThat(integrator.getPosition()).isEqualTo(RectanglePosition.MIDPOINT);
    }

    @Test
    void builderThrowsWhenFunctionNotSet() {
        assertThatThrownBy(() -> RectangularIntegrator.builder()
                .lowerBound(0.0)
                .upperBound(1.0)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Function must be set");
    }

    @Test
    void builderThrowsWhenLowerBoundNotSet() {
        Function f = new Function("x");
        assertThatThrownBy(() -> RectangularIntegrator.builder()
                .function(f)
                .upperBound(1.0)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Lower bound must be set");
    }

    @Test
    void builderThrowsWhenUpperBoundNotSet() {
        Function f = new Function("x");
        assertThatThrownBy(() -> RectangularIntegrator.builder()
                .function(f)
                .lowerBound(0.0)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Upper bound must be set");
    }

    // ==================== Construction (Validation) ====================

    @Test
    void constructorRejectsNullFunction() {
        assertThatThrownBy(() -> RectangularIntegrator.of(null, 0.0, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target function cannot be null");
    }

    @Test
    void constructorRejectsNullPosition() {
        Function f = new Function("x");
        assertThatThrownBy(() -> RectangularIntegrator.of(f, 0.0, 1.0, 100, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rectangle position cannot be null");
    }

    @ParameterizedTest
    @CsvSource({
            "Infinity",
            "-Infinity",
            "NaN"
    })
    void constructorRejectsInfiniteLowerBound(String value) {
        Function f = new Function("x");
        double bound = Double.parseDouble(value);
        assertThatThrownBy(() -> RectangularIntegrator.of(f, bound, 1.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");
    }

    @ParameterizedTest
    @CsvSource({
            "Infinity",
            "-Infinity",
            "NaN"
    })
    void constructorRejectsInfiniteUpperBound(String value) {
        Function f = new Function("x");
        double bound = Double.parseDouble(value);
        assertThatThrownBy(() -> RectangularIntegrator.of(f, 0.0, bound))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");
    }

    @ParameterizedTest
    @CsvSource({
            "1.0, 1.0",     // Equal bounds
            "2.0, 1.0",     // Lower > upper
            "0.0, -1.0"     // Lower > upper (negative)
    })
    void constructorRejectsInvalidBoundOrder(double lower, double upper) {
        Function f = new Function("x");
        assertThatThrownBy(() -> RectangularIntegrator.of(f, lower, upper))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be less than upper bound");
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "-1",
            "-100"
    })
    void constructorRejectsNonPositiveIterations(int iterations) {
        Function f = new Function("x");
        assertThatThrownBy(() -> RectangularIntegrator.of(f, 0.0, 1.0, iterations, RectanglePosition.MIDPOINT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Iterations must be positive");
    }

    @Test
    void builderRejectsNullFunction() {
        assertThatThrownBy(() -> RectangularIntegrator.builder()
                .function(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Function cannot be null");
    }

    @Test
    void builderRejectsNullPosition() {
        assertThatThrownBy(() -> RectangularIntegrator.builder()
                .position(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Position cannot be null");
    }

    @Test
    void builderRejectsInfiniteLowerBound() {
        assertThatThrownBy(() -> RectangularIntegrator.builder()
                .lowerBound(Double.POSITIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lower bound must be finite");
    }

    @Test
    void builderRejectsInfiniteUpperBound() {
        assertThatThrownBy(() -> RectangularIntegrator.builder()
                .upperBound(Double.NEGATIVE_INFINITY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Upper bound must be finite");
    }

    @Test
    void builderRejectsNonPositiveIterations() {
        assertThatThrownBy(() -> RectangularIntegrator.builder()
                .iterations(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Iterations must be positive");
    }

    // ==================== Properties ====================

    @Test
    void gettersReturnCorrectValues() {
        Function f = new Function("x^2 + 3*x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 1.5, 4.5, 200, RectanglePosition.LEFT);

        assertThat(integrator.getTargetFunction()).isNotNull();
        assertThat(integrator.getTargetFunction().getEquation()).isEqualTo("x^2 + 3*x");
        assertThat(integrator.getLowerBound()).isEqualTo(1.5);
        assertThat(integrator.getUpperBound()).isEqualTo(4.5);
        assertThat(integrator.getIterations()).isEqualTo(200);
        assertThat(integrator.getPosition()).isEqualTo(RectanglePosition.LEFT);
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT, 0.0",
            "MIDPOINT, 0.5",
            "RIGHT, 1.0"
    })
    void positionReturnsCorrectValue(RectanglePosition position, double expectedOffset) {
        Function f = new Function("x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, position);

        assertThat(integrator.getPosition()).isEqualTo(position);
    }

    // ==================== RectanglePosition.getOffset() ====================

    @ParameterizedTest
    @CsvSource({
            "LEFT, 0.0",
            "MIDPOINT, 0.5",
            "RIGHT, 1.0"
    })
    void rectanglePositionGetOffsetReturnsCorrectValue(RectanglePosition position, double expectedOffset) {
        assertThat(position.getOffset()).isEqualTo(expectedOffset);
    }

    @Test
    void allRectanglePositionsHaveValidOffsets() {
        // Ensure all enum values have defined offsets
        assertThat(RectanglePosition.LEFT.getOffset()).isEqualTo(0.0);
        assertThat(RectanglePosition.MIDPOINT.getOffset()).isEqualTo(0.5);
        assertThat(RectanglePosition.RIGHT.getOffset()).isEqualTo(1.0);
    }

    // ==================== Integration Accuracy - LEFT Position ====================

    @Test
    void leftPositionIntegratesXSquaredCorrectly() {
        // ∫₀¹ x² dx = 1/3 = 0.333...
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 10000, RectanglePosition.LEFT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(1.0 / 3.0, within(TOLERANCE));
    }

    @Test
    void leftPositionIntegratesLinearFunctionCorrectly() {
        // ∫₀² x dx = 2
        Function f = new Function("x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 2.0, 10000, RectanglePosition.LEFT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(2.0, within(RELAXED_TOLERANCE));
    }

    @Test
    void leftPositionUnderestimatesIncreasingFunction() {
        // For monotonically increasing functions, LEFT should underestimate
        // ∫₀¹ x² dx = 1/3
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.LEFT);

        double result = integrator.integrate();
        double exactValue = 1.0 / 3.0;

        // LEFT should be less than exact for increasing function
        assertThat(result).isLessThan(exactValue);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0, 10000",      // Standard bounds, many iterations
            "0.0, 2.0, 50000",      // Wider interval needs more iterations
            "-1.0, 1.0, 10000"      // Negative to positive
    })
    void leftPositionIntegratesWithVariousBounds(double lower, double upper, int iterations) {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, lower, upper, iterations, RectanglePosition.LEFT);

        double result = integrator.integrate();

        // Calculate exact value: ∫ x² dx = x³/3
        double exactValue = (Math.pow(upper, 3) - Math.pow(lower, 3)) / 3.0;

        assertThat(result).isCloseTo(exactValue, within(TOLERANCE));
    }

    // ==================== Integration Accuracy - MIDPOINT Position ====================

    @Test
    void midpointPositionIntegratesXSquaredCorrectly() {
        // ∫₀¹ x² dx = 1/3 = 0.333...
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 10000, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(1.0 / 3.0, within(TOLERANCE));
    }

    @Test
    void midpointPositionIntegratesLinearFunctionExactly() {
        // ∫₀² x dx = 2 (should be exact for linear)
        Function f = new Function("x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 2.0, 100, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(2.0, within(STRICT_TOLERANCE));
    }

    @Test
    void midpointPositionIsMoreAccurateThanLeft() {
        // For same iterations, MIDPOINT should be closer to exact value than LEFT
        Function f = new Function("x^2");
        int iterations = 100;
        double exactValue = 1.0 / 3.0;

        RectangularIntegrator left = RectangularIntegrator.of(f, 0.0, 1.0, iterations, RectanglePosition.LEFT);
        RectangularIntegrator midpoint = RectangularIntegrator.of(f, 0.0, 1.0, iterations, RectanglePosition.MIDPOINT);

        double leftError = Math.abs(left.integrate() - exactValue);
        double midpointError = Math.abs(midpoint.integrate() - exactValue);

        assertThat(midpointError).isLessThan(leftError);
    }

    @Test
    void midpointPositionIsMoreAccurateThanRight() {
        // For same iterations, MIDPOINT should be closer to exact value than RIGHT
        Function f = new Function("x^2");
        int iterations = 100;
        double exactValue = 1.0 / 3.0;

        RectangularIntegrator right = RectangularIntegrator.of(f, 0.0, 1.0, iterations, RectanglePosition.RIGHT);
        RectangularIntegrator midpoint = RectangularIntegrator.of(f, 0.0, 1.0, iterations, RectanglePosition.MIDPOINT);

        double rightError = Math.abs(right.integrate() - exactValue);
        double midpointError = Math.abs(midpoint.integrate() - exactValue);

        assertThat(midpointError).isLessThan(rightError);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0",       // Unit interval
            "0.0, 2.0",       // [0, 2]
            "-1.0, 1.0",      // Symmetric around zero
            "1.0, 5.0"        // Positive range
    })
    void midpointPositionIntegratesXSquaredForVariousBounds(double lower, double upper) {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, lower, upper, 10000, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        double exactValue = (Math.pow(upper, 3) - Math.pow(lower, 3)) / 3.0;

        assertThat(result).isCloseTo(exactValue, within(TOLERANCE));
    }

    @Test
    void midpointPositionIntegratesSineFunction() {
        // ∫₀ᵖⁱ sin(x) dx = 2
        Function f = new Function("sin(x)");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, Math.PI, 10000, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void midpointPositionIntegratesConstantFunction() {
        // ∫₀⁵ 3 dx = 15
        Function f = new Function("3");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 5.0, 100, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(15.0, within(STRICT_TOLERANCE));
    }

    // ==================== Integration Accuracy - RIGHT Position ====================

    @Test
    void rightPositionIntegratesXSquaredCorrectly() {
        // ∫₀¹ x² dx = 1/3 = 0.333...
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 10000, RectanglePosition.RIGHT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(1.0 / 3.0, within(TOLERANCE));
    }

    @Test
    void rightPositionIntegratesLinearFunctionCorrectly() {
        // ∫₀² x dx = 2
        Function f = new Function("x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 2.0, 10000, RectanglePosition.RIGHT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(2.0, within(RELAXED_TOLERANCE));
    }

    @Test
    void rightPositionOverestimatesIncreasingFunction() {
        // For monotonically increasing functions, RIGHT should overestimate
        // ∫₀¹ x² dx = 1/3
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.RIGHT);

        double result = integrator.integrate();
        double exactValue = 1.0 / 3.0;

        // RIGHT should be greater than exact for increasing function
        assertThat(result).isGreaterThan(exactValue);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0, 10000",
            "0.0, 2.0, 50000",
            "-2.0, 2.0, 10000"
    })
    void rightPositionIntegratesWithVariousBounds(double lower, double upper, int iterations) {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, lower, upper, iterations, RectanglePosition.RIGHT);

        double result = integrator.integrate();
        double exactValue = (Math.pow(upper, 3) - Math.pow(lower, 3)) / 3.0;

        assertThat(result).isCloseTo(exactValue, within(TOLERANCE));
    }

    // ==================== Integration Accuracy - Comparison Tests ====================

    @Test
    void allPositionsIntegrateLinearFunctionCorrectly() {
        // Linear functions should be integrated accurately by all positions
        Function f = new Function("2*x + 1");
        double lower = 0.0;
        double upper = 3.0;
        int iterations = 10000;

        // Exact: ∫₀³ (2x + 1) dx = [x² + x]₀³ = 9 + 3 = 12
        double exactValue = 12.0;

        RectangularIntegrator left = RectangularIntegrator.of(f, lower, upper, iterations, RectanglePosition.LEFT);
        RectangularIntegrator midpoint = RectangularIntegrator.of(f, lower, upper, iterations, RectanglePosition.MIDPOINT);
        RectangularIntegrator right = RectangularIntegrator.of(f, lower, upper, iterations, RectanglePosition.RIGHT);

        assertThat(left.integrate()).isCloseTo(exactValue, within(RELAXED_TOLERANCE));
        assertThat(midpoint.integrate()).isCloseTo(exactValue, within(STRICT_TOLERANCE));
        assertThat(right.integrate()).isCloseTo(exactValue, within(RELAXED_TOLERANCE));
    }

    @Test
    void leftAndRightBracketExactValue() {
        // For monotonic functions, LEFT and RIGHT should bracket the exact value
        Function f = new Function("x^2");
        int iterations = 100;
        double exactValue = 1.0 / 3.0;

        double leftResult = RectangularIntegrator.of(f, 0.0, 1.0, iterations, RectanglePosition.LEFT).integrate();
        double rightResult = RectangularIntegrator.of(f, 0.0, 1.0, iterations, RectanglePosition.RIGHT).integrate();

        // For increasing function: LEFT < exact < RIGHT
        assertThat(leftResult).isLessThan(exactValue);
        assertThat(rightResult).isGreaterThan(exactValue);
    }

    // ==================== Edge Cases ====================

    @Test
    void integrateWithVerySmallInterval() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 0.001, 100, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        double exactValue = Math.pow(0.001, 3) / 3.0;

        assertThat(result).isCloseTo(exactValue, within(1e-10));
    }

    @Test
    void integrateWithLargeInterval() {
        Function f = new Function("x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1000.0, 10000, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        // ∫₀¹⁰⁰⁰ x dx = 500000
        assertThat(result).isCloseTo(500000.0, within(1.0));
    }

    @Test
    void integrateWithNegativeBounds() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, -2.0, -1.0, 5000, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        // ∫₋₂⁻¹ x² dx = [-8/3] - [-1/3] = -8/3 + 1/3 = -7/3
        double exactValue = (-1.0 / 3.0) - (-8.0 / 3.0);

        assertThat(result).isCloseTo(exactValue, within(TOLERANCE));
    }

    @Test
    void integrateWithFewIterations() {
        Function f = new Function("x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 1, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        // With 1 iteration and midpoint, should evaluate at x=0.5
        // Result = 1 * f(0.5) = 0.5
        assertThat(result).isCloseTo(0.5, within(STRICT_TOLERANCE));
    }

    @Test
    void integrateZeroFunction() {
        Function f = new Function("0");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 10.0, 100, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(0.0, within(STRICT_TOLERANCE));
    }

    @Test
    void integrateNegativeFunction() {
        // ∫₀¹ -x dx = -0.5
        Function f = new Function("-x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 5000, RectanglePosition.MIDPOINT);

        double result = integrator.integrate();
        assertThat(result).isCloseTo(-0.5, within(TOLERANCE));
    }

    @Test
    void integrateThrowsOnNonFiniteFunctionValue() {
        Function f = new Function("ln(x)");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.LEFT);

        // Function evaluates to -infinity at x=0 (left endpoint with ln(0))
        assertThatThrownBy(() -> integrator.integrate())
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Function evaluation produced non-finite value at x = 0.0: NaN");
    }

    // ==================== Immutability ====================

    @Test
    void integratorIsImmutable() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);

        double originalLower = integrator.getLowerBound();
        double originalUpper = integrator.getUpperBound();
        int originalIterations = integrator.getIterations();
        RectanglePosition originalPosition = integrator.getPosition();

        // Perform integration multiple times
        integrator.integrate();
        integrator.integrate();
        integrator.integrate();

        // Verify state unchanged
        assertThat(integrator.getLowerBound()).isEqualTo(originalLower);
        assertThat(integrator.getUpperBound()).isEqualTo(originalUpper);
        assertThat(integrator.getIterations()).isEqualTo(originalIterations);
        assertThat(integrator.getPosition()).isEqualTo(originalPosition);
    }

    @Test
    void multipleIntegrationCallsProduceSameResult() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 1000, RectanglePosition.MIDPOINT);

        double result1 = integrator.integrate();
        double result2 = integrator.integrate();
        double result3 = integrator.integrate();

        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
    }

    // ==================== Equality and hashCode ====================

    @Test
    void equalIntegratorsAreEqual() {
        Function f1 = new Function("x^2");
        Function f2 = new Function("x^2");

        RectangularIntegrator integrator1 = RectangularIntegrator.of(f1, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);
        RectangularIntegrator integrator2 = RectangularIntegrator.of(f2, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);

        assertThat(integrator1).isEqualTo(integrator2);
        assertThat(integrator1.hashCode()).isEqualTo(integrator2.hashCode());
    }

    @Test
    void integratorsWithDifferentFunctionsAreNotEqual() {
        Function f1 = new Function("x^2");
        Function f2 = new Function("x^3");

        RectangularIntegrator integrator1 = RectangularIntegrator.of(f1, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);
        RectangularIntegrator integrator2 = RectangularIntegrator.of(f2, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);

        assertThat(integrator1).isNotEqualTo(integrator2);
    }

    @Test
    void integratorsWithDifferentBoundsAreNotEqual() {
        Function f = new Function("x^2");

        RectangularIntegrator integrator1 = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);
        RectangularIntegrator integrator2 = RectangularIntegrator.of(f, 0.0, 2.0, 100, RectanglePosition.MIDPOINT);

        assertThat(integrator1).isNotEqualTo(integrator2);
    }

    @Test
    void integratorsWithDifferentIterationsAreNotEqual() {
        Function f = new Function("x^2");

        RectangularIntegrator integrator1 = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);
        RectangularIntegrator integrator2 = RectangularIntegrator.of(f, 0.0, 1.0, 200, RectanglePosition.MIDPOINT);

        assertThat(integrator1).isNotEqualTo(integrator2);
    }

    @Test
    void integratorsWithDifferentPositionsAreNotEqual() {
        Function f = new Function("x^2");

        RectangularIntegrator integrator1 = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.LEFT);
        RectangularIntegrator integrator2 = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.RIGHT);

        assertThat(integrator1).isNotEqualTo(integrator2);
    }

    @Test
    void equalsHandlesSameInstance() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);

        assertThat(integrator).isEqualTo(integrator);
    }

    @Test
    void equalsHandlesNull() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);

        assertThat(integrator).isNotEqualTo(null);
    }

    @Test
    void equalsHandlesDifferentClass() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);

        assertThat(integrator).isNotEqualTo("not an integrator");
    }

    @Test
    void hashCodeIsConsistent() {
        Function f = new Function("x^2");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, RectanglePosition.MIDPOINT);

        int hash1 = integrator.hashCode();
        int hash2 = integrator.hashCode();

        assertThat(hash1).isEqualTo(hash2);
    }

    // ==================== toString ====================

    @Test
    void toStringContainsAllRelevantInformation() {
        Function f = new Function("x^2 + 3*x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 1.5, 4.5, 200, RectanglePosition.LEFT);

        String result = integrator.toString();

        assertThat(result).contains("RectangularIntegrator");
        assertThat(result).contains("x^2 + 3*x");
        assertThat(result).contains("1.5");
        assertThat(result).contains("4.5");
        assertThat(result).contains("200");
        assertThat(result).contains("LEFT");
    }

    @ParameterizedTest
    @CsvSource({
            "LEFT",
            "MIDPOINT",
            "RIGHT"
    })
    void toStringIncludesPosition(RectanglePosition position) {
        Function f = new Function("x");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 1.0, 100, position);

        String result = integrator.toString();

        assertThat(result).contains(position.toString());
    }

    @Test
    void toStringFormatIsConsistent() {
        Function f = new Function("sin(x)");
        RectangularIntegrator integrator = RectangularIntegrator.of(f, 0.0, 3.14159, 1000, RectanglePosition.MIDPOINT);

        String result = integrator.toString();

        // Verify expected format: RectangularIntegrator(f=..., bounds=[..., ...], iterations=..., position=...)
        assertThat(result).matches("RectangularIntegrator\\(f=.+, bounds=\\[.+, .+\\], iterations=\\d+, position=.+\\)");
    }
}
