package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;
import uk.co.ryanharrison.mathengine.parser.evaluator.UndefinedVariableException;
import uk.co.ryanharrison.mathengine.parser.operator.BroadcastingDispatcher;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeUnit;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVariable;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles variable resolution including implicit multiplication.
 * <p>
 * When a variable is not found, this handler can attempt to split
 * the identifier into multiple known variables and interpret as
 * implicit multiplication (e.g., "xy" -> x * y).
 *
 * <h2>Resolution Order:</h2>
 * <ol>
 *     <li>Try to resolve as a defined variable (highest priority - user override)</li>
 *     <li>Try to resolve as a user-defined function (first-class value)</li>
 *     <li>Try to resolve as a unit (creates NodeUnit with value 1.0)</li>
 *     <li>If implicit multiplication is enabled, try to split and multiply</li>
 *     <li>Throw {@link UndefinedVariableException} if nothing found</li>
 * </ol>
 */
public final class VariableResolver {

    private final MathEngineConfig config;

    /**
     * Creates a new variable resolver with the given configuration.
     *
     * @param config the engine configuration
     */
    public VariableResolver(MathEngineConfig config) {
        this.config = config;
    }

    /**
     * Resolves a variable to its value.
     *
     * @param node    the variable node to resolve
     * @param context the evaluation context
     * @return the resolved value
     * @throws UndefinedVariableException if the variable cannot be resolved
     */
    public NodeConstant resolve(NodeVariable node, EvaluationContext context) {
        String name = node.getName();

        // First try to resolve as a variable (highest priority - allows user override)
        if (context.isDefined(name)) {
            return context.resolve(name);
        }

        // Then try to resolve as a user-defined function (for first-class function support)
        FunctionDefinition func = context.resolveFunction(name);
        if (func != null) {
            return new NodeFunction(func);
        }

        // Check if it's a unit (creates a unit literal with value 1.0)
        UnitRegistry unitRegistry = context.getUnitRegistry();
        if (unitRegistry != null && unitRegistry.isUnit(name)) {
            return NodeUnit.of(1.0, unitRegistry.get(name));
        }

        // If implicit multiplication is enabled, try to split into multiple variables
        if (config.implicitMultiplication()) {
            NodeConstant splitResult = trySplitIntoVariables(name, context);
            if (splitResult != null) {
                return splitResult;
            }
        }

        throw new UndefinedVariableException(name);
    }

    /**
     * Tries to split an identifier into multiple defined variables and multiply them.
     * <p>
     * For example, "xy" would be split into "x" and "y" and multiplied if both are defined.
     *
     * @param name    the identifier to split
     * @param context the evaluation context
     * @return the result of multiplying the split variables, or null if not possible
     */
    private NodeConstant trySplitIntoVariables(String name, EvaluationContext context) {
        if (name.length() <= 1) {
            return null;
        }

        List<String> parts = findValidSplit(name, context);
        if (parts == null || parts.isEmpty()) {
            return null;
        }

        NodeConstant result = null;
        OperatorContext opCtx = new OperatorContext(context);
        for (String part : parts) {
            NodeConstant value = context.resolve(part);
            if (result == null) {
                result = value;
            } else {
                result = BroadcastingDispatcher.dispatch(result, value, opCtx, (l, r) ->
                        opCtx.applyNumericBinary(l, r, BigRational::multiply, (a, b) -> a * b)
                );
            }
        }

        return result;
    }

    /**
     * Finds a valid way to split the identifier into defined variable names.
     * Uses a greedy approach, trying longer prefixes first.
     *
     * @param name    the identifier to split
     * @param context the evaluation context
     * @return list of variable names that form the split, or null if no valid split exists
     */
    private List<String> findValidSplit(String name, EvaluationContext context) {
        if (name.isEmpty()) {
            return new ArrayList<>();
        }

        for (int i = 1; i <= name.length(); i++) {
            String prefix = name.substring(0, i);
            String suffix = name.substring(i);

            if (context.isDefined(prefix)) {
                if (suffix.isEmpty()) {
                    List<String> result = new ArrayList<>();
                    result.add(prefix);
                    return result;
                }

                List<String> suffixSplit = findValidSplit(suffix, context);
                if (suffixSplit != null) {
                    List<String> result = new ArrayList<>();
                    result.add(prefix);
                    result.addAll(suffixSplit);
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Checks if implicit multiplication is enabled in the current configuration.
     *
     * @return true if implicit multiplication is enabled
     */
    public boolean isImplicitMultiplicationEnabled() {
        return config.implicitMultiplication();
    }
}
