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
        return variableResolver.resolve(variable, ResolutionContext.GENERAL, operatorContext);
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
// Define variable
context.define("x",new NodeDouble(5.0));

// Lookup variable
NodeConstant value = context.get("x");

// Check existence
boolean exists = context.has("x");

// Assign (update if exists, create if not)
context.

assign("x",new NodeDouble(10.0));
```

### Scoping

**Flat Scoping:** Variables defined at top level

**Function Scoping:** New context created for function calls

```java
EvaluationContext funcContext = new EvaluationContext(parentContext);
funcContext.

define("param",argValue);
// Evaluate function body in funcContext
```

**Lambda Scoping:** Captures parent context

```java
EvaluationContext lambdaContext = new EvaluationContext(capturedContext);
lambdaContext.

define("param",argValue);
```

---

## Specialized Handlers

### 1. VariableResolver

**File:** `evaluator/handler/VariableResolver.java`

**Purpose:** Context-aware variable resolution with support for reference symbols and intelligent implicit multiplication

**Key Features:**

1. **Context-Aware Resolution** - Different priority based on syntactic position
2. **Reference Symbol Support** - Explicit disambiguation (`@unit`, `$var`, `#const`)
3. **Smart Implicit Multiplication** - Splits compound identifiers into variables, constants, and functions

#### Resolution Contexts

**ResolutionContext Types:**

```java
enum ResolutionContext {
    GENERAL,          // x + 1 (variable → function → unit → implicit mult)
    CALL_TARGET,      // f(x) (function → variable)
    POSTFIX_UNIT,     // 100m (unit → variable → implicit mult)
    ASSIGNMENT_TARGET // x := (not resolved, just stored)
}
```

#### General Context Resolution

**Priority:** variable → user function → unit → implicit multiplication

```java
NodeConstant resolveAsGeneral(String name, EvaluationContext context, OperatorContext opCtx) {
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

```java
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

```java
// x := 2
context.define("x", new NodeRational(2));

// xpi → x * pi
NodeConstant result = variableResolver.resolve(
    new NodeVariable("xpi"),
    ResolutionContext.GENERAL,
    opCtx
);
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

### 3. FunctionCallHandler

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
FunctionDefinition def = context.get("f").getFunction();

// Create new context for function
EvaluationContext funcContext = new EvaluationContext(context);
funcContext.

define("x",new NodeDouble(5));

// Evaluate body
NodeConstant result = evaluator.evaluate(def.getBody());
```

**Lambda Functions:**

```java
// (x -> x^2)(5)
NodeLambda lambda = ...;
NodeConstant result = functionCallHandler.evaluate(
        new NodeCall(lambda, List.of(new NodeDouble(5))),
        context
);
```

**Special: Lazy Evaluation (if function)**

```java
// if(condition, thenExpr, elseExpr)
// Only evaluates ONE branch, not both
if(condition.booleanValue()){
        return

evaluate(thenExpr);
}else{
        return

evaluate(elseExpr);
}
```

### 4. ComprehensionHandler

**File:** `evaluator/handler/ComprehensionHandler.java`

**Purpose:** Evaluate list comprehensions

**Syntax:**

```
{expression for variable in iterable if condition}
```

**Algorithm:**

```java
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

**Numeric Promotion:**

```java
if(left instanceof NodeRational &&right instanceof NodeRational){
        // Stay rational (exact)
        return new

NodeRational(leftRat.add(rightRat));
        }else{
        // Promote to double
        return new

NodeDouble(left.doubleValue() +right.

doubleValue());
        }
```

### Broadcasting

Handled by operators via `BroadcastingDispatcher`:

**Scalar to Vector:**

```java
5+{1,2,3}  →  {6,7,8}
```

**Vector Size Normalization:**

```java
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
try{
        // Evaluate function body
        }finally{
        recursionTracker.

exit();
}
```

**Configuration:**

```java
MathEngineConfig config = MathEngineConfig.builder()
        .maxRecursionDepth(1000)  // Default
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
throw new

TypeError("Cannot add string and number");

// Arity error (wrong argument count)
throw new

ArityException("Function 'max' expects 2 arguments, got 3");

// Stack overflow
throw new

StackOverflowException("Max recursion depth (1000) exceeded");

// General evaluation error
throw new

EvaluationException("Division by zero");
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
CompiledExpression expr = engine.compile("x^2 + 2*x + 1");

// Context created once, variables updated
for(
int i = 0;
i< 1000;i++){
        expr.

evaluateDouble("x",i);
}
```

### Operator Dispatch

TokenType-based dispatch is O(1):

```java
Map<TokenType, BinaryOperator> operators;
BinaryOperator op = operators.get(TokenType.PLUS);  // Constant time
```

---

## Extension Points

### Custom Evaluator Behavior

Extend `Evaluator` or provide custom handlers:

```java
class CustomEvaluator extends Evaluator {
    @Override
    public NodeConstant evaluate(Node node) {
        // Custom pre-processing
        if (node instanceof CustomNode custom) {
            return handleCustomNode(custom);
        }

        // Delegate to standard evaluation
        return super.evaluate(node);
    }
}
```

### Custom Context

```java
class CustomContext extends EvaluationContext {
    @Override
    public NodeConstant get(String name) {
        // Custom variable resolution logic
        if (name.startsWith("$")) {
            return resolveSpecialVariable(name);
        }
        return super.get(name);
    }
}
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
// WRONG - modifying parent context in function
context.define("x",newValue);  // Pollutes parent scope!
```

**Solution:**

```java
// RIGHT - create new context for function
EvaluationContext funcContext = new EvaluationContext(context);
funcContext.

define("x",newValue);
```

### 3. Not Handling NodeConstant Subclasses

**Problem:**

```java
if(node instanceof NodeConstant){
        return(NodeConstant)node;  // WRONG for NodeRange, NodeVector
}
```

**Solution:**

```java
if(node instanceof
NodeRange range){
        return range.

toVector();  // Evaluate range first
}
        if(node instanceof
NodeVector vector){
        return

evaluateVectorElements(vector);  // Evaluate elements
}
// Then check other NodeConstant types
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
