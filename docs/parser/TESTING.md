# Testing Infrastructure

**Purpose:** JSON-based test framework for comprehensive parser testing

---

## Overview

The Math Engine uses a **JSON-based testing framework** where test cases are defined declaratively in JSON files and executed by `MathEngineTest.java`. This approach provides:

- **Declarative test definitions** - Tests as data, not code
- **Easy test maintenance** - Update tests without recompiling
- **Comprehensive coverage** - Hundreds of tests organized by feature
- **Configuration flexibility** - Test with different engine settings
- **Clear documentation** - Test files serve as executable specifications

```
src/test/resources/engine/
├── advanced/           # Advanced features (comprehensions, recursion, unit conversion)
├── config/             # Configuration tests (angle units, feature toggles)
├── errors/             # Error handling tests
├── functions/          # Function tests (built-in, user-defined, lambdas)
├── integration/        # Integration tests
├── operators/          # Operator tests (binary, unary, precedence)
├── parser/             # Parser tests (lexer, AST construction)
└── structures/         # Data structure tests (vectors, matrices, ranges)
```

---

## Test Infrastructure Classes

### JsonTestCase

**File:** `src/test/java/uk/co/ryanharrison/mathengine/parser/JsonTestCase.java`

**Purpose:** Represents a single test case from JSON

**Fields:**

```java
record JsonTestCase(
    String id,                    // Unique test identifier
    String input,                 // Expression to evaluate
    Object expected,              // Expected result (number, array, etc.)
    String expectedType,          // Expected node type (NodeDouble, NodeRational, etc.)
    String evaluationOrder,       // Optional: for multi-statement tests
    String notes,                 // Test description/rationale
    Boolean skipTest,             // Skip this test
    Boolean expectError,          // Test should throw error
    String expectedErrorType,     // Expected exception type
    TestConfig config,            // Per-test engine configuration
    Double tolerance              // Floating-point tolerance
)
```

**Example JSON:**

```json
{
  "id": "add_001",
  "input": "2 + 3",
  "expected": 5,
  "expectedType": "NodeRational",
  "notes": "Simple integer addition"
}
```

### JsonTestSuite

**File:** `src/test/java/uk/co/ryanharrison/mathengine/parser/JsonTestSuite.java`

**Purpose:** Container for a collection of related test cases

**Fields:**

```java
record JsonTestSuite(
    String category,              // Test category/name
    String description,           // Suite description
    TestConfig defaultConfig,     // Default config for all tests in suite
    List<JsonTestCase> tests      // Test cases
)
```

**Example JSON:**

```json
{
  "category": "Arithmetic",
  "description": "Basic arithmetic operations",
  "defaultConfig": {
    "implicitMultiplication": true
  },
  "tests": [
    { "id": "add_001", "input": "2 + 3", "expected": 5 }
  ]
}
```

### JsonTestLoader

**File:** `src/test/java/uk/co/ryanharrison/mathengine/parser/JsonTestLoader.java`

**Purpose:** Load test suites from JSON files

**Methods:**

```java
// Load single test suite from classpath
JsonTestSuite loadFromResource(String resourcePath)

// Load single test suite from filesystem
JsonTestSuite loadFromFile(Path path)

// Load all test suites from directory
List<JsonTestSuite> loadAllFromDirectory(Path directory)
```

### TestConfig

**File:** `src/test/java/uk/co/ryanharrison/mathengine/parser/TestConfig.java`

**Purpose:** Per-test or per-suite engine configuration overrides

**Supported Options:**

- `angleUnit`: "RADIANS" | "DEGREES" | "GRADIANS"
- `implicitMultiplication`: boolean
- `vectorsEnabled`: boolean
- `matricesEnabled`: boolean
- `comprehensionsEnabled`: boolean
- `lambdasEnabled`: boolean
- `userDefinedFunctionsEnabled`: boolean
- `unitsEnabled`: boolean
- `maxRecursionDepth`: int
- `decimalPlaces`: int
- `forceDoubleArithmetic`: boolean

**Example:**

```json
{
  "id": "sin_degrees_001",
  "input": "sin(90)",
  "expected": 1.0,
  "expectedType": "NodeDouble",
  "config": {
    "angleUnit": "DEGREES"
  }
}
```

---

## Test File Organization

### Test Categories

Tests are organized by feature/category in `src/test/resources/engine/`:

**advanced/**
- `ambiguity_resolution.json` - Variable/function/unit/constant shadowing and reference symbols
- `comprehensions.json` - List comprehensions
- `functions_definitions.json` - User-defined functions
- `functions_first_class.json` - Functions as first-class values
- `functions_recursive.json` - Recursive functions
- `lambdas.json` - Lambda expressions
- `recursion_edge_cases.json` - Recursion limits and edge cases
- `subscripts.json` - Vector/matrix indexing and slicing
- `unit_conversion.json` - Unit conversion expressions
- `unit_parsing.json` - Unit parsing and disambiguation
- `user_functions.json` - User function features

**config/**
- `angle_units.json` - RADIANS vs DEGREES vs GRADIANS
- `arithmetic_mode.json` - Rational vs floating-point arithmetic
- `decimal_places.json` - Output formatting
- `feature_toggles.json` - Enable/disable features
- `implicit_multiplication.json` - Implicit multiplication behavior
- `recursion_limits.json` - Max recursion depth

**errors/**
- `arithmetic_errors.json` - Division by zero, overflow, etc.
- `domain_errors.json` - Invalid function domains
- `errors_evaluation.json` - Runtime evaluation errors
- `errors_lexer.json` - Tokenization errors
- `errors_parser.json` - Syntax errors
- `errors_type.json` - Type errors

**functions/**
- Tests for built-in functions organized by category

**operators/**
- Tests for binary and unary operators

**structures/**
- `vectors.json` - Vector operations
- `matrices.json` - Matrix operations
- `ranges.json` - Range expressions

---

## Writing JSON Tests

### Basic Test Case

```json
{
  "id": "unique_test_id",
  "input": "2 + 3",
  "expected": 5,
  "expectedType": "NodeRational",
  "notes": "Optional description of what this test verifies"
}
```

### Test with Custom Tolerance

For floating-point comparisons:

```json
{
  "id": "float_test_001",
  "input": "sqrt(2)",
  "expected": 1.41421356,
  "expectedType": "NodeDouble",
  "tolerance": 1e-7,
  "notes": "Floating-point result requires tolerance"
}
```

### Test with Configuration Override

```json
{
  "id": "degrees_test_001",
  "input": "sin(90)",
  "expected": 1.0,
  "expectedType": "NodeDouble",
  "config": {
    "angleUnit": "DEGREES"
  },
  "notes": "Trig function in degree mode"
}
```

### Test Expecting Error

```json
{
  "id": "error_test_001",
  "input": "1 / 0",
  "expectError": true,
  "expectedErrorType": "EvaluationException",
  "notes": "Division by zero should throw"
}
```

### Test with Vector/Matrix Result

```json
{
  "id": "vector_add_001",
  "input": "{1, 2, 3} + {4, 5, 6}",
  "expected": [5, 7, 9],
  "expectedType": "NodeVector",
  "notes": "Element-wise vector addition"
}
```

### Multi-Statement Test

```json
{
  "id": "var_assign_001",
  "input": "x := 10; x + 5",
  "expected": 15,
  "expectedType": "NodeRational",
  "evaluationOrder": "sequential",
  "notes": "Variable assignment followed by use"
}
```

### Skip Test (Temporarily Disabled)

```json
{
  "id": "wip_test_001",
  "input": "some unfinished feature",
  "expected": 42,
  "skipTest": true,
  "notes": "Test disabled while feature is in development"
}
```

---

## Test Suite Structure

```json
{
  "category": "Test Category Name",
  "description": "Detailed description of what this suite tests",
  "defaultConfig": {
    "angleUnit": "RADIANS",
    "implicitMultiplication": true
  },
  "tests": [
    {
      "id": "test_001",
      "input": "expression",
      "expected": "result"
    },
    {
      "id": "test_002",
      "input": "expression",
      "expected": "result",
      "config": {
        "angleUnit": "DEGREES"
      }
    }
  ]
}
```

**Suite-Level Configuration:**

- `defaultConfig` applies to all tests in the suite
- Individual tests can override with their own `config`
- Missing config values use engine defaults

---

## Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests MathEngineTest
```

### Run Tests with Pattern

```bash
# Run all parser tests
./gradlew test --tests "*Parser*"

# Run all distribution tests
./gradlew test --tests "*Distribution*"
```

### View Test Summary

```bash
# Run tests and view summary
./gradlew test testSummary

# Or separately
./gradlew testSummary
```

---

## Test Execution Flow

1. **Load JSON files** - `JsonTestLoader` reads test suites from `src/test/resources/engine/`
2. **Parse test definitions** - Jackson deserializes JSON into `JsonTestSuite` and `JsonTestCase` objects
3. **Configure engine** - For each test, create `MathEngine` with appropriate config
4. **Evaluate expression** - Run `engine.evaluate(testCase.input())`
5. **Verify result** - Compare actual vs expected (type, value, tolerance)
6. **Report failures** - AssertJ provides detailed failure messages

---

## Example: ambiguity_resolution.json

This test suite demonstrates the comprehensive testing approach for reference symbols and shadowing:

```json
{
  "category": "Ambiguity Resolution",
  "description": "Tests for ambiguous identifier resolution based on context and explicit disambiguation",
  "tests": [
    {
      "id": "var_func_coexist_001",
      "input": "f := 100; f(x) := x * 2; f",
      "expected": 100,
      "expectedType": "NodeRational",
      "notes": "Variable and function coexist - GENERAL context accesses variable"
    },
    {
      "id": "var_func_coexist_002",
      "input": "f := 100; f(x) := x * 2; f(5)",
      "expected": 10,
      "expectedType": "NodeRational",
      "notes": "Variable and function coexist - CALL context accesses function"
    },
    {
      "id": "const_shadow_002",
      "input": "pi := 100; #pi",
      "expected": 3.141592653589793,
      "expectedType": "NodeDouble",
      "notes": "Variable shadows constant pi, explicit # accesses constant"
    },
    {
      "id": "explicit_unit_006",
      "input": "m := 5; 100 @m in feet",
      "expectedType": "NodeUnit",
      "notes": "Explicit @ forces meter unit despite variable shadowing"
    }
  ]
}
```

---

## Adding New Tests

### 1. Choose Appropriate File

Select or create a JSON file in the appropriate category:

- **New feature?** → Create new file in `advanced/`
- **Config option?** → Add to appropriate file in `config/`
- **Error case?** → Add to appropriate file in `errors/`
- **Operator?** → Add to appropriate file in `operators/`
- **Function?** → Add to appropriate file in `functions/`

### 2. Write Test Case

```json
{
  "id": "descriptive_id_001",
  "input": "test expression",
  "expected": expectedResult,
  "expectedType": "NodeType",
  "notes": "Clear description of what this tests"
}
```

### 3. Run Tests

```bash
./gradlew test
```

### 4. Verify

Check that your test:
- Passes when it should
- Fails when it should (for error tests)
- Has clear ID and notes

---

## Best Practices

### Test IDs

- **Format**: `category_subcategory_###`
- **Examples**: `add_basic_001`, `var_shadow_unit_003`, `error_div_zero_001`
- **Unique**: Every ID must be unique across all test files

### Test Organization

- **One feature per file** - Don't mix unrelated tests
- **Logical ordering** - Simple cases first, complex cases last
- **Group related tests** - Use consistent ID prefixes

### Expected Values

- **Numbers**: Use appropriate precision
  - Integers: `5`, `100`, `-7`
  - Doubles: `3.14159`, `2.718281828`
  - Use `tolerance` for floating-point comparisons
- **Vectors**: `[1, 2, 3]`
- **Matrices**: `[[1, 2], [3, 4]]`
- **Booleans**: `true`, `false`

### Notes

- **Always include notes** - Explain what the test verifies
- **Reference bugs** - Include issue numbers if applicable
- **Explain edge cases** - Why is this case important?

### Configuration

- **Suite defaults** - Use `defaultConfig` for suite-wide settings
- **Per-test overrides** - Use test-level `config` only when needed
- **Minimal config** - Only specify what differs from defaults

---

## Common Test Patterns

### Operator Precedence

```json
{
  "id": "precedence_001",
  "input": "2 + 3 * 4",
  "expected": 14,
  "expectedType": "NodeRational",
  "notes": "Multiplication before addition"
}
```

### Shadowing

```json
{
  "id": "shadow_var_const_001",
  "input": "pi := 100; pi",
  "expected": 100,
  "notes": "Variable shadows constant"
},
{
  "id": "shadow_explicit_const_001",
  "input": "pi := 100; #pi",
  "expected": 3.141592653589793,
  "notes": "Explicit # accesses original constant"
}
```

### Error Testing

```json
{
  "id": "error_undefined_var_001",
  "input": "undefinedVariable",
  "expectError": true,
  "expectedErrorType": "UndefinedVariableException",
  "notes": "Reference to undefined variable should fail"
}
```

### Feature Toggle Testing

```json
{
  "id": "feature_disabled_001",
  "input": "{1, 2, 3}",
  "expectError": true,
  "config": {
    "vectorsEnabled": false
  },
  "notes": "Vectors disabled - should error"
}
```

---

## Test Utilities (Java)

While tests are defined in JSON, test execution uses standard JUnit 5 and AssertJ:

```java
@Test
void runJsonTests() throws IOException {
    JsonTestSuite suite = JsonTestLoader.loadFromResource("/engine/advanced/ambiguity_resolution.json");

    for (JsonTestCase test : suite.tests()) {
        if (test.shouldSkip()) continue;

        MathEngine engine = createEngineWithConfig(test.config());

        if (test.shouldExpectError()) {
            assertThatThrownBy(() -> engine.evaluate(test.input()))
                .hasMessageContaining(test.expectedErrorType());
        } else {
            NodeConstant result = engine.evaluate(test.input());
            assertResultMatches(result, test);
        }
    }
}
```

---

## Related Documentation

- **[GRAMMAR_TESTS.md](../GRAMMAR_TESTS.md)** - JSON test format specification
- **[OVERVIEW.md](./OVERVIEW.md)** - Parser architecture
- **[EVALUATOR.md](./EVALUATOR.md)** - Evaluation system
