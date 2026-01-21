package uk.co.ryanharrison.mathengine.parser;

import uk.co.ryanharrison.mathengine.core.AngleUnit;

/**
 * Configuration overrides for individual test cases.
 * <p>
 * Only the fields that are explicitly set will override the defaults.
 * Null values indicate "use default".
 *
 * <h2>Usage in JSON:</h2>
 * <pre>{@code
 * {
 *   "config": {
 *     "angleUnit": "DEGREES",
 *     "implicitMultiplication": false
 *   }
 * }
 * }</pre>
 */
public record TestConfig(
        String angleUnit,
        Boolean implicitMultiplication,
        Boolean vectorsEnabled,
        Boolean matricesEnabled,
        Boolean comprehensionsEnabled,
        Boolean lambdasEnabled,
        Boolean userDefinedFunctionsEnabled,
        Boolean unitsEnabled,
        Integer maxRecursionDepth,
        Boolean forceDoubleArithmetic,
        Integer decimalPlaces
) {

    /**
     * Builds a MathEngineConfig by applying this TestConfig's overrides to defaults.
     *
     * @return configured MathEngineConfig
     */
    public MathEngineConfig toMathEngineConfig() {
        MathEngineConfig.Builder builder = MathEngineConfig.builder();

        if (angleUnit != null) {
            builder.angleUnit(parseAngleUnit(angleUnit));
        }
        if (implicitMultiplication != null) {
            builder.implicitMultiplication(implicitMultiplication);
        }
        if (vectorsEnabled != null) {
            builder.vectorsEnabled(vectorsEnabled);
        }
        if (matricesEnabled != null) {
            builder.matricesEnabled(matricesEnabled);
        }
        if (comprehensionsEnabled != null) {
            builder.comprehensionsEnabled(comprehensionsEnabled);
        }
        if (lambdasEnabled != null) {
            builder.lambdasEnabled(lambdasEnabled);
        }
        if (userDefinedFunctionsEnabled != null) {
            builder.userDefinedFunctionsEnabled(userDefinedFunctionsEnabled);
        }
        if (unitsEnabled != null) {
            builder.unitsEnabled(unitsEnabled);
        }
        if (maxRecursionDepth != null) {
            builder.maxRecursionDepth(maxRecursionDepth);
        }
        if (forceDoubleArithmetic != null) {
            builder.forceDoubleArithmetic(forceDoubleArithmetic);
        }
        if (decimalPlaces != null) {
            builder.decimalPlaces(decimalPlaces);
        }

        return builder.build();
    }

    private AngleUnit parseAngleUnit(String value) {
        return switch (value.toUpperCase()) {
            case "DEGREES" -> AngleUnit.DEGREES;
            default -> AngleUnit.RADIANS;
        };
    }
}
