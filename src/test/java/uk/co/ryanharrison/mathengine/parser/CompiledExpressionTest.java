package uk.co.ryanharrison.mathengine.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CompiledExpressionTest {

    private static final double TOLERANCE = 1e-9;

    private MathEngine engine;

    @BeforeEach
    void setUp() {
        engine = MathEngine.create();
    }

    @Test
    void bindingsSupplyTheVariable() {
        CompiledExpression expression = engine.compile("x^2 + 1");

        assertThat(expression.evaluateDouble("x", 3.0)).isCloseTo(10.0, within(TOLERANCE));
        assertThat(expression.evaluateDouble("x", 4.0)).isCloseTo(17.0, within(TOLERANCE));
    }

    @Test
    void bindingsShadowSessionVariablesOnlyForTheCall() {
        engine.evaluate("x := 100");
        CompiledExpression expression = engine.compile("x + 1");

        assertThat(expression.evaluateDouble("x", 1.0)).isCloseTo(2.0, within(TOLERANCE));
        assertThat(engine.evaluateDouble("x")).isCloseTo(100.0, within(TOLERANCE));
    }

    @Test
    void bindingsDoNotLeakIntoTheSession() {
        CompiledExpression expression = engine.compile("x + 1");

        expression.evaluate(Map.of("x", 1));

        assertThat(engine.getLocalVariables()).doesNotContainKey("x");
    }

    @Test
    void anExpressionWithNoBindingsUsesTheSession() {
        engine.evaluate("k := 6");
        CompiledExpression expression = engine.compile("k * 7");

        assertThat(expression.evaluate().doubleValue()).isCloseTo(42.0, within(TOLERANCE));
    }

    /**
     * Bindings once accumulated in the session scope, which made lookups of anything
     * else steadily slower and eventually overflowed the stack. A plot or a solver
     * drives exactly this loop.
     */
    @Test
    void manyEvaluationsLeaveTheSessionUntouched() {
        CompiledExpression expression = engine.compile("x * pi");

        for (int i = 0; i < 50_000; i++) {
            expression.evaluate(Map.of("x", i));
        }

        assertThat(engine.getLocalVariables()).isEmpty();
        assertThat(expression.evaluateDouble("x", 2.0)).isCloseTo(2 * Math.PI, within(TOLERANCE));
    }
}
