package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Immutable executor for operators.
 * <p>
 * Once created, the executor cannot be modified, making it thread-safe.
 * All operators must be provided at construction time.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Create with standard operators
 * OperatorExecutor executor = OperatorExecutor.of(
 *     StandardBinaryOperators.all(),
 *     StandardUnaryOperators.all()
 * );
 *
 * // Create with custom operators
 * OperatorExecutor executor = OperatorExecutor.builder()
 *     .binary(TokenType.PLUS, new AddOperator())
 *     .unary(TokenType.MINUS, new NegateOperator())
 *     .build();
 *
 * // Execute operations
 * NodeConstant result = executor.executeBinary(TokenType.PLUS, left, () -> right, ctx);
 * NodeConstant negated = executor.executeUnary(TokenType.MINUS, operand, ctx);
 * }</pre>
 */
public final class OperatorExecutor {

    private final Map<TokenType, BinaryOperator> binaryOperators;
    private final Map<TokenType, UnaryOperator> unaryOperators;

    private OperatorExecutor(Map<TokenType, BinaryOperator> binaryOperators,
                             Map<TokenType, UnaryOperator> unaryOperators) {
        this.binaryOperators = Map.copyOf(binaryOperators);
        this.unaryOperators = Map.copyOf(unaryOperators);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an empty operator executor.
     *
     * @return new empty executor
     */
    public static OperatorExecutor empty() {
        return new OperatorExecutor(Map.of(), Map.of());
    }

    /**
     * Creates an operator executor with the given operators.
     *
     * @param binaryOperators the binary operators
     * @param unaryOperators  the unary operators
     * @return new executor with all operators registered
     */
    public static OperatorExecutor of(Map<TokenType, BinaryOperator> binaryOperators,
                                      Map<TokenType, UnaryOperator> unaryOperators) {
        return new OperatorExecutor(binaryOperators, unaryOperators);
    }

    /**
     * Creates a new builder for constructing operator executors.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Execution ====================

    /**
     * Executes a binary operation.
     * <p>
     * Supports short-circuit evaluation: for operators that require it (like && and ||),
     * the right operand evaluator is only called if necessary.
     *
     * @param tokenType      the operator token type
     * @param left           the evaluated left operand
     * @param rightEvaluator lazy evaluator for the right operand
     * @param ctx            the operator context (caller constructs with appropriate capabilities)
     * @return the result of the operation
     * @throws EvaluationException if the operator is not registered
     */
    public NodeConstant executeBinary(TokenType tokenType, NodeConstant left,
                                      Supplier<NodeConstant> rightEvaluator,
                                      OperatorContext ctx) {
        BinaryOperator operator = binaryOperators.get(tokenType);
        if (operator == null) {
            throw new EvaluationException("Unsupported binary operator: " + tokenType);
        }

        // Check for short-circuit
        if (operator.requiresShortCircuit()) {
            NodeConstant shortCircuitResult = operator.shortCircuitResult(left, ctx);
            if (shortCircuitResult != null) {
                return shortCircuitResult;
            }
        }

        // Evaluate right operand and apply operator
        NodeConstant right = rightEvaluator.get();
        return operator.apply(left, right, ctx);
    }

    /**
     * Executes a unary operation.
     *
     * @param tokenType the operator token type
     * @param operand   the operand
     * @param ctx       the operator context
     * @return the result of the operation
     * @throws EvaluationException if the operator is not registered
     */
    public NodeConstant executeUnary(TokenType tokenType, NodeConstant operand,
                                     OperatorContext ctx) {
        UnaryOperator operator = unaryOperators.get(tokenType);
        if (operator == null) {
            throw new EvaluationException("Unsupported unary operator: " + tokenType);
        }

        return operator.apply(operand, ctx);
    }

    // ==================== Builder ====================

    /**
     * Builder for constructing {@link OperatorExecutor} instances.
     */
    public static final class Builder {
        private final Map<TokenType, BinaryOperator> binaryOperators = new HashMap<>();
        private final Map<TokenType, UnaryOperator> unaryOperators = new HashMap<>();

        private Builder() {
        }

        /**
         * Adds a binary operator.
         *
         * @param tokenType the token type
         * @param operator  the operator implementation
         * @return this builder
         */
        public Builder binary(TokenType tokenType, BinaryOperator operator) {
            if (tokenType == null) {
                throw new IllegalArgumentException("Token type cannot be null");
            }
            if (operator == null) {
                throw new IllegalArgumentException("Operator cannot be null");
            }
            binaryOperators.put(tokenType, operator);
            return this;
        }

        /**
         * Adds multiple binary operators.
         *
         * @param operators map of token types to operators
         * @return this builder
         */
        public Builder binaryAll(Map<TokenType, BinaryOperator> operators) {
            for (var entry : operators.entrySet()) {
                binary(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Adds a unary operator.
         *
         * @param tokenType the token type
         * @param operator  the operator implementation
         * @return this builder
         */
        public Builder unary(TokenType tokenType, UnaryOperator operator) {
            if (tokenType == null) {
                throw new IllegalArgumentException("Token type cannot be null");
            }
            if (operator == null) {
                throw new IllegalArgumentException("Operator cannot be null");
            }
            unaryOperators.put(tokenType, operator);
            return this;
        }

        /**
         * Adds multiple unary operators.
         *
         * @param operators map of token types to operators
         * @return this builder
         */
        public Builder unaryAll(Map<TokenType, UnaryOperator> operators) {
            for (var entry : operators.entrySet()) {
                unary(entry.getKey(), entry.getValue());
            }
            return this;
        }

        /**
         * Builds the operator executor.
         *
         * @return new operator executor
         */
        public OperatorExecutor build() {
            return new OperatorExecutor(binaryOperators, unaryOperators);
        }
    }
}
