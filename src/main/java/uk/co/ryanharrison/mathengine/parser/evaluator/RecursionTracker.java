package uk.co.ryanharrison.mathengine.parser.evaluator;

/**
 * Tracks recursion depth across all evaluation contexts.
 * Shared across parent and child contexts to properly detect and prevent infinite recursion.
 * <p>
 * This class is mutable and should be shared by reference across all related contexts
 * in a single evaluation tree.
 */
public final class RecursionTracker {

    private final int maxDepth;
    private int currentDepth;

    /**
     * Create a new recursion tracker with the specified maximum depth.
     *
     * @param maxDepth the maximum allowed recursion depth (default: 1000)
     */
    public RecursionTracker(int maxDepth) {
        this.maxDepth = maxDepth;
        this.currentDepth = 0;
    }

    /**
     * Create a new recursion tracker with default maximum depth of 1000.
     */
    public RecursionTracker() {
        this(1000);
    }

    /**
     * Enter a function call, incrementing the recursion depth.
     *
     * @param functionName the name of the function being entered (for error messages)
     * @throws StackOverflowException if maximum recursion depth is exceeded
     */
    public void enterFunction(String functionName) {
        currentDepth++;
        if (currentDepth > maxDepth) {
            throw new StackOverflowException(functionName, maxDepth);
        }
    }

    /**
     * Exit a function call, decrementing the recursion depth.
     */
    public void exitFunction() {
        if (currentDepth > 0) {
            currentDepth--;
        }
    }

    /**
     * Get the current recursion depth.
     */
    public int getCurrentDepth() {
        return currentDepth;
    }

    /**
     * Get the maximum allowed recursion depth.
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Reset the recursion depth to zero.
     * This should only be used in special cases like resetting the evaluator state.
     */
    public void reset() {
        currentDepth = 0;
    }
}
