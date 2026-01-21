package uk.co.ryanharrison.mathengine.parser;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.utils.ResourceScanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Comprehensive end-to-end tests for the MathEngine.
 * <p>
 * Loads test cases from JSON files in both legacy (parser/) and new (engine/) directories.
 * Supports configuration overrides at suite and test case levels.
 *
 * <h2>Test File Structure:</h2>
 * <pre>
 * test/resources/
 * ├── parser/          (legacy tests - kept for compatibility)
 * │   └── *.json
 * └── engine/          (new structured tests)
 *     ├── operators/
 *     ├── functions/
 *     ├── datatypes/
 *     ├── features/
 *     ├── errors/
 *     └── integration/
 * </pre>
 */
public class MathEngineTest {

    private static final double TOLERANCE = 1e-7;

    /**
     * Creates dynamic tests from all JSON test files.
     */
    @TestFactory
    Stream<DynamicContainer> mathEngineTests() {
        List<DynamicContainer> testSuites = new ArrayList<>();

        // Load tests from the "engine" directory and add them as a DynamicContainer
        loadTestsFromDirectory("engine", testSuites);

        // Return the collection of DynamicContainers as a stream
        return testSuites.stream();
    }

    private void loadTestsFromDirectory(String directory, List<DynamicContainer> testSuites) {
        List<String> resourcePaths;
        try {
            resourcePaths = ResourceScanner.listResources(directory, ".json");
        } catch (Exception e) {
            // Directory may not exist yet - that's okay
            return;
        }

        // Iterate over each test suite resource
        for (String resourcePath : resourcePaths) {
            try {
                JsonTestSuite suite = JsonTestLoader.loadFromResource(resourcePath);
                TestConfig suiteConfig = suite.defaultConfig();

                // Build the dynamic tests for this suite
                Stream<DynamicTest> suiteTests = suite.tests().stream().map(testCase -> {
                    String displayName = String.format("%s: %s - %s",
                            testCase.id(),
                            testCase.input(),
                            testCase.notes());

                    return DynamicTest.dynamicTest(displayName, () -> {
                        // Skip tests marked with skipTest: true
                        assumeTrue(!testCase.shouldSkip(), "Test marked as skipped: " + testCase.notes());

                        // Determine config: test case overrides suite defaults
                        MathEngineConfig config = resolveConfig(suiteConfig, testCase.config());

                        executeTest(testCase, config);
                    });
                });

                // Create a container for the test suite with its tests
                DynamicContainer suiteContainer = DynamicContainer.dynamicContainer(
                        suite.category(),
                        suiteTests.toList() // Convert the Stream<DynamicTest> to a List
                );

                // Add the suite container to the list of test suites
                testSuites.add(suiteContainer);

            } catch (IOException e) {
                // Handle test suite loading failure
                testSuites.add(DynamicContainer.dynamicContainer(
                        "Failed to load " + resourcePath,
                        Stream.of(DynamicTest.dynamicTest("Error", () -> {
                            throw new RuntimeException("Failed to load test file: " + resourcePath, e);
                        }))
                ));
            }
        }
    }

    /**
     * Resolves the effective configuration by merging suite and test case configs.
     */
    private MathEngineConfig resolveConfig(TestConfig suiteConfig, TestConfig testConfig) {
        if (testConfig != null) {
            // Test case config takes precedence
            return testConfig.toMathEngineConfig();
        } else if (suiteConfig != null) {
            // Fall back to suite config
            return suiteConfig.toMathEngineConfig();
        } else {
            // Use default config
            return MathEngineConfig.defaults();
        }
    }

    /**
     * Execute a single test case with the given configuration.
     */
    private void executeTest(JsonTestCase testCase, MathEngineConfig config) {
        if (testCase.shouldExpectError()) {
            executeErrorTest(testCase, config);
        } else {
            executeSuccessTest(testCase, config);
        }
    }

    /**
     * Execute a test that expects an error to be thrown.
     */
    private void executeErrorTest(JsonTestCase testCase, MathEngineConfig config) {
        String input = testCase.input();
        String expectedErrorType = testCase.expectedErrorType();

        assertThatThrownBy(() -> parseAndEvaluate(input, config))
                .as("Test %s: Expected error %s", testCase.id(), expectedErrorType)
                .isInstanceOf(getExceptionClass(expectedErrorType));
    }

    /**
     * Execute a test that expects a successful result.
     */
    private void executeSuccessTest(JsonTestCase testCase, MathEngineConfig config) {
        String input = testCase.input();
        Object expected = testCase.expected();
        String expectedType = testCase.expectedType();

        // Get custom tolerance or use default
        double tolerance = testCase.hasTolerance() ? testCase.tolerance() : TOLERANCE;

        // Parse and evaluate the expression
        NodeConstant result = parseAndEvaluate(input, config);

        // Verify the result type matches
        verifyType(result, expectedType, testCase.id());

        // Verify the result value matches
        verifyValue(result, expected, expectedType, testCase.id(), tolerance);
    }

    /**
     * Get the exception class by name.
     */
    private Class<? extends Throwable> getExceptionClass(String errorType) {
        try {
            String[] packages = {
                    "uk.co.ryanharrison.mathengine.core.",
                    "uk.co.ryanharrison.mathengine.parser.evaluator.",
                    "uk.co.ryanharrison.mathengine.parser.parser.",
                    "uk.co.ryanharrison.mathengine.parser.lexer.",
                    "uk.co.ryanharrison.mathengine.parser.",
                    "java.lang."
            };

            for (String pkg : packages) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends Throwable> clazz = (Class<? extends Throwable>) Class.forName(pkg + errorType);
                    return clazz;
                } catch (ClassNotFoundException e) {
                    // Try next package
                }
            }

            return RuntimeException.class;
        } catch (Exception e) {
            return RuntimeException.class;
        }
    }

    /**
     * Parse and evaluate an expression using MathEngine with the given config.
     */
    private NodeConstant parseAndEvaluate(String input, MathEngineConfig config) {
        MathEngine engine = MathEngine.create(config);
        return engine.evaluate(input);
    }

    /**
     * Verify the result type matches the expected type.
     */
    private void verifyType(NodeConstant result, String expectedType, String testId) {
        if (expectedType == null) {
            return; // No type assertion required
        }

        String actualType = result.getClass().getSimpleName();

        // Allow flexible type matching for numeric types
        if (isNumericTypeMatch(expectedType, actualType)) {
            return;
        }

        assertThat(actualType)
                .as("Test %s: Type mismatch", testId)
                .isEqualTo(expectedType);
    }

    /**
     * Check if types are compatible for numeric values.
     */
    private boolean isNumericTypeMatch(String expected, String actual) {
        // NodeRational and NodeDouble are interchangeable for integer values
        if (expected.equals("NodeDouble") && actual.equals("NodeRational")) {
            return true;
        }
        if (expected.equals("NodeRational") && actual.equals("NodeDouble")) {
            return true;
        }
        // NodeNumber matches any numeric type
        if (expected.equals("NodeNumber")) {
            return actual.equals("NodeDouble") || actual.equals("NodeRational") ||
                    actual.equals("NodePercent") || actual.equals("NodeBoolean");
        }
        return false;
    }

    /**
     * Verify the result value matches the expected value.
     */
    private void verifyValue(NodeConstant result, Object expected, String expectedType, String testId, double tolerance) {
        if (expected == null) {
            // For function definitions, we just check that it doesn't throw
            return;
        }

        if (expected instanceof List<?> expectedList) {
            verifyListValue(result, expectedList, expectedType, testId, tolerance);
        } else if (expected instanceof Number) {
            verifyNumericValue(result, (Number) expected, testId, tolerance);
        } else if (expected instanceof Boolean) {
            verifyBooleanValue(result, (Boolean) expected, testId);
        } else if (expected instanceof String expectedStr) {
            // Convert string to appropriate type based on expectedType
            Object convertedValue = convertExpectedValue(expectedStr, expectedType);

            if (convertedValue instanceof Number) {
                verifyNumericValue(result, (Number) convertedValue, testId, tolerance);
            } else if (convertedValue instanceof Boolean) {
                verifyBooleanValue(result, (Boolean) convertedValue, testId);
            } else {
                verifyStringValue(result, expectedStr, expectedType, testId, tolerance);
            }
        } else {
            // Fallback for unknown expected types - try numeric comparison first
            if (result.isNumeric()) {
                try {
                    double expectedValue = Double.parseDouble(expected.toString());
                    verifyNumericValue(result, expectedValue, testId, tolerance);
                    return;
                } catch (NumberFormatException ignored) {
                    // Fall through to type-specific assertion
                }
            }

            // As last resort, fail with informative message about unsupported type
            throw new AssertionError(String.format(
                    "Test %s: Unsupported expected value type: %s (value: %s). " +
                            "Use Number, Boolean, String, or List in test cases.",
                    testId, expected.getClass().getSimpleName(), expected));
        }
    }

    /**
     * Convert expected string value to the appropriate type based on expectedType.
     * Returns the original string if no conversion is needed.
     */
    private Object convertExpectedValue(String expectedStr, String expectedType) {
        if (expectedType == null) {
            return expectedStr;
        }

        try {
            switch (expectedType) {
                case "NodeDouble", "NodeRational", "NodeNumber" -> {
                    return Double.parseDouble(expectedStr);
                }
                case "NodeBoolean" -> {
                    return Boolean.parseBoolean(expectedStr);
                }
                default -> {
                    return expectedStr;
                }
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return original string for string comparison
            return expectedStr;
        }
    }

    private void verifyListValue(NodeConstant result, List<?> expectedList, String expectedType, String testId, double tolerance) {
        // Only treat as matrix if expectedType explicitly says so
        // Otherwise, even nested lists are treated as nested vectors
        if ("NodeMatrix".equals(expectedType)) {
            verifyMatrixValue(result, expectedList, testId, tolerance);
        } else {
            verifyVectorValue(result, expectedList, testId, tolerance);
        }
    }

    private void verifyMatrixValue(NodeConstant result, List<?> expectedList, String testId, double tolerance) {
        assertThat(result)
                .as("Test %s: Expected matrix but got %s", testId, result.getClass().getSimpleName())
                .isInstanceOf(NodeMatrix.class);

        NodeMatrix matrix = (NodeMatrix) result;

        assertThat(matrix.getRows())
                .as("Test %s: Matrix row count mismatch", testId)
                .isEqualTo(expectedList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            Object expectedRow = expectedList.get(i);

            if (expectedRow instanceof List<?> expectedRowList) {
                assertThat(matrix.getCols())
                        .as("Test %s: Matrix column count mismatch in row %d", testId, i)
                        .isEqualTo(expectedRowList.size());

                for (int j = 0; j < expectedRowList.size(); j++) {
                    Object expectedElem = expectedRowList.get(j);
                    NodeConstant actualElem = (NodeConstant) matrix.getElement(i, j);
                    String elemTestId = testId + "[" + i + "," + j + "]";

                    if (expectedElem instanceof Number) {
                        double expectedVal = ((Number) expectedElem).doubleValue();
                        double actualVal = actualElem.doubleValue();
                        assertThat(actualVal)
                                .as("Test %s: Matrix element mismatch", elemTestId)
                                .isCloseTo(expectedVal, Offset.offset(tolerance));
                    } else if (expectedElem instanceof Boolean) {
                        verifyBooleanValue(actualElem, (Boolean) expectedElem, elemTestId);
                    } else if (expectedElem instanceof String expectedStr) {
                        String elemType = actualElem.getClass().getSimpleName();
                        verifyStringValue(actualElem, expectedStr, elemType, elemTestId, tolerance);
                    } else {
                        throw new AssertionError(String.format(
                                "Test %s: Unsupported matrix element type: %s",
                                elemTestId, expectedElem.getClass().getSimpleName()));
                    }
                }
            }
        }
    }

    private void verifyVectorValue(NodeConstant result, List<?> expectedList, String testId, double tolerance) {
        assertThat(result)
                .as("Test %s: Expected vector but got %s", testId, result.getClass().getSimpleName())
                .isInstanceOf(NodeVector.class);

        NodeVector vector = (NodeVector) result;

        assertThat(vector.size())
                .as("Test %s: Vector size mismatch", testId)
                .isEqualTo(expectedList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            Object expectedElem = expectedList.get(i);
            NodeConstant actualElem = (NodeConstant) vector.getElement(i);
            String elemTestId = testId + "[" + i + "]";

            if (expectedElem instanceof Number) {
                double expectedVal = ((Number) expectedElem).doubleValue();
                double actualVal = actualElem.doubleValue();
                assertThat(actualVal)
                        .as("Test %s: Vector element mismatch", elemTestId)
                        .isCloseTo(expectedVal, Offset.offset(tolerance));
            } else if (expectedElem instanceof Boolean) {
                verifyBooleanValue(actualElem, (Boolean) expectedElem, elemTestId);
            } else if (expectedElem instanceof String expectedStr) {
                // Determine element type from actual result
                String elemType = actualElem.getClass().getSimpleName();
                verifyStringValue(actualElem, expectedStr, elemType, elemTestId, tolerance);
            } else if (expectedElem instanceof List) {
                verifyValue(actualElem, expectedElem, "NodeVector", elemTestId, tolerance);
            } else {
                throw new AssertionError(String.format(
                        "Test %s: Unsupported vector element type: %s",
                        elemTestId, expectedElem.getClass().getSimpleName()));
            }
        }
    }

    private void verifyNumericValue(NodeConstant result, Number expected, String testId, double tolerance) {
        double expectedValue = expected.doubleValue();
        double actualValue = result.doubleValue();

        if (Double.isNaN(expectedValue)) {
            assertThat(Double.isNaN(actualValue))
                    .as("Test %s: Expected NaN but got %s", testId, actualValue)
                    .isTrue();
        } else if (Double.isInfinite(expectedValue)) {
            assertThat(actualValue)
                    .as("Test %s: Expected infinity but got %s", testId, actualValue)
                    .isEqualTo(expectedValue);
        } else {
            assertThat(actualValue)
                    .as("Test %s: Value mismatch (expected=%s, actual=%s)", testId, expectedValue, actualValue)
                    .isCloseTo(expectedValue, Offset.offset(tolerance));
        }
    }

    private void verifyBooleanValue(NodeConstant result, Boolean expected, String testId) {
        if (result instanceof NodeBoolean) {
            boolean actualValue = ((NodeBoolean) result).getValue();
            assertThat(actualValue)
                    .as("Test %s: Boolean value mismatch", testId)
                    .isEqualTo(expected);
        } else {
            // Boolean coerced to number
            double actualValue = result.doubleValue();
            double expectedNumeric = expected ? 1.0 : 0.0;
            assertThat(actualValue)
                    .as("Test %s: Boolean as number mismatch", testId)
                    .isCloseTo(expectedNumeric, Offset.offset(TOLERANCE));
        }
    }

    private void verifyStringValue(NodeConstant result, String expected, String expectedType, String testId, double tolerance) {
        if ("NodeUnit".equals(expectedType)) {
            verifyUnitValue(result, expected, testId, tolerance);
        } else if ("NodePercent".equals(expectedType)) {
            verifyPercentValue(result, expected, testId, tolerance);
        } else if ("NodeFunction".equals(expectedType)) {
            verifyFunctionValue(result, expected, testId);
        } else if ("NodeBoolean".equals(expectedType)) {
            boolean expectedBool = Boolean.parseBoolean(expected) ||
                    "1".equals(expected) || "true".equalsIgnoreCase(expected);
            verifyBooleanValue(result, expectedBool, testId);
        } else if ("NodeString".equals(expectedType)) {
            assertThat(result)
                    .as("Test %s: Expected NodeString but got %s", testId, result.getClass().getSimpleName())
                    .isInstanceOf(NodeString.class);
            NodeString str = (NodeString) result;
            assertThat(str.getValue())
                    .as("Test %s: String value mismatch", testId)
                    .isEqualTo(expected);
        } else if ("NodeRational".equals(expectedType) || "NodeDouble".equals(expectedType)) {
            // Parse expected numeric value
            verifyRationalValue(result, expected, testId, tolerance);
        } else {
            // Default: try to parse as numeric, otherwise compare as string
            try {
                double expectedValue = Double.parseDouble(expected);
                verifyNumericValue(result, expectedValue, testId, tolerance);
            } catch (NumberFormatException e) {
                // Not a number - compare string representation as fallback
                assertThat(result.toString())
                        .as("Test %s: String representation mismatch", testId)
                        .isEqualTo(expected);
            }
        }
    }

    /**
     * Verify a NodeUnit value against expected string format "value unitname".
     */
    private void verifyUnitValue(NodeConstant result, String expected, String testId, double tolerance) {
        assertThat(result)
                .as("Test %s: Expected NodeUnit but got %s", testId, result.getClass().getSimpleName())
                .isInstanceOf(NodeUnit.class);

        NodeUnit actualUnit = (NodeUnit) result;

        // Parse expected format: "value unitname" (e.g., "328.084 feet", "100.0 meters")
        String[] parts = expected.trim().split("\\s+", 2);
        if (parts.length == 2) {
            double expectedValue = Double.parseDouble(parts[0]);
            String expectedUnitName = parts[1];

            assertThat(actualUnit.getValue())
                    .as("Test %s: Unit value mismatch", testId)
                    .isCloseTo(expectedValue, Offset.offset(tolerance));

            // Compare unit name (may be singular or plural form)
            String actualUnitName = actualUnit.getUnit().getDisplayName(actualUnit.getValue());
            assertThat(actualUnitName)
                    .as("Test %s: Unit name mismatch", testId)
                    .isEqualTo(expectedUnitName);
        } else {
            // Single value without unit name - just verify the value
            double expectedValue = Double.parseDouble(expected);
            assertThat(actualUnit.getValue())
                    .as("Test %s: Unit value mismatch", testId)
                    .isCloseTo(expectedValue, Offset.offset(tolerance));
        }
    }

    /**
     * Verify a NodePercent value against expected string format "50%" or "50".
     */
    private void verifyPercentValue(NodeConstant result, String expected, String testId, double tolerance) {
        assertThat(result)
                .as("Test %s: Expected NodePercent but got %s", testId, result.getClass().getSimpleName())
                .isInstanceOf(NodePercent.class);

        NodePercent resultPercent = (NodePercent) result;

        // Parse expected percent value (e.g., "50%" -> 50.0, "50" -> 50.0)
        String expectedStr = expected.trim();
        if (expectedStr.endsWith("%")) {
            expectedStr = expectedStr.substring(0, expectedStr.length() - 1);
        }
        double expectedValue = Double.parseDouble(expectedStr);
        double actualValue = resultPercent.getPercentValue();

        assertThat(actualValue)
                .as("Test %s: Percent value mismatch (expected=%s%%, actual=%s%%)", testId, expectedValue, actualValue)
                .isCloseTo(expectedValue, Offset.offset(tolerance));
    }

    /**
     * Verify a NodeFunction value against expected function name.
     */
    private void verifyFunctionValue(NodeConstant result, String expected, String testId) {
        assertThat(result)
                .as("Test %s: Expected NodeFunction but got %s", testId, result.getClass().getSimpleName())
                .isInstanceOf(NodeFunction.class);

        NodeFunction func = (NodeFunction) result;
        String actualName = func.getFunction().name();

        // Expected can be just the function name or "<function:name>" format
        String expectedName = expected;
        if (expected.startsWith("<function:") && expected.endsWith(">")) {
            expectedName = expected.substring(10, expected.length() - 1);
        }

        assertThat(actualName)
                .as("Test %s: Function name mismatch", testId)
                .isEqualTo(expectedName);
    }

    /**
     * Verify a NodeRational value against expected string format "num/den" or "num".
     */
    private void verifyRationalValue(NodeConstant result, String expected, String testId, double tolerance) {
        // NodeRational can be compared numerically
        if (expected.contains("/")) {
            String[] parts = expected.split("/");
            double expectedValue = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
            assertThat(result.doubleValue())
                    .as("Test %s: Rational value mismatch", testId)
                    .isCloseTo(expectedValue, Offset.offset(tolerance));
        } else {
            double expectedValue = Double.parseDouble(expected);
            assertThat(result.doubleValue())
                    .as("Test %s: Rational value mismatch", testId)
                    .isCloseTo(expectedValue, Offset.offset(tolerance));
        }
    }
}
