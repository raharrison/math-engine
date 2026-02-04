package uk.co.ryanharrison.mathengine.parser.lexer;

/**
 * Represents the type of token in the lexer.
 */
public enum TokenType {

    // Structural tokens
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACKET,
    RBRACKET,
    COMMA,
    SEMICOLON,
    COLON,

    // Literals
    INTEGER,
    DECIMAL,
    SCIENTIFIC,
    RATIONAL,
    STRING,

    // Operators - with precedence (lower number = higher precedence)
    // Level 6: Power
    POWER,

    // Level 7: Multiplicative
    MULTIPLY,
    DIVIDE,
    AT,
    MOD,
    OF,

    // Level 8: Additive
    PLUS,
    MINUS,

    // Level 9: Range
    RANGE,

    // Level 10: Relational
    LT,
    GT,
    LTE,
    GTE,

    // Level 11: Equality
    EQ,
    NEQ,

    // Level 12: Logical AND
    AND,

    // Level 13: Logical XOR
    XOR,

    // Level 14: Logical OR
    OR,

    // Level 15: Assignment
    ASSIGN,

    // Level 16: Lambda
    LAMBDA,

    // Unary operators (no precedence - handled differently)
    NOT,
    FACTORIAL,
    DOUBLE_FACTORIAL,
    PERCENT,

    // Special
    IDENTIFIER,
    KEYWORD,
    UNIT,
    FUNCTION,
    EOF,
    NEWLINE,
    ERROR,

    // Explicit unit reference: @fahrenheit
    UNIT_REF,

    // Explicit variable reference: $x
    VAR_REF,

    // Explicit constant reference: #pi
    CONST_REF
}
