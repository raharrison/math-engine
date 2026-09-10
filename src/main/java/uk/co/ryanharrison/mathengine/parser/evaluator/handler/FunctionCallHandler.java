package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.*;
import uk.co.ryanharrison.mathengine.parser.function.FunctionExecutor;
import uk.co.ryanharrison.mathengine.parser.function.LazyFunction;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.operator.binary.MultiplyOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;
import uk.co.ryanharrison.mathengine.parser.util.TypeCoercion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Evaluates calls to built-in functions, user-defined functions and lambdas.
 *
 * <h2>Resolution order</h2>
 * <ol>
 *     <li>User-defined functions, so users can shadow built-ins</li>
 *     <li>Built-in functions</li>
 *     <li>A variable holding a function value</li>
 *     <li>Implicit multiplication, if enabled</li>
 * </ol>
 *
 * <h2>Scoping</h2>
 * Lambdas capture their defining scope at creation (lexical); named functions
 * resolve free variables at call time (dynamic).
 *
 * <h2>Lazy functions</h2>
 * A {@link LazyFunction} receives its arguments unevaluated plus an evaluator, so it
 * can choose what to evaluate. This is how {@code if} skips the untaken branch.
 */
public final class FunctionCallHandler implements FunctionCaller {

    private final MathEngineConfig config;
    private final FunctionExecutor functionExecutor;
    private final NodeEvaluator evaluator;

    public FunctionCallHandler(MathEngineConfig config, FunctionExecutor functionExecutor, NodeEvaluator evaluator) {
        this.config = config;
        this.functionExecutor = functionExecutor;
        this.evaluator = evaluator;
    }

    @Override
    public NodeConstant call(NodeFunction function, List<NodeConstant> args, EvaluationContext context) {
        return callUserFunction(function.getFunction(), new ArrayList<>(args), context);
    }

    /**
     * Evaluates a function call node.
     *
     * @throws TypeError                  if the callee is not callable
     * @throws UndefinedVariableException if the function is not found
     */
    public NodeConstant evaluate(NodeCall call, EvaluationContext context) {
        return switch (call.getFunction()) {
            case NodeVariable variable -> evaluateNamedCall(variable.getName(), call.getArguments(), context);

            // Inline lambda call: (x -> x*2)(5)
            case NodeLambda lambda -> callUserFunction(
                    evaluateLambda(lambda, context).getFunction(), call.getArguments(), context);

            case NodeFunction func -> callUserFunction(func.getFunction(), call.getArguments(), context);

            default -> {
                NodeConstant funcValue = evaluator.evaluate(call.getFunction(), context);
                yield funcValue instanceof NodeFunction func
                        ? callUserFunction(func.getFunction(), call.getArguments(), context)
                        : tryImplicitMultiplication(funcValue, call.getArguments(), null, context);
            }
        };
    }

    private NodeConstant evaluateNamedCall(String name, List<Node> arguments, EvaluationContext context) {
        var userFunc = context.resolveFunction(name);
        if (userFunc.isPresent()) {
            if (!config.userDefinedFunctionsEnabled()) {
                throw new EvaluationException("User-defined functions are disabled in current configuration");
            }
            return callUserFunction(userFunc.get(), arguments, context);
        }

        if (functionExecutor.hasFunction(name)) {
            return callBuiltinFunction(name, arguments, context);
        }

        var variable = context.resolve(name);
        if (variable.isPresent()) {
            NodeConstant value = variable.get();
            return value instanceof NodeFunction func
                    ? callUserFunction(func.getFunction(), arguments, context)
                    : tryImplicitMultiplication(value, arguments, name, context);
        }

        // "xsqrt(4)" may mean x * sqrt(4)
        if (config.implicitMultiplication()) {
            NodeConstant split = trySplitFunctionCall(name, arguments, context);
            if (split != null) {
                return split;
            }
        }

        throw UndefinedVariableException.function(name);
    }

    /**
     * Splits a call like {@code xsqrt(4)} into a variable prefix times a known function.
     */
    private NodeConstant trySplitFunctionCall(String name, List<Node> arguments, EvaluationContext context) {
        for (int i = 1; i < name.length(); i++) {
            String varPart = name.substring(0, i);
            String funcPart = name.substring(i);

            var varValue = context.resolve(varPart);
            if (varValue.isPresent() && functionExecutor.hasFunction(funcPart)) {
                NodeConstant funcResult = callBuiltinFunction(funcPart, arguments, context);
                if (TypeCoercion.isNumericOrCollection(varValue.get()) && TypeCoercion.isNumericOrCollection(funcResult)) {
                    return MultiplyOperator.INSTANCE.apply(
                            varValue.get(), funcResult, context.operatorContext(this));
                }
            }
        }
        return null;
    }

    /** Wraps a lambda as a value, capturing its defining scope for lexical scoping. */
    public NodeFunction evaluateLambda(NodeLambda lambda, EvaluationContext context) {
        if (!config.lambdasEnabled()) {
            throw new EvaluationException("Lambda expressions are disabled in current configuration");
        }
        return new NodeFunction(new FunctionDefinition(
                "<lambda>", lambda.getParameters(), lambda.getBody(), context.snapshot()));
    }

    /** Stores a named function definition in the current scope. */
    public NodeConstant evaluateFunctionDef(NodeFunctionDef node, EvaluationContext context) {
        if (!config.userDefinedFunctionsEnabled()) {
            throw new EvaluationException("User-defined functions are disabled in current configuration");
        }
        var function = new FunctionDefinition(node.getName(), node.getParameters(), node.getBody(), null);
        context.defineFunction(node.getName(), function);
        return new NodeFunction(function);
    }

    private NodeConstant callUserFunction(FunctionDefinition function, List<Node> argumentNodes, EvaluationContext context) {
        if (argumentNodes.size() != function.getArity()) {
            throw new ArityException(function.name(), function.getArity(), argumentNodes.size());
        }

        var bindings = new HashMap<String, NodeConstant>();
        List<String> params = function.parameters();
        for (int i = 0; i < params.size(); i++) {
            bindings.put(params.get(i), evaluator.evaluate(argumentNodes.get(i), context));
        }

        EvaluationContext parent = function.hasLexicalScope() ? function.closure() : context;
        EvaluationContext callScope = parent.withBindings(bindings);

        callScope.enterFunction(function.name());
        try {
            return evaluator.evaluate(function.body(), callScope);
        } finally {
            callScope.exitFunction();
        }
    }

    private NodeConstant callBuiltinFunction(String name, List<Node> argumentNodes, EvaluationContext context) {
        MathFunction function = functionExecutor.lookup(name);

        if (function instanceof LazyFunction lazy) {
            return functionExecutor.executeLazy(lazy, argumentNodes, context, this,
                    node -> evaluator.evaluate(node, context));
        }

        var arguments = new ArrayList<NodeConstant>(argumentNodes.size());
        for (Node argNode : argumentNodes) {
            arguments.add(evaluator.evaluate(argNode, context));
        }
        return functionExecutor.execute(function, arguments, context, this);
    }

    /**
     * Interprets {@code 2(3)} as {@code 2 * 3} when implicit multiplication is enabled.
     */
    private NodeConstant tryImplicitMultiplication(NodeConstant callee, List<Node> args, String calleeName,
                                                   EvaluationContext context) {
        if (config.implicitMultiplication() && args.size() == 1) {
            NodeConstant argValue = evaluator.evaluate(args.getFirst(), context);
            if (TypeCoercion.isNumericOrCollection(callee) && TypeCoercion.isNumericOrCollection(argValue)) {
                return MultiplyOperator.INSTANCE.apply(callee, argValue, context.operatorContext(this));
            }
        }

        String description = calleeName != null ? "'" + calleeName + "'" : callee.typeName();
        throw new TypeError("Cannot call " + description + " (value is not a function)");
    }
}
