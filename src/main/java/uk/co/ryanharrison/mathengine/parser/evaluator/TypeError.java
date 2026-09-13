package uk.co.ryanharrison.mathengine.parser.evaluator;

/**
 * Exception thrown when an operation is applied to incompatible types.
 * For example, trying to add a string to a number.
 */
public class TypeError extends EvaluationException {

    public TypeError(String message) {
        super(message);
    }

    @Override
    public String formatMessage() {
        return "Type error: " + getMessage();
    }
}
