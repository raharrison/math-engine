# Parser Architecture

**Purpose:** Recursive descent parser that builds an Abstract Syntax Tree (AST) from classified tokens

---

## Overview

The parser transforms a stream of tokens into an Abstract Syntax Tree (AST) using recursive descent with precedence climbing. It's
organized into three main components that work together:

```
Tokens: [NUMBER(2), MULTIPLY, IDENTIFIER(x), PLUS, NUMBER(3)]
    ↓
[Parser] coordinates → [PrecedenceParser] handles operators → [CollectionParser] handles collections
    ↓
AST:
    Binary(+)
    ├─ Binary(*)
    │  ├─ Rational(2)
    │  └─ Variable(x)
    └─ Rational(3)
```

**Key Principles:**

- **Single responsibility:** Each component handles one aspect of parsing
- **No evaluation:** Parser only builds AST, never evaluates
- **Position tracking:** Maintains token positions for error reporting
- **Backtracking support:** TokenStream provides savepoints for lookahead

---

## Architecture Components

### Parser (Main Coordinator)

**File:** `parser/Parser.java`

**Responsibility:** Entry point and pipeline coordinator

**Key Features:**

- Manages the parsing pipeline
- Handles statement sequences (semicolon-separated)
- Provides public API for parsing
- Breaks circular dependency between Parser and CollectionParser

**Constructor Pattern:**

```java
// Standard construction
Parser parser = new Parser(tokens, sourceCode, unitRegistry);

// Parser creates PrecedenceParser which uses CollectionParser
// CollectionParser needs access to expression parsing
// Solution: Use Supplier to delay the binding
CollectionParser collectionParser = new CollectionParser(stream, this::parseExpressionInternal);
PrecedenceParser precedenceParser = new PrecedenceParser(stream, collectionParser);
```

**Public Methods:**

```java
Node parse()                  // Parse full expression, expect EOF

Node parseExpression()        // Parse single expression (for sub-parsing)

int getCurrentPosition()      // Get current token position

boolean isAtEnd()             // Check if at end

Token peek()                  // Look at current token
```

**Statement Sequences:**

The parser supports multiple statements separated by semicolons:

```java
// Single statement
"x := 5"           →NodeAssignment

// Multiple statements
"x := 5; y := 10"  →

NodeSequence([NodeAssignment, NodeAssignment])

// Trailing semicolon allowed
"x := 5;"          →NodeAssignment
```

### TokenStream (Navigation)

**File:** `parser/TokenStream.java`

**Responsibility:** Token navigation and manipulation

**Core Navigation:**

```java
Token peek()                  // Get current token without consuming

Token peek(int offset)        // Lookahead by N tokens

Token advance()               // Consume and return current token

Token previous()              // Get previously consumed token

boolean isAtEnd()             // Check if at EOF
```

**Matching & Checking:**

```java
boolean check(TokenType... types)           // Check if current matches any type

boolean checkKeyword(String... keywords)    // Check if current is keyword

boolean match(TokenType... types)           // Check and consume if matches

Token expect(TokenType type, String msg)    // Consume or throw error

Token expectKeyword(String keyword)         // Expect specific keyword
```

**Backtracking:**

```java
int savePosition()                          // Save current position

void restorePosition(int position)          // Restore saved position
```

**Lookahead Utilities:**

```java
boolean isIdentifierAt(int offset)          // Check if token is identifier-like

Token getTokenAt(int position)              // Get token at absolute position
```

**Error Creation:**

```java
ParseException error(Token token, String message)  // Create exception with context
```

**Why TokenStream?**

- Abstracts token list manipulation
- Provides clean API for common operations
- Centralizes position tracking
- Simplifies backtracking logic

### PrecedenceParser (Expression Parsing)

**File:** `parser/PrecedenceParser.java`

**Responsibility:** Parse expressions with correct precedence and associativity

**Complete Precedence Chain** (lowest to highest):

```
1.  parseExpression()      → Entry point
2.  parseAssignment()      → x := 5, f(x) := expr
3.  parseLambda()          → x -> expr
4.  parseLogicalOr()       → ||, or
5.  parseLogicalXor()      → xor
6.  parseLogicalAnd()      → &&, and
7.  parseEquality()        → ==, !=
8.  parseRange()           → ..
9.  parseRelational()      → <, >, <=, >=
10. parseAdditive()        → +, -
11. parseMultiplicative()  → *, /, mod, of, @
12. parseUnary()           → -, +, not
13. parsePower()           → ^ (right-associative)
14. parsePostfix()         → !, !!, %, unit conversions
15. parseCallAndSubscript() → (), []
16. parsePrimary()         → literals, identifiers, groups
```

**Design Pattern:**

Each precedence level:

1. Parses the next higher precedence level
2. Loops while finding operators at current level
3. Builds left-associative tree (except for power and unary)

**Example:**

```java
private Node parseAdditive() {
    Node left = parseMultiplicative();  // Parse higher precedence first

    while (stream.match(TokenType.PLUS, TokenType.MINUS)) {
        Token op = stream.previous();
        Node right = parseMultiplicative();  // Parse right side
        left = new NodeBinary(op, left, right);  // Build tree
    }

    return left;
}
```

### CollectionParser (Collections & Comprehensions)

**File:** `parser/CollectionParser.java`

**Responsibility:** Parse collection literals and comprehensions

**Constructor:**

```java
// Takes expression parser as Supplier to break circular dependency
CollectionParser parser = new CollectionParser(stream, () -> parseExpression());
```

**Public Methods:**

```java
Node parseVectorOrComprehension()    // Parse {elements} or {expr for x in iter}

Node parseMatrix()                   // Parse [rows] or [[nested]]

List<SliceArg> parseSliceArgs()      // Parse [start:end] for subscripts
```

---

## Precedence Chain Details

### Level 1: Expression (Entry Point)

**Method:** `parseExpression()`

Simply delegates to assignment level:

```java
public Node parseExpression() {
    return parseAssignment();
}
```

### Level 2: Assignment

**Method:** `parseAssignment()`

**Syntax:**

- Variable assignment: `x := 5`
- Function definition: `f(x) := x + 1`
- Multi-parameter: `add(a, b) := a + b`

**Recognition Strategy:**

Uses lookahead to distinguish between assignment and regular expressions:

```java
// Save position for potential backtrack
int savepoint = stream.savePosition();

// Check for identifier
if(stream.

check(IDENTIFIER, UNIT, FUNCTION)){
Token id = stream.advance();

// Check for function parameters
    if(stream.

check(LPAREN)){
        if(

isFunctionDefinition()){  // Lookahead for 'id(...) :='
// Parse function definition
params =

parseParamList();
            stream.

expect(RPAREN);
        }else{
                // Not a definition, backtrack
                stream.

restorePosition(savepoint);
            return

parseLambda();
        }
                }

                // Check for assignment operator
                if(stream.

check(ASSIGN)){
        stream.

advance();

Node value = parseLambda();

        if(params !=null){
        return new

NodeFunctionDef(id, params, value);
        }else{
                return new

NodeAssignment(id, value);
        }
                }

                // Not an assignment, backtrack
                stream.

restorePosition(savepoint);
}

        return

parseLambda();
```

**Function Definition Lookahead:**

`isFunctionDefinition()` scans ahead to check for pattern `identifier(params) :=`:

```java
private boolean isFunctionDefinition() {
    int depth = 0;
    int lookAhead = 0;

    // Scan through balanced parentheses
    while (lookAhead < remaining) {
        Token token = stream.getTokenAt(current + lookAhead);

        if (token is LPAREN)depth++;
        else if (token is RPAREN){
            depth--;
            if (depth == 0) {
                // Check if next token is :=
                Token next = stream.getTokenAt(current + lookAhead + 1);
                return next.type == ASSIGN;
            }
        }

        lookAhead++;
    }

    return false;
}
```

**Parameter Names:**

Accepts IDENTIFIER, UNIT, or KEYWORD tokens as parameter names:

```java
// This allows: convert(x, from, to) := x from in to
// where 'from' and 'to' are keywords but valid param names
```

### Level 3: Lambda

**Method:** `parseLambda()`

**Syntax:**

- Single parameter: `x -> expr`
- Multi-parameter: `(x, y) -> expr` (handled in parsePrimary for parentheses)

**Single Parameter Pattern:**

```java
if(stream.check(IDENTIFIER)){
int savepoint = stream.savePosition();
Token param = stream.advance();

    if(stream.

match(LAMBDA)){  // LAMBDA = "->"
List<String> params = List.of(param.getLexeme());
Node body = parseExpression();  // Parse full expression as body
        return new

NodeLambda(params, body);
    }

            stream.

restorePosition(savepoint);
}

        return

parseLogicalOr();
```

**Why Check at This Level?**

Lambdas have lower precedence than most operators to allow:

```java
x ->x +1       // Body is full expression
x ->y ->x +y  // Nested lambdas (currying)
```

### Levels 4-7: Logical Operators

**Methods:** `parseLogicalOr()`, `parseLogicalXor()`, `parseLogicalAnd()`

**Operators:**

- OR: `||`, `or`
- XOR: `xor`
- AND: `&&`, `and`

**Pattern (Left-Associative):**

```java
private Node parseLogicalAnd() {
    Node left = parseEquality();

    while (stream.match(TokenType.AND) ||
            stream.checkKeyword("and") && stream.match(TokenType.KEYWORD)) {
        Token op = stream.previous();
        Node right = parseEquality();
        left = new NodeBinary(op, left, right);
    }

    return left;
}
```

**Why Two Checks for 'and'?**

Keywords are tokenized as KEYWORD, but operator is identified by lexeme:

- `stream.checkKeyword("and")` - verifies it's the "and" keyword
- `stream.match(TokenType.KEYWORD)` - consumes the token

### Level 8: Equality

**Method:** `parseEquality()`

**Operators:** `==`, `!=`

**Examples:**

```
x == 5           → Binary(==, Variable(x), Rational(5))
x != y           → Binary(!=, Variable(x), Variable(y))
a == b == c      → Binary(==, Binary(==, a, b), c)  [left-associative]
```

### Level 9: Range

**Method:** `parseRange()`

**Operators:** `..`, `.. step`

**Syntax:**

- Simple range: `1..10`
- With step: `1..10 step 2`
- Negative: `-10..-5`

**Implementation:**

```java
private Node parseRange() {
    Node left = parseRelational();

    if (stream.match(TokenType.RANGE)) {  // RANGE = ".."
        Node end = parseRelational();  // Same level to support negative ranges
        Node step = null;

        if (stream.checkKeyword("step")) {
            stream.advance();
            step = parseUnary();  // Parse step at unary level
        }

        return new NodeRangeExpression(left, end, step);
    }

    return left;
}
```

**Why Parse End at Same Level?**

To support negative ranges: `-10..-5`

- First `-10` is parsed as unary minus at higher level
- Then `..` is matched
- Then `-5` must be parsed (needs unary minus support)

### Level 10: Relational

**Method:** `parseRelational()`

**Operators:** `<`, `>`, `<=`, `>=`

**Examples:**

```
x < 5            → Binary(<, Variable(x), Rational(5))
2 + 3 < 10       → Binary(<, Binary(+, ...), Rational(10))
```

### Level 11: Additive

**Method:** `parseAdditive()`

**Operators:** `+`, `-`

**Left-Associative:**

```
10 - 5 - 2  →  Binary(-, Binary(-, 10, 5), 2)  =  (10 - 5) - 2  =  3
```

### Level 12: Multiplicative

**Method:** `parseMultiplicative()`

**Operators:** `*`, `/`, `@` (matrix multiply), `mod`, `of`

**Examples:**

```
20 / 4 / 2       → Binary(/, Binary(/, 20, 4), 2)  =  2.5
2 * 3 mod 5      → Binary(mod, Binary(*, 2, 3), 5)
50 of 100        → Binary(of, 50, 100)  =  50% of 100
```

**Matrix Multiply:**

```
A @ B            → Binary(@, A, B)  [special matrix multiplication]
```

### Level 13: Unary

**Method:** `parseUnary()`

**Operators:** `-`, `+`, `not`

**Right-Associative (Recursive):**

```java
Node parseUnary() {
    if (stream.match(MINUS, PLUS, NOT) ||
            stream.checkKeyword("not") && stream.match(KEYWORD)) {
        Token op = stream.previous();
        Node operand = parseUnary();  // Recursive for right-associativity
        return new NodeUnary(op, operand);
    }

    return parsePower();
}
```

**Examples:**

```
--5              → Unary(-, Unary(-, 5))  =  5
not not true     → Unary(not, Unary(not, true))  =  true
-2^2             → Unary(-, Binary(^, 2, 2))  =  -4
```

**Why Unary AFTER Power in Precedence?**

This is actually a mistake in the comment - unary is BEFORE power. The code shows:

```java
parseUnary() calls parsePower()
```

So unary binds tighter than power, giving:

```
-2^2  →  -(2^2)  =  -4  [mathematical convention]
```

To get `(-2)^2 = 4`, you need parentheses.

### Level 14: Power

**Method:** `parsePower()`

**Operator:** `^`

**Right-Associative:**

```java
private Node parsePower() {
    Node left = parsePostfix();

    if (stream.match(TokenType.POWER)) {
        Token op = stream.previous();
        Node right = parseUnary();  // Recursive to UNARY, not parsePower()
        return new NodeBinary(op, left, right);
    }

    return left;
}
```

**Examples:**

```
2^3^2            → Binary(^, 2, Binary(^, 3, 2))  =  2^9 = 512
2^-3             → Binary(^, 2, Unary(-, 3))  =  2^-3 = 0.125
```

**Why Call parseUnary() for Right Side?**

Allows unary minus in exponents: `2^-3`

Right-associativity ensures: `a^b^c = a^(b^c)`

### Level 15: Postfix

**Method:** `parsePostfix()`

**Operators:** `!`, `!!`, `%`, unit conversions

**Syntax:**

- Factorial: `5!`
- Double factorial: `5!!`
- Percent: `50%`
- Unit conversion: `100 meters in feet`, `10 celsius to fahrenheit`

**Implementation:**

```java
Node parsePostfix() {
    Node expr = parseCallAndSubscript();

    while (true) {
        if (stream.match(FACTORIAL, DOUBLE_FACTORIAL, PERCENT)) {
            Token op = stream.previous();
            expr = new NodeUnary(op, expr, false);  // false = postfix
        } else if (stream.checkKeyword("in", "to", "as")) {
            stream.advance();
            Token unitToken = stream.expect(UNIT, "Expected unit after conversion keyword");
            expr = new NodeUnitConversion(expr, unitToken.getLexeme());
        } else {
            break;
        }
    }

    return expr;
}
```

**Chaining:**

```
5!!              → UnaryPostfix(!!, UnaryPostfix(!, 5))
100 meters in feet → UnitConversion(100 meters, feet)
```

### Level 16: Call and Subscript

**Method:** `parseCallAndSubscript()`

**Operations:**

- Function call: `sin(x)`, `max(1, 2, 3)`
- Subscript: `v[0]`, `m[1, 2]`
- Slice: `v[1:5]`, `m[:, 2]`

**Left-to-Right Chaining:**

```java
private Node parseCallAndSubscript() {
    Node expr = parsePrimary();

    while (true) {
        if (stream.match(LPAREN)) {
            // Function call
            List<Node> args = parseArguments();
            stream.expect(RPAREN);
            expr = new NodeCall(expr, args);

        } else if (stream.match(LBRACKET)) {
            // Subscript or slice
            List<SliceArg> indices = collectionParser.parseSliceArgs();
            stream.expect(RBRACKET);
            expr = new NodeSubscript(expr, indices);

        } else if (stream.check(MULTIPLY) && isCallableOrParenthesized(expr)) {
            // Implicit call: f * (args) → f(args)
            int savePos = stream.savePosition();
            stream.advance();
            if (stream.check(LPAREN)) {
                stream.advance();
                List<Node> args = parseArguments();
                stream.expect(RPAREN);
                expr = new NodeCall(expr, args);
            } else {
                stream.restorePosition(savePos);
                break;
            }

        } else {
            break;
        }
    }

    return expr;
}
```

**Examples:**

```
sin(x)                  → Call(Variable(sin), [Variable(x)])
v[0]                    → Subscript(Variable(v), [0])
m[1, 2]                 → Subscript(Variable(m), [1, 2])
funcs[0](10)            → Call(Subscript(Variable(funcs), [0]), [10])
matrix[0][1]            → Subscript(Subscript(Variable(matrix), [0]), [1])
```

**Implicit Call Detection:**

Special case for higher-order functions:

```java
// (x -> x*2) * (5) should be parsed as ((x -> x*2))(5)
// But P * (1 + r) should stay as multiplication where P is variable

private boolean isCallableOrParenthesized(Node node) {
    return node instanceof NodeLambda ||
            node instanceof NodeCall;
    // Only lambdas and call results are callable via implicit multiplication
}
```

### Level 17: Primary

**Method:** `parsePrimary()`

**Handles:**

- Integer literals: `42`
- Decimal literals: `3.14`
- Rational literals: `22/7`
- String literals: `"hello"`
- Identifiers: `x`, function names (resolved at runtime)
- Reference symbols: `@unit`, `$var`, `#const` (explicit disambiguation)
- Parenthesized expressions: `(expr)`
- Vectors: `{1, 2, 3}`
- Matrices: `[1, 2; 3, 4]`
- Comprehensions: `{x*2 for x in 1..10}`

**Integer Literal:**

```java
if(stream.match(TokenType.INTEGER)){
Token token = stream.previous();
Object literal = token.getLiteral();
    if(literal instanceof
Long longVal){
        return new

NodeRational(longVal, 1L);
    }else if(literal instanceof
Integer intVal){
        return new

NodeRational(intVal.longValue(), 1L);
        }
        }
```

**Decimal/Scientific:**

```java
if(stream.match(DECIMAL, SCIENTIFIC)){
Token token = stream.previous();
Object literal = token.getLiteral();
    if(literal instanceof
Double doubleVal){
        return TypeCoercion.

toNumber(doubleVal);  // May become NodeRational
    }
            }
```

**Rational Literal:**

```java
// Input: "22/7"
if(stream.match(RATIONAL)){
String[] parts = token.getLexeme().split("/");
long numerator = Long.parseLong(parts[0]);
long denominator = Long.parseLong(parts[1]);

    if(denominator ==0){
        // Lazy evaluation: create division AST node
        return new

NodeBinary(divideToken,
                             new NodeRational(numerator),
                             new

NodeRational(0));
        }

        return new

NodeRational(numerator, denominator);
}
```

**Identifiers:**

```java
// IDENTIFIER, KEYWORD, or FUNCTION tokens become NodeVariable
// Resolution happens at runtime in VariableResolver
if (stream.match(IDENTIFIER, KEYWORD, FUNCTION)) {
    return new NodeVariable(stream.previous().getLexeme());
}
```

**Reference Symbols (Explicit Disambiguation):**

```java
// @unit or @"unit name" - Force unit resolution
if (stream.match(UNIT_REF)) {
    Token token = stream.previous();
    String unitName = token.getLexeme();
    boolean quoted = token.getValue() != null; // quoted if value is set
    return new NodeUnitRef(unitName, quoted);
}

// $var - Force variable resolution
if (stream.match(VAR_REF)) {
    String varName = stream.previous().getLexeme();
    return new NodeVarRef(varName);
}

// #const - Force constant resolution
if (stream.match(CONST_REF)) {
    String constName = stream.previous().getLexeme();
    return new NodeConstRef(constName);
}
```

**Parenthesized Content:**

Handles three cases:

1. Multi-parameter lambda: `(x, y) -> expr`
2. Statement sequence: `(stmt1; stmt2; expr)`
3. Regular expression: `(expr)`

```java
if(stream.match(LPAREN)){
        // Check for lambda first
        if(

isMultiParamLambda()){
        return

parseParenthesizedLambda();
    }

// Parse first expression
Node firstExpr = parseExpression();

// Check for statement sequence
    if(stream.

match(SEMICOLON)){
List<Node> statements = new ArrayList<>();
        statements.

add(firstExpr);

        while(!stream.

check(RPAREN) &&!stream.

isAtEnd()){
        statements.

add(parseExpression());
        if(!stream.

match(SEMICOLON))break;
        }

        stream.

expect(RPAREN);
        return new

NodeSequence(statements);
    }

            // Regular parenthesized expression
            stream.

expect(RPAREN);
    return firstExpr;
}
```

**Multi-Parameter Lambda Detection:**

Looks ahead for pattern: `(id [, id]*) ->`

```java
private boolean isMultiParamLambda() {
    int lookAhead = 0;

    // First token must be identifier
    if (!stream.isIdentifierAt(lookAhead)) return false;
    lookAhead++;

    // Scan through comma-separated identifiers
    while (lookAhead < remaining) {
        Token token = stream.getTokenAt(current + lookAhead);

        if (token is COMMA){
            lookAhead++;
            if (!stream.isIdentifierAt(lookAhead)) return false;
            lookAhead++;
        } else if (token is RPAREN){
            // Found closing paren, check for ->
            lookAhead++;
            Token next = stream.getTokenAt(current + lookAhead);
            return next.type == LAMBDA;
        } else{
            return false;
        }
    }

    return false;
}
```

**Collections:**

```java
// Vector or comprehension
if(stream.match(LBRACE)){
        return collectionParser.

parseVectorOrComprehension();
}

// Matrix
        if(stream.

match(LBRACKET)){
        return collectionParser.

parseMatrix();
}
```

---

## CollectionParser Details

### Vector Parsing

**Method:** `parseVectorOrComprehension()`

**Disambiguates:**

- Vector literal: `{1, 2, 3}`
- Comprehension: `{x*2 for x in 1..10}`

**Algorithm:**

```java
public Node parseVectorOrComprehension() {
    // Empty vector
    if (stream.check(RBRACE)) {
        stream.advance();
        return new NodeVector(new ArrayList<>());
    }

    // Parse first expression
    int savepoint = stream.savePosition();
    Node firstExpr = expressionParser.get();

    // Check for comprehension (has "for" keyword)
    if (stream.checkKeyword("for")) {
        stream.restorePosition(savepoint);
        return parseComprehension();
    }

    // It's a vector literal
    List<Node> elements = new ArrayList<>();
    elements.add(firstExpr);

    while (stream.match(COMMA)) {
        elements.add(expressionParser.get());
    }

    stream.expect(RBRACE);
    return new NodeVector(elements);
}
```

### Comprehension Parsing

**Method:** `parseComprehension()`

**Syntax:** `{ expression for variable in iterable [for variable in iterable]* [if condition] }`

**Supports:**

- Single iteration: `{x*2 for x in 1..10}`
- Nested iterations: `{x*y for x in 1..3 for y in 1..3}`
- Conditional filter: `{x for x in 1..10 if x > 5}`
- Nested vectors: `{{x, y} for x in 1..2 for y in 1..2}`

**Implementation:**

```java
private Node parseComprehension() {
    Node expr = expressionParser.get();

    List<Iterator> iterators = new ArrayList<>();

    // Parse all 'for variable in iterable' clauses
    while (stream.checkKeyword("for")) {
        stream.advance();

        // Variable name (IDENTIFIER or UNIT token)
        Token var = stream.check(IDENTIFIER, UNIT) ? stream.advance()
                : throw error("Expected variable name");

        stream.expectKeyword("in");
        Node iterable = expressionParser.get();
        iterators.add(new Iterator(var.getLexeme(), iterable));
    }

    // Optional condition
    Node condition = null;
    if (stream.checkKeyword("if")) {
        stream.advance();
        condition = expressionParser.get();
    }

    stream.expect(RBRACE);

    return new NodeComprehension(expr, iterators, condition);
}
```

**Examples:**

```java
// Single iteration
{x^2 for
x in 1..5}
        →

Comprehension(Binary(^, x, 2), [

Iterator(x, Range(1, 5))],null)

// Nested iterations
        {x*y for
x in 1..3 for
y in 1..3}
        →

Comprehension(Binary(*, x, y),
               [

Iterator(x, Range(1, 3)),

Iterator(y, Range(1, 3))],
        null)

// With condition
        {x for
x in 1..10if x >5}
        →

Comprehension(Variable(x),
               [

Iterator(x, Range(1, 10))],

Binary(>,x, 5))
```

### Matrix Parsing

**Method:** `parseMatrix()`

**Supports Two Syntaxes:**

1. **Traditional:** `[1, 2; 3, 4]`
    - Commas separate columns
    - Semicolons separate rows

2. **Nested:** `[[1, 2], [3, 4]]`
    - Each element is a vector (row)
    - Vectors must have same size

**Empty Matrices:**

- `[]` → 0×0 matrix (truly empty)
- `[[]]` → 1×0 matrix (one empty row)

**Algorithm:**

```java
public Node parseMatrix() {
    // Empty matrix
    if (stream.check(RBRACKET)) {
        stream.advance();
        return new NodeMatrix(new ArrayList<>());
    }

    // Parse first element
    Node firstExpr = expressionParser.get();

    // Determine syntax based on first element
    boolean isNestedSyntax = isNestedMatrixElement(firstExpr) &&
            (stream.check(COMMA) || stream.check(SEMICOLON) || stream.check(RBRACKET));

    if (isNestedSyntax) {
        return parseNestedMatrix(firstExpr);
    } else {
        return parseTraditionalMatrix(firstExpr);
    }
}

private boolean isNestedMatrixElement(Node node) {
    return node instanceof NodeVector || node instanceof NodeMatrix;
}
```

**Traditional Syntax:**

```java
private Node parseTraditionalMatrix(Node firstExpr) {
    List<List<Node>> rows = new ArrayList<>();

    // Build first row
    List<Node> firstRow = new ArrayList<>();
    firstRow.add(firstExpr);
    while (stream.match(COMMA)) {
        firstRow.add(expressionParser.get());
    }
    rows.add(firstRow);

    // Parse additional rows
    while (stream.match(SEMICOLON)) {
        List<Node> row = new ArrayList<>();
        row.add(expressionParser.get());

        while (stream.match(COMMA)) {
            row.add(expressionParser.get());
        }

        // Validate row size
        if (row.size() != firstRow.size()) {
            throw error("Matrix rows have inconsistent sizes");
        }

        rows.add(row);
    }

    stream.expect(RBRACKET);
    return new NodeMatrix(rows);
}
```

**Nested Syntax:**

```java
private Node parseNestedMatrix(Node firstExpr) {
    List<Node> rowNodes = new ArrayList<>();
    rowNodes.add(firstExpr);

    // Parse remaining rows
    while (stream.match(COMMA) || stream.match(SEMICOLON)) {
        rowNodes.add(expressionParser.get());
    }

    stream.expect(RBRACKET);

    // Convert each row node to list of elements
    List<List<Node>> rows = new ArrayList<>();
    for (Node rowNode : rowNodes) {
        List<Node> rowElements = extractRowElements(rowNode);

        // Validate consistent row sizes
        if (!rows.isEmpty() && rowElements.size() != rows.get(0).size()) {
            throw error("Matrix rows have inconsistent sizes");
        }

        rows.add(rowElements);
    }

    return new NodeMatrix(rows);
}

private List<Node> extractRowElements(Node rowNode) {
    if (rowNode instanceof NodeVector vec) {
        return vec.getElements();
    } else if (rowNode instanceof NodeMatrix mat) {
        // Matrix must be single row
        if (mat.getRows() != 1) {
            throw error("Nested matrix must be a single row");
        }
        return mat.getRow(0);
    } else {
        throw error("Expected vector or matrix");
    }
}
```

**Examples:**

```java
// Traditional syntax
[1,2;3,4]
        →Matrix([[1,2],[3,4]])

// Nested syntax
[[1,2],[3,4]]
        →

Matrix([[1,2],[3,4]])

// Single row
[1,2,3]
        →

Matrix([[1,2,3]])

// Single column
[1;2;3]
        →

Matrix([[1],[2],[3]])

// Mixed (error)
[1,2;3]
        →ParseException:"Matrix rows have inconsistent sizes"
```

### Slice Argument Parsing

**Method:** `parseSliceArgs()`

**Syntax:**

- Single index: `[5]`
- Slice: `[1:5]`
- Open-ended: `[:5]`, `[1:]`, `[:]`
- Multi-dimensional: `[1, 2]`, `[1:3, 2:4]`

**Implementation:**

```java
public List<SliceArg> parseSliceArgs() {
    List<SliceArg> args = new ArrayList<>();

    do {
        Node start = null, end = null;
        boolean isSlice = false;

        // Check if we have a start index
        if (!stream.check(COLON)) {
            start = expressionParser.get();
        }

        // Check for slice (has colon)
        if (stream.match(COLON)) {
            isSlice = true;
            // Check if we have an end index
            if (!stream.check(COMMA) && !stream.check(RBRACKET)) {
                end = expressionParser.get();
            }
        }

        args.add(new SliceArg(start, end, isSlice));

    } while (stream.match(COMMA));

    return args;
}
```

**Examples:**

```java
v[0]        → [

SliceArg(0,null,false)]
v[1:5]      → [

SliceArg(1,5,true)]
v[:5]       → [

SliceArg(null,5,true)]
v[1:]       → [

SliceArg(1,null,true)]
v[:]        → [

SliceArg(null,null,true)]
m[1,2]     → [

SliceArg(1,null,false),SliceArg(2,null,false)]
m[1:3,:]   → [

SliceArg(1,3,true),SliceArg(null,null,true)]
```

---

## Error Handling

### ParseException

**File:** `parser/ParseException.java`

**Extends:** `MathEngineException`

**Fields:**

```java
private final TokenType expected;  // Expected token type (if known)
private final TokenType actual;    // Actual token found
```

**Constructors:**

```java
ParseException(String message)

ParseException(String message, Token token)

ParseException(String message, Token token, String sourceCode)

ParseException(String message, TokenType expected, Token actual, String sourceCode)
```

**Error Message Format:**

```
Parse error at line 1, column 5: Expected ')' but found '+'
     1 | 2 * (3 + 4
       |          ^

Hint: Check for missing closing parenthesis ')'
```

**Error Context:**

The exception formats source code with a caret pointing to the error:

```java
private String formatSourceContext() {
    String[] lines = sourceCode.split("\n");
    int line = token.getLine();
    int column = token.getColumn();

    StringBuilder sb = new StringBuilder();
    String sourceLine = lines[line - 1];

    // Show the line with error
    sb.append(String.format("  %4d | %s\n", line, sourceLine));

    // Show caret pointing to error position
    sb.append("       | ");
    sb.append(" ".repeat(column - 1));
    sb.append("^");

    return sb.toString();
}
```

**Helpful Suggestions:**

The exception provides context-aware hints:

```java
private String getSuggestion() {
    if (expected == RPAREN) return "Check for missing closing parenthesis ')'";
    if (expected == RBRACE) return "Check for missing closing brace '}'";
    if (expected == RBRACKET) return "Check for missing closing bracket ']'";

    if (actual == ASSIGN && expected != ASSIGN) {
        return "Did you mean '==' for comparison? ':=' is used for assignment.";
    }

    return null;
}
```

### Common Parse Errors

**1. Unclosed Delimiters:**

```java
parse("(2 + 3")
→ParseException:Expected ')'
after expression
Hint:Check for
missing closing
parenthesis ')'

parse("{1, 2")
→ParseException:Expected '}'
after vector
elements
Hint:Check for
missing closing
brace '}'
```

**2. Unexpected Token:**

```java
parse("2 * / 3")
→ParseException:
Expected expression

parse("2 + + + 3")
→Valid!
Parses as:2+(+(+3))[
unary plus
operators]
```

**3. Empty Expression:**

```java
parse("")
→ParseException:
Empty expression

parse(";")
→ParseException:
Empty expression
```

**4. Malformed Assignment:**

```java
parse("x =")
→LexerException:
Invalid operator '='(use ':='for assignment)

parse("x := ")
→ParseException:
Expected expression
after assignment
```

**5. Matrix Size Mismatch:**

```java
parse("[1, 2; 3]")
→ParseException:
Matrix rows
have inconsistent
sizes:expected 2 elements,got 1
```

---

## Code Examples

### Parsing a Simple Expression

```java
// Input: "2 * x + 3"
List<Token> tokens = lexer.tokenize("2 * x + 3");
Parser parser = new Parser(tokens, "2 * x + 3", unitRegistry);
Node ast = parser.parse();

// Result AST:
//     Binary(+)
//     ├─ Binary(*)
//     │  ├─ Rational(2)
//     │  └─ Variable(x)
//     └─ Rational(3)
```

### Parsing a Function Definition

```java
// Input: "f(x, y) := x^2 + y^2"
Node ast = parser.parse();

// Result:
// NodeFunctionDef(name="f", params=["x", "y"],
//                 body=Binary(+, Binary(^, x, 2), Binary(^, y, 2)))
```

### Parsing a Comprehension

```java
// Input: "{x^2 for x in 1..10 if x > 5}"
Node ast = parser.parse();

// Result:
// NodeComprehension(
//     expr=Binary(^, Variable(x), Rational(2)),
//     iterators=[Iterator(x, RangeExpression(1, 10, null))],
//     condition=Binary(>, Variable(x), Rational(5))
// )
```

### Parsing Nested Structures

```java
// Input: "[[1, 2], [3, 4]]"
Node ast = parser.parse();

// Result:
// NodeMatrix([
//     [Rational(1), Rational(2)],
//     [Rational(3), Rational(4)]
// ])

// Input: "matrix[0][1]"
Node ast = parser.parse();

// Result:
// NodeSubscript(
//     target=NodeSubscript(
//         target=Variable(matrix),
//         indices=[SliceArg(0, false)]
//     ),
//     indices=[SliceArg(1, false)]
// )
```

### Handling Precedence

```java
// Input: "2 + 3 * 4^2"
Node ast = parser.parse();

// Result: Binary(+, Rational(2), Binary(*, Rational(3), Binary(^, 4, 2)))
//         2 + (3 * (4^2)) = 2 + 48 = 50

// Input: "2^3^2"
Node ast = parser.parse();

// Result: Binary(^, Rational(2), Binary(^, Rational(3), Rational(2)))
//         2^(3^2) = 2^9 = 512  [right-associative]
```

### Using Backtracking

```java
// Parser uses backtracking for disambiguation
// Input: "x(y)"
// Could be: function call OR variable assignment that failed

int savepoint = stream.savePosition();
if(stream.

check(IDENTIFIER)){
Token id = stream.advance();
    if(stream.

check(LPAREN)){
        if(

isFunctionDefinition()){
        // It's a function definition like: x(y) := ...
        }else{
        // It's a function call
        stream.

restorePosition(savepoint);
        }
                }
                }
```

---

## Common Pitfalls

### 1. Forgetting to Handle Right-Associativity

**Problem:** Power operator parsed as left-associative

**Wrong:**

```java
private Node parsePower() {
    Node left = parsePostfix();
    while (stream.match(POWER)) {
        Token op = stream.previous();
        Node right = parsePostfix();  // Wrong! Makes left-associative
        left = new NodeBinary(op, left, right);
    }
    return left;
}
```

**Correct:**

```java
private Node parsePower() {
    Node left = parsePostfix();
    if (stream.match(POWER)) {  // Use if, not while
        Token op = stream.previous();
        Node right = parseUnary();  // Recurse to unary level for right-associativity
        return new NodeBinary(op, left, right);
    }
    return left;
}
```

### 2. Not Using Backtracking for Lookahead

**Problem:** Can't distinguish between function definition and call

**Solution:** Save position before tentative parsing:

```java
int savepoint = stream.savePosition();
// Try to parse as function definition
if(!success){
        stream.

restorePosition(savepoint);
// Parse as something else
}
```

### 3. Forgetting to Check for Delimiters

**Problem:** Infinite loop in argument/parameter parsing

**Wrong:**

```java
while(true){
        args.add(parseExpression());  // Never checks for )
        stream.

match(COMMA);
}
```

**Correct:**

```java
if(!stream.check(RPAREN)){  // Check for closing delimiter
        do{
        args.

add(parseExpression());
        }while(stream.

match(COMMA));
        }
```

### 4. Calling Wrong Precedence Level

**Problem:** Breaking precedence chain

**Wrong:**

```java
private Node parseAdditive() {
    Node left = parsePrimary();  // Skipped multiplicative and unary!
    // ...
}
```

**Correct:**

```java
private Node parseAdditive() {
    Node left = parseMultiplicative();  // Call next higher level
    // ...
}
```

### 5. Not Handling Empty Collections

**Problem:** Parser expects at least one element

**Wrong:**

```java
public Node parseVector() {
    List<Node> elements = new ArrayList<>();
    do {
        elements.add(parseExpression());  // Fails on empty vector
    } while (stream.match(COMMA));
    return new NodeVector(elements);
}
```

**Correct:**

```java
public Node parseVector() {
    if (stream.check(RBRACE)) {  // Check for empty vector first
        stream.advance();
        return new NodeVector(new ArrayList<>());
    }

    List<Node> elements = new ArrayList<>();
    // ... parse elements
}
```

### 6. Creating Circular Dependencies

**Problem:** Parser needs CollectionParser, CollectionParser needs Parser

**Solution:** Use Supplier to break cycle:

```java
// In Parser constructor:
CollectionParser collectionParser = new CollectionParser(stream, this::parseExpressionInternal);
PrecedenceParser precedenceParser = new PrecedenceParser(stream, collectionParser);
```

---

## Testing Strategies

### Test Precedence

**Goal:** Verify operator precedence is correct

**Strategy:** Parse expressions and check AST structure

```java

@Test
void testMultiplicationBeforeAddition() {
    Node node = parse("2 + 3 * 4");

    assertThat(node).isInstanceOf(NodeBinary.class);
    NodeBinary binary = (NodeBinary) node;

    // Top level should be addition
    assertThat(binary.getOperator().getLexeme()).isEqualTo("+");

    // Right side should be multiplication
    assertThat(binary.getRight()).isInstanceOf(NodeBinary.class);
    NodeBinary mult = (NodeBinary) binary.getRight();
    assertThat(mult.getOperator().getLexeme()).isEqualTo("*");
}
```

### Test Associativity

**Goal:** Verify left vs right associativity

**Left-Associative Test:**

```java

@Test
void testLeftAssociativeSubtraction() {
    // 10 - 5 - 2 = (10 - 5) - 2 = 3
    Node node = parse("10 - 5 - 2");

    NodeBinary binary = (NodeBinary) node;
    assertThat(binary.getOperator().getLexeme()).isEqualTo("-");

    // Left side should be subtraction
    assertThat(binary.getLeft()).isInstanceOf(NodeBinary.class);
    NodeBinary leftSub = (NodeBinary) binary.getLeft();
    assertThat(leftSub.getOperator().getLexeme()).isEqualTo("-");

    // Right side should be number
    assertThat(binary.getRight()).isInstanceOf(NodeNumber.class);
}
```

**Right-Associative Test:**

```java

@Test
void testRightAssociativeExponentiation() {
    // 2^3^2 = 2^(3^2) = 2^9 = 512
    Node node = parse("2^3^2");

    NodeBinary binary = (NodeBinary) node;
    assertThat(binary.getOperator().getLexeme()).isEqualTo("^");

    // Right side should be exponentiation (NOT left)
    assertThat(binary.getRight()).isInstanceOf(NodeBinary.class);
    NodeBinary rightPow = (NodeBinary) binary.getRight();
    assertThat(rightPow.getOperator().getLexeme()).isEqualTo("^");

    // Left side should be number
    assertThat(binary.getLeft()).isInstanceOf(NodeNumber.class);
}
```

### Test Error Cases

**Goal:** Verify proper error reporting

```java

@Test
void testUnmatchedLeftParen() {
    assertThatThrownBy(() -> parse("(2 + 3"))
            .isInstanceOf(ParseException.class)
            .hasMessageContaining("Expected ')'");
}

@Test
void testMatrixSizeMismatch() {
    assertThatThrownBy(() -> parse("[1, 2; 3]"))
            .isInstanceOf(ParseException.class)
            .hasMessageContaining("inconsistent sizes");
}
```

### Test Special Constructs

**Comprehensions:**

```java

@Test
void testSimpleComprehension() {
    Node node = parse("{x^2 for x in 1..5}");
    assertThat(node).isInstanceOf(NodeComprehension.class);

    NodeComprehension comp = (NodeComprehension) node;
    assertThat(comp.getExpression()).isInstanceOf(NodeBinary.class);
    assertThat(comp.getVariable()).isEqualTo("x");
    assertThat(comp.getIterable()).isInstanceOf(NodeRangeExpression.class);
}
```

**Function Definitions:**

```java

@Test
void testMultiParameterFunctionDefinition() {
    Node node = parse("add(a, b) := a + b");
    assertThat(node).isInstanceOf(NodeFunctionDef.class);

    NodeFunctionDef funcDef = (NodeFunctionDef) node;
    assertThat(funcDef.getName()).isEqualTo("add");
    assertThat(funcDef.getParameters()).containsExactly("a", "b");
    assertThat(funcDef.getBody()).isInstanceOf(NodeBinary.class);
}
```

### Test Edge Cases

```java

@Test
void testEmptyVector() {
    Node node = parse("{}");
    assertThat(node).isInstanceOf(NodeVector.class);
    NodeVector vec = (NodeVector) node;
    assertThat(vec.size()).isEqualTo(0);
}

@Test
void testDoubleNegation() {
    Node node = parse("--5");
    assertThat(node).isInstanceOf(NodeUnary.class);
    NodeUnary outer = (NodeUnary) node;
    assertThat(outer.getOperand()).isInstanceOf(NodeUnary.class);
}

@Test
void testChainedSubscripts() {
    Node node = parse("matrix[0][1]");
    assertThat(node).isInstanceOf(NodeSubscript.class);
    NodeSubscript outer = (NodeSubscript) node;
    assertThat(outer.getTarget()).isInstanceOf(NodeSubscript.class);
}
```

---

## Extension Points

### Adding a New Binary Operator

1. **Add TokenType** (if needed): Already exists for most operators
2. **Update Lexer**: Ensure token is generated
3. **Choose Precedence Level**: Add to appropriate `parse*()` method
4. **Implement Operator**: In operator package (not parser's job)

**Example: Adding modulo (`mod`) operator at multiplicative level:**

```java
private Node parseMultiplicative() {
    Node left = parseUnary();

    while (stream.match(MULTIPLY, DIVIDE, AT, MOD) ||  // Add MOD
            stream.checkKeyword("mod") && stream.match(KEYWORD)) {
        Token op = stream.previous();
        Node right = parseUnary();
        left = new NodeBinary(op, left, right);
    }

    return left;
}
```

### Adding a New Unary Operator

1. **Determine if Prefix or Postfix**
2. **Add to parseUnary()** (prefix) or **parsePostfix()** (postfix)
3. **Implement operator logic** in operator package

**Example: Adding prefix operator `~` (bitwise NOT):**

```java
Node parseUnary() {
    if (stream.match(MINUS, PLUS, NOT, TILDE)) {  // Add TILDE
        Token op = stream.previous();
        Node operand = parseUnary();
        return new NodeUnary(op, operand);
    }
    return parsePower();
}
```

### Adding New Primary Expression Type

**Add to parsePrimary():**

```java
private Node parsePrimary() {
    // ... existing cases ...

    // New: Date literals
    if (stream.match(TokenType.DATE)) {
        Token token = stream.previous();
        return new NodeDate(token.getLiteral());
    }

    // ... rest of method
}
```

### Custom Delimiter Pairs

**Example: Adding angle brackets `< >` for tuple literals:**

1. **Add tokens:** `LANGLE`, `RANGLE`
2. **Add to parsePrimary():**

```java
if(stream.match(LANGLE)){
        return

parseTuple();
}
```

3. **Implement parsing:**

```java
private Node parseTuple() {
    List<Node> elements = new ArrayList<>();
    if (!stream.check(RANGLE)) {
        do {
            elements.add(parseExpression());
        } while (stream.match(COMMA));
    }
    stream.expect(RANGLE, "Expected '>' after tuple");
    return new NodeTuple(elements);
}
```

---

## Performance Considerations

### Token List Size

**Issue:** Large expressions create many tokens

**Optimization:** TokenStream uses simple array access (O(1))

**Note:** Backtracking only saves position (int), not copying tokens

### Lookahead Cost

**Issue:** `isFunctionDefinition()` scans ahead through balanced parens

**Worst Case:** O(n) where n = number of tokens in parameter list

**Mitigation:** Only called when seeing `identifier(`, which is rare in deep expressions

### AST Node Allocation

**Issue:** Every sub-expression creates a node object

**Note:** This is unavoidable for AST construction

**Optimization Opportunity:** Node pooling for common patterns (future work)

### Recursive Descent Depth

**Issue:** Deep expression nesting → deep call stack

**Mitigation:** MathEngineConfig has `maxExpressionDepth` limit

**Example:**

```java
// Very deep nesting
(((((((((((((((((((((x)))))))))))))))))))))
// Creates ~20 stack frames in parsePrimary()
```

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[LEXER.md](./LEXER.md)** - Previous stage (tokenization)
- **[EVALUATOR.md](./EVALUATOR.md)** - Next stage (evaluation)
- **[NODES.md](./NODES.md)** - AST node types and hierarchy
- **[../GRAMMAR.md](../GRAMMAR.md)** - Grammar specification

---

**For AI Agents:**

When working with the parser:

1. **Start with precedence chain** - Understand the order of operations first
2. **Use TokenStream abstraction** - Don't manipulate tokens directly
3. **Test precedence and associativity** - These are the most common bugs
4. **Use backtracking carefully** - Save/restore positions for lookahead, not speculation
5. **Handle empty cases** - Check for closing delimiters before parsing elements
6. **Follow the pattern** - Each precedence level follows the same structure
7. **Don't evaluate** - Parser builds AST only, evaluation happens later

Common patterns:

- Left-associative: Use `while` loop building left-to-right tree
- Right-associative: Use `if` statement with recursive call
- Lookahead: Save position, try parse, restore if fails
- Disambiguation: Use lookahead to choose between alternatives
- Error recovery: Use `expect()` for required tokens, provides good error messages
