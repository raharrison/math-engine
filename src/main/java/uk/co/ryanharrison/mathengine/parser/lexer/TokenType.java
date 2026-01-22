package uk.co.ryanharrison.mathengine.parser.lexer;

/**
 * Represents the type of token in the lexer.
 */
public enum TokenType {

    // Structural tokens
    LPAREN("LPAREN", "'('"),
    RPAREN("RPAREN", "')'"),
    LBRACE("LBRACE", "'{'"),
    RBRACE("RBRACE", "'}'"),
    LBRACKET("LBRACKET", "'['"),
    RBRACKET("RBRACKET", "']'"),
    COMMA("COMMA", "','"),
    SEMICOLON("SEMICOLON", "';'"),
    COLON("COLON", "':'"),

    // Literals
    INTEGER("INTEGER", "number"),
    DECIMAL("DECIMAL", "number"),
    SCIENTIFIC("SCIENTIFIC", "number"),
    RATIONAL("RATIONAL", "number"),
    STRING("STRING", "string"),

    // Operators - with precedence (lower number = higher precedence)
    // Level 6: Power
    POWER("POWER", "'^'"),

    // Level 7: Multiplicative
    MULTIPLY("MULTIPLY", "'*'"),
    DIVIDE("DIVIDE", "'/'"),
    AT("AT", "'@'"),
    MOD("MOD", "'%'"),
    OF("OF", "of"),

    // Level 8: Additive
    PLUS("PLUS", "'+'"),
    MINUS("MINUS", "'-'"),

    // Level 9: Range
    RANGE("RANGE", "'..'"),

    // Level 10: Relational
    LT("LT", "'<'"),
    GT("GT", "'>'"),
    LTE("LTE", "'<='"),
    GTE("GTE", "'>='"),

    // Level 11: Equality
    EQ("EQ", "'=='"),
    NEQ("NEQ", "'!='"),

    // Level 12: Logical AND
    AND("AND", "and"),

    // Level 13: Logical XOR
    XOR("XOR", "xor"),

    // Level 14: Logical OR
    OR("OR", "or"),

    // Level 15: Assignment
    ASSIGN("ASSIGN", "':='"),

    // Level 16: Lambda
    LAMBDA("LAMBDA", "'->'"),

    // Unary operators (no precedence - handled differently)
    NOT("NOT", "not"),
    FACTORIAL("FACTORIAL", "'!'"),
    DOUBLE_FACTORIAL("DOUBLE_FACTORIAL", "'!!'"),
    PERCENT("PERCENT", "'%'"),

    // Special
    IDENTIFIER("IDENTIFIER", "identifier"),
    KEYWORD("KEYWORD", "keyword"),
    UNIT("UNIT", "unit"),
    FUNCTION("FUNCTION", "function"),
    EOF("EOF", "end of expression"),
    NEWLINE("NEWLINE", "newline"),
    ERROR("ERROR", "error"),

    // Explicit unit reference: @fahrenheit
    UNIT_REF("UNIT_REF", "unit reference"),

    // Explicit variable reference: $x
    VAR_REF("VAR_REF", "variable reference"),

    // Explicit constant reference: #pi
    CONST_REF("CONST_REF", "constant reference");

    private final String name;
    private final String display;

    TokenType(String name, String display) {
        this.name = name;
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    @Override
    public String toString() {
        return name;
    }
}

