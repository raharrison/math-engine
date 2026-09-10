package uk.co.ryanharrison.mathengine.parser.spec;

import org.assertj.core.data.Offset;
import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.format.StringNodeFormatter;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compares an evaluated {@link NodeConstant} against the {@code expected} value written
 * in a spec file.
 *
 * <h2>How a value is written in JSON</h2>
 * <table border="1">
 *     <caption>Expected value encodings</caption>
 *     <tr><th>Result type</th><th>JSON form</th><th>Example</th></tr>
 *     <tr><td>{@code NodeRational}</td><td>number, or a string {@code "num/den"}</td><td>{@code 3}, {@code "1/3"}</td></tr>
 *     <tr><td>{@code NodeDouble}</td><td>number</td><td>{@code 0.5}</td></tr>
 *     <tr><td>{@code NodeBoolean}</td><td>boolean</td><td>{@code true}</td></tr>
 *     <tr><td>{@code NodeString}</td><td>string</td><td>{@code "hello"}</td></tr>
 *     <tr><td>{@code NodePercent}</td><td>string ending in {@code %}</td><td>{@code "50%"}</td></tr>
 *     <tr><td>{@code NodeUnit}</td><td>string {@code "value unit"}</td><td>{@code "328.084 feet"}</td></tr>
 *     <tr><td>{@code NodeVector}</td><td>array</td><td>{@code [1, 2, 3]}</td></tr>
 *     <tr><td>{@code NodeMatrix}</td><td>array of arrays</td><td>{@code [[1, 2], [3, 4]]}</td></tr>
 *     <tr><td>{@code NodeRange}</td><td>string in range syntax</td><td>{@code "1..10"}</td></tr>
 *     <tr><td>{@code NodeFunction}, {@code NodeLambda}</td><td>string, the formatted form</td><td>{@code "x -> (x ^ 2)"}</td></tr>
 * </table>
 *
 * <p>A rational written as {@code "num/den"} is compared exactly rather than through a
 * double, which is the only way a fixture can pin down that a result really did stay
 * exact.
 */
final class SpecValueAssertions {

    /**
     * The one loose spelling: a case that genuinely does not care which numeric type it gets.
     */
    static final String ANY_NUMBER = "NodeNumber";

    private static final StringNodeFormatter FORMATTER = StringNodeFormatter.fullPrecision();

    private SpecValueAssertions() {
    }

    /**
     * Asserts the result has exactly the expected node type.
     *
     * @param expectedType simple class name, or {@link #ANY_NUMBER} for any numeric type
     */
    static void assertType(NodeConstant result, String expectedType, String testId) {
        if (ANY_NUMBER.equals(expectedType) && result.isNumeric()) {
            return;
        }
        assertThat(result.getClass().getSimpleName())
                .as("Case %s: result type", testId)
                .isEqualTo(expectedType);
    }

    /**
     * Asserts the result equals the expected value written in the spec file.
     *
     * @param expected the raw JSON value: a number, boolean, string or list
     */
    static void assertValue(NodeConstant result, Object expected, String expectedType,
                            String testId, double tolerance) {
        switch (expected) {
            case List<?> list -> assertCollection(result, list, expectedType, testId, tolerance);
            case Boolean bool -> assertBoolean(result, bool, testId);
            case Number number -> assertNumeric(result, number.doubleValue(), testId, tolerance);
            case String text -> assertFromString(result, text, expectedType, testId, tolerance);
            default -> throw new AssertionError(String.format(
                    "Case %s: cannot express a %s as an expected value; use a number, boolean, string or array",
                    testId, expected.getClass().getSimpleName()));
        }
    }

    // ==================== Strings, which stand in for the richer types ====================

    private static void assertFromString(NodeConstant result, String expected, String expectedType,
                                         String testId, double tolerance) {
        switch (expectedType == null ? "" : expectedType) {
            case "NodeUnit" -> assertUnit(result, expected, testId, tolerance);
            case "NodePercent" -> assertPercent(result, expected, testId, tolerance);
            case "NodeString" -> assertString(result, expected, testId);
            case "NodeRational" -> assertRational(result, expected, testId);
            case "NodeBoolean" -> assertBoolean(result, Boolean.parseBoolean(expected), testId);
            case "NodeDouble", ANY_NUMBER -> assertNumeric(result, Double.parseDouble(expected), testId, tolerance);
            // NodeRange, NodeLambda, NodeFunction and anything else compare on their formatted form
            default -> assertFormatted(result, expected, testId);
        }
    }

    private static void assertString(NodeConstant result, String expected, String testId) {
        assertThat(result)
                .as("Case %s: expected a string", testId)
                .isInstanceOf(NodeString.class);
        assertThat(((NodeString) result).getValue())
                .as("Case %s: string value", testId)
                .isEqualTo(expected);
    }

    /**
     * A rational written as {@code "num/den"} is held to exact equality.
     */
    private static void assertRational(NodeConstant result, String expected, String testId) {
        assertThat(result)
                .as("Case %s: expected an exact rational", testId)
                .isInstanceOf(NodeRational.class);

        BigRational actual = ((NodeRational) result).getValue();
        assertThat(actual)
                .as("Case %s: exact value (expected %s, actual %s)", testId, expected, actual)
                .isEqualTo(parseRational(expected));
    }

    /**
     * Parsed through {@link BigDecimal} so that "0.1" means a tenth, not the nearest double.
     */
    private static BigRational parseRational(String text) {
        int slash = text.indexOf('/');
        if (slash < 0) {
            return BigRational.of(new BigDecimal(text.trim()));
        }
        return BigRational.of(new BigDecimal(text.substring(0, slash).trim()))
                .divide(BigRational.of(new BigDecimal(text.substring(slash + 1).trim())));
    }

    private static void assertUnit(NodeConstant result, String expected, String testId, double tolerance) {
        assertThat(result)
                .as("Case %s: expected a unit quantity", testId)
                .isInstanceOf(NodeUnit.class);

        NodeUnit unit = (NodeUnit) result;
        String[] parts = expected.trim().split("\\s+", 2);

        assertThat(unit.getValue())
                .as("Case %s: unit magnitude", testId)
                .isCloseTo(Double.parseDouble(parts[0]), Offset.offset(tolerance));

        if (parts.length == 2) {
            assertThat(unit.getUnit().getDisplayName(unit.getValue()))
                    .as("Case %s: unit name", testId)
                    .isEqualTo(parts[1]);
        }
    }

    private static void assertPercent(NodeConstant result, String expected, String testId, double tolerance) {
        assertThat(result)
                .as("Case %s: expected a percentage", testId)
                .isInstanceOf(NodePercent.class);

        String text = expected.trim();
        double expectedValue = Double.parseDouble(
                text.endsWith("%") ? text.substring(0, text.length() - 1) : text);

        assertThat(((NodePercent) result).getPercentValue())
                .as("Case %s: percentage", testId)
                .isCloseTo(expectedValue, Offset.offset(tolerance));
    }

    private static void assertFormatted(NodeConstant result, String expected, String testId) {
        assertThat(FORMATTER.format(result))
                .as("Case %s: formatted result", testId)
                .isEqualTo(expected);
    }

    // ==================== Scalars ====================

    private static void assertNumeric(NodeConstant result, double expected, String testId, double tolerance) {
        assertThat(result.isNumeric())
                .as("Case %s: expected a numeric result, got %s", testId, result.typeName())
                .isTrue();

        double actual = result.doubleValue();
        if (Double.isNaN(expected)) {
            assertThat(actual).as("Case %s: expected NaN", testId).isNaN();
        } else if (Double.isInfinite(expected)) {
            assertThat(actual).as("Case %s: expected %s", testId, expected).isEqualTo(expected);
        } else {
            assertThat(actual)
                    .as("Case %s: value", testId)
                    .isCloseTo(expected, Offset.offset(tolerance));
        }
    }

    private static void assertBoolean(NodeConstant result, boolean expected, String testId) {
        if (result instanceof NodeBoolean bool) {
            assertThat(bool.getValue()).as("Case %s: boolean value", testId).isEqualTo(expected);
        } else {
            assertThat(result.doubleValue())
                    .as("Case %s: boolean as a number", testId)
                    .isEqualTo(expected ? 1.0 : 0.0);
        }
    }

    // ==================== Collections ====================

    private static void assertCollection(NodeConstant result, List<?> expected, String expectedType,
                                         String testId, double tolerance) {
        if ("NodeMatrix".equals(expectedType)) {
            assertMatrix(result, expected, testId, tolerance);
        } else {
            assertVector(result, expected, testId, tolerance);
        }
    }

    private static void assertMatrix(NodeConstant result, List<?> expected, String testId, double tolerance) {
        assertThat(result)
                .as("Case %s: expected a matrix, got %s", testId, result.typeName())
                .isInstanceOf(NodeMatrix.class);

        NodeMatrix matrix = (NodeMatrix) result;
        assertThat(matrix.getRows()).as("Case %s: matrix row count", testId).isEqualTo(expected.size());

        for (int i = 0; i < expected.size(); i++) {
            if (!(expected.get(i) instanceof List<?> row)) {
                throw new AssertionError(String.format(
                        "Case %s: matrix row %d must be written as an array", testId, i));
            }
            assertThat(matrix.getCols())
                    .as("Case %s: column count of row %d", testId, i)
                    .isEqualTo(row.size());
            for (int j = 0; j < row.size(); j++) {
                assertElement((NodeConstant) matrix.getElement(i, j), row.get(j),
                        testId + "[" + i + "," + j + "]", tolerance);
            }
        }
    }

    private static void assertVector(NodeConstant result, List<?> expected, String testId, double tolerance) {
        assertThat(result)
                .as("Case %s: expected a vector, got %s", testId, result.typeName())
                .isInstanceOf(NodeVector.class);

        NodeVector vector = (NodeVector) result;
        assertThat(vector.size()).as("Case %s: vector size", testId).isEqualTo(expected.size());

        for (int i = 0; i < expected.size(); i++) {
            assertElement((NodeConstant) vector.getElement(i), expected.get(i),
                    testId + "[" + i + "]", tolerance);
        }
    }

    /**
     * An element carries no declared type, so its own runtime type decides the comparison.
     */
    private static void assertElement(NodeConstant element, Object expected, String testId, double tolerance) {
        if (expected instanceof List<?> nested) {
            assertCollection(element, nested, element.getClass().getSimpleName(), testId, tolerance);
        } else {
            assertValue(element, expected, element.getClass().getSimpleName(), testId, tolerance);
        }
    }
}
