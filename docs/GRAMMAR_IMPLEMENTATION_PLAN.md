# Math Engine Parser/Evaluator Implementation Plan

**Purpose:** Step-by-step implementation guide for building a complete parser/evaluator module
---

## Architecture

### Lexer

**Three-pass tokenization**:

1. **Scan**: Raw tokenization of characters
2. **Classify**: Identify keywords, units, functions
3. **Insert multiplication**: Add implicit `*` tokens (e.g., `2x` → `2 * x`)

**Token classification order**:

1. Keyword operators (and, or, etc.)
2. Keywords (true, false, etc.)
3. Units (from UnitRegistry)
4. Functions (from FunctionRegistry)
5. Identifiers (default)

### Parser

**Precedence (low to high)**:

- Expression → Assignment → Lambda → Comprehension → LogicalOr → LogicalXor → LogicalAnd → Equality → Relational → Range →
  Additive → Multiplicative → Power → Unary → Postfix → Subscript → Call → Primary

**Special handling**:

- Sequences at top level
- Lazy evaluation for `if`, `&&`, `||`
- Function definition lookahead

### Evaluator

**Context hierarchy**:

- Root context (globals, constants)
- Function call contexts (parameters + closure)
- Lambda contexts (parameters + current context)

**Shared state**:

- `RecursionTracker` - prevents infinite recursion
- Registries - builtin functions, units

## Testing Strategy

Tests are organized by feature:

- `functions_definitions.json` - Function definition and calling
- `functions_recursive.json` - Recursive functions
- Various operator and feature tests

## Future Improvements

1. **Better error messages**: Include source position in all errors
2. Defining custom operators and functions
3. **Memoization**: Cache function results for pure functions
4. **Tail call optimization**: Detect tail-recursive functions and optimize

## Performance Considerations

- Rational arithmetic is exact but slower than double
- Recursion depth limited to prevent stack overflow (default: 1000)
- Lazy evaluation prevents unnecessary computation in conditionals

## Prerequisites

Before starting implementation, ensure you have:

1. **Read all grammar documentation:**
    - `GRAMMAR.md` - Formal EBNF grammar specification
    - `parser_DESIGN.md` - Design for parser
    - `GRAMMAR_TESTS.md` - Comprehensive test catalog

**IMPORTANT: USE CODE EXAMPLES IN THESE DOCUMENTS AS INSPIRATION - NOT AS FINAL IMPLEMENTATION. USE OR DONT USE AS NECESSARY**

**DO NOT CUT CORNERS IN IMPLEMENTATION. AVOID HARDCODING VALUES.**

2. **Understand the overall architecture:**
   ```
   Input String
       ↓
   [Lexer] → Tokens
       ↓
   [Parser] → AST
       ↓
   [Evaluator] → Result (NodeConstant)
   ```

3. **Key design principles:**
    - Exact rational arithmetic by default
    - Type safety with clear coercion rules
    - Completeness of grammar
    - Comprehensive error reporting with position information
    - Stack overflow protection for recursion
    - Lazy evaluation where beneficial (ranges)

---

### Implementation Steps

#### 0.1 High Level Project Structure

Note that this is NOT final. Use or dont use it. Also some of these files already exist in the parser package. We DO NOT WANT to
reuse
that necessarily. Take if you need but dont copy it as it doesnt work. This is the parser package.

I DONT care about backward capability.

BigRational can be found in the root src/core package

#### 0.2 Define Core Exception Hierarchy

```java
abstract class MathEngineException extends RuntimeException {
    Token token;           // Location where error occurred
    String sourceCode;     // Full source code for context

    abstract String formatMessage();
}

class LexerException extends MathEngineException { }
class ParseException extends MathEngineException { }
class TypeError extends MathEngineException { }
class EvaluationException extends MathEngineException { }
class StackOverflowException extends MathEngineException { }
class UndefinedVariableException extends EvaluationException { }
class ArityException extends EvaluationException { }
class DomainException extends EvaluationException { }
```

#### 0.3 Create Test Infrastructure

- Test runner that can execute tests from `GRAMMAR_TESTS.md`
- JSON parser for machine-readable test format
- Assertion framework for comparing results
- Test result reporting (pass/fail counts, failures list)

---

## Phase 1: Lexer (Tokenization)

### Goal

Convert input string into a stream of classified tokens, handling all ambiguities correctly.

### Deliverables

- Complete `Lexer` class
- All `TokenType` enumerations
- Implicit multiplication insertion
- Token classification (keywords, units, functions)

### Implementation Steps

#### 1.1 Define TokenType Enum

I dont think this is a good idea as it hardcodes all the operators

#### 1.2 Define Token Class

#### 1.3 Implement Single-Character Token Scanning

Handle: `(`, `)`, `{`, `}`, `[`, `]`, `,`, `;`, `:`

#### 1.4 Implement Number Scanning

**Critical: Handle decimal vs range ambiguity**

#### 1.5 Implement Multi-Character Operator Scanning

Handle:

- `==`, `!=`, `<=`, `>=`
- `&&`, `||`
- `!!`, `..`
- `:=`, `->`

Use lookahead: if current char matches, check next char before deciding.

#### 1.6 Implement Identifier Scanning

#### 1.7 Implement String Literal Scanning

Handle both `"..."` and `'...'`

- Track opening quote type
- Consume until matching closing quote
- Handle escapes (optional: `\"`, `\'`, `\\`, `\n`, etc.)

#### 1.8 Implement Token Classification (Pass 2)

Keywords: `for`, `in`, `if`, `step`, `and`, `or`, `not`, `xor`, `true`, `false`, `in`, `to`, `as`, `of`, `mod`

BUT these need to be dynamic, not hardcoded!

#### 1.9 Implement Implicit Multiplication Insertion

Insert virtual `MULTIPLY` tokens where needed:

**Note:** Do NOT insert multiplication before function calls. At lexer level, we can't distinguish functions from variables, so we
err on the side of caution.

### Testing Milestones

Run lexical tests from `GRAMMAR_TESTS.md` (Section 1):

- Integer literals: `0`, `42`, `-17`
- Decimal literals: `3.14`, `-0.5`
- Scientific notation: `1e3`, `2.5E-2`
- Predefined constants: `pi`, `euler`, `true`, `false`

---

## Phase 2: Parser (AST Construction)

### Goal

Build an Abstract Syntax Tree (AST) from token stream using recursive descent parsing.

### Deliverables

- Complete `Parser` class
- All node types (`NodeBinary`, `NodeUnary`, `NodeCall`, etc.)
- Precedence handling (via precedence climbing or hand-written chain)
- Error reporting with position information

### Implementation Steps

#### 2.1 Define Node Hierarchy

#### 2.2 Implement Parser Skeleton

#### 2.3 Implement Precedence Chain

Follow the grammar from `GRAMMAR.md` exactly. Choose ONE approach:

**Option A: Hand-Written Precedence Chain** (Recommended - clearer, easier to debug)

**Option B: Precedence Climbing** (More flexible, requires precedence table)

See `GRAMMAR_IMPLEMENTATION.md` Phase 9 for precedence climbing details. Only use this if you need dynamic operator precedence.

#### 2.4 Implement Vector/Comprehension Parsing

#### 2.5 Implement Matrix Parsing

#### 2.6 Implement Slice Argument Parsing

### Testing Milestones

Run precedence and associativity tests from `GRAMMAR_TESTS.md` (Sections 2-3):

- Basic arithmetic precedence: `2 + 3 * 4` → AST with correct structure
- Power associativity: `2^3^2` → Right-associative AST
- Complex mixed: `2 + 3 * 4^2` → Correct nesting

---

## Phase 3: Basic Evaluator (Arithmetic)

### Goal

Implement evaluation of numeric expressions with correct type handling.

### Deliverables

- `Evaluator` class
- `EvaluationContext` class
- Numeric arithmetic (doubles and rationals)
- Variable storage and retrieval

### Implementation Steps

#### 3.1 Implement EvaluationContext

#### 3.2 Initialize Predefined Constants

#### 3.3 Implement Evaluator Skeleton

#### 3.4 Implement Binary Operator Evaluation

#### 3.5 Implement Numeric Arithmetic

#### 3.6 Implement Unary Operator Evaluation

#### 3.7 Implement Comparison Operators

#### 3.8 Implement Boolean Operators

#### 3.9 Implement Assignment Evaluation

### Testing Milestones

Run type system tests from `GRAMMAR_TESTS.md` (Section 4):

- Rational arithmetic: `1 + 2` → `3` (rational)
- Rational division: `7 / 3` → `7/3` (exact fraction)
- Mixed arithmetic: `2 + 3.0` → `5.0` (double)
- Boolean arithmetic: `true + 1` → `2`

---

## Phase 4: Type System (Coercion & Promotion)

### Goal

Handle all type coercions correctly, including percent, boolean conversions.

### Deliverables

- `NodePercent` class
- Type coercion rules
- Broadcasting rules (basic, for scalars)

### Implementation Steps

#### 4.1 Implement NodePercent

#### 4.2 Implement "of" Operator

#### 4.3 Handle Boolean Coercion

Update numeric operations to handle booleans:

### Testing Milestones

Run percent and boolean tests from `GRAMMAR_TESTS.md`:

- `50%` → `0.5`
- `100% of 200` → `200`
- `true + 1` → `2`

---

## Phase 5: Data Structures (Vectors, Matrices, Ranges)

### Goal

Implement vector/matrix arithmetic, broadcasting, and range iteration.

### Deliverables

- Vector arithmetic (element-wise)
- Vector broadcasting
- Matrix arithmetic
- Range evaluation (lazy or eager)
- Subscript/slice evaluation

### Implementation Steps

#### 5.1 Implement Vector Arithmetic

#### 5.2 Implement Scalar-Vector Broadcasting

#### 5.3 Implement Matrix Arithmetic

#### 5.4 Implement Range Evaluation

#### 5.5 Implement Subscript Evaluation

### Testing Milestones

Run vector/matrix/range tests from `GRAMMAR_TESTS.md` (Sections 5-8):

- Vector arithmetic: `{1,2} + {3,4}` → `{4,6}`
- Broadcasting: `{1,2,3} * 2` → `{2,4,6}`
- Matrix multiplication: `[1,2;3,4] @ [5,6;7,8]` → `[19,22;43,50]`
- Range: `1..5` → `{1,2,3,4,5}`
- Subscript: `{10,20,30}[1]` → `20`

---

## Phase 6: Functions (Definitions & Calls)

### Goal

Implement function definitions, calls, recursion, and scope management.

### Deliverables

- Function storage in context
- Function call evaluation
- Parameter binding
- Recursion depth tracking

### Implementation Steps

#### 6.1 Define FunctionDefinition Class

#### 6.2 Implement Function Definition Evaluation

#### 6.3 Implement Function Call Evaluation

#### 6.4 Implement Built-in Functions

#### 6.5 Implement Vector Functions

#### 6.6 Implement Recursion Depth Tracking

### Testing Milestones

Run function tests from `GRAMMAR_TESTS.md` (Section 9):

- Simple function: `square(x) := x^2; square(5)` → `25`
- Multi-param: `add(a,b) := a + b; add(3,4)` → `7`
- Recursive: `fib(n) := if(n <= 1, n, fib(n-1) + fib(n-2)); fib(5)` → `5`
- Scope: `x := 5; f(x) := x^2; f(3); x` → `x` still equals `5`

---

## Phase 7: Advanced Features (Lambdas, Comprehensions)

### Goal

Implement lambda functions and list comprehensions.

### Deliverables

- Lambda evaluation
- Comprehension evaluation
- Iterator-based comprehension (lazy for ranges)

### Implementation Steps

#### 7.1 Implement Lambda Evaluation

#### 7.2 Implement Comprehension Evaluation

#### 7.3 Implement Range Iterator (Lazy)

### Testing Milestones

Run lambda and comprehension tests from `GRAMMAR_TESTS.md` (Sections 10-11):

- Lambda: `f := x -> x^2; f(5)` → `25`
- Comprehension: `{x^2 for x in 1..5}` → `{1,4,9,16,25}`
- With condition: `{x for x in 1..10 if x mod 2 == 0}` → `{2,4,6,8,10}`

### Dependencies

- Phase 6 complete

---

## Phase 8: Error Handling & Recovery

### Goal

Implement comprehensive error reporting and parser recovery.

### Deliverables

- Error reporter with source context
- Parser synchronization
- Stack traces for runtime errors

### Implementation Steps

#### 8.1 Implement Error Reporter

```java
class ErrorReporter {
    void report(MathEngineException error) {
        System.err.println(formatError(error));
    }

    String formatError(MathEngineException error) {
        StringBuilder sb = new StringBuilder();

        // Error message
        sb.append(error.formatMessage()).append("\n\n");

        // Source context
        if (error.sourceCode != null && error.token != null) {
            String[] lines = error.sourceCode.split("\n");
            int lineNum = error.token.line;

            if (lineNum > 0 && lineNum <= lines.length) {
                // Show line with error
                sb.append(String.format("%4d | %s\n", lineNum, lines[lineNum - 1]));

                // Show caret pointing to error position
                sb.append("     | ");
                sb.append(" ".repeat(Math.max(0, error.token.column - 1)));
                sb.append("^\n");
            }
        }

        // Stack trace for runtime errors
        if (error instanceof StackOverflowException) {
            sb.append("\n").append(((StackOverflowException) error).stackTrace);
        }

        return sb.toString();
    }
}
```

#### 8.2 Implement Parser Synchronization

Parser Error Recovery

**Strategy: Synchronization Points**

When parser encounters error, skip tokens until reaching a synchronization point:

- Statement boundaries (`;`, newline)
- Structural tokens (`}`, `]`, `)`)
- Keywords (`for`, `if`, `in`)
-

```java
// In Parser:

void synchronize() {
    advance();  // Skip the bad token

    while (!isAtEnd()) {
        // Stop at statement boundaries
        if (previous().type == SEMICOLON) return;
        if (previous().type == NEWLINE) return;

        // Stop at structural boundaries
        switch (peek().type) {
            case RBRACE:
            case RBRACKET:
            case RPAREN:
            case KEYWORD:
                return;
        }

        advance();
    }
}

// Wrap parseExpression with error recovery:
Node parseExpression() {
    try {
        return parseAssignment();
    } catch (ParseException e) {
        errorReporter.report(e);
        synchronize();
        return new NodeError();  // Placeholder node
    }
}
```

#### 8.3 Implement Call Stack Tracking

```java
class CallStack {
    Deque<CallFrame> frames = new ArrayDeque<>();

    void push(String functionName, List<NodeConstant> args) {
        frames.push(new CallFrame(functionName, args));

        if (frames.size() > MAX_CALL_STACK) {
            throw new StackOverflowException(buildStackTrace());
        }
    }

    void pop() {
        if (!frames.isEmpty()) {
            frames.pop();
        }
    }

    String buildStackTrace() {
        StringBuilder sb = new StringBuilder("Call stack:\n");
        for (CallFrame frame : frames) {
            sb.append("  at ").append(frame.functionName)
              .append("(").append(formatArgs(frame.args)).append(")\n");
        }
        return sb.toString();
    }

    String formatArgs(List<NodeConstant> args) {
        return args.stream()
                   .map(Object::toString)
                   .collect(Collectors.joining(", "));
    }
}

class CallFrame {
    String functionName;
    List<NodeConstant> args;
    int line, column;  // Source location

    CallFrame(String functionName, List<NodeConstant> args) {
        this.functionName = functionName;
        this.args = new ArrayList<>(args);
    }
}

// Update FunctionDefinition.call() to track call stack:
NodeConstant call(List<NodeConstant> args, Evaluator evaluator) {
    evaluator.callStack.push(name, args);

    try {
        // ... existing logic ...
    } finally {
        evaluator.callStack.pop();
    }
}
```

### Testing Milestones

Run error tests from `GRAMMAR_TESTS.md` (Section 15):

- Syntax errors: `2 +` → Error with position
- Undefined variables: `x` → Error message
- Type errors: `det({1,2,3})` → Error message
- Stack overflow: `f(x) := g(x); g(x) := f(x); f(1)` → Stack trace

**Success Criteria:** 100% of error tests pass (all errors must be caught)

### Dependencies

- All previous phases complete

---

## Phase 9: Optimization & Polish

### Goal

Optimize performance and add quality-of-life improvements.

### Deliverables

- Node pooling
- Configuration system

### Implementation Steps

#### 9.2 Implement Node Pooling

Should be lazy

```java
class NodePool {
    private static final NodeRational[] SMALL_INTS = new NodeRational[256];

    static {
        for (int i = 0; i < 256; i++) {
            SMALL_INTS[i] = new NodeRational(i, 1);
        }
    }

    static NodeRational getRational(long numerator, long denominator) {
        if (denominator == 1 && numerator >= 0 && numerator < 256) {
            return SMALL_INTS[(int) numerator];
        }
        return new NodeRational(numerator, denominator);
    }

    static final NodeBoolean TRUE = new NodeBoolean(true);
    static final NodeBoolean FALSE = new NodeBoolean(false);

    static NodeBoolean getBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }
}

// Use throughout codebase:
return NodePool.getRational(5, 1);  // Instead of new NodeRational(5, 1)
return NodePool.getBoolean(true);   // Instead of new NodeBoolean(true)
```

## Troubleshooting Common Issues

### Evaluator Issues

**Problem:** Recursion stack overflow

- **Symptom:** `fib(30)` crashes
- **Solution:** Implement recursion depth tracking (see Phase 6.6)
