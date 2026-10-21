# Math Engine Grammar Specification

**Version:** 2.2
**Purpose:** Formal grammar definition and comprehensive function reference for mathematical expression language
**Updated:** 2026-01-22

---

## Philosophy

Natural mathematical language with:

- Exact rational arithmetic by default
- Rich data types (vectors, matrices, ranges, lambdas)
- Physical unit support
- Functional programming features
- Implicit multiplication (`2x`, `2(x+1)`)

**Design Goal:** Unambiguous, implementable via recursive descent with backtracking.

---

## Complete EBNF Grammar

```ebnf
(* Top-level program *)
program       := statement_list
statement_list := statement (';' statement)* ';'?
statement     := assignment | expression

(* Expressions *)
expression    := inline_assignment | comprehension | lambda | pipeline

(* '=' assigns only as a statement: at the top level or inside parentheses.
   ':=' assigns anywhere, so inside an argument, element, index or lambda body
   only ':=' does. Neither chains: 'x = y = 5' is a ParseException. *)
assignment    := target ('=' | ':=') expression
inline_assignment := target ':=' expression
target        := identifier params?
               | identifier index_group+
params        := '(' identifier (',' identifier)* ')'
index_group   := '[' slice_args ']'

comprehension := '{' expression ('for' identifier 'in' iterable)+ ('if' expression)? '}'

lambda        := identifier '->' expression
               | '(' identifier (',' identifier)* ')' '->' expression

(* Operator precedence chain - lowest to highest *)
pipeline      := logical_or

logical_or    := logical_xor (('||' | 'or') logical_xor)*

logical_xor   := logical_and ('xor' logical_and)*

logical_and   := equality (('&&' | 'and') equality)*

equality      := relational (('==' | '!=') relational)*

relational    := range (('<' | '>' | '<=' | '>=') range)*

range         := additive ('..' additive ('step' additive)?)?

additive      := multiplicative (('+' | '-') multiplicative)*

multiplicative := power (('*' | '/' | '@' | 'mod' | 'of') power)*

power         := unary ('^' power)?              (* right-associative *)

unary         := ('-' | '+' | 'not') unary | postfix

postfix       := subscript (('!' | '!!' | '%' | unit_conversion))*

unit_conversion := ('in' | 'to' | 'as') unit_expr

subscript     := call ('[' slice_args ']')*

slice_args    := expression (':' expression)? (',' expression (':' expression)?)*

call          := primary ('(' args? ')')*

args          := expression (',' expression)*

(* Atomic expressions *)
primary       := number | string | reference | identifier | vector | matrix | '(' statement_list ')'

reference     := '@' (identifier | string)   (* Unit reference: @m or @"km/h" *)
               | '$' identifier              (* Variable reference: $m1 *)
               | '#' identifier              (* Constant reference: #pi *)

number        := (integer | based_integer | decimal | scientific | rational_literal) 'd'?
                 (* a trailing 'd' makes a floating-point double *)

integer       := '-'? digits

based_integer := '0' [xX] hex_digits | '0' [oO] octal_digits | '0' [bB] binary_digits

decimal       := '-'? digits '.' digits

scientific    := decimal [eE] [+-]? digits

rational_literal := integer '/' integer

(* A '_' groups digits and is not part of the value, so it must sit between two of them *)
digits        := digit ('_'? digit)*
hex_digits    := hex_digit ('_'? hex_digit)*
octal_digits  := octal_digit ('_'? octal_digit)*
binary_digits := binary_digit ('_'? binary_digit)*

string        := '"' char* '"' | "'" char* "'"

vector        := '{' (args)? '}'

matrix        := '[' matrix_rows ']'
matrix_rows   := matrix_row ((',' | ';') matrix_row)*
matrix_row    := expression (',' expression)*

(* Matrix syntax supports two styles:
   - Semicolon syntax: [1, 2; 3, 4]  (traditional)
   - Nested syntax:    [[1, 2], [3, 4]]  (when each row is a vector/matrix)
   Both parse to the same NodeMatrix representation.
   String output uses semicolon format: [1, 2; 3, 4]
*)

identifier    := letter (letter | digit | '_')*

unit_expr     := identifier                      (* validated against unit registry *)

iterable      := range | vector | matrix | identifier

(* Comments, discarded by the lexer and allowed wherever whitespace is *)
comment       := '//' (any character except newline)*
               | '/*' (any character)* '*/'

(* Character classes *)
letter        := [a-zA-Z]
digit         := [0-9]
hex_digit     := [0-9a-fA-F]
octal_digit   := [0-7]
binary_digit  := [01]
char          := any character except quote
```

---

## Operator Precedence

**Lower number = Higher precedence (binds tighter)**

| Level | Operators                     | Type           | Assoc   | Example                    |
|-------|-------------------------------|----------------|---------|----------------------------|
| 0     | Literals, `( )`, `{ }`, `[ ]` | Primary        | N/A     | `42`, `(expr)`, `{1,2}`    |
| 1     | `f(...)`                      | Call           | Left    | `sin(x)`, `f(1,2)`         |
| 2     | `[...]`                       | Subscript      | Left    | `v[0]`, `m[1,2]`, `v[1:3]` |
| 3     | `in`, `to`, `as`              | Unit Conv      | Left    | `100m in ft`               |
| 4     | `!`, `!!`, `%`                | Postfix        | Left    | `5!`, `50%`                |
| 5     | `-`, `+`, `not`               | Unary          | Right   | `-x`, `not b`              |
| 6     | `^`                           | Power          | Right   | `2^3^2` = `2^(3^2)`        |
| 7     | `*`, `/`, `@`, `mod`, `of`    | Multiplicative | Left    | `a*b`, `A@B`, `7 mod 3`    |
| 8     | `+`, `-`                      | Additive       | Left    | `a+b`, `a-b`               |
| 9     | `..`                          | Range          | Left    | `1..10`, `1..10 step 2`    |
| 10    | `<`, `>`, `<=`, `>=`          | Relational     | Left    | `a < b`, `a >= b`          |
| 11    | `==`, `!=`                    | Equality       | Left    | `a == b`, `a != b`         |
| 12    | `&&`, `and`                   | Logical AND    | Left    | `a && b`                   |
| 13    | `xor`                         | Logical XOR    | Left    | `a xor b`                  |
| 14    | `\|\|`, `or`                  | Logical OR     | Left    | `a \|\| b`                 |
| 15    | `=`, `:=`                     | Assignment     | None    | `x = 5`, `sum(a := 3, a)`  |
| 16    | `->`                          | Lambda         | Right   | `x -> x^2`                 |
| 17    | `for`, `in`, `if`             | Comprehension  | N/A     | `{x for x in 1..10}`       |

**Note on implicit multiplication:** Inserted at tokenization, has same precedence as explicit `*` (level 7).

---

## Type System

```
Node (abstract AST)
├─ NodeConstant (evaluated values)
│   ├─ NodeNumber
│   │   ├─ NodeDouble (IEEE 754)
│   │   ├─ NodeRational (BigRational, exact)
│   │   ├─ NodePercent (auto /100)
│   │   └─ NodeBoolean (true=1, false=0)
│   ├─ NodeString
│   ├─ NodeUnit (value + unit descriptor)
│   ├─ NodeVector (1D array)
│   ├─ NodeMatrix (2D array)
│   ├─ NodeRange (lazy start..end..step)
│   ├─ NodeFunction (user-defined)
│   └─ NodeLambda (anonymous function)
└─ NodeExpression (AST nodes)
    ├─ NodeBinary
    ├─ NodeUnary
    ├─ NodeCall
    ├─ NodeSubscript
    └─ NodeComprehension
```

**Type Coercion:**

- Boolean → Integer → Rational → Double → Percent
- Scalar broadcasts to Vector/Matrix
- No implicit String ↔ Number conversion

---

## Operators

Key design constraint: I want to be able to easily define new operators easily without directly modifying lots of source code
Users of the library should also be able to define their own

### Arithmetic

**Binary:** `+`, `-`, `*`, `/`, `^`, `mod`
**Unary:** `-` (negate), `+` (no-op)
**Postfix:** `!` (factorial), `!!` (double factorial), `%` (percent)

**Special:**

- `@` - Matrix multiplication (true dot product, not element-wise). With a function on the
  right it maps instead: `{1, 2, 3} @ (x -> x * 2)` → `{2, 4, 6}`
- `mod` - Modulo: `7 mod 3` → `1`
- `of` - Percent: `50% of 200` → `100`

### Comparison

`<`, `>`, `<=`, `>=`, `==`, `!=`

All return `NodeBoolean` (true=1.0, false=0.0)

### Logical

**Binary:** `&&` or `and`, `||` or `or`, `xor`
**Unary:** `not`

### Range

`start .. end` - Creates sequence
`start .. end step increment` - With custom step

Examples:

- `1..10` → `{1,2,3,4,5,6,7,8,9,10}`
- `0..1 step 0.1` → `{0.0, 0.1, 0.2, ..., 1.0}`
- `10..1 step -1` → `{10,9,8,...,1}`
- `0..0.3 step 0.1` → `{0, 0.1, 0.2, 0.3}`

An exact step is stepped exactly, so a tenth is a tenth and the end is not missed by a
rounding error. A bound that is already a double keeps the range on double arithmetic.

### Subscript and Slice

**Vector:**

- `v[0]` - Element access (0-indexed)
- `v[-1]` - From end
- `v[1:3]` - Slice (inclusive start, exclusive end)
- `v[:3]` - From start
- `v[2:]` - To end

**Matrix:**

- `m[i,j]` - Element access
- `m[i,:]` - Row i
- `m[:,j]` - Column j
- `m[1:3, 2:4]` - Sub-matrix

**String:**

- `s[0]` - Character access, answering a one-character string
- `s[-1]` - From end
- `s[1:3]` - Slice, answering a string

An index must be whole. A single index out of range is an error; a slice clamps.

The same forms address the element an assignment writes, except a slice. See **Element
Assignment** under Assignment.

### Unit Conversion

`value unit1 in unit2` - Convert between units
`value unit1 to unit2` - Alternative syntax
`value unit1 as unit2` - Alternative syntax

Examples:

- `100 meters in feet` → `328.084 feet`
- `32 fahrenheit to celsius` → `0 celsius`

### Lambda

`param -> expression` - Single parameter
`(p1, p2, ...) -> expression` - Multiple parameters

Examples:

- `x -> x^2`
- `(a, b) -> a + b`
- Can be passed to functions: `map(x -> x^2, {1,2,3})`

### Comprehension

`{ expression for variable in iterable if condition }`

Examples:

- `{x^2 for x in 1..10}`
- `{x for x in 1..20 if x mod 2 == 0}`
- Nested: `{x*y for x in 1..3 for y in 1..3}`
- A filter sees every loop variable: `{{x, y} for x in 1..3 for y in 1..3 if x < y}`

There is no bracketed form: `[x for x in 1..3]` is a parse error.

---

## Built-In Functions

The expression parser includes **150+ built-in functions** organized into categories. A user-defined function of the
same name takes precedence in a call (`sin(x) := 1; sin(5)` is 1). Replacing a built-in takes `:=`: `sin(x) = 1` reads as a
comparison and is a parse error, so a built-in is never replaced by accident. A variable may also share a function's name
without hiding it from calls: `sum = 5; sum(1, 2)` is 3.

### Arithmetic Functions

**Basic Math:**

- `abs(x)` - Absolute value
- `sign(x)` - Sign (-1, 0, or 1)
- `copysign(mag, sign)` - Copy sign from one number to another
- `hypot(x, y)` - sqrt(x² + y²) without intermediate overflow
- `fmod(x, y)` - Floating-point modulo
- `remainder(x, y)` - IEEE remainder operation
- `frac(x)` - Fractional part (x - floor(x))

**Powers and Roots:**

- `sqrt(x)` - Square root
- `cbrt(x)` - Cube root
- `nroot(x, n)` - Nth root
- `pow(x, n)` - Power (x^n)

**Exponentials:**

- `exp(x)` - e^x
- `exp2(x)` - 2^x
- `exp10(x)` - 10^x
- `expm1(x)` - e^x - 1 (accurate for small x)

**Logarithms:**

- `ln(x)` - Natural logarithm (base e)
- `log(x)` - Base-10 logarithm
- `log10(x)` - Base-10 logarithm (alias)
- `log2(x)` - Base-2 logarithm
- `logn(x, base)` - Logarithm with custom base

**Rounding:**

- `floor(x)` - Round down to integer
- `ceil(x)` - Round up to integer
- `round(x)` - Round to nearest integer
- `trunc(x)` - Truncate toward zero
- `roundn(x, decimals)` - Round to n decimal places
- `format(x, decimals)` - Format number with n decimal places (returns string)

**Factorials and Combinatorics:**

- `factorial(n)` - Factorial (n!)
- `doublefactorial(n)` - Double factorial (n!!)
- `binomial(n, k)` - Binomial coefficient (n choose k)
- `permutation(n, k)` - Permutations (nPk)

**GCD and LCM:**

- `gcd(a, b, ...)` - Greatest common divisor (variadic)
- `lcm(a, b, ...)` - Least common multiple (variadic)

### Trigonometric Functions

**Basic Trigonometry:**

- `sin(x)` - Sine (angle unit aware: a bare number uses the configured unit, `sin(90 degrees)` is 1 in either mode)
- `cos(x)` - Cosine (angle unit aware)
- `tan(x)` - Tangent (angle unit aware)
- `sec(x)` - Secant (1/cos)
- `csc(x)` - Cosecant (1/sin)
- `cot(x)` - Cotangent (cos/sin)

**Inverse Trigonometry:**

Every inverse answers an angle in the configured unit, and a restricted domain is a domain
error (NaN under silent validation).

- `asin(x)` - Arcsine, defined on [-1, 1] (returns in configured angle unit)
- `acos(x)` - Arccosine, defined on [-1, 1]
- `atan(x)` - Arctangent
- `asec(x)` - Arcsecant, `acos(1/x)`, defined for |x| >= 1
- `acsc(x)` - Arccosecant, `asin(1/x)`, defined for |x| >= 1
- `acot(x)` - Arccotangent, `atan(1/x)`, a quarter turn at zero
- `atan2(y, x)` - Two-argument arctangent (handles quadrants)

**Hyperbolic Functions:**

- `sinh(x)` - Hyperbolic sine
- `cosh(x)` - Hyperbolic cosine
- `tanh(x)` - Hyperbolic tangent
- `sech(x)` - Hyperbolic secant (1/cosh)
- `csch(x)` - Hyperbolic cosecant (1/sinh)
- `coth(x)` - Hyperbolic cotangent (cosh/sinh)

**Inverse Hyperbolic:**

- `asinh(x)` - Inverse hyperbolic sine
- `acosh(x)` - Inverse hyperbolic cosine, defined for x >= 1
- `atanh(x)` - Inverse hyperbolic tangent, defined for |x| < 1
- `asech(x)` - Inverse hyperbolic secant, `acosh(1/x)`, defined on (0, 1]
- `acsch(x)` - Inverse hyperbolic cosecant, `asinh(1/x)`, undefined at zero
- `acoth(x)` - Inverse hyperbolic cotangent, `atanh(1/x)`, defined for |x| > 1

**Angle Conversion:**

- `deg2rad(degrees)` - Convert degrees to radians
- `rad2deg(radians)` - Convert radians to degrees

### Vector/Collection Functions

**Aggregates:**

- `sum(...)` - Sum of elements (variadic or vector)
- `product(...)` - Product of elements (variadic or vector)
- `min(...)` - Minimum value (variadic or vector)
- `max(...)` - Maximum value (variadic or vector)
- `mean(...)` - Arithmetic mean (variadic or vector)
- `median(...)` - Median value (variadic or vector)

**Manipulation:**

- `sort(vector)` - Sort in ascending order
- `reverse(vector)` - Reverse order
- `length(vector)` - Number of elements (alias: `len`)
- `unique(vector)` - Remove duplicates
- `concat(v1, v2)` - Concatenate vectors
- `flatten(nested)` - Flatten nested structure

**Selection:**

- `first(vector)` - First element
- `last(vector)` - Last element
- `take(vector, n)` - First n elements
- `drop(vector, n)` - Skip first n elements
- `slice(vector, start, end)` - Extract slice
- `get(vector, index)` - Get element at index

**Search:**

- `indexof(vector, value)` - Find index of value (-1 if not found)
- `contains(vector, value)` - Check if contains value (boolean)
- `count(vector, value)` - Count occurrences of value

**Predicates:**

- `any(boolean_vector)` - True if any element is true
- `all(boolean_vector)` - True if all elements are true
- `none(boolean_vector)` - True if no elements are true

**Generation:**

- `seq(start, end)` - Create sequence (alternative to `..`)
- `linspace(start, end, n)` - n evenly-spaced values between start and end
- `fill(value, n)` - Create vector filled with n copies of value
- `repeat(value, n)` - Alias for `fill`

### Matrix Functions

**Properties:**

- `det(matrix)` - Determinant
- `trace(matrix)` - Trace (sum of diagonal)
- `rank(matrix)` - Matrix rank
- `rows(matrix)` - Number of rows
- `cols(matrix)` - Number of columns
- `norm(vector_or_matrix)` - Euclidean norm

**Transformations:**

- `transpose(matrix)` - Transpose
- `inverse(matrix)` - Matrix inverse
- `adjugate(matrix)` - Adjugate (transpose of the cofactor matrix) (alias: `adj`)

**Construction:**

- `identity(n)` - n×n identity matrix
- `zeros(n)` - n×n zero matrix
- `zeros(rows, cols)` - rows×cols zero matrix
- `ones(n)` - n×n matrix of ones
- `ones(rows, cols)` - rows×cols matrix of ones
- `diag(vector)` - Create diagonal matrix from vector
- `diag(matrix)` - Extract diagonal as vector

**Access:**

- `row(matrix, index)` - Extract row as vector
- `col(matrix, index)` - Extract column as vector
- `reshape(vector, rows, cols)` - Reshape vector into matrix

**Advanced:**

- `minor(matrix, i, j)` - Minor matrix (removing row i, column j)
- `cofactor(matrix, i, j)` - Cofactor value

### Statistical Functions

**Descriptive Statistics:**

- `range(vector)` - Max - Min
- `variance(vector)` - Sample variance
- `stddev(vector)` - Sample standard deviation
- `mode(vector)` - Most frequent value

**Percentiles:**

- `percentile(vector, p)` - Percentile value (p from 0-100)
- `quartile(vector, q)` - Quartile (q = 1, 2, or 3)
- `iqr(vector)` - Interquartile range (Q3 - Q1)

**Means:**

- `gmean(...)` - Geometric mean (variadic or vector)
- `hmean(...)` - Harmonic mean (variadic or vector)
- `rms(...)` - Root mean square (variadic or vector)

**Distribution Shape:**

- `skewness(vector)` - Skewness coefficient
- `kurtosis(vector)` - Kurtosis coefficient

**Relationships:**

- `covariance(vector1, vector2)` - Covariance
- `correlation(vector1, vector2)` - Pearson correlation coefficient

### String Functions

**Length and Case:**

- `len(string)` - String length (alias: `strlen`)
- `upper(string)` - Convert to uppercase
- `lower(string)` - Convert to lowercase

**Whitespace:**

- `trim(string)` - Remove leading/trailing whitespace
- `ltrim(string)` - Remove leading whitespace
- `rtrim(string)` - Remove trailing whitespace

**Substring Operations:**

- `substring(string, start, length?)` - Extract substring
- `left(string, n)` - First n characters
- `right(string, n)` - Last n characters
- `charat(string, index)` - Character at index (supports negative indexing)

**Search:**

- `indexof(string, substr)` - Find first occurrence, -1 if absent (alias: `strindexof`)
- `strlastindexof(string, substr)` - Find last occurrence
- `contains(string, substr)` - Check if contains substring (alias: `strcontains`)
- `startswith(string, prefix)` - Check if starts with prefix
- `endswith(string, suffix)` - Check if ends with suffix

**Transformation:**

- `replace(string, old, new)` - Replace all occurrences
- `reverse(string)` - Reverse string (alias: `strreverse`)
- `repeat(string, n)` - Repeat string n times (alias: `strrepeat`)
- `padleft(string, width, pad?)` - Pad to width on left (default pad: space)
- `padright(string, width, pad?)` - Pad to width on right

**Conversion:**

- `str(value)` - Convert any value to string
- `ord(char)` - Character code (ASCII/Unicode)
- `chr(code)` - Character from code

**Split/Join:**

- `split(string, delimiter?)` - Split into vector (no delimiter: individual characters)
- `join(vector, delimiter?)` - Join vector into string (default: empty)

**Utilities:**

- `isempty(string)` - Check if empty or whitespace-only

### Bitwise Operations

**Logical Operations:**

- `bitand(a, b, ...)` - Bitwise AND (variadic)
- `bitor(a, b, ...)` - Bitwise OR (variadic)
- `bitxor(a, b, ...)` - Bitwise XOR (variadic)
- `bitnot(x)` - Bitwise NOT (one's complement)

**Shifts:**

- `lshift(x, n)` - Left shift (x << n)
- `rshift(x, n)` - Arithmetic right shift (x >> n)
- `urshift(x, n)` - Unsigned right shift (x >>> n)

**Rotation:**

- `rotl(x, n)` - Rotate left by n bits
- `rotr(x, n)` - Rotate right by n bits
- `bitreverse(x)` - Reverse bit order

**Bit Counting:**

- `popcount(x)` - Count set bits (population count)
- `clz(x)` - Count leading zeros
- `ctz(x)` - Count trailing zeros

### Type System Functions

**Type Checking:**

- `typeof(x)` - Get type name as string: "rational", "double", "percent", "boolean", "string", "vector" (a range
  is a vector), "matrix", "unit" or "function". Decimals are exact, so `typeof(3.14)` is "rational"
- `isnan(x)` - Check if Not-a-Number
- `isinf(x)` - Check if infinite
- `isfinite(x)` - Check if finite number
- `isint(x)` - Check if integer
- `iseven(x)` - Check if even integer
- `isodd(x)` - Check if odd integer
- `ispositive(x)` - Check if positive
- `isnegative(x)` - Check if negative
- `iszero(x)` - Check if zero
- `isnumber(x)` - Check if number type
- `isvector(x)` - Check if vector
- `ismatrix(x)` - Check if matrix
- `isboolean(x)` - Check if boolean
- `ispercent(x)` - Check if percent type

**Type Conversion:**

- `int(x)` - Convert to integer (truncate)
- `float(x)` - Convert to double (floating-point)
- `bool(x)` - Convert to boolean (0→false, non-zero→true)
- `todouble(x)` - Force double arithmetic (disable exact rationals)
- `numerator(rational)` - Extract numerator of rational
- `denominator(rational)` - Extract denominator of rational

### Prime Number Functions

**Testing:**

- `isprime(n)` - Check if prime
- `iscoprime(a, b)` - Check if coprime (gcd = 1)

**Generation:**

- `nextprime(n)` - Next prime after n
- `prevprime(n)` - Previous prime before n

**Factorization:**

- `factors(n)` - Prime factorization with repetition (e.g., 12 → {2, 2, 3})
- `distinctfactors(n)` - Distinct prime factors (e.g., 12 → {2, 3})

**Divisors:**

- `divisorcount(n)` - Number of divisors (including 1 and n)
- `divisorsum(n)` - Sum of all divisors

**Modular:**

- `modpow(base, exponent, modulus)` - Modular exponentiation (base^exp mod modulus)

### Percentage Functions

**Conversion:**

- `percent(x)` - Wrap a number as a percentage (50 → 50%) (alias: `pct`)
- `topercent(p)` - Decimal value of a percentage (50% → 0.5)
- `percentvalue(p)` - Number written before the % sign (50% → 50) (alias: `pctvalue`)
- `ispercent(x)` - Check if value is percent type

**Calculation:**

- `percentof(percent, value)` - Calculate percentage of value (20% of 100 → 20) (alias: `pctof`)
- `whatpercent(part, whole)` - What percent is part of whole (20 of 100 → 20%) (alias: `whatpct`)
- `percentchange(old, new)` - Percent change ((new-old)/old as percent) (aliases: `pctchange`, `pctdiff`)

**Application:**

- `addpercent(value, percent)` - Add percentage to value (100 + 20% → 120) (aliases: `addpct`, `markup`)
- `subtractpercent(value, percent)` - Subtract percentage from value (100 - 20% → 80) (aliases: `subpct`, `discount`)
- `reversepercent(result, percent)` - Original value before percent increase (120, 20% → 100) (aliases: `reversepct`,
  `unmarkup`)

**Advanced:**

- `ratiotopercent(ratio)` - Convert a growth ratio to a percent change (1.5 → 50%). Computed in floating point, so
  1.2 shows as 19.999999999999996% (alias: `ratiopct`)
- `percenttoratio(percent)` - Convert a percent change to a growth ratio, 1 + p (20% → 1.2) (alias: `pctratio`)
- `percentpoints(a, b)` - Change from a to b in percentage points, b - a (30%, 50% → 20) (alias: `pp`)

### Utility Functions

**Interpolation:**

- `clamp(value, min, max)` - Clamp value to range [min, max]
- `lerp(a, b, t)` - Linear interpolation: a + t*(b-a)
- `inverselerp(a, b, value)` - Inverse lerp: (value-a)/(b-a)
- `remap(value, in_min, in_max, out_min, out_max)` - Remap value between ranges
- `smoothstep(edge0, edge1, x)` - Smooth Hermite interpolation
- `normalize(value, min, max)` - Normalize to [0, 1] range

**Distance:**

- `distance(x1, y1, x2, y2)` - 2D Euclidean distance
- `distance3d(x1, y1, z1, x2, y2, z2)` - 3D Euclidean distance
- `manhattan(x1, y1, x2, y2)` - Manhattan distance (|x2-x1| + |y2-y1|)

**Comparison:**

- `approxeq(a, b, tolerance?)` - Approximately equal (default tolerance: 1e-9)
- `compare(a, b)` - Compare values (returns -1, 0, or 1)

**Mathematical Utilities:**

- `wrap(value, min, max)` - Wrap value to range [min, max) cyclically
- `compound(principal, rate, periods)` - Compound interest calculation

### Special Mathematical Functions

**Gamma Functions:**

- `gamma(x)` - Gamma function Γ(x)
- `lgamma(x)` - Natural log of gamma function: ln(Γ(x))
- `digamma(x)` - Digamma function (logarithmic derivative of gamma)

**Beta Functions:**

- `beta(a, b)` - Beta function B(a,b)

**Error Functions:**

- `erf(x)` - Error function
- `erfc(x)` - Complementary error function (1 - erf(x))

### Higher-Order Functions

**Functional Programming:**

- `map(function, vector)` - Apply function to each element
- `filter(function, vector)` - Keep elements where function returns true
- `reduce(function, vector, initial)` - Fold/accumulate (e.g., sum, product)
- `flatmap(function, vector)` - Map, then flatten one level
- `partition(predicate, vector)` - Split into `{matching, rest}`
- `takewhile(predicate, vector)` - Leading elements while the predicate holds
- `dropwhile(predicate, vector)` - Everything after the leading run that matches

**Examples:**

```
map(x -> x * 2, {1, 2, 3})                    → {2, 4, 6}
filter(x -> x > 3, {1, 2, 3, 4, 5})           → {4, 5}
reduce((acc, x) -> acc + x, {1, 2, 3}, 0)     → 6
```

### Conditional Function

**Lazy Evaluation:**

- `if(condition, true_value, false_value)` - Conditional evaluation

**Important:** The `if` function uses **lazy evaluation** - only the selected branch is evaluated. This allows:

```
if(true, 5, 1/0)                              → 5 (no error)
if(false, 1/0, 10)                            → 10 (no error)
```

---

## Literals

### Numbers

**Integer:** `42`, `-17`, `1000000`

- Parsed as `NodeRational` by default

**Decimal:** `3.14`, `-0.5`, `0.001`

- Held exactly as `NodeRational`, so `0.1 + 0.2 - 0.3` is 0

**Scientific:** `1e3`, `2.5E-2`, `-3.2e2`

- Held exactly as `NodeRational`, like a decimal

**Double:** `2.5d`, `1e3d`, `1d/2`

- A trailing `d` makes a `NodeDouble`, opting that value into floating-point arithmetic

**Digit separators:** `1_000`, `1_000.000_1`, `0xFF_FF`

- `_` groups digits and is not part of the value
- It must sit between two digits, so `1_` is one times a variable named `_`

**Other bases:** `0x1f`, `0o17`, `0b1011`

- Hexadecimal, octal and binary integers, parsed as `NodeRational` like any other integer
- The prefix may be uppercase, and needs at least one digit after it, so `0x` alone is
  zero times a variable named `x`
- A letter outside the base ends the literal, so `0b10x` is binary two times `x`
- A decimal digit outside the base is a lexer error, so `0b12` is refused rather than read
  as one times two

**Rational:** `1/3`, `22/7`

- Explicit rational notation
- Keeps exact representation

### Strings

`"hello"` or `'world'`

- Distinct from identifiers
- No implicit number conversion

### Vectors

`{1, 2, 3}` - Comma-separated elements
`{}` - Empty vector
Elements can be any expression: `{x, y+1, sin(z)}`

### Matrices

`[1, 2; 3, 4]` - Rows separated by `;`
`[1, 2, 3]` - Single row
`[1; 2; 3]` - Single column

---

## Reserved Words

**Keywords and Operators:**

- **Logical:** `and`, `or`, `not`, `xor`
- **Control:** `for`, `in`, `if`, `step`
- **Operators:** `of`, `mod`, `to`, `as`

**Built-In Functions:**
Built-in function names can be shadowed (see "Built-In Functions" section). Key categories include:

- **Math:** `sin`, `cos`, `tan`, `abs`, `ln`, `log`, `exp`, `sqrt`, `pow`, `floor`, `ceil`, `round`, etc.
- **Vector:** `sum`, `product`, `min`, `max`, `mean`, `median`, `sort`, `length`, `map`, `filter`, `reduce`, etc.
- **Matrix:** `det`, `trace`, `transpose`, `inverse`, `identity`, `zeros`, `ones`, etc.
- **String:** `len`, `upper`, `lower`, `trim`, `substring`, `replace`, `split`, `join`, etc.
- **Bitwise:** `bitand`, `bitor`, `bitxor`, `bitnot`, `lshift`, `rshift`, `popcount`, etc.
- **Type:** `typeof`, `isnan`, `isinf`, `isint`, `int`, `float`, `bool`, etc.
- **Statistical:** `variance`, `stddev`, `percentile`, `correlation`, `skewness`, etc.
- **Prime:** `isprime`, `nextprime`, `factors`, `gcd`, `lcm`, etc.
- **Percentage:** `percent`, `percentof`, `whatpercent`, `percentchange`, etc.
- **Utility:** `clamp`, `lerp`, `distance`, `normalize`, `deg2rad`, etc.
- **Special:** `gamma`, `beta`, `erf`, `factorial`, `binomial`, etc.
- **Conditional:** `if` (lazy evaluation)

**Constants:**

**Mathematical Constants:**

- `pi` - π (3.14159...)
- `e` or `euler` - Euler's number (2.71828...)
- `phi` or `goldenratio` - Golden ratio (1.61803...)
- `tau` - 2π (6.28318...)

**Boolean Constants:**

- `true` - Boolean true (numeric value: 1)
- `false` - Boolean false (numeric value: 0)

**Special Values:**

- `infinity` or `inf` - Positive infinity
- `nan` - Not-a-Number

**Note:** Function names and constants are case-sensitive. A variable can shadow a constant (`pi = 3`), and `#pi`
still reaches the constant.

---

## Reference Symbols

Reference symbols provide **explicit disambiguation** for identifiers that could be interpreted as variables, units, or constants. They force specific resolution types regardless of context.

### Symbol Syntax

| Symbol | Type | Syntax | Description | Example |
|--------|------|--------|-------------|---------|
| `@` | Unit | `@identifier` or `@"string"` | Force unit resolution | `@m`, `@"km/h"` |
| `$` | Variable | `$identifier` | Force variable resolution | `$m1`, `$x` |
| `#` | Constant | `#identifier` | Force constant resolution | `#pi`, `#e` |

### Unit References (`@`)

**Purpose:** Force an identifier to be resolved as a unit, bypassing variable shadowing.

**Syntax:**
- **Simple:** `@identifier` - For single-word units
- **Quoted:** `@"unit name"` - For units with spaces or special characters

**Examples:**

```javascript
// Simple unit reference
m = 5                   // Define variable 'm'
m                       // Returns 5 (variable has priority)
@m                      // Returns 1 meter (forced unit resolution)
100 * @m                // Returns 100 meters

// Quoted unit reference for multi-word units
@"km/h"                 // Force kilometers per hour unit
5 @"km/h" in mph        // 3.107 miles per hour
100 * @"km/h"           // 100 kilometers per hour

// Unit conversion with explicit reference
m1 = 1000               // Variable named 'm1'
m1                      // Returns 1000 (variable)
@m in feet              // 1 meter in feet (forced unit)
```

**Use Cases:**
- Accessing units when variables shadow them
- Disambiguating multi-character unit identifiers
- Working with units containing spaces or special characters (e.g., `"km/h"`, `"degrees celsius"`)

### Variable References (`$`)

**Purpose:** Force an identifier to be resolved as a variable, even in contexts where units or functions would normally take priority.

**Syntax:** `$identifier`

**Examples:**

```javascript
// Variable shadowing
f = 10                  // Define variable 'f'
f                       // Returns 10 (variable)
f(x) = x^2              // Define function 'f'
f                       // Still 10: outside a call the variable wins
f(3)                    // 9: in a call the function wins
$f                      // Returns 10 (forced variable resolution)

// Disambiguation in postfix position
x = 2
100x                    // 100 * x = 200 (implicit multiplication)
m = 3
100m                    // 300: the variable wins over the unit
100@m                   // 100 meters (forced unit)
100 m in feet           // 328.08 feet: a conversion still reads m as a unit
```

### Constant References (`#`)

**Purpose:** Access mathematical constants even when shadowed by user variables.

**Syntax:** `#identifier`

**Examples:**

```javascript
// Normal constant access
pi                      // Returns π ≈ 3.14159...
e                       // Returns e ≈ 2.71828...

// Constant shadowing by variables
pi = 3.0                // Redefine pi as variable
pi                      // Returns 3.0 (variable shadows constant)
#pi                     // Returns π ≈ 3.14159... (forced constant)

// Using constants in calculations
tau = 10                // Shadow tau constant
2 * #pi                 // 2π (using constant, not variable)
2 * pi                  // 6.0 (using variable)
```

### Resolution Priority Without Reference Symbols

Understanding default resolution priority helps you know when reference symbols are needed:

**General Expression Context** (e.g., `m + 1`, `x * 2`):
1. Variable (highest)
2. User-defined function
3. Unit
4. Implicit multiplication

**Call Position** (e.g., `f(x)`):
1. User-defined function (highest)
2. Built-in function
3. Variable holding lambda

**Postfix Unit Context** (e.g., `100m`):
1. Variable (highest)
2. Unit
3. Implicit multiplication

A conversion (`100 m in feet`) reads the name as a unit even when a variable shares it.

**Reference symbols override these priorities**, forcing the specific resolution type regardless of context.

---

## Semantics

### Statement Sequences

Multiple statements can be separated by semicolons. The result is the value of the last expression.

**Syntax:**

```
statement1; statement2; statement3
```

**Examples:**

```
x = 5; y = 10; x + y               → 15
x = 1; x = x + 1; x = x + 1; x     → 3
a = 10; b = 20;                    → 20 (trailing semicolon allowed)
```

### Assignment

**Variable Assignment:**

```
x = 5                              → Assign value to variable
```

**Function Definition:**

```
f(x) = x^2                         → Define single-parameter function
f(x, y) = x + y                    → Define multi-parameter function
```

**Element Assignment:**

```
v[0] = 99                          → Replace one element of a vector
v[-1] = 99                         → Index from the end
m[0, 1] = 9                        → Replace one element of a matrix
m[0] = {7, 8}                      → Replace a whole row, keeping its width
m[0][1] = 9                        → Chain of groups, reaching the same element as m[0, 1]
s[0] = "H"                         → Replace one character of a string
```

The target must be a name that already has a value, and the assignment answers the value
assigned. Nothing is mutated: the collection is rebuilt and the name rebound, so a second
name for the same value is unaffected. A string element takes a single character. A slice
target (`v[1:3] = ...`) is refused.

**Function Assignment:**

```
f = x -> x^2                       → Assign lambda to variable
g = f                              → Copy function reference
```

Functions are first-class values and can be passed to other functions.

**Where `=` may stand, and `:=`:**

`=` assigns only as a statement: a `;`-separated part of the input, or anything inside
parentheses. Everywhere else an expression is expected, as in an argument, a vector or
matrix element, an index, a comprehension, or a lambda body, `=` is a `ParseException`. A
comparison mistyped as `=` therefore cannot assign by accident. `:=` assigns anywhere, and
is the spelling to use when an assignment inside an expression is meant.

```
x = 5; x + 1                       → 6
(x = 2) + x                        → 4      (parentheses are a statement position)
f(y) = (t = y + 1; t * 2); f(5)    → 12     (a parenthesised body may assign)
if((x = 5) > 3, x, 0)              → 5      (parentheses opt in inside an argument)
if(x := 5, x * 2, 0)               → 10     (':=' assigns inside an argument)
sum(a := 3, a)                     → 6
{y for x in 1..3 if (y := x * 2) > 2}  → {4, 6}

if(x = 5, 1, 2)                    → ParseException: Use '==' to compare
{x for x in v if x = 2}            → ParseException
filter(x -> x = 2, v)              → ParseException (a lambda body is an expression)
```

As a statement, `=` and `:=` mean the same thing: `x := 5` is `x = 5`.

Neither chains, and only a name, a subscript or a function signature of plain parameter names
can be assigned. `=` never replaces a built-in function; `:=` does. There is no compound
assignment and no equation solving:

```
x = y = 5                          → ParseException: Assignments cannot be chained
2 + 2 = 4                          → ParseException: Cannot assign to this expression (use '==' to compare)
x^2 = 4                            → ParseException
f(2) = 4                           → ParseException (a call, so use '==' to compare)
sin(x) = 0                         → ParseException: 'sin' is a built-in function; use ':=' to redefine it, or '==' to compare
sin(x) := 0                        → defines sin, replacing the built-in for the session
x += 1                             → ParseException: Compound assignment '+=' is not supported
x %= 2                             → ParseException: Compound assignment '%=' is not supported
x === 5                            → ParseException (use '==' to compare)
```

A name once defined is read whole: after `pi2 = 3`, `pi2` is 3 rather than `pi * 2`, in the
same input or a later one.

Formatted output puts an assignment or a sequence in parentheses wherever it is not a
statement, so it always parses back: `{a := 1, a + 1}` prints as `{(a = 1), (a + 1)}`.

`!=`, `<=`, `>=` and `==` are lexed before a lone `=`, so `2!=2` is the inequality
`2 != 2` (false), not `2! == 2`.

### Vectorization

Many built-in functions automatically apply element-wise to vectors:

**Examples:**

```
sin({0, pi/2})                     → {0, 1}
sqrt({4, 9, 16})                   → {2, 3, 4}
abs({-1, -2, 3})                   → {1, 2, 3}
iseven({1, 2, 3, 4})               → {false, true, false, true}
```

**Vectorizes:**

- All unary math functions (sin, cos, sqrt, abs, floor, etc.)
- Type checking functions (isnan, isfinite, isint, iseven, etc.)
- Type conversion functions (int, float, bool, etc.)

**Does NOT vectorize:**

- Aggregate functions (sum, mean, etc.) - these reduce vectors to scalars
- Higher-order functions (map, filter, reduce) - these expect function arguments
- String functions - operate on individual strings, not vectors of strings
- Matrix functions (det, trace, etc.) - operate on entire matrix

### Broadcasting

Broadcasting allows operations between values of different shapes.

**Scalar to Vector:**

```
{1, 2, 3} + 5                      → {6, 7, 8}
{1, 2, 3} * 2                      → {2, 4, 6}
```

**Scalar to Matrix:**

```
[[1, 2], [3, 4]] + 10              → [[11, 12], [13, 14]]
[[1, 2], [3, 4]] * 2               → [[2, 4], [6, 8]]
```

**Single-Element Vector:**

```
{1} + {1, 2, 3}                    → {2, 3, 4}
{5} * {1, 2, 3, 4}                 → {5, 10, 15, 20}
```

**Size Mismatch (Zero Extension):**
When vectors have different sizes, the shorter one is extended with zeros:

```
{1, 2} + {1, 2, 3}                 → {2, 4, 3}
# {1, 2} extended to {1, 2, 0}

{10, 20} + {1, 2, 3, 4}            → {11, 22, 3, 4}
# {10, 20} extended to {10, 20, 0, 0}
```

**Vector to Matrix (Row-wise):**

```
[[1, 2], [3, 4]] + {10, 20}        → [[11, 22], [13, 24]]
# Vector applied to each row
```

**Matrix to Matrix:**

- Compatible dimensions: element-wise operation
- Incompatible dimensions: error

### Type Promotion

Operations promote types automatically:

**Numeric Promotion:**

```
Boolean → Integer → Rational → Double → Percent
```

**Examples:**

```
true + 1                           → 2 (boolean→number)
1 + 2.5                            → 3.5 (exact: decimals are rational)
1/3 + 1/3 + 1/3                    → 1 (exact rational)
1/3 + 0.5                          → 5/6 (exact)
1/3 + 0.5d                         → 0.833... (a double operand makes a double)
```

**Boolean Arithmetic:**

```
true + 1                           → 2
false + 5                          → 5
true * 10                          → 10
true && false                      → false (logical)
1 + 2 * 3                          → 7 (numeric)
```

### Function Calls

**Standard Syntax:**

```
f(x)                               → Single argument
f(x, y)                            → Multiple arguments
f(x, y, z)                         → Three arguments
```

**Variadic Functions:**
Some functions accept variable number of arguments:

```
sum(1, 2, 3)                       → 6
sum({1, 2, 3})                     → 6 (also accepts vector)
min(5, 2, 8, 1)                    → 1
gcd(12, 18, 24)                    → 6
```

**Nested Calls:**

```
sqrt(abs(-16))                     → 4
sin(cos(0))                        → sin(1)
```

**With Lambdas:**

```
map(x -> x^2, {1, 2, 3})           → {1, 4, 9}
filter(x -> x > 0, {-1, 0, 1, 2})  → {1, 2}
```

### Matrix Subscripting

**Element Access:**

```
m[i, j]                            → Element at row i, column j (0-indexed)
```

**Row/Column Selection:**

```
m[i, :]                            → Entire row i (returns vector)
m[:, j]                            → Entire column j (returns vector)
```

**Submatrix Slicing:**

```
m[1:3, 2:4]                        → Rows 1-2, columns 2-3 (returns submatrix)
m[0:2, :]                          → First 2 rows, all columns
m[:, 1:]                           → All rows, columns 1 onward
```

**Complete Example:**

```
m = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
m[0, 0]                            → 1
m[1, 2]                            → 6
m[0, :]                            → {1, 2, 3}
m[:, 1]                            → {2, 5, 8}
m[0:2, 1:3]                        → [[2, 3], [5, 6]]
```

**Slice Syntax:**

- `:` alone means "entire dimension"
- `start:` means "from start to end"
- `:end` means "from beginning to end (exclusive)"
- `start:end` means "from start (inclusive) to end (exclusive)"

### Scoping and Closures

**Global Scope:**
All variables exist in a single global scope:

```
x = 10
f(y) = x + y
f(5)                               → 15 (accesses global x)
```

**Parameter Shadowing:**
Function parameters shadow global variables:

```
x = 10
f(x) = x * 2
f(3)                               → 6 (parameter x shadows global)
x                                  → 10 (global unchanged)
```

**Variable Capture:**
A lambda captures the variables around it when it is created. A named function reads the
global value each time it is called:

```
a = 10
f = x -> x + a
g(x) = x + a
a = 20
f(5)                               → 15 (captured a = 10)
g(5)                               → 25 (reads the current a)
```

**Closures:**
A lambda returned from a function keeps that call's parameters:

```
makeCounter(n) = (x -> n + x)
counter = makeCounter(100)
counter(5)                         → 105

n = 200                            // A global n does not reach the closure
counter(5)                         → 105
```

**Recursion:**
Functions can call themselves:

```
fact(n) = if(n <= 1, 1, n * fact(n-1))
fact(5)                            → 120
```

Recursion depth is limited (default: 256, `maxRecursionDepth`) to prevent stack overflow.

**Mutual Recursion:**
Functions can call each other:

```
even(n) = if(n == 0, true, odd(n-1))
odd(n) = if(n == 0, false, even(n-1))
even(4)                            → true
```

---

## Examples

### Basic Arithmetic

```
2 + 3 * 4              → 14
2^3^2                  → 512
-2^2                   → -4
(-2)^2                 → 4
7 / 3                  → 7/3 (rational)
7.0 / 3                → 7/3 (decimals are exact too)
7 / 3d                 → 2.333... (double)
5!                     → 120
binomial(10, 5)        → 252
```

### Implicit Multiplication

```
2x                     → 2 * x
2(x+1)                 → 2 * (x+1)
(a)(b)                 → a * b
2pi                    → 2 * pi
xsqrt(4)               → x * sqrt(4)
```

### Vectors and Matrices

```
{1,2} + {3,4}          → {4,6}
{1,2,3} * 2            → {2,4,6}
sum({1,2,3})           → 6
[1,2;3,4] @ [5,6;7,8]  → [[19,22],[43,50]]
[[1,2],[3,4]]          → 2×2 matrix (nested syntax)
```

### Ranges

```
1..5                   → {1,2,3,4,5}
1..10 step 2           → {1,3,5,7,9}
0..1 step 0.25         → {0.0, 0.25, 0.5, 0.75, 1.0}
sum(1..100)            → 5050
```

### Indexing and Slicing

```
v = {10,20,30}
v[0]                   → 10
v[1:3]                 → {20,30}
v[-1]                  → 30

m = [[1,2,3],[4,5,6],[7,8,9]]
m[0,0]                 → 1
m[1,:]                 → {4,5,6}
m[:,1]                 → {2,5,8}
m[0:2, 1:3]            → [[2,3],[5,6]]
```

### User-Defined Functions

```
square(x) = x^2
square(5)              → 25

add(x, y) = x + y
add(3, 4)              → 7

fact(n) = if(n <= 1, 1, n * fact(n-1))
fact(5)                → 120
```

### Lambda Functions

```
f = x -> x^2 + 1
f(3)                   → 10

(x -> x * 2)(5)        → 10

map(x -> x^2, {1,2,3}) → {1,4,9}
```

### Comprehensions

```
{x^2 for x in 1..5}                      → {1,4,9,16,25}
{x for x in 1..20 if x mod 2 == 0}       → {2,4,6,...,20}
{x + y for x in 1..3 for y in 1..3}      → nested iteration
```

### Higher-Order Functions

```
map(x -> x * 2, {1,2,3})                 → {2,4,6}
filter(x -> x > 3, {1,2,3,4,5})          → {4,5}
reduce((a,b) -> a + b, {1,2,3,4,5}, 0)   → 15
```

### String Functions

```
len("hello")                             → 5
upper("hello")                           → "HELLO"
substring("hello world", 6)              → "world"
replace("hello world", "world", "there") → "hello there"
split("a,b,c", ",")                      → {"a", "b", "c"}
```

### Bitwise Operations

```
bitand(12, 10)         → 8
lshift(1, 4)           → 16
popcount(7)            → 3
bitxor(5, 3)           → 6
```

### Type Checking and Conversion

```
typeof({1,2,3})        → "vector"
isnan(0/0)             → true
isint(5.0)             → true
int(5.7)               → 5
```

### Statistical Functions

```
mean({1,2,3,4,5})      → 3
variance({1,2,3,4,5})  → 2.5
correlation({1,2,3}, {1,2,3}) → 1.0
percentile({1,2,3,4,5}, 50)   → 3
```

### Percentage Functions

```
50%                    → 50% (value 0.5)
50% of 200             → 100
whatpercent(20, 100)   → 20%
percentchange(100, 120) → 20%
100 + 10%              → 110
```

### Prime Number Functions

```
isprime(17)            → true
nextprime(10)          → 11
factors(12)            → {2,2,3}
gcd(12, 18)            → 6
```

### Unit Conversion

```
100 meters in feet     → 328.084 feet
32 fahrenheit to celsius → 0 celsius
(50 + 50) meters in centimeters → 10000 cm
```

### Statement Sequences

```
x = 5; y = 10; x + y                     → 15
x = 1; x = x + 1; x = x + 1; x           → 3
a = pi; r = 5; a * r^2                   → 78.54...
```

### Vectorization

```
sin({0, pi/2})                           → {0, 1}
sqrt({4, 9, 16})                         → {2, 3, 4}
iseven({1,2,3,4})                        → {false,true,false,true}
```

### Complex Expressions

```
sum({x^2 for x in 1..10})                → 385
length(filter(x -> isprime(x), 1..100))  → 25
v = {3,1,4,1,5,9}; max(v) - min(v)       → 8
mean({x meters in centimeters for x in 1..5}) → 300 centimeters
```

---

## Ambiguity Resolution

### Decimal vs Range

`1.5` is decimal, `1..5` is range
**Rule:** After digits + `.`, if next char is `.` → range, else if digit → decimal

### Function Call vs Multiplication

`f(x)` where `f` might be undefined
**Rule:** Check if `f` is registered function; if not, treat as implicit multiplication

### Comprehension vs Vector Literal

`{x for ...}` vs `{x, y, z}`
**Rule:** After first expression in `{}`, look ahead for `for` keyword

### Subscript vs Matrix Literal

`v[0]` vs `[1,2,3]`
**Rule:** Context-dependent - after primary expression = subscript, at statement start = literal

### Minus Sign: Unary vs Binary

`2-3` vs `2 -3` vs `2 - 3`
**Rule:** After number/identifier/`)`, minus is binary. After operator/`(`, minus is unary.

### Factorial vs Double Factorial

`5!` vs `5!!`
**Rule:** Greedy matching - if second `!` present, parse as double factorial

### Assignment vs Comparison

`x = 5` vs `x == 5`, and `if(x = 5, ...)`
**Rule:** `==` is always a comparison and `=` always an assignment; the lexer takes `==`, `!=`, `<=` and `>=`
before a lone `=`. A lone `=` assigns only as a statement (top level, or inside parentheses), so in an argument,
element, index or lambda body it is a parse error rather than a silent assignment. `:=` assigns anywhere.

---

## Critical Edge Cases

### Empty Expressions

- `()` → Parse error: "Expected expression"
- `{}` → Empty vector (valid)
- `[]` → Empty matrix (valid)

### Whitespace Handling

- Whitespace is **not significant** except for implicit multiplication
- `2 x` → `2 * x` (implicit multiplication)
- `2x` → `2 * x` (same)
- Whitespace required between keywords: `forx` is identifier, `for x` is keyword + identifier

### Comments

- `// ...` runs to the end of the line
- `/* ... */` runs to the closing delimiter and may span lines; they do not nest
- Neither is recognised inside a string literal: `"a // b"` is the string `a // b`
- A comment is removed before parsing, so it may appear anywhere whitespace may
- An input that is nothing but a comment is an empty expression, and an unterminated
  block comment is a lexer error
- A single `/` is still division; only `//` and `/*` start a comment

```text
x = 5;           // set x
x * 2 /* and double it */
```

### Unicode and Extended Characters

- Identifiers: ASCII letters only `[a-zA-Z][a-zA-Z0-9_]*`
- Numbers: ASCII digits only
- Strings: May contain any Unicode (implementation-dependent)
- Mathematical symbols: Not supported in identifiers (reserved for future)

### Circular Dependencies

- `f(x) = g(x)` and `g(x) = f(x)` → Stack overflow on evaluation
- Detection: Track call stack depth, fail at configured limit
- Error: "Maximum recursion depth exceeded (256): f"

### Undefined Names

- An undefined variable is an error as soon as it is evaluated: `x + 5` with no `x` fails with
  "No value associated with 'x'"
- Only code that is never evaluated escapes, such as the branch `if` does not take:
  `if(true, 1, missing)` is 1
