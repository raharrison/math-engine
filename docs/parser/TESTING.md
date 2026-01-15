# Testing Infrastructure

**Purpose:** Test framework and strategies for parser package

---

## Test Organization

```
src/test/java/uk/co/ryanharrison/mathengine/parser/
├── MathEngineTest.java              # End-to-end integration tests
├── lexer/
│   └── LexerTest.java               # Lexer pipeline tests
├── parser/
│   └── ParserTest.java              # Parser tests
├── JsonTestLoader.java              # JSON test suite loader
├── JsonTestSuite.java               # Test suite container
├── JsonTestCase.java                # Individual test case
└── TestConfig.java                  # Test configuration

src/test/resources/
└── (JSON test files - when implemented)
```

---

## Test Categories

### 1. Unit Tests

Test individual components in isolation.

**Lexer Components:**

```java
@Test
void tokenScannerNumbers() {
    TokenScanner scanner = new TokenScanner();
    List<Token> tokens = scanner.scan("3.14");

    assertThat(tokens).hasSize(2);  // DECIMAL + EOF
    assertThat(tokens.get(0).getType()).isEqualTo(TokenType.DECIMAL);
    assertThat(tokens.get(0).getValue()).isEqualTo(3.14);
}
```

**Operator Tests:**

```java
@Test
void plusOperator() {
    PlusOperator op = new PlusOperator();
    NodeConstant result = op.execute(
        new NodeDouble(2),
        new NodeDouble(3),
        context
    );
    assertThat(result.doubleValue()).isEqualTo(5.0);
}
```

**Node Tests:**

```java
@Test
void nodeRational() {
    NodeRational r = new NodeRational(new BigRational(22, 7));
    assertThat(r.doubleValue()).isCloseTo(3.142857, within(0.0001));
    assertThat(r.toString()).contains("22/7");
}
```

### 2. Integration Tests

Test multiple components working together.

**Full Pipeline:**

```java
@Test
void expressionEvaluation() {
    MathEngine engine = MathEngine.create();
    NodeConstant result = engine.evaluate("2 + 3 * 4");
    assertThat(result.doubleValue()).isEqualTo(14.0);
}
```

**Feature Tests:**

```java
@Test
void vectorArithmetic() {
    MathEngine engine = MathEngine.create();
    NodeConstant result = engine.evaluate("{1, 2, 3} + {4, 5, 6}");

    assertThat(result).isInstanceOf(NodeVector.class);
    NodeVector vec = (NodeVector) result;
    assertThat(vec.getElements()[0].doubleValue()).isEqualTo(5.0);
}
```

### 3. JSON-Based Tests

Data-driven tests from JSON files (future enhancement).

**Test File Format:**

```json
{
  "suite": "Arithmetic",
  "tests": [
    {
      "input": "2 + 3",
      "expected": 5,
      "type": "number"
    },
    {
      "input": "2 * 3 + 4",
      "expected": 10,
      "type": "number"
    }
  ]
}
```

**Test Execution:**

```java
@Test
void arithmeticSuite() {
    JsonTestSuite suite = JsonTestLoader.load("arithmetic.json");
    MathEngine engine = MathEngine.create();

    for (JsonTestCase test : suite.getTests()) {
        NodeConstant result = engine.evaluate(test.getInput());
        assertThat(result).matches(test.getExpected());
    }
}
```

---

## Testing Strategies by Component

### Lexer Testing

**1. Token Scanner:**

```java
@ParameterizedTest
@CsvSource({
    "42, INTEGER, 42",
    "3.14, DECIMAL, 3.14",
    "1e3, DECIMAL, 1000.0",
    "22/7, RATIONAL, null"
})
void scanNumbers(String input, TokenType expectedType, String expectedValue) {
    List<Token> tokens = scanner.scan(input);
    assertThat(tokens.get(0).getType()).isEqualTo(expectedType);
}
```

**2. Identifier Splitter:**

```java
@Test
void splitCompoundIdentifier() {
    List<Token> input = List.of(token(IDENTIFIER, "pi2e"));
    List<Token> output = splitter.split(input);

    assertThat(output).extracting(Token::getLexeme)
        .containsExactly("pi", "2", "e");
}
```

**3. Token Classifier:**

```java
@Test
void classifyFunction() {
    List<Token> input = List.of(token(IDENTIFIER, "sin"));
    List<Token> output = classifier.classify(input);

    assertThat(output.get(0).getType()).isEqualTo(TokenType.FUNCTION);
}
```

**4. Implicit Multiplication:**

```java
@Test
void insertMultiplication() {
    List<Token> input = List.of(
        token(INTEGER, "2"),
        token(IDENTIFIER, "x")
    );
    List<Token> output = inserter.insert(input);

    assertThat(output).hasSize(3);
    assertThat(output.get(1).getType()).isEqualTo(TokenType.MULTIPLY);
}
```

### Parser Testing

**1. Precedence:**

```java
@Test
void precedence() {
    // 2 + 3 * 4 should parse as 2 + (3 * 4)
    Node ast = parser.parse("2 + 3 * 4");

    assertThat(ast).isInstanceOf(NodeBinary.class);
    NodeBinary root = (NodeBinary) ast;
    assertThat(root.getOperator().getType()).isEqualTo(TokenType.PLUS);
    assertThat(root.getRight()).isInstanceOf(NodeBinary.class);
}
```

**2. Associativity:**

```java
@Test
void rightAssociativePower() {
    // 2^3^2 should parse as 2^(3^2)
    Node ast = parser.parse("2^3^2");

    NodeBinary root = (NodeBinary) ast;
    assertThat(root.getRight()).isInstanceOf(NodeBinary.class);
}
```

**3. Collections:**

```java
@Test
void vectorParsing() {
    Node ast = parser.parse("{1, 2, 3}");

    assertThat(ast).isInstanceOf(NodeVector.class);
    NodeVector vec = (NodeVector) ast;
    assertThat(vec.size()).isEqualTo(3);
}
```

### Evaluator Testing

**1. Arithmetic:**

```java
@ParameterizedTest
@CsvSource({
    "2 + 3, 5",
    "2 - 3, -1",
    "2 * 3, 6",
    "6 / 3, 2",
    "2 ^ 3, 8"
})
void arithmetic(String expr, double expected) {
    MathEngine engine = MathEngine.create();
    double result = engine.evaluateDouble(expr);
    assertThat(result).isEqualTo(expected);
}
```

**2. Type System:**

```java
@Test
void rationalArithmetic() {
    MathEngine engine = MathEngine.create();
    NodeConstant result = engine.evaluate("1/2 + 1/3");

    assertThat(result).isInstanceOf(NodeRational.class);
    NodeRational r = (NodeRational) result;
    assertThat(r.toString()).isEqualTo("5/6");
}
```

**3. Variables:**

```java
@Test
void variableAssignment() {
    MathEngine engine = MathEngine.create();
    engine.evaluate("x := 10");
    NodeConstant result = engine.evaluate("x + 5");

    assertThat(result.doubleValue()).isEqualTo(15.0);
}
```

**4. Functions:**

```java
@Test
void userDefinedFunction() {
    MathEngine engine = MathEngine.create();
    engine.evaluate("square(x) := x^2");
    NodeConstant result = engine.evaluate("square(5)");

    assertThat(result.doubleValue()).isEqualTo(25.0);
}
```

**5. Recursion:**

```java
@Test
void recursiveFunction() {
    MathEngine engine = MathEngine.create();
    engine.evaluate("fib(n) := if(n <= 1, n, fib(n-1) + fib(n-2))");
    NodeConstant result = engine.evaluate("fib(10)");

    assertThat(result.doubleValue()).isEqualTo(55.0);
}
```

---

## Error Testing

### Syntax Errors

```java
@Test
void syntaxError() {
    MathEngine engine = MathEngine.create();

    assertThatThrownBy(() -> engine.evaluate("2 +"))
        .isInstanceOf(ParseException.class)
        .hasMessageContaining("Unexpected");
}
```

### Type Errors

```java
@Test
void typeError() {
    MathEngine engine = MathEngine.create();

    assertThatThrownBy(() -> engine.evaluate("\"hello\" + 5"))
        .isInstanceOf(TypeError.class);
}
```

### Runtime Errors

```java
@Test
void divisionByZero() {
    MathEngine engine = MathEngine.create();

    assertThatThrownBy(() -> engine.evaluate("5 / 0"))
        .isInstanceOf(EvaluationException.class)
        .hasMessageContaining("division by zero");
}
```

### Stack Overflow

```java
@Test
void recursionLimit() {
    MathEngine engine = MathEngine.builder()
        .config(MathEngineConfig.builder()
            .maxRecursionDepth(100)
            .build())
        .build();

    engine.evaluate("f(x) := f(x+1)");

    assertThatThrownBy(() -> engine.evaluate("f(1)"))
        .isInstanceOf(StackOverflowException.class);
}
```

---

## Test Utilities

### Token Creation Helpers

```java
private Token token(TokenType type, String lexeme) {
    return new Token(type, lexeme, null, 1, 1, 0);
}

private Token token(TokenType type, String lexeme, Object value) {
    return new Token(type, lexeme, value, 1, 1, 0);
}
```

### Node Creation Helpers

```java
private NodeDouble num(double value) {
    return new NodeDouble(value);
}

private NodeVector vec(double... values) {
    Node[] nodes = Arrays.stream(values)
        .mapToObj(NodeDouble::new)
        .toArray(Node[]::new);
    return new NodeVector(nodes);
}
```

### Assertion Helpers

```java
private void assertVector(NodeConstant result, double... expected) {
    assertThat(result).isInstanceOf(NodeVector.class);
    NodeVector vec = (NodeVector) result;

    for (int i = 0; i < expected.length; i++) {
        assertThat(vec.getElements()[i].doubleValue())
            .isEqualTo(expected[i]);
    }
}
```

---

## Parameterized Tests

### Multiple Inputs

```java
@ParameterizedTest
@CsvSource({
    "sin(0), 0",
    "sin(pi/2), 1",
    "cos(0), 1",
    "cos(pi), -1"
})
void trigFunctions(String expr, double expected) {
    MathEngine engine = MathEngine.create();
    double result = engine.evaluateDouble(expr);
    assertThat(result).isCloseTo(expected, within(1e-10));
}
```

### Multiple Test Cases

```java
@ParameterizedTest
@MethodSource("vectorOperations")
void vectorArithmetic(String expr, double[] expected) {
    MathEngine engine = MathEngine.create();
    NodeConstant result = engine.evaluate(expr);
    assertVector(result, expected);
}

static Stream<Arguments> vectorOperations() {
    return Stream.of(
        Arguments.of("{1, 2} + {3, 4}", new double[]{4, 6}),
        Arguments.of("{1, 2, 3} * 2", new double[]{2, 4, 6}),
        Arguments.of("{2, 4, 6} / 2", new double[]{1, 2, 3})
    );
}
```

---

## Test Coverage Goals

### By Component

**Lexer:** 90%+ coverage

- All token types
- All special cases (decimal vs range, etc.)
- Edge cases (empty strings, invalid characters)

**Parser:** 85%+ coverage

- All precedence levels
- All node types
- Error recovery

**Evaluator:** 90%+ coverage

- All operators
- All node evaluations
- Type coercion
- Broadcasting

**Functions:** 80%+ coverage

- Representative samples of each function type
- Edge cases (domain errors, etc.)

---

## Performance Testing

### Microbenchmarks

```java
@Test
void performanceSimpleExpression() {
    MathEngine engine = MathEngine.create();
    CompiledExpression expr = engine.compile("x^2 + 2*x + 1");

    long start = System.nanoTime();
    for (int i = 0; i < 10000; i++) {
        expr.evaluateDouble("x", i);
    }
    long duration = System.nanoTime() - start;

    System.out.println("Average: " + (duration / 10000) + " ns");
}
```

### Stress Tests

```java
@Test
void largeVector() {
    MathEngine engine = MathEngine.create();

    StringBuilder sb = new StringBuilder("{");
    for (int i = 0; i < 10000; i++) {
        if (i > 0) sb.append(",");
        sb.append(i);
    }
    sb.append("}");

    NodeConstant result = engine.evaluate(sb.toString());
    assertThat(result).isInstanceOf(NodeVector.class);
}
```

---

## Common Testing Pitfalls

### 1. Not Testing Edge Cases

**Remember to test:**

- Empty inputs
- Very large inputs
- Boundary values (0, 1, -1)
- Special values (infinity, NaN)

### 2. Ignoring Floating-Point Precision

```java
// WRONG
assertThat(result).isEqualTo(0.3);

// RIGHT
assertThat(result).isCloseTo(0.3, within(1e-10));
```

### 3. Not Testing Error Paths

Test that errors are thrown correctly:

```java
assertThatThrownBy(() -> engine.evaluate("1/0"))
    .isInstanceOf(EvaluationException.class);
```

### 4. Assuming Order in Collections

Vectors have order, but don't assume implementation details:

```java
// Test behavior, not implementation
assertThat(result).isInstanceOf(NodeVector.class);
// Not: assertThat(result.getElements()).isInstanceOf(NodeDouble[].class);
```

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - Architecture overview
- **[../GRAMMAR_TESTS.md](../GRAMMAR_TESTS.md)** - Grammar test specifications
