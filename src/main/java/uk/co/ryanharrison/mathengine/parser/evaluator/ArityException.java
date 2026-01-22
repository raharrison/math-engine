package uk.co.ryanharrison.mathengine.parser.evaluator;

/**
 * Exception thrown when a function is called with the wrong number of arguments.
 * <p>
 * Provides both expected and actual argument counts for better error messages.
 * </p>
 */
public class ArityException extends EvaluationException {

    private final int expected;
    private final int actual;

    public ArityException(String message) {
        super(message);
        this.expected = -1;
        this.actual = -1;
    }

    public ArityException(String functionName, int expected, int actual) {
        super(String.format("Function '%s' expects %d argument%s, got %d",
                functionName, expected, expected == 1 ? "" : "s", actual));
        this.expected = expected;
        this.actual = actual;
    }

    public int getExpected() {
        return expected;
    }

    public int getActual() {
        return actual;
    }

    @Override
    public String formatMessage() {
        String baseMessage = super.formatMessage();

        // Add helpful hint about argument count mismatch
        if (expected > 0 && actual >= 0) {
            String hint = "\n\nHint: ";
            if (actual < expected) {
                hint += String.format("Missing %d argument%s",
                        expected - actual, (expected - actual) == 1 ? "" : "s");
            } else {
                hint += String.format("Too many arguments provided (%d extra)", actual - expected);
            }
            return baseMessage + hint;
        }

        return baseMessage;
    }
}
