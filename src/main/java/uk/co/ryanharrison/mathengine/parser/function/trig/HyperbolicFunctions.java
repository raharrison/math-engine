package uk.co.ryanharrison.mathengine.parser.function.trig;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
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
    public static final MathFunction SINH = FunctionBuilder
            .named("sinh")
            .describedAs("Hyperbolic sine")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(Math::sinh);

    /**
     * Hyperbolic cosine function
     */
    public static final MathFunction COSH = FunctionBuilder
            .named("cosh")
            .describedAs("Hyperbolic cosine")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(Math::cosh);

    /**
     * Hyperbolic tangent function
     */
    public static final MathFunction TANH = FunctionBuilder
            .named("tanh")
            .describedAs("Hyperbolic tangent")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(Math::tanh);

    // ==================== Inverse Hyperbolic Functions ====================

    /**
     * Inverse hyperbolic sine (area hyperbolic sine)
     */
    public static final MathFunction ASINH = FunctionBuilder
            .named("asinh")
            .describedAs("Inverse hyperbolic sine")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(TrigUtils::asinh);

    /**
     * Inverse hyperbolic cosine (area hyperbolic cosine)
     */
    public static final MathFunction ACOSH = FunctionBuilder
            .named("acosh")
            .describedAs("Inverse hyperbolic cosine")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(TrigUtils::acosh);

    /**
     * Inverse hyperbolic tangent (area hyperbolic tangent)
     */
    public static final MathFunction ATANH = FunctionBuilder
            .named("atanh")
            .describedAs("Inverse hyperbolic tangent")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(TrigUtils::atanh);

    // ==================== Reciprocal Hyperbolic Functions ====================

    /**
     * Hyperbolic secant (1/cosh)
     */
    public static final MathFunction SECH = FunctionBuilder
            .named("sech")
            .describedAs("Hyperbolic secant")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(TrigUtils::sech);

    /**
     * Hyperbolic cosecant (1/sinh)
     */
    public static final MathFunction CSCH = FunctionBuilder
            .named("csch")
            .describedAs("Hyperbolic cosecant")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(TrigUtils::cosech);

    /**
     * Hyperbolic cotangent (1/tanh)
     */
    public static final MathFunction COTH = FunctionBuilder
            .named("coth")
            .describedAs("Hyperbolic cotangent")
            .inCategory(HYPERBOLIC)
            .takingUnary()
            .implementedByDouble(TrigUtils::coth);

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
