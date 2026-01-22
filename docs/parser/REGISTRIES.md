# Registry System

**Purpose:** Provide lookup services for lexer classification and evaluator features

---

## Overview

Registries are simple lookup tables that enable extensibility without modifying core code:

```
Lexer needs: "Is 'sin' a function?" → FunctionNames
Lexer needs: "Is 'meters' a unit?" → UnitRegistry
Lexer needs: "Is 'pi' a constant?" → ConstantRegistry
Lexer needs: "Is 'and' a keyword?" → KeywordRegistry
```
---

## UnitRegistry

**File:** `registry/UnitRegistry.java`

**Purpose:** Define physical units and conversion factors

**Interface:**

```java
class UnitRegistry {
    void register(UnitDefinition unit);
    boolean isUnit(String name);
    UnitDefinition get(String name);
    Set<String> getAllNames();

    static UnitRegistry standard();
}
```

**UnitDefinition:**

```java
class UnitDefinition {
    String name;
    String category;        // "length", "mass", "temperature"
    double conversionFactor;  // To base unit
    UnitDefinition baseUnit;  // Reference unit for category
}
```

**Example:**

```java
UnitRegistry registry = new UnitRegistry();

// Length units (meters as base)
UnitDefinition meters = UnitDefinition.base("meters", "length");
UnitDefinition feet = UnitDefinition.of("feet", "length", 0.3048, meters);
UnitDefinition inches = UnitDefinition.of("inches", "length", 0.0254, meters);

registry.register(meters);
registry.register(feet);
registry.register(inches);

// Usage
boolean is = registry.isUnit("meters");  // true
UnitDefinition def = registry.get("feet");
```

**Conversion:**

```java
100 meters in feet
// 1. Lexer recognizes "meters" and "feet" as units
// 2. Parser creates NodeUnitConversion
// 3. Evaluator looks up units in registry and converts
```

**Standard Units:**

```java
UnitRegistry registry = UnitRegistry.standard();
// Includes: meters, feet, inches, miles, kilometers
//           grams, kilograms, pounds, ounces
//           celsius, fahrenheit, kelvin
//           seconds, minutes, hours, days
```

---

## ConstantRegistry

**File:** `registry/ConstantRegistry.java`

**Purpose:** Define predefined mathematical constants

**Interface:**

```java
class ConstantRegistry {
    void register(ConstantDefinition constant);
    boolean isConstant(String name);
    ConstantDefinition get(String name);
    List<ConstantDefinition> getDefinitions();

    static ConstantRegistry standard();
}
```

**ConstantDefinition:**

```java
class ConstantDefinition {
    String name;              // Primary name
    NodeConstant value;       // Constant value
    List<String> aliases;     // Alternative names

    static ConstantDefinition of(String name, NodeConstant value);
    static ConstantDefinition of(String name, NodeConstant value, String... aliases);
}
```

**Example:**

```java
ConstantRegistry registry = new ConstantRegistry();

// Mathematical constants
registry.register(ConstantDefinition.of(
    "pi",
    new NodeDouble(Math.PI),
    "PI", "π"
));

registry.register(ConstantDefinition.of(
    "e",
    new NodeDouble(Math.E),
    "euler", "E"
));

// Boolean constants
registry.register(ConstantDefinition.of("true", new NodeBoolean(true)));
registry.register(ConstantDefinition.of("false", new NodeBoolean(false)));
```

**Standard Constants:**

```java
ConstantRegistry registry = ConstantRegistry.standard();
// Includes:
//   - pi, e (Euler's number)
//   - goldenratio, sqrt2, sqrt3
//   - true, false
//   - infinity, nan
//   - zero, one, two, ..., ten
//   - hundred, thousand, million, billion
```

**Aliases:**

Allows multiple names for same constant:

```java
pi = 3.14159...
PI = 3.14159...  (alias)
π = 3.14159...   (alias)
```

**Initialization:**

Constants are loaded into `EvaluationContext` at startup:

```java
for (ConstantDefinition def : registry.getDefinitions()) {
    context.define(def.name(), def.value());
    for (String alias : def.aliases()) {
        context.define(alias, def.value());
    }
}
```

---

## KeywordRegistry

**File:** `registry/KeywordRegistry.java`

**Purpose:** Define reserved keywords that can't be used as identifiers

**Interface:**

```java
class KeywordRegistry {
    void register(String keyword);
    boolean isKeyword(String word);
    Set<String> getAllKeywords();

    static KeywordRegistry standard();
}
```

**Example:**

```java
KeywordRegistry registry = new KeywordRegistry();
registry.register("and");
registry.register("or");
registry.register("not");
registry.register("for");
registry.register("in");
registry.register("if");

boolean is = registry.isKeyword("and");  // true
boolean is = registry.isKeyword("x");    // false
```

**Standard Keywords:**

```java
KeywordRegistry registry = KeywordRegistry.standard();
// Includes:
//   Logical: and, or, not, xor
//   Control: for, in, if, step
//   Operators: of, mod, to, as
```

**Why Separate from Lexer?**

Makes keywords configurable. User could:

- Add custom keywords for DSL extensions
- Remove keywords to allow as variable names (risky!)

---

## Classification Priority

**In TokenClassifier, registries are checked in order:**

1. **Keywords** - Highest priority
    - `and`, `or`, `not`, etc.
    - Can't be shadowed

2. **Units**
    - `meters`, `feet`, etc.
    - Can shadow functions if both exist

3. **Functions**
    - `sin`, `cos`, `max`, etc.
    - Can be shadowed by units

4. **Constants**
    - `pi`, `e`, `true`, `false`
    - Can be shadowed by functions

5. **Identifiers** - Default
    - User variables
    - Anything not in above registries

**Example:**

If `m` is registered as both unit (meters) and function:

```
"m" → Classified as UNIT (units checked before functions)
```

---

## Custom Registries

### Adding Custom Units

```java
UnitRegistry units = UnitRegistry.standard();

// Add custom unit
UnitDefinition smoots = UnitDefinition.of(
    "smoots",
    "length",
    1.7018,  // meters per smoot
    units.get("meters")
);
units.register(smoots);

// Use in expressions
engine.evaluate("100 smoots in meters");  // 170.18 meters
```

### Adding Custom Constants

```java
ConstantRegistry constants = ConstantRegistry.standard();

// Add custom constant
constants.register(ConstantDefinition.of(
    "MY_CONSTANT",
    new NodeDouble(42.0),
    "myconst"  // alias
));

// Use in expressions
engine.evaluate("MY_CONSTANT * 2");  // 84.0
engine.evaluate("myconst * 2");      // 84.0 (via alias)
```

### Adding Custom Keywords

```java
KeywordRegistry keywords = KeywordRegistry.standard();

// Add custom keyword
keywords.register("unless");

// Now 'unless' can't be used as variable name
// You'd also need to add parser support for the keyword
```

---

## Testing Registries

### Unit Tests

```java
@Test
void constantRegistry() {
    ConstantRegistry registry = new ConstantRegistry();
    registry.register(ConstantDefinition.of(
        "MY_CONST",
        new NodeDouble(42.0)
    ));

    assertThat(registry.isConstant("MY_CONST")).isTrue();
    ConstantDefinition def = registry.get("MY_CONST");
    assertThat(def.value().doubleValue()).isEqualTo(42.0);
}
```

### Integration Tests

```java
@Test
void customUnitInExpression() {
    UnitRegistry units = UnitRegistry.standard();
    units.register(UnitDefinition.of("custom", "length", 2.0, units.get("meters")));

    MathEngine engine = MathEngine.builder()
        .unitRegistry(units)
        .build();

    NodeConstant result = engine.evaluate("10 custom in meters");
    assertThat(result.doubleValue()).isEqualTo(20.0);
}
```

---

## Performance Considerations

### Hash-Based Lookup

All registries use hash maps for O(1) lookup:

```java
private Map<String, UnitDefinition> units = new HashMap<>();
private Set<String> keywords = new HashSet<>();
```

### Case Sensitivity

All registries are case-sensitive:

```java
registry.isFunction("Sin")  // false
registry.isFunction("sin")  // true
```

If case-insensitive needed, normalize at registration and lookup:

```java
void register(String name) {
    String normalized = name.toLowerCase();
    map.put(normalized, ...);
}
```

### Prefix Matching (IdentifierSplitter)

For compound identifier splitting, longest-match-first:

```java
// If registry contains: "sin", "s", "in"
"sin2" → Check "sin" first (matches!) → ["sin", "2"]
       → Not "s" + "in" + "2"
```

---

## Related Documentation

- **[OVERVIEW.md](./OVERVIEW.md)** - High-level architecture
- **[LEXER.md](./LEXER.md)** - How registries are used in lexer
- **[FUNCTIONS.md](./FUNCTIONS.md)** - Function registration
- **[OPERATORS.md](./OPERATORS.md)** - Operator registration
