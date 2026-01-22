package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.UnaryFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.TYPE;

/**
 * Collection of type checking and conversion functions.
 */
public final class TypeFunctions {

    private TypeFunctions() {
    }

    // ==================== Type Checking Functions ====================

    /**
     * Check if value is NaN
     */
    public static final MathFunction ISNAN = UnaryFunction.of("isnan", "Check if value is NaN", TYPE, (arg, _) ->
            new NodeBoolean(arg instanceof NodeNumber num && Double.isNaN(num.doubleValue())));

    /**
     * Check if value is infinite
     */
    public static final MathFunction ISINF = UnaryFunction.of("isinf", "Check if value is infinite", TYPE, List.of("isinfinite"),
            (arg, _) -> new NodeBoolean(arg instanceof NodeNumber num && Double.isInfinite(num.doubleValue())));

    /**
     * Check if value is finite (not NaN and not infinite)
     */
    public static final MathFunction ISFINITE = UnaryFunction.of("isfinite", "Check if value is finite", TYPE, (arg, _) ->
            new NodeBoolean(arg instanceof NodeNumber num && Double.isFinite(num.doubleValue())));

    /**
     * Check if value is an integer
     */
    public static final MathFunction ISINT = UnaryFunction.of("isint", "Check if value is an integer", TYPE, List.of("isinteger"), (arg, _) -> {
        if (arg instanceof NodeRational rat) return new NodeBoolean(rat.getValue().isInteger());
        if (arg instanceof NodeNumber num) {
            double val = num.doubleValue();
            return new NodeBoolean(Double.isFinite(val) && val == Math.floor(val));
        }
        return new NodeBoolean(false);
    });

    /**
     * Check if value is even
     */
    public static final MathFunction ISEVEN = UnaryFunction.of("iseven", "Check if value is an even integer", TYPE, (arg, _) -> {
        if (arg instanceof NodeNumber num) {
            double val = num.doubleValue();
            if (Double.isFinite(val) && val == Math.floor(val)) return new NodeBoolean(((long) val) % 2 == 0);
        }
        return new NodeBoolean(false);
    });

    /**
     * Check if value is odd
     */
    public static final MathFunction ISODD = UnaryFunction.of("isodd", "Check if value is an odd integer", TYPE, (arg, _) -> {
        if (arg instanceof NodeNumber num) {
            double val = num.doubleValue();
            if (Double.isFinite(val) && val == Math.floor(val)) return new NodeBoolean(((long) val) % 2 != 0);
        }
        return new NodeBoolean(false);
    });

    /**
     * Check if value is positive
     */
    public static final MathFunction ISPOSITIVE = UnaryFunction.of("ispositive", "Check if value is positive", TYPE, (arg, _) ->
            new NodeBoolean(arg instanceof NodeNumber num && num.doubleValue() > 0));

    /**
     * Check if value is negative
     */
    public static final MathFunction ISNEGATIVE = UnaryFunction.of("isnegative", "Check if value is negative", TYPE, (arg, _) ->
            new NodeBoolean(arg instanceof NodeNumber num && num.doubleValue() < 0));

    /**
     * Check if value is zero
     */
    public static final MathFunction ISZERO = UnaryFunction.of("iszero", "Check if value is zero", TYPE, (arg, _) ->
            new NodeBoolean(arg instanceof NodeNumber num && num.doubleValue() == 0));

    // ==================== Type Conversion Functions ====================

    /**
     * Convert to integer (truncate toward zero)
     */
    public static final MathFunction TOINT = UnaryFunction.of("int", "Convert to integer (truncate toward zero)", TYPE,
            List.of("toint", "integer"), (arg, ctx) -> {
                double val = ctx.toNumber(arg).doubleValue();
                return new NodeRational((long) (val < 0 ? Math.ceil(val) : Math.floor(val)));
            });

    /**
     * Convert to double (floating point)
     */
    public static final MathFunction TODOUBLE = UnaryFunction.of("float", "Convert to floating point number", TYPE,
            List.of("tofloat", "todouble"), (arg, ctx) -> new NodeDouble(ctx.toNumber(arg).doubleValue()));

    /**
     * Convert to boolean
     */
    public static final MathFunction TOBOOL = UnaryFunction.of("bool", "Convert to boolean", TYPE,
            List.of("tobool", "boolean"), (arg, ctx) -> new NodeBoolean(ctx.toBoolean(arg)));

    /**
     * Convert a vector of vectors to a matrix
     */
    public static final MathFunction TOMATRIX = UnaryFunction.of("tomatrix", "Convert vector of vectors to matrix", TYPE, (arg, ctx) -> {
        NodeVector outerVector = ctx.requireVector(arg, "tomatrix");

        if (outerVector.size() == 0) {
            throw new IllegalArgumentException("tomatrix requires non-empty vector");
        }

        // Convert vector of vectors to matrix
        Node[] rows = outerVector.getElements();
        Node[][] matrixElements = new Node[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            if (!(rows[i] instanceof NodeVector rowVector)) {
                throw new IllegalArgumentException("tomatrix requires a vector of vectors, but element " + i + " is not a vector");
            }
            matrixElements[i] = rowVector.getElements();

            // Validate all rows have the same length
            if (i > 0 && matrixElements[i].length != matrixElements[0].length) {
                throw new IllegalArgumentException("tomatrix requires all rows to have the same length, but row 0 has " +
                        matrixElements[0].length + " elements and row " + i + " has " + matrixElements[i].length + " elements");
            }
        }

        return new NodeMatrix(matrixElements);
    }, false);

    /**
     * Convert a matrix to a vector of vectors
     */
    public static final MathFunction TOVECTOR = UnaryFunction.of("tovector", "Convert matrix to vector of vectors", TYPE, (arg, ctx) -> {
        NodeMatrix matrix = ctx.requireMatrix(arg, "tovector");

        Node[][] matrixElements = matrix.getElements();
        Node[] vectorOfVectors = new Node[matrix.getRows()];

        for (int i = 0; i < matrix.getRows(); i++) {
            vectorOfVectors[i] = new NodeVector(matrixElements[i]);
        }

        return new NodeVector(vectorOfVectors);
    }, false);

    /**
     * Get the numerator of a rational number
     */
    public static final MathFunction NUMERATOR = UnaryFunction.of("numerator", "Get numerator of a rational", TYPE, (arg, ctx) ->
            arg instanceof NodeRational rat ? new NodeRational(rat.getValue().getNumerator().longValue()) : ctx.toNumber(arg));

    /**
     * Get the denominator of a rational number
     */
    public static final MathFunction DENOMINATOR = UnaryFunction.of("denominator", "Get denominator of a rational", TYPE, (arg, _) ->
            arg instanceof NodeRational rat ? new NodeRational(rat.getValue().getDenominator().longValue()) : new NodeRational(1));

    // ==================== Type Query Functions ====================

    /**
     * Get the type name of a value
     */
    public static final MathFunction TYPEOF = UnaryFunction.of("typeof", "Get the type name of a value", TYPE, (arg, _) ->
            new NodeString(switch (arg) {
                case NodeRational _ -> "rational";
                case NodeDouble _ -> "double";
                case NodePercent _ -> "percent";
                case NodeBoolean _ -> "boolean";
                case NodeUnit _ -> "unit";
                case NodeVector _ -> "vector";
                case NodeMatrix _ -> "matrix";
                case NodeString _ -> "string";
                default -> "unknown";
            }), false);

    /**
     * Check if value is a number (excludes booleans)
     */
    public static final MathFunction ISNUMBER = UnaryFunction.of("isnumber", "Check if value is a number", TYPE,
            (arg, _) -> new NodeBoolean(arg instanceof NodeNumber && !(arg instanceof NodeBoolean)), false);

    /**
     * Check if value is a vector
     */
    public static final MathFunction ISVECTOR = UnaryFunction.of("isvector", "Check if value is a vector", TYPE,
            (arg, _) -> new NodeBoolean(arg instanceof NodeVector), false);

    /**
     * Check if value is a matrix
     */
    public static final MathFunction ISMATRIX = UnaryFunction.of("ismatrix", "Check if value is a matrix", TYPE,
            (arg, _) -> new NodeBoolean(arg instanceof NodeMatrix), false);

    /**
     * Check if value is a boolean
     */
    public static final MathFunction ISBOOLEAN = UnaryFunction.of("isboolean", "Check if value is a boolean", TYPE,
            (arg, _) -> new NodeBoolean(arg instanceof NodeBoolean), false);

    /**
     * Gets all type functions.
     */
    public static List<MathFunction> all() {
        return List.of(
                ISNAN, ISINF, ISFINITE, ISINT, ISEVEN, ISODD, ISPOSITIVE, ISNEGATIVE, ISZERO,
                TOINT, TODOUBLE, TOBOOL, TOMATRIX, TOVECTOR, NUMERATOR, DENOMINATOR,
                TYPEOF, ISNUMBER, ISVECTOR, ISMATRIX, ISBOOLEAN
        );
    }
}
