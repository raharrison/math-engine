# Testing Infrastructure

**Purpose:** the declarative spec suite that guards the parser against regressions

---

## Overview

The language the engine evaluates is specified by JSON files under
`src/test/resources/engine`. Each file holds a list of cases, and each case is one
expression together with the value or the exception it must produce. The files are the
contract; the Java classes under `parser/spec` only load and run them.

Writing the contract as data rather than as code buys three things:

- a case is added by editing one file, with no recompilation and no boilerplate
- a case reads as documentation, because it states an expression and its meaning
- the whole surface can be checked mechanically, which is what `SpecIntegrityTest` and
  `SpecCoverageTest` do

```
src/test/resources/engine/
├── language/     syntax and parsing: literals, precedence, associativity, grouping,
│                 sequences, assignment, implicit multiplication, comprehensions,
│                 lambdas, subscripts, references
├── values/       one file per value type: number, rational, percent, unit, string,
│                 boolean, vector, matrix, range, function, constant
├── operators/    one file per operator, covering every operand type it accepts
├── functions/    one file per MathFunction.Category, plus aliases.json
├── semantics/    scoping, closures, recursion, laziness, broadcasting, exactness,
│                 units, type coercion, name resolution, operator/function agreement
├── config/       one file per configuration flag
├── errors/       one file per exception type
└── integration/  several features at once, and real formulae
```

One home per concern, and no separate place for fixed bugs: a defect is a statement about
an operator, a function or a value type, so its case goes in that thing's file.

---

## Where a new case goes

| The case is about                                                | Put it in                                           |
|------------------------------------------------------------------|-----------------------------------------------------|
| how something parses                                             | `language/`                                         |
| what a literal of some type is, or its degenerate forms          | `values/`                                           |
| one operator meeting one operand type                            | `operators/<operator>.json`                         |
| one built-in function                                            | `functions/<category>.json`                         |
| how evaluation behaves: scope, laziness, exactness, broadcasting | `semantics/`                                        |
| a configuration flag changing the answer                         | `config/<flag>.json`                                |
| an exception                                                     | `errors/<exception>.json`                           |
| a bug that was fixed                                             | the file that owns the behaviour, by the rows above |

An expression may be asserted in exactly one place. `SpecIntegrityTest` fails if two
cases share an input under the same configuration, because two copies of an assertion
drift apart.

---

## The file format

```json
{
    "category": "Operator: + (addition)",
    "description": "Addition across every operand type it accepts.",
    "defaultConfig": {
        "angleUnit": "DEGREES"
    },
    "tests": [
        ...
    ]
}
```

`category` and `description` are required. `category` must be unique across the suite,
so a failure names the file to open. `defaultConfig` is optional and applies to every
case that declares no config of its own.

### A value case

```json
{
    "id": "add_rational_integers",
    "input": "1 + 2",
    "expected": 3,
    "expectedType": "NodeRational",
    "notes": "Integer addition stays exact"
}
```

`id`, `input`, `notes`, `expected` and `expectedType` are all required. The id must be
unique across the whole suite, so it can be quoted in a bug report and found.

### An error case

```json
{
    "id": "fractional_count",
    "input": "\"ab\" * 2.5",
    "expectError": true,
    "expectedErrorType": "TypeError",
    "expectedErrorMessage": "fractional",
    "notes": "A fractional count is refused rather than rounded down to 'abab'"
}
```

The exception must be **exactly** the class named, not a subclass, so an exception type
cannot be quietly widened. `expectedErrorMessage` is optional and asserts a substring.

### How a value is written

| Result type                  | JSON form                         | Example               |
|------------------------------|-----------------------------------|-----------------------|
| `NodeRational`               | a number, or a string `"num/den"` | `3`, `"1/3"`          |
| `NodeDouble`                 | a number                          | `0.5`                 |
| `NodeBoolean`                | a boolean                         | `true`                |
| `NodeString`                 | a string                          | `"hello"`             |
| `NodePercent`                | a string ending in `%`            | `"50%"`               |
| `NodeUnit`                   | a string `"value unit"`           | `"328.084 feet"`      |
| `NodeVector`                 | an array                          | `[1, 2, 3]`           |
| `NodeMatrix`                 | an array of arrays                | `[[1, 2], [3, 4]]`    |
| `NodeRange`                  | a string in range syntax          | `"1..10"`             |
| `NodeFunction`, `NodeLambda` | the formatted form                | `"<function:square>"` |

A rational written as `"num/den"` is compared exactly rather than through a double,
which is the only way a case can pin down that a result really did stay exact. Numbers
are compared with an absolute tolerance of `1e-7`, or whatever `tolerance` says.

`NodeNumber` is the one loose spelling of `expectedType`, for the rare case that
genuinely does not care which numeric type it gets.

### Optional fields

| Field             | Meaning                                                                                 |
|-------------------|-----------------------------------------------------------------------------------------|
| `evaluationOrder` | an equivalent expression with explicit parentheses; the two must parse to the same tree |
| `config`          | engine configuration overrides, replacing the suite default outright                    |
| `tolerance`       | absolute tolerance for the numeric comparison                                           |
| `skip`            | a reason to skip; a blank or absent value means run                                     |

`evaluationOrder` is how precedence and associativity are pinned down. It is not a
comment: the runner parses the annotation, renders both trees in fully parenthesised
form and compares them.

```json
{
    "id": "prec_power_before_modulo",
    "input": "2 ^ 3 mod 5",
    "expected": 3,
    "expectedType": "NodeRational",
    "evaluationOrder": "(2 ^ 3) mod 5",
    "notes": "Exponentiation binds tighter than modulo"
}
```

### Multi-step cases

Each case runs against a fresh engine, so nothing leaks between cases. A scenario that
needs several steps puts them in one input, separated by `;`, and the value of the last
statement is the result.

```json
{
    "id": "parameter_leaves_global_alone",
    "input": "f(x) := x * 2; x := 5; f(3); x",
    "expected": 5,
    "expectedType": "NodeRational",
    "notes": "A parameter shadows a global for the length of the call and then gives it back"
}
```

---

## The Java side

Everything lives in `src/test/java/uk/co/ryanharrison/mathengine/parser/spec`.

| Class                                 | Responsibility                                                         |
|---------------------------------------|------------------------------------------------------------------------|
| `SpecCase`, `SpecSuite`, `SpecConfig` | the file format, as records                                            |
| `SpecLoader`                          | finds and parses every file on the classpath, rejecting unknown fields |
| `SpecValueAssertions`                 | compares a result against the JSON encoding above                      |
| `SpecExceptions`                      | resolves the exception names a file may write                          |
| `SpecCaseRunner`                      | runs one case against a fresh engine                                   |
| `EngineSpecTest`                      | the JUnit entry point, one container per directory and file            |
| `SpecIntegrityTest`                   | the structural rules below                                             |
| `SpecCoverageTest`                    | fails when the engine grows a name no case exercises                   |

### Structural rules

`SpecIntegrityTest` exists because a fixture that asserts nothing still passes. It
enforces that:

- every file states a category and a description, and holds at least one case
- no two files claim the same category
- every case has an id, an input and a note, and ids are unique across the suite
- every error case names a resolvable exception, and does not also expect a value
- every value case states both a value and a real node type
- no expression is asserted twice under the same configuration

Unknown JSON properties are rejected at load time, so a misspelled field fails loudly
instead of silently disabling an assertion.

### Coverage rules

`SpecCoverageTest` fails when:

- a function name or alias is never called by any case
- a built-in constant is never referenced
- an operator symbol is never used
- an exception type is never named

An alias nobody calls is an alias that can break, or collide with a new function,
unnoticed. `functions/aliases.json` closes that gap in one file, by asserting that each
alternate spelling agrees with its primary name.

---

## Running the tests

```bash
# the whole suite
./gradlew test

# only the spec files
./gradlew test --tests "*EngineSpecTest*"

# only the structural and coverage checks
./gradlew test --tests "*Spec*Test"

# a readable pass/fail summary, including the names of any failures
./gradlew test testSummary
```

A failure reads as `operators > add > add_types_unit_string`, naming the directory, the
file and the case.

---

## Adding a defect to the suite

When a bug is found and fixed:

1. Add the case to the file that owns the behaviour, by the table above.
2. Put the wrong answer in the `notes`, not only the right one.
3. Add the neighbouring cases that must keep working, in their own files.

```json
{
    "id": "modulo_keeps_unit",
    "input": "(10 meters) mod 3",
    "expected": "1.0 meter",
    "expectedType": "NodeUnit",
    "notes": "Ten metres modulo three is one metre, not a bare one: a scalar leaves the label in place"
}
```

A defect worth a paragraph belongs in the file's `description`.

---

## Conventions

**Ids** describe the case, not its position: `min_km_vs_meters`, not `min_003`. They are
unique across the suite, so a bug report can quote one.

**Notes** say why the case exists, in a sentence. "Simple addition" adds nothing; "Ten
percent added to a hundred is a hundred and ten" says what the reader needs.

**Errors are worth pinning down.** Most combinations of an operator and a value type are
errors, and the error is part of the contract. The operator files carry a full matrix of
every operand type against every other, errors included.

**Assert the type as well as the value.** Most of the defects this suite was built for
were type losses, not value losses: an exact rational quietly becoming a double, or a
quantity quietly losing its unit. A case that checks only the value would have missed
every one of them.
