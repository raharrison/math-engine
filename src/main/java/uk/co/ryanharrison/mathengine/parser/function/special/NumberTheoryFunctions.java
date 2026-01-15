package uk.co.ryanharrison.mathengine.parser.function.special;

import uk.co.ryanharrison.mathengine.parser.function.AggregateFunction;
import uk.co.ryanharrison.mathengine.parser.function.BinaryFunction;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.UnaryFunction;
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
    public static final MathFunction ISPRIME = UnaryFunction.of("isprime", "Check if number is prime", NUMBER_THEORY,
            (arg, ctx) -> new NodeBoolean(Primes.isPrime((long) ctx.toNumber(arg).doubleValue())));

    /**
     * Find the next prime after n
     */
    public static final MathFunction NEXTPRIME = UnaryFunction.of("nextprime", "Next prime number after n", NUMBER_THEORY,
            (arg, ctx) -> new NodeRational(Primes.nextPrime((long) ctx.toNumber(arg).doubleValue())));

    /**
     * Find the previous prime before n
     */
    public static final MathFunction PREVPRIME = UnaryFunction.of("prevprime", "Previous prime number before n", NUMBER_THEORY,
            (arg, ctx) -> new NodeRational(Primes.previousPrime((long) ctx.toNumber(arg).doubleValue())));

    /**
     * Prime factorization of a number
     */
    public static final MathFunction FACTORS = UnaryFunction.of("factors", "Prime factorization of a number", NUMBER_THEORY,
            List.of("primefactors"), (arg, ctx) -> {
                List<Long> factorList = Primes.primeFactors((long) ctx.toNumber(arg).doubleValue());
                return new NodeVector(factorList.stream().map(NodeRational::new).toArray(Node[]::new));
            });

    /**
     * Distinct prime factors of a number
     */
    public static final MathFunction DISTINCTFACTORS = UnaryFunction.of("distinctfactors", "Distinct prime factors of a number", NUMBER_THEORY,
            (arg, ctx) -> {
                List<Long> factorList = Primes.distinctPrimeFactors((long) ctx.toNumber(arg).doubleValue());
                return new NodeVector(factorList.stream().map(NodeRational::new).toArray(Node[]::new));
            });

    /**
     * Count divisors of a number
     */
    public static final MathFunction DIVISORCOUNT = UnaryFunction.of("divisorcount", "Count of divisors of a number", NUMBER_THEORY, (arg, ctx) -> {
        long n = Math.abs((long) ctx.toNumber(arg).doubleValue());
        if (n == 0) return new NodeRational(0);
        if (n == 1) return new NodeRational(1);
        int count = 0;
        for (long i = 1; i * i <= n; i++) {
            if (n % i == 0) {
                count++;
                if (i != n / i) count++;
            }
        }
        return new NodeRational(count);
    });

    /**
     * Sum of divisors of a number
     */
    public static final MathFunction DIVISORSUM = UnaryFunction.of("divisorsum", "Sum of divisors of a number", NUMBER_THEORY, (arg, ctx) -> {
        long n = Math.abs((long) ctx.toNumber(arg).doubleValue());
        if (n == 0) return new NodeRational(0);
        long sum = 0;
        for (long i = 1; i * i <= n; i++) {
            if (n % i == 0) {
                sum += i;
                if (i != n / i) sum += n / i;
            }
        }
        return new NodeRational(sum);
    });

    /**
     * Modular exponentiation: base^exp mod m
     */
    public static final MathFunction MODPOW = AggregateFunction.of("modpow", "Modular exponentiation: base^exp mod m", NUMBER_THEORY, 3, 3, (args, ctx) -> {
        long base = (long) ctx.toNumber(args.get(0)).doubleValue();
        long exp = (long) ctx.toNumber(args.get(1)).doubleValue();
        long mod = (long) ctx.toNumber(args.get(2)).doubleValue();
        if (mod <= 0) throw new IllegalArgumentException("modpow: modulus must be positive");
        if (exp < 0) throw new IllegalArgumentException("modpow: exponent must be non-negative");
        long result = 1;
        base = base % mod;
        while (exp > 0) {
            if ((exp & 1) == 1) result = (result * base) % mod;
            exp >>= 1;
            base = (base * base) % mod;
        }
        return new NodeRational(result);
    });

    /**
     * Check if two numbers are coprime (gcd = 1)
     */
    public static final MathFunction ISCOPRIME = BinaryFunction.of("iscoprime", "Check if two numbers are coprime", NUMBER_THEORY, (a, b, ctx) -> {
        long aVal = (long) ctx.toNumber(a).doubleValue();
        long bVal = (long) ctx.toNumber(b).doubleValue();
        return new NodeBoolean(gcd(aVal, bVal) == 1);
    });

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
