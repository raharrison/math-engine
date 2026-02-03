package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
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
    public static final MathFunction ISNAN = FunctionBuilder
            .named("isnan")
            .describedAs("Check if value is NaN")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeNumber num && Double.isNaN(num.doubleValue())));

    /**
     * Check if value is infinite
     */
    public static final MathFunction ISINF = FunctionBuilder
            .named("isinf")
            .alias("isinfinite")
            .describedAs("Check if value is infinite")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeNumber num && Double.isInfinite(num.doubleValue())));

    /**
     * Check if value is finite (not NaN and not infinite)
     */
    public static final MathFunction ISFINITE = FunctionBuilder
            .named("isfinite")
            .describedAs("Check if value is finite")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeNumber num && Double.isFinite(num.doubleValue())));

    /**
     * Check if value is an integer
     */
    public static final MathFunction ISINT = FunctionBuilder
            .named("isint")
            .alias("isinteger")
            .describedAs("Check if value is an integer")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> {
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
    public static final MathFunction ISEVEN = FunctionBuilder
            .named("iseven")
            .describedAs("Check if value is an even integer")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> {
                if (arg instanceof NodeNumber num) {
                    double val = num.doubleValue();
                    if (Double.isFinite(val) && val == Math.floor(val)) return new NodeBoolean(((long) val) % 2 == 0);
                }
                return new NodeBoolean(false);
            });

    /**
     * Check if value is odd
     */
    public static final MathFunction ISODD = FunctionBuilder
            .named("isodd")
            .describedAs("Check if value is an odd integer")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> {
                if (arg instanceof NodeNumber num) {
                    double val = num.doubleValue();
                    if (Double.isFinite(val) && val == Math.floor(val)) return new NodeBoolean(((long) val) % 2 != 0);
                }
                return new NodeBoolean(false);
            });

    /**
     * Check if value is positive
     */
    public static final MathFunction ISPOSITIVE = FunctionBuilder
            .named("ispositive")
            .describedAs("Check if value is positive")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeNumber num && num.doubleValue() > 0));

    /**
     * Check if value is negative
     */
    public static final MathFunction ISNEGATIVE = FunctionBuilder
            .named("isnegative")
            .describedAs("Check if value is negative")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeNumber num && num.doubleValue() < 0));

    /**
     * Check if value is zero
     */
    public static final MathFunction ISZERO = FunctionBuilder
            .named("iszero")
            .describedAs("Check if value is zero")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeNumber num && num.doubleValue() == 0));

    // ==================== Type Conversion Functions ====================

    /**
     * Convert to integer (truncate toward zero)
     */
    public static final MathFunction TOINT = FunctionBuilder
            .named("int")
            .alias("toint", "integer")
            .describedAs("Convert to integer (truncate toward zero)")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, ctx) -> {
                double val = ctx.toNumber(arg).doubleValue();
                return new NodeRational((long) (val < 0 ? Math.ceil(val) : Math.floor(val)));
            });

    /**
     * Convert to double (floating point)
     */
    public static final MathFunction TODOUBLE = FunctionBuilder
            .named("float")
            .alias("tofloat", "todouble")
            .describedAs("Convert to floating point number")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, ctx) -> new NodeDouble(ctx.toNumber(arg).doubleValue()));

    /**
     * Convert to boolean
     */
    public static final MathFunction TOBOOL = FunctionBuilder
            .named("bool")
            .alias("tobool", "boolean")
            .describedAs("Convert to boolean")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, ctx) -> new NodeBoolean(ctx.toBoolean(arg)));

    /**
     * Convert a vector of vectors to a matrix
     */
    public static final MathFunction TOMATRIX = FunctionBuilder
            .named("tomatrix")
            .describedAs("Convert vector of vectors to matrix")
            .inCategory(TYPE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                NodeVector outerVector = ctx.requireVector(arg);

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
            });

    /**
     * Convert a matrix to a vector of vectors
     */
    public static final MathFunction TOVECTOR = FunctionBuilder
            .named("tovector")
            .describedAs("Convert matrix to vector of vectors")
            .inCategory(TYPE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                NodeMatrix matrix = ctx.requireMatrix(arg);

                Node[][] matrixElements = matrix.getElements();
                Node[] vectorOfVectors = new Node[matrix.getRows()];

                for (int i = 0; i < matrix.getRows(); i++) {
                    vectorOfVectors[i] = new NodeVector(matrixElements[i]);
                }

                return new NodeVector(vectorOfVectors);
            });

    /**
     * Get the numerator of a rational number
     */
    public static final MathFunction NUMERATOR = FunctionBuilder
            .named("numerator")
            .describedAs("Get numerator of a rational")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, ctx) ->
                    arg instanceof NodeRational rat ? new NodeRational(rat.getValue().getNumerator().longValue()) : ctx.toNumber(arg));

    /**
     * Get the denominator of a rational number
     */
    public static final MathFunction DENOMINATOR = FunctionBuilder
            .named("denominator")
            .describedAs("Get denominator of a rational")
            .inCategory(TYPE)
            .takingUnary()
            .implementedBy((arg, _) ->
                    arg instanceof NodeRational rat ? new NodeRational(rat.getValue().getDenominator().longValue()) : new NodeRational(1));

    // ==================== Type Query Functions ====================

    /**
     * Get the type name of a value
     */
    public static final MathFunction TYPEOF = FunctionBuilder
            .named("typeof")
            .describedAs("Get the type name of a value")
            .inCategory(TYPE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, _) -> new NodeString(switch (arg) {
                case NodeRational _ -> "rational";
                case NodeDouble _ -> "double";
                case NodePercent _ -> "percent";
                case NodeBoolean _ -> "boolean";
                case NodeUnit _ -> "unit";
                case NodeVector _ -> "vector";
                case NodeMatrix _ -> "matrix";
                case NodeString _ -> "string";
                default -> "unknown";
            }));

    /**
     * Check if value is a number (excludes booleans)
     */
    public static final MathFunction ISNUMBER = FunctionBuilder
            .named("isnumber")
            .describedAs("Check if value is a number")
            .inCategory(TYPE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeNumber && !(arg instanceof NodeBoolean)));

    /**
     * Check if value is a vector
     */
    public static final MathFunction ISVECTOR = FunctionBuilder
            .named("isvector")
            .describedAs("Check if value is a vector")
            .inCategory(TYPE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeVector));

    /**
     * Check if value is a matrix
     */
    public static final MathFunction ISMATRIX = FunctionBuilder
            .named("ismatrix")
            .describedAs("Check if value is a matrix")
            .inCategory(TYPE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeMatrix));

    /**
     * Check if value is a boolean
     */
    public static final MathFunction ISBOOLEAN = FunctionBuilder
            .named("isboolean")
            .describedAs("Check if value is a boolean")
            .inCategory(TYPE)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, _) -> new NodeBoolean(arg instanceof NodeBoolean));

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
