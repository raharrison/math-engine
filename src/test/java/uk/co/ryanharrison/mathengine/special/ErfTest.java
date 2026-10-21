package uk.co.ryanharrison.mathengine.special;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive test suite for the {@link Erf} utility class.
 * <p>
 * Tests verify mathematical properties of the error function (erf) and
 * complementary error function (erfc), including:
 * </p>
 * <ul>
 *     <li>Known mathematical values and properties</li>
 *     <li>Symmetry and odd function properties</li>
 *     <li>Relationship between erf and erfc</li>
 *     <li>Boundary and edge case behavior</li>
 *     <li>Accuracy of the Abramowitz and Stegun approximation</li>
 * </ul>
 */
class ErfTest {

    /**
     * Tolerance for comparing error function values.
     * The Abramowitz and Stegun approximation provides accuracy to approximately 1.5 × 10⁻⁷.
     * We use 2e-7 to account for the documented maximum error.
     */
    private static final double TOLERANCE = 2e-7;

    /**
     * Relaxed tolerance for extreme values where approximation may have slightly reduced accuracy.
     * At large values (x ≥ 3), the error can be on the order of 1e-5 due to the approximation formula.
     */
    private static final double RELAXED_TOLERANCE = 1e-4;

    // ==================== Basic Properties ====================

    /**
     * Tests that erf(0) = 0, a fundamental property of the error function.
     */
    @Test
    void erfAtZeroIsZero() {
        assertThat(Erf.erf(0.0)).isCloseTo(0.0, within(TOLERANCE));
    }

    /**
     * Tests that erfc(0) = 1, since erfc(x) = 1 - erf(x) and erf(0) = 0.
     */
    @Test
    void erfcAtZeroIsOne() {
        assertThat(Erf.erfc(0.0)).isCloseTo(1.0, within(TOLERANCE));
    }

    /**
     * Tests that erf(x) approaches 1 as x approaches infinity.
     * For practical purposes, erf(x) ≈ 1 for x > 3.
     */
    @Test
    void erfAtLargePositiveValuesApproachesOne() {
        assertThat(Erf.erf(3.0)).isCloseTo(1.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erf(4.0)).isCloseTo(1.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erf(5.0)).isCloseTo(1.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erf(10.0)).isCloseTo(1.0, within(RELAXED_TOLERANCE));
    }

    /**
     * Tests that erf(x) approaches -1 as x approaches negative infinity.
     * For practical purposes, erf(x) ≈ -1 for x < -3.
     */
    @Test
    void erfAtLargeNegativeValuesApproachesNegativeOne() {
        assertThat(Erf.erf(-3.0)).isCloseTo(-1.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erf(-4.0)).isCloseTo(-1.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erf(-5.0)).isCloseTo(-1.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erf(-10.0)).isCloseTo(-1.0, within(RELAXED_TOLERANCE));
    }

    /**
     * Tests that erfc(x) approaches 0 as x approaches infinity.
     */
    @Test
    void erfcAtLargePositiveValuesApproachesZero() {
        assertThat(Erf.erfc(3.0)).isCloseTo(0.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erfc(4.0)).isCloseTo(0.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erfc(5.0)).isCloseTo(0.0, within(RELAXED_TOLERANCE));
    }

    /**
     * Tests that erfc(x) approaches 2 as x approaches negative infinity.
     */
    @Test
    void erfcAtLargeNegativeValuesApproachesTwo() {
        assertThat(Erf.erfc(-3.0)).isCloseTo(2.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erfc(-4.0)).isCloseTo(2.0, within(RELAXED_TOLERANCE));
        assertThat(Erf.erfc(-5.0)).isCloseTo(2.0, within(RELAXED_TOLERANCE));
    }

    // ==================== Odd Function Property ====================

    /**
     * Tests the odd function property: erf(-x) = -erf(x).
     * This is a fundamental property of the error function.
     */
    @ParameterizedTest
    @CsvSource({
            "0.5",
            "1.0",
            "1.5",
            "2.0",
            "2.5",
            "3.0"
    })
    void erfIsOddFunction(double x) {
        double erfPositive = Erf.erf(x);
        double erfNegative = Erf.erf(-x);

        assertThat(erfNegative).isCloseTo(-erfPositive, within(TOLERANCE));
    }

    // ==================== Complementary Relationship ====================

    /**
     * Tests the fundamental relationship: erf(x) + erfc(x) = 1 for all x.
     * This must hold exactly for all values.
     */
    @ParameterizedTest
    @CsvSource({
            "-3.0",
            "-2.5",
            "-2.0",
            "-1.5",
            "-1.0",
            "-0.5",
            "0.0",
            "0.5",
            "1.0",
            "1.5",
            "2.0",
            "2.5",
            "3.0"
    })
    void erfPlusErfcEqualsOne(double x) {
        double sum = Erf.erf(x) + Erf.erfc(x);
        assertThat(sum).isCloseTo(1.0, within(1e-15));  // Should be exact to machine precision
    }

    // ==================== Known Values ====================

    /**
     * Tests erf against known mathematical values computed using high-precision references.
     * These values are taken from standard mathematical tables and verified against
     * multiple sources.
     */
    @ParameterizedTest
    @CsvSource({
            // x,      expected erf(x)
            "0.0,     0.0",
            "0.1,     0.1124629160182849",
            "0.2,     0.2227025892104785",
            "0.3,     0.3286267594591274",
            "0.4,     0.4283923550466685",
            "0.5,     0.5204998778130465",
            "0.6,     0.6038560908479259",
            "0.7,     0.6778011938374185",
            "0.8,     0.7421009647076834",
            "0.9,     0.7969082124228321",
            "1.0,     0.8427007929497149",
            "1.5,     0.9661051464753107",
            "2.0,     0.9953222650189527",
            "2.5,     0.9995930479825550",
            "3.0,     0.9999779095030014"
    })
    void erfKnownValues(double x, double expected) {
        assertThat(Erf.erf(x)).isCloseTo(expected, within(TOLERANCE));
    }

    /**
     * Tests erf with negative values using the odd function property.
     */
    @ParameterizedTest
    @CsvSource({
            // x,      expected erf(x)
            "-0.5,   -0.5204998778130465",
            "-1.0,   -0.8427007929497149",
            "-1.5,   -0.9661051464753107",
            "-2.0,   -0.9953222650189527"
    })
    void erfKnownNegativeValues(double x, double expected) {
        assertThat(Erf.erf(x)).isCloseTo(expected, within(TOLERANCE));
    }

    /**
     * Tests erfc against known mathematical values.
     * These are computed as 1 - erf(x).
     */
    @ParameterizedTest
    @CsvSource({
            // x,      expected erfc(x)
            "0.0,     1.0",
            "0.5,     0.4795001221869535",
            "1.0,     0.1572992070502851",
            "1.5,     0.0338948535246893",
            "2.0,     0.0046777349810473",
            "2.5,     0.0004069520174450",
            "3.0,     0.0000220904969986"
    })
    void erfcKnownValues(double x, double expected) {
        assertThat(Erf.erfc(x)).isCloseTo(expected, within(TOLERANCE));
    }

    @ParameterizedTest
    @CsvSource({
            "0.5, 0.5204998778130465",              // x, erf(x) to machine precision
            "1.0, 0.8427007929497149",
            "2.0, 0.9953222650189527",
            "2.5, 0.9995930479825550",              // where the series hands over
            "3.5, 0.9999992569016276"
    })
    void erfMatchesReferenceToMachinePrecision(double x, double expected) {
        // The former Abramowitz and Stegun approximation was good only to 1.5e-7
        assertThat(Erf.erf(x)).isCloseTo(expected, within(1e-15));
    }

    @ParameterizedTest
    @CsvSource({
            "3.0, 2.209049699858544e-5",            // x, erfc(x)
            "5.0, 1.537459794428035e-12",
            "10.0, 2.088487583762545e-45"
    })
    void erfcKeepsRelativeAccuracyInTheTail(double x, double expected) {
        assertThat(Erf.erfc(x)).isCloseTo(expected, within(expected * 1e-13));
    }

    // ==================== Range Validation ====================

    /**
     * Tests that erf(x) always returns values within the valid range [-1, 1].
     */
    @ParameterizedTest
    @CsvSource({
            "-10.0",
            "-5.0",
            "-3.0",
            "-2.0",
            "-1.0",
            "0.0",
            "1.0",
            "2.0",
            "3.0",
            "5.0",
            "10.0"
    })
    void erfValueIsWithinValidRange(double x) {
        double result = Erf.erf(x);
        assertThat(result).isBetween(-1.0, 1.0);
    }

    /**
     * Tests that erfc(x) always returns values within the valid range [0, 2].
     */
    @ParameterizedTest
    @CsvSource({
            "-10.0",
            "-5.0",
            "-3.0",
            "-2.0",
            "-1.0",
            "0.0",
            "1.0",
            "2.0",
            "3.0",
            "5.0",
            "10.0"
    })
    void erfcValueIsWithinValidRange(double x) {
        double result = Erf.erfc(x);
        assertThat(result).isBetween(0.0, 2.0);
    }

    // ==================== Monotonicity ====================

    /**
     * Tests that erf(x) is monotonically increasing.
     * For any x1 < x2, we must have erf(x1) < erf(x2).
     */
    @Test
    void erfIsMonotonicallyIncreasing() {
        double prev = Erf.erf(-5.0);

        for (double x = -4.5; x <= 5.0; x += 0.5) {
            double current = Erf.erf(x);
            assertThat(current).isGreaterThan(prev);
            prev = current;
        }
    }

    /**
     * Tests that erfc(x) is monotonically decreasing.
     * For any x1 < x2, we must have erfc(x1) > erfc(x2).
     */
    @Test
    void erfcIsMonotonicallyDecreasing() {
        double prev = Erf.erfc(-5.0);

        for (double x = -4.5; x <= 5.0; x += 0.5) {
            double current = Erf.erfc(x);
            assertThat(current).isLessThan(prev);
            prev = current;
        }
    }

    // ==================== Special Cases ====================

    /**
     * Tests erf with very small positive values.
     * For small x, erf(x) ≈ (2/√π) * x.
     */
    @Test
    void erfWithVerySmallPositiveValues() {
        double x = 0.01;
        double result = Erf.erf(x);

        // For very small x, erf(x) ≈ (2/√π) * x ≈ 1.1284 * x
        double approximation = (2.0 / Math.sqrt(Math.PI)) * x;
        assertThat(result).isCloseTo(approximation, within(1e-5));
    }

    /**
     * Tests erf with very small negative values.
     */
    @Test
    void erfWithVerySmallNegativeValues() {
        double x = -0.01;
        double result = Erf.erf(x);

        double approximation = (2.0 / Math.sqrt(Math.PI)) * x;
        assertThat(result).isCloseTo(approximation, within(1e-5));
    }

    /**
     * Tests that erf handles special double values appropriately.
     */
    @Test
    void erfWithSpecialDoubleValues() {
        // Test with positive infinity - should approach 1
        double erfInf = Erf.erf(Double.POSITIVE_INFINITY);
        assertThat(erfInf).isCloseTo(1.0, within(TOLERANCE));

        // Test with negative infinity - should approach -1
        double erfNegInf = Erf.erf(Double.NEGATIVE_INFINITY);
        assertThat(erfNegInf).isCloseTo(-1.0, within(TOLERANCE));

        // Test with very small positive value
        double erfTiny = Erf.erf(Double.MIN_VALUE);
        assertThat(erfTiny).isCloseTo(0.0, within(TOLERANCE));
    }

    /**
     * Tests that erfc handles special double values appropriately.
     */
    @Test
    void erfcWithSpecialDoubleValues() {
        // Test with positive infinity - should approach 0
        double erfcInf = Erf.erfc(Double.POSITIVE_INFINITY);
        assertThat(erfcInf).isCloseTo(0.0, within(TOLERANCE));

        // Test with negative infinity - should approach 2
        double erfcNegInf = Erf.erfc(Double.NEGATIVE_INFINITY);
        assertThat(erfcNegInf).isCloseTo(2.0, within(TOLERANCE));
    }

    // ==================== Intermediate Values ====================

    /**
     * Tests erf at intermediate values to ensure accuracy across the entire range.
     */
    @ParameterizedTest
    @CsvSource({
            "0.05",
            "0.15",
            "0.25",
            "0.35",
            "0.45",
            "0.55",
            "0.65",
            "0.75",
            "0.85",
            "0.95"
    })
    void erfAtIntermediateValues(double x) {
        double result = Erf.erf(x);

        // Verify the result is reasonable (between 0 and 1 for positive x)
        assertThat(result).isBetween(0.0, 1.0);

        // Verify symmetry
        assertThat(Erf.erf(-x)).isCloseTo(-result, within(TOLERANCE));

        // Verify complementary relationship
        assertThat(result + Erf.erfc(x)).isCloseTo(1.0, within(1e-15));
    }

    // ==================== Utility Class Verification ====================

    /**
     * Tests that the Erf class cannot be instantiated.
     * This verifies it is properly designed as a utility class.
     */
    @Test
    void cannotInstantiateErfClass() {
        // Verify that the constructor is private by checking there are no public constructors
        assertThat(Erf.class.getConstructors()).isEmpty();
    }
}
