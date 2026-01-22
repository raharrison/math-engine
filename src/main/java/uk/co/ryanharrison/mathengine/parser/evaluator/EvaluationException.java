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

    /**
     * Creates an evaluation exception with token and source code.
     *
     * @param message    the error message
     * @param token      the token where the error occurred
     * @param sourceCode the source code being evaluated
     */
    public EvaluationException(String message, Token token, String sourceCode) {
        super(message, token, sourceCode);
    }

    /**
     * Creates an evaluation exception wrapping another exception.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    // Uses default formatMessage() implementation from MathEngineException
}
