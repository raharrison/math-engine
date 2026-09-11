package uk.co.ryanharrison.mathengine.parser.operator;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeFunction;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.util.FunctionCaller;

import java.util.List;

/**
 * What an operator can reach beyond its operands: the current scope, and the
 * ability to call a user function.
 * <p>
 * Arithmetic needs none of this. Operators delegate to {@link NodeConstant#add}
 * and friends, which carry all of the type rules.
 */
public final class OperatorContext {

    private final EvaluationContext evaluationContext;
    private final FunctionCaller functionCaller;

    public OperatorContext(EvaluationContext evaluationContext, FunctionCaller functionCaller) {
        this.evaluationContext = evaluationContext;
        this.functionCaller = functionCaller;
    }

    /**
     * Calls a user-defined function or lambda, as {@code @} does when mapping over a vector.
     */
    public NodeConstant callFunction(NodeFunction function, List<NodeConstant> args) {
        if (functionCaller == null) {
            throw new IllegalStateException("This operator context cannot call functions");
        }
        return functionCaller.call(function, args, evaluationContext);
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }

    public AngleUnit getAngleUnit() {
        return evaluationContext.getAngleUnit();
    }
}
