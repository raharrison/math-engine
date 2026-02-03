# CLAUDE.md

IMPORTANT: When applicable, prefer using intellij-index MCP tools for code navigation and refactoring.

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Math Engine is a comprehensive Java mathematical library featuring an advanced expression parser with support for custom
functions, vectors, matrices, symbolic differentiation, numerical integration, equation solving, unit conversions, probability
distributions, and more.

The library is designed around the `Function` class (modeling f(x) equations) and the `MathEngine` class (an expression
parser/evaluator). Most packages can be used standalone or integrated through the engine's operator and function system.

## File Structure Quick Reference

```
src/main/java/uk/co/ryanharrison/mathengine/
├── core/                             # BigRational and Function
├── utils/                            # Utility methods
│
├── parser/                           # Expression parser (CORE)
│   ├── MathEngine.java               # Main entry point
│   ├── MathEngineConfig.java         # Configuration builder
│   ├── CompiledExpression.java       # Pre-compiled expressions
│   ├── MathEngineException.java      # Base exception
│   │
│   ├── lexer/                        # Tokenization (2-stage pipeline)
│   │   ├── Lexer.java                # Main coordinator
│   │   ├── TokenScanner.java         # Character → tokens
│   │   ├── TokenProcessor.java       # Split + classify + implicit mult
│   │   ├── Token.java                # Token record
│   │   └── TokenType.java            # Token types enum
│   │
│   ├── parser/                       # AST construction
│   │   ├── Parser.java               # Main parser
│   │   ├── PrecedenceParser.java     # Operator precedence
│   │   ├── CollectionParser.java     # Vectors/matrices/comprehensions
│   │   └── nodes/                    # AST node types
│   │       ├── Node.java             # Base class
│   │       ├── NodeConstant.java     # Evaluated values
│   │       ├── NodeExpression.java   # Unevaluated AST
│   │       ├── NodeVariable.java     # Variable reference
│   │       └── (30+ other nodes)
│   │
│   ├── evaluator/                    # Evaluation engine
│   │   ├── Evaluator.java            # Main evaluator
│   │   ├── EvaluationContext.java    # Variable/function storage
│   │   └── handler/                  # Specialized handlers
│   │       ├── VariableResolver.java # Context-aware resolution
│   │       └── FunctionCallHandler.java
│   │
│   ├── operator/                     # Operator system
│   │   ├── OperatorExecutor.java     # Dispatch system
│   │   ├── OperatorContext.java      # Utilities for operators
│   │   ├── MatrixOperations.java     # True matrix multiply/power
│   │   ├── binary/                   # Binary operators (20+)
│   │   └── unary/                    # Unary operators (10+)
│   │
│   ├── function/                     # Function system
│   │   ├── FunctionExecutor.java     # Function dispatch
│   │   ├── FunctionContext.java      # Evaluator context for functions
│   │   ├── FunctionBuilder.java      # Fluent DSL for defining functions
│   │   ├── ArgType.java              # Type-safe parameter extraction
│   │   ├── ArgTypes.java             # Predefined type extractors
│   │   ├── TypedUnaryBuilder.java    # Builder for typed unary functions
│   │   ├── TypedBinaryBuilder.java   # Builder for typed binary functions
│   │   ├── TypedTernaryBuilder.java  # Builder for typed ternary functions
│   │   ├── UnaryFunction.java        # Unary function interface
│   │   ├── BinaryFunction.java       # Binary function interface
│   │   ├── AggregateFunction.java    # Variadic function interface
│   │   ├── TrigFunction.java         # Trig function factory (angle units)
│   │   ├── math/                     # Math functions
│   │   ├── trig/                     # Trigonometric functions
│   │   ├── vector/                   # Vector functions
│   │   ├── special/                  # Type/conditional/bitwise
│   │   └── string/                   # String functions
│   │
│   ├── format/                       # Output formatting
│   │   └── NodeFormatter.java        # Node formatting by String or AsciiMath
│   │
│   ├── registry/                     # Lookup registries
│   │   ├── UnitRegistry.java         # Physical units
│   │   ├── ConstantRegistry.java     # Mathematical constants
│   │   └── KeywordRegistry.java      # Reserved keywords
│   │
│   ├── symbolic/                     # Symbolic math (differentiation)
│   └── util/                         # Parser utilities
│       ├── BroadcastingEngine.java   # Unified broadcasting for functions/operators
│       ├── TypeCoercion.java         # Type promotion/conversion
│       ├── NumericOperations.java    # Numeric helpers
│       ├── AstTreeBuilder.java       # AST construction helpers
│       ├── FunctionCaller.java       # Function invocation helper
│       └── PersistentHashMap.java    # Immutable map
│
├── differential/                     # Differentiation
├── integral/                         # Numerical integration
├── solvers/                          # Root finding algorithms
├── distributions/                    # Probability distributions
├── linearalgebra/                    # Vector/Matrix
├── regression/                       # Regression models
├── unitconversion/                   # Unit conversion
├── special/                          # Special functions
├── plotting/                         # Graphing
└── gui/                              # GUI applications
```

## Build and Test Commands

**Build System**: Gradle with Kotlin DSL

```bash
# Build the project
./gradlew build

# Run all tests
./gradlew test

# Run all tests with detailed output
./gradlew test --info

# Run tests for a specific class
./gradlew test --tests uk.co.ryanharrison.mathengine.parser.ParserTest

# Run a single test method
./gradlew test --tests uk.co.ryanharrison.mathengine.parser.ParserTest.testSingleRowMatrix

# Run tests matching a pattern
./gradlew test --tests "*Distribution*"

# Run tests in a specific package
./gradlew test --tests "uk.co.ryanharrison.mathengine.distributions.*"

# Generate test coverage report
./gradlew jacocoTestReport
# Report will be in build/reports/jacoco/test/html/index.html

# Print test summary from XML reports (custom task)
./gradlew testSummary

# Run tests and immediately view summary
./gradlew test testSummary

# Clean build artifacts
./gradlew clean
```

### Efficient Test Workflow for AI Agents

**Critical: Use test filters to minimize feedback latency**

When developing or refactoring tests, ALWAYS use test filters instead of running all tests:

```bash
# Run specific test class during development
./gradlew test --tests NormalDistributionTest

# Run all tests in a package
./gradlew test --tests "uk.co.ryanharrison.mathengine.distributions.*"

# Run tests matching a pattern
./gradlew test --tests "*Distribution*"
```

**Get structured test feedback with testSummary**

After running tests, ALWAYS use `testSummary` to get clean, parseable output:

```bash
# Run tests and immediately view summary
./gradlew test --tests NormalDistributionTest && ./gradlew testSummary
```

The `testSummary` task outputs:

- Total test counts (passed/failed/errors/skipped)
- List of all failed test names (easy to parse and act on)
- Concise format without grepping through large console output

**Key strategies for efficient AI agent workflows**:

1. **Always filter tests** - Running all tests is slow. Use `--tests` flag to run only relevant tests.

2. **Use testSummary for failures** - Provides structured output showing exactly which tests failed without parsing verbose logs.

3. **Avoid `--info` or `--debug`** - These produce massive output that's hard to parse. Standard output plus `testSummary` is
   sufficient.

4. **Test reports location** - If you need detailed stack traces, read from:
    - XML reports: `build/test-results/test/*.xml` (machine-readable)
    - HTML report: `build/reports/tests/test/index.html` (can be read with Read tool)

## Tools

**Java Version**: Java 25 (using toolchain)

**Dependencies**:

- JUnit Jupiter 5 (testing via BOM)
- AssertJ (test assertions)

## GUI Applications

The project includes runnable GUI applications with `main` methods:

1. **MainFrame** (`uk.co.ryanharrison.mathengine.gui.MainFrame`) - Interactive expression evaluator with graphical interface
2. **Converter** (`uk.co.ryanharrison.mathengine.gui.Converter`) - Unit conversion tool with GUI
3. **Grapher** (`uk.co.ryanharrison.mathengine.plotting.Grapher`) - Function plotter with pan/zoom

## Core Architecture

### Function Class - Single-Variable Equations

The `Function` class (`uk.co.ryanharrison.mathengine.core.Function`) is the primary interface for packages like differential,
integral,
solvers, and plotting.

**Usage Pattern**:

```java
Function f = new Function("x^2 + 8*x + 12");
double result = f.evaluateAt(3.5);
Node tree = f.getCompiledExpression(); // Cached parse tree
```

**Constructor Overloads**:

- `Function(String equation)` - Defaults to variable "x", Radians
- `Function(String equation, AngleUnit angleUnit)`
- `Function(String equation, String variable)`
- `Function(String equation, String variable, AngleUnit angleUnit)`

Internally uses `Evaluator.newSimpleEvaluator()` with lazy initialization. The expression tree is cached for performance.

### Package Organization

**parser/** - Expression parser and evaluator (CORE)

- **Entry point**: `MathEngine.create()` or `MathEngine.builder()...build()`
- **Pipeline**: Text → Lexer → Parser → Evaluator → Result
- **Lexer**: Two-stage tokenization (TokenScanner → TokenProcessor)
  - Conservative identifier splitting (only constants/functions, NOT units)
- **Parser**: Recursive descent with precedence climbing
  - Builds AST from tokens
  - 30+ node types (NodeVariable, NodeUnitRef, NodeBinary, NodeVector, etc.)
- **Evaluator**: Context-aware evaluation with specialized handlers
  - `VariableResolver`: Context-dependent resolution (variable → function → unit)
- **Operator System**: Extensible binary/unary operators with broadcasting
- **Function System**: 100+ built-in functions organized by category
- See `docs/parser/` for detailed architecture documentation

**differential/** - Differentiation

- Numeric methods: `DividedDifferenceMethod`, `ExtendedCentralDifferenceMethod`, `RichardsonExtrapolationMethod`
- Symbolic: `Differentiator` (returns new `Function` representing derivative)

**integral/** - Numerical integration

- Implementations: `TrapeziumIntegrator`, `SimpsonIntegrator`, `RectangularIntegrator`
- All extend `IntegrationMethod` base class

**solvers/** - Root finding algorithms

- Implementations: `BrentSolver`, `BisectionSolver`, `NewtonRaphsonSolver`, `NewtonBisectionSolver`
- Methods: `solve()` finds one root, `solveAll()` finds all roots in bounds
- Configure via `setUpperBound()`, `setLowerBound()`, `setIterations()`, `setConvergenceCriteria()`

**linearalgebra/** - Vectors and Matrices

- `Vector` and `Matrix` classes with arithmetic operations
- `LUDecomposition`, `QRDecomposition` for advanced operations
- Parser integrates via `NodeVector` / `NodeMatrix`

**distributions/** - Probability distributions

- Base classes: `DiscreteProbabilityDistribution`, `ContinuousProbabilityDistribution`
- Implementations: Normal, Beta, Binomial, Exponential, F, Logistic, Student T
- Methods: `.density()`, `.cumulative()`

**unitconversion/** - Unit conversion engine

- `ConversionEngine` with flexible string-based unit matching
- Supports currencies (`.updateCurrencies()` for live rates)
- Supports timezones (`.updateTimeZones()` for city-based conversion)
- Unit definitions stored in external XML/JSON files

**special/** - Special mathematical functions

- `Gamma`, `Beta`, `Erf` (error function)
- `Primes`: `isPrime()`, `nextPrime()`, `primeFactors()`

**regression/** - Regression models

- `LinearRegressionModel` and base `RegressionModel`
- Returns best-fit functions from sample data

**plotting/** - Graphical function plotting

- `Grapher` provides interactive pan/zoom graphing control

**gui/** - Graphical interfaces

- `MainFrame`: Expression evaluator interface
- `Converter`: Unit conversion GUI
- `HistoricalTextField`: Reusable text field with arrow-key history

## Important Implementation Notes

### Test Organization

Tests mirror the main source structure:

- `src/test/java/uk/co/ryanharrison/mathengine/...`
- Package tests: `differential/`, `unitconversion/`, `regression/`, etc.

Use AssertJ for assertions (`assertThat(...).isEqualTo(...)`) rather than JUnit assertions.

## Code Quality Standards & Refactoring Guidelines

This section documents the coding standards and refactoring patterns established during the distributions package refactoring.
Apply these principles when refactoring other packages.

### Immutability & Modern Java Practices

**Prefer Immutability**:

- All fields should be `private final`
- No setter methods - use builders or factory methods for construction
- Validate parameters in constructors/factory methods and throw `IllegalArgumentException` for invalid inputs
- Pre-compute expensive calculations in constructors (e.g., logarithms, normalization factors)

**Factory Methods vs Constructors**:

```java
// GOOD: Static factory methods with descriptive names
public static NormalDistribution standard() {
    return new NormalDistribution(0.0, 1.0);
}

public static NormalDistribution of(double mean, double standardDeviation) {
    if (standardDeviation <= 0.0) {
        throw new IllegalArgumentException("Standard deviation must be positive, got: " + standardDeviation);
    }
    return new NormalDistribution(mean, standardDeviation);
}

// BAD: Public constructors with unclear parameter order
public NormalDistribution(double a, double b) { ...}
```

**Builder Pattern**:

- Use builders for classes with 3+ parameters or optional configurations, otherwise for pure functions keep them static
- Builders enable named parameters and default values
- Validate in builder methods (not just in `build()`)

```java
public static Builder builder() {
    return new Builder();
}

public static final class Builder {
    private double mean = 0.0;  // Sensible defaults
    private double standardDeviation = 1.0;

    public Builder mean(double mean) {
        this.mean = mean;
        return this;
    }

    public Builder standardDeviation(double standardDeviation) {
        if (standardDeviation <= 0.0) {
            throw new IllegalArgumentException("Must be positive, got: " + standardDeviation);
        }
        this.standardDeviation = standardDeviation;
        return this;
    }

    public NormalDistribution build() {
        return new NormalDistribution(mean, standardDeviation);
    }
}
```

**Interfaces Over Abstract Classes**:

- Prefer interfaces for contracts/type hierarchies
- Use `default` methods sparingly - only for utility methods
- Abstract classes are acceptable only when sharing implementation code

```java
// GOOD: Interface defining contract
public interface ContinuousProbabilityDistribution extends ProbabilityDistribution {
    double density(double x);

    double cumulative(double x);

    double inverseCumulative(double p);
}

// GOOD: Immutable implementation
public final class NormalDistribution implements ContinuousProbabilityDistribution {
    private final double mean;
    private final double standardDeviation;
    // ...
}
```

### Comprehensive Javadoc Standards

**Class-Level Documentation**:

- Start with brief description of what the class represents
- Include mathematical formulas and definitions
- Provide parameter descriptions (what they represent mathematically)
- Include usage examples in `<pre>{@code ...}</pre>` blocks
- List key properties (mean, variance formulas)

```java
/**
 * Immutable implementation of the Normal (Gaussian) {@link ContinuousProbabilityDistribution}.
 * <p>
 * The normal distribution is a continuous probability distribution characterized by
 * its bell-shaped curve. It is defined by two parameters:
 * </p>
 * <ul>
 *     <li><b>μ (mu)</b>: the mean, which determines the center of the distribution</li>
 *     <li><b>σ (sigma)</b>: the standard deviation, which determines the spread</li>
 * </ul>
 * <p>
 * The probability density function is:
 * <br>
 * f(x) = (1 / (σ√(2π))) * exp(-((x-μ)² / (2σ²)))
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Standard normal distribution (mean=0, stddev=1)
 * NormalDistribution standard = NormalDistribution.standard();
 *
 * // Custom distribution using builder
 * NormalDistribution custom = NormalDistribution.builder()
 *     .mean(100)
 *     .standardDeviation(15)
 *     .build();
 * }</pre>
 *
 */
```

**Method-Level Documentation**:

- Document what the method computes/returns
- Specify valid input ranges and what happens for invalid inputs
- Use `@throws` tags for all exception cases
- Reference related methods with `{@link}`

```java
/**
 * Calculates the cumulative distribution function (CDF) at the given point.
 * <p>
 * The CDF represents the probability that the random variable is less than or
 * equal to {@code x}. Mathematically, this is the integral of the PDF from
 * negative infinity to {@code x}.
 * </p>
 *
 * @param x the upper bound of the probability calculation
 * @return the probability that a random variable is less than or equal to {@code x},
 *         a value in the range [0, 1]
 * @throws IllegalArgumentException if {@code x} is outside the valid range for this distribution
 */
public double cumulative(double x);
```

### Comprehensive Test Suite Design

**Test Organization**:

- Group tests by functionality using comments: `// ==================== Section ====================`
- Order: Construction → Properties → Density → Cumulative → Inverse → Equality → Edge Cases → Immutability

**Parameterized Tests**:

- Use `@ParameterizedTest` with `@CsvSource` for testing multiple values
- Prefer parameterized tests over copy-pasted test methods
- Include descriptive values in CSV (actual parameter names as comments)

```java

@ParameterizedTest
@CsvSource({
        "0.0, 0.5",                 // mean, expected cumulative at mean
        "10.0, 0.5",
        "-5.0, 0.5"
})
void cumulativeAtMeanIsHalf(double mean, double expected) {
    NormalDistribution dist = NormalDistribution.of(mean, 1.0);
    assertThat(dist.cumulative(mean)).isCloseTo(expected, within(TOLERANCE));
}
```

**Test Data & Tolerances**:

- Define tolerance constants at class level
- Use strict tolerance (1e-9) for exact calculations
- Use relaxed tolerance (1e-6 or 1e-7) for approximations (Gamma, Erf, etc.)
- Update expected values to match actual implementation (don't force incorrect values)

```java
private static final double TOLERANCE = 1e-7;          // For Gamma/Erf approximations
private static final double RELAXED_TOLERANCE = 1e-6;  // For iterative algorithms
```

**Multiple Assertions Per Test**:

- For related properties, test multiple values in one test method
- This prevents test explosion while maintaining clarity
- Use loops for monotonicity checks

```java

@Test
void cumulativeIsMonotonicallyIncreasing() {
    NormalDistribution dist = NormalDistribution.of(5.0, 2.0);

    double prev = 0.0;
    for (double x = -10.0; x <= 20.0; x += 0.5) {
        double current = dist.cumulative(x);
        assertThat(current).isGreaterThanOrEqualTo(prev);
        prev = current;
    }
}
```

**Edge Cases & Validation**:

- Test boundary conditions (0, 1, min, max values)
- Test invalid inputs throw appropriate exceptions
- Test extreme values don't produce NaN/Infinity (unless mathematically correct)
- Test immutability by calling methods and verifying state unchanged

```java

@Test
void densityRejectsNegativeX() {
    ExponentialDistribution dist = ExponentialDistribution.of(1.0);

    assertThatThrownBy(() -> dist.density(-0.1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("non-negative");
}

@Test
void distributionIsImmutable() {
    NormalDistribution dist = NormalDistribution.of(5.0, 2.0);

    double originalMean = dist.getMean();

    // Perform operations
    dist.density(10.0);
    dist.cumulative(10.0);

    // Verify state unchanged
    assertThat(dist.getMean()).isEqualTo(originalMean);
}
```

**Test Coverage Goals**:

- All public methods must have tests
- All error conditions must be tested
- Statistical properties should be verified (not just implementation details)
- Test both typical and edge cases

**AssertJ Usage**:

- Always use AssertJ over JUnit assertions
- Use descriptive assertions: `isCloseTo()`, `isEqualTo()`, `isGreaterThan()`
- Use `within()` for floating-point comparisons
- Use `assertThatThrownBy()` for exception testing

### Code Structure & Naming

**Method Naming**:

- Getters: `getMean()`, `getStandardDeviation()`, `getVariance()`
- Factory methods: `of()`, `standard()`, `withMean()`, `builder()`
- Avoid abbreviations: `standardDeviation` not `stdDev`

**Field Naming**:

- Use full mathematical names: `mean`, `standardDeviation`, `degreesOfFreedom`
- Pre-computed values: prefix with purpose: `logNormalizationFactor`, `halfDegreesPlus1`

**Constant Naming**:

- Mathematical constants: `ONE_OVER_SQRT_2PI`, `PI_OVER_SQRT3`
- Always include documentation explaining the value

**Error Messages**:

- Include parameter name and actual value in error messages
- Be specific about what's wrong and what's expected

```java
if (standardDeviation <= 0.0) {
    throw new IllegalArgumentException(
        "Standard deviation must be positive, got: " + standardDeviation);
}
```
