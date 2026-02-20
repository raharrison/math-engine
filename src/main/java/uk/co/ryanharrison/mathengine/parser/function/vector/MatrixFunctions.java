package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.linearalgebra.Matrix;
import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.operator.MatrixOperations;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.Arrays;
import java.util.List;

/**
 * Collection of matrix functions.
 * <p>
 * These functions operate on matrices and provide linear algebra operations.
 */
public final class MatrixFunctions {

    private MatrixFunctions() {
    }

    // ==================== Matrix Properties ====================

    /**
     * Matrix determinant
     */
    public static final MathFunction DET = FunctionBuilder
            .named("det")
            .describedAs("Returns the determinant of a square matrix")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix())
            .implementedBy((matrix, ctx) -> {
                ctx.requireSquareMatrix(matrix);
                Matrix m = ctx.toMatrix(matrix);
                return new NodeDouble(m.determinant());
            });

    /**
     * Matrix trace (sum of diagonal elements)
     */
    public static final MathFunction TRACE = FunctionBuilder
            .named("trace")
            .describedAs("Returns the trace of a matrix (sum of diagonal elements)")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix())
            .implementedBy((matrix, ctx) -> {
                ctx.requireSquareMatrix(matrix);

                double trace = 0;
                Node[][] elements = matrix.getElements();
                for (int i = 0; i < matrix.getRows(); i++) {
                    trace += ((NodeConstant) elements[i][i]).doubleValue();
                }
                return new NodeDouble(trace);
            });

    /**
     * Number of rows
     */
    public static final MathFunction ROWS = FunctionBuilder
            .named("rows")
            .describedAs("Returns the number of rows in the matrix")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix())
            .implementedBy((matrix, ctx) -> new NodeRational(matrix.getRows()));

    /**
     * Number of columns
     */
    public static final MathFunction COLS = FunctionBuilder
            .named("cols")
            .describedAs("Returns the number of columns in the matrix")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix())
            .implementedBy((matrix, ctx) -> new NodeRational(matrix.getCols()));

    // ==================== Matrix Transformations ====================

    /**
     * Matrix transpose
     */
    public static final MathFunction TRANSPOSE = FunctionBuilder
            .named("transpose")
            .describedAs("Returns the transpose of the matrix (rows and columns swapped)")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix())
            .implementedBy((matrix, ctx) -> {
                Node[][] elements = matrix.getElements();
                Node[][] result = new Node[matrix.getCols()][matrix.getRows()];

                for (int i = 0; i < matrix.getRows(); i++) {
                    for (int j = 0; j < matrix.getCols(); j++) {
                        result[j][i] = elements[i][j];
                    }
                }

                return new NodeMatrix(result);
            });

    /**
     * Identity matrix
     */
    public static final MathFunction IDENTITY = FunctionBuilder
            .named("identity")
            .describedAs("Returns the n×n identity matrix")
            .withParams("n")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.integer())
            .implementedBy((n, _) -> {
                if (n <= 0) {
                    throw new IllegalArgumentException("identity: size must be positive, got: " + n);
                }
                return MatrixOperations.identityMatrix(n);
            });

    /**
     * Zero matrix
     */
    public static final MathFunction ZEROS = FunctionBuilder
            .named("zeros")
            .describedAs("Returns a matrix of zeros with the given dimensions")
            .withParams("n")
            .withParams("rows", "cols")
            .inCategory(MathFunction.Category.MATRIX)
            .takingBetween(1, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                int rows = ctx.requireInteger(args.get(0));
                int cols = args.size() > 1
                        ? ctx.requireInteger(args.get(1))
                        : rows;

                if (rows <= 0 || cols <= 0) {
                    throw new IllegalArgumentException("zeros: dimensions must be positive");
                }

                Node[][] result = new Node[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        result[i][j] = new NodeRational(0);
                    }
                }
                return new NodeMatrix(result);
            });

    /**
     * Ones matrix
     */
    public static final MathFunction ONES = FunctionBuilder
            .named("ones")
            .describedAs("Returns a matrix of ones with the given dimensions")
            .withParams("n")
            .withParams("rows", "cols")
            .inCategory(MathFunction.Category.MATRIX)
            .takingBetween(1, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                int rows = ctx.requireInteger(args.get(0));
                int cols = args.size() > 1
                        ? ctx.requireInteger(args.get(1))
                        : rows;

                if (rows <= 0 || cols <= 0) {
                    throw new TypeError("ones: dimensions must be positive");
                }

                Node[][] result = new Node[rows][cols];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        result[i][j] = new NodeRational(1);
                    }
                }
                return new NodeMatrix(result);
            });

    /**
     * Diagonal matrix from vector or extract diagonal
     */
    public static final MathFunction DIAG = FunctionBuilder
            .named("diag")
            .describedAs("Creates a diagonal matrix from a vector, or extracts the diagonal of a matrix as a vector")
            .withParams("vector")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                // If vector, create diagonal matrix
                if (arg instanceof NodeVector vector) {
                    int n = vector.size();
                    Node[][] result = new Node[n][n];
                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            result[i][j] = i == j
                                    ? vector.getElement(i)
                                    : new NodeRational(0);
                        }
                    }
                    return new NodeMatrix(result);
                }

                // If matrix, extract diagonal
                if (arg instanceof NodeMatrix matrix) {
                    int n = Math.min(matrix.getRows(), matrix.getCols());
                    Node[] diagonal = new Node[n];
                    Node[][] elements = matrix.getElements();
                    for (int i = 0; i < n; i++) {
                        diagonal[i] = elements[i][i];
                    }
                    return new NodeVector(diagonal);
                }

                throw new TypeError("diag requires a vector or matrix, got: " +
                        arg.typeName());
            });

    // ==================== Advanced Matrix Operations ====================

    /**
     * Matrix inverse
     */
    public static final MathFunction INVERSE = FunctionBuilder
            .named("inverse")
            .alias("inv")
            .describedAs("Returns the inverse of a square matrix")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix())
            .implementedBy((matrix, ctx) -> {
                ctx.requireSquareMatrix(matrix);
                Matrix m = ctx.toMatrix(matrix);
                Matrix inverse = m.inverse();
                return ctx.fromMatrix(inverse);
            });

    /**
     * Matrix rank
     */
    public static final MathFunction RANK = FunctionBuilder
            .named("rank")
            .describedAs("Returns the rank of the matrix (number of linearly independent rows)")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix())
            .implementedBy((nodeMatrix, ctx) -> {
                // Convert to double array for rank calculation
                int rows = nodeMatrix.getRows();
                int cols = nodeMatrix.getCols();
                Node[][] elements = nodeMatrix.getElements();
                double[][] m = new double[rows][cols];

                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        m[i][j] = ((NodeConstant) elements[i][j]).doubleValue();
                    }
                }

                // Calculate rank using row reduction
                int rank = 0;
                boolean[] rowUsed = new boolean[rows];

                for (int col = 0; col < cols; col++) {
                    // Find pivot
                    int pivot = -1;
                    for (int row = 0; row < rows; row++) {
                        if (!rowUsed[row] && Math.abs(m[row][col]) > 1e-10) {
                            pivot = row;
                            break;
                        }
                    }

                    if (pivot == -1) continue;

                    rowUsed[pivot] = true;
                    rank++;

                    // Eliminate
                    double pivotVal = m[pivot][col];
                    for (int j = col; j < cols; j++) {
                        m[pivot][j] /= pivotVal;
                    }

                    for (int row = 0; row < rows; row++) {
                        if (row != pivot && Math.abs(m[row][col]) > 1e-10) {
                            double factor = m[row][col];
                            for (int j = col; j < cols; j++) {
                                m[row][j] -= factor * m[pivot][j];
                            }
                        }
                    }
                }

                return new NodeRational(rank);
            });

    /**
     * Frobenius norm
     */
    public static final MathFunction NORM = FunctionBuilder
            .named("norm")
            .describedAs("Returns the Frobenius norm of a matrix, or the Euclidean (2-norm) of a vector")
            .withParams("vector")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingUnary()
            .noBroadcasting()
            .implementedBy((arg, ctx) -> {
                double sumOfSquares = 0;

                if (arg instanceof NodeMatrix matrix) {
                    Node[][] elements = matrix.getElements();
                    for (int i = 0; i < matrix.getRows(); i++) {
                        for (int j = 0; j < matrix.getCols(); j++) {
                            double val = ((NodeConstant) elements[i][j]).doubleValue();
                            sumOfSquares += val * val;
                        }
                    }
                } else if (arg instanceof NodeVector vector) {
                    for (int i = 0; i < vector.size(); i++) {
                        double val = ((NodeConstant) vector.getElement(i)).doubleValue();
                        sumOfSquares += val * val;
                    }
                } else {
                    throw new TypeError("norm requires a vector or matrix");
                }

                return new NodeDouble(Math.sqrt(sumOfSquares));
            });

    /**
     * Extract row from matrix
     */
    public static final MathFunction ROW = FunctionBuilder
            .named("row")
            .describedAs("Returns the row at index i of the matrix as a vector (0-based)")
            .withParams("matrix", "i")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix(), ArgTypes.integer())
            .implementedBy((matrix, rowIdx, ctx) -> {
                if (rowIdx < 0 || rowIdx >= matrix.getRows()) {
                    throw new IllegalArgumentException("row: index out of bounds");
                }

                Node[][] elements = matrix.getElements();
                Node[] row = new Node[matrix.getCols()];
                System.arraycopy(elements[rowIdx], 0, row, 0, matrix.getCols());
                return new NodeVector(row);
            });

    /**
     * Extract column from matrix
     */
    public static final MathFunction COL = FunctionBuilder
            .named("col")
            .alias("column")
            .describedAs("Returns the column at index i of the matrix as a vector (0-based)")
            .withParams("matrix", "i")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix(), ArgTypes.integer())
            .implementedBy((matrix, colIdx, ctx) -> {
                if (colIdx < 0 || colIdx >= matrix.getCols()) {
                    throw new IllegalArgumentException("col: index out of bounds");
                }

                Node[][] elements = matrix.getElements();
                Node[] col = new Node[matrix.getRows()];
                for (int i = 0; i < matrix.getRows(); i++) {
                    col[i] = elements[i][colIdx];
                }
                return new NodeVector(col);
            });

    /**
     * Reshape vector/matrix to new dimensions
     */
    public static final MathFunction RESHAPE = FunctionBuilder
            .named("reshape")
            .describedAs("Reshapes a vector or matrix to the specified rows×cols dimensions")
            .withParams("vector", "rows", "cols")
            .withParams("matrix", "rows", "cols")
            .inCategory(MathFunction.Category.MATRIX)
            .takingExactly(3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                NodeConstant data = args.get(0);
                int newRows = ctx.requireInteger(args.get(1));
                int newCols = ctx.requireInteger(args.get(2));

                if (newRows <= 0 || newCols <= 0) {
                    throw new TypeError("reshape: dimensions must be positive");
                }

                // Flatten input to list of values
                java.util.List<Node> values = new java.util.ArrayList<>();
                if (data instanceof NodeVector vector) {
                    for (int i = 0; i < vector.size(); i++) {
                        values.add(vector.getElement(i));
                    }
                } else if (data instanceof NodeMatrix matrix) {
                    Node[][] elements = matrix.getElements();
                    for (int i = 0; i < matrix.getRows(); i++) {
                        values.addAll(Arrays.asList(elements[i]).subList(0, matrix.getCols()));
                    }
                } else {
                    throw new TypeError("reshape requires a vector or matrix");
                }

                if (values.size() != newRows * newCols) {
                    throw new IllegalArgumentException("reshape: cannot reshape " + values.size() +
                            " elements to " + newRows + "x" + newCols);
                }

                Node[][] result = new Node[newRows][newCols];
                int idx = 0;
                for (int i = 0; i < newRows; i++) {
                    for (int j = 0; j < newCols; j++) {
                        result[i][j] = values.get(idx++);
                    }
                }
                return new NodeMatrix(result);
            });

    /**
     * Matrix minor (element-wise)
     */
    public static final MathFunction MINOR = FunctionBuilder
            .named("minor")
            .describedAs("Returns the minor of the matrix at row i, col j (determinant of submatrix)")
            .withParams("matrix", "i", "j")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix(), ArgTypes.integer(), ArgTypes.integer())
            .implementedBy((matrix, row, col, ctx) -> {
                if (row < 0 || row >= matrix.getRows() || col < 0 || col >= matrix.getCols()) {
                    throw new TypeError("minor: indices out of bounds");
                }

                // Create submatrix excluding row and col
                int n = matrix.getRows();
                Node[][] elements = matrix.getElements();
                Node[][] sub = new Node[n - 1][n - 1];

                int si = 0;
                for (int i = 0; i < n; i++) {
                    if (i == row) continue;
                    int sj = 0;
                    for (int j = 0; j < n; j++) {
                        if (j == col) continue;
                        sub[si][sj++] = elements[i][j];
                    }
                    si++;
                }

                NodeMatrix subMatrix = new NodeMatrix(sub);
                Matrix m = ctx.toMatrix(subMatrix);
                return new NodeDouble(m.determinant());
            });

    /**
     * Cofactor matrix
     */
    public static final MathFunction COFACTOR = FunctionBuilder
            .named("cofactor")
            .describedAs("Returns the cofactor of the matrix at row i, col j")
            .withParams("matrix", "i", "j")
            .inCategory(MathFunction.Category.MATRIX)
            .takingExactly(3)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> {
                int row = ctx.requireInteger(args.get(1));
                int col = ctx.requireInteger(args.get(2));

                // Cofactor = (-1)^(i+j) * minor
                double minor = MINOR.apply(args, ctx).doubleValue();
                double sign = ((row + col) % 2 == 0) ? 1 : -1;
                return new NodeDouble(sign * minor);
            });

    /**
     * Adjugate matrix (transpose of cofactor matrix)
     */
    public static final MathFunction ADJUGATE = FunctionBuilder
            .named("adjugate")
            .alias("adj")
            .describedAs("Returns the adjugate (classical adjoint) of the matrix")
            .withParams("matrix")
            .inCategory(MathFunction.Category.MATRIX)
            .takingTyped(ArgTypes.matrix())
            .implementedBy((matrix, ctx) -> {
                ctx.requireSquareMatrix(matrix);

                int n = matrix.getRows();
                Node[][] result = new Node[n][n];

                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        // Cofactor at (i,j), but transposed to (j,i)
                        List<NodeConstant> cofArgs = List.of(matrix, new NodeRational(j), new NodeRational(i));
                        result[i][j] = COFACTOR.apply(cofArgs, ctx);
                    }
                }

                return new NodeMatrix(result);
            });

    // ==================== All Functions ====================

    /**
     * Gets all matrix functions.
     */
    public static List<MathFunction> all() {
        return List.of(DET, TRACE, ROWS, COLS, TRANSPOSE, IDENTITY, ZEROS, ONES, DIAG,
                INVERSE, RANK, NORM, ROW, COL, RESHAPE, MINOR, COFACTOR, ADJUGATE);
    }
}
