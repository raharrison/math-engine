package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.Node;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeRational;
import uk.co.ryanharrison.mathengine.parser.parser.nodes.NodeVector;
import uk.co.ryanharrison.mathengine.special.Primes;

import java.util.List;

import static uk.co.ryanharrison.mathengine.parser.function.MathFunction.Category.NUMBER_THEORY;

/**
 * Collection of number theory functions (primes, factorization, etc.).
 */
public final class NumberTheoryFunctions {

    private NumberTheoryFunctions() {
    }

    /**
     * Check if a number is prime
     */
    public static final MathFunction ISPRIME = FunctionBuilder
            .named("isprime")
            .describedAs("Check if number is prime")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> new NodeBoolean(Primes.isPrime(n)));

    /**
     * Find the next prime after n
     */
    public static final MathFunction NEXTPRIME = FunctionBuilder
            .named("nextprime")
            .describedAs("Next prime number after n")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> new NodeRational(Primes.nextPrime(n)));

    /**
     * Find the previous prime before n
     */
    public static final MathFunction PREVPRIME = FunctionBuilder
            .named("prevprime")
            .describedAs("Previous prime number before n")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> new NodeRational(Primes.previousPrime(n)));

    /**
     * Prime factorization of a number
     */
    public static final MathFunction FACTORS = FunctionBuilder
            .named("factors")
            .alias("primefactors")
            .describedAs("Prime factorization of a number")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> {
                List<Long> factorList = Primes.primeFactors(n);
                return new NodeVector(factorList.stream().map(NodeRational::new).toArray(Node[]::new));
            });

    /**
     * Distinct prime factors of a number
     */
    public static final MathFunction DISTINCTFACTORS = FunctionBuilder
            .named("distinctfactors")
            .describedAs("Distinct prime factors of a number")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> {
                List<Long> factorList = Primes.distinctPrimeFactors(n);
                return new NodeVector(factorList.stream().map(NodeRational::new).toArray(Node[]::new));
            });

    /**
     * Count divisors of a number
     */
    public static final MathFunction DIVISORCOUNT = FunctionBuilder
            .named("divisorcount")
            .describedAs("Count of divisors of a number")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> {
                long absN = Math.abs(n);
                if (absN == 0) return new NodeRational(0);
                if (absN == 1) return new NodeRational(1);
                int count = 0;
                for (long i = 1; i * i <= absN; i++) {
                    if (absN % i == 0) {
                        count++;
                        if (i != absN / i) count++;
                    }
                }
                return new NodeRational(count);
            });

    /**
     * Sum of divisors of a number
     */
    public static final MathFunction DIVISORSUM = FunctionBuilder
            .named("divisorsum")
            .describedAs("Sum of divisors of a number")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> {
                long absN = Math.abs(n);
                if (absN == 0) return new NodeRational(0);
                long sum = 0;
                for (long i = 1; i * i <= absN; i++) {
                    if (absN % i == 0) {
                        sum += i;
                        if (i != absN / i) sum += absN / i;
                    }
                }
                return new NodeRational(sum);
            });

    /**
     * Modular exponentiation: base^exp mod m
     */
    public static final MathFunction MODPOW = FunctionBuilder
            .named("modpow")
            .describedAs("Modular exponentiation: base^exp mod m")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((base, exp, mod, ctx) -> {
                if (mod <= 0) throw new IllegalArgumentException("modpow: modulus must be positive");
                if (exp < 0) throw new IllegalArgumentException("modpow: exponent must be non-negative");
                long result = 1;
                long b = base % mod;
                long e = exp;
                while (e > 0) {
                    if ((e & 1) == 1) result = (result * b) % mod;
                    e >>= 1;
                    b = (b * b) % mod;
                }
                return new NodeRational(result);
            });

    /**
     * Check if two numbers are coprime (gcd = 1)
     */
    public static final MathFunction ISCOPRIME = FunctionBuilder
            .named("iscoprime")
            .describedAs("Check if two numbers are coprime")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((a, b, ctx) -> new NodeBoolean(gcd(a, b) == 1));

    private static long gcd(long a, long b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Gets all number theory functions.
     */
    public static List<MathFunction> all() {
        return List.of(ISPRIME, NEXTPRIME, PREVPRIME, FACTORS, DISTINCTFACTORS, DIVISORCOUNT, DIVISORSUM, MODPOW, ISCOPRIME);
    }
}
