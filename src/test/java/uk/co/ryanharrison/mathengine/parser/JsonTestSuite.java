package uk.co.ryanharrison.mathengine.parser;

import java.util.List;

/**
 * Represents a test suite (collection of test cases) loaded from JSON.
 * <p>
 * Supports suite-level configuration defaults that apply to all tests in the suite.
 * Individual test cases can override suite defaults with their own config.
 *
 * <h2>Usage in JSON:</h2>
 * <pre>{@code
 * {
 *   "category": "Trigonometric Functions (Degrees)",
 *   "description": "Tests for trig functions using degree mode",
 *   "defaultConfig": {
 *     "angleUnit": "DEGREES"
 *   },
 *   "tests": [
 *     { "id": "sin_001", "input": "sin(90)", "expected": 1.0 }
 *   ]
 * }
 * }</pre>
 */
public record JsonTestSuite(
        String category,
        String description,
        TestConfig defaultConfig,
        List<JsonTestCase> tests
) {
}
