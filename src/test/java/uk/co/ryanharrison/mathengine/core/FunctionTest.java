package uk.co.ryanharrison.mathengine.core;

import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.integral.SimpsonIntegrator;
import uk.co.ryanharrison.mathengine.solvers.BrentSolver;

import static org.assertj.core.api.Assertions.*;

class FunctionTest {

    private static final double TOLERANCE = 1e-9;

    // ==================== Equation ====================

    @Test
    void equationIsEvaluatedAtAPoint() {
        Function f = new Function("x^2 + 8*x + 12");

        assertThat(f.evaluateAt(3.5)).isCloseTo(52.25, within(TOLERANCE));
        assertThat(f.evaluateAt("1 + 1")).isCloseTo(32.0, within(TOLERANCE));
    }

    @Test
    void equationHasAnExpressionTree() {
        Function f = new Function("t^2", "t");

        assertThat(f.getCompiledExpression()).isNotNull();
        assertThat(f).hasToString("f(t) = t^2");
    }

    // ==================== Backed by code ====================

    @Test
    void codeBackedFunctionEvaluatesThroughItsOperator() {
        Function f = Function.of(x -> x * x, "x^2");

        assertThat(f.evaluateAt(3.0)).isEqualTo(9.0);
        assertThat(f.evaluateAt("2 + 1")).isEqualTo(9.0);
        assertThat(f.getEquation()).isEqualTo("x^2");
        assertThat(f.getVariable()).isEqualTo("x");
    }

    @Test
    void codeBackedFunctionWorksWithTheNumericalMethods() {
        Function f = Function.of(x -> x * x - 2, "x^2 - 2");

        assertThat(SimpsonIntegrator.of(f, 0.0, 3.0).integrate()).isCloseTo(3.0, within(TOLERANCE));
        assertThat(BrentSolver.of(f, 0.0, 2.0).solve()).isCloseTo(Math.sqrt(2), within(1e-5));
    }

    @Test
    void codeBackedFunctionHasNoExpressionTree() {
        Function f = Function.of(Math::sin, "sin");

        assertThatThrownBy(f::getCompiledExpression)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("no expression tree");
    }

    @Test
    void codeBackedFunctionRejectsNulls() {
        assertThatThrownBy(() -> Function.of(null, "f"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Function.of(x -> x, null))
                .isInstanceOf(NullPointerException.class);
    }
}
