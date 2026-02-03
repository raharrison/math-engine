# Broadcasting Architecture - Final Design

## Overview

All binary operators and functions use a unified broadcasting system via `BroadcastingEngine` (in `util/BroadcastingEngine.java`).
This provides maximum flexibility with minimal code duplication, serving as a shared infrastructure layer for both operators
(via `OperatorContext`) and functions (via `FunctionBuilder`/`FunctionContext`).

## Key Components

### 1. BroadcastingEngine (util/BroadcastingEngine.java)

**Central broadcasting engine** - handles ALL type combinations:

```
Scalar  op Scalar  → Scalar
Vector  op Scalar  → Vector (broadcast scalar to all elements)
Scalar  op Vector  → Vector (broadcast scalar to all elements)
Vector  op Vector  → Vector (element-wise, with zero-padding for size mismatches)
Matrix  op Scalar  → Matrix (broadcast scalar to all elements)
Scalar  op Matrix  → Matrix (broadcast scalar to all elements)
Matrix  op Matrix  → Matrix (element-wise, with zero-padding for size mismatches)
Matrix  op Vector  → Matrix (intelligent row/column broadcasting)
Vector  op Matrix  → Matrix (intelligent row/column broadcasting)
```

**Key Features:**

- **Two entry points**: `applyUnary(value, op)` for element-wise unary operations, `applyBinary(left, right, op)` for binary
- **Recursive dispatch**: Handles nested structures automatically
- **Zero-padding**: Vectors and matrices are expanded with zeros when sizes don't match
    - `{1,2} + {1,2,3}` → `{1,2,0} + {1,2,3}` → `{2,4,3}`
    - `{10} + {1,2,3,4}` → `{10,0,0,0} + {1,2,3,4}` → `{11,2,3,4}` (single-element vectors also zero-pad)
    - `[[1,2]] + [[1],[2],[3]]` → `[[1,2],[0,0],[0,0]] + [[1,0],[2,0],[3,0]]` → `[[2,2],[2,0],[3,0]]`
- **Single-element broadcasting**: ONLY 1x1 matrices broadcast to any size (not vectors)
- **Shared by operators and functions**: Both `OperatorContext` and `FunctionBuilder`/`FunctionContext` delegate to this engine
- **Type preservation**: Maintains rational precision where possible

### 2. OperatorContext (operator/OperatorContext.java)

Provides utility methods for operators:

- `toNumber()`, `toBoolean()` - type coercion
- `applyNumericBinary()` - handles rational vs double promotion
- `applyAdditive()` - addition/subtraction with percent arithmetic support
- `applyDoubleBinary()` - forces double result

### 3. MatrixOperations (operator/MatrixOperations.java)

Specialized matrix operations that DON'T use element-wise broadcasting:

- `multiply(Matrix, Matrix)` - True matrix multiplication (A @ B)
- `power(Matrix, int)` - Matrix exponentiation via squaring (A^n)
- `dotProduct(Vector, Vector)` - Scalar dot product
- `identityMatrix(int)` - Creates identity matrix

## Operator Categories

### Arithmetic Operators (+, -, *, /, mod, ^)

**Use BroadcastingEngine** for element-wise operations with full broadcasting support.

**Special cases:**

- **Power (^)**: Matrix^Integer uses MatrixOperations.power(), otherwise broadcasts
- **Percent arithmetic**: Special handling for NodePercent types
    - `100 + 10%` → `110` (percentage of base)
    - `10% + 20%` → `30%`
    - `10% * 20%` → `2%`
    - `20% / 10%` → `2` (ratio, not percent)

**Implementation pattern:**

```java
public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
    return BroadcastingEngine.applyBinary(left, right, (l, r) -> {
        // Special cases (e.g., percent arithmetic)
        if (l instanceof NodePercent && r instanceof NodePercent) {
            // handle percent-specific logic
        }

        // Default: use ctx.applyNumericBinary for type promotion
        return ctx.applyNumericBinary(l, r, BigRational::add, Double::sum);
    });
}
```

### Comparison Operators (==, !=, <, >, <=, >=)

**Equality (==, !=)**:

- **Containers**: Use structural equality, return **scalar** boolean
    - `{1,2,3} == {1,2,3}` → `true` (not `{true,true,true}`)
    - `[[1,2],[3,4]] == [[1,2],[3,4]]` → `true`
- **Scalars**: Normal value comparison

**Ordering (<, >, <=, >=)**:

- **Containers**: **REJECT** - throw TypeError
    - `{1,2} < {2,3}` → ERROR
- **Scalars only**: Normal numeric comparison

**Rationale**: Container ordering is ambiguous. Use element-wise functions if needed.

### Logical Operators (&&, ||, xor)

**Reject containers entirely** - logical operations only make sense for scalars.

- `{true, false} && {true, true}` → ERROR
- Short-circuit evaluation works for scalars only

### Matrix Multiply (@)

**Special semantics** - does NOT use BroadcastingEngine:

- `Matrix @ Matrix` → True matrix multiplication
- `Vector @ Vector` → Dot product (scalar)
- `Vector @ Function` → Map operation (not yet implemented)

## Implementation Pattern

All operators use `BroadcastingEngine.applyBinary()`:

```java
@Override
public NodeConstant apply(NodeConstant left, NodeConstant right, OperatorContext ctx) {
    return BroadcastingEngine.applyBinary(left, right, (l, r) ->
        ctx.applyNumericBinary(l, r, BigRational::add, Double::sum)
    );
}
```

Functions using `FunctionBuilder` get broadcasting automatically:

```java
// Level 0: auto-broadcasts via FunctionBuilder
MathFunction exp = FunctionBuilder.named("exp")
                .takingUnary()
                .implementedByDouble(Math::exp);

// Level 2: manual broadcasting via FunctionContext
MathFunction sin = FunctionBuilder.named("sin")
        .takingUnary()
        .noBroadcasting()
        .implementedBy((arg, ctx) ->
                ctx.applyWithBroadcasting(arg, value -> Math.sin(ctx.toRadians(value))));
```

Broadcasting is handled automatically. Just define the scalar operation.

## Broadcasting Examples

### Vectors

```
{1,2,3} + 5              → {6, 7, 8}       (scalar broadcasts)
{1,2} + {10,20,30}       → {11, 22, 30}    (zero-pad: {1,2,0} + {10,20,30})
{10} + {1,2,3,4}         → {11, 2, 3, 4}   (zero-pad: {10,0,0,0} + {1,2,3,4})
```

### Matrices

```
[[1,2],[3,4]] + 10                    → [[11,12],[13,14]]
[[1,2]] + [[1,2,3],[4,5,6]]           → [[2,4,3],[4,5,6]]   (zero-pad + broadcast)
[[1],[2]] + [[10,20]]                 → [[11,20],[12,20]]   (column + row broadcast)
[[5]] + [[1,2],[3,4]]                 → [[6,7],[8,9]]       (1x1 broadcasts everywhere)
```

### Mixed

```
{1,2,3} + [[1,2],[3,4]]              → [[2,4,0],[6,6,0]]   (vector broadcasts as row)
```

## Design Principles

1. **Single Source of Truth**: `BroadcastingEngine` handles ALL broadcasting for both operators and functions
2. **Flexibility**: Zero-padding allows operations on mismatched sizes
3. **Type Safety**: Containers in logical/ordering operators rejected early
4. **Clarity**: Equality on containers returns scalar (structural comparison)
5. **Performance**: Recursive dispatch is elegant but efficient
6. **Extensibility**: New operators just define scalar operation, broadcasting is automatic
7. **Shared Infrastructure**: Operators use `BroadcastingEngine` directly; functions use it via `FunctionBuilder` (auto) or
   `FunctionContext.applyWithBroadcasting()` (manual)

## Summary

- **All arithmetic operators**: Use BroadcastingEngine with zero-padding
- **Equality operators**: Structural comparison for containers (scalar result)
- **Ordering operators**: Scalars only
- **Logical operators**: Scalars only
- **Matrix multiply**: Special semantics, bypasses broadcasting
- **Type preservation**: Rational precision maintained where possible
- **Zero-padding**: Maximum flexibility for size mismatches
