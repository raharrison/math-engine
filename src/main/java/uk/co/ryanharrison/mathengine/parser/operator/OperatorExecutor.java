package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationException;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for operators that manages registration and execution.
 * <p>
 * This class serves as the central registry for all operators and handles
 * dispatching operations to the appropriate operator implementation.
 *
 * <h2>Usage:</h2>
 * <pre>{@code
 * OperatorExecutor executor = new OperatorExecutor();
 *
 * // Register standard operators
 * executor.registerBinaryOperators(StandardBinaryOperators.all());
 * executor.registerUnaryOperators(StandardUnaryOperators.all());
 *
 * // Execute operations
 * NodeConstant result = executor.executeBinary(TokenType.PLUS, left, right, context);
 * NodeConstant negated = executor.executeUnary(TokenType.MINUS, operand, context);
 * }</pre>
 */
public final class OperatorExecutor {

    private final Map<TokenType, BinaryOperator> binaryOperators;
    private final Map<TokenType, UnaryOperator> unaryOperators;

    /**
     * Creates a new operator executor with no registered operators.
     */
    public OperatorExecutor() {
        this.binaryOperators = new HashMap<>();
        this.unaryOperators = new HashMap<>();
    }

    // ==================== Registration ====================

    /**
     * Registers multiple binary operators.
     *
     * @param operators map of token types to operators
     * @return this executor for method chaining
     */
    public OperatorExecutor registerBinaryOperators(Map<TokenType, BinaryOperator> operators) {
        binaryOperators.putAll(operators);
        return this;
    }

    /**
     * Registers multiple unary operators.
     *
     * @param operators map of token types to operators
     * @return this executor for method chaining
     */
    public OperatorExecutor registerUnaryOperators(Map<TokenType, UnaryOperator> operators) {
        unaryOperators.putAll(operators);
        return this;
    }

    // ==================== Execution ====================

    /**
     * Executes a binary operation.
     *
     * @param tokenType the operator token type
     * @param left      the left operand
     * @param right     the right operand
     * @param context   the evaluation context
     * @return the result of the operation
     * @throws EvaluationException if the operator is not registered
     */
    public NodeConstant executeBinary(TokenType tokenType, NodeConstant left,
                                      NodeConstant right, EvaluationContext context) {
        BinaryOperator operator = binaryOperators.get(tokenType);
        if (operator == null) {
            throw new EvaluationException("Unsupported binary operator: " + tokenType);
        }

        OperatorContext ctx = new OperatorContext(context);
        return operator.apply(left, right, ctx);
    }

    /**
     * Executes a binary operation with short-circuit evaluation support.
     * <p>
     * For operators that require short-circuit evaluation (like && and ||),
     * the right operand evaluator is only called if necessary.
     *
     * @param tokenType      the operator token type
     * @param left           the evaluated left operand
     * @param rightEvaluator lazy evaluator for the right operand
     * @param context        the evaluation context
     * @return the result of the operation
     */
    public NodeConstant executeBinaryShortCircuit(TokenType tokenType, NodeConstant left,
                                                  java.util.function.Supplier<NodeConstant> rightEvaluator,
                                                  EvaluationContext context) {
        BinaryOperator operator = binaryOperators.get(tokenType);
        if (operator == null) {
            throw new EvaluationException("Unsupported binary operator: " + tokenType);
        }

        OperatorContext ctx = new OperatorContext(context);

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
     * @param context   the evaluation context
     * @return the result of the operation
     * @throws EvaluationException if the operator is not registered
     */
    public NodeConstant executeUnary(TokenType tokenType, NodeConstant operand,
                                     EvaluationContext context) {
        UnaryOperator operator = unaryOperators.get(tokenType);
        if (operator == null) {
            throw new EvaluationException("Unsupported unary operator: " + tokenType);
        }

        OperatorContext ctx = new OperatorContext(context);
        return operator.apply(operand, ctx);
    }
}
