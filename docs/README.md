# Math Engine Documentation

**For:** AI Agents, Developers, and Contributors

This directory contains comprehensive documentation for the Math Engine project, with a focus on helping AI agents understand and
work with the codebase.

---

## Documentation Structure

### parser Package (Modern Expression Engine)

**Location:** `parser/`

The parser package is a complete rewrite with improved architecture, extensibility, and exact arithmetic support.

**Core Documentation:**

- **[OVERVIEW.md](parser/OVERVIEW.md)** - Start here! High-level architecture and component overview
- **[LEXER.md](parser/LEXER.md)** - Multi-pass tokenization pipeline in detail
- **[PARSER.md](parser/PARSER.md)** - Parsing strategy, precedence chain, and AST construction
- **[EVALUATOR.md](parser/EVALUATOR.md)** - Evaluation system, handlers, and context management
- **[NODES.md](parser/NODES.md)** - Complete AST node hierarchy and type system
- **[OPERATORS.md](parser/OPERATORS.md)** - Operator registration, dispatch, and implementation
- **[FUNCTIONS.md](parser/FUNCTIONS.md)** - Function system, types, and registration
- **[REGISTRIES.md](parser/REGISTRIES.md)** - Registry system for extensibility
- **[TESTING.md](parser/TESTING.md)** - Testing infrastructure and strategies

### Grammar Specifications

**Grammar Language:**

- **[GRAMMAR.md](GRAMMAR.md)** - Formal EBNF grammar specification
- **[GRAMMAR_TESTS.md](GRAMMAR_TESTS.md)** - Comprehensive test cases for grammar features
- **[GRAMMAR_IMPLEMENTATION_PLAN.md](GRAMMAR_IMPLEMENTATION_PLAN.md)** - Step-by-step implementation guide

**Design Documents:**

- **[BROADCASTING_ARCHITECTURE.md](BROADCASTING_ARCHITECTURE.md)** - Vector/matrix broadcasting system
- **[REFACTORING.md](../REFACTORING.md)** - Code quality standards and refactoring patterns

---

## Quick Start for AI Agents

### Understanding the Architecture

**Read in this order:**

1. **[parser/OVERVIEW.md](parser/OVERVIEW.md)** - Get the big picture
2. **[GRAMMAR.md](GRAMMAR.md)** - Understand the language
3. **[parser/LEXER.md](parser/LEXER.md)** - Tokenization process
4. **[parser/PARSER.md](parser/PARSER.md)** - AST construction
5. **[parser/EVALUATOR.md](parser/EVALUATOR.md)** - Evaluation process

### Working with Specific Features

**Operators:**

- Read **[parser/OPERATORS.md](parser/OPERATORS.md)** for operator system
- Read **[parser/NODES.md](parser/NODES.md)** for node types
- See **[BROADCASTING_ARCHITECTURE.md](BROADCASTING_ARCHITECTURE.md)** for vector operations

**Functions:**

- Read **[parser/FUNCTIONS.md](parser/FUNCTIONS.md)** for function types
- Read **[parser/REGISTRIES.md](parser/REGISTRIES.md)** for registration

**Testing:**

- Read **[parser/TESTING.md](parser/TESTING.md)** for test strategies
- Read **[GRAMMAR_TESTS.md](GRAMMAR_TESTS.md)** for expected behavior

### Extending the System

**Adding new features:**

1. Read the relevant component doc (operators, functions, etc.)
2. Follow the "Implementing Custom X" section
3. Register in appropriate registry or config
4. Add tests following patterns in TESTING.md

---

## Documentation Philosophy

### For AI Agents

This documentation is designed to help AI agents:

- **Understand** the codebase structure quickly
- **Navigate** between related components
- **Extend** the system with new features
- **Debug** issues with clear mental models
- **Test** changes systematically

### Key Principles

**1. Complete Context**

- Each doc stands alone with minimal forward references
- Links provided for deep dives

**2. Practical Examples**

- Every concept has code examples
- Common patterns explicitly documented

**3. AI-Friendly Structure**

- Clear headings and hierarchy
- Consistent formatting
- Explicit relationships between components

**4. Implementation Focus**

- Not just "what" but "how" and "why"
- Common pitfalls highlighted
- Extension points clearly marked

---

## Documentation Maintenance

### When to Update

**Add documentation when:**

- New major component added
- Architecture changes significantly
- Common pattern emerges

**Update documentation when:**

- APIs change
- New capabilities added to existing components
- Bugs reveal missing edge cases

### Documentation Style Guide

**Structure:**

```markdown
# Title

**Purpose:** One-line summary

---

## Section

Clear explanation with:
- Code examples
- Diagrams (if helpful)
- Common pitfalls
- Related documentation links
```

**Code Examples:**

- Use real, runnable code
- Include expected output
- Show common patterns, not exotic cases

**Links:**

- Relative links within docs
- Use `[Text](path/to/file.md)` format
- Include section anchors for subsections

---

## Related Resources

### Project Root Docs

- **[../CLAUDE.md](../CLAUDE.md)** - Instructions for Claude Code (AI agent)
- **[../README.md](../README.md)** - Project README

### External Resources

- Java 25 Documentation
- JUnit 5 User Guide
- AssertJ Documentation

---

## Document Index

### By Component

**Entry Points:**

- MathEngine: [parser/OVERVIEW.md](parser/OVERVIEW.md)
- Lexer: [parser/LEXER.md](parser/LEXER.md)
- Parser: [parser/PARSER.md](parser/PARSER.md)
- Evaluator: [parser/EVALUATOR.md](parser/EVALUATOR.md)

**Core Systems:**

- Nodes: [parser/NODES.md](parser/NODES.md)
- Operators: [parser/OPERATORS.md](parser/OPERATORS.md)
- Functions: [parser/FUNCTIONS.md](parser/FUNCTIONS.md)
- Registries: [parser/REGISTRIES.md](parser/REGISTRIES.md)

**Development:**

- Testing: [parser/TESTING.md](parser/TESTING.md)
- Grammar: [GRAMMAR.md](GRAMMAR.md)
- Implementation: [GRAMMAR_IMPLEMENTATION_PLAN.md](GRAMMAR_IMPLEMENTATION_PLAN.md)

### By Task

**"I want to add a new operator"**
→ Read [parser/OPERATORS.md](parser/OPERATORS.md)

**"I want to add a new function"**
→ Read [parser/FUNCTIONS.md](parser/FUNCTIONS.md)

**"I want to understand how parsing works"**
→ Read [parser/PARSER.md](parser/PARSER.md)

**"I want to add a new node type"**
→ Read [parser/NODES.md](parser/NODES.md)

**"I want to understand the grammar"**
→ Read [GRAMMAR.md](GRAMMAR.md)

**"I want to write tests"**
→ Read [parser/TESTING.md](parser/TESTING.md)

---

## Feedback

Found something unclear or missing?

- File an issue at https://github.com/anthropics/claude-code/issues
- Documentation improvements welcome!

---

**Last Updated:** January 2026
**Documentation Version:** 2.0
**Target Audience:** AI Agents and Developers
