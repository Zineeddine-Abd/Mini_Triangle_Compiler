public class Token {
    // Token kinds
    public static final byte IDENTIFIER = 0;
    public static final byte INTLITERAL = 1;
    public static final byte STRINGLITERAL = 2;
    public static final byte KEYWORD = 3;
    public static final byte PLUS = 4;
    public static final byte MINUS = 5;
    public static final byte MULT = 6;
    public static final byte DIV = 7;
    public static final byte LESS = 8;
    public static final byte GREATER = 9;
    public static final byte EQUAL = 10;
    public static final byte SEMICOLON = 11;
    public static final byte COLON = 12;
    public static final byte ASSIGN = 13;
    public static final byte TILDE = 14;
    public static final byte LPAREN = 15;
    public static final byte RPAREN = 16;
    public static final byte EOT = 17;
    public static final byte UNKNOWN = 18;

    public byte kind;
    public String spelling;

    public Token(byte kind, String spelling) {
        this.kind = kind;
        this.spelling = spelling;
    }

    @Override
    public String toString() {
        return "Token(kind=" + kind + ", spelling=\"" + spelling + "\")";
    }
}