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

## Documentation Standards

### What Each Doc Contains

**OVERVIEW.md:**

- High-level architecture
- All component overview
- Type system
- Configuration
- Quick reference with file counts

**Component Docs (LEXER, PARSER, EVALUATOR, etc.):**

- Purpose and responsibility
- Architecture and design
- Key algorithms and patterns
- Code examples for common operations
- Common pitfalls for AI agents
- Testing strategies
- Performance considerations
- Extension points

**System Docs (OPERATORS, FUNCTIONS, etc.):**

- Interface definitions
- Implementation patterns
- Standard implementations
- Registration and dispatch
- Custom implementation guide
- Testing patterns

### Reading Strategies

**First-time readers:**

1. Start with OVERVIEW.md
2. Read data flow docs (LEXER → PARSER → EVALUATOR)
3. Dive into specific components as needed

**Experienced readers (returning to codebase):**

1. Refresh with OVERVIEW.md
2. Jump directly to relevant component doc
3. Use "Common Tasks" section above

**Debugging/investigating:**

1. Identify which component is involved (lexer/parser/evaluator)
2. Read that component's doc
3. Check "Common Pitfalls" section
4. Review related node/operator/function docs

---

## File Organization

```
docs/
├── README.md                          # Documentation hub (start here)
├── DOCUMENTATION_GUIDE.md             # This file (quick reference)
│
├── GRAMMAR.md                         # Language specification (EBNF)
├── GRAMMAR_TESTS.md                   # Test specifications
├── GRAMMAR_IMPLEMENTATION_PLAN.md     # Implementation guide
├── BROADCASTING_ARCHITECTURE.md       # Broadcasting system design
├── REFACTORING.md                     # Code quality standards
│
└── parser/                          # parser package documentation
    ├── OVERVIEW.md                   # ⭐ START HERE - Architecture overview
    │
    ├── LEXER.md                      # Tokenization (15KB, 4-pass pipeline)
    ├── PARSER.md                     # AST construction (46KB, precedence chain)
    ├── EVALUATOR.md                  # Evaluation (17KB, handlers)
    │
    ├── NODES.md                      # Node types (19KB, complete hierarchy)
    ├── OPERATORS.md                  # Operator system (17KB, registration/dispatch)
    ├── FUNCTIONS.md                  # Function system (18KB, types & registration)
    ├── REGISTRIES.md                 # Registry system (10KB, extensibility)
    └── TESTING.md                    # Testing strategies (13KB, patterns)
```

**Total:** ~170KB of documentation across 15 files

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
- FunctionRegistry (function names)
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

---

## Documentation Completeness

### Lexer ✅

- Token types: Complete
- Scanning: Complete
- Classification: Complete
- Implicit multiplication: Complete
- Edge cases: Documented

### Parser ✅

- Precedence chain: Complete
- All node types: Complete
- Special constructs: Complete
- Error handling: Complete

### Evaluator ✅

- All node evaluations: Complete
- Type coercion: Complete
- Broadcasting: Complete
- Handlers: Complete
- Context management: Complete

### Operators ✅

- Interface: Complete
- Registration: Complete
- Standard operators: Complete
- Broadcasting: Complete
- Custom operators: Complete

### Functions ✅

- All function types: Complete
- Registration: Complete
- Standard functions: Complete
- User-defined: Complete
- Lambdas: Complete

### Testing ✅

- Unit tests: Complete
- Integration tests: Complete
- Strategies: Complete
- Utilities: Complete

---

## Missing Documentation

**None for parser core functionality.**

**Future Enhancements (if needed):**

- Advanced configuration patterns
- Performance tuning guide
- Migration guide from parser (legacy) to parser
- Debugging guide with common error messages
- Architecture decision records (ADRs)

---

## Maintenance Checklist

When modifying the codebase, update docs if:

- [ ] New component added
- [ ] API changes
- [ ] New feature added
- [ ] Bug reveals edge case not documented
- [ ] Architecture changes
- [ ] Configuration options change

**Where to update:**

- Component-specific doc if single component affected
- OVERVIEW.md if architecture changes
- Multiple docs if cross-cutting change

---

## Documentation Philosophy

**Principle 1: AI-First**

- Written for AI agents to understand quickly
- Complete context in each doc
- Explicit relationships
- Common pitfalls highlighted

**Principle 2: Practical**

- Every concept has code examples
- Real patterns, not toy examples
- Extension points clearly marked

**Principle 3: Maintainable**

- Consistent structure across all docs
- Clear section hierarchy
- Cross-references for deep dives

**Principle 4: Complete**

- No "TODO" sections
- No forward references to non-existent docs
- Every public API documented

---

## Questions?

**Not finding what you need?**

1. Check **[README.md](README.md)** for document index
2. Use "By Task" index in README
3. Search all docs for keywords
4. Check component source files for inline comments

**Found an issue?**

File at: https://github.com/anthropics/claude-code/issues

---

## Version History

**v2.0 (January 2026)** - Initial comprehensive documentation

- 9 parser component docs
- 5 supporting docs
- 1 documentation hub
- 1 quick reference (this file)

---

**Happy coding! 🚀**

_This documentation is designed to help AI agents work efficiently with the math-engine codebase._
