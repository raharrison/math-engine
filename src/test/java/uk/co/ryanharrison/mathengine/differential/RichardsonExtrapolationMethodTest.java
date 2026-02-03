package uk.co.ryanharrison.mathengine.differential;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.core.Function;

import static org.assertj.core.api.Assertions.*;

class RichardsonExtrapolationMethodTest {
    private static final double TOLERANCE = 1e-6;  // Tighter tolerance due to higher accuracy

    // ==================== Construction Tests ====================

    @Test
    void constructWithBuilder() {
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.getTargetFunction()).isEqualTo(f);
        assertThat(method.getTargetPoint()).isEqualTo(2.0);
        assertThat(method.getStepSize()).isEqualTo(0.01);
        assertThat(method.getDirection()).isEqualTo(DifferencesDirection.Central);
    }

    @Test
    void constructWithOfMethod() {
        Function f = new Function("x^3");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.of(f);

        assertThat(method.getTargetFunction()).isEqualTo(f);
        assertThat(method.getTargetPoint()).isEqualTo(1.0);
        assertThat(method.getStepSize()).isEqualTo(0.01);
        assertThat(method.getDirection()).isEqualTo(DifferencesDirection.Central);
    }

    @Test
    void constructWithDirection() {
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.of(f, DifferencesDirection.Forward);

        assertThat(method.getDirection()).isEqualTo(DifferencesDirection.Forward);
        assertThat(method.getTargetPoint()).isEqualTo(1.0);
        assertThat(method.getStepSize()).isEqualTo(0.01);
    }

    @Test
    void constructWithDirectionAndStepSize() {
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.of(f, DifferencesDirection.Backward, 0.001);

        assertThat(method.getDirection()).isEqualTo(DifferencesDirection.Backward);
        assertThat(method.getStepSize()).isEqualTo(0.001);
    }

    @Test
    void builderRequiresTargetFunction() {
        assertThatThrownBy(() -> RichardsonExtrapolationMethod.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Target function must be set");
    }

    @Test
    void builderRejectsNullFunction() {
        assertThatThrownBy(() -> RichardsonExtrapolationMethod.builder()
                .targetFunction(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Target function cannot be null");
    }

    @Test
    void builderRejectsNullDirection() {
        Function f = new Function("x^2");

        assertThatThrownBy(() -> RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .direction(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Direction cannot be null");
    }

    @Test
    void builderRejectsNonPositiveStepSize() {
        Function f = new Function("x^2");

        assertThatThrownBy(() -> RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .stepSize(0.0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Step size must be positive");

        assertThatThrownBy(() -> RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .stepSize(-0.01)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Step size must be positive");
    }

    @Test
    void builderRejectsNonFiniteTargetPoint() {
        Function f = new Function("x^2");

        assertThatThrownBy(() -> RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(Double.NaN)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target point must be finite");

        assertThatThrownBy(() -> RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(Double.POSITIVE_INFINITY)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Target point must be finite");
    }

    // ==================== First Derivative Tests (Central) ====================

    @ParameterizedTest
    @CsvSource({
            "1.0, 2.0",     // f(x) = x^2, f'(x) = 2x, at x=1
            "2.0, 4.0",     // at x=2
            "3.0, 6.0",     // at x=3
            "-1.0, -2.0",   // at x=-1
    })
    void centralFirstDerivativeOfQuadratic(double x, double expected) {
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.deriveFirst()).isCloseTo(expected, within(TOLERANCE));
    }

    @Test
    void centralFirstDerivativeOfCubic() {
        // f(x) = x^3, f'(x) = 3x^2
        Function f = new Function("x^3");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.deriveFirst()).isCloseTo(12.0, within(TOLERANCE));
    }

    @Test
    void centralFirstDerivativeOfQuartic() {
        // f(x) = x^4, f'(x) = 4x^3
        Function f = new Function("x^4");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.deriveFirst()).isCloseTo(32.0, within(TOLERANCE));
    }

    // ==================== Second Derivative Tests (Central) ====================

    @Test
    void centralSecondDerivativeOfQuadratic() {
        // f(x) = x^2, f''(x) = 2
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.deriveSecond()).isCloseTo(2.0, within(TOLERANCE));
    }

    @Test
    void centralSecondDerivativeOfCubic() {
        // f(x) = x^3, f''(x) = 6x
        Function f = new Function("x^3");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.deriveSecond()).isCloseTo(12.0, within(TOLERANCE));
    }

    @Test
    void centralSecondDerivativeOfQuartic() {
        // f(x) = x^4, f''(x) = 12x^2
        Function f = new Function("x^4");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.deriveSecond()).isCloseTo(48.0, within(TOLERANCE));
    }

    // ==================== Third Derivative Tests (Central) ====================

    @Test
    void centralThirdDerivativeOfCubic() {
        // f(x) = x^3, f'''(x) = 6
        Function f = new Function("x^3");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.deriveThird()).isCloseTo(6.0, within(TOLERANCE));
    }

    @Test
    void centralThirdDerivativeOfQuartic() {
        // f(x) = x^4, f'''(x) = 24x
        Function f = new Function("x^4");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method.deriveThird()).isCloseTo(48.0, within(TOLERANCE));
    }

    // ==================== Fourth Derivative Tests (Central) ====================

    @Test
    void centralFourthDerivativeOfQuartic() {
        // f(x) = x^4, f''''(x) = 24
        Function f = new Function("x^4");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        // Fourth derivatives have larger numerical errors
        assertThat(method.deriveFourth()).isCloseTo(24.0, within(1e-4));
    }

    // ==================== Forward Differences Tests ====================

    @Test
    void forwardFirstDerivative() {
        // f(x) = x^2, f'(x) = 2x
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Forward)
                .build();

        assertThat(method.deriveFirst()).isCloseTo(4.0, within(TOLERANCE));
    }

    @Test
    void forwardSecondDerivative() {
        // f(x) = x^2, f''(x) = 2
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Forward)
                .build();

        assertThat(method.deriveSecond()).isCloseTo(2.0, within(TOLERANCE));
    }

    // ==================== Backward Differences Tests ====================

    @Test
    void backwardFirstDerivative() {
        // f(x) = x^2, f'(x) = 2x
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Backward)
                .build();

        assertThat(method.deriveFirst()).isCloseTo(4.0, within(TOLERANCE));
    }

    @Test
    void backwardSecondDerivative() {
        // f(x) = x^2, f''(x) = 2
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Backward)
                .build();

        assertThat(method.deriveSecond()).isCloseTo(2.0, within(TOLERANCE));
    }

    // ==================== Accuracy Comparison Tests ====================

    @Test
    void moreAccurateThanBaseDividedDifferences() {
        // Richardson extrapolation should be more accurate than base method
        Function f = new Function("x^4");
        double x = 2.0;

        // Base divided differences
        DividedDifferenceMethod base = DividedDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(0.1)
                .direction(DifferencesDirection.Central)
                .build();

        // Richardson extrapolation with same step size
        RichardsonExtrapolationMethod richardson = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(0.1)
                .direction(DifferencesDirection.Central)
                .build();

        double exactDerivative = 32.0; // 4x^3 at x=2
        double baseError = Math.abs(base.deriveFirst() - exactDerivative);
        double richardsonError = Math.abs(richardson.deriveFirst() - exactDerivative);

        // Richardson method should be more accurate
        assertThat(richardsonError).isLessThan(baseError);
    }

    @Test
    void similarAccuracyToExtendedCentralDifferences() {
        // Richardson extrapolation should achieve similar accuracy to extended central differences
        Function f = new Function("x^5");
        double x = 1.5;

        RichardsonExtrapolationMethod richardson = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(0.1)
                .direction(DifferencesDirection.Central)
                .build();

        ExtendedCentralDifferenceMethod extended = ExtendedCentralDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(0.1)
                .build();

        double exactDerivative = 25.3125; // 5x^4 at x=1.5
        double richardsonError = Math.abs(richardson.deriveFirst() - exactDerivative);
        double extendedError = Math.abs(extended.deriveFirst() - exactDerivative);

        // Both should have comparable accuracy (within same order of magnitude)
        assertThat(richardsonError).isLessThan(5e-4);
        assertThat(extendedError).isLessThan(5e-4);
    }

    // ==================== Immutability Tests ====================

    @Test
    void withTargetPointCreatesNewInstance() {
        Function f = new Function("x^2");
        RichardsonExtrapolationMethod original = RichardsonExtrapolationMethod.of(f);

        RichardsonExtrapolationMethod modified = original.withTargetPoint(3.0);

        assertThat(modified).isNotSameAs(original);
        assertThat(original.getTargetPoint()).isEqualTo(1.0);
        assertThat(modified.getTargetPoint()).isEqualTo(3.0);
        assertThat(modified.getTargetFunction()).isEqualTo(original.getTargetFunction());
        assertThat(modified.getStepSize()).isEqualTo(original.getStepSize());
        assertThat(modified.getDirection()).isEqualTo(original.getDirection());
    }

    @Test
    void withStepSizeCreatesNewInstance() {
        Function f = new Function("x^2");
        RichardsonExtrapolationMethod original = RichardsonExtrapolationMethod.of(f);

        RichardsonExtrapolationMethod modified = original.withStepSize(0.001);

        assertThat(modified).isNotSameAs(original);
        assertThat(original.getStepSize()).isEqualTo(0.01);
        assertThat(modified.getStepSize()).isEqualTo(0.001);
        assertThat(modified.getTargetPoint()).isEqualTo(original.getTargetPoint());
    }

    @Test
    void withDirectionCreatesNewInstance() {
        Function f = new Function("x^2");
        RichardsonExtrapolationMethod original = RichardsonExtrapolationMethod.of(f);

        RichardsonExtrapolationMethod modified = original.withDirection(DifferencesDirection.Forward);

        assertThat(modified).isNotSameAs(original);
        assertThat(original.getDirection()).isEqualTo(DifferencesDirection.Central);
        assertThat(modified.getDirection()).isEqualTo(DifferencesDirection.Forward);
    }

    @Test
    void methodIsImmutable() {
        Function f = new Function("x^2");
        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        double originalTargetPoint = method.getTargetPoint();
        double originalStepSize = method.getStepSize();
        DifferencesDirection originalDirection = method.getDirection();

        // Perform operations
        method.deriveFirst();
        method.deriveSecond();
        method.deriveThird();
        method.deriveFourth();

        // Verify state unchanged
        assertThat(method.getTargetPoint()).isEqualTo(originalTargetPoint);
        assertThat(method.getStepSize()).isEqualTo(originalStepSize);
        assertThat(method.getDirection()).isEqualTo(originalDirection);
    }

    // ==================== Equality Tests ====================

    @Test
    void equalityBasedOnBaseMethod() {
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method1 = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        RichardsonExtrapolationMethod method2 = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.01)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(method1).isEqualTo(method2);
        assertThat(method1.hashCode()).isEqualTo(method2.hashCode());
    }

    @Test
    void inequalityWhenFieldsDiffer() {
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method1 = RichardsonExtrapolationMethod.of(f);
        RichardsonExtrapolationMethod method2 = method1.withTargetPoint(2.0);
        RichardsonExtrapolationMethod method3 = method1.withStepSize(0.001);
        RichardsonExtrapolationMethod method4 = method1.withDirection(DifferencesDirection.Forward);

        assertThat(method1).isNotEqualTo(method2);
        assertThat(method1).isNotEqualTo(method3);
        assertThat(method1).isNotEqualTo(method4);
    }

    @Test
    void equalsHandlesSameInstance() {
        Function f = new Function("x^2");
        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.of(f);

        assertThat(method).isEqualTo(method);
    }

    @Test
    void equalsHandlesNull() {
        Function f = new Function("x^2");
        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.of(f);

        assertThat(method).isNotEqualTo(null);
    }

    @Test
    void equalsHandlesDifferentType() {
        Function f = new Function("x^2");
        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.of(f);
        DividedDifferenceMethod different = DividedDifferenceMethod.of(f);

        assertThat(method).isNotEqualTo(different);
    }

    // ==================== toString Tests ====================

    @Test
    void toStringContainsRelevantInformation() {
        Function f = new Function("x^2");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.5)
                .stepSize(0.001)
                .direction(DifferencesDirection.Central)
                .build();

        String str = method.toString();

        assertThat(str).contains("RichardsonExtrapolationMethod");
        assertThat(str).contains("Central");
        assertThat(str).contains("2.5");
        assertThat(str).contains("0.001");
    }

    // ==================== Delegation Tests ====================

    @Test
    void delegatesToBaseMethods() {
        // Verify that Richardson extrapolation properly delegates to base methods
        Function f = new Function("x^3");

        RichardsonExtrapolationMethod method = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(2.0)
                .stepSize(0.1)
                .direction(DifferencesDirection.Central)
                .build();

        // Should compute derivatives without error (validates delegation)
        assertThat(method.deriveFirst()).isFinite();
        assertThat(method.deriveSecond()).isFinite();
        assertThat(method.deriveThird()).isFinite();
        assertThat(method.deriveFourth()).isFinite();
    }

    @Test
    void extrapolationFormulaCorrect() {
        // Manually verify the Richardson extrapolation formula
        Function f = new Function("x^2");
        double x = 2.0;
        double h = 0.1;

        // Create base methods manually
        DividedDifferenceMethod baseH = DividedDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(h)
                .direction(DifferencesDirection.Central)
                .build();

        DividedDifferenceMethod baseHalfH = DividedDifferenceMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(h / 2.0)
                .direction(DifferencesDirection.Central)
                .build();

        // Manual Richardson extrapolation: (4*D(h/2) - D(h)) / 3
        double d1 = baseH.deriveFirst();
        double d2 = baseHalfH.deriveFirst();
        double manualExtrapolation = (4.0 * d2 - d1) / 3.0;

        // Compare with Richardson method
        RichardsonExtrapolationMethod richardson = RichardsonExtrapolationMethod.builder()
                .targetFunction(f)
                .targetPoint(x)
                .stepSize(h)
                .direction(DifferencesDirection.Central)
                .build();

        assertThat(richardson.deriveFirst()).isCloseTo(manualExtrapolation, within(1e-10));
    }
}
