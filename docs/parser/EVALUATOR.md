# Evaluator System

**Purpose:** Evaluate Abstract Syntax Trees (ASTs) to produce results

---

## Overview

The Evaluator is the final stage of the parser pipeline. It traverses the AST and computes results:

```
AST (Node tree)
    ↓
Evaluator.evaluate(node)
    ↓
Result (NodeConstant)
```

**Key Responsibilities:**

- Evaluate expressions recursively
- Manage execution context (variables, functions)
- Dispatch operators and functions
- Handle type coercion and broadcasting
- Track recursion depth
- Implement short-circuit evaluation

---

## Architecture

### Core Components

**1. Evaluator**

- Main evaluation coordinator
- Dispatches to appropriate handlers
- Manages context stack

**2. EvaluationContext**

- Stores variables and user-defined functions
- Maintains angle unit setting
- Provides context isolation for function calls

**3. Specialized Handlers**

- **VariableResolver** - Variable lookup
- **SubscriptHandler** - Indexing and slicing
- **ElementAssignmentHandler** - Writing one element of a collection
- **FunctionCallHandler** - Function invocation
- **ComprehensionHandler** - List comprehensions

**4. Executors**

- **OperatorExecutor** - Operator dispatch
- **FunctionExecutor** - Built-in function dispatch

```
┌─────────────────────────────────────┐
│         Evaluator (main)            │
├─────────────────────────────────────┤
│  evaluate(Node) → NodeConstant      │
│                                     │
│  Delegates to:                      │
│  ├─ VariableResolver                │
│  ├─ SubscriptHandler                │
│  ├─ ElementAssignmentHandler        │
│  ├─ FunctionCallHandler             │
│  └─ ComprehensionHandler            │
│                                     │
│  Uses:                              │
│  ├─ OperatorExecutor                │
│  └─ FunctionExecutor                │
└─────────────────────────────────────┘
```

---

## Evaluator Class

**File:** `evaluator/Evaluator.java`

### Main Evaluation Method

```java
public NodeConstant evaluate(Node node) {
    // NodeConstant subclasses that need special handling
    if (node instanceof NodeLambda lambda) {
        return functionCallHandler.evaluateLambda(lambda, context);
    }

    if (node instanceof NodeRange range) {
        return range.toVector();
    }

    if (node instanceof NodeVector vector) {
        return evaluateVectorElements(vector);
    }

    if (node instanceof NodeMatrix matrix) {
        return evaluateMatrixElements(matrix);
    }

    // Other NodeConstant subclasses returned directly
    if (node instanceof NodeConstant constant) {
        return constant;
    }

    // NodeExpression subclasses require evaluation
    if (node instanceof NodeVariable variable) {
        return variableResolver.resolve(variable, operatorContext);
    }

    // Reference symbols (explicit disambiguation)
    if (node instanceof NodeUnitRef unitRef) {
        return variableResolver.resolveUnitRef(unitRef.getUnitName(), context);
    }

    if (node instanceof NodeVarRef varRef) {
        return variableResolver.resolveVarRef(varRef.getVariableName(), context);
    }

    if (node instanceof NodeConstRef constRef) {
        return variableResolver.resolveConstRef(constRef.getConstantName(), context);
    }

    if (node instanceof NodeBinary binary) {
        return evaluateBinary(binary);
    }

    if (node instanceof NodeUnary unary) {
        return evaluateUnary(unary);
    }

    // ... other node types
}
```

### Node Evaluation Dispatch

**Strategy:** Pattern matching on node type

**NodeConstant Subclasses:**

- Directly evaluated values (numbers, strings, booleans)
- Return as-is OR evaluate internal elements

**NodeExpression Subclasses:**

- Unevaluated expressions
- Recursively evaluate subexpressions
- Apply operations

---

## EvaluationContext

**File:** `evaluator/EvaluationContext.java`

**Purpose:** Execution environment for evaluation

### Fields

```java
class EvaluationContext {
    private Map<String, NodeConstant> variables;
    private RecursionTracker recursionTracker;
    private AngleUnit angleUnit;
    private EvaluationContext parent;  // For lexical scoping
}
```

### Variable Management

```java
// Define in this scope
context.define("x", new NodeRational(5));

// Look up through the scope chain
Optional<NodeConstant> value = context.resolve("x");

// Update where it is defined, or define here if it is not defined anywhere
context.assign("x", new NodeRational(10));
```

### Scoping

A scope is never mutated in place for a call. `withBindings` returns a child holding the
arguments and pointing at this context as its parent, so lookups walk outwards and nothing
a call defines can leak back.

**Function and comprehension scoping:**

```java
EvaluationContext callScope = parent.withBindings(Map.of("x", argument));
// Evaluate the body against callScope
```

**Lambda scoping:** a lambda captures where it was written, not where it is called, by
flattening the chain it was defined in.

```java
EvaluationContext captured = context.snapshot();
```

---

## Specialized Handlers

### 1. VariableResolver

**File:** `evaluator/handler/VariableResolver.java`

**Purpose:** Context-aware variable resolution with support for reference symbols and intelligent implicit multiplication

**Key Features:**

1. **One resolution order** - the same priority wherever the identifier appears
2. **Reference Symbol Support** - Explicit disambiguation (`@unit`, `$var`, `#const`)
3. **Smart Implicit Multiplication** - Splits compound identifiers into variables, constants, and functions

#### Resolution Order

**Priority:** variable → user function → unit → implicit multiplication

A name defined in the session therefore shadows a unit or a built-in of the same name, so
`f := 5; 100f` is 500 and not 100 fahrenheit. A sigil is the way to override that: `@f` is
the unit, `$f` the variable, `#f` the constant.

```java
NodeConstant resolveName(String name, EvaluationContext context, OperatorContext opCtx) {
    // 1. Variables (highest priority - allows shadowing)
    if (context.isDefined(name)) {
        return context.resolve(name);
    }

    // 2. User-defined functions
    FunctionDefinition func = context.resolveFunction(name);
    if (func != null) {
        return new NodeFunction(func);
    }

    // 3. Units
    UnitRegistry unitRegistry = context.getUnitRegistry();
    if (unitRegistry != null && unitRegistry.isUnit(name)) {
        return NodeUnit.of(1.0, unitRegistry.get(name));
    }

    // 4. Implicit multiplication (split into parts)
    if (config.implicitMultiplication() && opCtx != null) {
        NodeConstant splitResult = trySplitIntoVariables(name, context, opCtx);
        if (splitResult != null) {
            return splitResult;
        }
    }

    throw new UndefinedVariableException(name);
}
```

#### Call Target Resolution

**Priority:** user function → builtin function (checked by caller) → variable

Used for: `f(x)` - the `f` is resolved in call target context

```java
NodeConstant resolveAsCallTarget(String name, EvaluationContext context) {
    // User functions take priority
    FunctionDefinition func = context.resolveFunction(name);
    if (func != null) {
        return new NodeFunction(func);
    }

    // Fall back to variable (could hold lambda)
    if (context.isDefined(name)) {
        return context.resolve(name);
    }

    throw new UndefinedVariableException(name);
}
```

#### Postfix Unit Resolution

**Priority:** unit → variable → implicit multiplication

Used for: `100m` - the `m` is resolved in postfix unit context

```java
NodeConstant resolveAsPostfixUnit(String name, EvaluationContext context, OperatorContext opCtx) {
    // Units have priority after numbers
    UnitRegistry unitRegistry = context.getUnitRegistry();
    if (unitRegistry != null && unitRegistry.isUnit(name)) {
        return NodeUnit.of(1.0, unitRegistry.get(name));
    }

    // Fall back to variable
    if (context.isDefined(name)) {
        return context.resolve(name);
    }

    // Try implicit multiplication
    if (config.implicitMultiplication() && opCtx != null) {
        NodeConstant splitResult = trySplitIntoVariables(name, context, opCtx);
        if (splitResult != null) {
            return splitResult;
        }
    }

    throw new UndefinedVariableException(name);
}
```

#### Reference Symbol Resolution

**Explicit Disambiguation:**

```java
// @unit - Force unit resolution
NodeConstant resolveUnitRef(String unitName, EvaluationContext context) {
    UnitRegistry unitRegistry = context.getUnitRegistry();
    if (unitRegistry == null || !unitRegistry.isUnit(unitName)) {
        throw new UndefinedVariableException("Unknown unit: @" + unitName);
    }
    return NodeUnit.of(1.0, unitRegistry.get(unitName));
}

// $var - Force variable resolution
NodeConstant resolveVarRef(String varName, EvaluationContext context) {
    if (!context.isDefined(varName)) {
        throw new UndefinedVariableException("Undefined variable: $" + varName);
    }
    return context.resolve(varName);
}

// #const - Force constant resolution
NodeConstant resolveConstRef(String constName, EvaluationContext context) {
    return context.getConfig().constantRegistry()
            .getValue(constName)
            .orElseThrow(() -> new UndefinedVariableException("Undefined constant: #" + constName));
}
```

#### Implicit Multiplication Enhancement

**Splits compound identifiers into resolvable parts:**

```text
// "xy" where x=2, y=3 → 2 * 3 = 6
// "xpi" where x=2 → 2 * π
// "abc" where a=1, b=2, c=3 → 1 * 2 * 3 = 6
NodeConstant trySplitIntoVariables(String name, EvaluationContext context, OperatorContext opCtx) {
    if (name.length() <= 1) {
        return null;
    }
    return splitAndMultiply(name, 0, context, opCtx);
}
```

**Resolution Priority for Each Part:**

1. Variables (user-defined)
2. Constants (from constant registry)
3. User-defined functions

**Example:**

```text
// x := 2
context.define("x", new NodeRational(2));

// xpi → x * pi
NodeConstant result = variableResolver.resolve(new NodeVariable("xpi"), opCtx);
// Returns: 2 * π ≈ 6.28
```

### 2. SubscriptHandler

**File:** `evaluator/handler/SubscriptHandler.java`

**Purpose:** Handle indexing and slicing

**Operations:**

- Single index: `v[0]`, `m[1, 2]`
- Slices: `v[1:3]`, `m[1:3, 2:4]`
- Open slices: `v[:5]`, `v[2:]`, `m[:, 1]`

**Example:**

```java
// {10, 20, 30}[1]
NodeVector vec = new NodeVector(...);
NodeSubscript sub = new NodeSubscript(
        vec,
        List.of(new SliceArg(new NodeDouble(1), null))
);
NodeConstant result = subscriptHandler.evaluate(sub);  // NodeDouble(20)
```

**Slice Semantics:**

- Start: inclusive
- End: exclusive
- Negative indices: from end (-1 = last element)

**Matrix Subscripting:**

```java
m[i,j]        // Element
m[i,:]        // Row i (all columns)
m[:,j]        // Column j (all rows)
m[1:3,2:4]    // Sub-matrix
```

### 3. ElementAssignmentHandler

**File:** `evaluator/handler/ElementAssignmentHandler.java`

**Purpose:** Write one element of a vector, a matrix or a string

**Operations:**

- Vector element: `v[0] := 5`, `v[-1] := 5`
- Matrix element: `m[1, 2] := 9`
- Matrix row: `m[0] := {7, 8}`, keeping the width
- String character: `s[0] := "H"`, a single character
- Chains: `m[0][1] := 9`, `v[0][0] := "A"`

A value is immutable, so the write is a copy:

```text
update(container, groups, value):
    if groups is empty:  return value
    head        = groups.first
    current     = element of container at head
    replacement = update(current, groups.rest, value)
    return a copy of container with head replaced by replacement
```

The name is rebound with `EvaluationContext.assign`, which walks to the scope that defines
it, and the assignment answers the value assigned.

Index rules come from `IndexResolver`, shared with `SubscriptHandler`. A slice target is
refused, writing past the end does not grow the collection, and a target that holds no
elements is a `TypeError`.

### 4. FunctionCallHandler

**File:** `evaluator/handler/FunctionCallHandler.java`

**Purpose:** Handle all function calls

**Function Types:**

**Built-in Functions:**

```java
// sin(x)
NodeConstant result = functionExecutor.execute("sin", List.of(arg), context);
```

**User-Defined Functions:**

```java
// f(x) := x^2; f(5)
FunctionDefinition def = context.resolveFunction("f").orElseThrow();

// A child scope holding the arguments, with the caller as its parent
EvaluationContext callScope = context.withBindings(Map.of("x", new NodeRational(5)));

NodeConstant result = evaluator.evaluate(def.getBody(), callScope);
```

**Lambda Functions:**

```java
// (x -> x^2)(5)
NodeConstant result = functionCallHandler.evaluate(
        new NodeCall(lambda, List.of(new NodeRational(5))),
        context
);
```

**Special: Lazy Evaluation (if function)**

```java
// if(condition, thenExpr, elseExpr) evaluates one branch, never both
return TypeCoercion.toBoolean(condition) ? evaluate(thenExpr) : evaluate(elseExpr);
```

### 5. ComprehensionHandler

**File:** `evaluator/handler/ComprehensionHandler.java`

**Purpose:** Evaluate list comprehensions

**Syntax:**

```
{expression for variable in iterable if condition}
```

**Algorithm:**

```text
1.Evaluate iterable →
get collection
2.
For each
element in
collection:
a.Create new context
b.Bind variable
to element
c.If condition
specified:
        -
Evaluate condition
      -Skip if false
d.Evaluate expression
e.Add result
to output
vector
3.
Return NodeVector
of results
```

**Example:**

```java
// {x^2 for x in 1..5 if x mod 2 == 0}
NodeComprehension comp = ...;
NodeConstant result = comprehensionHandler.evaluate(comp, context);
// Returns: {4, 16} (squares of 2 and 4)
```

---

## Binary Operation Evaluation

**File:** `evaluator/Evaluator.java` - `evaluateBinary()` method

### Standard Evaluation

```java
private NodeConstant evaluateBinary(NodeBinary node) {
    TokenType opType = node.getOperator().getType();

    // Short-circuit for logical operators
    if (opType == TokenType.AND || opType == TokenType.OR) {
        return evaluateWithShortCircuit(node, opType);
    }

    // Eager evaluation
    NodeConstant left = evaluate(node.getLeft());
    NodeConstant right = evaluate(node.getRight());

    return operatorExecutor.executeBinary(opType, left, right, context);
}
```

### Short-Circuit Evaluation

**For && and ||:**

```java
private NodeConstant evaluateWithShortCircuit(NodeBinary node, TokenType opType) {
    NodeConstant left = evaluate(node.getLeft());

    return operatorExecutor.executeBinaryShortCircuit(
            opType,
            left,
            () -> evaluate(node.getRight()),  // Lazy supplier
            context
    );
}
```

**Behavior:**

```java
false&&expensive()  // expensive() NOT called
true||

expensive()   // expensive() NOT called
```

---

## Type Handling

### Type Coercion

**File:** `util/TypeCoercion.java`

**Utilities:**

```java
boolean isNumeric(NodeConstant node);

NodeNumber toNumber(NodeConstant node);

boolean toBoolean(NodeConstant node);

String typeName(NodeConstant node);
```

**Numeric Promotion:** not the evaluator's decision. Two nodes meeting in an operation go
through `NodeArithmetic`, reached by `NodeConstant.add` and friends, which is also what
operators and functions call. Exact meeting exact stays exact; anything meeting a
`NodeDouble` gives a double. See the dispatch order in that class.

### Broadcasting

Handled by `util/BroadcastingEngine`:

**Scalar to Vector:**

```text
5+{1,2,3}  →  {6,7,8}
```

**Vector Size Normalization:**

```text
{1,2}+{3,4,5}  →  {1,2,0}+{3,4,5}={4,6,5}
```

---

## Recursion Tracking

**File:** `evaluator/RecursionTracker.java`

**Purpose:** Prevent stack overflow

**Mechanism:**

```java
class RecursionTracker {
    private int depth = 0;
    private final int maxDepth;

    void enter(String functionName) {
        depth++;
        if (depth > maxDepth) {
            throw new StackOverflowException("Max recursion depth exceeded");
        }
    }

    void exit() {
        depth--;
    }
}
```

**Usage in Function Calls:**

```java
recursionTracker.enter("fib");
try {
    // Evaluate function body
} finally {
    recursionTracker.exit();
}
```

**Configuration:**

```java
MathEngineConfig config = MathEngineConfig.builder()
        .maxRecursionDepth(256)  // Default
        .build();
```

---

## Assignment Evaluation

**Variable Assignment:**

```java
// x := 5
private NodeConstant evaluateAssignment(NodeAssignment node) {
    NodeConstant value = evaluate(node.getValue());
    context.assign(node.getIdentifier(), value);
    return value;
}
```

**Element Assignment:**

```java
// v[0] := 5
private NodeConstant evaluate(NodeElementAssignment node, EvaluationContext context) {
    NodeConstant target = context.resolve(node.getIdentifier()).orElseThrow(...);
    NodeConstant value = evaluate(node.getValue(), context);
    context.assign(node.getIdentifier(), update(target, node.getIndexGroups(), 0, value, context));
    return value;
}
```

**Function Definition:**

```java
// f(x) := x^2
private NodeConstant evaluateFunctionDef(NodeFunctionDef node) {
    FunctionDefinition def = new FunctionDefinition(
            node.getName(),
            node.getParameters(),
            node.getBody()
    );
    NodeFunction func = new NodeFunction(def);
    context.define(node.getName(), func);
    return func;
}
```

---

## Error Handling

### Exception Types

```java
// Undefined variable
throw new UndefinedVariableException("Variable 'x' not defined");

// Type error
throw new TypeError("Cannot add string and number");

// Arity error (wrong argument count)
throw new ArityException("Function 'max' expects 2 arguments, got 3");

// Stack overflow
throw new StackOverflowException("Max recursion depth (1000) exceeded");

// General evaluation error
throw new EvaluationException("Division by zero");
```

### Error Context

All exceptions include:

- Source location (token)
- Error message
- Stack trace (for recursion errors)

---

## Testing Evaluator

### Basic Evaluation

```java

@Test
void evaluateArithmetic() {
    MathEngine engine = MathEngine.create();
    NodeConstant result = engine.evaluate("2 + 3");
    assertThat(result.doubleValue()).isEqualTo(5.0);
}
```

### Type Handling

```java

@Test
void typePromotion() {
    MathEngine engine = MathEngine.create();
    NodeConstant result = engine.evaluate("1/2 + 0.5");

    // Rational promoted to double
    assertThat(result).isInstanceOf(NodeDouble.class);
    assertThat(result.doubleValue()).isEqualTo(1.0);
}
```

### Variable Context

```java

@Test
void variableScoping() {
    MathEngine engine = MathEngine.create();
    engine.evaluate("x := 10");
    engine.evaluate("f(x) := x^2");

    // Call with different x
    NodeConstant result = engine.evaluate("f(5)");
    assertThat(result.doubleValue()).isEqualTo(25.0);

    // Original x unchanged
    result = engine.evaluate("x");
    assertThat(result.doubleValue()).isEqualTo(10.0);
}
```

### Recursion

```java

@Test
void recursiveFunction() {
    MathEngine engine = MathEngine.create();
    engine.evaluate("factorial(n) := if(n <= 1, 1, n * factorial(n-1))");

    NodeConstant result = engine.evaluate("factorial(5)");
    assertThat(result.doubleValue()).isEqualTo(120.0);
}
```

---

## Performance Considerations

### Lazy Evaluation

**Ranges:** Not materialized until needed

```java
NodeRange range = new NodeRange(1, 1000000, null);
// Only converted to vector when accessed
```

**Conditional:** Only one branch evaluated

```java
if(false,expensive(),cheap())  // Only evaluates cheap()
```

### Context Reuse

For compiled expressions:

```java
// Parsed once, then evaluated with a fresh binding each time
CompiledExpression expr = engine.compile("x^2 + 2*x + 1");
for (int i = 0; i < 1000; i++) {
    expr.evaluateDouble("x", i);
}
```

### Operator Dispatch

TokenType-based dispatch is O(1):

```java
Map<TokenType, BinaryOperator> operators;
BinaryOperator op = operators.get(TokenType.PLUS);  // Constant time
```
---

## Common Pitfalls for AI Agents

### 1. Forgetting to Evaluate Subexpressions

**Problem:**

```java
// WRONG - using NodeExpression directly
NodeConstant left = binary.getLeft();  // Still a NodeBinary!
double value = left.doubleValue();     // ClassCastException
```

**Solution:**

```java
// RIGHT - evaluate first
NodeConstant left = evaluate(binary.getLeft());
double value = left.doubleValue();
```

### 2. Mutating Context Incorrectly

**Problem:**

```java
// WRONG: defines into the caller's own scope
context.define("x", newValue);
```

**Solution:**

```java
// RIGHT: a child scope holding the binding, with the caller as its parent
EvaluationContext callScope = context.withBindings(Map.of("x", newValue));
```

### 3. Not Handling NodeConstant Subclasses

**Problem:**

```java
// WRONG: a range or a vector of expressions is a NodeConstant that is not finished yet
if (node instanceof NodeConstant constant) {
    return constant;
}
```

**Solution:**

```java
if (node instanceof NodeRange range) {
    return range.toVector();              // build the elements
}
if (node instanceof NodeVector vector) {
    return evaluateVectorElements(vector); // evaluate each element
}
// Then the other NodeConstant types
```

### 4. Forgetting Short-Circuit Evaluation

**Problem:**

```java
// WRONG - always evaluates both sides
NodeConstant left = evaluate(binary.getLeft());
NodeConstant right = evaluate(binary.getRight());
return left &&right;
```

**Solution:**

```java
// RIGHT - short-circuit
NodeConstant left = evaluate(binary.getLeft());
if(!left.

booleanValue()){
        return NodeBoolean.FALSE;  // Don't evaluate right
}
        return

evaluate(binary.getRight());
```

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[PARSER.md](./PARSER.md)** - AST construction
- **[NODES.md](./NODES.md)** - Node types evaluated
- **[OPERATORS.md](./OPERATORS.md)** - Operator execution
- **[FUNCTIONS.md](./FUNCTIONS.md)** - Function execution
