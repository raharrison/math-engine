# MathEngine API Reference

Technical reference for integrating MathEngine into REST/GraphQL APIs.

## 1. Core API

### Entry Point

```java
// Default (all features)
MathEngine engine = MathEngine.create();

// Custom configuration
MathEngineConfig config = MathEngineConfig.builder()
        .angleUnit(AngleUnit.DEGREES)
        .maxVectorSize(10_000)
        .build();
MathEngine engine = MathEngine.create(config);

// Presets
MathEngine.

arithmetic();  // Basic operators only, no functions
MathEngine.

basic();       // Core math functions, no vectors/matrices
MathEngine.

full();        // All features (same as create())
```

### Evaluation

```java
// Single evaluation
NodeConstant result = engine.evaluate("2 + 3 * 4");
double d = engine.evaluateDouble("2 + 3 * 4");

// Stateful session: variables and functions persist across calls
engine.

evaluate("x := 10");
engine.

evaluate("f(n) := n^2");
engine.

evaluate("f(x)");                    // 100

// Pre-compile for repeated evaluation, binding the variables per call
CompiledExpression expr = engine.compile("x^2 + y^2");
expr.

evaluate(Map.of("x", 3,"y",4));      // 25
        expr.

evaluate("x",3);                      // one variable
expr.

evaluate();                            // whatever the session holds
```

### Session State

```java
// Define
engine.defineVariable("x",5.0);
engine.

defineVariable("y",new NodeRational(1, 2));
        engine.

defineFunction("f(n) := n^2");

// Read back what this session has defined
Map<String, NodeConstant> variables = engine.getLocalVariables();
Map<String, FunctionDefinition> functions = engine.getLocalFunctions();

// A session is cleared by making a new engine on the same configuration
engine =MathEngine.

create(engine.getConfig());
```

## 2. Configuration (MathEngineConfig)

### Builder Pattern

```java
MathEngineConfig config = MathEngineConfig.builder()
        // Arithmetic
        .angleUnit(AngleUnit.RADIANS)           // RADIANS | DEGREES
        .forceDoubleArithmetic(false)           // true = faster, false = exact rationals
        .decimalPlaces(-1)                      // -1 = full precision

        // Limits (DoS protection)
        .maxRecursionDepth(256)                 // levels of user function nesting
        .maxExpressionDepth(256)                // levels of parser recursion
        .maxVectorSize(1_000_000)
        .maxMatrixDimension(10_000)
        .maxIdentifierLength(256)
        .maxLiteralDigits(10_000)               // -1 = no limit

        // Features (disable for security/performance)
        .implicitMultiplication(true)           // "2x" means "2*x"
        .vectorsEnabled(true)
        .matricesEnabled(true)
        .unitsEnabled(true)
        .comprehensionsEnabled(true)            // {x^2 for x in 1..10}
        .lambdasEnabled(true)                   // x -> x^2
        .userDefinedFunctionsEnabled(true)      // f(x) := x^2

        .build();
```

### Key Toggles for API Usage

- **forceDoubleArithmetic**: Enable for speed, disable for exact fractions
- **userDefinedFunctionsEnabled**: Disable if users shouldn't define functions
- **lambdasEnabled/comprehensionsEnabled**: Disable if not needed (reduces attack surface)
- **maxVectorSize/maxMatrixDimension**: Critical for DoS prevention
- **maxLiteralDigits**: caps how long a number a literal may name, since `1e100000000`
  would exhaust memory. Default 10,000; negative lifts it

## 3. Input Format

### Literals

```
Numbers:     42, 3.14, 1/3, 1.5e10
Strings:     "hello"
Booleans:    true, false
Vectors:     {1, 2, 3}
Matrices:    [1, 2; 3, 4]  (semicolon = new row)
Ranges:      1..10, 0..100 step 5
```

### Operators

```
Arithmetic:  + - * / ^ %
Comparison:  == != < <= > >=
Logical:     and or xor not
Bitwise:     & | ~ << >>
Assignment:  :=
```

### Syntax

```
Variables:      x := 10
Functions:      f(x) := x^2
Lambdas:        map(x -> x^2, {1,2,3})
Comprehensions: {x^2 for x in 1..10}
Conditionals:   if(x > 0, 1, -1)
Units:          5 meters to feet
```

## 4. Output Types (NodeConstant)

All evaluation returns `NodeConstant`. Use `instanceof` or type methods to handle.

### Number Types

```java
// NodeRational: exact, and the default for integers, fractions and decimal literals
if(result instanceof
NodeRational r){
BigRational value = r.getValue();      // getNumerator(), getDenominator()
double approx = r.doubleValue();
}

// NodeDouble: what a value falls back to once a calculation cannot stay exact
        if(result instanceof
NodeDouble d){
double value = d.getValue();
}

// NodePercent: holds the fraction it stands for, so 50% holds one half
        if(result instanceof
NodePercent p){
NodeNumber fraction = p.getFraction();   // 1/2, exact
NodeNumber percent = p.getPercent();     // 50,  exact
double decimal = p.getValue();           // 0.5
double display = p.getPercentValue();    // 50.0
}
```

### Other Types

```java
if(result instanceof
NodeBoolean b){
boolean value = b.getValue();
}

        if(result instanceof
NodeString s){
String value = s.getValue();
}

        if(result instanceof
NodeVector v){
int size = v.size();
Node[] elements = v.getElements();
List<NodeConstant> list = v.toList();
}

        if(result instanceof
NodeMatrix m){
int rows = m.getRows();
int cols = m.getCols();
Node element = m.getElement(row, col);
}

// NodeUnit: a magnitude wearing a label
        if(result instanceof
NodeUnit u){
NodeNumber magnitude = u.getMagnitude();   // exact where the quantity is
double value = u.getValue();
UnitDefinition unit = u.getUnit();         // getName(), getDisplayName(value)
}

// A range expression evaluates to a vector, so there is no NodeRange to match on:
// "1..5" gives NodeVector {1, 2, 3, 4, 5}

// NodeFunction and NodeLambda are callable and have no serialised form
```

### Type Checking

```java
result.isNumeric();   // NodeRational, NodeDouble, NodePercent, NodeBoolean, NodeUnit
result.

isBoolean();
result.

isString();
result.

isVector();
result.

isMatrix();
```

## 5. Serialization

### Showing a value

Use a `NodeFormatter`. `toString()` gives the same text at full precision, but a formatter
is the one that takes a decimal-place setting.

```java
NodeFormatter fmt = StringNodeFormatter.fullPrecision();
fmt.

format(result);
```

```text
NodeRational   "2.5", "1/3" or "42"
NodeDouble     "3.14159"
NodePercent    "50%"
NodeBoolean    "true" or "false"
NodeString     "hello"
NodeVector     "{1, 2, 3}"
NodeMatrix     "[1, 2; 3, 4]"
NodeUnit       "5 meters"
```

A rational shows as a decimal where a decimal equals it exactly and as a ratio where none
does, so `2.5` is not shown back as `5/2`. See `RationalDisplay`, and the NodeRational
section of NODES.md.

### JSON Serialization (Recommended Approach)

```java
// Manual conversion to JSON-friendly structure
public Object toJson(NodeConstant node) {
    return switch (node) {
        case NodeRational r -> Map.of(
                "type", "rational",
                "numerator", r.getValue().getNumerator().toString(),
                "denominator", r.getValue().getDenominator().toString(),
                "decimal", r.doubleValue()
        );
        case NodeDouble d -> Map.of(
                "type", "double",
                "value", d.getValue()
        );
        case NodeBoolean b -> Map.of(
                "type", "boolean",
                "value", b.getValue()
        );
        case NodeString s -> Map.of(
                "type", "string",
                "value", s.getValue()
        );
        case NodeVector v -> Map.of(
                "type", "vector",
                "elements", Arrays.stream(v.getElements())
                        .map(this::toJson)
                        .toList()
        );
        case NodeMatrix m -> Map.of(
                "type", "matrix",
                "rows", m.getRows(),
                "cols", m.getCols(),
                "elements", Arrays.stream(m.getElements())
                        .map(row -> Arrays.stream(row).map(this::toJson).toList())
                        .toList()
        );
        case NodePercent p -> Map.of(
                "type", "percent",
                "decimal", p.getValue(),
                "display", p.getPercentValue() + "%"
        );
        case NodeUnit u -> Map.of(
                "type", "unit",
                "value", toJson(u.getMagnitude()),
                "unit", u.getUnit().getName()
        );
        case NodeRange r -> Map.of(
                "type", "range",   // internal only; evaluation returns a vector
                "start", r.getStart().doubleValue(),
                "end", r.getEnd().doubleValue(),
                "step", r.getStep().doubleValue()
        );
        case NodeFunction f, NodeLambda l -> Map.of(
                "type", "function",
                "error", "Functions cannot be serialized"
        );
    };
}
```

### Simple String Response

```java
// For basic calculator APIs
String response = result.toString();
```

## 6. Exception Handling

```java
try{
NodeConstant result = engine.evaluate(expression);
}catch(
MathEngineException e){
// Base class for all engine exceptions
String message = e.getMessage();

// Specific types:
// - LexerException: Invalid syntax during tokenization
// - ParserException: Invalid syntax during parsing
// - EvaluationException: Runtime error during evaluation
// - ArityException: Wrong number of function arguments
// - TypeError: Type mismatch (e.g., "hello" + 5)
// - RecursionLimitException: Stack overflow
}
```

## 7. Thread Safety

- **MathEngine**: Immutable and thread-safe **except** for session state (variables/functions)
- **Session State**: NOT thread-safe. Use separate instances per user session.
- **MathEngineConfig**: Immutable, fully thread-safe

### Recommended Architecture

```java
// Singleton config (shared)
private static final MathEngineConfig CONFIG = MathEngineConfig.builder()
                .maxVectorSize(10_000)
                .build();

// Per-user instance (not shared)
@SessionScoped
public class UserSession {
    private final MathEngine engine = MathEngine.create(CONFIG);
}
```

## 8. API Considerations

### Security

1. **Input Validation**: Limit expression length before evaluation
2. **Resource Limits**: Set max vector/matrix sizes, recursion depth
3. **Disable Features**: Turn off lambdas/comprehensions if not needed
4. **Timeout**: Wrap evaluation in timeout (no built-in timeout)
5. **User Functions**: Disable if users shouldn't define custom functions

### Performance

1. **Compile Once**: Use `compile()` for repeated expressions
2. **Double Arithmetic**: Enable `forceDoubleArithmetic` for speed
3. **Disable Features**: Turn off unused features (vectors, matrices, etc.)
4. **Session Cleanup**: Call `clearVariables()` periodically to prevent memory leaks
