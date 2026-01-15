package uk.co.ryanharrison.mathengine.parser;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class JsonTestCase {

    @JsonProperty("id")
    private String id;

    @JsonProperty("input")
    private String input;

    @JsonProperty("expected")
    private Object expected;

    @JsonProperty("expectedType")
    private String expectedType;

    @JsonProperty("evaluationOrder")
    private String evaluationOrder;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("skipTest")
    private Boolean skipTest;

    @JsonProperty("expectError")
    private Boolean expectError;

    @JsonProperty("expectedErrorType")
    private String expectedErrorType;

    @JsonProperty("config")
    private TestConfig config;

    @JsonProperty("tolerance")
    private Double tolerance;

    // Default constructor for Jackson
    public JsonTestCase() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Object getExpected() {
        return expected;
    }

    public void setExpected(Object expected) {
        this.expected = expected;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public void setExpectedType(String expectedType) {
        this.expectedType = expectedType;
    }

    public String getEvaluationOrder() {
        return evaluationOrder;
    }

    public void setEvaluationOrder(String evaluationOrder) {
        this.evaluationOrder = evaluationOrder;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getSkipTest() {
        return skipTest;
    }

    public void setSkipTest(Boolean skipTest) {
        this.skipTest = skipTest;
    }

    public boolean shouldSkip() {
        return Boolean.TRUE.equals(skipTest);
    }

    public Boolean getExpectError() {
        return expectError;
    }

    public void setExpectError(Boolean expectError) {
        this.expectError = expectError;
    }

    public boolean shouldExpectError() {
        return Boolean.TRUE.equals(expectError);
    }

    public String getExpectedErrorType() {
        return expectedErrorType;
    }

    public void setExpectedErrorType(String expectedErrorType) {
        this.expectedErrorType = expectedErrorType;
    }

    public TestConfig getConfig() {
        return config;
    }

    public void setConfig(TestConfig config) {
        this.config = config;
    }

    /**
     * Check if this test has configuration overrides.
     */
    public boolean hasConfig() {
        return config != null;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public void setTolerance(Double tolerance) {
        this.tolerance = tolerance;
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
