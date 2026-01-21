package uk.co.ryanharrison.mathengine.parser.util;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formats evaluation results according to configuration settings.
 * <p>
 * This utility handles end-formatting of results, applying the {@code decimalPlaces}
 * configuration setting. Internal calculations always use full precision; formatting
 * is only applied when converting to output strings.
 * </p>
 *
 * <h2>Design Rationale:</h2>
 * <ul>
 *     <li>Nodes remain config-agnostic - they don't need configuration to work</li>
 *     <li>Full precision is preserved internally for chained calculations</li>
 *     <li>Formatting is applied only at output time</li>
 *     <li>{@code decimalPlaces = -1} means full precision (no rounding)</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * MathEngine engine = MathEngine.builder()
 *     .config(MathEngineConfig.builder().decimalPlaces(4).build())
 *     .build();
 *
 * NodeConstant result = engine.evaluate("pi");
 * String formatted = engine.format(result);  // "3.1416"
 * }</pre>
 */
public final class ResultFormatter {

    private ResultFormatter() {
    }

    /**
     * Formats a result according to the configuration's decimal places setting.
     *
     * @param result the result to format
     * @param config the configuration containing formatting settings
     * @return the formatted string representation
     */
    public static String format(NodeConstant result, MathEngineConfig config) {
        int decimalPlaces = config.decimalPlaces();
        return format(result, decimalPlaces);
    }

    /**
     * Formats a result with a specific number of decimal places.
     *
     * @param result        the result to format
     * @param decimalPlaces the number of decimal places (-1 for full precision)
     * @return the formatted string representation
     */
    public static String format(NodeConstant result, int decimalPlaces) {
        if (result == null) {
            return "null";
        }

        // Full precision mode - use node's natural toString
        if (decimalPlaces < 0) {
            return result.toString();
        }

        // Type-specific formatting
        return switch (result) {
            case NodeDouble nodeDouble -> formatDouble(nodeDouble.getValue(), decimalPlaces);
            case NodeRational nodeRational -> formatRational(nodeRational, decimalPlaces);
            case NodeUnit nodeUnit -> formatUnit(nodeUnit, decimalPlaces);
            case NodePercent nodePercent -> formatPercent(nodePercent, decimalPlaces);
            case NodeVector nodeVector -> formatVector(nodeVector, decimalPlaces);
            case NodeMatrix nodeMatrix -> formatMatrix(nodeMatrix, decimalPlaces);
            default -> result.toString();
        };

    }

    /**
     * Formats a double value with the specified decimal places.
     */
    private static String formatDouble(double value, int decimalPlaces) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }

        if (decimalPlaces == 0) {
            return String.valueOf(Math.round(value));
        }

        try {
            BigDecimal bd = BigDecimal.valueOf(value);
            bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);
            return bd.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return String.valueOf(value);
        }
    }

    /**
     * Formats a rational number with the specified decimal places.
     * If the rational can be expressed as a simple fraction, returns that.
     * Otherwise, converts to decimal.
     */
    private static String formatRational(NodeRational nodeRational, int decimalPlaces) {
        var value = nodeRational.getValue();

        // If it's a whole number, return as integer
        if (value.isInteger()) {
            return value.toString();
        }

        // For decimals, round to specified precision
        return formatDouble(value.doubleValue(), decimalPlaces);
    }

    /**
     * Formats a unit value with the specified decimal places.
     */
    private static String formatUnit(NodeUnit nodeUnit, int decimalPlaces) {
        double value = nodeUnit.getValue();
        String formattedValue = formatDouble(value, decimalPlaces);

        // Parse back to get the display value for pluralization
        double displayValue;
        try {
            displayValue = Double.parseDouble(formattedValue);
        } catch (NumberFormatException e) {
            displayValue = value;
        }

        return formattedValue + " " + nodeUnit.getUnit().getDisplayName(displayValue);
    }

    /**
     * Formats a percent value with the specified decimal places.
     */
    private static String formatPercent(NodePercent nodePercent, int decimalPlaces) {
        // Percent stores value as decimal (0.5 = 50%), display as percentage
        double displayValue = nodePercent.getValue() * 100;
        return formatDouble(displayValue, decimalPlaces) + "%";
    }

    /**
     * Formats a vector with the specified decimal places.
     */
    private static String formatVector(NodeVector vector, int decimalPlaces) {
        Node[] elements = vector.getElements();
        var sb = new StringBuilder("{");

        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (elements[i] instanceof NodeConstant constant) {
                sb.append(format(constant, decimalPlaces));
            } else {
                sb.append(elements[i].toString());
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Formats a matrix with the specified decimal places.
     */
    private static String formatMatrix(NodeMatrix matrix, int decimalPlaces) {
        Node[][] elements = matrix.getElements();
        var sb = new StringBuilder("[");

        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                sb.append("; ");
            }
            for (int j = 0; j < elements[i].length; j++) {
                if (j > 0) {
                    sb.append(", ");
                }
                if (elements[i][j] instanceof NodeConstant constant) {
                    sb.append(format(constant, decimalPlaces));
                } else {
                    sb.append(elements[i][j].toString());
                }
            }
        }

        sb.append("]");
        return sb.toString();
    }
}
