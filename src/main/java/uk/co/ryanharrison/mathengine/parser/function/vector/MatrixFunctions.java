package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

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
    public static final MathFunction DET = new MathFunction() {
        @Override
        public String name() {
            return "det";
        }

        @Override
        public String description() {
            return "Matrix determinant";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.getFirst(), "det");
            ctx.requireSquareMatrix(matrix, "det");
            return new NodeDouble(calculateDeterminant(matrix));
        }
    };

    /**
     * Matrix trace (sum of diagonal elements)
     */
    public static final MathFunction TRACE = new MathFunction() {
        @Override
        public String name() {
            return "trace";
        }

        @Override
        public String description() {
            return "Matrix trace";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.getFirst(), "trace");
            ctx.requireSquareMatrix(matrix, "trace");

            double trace = 0;
            Node[][] elements = matrix.getElements();
            for (int i = 0; i < matrix.getRows(); i++) {
                trace += ((NodeConstant) elements[i][i]).doubleValue();
            }
            return new NodeDouble(trace);
        }
    };

    /**
     * Number of rows
     */
    public static final MathFunction ROWS = new MathFunction() {
        @Override
        public String name() {
            return "rows";
        }

        @Override
        public String description() {
            return "Number of matrix rows";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.getFirst(), "rows");
            return new NodeRational(matrix.getRows());
        }
    };

    /**
     * Number of columns
     */
    public static final MathFunction COLS = new MathFunction() {
        @Override
        public String name() {
            return "cols";
        }

        @Override
        public String description() {
            return "Number of matrix columns";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.getFirst(), "cols");
            return new NodeRational(matrix.getCols());
        }
    };

    // ==================== Matrix Transformations ====================

    /**
     * Matrix transpose
     */
    public static final MathFunction TRANSPOSE = new MathFunction() {
        @Override
        public String name() {
            return "transpose";
        }

        @Override
        public String description() {
            return "Matrix transpose";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.getFirst(), "transpose");
            Node[][] elements = matrix.getElements();
            Node[][] result = new Node[matrix.getCols()][matrix.getRows()];

            for (int i = 0; i < matrix.getRows(); i++) {
                for (int j = 0; j < matrix.getCols(); j++) {
                    result[j][i] = elements[i][j];
                }
            }

            return new NodeMatrix(result);
        }
    };

    /**
     * Identity matrix
     */
    public static final MathFunction IDENTITY = new MathFunction() {
        @Override
        public String name() {
            return "identity";
        }

        @Override
        public String description() {
            return "Identity matrix of given size";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            int n = (int) ctx.toNumber(args.getFirst()).doubleValue();
            if (n <= 0) {
                throw new IllegalArgumentException("identity: size must be positive, got " + n);
            }

            Node[][] result = new Node[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    result[i][j] = new NodeRational(i == j ? 1 : 0);
                }
            }
            return new NodeMatrix(result);
        }
    };

    /**
     * Zero matrix
     */
    public static final MathFunction ZEROS = new MathFunction() {
        @Override
        public String name() {
            return "zeros";
        }

        @Override
        public String description() {
            return "Zero matrix of given dimensions";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            int rows = (int) ctx.toNumber(args.get(0)).doubleValue();
            int cols = args.size() > 1
                    ? (int) ctx.toNumber(args.get(1)).doubleValue()
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
        }
    };

    /**
     * Ones matrix
     */
    public static final MathFunction ONES = new MathFunction() {
        @Override
        public String name() {
            return "ones";
        }

        @Override
        public String description() {
            return "Matrix of ones with given dimensions";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            int rows = (int) ctx.toNumber(args.get(0)).doubleValue();
            int cols = args.size() > 1
                    ? (int) ctx.toNumber(args.get(1)).doubleValue()
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
        }
    };

    /**
     * Diagonal matrix from vector or extract diagonal
     */
    public static final MathFunction DIAG = new MathFunction() {
        @Override
        public String name() {
            return "diag";
        }

        @Override
        public String description() {
            return "Create diagonal matrix or extract diagonal";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant arg = args.getFirst();

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

            throw new TypeError("diag requires a vector or matrix, got " +
                    arg.getClass().getSimpleName());
        }
    };

    // ==================== Advanced Matrix Operations ====================

    /**
     * Matrix inverse
     */
    public static final MathFunction INVERSE = new MathFunction() {
        @Override
        public String name() {
            return "inverse";
        }

        @Override
        public List<String> aliases() {
            return List.of("inv");
        }

        @Override
        public String description() {
            return "Matrix inverse";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.getFirst(), "inverse");
            ctx.requireSquareMatrix(matrix, "inverse");

            int n = matrix.getRows();
            double[][] m = toDoubleArray(matrix);
            double[][] inv = new double[n][n];

            // Initialize inverse as identity
            for (int i = 0; i < n; i++) {
                inv[i][i] = 1.0;
            }

            // Gauss-Jordan elimination
            for (int col = 0; col < n; col++) {
                // Find pivot
                int maxRow = col;
                for (int row = col + 1; row < n; row++) {
                    if (Math.abs(m[row][col]) > Math.abs(m[maxRow][col])) {
                        maxRow = row;
                    }
                }

                // Swap rows
                double[] temp = m[col];
                m[col] = m[maxRow];
                m[maxRow] = temp;
                temp = inv[col];
                inv[col] = inv[maxRow];
                inv[maxRow] = temp;

                // Check for singular matrix
                if (Math.abs(m[col][col]) < 1e-10) {
                    throw new IllegalArgumentException("inverse: matrix is singular");
                }

                // Scale pivot row
                double pivot = m[col][col];
                for (int j = 0; j < n; j++) {
                    m[col][j] /= pivot;
                    inv[col][j] /= pivot;
                }

                // Eliminate column
                for (int row = 0; row < n; row++) {
                    if (row != col) {
                        double factor = m[row][col];
                        for (int j = 0; j < n; j++) {
                            m[row][j] -= factor * m[col][j];
                            inv[row][j] -= factor * inv[col][j];
                        }
                    }
                }
            }

            return toNodeMatrix(inv);
        }
    };

    /**
     * Matrix rank
     */
    public static final MathFunction RANK = new MathFunction() {
        @Override
        public String name() {
            return "rank";
        }

        @Override
        public String description() {
            return "Matrix rank";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.getFirst(), "rank");
            double[][] m = toDoubleArray(matrix);

            int rows = m.length;
            int cols = m[0].length;
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
        }
    };

    /**
     * Frobenius norm
     */
    public static final MathFunction NORM = new MathFunction() {
        @Override
        public String name() {
            return "norm";
        }

        @Override
        public String description() {
            return "Frobenius norm (or vector 2-norm)";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant arg = args.getFirst();

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
        }
    };

    /**
     * Extract row from matrix
     */
    public static final MathFunction ROW = new MathFunction() {
        @Override
        public String name() {
            return "row";
        }

        @Override
        public String description() {
            return "Extract row from matrix";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.get(0), "row");
            int rowIdx = (int) ctx.toNumber(args.get(1)).doubleValue();

            if (rowIdx < 0 || rowIdx >= matrix.getRows()) {
                throw new IllegalArgumentException("row: index out of bounds");
            }

            Node[][] elements = matrix.getElements();
            Node[] row = new Node[matrix.getCols()];
            System.arraycopy(elements[rowIdx], 0, row, 0, matrix.getCols());
            return new NodeVector(row);
        }
    };

    /**
     * Extract column from matrix
     */
    public static final MathFunction COL = new MathFunction() {
        @Override
        public String name() {
            return "col";
        }

        @Override
        public List<String> aliases() {
            return List.of("column");
        }

        @Override
        public String description() {
            return "Extract column from matrix";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 2;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.get(0), "col");
            int colIdx = (int) ctx.toNumber(args.get(1)).doubleValue();

            if (colIdx < 0 || colIdx >= matrix.getCols()) {
                throw new IllegalArgumentException("col: index out of bounds");
            }

            Node[][] elements = matrix.getElements();
            Node[] col = new Node[matrix.getRows()];
            for (int i = 0; i < matrix.getRows(); i++) {
                col[i] = elements[i][colIdx];
            }
            return new NodeVector(col);
        }
    };

    /**
     * Reshape vector/matrix to new dimensions
     */
    public static final MathFunction RESHAPE = new MathFunction() {
        @Override
        public String name() {
            return "reshape";
        }

        @Override
        public String description() {
            return "Reshape to new dimensions";
        }

        @Override
        public int minArity() {
            return 3;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant data = args.get(0);
            int newRows = (int) ctx.toNumber(args.get(1)).doubleValue();
            int newCols = (int) ctx.toNumber(args.get(2)).doubleValue();

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
                    for (int j = 0; j < matrix.getCols(); j++) {
                        values.add(elements[i][j]);
                    }
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
        }
    };

    /**
     * Matrix minor (element-wise)
     */
    public static final MathFunction MINOR = new MathFunction() {
        @Override
        public String name() {
            return "minor";
        }

        @Override
        public String description() {
            return "Matrix minor (submatrix determinant)";
        }

        @Override
        public int minArity() {
            return 3;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.get(0), "minor");
            int row = (int) ctx.toNumber(args.get(1)).doubleValue();
            int col = (int) ctx.toNumber(args.get(2)).doubleValue();

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

            return new NodeDouble(calculateDeterminant(new NodeMatrix(sub)));
        }
    };

    /**
     * Cofactor matrix
     */
    public static final MathFunction COFACTOR = new MathFunction() {
        @Override
        public String name() {
            return "cofactor";
        }

        @Override
        public String description() {
            return "Cofactor at (row, col)";
        }

        @Override
        public int minArity() {
            return 3;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.get(0), "cofactor");
            int row = (int) ctx.toNumber(args.get(1)).doubleValue();
            int col = (int) ctx.toNumber(args.get(2)).doubleValue();

            // Cofactor = (-1)^(i+j) * minor
            double minor = MINOR.apply(args, ctx).doubleValue();
            double sign = ((row + col) % 2 == 0) ? 1 : -1;
            return new NodeDouble(sign * minor);
        }
    };

    /**
     * Adjugate matrix (transpose of cofactor matrix)
     */
    public static final MathFunction ADJUGATE = new MathFunction() {
        @Override
        public String name() {
            return "adjugate";
        }

        @Override
        public List<String> aliases() {
            return List.of("adj");
        }

        @Override
        public String description() {
            return "Adjugate matrix";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 1;
        }

        @Override
        public Category category() {
            return Category.MATRIX;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeMatrix matrix = ctx.requireMatrix(args.getFirst(), "adjugate");
            ctx.requireSquareMatrix(matrix, "adjugate");

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
        }
    };

    // ==================== All Functions ====================

    /**
     * Gets all matrix functions.
     */
    public static List<MathFunction> all() {
        return List.of(DET, TRACE, ROWS, COLS, TRANSPOSE, IDENTITY, ZEROS, ONES, DIAG,
                INVERSE, RANK, NORM, ROW, COL, RESHAPE, MINOR, COFACTOR, ADJUGATE);
    }

    // ==================== Helper Methods ====================

    private static double[][] toDoubleArray(NodeMatrix matrix) {
        int rows = matrix.getRows();
        int cols = matrix.getCols();
        double[][] result = new double[rows][cols];
        Node[][] elements = matrix.getElements();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = ((NodeConstant) elements[i][j]).doubleValue();
            }
        }
        return result;
    }

    private static NodeMatrix toNodeMatrix(double[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        Node[][] result = new Node[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = new NodeDouble(data[i][j]);
            }
        }
        return new NodeMatrix(result);
    }

    /**
     * Calculates the determinant of a square matrix using LU decomposition.
     */
    private static double calculateDeterminant(NodeMatrix matrix) {
        int n = matrix.getRows();
        double[][] m = new double[n][n];
        Node[][] elements = matrix.getElements();

        // Convert to double array
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m[i][j] = ((NodeConstant) elements[i][j]).doubleValue();
            }
        }

        // Simple recursive implementation for small matrices
        if (n == 1) {
            return m[0][0];
        }
        if (n == 2) {
            return m[0][0] * m[1][1] - m[0][1] * m[1][0];
        }

        // LU decomposition for larger matrices
        double det = 1.0;
        int swaps = 0;

        for (int k = 0; k < n; k++) {
            // Find pivot
            int pivot = k;
            for (int i = k + 1; i < n; i++) {
                if (Math.abs(m[i][k]) > Math.abs(m[pivot][k])) {
                    pivot = i;
                }
            }

            // Swap rows if needed
            if (pivot != k) {
                double[] temp = m[k];
                m[k] = m[pivot];
                m[pivot] = temp;
                swaps++;
            }

            // Check for singular matrix
            if (Math.abs(m[k][k]) < 1e-10) {
                return 0.0;
            }

            det *= m[k][k];

            // Eliminate
            for (int i = k + 1; i < n; i++) {
                double factor = m[i][k] / m[k][k];
                for (int j = k + 1; j < n; j++) {
                    m[i][j] -= factor * m[k][j];
                }
            }
        }

        return swaps % 2 == 0 ? det : -det;
    }
}
