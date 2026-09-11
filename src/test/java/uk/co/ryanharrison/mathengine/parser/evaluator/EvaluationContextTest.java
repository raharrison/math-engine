package uk.co.ryanharrison.mathengine.parser.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeRational;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationContextTest {

    private EvaluationContext root;

    @BeforeEach
    void setUp() {
        root = new EvaluationContext(MathEngineConfig.defaults(), new RecursionTracker(100));
    }

    private static NodeConstant number(long value) {
        return new NodeRational(value);
    }

    @Test
    void aChildSeesItsParentsVariables() {
        root.define("x", number(1));

        EvaluationContext child = root.withBindings(Map.of("y", number(2)));

        assertThat(child.resolve("x")).contains(number(1));
        assertThat(child.resolve("y")).contains(number(2));
    }

    @Test
    void aChildBindingShadowsTheParentWithoutDisturbingIt() {
        root.define("x", number(1));

        EvaluationContext child = root.withBindings(Map.of("x", number(99)));

        assertThat(child.resolve("x")).contains(number(99));
        assertThat(root.resolve("x")).contains(number(1));
    }

    @Test
    void assigningUpdatesTheScopeThatDefinedTheVariable() {
        root.define("x", number(1));
        EvaluationContext child = root.withBindings(Map.of());

        child.assign("x", number(5));

        assertThat(root.resolve("x")).contains(number(5));
        assertThat(child.getLocalVariables()).isEmpty();
    }

    @Test
    void assigningAnUnknownNameDefinesItLocally() {
        EvaluationContext child = root.withBindings(Map.of());

        child.assign("fresh", number(7));

        assertThat(child.getLocalVariables()).containsKey("fresh");
        assertThat(root.resolve("fresh")).isEmpty();
    }

    @Test
    void unknownNamesFallThroughToTheConstantRegistry() {
        assertThat(root.resolve("pi")).isPresent();
        assertThat(root.resolve("definitely_not_defined")).isEmpty();
    }

    @Test
    void aSnapshotFlattensTheScopeChainAndDetachesFromIt() {
        root.define("x", number(1));
        EvaluationContext child = root.withBindings(Map.of("y", number(2)));

        EvaluationContext snapshot = child.snapshot();
        root.define("x", number(100));

        assertThat(snapshot.resolve("x")).contains(number(1));
        assertThat(snapshot.resolve("y")).contains(number(2));
    }

    /**
     * Scopes used to be layered onto a chain that was never collapsed, so repeated
     * binding made every lookup slower until it overflowed the stack.
     */
    @Test
    void repeatedChildScopesDoNotAccumulate() {
        for (int i = 0; i < 100_000; i++) {
            EvaluationContext scope = root.withBindings(Map.of("x", number(i)));
            assertThat(scope.resolve("x")).isPresent();
        }

        assertThat(root.getLocalVariables()).isEmpty();
        assertThat(root.resolve("still_undefined")).isEmpty();
    }
}
