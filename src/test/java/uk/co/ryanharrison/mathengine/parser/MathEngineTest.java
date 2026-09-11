package uk.co.ryanharrison.mathengine.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.ast.NodeRational;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;

import static org.assertj.core.api.Assertions.*;

/**
 * The engine's own API: how it is built, what a session remembers, and what it can be
 * asked about itself.
 * <p>
 * The behaviour of the language it evaluates is covered by the spec files under
 * {@code src/test/resources/engine}, not here.
 */
class MathEngineTest {

    @Nested
    @DisplayName("Factories")
    class Factories {

        @Test
        void createUsesTheDefaults() {
            assertThat(MathEngine.create().getConfig().angleUnit()).isEqualTo(AngleUnit.RADIANS);
        }

        @Test
        void createRejectsANullConfig() {
            assertThatThrownBy(() -> MathEngine.create(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Config");
        }

        @Test
        void arithmeticEngineDoesArithmeticAndNothingElse() {
            MathEngine engine = MathEngine.arithmetic();

            assertThat(engine.evaluateDouble("2 + 3 * 4")).isEqualTo(14.0);
            assertThatThrownBy(() -> engine.evaluate("sin(0)"))
                    .isInstanceOf(MathEngineException.class);
        }

        @Test
        void basicEngineHasTheCalculatorFunctionsButNoVectors() {
            MathEngine engine = MathEngine.basic();

            assertThat(engine.evaluateDouble("sqrt(16)")).isEqualTo(4.0);
            assertThatThrownBy(() -> engine.evaluate("sum({1, 2, 3})"))
                    .isInstanceOf(MathEngineException.class);
        }

        @Test
        void fullEngineMatchesCreate() {
            assertThat(MathEngine.full().evaluateDouble("sum({1, 2, 3})"))
                    .isEqualTo(MathEngine.create().evaluateDouble("sum({1, 2, 3})"));
        }
    }

    @Nested
    @DisplayName("Session")
    class Session {

        private final MathEngine engine = MathEngine.create();

        @Test
        void variablesSurviveBetweenEvaluations() {
            engine.evaluate("x := 10");
            engine.evaluate("y := 20");

            assertThat(engine.evaluateDouble("x + y")).isEqualTo(30.0);
        }

        @Test
        void definedVariablesAreVisibleToTheCaller() {
            engine.defineVariable("radius", 3.0);

            assertThat(engine.getLocalVariables()).containsKey("radius");
            assertThat(engine.evaluateDouble("radius * 2")).isEqualTo(6.0);
        }

        @Test
        void aVariableCanBeDefinedAsAValueRatherThanAnExpression() {
            engine.defineVariable("count", new NodeRational(7));

            assertThat(engine.evaluateDouble("count + 1")).isEqualTo(8.0);
        }

        @Test
        void functionsSurviveBetweenEvaluations() {
            engine.defineFunction("square(n) := n^2");

            assertThat(engine.getLocalFunctions()).containsKey("square");
            assertThat(engine.evaluateDouble("square(5)")).isEqualTo(25.0);
        }

        @Test
        void separateEnginesDoNotShareASession() {
            engine.evaluate("x := 10");

            assertThatThrownBy(() -> MathEngine.create().evaluate("x"))
                    .isInstanceOf(MathEngineException.class);
        }

        @Test
        void anEmptyExpressionIsRejected() {
            assertThatThrownBy(() -> engine.evaluate("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Reconfiguration")
    class Reconfiguration {

        @Test
        void withConfigCarriesTheSessionOver() {
            MathEngine engine = MathEngine.create();
            engine.evaluate("x := 90");
            engine.defineFunction("twice(n) := n * 2");

            MathEngine degrees = engine.withConfig(MathEngineConfig.builder()
                    .angleUnit(AngleUnit.DEGREES)
                    .build());

            assertThat(degrees.evaluateDouble("sin(x)")).isCloseTo(1.0, within(1e-12));
            assertThat(degrees.evaluateDouble("twice(4)")).isEqualTo(8.0);
        }

        @Test
        void withConfigLeavesTheOriginalEngineAlone() {
            MathEngine engine = MathEngine.create();
            engine.evaluate("x := 90");

            engine.withConfig(MathEngineConfig.builder().angleUnit(AngleUnit.DEGREES).build());

            assertThat(engine.getConfig().angleUnit()).isEqualTo(AngleUnit.RADIANS);
            assertThat(engine.evaluateDouble("sin(x)")).isCloseTo(Math.sin(90), within(1e-12));
        }
    }

    @Nested
    @DisplayName("Queries")
    class Queries {

        private final MathEngine engine = MathEngine.create();

        @Test
        void functionsAreGroupedByCategory() {
            var byCategory = engine.getFunctionsByCategory();

            assertThat(byCategory).containsKeys(
                    MathFunction.Category.TRIGONOMETRIC,
                    MathFunction.Category.MATRIX,
                    MathFunction.Category.STRING);
            assertThat(byCategory.get(MathFunction.Category.TRIGONOMETRIC))
                    .extracting(MathFunction::name)
                    .contains("sin", "cos", "tan");
        }

        @Test
        void everyCategoryListedIsNonEmpty() {
            assertThat(engine.getFunctionsByCategory().values())
                    .allSatisfy(functions -> assertThat(functions).isNotEmpty());
        }

        @Test
        void constantsAndUnitsCanBeListed() {
            assertThat(engine.getAllConstants()).isNotEmpty();
            assertThat(engine.getAllUnits()).isNotEmpty();
        }

        @Test
        void tokenizeExposesTheLexerOutput() {
            assertThat(engine.tokenize("1 + 2")).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Compilation")
    class Compilation {

        @Test
        void aCompiledExpressionEvaluatesRepeatedly() {
            var compiled = MathEngine.create().compile("x^2 + 1");

            assertThat(compiled.evaluateDouble("x", 3)).isEqualTo(10.0);
            assertThat(compiled.evaluateDouble("x", 4)).isEqualTo(17.0);
        }

        @Test
        void compilationReportsASyntaxErrorStraightAway() {
            assertThatThrownBy(() -> MathEngine.create().compile("(1 + "))
                    .isInstanceOf(MathEngineException.class);
        }
    }
}
