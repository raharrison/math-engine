package uk.co.ryanharrison.mathengine.parser.function.trig;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.utils.TrigUtils;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.HYPERBOLIC;

/**
 * Collection of hyperbolic functions.
 * <p>
 * Hyperbolic functions do NOT use the angle unit setting since they
 * operate on real numbers, not angles.
 * <p>
 * A restricted domain belongs to the maths: the routines in {@link TrigUtils} police their
 * own arguments, and {@link FunctionContext#checkingDomain} reports that as the engine's
 * own domain error, as it does for the circular functions.
 */
public final class HyperbolicFunctions {

    private HyperbolicFunctions() {
    }

    // ==================== Standard Hyperbolic Functions ====================

    /**
     * Hyperbolic sine function.
     */
    public static final MathFunction SINH = hyperbolic(
            "sinh", "Returns the hyperbolic sine of x", Math::sinh);

    /**
     * Hyperbolic cosine function.
     */
    public static final MathFunction COSH = hyperbolic(
            "cosh", "Returns the hyperbolic cosine of x", Math::cosh);

    /**
     * Hyperbolic tangent function.
     */
    public static final MathFunction TANH = hyperbolic(
            "tanh", "Returns the hyperbolic tangent of x", Math::tanh);

    // ==================== Reciprocal Hyperbolic Functions ====================

    /**
     * Hyperbolic secant (1/cosh).
     */
    public static final MathFunction SECH = hyperbolic(
            "sech", "Returns the hyperbolic secant of x (1/cosh(x))", TrigUtils::sech);

    /**
     * Hyperbolic cosecant (1/sinh), undefined at zero.
     */
    public static final MathFunction CSCH = hyperbolic(
            "csch", "Returns the hyperbolic cosecant of x (1/sinh(x))", TrigUtils::cosech);

    /**
     * Hyperbolic cotangent (1/tanh), undefined at zero.
     */
    public static final MathFunction COTH = hyperbolic(
            "coth", "Returns the hyperbolic cotangent of x (1/tanh(x))", TrigUtils::coth);

    // ==================== Inverse Hyperbolic Functions ====================

    /**
     * Inverse hyperbolic sine (area hyperbolic sine).
     */
    public static final MathFunction ASINH = hyperbolic(
            "asinh", "Returns the inverse hyperbolic sine of x", TrigUtils::asinh);

    /**
     * Inverse hyperbolic cosine (area hyperbolic cosine), defined for x >= 1.
     */
    public static final MathFunction ACOSH = hyperbolic(
            "acosh", "Returns the inverse hyperbolic cosine of x", TrigUtils::acosh);

    /**
     * Inverse hyperbolic tangent (area hyperbolic tangent), defined for |x| < 1.
     */
    public static final MathFunction ATANH = hyperbolic(
            "atanh", "Returns the inverse hyperbolic tangent of x", TrigUtils::atanh);

    // ==================== Inverse Reciprocal Hyperbolic Functions ====================

    /**
     * Inverse hyperbolic secant, acosh(1/x), defined on (0, 1].
     */
    public static final MathFunction ASECH = hyperbolic(
            "asech", "Returns the inverse hyperbolic secant of x, acosh(1/x)", TrigUtils::asech);

    /**
     * Inverse hyperbolic cosecant, asinh(1/x), undefined at zero.
     */
    public static final MathFunction ACSCH = hyperbolic(
            "acsch", "Returns the inverse hyperbolic cosecant of x, asinh(1/x)", TrigUtils::acosech);

    /**
     * Inverse hyperbolic cotangent, atanh(1/x), defined for |x| > 1.
     */
    public static final MathFunction ACOTH = hyperbolic(
            "acoth", "Returns the inverse hyperbolic cotangent of x, atanh(1/x)", TrigUtils::acoth);

    // ==================== All Functions ====================

    /**
     * Gets all hyperbolic functions.
     *
     * @return list of all hyperbolic functions
     */
    public static List<MathFunction> all() {
        return List.of(SINH, COSH, TANH, SECH, CSCH, COTH,
                ASINH, ACOSH, ATANH, ASECH, ACSCH, ACOTH);
    }

    private static MathFunction hyperbolic(String name, String description, DoubleUnaryOperator fn) {
        return FunctionBuilder
                .named(name)
                .describedAs(description)
                .withParams("x")
                .inCategory(HYPERBOLIC)
                .takingUnary()
                .implementedBy((arg, ctx) -> ctx.mapDouble(arg, ctx.checkingDomain(fn)));
    }
}
