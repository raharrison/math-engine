package uk.co.ryanharrison.mathengine.special;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive test suite for the {@link Primes} utility class.
 * <p>
 * Tests cover primality testing, finding next/previous primes, prime factorization,
 * and distinct prime factors with extensive edge case coverage.
 * </p>
 */
class PrimesTest {

    // ==================== isPrime Tests ====================

    /**
     * Tests that known prime numbers are correctly identified.
     */
    @ParameterizedTest
    @ValueSource(longs = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97})
    void isPrimeIdentifiesKnownPrimes(long prime) {
        assertThat(Primes.isPrime(prime))
                .as("Expected %d to be identified as prime", prime)
                .isTrue();
    }

    /**
     * Tests that known composite numbers are correctly identified as not prime.
     */
    @ParameterizedTest
    @ValueSource(longs = {4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 22, 24, 25, 26, 27, 28, 30, 100, 121, 144})
    void isPrimeIdentifiesCompositeNumbers(long composite) {
        assertThat(Primes.isPrime(composite))
                .as("Expected %d to be identified as composite (not prime)", composite)
                .isFalse();
    }

    /**
     * Tests edge cases for isPrime: 0, 1, and 2.
     */
    @Test
    void isPrimeHandlesEdgeCases() {
        assertThat(Primes.isPrime(0)).isFalse();
        assertThat(Primes.isPrime(1)).isFalse();
        assertThat(Primes.isPrime(2)).isTrue();
    }

    /**
     * Tests that negative numbers are treated as their absolute values.
     */
    @ParameterizedTest
    @CsvSource({
            "-2, true",
            "-3, true",
            "-5, true",
            "-7, true",
            "-11, true",
            "-4, false",
            "-6, false",
            "-8, false",
            "-9, false",
            "-1, false"
    })
    void isPrimeTreatsNegativeNumbersAsAbsolute(long n, boolean expectedPrime) {
        assertThat(Primes.isPrime(n)).isEqualTo(expectedPrime);
    }

    /**
     * Tests isPrime with larger prime numbers.
     */
    @ParameterizedTest
    @ValueSource(longs = {101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199})
    void isPrimeIdentifiesLargerPrimes(long prime) {
        assertThat(Primes.isPrime(prime)).isTrue();
    }

    /**
     * Tests isPrime with larger composite numbers.
     */
    @ParameterizedTest
    @ValueSource(longs = {100, 102, 104, 105, 106, 108, 110, 111, 112, 114, 115, 116, 117, 118, 119, 120})
    void isPrimeIdentifiesLargerComposites(long composite) {
        assertThat(Primes.isPrime(composite)).isFalse();
    }

    // ==================== nextPrime Tests ====================

    /**
     * Tests finding the next prime after various starting values.
     */
    @ParameterizedTest
    @CsvSource({
            "1, 2",
            "2, 3",
            "3, 5",
            "4, 5",
            "5, 7",
            "6, 7",
            "7, 11",
            "10, 11",
            "11, 13",
            "12, 13",
            "13, 17",
            "20, 23",
            "50, 53",
            "100, 101"
    })
    void nextPrimeReturnsCorrectPrime(long n, long expectedNext) {
        assertThat(Primes.nextPrime(n))
                .as("Next prime after %d should be %d", n, expectedNext)
                .isEqualTo(expectedNext);
    }

    /**
     * Tests nextPrime with negative starting values.
     */
    @ParameterizedTest
    @CsvSource({
            "-10, 2",
            "-5, 2",
            "-1, 2",
            "0, 2"
    })
    void nextPrimeHandlesNegativeAndZero(long n, long expectedNext) {
        assertThat(Primes.nextPrime(n)).isEqualTo(expectedNext);
    }

    /**
     * Tests that nextPrime always returns a prime number.
     */
    @ParameterizedTest
    @ValueSource(longs = {1, 2, 5, 10, 15, 20, 50, 100, 200})
    void nextPrimeReturnsActualPrime(long n) {
        long next = Primes.nextPrime(n);
        assertThat(Primes.isPrime(next))
                .as("nextPrime(%d) = %d should be prime", n, next)
                .isTrue();
        assertThat(next).isGreaterThan(n);
    }

    /**
     * Tests nextPrime sequence continuity.
     */
    @Test
    void nextPrimeProducesCorrectSequence() {
        long[] expectedPrimes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
        long current = 1;

        for (long expectedPrime : expectedPrimes) {
            current = Primes.nextPrime(current);
            assertThat(current).isEqualTo(expectedPrime);
        }
    }

    // ==================== previousPrime Tests ====================

    /**
     * Tests finding the previous prime before various starting values.
     */
    @ParameterizedTest
    @CsvSource({
            "3, 2",
            "4, 3",
            "5, 3",
            "6, 5",
            "7, 5",
            "8, 7",
            "10, 7",
            "11, 7",
            "12, 11",
            "13, 11",
            "20, 19",
            "50, 47",
            "100, 97"
    })
    void previousPrimeReturnsCorrectPrime(long n, long expectedPrevious) {
        assertThat(Primes.previousPrime(n))
                .as("Previous prime before %d should be %d", n, expectedPrevious)
                .isEqualTo(expectedPrevious);
    }

    /**
     * Tests that previousPrime throws exception for values <= 2.
     */
    @ParameterizedTest
    @ValueSource(longs = {2, 1, 0, -1, -5, -10})
    void previousPrimeThrowsExceptionForInvalidInput(long n) {
        assertThatThrownBy(() -> Primes.previousPrime(n))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot find previous prime before " + n)
                .hasMessageContaining("no prime less than 2");
    }

    /**
     * Tests that previousPrime always returns a prime number.
     */
    @ParameterizedTest
    @ValueSource(longs = {5, 10, 15, 20, 50, 100, 200})
    void previousPrimeReturnsActualPrime(long n) {
        long prev = Primes.previousPrime(n);
        assertThat(Primes.isPrime(prev))
                .as("previousPrime(%d) = %d should be prime", n, prev)
                .isTrue();
        assertThat(prev).isLessThan(n);
    }

    /**
     * Tests previousPrime sequence continuity in reverse.
     */
    @Test
    void previousPrimeProducesCorrectReverseSequence() {
        long[] expectedPrimes = {29, 23, 19, 17, 13, 11, 7, 5, 3, 2};
        long current = 30;

        for (long expectedPrime : expectedPrimes) {
            current = Primes.previousPrime(current);
            assertThat(current).isEqualTo(expectedPrime);
        }
    }

    // ==================== primeFactors Tests ====================

    /**
     * Tests prime factorization of known composite numbers.
     */
    @Test
    void primeFactorsReturnsCorrectFactorization() {
        assertThat(Primes.primeFactors(60)).containsExactly(2L, 2L, 3L, 5L);
        assertThat(Primes.primeFactors(100)).containsExactly(2L, 2L, 5L, 5L);
        assertThat(Primes.primeFactors(12)).containsExactly(2L, 2L, 3L);
        assertThat(Primes.primeFactors(30)).containsExactly(2L, 3L, 5L);
        assertThat(Primes.primeFactors(18)).containsExactly(2L, 3L, 3L);
        assertThat(Primes.primeFactors(120)).containsExactly(2L, 2L, 2L, 3L, 5L);
    }

    /**
     * Tests that prime numbers return themselves as their only factor.
     */
    @ParameterizedTest
    @ValueSource(longs = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47})
    void primeFactorsOfPrimeNumberIsItself(long prime) {
        assertThat(Primes.primeFactors(prime)).containsExactly(prime);
    }

    /**
     * Tests prime factorization of 1 (special case - empty product).
     */
    @Test
    void primeFactorsOf1ReturnsEmptyList() {
        assertThat(Primes.primeFactors(1)).isEmpty();
    }

    /**
     * Tests prime factorization of 0 (special case - no factorization).
     */
    @Test
    void primeFactorsOf0ReturnsEmptyList() {
        assertThat(Primes.primeFactors(0)).isEmpty();
    }

    /**
     * Tests that negative numbers have their first prime factor negated.
     */
    @Test
    void primeFactorsHandlesNegativeNumbers() {
        assertThat(Primes.primeFactors(-12)).containsExactly(-2L, 2L, 3L);
        assertThat(Primes.primeFactors(-60)).containsExactly(-2L, 2L, 3L, 5L);
        assertThat(Primes.primeFactors(-30)).containsExactly(-2L, 3L, 5L);
        assertThat(Primes.primeFactors(-18)).containsExactly(-2L, 3L, 3L);
        assertThat(Primes.primeFactors(-7)).containsExactly(-7L);
    }

    /**
     * Tests that the product of prime factors equals the original number.
     */
    @ParameterizedTest
    @ValueSource(longs = {60, 100, 12, 30, 18, 120, 77, 91, 143})
    void primeFactorsProductEqualsOriginal(long n) {
        List<Long> factors = Primes.primeFactors(n);
        long product = factors.stream().reduce(1L, (a, b) -> a * b);
        assertThat(product).isEqualTo(n);
    }

    /**
     * Tests that the product of prime factors equals the original negative number.
     */
    @ParameterizedTest
    @ValueSource(longs = {-60, -100, -12, -30, -18, -120})
    void primeFactorsProductEqualsOriginalForNegative(long n) {
        List<Long> factors = Primes.primeFactors(n);
        long product = factors.stream().reduce(1L, (a, b) -> a * b);
        assertThat(product).isEqualTo(n);
    }

    /**
     * Tests prime factorization with powers of 2.
     */
    @Test
    void primeFactorsOfPowersOfTwo() {
        assertThat(Primes.primeFactors(2)).containsExactly(2L);
        assertThat(Primes.primeFactors(4)).containsExactly(2L, 2L);
        assertThat(Primes.primeFactors(8)).containsExactly(2L, 2L, 2L);
        assertThat(Primes.primeFactors(16)).containsExactly(2L, 2L, 2L, 2L);
        assertThat(Primes.primeFactors(32)).containsExactly(2L, 2L, 2L, 2L, 2L);
        assertThat(Primes.primeFactors(64)).containsExactly(2L, 2L, 2L, 2L, 2L, 2L);
    }

    /**
     * Tests that primeFactors returns an unmodifiable list.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void primeFactorsReturnsUnmodifiableList() {
        List<Long> factors = Primes.primeFactors(60);

        assertThatThrownBy(() -> factors.add(7L))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(factors::removeFirst)
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> factors.set(0, 3L))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    // ==================== distinctPrimeFactors Tests ====================

    /**
     * Tests that distinct prime factors removes duplicates.
     */
    @Test
    void distinctPrimeFactorsRemovesDuplicates() {
        assertThat(Primes.distinctPrimeFactors(60)).containsExactly(2L, 3L, 5L);
        assertThat(Primes.distinctPrimeFactors(100)).containsExactly(2L, 5L);
        assertThat(Primes.distinctPrimeFactors(12)).containsExactly(2L, 3L);
        assertThat(Primes.distinctPrimeFactors(18)).containsExactly(2L, 3L);
        assertThat(Primes.distinctPrimeFactors(120)).containsExactly(2L, 3L, 5L);
    }

    /**
     * Tests that prime numbers return themselves as their only distinct factor.
     */
    @ParameterizedTest
    @ValueSource(longs = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29})
    void distinctPrimeFactorsOfPrimeIsItself(long prime) {
        assertThat(Primes.distinctPrimeFactors(prime)).containsExactly(prime);
    }

    /**
     * Tests distinct prime factors of 1 (empty - no prime factors).
     */
    @Test
    void distinctPrimeFactorsOf1ReturnsEmptyList() {
        assertThat(Primes.distinctPrimeFactors(1)).isEmpty();
    }

    /**
     * Tests distinct prime factors of 0 (empty - no prime factors).
     */
    @Test
    void distinctPrimeFactorsOf0ReturnsEmptyList() {
        assertThat(Primes.distinctPrimeFactors(0)).isEmpty();
    }

    /**
     * Tests distinct prime factors with negative numbers.
     */
    @Test
    void distinctPrimeFactorsHandlesNegativeNumbers() {
        assertThat(Primes.distinctPrimeFactors(-12)).containsExactly(-2L, 3L);
        assertThat(Primes.distinctPrimeFactors(-60)).containsExactly(-2L, 3L, 5L);
        assertThat(Primes.distinctPrimeFactors(-18)).containsExactly(-2L, 3L);
    }

    /**
     * Tests that distinct factors are in ascending order.
     */
    @Test
    void distinctPrimeFactorsAreInAscendingOrder() {
        List<Long> factors = Primes.distinctPrimeFactors(210);  // 2 * 3 * 5 * 7
        assertThat(factors).containsExactly(2L, 3L, 5L, 7L);
        assertThat(factors).isSorted();
    }

    /**
     * Tests that distinctPrimeFactors returns an unmodifiable list.
     */
    @SuppressWarnings("DataFlowIssue")
    @Test
    void distinctPrimeFactorsReturnsUnmodifiableList() {
        List<Long> factors = Primes.distinctPrimeFactors(60);

        assertThatThrownBy(() -> factors.add(7L))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(factors::removeFirst)
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> factors.set(0, 3L))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Tests distinct factors with powers of a single prime.
     */
    @Test
    void distinctPrimeFactorsOfPrimePowers() {
        assertThat(Primes.distinctPrimeFactors(8)).containsExactly(2L);   // 2^3
        assertThat(Primes.distinctPrimeFactors(27)).containsExactly(3L);  // 3^3
        assertThat(Primes.distinctPrimeFactors(125)).containsExactly(5L); // 5^3
    }

    /**
     * Tests that distinct factors count is always less than or equal to all factors.
     */
    @ParameterizedTest
    @ValueSource(longs = {60, 100, 120, 210, 360, 1000})
    void distinctFactorsCountIsLessThanOrEqualToAllFactors(long n) {
        List<Long> allFactors = Primes.primeFactors(n);
        List<Long> distinctFactors = Primes.distinctPrimeFactors(n);

        assertThat(distinctFactors.size())
                .as("Distinct factors should be <= all factors for %d", n)
                .isLessThanOrEqualTo(allFactors.size());
    }

    // ==================== Integration Tests ====================

    /**
     * Tests that nextPrime and previousPrime are inverse operations.
     */
    @ParameterizedTest
    @ValueSource(longs = {5, 10, 20, 50, 100})
    void nextPrimeAndPreviousPrimeAreInverse(long n) {
        long next = Primes.nextPrime(n);
        long prev = Primes.previousPrime(next);

        // previousPrime(nextPrime(n)) should be <= n
        assertThat(prev).isLessThanOrEqualTo(n);

        // nextPrime(previousPrime(n)) should be >= n (if n >= 3)
        if (n >= 3) {
            prev = Primes.previousPrime(n);
            next = Primes.nextPrime(prev);
            assertThat(next).isGreaterThanOrEqualTo(n);
        }
    }

    /**
     * Tests that all prime factors are actually prime (except for 1 and negative signs).
     */
    @ParameterizedTest
    @ValueSource(longs = {60, 100, 120, 210, 360, 1000})
    void allPrimeFactorsArePrime(long n) {
        List<Long> factors = Primes.primeFactors(n);

        for (Long factor : factors) {
            long absFactor = Math.abs(factor);
            if (absFactor != 1) {
                assertThat(Primes.isPrime(absFactor))
                        .as("Factor %d of %d should be prime", factor, n)
                        .isTrue();
            }
        }
    }

    /**
     * Tests that all distinct prime factors are actually prime (except for 1 and negative signs).
     */
    @ParameterizedTest
    @ValueSource(longs = {60, 100, 120, 210, 360, 1000})
    void allDistinctPrimeFactorsArePrime(long n) {
        List<Long> factors = Primes.distinctPrimeFactors(n);

        for (Long factor : factors) {
            long absFactor = Math.abs(factor);
            if (absFactor != 1) {
                assertThat(Primes.isPrime(absFactor))
                        .as("Distinct factor %d of %d should be prime", factor, n)
                        .isTrue();
            }
        }
    }
}
