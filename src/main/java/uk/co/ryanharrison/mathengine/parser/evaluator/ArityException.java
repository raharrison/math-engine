package uk.co.ryanharrison.mathengine.parser.evaluator;

/**
 * Exception thrown when a function is called with the wrong number of arguments.
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
}
