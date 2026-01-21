package uk.co.ryanharrison.mathengine.parser.evaluator.handler;

import uk.co.ryanharrison.mathengine.parser.MathEngineConfig;
import uk.co.ryanharrison.mathengine.parser.evaluator.EvaluationContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.FunctionDefinition;
import uk.co.ryanharrison.mathengine.parser.evaluator.ResolutionContext;
import uk.co.ryanharrison.mathengine.parser.evaluator.UndefinedVariableException;
import uk.co.ryanharrison.mathengine.parser.operator.OperatorContext;
import uk.co.ryanharrison.mathengine.parser.operator.binary.MultiplyOperator;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeUnit;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVariable;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles variable resolution with context-aware priority and explicit disambiguation support.
 * <p>
 * <ul>
 *     <li><b>Context-aware resolution</b> - Different priority based on syntactic context</li>
 *     <li><b>Explicit disambiguation</b> - Force resolution with @unit, $var, #const</li>
 *     <li><b>Implicit multiplication</b> - Split compound identifiers when enabled</li>
 * </ul>
 *
 * <h2>Resolution Order by Context:</h2>
 * <table border="1">
 *     <tr><th>Context</th><th>Priority Order</th><th>Example</th></tr>
 *     <tr>
 *         <td>{@link ResolutionContext#GENERAL GENERAL}</td>
 *         <td>variable → user function → unit → implicit mult</td>
 *         <td>{@code f + 1}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link ResolutionContext#CALL_TARGET CALL_TARGET}</td>
 *         <td>user function → builtin function → variable</td>
 *         <td>{@code f(x)}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link ResolutionContext#POSTFIX_UNIT POSTFIX_UNIT}</td>
 *         <td>unit → variable → implicit mult</td>
 *         <td>{@code 100f}</td>
 *     </tr>
 *     <tr>
 *         <td>{@link ResolutionContext#ASSIGNMENT_TARGET ASSIGNMENT_TARGET}</td>
 *         <td>N/A (handled by assignment logic)</td>
 *         <td>{@code f := 5}</td>
 *     </tr>
 * </table>
 *
 * <h2>Explicit Disambiguation:</h2>
 * <p>
 * Users can override context-based resolution using prefixes:
 * <ul>
 *     <li>{@code @unit} - Forces unit resolution</li>
 *     <li>{@code $var} - Forces variable resolution</li>
 *     <li>{@code #const} - Forces constant resolution</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Context determines priority:
 * f := 5;              // Variable 'f' = 5
 * f                    // Returns 5 (GENERAL context - variable priority)
 * @fahrenheit // Returns fahrenheit unit (explicit override)
 * 100f                 // Returns 100°F (POSTFIX_UNIT context - unit priority)
 * f(x) := x^2          // Defines function 'f'
 * f(3)                 // Returns 9 (CALL_TARGET context - function priority)
 * }</pre>
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
     * Resolves a variable to its value based on resolution context.
     * <p>
     * The resolution context determines priority order when the same identifier
     * could represent multiple entities (variable, function, unit).
     *
     * @param node              the variable node to resolve
     * @param resolutionContext the syntactic context (determines priority)
     * @param opCtx             the operator context (contains evaluation context, used for implicit multiplication)
     * @return the resolved value
     * @throws UndefinedVariableException if the variable cannot be resolved
     */
    public NodeConstant resolve(NodeVariable node, ResolutionContext resolutionContext, OperatorContext opCtx) {
        String name = node.getName();
        EvaluationContext context = opCtx.getEvaluationContext();

        return switch (resolutionContext) {
            case ASSIGNMENT_TARGET ->
                // This shouldn't be called for assignment targets
                // (assignment is handled in evaluateAssignment)
                    throw new IllegalStateException("ASSIGNMENT_TARGET context not supported for variable resolution");

            case CALL_TARGET -> resolveAsCallTarget(name, context);
            case POSTFIX_UNIT -> resolveAsPostfixUnit(name, context, opCtx);
            case GENERAL -> resolveAsGeneral(name, context, opCtx);
        };
    }

    /**
     * Resolves identifier in call position: f in f(...).
     * <p>
     * <b>Priority:</b> user function {@literal >} builtin function {@literal >} variable
     * <p>
     * This priority makes function calls work as expected while still allowing
     * variables that hold lambdas to be called.
     *
     * @param name    the identifier name
     * @param context the evaluation context
     * @return the resolved value
     * @throws UndefinedVariableException if not found
     */
    private NodeConstant resolveAsCallTarget(String name, EvaluationContext context) {
        // User functions take priority
        FunctionDefinition func = context.resolveFunction(name);
        if (func != null) {
            return new NodeFunction(func);
        }

        // Builtin functions are checked by FunctionCallHandler, not here
        // So we just fall back to variable (which might hold a lambda)

        if (context.isDefined(name)) {
            return context.resolve(name);
        }

        throw new UndefinedVariableException(name);
    }

    /**
     * Resolves identifier after number: "f" in "100f".
     * <p>
     * <b>Priority:</b> unit {@literal >} variable {@literal >} implicit multiplication
     * <p>
     * This priority makes unit expressions like "100km" work intuitively.
     *
     * @param name    the identifier name
     * @param context the evaluation context
     * @param opCtx   the operator context for implicit multiplication
     * @return the resolved value
     * @throws UndefinedVariableException if not found
     */
    private NodeConstant resolveAsPostfixUnit(String name, EvaluationContext context, OperatorContext opCtx) {
        // Units have priority after numbers
        UnitRegistry unitRegistry = context.getUnitRegistry();
        if (unitRegistry != null && unitRegistry.isUnit(name)) {
            return NodeUnit.of(1.0, unitRegistry.get(name));
        }

        // Fall back to variable
        if (context.isDefined(name)) {
            return context.resolve(name);
        }

        // Try implicit multiplication
        if (config.implicitMultiplication() && opCtx != null) {
            NodeConstant splitResult = trySplitIntoVariables(name, context, opCtx);
            if (splitResult != null) {
                return splitResult;
            }
        }

        throw new UndefinedVariableException(name);
    }

    /**
     * Resolves identifier in general expression context.
     * <p>
     * <b>Priority:</b> variable {@literal >} user function {@literal >} unit {@literal >} implicit multiplication
     * <p>
     * This priority allows users to shadow built-in entities by defining variables,
     * which is essential for user control and flexibility.
     *
     * @param name    the identifier name
     * @param context the evaluation context
     * @param opCtx   the operator context for implicit multiplication
     * @return the resolved value
     * @throws UndefinedVariableException if not found
     */
    private NodeConstant resolveAsGeneral(String name, EvaluationContext context, OperatorContext opCtx) {
        // Variables have highest priority (allows shadowing units/functions)
        if (context.isDefined(name)) {
            return context.resolve(name);
        }

        // User-defined functions (for first-class function support)
        FunctionDefinition func = context.resolveFunction(name);
        if (func != null) {
            return new NodeFunction(func);
        }

        // Units
        UnitRegistry unitRegistry = context.getUnitRegistry();
        if (unitRegistry != null && unitRegistry.isUnit(name)) {
            return NodeUnit.of(1.0, unitRegistry.get(name));
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
        UnitRegistry unitRegistry = context.getUnitRegistry();
        if (unitRegistry == null || !unitRegistry.isUnit(unitName)) {
            throw new UndefinedVariableException("Unknown unit: @" + unitName);
        }
        return NodeUnit.of(1.0, unitRegistry.get(unitName));
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
        if (!context.isDefined(varName)) {
            throw new UndefinedVariableException("Undefined variable: $" + varName);
        }
        return context.resolve(varName);
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
        // Access the constant registry from config (immutable, never modified by user code)
        // This bypasses any user variable shadowing
        return context.getConfig().constantRegistry()
                .getValue(constName)
                .orElseThrow(() -> new UndefinedVariableException("Undefined constant: #" + constName));
    }

    // ==================== Implicit Multiplication Support ====================

    /**
     * Tries to split an identifier into multiple defined variables and multiply them.
     * <p>
     * For example, "xy" would be split into "x" and "y" and multiplied if both are defined.
     *
     * @param name    the identifier to split
     * @param context the evaluation context
     * @param opCtx   the operator context for multiplication
     * @return the result of multiplying the split variables, or null if not possible
     */
    private NodeConstant trySplitIntoVariables(String name, EvaluationContext context, OperatorContext opCtx) {
        if (name.length() <= 1) {
            return null;
        }

        List<String> parts = findValidSplit(name, context);
        if (parts == null || parts.isEmpty()) {
            return null;
        }

        NodeConstant result = null;
        for (String part : parts) {
            NodeConstant value = context.resolve(part);
            if (result == null) {
                result = value;
            } else {
                result = MultiplyOperator.INSTANCE.apply(result, value, opCtx);
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
