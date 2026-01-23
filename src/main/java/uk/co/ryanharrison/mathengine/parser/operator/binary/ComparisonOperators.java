package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeMatrix;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;
import uk.co.ryanharrison.mathengine.parser.util.NumericOperations;

/**
 * Collection of comparison operators.
 * <p>
 * All comparison operators support broadcasting for maximum flexibility:
 * </p>
 * <ul>
 *     <li>Scalar comparisons: {@code 5 < 10} → {@code true}</li>
 *     <li>Vector comparisons: {@code {1,2,3} < {2,2,2}} → {@code {true, false, false}}</li>
 *     <li>Broadcasting: {@code {1,2,3} < 2} → {@code {true, false, false}}</li>
 *     <li>Ordering operators (&lt;, &gt;, &lt;=, &gt;=) work only on numeric types</li>
 *     <li>Equality operators (==, !=) work on all types using structural equality</li>
 * </ul>
 */
public final class ComparisonOperators {

    private ComparisonOperators() {
    }

    /**
     * Less than operator (<).
     * <p>
     * Supports unit conversion for same-type units (e.g., 500 cm < 6 m).
     * </p>
     */
    public static final BinaryOperator LESS_THAN = new BinaryOperator() {
        @Override
        public String symbol() {
            return "<";
        }

        @Override
        public String displayName() {
            return "less than";
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            // Ordering operators don't work on containers
            if (left instanceof NodeVector || left instanceof NodeMatrix ||
                    right instanceof NodeVector || right instanceof NodeMatrix) {
                throw new TypeError("Cannot use '<' on containers. Use '==' or '!=' for equality checks.");
            }

            if (!left.isNumeric() || !right.isNumeric()) {
                throw new TypeError("Cannot compare non-numeric types with '<': " +
                        left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
            }

            return NodeBoolean.of(NumericOperations.isLessThan(left, right));
        }
    };

    /**
     * Greater than operator (>).
     * <p>
     * Supports unit conversion for same-type units (e.g., 6 m > 500 cm).
     * </p>
     */
    public static final BinaryOperator GREATER_THAN = new BinaryOperator() {
        @Override
        public String symbol() {
            return ">";
        }

        @Override
        public String displayName() {
            return "greater than";
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            // Ordering operators don't work on containers
            if (left instanceof NodeVector || left instanceof NodeMatrix ||
                    right instanceof NodeVector || right instanceof NodeMatrix) {
                throw new TypeError("Cannot use '>' on containers. Use '==' or '!=' for equality checks.");
            }

            if (!left.isNumeric() || !right.isNumeric()) {
                throw new TypeError("Cannot compare non-numeric types with '>': " +
                        left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
            }

            return NodeBoolean.of(NumericOperations.isGreaterThan(left, right));
        }
    };

    /**
     * Less than or equal operator (<=).
     * <p>
     * Supports unit conversion for same-type units.
     * </p>
     */
    public static final BinaryOperator LESS_THAN_OR_EQUAL = new BinaryOperator() {
        @Override
        public String symbol() {
            return "<=";
        }

        @Override
        public String displayName() {
            return "less than or equal";
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            // Ordering operators don't work on containers
            if (left instanceof NodeVector || left instanceof NodeMatrix ||
                    right instanceof NodeVector || right instanceof NodeMatrix) {
                throw new TypeError("Cannot use '<=' on containers. Use '==' or '!=' for equality checks.");
            }

            if (!left.isNumeric() || !right.isNumeric()) {
                throw new TypeError("Cannot compare non-numeric types with '<=': " +
                        left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
            }

            return NodeBoolean.of(NumericOperations.isLessThanOrEqual(left, right));
        }
    };

    /**
     * Greater than or equal operator (>=).
     * <p>
     * Supports unit conversion for same-type units.
     * </p>
     */
    public static final BinaryOperator GREATER_THAN_OR_EQUAL = new BinaryOperator() {
        @Override
        public String symbol() {
            return ">=";
        }

        @Override
        public String displayName() {
            return "greater than or equal";
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            // Ordering operators don't work on containers
            if (left instanceof NodeVector || left instanceof NodeMatrix ||
                    right instanceof NodeVector || right instanceof NodeMatrix) {
                throw new TypeError("Cannot use '>=' on containers. Use '==' or '!=' for equality checks.");
            }

            if (!left.isNumeric() || !right.isNumeric()) {
                throw new TypeError("Cannot compare non-numeric types with '>=': " +
                        left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
            }

            return NodeBoolean.of(NumericOperations.isGreaterThanOrEqual(left, right));
        }
    };

    /**
     * Equality operator (==).
     * <p>
     * Works on all types with proper type-aware comparison:
     * </p>
     * <ul>
     *     <li>Unit vs Unit (same type): auto-converts to common unit, compares with tolerance</li>
     *     <li>Unit vs scalar: compares with tolerance (unit conversions may have precision issues)</li>
     *     <li>Both Rational: exact comparison (rationals are exact)</li>
     *     <li>Double involved (no units): exact comparison (floating-point errors visible)</li>
     *     <li>Containers: structural equality via equals()</li>
     * </ul>
     */
    public static final BinaryOperator EQUAL = new BinaryOperator() {
        @Override
        public String symbol() {
            return "==";
        }

        @Override
        public String displayName() {
            return "equal";
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            // For containers (vectors/matrices), use structural equality
            if ((left instanceof NodeVector || left instanceof NodeMatrix) ||
                    (right instanceof NodeVector || right instanceof NodeMatrix)) {
                return NodeBoolean.of(left.equals(right));
            }

            // Identity check
            if (left == right) return NodeBoolean.TRUE;

            // Numeric comparison with proper type handling
            if (left.isNumeric() && right.isNumeric()) {
                return NodeBoolean.of(NumericOperations.areEqual(left, right));
            }

            // Non-numeric: structural equality
            return NodeBoolean.of(left.equals(right));
        }
    };

    /**
     * Not equal operator (!=).
     * <p>
     * Returns the logical negation of the equality operator.
     * Uses the same type-aware comparison rules as ==.
     * </p>
     */
    public static final BinaryOperator NOT_EQUAL = new BinaryOperator() {
        @Override
        public String symbol() {
            return "!=";
        }

        @Override
        public String displayName() {
            return "not equal";
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            // For containers (vectors/matrices), use structural inequality
            if ((left instanceof NodeVector || left instanceof NodeMatrix) ||
                    (right instanceof NodeVector || right instanceof NodeMatrix)) {
                return NodeBoolean.of(!left.equals(right));
            }

            // Identity check
            if (left == right) return NodeBoolean.FALSE;

            // Numeric comparison with proper type handling
            if (left.isNumeric() && right.isNumeric()) {
                return NodeBoolean.of(!NumericOperations.areEqual(left, right));
            }

            // Non-numeric: structural inequality
            return NodeBoolean.of(!left.equals(right));
        }
    };
}
