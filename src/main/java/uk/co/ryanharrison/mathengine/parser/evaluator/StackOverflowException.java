package uk.co.ryanharrison.mathengine.parser.evaluator;

/**
 * Exception thrown when the maximum recursion depth is exceeded.
 * This typically indicates infinite recursion or circular function definitions.
 */
public class StackOverflowException extends EvaluationException {

    private final String trace;

    public StackOverflowException(String functionName, int maxDepth) {
        super(String.format("Maximum recursion depth exceeded (%d): %s", maxDepth, functionName));
        this.trace = null;
    }

    public StackOverflowException(String message, String trace) {
        super(message);
        this.trace = trace;
    }

    /**
     * The JVM stack ran out before any configured limit was reached. Whether the limit or
     * the stack goes first depends on how much the calling thread has left.
     */
    public static StackOverflowException outOfStack() {
        return new StackOverflowException(
                "Expression nests too deeply to process: the JVM stack ran out before any "
                        + "configured limit was reached. Simplify the expression, or run the "
                        + "engine on a thread with a larger stack.", null);
    }

    @Override
    public String formatMessage() {
        var sb = new StringBuilder();
        sb.append(getMessage());
        if (trace != null) {
            sb.append("\n\n").append(trace);
        }
        return sb.toString();
    }
}
