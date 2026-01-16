package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.handler.ComprehensionHandler;
import uk.co.ryanharrison.mathengine.parser.evaluator.handler.FunctionCallHandler;
import uk.co.ryanharrison.mathengine.parser.evaluator.handler.SubscriptHandler;
import uk.co.ryanharrison.mathengine.parser.evaluator.handler.VariableResolver;
import uk.co.ryanharrison.mathengine.parser.function.FunctionExecutor;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorExecutor;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

/**
 * Evaluates an Abstract Syntax Tree (AST) to produce a result.
 * <p>
 * The evaluator coordinates evaluation by delegating to specialized handlers:
 * <ul>
 *     <li>{@link VariableResolver} - variable resolution and implicit multiplication</li>
 *     <li>{@link SubscriptHandler} - vector and matrix indexing/slicing</li>
 *     <li>{@link FunctionCallHandler} - function calls (built-in, user-defined, lambda)</li>
 *     <li>{@link ComprehensionHandler} - list comprehension evaluation</li>
 * </ul>
 * <p>
 * Operators are executed via the {@link OperatorExecutor} and built-in functions
 * via the {@link FunctionExecutor}.
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Exact rational arithmetic by default (configurable)</li>
 *     <li>Type promotion (Rational → Double when necessary)</li>
 *     <li>Boolean and comparison operations</li>
 *     <li>Variable storage and retrieval</li>
 *     <li>User-defined functions with dynamic scoping</li>
 *     <li>Lambda expressions with lexical scoping</li>
 *     <li>Short-circuit evaluation for logical operators</li>
 *     <li>Lazy evaluation for conditional (if) function</li>
 *     <li>Feature toggles via configuration</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Preferred: Use MathEngine which handles all setup
 * MathEngine engine = MathEngine.create();
 * NodeConstant result = engine.evaluate("2 + 3 * 4");
 * }</pre>
 *
 * @see MathEngineConfig
 * @see uk.co.ryanharrison.mathengine.parser.MathEngine
 */
public final class Evaluator {

    private final MathEngineConfig config;
    private EvaluationContext context;
    private final OperatorExecutor operatorExecutor;

    // Handlers for different evaluation concerns
    private final VariableResolver variableResolver;
    private final SubscriptHandler subscriptHandler;
    private final FunctionCallHandler functionCallHandler;
    private final ComprehensionHandler comprehensionHandler;

    /**
     * Creates a new evaluator with the given configuration, context, and executors.
     * <p>
     * This is the preferred constructor. The executors should be pre-configured
     * with all required operators and functions.
     *
     * @param config           the engine configuration
     * @param context          the evaluation context for variables and settings
     * @param operatorExecutor the executor for operators (pre-configured)
     * @param functionExecutor the executor for built-in functions (pre-configured)
     */
    public Evaluator(MathEngineConfig config, EvaluationContext context,
                     OperatorExecutor operatorExecutor, FunctionExecutor functionExecutor) {
        this.config = config;
        this.context = context;
        this.operatorExecutor = operatorExecutor;

        // Initialize handlers with callbacks to this evaluator
        this.variableResolver = new VariableResolver(config);
        this.subscriptHandler = new SubscriptHandler(config, this::evaluate);
        this.functionCallHandler = new FunctionCallHandler(
                config,
                functionExecutor,
                this::evaluate,
                this::pushContext,
                this::popContext
        );
        this.comprehensionHandler = new ComprehensionHandler(
                config,
                this::evaluate,
                this::pushContext,
                this::popContext
        );
    }

    /**
     * Gets the current evaluation context.
     *
     * @return the evaluation context
     */
    public EvaluationContext getContext() {
        return context;
    }

    // ==================== Context Management ====================

    /**
     * Temporarily set a new context for evaluation (used for function calls).
     * Returns the previous context so it can be restored.
     */
    private EvaluationContext pushContext(EvaluationContext newContext) {
        EvaluationContext old = this.context;
        this.context = newContext;
        return old;
    }

    /**
     * Restore the previous context.
     */
    private void popContext(EvaluationContext newContext, EvaluationContext oldContext) {
        this.context = oldContext;
    }

    // ==================== Main Evaluation Entry Point ====================

    /**
     * Evaluates a node and returns its constant value.
     *
     * @param node the AST node to evaluate
     * @return the evaluated constant value
     * @throws EvaluationException if evaluation fails
     */
    public NodeConstant evaluate(Node node) {
        // Handle specific NodeConstant subclasses that need special handling FIRST
        // because they extend NodeConstant but need conversion/evaluation

        // NodeLambda extends NodeConstant but needs to be converted to NodeFunction
        if (node instanceof NodeLambda lambda) {
            return functionCallHandler.evaluateLambda(lambda, context);
        }

        // NodeRange extends NodeConstant but needs to be converted to NodeVector
        if (node instanceof NodeRange range) {
            return range.toVector();
        }

        // NodeVector needs its elements evaluated (they might be unevaluated expressions)
        if (node instanceof NodeVector vector) {
            return evaluateVectorElements(vector);
        }

        // NodeMatrix needs its elements evaluated (they might be unevaluated expressions)
        if (node instanceof NodeMatrix matrix) {
            if (!config.matricesEnabled()) {
                throw new EvaluationException("Matrices are disabled in current configuration");
            }
            return evaluateMatrixElements(matrix);
        }

        // Other NodeConstant subclasses can be returned directly
        if (node instanceof NodeConstant constant) {
            return constant;
        }

        if (node instanceof NodeVariable variable) {
            return variableResolver.resolve(variable, context, uk.co.ryanharrison.mathengine.parser.evaluator.ResolutionContext.GENERAL);
        }

        // Explicit unit reference (@unit)
        if (node instanceof NodeUnitRef unitRef) {
            return variableResolver.resolveUnitRef(unitRef.getUnitName(), context);
        }

        // Explicit variable reference ($var)
        if (node instanceof NodeVarRef varRef) {
            return variableResolver.resolveVarRef(varRef.getVarName(), context);
        }

        // Explicit constant reference (#const)
        if (node instanceof NodeConstRef constRef) {
            return variableResolver.resolveConstRef(constRef.getConstName(), context);
        }

        if (node instanceof NodeBinary binary) {
            return evaluateBinary(binary);
        }

        if (node instanceof NodeUnary unary) {
            return evaluateUnary(unary);
        }

        if (node instanceof NodeAssignment assignment) {
            return evaluateAssignment(assignment);
        }

        if (node instanceof NodeSubscript subscript) {
            return subscriptHandler.evaluate(subscript);
        }

        if (node instanceof NodeRangeExpression rangeExpr) {
            return evaluateRangeExpression(rangeExpr);
        }

        if (node instanceof NodeFunctionDef funcDef) {
            return functionCallHandler.evaluateFunctionDef(funcDef, context);
        }

        if (node instanceof NodeCall call) {
            return functionCallHandler.evaluate(call, context);
        }

        if (node instanceof NodeSequence sequence) {
            return evaluateSequence(sequence);
        }

        if (node instanceof NodeComprehension comprehension) {
            return comprehensionHandler.evaluate(comprehension, context);
        }

        if (node instanceof NodeUnitConversion unitConversion) {
            return evaluateUnitConversion(unitConversion);
        }

        throw new EvaluationException("Cannot evaluate node type: " + node.getClass().getSimpleName());
    }

    // ==================== Vector Evaluation ====================

    /**
     * Evaluates a vector by evaluating all its elements.
     */
    private NodeConstant evaluateVectorElements(NodeVector vector) {
        if (!config.vectorsEnabled()) {
            throw new EvaluationException("Vectors are disabled in current configuration");
        }

        Node[] elements = vector.getElements();

        // Validate vector size against configuration limit
        if (elements.length > config.maxVectorSize()) {
            throw new EvaluationException("Vector size " + elements.length +
                    " exceeds maximum allowed size of " + config.maxVectorSize());
        }

        Node[] evaluated = new Node[elements.length];

        for (int i = 0; i < elements.length; i++) {
            Node element = elements[i];
            if (element instanceof NodeConstant && !(element instanceof NodeVector)) {
                evaluated[i] = element;
            } else {
                evaluated[i] = evaluate(element);
            }
        }

        return new NodeVector(evaluated);
    }

    /**
     * Evaluates a matrix by evaluating all its elements.
     */
    private NodeConstant evaluateMatrixElements(NodeMatrix matrix) {
        Node[][] elements = matrix.getElements();

        // Validate matrix dimensions against configuration limits
        int rows = elements.length;
        int cols = rows > 0 ? elements[0].length : 0;
        if (rows > config.maxMatrixDimension()) {
            throw new EvaluationException("Matrix row count " + rows +
                    " exceeds maximum allowed dimension of " + config.maxMatrixDimension());
        }
        if (cols > config.maxMatrixDimension()) {
            throw new EvaluationException("Matrix column count " + cols +
                    " exceeds maximum allowed dimension of " + config.maxMatrixDimension());
        }

        Node[][] evaluated = new Node[elements.length][];

        for (int i = 0; i < elements.length; i++) {
            evaluated[i] = new Node[elements[i].length];
            for (int j = 0; j < elements[i].length; j++) {
                Node element = elements[i][j];
                if (element instanceof NodeConstant && !(element instanceof NodeVector) && !(element instanceof NodeMatrix)) {
                    evaluated[i][j] = element;
                } else {
                    evaluated[i][j] = evaluate(element);
                }
            }
        }

        return new NodeMatrix(evaluated);
    }

    // ==================== Binary Operations ====================

    /**
     * Evaluates a binary operation.
     * Implements short-circuit evaluation for logical operators (&&, ||).
     */
    private NodeConstant evaluateBinary(NodeBinary node) {
        TokenType opType = node.getOperator().getType();

        // Short-circuit evaluation for logical operators
        if (opType == TokenType.AND || opType == TokenType.OR) {
            return evaluateWithShortCircuit(node, opType);
        }

        // Eager evaluation for all other operators
        NodeConstant left = evaluate(node.getLeft());
        NodeConstant right = evaluate(node.getRight());

        // Special case: Vector @ Function (map operation) needs evaluator access
        if (opType == TokenType.AT && left instanceof NodeVector vector && right instanceof NodeFunction func) {
            return evaluateVectorMap(vector, func);
        }

        return operatorExecutor.executeBinary(opType, left, right, context);
    }

    /**
     * Evaluates vector map operation: Vector @ Function.
     * Applies the function to each element of the vector.
     */
    private NodeConstant evaluateVectorMap(NodeVector vector, NodeFunction func) {
        Node[] elements = vector.getElements();
        Node[] results = new Node[elements.length];

        for (int i = 0; i < elements.length; i++) {
            NodeConstant element = evaluate(elements[i]);
            // Call function with single argument
            results[i] = functionCallHandler.evaluate(
                    new NodeCall(func, java.util.List.of(element)),
                    context
            );
        }

        return new NodeVector(results);
    }

    /**
     * Evaluates a binary operation with short-circuit support.
     */
    private NodeConstant evaluateWithShortCircuit(NodeBinary node, TokenType opType) {
        NodeConstant left = evaluate(node.getLeft());

        return operatorExecutor.executeBinaryShortCircuit(
                opType,
                left,
                () -> evaluate(node.getRight()),
                context
        );
    }

    // ==================== Unary Operations ====================

    /**
     * Evaluates a unary operation.
     */
    private NodeConstant evaluateUnary(NodeUnary node) {
        TokenType opType = node.getOperator().getType();
        NodeConstant operand = evaluate(node.getOperand());
        return operatorExecutor.executeUnary(opType, operand, context);
    }

    // ==================== Assignment ====================

    /**
     * Evaluates an assignment.
     */
    private NodeConstant evaluateAssignment(NodeAssignment node) {
        NodeConstant value = evaluate(node.getValue());
        context.assign(node.getIdentifier(), value);
        return value;
    }

    // ==================== Range Expression ====================

    /**
     * Evaluates a range expression by evaluating its components and creating a NodeRange.
     */
    private NodeConstant evaluateRangeExpression(NodeRangeExpression node) {
        NodeConstant startVal = evaluate(node.getStart());
        NodeConstant endVal = evaluate(node.getEnd());
        NodeConstant stepVal = node.hasStep() ? evaluate(node.getStep()) : null;

        if (!TypeCoercion.isNumeric(startVal)) {
            throw new TypeError("Range start must be a number, got " + TypeCoercion.typeName(startVal));
        }
        if (!TypeCoercion.isNumeric(endVal)) {
            throw new TypeError("Range end must be a number, got " + TypeCoercion.typeName(endVal));
        }
        if (stepVal != null && !TypeCoercion.isNumeric(stepVal)) {
            throw new TypeError("Range step must be a number, got " + TypeCoercion.typeName(stepVal));
        }

        NodeRange range = new NodeRange(
                TypeCoercion.toNumber(startVal),
                TypeCoercion.toNumber(endVal),
                stepVal != null ? TypeCoercion.toNumber(stepVal) : null
        );

        // Validate estimated range size before expanding
        long estimatedSize = range.estimateSize();
        if (estimatedSize > config.maxVectorSize()) {
            throw new EvaluationException("Range would produce " + estimatedSize +
                    " elements, exceeding maximum allowed size of " + config.maxVectorSize());
        }

        return range.toVector();
    }

    // ==================== Unit Conversion Evaluation ====================

    /**
     * Evaluates a unit conversion expression.
     * <p>
     * Examples: 100 meters in feet, 25 celsius to fahrenheit
     * </p>
     * <p>
     * The value can be a number, vector, or matrix. Broadcasting applies:
     * {100, 200} meters in feet → {328.084, 656.168} feet
     * </p>
     */
    private NodeConstant evaluateUnitConversion(NodeUnitConversion node) {
        if (!config.unitsEnabled()) {
            throw new EvaluationException("Unit conversions are disabled in current configuration");
        }

        NodeConstant value = evaluate(node.getValue());
        String targetUnitName = node.getTargetUnit();

        // Get the target unit from the registry
        var unitRegistry = context.getUnitRegistry();
        if (!unitRegistry.isUnit(targetUnitName)) {
            throw new TypeError("Unknown target unit: " + targetUnitName);
        }
        var targetUnit = unitRegistry.get(targetUnitName);

        // If value is already a NodeUnit, perform conversion
        if (value instanceof NodeUnit unitValue) {
            return unitValue.convertTo(targetUnit);
        }

        // If value is a plain number, create a NodeUnit with the target unit
        if (TypeCoercion.isNumeric(value)) {
            return NodeUnit.of(value.doubleValue(), targetUnit);
        }

        // If value is a vector, broadcast unit conversion over elements
        if (value instanceof NodeVector vector) {
            Node[] elements = vector.getElements();
            Node[] converted = new Node[elements.length];
            for (int i = 0; i < elements.length; i++) {
                NodeUnitConversion elemConversion = new NodeUnitConversion(elements[i], targetUnitName);
                converted[i] = evaluateUnitConversion(elemConversion);
            }
            return new NodeVector(converted);
        }

        // If value is a matrix, broadcast unit conversion over elements
        if (value instanceof NodeMatrix matrix) {
            Node[][] rows = matrix.getElements();
            Node[][] converted = new Node[rows.length][];
            for (int i = 0; i < rows.length; i++) {
                converted[i] = new Node[rows[i].length];
                for (int j = 0; j < rows[i].length; j++) {
                    NodeUnitConversion elemConversion = new NodeUnitConversion(rows[i][j], targetUnitName);
                    converted[i][j] = evaluateUnitConversion(elemConversion);
                }
            }
            return new NodeMatrix(converted);
        }

        throw new TypeError("Cannot apply unit conversion to " + TypeCoercion.typeName(value));
    }

    // ==================== Sequence Evaluation ====================

    /**
     * Evaluates a sequence of statements, returning the last result.
     */
    private NodeConstant evaluateSequence(NodeSequence node) {
        NodeConstant result = null;

        for (Node statement : node.getStatements()) {
            result = evaluate(statement);
        }

        return result;
    }
}
