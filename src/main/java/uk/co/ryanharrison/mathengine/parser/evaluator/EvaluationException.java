package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.MathEngineException;
import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Exception thrown during expression evaluation.
 * <p>
 * Indicates runtime errors such as:
 * <ul>
 *     <li>Division by zero</li>
 *     <li>Domain errors (e.g., sqrt of negative number)</li>
 *     <li>Type mismatches</li>
 *     <li>Feature disabled in configuration</li>
 * </ul>
 *
 * <h2>Example Messages:</h2>
 * <pre>
 * Evaluation error: Division by zero
 * Evaluation error: Cannot take square root of negative number: -4
 * Evaluation error: Vectors are disabled in current configuration
 * </pre>
 */
public class EvaluationException extends MathEngineException {

    /**
     * Creates an evaluation exception with just a message.
     *
     * @param message the error message
     */
    public EvaluationException(String message) {
        super(message);
    }

    /**
     * Creates an evaluation exception with token position info.
     *
     * @param message the error message
     * @param token   the token where the error occurred
     */
    public EvaluationException(String message, Token token) {
        super(message, token);
    }
}
