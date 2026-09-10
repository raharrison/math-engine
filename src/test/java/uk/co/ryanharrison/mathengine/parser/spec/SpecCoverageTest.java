package uk.co.ryanharrison.mathengine.parser.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.co.ryanharrison.mathengine.parser.MathEngine;
import uk.co.ryanharrison.mathengine.parser.function.MathFunction;
import uk.co.ryanharrison.mathengine.parser.function.StandardFunctions;
import uk.co.ryanharrison.mathengine.parser.registry.ConstantDefinition;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fails when the engine grows a name the spec files never exercise.
 * <p>
 * An alias nobody calls is an alias that can break, or collide with a new function,
 * unnoticed. The same goes for a constant. Both are cheap to cover and expensive to
 * discover broken, so the suite refuses to let one slip in silently.
 */
class SpecCoverageTest {

    /**
     * A call: an identifier immediately followed by an opening bracket.
     */
    private static final Pattern CALL = Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");

    /**
     * Any identifier, used to spot constants, which are referenced rather than called.
     */
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private static final Set<String> CALLED_NAMES = namesMatching(CALL);
    private static final Set<String> MENTIONED_NAMES = namesMatching(IDENTIFIER);

    @Test
    @DisplayName("every function name and alias is called by at least one case")
    void everyCallableNameIsExercised() {
        Set<String> missing = new TreeSet<>();
        for (MathFunction function : StandardFunctions.all()) {
            for (String name : callableNames(function)) {
                if (!CALLED_NAMES.contains(name)) {
                    missing.add(name + " (" + function.category() + ")");
                }
            }
        }
        assertThat(missing)
                .as("functions and aliases never called by a spec case")
                .isEmpty();
    }

    @Test
    @DisplayName("every built-in constant is referenced by at least one case")
    void everyConstantIsExercised() {
        Set<String> missing = MathEngine.create().getAllConstants().stream()
                .map(ConstantDefinition::name)
                .map(String::toLowerCase)
                .filter(name -> !MENTIONED_NAMES.contains(name))
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(missing).as("constants never referenced by a spec case").isEmpty();
    }

    @Test
    @DisplayName("every operator symbol is exercised by at least one case")
    void everyOperatorIsExercised() {
        List<String> symbols = List.of(
                "+", "-", "*", "/", "^", "mod", "of", "@", "..", ":=", "->",
                "==", "!=", "<", ">", "<=", ">=", "and", "or", "xor", "not",
                "!", "!!", "%", "in");

        Set<String> inputs = SpecLoader.allCases().stream()
                .map(SpecCase::input)
                .collect(Collectors.toSet());

        Set<String> missing = symbols.stream()
                .filter(symbol -> inputs.stream().noneMatch(input -> input.contains(symbol)))
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(missing).as("operators never used by a spec case").isEmpty();
    }

    /**
     * Every failure the engine can produce. Adding an exception type means adding a case
     * that names it, which is the only way a spec file can pin an exception down.
     */
    private static final List<String> EXCEPTION_TYPES = List.of(
            "ArityException", "DomainException", "EvaluationException", "LexerException",
            "ParseException", "StackOverflowException", "TypeError", "UndefinedVariableException",
            "ArithmeticException", "IllegalArgumentException");

    @Test
    @DisplayName("every exception type is named by at least one case")
    void everyExceptionTypeIsExercised() {
        Set<String> named = SpecLoader.allCases().stream()
                .map(SpecCase::expectedErrorType)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> missing = EXCEPTION_TYPES.stream()
                .filter(type -> !named.contains(type))
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(missing)
                .as("exception types no case pins down, so they could be widened unnoticed")
                .isEmpty();
    }

    private static List<String> callableNames(MathFunction function) {
        return java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(function.name()),
                        function.aliases().stream())
                .map(String::toLowerCase)
                .toList();
    }

    private static Set<String> namesMatching(Pattern pattern) {
        Set<String> names = new TreeSet<>();
        for (SpecCase testCase : SpecLoader.allCases()) {
            Matcher matcher = pattern.matcher(testCase.input());
            while (matcher.find()) {
                names.add((matcher.groupCount() >= 1 ? matcher.group(1) : matcher.group()).toLowerCase());
            }
        }
        return names;
    }
}
