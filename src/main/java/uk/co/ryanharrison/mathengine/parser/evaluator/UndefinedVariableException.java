package uk.co.ryanharrison.mathengine.parser.evaluator;

import uk.co.ryanharrison.mathengine.parser.lexer.Token;

/**
 * Thrown when a name resolves to nothing.
 * <p>
 * The {@link Kind} records what sort of name was being looked for, which decides both
 * how the failure reads and what advice is worth giving. Only a plain name suggests
 * defining a variable; an unknown unit does not.
 *
 * <pre>{@code
 * throw UndefinedVariableException.unit("furlong");   // Unknown unit '@furlong'
 * throw UndefinedVariableException.function("sinn");  // Unknown function 'sinn'
 * }</pre>
 */
public class UndefinedVariableException extends EvaluationException {

    /**
     * What the syntax was trying to resolve, which decides the wording.
     */
    public enum Kind {
        /**
         * A bare identifier that could have been any of the below.
         */
        NAME("No value associated with '%s'"),
        /**
         * An explicit {@code $name} reference.
         */
        VARIABLE("Undefined variable '$%s'"),
        /**
         * A function call.
         */
        FUNCTION("Unknown function '%s'"),
        /**
         * A unit, whether written bare or as an explicit {@code @name} reference.
         */
        UNIT("Unknown unit '%s'"),
        /**
         * An explicit {@code #name} reference.
         */
        CONSTANT("Undefined constant '#%s'");

        private final String template;

        Kind(String template) {
            this.template = template;
        }

        String describe(String name) {
            return String.format(template, name);
        }
    }

    private final String variableName;
    private final Kind kind;

    public UndefinedVariableException(String variableName) {
        this(variableName, Kind.NAME);
    }

    public UndefinedVariableException(String variableName, Token token) {
        super(Kind.NAME.describe(variableName), token);
        this.variableName = variableName;
        this.kind = Kind.NAME;
    }

    private UndefinedVariableException(String variableName, Kind kind) {
        super(kind.describe(variableName));
        this.variableName = variableName;
        this.kind = kind;
    }

    /**
     * An explicit {@code $name} reference that resolved to nothing.
     */
    public static UndefinedVariableException variable(String name) {
        return new UndefinedVariableException(name, Kind.VARIABLE);
    }

    /**
     * A call to a function that is not registered and not user-defined.
     */
    public static UndefinedVariableException function(String name) {
        return new UndefinedVariableException(name, Kind.FUNCTION);
    }

    /**
     * An explicit {@code @name} reference that is not in the unit registry.
     */
    public static UndefinedVariableException unit(String name) {
        return new UndefinedVariableException(name, Kind.UNIT);
    }

    /**
     * An explicit {@code #name} reference that is not in the constant registry.
     */
    public static UndefinedVariableException constant(String name) {
        return new UndefinedVariableException(name, Kind.CONSTANT);
    }

    /**
     * The bare name that could not be resolved, without any sigil.
     */
    public String getVariableName() {
        return variableName;
    }

    /** What sort of name was being looked for. */
    public Kind getKind() {
        return kind;
    }

    @Override
    public String formatMessage() {
        String baseMessage = super.formatMessage();
        if (kind != Kind.NAME || variableName.length() <= 1) {
            return baseMessage;
        }
        return baseMessage
                + "\n\nHint: '" + variableName + "' is not defined."
                + "\n  - Check spelling and case sensitivity"
                + "\n  - Use ':=' to assign variables (e.g., '" + variableName + " := 5')";
    }
}
