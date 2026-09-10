package uk.co.ryanharrison.mathengine.parser.spec;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;

/**
 * Engine configuration overrides declared by a spec suite or an individual spec case.
 * <p>
 * Every field is a boxed type so that {@code null} means "leave the engine default
 * alone". A case-level config completely replaces the suite-level one rather than
 * merging with it, so a case that overrides anything must state everything it needs.
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
public record SpecConfig(
        String angleUnit,
        Boolean implicitMultiplication,
        Boolean vectorsEnabled,
        Boolean matricesEnabled,
        Boolean comprehensionsEnabled,
        Boolean lambdasEnabled,
        Boolean userDefinedVariablesEnabled,
        Boolean userDefinedFunctionsEnabled,
        Boolean unitsEnabled,
        Integer maxRecursionDepth,
        Integer maxExpressionDepth,
        Integer maxVectorSize,
        Integer maxMatrixDimension,
        Integer maxIdentifierLength,
        Integer maxLiteralDigits,
        Boolean forceDoubleArithmetic,
        Integer decimalPlaces,
        Boolean silentValidation
) {

    /**
     * Builds a {@link MathEngineConfig} by applying every non-null override to the
     * engine defaults.
     */
    public MathEngineConfig toMathEngineConfig() {
        MathEngineConfig.Builder builder = MathEngineConfig.builder();

        applyIfPresent(angleUnit, value -> builder.angleUnit(parseAngleUnit(value)));
        applyIfPresent(implicitMultiplication, builder::implicitMultiplication);
        applyIfPresent(vectorsEnabled, builder::vectorsEnabled);
        applyIfPresent(matricesEnabled, builder::matricesEnabled);
        applyIfPresent(comprehensionsEnabled, builder::comprehensionsEnabled);
        applyIfPresent(lambdasEnabled, builder::lambdasEnabled);
        applyIfPresent(userDefinedVariablesEnabled, builder::userDefinedVariablesEnabled);
        applyIfPresent(userDefinedFunctionsEnabled, builder::userDefinedFunctionsEnabled);
        applyIfPresent(unitsEnabled, builder::unitsEnabled);
        applyIfPresent(maxRecursionDepth, builder::maxRecursionDepth);
        applyIfPresent(maxExpressionDepth, builder::maxExpressionDepth);
        applyIfPresent(maxVectorSize, builder::maxVectorSize);
        applyIfPresent(maxMatrixDimension, builder::maxMatrixDimension);
        applyIfPresent(maxIdentifierLength, builder::maxIdentifierLength);
        applyIfPresent(maxLiteralDigits, builder::maxLiteralDigits);
        applyIfPresent(forceDoubleArithmetic, builder::forceDoubleArithmetic);
        applyIfPresent(decimalPlaces, builder::decimalPlaces);
        applyIfPresent(silentValidation, builder::silentValidation);

        return builder.build();
    }

    private static <T> void applyIfPresent(T value, java.util.function.Consumer<T> apply) {
        if (value != null) {
            apply.accept(value);
        }
    }

    private static AngleUnit parseAngleUnit(String value) {
        return switch (value.toUpperCase()) {
            case "DEGREES" -> AngleUnit.DEGREES;
            case "RADIANS" -> AngleUnit.RADIANS;
            default -> throw new IllegalArgumentException("Unknown angle unit: " + value);
        };
    }
}
