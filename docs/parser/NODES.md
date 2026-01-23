# AST Node Hierarchy

**Purpose:** Complete documentation of the Abstract Syntax Tree node types

---

## Node Hierarchy

```
Node (abstract base class)
│
├─ NodeConstant (evaluated, immutable values)
│   │
│   ├─ NodeNumber (numeric values)
│   │   ├─ NodeDouble        - IEEE 754 double precision
│   │   ├─ NodeRational      - Exact rational (BigRational)
│   │   ├─ NodePercent       - Percentage value (auto /100)
│   │   └─ NodeBoolean       - Boolean (true=1, false=0)
│   │
│   ├─ NodeString            - String values
│   ├─ NodeUnit              - Value with physical unit
│   ├─ NodeVector            - 1D array of nodes
│   ├─ NodeMatrix            - 2D array of nodes
│   ├─ NodeRange             - Lazy sequence (start..end..step)
│   ├─ NodeFunction          - User-defined function
│   └─ NodeLambda            - Anonymous function (x -> expr)
│
└─ NodeExpression (unevaluated AST nodes)
    ├─ NodeBinary            - Binary operations (left op right)
    ├─ NodeUnary             - Unary operations (op operand)
    ├─ NodeCall              - Function calls
    ├─ NodeSubscript         - Indexing/slicing
    ├─ NodeVariable          - Variable/identifier reference
    ├─ NodeUnitRef           - Explicit unit reference (@unit, @"km/h")
    ├─ NodeVarRef            - Explicit variable reference ($var)
    ├─ NodeConstRef          - Explicit constant reference (#const)
    ├─ NodeAssignment        - Variable assignment (x := value)
    ├─ NodeFunctionDef       - Function definition (f(x) := expr)
    ├─ NodeRangeExpression   - Range before evaluation
    ├─ NodeUnitConversion    - Unit conversion (value in unit)
    ├─ NodeComprehension     - List comprehension
    └─ NodeSequence          - Multiple statements (stmt1; stmt2)
```

---

## NodeConstant (Evaluated Values)

**Base class for all evaluated, immutable values**

**Key Properties:**

- Immutable (all fields final)
- Can be returned directly from evaluation
- Have concrete values

### Common Methods

All NodeConstant subclasses implement:

```java
double doubleValue()      // Convert to double
String toString()         // String representation
```

---

## Numeric Types

### NodeDouble

**Purpose:** IEEE 754 double-precision floating-point numbers

**When Created:**

- Parsing decimal literals: `3.14`, `.5`, `1e3`
- Operations that produce non-rational results: `sqrt(2)`, `sin(1)`
- Explicit double operations

**Fields:**

```java
private final double value;
```

**Example:**

```java
NodeDouble d = new NodeDouble(3.14159);
double v = d.doubleValue();  // 3.14159
```

**Precision:** 15-17 significant decimal digits

### NodeRational

**Purpose:** Exact rational numbers using BigRational

**When Created:**

- Parsing integer literals: `42`, `-17`
- Parsing rational literals: `22/7`, `1/3`
- Rational arithmetic that stays exact: `1/2 + 1/3` → `5/6`

**Fields:**

```java
private final BigRational value;  // Immutable numerator/denominator pair
```

**Example:**

```java
NodeRational r = new NodeRational(new BigRational(22, 7));
double v = r.doubleValue();  // 3.142857...
String s = r.toString();     // "22/7"
```

**Automatic Simplification:**

```java
new NodeRational(4, 6)  →  stores as 2/3 (simplified)
```

**Promotion to Double:**
When mixed with doubles in operations:

```java
NodeRational(5, 2) + NodeDouble(3.5)  →  NodeDouble(6.0)
```

### NodeBoolean

**Purpose:** Boolean values (true/false)

**When Created:**

- Parsing boolean literals: `true`, `false`
- Comparison operations: `x < 5`
- Logical operations: `a && b`

**Fields:**

```java
private final boolean value;
```

**Numeric Representation:**

- `true` → `1.0`
- `false` → `0.0`

**Example:**

```java
NodeBoolean b = new NodeBoolean(true);
double v = b.doubleValue();  // 1.0
```

**Type Promotion:**

```java
true + 5      →  NodeDouble(6.0)
false * 10    →  NodeDouble(0.0)
```

### NodePercent

**Purpose:** Percentage values with automatic /100 conversion

**When Created:**

- Parsing percent literals: `50%`
- Postfix percent operator: `expression%`

**Fields:**

```java
private final double value;  // Already divided by 100
```

**Example:**

```java
NodePercent p = new NodePercent(50.0);  // Stores 0.5
double v = p.doubleValue();  // 0.5
String s = p.toString();     // "50%"
```

**Operations:**

```java
50%           →  NodePercent(50) → value 0.5
50% of 200    →  NodeDouble(100.0)
50% + 25%     →  NodePercent(75) → value 0.75
```

---

## String Type

### NodeString

**Purpose:** String values

**When Created:**

- Parsing string literals: `"hello"`, `'world'`

**Fields:**

```java
private final String value;
```

**Example:**

```java
NodeString s = new NodeString("hello");
String v = s.getValue();  // "hello"
```

**No Implicit Conversion:**

- Cannot convert to/from numbers
- String operations separate from numeric operations

---

## Physical Units

### NodeUnit

**Purpose:** Values with physical units (length, mass, temperature, etc.)

**When Created:**

- Literal with unit: `100 meters`
- Unit conversion: `100 meters in feet`
- Unit arithmetic: `5 meters + 3 meters`

**Fields:**

```java
private final double value;
private final UnitDefinition unit;
```

**Example:**

```java
UnitDefinition meters = unitRegistry.get("meters");
NodeUnit distance = NodeUnit.of(100.0, meters);

// Convert
UnitDefinition feet = unitRegistry.get("feet");
NodeUnit converted = distance.convertTo(feet);  // ~328.084 feet
```

**Unit Arithmetic:**

```java
5 meters + 3 meters       →  NodeUnit(8, meters)
100 meters in feet        →  NodeUnit(328.084, feet)
5 meters + 10 feet        →  ERROR (unit mismatch)
```

**Dimensionless Result:**

```java
10 meters / 5 meters      →  NodeDouble(2.0)  // Units cancel
```

---

## Collection Types

### NodeVector

**Purpose:** 1D arrays of elements

**When Created:**

- Parsing vector literals: `{1, 2, 3}`
- Range evaluation: `1..5` → `{1, 2, 3, 4, 5}`
- Vector operations: `{1, 2} + {3, 4}` → `{4, 6}`

**Fields:**

```java
private final Node[] elements;  // Heterogeneous types allowed
```

**Example:**

```java
Node[] elems = { new NodeDouble(1), new NodeDouble(2), new NodeDouble(3) };
NodeVector v = new NodeVector(elems);
int size = v.size();  // 3
```

**Element Access:**

```java
NodeVector v = ...;
Node elem = v.getElements()[0];
```

**Broadcasting:**

```java
{1, 2, 3} + 5           →  {6, 7, 8}
{1, 2, 3} * 2           →  {2, 4, 6}
{1, 2} + {3, 4, 5}      →  {4, 6, 5}  // Shorter extended with zeros
```

**Lazy Evaluation:**
Elements may be unevaluated expressions. Evaluator must evaluate elements when needed.

### NodeMatrix

**Purpose:** 2D arrays of elements

**When Created:**

- Parsing matrix literals: `[1, 2; 3, 4]` or `[[1, 2], [3, 4]]`
- Matrix operations: `A @ B`

**Fields:**

```java
private final Node[][] elements;  // rows[cols]
```

**Example:**

```java
// 2x2 matrix:
// [1, 2]
// [3, 4]
Node[][] rows = {
    { new NodeDouble(1), new NodeDouble(2) },
    { new NodeDouble(3), new NodeDouble(4) }
};
NodeMatrix m = new NodeMatrix(rows);
```

**Dimensions:**

```java
int rows = m.getRowCount();      // 2
int cols = m.getColumnCount();   // 2
```

**Matrix Operations:**

```java
[1, 2; 3, 4] + [5, 6; 7, 8]       →  [6, 8; 10, 12]
[1, 2; 3, 4] @ [5, 6; 7, 8]       →  [19, 22; 43, 50]  // Matrix multiply
[1, 2; 3, 4] * 2                  →  [2, 4; 6, 8]      // Scalar multiply
```

### NodeRange

**Purpose:** Lazy sequence generation (start..end..step)

**When Created:**

- Parsing range expressions: `1..10`, `0..1 step 0.1`

**Fields:**

```java
private final NodeNumber start;
private final NodeNumber end;
private final NodeNumber step;  // Nullable (default depends on direction)
```

**Lazy Evaluation:**
NodeRange doesn't store elements. It generates them on demand when converted to NodeVector.

**Conversion to Vector:**

```java
NodeRange range = new NodeRange(
    new NodeRational(1, 1),
    new NodeRational(5, 1),
    null  // Default step = 1
);
NodeVector vector = range.toVector();  // {1, 2, 3, 4, 5}
```

**Step Calculation:**

- If not specified:
    - start < end → step = 1
    - start > end → step = -1
- If specified: use given step

**Inclusive/Exclusive:**

- Start is inclusive
- End is inclusive

**Examples:**

```java
1..5              →  {1, 2, 3, 4, 5}
1..10 step 2      →  {1, 3, 5, 7, 9}
10..1 step -1     →  {10, 9, 8, 7, 6, 5, 4, 3, 2, 1}
0..1 step 0.1     →  {0.0, 0.1, 0.2, ..., 1.0}
```

---

## Function Types

### NodeFunction

**Purpose:** User-defined functions

**When Created:**

- Function definition: `f(x) := x^2`

**Fields:**

```java
private final FunctionDefinition function;
```

**FunctionDefinition Structure:**

```java
class FunctionDefinition {
    String name;
    List<String> parameters;
    Node body;  // Unevaluated expression
}
```

**Example:**

```java
// f(x, y) := x^2 + y^2
FunctionDefinition def = new FunctionDefinition(
    "f",
    List.of("x", "y"),
    new NodeBinary(
        new NodeBinary(
            new NodeVariable("x"),
            TokenType.POWER,
            new NodeDouble(2)
        ),
        TokenType.PLUS,
        new NodeBinary(
            new NodeVariable("y"),
            TokenType.POWER,
            new NodeDouble(2)
        )
    )
);
NodeFunction func = new NodeFunction(def);
```

**Calling:**

```java
// Handled by FunctionCallHandler in evaluator
NodeCall call = new NodeCall(func, List.of(arg1, arg2));
```

### NodeLambda

**Purpose:** Anonymous functions (lambda expressions)

**When Created:**

- Parsing lambda expressions: `x -> x^2`, `(a, b) -> a + b`

**Fields:**

```java
private final List<String> parameters;
private final Node body;
```

**Example:**

```java
// x -> x^2
NodeLambda lambda = new NodeLambda(
    List.of("x"),
    new NodeBinary(
        new NodeVariable("x"),
        TokenType.POWER,
        new NodeDouble(2)
    )
);
```

**Conversion to NodeFunction:**
During evaluation, lambdas are converted to anonymous NodeFunction objects:

```java
FunctionDefinition def = new FunctionDefinition(
    "<lambda>",  // Anonymous name
    lambda.getParameters(),
    lambda.getBody()
);
return new NodeFunction(def);
```

**Usage:**

```java
map(x -> x^2, {1, 2, 3})     →  {1, 4, 9}
filter(x -> x > 5, 1..10)    →  {6, 7, 8, 9, 10}
```

---

## Expression Nodes (Unevaluated)

### NodeBinary

**Purpose:** Binary operations (two operands)

**Fields:**

```java
private final Node left;
private final Token operator;
private final Node right;
```

**Example:**

```java
// 2 + 3
NodeBinary add = new NodeBinary(
    new NodeDouble(2),
    new Token(TokenType.PLUS, "+", ...),
    new NodeDouble(3)
);
```

**Evaluation:**

```java
NodeConstant result = evaluator.evaluate(add);  // NodeDouble(5)
```

### NodeUnary

**Purpose:** Unary operations (single operand)

**Fields:**

```java
private final Token operator;
private final Node operand;
```

**Example:**

```java
// -5
NodeUnary negate = new NodeUnary(
    new Token(TokenType.MINUS, "-", ...),
    new NodeDouble(5)
);
```

**Common Unary Operations:**

- Negate: `-x`
- Not: `not x`
- Factorial: `x!`
- Percent: `x%`

### NodeVariable

**Purpose:** Variable/identifier reference (context-aware resolution)

**Fields:**

```java
private final String name;
```

**Example:**

```java
NodeVariable x = new NodeVariable("x");
```

**Resolution:**
During evaluation, resolved by `VariableResolver` with context-aware priority:

```java
// General context: variable → function → unit → implicit mult
NodeConstant value = variableResolver.resolve(
    variable,
    ResolutionContext.GENERAL,
    operatorContext
);
```

**Resolution Contexts:**

- **GENERAL**: `x + 1` - variable → function → unit
- **CALL_TARGET**: `f(x)` - function → variable
- **POSTFIX_UNIT**: `100m` - unit → variable

**Undefined Variable:**
If not found and no implicit multiplication possible, throws `UndefinedVariableException`.

---

## Reference Nodes (Explicit Disambiguation)

### NodeUnitRef

**Purpose:** Force unit resolution, bypassing variable shadowing

**Syntax:** `@identifier` or `@"unit name"`

**Fields:**

```java
private final String unitName;
private final boolean quoted;  // true for @"km/h" syntax
```

**Example:**

```java
// Simple unit reference
NodeUnitRef meters = new NodeUnitRef("m", false);

// Quoted unit reference for multi-word units
NodeUnitRef speed = new NodeUnitRef("km/h", true);
```

**Evaluation:**

```java
NodeConstant value = variableResolver.resolveUnitRef(unitName, context);
// Always resolves as unit, even if variable 'm' exists
```

**Use Cases:**

```java
m := 5                  // Define variable 'm'
m                       // Returns 5 (variable)
@m                      // Returns 1 meter (forced unit)
100 * @m                // 100 meters

@"km/h"                 // Units with spaces/special chars
@"miles per hour"       // Multi-word units
```

**Error:**
If unit doesn't exist: `UndefinedVariableException("Unknown unit: @m")`

### NodeVarRef

**Purpose:** Force variable resolution, even in unit/function contexts

**Syntax:** `$identifier`

**Fields:**

```java
private final String variableName;
```

**Example:**

```java
NodeVarRef x = new NodeVarRef("x");
```

**Evaluation:**

```java
NodeConstant value = variableResolver.resolveVarRef(variableName, context);
// Always resolves as variable, bypasses unit/function priority
```

**Use Cases:**

```java
m := 3
100m                    // 100 meters (unit takes priority after number)
100$m                   // 100 * 3 = 300 (forced variable)

f := 10
f(x) := x^2            // Define function
f                       // Function object
$f                      // 10 (forced variable)
```

**Error:**
If variable not defined: `UndefinedVariableException("Undefined variable: $x")`

### NodeConstRef

**Purpose:** Force constant resolution, even when shadowed by variables

**Syntax:** `#identifier`

**Fields:**

```java
private final String constantName;
```

**Example:**

```java
NodeConstRef pi = new NodeConstRef("pi");
```

**Evaluation:**

```java
NodeConstant value = variableResolver.resolveConstRef(constantName, context);
// Accesses constant registry directly, bypassing variables
```

**Use Cases:**

```java
pi := 3.0               // Shadow pi constant with variable
pi                      // Returns 3.0 (variable)
#pi                     // Returns π ≈ 3.14159... (constant)

e := 10
2 * #e                  // Uses Euler's number, not variable
```

**Available Constants:**

- `#pi` - π (3.14159...)
- `#e`, `#euler` - Euler's number (2.71828...)
- `#phi`, `#goldenratio` - Golden ratio (1.61803...)
- `#tau` - 2π (6.28318...)
- `#inf`, `#infinity` - Positive infinity
- `#nan` - Not-a-Number

**Error:**
If constant not in registry: `UndefinedVariableException("Undefined constant: #xyz")`

---

### NodeAssignment

**Purpose:** Variable assignment

**Fields:**

```java
private final String identifier;
private final Node value;
```

**Example:**

```java
// x := 5
NodeAssignment assign = new NodeAssignment("x", new NodeDouble(5));
```

**Evaluation:**

```java
NodeConstant result = evaluator.evaluate(value);
context.define(identifier, result);
return result;
```

### NodeFunctionDef

**Purpose:** Function definition

**Fields:**

```java
private final String name;
private final List<String> parameters;
private final Node body;
```

**Example:**

```java
// f(x, y) := x + y
NodeFunctionDef def = new NodeFunctionDef(
    "f",
    List.of("x", "y"),
    new NodeBinary(...)
);
```

**Evaluation:**

```java
FunctionDefinition funcDef = new FunctionDefinition(name, parameters, body);
NodeFunction func = new NodeFunction(funcDef);
context.define(name, func);
return func;
```

### NodeCall

**Purpose:** Function call

**Fields:**

```java
private final Node function;  // NodeFunction, NodeLambda, or NodeVariable
private final List<Node> arguments;
```

**Example:**

```java
// sin(pi)
NodeCall call = new NodeCall(
    new NodeVariable("sin"),  // Resolved to function during evaluation
    List.of(new NodeVariable("pi"))
);
```

**Evaluation Types:**

1. **Built-in function:** `sin(x)`, `max(a, b)`
2. **User-defined function:** `f(x)` where `f` defined by user
3. **Lambda call:** `(x -> x^2)(5)`
4. **Higher-order:** `map(x -> x^2, {1,2,3})`

### NodeSubscript

**Purpose:** Indexing and slicing

**Fields:**

```java
private final Node target;           // Vector or Matrix
private final List<SliceArg> args;   // Indices or slices
```

**SliceArg Structure:**

```java
class SliceArg {
    Node start;   // Nullable (means from beginning)
    Node end;     // Nullable (means to end)
}
```

**Examples:**

```java
v[0]          // Single index
v[1:3]        // Slice (start:end)
v[:5]         // From beginning to 5
v[2:]         // From 2 to end
m[0, 1]       // Matrix element
m[1, :]       // Matrix row
m[:, 2]       // Matrix column
m[1:3, 2:4]   // Matrix sub-matrix
```

### NodeRangeExpression

**Purpose:** Range expression before evaluation

**Fields:**

```java
private final Node start;
private final Node end;
private final Node step;  // Nullable
```

**Example:**

```java
// x..y
NodeRangeExpression range = new NodeRangeExpression(
    new NodeVariable("x"),
    new NodeVariable("y"),
    null
);
```

**Evaluation:**
Evaluates start, end, step expressions, then creates NodeRange, then converts to NodeVector.

### NodeUnitConversion

**Purpose:** Unit conversion expression

**Fields:**

```java
private final Node value;
private final String targetUnit;
```

**Example:**

```java
// 100 meters in feet
NodeUnitConversion conv = new NodeUnitConversion(
    new NodeDouble(100),
    "feet"
);
```

### NodeComprehension

**Purpose:** List comprehension

**Fields:**

```java
private final Node expression;       // What to compute
private final String variable;       // Loop variable
private final Node iterable;         // What to iterate over
private final Node condition;        // Filter condition (nullable)
```

**Example:**

```java
// {x^2 for x in 1..10 if x mod 2 == 0}
NodeComprehension comp = new NodeComprehension(
    new NodeBinary(new NodeVariable("x"), TokenType.POWER, new NodeDouble(2)),
    "x",
    new NodeRangeExpression(...),
    new NodeBinary(...)  // x mod 2 == 0
);
```

### NodeSequence

**Purpose:** Multiple statements

**Fields:**

```java
private final List<Node> statements;
```

**Example:**

```java
// x := 5; y := 10; x + y
NodeSequence seq = new NodeSequence(List.of(
    new NodeAssignment("x", new NodeDouble(5)),
    new NodeAssignment("y", new NodeDouble(10)),
    new NodeBinary(...)
));
```

**Evaluation:**
Evaluate each statement in order, return result of last statement.

---

## NodeVisitor Pattern

**Purpose:** Traverse and transform AST without modifying node classes

**Interface:**

```java
interface NodeVisitor<T> {
    T visit(NodeDouble node);
    T visit(NodeRational node);
    T visit(NodeBinary node);
    T visit(NodeUnary node);
    // ... one method per node type
}
```

**Node Method:**

```java
interface Node {
    <T> T accept(NodeVisitor<T> visitor);
}

// Implementation in each node class:
class NodeBinary extends Node {
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
```

**Example Visitor:**

```java
class ASTToString implements NodeVisitor<String> {
    @Override
    public String visit(NodeDouble node) {
        return String.valueOf(node.doubleValue());
    }

    @Override
    public String visit(NodeBinary node) {
        return "(" + node.getLeft().accept(this) +
               " " + node.getOperator().getLexeme() +
               " " + node.getRight().accept(this) + ")";
    }

    // ... other visits
}
```

---

## Common Patterns for AI Agents

### 1. Type Checking

```java
if (node instanceof NodeNumber num) {
    // Handle numeric types
} else if (node instanceof NodeVector vec) {
    // Handle vectors
} else if (node instanceof NodeConstant) {
    // Handle other constants
} else {
    // Unevaluated expression - need to evaluate first
}
```

### 2. Recursive Evaluation

```java
public NodeConstant evaluate(Node node) {
    if (node instanceof NodeConstant constant) {
        return constant;  // Already evaluated
    }

    if (node instanceof NodeBinary binary) {
        NodeConstant left = evaluate(binary.getLeft());
        NodeConstant right = evaluate(binary.getRight());
        return applyOperator(left, binary.getOperator(), right);
    }

    // ... other cases
}
```

### 3. Type Promotion

```java
// Promote to common type
NodeNumber left = ...;
NodeNumber right = ...;

if (left instanceof NodeDouble || right instanceof NodeDouble) {
    // Promote both to double
    return new NodeDouble(left.doubleValue() + right.doubleValue());
} else {
    // Both rational - stay exact
    return new NodeRational(left.rationalValue().add(right.rationalValue()));
}
```

---

## Testing Node Types

### NodeConstant Tests

```java
@Test
void nodeDouble() {
    NodeDouble d = new NodeDouble(3.14);
    assertThat(d.doubleValue()).isEqualTo(3.14);
    assertThat(d.toString()).isEqualTo("3.14");
}

@Test
void nodeRational() {
    NodeRational r = new NodeRational(new BigRational(22, 7));
    assertThat(r.toString()).contains("22/7");
    assertThat(r.doubleValue()).isCloseTo(3.142857, within(0.0001));
}
```

### NodeExpression Tests

```java
@Test
void nodeBinary() {
    Node left = new NodeDouble(2);
    Node right = new NodeDouble(3);
    NodeBinary add = new NodeBinary(left, plusToken, right);

    assertThat(add.getLeft()).isEqualTo(left);
    assertThat(add.getRight()).isEqualTo(right);
}
```

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[PARSER.md](./PARSER.md)** - How nodes are created
- **[EVALUATOR.md](./EVALUATOR.md)** - How nodes are evaluated
