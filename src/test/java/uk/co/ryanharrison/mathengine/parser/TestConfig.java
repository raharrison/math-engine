package uk.co.ryanharrison.mathengine.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public final class TestConfig {

    @JsonProperty("angleUnit")
    private String angleUnit;

    @JsonProperty("implicitMultiplication")
    private Boolean implicitMultiplication;

    @JsonProperty("vectorsEnabled")
    private Boolean vectorsEnabled;

    @JsonProperty("matricesEnabled")
    private Boolean matricesEnabled;

    @JsonProperty("comprehensionsEnabled")
    private Boolean comprehensionsEnabled;

    @JsonProperty("lambdasEnabled")
    private Boolean lambdasEnabled;

    @JsonProperty("userDefinedFunctionsEnabled")
    private Boolean userDefinedFunctionsEnabled;

    @JsonProperty("unitsEnabled")
    private Boolean unitsEnabled;

    @JsonProperty("maxRecursionDepth")
    private Integer maxRecursionDepth;

    @JsonProperty("forceDoubleArithmetic")
    private Boolean forceDoubleArithmetic;

    @JsonProperty("decimalPlaces")
    private Integer decimalPlaces;

    // Default constructor for Jackson
    public TestConfig() {
    }

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

    // Getters and setters

    public String getAngleUnit() {
        return angleUnit;
    }

    public void setAngleUnit(String angleUnit) {
        this.angleUnit = angleUnit;
    }

    public Boolean getImplicitMultiplication() {
        return implicitMultiplication;
    }

    public void setImplicitMultiplication(Boolean implicitMultiplication) {
        this.implicitMultiplication = implicitMultiplication;
    }

    public Boolean getVectorsEnabled() {
        return vectorsEnabled;
    }

    public void setVectorsEnabled(Boolean vectorsEnabled) {
        this.vectorsEnabled = vectorsEnabled;
    }

    public Boolean getMatricesEnabled() {
        return matricesEnabled;
    }

    public void setMatricesEnabled(Boolean matricesEnabled) {
        this.matricesEnabled = matricesEnabled;
    }

    public Boolean getComprehensionsEnabled() {
        return comprehensionsEnabled;
    }

    public void setComprehensionsEnabled(Boolean comprehensionsEnabled) {
        this.comprehensionsEnabled = comprehensionsEnabled;
    }

    public Boolean getLambdasEnabled() {
        return lambdasEnabled;
    }

    public void setLambdasEnabled(Boolean lambdasEnabled) {
        this.lambdasEnabled = lambdasEnabled;
    }

    public Boolean getUserDefinedFunctionsEnabled() {
        return userDefinedFunctionsEnabled;
    }

    public void setUserDefinedFunctionsEnabled(Boolean userDefinedFunctionsEnabled) {
        this.userDefinedFunctionsEnabled = userDefinedFunctionsEnabled;
    }

    public Boolean getUnitsEnabled() {
        return unitsEnabled;
    }

    public void setUnitsEnabled(Boolean unitsEnabled) {
        this.unitsEnabled = unitsEnabled;
    }

    public Integer getMaxRecursionDepth() {
        return maxRecursionDepth;
    }

    public void setMaxRecursionDepth(Integer maxRecursionDepth) {
        this.maxRecursionDepth = maxRecursionDepth;
    }

    public Boolean getForceDoubleArithmetic() {
        return forceDoubleArithmetic;
    }

    public void setForceDoubleArithmetic(Boolean forceDoubleArithmetic) {
        this.forceDoubleArithmetic = forceDoubleArithmetic;
    }

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(Integer decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }
}
