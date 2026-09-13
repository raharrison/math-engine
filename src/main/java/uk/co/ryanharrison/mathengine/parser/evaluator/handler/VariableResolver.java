package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.ast.*;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.UndefinedVariableException;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.binary.MultiplyOperator;

/**
 * Resolves a bare identifier: variable → user function → unit → implicit multiplication.
 * A session name therefore shadows a unit of the same name. The sigils {@code @unit},
 * {@code $var} and {@code #const} each resolve one kind only.
 */
public final class VariableResolver {

    private final MathEngineConfig config;

    public VariableResolver(MathEngineConfig config) {
        this.config = config;
    }

    /**
     * @throws UndefinedVariableException if the name resolves to nothing
     */
    public NodeConstant resolve(NodeVariable node, OperatorContext opCtx) {
        return resolveName(node.getName(), opCtx.getEvaluationContext(), opCtx);
    }

    private NodeConstant resolveName(String name, EvaluationContext context, OperatorContext opCtx) {
        // Variables have highest priority (allows shadowing units/functions)
        var varOpt = context.resolve(name);
        if (varOpt.isPresent()) {
            return varOpt.get();
        }

        // User-defined functions (for first-class function support)
        var funcOpt = context.resolveFunction(name);
        if (funcOpt.isPresent()) {
            return new NodeFunction(funcOpt.get());
        }

        // Units
        var unitOpt = context.resolveUnit(name);
        if (unitOpt.isPresent()) {
            return NodeUnit.of(new NodeRational(1), unitOpt.get());
        }

        // Implicit multiplication as last resort
        if (config.implicitMultiplication() && opCtx != null) {
            NodeConstant splitResult = trySplitIntoVariables(name, context, opCtx);
            if (splitResult != null) {
                return splitResult;
            }
        }

        throw new UndefinedVariableException(name);
    }

    // ==================== Explicit Disambiguation Methods ====================

    /**
     * Resolves an explicit unit reference (@unit).
     * <p>
     * Forces resolution as a unit, bypassing normal priority rules.
     *
     * @param unitName the unit name (without @ prefix)
     * @param context  the evaluation context
     * @return NodeUnit with value 1.0
     * @throws UndefinedVariableException if unit doesn't exist
     */
    public NodeConstant resolveUnitRef(String unitName, EvaluationContext context) {
        return context.resolveUnit(unitName)
                .map(unit -> NodeUnit.of(new NodeRational(1), unit))
                .orElseThrow(() -> UndefinedVariableException.unit(unitName));
    }

    /**
     * Resolves an explicit variable reference ($var).
     * <p>
     * Forces resolution as a variable, bypassing normal priority rules.
     *
     * @param varName the variable name (without $ prefix)
     * @param context the evaluation context
     * @return the variable value
     * @throws UndefinedVariableException if variable not defined
     */
    public NodeConstant resolveVarRef(String varName, EvaluationContext context) {
        return context.resolve(varName)
                .orElseThrow(() -> UndefinedVariableException.variable(varName));
    }

    /**
     * Resolves an explicit constant reference (#const).
     * <p>
     * Forces resolution as a mathematical constant, bypassing variable shadowing.
     * Constants are stored in the immutable ConstantRegistry and cannot be overwritten
     * by user variable assignments.
     *
     * @param constName the constant name (without # prefix)
     * @param context   the evaluation context
     * @return the constant value from the constant registry
     * @throws UndefinedVariableException if constant not defined in registry
     */
    public NodeConstant resolveConstRef(String constName, EvaluationContext context) {
        return context.resolveConstant(constName)
                .orElseThrow(() -> UndefinedVariableException.constant(constName));
    }

    // ==================== Implicit Multiplication Support ====================

    /**
     * Tries to split an identifier into resolvable parts (variables, constants, functions) and multiply them.
     * <p>
     * <b>Examples:</b>
     * <ul>
     *     <li>"xy" where x=2, y=3 → 2 * 3 = 6</li>
     *     <li>"xpi" where x=2 → 2 * π ≈ 6.28</li>
     *     <li>"abc" where a=1, b=2, c=3 → 1 * 2 * 3 = 6</li>
     * </ul>
     * <p>
     * Uses recursive backtracking to find a valid split. Checks for:
     * <ol>
     *     <li>User-defined variables</li>
     *     <li>Constants (pi, e, etc.)</li>
     *     <li>User-defined functions</li>
     * </ol>
     *
     * @param name    the identifier to split
     * @param context the evaluation context
     * @param opCtx   the operator context for multiplication
     * @return the result of multiplying the split parts, or null if no valid split exists
     */
    private NodeConstant trySplitIntoVariables(String name, EvaluationContext context, OperatorContext opCtx) {
        if (name.length() <= 1) {
            return null;
        }
        return splitAndMultiply(name, 0, context, opCtx);
    }

    /**
     * Recursively finds a valid split starting at position 'start' and computes the product.
     * <p>
     * Checks each substring to see if it can be resolved as:
     * <ul>
     *     <li>A defined variable</li>
     *     <li>A mathematical constant (from constant registry)</li>
     *     <li>A user-defined function</li>
     * </ul>
     *
     * @return the product of all parts, or null if no valid split exists from the given position
     */
    private NodeConstant splitAndMultiply(String name, int start, EvaluationContext context, OperatorContext opCtx) {
        if (start == name.length()) {
            return null; // Empty suffix - caller will handle
        }

        for (int end = start + 1; end <= name.length(); end++) {
            String part = name.substring(start, end);

            // Try to resolve this part (variable, constant, or function)
            NodeConstant partValue = tryResolvePart(part, context);

            if (partValue != null) {
                if (end == name.length()) {
                    // Last part - return its value
                    return partValue;
                }

                // Try to split the remainder
                NodeConstant restValue = splitAndMultiply(name, end, context, opCtx);
                if (restValue != null) {
                    return MultiplyOperator.INSTANCE.apply(partValue, restValue, opCtx);
                }
            }
        }

        return null;
    }

    /**
     * Attempts to resolve a string as a variable, constant, or function.
     * <p>
     * <b>Resolution order:</b>
     * <ol>
     *     <li>Variable (highest priority - allows shadowing)</li>
     *     <li>Constant (from constant registry)</li>
     *     <li>User-defined function (for first-class function support)</li>
     * </ol>
     *
     * @param part    the string to resolve
     * @param context the evaluation context
     * @return the resolved value, or null if not resolvable
     */
    private NodeConstant tryResolvePart(String part, EvaluationContext context) {
        // resolve() checks variables first, then constants
        var resolvedOpt = context.resolve(part);
        if (resolvedOpt.isPresent()) {
            return resolvedOpt.get();
        }

        // User-defined functions (for first-class function support)
        var funcOpt = context.resolveFunction(part);
        return funcOpt.map(NodeFunction::new).orElse(null);
    }
}
