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

// Stateful session (variables/functions persist)
engine.

evaluate("x := 10");
engine.

evaluate("f(n) := n^2");

NodeConstant result = engine.evaluate("f(x)");  // 100

// Pre-compile for repeated evaluation
CompiledExpression expr = engine.compile("x^2 + y^2");
result =engine.

evaluate(expr);  // Uses current variable values
```

### Session State

```java
// Variables
engine.setVariable("x",new NodeDouble(5.0));
NodeConstant value = engine.getVariable("x");

// Clear session
engine.

clearVariables();
engine.

clearFunctions();
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
        .maxRecursionDepth(1000)
        .maxExpressionDepth(1000)
        .maxVectorSize(1_000_000)
        .maxMatrixDimension(10_000)
        .maxIdentifierLength(256)

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
// NodeRational (exact fractions, default)
if(result instanceof
NodeRational r){
int numerator = r.getNumerator();
int denominator = r.getDenominator();
double value = r.doubleValue();
}

// NodeDouble (floating point)
        if(result instanceof
NodeDouble d){
double value = d.getValue();
}

// NodePercent (0.5 = 50%)
        if(result instanceof
NodePercent p){
double decimal = p.getValue();  // 0.5
double display = p.getPercentage();  // 50.0
}
```

### Other Types

```java
// NodeBoolean
if(result instanceof
NodeBoolean b){
boolean value = b.getValue();
}

// NodeString
        if(result instanceof
NodeString s){
String value = s.getValue();
}

// NodeVector
        if(result instanceof
NodeVector v){
int size = v.size();
Node[] elements = v.getElements();
List<NodeConstant> list = v.toList();
}

// NodeMatrix
        if(result instanceof
NodeMatrix m){
int rows = m.getRows();
int cols = m.getCols();
Node element = m.getElement(row, col);
}

// NodeUnit (value with unit)
        if(result instanceof
NodeUnit u){
NodeNumber value = u.getValue();
String unit = u.getUnit();
}

// NodeRange (1..10)
        if(result instanceof
NodeRange r){
double start = r.getStart();
double end = r.getEnd();
double step = r.getStep();
}

// NodeFunction/NodeLambda (callable)
        if(result instanceof
NodeFunction f){
        // Cannot be serialized, only used internally
        }
```

### Type Checking

```java
result.isNumeric();   // true for NodeRational, NodeDouble, NodePercent
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

### toString() Formatting

All nodes have sensible `toString()`:

```java
NodeRational:"1/3"or "42"
NodeDouble:"3.14159"
NodePercent:"50%"
NodeBoolean:"true"or "false"
NodeString:"hello"
NodeVector:"{1, 2, 3}"
NodeMatrix:"[[1, 2], [3, 4]]"
NodeUnit:"5 meters"
```

### JSON Serialization (Recommended Approach)

```java
// Manual conversion to JSON-friendly structure
public Object toJson(NodeConstant node) {
    return switch (node) {
        case NodeRational r -> Map.of(
                "type", "rational",
                "numerator", r.getNumerator(),
                "denominator", r.getDenominator(),
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
                "display", p.getPercentage() + "%"
        );
        case NodeUnit u -> Map.of(
                "type", "unit",
                "value", toJson(u.getValue()),
                "unit", u.getUnit()
        );
        case NodeRange r -> Map.of(
                "type", "range",
                "start", r.getStart(),
                "end", r.getEnd(),
                "step", r.getStep()
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
