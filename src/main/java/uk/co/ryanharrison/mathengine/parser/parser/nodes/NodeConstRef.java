package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing an explicit constant reference using the {@code #} prefix.
 * <p>
 * Syntax: {@code #constantname}
 * <p>
 * This node forces the identifier to be resolved as a mathematical constant,
 * even if a variable with the same name has been defined. This provides
 * unambiguous access to built-in constants like pi, e, phi, etc.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Force constant resolution:
 * pi := 100;           // Define variable 'pi' (shadows constant)
 * pi                   // Returns 100 (variable takes priority)
 * #pi                  // Returns 3.14159... (explicit constant reference)
 * $pi                  // Returns 100 (explicit variable reference)
 *
 * // Useful in complex expressions:
 * e := 2;              // Variable 'e'
 * e^2                  // Returns 4 (variable used)
 * #e^2                 // Returns 7.389... (constant used)
 * }</pre>
 *
 * <h2>Supported Constants:</h2>
 * <ul>
 *     <li>{@code pi} - π (3.14159...)</li>
 *     <li>{@code e} or {@code euler} - Euler's number (2.71828...)</li>
 *     <li>{@code phi} or {@code goldenratio} - Golden ratio (1.61803...)</li>
 *     <li>{@code tau} - 2π (6.28318...)</li>
 *     <li>{@code infinity} or {@code inf} - Positive infinity</li>
 *     <li>{@code nan} - Not-a-Number</li>
 *     <li>{@code true}, {@code false} - Boolean constants</li>
 * </ul>
 *
 * <h2>Design Rationale:</h2>
 * <p>
 * The {@code #} prefix disambiguates cases where a user has shadowed a built-in
 * constant with a variable of the same name. This is useful when:
 * <ul>
 *     <li>You want to temporarily override a constant but still access the original</li>
 *     <li>Clarity is needed about which value is being used</li>
 *     <li>Teaching/demonstration code needs to show the difference</li>
 * </ul>
 *
 * @see NodeVarRef for explicit variable references ($var)
 * @see NodeUnitRef for explicit unit references (@unit)
 */
public final class NodeConstRef extends NodeExpression {

    private final String constName;

    /**
     * Creates a new explicit constant reference node.
     *
     * @param constName the name of the constant (without the # prefix)
     */
    public NodeConstRef(String constName) {
        if (constName == null || constName.isEmpty()) {
            throw new IllegalArgumentException("Constant name cannot be null or empty");
        }
        this.constName = constName;
    }

    /**
     * Gets the constant name being explicitly referenced.
     *
     * @return the constant name (without # prefix)
     */
    public String getConstName() {
        return constName;
    }

    @Override
    public String toString() {
        return "#" + constName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeConstRef other)) return false;
        return constName.equals(other.constName);
    }

    @Override
    public int hashCode() {
        return constName.hashCode();
    }
}
