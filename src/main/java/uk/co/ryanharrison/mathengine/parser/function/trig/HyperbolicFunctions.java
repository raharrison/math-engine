package uk.co.ryanharrison.mathengine.parser.function.trig;

import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.UnaryFunction;
import uk.co.ryanharrison.mathengine.utils.TrigUtils;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.HYPERBOLIC;

/**
 * Collection of hyperbolic functions.
 * <p>
 * Hyperbolic functions do NOT use the angle unit setting since they
 * operate on real numbers, not angles.
 */
public final class HyperbolicFunctions {

    private HyperbolicFunctions() {
    }

    // ==================== Standard Hyperbolic Functions ====================

    /**
     * Hyperbolic sine function
     */
    public static final MathFunction SINH = UnaryFunction.ofDouble("sinh", "Hyperbolic sine", HYPERBOLIC, Math::sinh);

    /**
     * Hyperbolic cosine function
     */
    public static final MathFunction COSH = UnaryFunction.ofDouble("cosh", "Hyperbolic cosine", HYPERBOLIC, Math::cosh);

    /**
     * Hyperbolic tangent function
     */
    public static final MathFunction TANH = UnaryFunction.ofDouble("tanh", "Hyperbolic tangent", HYPERBOLIC, Math::tanh);

    // ==================== Inverse Hyperbolic Functions ====================

    /**
     * Inverse hyperbolic sine (area hyperbolic sine)
     */
    public static final MathFunction ASINH = UnaryFunction.ofDouble("asinh", "Inverse hyperbolic sine", HYPERBOLIC,
            TrigUtils::asinh);

    /**
     * Inverse hyperbolic cosine (area hyperbolic cosine)
     */
    public static final MathFunction ACOSH = UnaryFunction.ofDouble("acosh", "Inverse hyperbolic cosine", HYPERBOLIC,
            TrigUtils::acosh);

    /**
     * Inverse hyperbolic tangent (area hyperbolic tangent)
     */
    public static final MathFunction ATANH = UnaryFunction.ofDouble("atanh", "Inverse hyperbolic tangent", HYPERBOLIC,
            TrigUtils::atanh);

    // ==================== Reciprocal Hyperbolic Functions ====================

    /**
     * Hyperbolic secant (1/cosh)
     */
    public static final MathFunction SECH = UnaryFunction.ofDouble("sech", "Hyperbolic secant", HYPERBOLIC,
            TrigUtils::sech);

    /**
     * Hyperbolic cosecant (1/sinh)
     */
    public static final MathFunction CSCH = UnaryFunction.ofDouble("csch", "Hyperbolic cosecant", HYPERBOLIC,
            TrigUtils::cosech);

    /**
     * Hyperbolic cotangent (1/tanh)
     */
    public static final MathFunction COTH = UnaryFunction.ofDouble("coth", "Hyperbolic cotangent", HYPERBOLIC,
            TrigUtils::coth);

    // ==================== All Functions ====================

    /**
     * Gets all hyperbolic functions.
     *
     * @return list of all hyperbolic functions
     */
    public static List<MathFunction> all() {
        return List.of(SINH, COSH, TANH, ASINH, ACOSH, ATANH, SECH, CSCH, COTH);
    }

    /**
     * Gets standard hyperbolic functions (sinh, cosh, tanh).
     *
     * @return list of standard hyperbolic functions
     */
    public static List<MathFunction> standard() {
        return List.of(SINH, COSH, TANH);
    }
}
