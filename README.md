# Math Engine

A comprehensive Java mathematical library featuring an advanced expression parser with support for custom functions, vectors, matrices, symbolic differentiation, numerical integration, equation solving, unit conversions, probability distributions, and more.

## Features

- **Advanced Expression Parser** - Natural mathematical syntax with exact rational arithmetic, lambdas, and comprehensions
- **Rich Data Types** - Vectors, matrices, ranges, strings, functions
- **Functional Programming** - Lambda functions, list comprehensions, higher-order functions (map, filter, reduce)
- **Unit Conversions** - Physical units with automatic conversion integrated into parser
- **Mathematical Analysis** - Symbolic differentiation, numerical integration, root finding
- **Probability Distributions** - Normal, Beta, Binomial, Exponential, F, Logistic, Student T
- **Linear Algebra** - Vector and matrix operations with QR/LU decomposition
- **GUI Applications** - Interactive expression evaluator, unit converter, and function grapher

## Requirements

- **Java 25+**
- **Gradle** (wrapper included)

## Build and Run

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run GUI applications
./gradlew run  # Interactive expression evaluator (MainFrame)
```

For complete grammar documentation, see [docs/GRAMMAR.md](docs/GRAMMAR.md).

---

## Quick Start - Expression Parser (MathEngine)

The `MathEngine` class is the main entry point for parsing and evaluating mathematical expressions. It provides a modern, immutable API with comprehensive configuration options.

### Basic Usage

```java
// Create default engine
MathEngine engine = MathEngine.create();

// Simple arithmetic
NodeConstant result = engine.evaluate("2 + 3 * 4");
System.out.println(result);  // 14

// Exact rational arithmetic (fractions preserved)
result = engine.evaluate("1/3 + 1/6");
System.out.println(result);  // 1/2

// Power and factorial
result = engine.evaluate("2^10 + 5!");
System.out.println(result);  // 1144
```

### Variables and Functions

```java
MathEngine engine = MathEngine.create();

// Define variables
engine.evaluate("x := 10");
engine.evaluate("y := 20");
NodeConstant result = engine.evaluate("x + y");  // 30

// Define custom functions
engine.evaluate("square(n) := n^2");
result = engine.evaluate("square(5)");  // 25

// Multi-parameter functions
engine.evaluate("add(a, b) := a + b");
result = engine.evaluate("add(3, 7)");  // 10
```

### Vectors and Matrices

```java
MathEngine engine = MathEngine.create();

// Vector operations
engine.evaluate("v1 := {1, 2, 3}");
engine.evaluate("v2 := {4, 5, 6}");
NodeConstant result = engine.evaluate("v1 + v2");  // {5, 7, 9}

// Matrix operations
engine.evaluate("m1 := [[1, 2], [3, 4]]");
engine.evaluate("m2 := [[5, 6], [7, 8]]");
result = engine.evaluate("m1 + m2");  // [[6, 8], [10, 12]]

// Matrix multiplication (@ operator)
result = engine.evaluate("m1 @ m2");  // [[19, 22], [43, 50]]

// Subscript access
result = engine.evaluate("v1[0]");    // 1
result = engine.evaluate("m1[0, 1]"); // 2
result = engine.evaluate("v1[1:3]");  // {2, 3} (slice)
```

### Lambdas and Higher-Order Functions

```java
MathEngine engine = MathEngine.create();

// Lambda functions
NodeConstant result = engine.evaluate("map(x -> x + 3, {1, 2, 3, 4, 5})");
// {4, 5, 6, 7, 8}

// Filter with lambda
result = engine.evaluate("filter(x -> x > 3, {1, 2, 3, 4, 5})");
// {4, 5}

// Reduce (fold)
result = engine.evaluate("reduce((a, b) -> a + b, {1, 2, 3, 4}, 10)");
// 10

// Inline lambda call
result = engine.evaluate("(x -> x * 2)(5)");
// 10
```

### List Comprehensions

```java
MathEngine engine = MathEngine.create();

// Basic comprehension
NodeConstant result = engine.evaluate("{x^2 for x in 1..5}");
// {1, 4, 9, 16, 25}

// With filter condition
result = engine.evaluate("{x for x in 1..20 if x mod 2 == 0}");
// {2, 4, 6, 8, 10, 12, 14, 16, 18, 20}

// Nested comprehensions
result = engine.evaluate("{x*y for x in 1..3 for y in 1..3}");
// {1, 2, 3, 2, 4, 6, 3, 6, 9}
```

### Ranges

```java
MathEngine engine = MathEngine.create();

// Simple range
NodeConstant result = engine.evaluate("1..5");
// {1, 2, 3, 4, 5}

// Range with step
result = engine.evaluate("0..1 step 0.2");
// {0.0, 0.2, 0.4, 0.6, 0.8, 1.0}

// Descending range
result = engine.evaluate("10..1 step -2");
// {10, 8, 6, 4, 2}

// Sum of range
result = engine.evaluate("sum(1..100)");
// 5050
```

### Recursion and Advanced Functions

```java
MathEngine engine = MathEngine.create();

// Recursive factorial
engine.evaluate("fact(n) := if(n <= 1, 1, n * fact(n-1))");
NodeConstant result = engine.evaluate("fact(5)");  // 120

// Recursive fibonacci
engine.evaluate("fib(n) := if(n <= 1, n, fib(n-1) + fib(n-2))");
result = engine.evaluate("fib(10)");  // 55

// Mutual recursion
engine.evaluate("even(n) := if(n == 0, true, odd(n-1))");
engine.evaluate("odd(n) := if(n == 0, false, even(n-1))");
result = engine.evaluate("even(4)");  // true (as 1.0)

// Higher-order functions (functions returning functions)
engine.evaluate("makeAdder(n) := (x -> n + x)");
engine.evaluate("add10 := makeAdder(10)");
result = engine.evaluate("add10(5)");  // 15

// Function composition
engine.evaluate("compose(f, g) := (x -> f(g(x)))");
engine.evaluate("double(x) := x * 2");
engine.evaluate("square(x) := x^2");
engine.evaluate("doubleSquare := compose(double, square)");
result = engine.evaluate("doubleSquare(5)");  // 50 (2 * 5^2)
```

### Built-in Functions

```java
MathEngine engine = MathEngine.create();

// Trigonometry (angle unit aware)
NodeConstant result = engine.evaluate("sin(pi/2)");  // 1.0
result = engine.evaluate("cos(0)");                   // 1.0

// Logarithms and exponentials
result = engine.evaluate("ln(e)");      // 1.0
result = engine.evaluate("log10(100)"); // 2.0
result = engine.evaluate("exp(1)");     // e

// Rounding
result = engine.evaluate("floor(3.7)");     // 3
result = engine.evaluate("ceil(3.2)");      // 4
result = engine.evaluate("round(3.5)");     // 4
result = engine.evaluate("roundn(3.14159, 2)");  // 3.14

// Vector operations
result = engine.evaluate("sum({1, 2, 3, 4})");     // 10
result = engine.evaluate("mean({1, 2, 3, 4})");    // 2.5
result = engine.evaluate("max({5, 2, 8, 1})");     // 8
result = engine.evaluate("sort({3, 1, 4, 2})");    // {1, 2, 3, 4}
result = engine.evaluate("reverse({1, 2, 3})");    // {3, 2, 1}
```

### Unit Conversions

```java
MathEngine engine = MathEngine.create();

// Length conversions
NodeConstant result = engine.evaluate("100 meters in feet");
// 328.084 feet

// Temperature
result = engine.evaluate("32 fahrenheit to celsius");
// 0 celsius

// Speed
result = engine.evaluate("60 mph in kph");
// 96.56064 kph

// Combined expressions
result = engine.evaluate("(50 + 50) meters in centimeters");
// 10000 cm
```

### Configuration

```java
// Configure angle units
MathEngineConfig config = MathEngineConfig.builder()
    .angleUnit(AngleUnit.DEGREES)
    .build();
MathEngine engine = MathEngine.create(config);

NodeConstant result = engine.evaluate("sin(90)");  // 1.0 (degrees mode)

// Configure decimal places for display
config = MathEngineConfig.builder()
    .decimalPlaces(4)
    .build();
engine = MathEngine.create(config);

// Disable specific features
config = MathEngineConfig.builder()
    .vectorsEnabled(false)
    .matricesEnabled(false)
    .build();
engine = MathEngine.create(config);
```

---

## Function Class - Single Variable Equations

The `Function` class provides a simple interface for working with single-variable equations. It's used by differential, integral, solvers, and plotting packages.

### Basic Usage

```java
import uk.co.ryanharrison.mathengine.core.Function;
import uk.co.ryanharrison.mathengine.parser.AngleUnit;

// Create function (defaults to variable 'x' and radians)
Function f = new Function("x^2 + 8*x + 12");

// Evaluate at a point
double result = f.evaluateAt(3.5);
System.out.println(result);  // 268.25

// Specify variable and angle unit
Function g = new Function("t^2 - 4*t + 3", "t", AngleUnit.DEGREES);
result = g.evaluateAt(2.0);  // -1.0
```

The `Function` class internally uses the parser and caches the compiled expression tree for performance.

---

## Differential - Symbolic and Numeric Differentiation

### Symbolic Differentiation

Returns exact derivative as a new `Function`:

```java
import uk.co.ryanharrison.mathengine.differential.symbolic.Differentiator;
import uk.co.ryanharrison.mathengine.core.Function;

Function f = new Function("x^2 + 8*x + 12");
Differentiator diff = new Differentiator();

// Get derivative function
Function derivative = diff.differentiate(f, true);
System.out.println(derivative);  // f(x) = 2*x+8

// Evaluate derivative at point
double result = derivative.evaluateAt(3.5);
System.out.println(result);  // 15.0 (exact)
```

### Numeric Differentiation

Estimates derivative at a specific point:

```java
import uk.co.ryanharrison.mathengine.differential.DividedDifferenceMethod;
import uk.co.ryanharrison.mathengine.differential.DifferencesDirection;
import uk.co.ryanharrison.mathengine.core.Function;

Function f = new Function("x^2 + 8*x + 12");

// Central differences (most accurate for smooth functions)
DividedDifferenceMethod method = DividedDifferenceMethod.builder()
    .targetFunction(f)
    .targetPoint(3.5)
    .direction(DifferencesDirection.Central)
    .build();
double derivative = method.deriveFirst();
System.out.println(derivative);  // ~15.0 (with small error)

// Can also use ExtendedCentralDifferenceMethod or RichardsonExtrapolationMethod
// for higher accuracy
```

---

## Integral - Numerical Integration

Estimates definite integrals using various numerical methods:

```java
import uk.co.ryanharrison.mathengine.integral.TrapeziumIntegrator;
import uk.co.ryanharrison.mathengine.integral.SimpsonIntegrator;
import uk.co.ryanharrison.mathengine.core.Function;

Function f = new Function("x^2 + 8*x + 12");

// Trapezium rule
TrapeziumIntegrator integrator = TrapeziumIntegrator.builder()
    .function(f)
    .lowerBound(0.5)
    .upperBound(5.0)
    .iterations(100)
    .build();
double result = integrator.integrate();
System.out.println(result);  // ~194.626 (exact: 194.625)

// Simpson's rule (generally more accurate)
SimpsonIntegrator simpson = SimpsonIntegrator.builder()
    .function(f)
    .lowerBound(0.5)
    .upperBound(5.0)
    .iterations(100)
    .build();
result = simpson.integrate();
// More accurate than trapezium for same iterations
```

Other available methods:
- `RectangularIntegrator` - Simple rectangle approximation

---

## Solvers - Root Finding Algorithms

Find roots (zeros) of functions numerically:

```java
import uk.co.ryanharrison.mathengine.solvers.BrentSolver;
import uk.co.ryanharrison.mathengine.core.Function;
import java.util.List;

Function f = new Function("x^2 + 8*x + 12");

// Find single root
BrentSolver solver = BrentSolver.builder()
    .targetFunction(f)
    .lowerBound(-10)
    .upperBound(-5)
    .iterations(25)
    .build();
double root = solver.solve();
System.out.println(root);  // ~-6.0

// Find all roots in range
List<Double> roots = solver.solveAll(-8, 2);
System.out.println(roots);  // [-6.0, -2.0]
```

Other available solvers:
- `BisectionSolver` - Simple but reliable bisection method
- `NewtonRaphsonSolver` - Fast convergence (requires derivative)
- `NewtonBisectionSolver` - Hybrid approach

Configure convergence criteria:
```java
import uk.co.ryanharrison.mathengine.solvers.ConvergenceCriteria;

BrentSolver preciseSolver = BrentSolver.builder()
    .targetFunction(f)
    .lowerBound(-10)
    .upperBound(-5)
    .convergenceCriteria(ConvergenceCriteria.WithinTolerance)
    .tolerance(1e-10)
    .build();
```

---

## Distributions - Probability Distributions

Immutable implementations of probability distributions with factory methods and builders.

### Normal Distribution

```java
import uk.co.ryanharrison.mathengine.distributions.NormalDistribution;

// Standard normal (mean=0, stddev=1)
NormalDistribution standard = NormalDistribution.standard();
double density = standard.density(0.0);     // 0.3989 (peak at mean)
double cumulative = standard.cumulative(1.0); // 0.8413

// Custom distribution using factory method
NormalDistribution custom = NormalDistribution.of(15, 2.6);
density = custom.density(15.7);             // 0.1480
cumulative = custom.cumulative(15.7);       // 0.6061

// Using builder
NormalDistribution dist = NormalDistribution.builder()
    .mean(100)
    .standardDeviation(15)
    .build();

// Inverse CDF (quantile function)
double quantile = dist.inverseCumulative(0.95);  // ~124.67
```

### Other Distributions

```java
import uk.co.ryanharrison.mathengine.distributions.*;

// Beta distribution
BetaDistribution beta = BetaDistribution.of(2.0, 5.0);
double density = beta.density(0.3);

// Exponential distribution
ExponentialDistribution exp = ExponentialDistribution.of(1.5);
double cumulative = exp.cumulative(2.0);

// F distribution
FDistribution f = FDistribution.of(5, 10);  // degrees of freedom
double pValue = f.cumulative(2.5);

// Student's T distribution
StudentTDistribution t = StudentTDistribution.of(20);  // df
double criticalValue = t.inverseCumulative(0.975);

// Binomial distribution (discrete)
BinomialDistribution binomial = BinomialDistribution.of(10, 0.5);
double probability = binomial.density(5);  // P(X = 5)
cumulative = binomial.cumulative(7);       // P(X <= 7)

// Logistic distribution
LogisticDistribution logistic = LogisticDistribution.of(0.0, 1.0);
density = logistic.density(0.0);
```

All distributions provide:
- `density(x)` - Probability density/mass function
- `cumulative(x)` - Cumulative distribution function
- `inverseCumulative(p)` - Quantile function (continuous only)
- `getMean()`, `getVariance()`, `getStandardDeviation()` - Distribution properties

---

## Linear Algebra - Vectors and Matrices

### Vectors

```java
import uk.co.ryanharrison.mathengine.linearalgebra.Vector;

// Create vectors
Vector v1 = Vector.of(1, 2, 3, 4);
Vector v2 = Vector.parse("{5, 6, 7, 8}");  // Parse from string

// Basic operations
Vector sum = v1.add(v2);           // {6, 8, 10, 12}
Vector diff = v1.subtract(v2);     // {-4, -4, -4, -4}
Vector scaled = v1.multiply(2);    // {2, 4, 6, 8}

// Vector operations
double dotProduct = v1.dotProduct(v2);  // 70
double magnitude = v1.getNorm();        // 5.477
Vector normalized = v1.getUnitVector();

// Cross product (3D vectors)
Vector a = Vector.of(1, 0, 0);
Vector b = Vector.of(0, 1, 0);
Vector cross = a.crossProduct(b);  // {0, 0, 1}
```

### Matrices

```java
import uk.co.ryanharrison.mathengine.linearalgebra.Matrix;

// Create matrices
Matrix m1 = Matrix.of(new double[][]{{1, 2}, {3, 4}});
Matrix m2 = Matrix.ofSize(2, 2);  // 2x2 zero matrix

// Basic operations
Matrix sum = m1.add(m2);
Matrix product = m1.multiply(m2);
Matrix scaled = m1.multiply(2.0);

// Matrix properties
double determinant = m1.determinant();  // -2
Matrix transpose = m1.transpose();
Matrix inverse = m1.inverse();

// Verify: A * A^-1 = I
Matrix identity = m1.multiply(inverse);

// Solve linear system: A*X = B
Matrix A = Matrix.of(new double[][]{{3, 7}, {4, 12}});
Matrix B = Matrix.of(new double[][]{{-4, 5}, {8, 1}});
Matrix X = A.solve(B);

// Advanced decompositions
import uk.co.ryanharrison.mathengine.linearalgebra.QRDecomposition;
import uk.co.ryanharrison.mathengine.linearalgebra.LUDecomposition;

QRDecomposition qr = new QRDecomposition(A);
Matrix Q = qr.getQ();
Matrix R = qr.getR();

LUDecomposition lu = new LUDecomposition(A);
Matrix L = lu.getL();
Matrix U = lu.getU();
```

---

## Unit Conversion

Flexible unit conversion with string-based matching:

```java
import uk.co.ryanharrison.mathengine.unitconversion.ConversionEngine;

ConversionEngine engine = ConversionEngine.loadDefaults();

// Basic conversion
String result = engine.convertToFormattedString(12, "mph", "kph", 2);
// "12.0 miles per hour = 19.31 kilometres per hour"

// Flexible unit aliases
result = engine.convertToFormattedString(12, "miles per hour", "kilometer per hour", 2);
// Same result

// Get numeric value only
double value = engine.convert(100, "meters", "feet").result().toDouble();  // 328.084

// Currency conversion (requires internet)
engine.updateCurrencies();
result = engine.convertToFormattedString(100, "usd", "eur", 2);

// Timezone conversion
engine.updateTimeZones();
result = engine.convertToFormattedString(14, "london", "new york", 1);
// "14.0 London = 9.0 New York" (2pm in London = 9am in NYC)
```

The engine supports hundreds of units including:
- Length: meters, feet, miles, kilometers, etc.
- Mass: grams, kilograms, pounds, ounces, etc.
- Temperature: celsius, fahrenheit, kelvin
- Speed: mph, kph, m/s, knots, etc.
- Area, volume, pressure, energy, power, and more

Unit definitions are stored in `unit.xml` and can be extended.

---

## Special Functions

Mathematical special functions and number theory utilities:

```java
import uk.co.ryanharrison.mathengine.special.*;

// Gamma function
double gamma = Gamma.gamma(5.0);          // 24.0 (= 4!)
double logGamma = Gamma.logGamma(100.0);  // Avoid overflow

// Beta function
double beta = Beta.beta(2.0, 3.0);  // 0.0833

// Error function (used in normal distribution)
double erf = Erf.erf(1.0);           // 0.8427
double erfc = Erf.erfc(1.0);         // 0.1573 (= 1 - erf)
double erfInv = Erf.erfInv(0.5);     // ~0.4769

// Prime numbers
boolean isPrime = Primes.isPrime(97);        // true
long nextPrime = Primes.nextPrime(97);       // 101
List<Long> factors = Primes.primeFactors(84);  // [2, 2, 3, 7]
```

---

## Regression Models

Fit mathematical models to data:

```java
import uk.co.ryanharrison.mathengine.regression.LinearRegressionModel;

// Sample data
double[] x = {1, 2, 3, 4, 5};
double[] y = {2.1, 3.9, 6.2, 8.1, 9.9};

// Fit linear model: y = a + bx
LinearRegressionModel model = LinearRegressionModel.of(x, y);

double intercept = model.getIntercept();  // a ≈ 0.08
double slope = model.getSlope();          // b ≈ 1.98

// Use fitted model for predictions
double predicted = model.evaluateAt(6.0);  // ~12.0

// Get coefficients
double[] coeffs = model.getCoefficients();  // {intercept, slope}
```

---

## Plotting and GUI

### GUI Applications

**MainFrame** - Interactive expression evaluator with history:

Features:
- Expression input with history (arrow keys)
- Live variable/function tracking
- Enhanced error messages with source context
- Syntax reference panel

**Converter** - Unit conversion tool:
- Dropdown selection for units
- String-based conversion (e.g., "12 mph in kph")
- Currency and timezone support

**Grapher** - Function plotter:
- Interactive pan and zoom
- Plot any `Function` object
- Real-time rendering

---

## Utility Classes

### MathUtils

Extended mathematical functions:

```java
import uk.co.ryanharrison.mathengine.util.MathUtils;

// Hyperbolic functions
double sinh = MathUtils.sinh(1.5);
double cosh = MathUtils.cosh(1.5);
double tanh = MathUtils.tanh(1.5);

// Inverse hyperbolic
double asinh = MathUtils.asinh(1.0);
double acosh = MathUtils.acosh(2.0);
double atanh = MathUtils.atanh(0.5);

// Combinatorics
long factorial = MathUtils.factorial(10);        // 3628800
double choose = MathUtils.combination(10, 3);    // 120
double permute = MathUtils.permutation(10, 3);   // 720

// Number utilities
int gcd = MathUtils.gcd(48, 18);                 // 6
int lcm = MathUtils.lcm(12, 18);                 // 36
double round = MathUtils.round(3.14159, 2);      // 3.14
```

### StatUtils

Statistical functions:

```java
import uk.co.ryanharrison.mathengine.util.StatUtils;

double[] data = {2.1, 3.5, 2.8, 4.2, 3.9, 3.1, 2.9};

double mean = StatUtils.mean(data);                  // 3.21
double median = StatUtils.median(data);              // 3.1
double stddev = StatUtils.standardDeviation(data);   // 0.73
double variance = StatUtils.variance(data);          // 0.54

double min = StatUtils.min(data);                    // 2.1
double max = StatUtils.max(data);                    // 4.2

// Percentiles
double q1 = StatUtils.percentile(data, 0.25);  // First quartile
double q3 = StatUtils.percentile(data, 0.75);  // Third quartile

// Distribution properties
double skewness = StatUtils.skewness(data);
double kurtosis = StatUtils.kurtosis(data);
```

### BigRational

Exact fraction arithmetic (used internally by parser):

```java
import uk.co.ryanharrison.mathengine.core.BigRational;

BigRational a = new BigRational(1, 3);  // 1/3
BigRational b = new BigRational(1, 6);  // 1/6

BigRational sum = a.add(b);             // 1/2 (exact)
BigRational product = a.multiply(b);    // 1/18

// Automatic simplification
BigRational c = new BigRational(6, 8);  // Stored as 3/4

// Convert to decimal when needed
double decimal = c.toDouble();          // 0.75
```

---

## Advanced Topics

### Custom Built-in Functions

Add your own functions to the parser:

```java
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.*;

// Define custom function
MathFunction myFunc = new MathFunction() {
    @Override
    public String getName() {
        return "double";
    }

    @Override
    public int getArity() {
        return 1;  // Number of parameters
    }

    @Override
    public NodeConstant execute(NodeConstant[] args) {
        double value = args[0].getTransformer().toNodeNumber().doubleValue();
        return new NodeDouble(value * 2);
    }
};

// Register with engine
MathEngineConfig config = MathEngineConfig.builder()
    .additionalFunction(myFunc)
    .build();
MathEngine engine = MathEngine.create(config);

NodeConstant result = engine.evaluate("double(21)");  // 42
```

### Error Handling

All parser exceptions extend `MathEngineException` and provide detailed error messages:

```java
import uk.co.ryanharrison.mathengine.parser.MathEngineException;
import uk.co.ryanharrison.mathengine.parser.parser.ParseException;
import uk.co.ryanharrison.mathengine.parser.evaluator.UndefinedVariableException;

MathEngine engine = MathEngine.create();

try {
    engine.evaluate("2 + * 3");  // Syntax error
} catch (ParseException e) {
    System.out.println(e.formatMessage());
    // Parse error at line 1, column 5: Unexpected token '*'
    //    1 | 2 + * 3
    //      |      ^
}

try {
    engine.evaluate("unknownVar + 5");
} catch (UndefinedVariableException e) {
    System.out.println(e.formatMessage());
    System.out.println(e.getVariableName());  // "unknownVar"
}
```

---

## Documentation

- **[Grammar Reference](docs/GRAMMAR.md)** - Complete parser grammar and built-in function reference (150+ functions)
- **[Documentation Guide](docs/DOCUMENTATION_GUIDE.md)** - Javadoc standards and code quality guidelines
- **Source Code Javadoc** - Comprehensive inline documentation

## License

See LICENSE file for details.

## Contributing

Contributions welcome. Please follow the code quality standards documented in `docs/DOCUMENTATION_GUIDE.md`.
