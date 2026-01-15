# Lexer Architecture

**Purpose:** Multi-pass tokenization pipeline that converts input text into classified tokens

---

## Overview

The lexer transforms raw text into a stream of classified tokens ready for parsing. It uses a four-pass pipeline where each pass
has a single responsibility.

```
Input: "sin(pi2x)"
    ↓
Pass 1: TokenScanner
    → [IDENTIFIER(sin), LPAREN, IDENTIFIER(pi2x), RPAREN]
    ↓
Pass 1.5: IdentifierSplitter
    → [IDENTIFIER(sin), LPAREN, IDENTIFIER(pi), NUMBER(2), IDENTIFIER(x), RPAREN]
    ↓
Pass 2: TokenClassifier
    → [FUNCTION(sin), LPAREN, CONSTANT(pi), NUMBER(2), IDENTIFIER(x), RPAREN]
    ↓
Pass 3: ImplicitMultiplicationInserter
    → [FUNCTION(sin), LPAREN, CONSTANT(pi), MULTIPLY, NUMBER(2), MULTIPLY, IDENTIFIER(x), RPAREN]
```

---

## Pipeline Components

### Pass 1: TokenScanner

**File:** `lexer/TokenScanner.java`

**Responsibility:** Character-level scanning to produce raw tokens

**Features:**

- Number scanning (integers, decimals, scientific notation, rationals)
- String literal scanning (single and double quotes)
- Identifier scanning
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

### Pass 1.5: IdentifierSplitter

**File:** `lexer/IdentifierSplitter.java`

**Responsibility:** Split compound identifiers into separate tokens

**Purpose:** Enable natural writing like `pi2e` → `pi * 2 * e`

**Algorithm:**

1. For each IDENTIFIER token, scan left-to-right
2. Check if any prefix matches a known constant, unit, or function
3. If match found:
    - Emit the matched token (with correct classification)
    - Continue with remainder of identifier
4. If no match, check for embedded numbers
    - Split at number boundaries: `abc123def` → `abc`, `123`, `def`

**Examples:**

```
"pi2e"      → [CONSTANT(pi), NUMBER(2), CONSTANT(e)]
"sin2x"     → [FUNCTION(sin), NUMBER(2), IDENTIFIER(x)]
"2meters"   → [NUMBER(2), UNIT(meters)]
"abc123"    → [IDENTIFIER(abc), NUMBER(123)]
```

**Registry Lookup Priority:**

1. Functions (longest match first)
2. Units (longest match first)
3. Constants (longest match first)
4. Numbers (greedy digit scan)
5. Remaining characters → new identifier

**Why This Pass Exists:**

Without splitting, we'd need implicit multiplication between identifiers, which is ambiguous:

- `pi e` → Is this `pi * e` or a single identifier `pie`?

With splitting, we can unambiguously handle:

- `pi e` → two separate identifiers
- `pie` → `pi * e` (split and multiply)

### Pass 2: TokenClassifier

**File:** `lexer/TokenClassifier.java`

**Responsibility:** Classify IDENTIFIER tokens into specific types

**Classification Priority:**

1. **Keywords** → KEYWORD token
    - `and`, `or`, `not`, `xor`, `for`, `in`, `if`, `step`, `to`, `as`, `of`, `mod`
2. **Units** → UNIT token
    - Looked up in UnitRegistry
    - `meters`, `feet`, `celsius`, `fahrenheit`, etc.
3. **Functions** → FUNCTION token
    - Looked up in FunctionRegistry
    - `sin`, `cos`, `log`, `max`, etc.
4. **Constants** → CONSTANT token
    - Looked up in ConstantRegistry
    - `pi`, `euler`, `e`, `true`, `false`, etc.
5. **Default** → remains IDENTIFIER
    - User variables

**Why This Order?**

Keywords must be recognized first to avoid conflicts:

- `for` should never be treated as a variable
- `and` should never be treated as a function name

Units before functions allows shadowing:

- If `m` is both a unit (meters) and a function, treat as unit

Functions before constants allows custom functions:

- User can define `pi()` function that shadows the constant

**Edge Cases:**

1. **Empty identifiers:** Not possible (scanner requires at least one character)
2. **Reserved words as function names:** Keywords can't be used as function names
3. **Case sensitivity:** All matching is case-sensitive (`PI` ≠ `pi`)

### Pass 3: ImplicitMultiplicationInserter

**File:** `lexer/ImplicitMultiplicationInserter.java`

**Responsibility:** Insert multiplication tokens between adjacent values

**Insertion Rules:**

Insert `MULTIPLY` token between:

- Number and Identifier: `2x` → `2 * x`
- Number and Function: `2sin(x)` → `2 * sin(x)`
- Number and Constant: `2pi` → `2 * pi`
- Number and Unit: `2meters` → `2 * meters`  [Note: should be split first]
- Number and LPAREN: `2(x+1)` → `2 * (x+1)`
- RPAREN and LPAREN: `(a)(b)` → `(a) * (b)`
- RPAREN and Number: `(a)2` → `(a) * 2`
- RPAREN and Identifier: `(a)x` → `(a) * x`
- Identifier and LPAREN: `x(y)` → NO (could be function call!)
- Constant and LPAREN: `pi(x)` → `pi * (x)` [constant can't be called]
- Unit and LPAREN: `meters(x)` → `meters * (x)` [unit can't be called]

**Critical: Function Call Detection**

DO NOT insert multiplication before function calls:

```
sin(x)      → sin(x)           ✓ (function call)
x(y)        → x(y)             ✓ (function call if x is function)
2(x)        → 2 * (x)          ✓ (not a function call)
(a)(b)      → (a) * (b)        ✓ (not a function call)
```

**Algorithm:**

```java
for each pair of adjacent tokens (prev, current):
    if shouldInsertMultiplication(prev, current):
        insert MULTIPLY token between them
```

**Why Not Earlier?**

Implicit multiplication requires knowing token types:

- Need to distinguish functions from variables
- Need to know if identifier is a constant

Therefore, must run after classification.

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

- `IDENTIFIER` - User-defined variable
- `FUNCTION` - Known function name
- `CONSTANT` - Known constant name
- `UNIT` - Physical unit name
- `KEYWORD` - Reserved keyword

### 3. Operators

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

### 4. Structural

- `LPAREN` ((), `RPAREN` ())
- `LBRACE` ({), `RBRACE` (})
- `LBRACKET` ([), `RBRACKET` (])
- `COMMA` (,), `SEMICOLON` (;), `COLON` (:)
- `DOT` (.)

### 5. Special

- `EOF` - End of file marker

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
// Create registries
FunctionRegistry functionRegistry = FunctionRegistry.standard();
UnitRegistry unitRegistry = UnitRegistry.standard();
ConstantRegistry constantRegistry = ConstantRegistry.standard();
KeywordRegistry keywordRegistry = KeywordRegistry.standard();

// Create lexer
Lexer lexer = new Lexer(
    functionRegistry,
    unitRegistry,
    constantRegistry,
    keywordRegistry
);

// Tokenize
List<Token> tokens = lexer.tokenize("sin(pi * x) + 2");

// Result:
// [FUNCTION(sin), LPAREN, CONSTANT(pi), MULTIPLY, IDENTIFIER(x), RPAREN,
//  PLUS, INTEGER(2), EOF]
```

### Custom Registries

```java
// Add custom function
FunctionRegistry functions = FunctionRegistry.standard();
functions.register("myFunc");

// Add custom unit
UnitRegistry units = UnitRegistry.standard();
units.register(UnitDefinition.of("myunit", 1.5));

// Add custom constant
ConstantRegistry constants = ConstantRegistry.standard();
constants.register(ConstantDefinition.of("MY_CONST", new NodeDouble(42.0)));

Lexer lexer = new Lexer(functions, units, constants, KeywordRegistry.standard());
```

---

## Testing Strategies

### Unit Tests

**Test TokenScanner:**

```java
@Test
void scanInteger() {
    List<Token> tokens = scanner.scan("42");
    assertThat(tokens).hasSize(2);  // INTEGER + EOF
    assertThat(tokens.get(0).getType()).isEqualTo(TokenType.INTEGER);
    assertThat(tokens.get(0).getValue()).isEqualTo(42L);
}
```

**Test IdentifierSplitter:**

```java
@Test
void splitCompoundIdentifier() {
    List<Token> input = List.of(
        new Token(TokenType.IDENTIFIER, "pi2e", ...)
    );
    List<Token> output = splitter.split(input);

    assertThat(output).hasSize(3);
    assertThat(output.get(0).getLexeme()).isEqualTo("pi");
    assertThat(output.get(1).getLexeme()).isEqualTo("2");
    assertThat(output.get(2).getLexeme()).isEqualTo("e");
}
```

**Test TokenClassifier:**

```java
@Test
void classifyFunction() {
    List<Token> input = List.of(
        new Token(TokenType.IDENTIFIER, "sin", ...)
    );
    List<Token> output = classifier.classify(input);

    assertThat(output.get(0).getType()).isEqualTo(TokenType.FUNCTION);
}
```

**Test ImplicitMultiplicationInserter:**

```java
@Test
void insertMultiplication() {
    List<Token> input = List.of(
        new Token(TokenType.INTEGER, "2", 2L, ...),
        new Token(TokenType.IDENTIFIER, "x", ...)
    );
    List<Token> output = inserter.insert(input);

    assertThat(output).hasSize(3);
    assertThat(output.get(1).getType()).isEqualTo(TokenType.MULTIPLY);
}
```

### Integration Tests

Test complete lexer pipeline:

```java
@Test
void fullPipeline() {
    Lexer lexer = new Lexer(...);
    List<Token> tokens = lexer.tokenize("sin(pi2x)");

    assertThat(tokens).containsExactly(
        token(FUNCTION, "sin"),
        token(LPAREN, "("),
        token(CONSTANT, "pi"),
        token(MULTIPLY, "*"),
        token(INTEGER, "2"),
        token(MULTIPLY, "*"),
        token(IDENTIFIER, "x"),
        token(RPAREN, ")"),
        token(EOF, "")
    );
}
```

---

## Common Pitfalls for AI Agents

### 1. Forgetting Implicit Multiplication

**Problem:** Parser expects multiplication tokens, but they're missing

**Solution:** Always run ImplicitMultiplicationInserter after classification

### 2. Wrong Classification Order

**Problem:** Keywords classified as variables, breaking parser

**Solution:** Keywords must be checked first in TokenClassifier

### 3. Decimal vs Range Confusion

**Problem:** `1..5` tokenized as `DECIMAL(1.0), IDENTIFIER(.5)`

**Solution:** TokenScanner must check for double-dot when it sees `.` after a number

### 4. Function Call vs Multiplication

**Problem:** `sin(x)` becoming `sin * (x)`

**Solution:** Never insert multiplication when `FUNCTION` token is followed by `LPAREN`

### 5. Compound Identifier Infinite Loop

**Problem:** Splitter gets stuck on identifiers with no matches

**Solution:** Always make progress even if no registry match (scan one character at minimum)

---

## Extension Points

### Adding a New Operator

1. Add `TokenType` enum value
2. Update `TokenScanner` to recognize the operator text
3. Register operator implementation in `OperatorExecutor`

### Adding a New Keyword

1. Add to `KeywordRegistry`
2. Update parser to handle the keyword syntax

### Custom Lexer Behavior

Use `MathEngineConfig` to customize:

- Disable implicit multiplication
- Change identifier length limit
- Custom registry implementations

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
