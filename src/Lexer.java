import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Lexer {
    //keywords
    private static final String[] keywordsArray = {
            "begin", "const", "do", "else", "end", "if", "in",
            "let", "while", "then", "var", "function", "return"
    };
    private static final Set<String> keywords = new HashSet<>(Arrays.asList(keywordsArray));

    private BufferedReader reader;
    private int currentChar;  //because read returns the ASCII code
    private StringBuilder tokenValue; //to trace order of tokens

    public Lexer(String filename) throws IOException {
        reader = new BufferedReader(new FileReader(filename));
        nextChar();
    }

    private void nextChar() throws IOException {
        currentChar = reader.read();
    }

    private void appendIt() throws IOException {
        tokenValue.append((char) currentChar);
        nextChar();
    }

    private boolean isLetter() {
        return Character.isLetter(currentChar); //works with ASCII codes
    }

    private boolean isDigit() {
        return Character.isDigit(currentChar); //works with ASCII codes
    }

    private void scanSeparator() throws IOException {
        if (currentChar == '!') {  // Comment
            while (currentChar != '\n' && currentChar != -1) {
                nextChar();
            }
        } else {  // Space or End of Line
            nextChar();
        }
    }

    public Token scan() throws IOException {
        tokenValue = new StringBuilder();

        // Skip all whitespace and comments
        while (currentChar != -1 && (Character.isWhitespace(currentChar) || currentChar == '!')) {
            scanSeparator();
        }

        if (isLetter()) {  // Identifier or Keyword
            while (isLetter() || isDigit()) appendIt();
            String value = tokenValue.toString();
            if (keywords.contains(value)) {
                return new Token(Token.KEYWORD, value);
            }
            return new Token(Token.IDENTIFIER, value);

        } else if (isDigit()) {  // Integer Literal
            while (isDigit()) appendIt();
            return new Token(Token.INTLITERAL, tokenValue.toString());

        } else if (currentChar == '"') {  // String Literal
            appendIt();  // Consume the opening quote
            while (currentChar != '"' && currentChar != -1) {
                appendIt();  // Collect characters inside the quotes
            }
            if (currentChar == '"') {
                appendIt();  // Consume the closing quote
                return new Token(Token.STRINGLITERAL, tokenValue.toString());
            } else {
                return new Token(Token.UNKNOWN, "Unterminated string literal");
            }

        } else {  // Operator or Symbol
            switch (currentChar) {
                case '+': appendIt(); return new Token(Token.PLUS, tokenValue.toString());
                case '-': appendIt(); return new Token(Token.MINUS, tokenValue.toString());
                case '*': appendIt(); return new Token(Token.MULT, tokenValue.toString());
                case '/': appendIt(); return new Token(Token.DIV, tokenValue.toString());
                case '<': appendIt(); return new Token(Token.LESS, tokenValue.toString());
                case '>': appendIt(); return new Token(Token.GREATER, tokenValue.toString());
                case '=': appendIt(); return new Token(Token.EQUAL, tokenValue.toString());
                case ';': appendIt(); return new Token(Token.SEMICOLON, tokenValue.toString());
                case ':': appendIt();
                    if (currentChar == '=') {
                        appendIt();
                        return new Token(Token.ASSIGN, ":=");
                    }
                    return new Token(Token.COLON, tokenValue.toString());
                case '~': appendIt(); return new Token(Token.TILDE, tokenValue.toString());
                case '(': appendIt(); return new Token(Token.LPAREN, tokenValue.toString());
                case ')': appendIt(); return new Token(Token.RPAREN, tokenValue.toString());
                case -1: return new Token(Token.EOT, "");  // End of text (end of the file)
                default:
                    int tempChar = currentChar; nextChar(); return new Token(Token.UNKNOWN, String.valueOf((char) tempChar));
            }
        }
    }
}