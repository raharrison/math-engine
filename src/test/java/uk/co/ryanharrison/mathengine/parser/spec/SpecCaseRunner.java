package uk.co.ryanharrison.mathengine.parser.spec;

import uk.co.ryanharrison.mathengine.parser.MathEngine;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.format.StringNodeFormatter;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Executes one {@link SpecCase} against a freshly built engine.
 * <p>
 * A new engine per case keeps session state from leaking between cases, so a spec file
 * can be read top to bottom without wondering what an earlier case defined. Multi-step
 * scenarios belong in one input, separated by {@code ;}.
 */
public final class SpecCaseRunner {

    /**
     * Default absolute tolerance, loose enough for the series approximations in {@code special}.
     */
    static final double DEFAULT_TOLERANCE = 1e-7;

    private static final StringNodeFormatter FORMATTER = StringNodeFormatter.fullPrecision();

    /**
     * Engines are immutable apart from their session scope, so configs can be cached and shared.
     */
    private static final Map<SpecConfig, MathEngineConfig> CONFIG_CACHE = new LinkedHashMap<>();

    private SpecCaseRunner() {
    }

    /**
     * Runs a case, throwing an {@link AssertionError} describing any mismatch.
     */
    public static void run(SpecCase testCase, SpecConfig suiteDefault) {
        MathEngineConfig config = resolveConfig(testCase.config() != null ? testCase.config() : suiteDefault);
        MathEngine engine = MathEngine.create(config);

        if (testCase.shouldExpectError()) {
            runErrorCase(testCase, engine);
            return;
        }

        assertEvaluationOrder(testCase, engine);

        NodeConstant result = engine.evaluate(testCase.input());
        SpecValueAssertions.assertType(result, testCase.expectedType(), testCase.id());
        SpecValueAssertions.assertValue(result, testCase.expected(), testCase.expectedType(),
                testCase.id(), testCase.hasTolerance() ? testCase.tolerance() : DEFAULT_TOLERANCE);
    }

    /**
     * The exception must be exactly the one named, not a subclass of it. Naming a
     * supertype would let the engine quietly widen what it throws.
     */
    private static void runErrorCase(SpecCase testCase, MathEngine engine) {
        var assertion = assertThatThrownBy(() -> engine.evaluate(testCase.input()))
                .as("Case %s: '%s' should raise exactly %s", testCase.id(), testCase.input(),
                        testCase.expectedErrorType())
                .isExactlyInstanceOf(SpecExceptions.resolve(testCase.expectedErrorType()));

        if (testCase.expectedErrorMessage() != null) {
            assertion.hasMessageContaining(testCase.expectedErrorMessage());
        }
    }

    /**
     * Checks that the declared grouping is the grouping the parser actually produced.
     * <p>
     * The annotation is an ordinary expression with explicit parentheses. Both it and
     * the input are parsed and rendered in the formatter's fully parenthesised form, so
     * only the tree shape is compared and incidental spacing does not matter.
     */
    private static void assertEvaluationOrder(SpecCase testCase, MathEngine engine) {
        if (testCase.evaluationOrder() == null) {
            return;
        }
        String actual = FORMATTER.format(engine.compile(testCase.input()).getAst());
        String declared = FORMATTER.format(engine.compile(testCase.evaluationOrder()).getAst());

        assertThat(actual)
                .as("Case %s: '%s' should group as '%s'", testCase.id(), testCase.input(), testCase.evaluationOrder())
                .isEqualTo(declared);
    }

    private static synchronized MathEngineConfig resolveConfig(SpecConfig config) {
        if (config == null) {
            return MathEngineConfig.defaults();
        }
        return CONFIG_CACHE.computeIfAbsent(config, SpecConfig::toMathEngineConfig);
    }
}
