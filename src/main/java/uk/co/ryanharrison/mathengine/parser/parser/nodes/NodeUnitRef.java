package uk.co.ryanharrison.mathengine.parser.parser.nodes;

/**
 * Node representing an explicit unit reference using the {@code @} prefix.
 * <p>
 * Syntax: {@code @unitname}
 * <p>
 * This node forces the identifier to be resolved as a unit, even if a variable
 * or function with the same name exists. This provides unambiguous unit access.
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Force unit resolution:
 * @fahrenheit // Always resolves to fahrenheit unit
 * 100@fahrenheit        // 100 fahrenheit (explicit unit reference)
 *
 * // Compare with ambiguous case:
 * f := 5;              // Variable 'f'
 * f                    // Returns 5 (variable takes priority)
 * @f // Returns fahrenheit unit (explicit override)
 * }</pre>
 *
 * <h2>Design Rationale:</h2>
 * <p>
 * The {@code @} prefix disambiguates cases where the same identifier could
 * represent a variable, function, or unit. This is particularly useful when:
 * <ul>
 *     <li>A user has defined a variable that shadows a unit name</li>
 *     <li>Explicit clarity is desired in complex expressions</li>
 *     <li>Programmatic generation of expressions needs guaranteed unit access</li>
 * </ul>
 *
 * @see NodeVarRef for explicit variable references ($var)
 * @see NodeConstRef for explicit constant references (#const)
 */
public final class NodeUnitRef extends NodeExpression {

    private final String unitName;

    /**
     * Creates a new explicit unit reference node.
     *
     * @param unitName the name of the unit (without the @ prefix)
     */
    public NodeUnitRef(String unitName) {
        if (unitName == null || unitName.isEmpty()) {
            throw new IllegalArgumentException("Unit name cannot be null or empty");
        }
        this.unitName = unitName;
    }

    /**
     * Gets the unit name being explicitly referenced.
     *
     * @return the unit name (without @ prefix)
     */
    public String getUnitName() {
        return unitName;
    }

    @Override
    public String toString() {
        return "@" + unitName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NodeUnitRef other)) return false;
        return unitName.equals(other.unitName);
    }

    @Override
    public int hashCode() {
        return unitName.hashCode();
    }
}
