package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing an explicit variable reference using the {@code $} prefix.
 * <p>
 * Syntax: {@code $variablename}
 * <p>
 * This node forces the identifier to be resolved as a variable, even if a unit,
 * function, or constant with the same name exists. This provides unambiguous
 * variable access.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Force variable resolution:
 * pi := 100;           // Define variable 'pi' (shadows constant)
 * pi                   // Returns 100 (variable takes priority)
 * $pi                  // Returns 100 (explicit variable reference)
 * #pi                  // Returns 3.14159... (explicit constant reference)
 *
 * // With functions:
 * f := 5;              // Variable 'f'
 * f(x) := x^2;         // Function 'f' (separate namespace)
 * $f                   // Returns 5 (explicit variable access)
 * f(3)                 // Returns 9 (function call)
 * }</pre>
 *
 * <h2>Design Rationale:</h2>
 * <p>
 * The {@code $} prefix disambiguates cases where the same identifier could
 * represent a variable, function, constant, or unit. This is particularly useful when:
 * <ul>
 *     <li>A user wants to explicitly access a variable that might shadow a built-in</li>
 *     <li>Clarity is needed in complex expressions with multiple namespaces</li>
 *     <li>Programmatic generation needs guaranteed variable access</li>
 * </ul>
 *
 * @see NodeUnitRef for explicit unit references (@unit)
 * @see NodeConstRef for explicit constant references (#const)
 */
public final class NodeVarRef extends NodeExpression {

    private final String varName;

    /**
     * Creates a new explicit variable reference node.
     *
     * @param varName the name of the variable (without the $ prefix)
     */
    public NodeVarRef(String varName) {
        if (varName == null || varName.isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        this.varName = varName;
    }

    /**
     * Gets the variable name being explicitly referenced.
     *
     * @return the variable name (without $ prefix)
     */
    public String getVarName() {
        return varName;
    }

    @Override
    public String toString() {
        return "$" + varName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeVarRef other)) return false;
        return varName.equals(other.varName);
    }

    @Override
    public int hashCode() {
        return varName.hashCode();
    }
}
