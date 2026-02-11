package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.*;
import uk.co.ryanharrison.mathengine.parser.function.FunctionExecutor;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.binary.MultiplyOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Handles function call evaluation including built-in functions, user-defined functions,
 * lambdas, and implicit multiplication fallback.
 * <p>
 * Resolution order for function calls:
 * <ol>
 *     <li>Special built-in functions with lazy evaluation (if)</li>
 *     <li>User-defined functions (takes priority over built-ins)</li>
 *     <li>Built-in functions from FunctionExecutor</li>
 *     <li>Variable lookup (if variable holds a function)</li>
 *     <li>Implicit multiplication fallback (if enabled)</li>
 * </ol>
 *
 * <h2>Lazy Evaluation:</h2>
 * The 'if' function uses lazy evaluation - only the selected branch is evaluated.
 * This prevents errors from evaluating unused branches with side effects.
 *
 * <h2>Lexical vs Dynamic Scoping:</h2>
 * <ul>
 *     <li>Lambda expressions use lexical scoping (capture context at definition)</li>
 *     <li>Regular functions use dynamic scoping (use context at call time)</li>
 * </ul>
 */
public final class FunctionCallHandler implements FunctionCaller {

    private final MathEngineConfig config;
    private final FunctionExecutor functionExecutor;
    private final Function<Node, NodeConstant> evaluator;
    private final Function<EvaluationContext, EvaluationContext> contextPusher;
    private final BiConsumer<EvaluationContext, EvaluationContext> contextPopper;

    /**
     * Creates a new function call handler.
     *
     * @param config           the engine configuration
     * @param functionExecutor the function executor for built-in functions
     * @param evaluator        function to evaluate nodes
     * @param contextPusher    function to push a new context and return the old one
     * @param contextPopper    consumer to pop/restore context (new, old)
     */
    public FunctionCallHandler(
            MathEngineConfig config,
            FunctionExecutor functionExecutor,
            Function<Node, NodeConstant> evaluator,
            Function<EvaluationContext, EvaluationContext> contextPusher,
            BiConsumer<EvaluationContext, EvaluationContext> contextPopper) {
        this.config = config;
        this.functionExecutor = functionExecutor;
        this.evaluator = evaluator;
        this.contextPusher = contextPusher;
        this.contextPopper = contextPopper;
    }

    // ==================== FunctionCaller Implementation ====================

    @Override
    public NodeConstant call(NodeFunction function, List<NodeConstant> args, EvaluationContext context) {
        var nodeArgs = new ArrayList<Node>(args);
        return evaluate(new NodeCall(function, nodeArgs), context);
    }

    /**
     * Evaluates a function call node.
     *
     * @param call    the function call node
     * @param context the evaluation context
     * @return the result of the function call
     * @throws TypeError                  if the callee is not callable
     * @throws UndefinedVariableException if the function is not found
     */
    public NodeConstant evaluate(NodeCall call, EvaluationContext context) {
        Node funcExpr = call.getFunction();

        return switch (funcExpr) {
            // Handle named function calls
            case NodeVariable variable -> evaluateNamedCall(variable.getName(), call.getArguments(), context);

            // Handle inline lambda calls: (x -> x*2)(5)
            case NodeLambda lambda -> {
                NodeFunction funcValue = evaluateLambda(lambda, context);
                yield callUserFunction(funcValue.getFunction(), call.getArguments(), context);
            }

            // Handle already-evaluated function values
            case NodeFunction func -> callUserFunction(func.getFunction(), call.getArguments(), context);

            // Evaluate the expression and try to call it
            default -> {
                NodeConstant funcValue = evaluator.apply(funcExpr);
                if (funcValue instanceof NodeFunction func) {
                    yield callUserFunction(func.getFunction(), call.getArguments(), context);
                }
                // Fall back to implicit multiplication if enabled
                yield tryImplicitMultiplication(funcValue, call.getArguments(), null, context);
            }
        };
    }

    /**
     * Evaluates a named function call (function referenced by name).
     */
    private NodeConstant evaluateNamedCall(String name, List<Node> arguments, EvaluationContext context) {
        // Special case: 'if' function needs lazy evaluation
        if ("if".equals(name)) {
            return evaluateIfLazy(arguments);
        }

        // Check for user-defined function FIRST (takes priority over built-ins)
        var userFuncOpt = context.resolveFunction(name);
        if (userFuncOpt.isPresent()) {
            if (!config.userDefinedFunctionsEnabled()) {
                throw new EvaluationException("User-defined functions are disabled in current configuration");
            }
            return callUserFunction(userFuncOpt.get(), arguments, context);
        }

        // Check for built-in function
        if (functionExecutor.hasFunction(name)) {
            return callBuiltinFunction(name, arguments, context);
        }

        // Check if there's a value stored as a variable that might be a function
        var varOpt = context.resolve(name);
        if (varOpt.isPresent()) {
            NodeConstant varValue = varOpt.get();
            if (varValue instanceof NodeFunction func) {
                return callUserFunction(func.getFunction(), arguments, context);
            }
            return tryImplicitMultiplication(varValue, arguments, name, context);
        }

        // Try to split the function name into variable + function (implicit multiplication)
        // e.g., "xsqrt(4)" -> x * sqrt(4)
        if (config.implicitMultiplication()) {
            NodeConstant splitResult = trySplitFunctionCall(name, arguments, context);
            if (splitResult != null) {
                return splitResult;
            }
        }

        throw new UndefinedVariableException("Function not found: " + name);
    }

    /**
     * Tries to split a function name into a variable prefix and a known function.
     * For example, "xsqrt" becomes "x * sqrt".
     *
     * @param name      the function name to split
     * @param arguments the function arguments
     * @param context   the evaluation context
     * @return the result of variable * function(arguments), or null if not possible
     */
    private NodeConstant trySplitFunctionCall(String name, List<Node> arguments, EvaluationContext context) {
        // Try splitting at each position
        for (int i = 1; i < name.length(); i++) {
            String varPart = name.substring(0, i);
            String funcPart = name.substring(i);

            // Check if varPart is a defined variable and funcPart is a known function
            var varPartOpt = context.resolve(varPart);
            if (varPartOpt.isPresent() && functionExecutor.hasFunction(funcPart)) {
                NodeConstant varValue = varPartOpt.get();
                NodeConstant funcResult = callBuiltinFunction(funcPart, arguments, context);

                if (TypeCoercion.isNumericOrCollection(varValue) && TypeCoercion.isNumericOrCollection(funcResult)) {
                    OperatorContext opCtx = new OperatorContext(context, this);
                    return MultiplyOperator.INSTANCE.apply(varValue, funcResult, opCtx);
                }
            }
        }

        return null;
    }

    /**
     * Evaluates a lambda expression and wraps it as a first-class function value.
     * Lambdas use lexical scoping - they capture a snapshot of the current context.
     *
     * @param lambda  the lambda node
     * @param context the current evaluation context
     * @return the function wrapper
     */
    public NodeFunction evaluateLambda(NodeLambda lambda, EvaluationContext context) {
        if (!config.lambdasEnabled()) {
            throw new EvaluationException("Lambda expressions are disabled in current configuration");
        }

        var functionDef = new FunctionDefinition(
                "<lambda>",
                lambda.getParameters(),
                lambda.getBody(),
                context.snapshot()  // Capture snapshot for lexical scoping
        );
        return new NodeFunction(functionDef);
    }

    /**
     * Evaluates a function definition and stores it in the context.
     *
     * @param node    the function definition node
     * @param context the evaluation context
     * @return the function wrapper
     */
    public NodeConstant evaluateFunctionDef(NodeFunctionDef node, EvaluationContext context) {
        if (!config.userDefinedFunctionsEnabled()) {
            throw new EvaluationException("User-defined functions are disabled in current configuration");
        }

        var function = new FunctionDefinition(
                node.getName(),
                node.getParameters(),
                node.getBody(),
                null  // No closure - dynamic scoping for regular functions
        );

        context.defineFunction(node.getName(), function);
        return new NodeFunction(function);
    }

    /**
     * Calls a user-defined function with the given arguments.
     */
    private NodeConstant callUserFunction(FunctionDefinition function, List<Node> argumentNodes, EvaluationContext context) {
        if (argumentNodes.size() != function.getArity()) {
            throw new ArityException("Function '" + function.name() + "' expects " +
                    function.getArity() + " argument(s), got " + argumentNodes.size());
        }

        // Evaluate arguments eagerly
        var bindings = new HashMap<String, NodeConstant>();
        List<String> params = function.parameters();
        for (int i = 0; i < params.size(); i++) {
            NodeConstant argValue = evaluator.apply(argumentNodes.get(i));
            bindings.put(params.get(i), argValue);
        }

        // Determine parent context based on scoping rules
        EvaluationContext parentContext = function.hasLexicalScope()
                ? function.closure()
                : context;

        // Create child context with parameter bindings
        EvaluationContext childContext = parentContext.withBindings(bindings);
        childContext.enterFunction(function.name());

        try {
            EvaluationContext oldContext = contextPusher.apply(childContext);
            try {
                return evaluator.apply(function.body());
            } finally {
                contextPopper.accept(childContext, oldContext);
            }
        } finally {
            childContext.exitFunction();
        }
    }

    /**
     * Calls a built-in function with the given arguments.
     */
    private NodeConstant callBuiltinFunction(String name, List<Node> argumentNodes, EvaluationContext context) {
        var arguments = new ArrayList<NodeConstant>();
        for (Node argNode : argumentNodes) {
            arguments.add(evaluator.apply(argNode));
        }
        return functionExecutor.execute(name, arguments, context, this);
    }

    /**
     * Evaluates the 'if' function with lazy evaluation.
     * Only the selected branch is evaluated.
     */
    private NodeConstant evaluateIfLazy(List<Node> argumentNodes) {
        if (argumentNodes.size() != 3) {
            throw new ArityException("Function 'if' expects 3 arguments, got " + argumentNodes.size());
        }

        NodeConstant conditionResult = evaluator.apply(argumentNodes.get(0));
        boolean condition = TypeCoercion.toBoolean(conditionResult);

        return condition
                ? evaluator.apply(argumentNodes.get(1))
                : evaluator.apply(argumentNodes.get(2));
    }

    /**
     * Tries to interpret a "call" as implicit multiplication.
     * <p>
     * For example, 2(3) is interpreted as 2 * 3 if implicit multiplication is enabled.
     *
     * @param calleeValue the value being "called"
     * @param args        the arguments
     * @param calleeName  the name of the callee (for error messages)
     * @param context     the evaluation context
     * @return the result of implicit multiplication
     * @throws TypeError if implicit multiplication is not applicable
     */
    private NodeConstant tryImplicitMultiplication(NodeConstant calleeValue, List<Node> args, String calleeName, EvaluationContext context) {
        if (config.implicitMultiplication() && args.size() == 1) {
            NodeConstant argValue = evaluator.apply(args.getFirst());

            if (TypeCoercion.isNumericOrCollection(calleeValue) && TypeCoercion.isNumericOrCollection(argValue)) {
                var opCtx = new OperatorContext(context, this);
                return MultiplyOperator.INSTANCE.apply(calleeValue, argValue, opCtx);
            }
        }

        String calleeDesc = (calleeName != null) ? "'" + calleeName + "'" : TypeCoercion.typeName(calleeValue);
        throw new TypeError("Cannot call " + calleeDesc + " (value is not a function)");
    }
}
