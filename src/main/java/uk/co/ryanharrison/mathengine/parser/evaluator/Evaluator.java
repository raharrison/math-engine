package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.handler.*;
import uk.co.ryanharrison.mathengine.parser.function.FunctionExecutor;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorExecutor;
import uk.co.ryanharrison.mathengine.parser.registry.UnitDefinition;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.ArrayDeque;

/**
 * Evaluates an Abstract Syntax Tree to produce a value.
 * <p>
 * Evaluation is a pure function of (node, scope): the scope travels as an argument,
 * so a single evaluator can serve nested calls and independent sessions.
 * Specialised concerns are delegated to {@link VariableResolver},
 * {@link SubscriptHandler}, {@link ElementAssignmentHandler}, {@link FunctionCallHandler}
 * and {@link ComprehensionHandler}.
 *
 * @see uk.co.ryanharrison.mathengine.parser.MathEngine
 */
public final class Evaluator {

    private final MathEngineConfig config;
    private final EvaluationContext rootContext;
    private final OperatorExecutor operatorExecutor;

    private final VariableResolver variableResolver;
    private final SubscriptHandler subscriptHandler;
    private final ElementAssignmentHandler elementAssignmentHandler;
    private final FunctionCallHandler functionCallHandler;
    private final ComprehensionHandler comprehensionHandler;

    public Evaluator(MathEngineConfig config, EvaluationContext rootContext,
                     OperatorExecutor operatorExecutor, FunctionExecutor functionExecutor) {
        this.config = config;
        this.rootContext = rootContext;
        this.operatorExecutor = operatorExecutor;

        this.variableResolver = new VariableResolver(config);
        this.subscriptHandler = new SubscriptHandler(config, this::evaluate);
        this.elementAssignmentHandler = new ElementAssignmentHandler(config, this::evaluate);
        this.functionCallHandler = new FunctionCallHandler(config, functionExecutor, this::evaluate);
        this.comprehensionHandler = new ComprehensionHandler(config, this::evaluate);
    }

    /**
     * The scope used by {@link #evaluate(Node)}.
     */
    public EvaluationContext getContext() {
        return rootContext;
    }

    /** Evaluates a node in the root scope. */
    public NodeConstant evaluate(Node node) {
        return evaluate(node, rootContext);
    }

    /**
     * Evaluates a node in the given scope.
     *
     * @throws EvaluationException if evaluation fails
     */
    public NodeConstant evaluate(Node node, EvaluationContext context) {
        return switch (node) {
            // Constants that still need work before they are values
            case NodeLambda lambda -> functionCallHandler.evaluateLambda(lambda, context);
            case NodeRange range -> range.toVector();
            case NodeVector vector -> evaluateVectorElements(vector, context);
            case NodeMatrix matrix -> evaluateMatrixElements(matrix, context);
            case NodeConstant constant -> constant;

            // Expressions
            case NodeVariable variable -> variableResolver.resolve(
                    variable, context.operatorContext(functionCallHandler));
            case NodeUnitRef unitRef -> variableResolver.resolveUnitRef(unitRef.getUnitName(), context);
            case NodeVarRef varRef -> variableResolver.resolveVarRef(varRef.getVarName(), context);
            case NodeConstRef constRef -> variableResolver.resolveConstRef(constRef.getConstName(), context);
            case NodeBinary binary -> evaluateBinary(binary, context);
            case NodeUnary unary -> evaluateUnary(unary, context);
            case NodeAssignment assignment -> evaluateAssignment(assignment, context);
            case NodeElementAssignment assignment -> elementAssignmentHandler.evaluate(assignment, context);
            case NodeSubscript subscript -> subscriptHandler.evaluate(subscript, context);
            case NodeRangeExpression rangeExpr -> evaluateRangeExpression(rangeExpr, context);
            case NodeFunctionDef funcDef -> functionCallHandler.evaluateFunctionDef(funcDef, context);
            case NodeCall call -> functionCallHandler.evaluate(call, context);
            case NodeSequence sequence -> evaluateSequence(sequence, context);
            case NodeComprehension comprehension -> comprehensionHandler.evaluate(comprehension, context);
            case NodeUnitConversion unitConversion -> evaluateUnitConversion(unitConversion, context);
        };
    }

    // ==================== Collections ====================

    private NodeConstant evaluateVectorElements(NodeVector vector, EvaluationContext context) {
        if (!config.vectorsEnabled()) {
            throw new EvaluationException("Vectors are disabled in current configuration");
        }
        if (vector.size() > config.maxVectorSize()) {
            throw new EvaluationException("Vector size " + vector.size() +
                    " exceeds maximum allowed size of " + config.maxVectorSize());
        }

        var evaluated = new Node[vector.size()];
        for (int i = 0; i < evaluated.length; i++) {
            evaluated[i] = evaluateElement(vector.getElement(i), context);
        }
        return new NodeVector(evaluated);
    }

    private NodeConstant evaluateMatrixElements(NodeMatrix matrix, EvaluationContext context) {
        if (!config.matricesEnabled()) {
            throw new EvaluationException("Matrices are disabled in current configuration");
        }
        int rows = matrix.getRows();
        int cols = matrix.getCols();
        if (rows > config.maxMatrixDimension() || cols > config.maxMatrixDimension()) {
            throw new EvaluationException("Matrix dimensions " + rows + "x" + cols +
                    " exceed maximum allowed dimension of " + config.maxMatrixDimension());
        }

        var evaluated = new Node[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                evaluated[i][j] = evaluateElement(matrix.getElement(i, j), context);
            }
        }
        return new NodeMatrix(evaluated);
    }

    /**
     * Nested collections still need evaluating; other constants are already values.
     */
    private Node evaluateElement(Node element, EvaluationContext context) {
        boolean isValue = element instanceof NodeConstant
                && !(element instanceof NodeVector)
                && !(element instanceof NodeMatrix);
        return isValue ? element : evaluate(element, context);
    }

    // ==================== Operators ====================

    /**
     * The right operand stays lazy so short-circuiting operators can skip it.
     * <p>
     * The left spine is collected and folded back up rather than recursed down, so a long
     * {@code 1 + 1 + 1 + ...} costs heap rather than Java stack.
     */
    private NodeConstant evaluateBinary(NodeBinary node, EvaluationContext context) {
        var spine = new ArrayDeque<NodeBinary>();
        Node current = node;
        while (current instanceof NodeBinary binary) {
            spine.push(binary);
            current = binary.getLeft();
        }

        NodeConstant result = evaluate(current, context);
        while (!spine.isEmpty()) {
            NodeBinary step = spine.pop();
            Node right = step.getRight();
            result = operatorExecutor.executeBinary(
                    step.getOperator().type(),
                    result,
                    () -> evaluate(right, context),
                    context.operatorContext(functionCallHandler));
        }
        return result;
    }

    /**
     * Folds the operand chain iteratively, for the same reason as {@link #evaluateBinary}.
     */
    private NodeConstant evaluateUnary(NodeUnary node, EvaluationContext context) {
        var chain = new ArrayDeque<NodeUnary>();
        Node current = node;
        while (current instanceof NodeUnary unary) {
            chain.push(unary);
            current = unary.getOperand();
        }

        NodeConstant result = evaluate(current, context);
        while (!chain.isEmpty()) {
            result = operatorExecutor.executeUnary(
                    chain.pop().getOperator().type(), result, context.operatorContext(functionCallHandler));
        }
        return result;
    }

    // ==================== Statements ====================

    private NodeConstant evaluateAssignment(NodeAssignment node, EvaluationContext context) {
        if (!config.userDefinedVariablesEnabled()) {
            throw new EvaluationException("User-defined variables are disabled in current configuration");
        }
        NodeConstant value = evaluate(node.getValue(), context);
        context.assign(node.getIdentifier(), value);
        return value;
    }

    private NodeConstant evaluateSequence(NodeSequence node, EvaluationContext context) {
        NodeConstant result = null;
        for (Node statement : node.getStatements()) {
            result = evaluate(statement, context);
        }
        if (result == null) {
            throw new EvaluationException("Empty statement sequence has no value");
        }
        return result;
    }

    private NodeConstant evaluateRangeExpression(NodeRangeExpression node, EvaluationContext context) {
        NodeNumber start = rangeBound(node.getStart(), "start", context);
        NodeNumber end = rangeBound(node.getEnd(), "end", context);
        NodeNumber step = node.hasStep() ? rangeBound(node.getStep(), "step", context) : null;

        NodeRange range = new NodeRange(start, end, step);
        long estimatedSize = range.estimateSize();
        if (estimatedSize > config.maxVectorSize()) {
            throw new EvaluationException("Range would produce " + estimatedSize +
                    " elements, exceeding maximum allowed size of " + config.maxVectorSize());
        }
        return range.toVector();
    }

    private NodeNumber rangeBound(Node node, String description, EvaluationContext context) {
        NodeConstant value = evaluate(node, context);
        if (!TypeCoercion.isNumeric(value)) {
            throw new TypeError("Range " + description + " must be a number, got: " + value.typeName());
        }
        return TypeCoercion.toNumber(value);
    }

    // ==================== Unit conversion ====================

    /**
     * Converts a value to a target unit, broadcasting over vectors and matrices.
     */
    private NodeConstant evaluateUnitConversion(NodeUnitConversion node, EvaluationContext context) {
        if (!config.unitsEnabled()) {
            throw new EvaluationException("Unit conversions are disabled in current configuration");
        }

        String targetName = node.getTargetUnit();
        // A name that is not a unit is the same failure as '@name', so it reads the same way
        UnitDefinition target = context.resolveUnit(targetName)
                .orElseThrow(() -> UndefinedVariableException.unit(targetName));
        return convertTo(evaluate(node.getValue(), context), target);
    }

    private NodeConstant convertTo(NodeConstant value, UnitDefinition target) {
        return switch (value) {
            case NodeUnit unit -> unit.convertTo(target);
            case NodeVector vector -> {
                var converted = new Node[vector.size()];
                for (int i = 0; i < converted.length; i++) {
                    converted[i] = convertTo((NodeConstant) vector.getElement(i), target);
                }
                yield new NodeVector(converted);
            }
            case NodeMatrix matrix -> {
                var converted = new Node[matrix.getRows()][matrix.getCols()];
                for (int i = 0; i < matrix.getRows(); i++) {
                    for (int j = 0; j < matrix.getCols(); j++) {
                        converted[i][j] = convertTo((NodeConstant) matrix.getElement(i, j), target);
                    }
                }
                yield new NodeMatrix(converted);
            }
            default -> {
                if (!TypeCoercion.isNumeric(value)) {
                    throw new TypeError("Cannot apply unit conversion to: " + value.typeName());
                }
                yield NodeUnit.of(TypeCoercion.toNumber(value), target);
            }
        };
    }
}
