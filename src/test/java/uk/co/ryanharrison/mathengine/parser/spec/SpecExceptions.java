package uk.co.ryanharrison.mathengine.parser.spec;

import java.util.List;

/**
 * Resolves the exception simple names that spec files are allowed to write.
 * <p>
 * The set is closed on purpose. A fixture cannot name an exception the engine has no
 * way to raise, and a typo fails at load time rather than turning into a silent
 * "anything may throw" assertion.
 */
final class SpecExceptions {

    /**
     * Packages searched for a named exception, most specific first. {@code java.lang}
     * comes last so an engine type always wins a name clash.
     */
    private static final List<String> PACKAGES = List.of(
            "uk.co.ryanharrison.mathengine.parser.evaluator.",
            "uk.co.ryanharrison.mathengine.parser.syntax.",
            "uk.co.ryanharrison.mathengine.parser.lexer.",
            "uk.co.ryanharrison.mathengine.parser.",
            "uk.co.ryanharrison.mathengine.core.",
            "java.lang."
    );

    private SpecExceptions() {
    }

    /**
     * @param simpleName the {@code expectedErrorType} written in a spec file
     * @return the exception class it names
     * @throws IllegalArgumentException if no such exception exists
     */
    static Class<? extends Throwable> resolve(String simpleName) {
        if (simpleName == null || simpleName.isBlank()) {
            throw new IllegalArgumentException(
                    "An error case must name its exception via 'expectedErrorType'");
        }
        for (String pkg : PACKAGES) {
            Class<? extends Throwable> found = tryLoad(pkg + simpleName);
            if (found != null) {
                return found;
            }
        }
        throw new IllegalArgumentException("Unknown exception type '" + simpleName
                + "'; expected one of the engine exceptions or a java.lang throwable");
    }

    /**
     * True when the name resolves, used by the integrity checks.
     */
    static boolean isKnown(String simpleName) {
        try {
            resolve(simpleName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static Class<? extends Throwable> tryLoad(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return Throwable.class.isAssignableFrom(clazz)
                    ? clazz.asSubclass(Throwable.class)
                    : null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
