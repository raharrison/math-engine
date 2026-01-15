# Function System

**Purpose:** Extensible function registration, dispatch, and execution system

---

## Overview

The function system provides both built-in mathematical functions and support for user-defined functions:

```
Function Call: sin(pi)
        ↓
FunctionCallHandler determines type
        ↓
Built-in: FunctionExecutor.execute()
User-defined: Evaluate function body with parameters
        ↓
Result (NodeConstant)
```

**Function Types:**

1. **Built-in functions** - Registered via `MathFunction` interface
2. **User-defined functions** - Defined in expressions (`f(x) := x^2`)
3. **Lambda functions** - Anonymous functions (`x -> x^2`)

---

## Architecture

### Core Components

**1. MathFunction Interface**

- Base interface for all built-in functions
- Defines name, arity, execution

**2. FunctionExecutor**

- Central registry for built-in functions
- Dispatches function calls by name

**3. FunctionDefinition**

- Represents user-defined functions
- Stores name, parameters, body (AST)

**4. FunctionCallHandler**

- Evaluates all function calls
- Handles built-in, user-defined, and lambda calls

---

## MathFunction Interface

**File:** `function/MathFunction.java`

```java
public interface MathFunction {
    /**
     * The function name (primary).
     */
    String name();

    /**
     * Alternative names for this function.
     */
    default List<String> aliases() {
        return List.of();
    }

    /**
     * Minimum number of arguments.
     */
    int minArity();

    /**
     * Maximum number of arguments (-1 for unlimited).
     */
    default int maxArity() {
        return minArity();
    }

    /**
     * Executes the function with given arguments.
     *
     * @param args evaluated arguments
     * @param context execution context
     * @return the result
     */
    NodeConstant apply(List<NodeConstant> args, FunctionContext context);
}
```

**FunctionContext:**

```java
interface FunctionContext {
    AngleUnit getAngleUnit();
    // Future: additional context as needed
}
```

---

## Function Types

### 1. UnaryFunction

**Purpose:** Single numeric argument

**Base Class:** `function/UnaryFunction.java`

**Interface:**

```java
interface UnaryFunction extends MathFunction {
    double apply(double x);

    // Default implementation for MathFunction
    default NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
        double arg = args.get(0).doubleValue();
        double result = apply(arg);
        return new NodeDouble(result);
    }
}
```

**Wrapper:** `function/UnaryFunctionWrapper.java`

Wraps a `DoubleUnaryOperator`:

```java
MathFunction abs = new UnaryFunctionWrapper(
    "abs",
    Math::abs
);
```

**Examples:**

```java
abs(x)     → Math.abs(x)
sqrt(x)    → Math.sqrt(x)
exp(x)     → Math.exp(x)
ln(x)      → Math.log(x)
log(x)     → Math.log10(x)
```

### 2. BinaryFunction

**Purpose:** Two numeric arguments

**Base Class:** `function/BinaryFunction.java`

**Interface:**

```java
interface BinaryFunction extends MathFunction {
    double apply(double x, double y);
}
```

**Wrapper:** `function/BinaryFunctionWrapper.java`

```java
MathFunction max = new BinaryFunctionWrapper(
    "max",
    Math::max
);

MathFunction pow = new BinaryFunctionWrapper(
    "pow",
    Math::pow
);
```

**Examples:**

```java
max(a, b)      → Math.max(a, b)
min(a, b)      → Math.min(a, b)
pow(base, exp) → Math.pow(base, exp)
gcd(a, b)      → Greatest common divisor
lcm(a, b)      → Least common multiple
```

### 3. AggregateFunction

**Purpose:** Variable number of arguments (or vector)

**Base Class:** `function/AggregateFunction.java`

**Interface:**

```java
interface AggregateFunction extends MathFunction {
    double apply(double[] values);

    default int minArity() { return 1; }
    default int maxArity() { return -1; }  // Unlimited
}
```

**Wrapper:** `function/AggregateFunctionWrapper.java`

**Examples:**

```java
sum(1, 2, 3)           → 6
sum({1, 2, 3})         → 6
mean(1, 2, 3, 4, 5)    → 3.0
max(5, 2, 8, 1)        → 8
```

**Implementation Pattern:**

```java
class SumFunction implements AggregateFunction {
    @Override
    public String name() { return "sum"; }

    @Override
    public double apply(double[] values) {
        return Arrays.stream(values).sum();
    }
}
```

### 4. TrigFunction

**Purpose:** Trigonometric functions with angle unit awareness

**Base Class:** `function/TrigFunction.java`

**Special Behavior:**

- Respects `AngleUnit` setting (radians vs degrees)
- Converts input before computation

**Examples:**

```java
// In radians mode
sin(pi/2)    → 1.0

// In degrees mode
sin(90)      → 1.0
```

**Implementation:**

```java
class SinFunction extends TrigFunction {
    @Override
    public String name() { return "sin"; }

    @Override
    protected double applyInRadians(double radians) {
        return Math.sin(radians);
    }
}
```

**Functions:**

```java
sin(x), cos(x), tan(x)
asin(x), acos(x), atan(x), atan2(y, x)
sinh(x), cosh(x), tanh(x)
asinh(x), acosh(x), atanh(x)
```

### 5. Higher-Order Functions

**Purpose:** Functions that take other functions as arguments

**File:** `function/vector/HigherOrderFunctions.java`

**Examples:**

**map:**

```java
map(x -> x^2, {1, 2, 3})  →  {1, 4, 9}
```

**filter:**

```java
filter(x -> x > 5, {3, 6, 2, 8})  →  {6, 8}
```

**reduce/fold:**

```java
reduce((acc, x) -> acc + x, 0, {1, 2, 3, 4})  →  10
```

**Implementation Pattern:**

```java
class MapFunction implements MathFunction {
    @Override
    public NodeConstant apply(List<NodeConstant> args, FunctionContext ctx) {
        NodeFunction func = (NodeFunction) args.get(0);
        NodeVector vec = (NodeVector) args.get(1);

        Node[] results = new Node[vec.size()];
        for (int i = 0; i < vec.size(); i++) {
            // Call function on each element
            results[i] = callFunction(func, vec.getElements()[i]);
        }

        return new NodeVector(results);
    }
}
```

---

## FunctionExecutor

**File:** `function/FunctionExecutor.java`

**Purpose:** Central registry and dispatcher for built-in functions

### Registration

```java
FunctionExecutor executor = new FunctionExecutor();

// Register individual function
executor.register(new SinFunction());

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

**Exponential & Logarithmic:**

```java
exp(x)       // e^x
ln(x)        // Natural log
log(x)       // Base-10 log
log2(x)      // Base-2 log
```

**Rounding:**

```java
floor(x)     // Round down
ceil(x)      // Round up
round(x)     // Round to nearest
trunc(x)     // Truncate to integer
```

**Utility:**

```java
abs(x)       // Absolute value
sign(x)      // Sign (-1, 0, 1)
sqrt(x)      // Square root
cbrt(x)      // Cube root
```

### Trigonometric Functions

**Basic:**

```java
sin(x), cos(x), tan(x)
asin(x), acos(x), atan(x)
atan2(y, x)  // Two-argument arctangent
```

**Hyperbolic:**

```java
sinh(x), cosh(x), tanh(x)
asinh(x), acosh(x), atanh(x)
```

### Vector Functions

**Aggregates:**

```java
sum(vec)         // Sum of elements
product(vec)     // Product of elements
mean(vec)        // Average
median(vec)      // Median
mode(vec)        // Most common value
```

**Statistical:**

```java
variance(vec)    // Variance
stddev(vec)      // Standard deviation
min(vec)         // Minimum element
max(vec)         // Maximum element
```

**Manipulation:**

```java
sort(vec)        // Sort ascending
reverse(vec)     // Reverse order
unique(vec)      // Remove duplicates
length(vec)      // Number of elements
```

### Special Functions

**Conditional:**

```java
if(condition, thenValue, elseValue)  // Lazy evaluation
```

**Number Theory:**

```java
gcd(a, b)        // Greatest common divisor
lcm(a, b)        // Least common multiple
isprime(n)       // Check if prime
factorial(n)     // n!
```

**Bitwise:**

```java
band(a, b)       // Bitwise AND
bor(a, b)        // Bitwise OR
bxor(a, b)       // Bitwise XOR
bnot(n)          // Bitwise NOT
```

**Type Functions:**

```java
isnumber(x)      // Check if numeric
isstring(x)      // Check if string
isvector(x)      // Check if vector
ismatrix(x)      // Check if matrix
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

**In Context:**

```java
FunctionDefinition def = new FunctionDefinition(
    "square",
    List.of("x"),
    new NodeBinary(...)  // x^2
);
NodeFunction func = new NodeFunction(def);
context.define("square", func);
```

### Evaluation

**Process:**

1. Lookup function in context
2. Create new evaluation context
3. Bind parameters to arguments
4. Evaluate body in new context
5. Return result

**Code:**

```java
NodeFunction func = (NodeFunction) context.get("square");
FunctionDefinition def = func.getFunction();

// Create function context
EvaluationContext funcContext = new EvaluationContext(context);

// Bind parameters
for (int i = 0; i < def.getParameters().size(); i++) {
    funcContext.define(def.getParameters().get(i), args.get(i));
}

// Evaluate body
NodeConstant result = evaluator.evaluate(def.getBody());
```

### Recursion

**Supported with depth tracking:**

```java
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

**Storage:**

```java
class NodeLambda extends NodeConstant {
    List<String> parameters;
    Node body;
}
```

### Usage

**As argument to higher-order function:**

```java
map(x -> x^2, {1, 2, 3})
filter(x -> x > 5, 1..10)
```

**Assigned to variable:**

```java
f := x -> x^2
f(5)  // 25
```

### Evaluation

**Converted to NodeFunction:**

```java
FunctionDefinition def = new FunctionDefinition(
    "<lambda>",  // Anonymous name
    lambda.getParameters(),
    lambda.getBody()
);
return new NodeFunction(def);
```

---

## Implementing Custom Functions

### Step 1: Implement MathFunction

```java
public class CustomFunction implements MathFunction {
    @Override
    public String name() {
        return "myFunc";
    }

    @Override
    public List<String> aliases() {
        return List.of("mf", "my_func");
    }

    @Override
    public int minArity() {
        return 2;
    }

    @Override
    public int maxArity() {
        return 2;
    }

    @Override
    public NodeConstant apply(List<NodeConstant> args, FunctionContext context) {
        // Type checking
        if (!(args.get(0) instanceof NodeNumber) ||
            !(args.get(1) instanceof NodeNumber)) {
            throw new TypeError("myFunc requires numeric arguments");
        }

        // Get values
        double x = args.get(0).doubleValue();
        double y = args.get(1).doubleValue();

        // Compute
        double result = customComputation(x, y);

        return new NodeDouble(result);
    }

    private double customComputation(double x, double y) {
        // Your logic here
        return x * Math.log(y) + y * Math.exp(x);
    }
}
```

### Step 2: Register Function

**Via MathEngine:**

```java
MathEngine engine = MathEngine.create();
engine.registerFunction(new CustomFunction());

// Use immediately
NodeConstant result = engine.evaluate("myFunc(2, 3)");
```

**Via Config:**

```java
List<MathFunction> functions = new ArrayList<>(StandardFunctions.all());
functions.add(new CustomFunction());

MathEngineConfig config = MathEngineConfig.builder()
    .functions(functions)
    .build();

MathEngine engine = MathEngine.create(config);
```

---

## Testing Functions

### Unit Tests

```java
@Test
void sinFunction() {
    SinFunction sin = new SinFunction();
    FunctionContext ctx = new FunctionContext(AngleUnit.RADIANS);

    NodeConstant result = sin.apply(
        List.of(new NodeDouble(Math.PI / 2)),
        ctx
    );

    assertThat(result.doubleValue()).isCloseTo(1.0, within(1e-10));
}
```

### Integration Tests

```java
@Test
void functionInExpression() {
    MathEngine engine = MathEngine.create();
    NodeConstant result = engine.evaluate("sin(pi / 2)");

    assertThat(result.doubleValue()).isCloseTo(1.0, within(1e-10));
}

@Test
void userDefinedFunction() {
    MathEngine engine = MathEngine.create();
    engine.evaluate("double(x) := 2 * x");
    NodeConstant result = engine.evaluate("double(21)");

    assertThat(result.doubleValue()).isEqualTo(42.0);
}

@Test
void lambdaFunction() {
    MathEngine engine = MathEngine.create();
    NodeConstant result = engine.evaluate("map(x -> x^2, {1, 2, 3})");

    assertThat(result).isInstanceOf(NodeVector.class);
    NodeVector vec = (NodeVector) result;
    assertThat(vec.getElements()[0].doubleValue()).isEqualTo(1.0);
    assertThat(vec.getElements()[1].doubleValue()).isEqualTo(4.0);
    assertThat(vec.getElements()[2].doubleValue()).isEqualTo(9.0);
}
```

---

## Common Pitfalls for AI Agents

### 1. Not Checking Arity

**Problem:**

```java
// Calling function with wrong number of args
sin.apply(List.of(arg1, arg2), ctx);  // sin expects 1 arg!
```

**Solution:**
FunctionExecutor automatically checks arity before calling apply().

### 2. Not Handling Vector Arguments

**Problem:**

```java
// User calls: sum({1, 2, 3})
// But function only handles varargs
```

**Solution:**
Aggregate functions should handle both:

```java
if (args.size() == 1 && args.get(0) instanceof NodeVector) {
    // Extract vector elements
    NodeVector vec = (NodeVector) args.get(0);
    double[] values = extractValues(vec);
    return apply(values);
}
```

### 3. Ignoring Angle Units

**Problem:**

```java
// Always computing in radians
return Math.sin(x);
```

**Solution:**
Use TrigFunction base class:

```java
class SinFunction extends TrigFunction {
    @Override
    protected double applyInRadians(double radians) {
        return Math.sin(radians);  // Already converted
    }
}
```

### 4. Forgetting Lazy Evaluation

**Problem:**

```java
// if function evaluates all arguments
NodeConstant cond = args.get(0);
NodeConstant thenVal = args.get(1);  // Always evaluated!
NodeConstant elseVal = args.get(2);  // Always evaluated!
```

**Solution:**
Special handling in evaluator (don't evaluate arguments before passing to if):

```java
// In evaluator, NOT in function
if (conditionTrue) {
    return evaluate(thenExpr);
} else {
    return evaluate(elseExpr);
}
```

---

## Performance Considerations

### Function Lookup

Hash-based O(1) lookup:

```java
Map<String, MathFunction> functions;
MathFunction func = functions.get("sin");
```

### Alias Handling

All aliases stored in map pointing to same function:

```java
functions.put("ln", logFunction);
functions.put("log", logFunction);   // Alias
functions.put("log10", logFunction); // Alias
```

### Vector Operations

Avoid copying for aggregate functions:

```java
// SLOW
List<Double> copy = new ArrayList<>();
for (Node n : vec.getElements()) {
    copy.add(n.doubleValue());
}

// FASTER
double sum = 0;
for (Node n : vec.getElements()) {
    sum += n.doubleValue();
}
```

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[EVALUATOR.md](./EVALUATOR.md)** - How functions are called
- **[OPERATORS.md](./OPERATORS.md)** - Operator system (similar pattern)
- **[REGISTRIES.md](./REGISTRIES.md)** - Function registration
- **[NODES.md](./NODES.md)** - NodeFunction and NodeLambda
