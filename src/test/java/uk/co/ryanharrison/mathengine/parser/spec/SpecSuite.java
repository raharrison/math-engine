package uk.co.ryanharrison.mathengine.parser.spec;

import java.util.List;

/**
 * One spec file: a named group of {@link SpecCase}s sharing an optional default
 * configuration.
 *
 * <h2>File shape:</h2>
 * <pre>{@code
 * {
 *   "category": "Trigonometric Functions",
 *   "description": "sin, cos and tan over the unit circle, in both angle units",
 *   "defaultConfig": { "angleUnit": "DEGREES" },
 *   "tests": [ ... ]
 * }
 * }</pre>
 *
 * @param category      short display name, shown as the JUnit container name
 * @param description   what the file covers; required, so every file explains itself
 * @param defaultConfig configuration applied to cases that declare none of their own
 * @param tests         the cases, in the order they should be read
 */
public record SpecSuite(
        String category,
        String description,
        SpecConfig defaultConfig,
        List<SpecCase> tests
) {

    public List<SpecCase> tests() {
        return tests == null ? List.of() : tests;
    }
}
