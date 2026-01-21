package uk.co.ryanharrison.mathengine.parser;

/**
 * Represents a test case loaded from JSON.
 * <p>
 * Maps directly to the JSON structure defined in GRAMMAR_TESTS.md.
 * Supports configuration overrides for testing specific engine behaviors.
 *
 * <h2>Configuration Support:</h2>
 * Tests can override MathEngineConfig defaults via the "config" object:
 * <pre>{@code
 * {
 *   "id": "test_001",
 *   "input": "sin(90)",
 *   "expected": 1.0,
 *   "expectedType": "NodeDouble",
 *   "config": {
 *     "angleUnit": "DEGREES"
 *   }
 * }
 * }</pre>
 *
 * <h2>Supported Config Options:</h2>
 * <ul>
 *     <li>angleUnit: "RADIANS" | "DEGREES" | "GRADIANS"</li>
 *     <li>implicitMultiplication: boolean</li>
 *     <li>vectorsEnabled: boolean</li>
 *     <li>matricesEnabled: boolean</li>
 *     <li>comprehensionsEnabled: boolean</li>
 *     <li>lambdasEnabled: boolean</li>
 *     <li>userDefinedFunctionsEnabled: boolean</li>
 *     <li>unitsEnabled: boolean</li>
 *     <li>maxRecursionDepth: int</li>
 * </ul>
 */
public record JsonTestCase(
        String id,
        String input,
        Object expected,
        String expectedType,
        String evaluationOrder,
        String notes,
        Boolean skipTest,
        Boolean expectError,
        String expectedErrorType,
        TestConfig config,
        Double tolerance
) {

    /**
     * Check if this test should be skipped.
     */
    public boolean shouldSkip() {
        return Boolean.TRUE.equals(skipTest);
    }

    /**
     * Check if this test expects an error.
     */
    public boolean shouldExpectError() {
        return Boolean.TRUE.equals(expectError);
    }

    /**
     * Check if this test has a custom tolerance.
     */
    public boolean hasTolerance() {
        return tolerance != null;
    }

    @Override
    public String toString() {
        return String.format("%s: '%s' -> %s (%s)", id, input, expected, expectedType);
    }
}
