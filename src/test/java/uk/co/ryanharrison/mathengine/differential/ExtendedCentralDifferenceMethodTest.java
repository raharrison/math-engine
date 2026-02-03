package uk.co.ryanharrison.mathengine.differential;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.core.Function;

import static org.assertj.core.api.Assertions.*;

class ExtendedCentralDifferenceMethodTest {
    private static final double TOLERANCE = 1e-6;  // Tighter tolerance due to higher accuracy

    // ==================== Construction Tests ====================

    @Test
    void constructWithBuilder() {
        Function f = new Function("x^2");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.getTargetFunction()).isEqualTo(f);
        assertThat(method.getTargetPoint()).isEqualTo(2.0);
        assertThat(method.getStepSize()).isEqualTo(0.01);
    }

    @Test
    void constructWithOfMethod() {
        Function f = new Function("x^3");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.of(f);

        assertThat(method.getTargetFunction()).isEqualTo(f);
        assertThat(method.getTargetPoint()).isEqualTo(1.0);
        assertThat(method.getStepSize()).isEqualTo(0.01);
    }

    @Test
    void constructWithStepSize() {
        Function f = new Function("x^2");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.of(f, 0.001);

        assertThat(method.getStepSize()).isEqualTo(0.001);
        assertThat(method.getTargetPoint()).isEqualTo(1.0);
    }

    @Test
    void builderRequiresTargetFunction() {
        assertThatThrownBy(() -> ExtendedCentralDifferenceMethod.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Target function must be set");
    }

    @Test
    void builderRejectsNullFunction() {
        assertThatThrownBy(() -> ExtendedCentralDifferenceMethod.builder()
                .targetFunction(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Target function cannot be null");
    }

    @Test
    void builderRejectsNonPositiveStepSize() {
        Function f = new Function("x^2");

        assertThatThrownBy(() -> ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .stepSize(0.0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Step size must be positive");

        assertThatThrownBy(() -> ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .stepSize(-0.01)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Step size must be positive");
    }

    @Test
    void builderRejectsNonFiniteTargetPoint() {
        Function f = new Function("x^2");

        assertThatThrownBy(() -> ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(Double.NaN)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target point must be finite");

        assertThatThrownBy(() -> ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(Double.NEGATIVE_INFINITY)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target point must be finite");
    }

    // ==================== First Derivative Tests ====================

    @ParameterizedTest
    @CsvSource({
            "1.0, 2.0",     // f(x) = x^2, f'(x) = 2x, at x=1
            "2.0, 4.0",     // at x=2
            "3.0, 6.0",     // at x=3
            "-1.0, -2.0",   // at x=-1
    })
    void firstDerivativeOfQuadratic(double x, double expected) {
        Function f = new Function("x^2");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveFirst()).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void firstDerivativeOfCubic() {
        // f(x) = x^3, f'(x) = 3x^2
        Function f = new Function("x^3");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveFirst()).isCloseTo(12.0, within(TOLERANCE));
    }

    @Test
    void firstDerivativeOfQuartic() {
        // f(x) = x^4, f'(x) = 4x^3
        Function f = new Function("x^4");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveFirst()).isCloseTo(32.0, within(TOLERANCE));
    }

    // ==================== Second Derivative Tests ====================

    @Test
    void secondDerivativeOfQuadratic() {
        // f(x) = x^2, f''(x) = 2
        Function f = new Function("x^2");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveSecond()).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void secondDerivativeOfCubic() {
        // f(x) = x^3, f''(x) = 6x
        Function f = new Function("x^3");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveSecond()).isCloseTo(12.0, within(TOLERANCE));
    }

    @Test
    void secondDerivativeOfQuartic() {
        // f(x) = x^4, f''(x) = 12x^2
        Function f = new Function("x^4");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveSecond()).isCloseTo(48.0, within(TOLERANCE));
    }

    // ==================== Third Derivative Tests ====================

    @Test
    void thirdDerivativeOfCubic() {
        // f(x) = x^3, f'''(x) = 6
        Function f = new Function("x^3");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveThird()).isCloseTo(6.0, within(TOLERANCE));
    }

    @Test
    void thirdDerivativeOfQuartic() {
        // f(x) = x^4, f'''(x) = 24x
        Function f = new Function("x^4");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveThird()).isCloseTo(48.0, within(TOLERANCE));
    }

    // ==================== Fourth Derivative Tests ====================

    @Test
    void fourthDerivativeOfQuartic() {
        // f(x) = x^4, f''''(x) = 24
        Function f = new Function("x^4");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method.deriveFourth()).isCloseTo(24.0, within(1E-5));
    }

    @Test
    void fourthDerivativeOfQuintic() {
        // f(x) = x^5, f''''(x) = 120x
        Function f = new Function("x^5");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        // Fourth derivatives have larger numerical errors
        assertThat(method.deriveFourth()).isCloseTo(240.0, within(1e-2));
    }

    // ==================== Accuracy Comparison Tests ====================

    @Test
    void moreAccurateThanStandardCentralDifferences() {
        // Extended central differences should be more accurate than standard
        Function f = new Function("x^4");
        double x = 2.0;

        // Standard central differences
        DividedDifferenceMethod standard = DividedDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(0.1)
                .direction(DifferencesDirection.Central)
                .build();

        // Extended central differences with same step size
        ExtendedCentralDifferenceMethod extended = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(0.1)
                .build();

        double exactDerivative = 32.0; // 4x^3 at x=2
        double standardError = Math.abs(standard.deriveFirst() - exactDerivative);
        double extendedError = Math.abs(extended.deriveFirst() - exactDerivative);

        // Extended method should be more accurate
        assertThat(extendedError).isLessThan(standardError);
    }

    // ==================== Immutability Tests ====================

    @Test
    void withTargetPointCreatesNewInstance() {
        Function f = new Function("x^2");
        ExtendedCentralDifferenceMethod original = ExtendedCentralDifferenceMethod.of(f);

        ExtendedCentralDifferenceMethod modified = original.withTargetPoint(3.0);

        assertThat(modified).isNotSameAs(original);
        assertThat(original.getTargetPoint()).isEqualTo(1.0);
        assertThat(modified.getTargetPoint()).isEqualTo(3.0);
        assertThat(modified.getTargetFunction()).isEqualTo(original.getTargetFunction());
        assertThat(modified.getStepSize()).isEqualTo(original.getStepSize());
    }

    @Test
    void withStepSizeCreatesNewInstance() {
        Function f = new Function("x^2");
        ExtendedCentralDifferenceMethod original = ExtendedCentralDifferenceMethod.of(f);

        ExtendedCentralDifferenceMethod modified = original.withStepSize(0.001);

        assertThat(modified).isNotSameAs(original);
        assertThat(original.getStepSize()).isEqualTo(0.01);
        assertThat(modified.getStepSize()).isEqualTo(0.001);
        assertThat(modified.getTargetPoint()).isEqualTo(original.getTargetPoint());
    }

    @Test
    void methodIsImmutable() {
        Function f = new Function("x^2");
        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        double originalTargetPoint = method.getTargetPoint();
        double originalStepSize = method.getStepSize();

        // Perform operations
        method.deriveFirst();
        method.deriveSecond();
        method.deriveThird();
        method.deriveFourth();

        // Verify state unchanged
        assertThat(method.getTargetPoint()).isEqualTo(originalTargetPoint);
        assertThat(method.getStepSize()).isEqualTo(originalStepSize);
    }

    // ==================== Equality Tests ====================

    @Test
    void equalityBasedOnAllFields() {
        Function f = new Function("x^2");

        ExtendedCentralDifferenceMethod method1 = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        ExtendedCentralDifferenceMethod method2 = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .build();

        assertThat(method1).isEqualTo(method2);
        assertThat(method1.hashCode()).isEqualTo(method2.hashCode());
    }

    @Test
    void inequalityWhenFieldsDiffer() {
        Function f = new Function("x^2");

        ExtendedCentralDifferenceMethod method1 = ExtendedCentralDifferenceMethod.of(f);
        ExtendedCentralDifferenceMethod method2 = method1.withTargetPoint(2.0);
        ExtendedCentralDifferenceMethod method3 = method1.withStepSize(0.001);

        assertThat(method1).isNotEqualTo(method2);
        assertThat(method1).isNotEqualTo(method3);
    }

    @Test
    void equalsHandlesSameInstance() {
        Function f = new Function("x^2");
        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.of(f);

        assertThat(method).isEqualTo(method);
    }

    @Test
    void equalsHandlesNull() {
        Function f = new Function("x^2");
        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.of(f);

        assertThat(method).isNotEqualTo(null);
    }

    @Test
    void equalsHandlesDifferentType() {
        Function f = new Function("x^2");
        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.of(f);
        DividedDifferenceMethod different = DividedDifferenceMethod.of(f);

        assertThat(method).isNotEqualTo(different);
    }

    // ==================== toString Tests ====================

    @Test
    void toStringContainsRelevantInformation() {
        Function f = new Function("x^2");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(2.5)
                .stepSize(0.001)
                .build();

        String str = method.toString();

        assertThat(str).contains("ExtendedCentralDifferenceMethod");
        assertThat(str).contains("2.5");
        assertThat(str).contains("0.001");
    }

    // ==================== Pre-computed Powers Tests ====================

    @Test
    void preComputedPowersImprovePerformance() {
        // This test verifies that pre-computed powers are used correctly
        // by checking that derivatives are still accurate
        Function f = new Function("x^5");

        ExtendedCentralDifferenceMethod method = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(1.5)
                .stepSize(0.01)
                .build();

        // f(x) = x^5
        // f'(x) = 5x^4 = 5 * 1.5^4 = 25.3125
        assertThat(method.deriveFirst()).isCloseTo(25.3125, within(TOLERANCE));

        // f''(x) = 20x^3 = 20 * 1.5^3 = 67.5
        assertThat(method.deriveSecond()).isCloseTo(67.5, within(TOLERANCE));

        // f'''(x) = 60x^2 = 60 * 1.5^2 = 135.0
        assertThat(method.deriveThird()).isCloseTo(135.0, within(TOLERANCE));

        // f''''(x) = 120x = 120 * 1.5 = 180.0
        assertThat(method.deriveFourth()).isCloseTo(180.0, within(TOLERANCE));
    }
}
