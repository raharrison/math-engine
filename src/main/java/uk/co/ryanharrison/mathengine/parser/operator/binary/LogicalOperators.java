package uk.co.ryanharrison.mathengine.parser.operator.binary;

import uk.co.ryanharrison.mathengine.parser.evaluator.TypeError;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.function.BooleanSupplier;

/**
 * The logical operators, which take scalars only. Numbers count as true when non-zero.
 * {@code &&} and {@code ||} short-circuit, so the right operand may go unevaluated.
 */
public final class LogicalOperators {

    private LogicalOperators() {
    }

    /**
     * Stops at a false left operand.
     */
    public static final BinaryOperator AND = new ShortCircuit("&&", false);

    /**
     * Stops at a true left operand.
     */
    public static final BinaryOperator OR = new ShortCircuit("||", true);

    public static final BinaryOperator XOR = (left, right, ctx) ->
            NodeBoolean.of(operand(left, "xor") ^ operand(right, "xor"));

    private record ShortCircuit(String symbol, boolean decidingValue) implements BinaryOperator {

        @Override
        public boolean requiresShortCircuit() {
            return true;
        }

        @Override
        public NodeConstant shortCircuitResult(NodeConstant left, OperatorContext ctx) {
            return operand(left, symbol) == decidingValue ? NodeBoolean.of(decidingValue) : null;
        }

        @Override
        public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
            return NodeBoolean.of(combine(() -> operand(left, symbol), () -> operand(right, symbol)));
        }

        private boolean combine(BooleanSupplier left, BooleanSupplier right) {
            return left.getAsBoolean() == decidingValue ? decidingValue : right.getAsBoolean();
        }
    }

    private static boolean operand(NodeConstant value, String symbol) {
        if (value.isVector() || value.isMatrix()) {
            throw new TypeError("Logical '" + symbol + "' does not work on collections");
        }
        return TypeCoercion.toBoolean(value);
    }
}
