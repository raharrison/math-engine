# Operator System

**Purpose:** Extensible operator registration, dispatch, and execution system

---

## Architecture Overview

The operator system separates operator **definition** from operator **execution**:

```
Parser creates NodeBinary/NodeUnary
              ↓
Evaluator calls OperatorExecutor
              ↓
OperatorExecutor looks up operator by TokenType
              ↓
Operator.execute() performs operation
              ↓
Result (NodeConstant)
```

**Key Insight:** Operators are registered by `TokenType` (not by string symbol). This allows:

- Multiple symbols for same operator (`and` and `&&`)
- Easy extension without modifying lexer
- Dynamic operator registration

---

## Core Components

### 1. Operator Interface

**File:** `operator/Operator.java`

```java
public interface Operator {
    /**
     * Gets the name of this operator (for error messages).
     */
    String name();
}
```

Base marker interface for all operators.

### 2. BinaryOperator

**File:** `operator/BinaryOperator.java`

```java
public interface BinaryOperator extends Operator {
    /**
     * Executes the binary operation.
     *
     * @param left    the left operand
     * @param right   the right operand
     * @param context the evaluation context
     * @return the result
     */
    NodeConstant execute(NodeConstant left, NodeConstant right, OperatorContext context);
}
```

**OperatorContext** provides:

- Angle unit for trigonometric operations
- Configuration flags

**Responsibilities:**

- Type checking (ensure operands are compatible)
- Type promotion (Rational → Double when needed)
- Broadcasting (scalar to vector/matrix)
- Actual computation

### 3. UnaryOperator

**File:** `operator/UnaryOperator.java`

```java
public interface UnaryOperator extends Operator {
    /**
     * Executes the unary operation.
     *
     * @param operand the operand
     * @param context the evaluation context
     * @return the result
     */
    NodeConstant execute(NodeConstant operand, OperatorContext context);
}
```

---

## OperatorExecutor

**File:** `operator/OperatorExecutor.java`

**Purpose:** Central registry and dispatcher for all operators

**Architecture:**

```java
class OperatorExecutor {
    private Map<TokenType, BinaryOperator> binaryOperators;
    private Map<TokenType, UnaryOperator> unaryOperators;

    // Registration
    void registerBinary(TokenType type, BinaryOperator op);
    void registerUnary(TokenType type, UnaryOperator op);

    // Execution
    NodeConstant executeBinary(TokenType type, NodeConstant left, NodeConstant right, EvaluationContext ctx);
    NodeConstant executeUnary(TokenType type, NodeConstant operand, EvaluationContext ctx);

    // Short-circuit support
    NodeConstant executeBinaryShortCircuit(TokenType type, NodeConstant left, Supplier<NodeConstant> right, ...);
}
```

### Registration

```java
OperatorExecutor executor = new OperatorExecutor();

// Register binary operators
executor.registerBinary(TokenType.PLUS, new PlusOperator());
executor.registerBinary(TokenType.MINUS, new MinusOperator());
executor.registerBinary(TokenType.MULTIPLY, new MultiplyOperator());

// Register unary operators
executor.registerUnary(TokenType.MINUS, new NegateOperator());
executor.registerUnary(TokenType.FACTORIAL, new FactorialOperator());
```

### Execution

```java
// Binary operation
NodeConstant result = executor.executeBinary(
    TokenType.PLUS,
    new NodeDouble(2),
    new NodeDouble(3),
    context
);  // Returns NodeDouble(5)

// Unary operation
NodeConstant result = executor.executeUnary(
    TokenType.MINUS,
    new NodeDouble(5),
    context
);  // Returns NodeDouble(-5)
```

### Short-Circuit Evaluation

For logical operators (`&&`, `||`), the right operand may not need evaluation:

```java
// false && expensive_operation  →  doesn't evaluate expensive_operation
NodeConstant result = executor.executeBinaryShortCircuit(
    TokenType.AND,
    new NodeBoolean(false),
    () -> evaluateExpensiveOperation(),  // Not called!
    context
);  // Returns NodeBoolean(false)
```

---

## Standard Binary Operators

**File:** `operator/binary/StandardBinaryOperators.java`

Provides factory method to get all standard operators:

```java
Map<TokenType, BinaryOperator> operators = StandardBinaryOperators.all();
```

### Arithmetic Operators

#### PlusOperator (+)

**Behavior:**

- Number + Number → Number (with type promotion)
- Vector + Vector → Vector (element-wise)
- Matrix + Matrix → Matrix (element-wise)
- Scalar + Vector → Vector (broadcast)
- Scalar + Matrix → Matrix (broadcast)

**Type Promotion:**

```java
Rational + Rational → Rational (exact)
Rational + Double   → Double
Double + Double     → Double
```

**Broadcasting:**

```java
{1, 2, 3} + 5       → {6, 7, 8}
5 + {1, 2, 3}       → {6, 7, 8}
{1, 2} + {3, 4, 5}  → {4, 6, 5}  // Extend shorter with zeros
```

#### MinusOperator (-)

Same as PlusOperator but for subtraction.

#### MultiplyOperator (*)

**Behavior:**

- Number * Number → Number
- Vector * Vector → Vector (element-wise, NOT dot product)
- Matrix * Matrix → Matrix (element-wise, NOT matrix multiply)
- Scalar * Vector → Vector (broadcast)
- Scalar * Matrix → Matrix (broadcast)

**Note:** Element-wise multiplication. Use `@` for matrix multiplication.

#### DivideOperator (/)

**Behavior:**

- Number / Number → Number
- Division by zero → ERROR
- Rational / Rational stays Rational when possible:
  ```java
  (2/3) / (4/5) = (2/3) * (5/4) = 10/12 = 5/6  (simplified)
  ```
- Vector / Vector → Vector (element-wise)
- Vector / Scalar → Vector (broadcast)

#### PowerOperator (^)

**Behavior:**

- Number ^ Number → Number
- Negative base with fractional exponent → Complex (currently error)
- 0^0 → 1 (by convention)
- Vector ^ Scalar → Vector (element-wise)

**Type Promotion:**
Always returns Double (except for integer powers of rationals).

**Right-Associative:**

```java
2^3^2 = 2^(3^2) = 2^9 = 512
```

#### ModOperator (mod)

**Behavior:**

- Number mod Number → Number
- Uses: `a - floor(a/b) * b`
- Always returns non-negative result
- Works with rationals and doubles

```java
7 mod 3     → 1
-7 mod 3    → 2  (not -1)
7.5 mod 2.0 → 1.5
```

#### OfOperator (of)

**Purpose:** Percentage calculations

**Behavior:**

```java
50% of 200  → 100
25% of 80   → 20
```

**Implementation:**

```java
percent.doubleValue() * value.doubleValue()
```

#### MatrixMultiplyOperator (@)

**Purpose:** True matrix multiplication (not element-wise)

**Behavior:**

- Matrix @ Matrix → Matrix (matrix multiply)
- Vector @ Vector → Scalar (dot product)
- Matrix @ Vector → Vector (matrix-vector multiply)

**Dimension Check:**

```java
[m × n] @ [n × p] → [m × p]
[1 × n] @ [n × 1] → [1 × 1] (scalar)
```

**Example:**

```java
[1, 2]    @  [5, 6]    =  [19, 22]
[3, 4]       [7, 8]       [43, 50]

// Calculation:
// [0,0] = 1*5 + 2*7 = 19
// [0,1] = 1*6 + 2*8 = 22
// [1,0] = 3*5 + 4*7 = 43
// [1,1] = 3*6 + 4*8 = 50
```

### Comparison Operators

**File:** `operator/binary/ComparisonOperators.java`

All comparison operators return `NodeBoolean`.

#### RelationalOperators (<, >, <=, >=)

**Behavior:**

- Number comparison (type promotion applies)
- String comparison (lexicographic)
- Vector comparison (element-wise, returns vector of booleans)

```java
5 < 10           → true
"abc" < "xyz"    → true
{1, 5, 3} < 4    → {true, false, true}
```

#### EqualityOperators (==, !=)

**Behavior:**

- Number equality (with tolerance for doubles)
- String equality (exact match)
- Boolean equality
- Vector equality (element-wise)

```java
5 == 5                  → true
5.0 == 5                → true (cross-type)
"hello" == "hello"      → true
{1, 2} == {1, 2}        → true
```

**Floating-Point Tolerance:**

```java
0.1 + 0.2 == 0.3  → true  (with small epsilon)
```

### Logical Operators

**File:** `operator/binary/LogicalOperators.java`

**Type Coercion:**

- Numbers: 0 = false, non-zero = true
- Booleans: direct use

#### AndOperator (&&, and)

**Behavior:**

```java
true && true    → true
true && false   → false
false && true   → false
false && false  → false
```

**Short-Circuit:**

```java
false && expensive()  → false (expensive not called)
```

**Numeric:**

```java
5 && 3   → true  (both non-zero)
0 && 5   → false (first is zero)
```

#### OrOperator (||, or)

**Behavior:**

```java
true || true    → true
true || false   → true
false || true   → true
false || false  → false
```

**Short-Circuit:**

```java
true || expensive()  → true (expensive not called)
```

#### XorOperator (xor)

**Behavior:**

```java
true xor true   → false
true xor false  → true
false xor true  → true
false xor false → false
```

**No Short-Circuit:** Both operands always evaluated.

---

## Standard Unary Operators

**File:** `operator/unary/StandardUnaryOperators.java`

### NegateOperator (-)

**Behavior:**

```java
-5        → -5
-(-5)     → 5
-{1,2,3}  → {-1,-2,-3}
```

**Type Preservation:**

```java
-NodeRational(5, 2) → NodeRational(-5, 2)
-NodeDouble(3.14)   → NodeDouble(-3.14)
```

### UnaryPlusOperator (+)

**Behavior:**
No-op, returns operand unchanged.

```java
+5  → 5
```

### NotOperator (not)

**Behavior:**

```java
not true   → false
not false  → true
not 0      → true
not 5      → false
```

### FactorialOperator (!)

**Behavior:**

```java
5!   → 120
0!   → 1
(-1)! → ERROR (negative factorial)
```

**Type:**
Returns Double for large results, Rational for small.

**Limit:**
Typically limited to n <= 170 (double overflow prevention).

### DoubleFactorialOperator (!!)

**Behavior:**

```java
6!!  → 6 * 4 * 2 = 48
7!!  → 7 * 5 * 3 * 1 = 105
```

**Definition:**

- n!! = n * (n-2) * (n-4) * ... * 1 (or 2)

### PercentOperator (%)

**Behavior:**

```java
50%  → 0.5 (NodePercent)
```

**Type:**
Returns `NodePercent` which displays as percentage but computes as decimal.

---

## Broadcasting System

**File:** `operator/BroadcastingDispatcher.java`

**Purpose:** Handle scalar-vector and scalar-matrix operations

**Rules:**

### Scalar Broadcasting

```java
5 + {1, 2, 3}       → {6, 7, 8}
{1, 2, 3} * 2       → {2, 4, 6}
5 + [1, 2; 3, 4]    → [6, 7; 8, 9]
```

### Vector Size Normalization

When vectors have different sizes, extend shorter with zeros:

```java
{1, 2} + {3, 4, 5}  → {1, 2, 0} + {3, 4, 5} = {4, 6, 5}
```

### Matrix Broadcasting

Scalar to matrix:

```java
2 * [1, 2; 3, 4] → [2, 4; 6, 8]
```

### Type Dispatch Pattern

```java
public NodeConstant execute(NodeConstant left, NodeConstant right, OperatorContext ctx) {
    // Scalar + Scalar
    if (bothAreNumbers(left, right)) {
        return executeNumbers(left, right);
    }

    // Scalar + Vector (or Vector + Scalar)
    if (oneIsScalar(left, right) && oneIsVector(left, right)) {
        return broadcastToVector(left, right);
    }

    // Vector + Vector
    if (bothAreVectors(left, right)) {
        return executeVectors(left, right);
    }

    // Scalar + Matrix (or Matrix + Scalar)
    if (oneIsScalar(left, right) && oneIsMatrix(left, right)) {
        return broadcastToMatrix(left, right);
    }

    // Matrix + Matrix
    if (bothAreMatrices(left, right)) {
        return executeMatrices(left, right);
    }

    throw new TypeError("Incompatible types for operation");
}
```

---

## Matrix Operations

**File:** `operator/MatrixOperations.java`

**Utilities for matrix operations:**

### Addition/Subtraction

```java
NodeMatrix add(NodeMatrix a, NodeMatrix b);
NodeMatrix subtract(NodeMatrix a, NodeMatrix b);
```

**Dimension Check:**
Must have same dimensions.

### Multiplication

```java
NodeMatrix multiply(NodeMatrix a, NodeMatrix b);
```

**Dimension Check:**

```
[m × n] @ [n × p] → [m × p]
```

### Transpose

```java
NodeMatrix transpose(NodeMatrix m);
```

### Element Access

```java
NodeConstant get(NodeMatrix m, int row, int col);
void set(NodeMatrix m, int row, int col, NodeConstant value);
```

---

## Implementing a Custom Operator

### Step 1: Create Operator Class

```java
public class CustomOperator implements BinaryOperator {
    @Override
    public String name() {
        return "custom";
    }

    @Override
    public NodeConstant execute(NodeConstant left, NodeConstant right, OperatorContext context) {
        // Type checking
        if (!(left instanceof NodeNumber) || !(right instanceof NodeNumber)) {
            throw new TypeError("custom requires numeric operands");
        }

        // Get values
        double leftVal = left.doubleValue();
        double rightVal = right.doubleValue();

        // Compute
        double result = customOperation(leftVal, rightVal);

        return new NodeDouble(result);
    }

    private double customOperation(double a, double b) {
        // Your logic here
        return a * Math.log(b);
    }
}
```

### Step 2: Register Operator

```java
// In MathEngineConfig
Map<TokenType, BinaryOperator> operators = new HashMap<>(StandardBinaryOperators.all());
operators.put(TokenType.CUSTOM, new CustomOperator());

MathEngineConfig config = MathEngineConfig.builder()
    .binaryOperators(operators)
    .build();
```

### Step 3: Add TokenType (if new)

If you need a new token type, add to lexer:

```java
// In TokenScanner
if (current == '@' && peek() == '@') {
    advance();
    addToken(TokenType.DOUBLE_AT);
}
```

---

## Common Pitfalls for AI Agents

### 1. Forgetting Type Promotion

**Problem:** Mixing Rational and Double without promotion

**Solution:** Always promote to common type:

```java
if (left instanceof NodeDouble || right instanceof NodeDouble) {
    // Promote both to double
}
```

### 2. Not Handling Broadcasting

**Problem:** Only implementing scalar operations

**Solution:** Check for vectors/matrices and broadcast:

```java
if (left instanceof NodeVector || right instanceof NodeVector) {
    return broadcastOperation(left, right);
}
```

### 3. Ignoring Operator Precedence

**Problem:** Parser handles precedence, not operator

**Reminder:** Operator just executes. Parser ensures correct order.

### 4. Mutating Operands

**Problem:** Modifying input nodes

**Solution:** Always create new result nodes:

```java
// WRONG
left.setValue(result);
return left;

// RIGHT
return new NodeDouble(result);
```

### 5. Not Checking Dimensions

**Problem:** Matrix operation on incompatible sizes

**Solution:** Always validate dimensions first:

```java
if (a.getColumnCount() != b.getRowCount()) {
    throw new TypeError("Matrix dimensions incompatible");
}
```

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[EVALUATOR.md](./EVALUATOR.md)** - How operators are invoked
- **[NODES.md](./NODES.md)** - Node types operators work with
- **[FUNCTIONS.md](./FUNCTIONS.md)** - Function system (similar pattern)
