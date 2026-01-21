package uk.co.ryanharrison.mathengine.parser.evaluator;

/**
 * Exception thrown when the maximum recursion depth is exceeded.
 * This typically indicates infinite recursion or circular function definitions.
 */
public class StackOverflowException extends EvaluationException {

    private final String trace;
    private final int maxDepth;

    public StackOverflowException(String functionName, int maxDepth) {
        super(String.format("Maximum recursion depth exceeded (%d): %s", maxDepth, functionName));
        this.trace = null;
        this.maxDepth = maxDepth;
    }

    public StackOverflowException(String message, String trace) {
        super(message);
        this.trace = trace;
        this.maxDepth = -1;
    }

    public String getTrace() {
        return trace;
    }

    public int getMaxDepth() {
        return maxDepth;
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
