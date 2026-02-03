# Function System

**Purpose:** Extensible function registration, dispatch, and execution system

---

## Overview

The function system provides both built-in mathematical functions and support for user-defined functions:

```
Function Call: sin(pi)
        |
FunctionCallHandler determines type
        |
Built-in: FunctionExecutor.execute()
User-defined: Evaluate function body with parameters
        |
Result (NodeConstant)
```

**Function Types:**

1. **Built-in functions** - Defined via `FunctionBuilder` DSL, registered in `FunctionExecutor`
2. **User-defined functions** - Defined in expressions (`f(x) := x^2`)
3. **Lambda functions** - Anonymous functions (`x -> x^2`)

---

## Architecture

### Core Components

**1. FunctionBuilder** (`function/FunctionBuilder.java`)

- Fluent DSL for defining functions with minimal boilerplate
- Handles broadcasting, metadata, arity, and type conversion automatically
- Entry point: `FunctionBuilder.named("functionName")`

**2. MathFunction** (`function/MathFunction.java`)

- Interface for all built-in functions
- Defines name, aliases, arity, category, and execution

**3. FunctionExecutor** (`function/FunctionExecutor.java`)

- Central registry for built-in functions
- Dispatches function calls by name

**4. FunctionContext** (`function/FunctionContext.java`)

- Provides validation helpers, type coercion, angle conversion, and broadcasting
- Every context knows the function name it serves (for error messages)

**5. FunctionCallHandler** (`evaluator/handler/FunctionCallHandler.java`)

- Evaluates all function calls
- Handles built-in, user-defined, and lambda calls

---

## Function Definition Hierarchy

All built-in functions are defined using `FunctionBuilder`. The builder supports four levels of abstraction,
from simplest (pure math) to most flexible (full control):

```
Level 0: Pure Math (No Type Inspection)
+-- implementedByDouble(DoubleUnaryOperator)     <- auto-broadcasts, auto-converts to double
+-- implementedByDouble(DoubleBinaryOperator)    <- auto-broadcasts via BroadcastingEngine
+-- Use: sqrt, exp, log, sinh, cosh, basic arithmetic

Level 1: Type-Safe Extraction (via ArgType)
+-- takingTyped(ArgType<A>).implementedBy(...)               <- no broadcasting (by design)
+-- takingTyped(ArgType<A>, ArgType<B>).implementedBy(...)   <- no broadcasting (by design)
+-- takingTyped(ArgType<A>, ArgType<B>, ArgType<C>).implementedBy(...)
+-- Use: take, drop, get, row, col, det, transpose, minor, map, filter, reduce

Level 2: Type-Aware (Full Control)
+-- implementedBy(UnaryFunction)                 <- auto-broadcasts (or manual via noBroadcasting())
+-- implementedBy(BinaryFunction)                <- auto-broadcasts via BroadcastingEngine
+-- Use: sin/cos/tan (manual broadcast for angle conversion), diag, norm, pow

Level 3: Aggregate (Variadic)
+-- implementedByAggregate(AggregateFunction)    <- no broadcasting
+-- Use: sum, min, max, concat, zip, slice, any, all, none
```

### Level 0: Pure Math (`implementedByDouble`)

For functions that simply map `double -> double` or `(double, double) -> double`. The builder
automatically handles type conversion (via `FunctionContext.toNumber()`) and broadcasting over
vectors/matrices.

```java
// Unary: auto-broadcasts over vectors and matrices
MathFunction exp = FunctionBuilder
    .named("exp")
    .describedAs("Natural exponential (e^x)")
    .inCategory(EXPONENTIAL)
    .takingUnary()
    .implementedByDouble(Math::exp);

// Binary: auto-broadcasts via BroadcastingEngine
MathFunction hypot = FunctionBuilder
    .named("hypot")
    .describedAs("Hypotenuse")
    .inCategory(UTILITY)
    .takingBinary()
    .implementedByDouble(Math::hypot);
```

### Level 1: Type-Safe Extraction (`takingTyped` + `ArgType`)

For functions that need specific argument types (vectors, matrices, integers, etc.).
Uses `ArgType<T>` extractors for type-safe parameter access without instanceof checks.
Broadcasting is disabled by design since these functions operate on specific types.

**ArgType extractors** (defined in `ArgTypes`):

| Extractor                   | Extracts       | Throws on            |
|-----------------------------|----------------|----------------------|
| `ArgTypes.number()`         | `Double`       | Non-numeric          |
| `ArgTypes.integer()`        | `Integer`      | Non-integer          |
| `ArgTypes.longInt()`        | `Long`         | Non-integer          |
| `ArgTypes.bool()`           | `Boolean`      | Non-convertible      |
| `ArgTypes.string()`         | `String`       | Non-string           |
| `ArgTypes.vector()`         | `NodeVector`   | Non-vector           |
| `ArgTypes.matrix()`         | `NodeMatrix`   | Non-matrix           |
| `ArgTypes.doubleArray()`    | `double[]`     | Non-vector           |
| `ArgTypes.function()`       | `NodeFunction` | Non-function         |
| `ArgTypes.any()`            | `NodeConstant` | Never                |
| `ArgTypes.vectorOrScalar()` | `NodeVector`   | Never (wraps scalar) |

```java
// Unary typed
MathFunction det = FunctionBuilder
    .named("det")
    .describedAs("Matrix determinant")
    .inCategory(MATRIX)
    .takingTyped(ArgTypes.matrix())
    .implementedBy((matrix, ctx) -> {
        ctx.requireSquareMatrix(matrix);
        return new NodeDouble(ctx.toMatrix(matrix).determinant());
    });

// Binary typed
MathFunction take = FunctionBuilder
    .named("take")
    .describedAs("Take first n elements")
    .inCategory(VECTOR)
    .takingTyped(ArgTypes.vector(), ArgTypes.integer())
    .implementedBy((vector, n, ctx) -> {
        n = Math.min(Math.max(n, 0), vector.size());
        Node[] result = new Node[n];
        for (int i = 0; i < n; i++) {
            result[i] = vector.getElement(i);
        }
        return new NodeVector(result);
    });

// Ternary typed (e.g., reduce(fn, vector, initial))
MathFunction reduce = FunctionBuilder
    .named("reduce")
    .describedAs("Reduce vector with binary function")
    .inCategory(VECTOR)
    .takingTyped(ArgTypes.function(), ArgTypes.vector(), ArgTypes.any())
    .implementedBy((fn, vector, initial, ctx) -> {
        NodeConstant acc = initial;
        for (int i = 0; i < vector.size(); i++) {
            acc = ctx.callFunction(fn, List.of(acc, (NodeConstant) vector.getElement(i)));
        }
        return acc;
    });
```

### Level 2: Type-Aware (`implementedBy` with UnaryFunction/BinaryFunction)

For functions that need full control over the input `NodeConstant` but still want optional
broadcasting. Use `noBroadcasting()` when the function broadcasts internally (e.g., trig
functions that need angle conversion before broadcasting).

```java
// With automatic broadcasting (default)
MathFunction abs = FunctionBuilder
    .named("abs")
    .describedAs("Absolute value")
    .inCategory(UTILITY)
    .takingUnary()
    .implementedBy((arg, ctx) -> {
        // Preserve rational precision
        if (arg instanceof NodeRational rat) {
            return new NodeRational(rat.getValue().abs());
        }
        return new NodeDouble(Math.abs(ctx.toNumber(arg).doubleValue()));
    });

// With manual broadcasting (for angle conversion)
MathFunction sin = FunctionBuilder
    .named("sin")
    .describedAs("Sine")
    .inCategory(TRIGONOMETRIC)
    .takingUnary()
    .noBroadcasting()  // broadcasts internally
    .implementedBy((arg, ctx) ->
        ctx.applyWithBroadcasting(arg, value ->
            Math.sin(ctx.toRadians(value))));

// Binary with broadcasting
MathFunction pow = FunctionBuilder
    .named("pow")
    .describedAs("Power function")
    .inCategory(EXPONENTIAL)
    .takingBinary()
    .implementedBy((base, exp, ctx) -> {
        double baseVal = ctx.toNumber(base).doubleValue();
        double expVal = ctx.toNumber(exp).doubleValue();
        if (base instanceof NodeRational baseRat) {
            if (expVal == Math.floor(expVal) && !Double.isInfinite(expVal)) {
                return new NodeRational(baseRat.getValue().pow((int) expVal));
            }
        }
        return new NodeDouble(Math.pow(baseVal, expVal));
    });
```

### Level 3: Aggregate (`implementedByAggregate`)

For variadic functions that receive all arguments at once. No broadcasting is applied.

```java
MathFunction sum = FunctionBuilder
    .named("sum")
    .describedAs("Sum of all values")
    .inCategory(VECTOR)
    .takingVariadic(1)
    .implementedByAggregate((args, ctx) -> {
        double[] values = ctx.flattenToDoubles(args);
        double total = 0;
        for (double v : values) total += v;
        return new NodeDouble(total);
    });
```

### TrigFunction Helper

The `TrigFunction` factory simplifies creating trigonometric functions with automatic angle
unit conversion:

```java
// Standard trig: input is an angle (converted from context unit to radians)
MathFunction sin = TrigFunction.standard("sin", "Sine", Math::sin);
MathFunction cos = TrigFunction.standard("cos", "Cosine", Math::cos);

// Inverse trig: output is an angle (converted from radians to context unit)
MathFunction asin = TrigFunction.inverse("asin", "Arcsine", Math::asin);
MathFunction atan = TrigFunction.inverse("atan", "Arctangent", Math::atan);
```

Internally these use `FunctionBuilder` with `noBroadcasting()` and broadcast manually via
`FunctionContext.applyWithBroadcasting()`.

---

## FunctionContext

**File:** `function/FunctionContext.java`

Provides utilities to function implementations. Every context knows its function name.

### Error Reporting

```java
// Creates IllegalArgumentException with function name prepended
throw ctx.error("requires positive value, got: " + value);
// -> "sqrt: requires positive value, got: -1.0"
```

### Domain Validation

```java
ctx.requirePositive(value);          // > 0
ctx.requireNonNegative(value);       // >= 0
ctx.requireNonZero(value);           // != 0
ctx.requireInRange(value, min, max); // min <= value <= max
```

### Type Coercion

```java
NodeNumber num = ctx.toNumber(node);     // any numeric -> NodeNumber
boolean b = ctx.toBoolean(node);         // numeric -> truthy/falsy
int i = ctx.requireInteger(node);        // validates no fractional part
long l = ctx.requireLong(node);          // validates no fractional part
NodeVector v = ctx.requireVector(node);  // must be vector
NodeMatrix m = ctx.requireMatrix(node);  // must be matrix
NodeString s = ctx.requireString(node);  // must be string
```

### Angle Conversion

```java
double rad = ctx.toRadians(angle);    // context unit -> radians
double angle = ctx.fromRadians(rad);  // radians -> context unit
```

### Broadcasting

```java
// Apply double operation with broadcasting over vectors/matrices
ctx.applyWithBroadcasting(arg, Math::sqrt);

// Apply with type preservation (rationals stay rational)
ctx.applyWithTypePreservation(arg, BigRational::negate, x -> -x);
```

### Collection Operations

```java
// Flatten mixed args: sum(1, {2,3}, 4) -> [1, 2, 3, 4]
List<NodeConstant> flat = ctx.flattenArguments(args);
double[] values = ctx.flattenToDoubles(args);
double[] arr = ctx.toDoubleArray(vector);

// Matrix conversions
Matrix m = ctx.toMatrix(nodeMatrix);
NodeMatrix nm = ctx.fromMatrix(matrix);
```

### Function Calling

```java
// Call a user-defined function or lambda (for higher-order functions)
NodeConstant result = ctx.callFunction(func, List.of(arg1, arg2));
```

---

## FunctionExecutor

**File:** `function/FunctionExecutor.java`

**Purpose:** Central registry and dispatcher for built-in functions

### Registration

```java
FunctionExecutor executor = new FunctionExecutor();

// Register individual function
executor.register(sinFunction);

// Register multiple functions
executor.registerAll(StandardFunctions.all());
```

### Execution

```java
// By name
NodeConstant result = executor.execute(
    "sin",
    List.of(new NodeDouble(Math.PI / 2)),
    context
);

// Check existence
boolean exists = executor.hasFunction("sin");

// Get all function names
Set<String> names = executor.getAllFunctionNames();
```

### Arity Checking

```java
// Automatically checked before execution
executor.execute("sin", List.of(arg1, arg2), ctx);
// Throws: ArityException("sin expects 1 argument, got 2")
```

---

## Standard Functions

**File:** `function/StandardFunctions.java`

Factory method to get all standard functions:

```java
List<MathFunction> functions = StandardFunctions.all();
```

### Math Functions

**Exponential & Logarithmic** (`math/ExponentialFunctions.java`):

```
exp(x)       // e^x
exp2(x)      // 2^x
exp10(x)     // 10^x
expm1(x)     // e^x - 1 (accurate for small x)
ln(x)        // Natural log
log(x)       // Base-10 log (alias: log10)
log2(x)      // Base-2 log
logn(x, b)   // Arbitrary base log
log1p(x)     // ln(1+x) (accurate for small x)
sqrt(x)      // Square root
cbrt(x)      // Cube root
nroot(x, n)  // nth root
pow(x, n)    // Power function
```

**Rounding** (`math/RoundingFunctions.java`):

```
floor(x)     // Round down
ceil(x)      // Round up
round(x)     // Round to nearest
trunc(x)     // Truncate to integer
```

**Utility** (`math/UtilityFunctions.java`):

```
abs(x)       // Absolute value
sign(x)      // Sign (-1, 0, 1)
frac(x)      // Fractional part
hypot(x, y)  // Hypotenuse
clamp(x,a,b) // Clamp to range
lerp(a,b,t)  // Linear interpolation
```

### Trigonometric Functions

**Basic** (`trig/TrigonometricFunctions.java`):

```
sin(x), cos(x), tan(x)
asin(x), acos(x), atan(x)
atan2(y, x)  // Two-argument arctangent
```

**Hyperbolic** (`trig/HyperbolicFunctions.java`):

```
sinh(x), cosh(x), tanh(x)
asinh(x), acosh(x), atanh(x)
```

### Vector Functions

**Statistical** (`vector/StatisticalFunctions.java`):

```
mean(vec)        // Average
median(vec)      // Median
mode(vec)        // Most common value
variance(vec)    // Variance
stddev(vec)      // Standard deviation
percentile(vec, p)
```

**Manipulation** (`vector/VectorManipulationFunctions.java`):

```
sort(vec)        // Sort ascending
reverse(vec)     // Reverse order
unique(vec)      // Remove duplicates
length(vec)      // Number of elements
take(vec, n)     // First n elements
drop(vec, n)     // Remove first n elements
```

**Aggregates** (`vector/VectorFunctions.java`):

```
sum(vec)         // Sum of elements
product(vec)     // Product of elements
min(vec)         // Minimum element
max(vec)         // Maximum element
concat(v1, v2)   // Concatenate vectors
```

**Matrix** (`vector/MatrixFunctions.java`):

```
det(m)           // Determinant
trace(m)         // Trace
transpose(m)     // Transpose
inverse(m)       // Inverse
row(m, i)        // Extract row
col(m, j)        // Extract column
diag(v)          // Diagonal matrix from vector
```

**Higher-Order** (`vector/HigherOrderFunctions.java`):

```
map(fn, vec)            // Apply fn to each element
filter(fn, vec)         // Keep elements where fn is truthy
reduce(fn, vec, init)   // Fold vector with binary function
```

### Special Functions

**Conditional** (`special/ConditionalFunctions.java`):

```
if(cond, then, else)    // Lazy evaluation
```

**Number Theory** (`special/NumberTheoryFunctions.java`):

```
gcd(a, b)        // Greatest common divisor
lcm(a, b)        // Least common multiple
isprime(n)       // Check if prime
factorial(n)     // n!
```

**Bitwise** (`special/BitwiseFunctions.java`):

```
bitand(a, b)     // Bitwise AND
bitor(a, b)      // Bitwise OR
bitxor(a, b)     // Bitwise XOR
bitnot(n)        // Bitwise NOT
lshift(n, s)     // Left shift
rshift(n, s)     // Right shift
```

**Type** (`special/TypeFunctions.java`):

```
isnumber(x)      // Check if numeric
isstring(x)      // Check if string
isvector(x)      // Check if vector
ismatrix(x)      // Check if matrix
typeof(x)        // Get type name
```

**String** (`string/StringFunctions.java`):

```
upper(s)         // Uppercase
lower(s)         // Lowercase
trim(s)          // Strip whitespace
strlen(s)        // String length
substring(s,i,j) // Substring
```

---

## User-Defined Functions

### Definition

**Syntax:**

```
f(x) := x^2
g(x, y) := x + y
```

**Storage:**

```java
class FunctionDefinition {
    String name;
    List<String> parameters;
    Node body;  // Unevaluated AST
}
```

### Evaluation

**Process:**

1. Lookup function in context
2. Create new evaluation context
3. Bind parameters to arguments
4. Evaluate body in new context
5. Return result

### Recursion

**Supported with depth tracking:**

```
factorial(n) := if(n <= 1, 1, n * factorial(n-1))
```

**Depth Limit:**

```java
MathEngineConfig config = MathEngineConfig.builder()
    .maxRecursionDepth(1000)  // Default
    .build();
```

---

## Lambda Functions

### Definition

**Syntax:**

```
x -> x^2
(a, b) -> a + b
```

### Usage

**As argument to higher-order function:**

```
map(x -> x^2, {1, 2, 3})
filter(x -> x > 5, 1..10)
reduce((acc, x) -> acc + x, {1,2,3,4}, 0)
```

**Assigned to variable:**

```
f := x -> x^2
f(5)  // 25
```

---

## Broadcasting in Functions

Broadcasting is the automatic element-wise application of a function over vectors and matrices.

### Automatic Broadcasting (Level 0 and Level 2 default)

When `FunctionBuilder` creates a function with `implementedByDouble()` or `implementedBy()` (without
`noBroadcasting()`), broadcasting is handled automatically via `BroadcastingEngine`:

```
sqrt(4)           -> 2.0         (scalar)
sqrt({4, 9, 16})  -> {2, 3, 4}  (vector - auto-broadcast)
sqrt([[4,9]])     -> [[2,3]]    (matrix - auto-broadcast)
```

### Manual Broadcasting (Level 2 with `noBroadcasting()`)

Functions that need pre-processing before broadcasting (e.g., angle conversion) disable
automatic broadcasting and use `FunctionContext.applyWithBroadcasting()`:

```java
.noBroadcasting()
.implementedBy((arg, ctx) ->
    ctx.applyWithBroadcasting(arg, value ->
        Math.sin(ctx.toRadians(value))));
```

### No Broadcasting (Level 1 and Level 3)

Typed functions (`takingTyped`) and aggregate functions have no broadcasting because they
operate on specific types (vectors, matrices) or handle their own argument processing.

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[EVALUATOR.md](./EVALUATOR.md)** - How functions are called
- **[OPERATORS.md](./OPERATORS.md)** - Operator system (similar pattern)
- **[REGISTRIES.md](./REGISTRIES.md)** - Function registration
- **[NODES.md](./NODES.md)** - NodeFunction and NodeLambda
