# Parser Package Overview

**Purpose:** Complete expression parser and evaluator for mathematical expressions with rich type support

---

## Architecture Overview

Parser is a three-stage pipeline that transforms input text into evaluated results:

```
Input String
    ↓
[LEXER] → Tokens (List<Token>)
    ↓
[PARSER] → AST (Node tree)
    ↓
[EVALUATOR] → Result (NodeConstant)
```

## Core Components

### 1. MathEngine (Entry Point)

**Location:** `MathEngine.java`

The main entry point and configuration hub. Creates and coordinates all subsystems.

**Key Responsibilities:**

- Configuration management via `MathEngineConfig`
- Registry initialization (operators, functions, units, constants, keywords)
- Component lifecycle management
- Session state (variables, user-defined functions)

**Usage:**

```java
// Simple evaluation
MathEngine engine = MathEngine.create();
NodeConstant result = engine.evaluate("2 + 3 * 4");  // 14

// With configuration
MathEngine engine = MathEngine.builder()
    .angleUnit(AngleUnit.DEGREES)
    .build();

// Compiled expressions (for repeated evaluation)
CompiledExpression expr = engine.compile("x^2 + 2*x + 1");
double result = expr.evaluateDouble("x", 5.0);
```

### 2. Lexer (Tokenization)

**Location:** `lexer/` package

Multi-pass pipeline that converts text into classified tokens.

**Pipeline:**

1. **TokenScanner** - Character-level scanning (numbers, strings, operators, identifiers)
2. **IdentifierSplitter** - Split compound identifiers (e.g., `pi2e` → `pi`, `2`, `e`)
3. **TokenClassifier** - Classify identifiers (keywords, units, functions, constants)
4. **ImplicitMultiplicationInserter** - Insert multiplication tokens (e.g., `2x` → `2 * x`)

**Key Features:**

- Decimal vs range disambiguation (`1.5` vs `1..5`)
- Position tracking for error reporting
- Multi-character operator support (`==`, `!=`, `<=`, `>=`, `&&`, `||`, `!!`, `..`, `:=`, `->`)

### 3. Parser (AST Construction)

**Location:** `parser/` package

Recursive descent parser with precedence chain.

**Components:**

- **Parser** - Main coordinator
- **TokenStream** - Token navigation with lookahead and savepoints
- **PrecedenceParser** - Expression parsing with proper precedence
- **CollectionParser** - Vectors, matrices, comprehensions

**Precedence Chain** (lowest to highest):

```
Expression → Assignment → Lambda → LogicalOr → LogicalXor → LogicalAnd →
Equality → Range → Relational → Additive → Multiplicative → Unary →
Power → Postfix → Call/Subscript → Primary
```

**Special Handling:**

- Right-associative power operator (`^`)
- Function definition lookahead
- Comprehension syntax
- Slice arguments for subscripts

### 4. Evaluator (Execution)

**Location:** `evaluator/` package

Evaluates AST nodes to produce results.

**Components:**

- **Evaluator** - Main evaluation coordinator
- **EvaluationContext** - Variable/function storage, settings
- **OperatorExecutor** - Operator dispatch system
- **FunctionExecutor** - Built-in function dispatch system

**Specialized Handlers:**

- **VariableResolver** - Variable resolution and implicit multiplication fallback
- **SubscriptHandler** - Vector/matrix indexing and slicing
- **FunctionCallHandler** - Function calls (built-in, user-defined, lambda)
- **ComprehensionHandler** - List comprehension evaluation

**Features:**

- Exact rational arithmetic by default
- Type promotion (Rational → Double when needed)
- Short-circuit evaluation (`&&`, `||`)
- Lazy evaluation (conditional `if` function)
- Recursion tracking with configurable depth limit

---

## Type System

### Node Hierarchy

```
Node (abstract AST base)
├─ NodeConstant (evaluated values)
│   ├─ NodeNumber
│   │   ├─ NodeDouble (IEEE 754)
│   │   ├─ NodeRational (BigRational, exact)
│   │   ├─ NodePercent (auto /100)
│   │   └─ NodeBoolean (true=1, false=0)
│   ├─ NodeString
│   ├─ NodeUnit (value + unit descriptor)
│   ├─ NodeVector (1D array)
│   ├─ NodeMatrix (2D array)
│   ├─ NodeRange (lazy start..end..step)
│   ├─ NodeFunction (user-defined)
│   └─ NodeLambda (anonymous function)
└─ NodeExpression (unevaluated AST)
    ├─ NodeBinary (binary operations)
    ├─ NodeUnary (unary operations)
    ├─ NodeCall (function calls)
    ├─ NodeSubscript (indexing/slicing)
    ├─ NodeVariable (variable reference)
    ├─ NodeAssignment (variable assignment)
    ├─ NodeFunctionDef (function definition)
    ├─ NodeRangeExpression (range before evaluation)
    ├─ NodeUnitConversion (unit conversion)
    ├─ NodeComprehension (list comprehension)
    └─ NodeSequence (multiple statements)
```

### Type Coercion Rules

**Numeric Promotion:**

```
Boolean → Integer → Rational → Double → Percent
```

**Broadcasting:**

- Scalar broadcasts to Vector/Matrix
- Single-element Vector broadcasts to larger Vector
- Vectors broadcast to Matrices when dimensions align

---

## Operator System

### Architecture

**Location:** `operator/` package

Extensible operator system with registration and dispatch.

**Key Components:**

- **Operator** - Base interface for all operators
- **BinaryOperator** - Two-operand operations
- **UnaryOperator** - Single-operand operations
- **OperatorExecutor** - Central registry and dispatcher
- **BroadcastingDispatcher** - Handles vector/matrix broadcasting

### Operator Registration

Operators are registered by `TokenType` (not by string symbol):

```java
OperatorExecutor executor = new OperatorExecutor();
executor.registerBinary(TokenType.PLUS, new PlusOperator());
executor.registerUnary(TokenType.MINUS, new NegateOperator());
```

This allows:

- Multiple symbols for same operator (e.g., `and` and `&&`)
- Dynamic operator definitions without hardcoding
- Easy extension with custom operators

### Standard Operators

**Binary:**

- Arithmetic: `+`, `-`, `*`, `/`, `^`, `mod`
- Comparison: `<`, `>`, `<=`, `>=`, `==`, `!=`
- Logical: `&&` (and), `||` (or), `xor`
- Special: `@` (matrix multiply), `of` (percentage)

**Unary:**

- Arithmetic: `-` (negate), `+` (no-op)
- Postfix: `!` (factorial), `!!` (double factorial), `%` (percent)
- Logical: `not`

---

## Function System

### Architecture

**Location:** `function/` package

Extensible function system with multiple function types.

**Key Components:**

- **MathFunction** - Base interface for all functions
- **FunctionExecutor** - Central registry and dispatcher
- **FunctionContext** - Provides evaluator context to functions

### Function Types

**1. UnaryFunction / DoubleUnaryFunctionWrapper**

- Single numeric argument
- Example: `sin`, `cos`, `abs`, `ln`

**2. BinaryFunction / BinaryFunctionWrapper**

- Two numeric arguments
- Example: `max`, `min`, `pow`, `gcd`, `lcm`

**3. AggregateFunction / AggregateFunctionWrapper**

- Variable number of arguments (typically operates on vectors)
- Example: `sum`, `product`, `mean`, `median`

**4. TrigFunction**

- Trigonometric functions with angle unit awareness
- Example: `sin`, `cos`, `tan`, `asin`, `acos`, `atan`

**5. Higher-Order Functions**

- Take functions as arguments
- Example: `map`, `filter`, `reduce`, `fold`

### Function Categories

**Math Functions:**

- `function/math/` - Exponential, rounding, utility functions

**Trig Functions:**

- `function/trig/` - Trigonometric and hyperbolic functions

**Vector Functions:**

- `function/vector/` - Vector operations, statistics, manipulation

**Special Functions:**

- `function/special/` - Conditionals, bitwise, number theory, type functions

**String Functions:**

- `function/string/` - String manipulation

---

## Registry System

### Overview

**Location:** `registry/` package

Registries provide lookup services for the lexer and evaluator.

### Registry Types

**1. FunctionRegistry**

- **Purpose:** Tell lexer which identifiers are functions
- **Used by:** TokenClassifier during lexical analysis
- **Content:** Function names and aliases

**2. UnitRegistry**

- **Purpose:** Unit definitions for conversion
- **Used by:** TokenClassifier (lexer) and unit conversion evaluation
- **Content:** `UnitDefinition` objects with conversion factors

**3. ConstantRegistry**

- **Purpose:** Predefined mathematical constants
- **Used by:** TokenClassifier (lexer) and context initialization
- **Content:** `ConstantDefinition` with name, value, and aliases

**4. KeywordRegistry**

- **Purpose:** Reserved keywords (and, or, not, for, in, if, etc.)
- **Used by:** TokenClassifier during lexical analysis
- **Content:** Set of keyword strings

---

## Configuration

### MathEngineConfig

**Location:** `MathEngineConfig.java`

Immutable configuration object with builder pattern.

**Categories:**

**1. Evaluation Settings**

```java
.angleUnit(AngleUnit.RADIANS)           // or DEGREES
.maxRecursionDepth(1000)                // Stack overflow protection
.maxExpressionDepth(1000)               // Parse depth limit
.forceDoubleArithmetic(false)           // Disable exact rationals
```

**2. Size Limits**

```java
.maxVectorSize(1_000_000)
.maxMatrixDimension(10_000)
.maxIdentifierLength(256)
```

**3. Feature Toggles**

```java
.implicitMultiplication(true)           // 2x → 2 * x
.vectorsEnabled(true)
.matricesEnabled(true)
.unitsEnabled(true)
.comprehensionsEnabled(true)
.lambdasEnabled(true)
.userDefinedFunctionsEnabled(true)
```

**4. Component Registration**

```java
.constantRegistry(ConstantRegistry.standard())
.keywordRegistry(KeywordRegistry.standard())
.functions(StandardFunctions.all())
.binaryOperators(StandardBinaryOperators.all())
.unaryOperators(StandardUnaryOperators.all())
```

---

## Error Handling

### Exception Hierarchy

```
MathEngineException (base)
├─ LexerException (tokenization errors)
├─ ParseException (syntax errors)
├─ EvaluationException
│   ├─ TypeError (type mismatches)
│   ├─ ArityException (wrong number of arguments)
│   ├─ StackOverflowException (recursion limit)
│   └─ UndefinedVariableException (unknown variable)
```

### Error Context

All exceptions include:

- **Token** - Location in source where error occurred
- **Source code** - Full source for context display
- **Error message** - Human-readable description

---

## Testing Infrastructure

### Test Organization

**Location:** `src/test/java/uk/co/ryanharrison/mathengine/parser/`

**Test Classes:**

- `MathEngineTest.java` - End-to-end integration tests
- `lexer/LexerTest.java` - Tokenization tests
- `parser/ParserTest.java` - Parsing tests

**JSON Test Framework:**

- `JsonTestLoader.java` - Loads test suites from JSON
- `JsonTestSuite.java` - Test suite container
- `JsonTestCase.java` - Individual test case
- `TestConfig.java` - Test configuration

**Test Resources:**

- JSON test files organized by feature (when implemented)
- Location: `src/test/resources/`

---

## Key Design Principles

### 1. Separation of Concerns

- **Lexer:** Only tokenizes, doesn't parse
- **Parser:** Only builds AST, doesn't evaluate
- **Evaluator:** Only evaluates, doesn't parse

### 2. Extensibility

- Operators registered by TokenType (not hardcoded)
- Functions registered via MathFunction interface
- Registries allow custom units, constants, keywords

### 3. Type Safety

- Strict type checking with clear coercion rules
- Type promotion only when mathematically sound
- Explicit error messages for type mismatches

### 4. Performance

- Compiled expressions for repeated evaluation
- Lazy evaluation for ranges
- Exact rationals only when needed
- Short-circuit evaluation for logical operators

### 5. Error Reporting

- Position tracking through entire pipeline
- Rich error context (token, source, message)
- Recursion depth tracking with clear stack traces

---

## Quick Reference

### File Counts by Package

```
parser/                         (5 files)    # Core entry points
├── lexer/                       (10 files)   # Tokenization
├── parser/                      (5 files)    # Parsing
│   └── nodes/                   (23 files)   # AST nodes
├── evaluator/                   (8 files)    # Evaluation
│   └── handler/                 (4 files)    # Evaluation handlers
├── operator/                    (8 files)    # Operator system
│   ├── binary/                  (10 files)   # Binary operators
│   └── unary/                   (7 files)    # Unary operators
├── function/                    (11 files)   # Function system
│   ├── math/                    (3 files)    # Math functions
│   ├── trig/                    (2 files)    # Trig functions
│   ├── vector/                  (5 files)    # Vector functions
│   ├── special/                 (5 files)    # Special functions
│   └── string/                  (1 file)     # String functions
├── registry/                    (6 files)    # Registry system
└── util/                        (1 file)     # Utilities
```

**Total:** ~100 Java files

---

## Related Documentation

- **[LEXER.md](./LEXER.md)** - Detailed lexer architecture and pipeline
- **[PARSER.md](./PARSER.md)** - Parser implementation and precedence chain
- **[EVALUATOR.md](./EVALUATOR.md)** - Evaluation system and handlers
- **[OPERATORS.md](./OPERATORS.md)** - Operator system and implementations
- **[FUNCTIONS.md](./FUNCTIONS.md)** - Function system and implementations
- **[NODES.md](./NODES.md)** - AST node types and hierarchy
- **[REGISTRIES.md](./REGISTRIES.md)** - Registry system details
- **[TESTING.md](./TESTING.md)** - Testing infrastructure and patterns
- **[../GRAMMAR.md](../GRAMMAR.md)** - Grammar specification
- **[../GRAMMAR_IMPLEMENTATION_PLAN.md](../GRAMMAR_IMPLEMENTATION_PLAN.md)** - Implementation guide

---

**For AI Agents:** Start with this overview, then dive into specific subsystem docs as needed. Each subsystem doc includes code
examples, common patterns, and extension points.
