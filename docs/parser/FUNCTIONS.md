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
        return MatrixOperations.determinant(matrix);
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

For functions that need full control over the input `NodeConstant` but still want
broadcasting. Use `noBroadcasting()` only when the function is not element-wise: an
aggregate, or one that takes a vector, matrix or string as a whole. An element-wise
function must leave broadcasting on, because the same flag decides whether several
arguments are folded into a vector, so `sin(0, 90)` means `sin({0, 90})` as
`sqrt(4, 9, 16)` does.

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

// Reading the argument as an angle, which needs the context per element
MathFunction sin = FunctionBuilder
    .named("sin")
    .describedAs("Sine")
    .inCategory(TRIGONOMETRIC)
    .takingUnary()
    .implementedBy((arg, ctx) -> ctx.mapAngle(arg, Math::sin));

// Binary with broadcasting. Exactness, units and percentages are the
// value arithmetic's job, so the body stays a delegation.
MathFunction pow = FunctionBuilder
    .named("pow")
    .describedAs("Power function")
    .inCategory(EXPONENTIAL)
    .takingBinary()
        .implementedBy((base, exp, ctx) -> base.power(exp));
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

Every trigonometric function is built by the `TrigFunction` factory, which is the one place
the angle unit is applied: to the argument of a standard function, and to the result of an
inverse one.

```java
// Standard trig: the argument is an angle, read by its label or by the context unit
MathFunction sin = TrigFunction.standard("sin", "Sine", Math::sin);
MathFunction cos = TrigFunction.standard("cos", "Cosine", Math::cos);

// Inverse trig: the result is radians, expressed in the context unit
MathFunction atan = TrigFunction.inverse("atan", "Arctangent", Math::atan);

// An inverse defined only on part of the real line
MathFunction asin = TrigFunction.inverse("asin", "Arcsine", -1.0, 1.0, Math::asin);

// An inverse of two arguments
MathFunction atan2 = TrigFunction.inverseBinary("atan2", "...", "y", "x", Math::atan2);
```

---

## FunctionContext

**File:** `function/FunctionContext.java`

Provides utilities to function implementations. Every context knows its function name.

### Error Reporting

```java
// Creates a DomainException with the function name prepended
throw ctx.error("requires positive value, got: " + value);
// -> "sqrt: requires positive value, got: -1.0"
```

Under `silentValidation`, a `DomainException` becomes NaN instead of propagating.
Nothing else is swallowed, so a genuine bug inside a function still surfaces.

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
double d = ctx.toDouble(node);           // any numeric -> double
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
double rad2 = ctx.toRadians(value);   // a NodeConstant: an angle label wins over the context unit
double angle = ctx.fromRadians(rad);  // radians -> context unit
```

### Broadcasting

Three element-wise helpers, one per answer type:

| Helper         | The answer is                                     | Example                         |
|----------------|---------------------------------------------------|---------------------------------|
| `mapDouble`    | a pure number, so the marker is dropped           | `log(100 meters)` is 2          |
| `mapMagnitude` | the same kind of thing, so the marker rides along | `sqrt(100 meters)` is 10 meters |
| `mapAngle`     | a ratio, and the argument is read as an angle     | `sin(90 degrees)` is 1          |

```text
ctx.mapDouble(arg, Math::log);          // a logarithm, an exponential, a gamma
ctx.mapMagnitude(arg, Math::sqrt);      // a root, a fractional part
ctx.mapMagnitude(x, y, Math::hypot);    // two arguments: the marker comes from whichever
                                        // side has one, the left first, and the right is
                                        // converted into the left's unit first
ctx.mapAngle(arg, Math::sin);           // an angle label decides the unit, the configured
                                        // angle unit applies otherwise
```

`FunctionBuilder` has the same pair as one-liners: `implementedByDouble` for a pure
number and `implementedByMagnitude` for a marker-preserving transform.

When you want to keep exactness as well as the marker, use the operations on the value
itself. They preserve units, percentages and exact rationals, and broadcast:

```text
arg.negate();
arg.floor();
arg.abs();
left.multiply(right);
```

### Collection Operations

```java
// Flatten mixed args: sum(1, {2,3}, 4) -> [1, 2, 3, 4]
List<NodeConstant> flat = ctx.flattenArguments(args);
double[] values = ctx.flattenToDoubles(args);
double[] arr = ctx.toDoubleArray(vector);

// Matrix conversions, for reaching the linearalgebra package
Matrix m = ctx.toMatrix(nodeMatrix);
NodeMatrix nm = ctx.fromMatrix(matrix);
```

These conversions read every element as a `double`, so anything that goes through them
comes back inexact. Where the operation can be written in terms of `NodeConstant`
arithmetic, write it that way instead: `MatrixOperations` does, which is why the
determinant of an integer matrix is an exact integer rather than -1.0000000000000004.

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

**Circular** (`trig/TrigonometricFunctions.java`):

```
sin(x),  cos(x),  tan(x)     // the argument is an angle
sec(x),  csc(x),  cot(x)     // reciprocals, also of an angle
asin(x), acos(x), atan(x)    // the result is an angle
asec(x), acsc(x), acot(x)    // inverse reciprocals: acos(1/x), asin(1/x), atan(1/x)
atan2(y, x)                  // two-argument arctangent
```

**Hyperbolic** (`trig/HyperbolicFunctions.java`):

```
sinh(x),  cosh(x),  tanh(x)
sech(x),  csch(x),  coth(x)
asinh(x), acosh(x), atanh(x)
asech(x), acsch(x), acoth(x)  // acosh(1/x), asinh(1/x), atanh(1/x)
```

Both families are built by one factory each, so every entry is a name, a description and
the operation. A restricted domain lives with the maths in `TrigUtils`, and
`FunctionContext.checkingDomain` turns its complaint into the engine's own
`DomainException`.

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

The same three rules apply outside this package. `UtilityFunctions` and
`PercentageFunctions` are written as the arithmetic they stand for, so `frac(3/2)` is 1/2
and `distance(0, 0, 3 m, 4 m)` is 5 meters. The percentage functions are spelled as the
operator that means the same thing, so `addpercent(x, p)` is `x + p` and
`percentof(p, x)` is `p of x`, and neither can drift from it.

These compute on the values, not on a `double[]`, which is what keeps units,
percentages and exact rationals. See `vector/Statistics.java`. Three rules decide the
answer's type, and no per-function table is needed:

- a function that **selects** one of its inputs returns that input, so `mode`,
  `percentile` and `quartile` come back with whatever the element wore
- a function whose answer is a **sum, difference, product or quotient** of its inputs is
  written that way, so `range`, `iqr`, `variance`, `hmean` and `covariance` stay exact
- a function needing a **root or a logarithm** maps the magnitude and keeps the label, so
  `stddev`, `rms` and `gmean` are inexact but still measured in metres

`skewness`, `kurtosis` and `correlation` come out as plain doubles, not by special case
but because they divide like into like and the label cancels. They are ratios, so that is
what they should be.

The one place this is a compromise is `variance` and `covariance`, whose real dimension is
the input's label squared. The engine has no way to spell m², so they keep the label they
had. That is the same compromise `(2 m)^2` already makes.

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
map(fn, vec)             // Apply fn to each element
flatmap(fn, vec)         // Apply fn, then splice each result in
filter(pred, vec)        // Keep the elements that match
partition(pred, vec)     // The matches and the rest, as two vectors
takewhile(pred, vec)     // The leading run that matches
dropwhile(pred, vec)     // Everything after that run
reduce(fn, vec, init)    // Fold to a single value
scan(fn, vec, init)      // The same fold, keeping every step
sortby(key, vec)         // Sort by a computed key
zipwith(fn, a, b)        // Combine two collections pairwise
```

Two argument orders, and one rule that decides which:

- a **dedicated** higher-order function takes the function **first**, as `map` always has
- an **existing** function that already took a value keeps its own order and accepts a
  function **where that value went**

So `filter(pred, vec)` but `count(vec, pred)`. Extending in place is what lets
`sort(vec)` keep working while `sort(vec, comparator)` appears beside it. The functions
that gained a callback this way:

```
sort(vec, comparator)    // (a, b) -> negative, zero or positive, as compare returns
count(vec, pred)         // how many match
indexof(vec, pred)       // the first position that matches, or -1
contains(vec, pred)      // whether any match
unique(vec, key)         // deduplicate by a computed key
first(vec, pred)         // the first match
last(vec, pred)          // the last match
```

A function argument is told apart from a value at runtime, so the two forms never
collide. Arity is checked before the first call, so a one-parameter comparator fails with
a message rather than at some arbitrary element.

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
len(s)           // Element or character count
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
    .maxRecursionDepth(256)  // Default
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

### No Broadcasting (`noBroadcasting()`, Level 1 and Level 3)

Typed functions (`takingTyped`) and aggregate functions have no broadcasting because they
operate on specific types (vectors, matrices) or handle their own argument processing. The
same flag also turns off the shorthand that folds several arguments into a vector, so an
element-wise function must not set it.

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[EVALUATOR.md](./EVALUATOR.md)** - How functions are called
- **[OPERATORS.md](./OPERATORS.md)** - Operator system (similar pattern)
- **[REGISTRIES.md](./REGISTRIES.md)** - Function registration
- **[NODES.md](./NODES.md)** - NodeFunction and NodeLambda
