# Lexer Architecture

**Purpose:** Two-stage tokenization pipeline that converts input text into classified tokens

---

## Overview

The lexer transforms raw text into a stream of classified tokens ready for parsing. It uses a two-stage pipeline optimized for efficiency and correctness.

```
Input: "sin(pi2x)"
    ↓
Stage 1: TokenScanner
    → [IDENTIFIER(sin), LPAREN, IDENTIFIER(pi2x), RPAREN]
    ↓
Stage 2: TokenProcessor (single-pass: split + classify + implicit multiplication)
    → [FUNCTION(sin), LPAREN, IDENTIFIER(pi), MULTIPLY, INTEGER(2), MULTIPLY, IDENTIFIER(x), RPAREN]
```

**Key Design Principle:** Conservative splitting - only split identifiers when unambiguous (constants and functions). Units are resolved at runtime where variable context is available.

---

## Pipeline Components

### Stage 1: TokenScanner

**File:** `lexer/TokenScanner.java`

**Responsibility:** Character-level scanning to produce raw tokens

**Features:**

- Number scanning (integers, decimals, scientific notation, rationals)
- String literal scanning (single and double quotes)
- Identifier scanning
- Reference symbol scanning (`@unit`, `@"km/h"`, `$var`, `#const`)
- Operator scanning (single and multi-character)
- Structural tokens (parentheses, brackets, braces, commas, semicolons)
- Position tracking (line and column numbers)

**Key Challenge: Decimal vs Range Disambiguation**

The scanner must distinguish between:

- `1.5` - decimal number
- `1..5` - range expression

**Solution:** After scanning a number followed by `.`, check if the next character is also `.`:

- If next is `.` → emit integer `1`, then scan `..` as RANGE operator
- If next is digit → continue scanning decimal `1.5`
- Otherwise → emit integer `1`, emit DOT token

**Number Formats:**

```java
// Integers
"42"        → INTEGER(42)
"-17"       → MINUS, INTEGER(17)  // Unary minus handled by parser

// Decimals
"3.14"      → DECIMAL(3.14)
".5"        → DECIMAL(0.5)

// Scientific notation
"1e3"       → DECIMAL(1000.0)
"2.5E-2"    → DECIMAL(0.025)

// Rationals (explicit)
"22/7"      → RATIONAL(22, 7)
```

**Operator Scanning:**

Multi-character operators require lookahead:

```java
'=' → check next:
    '=' → EQUALS (==)
    else → ERROR (= is not valid, must use :=)

':' → check next:
    '=' → ASSIGN (:=)
    else → COLON (:)

'!' → check next:
    '!' → DOUBLE_FACTORIAL (!!)
    '=' → NOT_EQUALS (!=)
    else → FACTORIAL (!)

'.' → check next:
    '.' → RANGE (..)
    else → DOT (.)
```

**Reference Symbol Scanning:**

The scanner recognizes reference symbols for explicit disambiguation:

```java
'@' → UNIT_REF or UNIT_REF_STR (unit reference)
    '@' 'identifier' → UNIT_REF(@m)
    '@' '"' string '"' → UNIT_REF_STR(@"km/h")  // For units with spaces

'$' → VAR_REF (variable reference)
    '$' 'identifier' → VAR_REF($var)

'#' → CONST_REF (constant reference)
    '#' 'identifier' → CONST_REF(#pi)
```

**Examples:**

```
"@m"            → UNIT_REF(m)
"@\"km/h\""     → UNIT_REF_STR("km/h")
"$x"            → VAR_REF(x)
"#pi"           → CONST_REF(pi)
```

### Stage 2: TokenProcessor

**File:** `lexer/TokenProcessor.java`

**Responsibility:** Single-pass processing that splits identifiers, classifies tokens, and inserts implicit multiplication

**Design Philosophy:** Conservative splitting to avoid ambiguity. Only split when unambiguous:

- ✅ Split constants (pi, e) - fixed values, can't be redefined
- ✅ Split functions (sin, cos) - unambiguous, can't be shadowed by variables
- ❌ DON'T split units - they can be shadowed by variables (e.g., `m := 5`)
- ❌ Runtime resolution needed for context-dependent priority

**Three Responsibilities:**

1. **Identifier Splitting** - Break compound identifiers at digit boundaries and function suffixes
2. **Token Classification** - Classify identifiers as keywords, functions, or leave for runtime resolution
3. **Implicit Multiplication** - Insert multiplication tokens between adjacent values

#### 1. Identifier Splitting

**Algorithm:**

For each IDENTIFIER token:

1. Check if it's a definition target (`m1 :=` or `f(...) :=`) - if so, don't split
2. Try digit splitting: `pi2` → `pi`, `2` (only if prefix is constant/function)
3. Try function suffix splitting: `xsin` → `x`, `sin` (only if prefix is single-char or constant)

**Digit Splitting Rules:**

```
"pi2e"      → [IDENTIFIER(pi), INTEGER(2), IDENTIFIER(e)]  ✓ (pi is constant)
"sin2x"     → [IDENTIFIER(sin), INTEGER(2), IDENTIFIER(x)] ✓ (sin is function)
"m1"        → [IDENTIFIER(m1)]                              ✓ (m is unit, not split - could be variable)
"km5"       → [IDENTIFIER(km5)]                             ✓ (km is unit, not split)
"e3"        → [IDENTIFIER(e), INTEGER(3)]                   ✓ (e is constant)
```

**Function Suffix Splitting Rules:**

```
"xsin"      → [IDENTIFIER(x), IDENTIFIER(sin)]    ✓ (single-char prefix)
"pisin"     → [IDENTIFIER(pi), IDENTIFIER(sin)]   ✓ (constant prefix)
"msin"      → [IDENTIFIER(msin)]                  ✓ (unit prefix, not split)
"sincos"    → [IDENTIFIER(sincos)]                ✓ (function prefix, ambiguous)
```

**Why This Conservative Approach?**

- **Variable names like `m1`, `s2`, `g3`** are common - splitting would break them
- **Unit contexts are explicit** - `100 m` uses spaces, not `100m`
- **Runtime resolution** - VariableResolver can check if `m1` is a variable before falling back to unit

#### 2. Token Classification

**Classification Rules:**

For each IDENTIFIER token:

1. **Keyword operators** (`and`, `or`, `xor`, `not`, `mod`, `of`) → Corresponding operator token type
2. **Reserved keywords** (`for`, `in`, `if`, `step`, `true`, `false`, `to`, `as`) → KEYWORD
3. **Functions** (from function registry) → FUNCTION
4. **Everything else** → remains IDENTIFIER (resolved at runtime as variable, unit, or constant)

**Runtime Resolution (Evaluator):**

Identifiers left as IDENTIFIER tokens are resolved by VariableResolver with context-aware priority:

- **General context**: variable → function → unit
- **Call target**: function → variable
- **Postfix unit**: unit → variable

**Why Not Classify Constants/Units at Lexer?**

- **Shadowing**: `pi := 3` should work, but lexer doesn't know about variable assignments yet
- **Context matters**: `100m` should be unit, but `m` alone might be a variable
- **Flexibility**: User can redefine constants/units as variables

#### 3. Implicit Multiplication Insertion

**Insertion Rules:**

Insert `MULTIPLY` token between:

- Number and Identifier: `2x` → `2 * x`
- Number and Function: `2sin(x)` → `2 * sin(x)`
- Number and Reference: `2@m` → `2 * @m`
- Number and LPAREN: `2(x+1)` → `2 * (x+1)`
- RPAREN and LPAREN: `(a)(b)` → `(a) * (b)`
- RPAREN and Number: `(a)2` → `(a) * 2`
- RPAREN and Identifier: `(a)x` → `(a) * x`
- RBRACE and Number: `{1,2}x` → `{1,2} * x`
- RBRACKET and Number: `[1,2]x` → `[1,2] * x`
- Identifier and Identifier: `x y` → `x * y` (same line only)
- Postfix and Number: `5! 2` → `5! * 2`

**Critical: Function Call Detection**

DO NOT insert multiplication before function calls:

```
sin(x)      → sin(x)           ✓ (function call)
x(y)        → x(y)             ✓ (could be function call, let parser decide)
2(x)        → 2 * (x)          ✓ (not a function call)
(a)(b)      → (a) * (b)        ✓ (not a function call)
```

**Same-Line Requirement:**

Implicit multiplication is NOT inserted across line boundaries:

```
x          → x (line 1)
y          → y (line 2)
// NOT: x * y
```

This prevents accidental multiplication when user intends separate expressions.

**Algorithm:**

```java
for each pair of adjacent tokens (prev, current):
    if prev.line == current.line && shouldInsertMultiplication(prev, current):
        insert MULTIPLY token between them
```

---

## Token Types

**File:** `lexer/TokenType.java`

**Categories:**

### 1. Literals

- `INTEGER` - Integer literal
- `DECIMAL` - Decimal literal
- `RATIONAL` - Rational literal (a/b format)
- `STRING` - String literal

### 2. Identifiers & Classifications

- `IDENTIFIER` - User-defined variable or runtime-resolved identifier
- `FUNCTION` - Known function name
- `KEYWORD` - Reserved keyword

### 3. Reference Symbols

- `UNIT_REF` - Explicit unit reference (`@m`)
- `VAR_REF` - Explicit variable reference (`$x`)
- `CONST_REF` - Explicit constant reference (`#pi`)

### 4. Operators

**Arithmetic:**

- `PLUS` (+), `MINUS` (-), `MULTIPLY` (*), `DIVIDE` (/), `POWER` (^)
- `MOD` (mod), `OF` (of)

**Comparison:**

- `LESS` (<), `GREATER` (>), `LESS_EQUAL` (<=), `GREATER_EQUAL` (>=)
- `EQUALS` (==), `NOT_EQUALS` (!=)

**Logical:**

- `AND` (&&, and), `OR` (||, or), `NOT` (not), `XOR` (xor)

**Postfix:**

- `FACTORIAL` (!), `DOUBLE_FACTORIAL` (!!), `PERCENT` (%)

**Special:**

- `RANGE` (..), `ASSIGN` (:=), `ARROW` (->), `AT` (@)

### 5. Structural

- `LPAREN` ((), `RPAREN` ())
- `LBRACE` ({), `RBRACE` (})
- `LBRACKET` ([), `RBRACKET` (])
- `COMMA` (,), `SEMICOLON` (;), `COLON` (:)
- `DOT` (.)

### 6. Special

- `EOF` - End of file marker
- `SCIENTIFIC` - Scientific notation number (alternative to DECIMAL)

---

## Token Class

**File:** `lexer/Token.java`

**Fields:**

```java
class Token {
    TokenType type;        // Token classification
    String lexeme;         // Original text
    Object value;          // Parsed value (for numbers)
    int line;              // Line number (1-indexed)
    int column;            // Column number (1-indexed)
    int position;          // Absolute character position
}
```

**Immutable:** All fields are final and set in constructor

**Value Field:**

- For `INTEGER`: Long
- For `DECIMAL`: Double
- For `RATIONAL`: BigRational
- For `STRING`: String (without quotes)
- For others: null

---

## CharacterScanner

**File:** `lexer/CharacterScanner.java`

**Purpose:** Low-level character navigation for TokenScanner

**Methods:**

```java
char peek()              // Look at current char
char peek(int offset)    // Look ahead n chars
char advance()           // Consume and return current char
boolean match(char c)    // Check and optionally consume
boolean isAtEnd()        // Check if at end of input
```

**Position Tracking:**

- Maintains line and column numbers
- Updates on every character advance
- Handles newlines correctly (increment line, reset column)

---

## Error Handling

### LexerException

**File:** `lexer/LexerException.java`

**Extends:** `MathEngineException`

**Thrown For:**

- Unterminated string literals
- Invalid characters
- Malformed numbers
- Unknown operators

**Error Context:**

```java
throw new LexerException(
    token,
    sourceCode,
    "Unterminated string literal"
);
```

**Error Message Format:**

```
Error: Unterminated string literal

  line 2: x := "hello
             | ^^^^^^
```

---

## Usage Examples

### Basic Usage

```java
// Create lexer (automatically loads standard registries)
Lexer lexer = Lexer.create();

// Tokenize
List<Token> tokens = lexer.tokenize("sin(pi * x) + 2");

// Result:
// [FUNCTION(sin), LPAREN, IDENTIFIER(pi), MULTIPLY, IDENTIFIER(x), RPAREN,
//  PLUS, INTEGER(2), EOF]
// Note: 'pi' stays as IDENTIFIER - resolved at runtime as constant or variable
```

**With Reference Symbols:**

```java
// Explicit disambiguation
tokens = lexer.tokenize("m1 := 5; @m in feet");

// Result:
// [IDENTIFIER(m1), ASSIGN, INTEGER(5), SEMICOLON,
//  UNIT_REF(m), KEYWORD(in), IDENTIFIER(feet), EOF]
// Note: @m forces unit resolution, m1 is variable name
```

---

## Common Pitfalls for AI Agents

### 1. Over-Eager Splitting

**Problem:** Splitting units at digits breaks variable names like `m1`, `s2`

**Solution:** TokenProcessor only splits constants and functions, not units. Units resolved at runtime.

### 2. Classifying Constants/Units at Lexer

**Problem:** Marking `pi` as CONSTANT prevents user from defining `pi := 3`

**Solution:** Leave as IDENTIFIER - VariableResolver handles runtime priority (variables shadow constants)

### 3. Decimal vs Range Confusion

**Problem:** `1..5` tokenized as `DECIMAL(1.0), IDENTIFIER(.5)`

**Solution:** TokenScanner must check for double-dot when it sees `.` after a number

### 4. Function Call vs Multiplication

**Problem:** `sin(x)` becoming `sin * (x)`

**Solution:** Never insert multiplication when `FUNCTION` token is followed by `LPAREN`

### 5. Cross-Line Implicit Multiplication

**Problem:** `x\ny` becoming `x * y` across lines

**Solution:** TokenProcessor checks line numbers - only insert multiplication on same line

### 6. Reference Symbol Syntax

**Problem:** Treating `@` as regular operator instead of reference prefix

**Solution:** TokenScanner must recognize `@`, `$`, `#` as reference prefixes and scan complete reference


---

## Performance Considerations

### Token Allocation

Tokens are immutable, created once per lexical element. For very long expressions:

- Consider token pooling for common tokens (numbers 0-9, operators)
- Reuse EOF token singleton

### String Interning

Token lexemes are stored as strings. For repeated tokenization:

- Consider interning common lexemes (operators, keywords)
- Reduces memory for duplicate strings

### Registry Lookups

Classification requires registry lookups. Optimize by:

- Use hash-based registries (O(1) lookup)
- Longest-match-first for prefix scanning in IdentifierSplitter
- Cache negative lookups in identifier splitting

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[PARSER.md](./PARSER.md)** - Next stage (parser)
- **[../GRAMMAR.md](../GRAMMAR.md)** - Grammar specification
