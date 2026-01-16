package uk.co.ryanharrison.mathengine.parser.evaluator;

/**
 * Specifies the syntactic context for resolving ambiguous identifiers.
 * <p>
 * The resolution context determines priority order when an identifier
 * could represent multiple things (variable, function, unit, constant).
 * This enables context-aware resolution that matches user expectations.
 *
 * <h2>Design Rationale:</h2>
 * <p>
 * The same identifier (e.g., "f") can have different meanings depending on
 * where it appears in the syntax tree:
 * <ul>
 *     <li>After `:=` it's always a variable assignment target</li>
 *     <li>Before `(` it's likely a function call</li>
 *     <li>After a number it's likely a unit (e.g., "100f" = 100 fahrenheit)</li>
 *     <li>In a general expression, variables take priority (allowing user override)</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Context changes priority order:
 * f := 5              // ASSIGNMENT_TARGET - creates variable 'f'
 * 100f                // POSTFIX_UNIT - tries unit first (100 fahrenheit)
 * f(x)                // CALL_TARGET - tries function first
 * f + 1               // GENERAL - tries variable first (user override)
 * }</pre>
 */
public enum ResolutionContext {

    /**
     * General expression context (default).
     * <p>
     * <b>Priority:</b> variable {@literal >} user function {@literal >} unit {@literal >} implicit multiplication
     * <p>
     * This priority allows users to shadow built-in entities by defining variables.
     * Example: {@code f := 5; f + 1} returns 6, even if 'f' is a unit name.
     */
    GENERAL,

    /**
     * Assignment target context (left side of :=).
     * <p>
     * <b>Priority:</b> always creates/updates variable (shadowing is intentional)
     * <p>
     * Example: {@code f := 100} creates a variable 'f' with value 100,
     * even if 'fahrenheit' exists as a unit.
     */
    ASSIGNMENT_TARGET,

    /**
     * Function call target context (before parentheses).
     * <p>
     * <b>Priority:</b> user function {@literal >} builtin function {@literal >} variable
     * <p>
     * Example: {@code f(x)} tries to call function 'f' first before
     * checking if 'f' is a variable containing a lambda.
     */
    CALL_TARGET,

    /**
     * Postfix unit context (after a number).
     * <p>
     * <b>Priority:</b> unit {@literal >} variable {@literal >} implicit multiplication
     * <p>
     * Example: {@code 100f} tries to interpret 'f' as a unit (fahrenheit)
     * before checking if it's a variable.
     */
    POSTFIX_UNIT
}
