package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.core.BigRational;
import uk.co.ryanharrison.mathengine.parser.ast.Node;
import uk.co.ryanharrison.mathengine.parser.ast.NodeBoolean;
import uk.co.ryanharrison.mathengine.parser.ast.NodeConstant;
import uk.co.ryanharrison.mathengine.parser.ast.NodeRational;
import uk.co.ryanharrison.mathengine.parser.ast.NodeVector;
import uk.co.ryanharrison.mathengine.parser.evaluator.DomainException;
import uk.co.ryanharrison.mathengine.parser.function.ArgTypes;
import uk.co.ryanharrison.mathengine.parser.function.FunctionBuilder;
import uk.co.ryanharrison.mathengine.parser.function.FunctionContext;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.special.Primes;

import java.math.BigInteger;
import java.util.ArrayList;
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
            .describedAs("Returns true if n is a prime number")
            .withParams("n")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> new NodeBoolean(Primes.isPrime(n)));

    /**
     * Find the next prime after n
     */
    public static final MathFunction NEXTPRIME = FunctionBuilder
            .named("nextprime")
            .describedAs("Returns the smallest prime number greater than n")
            .withParams("n")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> new NodeRational(Primes.nextPrime(n)));

    /**
     * Find the previous prime before n
     */
    public static final MathFunction PREVPRIME = FunctionBuilder
            .named("prevprime")
            .describedAs("Returns the largest prime number less than n")
            .withParams("n")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt())
            .implementedBy((n, ctx) -> new NodeRational(Primes.previousPrime(n)));

    /**
     * Prime factorization of a number
     */
    public static final MathFunction FACTORS = FunctionBuilder
            .named("factors")
            .alias("primefactors")
            .describedAs("Returns all prime factors of n as a vector with repetition (e.g. factors(12) = {2, 2, 3})")
            .withParams("n")
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
            .describedAs("Returns the unique prime factors of n as a vector, without repetition (e.g. distinctfactors(12) = {2, 3})")
            .withParams("n")
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
            .describedAs("Returns the number of positive divisors of n")
            .withParams("n")
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
            .describedAs("Returns the sum of all positive divisors of n")
            .withParams("n")
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
            .describedAs("Returns (base^exp) mod modulus using fast modular exponentiation")
            .withParams("base", "exp", "modulus")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((base, exp, mod, ctx) -> {
                if (mod <= 0) throw new DomainException("modpow: modulus must be positive");
                if (exp < 0) throw new DomainException("modpow: exponent must be non-negative");
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
            .describedAs("Returns true if a and b share no common factors (gcd = 1)")
            .withParams("a", "b")
            .inCategory(NUMBER_THEORY)
            .takingTyped(ArgTypes.longInt(), ArgTypes.longInt())
            .implementedBy((a, b, ctx) -> new NodeBoolean(gcd(a, b) == 1));

    /**
     * Integer square root, exact for integers of any size
     */
    public static final MathFunction ISQRT = FunctionBuilder
            .named("isqrt")
            .describedAs("Returns the largest integer whose square does not exceed n")
            .withParams("n")
            .inCategory(NUMBER_THEORY)
            .takingUnary()
            .implementedBy((n, ctx) -> {
                BigInteger value = ctx.requireBigInteger(n);
                if (value.signum() < 0) {
                    throw ctx.error("requires non-negative value, got: " + value);
                }
                return new NodeRational(BigRational.of(value.sqrt()));
            });

    /**
     * Digits of an integer, most significant first
     */
    public static final MathFunction DIGITS = FunctionBuilder
            .named("digits")
            .describedAs("Returns the digits of integer n as a vector, most significant first, in base 10 or the given base")
            .withParams("n")
            .withParams("n", "base")
            .inCategory(NUMBER_THEORY)
            .takingBetween(1, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) -> new NodeVector(digitsOf(args, ctx).stream()
                    .map(digit -> new NodeRational(digit))
                    .toArray(Node[]::new)));

    /**
     * Sum of the digits of an integer
     */
    public static final MathFunction DIGITSUM = FunctionBuilder
            .named("digitsum")
            .describedAs("Returns the sum of the digits of integer n, in base 10 or the given base")
            .withParams("n")
            .withParams("n", "base")
            .inCategory(NUMBER_THEORY)
            .takingBetween(1, 2)
            .noBroadcasting()
            .implementedByAggregate((args, ctx) ->
                    new NodeRational(digitsOf(args, ctx).stream().mapToLong(Long::longValue).sum()));

    /**
     * The digits of {@code args[0]} in the base {@code args[1]}, or 10. The sign is ignored.
     */
    private static List<Long> digitsOf(List<NodeConstant> args, FunctionContext ctx) {
        BigInteger value = ctx.requireBigInteger(args.getFirst()).abs();
        int base = args.size() > 1 ? ctx.requireInteger(args.get(1)) : 10;
        if (base < 2) {
            throw ctx.error("base must be at least 2, got: " + base);
        }
        BigInteger radix = BigInteger.valueOf(base);
        var digits = new ArrayList<Long>();
        do {
            BigInteger[] quotientAndRemainder = value.divideAndRemainder(radix);
            digits.addFirst(quotientAndRemainder[1].longValue());
            value = quotientAndRemainder[0];
        } while (value.signum() > 0);
        return digits;
    }

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
        return List.of(ISPRIME, NEXTPRIME, PREVPRIME, FACTORS, DISTINCTFACTORS, DIVISORCOUNT, DIVISORSUM, MODPOW, ISCOPRIME,
                ISQRT, DIGITS, DIGITSUM);
    }
}
