package uk.co.ryanharrison.mathengine.parser.spec;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assumptions.abort;

/**
 * Runs every case in every spec file under {@code src/test/resources/engine}.
 * <p>
 * This is the end-to-end contract for the language: text in, value out. Cases are
 * grouped into a JUnit container per directory and a nested container per file, so a
 * failure reads as {@code operators > add > add_unit_converts_before_adding}.
 *
 * <p>The spec files themselves are checked by {@link SpecIntegrityTest}, and the set of
 * names they exercise by {@link SpecCoverageTest}.
 */
class EngineSpecTest {

    @TestFactory
    Stream<DynamicNode> engineSpec() {
        Map<String, List<DynamicNode>> byGroup = new LinkedHashMap<>();

        for (SpecLoader.LoadedSuite loaded : SpecLoader.loadAll()) {
            byGroup.computeIfAbsent(loaded.group(), key -> new java.util.ArrayList<>())
                    .add(containerFor(loaded));
        }

        return byGroup.entrySet().stream()
                .map(entry -> DynamicContainer.dynamicContainer(entry.getKey(), entry.getValue()));
    }

    private DynamicNode containerFor(SpecLoader.LoadedSuite loaded) {
        SpecConfig suiteDefault = loaded.suite().defaultConfig();

        List<DynamicNode> cases = loaded.suite().tests().stream()
                .map(testCase -> (DynamicNode) DynamicTest.dynamicTest(
                        displayName(testCase),
                        () -> {
                            if (testCase.shouldSkip()) {
                                abort("Skipped: " + testCase.skip());
                            }
                            SpecCaseRunner.run(testCase, suiteDefault);
                        }))
                .toList();

        return DynamicContainer.dynamicContainer(loaded.stem(), cases);
    }

    private static String displayName(SpecCase testCase) {
        return testCase.id() + ": " + testCase.input();
    }
}
