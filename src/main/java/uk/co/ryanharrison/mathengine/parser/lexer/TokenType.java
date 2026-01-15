package uk.co.ryanharrison.mathengine.parser.lexer;

/**
 * Represents the type of a token in the lexer.
 * <p>
 * This class uses a flexible design to allow for dynamic operator registration.
 * Instead of a fixed enum, token types are represented as constants that can be extended.
 */
public final class TokenType {

    private final String name;
    private final int precedence;
    private final boolean rightAssociative;

    private TokenType(String name, int precedence, boolean rightAssociative) {
        this.name = name;
        this.precedence = precedence;
        this.rightAssociative = rightAssociative;
    }

    private TokenType(String name) {
        this(name, -1, false);
    }

    public String getName() {
        return name;
    }

    public int getPrecedence() {
        return precedence;
    }

    public boolean isRightAssociative() {
        return rightAssociative;
    }

    public boolean isOperator() {
        return precedence >= 0;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TokenType other = (TokenType) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    // Structural tokens
    public static final TokenType LPAREN = new TokenType("LPAREN");
    public static final TokenType RPAREN = new TokenType("RPAREN");
    public static final TokenType LBRACE = new TokenType("LBRACE");
    public static final TokenType RBRACE = new TokenType("RBRACE");
    public static final TokenType LBRACKET = new TokenType("LBRACKET");
    public static final TokenType RBRACKET = new TokenType("RBRACKET");
    public static final TokenType COMMA = new TokenType("COMMA");
    public static final TokenType SEMICOLON = new TokenType("SEMICOLON");
    public static final TokenType COLON = new TokenType("COLON");

    // Literals
    public static final TokenType INTEGER = new TokenType("INTEGER");
    public static final TokenType DECIMAL = new TokenType("DECIMAL");
    public static final TokenType SCIENTIFIC = new TokenType("SCIENTIFIC");
    public static final TokenType RATIONAL = new TokenType("RATIONAL");
    public static final TokenType STRING = new TokenType("STRING");

    // Operators - with precedence (lower number = higher precedence)
    // Level 6: Power
    public static final TokenType POWER = new TokenType("POWER", 6, true);

    // Level 7: Multiplicative
    public static final TokenType MULTIPLY = new TokenType("MULTIPLY", 7, false);
    public static final TokenType DIVIDE = new TokenType("DIVIDE", 7, false);
    public static final TokenType AT = new TokenType("AT", 7, false);  // Matrix multiplication
    public static final TokenType MOD = new TokenType("MOD", 7, false);
    public static final TokenType OF = new TokenType("OF", 7, false);

    // Level 8: Additive
    public static final TokenType PLUS = new TokenType("PLUS", 8, false);
    public static final TokenType MINUS = new TokenType("MINUS", 8, false);

    // Level 9: Range
    public static final TokenType RANGE = new TokenType("RANGE", 9, false);

    // Level 10: Relational
    public static final TokenType LT = new TokenType("LT", 10, false);
    public static final TokenType GT = new TokenType("GT", 10, false);
    public static final TokenType LTE = new TokenType("LTE", 10, false);
    public static final TokenType GTE = new TokenType("GTE", 10, false);

    // Level 11: Equality
    public static final TokenType EQ = new TokenType("EQ", 11, false);
    public static final TokenType NEQ = new TokenType("NEQ", 11, false);

    // Level 12: Logical AND
    public static final TokenType AND = new TokenType("AND", 12, false);

    // Level 13: Logical XOR
    public static final TokenType XOR = new TokenType("XOR", 13, false);

    // Level 14: Logical OR
    public static final TokenType OR = new TokenType("OR", 14, false);

    // Level 15: Assignment
    public static final TokenType ASSIGN = new TokenType("ASSIGN", 15, true);

    // Level 16: Lambda
    public static final TokenType LAMBDA = new TokenType("LAMBDA", 16, true);

    // Unary operators (no precedence - handled differently)
    public static final TokenType NOT = new TokenType("NOT");
    public static final TokenType FACTORIAL = new TokenType("FACTORIAL");
    public static final TokenType DOUBLE_FACTORIAL = new TokenType("DOUBLE_FACTORIAL");
    public static final TokenType PERCENT = new TokenType("PERCENT");

    // Special
    public static final TokenType IDENTIFIER = new TokenType("IDENTIFIER");
    public static final TokenType KEYWORD = new TokenType("KEYWORD");
    public static final TokenType UNIT = new TokenType("UNIT");
    public static final TokenType FUNCTION = new TokenType("FUNCTION");
    public static final TokenType EOF = new TokenType("EOF");
    public static final TokenType NEWLINE = new TokenType("NEWLINE");
}
