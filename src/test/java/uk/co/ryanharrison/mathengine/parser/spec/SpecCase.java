package uk.co.ryanharrison.mathengine.parser.spec;

/**
 * One executable case from a spec file.
 * <p>
 * A case is either a value case or an error case. A value case states the result and
 * its exact node type; an error case names the exception it must raise. Both spellings
 * are checked by {@link SpecIntegrityTest}, so a case cannot quietly assert nothing.
 *
 * <h2>Value case:</h2>
 * <pre>{@code
 * {
 *   "id": "add_rational_integers",
 *   "input": "1 + 2",
 *   "expected": 3,
 *   "expectedType": "NodeRational",
 *   "notes": "Integer addition stays exact"
 * }
 * }</pre>
 *
 * <h2>Error case:</h2>
 * <pre>{@code
 * {
 *   "id": "divide_by_zero_rational",
 *   "input": "1 / 0",
 *   "expectError": true,
 *   "expectedErrorType": "ArithmeticException",
 *   "notes": "Exact division by zero has no representable result"
 * }
 * }</pre>
 *
 * @param id                   globally unique, human-readable identifier
 * @param input                the expression to evaluate
 * @param expected             the expected value, in the JSON encoding described by {@link SpecValueAssertions}
 * @param expectedType         the simple class name of the expected result node
 * @param evaluationOrder      an equivalent, explicitly parenthesised expression whose parse tree must match
 * @param notes                why the case exists and what it pins down
 * @param expectError          true when evaluation must throw
 * @param expectedErrorType    the simple class name of the expected exception
 * @param expectedErrorMessage a substring the exception message must contain
 * @param config               engine configuration overrides for this case
 * @param tolerance            absolute tolerance for numeric comparison
 * @param skip                 a reason to skip, or null to run
 */
public record SpecCase(
        String id,
        String input,
        Object expected,
        String expectedType,
        String evaluationOrder,
        String notes,
        Boolean expectError,
        String expectedErrorType,
        String expectedErrorMessage,
        SpecConfig config,
        Double tolerance,
        String skip
) {

    public boolean shouldSkip() {
        return skip != null && !skip.isBlank();
    }

    public boolean shouldExpectError() {
        return Boolean.TRUE.equals(expectError);
    }

    public boolean hasTolerance() {
        return tolerance != null;
    }

    @Override
    public String toString() {
        return shouldExpectError()
                ? String.format("%s: '%s' -> throws %s", id, input, expectedErrorType)
                : String.format("%s: '%s' -> %s (%s)", id, input, expected, expectedType);
    }
}
