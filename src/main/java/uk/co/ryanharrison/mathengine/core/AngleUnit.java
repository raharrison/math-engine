package uk.co.ryanharrison.mathengine.core;

/**
 * Represents the unit used for angle measurements in trigonometric functions.
 */
public enum AngleUnit {
    /**
     * Angles measured in radians (default for most mathematical contexts).
     */
    RADIANS,

    /**
     * Angles measured in degrees (common in engineering and everyday use).
     */
    DEGREES;

    /**
     * Convert an angle from this unit to radians.
     */
    public double toRadians(double angle) {
        return switch (this) {
            case RADIANS -> angle;
            case DEGREES -> Math.toRadians(angle);
        };
    }

    /**
     * Convert an angle from radians to this unit.
     */
    public double fromRadians(double radians) {
        return switch (this) {
            case RADIANS -> radians;
            case DEGREES -> Math.toDegrees(radians);
        };
    }
}
