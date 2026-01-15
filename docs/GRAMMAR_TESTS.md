# Math Engine Comprehensive Test Catalog

**Version:** 2.0
**Purpose:** Complete test specifications for parser/evaluator validation

**Format:** Machine-readable test cases organized by category. Each test includes:

- Input expression
- Expected output/result
- Expected type
- Evaluation order (for precedence tests)
- Notes on what is being tested

---

## Test Execution Guidelines

### Test Organization

Tests are organized by complexity:

1. **Lexical Tests** - Token recognition and classification
2. **Precedence Tests** - Operator precedence and associativity
3. **Type System Tests** - Type coercion and promotion
4. **Data Structure Tests** - Vectors, matrices, ranges
5. **Function Tests** - Definitions, calls, recursion
6. **Feature Tests** - Lambdas, comprehensions, subscripting
7. **Logic Tests** - Boolean operations, comparisons
8. **Edge Case Tests** - Limits, special values, errors
9. **Integration Tests** - Complex multi-step scenarios

### Test Execution Order

**Phase 1: Foundation**

1. Lexical tests (ensure tokenizer works)
2. Basic precedence tests (ensure parser precedence is correct)
3. Simple type tests (ensure evaluator handles basic types)

**Phase 2: Core Features**

4. Vector/matrix tests
5. Function definition tests
6. Implicit multiplication tests

**Phase 3: Advanced Features**

7. Lambda tests
8. Comprehension tests
9. Subscript/slice tests

**Phase 4: Robustness**

10. Edge case tests
11. Error tests
12. Integration tests

### Success Criteria

- **Lexical**: 100% pass required (foundation)
- **Precedence**: 100% pass required (critical for correctness)
- **Type System**: 95% pass minimum
- **Features**: 90% pass minimum
- **Edge Cases**: 80% pass minimum (some are implementation-dependent)
- **Error Tests**: 100% pass required (security/stability)

---

## Test Categories

---

## 1. Lexical Tests

### 1.1 Integer Literals

```test
Input              | Expected | Type           | Notes
-------------------|----------|----------------|------------------
0                  | 0        | NodeRational   | Zero
42                 | 42       | NodeRational   | Positive integer
-17                | -17      | NodeRational   | Negative integer
1000000            | 1000000  | NodeRational   | Large integer
```

### 1.2 Decimal Literals

```test
Input              | Expected | Type           | Notes
-------------------|----------|----------------|------------------
3.14               | 3.14     | NodeDouble     | Simple decimal
-0.5               | -0.5     | NodeDouble     | Negative decimal
0.0                | 0.0      | NodeDouble     | Zero decimal
1.0                | 1.0      | NodeDouble     | Integer as decimal
0.001              | 0.001    | NodeDouble     | Small decimal
```

### 1.3 Scientific Notation

```test
Input              | Expected | Type           | Notes
-------------------|----------|----------------|------------------
1e3                | 1000.0   | NodeDouble     | Positive exponent
2.5E-2             | 0.025    | NodeDouble     | Negative exponent (uppercase E)
-3.2e2             | -320.0   | NodeDouble     | Negative number with exponent
1.5e+3             | 1500.0   | NodeDouble     | Explicit + in exponent
```

### 1.4 Rational Literals

```test
Input              | Expected | Type           | Notes
-------------------|----------|----------------|------------------
1/2                | 1/2      | NodeRational   | Simple fraction
22/7               | 22/7     | NodeRational   | Pi approximation
-3/4               | -3/4     | NodeRational   | Negative fraction
100/25             | 4/1      | NodeRational   | Simplified fraction (implementation choice)
```

### 1.5 Predefined Constants

```test
Input              | Expected        | Type           | Notes
-------------------|-----------------|----------------|------------------
pi                 | 3.14159265...   | NodeDouble     | Mathematical π
euler              | 2.71828182...   | NodeDouble     | Euler's number e
true               | 1.0             | NodeBoolean    | Boolean true
false              | 0.0             | NodeBoolean    | Boolean false
infinity           | Infinity        | NodeDouble     | Positive infinity
nan                | NaN             | NodeDouble     | Not a number
zero               | 0               | NodeRational   | Numeric constant
ten                | 10              | NodeRational   | Numeric constant
million            | 1000000         | NodeRational   | Large numeric constant
```

---

## 2. Operator Precedence Tests

### 2.1 Basic Arithmetic Precedence

```test
Input              | Expected | Evaluation Order           | Notes
-------------------|----------|----------------------------|------------------
2 + 3 * 4          | 14       | 2 + (3 * 4)                | Multiplication before addition
2 * 3 + 4          | 10       | (2 * 3) + 4                | Multiplication before addition
10 - 2 * 3         | 4        | 10 - (2 * 3)               | Multiplication before subtraction
8 / 2 + 2          | 6        | (8 / 2) + 2                | Division before addition
2 + 3 - 1          | 4        | (2 + 3) - 1                | Left-to-right for same precedence
```

### 2.2 Exponentiation Precedence

```test
Input              | Expected | Evaluation Order           | Notes
-------------------|----------|----------------------------|------------------
2 * 3^2            | 18       | 2 * (3^2)                  | Exponentiation before multiplication
2^3 * 4            | 32       | (2^3) * 4                  | Exponentiation before multiplication
2 + 3^2            | 11       | 2 + (3^2)                  | Exponentiation before addition
2^3 + 4            | 12       | (2^3) + 4                  | Exponentiation before addition
-2^2               | -4       | -(2^2)                     | Exponentiation before unary minus
(-2)^2             | 4        | (-2)^2                     | Parentheses override
```

### 2.3 Postfix Operator Precedence

```test
Input              | Expected | Evaluation Order           | Notes
-------------------|----------|----------------------------|------------------
5! + 1             | 121      | (5!) + 1                   | Factorial before addition
2 * 3!             | 12       | 2 * (3!)                   | Factorial before multiplication
5!^2               | 14400    | (5!)^2                     | Factorial before exponentiation
50% + 0.5          | 1.0      | (50%) + 0.5 = 0.5 + 0.5    | Percent before addition
50% * 2            | 1.0      | (50%) * 2 = 0.5 * 2        | Percent before multiplication
```

### 2.4 Function Call Precedence

```test
Input              | Expected | Evaluation Order           | Notes
-------------------|----------|----------------------------|------------------
sin(0) + 1         | 1.0      | (sin(0)) + 1               | Function call before addition (degrees)
2 * cos(0)         | 2.0      | 2 * (cos(0))               | Function call before multiplication (degrees)
abs(-5) + 3        | 8        | (abs(-5)) + 3              | Function call before addition
-sin(0)            | 0.0      | -(sin(0))                  | Function call before unary minus (degrees)
```

### 2.5 Comparison and Logical Precedence

```test
Input                    | Expected | Evaluation Order                 | Notes
-------------------------|----------|----------------------------------|------------------
2 + 3 < 10               | true     | (2 + 3) < 10                     | Arithmetic before comparison
5 * 2 > 8                | true     | (5 * 2) > 8                      | Arithmetic before comparison
10 - 5 == 5              | true     | (10 - 5) == 5                    | Arithmetic before equality
true || false && false   | true     | true || (false && false)         | AND before OR
false && false || true   | true     | (false && false) || true         | AND before OR
5 > 3 && 2 < 4           | true     | (5 > 3) && (2 < 4)               | Comparison before AND
```

### 2.6 Complex Mixed Precedence

```test
Input                    | Expected | Evaluation Order                             | Notes
-------------------------|----------|----------------------------------------------|------------------
2 + 3 * 4^2              | 50       | 2 + (3 * (4^2)) = 2 + 48                     | Multiple precedence levels
5! + 2^3 * 4             | 152      | (5!) + ((2^3) * 4) = 120 + 32                | Factorial, exponent, multiply, add
2 * 3 + 4 * 5            | 26       | (2 * 3) + (4 * 5)                            | Left-to-right for equal precedence
```

---

## 3. Operator Associativity Tests

### 3.1 Left-Associative Operators

```test
Input                    | Expected | Evaluation Order           | Notes
-------------------------|----------|----------------------------|------------------
10 - 5 - 2               | 3        | (10 - 5) - 2               | Subtraction is left-associative
20 / 4 / 2               | 2.5      | (20 / 4) / 2               | Division is left-associative
2 + 3 + 4                | 9        | (2 + 3) + 4                | Addition is left-associative
2 * 3 * 4                | 24       | (2 * 3) * 4                | Multiplication is left-associative
true && false && true    | false    | (true && false) && true    | AND is left-associative
true || false || false   | true     | (true || false) || false   | OR is left-associative
```

### 3.2 Right-Associative Operators

```test
Input              | Expected | Evaluation Order           | Notes
-------------------|----------|----------------------------|------------------
2^3^2              | 512      | 2^(3^2) = 2^9              | Exponentiation is right-associative
4^3^2              | 262144   | 4^(3^2) = 4^9              | Exponentiation is right-associative
2^2^2^2            | 65536    | 2^(2^(2^2)) = 2^16         | Multiple right-assoc exponents
```

### 3.3 Unary Operator Associativity (Right)

```test
Input              | Expected | Evaluation Order           | Notes
-------------------|----------|----------------------------|------------------
-5                 | -5       | Unary minus                | Single unary operator
--5                | 5        | -(-5)                      | Double negation
---5               | -5       | -(-(- 5))                  | Triple negation
not not true       | true     | not(not(true))             | Logical NOT is right-associative
```

---

## 4. Type System Tests

### 4.1 Rational Arithmetic (Exact)

```test
Input              | Expected      | Type           | Notes
-------------------|---------------|----------------|------------------
1 + 2              | 3             | NodeRational   | Integer addition stays rational
5 - 3              | 2             | NodeRational   | Integer subtraction stays rational
2 * 3              | 6             | NodeRational   | Integer multiplication stays rational
6 / 3              | 2             | NodeRational   | Exact division stays rational (2/1)
7 / 3              | 7/3           | NodeRational   | Non-exact division creates fraction
1/3 + 1/3          | 2/3           | NodeRational   | Rational arithmetic is exact
1/2 + 1/3          | 5/6           | NodeRational   | Rational addition with different denominators
```

### 4.2 Mixed Rational/Double Arithmetic

```test
Input              | Expected      | Type           | Notes
-------------------|---------------|----------------|------------------
2 + 3.0            | 5.0           | NodeDouble     | Rational + Double = Double
1/3 + 0.5          | 0.8333...     | NodeDouble     | Rational + Double = Double
2.5 * 3            | 7.5           | NodeDouble     | Double * Rational = Double
1/2 + 1.5          | 2.0           | NodeDouble     | Rational + Double = Double
```

### 4.3 Boolean Arithmetic

```test
Input              | Expected      | Type           | Notes
-------------------|---------------|----------------|------------------
true + 1           | 2             | NodeRational   | Boolean (1.0) + Rational = Rational
false + 1          | 1             | NodeRational   | Boolean (0.0) + Rational = Rational
true * 5           | 5             | NodeRational   | Boolean multiplication
true + false       | 1             | NodeRational   | Boolean addition
not true           | false         | NodeBoolean    | Logical NOT preserves boolean
```

### 4.4 Percent Arithmetic

```test
Input              | Expected      | Type           | Notes
-------------------|---------------|----------------|------------------
50%                | 0.5           | NodePercent    | Percent literal
50% + 0.5          | 1.0           | NodeDouble     | Percent + Double
25% * 4            | 1.0           | NodeDouble     | Percent multiplication
100% of 200        | 200           | NodeDouble     | Percent "of" operator
```

---

## 5. Vector Tests

### 5.1 Vector Literals

```test
Input              | Expected           | Type        | Notes
-------------------|--------------------|--------------|-----------------
{1, 2, 3}          | {1, 2, 3}          | NodeVector  | Basic vector literal
{1}                | {1}                | NodeVector  | Single-element vector
{}                 | {}                 | NodeVector  | Empty vector
{1.5, 2.5, 3.5}    | {1.5, 2.5, 3.5}    | NodeVector  | Decimal elements
```

### 5.2 Vector Arithmetic

```test
Input              | Expected           | Type        | Notes
-------------------|--------------------|--------------|-----------------
{1,2} + {3,4}      | {4,6}              | NodeVector  | Element-wise addition
{5,6} - {1,2}      | {4,4}              | NodeVector  | Element-wise subtraction
{1,2} * {3,4}      | {3,8}              | NodeVector  | Element-wise multiplication (Hadamard)
{6,8} / {2,4}      | {3,2}              | NodeVector  | Element-wise division
{1,2,3} * 2        | {2,4,6}            | NodeVector  | Scalar multiplication
3 + {1,2,3}        | {4,5,6}            | NodeVector  | Scalar addition (broadcast)
```

### 5.3 Vector Broadcasting

```test
Input              | Expected           | Type        | Notes
-------------------|--------------------|--------------|-----------------
{1} + {1,2,3}      | {2,3,4}            | NodeVector  | Broadcast single element
{1,2} + {1,2,3}    | {2,4,3}            | NodeVector  | Extend with zeros: {1,2,0} + {1,2,3}
5 + {1,2,3}        | {6,7,8}            | NodeVector  | Broadcast scalar
{1,2} + 10         | {11,12}            | NodeVector  | Broadcast scalar
```

### 5.4 Vector Functions

```test
Input              | Expected           | Type           | Notes
-------------------|--------------------|--------------  |-----------------
sum({1,2,3})       | 6                  | NodeNumber    | Sum all elements
sum(1,2,3)         | 6                  | NodeNumber    | Variadic syntax
sort({3,1,2})      | {1,2,3}            | NodeVector    | Sort ascending
reverse({1,2,3})   | {3,2,1}            | NodeVector    | Reverse order
sum({})            | 0                  | NodeNumber    | Sum of empty vector
```

### 5.5 Vector with Unary Functions

```test
Input                   | Expected (degrees) | Type       | Notes
------------------------|--------------------|-----------|-----------------
sin({0,90,180})         | {0.0,1.0,0.0}      | NodeVector | Element-wise sine
abs({-1,2,-3})          | {1,2,3}            | NodeVector | Element-wise abs
```

---

## 6. Matrix Tests

### 6.1 Matrix Literals

```test
Input              | Expected           | Type        | Notes
-------------------|--------------------|--------------|-----------------
[1,2;3,4]          | [[1,2],[3,4]]      | NodeMatrix  | 2×2 matrix
[1]                | [[1]]              | NodeMatrix  | 1×1 matrix
[1,2,3]            | [[1,2,3]]          | NodeMatrix  | 1×3 matrix (row vector)
[1;2;3]            | [[1],[2],[3]]      | NodeMatrix  | 3×1 matrix (column vector)
```

### 6.2 Matrix Arithmetic

```test
Input                        | Expected               | Type        | Notes
-----------------------------|------------------------|--------------|-----------------
[1,2;3,4] + [5,6;7,8]        | [6,8;10,12]            | NodeMatrix  | Element-wise addition
[1,2;3,4] * 2                | [2,4;6,8]              | NodeMatrix  | Scalar multiplication
[1,2;3,4] + 5                | [6,7;8,9]              | NodeMatrix  | Scalar addition (broadcast)
```

### 6.3 Matrix Multiplication

```test
Input                        | Expected               | Type        | Notes
-----------------------------|------------------------|--------------|-----------------
[1,2;3,4] @ [5,6;7,8]        | [19,22;43,50]          | NodeMatrix  | True matrix multiplication
```

### 6.4 Matrix Functions

```test
Input                        | Expected    | Type           | Notes
-----------------------------|-------------|----------------|-----------------
det([1,2;3,4])               | -2.0        | NodeNumber     | Determinant
det([1])                     | 1.0         | NodeNumber     | Determinant of 1×1
```

---

## 7. Range Tests

```test
Input              | Expected                        | Type           | Notes
-------------------|---------------------------------|----------------|-----------------
1..5               | {1,2,3,4,5}                     | NodeRange      | Simple range
1..10 step 2       | {1,3,5,7,9}                     | NodeRange      | Range with step
10..1 step -1      | {10,9,8,7,6,5,4,3,2,1}          | NodeRange      | Descending range
0..1 step 0.1      | {0.0,0.1,0.2,...,1.0}           | NodeRange      | Decimal range
sum(1..100)        | 5050                            | NodeNumber     | Range in function
```

---

## 8. Subscript and Slice Tests

### 8.1 Vector Indexing

```test
Input              | Expected | Notes
-------------------|----------|--------------------
{10,20,30}[0]      | 10       | First element (0-indexed)
{10,20,30}[1]      | 20       | Second element
{10,20,30}[2]      | 30       | Third element
{10,20,30}[-1]     | 30       | Last element (negative index)
{10,20,30}[-2]     | 20       | Second from last
```

### 8.2 Vector Slicing

```test
Input                     | Expected           | Notes
--------------------------|--------------------|--------------------
{10,20,30,40,50}[1:3]     | {20,30}            | Slice (inclusive:exclusive)
{10,20,30,40,50}[:3]      | {10,20,30}         | From start
{10,20,30,40,50}[2:]      | {30,40,50}         | To end
{10,20,30,40,50}[:]       | {10,20,30,40,50}   | Full copy
```

### 8.3 Matrix Indexing

```test
Input                     | Expected | Notes
--------------------------|----------|--------------------
[1,2,3;4,5,6;7,8,9][0,0]  | 1        | Element (0,0)
[1,2,3;4,5,6;7,8,9][1,2]  | 6        | Element (1,2)
[1,2,3;4,5,6;7,8,9][2,1]  | 8        | Element (2,1)
```

### 8.4 Matrix Slicing

```test
Input                        | Expected        | Notes
-----------------------------|-----------------|--------------------
[1,2,3;4,5,6;7,8,9][0,:]     | {1,2,3}         | Row 0
[1,2,3;4,5,6;7,8,9][:,1]     | {2,5,8}         | Column 1
[1,2,3;4,5,6;7,8,9][0:2,1:3] | [2,3;5,6]       | Sub-matrix
```

---

## 9. Function Definition Tests

### 9.1 Variable Assignment

```test
Input              | Effect                          | Notes
-------------------|---------------------------------|-----------------
x := 5             | Define variable x = 5           | Simple assignment
y := x + 3         | Define y = 8 (if x = 5)         | Expression assignment
```

### 9.2 Function Definitions (One Argument)

```test
Definition         | Invocation     | Expected | Notes
-------------------|----------------|----------|--------------------
square(x) := x^2   | square(5)      | 25       | Basic function
double(n) := 2*n   | double(3)      | 6        | Function with multiplication
neg(x) := -x       | neg(5)         | -5       | Unary operator in function
```

### 9.3 Function Definitions (Multiple Arguments)

```test
Definition                                          | Invocation          | Expected | Notes
----------------------------------------------------|---------------------|----------|--------------------
add(a,b) := a + b                                   | add(3,4)            | 7        | Two arguments
dist(x1,y1,x2,y2) := sqrt((x2-x1)^2 + (y2-y1)^2)    | dist(0,0,3,4)       | 5.0      | Four arguments
```

### 9.4 Recursive Functions

```test
Definition                                          | Invocation     | Expected | Notes
----------------------------------------------------|----------------|----------|--------------------
fib(n) := if(n <= 1, n, fib(n-1) + fib(n-2))        | fib(5)         | 5        | Fibonacci
fact(n) := if(n <= 1, 1, n * fact(n-1))             | fact(5)        | 120      | Factorial
```

### 9.5 Function Composition

```test
Definitions              | Invocation     | Expected | Notes
-------------------------|----------------|----------|--------------------
f(x) := x^2              | f(5)           | 25       | Define f
g(x) := x + 1            | g(5)           | 6        | Define g
h(x) := f(g(x))          | h(2)           | 9        | Composition: f(g(2)) = f(3) = 9
```

### 9.6 Function Scope

```test
Definitions              | Invocation     | Expected | Notes
-------------------------|----------------|----------|--------------------
x := 5                   | x              | 5        | Global variable
f(x) := x^2              | f(3)           | 9        | Parameter shadows global
f(x) (then check x)      | x              | 5        | Global x unchanged
```

---

## 10. Lambda Tests

```test
Input                      | Expected | Notes
---------------------------|----------|--------------------
f := x -> x^2; f(5)        | 25       | Single-parameter lambda
add := (a,b) -> a+b; add(3,4) | 7     | Multi-parameter lambda
(x -> x*2)(3)              | 6        | Immediate invocation
```

---

## 11. Comprehension Tests

```test
Input                                    | Expected                | Type       | Notes
-----------------------------------------|-------------------------|------------|-----------------
{x^2 for x in 1..5}                      | {1,4,9,16,25}           | NodeVector | Basic comprehension
{x for x in 1..10 if x mod 2 == 0}       | {2,4,6,8,10}            | NodeVector | With condition
{x*y for x in 1..3 for y in 1..3}        | {1,2,3,2,4,6,3,6,9}     | NodeVector | Nested comprehension
sum({x for x in 1..100})                 | 5050                    | NodeNumber | Comprehension in function
```

---

## 12. Implicit Multiplication Tests

```test
Input              | Equivalent     | Expected (x=5) | Notes
-------------------|----------------|----------------|--------------------
2x                 | 2 * x          | 10             | Number × variable
2(3+4)             | 2 * (3+4)      | 14             | Number × parenthesized
(2)(3)             | 2 * 3          | 6              | Parenthesized × parenthesized
2pi                | 2 * pi         | 6.2831...      | Number × constant
2x^2 (x=3)         | 2 * (x^2)      | 18             | Precedence: 2 * (x^2) NOT ((2*x)^2)
```

---

## 13. Boolean and Logical Tests

### 13.1 Comparison Operators

```test
Input              | Expected | Notes
-------------------|----------|--------------------
5 == 5             | true     | Equality
5 != 5             | false    | Inequality
5 == 6             | false    | Equality (false)
5 != 6             | true     | Inequality (true)
3 < 5              | true     | Less than
5 < 3              | false    | Less than (false)
5 > 3              | true     | Greater than
5 <= 5             | true     | Less than or equal
5 >= 5             | true     | Greater than or equal
```

### 13.2 Logical Operators

```test
Input                    | Expected | Notes
-------------------------|----------|--------------------
true && true             | true     | AND: both true
true && false            | false    | AND: one false
false || true            | true     | OR: one true
false || false           | false    | OR: both false
true xor false           | true     | XOR: different
true xor true            | false    | XOR: same
not true                 | false    | NOT: negate true
not not true             | true     | NOT: double negation
```

### 13.3 Complex Logical Expressions

```test
Input                             | Expected | Notes
----------------------------------|----------|--------------------
(5 > 3) && (2 < 4) || false       | true     | Mixed AND/OR
true && false || true && true     | true     | Precedence: AND before OR
not (true && false)               | true     | NOT on grouped expression
(5 == 5) && not (3 > 4)           | true     | Comparison + NOT
```

### 13.4 Conditional (if)

```test
Input                        | Expected | Notes
-----------------------------|----------|--------------------
if(true, 10, 20)             | 10       | Condition true
if(false, 10, 20)            | 20       | Condition false
if(5 > 3, 100, 200)          | 100      | Comparison condition
if(5 < 3, 100, 200)          | 200      | Comparison condition (false)
```

---

## 14. Edge Case Tests

### 14.1 Division by Zero

```test
Input              | Expected      | Notes
-------------------|---------------|--------------------
1 / 0              | Infinity      | Positive infinity
-1 / 0             | -Infinity     | Negative infinity
0 / 0              | NaN           | Undefined
10 / 0.0           | Infinity      | Division by zero (double)
```

### 14.2 Very Large Numbers

```test
Input              | Expected      | Notes
-------------------|---------------|--------------------
100!               | Very large    | May overflow to infinity
2^1000             | Infinity      | Double overflow
10^308             | ~Infinity     | Near double limit
```

### 14.3 Empty Structures

```test
Input              | Expected      | Type       | Notes
-------------------|---------------|------------|-----------------
{}                 | Empty vector  | NodeVector | Empty vector literal
sum({})            | 0             | NodeNumber | Sum of empty vector
sort({})           | {}            | NodeVector | Sort empty vector
reverse({})        | {}            | NodeVector | Reverse empty vector
```

### 14.4 Special Values

```test
Input              | Expected      | Notes
-------------------|---------------|--------------------
infinity + 1       | Infinity      | Infinity arithmetic
-infinity - 1      | -Infinity     | Negative infinity
infinity - infinity| NaN           | Undefined
0 * infinity       | NaN           | Undefined
nan + 1            | NaN           | NaN propagation
nan == nan         | false         | NaN not equal to itself
```

### 14.5 Nested Structures

```test
Input                      | Expected                | Type       | Notes
---------------------------|-------------------------|------------|-----------------
{{1,2}, {3,4}}             | Vector of vectors       | NodeVector | Nested vectors
sum({{1,2}, {3,4}})        | {3, 7}                  | NodeVector | Sum each sub-vector
```

### 14.6 Precedence Edge Cases

```test
Input              | Expected | Evaluation Order           | Notes
-------------------|----------|----------------------------|--------------------
-2^2               | -4       | -(2^2)                     | Unary minus after power
(-2)^2             | 4        | (-2)^2                     | Parentheses change order
5!^2               | 14400    | (5!)^2 = 120^2             | Factorial before power
2^3!               | 64       | 2^(3!) = 2^6               | Factorial in exponent
```

---

## 15. Error Tests

### 15.1 Syntax Errors

```test
Input              | Error Type           | Error Message (approximate)
-------------------|----------------------|--------------------------------
2 +                | Incomplete expression| "Expected expression after '+'"
(2 + 3             | Unmatched paren      | "Expected ')'"
2 + + 3            | Unexpected operator  | "Unexpected token '+'"
{1, 2,             | Incomplete vector    | "Expected expression or '}'"
[1, 2; 3]          | Matrix row mismatch  | "Inconsistent row sizes"
()                 | Empty parentheses    | "Empty parentheses not allowed"
```

### 15.2 Undefined Variables

```test
Input              | Error Type           | Error Message (approximate)
-------------------|----------------------|--------------------------------
x                  | Undefined variable   | "No value associated with 'x'"
2 * undefined      | Undefined variable   | "No value associated with 'undefined'"
```

### 15.3 Type Errors

```test
Input              | Error Type           | Error Message (approximate)
-------------------|----------------------|--------------------------------
det({1,2,3})       | Type mismatch        | "Determinant requires matrix"
det([1,2;3])       | Invalid matrix       | "Matrix must be square" OR "Inconsistent row sizes"
"hello" + 5        | Type mismatch        | "Cannot apply + to String and Number"
```

### 15.4 Redefinition Errors

```test
Input              | Error Type           | Error Message (approximate)
-------------------|----------------------|--------------------------------
sin(x) := x + 2    | Redefine operator    | "Cannot redefine system operator: sin"
```

### 15.5 Function Arity Errors

```test
Input                       | Error Type           | Error Message (approximate)
----------------------------|----------------------|--------------------------------
add(a,b) := a + b; add(5)   | Wrong argument count | "Function 'add' expects 2 arguments, got 1"
f(x) := x^2; f(1, 2)        | Wrong argument count | "Function 'f' expects 1 argument, got 2"
```

### 15.6 Domain Errors (Runtime)

```test
Input              | Error Type           | Behavior
-------------------|----------------------|--------------------------------
ln(-1)             | Domain error         | May return NaN or throw error
sqrt(-4)           | Domain error         | May return NaN (no complex numbers)
log(0)             | Domain error         | May return -Infinity
```

### 15.7 Stack Overflow

```test
Input                                   | Error Type           | Behavior
----------------------------------------|----------------------|--------------------------------
f(x) := g(x); g(x) := f(x); f(1)        | Circular reference   | Stack overflow or recursion limit
fib(10000)                              | Too deep recursion   | Stack overflow or recursion limit
```

---

## 16. Integration Tests

### 16.1 Physics Calculation

```test
Input: F(m, a) := m * a
       F(10, 9.8)

Expected: 98.0
Type: NodeDouble
Notes: Force = mass × acceleration
```

### 16.2 Quadratic Formula

```test
Input: a := 1
       b := -5
       c := 6
       x1 := (-b + sqrt(b^2 - 4*a*c)) / (2*a)
       x2 := (-b - sqrt(b^2 - 4*a*c)) / (2*a)

Expected: x1 = 3.0, x2 = 2.0
Notes: Solving x^2 - 5x + 6 = 0
```

### 16.3 Vector Dot Product

```test
Input: v1 := {1, 2, 3}
       v2 := {4, 5, 6}
       dot := sum(v1 * v2)

Expected: 32
Type: NodeNumber
Notes: Dot product via element-wise multiply + sum
```

### 16.4 Statistical Mean

```test
Input: values := {10, 20, 30, 40, 50}
       count := 5
       mean := sum(values) / count

Expected: 30.0
Notes: Mean calculation
```

### 16.5 Compound Interest

```test
Input: P := 1000
       r := 0.05
       n := 12
       t := 10
       A := P * (1 + r/n)^(n*t)

Expected: ~1647.01
Notes: Compound interest formula
```

### 16.6 Recursive Fibonacci

```test
Input: fib(n) := if(n <= 1, n, fib(n-1) + fib(n-2))
       fib(10)

Expected: 55
Notes: 10th Fibonacci number
```

### 16.7 Vector Normalization

```test
Input: v := {3, 4}
       magnitude := sqrt(sum(v * v))
       normalized := v / magnitude

Expected: {0.6, 0.8}
Notes: Unit vector
```

### 16.8 Matrix Element-wise Operations

```test
Input: m1 := [1, 2; 3, 4]
       m2 := [2, 0; 1, 2]
       sum := m1 + m2

Expected: [3, 2; 4, 6]
Notes: Element-wise matrix addition
```

---

## Test Execution Format (Machine-Readable JSON)

For automated testing, tests can be formatted as JSON:

```json
{
  "category": "Operator Precedence",
  "tests": [
    {
      "id": "precedence_001",
      "input": "2 + 3 * 4",
      "expected": 14,
      "expectedType": "NodeDouble",
      "evaluationOrder": "2 + (3 * 4)",
      "notes": "Multiplication before addition"
    },
    {
      "id": "precedence_002",
      "input": "2^3^2",
      "expected": 512,
      "expectedType": "NodeDouble",
      "evaluationOrder": "2^(3^2)",
      "notes": "Right-associative exponentiation"
    }
  ]
}
```

These should be split into different files as to not create a massive super test file which is unmaintainable

---

**Version 2.0 - Comprehensive Test Catalog**
