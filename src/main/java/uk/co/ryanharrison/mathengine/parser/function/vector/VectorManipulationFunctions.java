package uk.co.ryanharrison.mathengine.parser.function.vector;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Collection of vector manipulation and transformation functions.
 */
public final class VectorManipulationFunctions {

    private VectorManipulationFunctions() {
    }

    // ==================== Slicing Functions ====================

    /**
     * Take first n elements
     */
    public static final MathFunction TAKE = new MathFunction() {
        @Override
        public String name() {
            return "take";
        }

        @Override
        public String description() {
            return "Take first n elements";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "take");
            int n = (int) ctx.toNumber(args.get(1)).doubleValue();

            if (n < 0) n = 0;
            n = Math.min(n, vector.size());

            Node[] result = new Node[n];
            for (int i = 0; i < n; i++) {
                result[i] = vector.getElement(i);
            }
            return new NodeVector(result);
        }
    };

    /**
     * Drop first n elements
     */
    public static final MathFunction DROP = new MathFunction() {
        @Override
        public String name() {
            return "drop";
        }

        @Override
        public String description() {
            return "Drop first n elements";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "drop");
            int n = (int) ctx.toNumber(args.get(1)).doubleValue();

            if (n < 0) n = 0;
            if (n >= vector.size()) {
                return new NodeVector(new Node[0]);
            }

            int newLen = vector.size() - n;
            Node[] result = new Node[newLen];
            for (int i = 0; i < newLen; i++) {
                result[i] = vector.getElement(n + i);
            }
            return new NodeVector(result);
        }
    };

    /**
     * Slice vector [start, end) with optional step
     */
    public static final MathFunction SLICE = new MathFunction() {
        @Override
        public String name() {
            return "slice";
        }

        @Override
        public String description() {
            return "Slice vector [start, end) with optional step";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return 4;
        }

        @Override
        public Category category() {
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "slice");
            int start = (int) ctx.toNumber(args.get(1)).doubleValue();
            int end = args.size() > 2
                    ? (int) ctx.toNumber(args.get(2)).doubleValue()
                    : vector.size();
            int step = args.size() > 3
                    ? (int) ctx.toNumber(args.get(3)).doubleValue()
                    : 1;

            if (step == 0) {
                throw new TypeError("slice: step cannot be 0");
            }

            // Handle negative indices
            if (start < 0) start = Math.max(0, vector.size() + start);
            if (end < 0) end = vector.size() + end;

            start = Math.max(0, Math.min(start, vector.size()));
            end = Math.max(0, Math.min(end, vector.size()));

            List<Node> result = new ArrayList<>();
            if (step > 0) {
                for (int i = start; i < end; i += step) {
                    result.add(vector.getElement(i));
                }
            } else {
                for (int i = start; i > end; i += step) {
                    result.add(vector.getElement(i));
                }
            }
            return new NodeVector(result.toArray(Node[]::new));
        }
    };

    // ==================== Element Access Functions ====================

    /**
     * Get element at index
     */
    public static final MathFunction GET = new MathFunction() {
        @Override
        public String name() {
            return "get";
        }

        @Override
        public List<String> aliases() {
            return List.of("at", "nth");
        }

        @Override
        public String description() {
            return "Get element at index (0-based)";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "get");
            int index = (int) ctx.toNumber(args.get(1)).doubleValue();

            // Handle negative indices
            if (index < 0) index = vector.size() + index;

            if (index < 0 || index >= vector.size()) {
                throw new TypeError("get: index " + index + " out of bounds for vector of size " + vector.size());
            }
            return (NodeConstant) vector.getElement(index);
        }
    };

    /**
     * Find index of element
     */
    public static final MathFunction INDEXOF = new MathFunction() {
        @Override
        public String name() {
            return "indexof";
        }

        @Override
        public List<String> aliases() {
            return List.of("find");
        }

        @Override
        public String description() {
            return "Find first index of element (-1 if not found)";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "indexof");
            double target = ctx.toNumber(args.get(1)).doubleValue();

            for (int i = 0; i < vector.size(); i++) {
                double val = ctx.toNumber((NodeConstant) vector.getElement(i)).doubleValue();
                if (val == target) {
                    return new NodeRational(i);
                }
            }
            return new NodeRational(-1);
        }
    };

    /**
     * Check if vector contains element
     */
    public static final MathFunction CONTAINS = new MathFunction() {
        @Override
        public String name() {
            return "contains";
        }

        @Override
        public List<String> aliases() {
            return List.of("includes");
        }

        @Override
        public String description() {
            return "Check if vector contains element";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "contains");
            double target = ctx.toNumber(args.get(1)).doubleValue();

            for (int i = 0; i < vector.size(); i++) {
                double val = ctx.toNumber((NodeConstant) vector.getElement(i)).doubleValue();
                if (val == target) {
                    return new NodeBoolean(true);
                }
            }
            return new NodeBoolean(false);
        }
    };

    // ==================== Transformation Functions ====================

    /**
     * Remove duplicates
     */
    public static final MathFunction UNIQUE = new MathFunction() {
        @Override
        public String name() {
            return "unique";
        }

        @Override
        public List<String> aliases() {
            return List.of("distinct");
        }

        @Override
        public String description() {
            return "Remove duplicate elements";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.getFirst(), "unique");

            Set<Double> seen = new LinkedHashSet<>();
            List<Node> result = new ArrayList<>();

            for (int i = 0; i < vector.size(); i++) {
                double val = ctx.toNumber((NodeConstant) vector.getElement(i)).doubleValue();
                if (seen.add(val)) {
                    result.add(vector.getElement(i));
                }
            }
            return new NodeVector(result.toArray(Node[]::new));
        }
    };

    /**
     * Concatenate vectors
     */
    public static final MathFunction CONCAT = new MathFunction() {
        @Override
        public String name() {
            return "concat";
        }

        @Override
        public List<String> aliases() {
            return List.of("append");
        }

        @Override
        public String description() {
            return "Concatenate vectors";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            List<Node> result = new ArrayList<>();

            for (NodeConstant arg : args) {
                if (arg instanceof NodeVector vector) {
                    for (int i = 0; i < vector.size(); i++) {
                        result.add(vector.getElement(i));
                    }
                } else {
                    result.add(arg);
                }
            }
            return new NodeVector(result.toArray(Node[]::new));
        }
    };

    /**
     * Flatten nested vectors
     */
    public static final MathFunction FLATTEN = new MathFunction() {
        @Override
        public String name() {
            return "flatten";
        }

        @Override
        public String description() {
            return "Flatten nested vectors";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            List<Node> result = new ArrayList<>();
            flatten(args.getFirst(), result);
            return new NodeVector(result.toArray(Node[]::new));
        }

        private void flatten(NodeConstant node, List<Node> result) {
            if (node instanceof NodeVector vector) {
                for (int i = 0; i < vector.size(); i++) {
                    flatten((NodeConstant) vector.getElement(i), result);
                }
            } else if (node instanceof NodeMatrix matrix) {
                for (int i = 0; i < matrix.getRows(); i++) {
                    for (int j = 0; j < matrix.getCols(); j++) {
                        flatten((NodeConstant) matrix.getElements()[i][j], result);
                    }
                }
            } else {
                result.add(node);
            }
        }
    };

    /**
     * Zip two vectors into pairs
     */
    public static final MathFunction ZIP = new MathFunction() {
        @Override
        public String name() {
            return "zip";
        }

        @Override
        public String description() {
            return "Zip vectors into matrix of pairs";
        }

        @Override
        public int minArity() {
            return 2;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            // Get all vectors
            List<NodeVector> vectors = new ArrayList<>();
            for (NodeConstant arg : args) {
                vectors.add(ctx.requireVector(arg, "zip"));
            }

            // Find minimum length
            int minLen = vectors.stream().mapToInt(NodeVector::size).min().orElse(0);

            // Create result matrix
            Node[][] result = new Node[minLen][vectors.size()];
            for (int i = 0; i < minLen; i++) {
                for (int j = 0; j < vectors.size(); j++) {
                    result[i][j] = vectors.get(j).getElement(i);
                }
            }
            return new NodeMatrix(result);
        }
    };

    /**
     * Repeat vector n times
     */
    public static final MathFunction REPEAT = new MathFunction() {
        @Override
        public String name() {
            return "repeat";
        }

        @Override
        public List<String> aliases() {
            return List.of("replicate");
        }

        @Override
        public String description() {
            return "Repeat value or vector n times";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant input = args.get(0);
            int n = (int) ctx.toNumber(args.get(1)).doubleValue();

            if (n < 0) n = 0;

            List<Node> result = new ArrayList<>();

            // Handle both scalar and vector inputs
            if (input instanceof NodeVector vector) {
                for (int rep = 0; rep < n; rep++) {
                    for (int i = 0; i < vector.size(); i++) {
                        result.add(vector.getElement(i));
                    }
                }
            } else {
                // Scalar: repeat the value n times
                for (int rep = 0; rep < n; rep++) {
                    result.add(input);
                }
            }

            return new NodeVector(result.toArray(Node[]::new));
        }
    };

    // ==================== Predicate Functions ====================

    /**
     * Count elements equal to value
     */
    public static final MathFunction COUNT = new MathFunction() {
        @Override
        public String name() {
            return "count";
        }

        @Override
        public String description() {
            return "Count occurrences of value";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeVector vector = ctx.requireVector(args.get(0), "count");
            double target = ctx.toNumber(args.get(1)).doubleValue();

            int count = 0;
            for (int i = 0; i < vector.size(); i++) {
                double val = ctx.toNumber((NodeConstant) vector.getElement(i)).doubleValue();
                if (val == target) count++;
            }
            return new NodeRational(count);
        }
    };

    /**
     * Check if any element is truthy (non-zero)
     */
    public static final MathFunction ANY = new MathFunction() {
        @Override
        public String name() {
            return "any";
        }

        @Override
        public String description() {
            return "Check if any element is truthy";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            for (NodeConstant arg : args) {
                if (arg instanceof NodeVector vector) {
                    for (int i = 0; i < vector.size(); i++) {
                        if (ctx.toBoolean((NodeConstant) vector.getElement(i))) {
                            return new NodeBoolean(true);
                        }
                    }
                } else {
                    if (ctx.toBoolean(arg)) {
                        return new NodeBoolean(true);
                    }
                }
            }
            return new NodeBoolean(false);
        }
    };

    /**
     * Check if all elements are truthy (non-zero)
     */
    public static final MathFunction ALL = new MathFunction() {
        @Override
        public String name() {
            return "all";
        }

        @Override
        public String description() {
            return "Check if all elements are truthy";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            for (NodeConstant arg : args) {
                if (arg instanceof NodeVector vector) {
                    for (int i = 0; i < vector.size(); i++) {
                        if (!ctx.toBoolean((NodeConstant) vector.getElement(i))) {
                            return new NodeBoolean(false);
                        }
                    }
                } else {
                    if (!ctx.toBoolean(arg)) {
                        return new NodeBoolean(false);
                    }
                }
            }
            return new NodeBoolean(true);
        }
    };

    /**
     * Check if no elements are truthy
     */
    public static final MathFunction NONE = new MathFunction() {
        @Override
        public String name() {
            return "none";
        }

        @Override
        public String description() {
            return "Check if no elements are truthy";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Category category() {
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            for (NodeConstant arg : args) {
                if (arg instanceof NodeVector vector) {
                    for (int i = 0; i < vector.size(); i++) {
                        if (ctx.toBoolean((NodeConstant) vector.getElement(i))) {
                            return new NodeBoolean(false);
                        }
                    }
                } else {
                    if (ctx.toBoolean(arg)) {
                        return new NodeBoolean(false);
                    }
                }
            }
            return new NodeBoolean(true);
        }
    };

    // ==================== Generator Functions ====================

    /**
     * Generate range [start, end) with optional step
     */
    public static final MathFunction RANGEGEN = new MathFunction() {
        @Override
        public String name() {
            return "seq";
        }

        @Override
        public List<String> aliases() {
            return List.of("sequence", "arange");
        }

        @Override
        public String description() {
            return "Generate sequence [start, end] with step";
        }

        @Override
        public int minArity() {
            return 1;
        }

        @Override
        public int maxArity() {
            return 3;
        }

        @Override
        public Category category() {
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            double start, end, step;

            if (args.size() == 1) {
                start = 0;
                end = ctx.toNumber(args.get(0)).doubleValue();
                step = 1;
            } else if (args.size() == 2) {
                start = ctx.toNumber(args.get(0)).doubleValue();
                end = ctx.toNumber(args.get(1)).doubleValue();
                step = 1;
            } else {
                start = ctx.toNumber(args.get(0)).doubleValue();
                end = ctx.toNumber(args.get(1)).doubleValue();
                step = ctx.toNumber(args.get(2)).doubleValue();
            }

            if (step == 0) {
                throw new TypeError("seq: step cannot be 0");
            }

            List<Node> result = new ArrayList<>();
            if (step > 0) {
                for (double i = start; i <= end; i += step) {
                    result.add(new NodeDouble(i));
                }
            } else {
                for (double i = start; i >= end; i += step) {
                    result.add(new NodeDouble(i));
                }
            }
            return new NodeVector(result.toArray(Node[]::new));
        }
    };

    /**
     * Generate n evenly spaced values between start and end
     */
    public static final MathFunction LINSPACE = new MathFunction() {
        @Override
        public String name() {
            return "linspace";
        }

        @Override
        public String description() {
            return "Generate n evenly spaced values";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            double start = ctx.toNumber(args.get(0)).doubleValue();
            double end = ctx.toNumber(args.get(1)).doubleValue();
            int n = (int) ctx.toNumber(args.get(2)).doubleValue();

            if (n < 1) {
                throw new TypeError("linspace: n must be at least 1");
            }

            if (n == 1) {
                return new NodeVector(new Node[]{new NodeDouble(start)});
            }

            Node[] result = new Node[n];
            double step = (end - start) / (n - 1);
            for (int i = 0; i < n; i++) {
                result[i] = new NodeDouble(start + i * step);
            }
            return new NodeVector(result);
        }
    };

    /**
     * Fill with value
     */
    public static final MathFunction FILL = new MathFunction() {
        @Override
        public String name() {
            return "fill";
        }

        @Override
        public String description() {
            return "Create vector filled with value";
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
            return Category.VECTOR;
        }

        @Override
        public boolean supportsVectorBroadcasting() {
            return false;
        }

        @Override
        public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
            NodeConstant value = args.get(0);
            int n = (int) ctx.toNumber(args.get(1)).doubleValue();

            if (n < 0) n = 0;

            Node[] result = new Node[n];
            for (int i = 0; i < n; i++) {
                result[i] = value;
            }
            return new NodeVector(result);
        }
    };

    /**
     * Gets all vector manipulation functions.
     */
    public static List<MathFunction> all() {
        return List.of(
                TAKE, DROP, SLICE,
                GET, INDEXOF, CONTAINS,
                UNIQUE, CONCAT, FLATTEN, ZIP, REPEAT,
                COUNT, ANY, ALL, NONE,
                RANGEGEN, LINSPACE, FILL
        );
    }
}
