package uk.co.ryanharrison.mathengine.parser.evaluator;

/**
 * Thrown when an argument is outside a function's valid domain, for example
 * {@code ln(-1)} or {@code percentile(v, 2)}.
 * <p>
 * Distinct from a plain {@link IllegalArgumentException} so that
 * {@code silentValidation} mode can turn domain errors into NaN without also
 * swallowing genuine programming errors.
 */
public class DomainException extends EvaluationException {

    public DomainException(String message) {
        super(message);
    }
}
