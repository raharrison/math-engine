package uk.co.ryanharrison.mathengine.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * The two things about depth that only show up at size: a flat chain costs no Java stack,
 * and nothing the engine does escapes as an {@link Error}.
 * <p>
 * What the depth limits mean is spec'd as data in {@code config/limits.json}. Everything
 * here runs on a 256KB thread, small enough that the sizes below stay short.
 */
class DeepExpressionTest {

    private static final int STACK_KB = 256;

    /**
     * Ten times what the old per-term recursive walk managed on this stack.
     */
    private static final int TERMS = 5_000;

    /**
     * Depth limits off, so it is the JVM stack that gives out and the boundary guard,
     * rather than a limit, that has to produce the exception.
     */
    private static final MathEngineConfig NO_DEPTH_LIMITS = MathEngineConfig.builder()
            .maxExpressionDepth(Integer.MAX_VALUE)
            .maxRecursionDepth(Integer.MAX_VALUE)
            .build();

    @Nested
    @DisplayName("A flat chain costs no Java stack")
    class FlatChains {

        @Test
        void aLongSum() {
            assertThat(value("1" + "+1".repeat(TERMS)).doubleValue())
                    .isCloseTo(TERMS + 1, within(1e-9));
        }

        @Test
        void aLongProduct() {
            assertThat(value("2" + "*1".repeat(TERMS)).doubleValue()).isCloseTo(2, within(1e-9));
        }

        @Test
        void aLongShortCircuitingChain() {
            assertThat(value("true" + " && true".repeat(TERMS))).hasToString("true");
        }

        @Test
        void aLongRunOfPostfixOperators() {
            // Each % divides by a hundred, so the value bottoms out; the length is the point
            assertThat(value("1" + "%".repeat(TERMS)).doubleValue()).isCloseTo(0, within(1e-9));
        }

        @Test
        void foldingTheChainKeepsTheOrder() {
            assertThat(value("100 - 10 - 1").doubleValue()).isCloseTo(89, within(1e-9));
            assertThat(value("100 / 10 / 2").doubleValue()).isCloseTo(5, within(1e-9));
        }

        @Test
        void foldingTheChainStillShortCircuits() {
            assertThat(value("false && false && undefined_name")).hasToString("false");
        }
    }

    @Nested
    @DisplayName("Running out of JVM stack is an exception, not an Error")
    class OutOfStack {

        /**
         * The level counts are per shape because the cost of a level differs by an order of
         * magnitude: a grouping spends ten Java frames, a prefix operator one. Each is well
         * clear of where that shape gives out on this stack, so none of these sits near the
         * boundary where the answer would depend on what the JIT had compiled.
         */
        @ParameterizedTest(name = "{0}")
        @CsvSource({
                "groupings,         5000, (,        )",
                "vectors,           5000, '{',      '}'",
                "matrices,          5000, '[',      ']'",
                "calls,             5000, abs(,     )",
                "argument lists,    5000, 'max(1,', )",
                "subscript chain,   5000, '',       '[0]'",
                "prefix operators, 50000, -,        ''",
                "power chain,      50000, '1^',     ''"
        })
        void nesting(String shape, int levels, String open, String close) {
            assertThat(outcome(open.repeat(levels) + "{1}" + close.repeat(levels), NO_DEPTH_LIMITS))
                    .as("%s nested %d deep", shape, levels)
                    .isInstanceOf(MathEngineException.class);
        }

        @Test
        void theEngineStillWorksAfterwards() {
            MathEngine engine = MathEngine.create();
            assertThat(catching(() -> engine.evaluate("loop(n) := loop(n + 1); loop(1)")))
                    .isInstanceOf(MathEngineException.class);

            // A leaked recursion count would show up as a spurious failure here
            assertThat(engine.evaluate("f(n) := if(n <= 0, 0, f(n - 1) + 1); f(200)").doubleValue())
                    .isCloseTo(200, within(1e-9));
        }
    }

    // ==================== Helpers ====================

    private static NodeConstant value(String expression) {
        Object result = outcome(expression, null);
        assertThat(result).as("evaluating %d characters", expression.length())
                .isInstanceOf(NodeConstant.class);
        return (NodeConstant) result;
    }

    /**
     * The value or the throwable, from a thread with a stack too small to hide a per-level
     * Java frame.
     */
    private static Object outcome(String expression, MathEngineConfig config) {
        var result = new AtomicReference<>();
        Runnable body = () -> {
            MathEngine engine = config == null ? MathEngine.create() : MathEngine.create(config);
            result.set(catching(() -> engine.evaluate(expression)));
        };

        Thread thread = new Thread(null, body, "deep-expression", STACK_KB * 1024L);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while evaluating", e);
        }
        return result.get();
    }

    /**
     * Catches {@link Throwable}: an escaping {@link StackOverflowError} is what these
     * cases are looking for.
     */
    private static Object catching(java.util.function.Supplier<?> work) {
        try {
            return work.get();
        } catch (Throwable t) {
            return t;
        }
    }
}
