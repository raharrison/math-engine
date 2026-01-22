# Documentation Guide for AI Agents

**Last Updated:** January 2026
**Purpose:** Quick reference guide for AI agents working with the math-engine parser package

---

## Quick Start (3 Steps)

### 1. Understand the Big Picture

Read **[parser/OVERVIEW.md](parser/OVERVIEW.md)** first (15 min read)

**You'll learn:**

- 3-stage pipeline: Lexer → Parser → Evaluator
- Component responsibilities and relationships
- Type system hierarchy
- Configuration options

### 2. Follow the Data Flow

Read these in order to understand how expressions are processed:

1. **[parser/LEXER.md](parser/LEXER.md)** - Text → Tokens (4-pass pipeline)
2. **[parser/PARSER.md](parser/PARSER.md)** - Tokens → AST (precedence chain)
3. **[parser/EVALUATOR.md](parser/EVALUATOR.md)** - AST → Result (evaluation handlers)

### 3. Deep Dive as Needed

Component-specific documentation:

- **[parser/NODES.md](parser/NODES.md)** - AST node types and hierarchy
- **[parser/OPERATORS.md](parser/OPERATORS.md)** - Operator system
- **[parser/FUNCTIONS.md](parser/FUNCTIONS.md)** - Function system
- **[parser/REGISTRIES.md](parser/REGISTRIES.md)** - Registry system
- **[parser/TESTING.md](parser/TESTING.md)** - Testing strategies

---

## Common Tasks

### "I need to add a new operator"

1. Read **[parser/OPERATORS.md](parser/OPERATORS.md)** § "Implementing a Custom Operator"
2. Create operator class implementing `BinaryOperator` or `UnaryOperator`
3. Register in `StandardBinaryOperators` or `StandardUnaryOperators`
4. Add tests following patterns in **[parser/TESTING.md](parser/TESTING.md)**

**Key files to modify:**

- `operator/binary/YourOperator.java` or `operator/unary/YourOperator.java`
- `operator/binary/StandardBinaryOperators.java` or `operator/unary/StandardUnaryOperators.java`

### "I need to add a new function"

1. Read **[parser/FUNCTIONS.md](parser/FUNCTIONS.md)** § "Implementing Custom Functions"
2. Implement `MathFunction` interface (or extend helper class)
3. Add to `StandardFunctions.all()`
4. Add tests

**Key files to modify:**

- `function/YourFunction.java`
- `function/StandardFunctions.java`

### "I need to understand how parsing works"

1. Read **[parser/PARSER.md](parser/PARSER.md)** - Complete parsing documentation
2. Focus on:
    - Precedence chain (section on operator precedence)
    - Special constructs (functions, lambdas, comprehensions)
    - Error handling

**Key files:**

- `parser/Parser.java` - Main coordinator
- `parser/PrecedenceParser.java` - Precedence chain
- `parser/CollectionParser.java` - Vectors, matrices, comprehensions

### "I need to add a new node type"

1. Read **[parser/NODES.md](parser/NODES.md)** - Complete node hierarchy
2. Decide: NodeConstant (evaluated value) or NodeExpression (unevaluated)
3. Add node class extending appropriate base
4. Add evaluation logic in `Evaluator.evaluate()`
5. Add parsing logic in `Parser` or `PrecedenceParser`

**Key files to modify:**

- `parser/nodes/YourNode.java`
- `evaluator/Evaluator.java`
- `parser/PrecedenceParser.java` or `parser/CollectionParser.java`

### "I need to understand the grammar"

1. Read **[GRAMMAR.md](GRAMMAR.md)** - Formal EBNF specification
2. Read **[GRAMMAR_TESTS.md](GRAMMAR_TESTS.md)** - Examples and expected behavior
3. Cross-reference with implementation docs

### "I need to write tests"

1. Read **[parser/TESTING.md](parser/TESTING.md)** - Testing strategies
2. Follow patterns for your component:
    - Lexer: Test tokenization pipeline
    - Parser: Test AST structure
    - Evaluator: Test computed results
    - Operators/Functions: Test behavior with various inputs

**Key test files:**

- `src/test/java/uk/co/ryanharrison/mathengine/parser/MathEngineTest.java`
- `src/test/java/uk/co/ryanharrison/mathengine/parser/lexer/LexerTest.java`
- `src/test/java/uk/co/ryanharrison/mathengine/parser/parser/ParserTest.java`

---

## Component Relationships

```
┌─────────────┐
│ MathEngine  │ ← Entry point
└──────┬──────┘
       │
   Creates and coordinates:
       │
       ├─► Lexer ──────┐
       │                │
       ├─► Parser ──────┤
       │                │
       ├─► Evaluator ───┤
       │                │
       └─► Registries ──┘

Lexer uses:
- UnitRegistry (unit names)
- ConstantRegistry (constant names)
- KeywordRegistry (keywords)

Parser uses:
- TokenStream (navigation)
- PrecedenceParser (operator precedence)
- CollectionParser (vectors, matrices, comprehensions)
- UnitRegistry (unit validation)

Evaluator uses:
- EvaluationContext (variables, functions)
- OperatorExecutor (operator dispatch)
- FunctionExecutor (function dispatch)
- Handlers (variables, subscripts, functions, comprehensions)
```
