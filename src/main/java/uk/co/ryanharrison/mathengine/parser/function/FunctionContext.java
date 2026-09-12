package uk.co.ryanharrison.mathengine.parser.function;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.format.StringNodeFormatter;
import uk.co.ryanharrison.mathengine.parser.util.BroadcastingEngine;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

/**
 * The toolkit a built-in function body is handed: argument extraction, domain
 * checks, angle conversion and the ability to call back into user functions.
 * <p>
 * The context knows which function it is serving, so every error message it
 * produces is already attributed. Arithmetic is deliberately absent: use the
 * operations on {@link NodeConstant}, which carry the type rules.
 *
 * <pre>{@code
 * .implementedBy((arg, ctx) -> new NodeDouble(Math.log(ctx.requirePositive(ctx.toDouble(arg)))))
 * }</pre>
 */
public final class FunctionContext {

    private static final String ANGLE_UNIT_TYPE = "angle";

    private final String functionName;
    private final EvaluationContext evaluationContext;
    private final FunctionCaller functionCaller;

    public FunctionContext(String functionName, EvaluationContext evaluationContext, FunctionCaller functionCaller) {
        this.functionName = functionName;
        this.evaluationContext = evaluationContext;
        this.functionCaller = functionCaller;
    }

    public String functionName() {
        return functionName;
    }

    /**
     * Calls a user-defined function or lambda, as the higher-order functions do.
     *
     * @throws IllegalStateException if this context cannot call functions
     */
    public NodeConstant callFunction(NodeFunction function, List<NodeConstant> args) {
        if (functionCaller == null) {
            throw new IllegalStateException("This function context cannot call functions");
        }
        return functionCaller.call(function, args, evaluationContext);
    }

    public AngleUnit getAngleUnit() {
        return evaluationContext.getAngleUnit();
    }

    /**
     * When true, a domain error yields NaN instead of throwing.
     */
    public boolean isSilentValidation() {
        return evaluationContext.isSilentValidation();
    }

    // ==================== Errors and domain checks ====================

    /**
     * Builds a domain error with the function name prepended. All function errors
     * should go through here so messages stay consistent.
     *
     * @return the exception, which the caller must throw
     */
    public DomainException error(String message) {
        return new DomainException(functionName + ": " + message);
    }

    /**
     * The require* checks return the value so they can wrap an argument in place.
     * Under silent validation they return NaN rather than throwing.
     */
    public double requirePositive(double value) {
        return require(value > 0, value, "requires positive value, got: " + value);
    }

    public double requireNonNegative(double value) {
        return require(value >= 0, value, "requires non-negative value, got: " + value);
    }

    public double requireNonZero(double value) {
        return require(value != 0, value, "requires non-zero value");
    }

    public double requireInRange(double value, double min, double max) {
        return require(value >= min && value <= max, value,
                "requires value in range [" + min + ", " + max + "], got: " + value);
    }

    /**
     * Adapts a maths routine that polices its own domain, so its complaint arrives as a
     * {@link DomainException} attributed to the function, exactly as the require* checks
     * above do, rather than as a raw {@link IllegalArgumentException} that
     * {@code silentValidation} would not catch.
     */
    public DoubleUnaryOperator checkingDomain(DoubleUnaryOperator fn) {
        return value -> {
            try {
                return fn.applyAsDouble(value);
            } catch (IllegalArgumentException e) {
                if (isSilentValidation()) {
                    return Double.NaN;
                }
                throw error(e.getMessage());
            }
        };
    }

    private double require(boolean valid, double value, String message) {
        if (valid) {
            return value;
        }
        if (isSilentValidation()) {
            return Double.NaN;
        }
        throw error(message);
    }

    // ==================== Argument extraction ====================

    /**
     * @throws TypeError if the value is not numeric
     */
    public NodeNumber toNumber(NodeConstant value) {
        return TypeCoercion.toNumber(value);
    }

    /**
     * @throws TypeError if the value is not numeric
     */
    public double toDouble(NodeConstant value) {
        return TypeCoercion.toDouble(value);
    }

    /**
     * Truncates towards zero. Use {@link #requireInteger} to reject a fractional argument.
     */
    public int toInt(NodeConstant value) {
        return (int) toDouble(value);
    }

    /** Numbers are true when non-zero. */
    public boolean toBoolean(NodeConstant value) {
        return TypeCoercion.toBoolean(value);
    }

    /**
     * Displayed form, unquoted, rounded to the configured decimal places.
     */
    public String toStringValue(NodeConstant value) {
        int places = evaluationContext.getDecimalPlaces();
        if (places < 0 || value instanceof NodeString || value instanceof NodeBoolean) {
            return TypeCoercion.toDisplayString(value);
        }
        return StringNodeFormatter.withDecimalPlaces(places).format(value);
    }

    /**
     * The decimal behind a percentage, so 50% gives 0.5. Plain numbers pass through.
     */
    public double toPercentDecimal(NodeConstant value) {
        return value instanceof NodePercent percent ? percent.getValue() : toDouble(value);
    }

    public int requireInteger(NodeConstant value) {
        return (int) requireWholeNumber(value);
    }

    public long requireLong(NodeConstant value) {
        return (long) requireWholeNumber(value);
    }

    private double requireWholeNumber(NodeConstant value) {
        double number = toDouble(value);
        if (number != Math.floor(number)) {
            throw new TypeError(functionName + " requires whole-number arguments, got: " + number);
        }
        return number;
    }

    public NodeVector requireVector(NodeConstant value) {
        if (value instanceof NodeVector vector) {
            return vector;
        }
        throw new TypeError("Function '" + functionName + "' requires a vector, got: " + value.typeName());
    }

    public NodeMatrix requireMatrix(NodeConstant value) {
        if (value instanceof NodeMatrix matrix) {
            return matrix;
        }
        throw new TypeError("Function '" + functionName + "' requires a matrix, got: " + value.typeName());
    }

    public void requireSquareMatrix(NodeMatrix matrix) {
        if (matrix.getRows() != matrix.getCols()) {
            throw error("requires a square matrix, got " + matrix.getRows() + "x" + matrix.getCols());
        }
    }

    public NodeString requireString(NodeConstant value) {
        if (value instanceof NodeString text) {
            return text;
        }
        throw new TypeError("Function '" + functionName + "' requires a string, got: " + value.typeName());
    }

    /**
     * The value as a string, converting non-strings to their display form.
     */
    public NodeString asString(NodeConstant value) {
        return value instanceof NodeString text ? text : new NodeString(toStringValue(value));
    }

    public void requireNonEmpty(double[] values) {
        requireMinSize(values, 1);
    }

    public void requireMinSize(double[] values, int min) {
        if (values.length < min) {
            throw new TypeError(functionName + " requires at least " + min +
                    " element(s), got " + values.length);
        }
    }

    // ==================== Angles ====================

    /**
     * Interprets an angle given in the configured unit as radians.
     */
    public double toRadians(double angle) {
        return getAngleUnit() == AngleUnit.DEGREES ? Math.toRadians(angle) : angle;
    }

    /** An angle in radians. An angle label beats the configured unit. */
    public double toRadians(NodeConstant value) {
        if (value instanceof NodeUnit quantity && ANGLE_UNIT_TYPE.equals(quantity.getUnit().type())) {
            // Every angle unit has the radian as its base, so this is the conversion
            return quantity.getUnit().toBase(quantity.getValue());
        }
        return toRadians(toDouble(value));
    }

    /**
     * Expresses a result computed in radians in the configured unit.
     */
    public double fromRadians(double radians) {
        return getAngleUnit() == AngleUnit.DEGREES ? Math.toDegrees(radians) : radians;
    }

    // ==================== Broadcasting ====================

    /**
     * Applies a real-valued function, element-wise over vectors and matrices.
     * The result is always a plain number, so units and percentages are dropped;
     * that suits the transcendental functions, where a unit would be meaningless.
     */
    public NodeConstant mapDouble(NodeConstant value, DoubleUnaryOperator op) {
        return BroadcastingEngine.applyUnary(value, v -> new NodeDouble(op.applyAsDouble(toDouble(v))));
    }

    /** Like {@link #mapDouble}, but keeps the marker: {@code sqrt(100 meters)} is 10 meters. */
    public NodeConstant mapMagnitude(NodeConstant value, DoubleUnaryOperator op) {
        return value.mapMagnitude(op);
    }

    /**
     * The marker comes from whichever side has one, the left first.
     *
     * @throws TypeError if the two carry units measuring different things
     */
    public NodeConstant mapMagnitude(NodeConstant left, NodeConstant right, DoubleBinaryOperator op) {
        if (left instanceof NodeUnit leftUnit && right instanceof NodeUnit rightUnit) {
            double converted = rightUnit.convertTo(leftUnit.getUnit()).getValue();
            return left.mapMagnitude(value -> op.applyAsDouble(value, converted));
        }
        if (isMarked(right) && !isMarked(left)) {
            double leftValue = toDouble(left);
            return right.mapMagnitude(value -> op.applyAsDouble(leftValue, value));
        }
        double rightValue = toDouble(right);
        return left.mapMagnitude(value -> op.applyAsDouble(value, rightValue));
    }

    /**
     * Applies a function of an angle, element-wise. The answer is a ratio, so it has no marker.
     */
    public NodeConstant mapAngle(NodeConstant value, DoubleUnaryOperator radiansOp) {
        return BroadcastingEngine.applyUnary(value,
                v -> new NodeDouble(radiansOp.applyAsDouble(toRadians(v))));
    }

    /**
     * Whether a value wears a marker that arithmetic has to carry: a unit or a percentage.
     */
    private static boolean isMarked(NodeConstant value) {
        return value instanceof NodeUnit || value instanceof NodePercent;
    }

    // ==================== Collections ====================

    /**
     * Flattens one level of vectors, so {@code sum(1, {2, 3}, 4)} sees four values.
     */
    public List<NodeConstant> flattenArguments(List<NodeConstant> args) {
        var flattened = new ArrayList<NodeConstant>(args.size());
        for (NodeConstant arg : args) {
            if (arg instanceof NodeVector vector) {
                flattened.addAll(vector.toList());
            } else {
                flattened.add(arg);
            }
        }
        return flattened;
    }

    public double[] toDoubleArray(NodeVector vector) {
        double[] values = new double[vector.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = toDouble((NodeConstant) vector.getElement(i));
        }
        return values;
    }

    /**
     * The arguments flattened to plain numbers, for the statistics routines.
     */
    public double[] flattenToDoubles(List<NodeConstant> args) {
        List<NodeConstant> flattened = flattenArguments(args);
        double[] values = new double[flattened.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = toDouble(flattened.get(i));
        }
        return values;
    }
}
