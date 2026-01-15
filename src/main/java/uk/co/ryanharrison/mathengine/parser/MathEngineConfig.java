package uk.co.ryanharrison.mathengine.parser;

import uk.co.ryanharrison.mathengine.core.AngleUnit;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.StandardFunctions;
import uk.co.ryanharrison.mathengine.parser.lexer.TokenType;
import uk.co.ryanharrison.mathengine.parser.operator.BinaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.UnaryOperator;
import uk.co.ryanharrison.mathengine.parser.operator.binary.StandardBinaryOperators;
import uk.co.ryanharrison.mathengine.parser.operator.unary.StandardUnaryOperators;
import uk.co.ryanharrison.mathengine.parser.registry.ConstantRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.KeywordRegistry;
import uk.co.ryanharrison.mathengine.parser.registry.UnitRegistry;

import java.util.List;
import java.util.Map;

/**
 * Immutable configuration for the Math Engine.
 * <p>
 * Use {@link Builder} to construct instances with custom settings.
 * Default configuration uses radians, full precision rationals, and all features enabled.
 *
 * <h2>Feature Toggles:</h2>
 * <ul>
 *     <li>{@link #implicitMultiplication()} - Allow expressions like "2x" or "xy"</li>
 *     <li>{@link #vectorsEnabled()} - Support vector syntax and operations</li>
 *     <li>{@link #matricesEnabled()} - Support matrix syntax and operations</li>
 *     <li>{@link #unitsEnabled()} - Support unit conversions</li>
 *     <li>{@link #comprehensionsEnabled()} - Support list comprehensions</li>
 *     <li>{@link #lambdasEnabled()} - Support lambda expressions</li>
 * </ul>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Default configuration (all features enabled)
 * MathEngineConfig config = MathEngineConfig.defaults();
 *
 * // Custom configuration - calculator mode (no vectors, matrices, etc.)
 * MathEngineConfig calcMode = MathEngineConfig.builder()
 *     .angleUnit(AngleUnit.DEGREES)
 *     .vectorsEnabled(false)
 *     .matricesEnabled(false)
 *     .comprehensionsEnabled(false)
 *     .lambdasEnabled(false)
 *     .build();
 *
 * // High-performance mode (force double arithmetic)
 * MathEngineConfig fastMode = MathEngineConfig.builder()
 *     .forceDoubleArithmetic(true)
 *     .build();
 * }</pre>
 */
public final class MathEngineConfig {

    // Arithmetic settings
    private final AngleUnit angleUnit;
    private final int decimalPlaces;
    private final boolean forceDoubleArithmetic;

    // Limits
    private final int maxRecursionDepth;
    private final int maxExpressionDepth;
    private final int maxVectorSize;
    private final int maxMatrixDimension;
    private final int maxIdentifierLength;

    // Feature toggles
    private final boolean implicitMultiplication;
    private final boolean vectorsEnabled;
    private final boolean matricesEnabled;
    private final boolean unitsEnabled;
    private final boolean comprehensionsEnabled;
    private final boolean lambdasEnabled;
    private final boolean userDefinedFunctionsEnabled;

    // Registries
    private final ConstantRegistry constantRegistry;
    private final KeywordRegistry keywordRegistry;
    private final UnitRegistry unitRegistry;

    // Functions and operators
    private final List<MathFunction> functions;
    private final Map<TokenType, BinaryOperator> binaryOperators;
    private final Map<TokenType, UnaryOperator> unaryOperators;

    private MathEngineConfig(Builder builder) {
        // Arithmetic settings
        this.angleUnit = builder.angleUnit;
        this.decimalPlaces = builder.decimalPlaces;
        this.forceDoubleArithmetic = builder.forceDoubleArithmetic;

        // Limits
        this.maxRecursionDepth = builder.maxRecursionDepth;
        this.maxExpressionDepth = builder.maxExpressionDepth;
        this.maxVectorSize = builder.maxVectorSize;
        this.maxMatrixDimension = builder.maxMatrixDimension;
        this.maxIdentifierLength = builder.maxIdentifierLength;

        // Feature toggles
        this.implicitMultiplication = builder.implicitMultiplication;
        this.vectorsEnabled = builder.vectorsEnabled;
        this.matricesEnabled = builder.matricesEnabled;
        this.unitsEnabled = builder.unitsEnabled;
        this.comprehensionsEnabled = builder.comprehensionsEnabled;
        this.lambdasEnabled = builder.lambdasEnabled;
        this.userDefinedFunctionsEnabled = builder.userDefinedFunctionsEnabled;

        // Registries
        this.constantRegistry = builder.constantRegistry;
        this.keywordRegistry = builder.keywordRegistry;
        this.unitRegistry = builder.unitRegistry;

        // Functions and operators
        this.functions = List.copyOf(builder.functions);
        this.binaryOperators = Map.copyOf(builder.binaryOperators);
        this.unaryOperators = Map.copyOf(builder.unaryOperators);
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a default configuration instance with all features enabled.
     *
     * @return default configuration with standard settings
     */
    public static MathEngineConfig defaults() {
        return new Builder().build();
    }

    /**
     * Creates a minimal arithmetic-only configuration for maximum performance.
     * Only basic operators, no functions, no advanced features, double arithmetic enabled.
     *
     * @return arithmetic-only configuration
     */
    public static MathEngineConfig arithmetic() {
        return new Builder()
                .forceDoubleArithmetic(true)
                .implicitMultiplication(false)
                .vectorsEnabled(false)
                .matricesEnabled(false)
                .unitsEnabled(false)
                .comprehensionsEnabled(false)
                .lambdasEnabled(false)
                .userDefinedFunctionsEnabled(false)
                .functions(List.of())
                .build();
    }

    /**
     * Creates a basic calculator configuration with core math functions.
     * Includes trig, exponential, logarithmic, rounding functions. Uses double arithmetic.
     * Excludes vectors, matrices, lambdas, and specialized functions.
     *
     * @return basic calculator configuration
     */
    public static MathEngineConfig basic() {
        return new Builder()
                .forceDoubleArithmetic(true)
                .vectorsEnabled(false)
                .matricesEnabled(false)
                .comprehensionsEnabled(false)
                .lambdasEnabled(false)
                .userDefinedFunctionsEnabled(false)
                .functions(StandardFunctions.basic())
                .build();
    }

    /**
     * Creates a new builder for constructing custom configurations.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder pre-populated with this configuration's settings.
     * Useful for creating modified copies of existing configurations.
     *
     * @return builder with current settings
     */
    public Builder toBuilder() {
        return new Builder()
                .angleUnit(angleUnit)
                .decimalPlaces(decimalPlaces)
                .forceDoubleArithmetic(forceDoubleArithmetic)
                .maxRecursionDepth(maxRecursionDepth)
                .maxExpressionDepth(maxExpressionDepth)
                .maxVectorSize(maxVectorSize)
                .maxMatrixDimension(maxMatrixDimension)
                .maxIdentifierLength(maxIdentifierLength)
                .implicitMultiplication(implicitMultiplication)
                .vectorsEnabled(vectorsEnabled)
                .matricesEnabled(matricesEnabled)
                .unitsEnabled(unitsEnabled)
                .comprehensionsEnabled(comprehensionsEnabled)
                .lambdasEnabled(lambdasEnabled)
                .userDefinedFunctionsEnabled(userDefinedFunctionsEnabled)
                .constantRegistry(constantRegistry)
                .keywordRegistry(keywordRegistry)
                .unitRegistry(unitRegistry)
                .functions(functions)
                .binaryOperators(binaryOperators)
                .unaryOperators(unaryOperators);
    }

    // ==================== Arithmetic Settings ====================

    /**
     * The angle unit for trigonometric functions.
     */
    public AngleUnit angleUnit() {
        return angleUnit;
    }

    /**
     * Number of decimal places for output formatting (-1 for full precision).
     */
    public int decimalPlaces() {
        return decimalPlaces;
    }

    /**
     * Whether to force double arithmetic instead of rationals.
     * Setting this to true can improve performance at the cost of precision.
     */
    public boolean forceDoubleArithmetic() {
        return forceDoubleArithmetic;
    }

    // ==================== Limits ====================

    /**
     * Maximum recursion depth for function calls.
     */
    public int maxRecursionDepth() {
        return maxRecursionDepth;
    }

    /**
     * Maximum nesting depth for expressions.
     */
    public int maxExpressionDepth() {
        return maxExpressionDepth;
    }

    /**
     * Maximum number of elements in a vector.
     */
    public int maxVectorSize() {
        return maxVectorSize;
    }

    /**
     * Maximum dimension (rows or columns) for a matrix.
     */
    public int maxMatrixDimension() {
        return maxMatrixDimension;
    }

    /**
     * Maximum length of an identifier name.
     */
    public int maxIdentifierLength() {
        return maxIdentifierLength;
    }

    // ==================== Feature Toggles ====================

    /**
     * Whether implicit multiplication is allowed (e.g., "2x" means "2 * x").
     * When disabled, variables must be explicitly multiplied.
     */
    public boolean implicitMultiplication() {
        return implicitMultiplication;
    }

    /**
     * Whether vector syntax and operations are enabled.
     * When disabled, vector literals like {1, 2, 3} will cause an error.
     */
    public boolean vectorsEnabled() {
        return vectorsEnabled;
    }

    /**
     * Whether matrix syntax and operations are enabled.
     * When disabled, matrix literals like [1, 2; 3, 4] will cause an error.
     */
    public boolean matricesEnabled() {
        return matricesEnabled;
    }

    /**
     * Whether unit conversions are enabled.
     * When disabled, expressions like "5 meters to feet" will cause an error.
     */
    public boolean unitsEnabled() {
        return unitsEnabled;
    }

    /**
     * Whether list comprehensions are enabled.
     * When disabled, expressions like "{x^2 for x in 1..10}" will cause an error.
     */
    public boolean comprehensionsEnabled() {
        return comprehensionsEnabled;
    }

    /**
     * Whether lambda expressions are enabled.
     * When disabled, expressions like "x -> x^2" will cause an error.
     */
    public boolean lambdasEnabled() {
        return lambdasEnabled;
    }

    /**
     * Whether user-defined functions are enabled.
     * When disabled, expressions like "f(x) := x^2" will cause an error.
     */
    public boolean userDefinedFunctionsEnabled() {
        return userDefinedFunctionsEnabled;
    }

    // ==================== Registries ====================

    /**
     * Gets the constant registry defining available constants.
     * <p>
     * This registry provides:
     * <ul>
     *     <li>Constant names for lexer recognition (identifier splitting)</li>
     *     <li>Constant values for predefined variables in the evaluation context</li>
     * </ul>
     *
     * @return the constant registry (never null)
     */
    public ConstantRegistry constantRegistry() {
        return constantRegistry;
    }

    /**
     * Gets the keyword registry defining reserved keywords.
     * <p>
     * This registry provides:
     * <ul>
     *     <li>Reserved keywords (for, in, if, step, to, as)</li>
     *     <li>Keyword operators (and, or, xor, not, mod, of) mapped to token types</li>
     * </ul>
     *
     * @return the keyword registry (never null)
     */
    public KeywordRegistry keywordRegistry() {
        return keywordRegistry;
    }

    /**
     * Gets the unit registry defining available units for conversion.
     *
     * @return the unit registry (never null)
     */
    public UnitRegistry unitRegistry() {
        return unitRegistry;
    }

    // ==================== Functions and Operators ====================

    /**
     * Gets the list of built-in functions to register.
     * <p>
     * These functions will be registered in the FunctionExecutor and made available
     * for use in expressions.
     *
     * @return unmodifiable list of functions (never null)
     */
    public List<MathFunction> functions() {
        return functions;
    }

    /**
     * Gets the map of binary operators to register.
     *
     * @return unmodifiable map of token types to binary operators (never null)
     */
    public Map<TokenType, BinaryOperator> binaryOperators() {
        return binaryOperators;
    }

    /**
     * Gets the map of unary operators to register.
     *
     * @return unmodifiable map of token types to unary operators (never null)
     */
    public Map<TokenType, UnaryOperator> unaryOperators() {
        return unaryOperators;
    }

    // ==================== Builder ====================

    /**
     * Builder for constructing {@link MathEngineConfig} instances.
     */
    public static final class Builder {
        private AngleUnit angleUnit = AngleUnit.RADIANS;
        private int decimalPlaces = -1;
        private boolean forceDoubleArithmetic = false;
        private int maxRecursionDepth = 1000;
        private int maxExpressionDepth = 1000;
        private int maxVectorSize = 1_000_000;
        private int maxMatrixDimension = 10_000;
        private int maxIdentifierLength = 256;
        private boolean implicitMultiplication = true;
        private boolean vectorsEnabled = true;
        private boolean matricesEnabled = true;
        private boolean unitsEnabled = true;
        private boolean comprehensionsEnabled = true;
        private boolean lambdasEnabled = true;
        private boolean userDefinedFunctionsEnabled = true;
        private ConstantRegistry constantRegistry = ConstantRegistry.withDefaults();
        private KeywordRegistry keywordRegistry = KeywordRegistry.withDefaults();
        private UnitRegistry unitRegistry = new UnitRegistry();
        private List<MathFunction> functions = StandardFunctions.all();
        private Map<TokenType, BinaryOperator> binaryOperators = StandardBinaryOperators.all();
        private Map<TokenType, UnaryOperator> unaryOperators = StandardUnaryOperators.all();

        private Builder() {
        }

        // ==================== Arithmetic Settings ====================

        /**
         * Sets angle unit for trigonometric functions.
         */
        public Builder angleUnit(AngleUnit angleUnit) {
            if (angleUnit == null) throw new IllegalArgumentException("Angle unit cannot be null");
            this.angleUnit = angleUnit;
            return this;
        }

        /**
         * Sets decimal places for output formatting (-1 for full precision).
         */
        public Builder decimalPlaces(int decimalPlaces) {
            if (decimalPlaces < -1) throw new IllegalArgumentException("Decimal places must be >= -1, got: " + decimalPlaces);
            this.decimalPlaces = decimalPlaces;
            return this;
        }

        /**
         * Enables double arithmetic (faster, less precision for exact fractions).
         */
        public Builder forceDoubleArithmetic(boolean force) {
            this.forceDoubleArithmetic = force;
            return this;
        }

        // ==================== Limits ====================

        /**
         * Sets maximum recursion depth for function calls.
         */
        public Builder maxRecursionDepth(int maxRecursionDepth) {
            if (maxRecursionDepth <= 0)
                throw new IllegalArgumentException("Max recursion depth must be positive, got: " + maxRecursionDepth);
            this.maxRecursionDepth = maxRecursionDepth;
            return this;
        }

        /**
         * Sets maximum nesting depth for expressions.
         */
        public Builder maxExpressionDepth(int maxExpressionDepth) {
            if (maxExpressionDepth <= 0)
                throw new IllegalArgumentException("Max expression depth must be positive, got: " + maxExpressionDepth);
            this.maxExpressionDepth = maxExpressionDepth;
            return this;
        }

        /**
         * Sets maximum vector size.
         */
        public Builder maxVectorSize(int maxVectorSize) {
            if (maxVectorSize <= 0) throw new IllegalArgumentException("Max vector size must be positive, got: " + maxVectorSize);
            this.maxVectorSize = maxVectorSize;
            return this;
        }

        /**
         * Sets maximum matrix dimension (rows or columns).
         */
        public Builder maxMatrixDimension(int maxMatrixDimension) {
            if (maxMatrixDimension <= 0)
                throw new IllegalArgumentException("Max matrix dimension must be positive, got: " + maxMatrixDimension);
            this.maxMatrixDimension = maxMatrixDimension;
            return this;
        }

        /**
         * Sets maximum identifier length.
         */
        public Builder maxIdentifierLength(int maxIdentifierLength) {
            if (maxIdentifierLength <= 0)
                throw new IllegalArgumentException("Max identifier length must be positive, got: " + maxIdentifierLength);
            this.maxIdentifierLength = maxIdentifierLength;
            return this;
        }

        // ==================== Feature Toggles ====================

        /**
         * Enables/disables implicit multiplication (e.g., "2x" means "2 * x").
         */
        public Builder implicitMultiplication(boolean enabled) {
            this.implicitMultiplication = enabled;
            return this;
        }

        /**
         * Enables/disables vector syntax and operations.
         */
        public Builder vectorsEnabled(boolean enabled) {
            this.vectorsEnabled = enabled;
            return this;
        }

        /**
         * Enables/disables matrix syntax and operations.
         */
        public Builder matricesEnabled(boolean enabled) {
            this.matricesEnabled = enabled;
            return this;
        }

        /**
         * Enables/disables unit conversions.
         */
        public Builder unitsEnabled(boolean enabled) {
            this.unitsEnabled = enabled;
            return this;
        }

        /**
         * Enables/disables list comprehensions.
         */
        public Builder comprehensionsEnabled(boolean enabled) {
            this.comprehensionsEnabled = enabled;
            return this;
        }

        /**
         * Enables/disables lambda expressions.
         */
        public Builder lambdasEnabled(boolean enabled) {
            this.lambdasEnabled = enabled;
            return this;
        }

        /**
         * Enables/disables user-defined functions.
         */
        public Builder userDefinedFunctionsEnabled(boolean enabled) {
            this.userDefinedFunctionsEnabled = enabled;
            return this;
        }

        // ==================== Registries ====================

        /**
         * Sets the constant registry (defines available constants and their values).
         */
        public Builder constantRegistry(ConstantRegistry constantRegistry) {
            if (constantRegistry == null) throw new IllegalArgumentException("Constant registry cannot be null");
            this.constantRegistry = constantRegistry;
            return this;
        }

        /**
         * Sets the keyword registry (defines reserved keywords and keyword operators).
         */
        public Builder keywordRegistry(KeywordRegistry keywordRegistry) {
            if (keywordRegistry == null) throw new IllegalArgumentException("Keyword registry cannot be null");
            this.keywordRegistry = keywordRegistry;
            return this;
        }

        /**
         * Sets the unit registry (defines available units for conversion).
         */
        public Builder unitRegistry(UnitRegistry unitRegistry) {
            if (unitRegistry == null) throw new IllegalArgumentException("Unit registry cannot be null");
            this.unitRegistry = unitRegistry;
            return this;
        }

        // ==================== Functions and Operators ====================

        /**
         * Sets built-in functions to register. Pass empty list to disable all.
         */
        public Builder functions(List<MathFunction> functions) {
            if (functions == null) throw new IllegalArgumentException("Functions list cannot be null");
            this.functions = functions;
            return this;
        }

        /**
         * Sets binary operators to register.
         */
        public Builder binaryOperators(Map<TokenType, BinaryOperator> binaryOperators) {
            if (binaryOperators == null) throw new IllegalArgumentException("Binary operators map cannot be null");
            this.binaryOperators = binaryOperators;
            return this;
        }

        /**
         * Sets unary operators to register.
         */
        public Builder unaryOperators(Map<TokenType, UnaryOperator> unaryOperators) {
            if (unaryOperators == null) throw new IllegalArgumentException("Unary operators map cannot be null");
            this.unaryOperators = unaryOperators;
            return this;
        }

        /**
         * Builds the immutable configuration.
         */
        public MathEngineConfig build() {
            return new MathEngineConfig(this);
        }
    }
}
