package uk.co.ryanharrison.mathengine.parser;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class JsonTestSuite {

    @JsonProperty("category")
    private String category;

    @JsonProperty("description")
    private String description;

    @JsonProperty("defaultConfig")
    private TestConfig defaultConfig;

    @JsonProperty("tests")
    private List<JsonTestCase> tests;

    // Default constructor for Jackson
    public JsonTestSuite() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TestConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(TestConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    /**
     * Check if this suite has default configuration.
     */
    public boolean hasDefaultConfig() {
        return defaultConfig != null;
    }

    public List<JsonTestCase> getTests() {
        return tests;
    }

    public void setTests(List<JsonTestCase> tests) {
        this.tests = tests;
    }
}
