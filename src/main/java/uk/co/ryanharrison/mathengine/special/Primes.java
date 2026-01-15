package uk.co.ryanharrison.mathengine.special;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Utility class for working with prime numbers and prime factorization.
 * <p>
 * A prime number is a natural number greater than 1 that has no positive divisors
 * other than 1 and itself. The first few prime numbers are 2, 3, 5, 7, 11, 13, 17, 19, 23, 29...
 * </p>
 * <p>
 * This class provides methods for:
 * </p>
 * <ul>
 *     <li>Testing whether a number is prime</li>
 *     <li>Finding the next or previous prime number in the sequence</li>
 *     <li>Computing prime factorizations of numbers</li>
 *     <li>Finding distinct prime factors</li>
 * </ul>
 * <p>
 * All methods are static, and this class cannot be instantiated.
 * </p>
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Test if a number is prime
 * boolean isPrime = Primes.isPrime(17);  // true
 * boolean isNotPrime = Primes.isPrime(18);  // false
 *
 * // Find next and previous primes
 * long next = Primes.nextPrime(10);  // 11
 * long prev = Primes.previousPrime(10);  // 7
 *
 * // Prime factorization
 * List<Long> factors = Primes.primeFactors(60);  // [2, 2, 3, 5]
 * List<Long> distinct = Primes.distinctPrimeFactors(60);  // [2, 3, 5]
 * }</pre>
 *
 * @author Ryan Harrison
 */
public final class Primes {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Primes() {
        throw new AssertionError("Primes is a utility class and should not be instantiated");
    }

    /**
     * Computes the distinct (unique) prime factors of a number in ascending order.
     * <p>
     * Returns a list containing each prime factor exactly once, even if the prime
     * appears multiple times in the complete factorization. The factors are returned
     * in ascending order. For negative numbers, only the first occurrence of the
     * smallest prime factor is negative.
     * </p>
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * Primes.distinctPrimeFactors(60);   // [2, 3, 5]  (60 = 2^2 * 3 * 5)
     * Primes.distinctPrimeFactors(17);   // [17]       (17 is prime)
     * Primes.distinctPrimeFactors(1);    // []         (empty - no prime factors)
     * Primes.distinctPrimeFactors(0);    // []         (empty - no prime factors)
     * Primes.distinctPrimeFactors(-12);  // [-2, 3]    (only first 2 is negative)
     * }</pre>
     *
     * @param n the number whose distinct prime factors to compute (can be negative)
     * @return an unmodifiable list of the distinct prime factors of {@code n} in ascending order
     * @see #primeFactors(long)
     */
    public static List<Long> distinctPrimeFactors(long n) {
        // Generate all prime factors (with duplicates)
        List<Long> factors = primeFactors(n);

        // Use LinkedHashSet to remove duplicates while maintaining insertion order
        // For negative numbers, we need to handle the case where -p and p should be considered the same prime
        LinkedHashSet<Long> distinct = new LinkedHashSet<>();
        boolean negativeAdded = false;

        for (Long factor : factors) {
            long absFactor = Math.abs(factor);
            // If this is a negative factor and we haven't added a negative yet, add it as negative
            // Otherwise add the absolute value if it's not already present
            if (factor < 0 && !negativeAdded) {
                distinct.add(factor);
                negativeAdded = true;
            } else if (!distinct.contains(absFactor) && !distinct.contains(-absFactor)) {
                distinct.add(absFactor);
            }
        }

        // Return as an unmodifiable list
        return List.copyOf(distinct);
    }

    /**
     * Determines whether a number is prime.
     * <p>
     * A prime number is a natural number greater than 1 that has no positive divisors
     * other than 1 and itself. This method uses trial division up to the square root
     * of the number for efficiency.
     * </p>
     * <p>
     * Negative numbers are treated as their absolute values. Special cases:
     * </p>
     * <ul>
     *     <li>0 and 1 are not prime</li>
     *     <li>2 is the only even prime number</li>
     *     <li>Negative numbers are converted to positive (e.g., -7 is treated as 7)</li>
     * </ul>
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * Primes.isPrime(2);    // true  (smallest prime)
     * Primes.isPrime(17);   // true
     * Primes.isPrime(18);   // false (divisible by 2, 3, 6, 9)
     * Primes.isPrime(1);    // false (not prime by definition)
     * Primes.isPrime(0);    // false
     * Primes.isPrime(-7);   // true  (treated as 7)
     * }</pre>
     *
     * @param n the number to test for primality
     * @return {@code true} if {@code n} is prime, {@code false} otherwise
     */
    public static boolean isPrime(long n) {
        n = Math.abs(n);

        // Special cases: 0, 1 are not prime; 2 is the only even prime
        if (n < 2) {
            return false;
        }
        if (n == 2) {
            return true;
        }
        if (n % 2 == 0) {
            return false;
        }

        // Check odd divisors from 3 up to sqrt(n)
        long sqrtN = (long) Math.sqrt(n);
        for (long i = 3; i <= sqrtN; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Finds the next prime number strictly greater than the specified number.
     * <p>
     * Returns the smallest prime number that is greater than {@code n}. For efficiency,
     * the method only tests odd numbers (except when starting from values less than 2).
     * </p>
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * Primes.nextPrime(10);   // 11
     * Primes.nextPrime(11);   // 13
     * Primes.nextPrime(1);    // 2
     * Primes.nextPrime(2);    // 3
     * Primes.nextPrime(-5);   // 2  (next prime after -5)
     * }</pre>
     *
     * @param n the number after which to find the next prime
     * @return the smallest prime number greater than {@code n}
     */
    public static long nextPrime(long n) {
        // Start with the next number
        long candidate = n + 1;

        // Special case: if candidate is less than 2, return 2 (the first prime)
        if (candidate < 2) {
            return 2;
        }

        // Special case: if candidate is 2, return it
        if (candidate == 2) {
            return 2;
        }

        // Make sure we start with an odd number (unless it's 2)
        if (candidate % 2 == 0) {
            candidate++;
        }

        // Search for the next prime by testing odd numbers
        while (!isPrime(candidate)) {
            candidate += 2;
        }

        return candidate;
    }

    /**
     * Finds the previous prime number strictly less than the specified number.
     * <p>
     * Returns the largest prime number that is less than {@code n}. For efficiency,
     * the method only tests odd numbers (except when testing for 2).
     * </p>
     * <p>
     * There is no prime number less than 2, so this method requires {@code n > 2}.
     * </p>
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * Primes.previousPrime(10);   // 7
     * Primes.previousPrime(11);   // 7
     * Primes.previousPrime(8);    // 7
     * Primes.previousPrime(3);    // 2
     * }</pre>
     *
     * @param n the number before which to find the previous prime (must be greater than 2)
     * @return the largest prime number less than {@code n}
     * @throws IllegalArgumentException if {@code n <= 2} (no prime exists before 2)
     */
    public static long previousPrime(long n) {
        if (n <= 2) {
            throw new IllegalArgumentException(
                    "Cannot find previous prime before " + n + ". There is no prime less than 2.");
        }

        // Start with the previous number
        long candidate = n - 1;

        // Special case: if candidate is 2, return it
        if (candidate == 2) {
            return 2;
        }

        // Make sure we start with an odd number
        if (candidate % 2 == 0) {
            candidate--;
        }

        // Search backwards for a prime by testing odd numbers
        while (!isPrime(candidate)) {
            candidate -= 2;
        }

        return candidate;
    }

    /**
     * Computes the complete prime factorization of a number.
     * <p>
     * Returns a list of all prime factors of {@code n}, including duplicates. The factors
     * are returned in ascending order. The product of all returned factors equals the
     * absolute value of {@code n}.
     * </p>
     * <p>
     * For negative numbers, only the first occurrence of the smallest prime factor is
     * negative, ensuring that the product of all factors equals {@code n}.
     * </p>
     * <p>
     * Special cases:
     * </p>
     * <ul>
     *     <li>1 returns [] (empty list - the empty product equals 1 by mathematical convention)</li>
     *     <li>0 returns [] (empty list - 0 has no prime factorization)</li>
     *     <li>Prime numbers return a list containing only that number</li>
     *     <li>Negative numbers have their first prime factor negated</li>
     * </ul>
     *
     * <h3>Examples:</h3>
     * <pre>{@code
     * Primes.primeFactors(60);    // [2, 2, 3, 5]  (60 = 2 * 2 * 3 * 5)
     * Primes.primeFactors(17);    // [17]          (17 is prime)
     * Primes.primeFactors(1);     // []            (empty product = 1)
     * Primes.primeFactors(0);     // []            (no prime factorization)
     * Primes.primeFactors(-12);   // [-2, 2, 3]    (-12 = -2 * 2 * 3)
     * Primes.primeFactors(100);   // [2, 2, 5, 5]  (100 = 2^2 * 5^2)
     * }</pre>
     *
     * @param n the number to factorize (can be negative or zero)
     * @return an unmodifiable list of the prime factors of {@code n} in ascending order
     */
    public static List<Long> primeFactors(long n) {
        long num = Math.abs(n);
        List<Long> factors = new ArrayList<>();

        // Special case for 0 and 1: both return empty list
        // 1 = empty product (by mathematical convention)
        // 0 has no prime factorization
        if (num <= 1) {
            return Collections.unmodifiableList(factors);
        }

        // Trial division: find all prime factors
        // We only need to check up to sqrt(num) because i <= num/i is equivalent to i^2 <= num
        for (long i = 2; i <= num / i; i++) {
            while (num % i == 0) {
                factors.add(i);
                num /= i;
            }
        }

        // If num > 1 at this point, it's a prime factor itself
        if (num > 1) {
            factors.add(num);
        }

        // Handle negative input: negate only the first factor
        if (n < 0 && !factors.isEmpty()) {
            factors.set(0, -factors.get(0));
        }

        return Collections.unmodifiableList(factors);
    }
}
