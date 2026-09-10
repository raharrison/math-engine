package uk.co.ryanharrison.mathengine.parser.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Structural rules the spec files must obey.
 * <p>
 * These exist because a fixture that asserts nothing still passes. Each rule here closes
 * one way a case can look like coverage without being coverage: an error case that
 * accepts any exception, a value case with no declared type, an identifier that appears
 * twice so a bug report cannot name a single case.
 */
class SpecIntegrityTest {

    private static final List<SpecLoader.LoadedSuite> SUITES = SpecLoader.loadAll();

    /**
     * Every simple name a fixture may write in {@code expectedType}.
     */
    private static final Set<String> VALUE_TYPE_NAMES = valueTypeNames();

    @Test
    @DisplayName("every spec file states its category and what it covers")
    void everyFileIsDescribed() {
        List<String> problems = new ArrayList<>();
        for (SpecLoader.LoadedSuite loaded : SUITES) {
            if (isBlank(loaded.suite().category())) {
                problems.add(loaded.resourcePath() + ": missing 'category'");
            }
            if (isBlank(loaded.suite().description())) {
                problems.add(loaded.resourcePath() + ": missing 'description'");
            }
            if (loaded.suite().tests().isEmpty()) {
                problems.add(loaded.resourcePath() + ": has no cases");
            }
        }
        assertThat(problems).as("spec files missing their header").isEmpty();
    }

    @Test
    @DisplayName("no two spec files claim the same category")
    void categoriesAreUnique() {
        Map<String, List<String>> byCategory = new HashMap<>();
        SUITES.forEach(loaded -> byCategory
                .computeIfAbsent(String.valueOf(loaded.suite().category()), key -> new ArrayList<>())
                .add(loaded.resourcePath()));

        List<String> duplicates = byCategory.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " in " + entry.getValue())
                .sorted()
                .toList();

        assertThat(duplicates)
                .as("a category names one file, so a failure says where to look")
                .isEmpty();
    }

    @Test
    @DisplayName("case ids are unique across the whole suite")
    void caseIdsAreUnique() {
        Map<String, List<String>> byId = new HashMap<>();
        forEachCase((loaded, testCase) -> byId
                .computeIfAbsent(testCase.id() == null ? "<missing>" : testCase.id(), key -> new ArrayList<>())
                .add(loaded.resourcePath()));

        List<String> duplicates = byId.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " in " + entry.getValue())
                .sorted()
                .toList();

        assertThat(duplicates)
                .as("an id must identify one case, so it can be quoted in a bug report and found")
                .isEmpty();
    }

    @Test
    @DisplayName("every case has an id, an input and a note saying why it exists")
    void everyCaseIsLabelled() {
        List<String> problems = new ArrayList<>();
        forEachCase((loaded, testCase) -> {
            String where = loaded.resourcePath() + " -> " + testCase.id();
            if (isBlank(testCase.id())) {
                problems.add(loaded.resourcePath() + ": a case has no 'id'");
            }
            if (isBlank(testCase.input())) {
                problems.add(where + ": missing 'input'");
            }
            if (isBlank(testCase.notes())) {
                problems.add(where + ": missing 'notes'");
            }
        });
        assertThat(problems).as("unlabelled cases").isEmpty();
    }

    @Test
    @DisplayName("every error case names the exception it expects")
    void errorCasesNameTheirException() {
        List<String> problems = new ArrayList<>();
        forEachCase((loaded, testCase) -> {
            String where = loaded.resourcePath() + " -> " + testCase.id();
            if (testCase.shouldExpectError()) {
                if (isBlank(testCase.expectedErrorType())) {
                    problems.add(where + ": expectError without 'expectedErrorType'");
                } else if (!SpecExceptions.isKnown(testCase.expectedErrorType())) {
                    problems.add(where + ": unknown exception '" + testCase.expectedErrorType() + "'");
                }
                if (testCase.expected() != null || testCase.expectedType() != null) {
                    problems.add(where + ": an error case cannot also expect a value");
                }
            } else if (testCase.expectedErrorType() != null || testCase.expectedErrorMessage() != null) {
                problems.add(where + ": names an error but does not set 'expectError'");
            }
        });
        assertThat(problems).as("error cases that would accept any failure").isEmpty();
    }

    @Test
    @DisplayName("every value case asserts both a value and its exact type")
    void valueCasesAssertTypeAndValue() {
        List<String> problems = new ArrayList<>();
        forEachCase((loaded, testCase) -> {
            if (testCase.shouldExpectError() || testCase.shouldSkip()) {
                return;
            }
            String where = loaded.resourcePath() + " -> " + testCase.id();
            if (testCase.expected() == null) {
                problems.add(where + ": missing 'expected'");
            }
            if (isBlank(testCase.expectedType())) {
                problems.add(where + ": missing 'expectedType'");
            } else if (!VALUE_TYPE_NAMES.contains(testCase.expectedType())) {
                problems.add(where + ": '" + testCase.expectedType() + "' is not a value type");
            }
            if (testCase.tolerance() != null && testCase.tolerance() <= 0) {
                problems.add(where + ": tolerance must be positive");
            }
        });
        assertThat(problems).as("value cases that assert less than they look like they do").isEmpty();
    }

    @Test
    @DisplayName("no expression is asserted twice under the same configuration")
    void inputsAreNotDuplicated() {
        record Key(String input, SpecConfig config) {
        }

        Map<Key, List<String>> byKey = new HashMap<>();
        for (SpecLoader.LoadedSuite loaded : SUITES) {
            SpecConfig suiteDefault = loaded.suite().defaultConfig();
            for (SpecCase testCase : loaded.suite().tests()) {
                SpecConfig effective = testCase.config() != null ? testCase.config() : suiteDefault;
                byKey.computeIfAbsent(new Key(testCase.input(), effective), key -> new ArrayList<>())
                        .add(loaded.stem() + "/" + testCase.id());
            }
        }

        List<String> duplicates = byKey.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> "'" + entry.getKey().input() + "' in " + entry.getValue())
                .sorted()
                .toList();

        assertThat(duplicates)
                .as("the same input asserted in several places drifts apart; assert it once")
                .isEmpty();
    }

    // ==================== Helpers ====================

    private interface CaseVisitor {
        void visit(SpecLoader.LoadedSuite loaded, SpecCase testCase);
    }

    private static void forEachCase(CaseVisitor visitor) {
        for (SpecLoader.LoadedSuite loaded : SUITES) {
            for (SpecCase testCase : loaded.suite().tests()) {
                visitor.visit(loaded, testCase);
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Walks the sealed {@link NodeConstant} hierarchy so the list cannot go stale.
     */
    private static Set<String> valueTypeNames() {
        Set<String> names = new TreeSet<>(Set.of(SpecValueAssertions.ANY_NUMBER));
        Deque<Class<?>> pending = new ArrayDeque<>(List.of(NodeConstant.class));
        Set<Class<?>> seen = new HashSet<>();

        while (!pending.isEmpty()) {
            Class<?> type = pending.pop();
            if (!seen.add(type)) {
                continue;
            }
            Class<?>[] permitted = type.getPermittedSubclasses();
            if (permitted == null || permitted.length == 0) {
                names.add(type.getSimpleName());
            } else {
                pending.addAll(List.of(permitted));
            }
        }
        return names;
    }
}
