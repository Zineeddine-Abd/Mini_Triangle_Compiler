import java.io.IOException;

public class Parser {
    private Token currentToken;
    private Lexer scanner;

    public Parser(String filename) throws IOException {
        scanner = new Lexer(filename);
    }

    private void accept(byte expectedKind) throws IOException { // check the token before accepting it
        if (currentToken.kind == expectedKind) {
            currentToken = scanner.scan();
        } else {
            System.err.println("Syntax error: expected token kind " + expectedKind +
                    ", but found " + currentToken.kind +
                    " (" + currentToken.spelling + ")");
            throw new SyntaxError("Unexpected token");
        }
    }

    private void acceptIt() throws IOException { //accept the token anyway
        currentToken = scanner.scan();
    }

    // Main parsing method
    public void parse() throws IOException {
        currentToken = scanner.scan(); // Get the first token
        parseProgram();
        if (currentToken.kind != Token.EOT) {
            System.err.println("Syntax error: expected end of file, but found " +
                    currentToken.kind + " (" + currentToken.spelling + ")");
            throw new SyntaxError("Unexpected token after program end");
        }
        System.out.println("Parsing completed successfully.");
    }

    // Derivation 1 : Program ::= single-Command
    private void parseProgram() throws IOException {
        System.out.println("Parsing Program...");
        parseSingleCommand();
    }

    // Derivation 2 : Command ::= single-Command Command'
    private void parseCommand() throws IOException {
        System.out.println("Parsing Command...");
        parseSingleCommand();
        parseCommandPrime();
    }

    //Derivation 3 : Command' ::= ";" single-Command Command' | ε
    private void parseCommandPrime() throws IOException {
        System.out.println("Parsing Command'...");
        if (currentToken.kind == Token.SEMICOLON) {
            accept(Token.SEMICOLON);
            parseSingleCommand();
            parseCommandPrime();
        }
        // else ε
    }

    // single-Command ::= Identifier (":=" Expression | "(" Expression ")")
    //                  | "if" Expression "then" single-Command "else" single-Command
    //                  | "while" Expression "do" single-Command
    //                  | "let" Declaration "in" single-Command
    //                  | "begin" Command "end"
    private void parseSingleCommand() throws IOException {
        System.out.println("Parsing single-Command...");
        if (currentToken.kind == Token.IDENTIFIER) {
            accept(Token.IDENTIFIER);
            if (currentToken.kind == Token.ASSIGN) {
                accept(Token.ASSIGN);
                parseExpression();
            } else if (currentToken.kind == Token.LPAREN) {
                accept(Token.LPAREN);
                parseExpression();
                accept(Token.RPAREN);
            } else {
                System.err.println("Syntax error: expected ':=' or '(', found " +
                        currentToken.kind + " (" + currentToken.spelling + ")");
                throw new SyntaxError("Unexpected token after identifier");
            }
        } else if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("if")) {
            acceptIt(); // consume 'if'
            parseExpression();
            if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("then")) {
                acceptIt(); // consume 'then'
                parseSingleCommand();
                if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("else")) {
                    acceptIt(); // consume 'else'
                    parseSingleCommand();
                } else {
                    System.err.println("Syntax error: expected 'else' after then-clause");
                    throw new SyntaxError("Missing 'else' in if-statement");
                }
            } else {
                System.err.println("Syntax error: expected 'then' after if-condition");
                throw new SyntaxError("Missing 'then' in if-statement");
            }
        } else if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("while")) {
            acceptIt(); // consume 'while'
            parseExpression();
            if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("do")) {
                acceptIt(); // consume 'do'
                parseSingleCommand();
            } else {
                System.err.println("Syntax error: expected 'do' after while-condition");
                throw new SyntaxError("Missing 'do' in while-statement");
            }
        } else if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("let")) {
            acceptIt(); // consume 'let'
            parseDeclaration();
            if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("in")) {
                acceptIt(); // consume 'in'
                parseSingleCommand();
            } else {
                System.err.println("Syntax error: expected 'in' after declaration");
                throw new SyntaxError("Missing 'in' in let-statement");
            }
        } else if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("begin")) {
            acceptIt(); // consume 'begin'
            parseCommand();
            if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("end")) {
                acceptIt(); // consume 'end'
            } else {
                System.err.println("Syntax error: expected 'end' to close begin-block");
                throw new SyntaxError("Missing 'end' in begin-block");
            }
        } else {
            System.err.println("Syntax error: expected identifier or keyword, but found " +
                    currentToken.kind + " (" + currentToken.spelling + ")");
            throw new SyntaxError("Unexpected token in single-command");
        }
    }

    // Derivation 6 : Expression ::= primary-Expression Expresion'
    private void parseExpression() throws IOException {
        System.out.println("Parsing Expression...");
        parsePrimaryExpression();
        parseExpressionPrime();
    }

    //Derivation 7 : Expression' ::= Operator primary-Expression Expression' | ε
    private void parseExpressionPrime() throws IOException {
        System.out.println("Parsing Expression'...");
        if (isOperator(currentToken)) {
            acceptIt();
            parsePrimaryExpression();
            parseExpressionPrime();
        }
        // else ε
    }

    //Derivation 8 : primary-Expression ::= Integer-Literal | V-name | Operator primary-Expression | ( Expression )
    private void parsePrimaryExpression() throws IOException {
        System.out.println("Parsing primary-Expression...");
        switch (currentToken.kind) {
            case Token.INTLITERAL:
                acceptIt();
                break;
            case Token.IDENTIFIER:
                parseVname();
                break;
            case Token.PLUS:
            case Token.MINUS:
            case Token.MULT:
            case Token.DIV:
            case Token.LESS:
            case Token.GREATER:
            case Token.EQUAL:
                acceptIt();
                parsePrimaryExpression();
                break;
            case Token.LPAREN:
                accept(Token.LPAREN);
                parseExpression();
                accept(Token.RPAREN);
                break;
            default:
                System.err.println("Syntax error: unexpected token in primary expression: " +
                        currentToken.kind + " (" + currentToken.spelling + ")");
                throw new SyntaxError("Unexpected token in primary expression");
        }
    }

    //Derivation 9 : V-name ::= Identifier
    private void parseVname() throws IOException {
        System.out.println("Parsing V-name...");
        accept(Token.IDENTIFIER);
    }

    //Derivation 10 : Declaration ::= single-Declaration Declaration'
    private void parseDeclaration() throws IOException {
        System.out.println("Parsing Declaration...");
        parseSingleDeclaration();
        parseDeclarationPrime();
    }

    //Derivation 11 : Declaration' ::= ";" single-Declaration Declaration' | ε
    private void parseDeclarationPrime() throws IOException {
        System.out.println("Parsing Declaration'...");
        if (currentToken.kind == Token.SEMICOLON) {
            accept(Token.SEMICOLON);
            parseSingleDeclaration();
            parseDeclarationPrime();
        }
        // else ε
    }

    //Derivation 12 : single-Declaration ::= "const" Identifier "~" Expression | "var" Identifier ":" Type-denoter
    private void parseSingleDeclaration() throws IOException {
        System.out.println("Parsing single-Declaration...");
        if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("const")) {
            acceptIt();
            accept(Token.IDENTIFIER);
            accept(Token.TILDE);
            parseExpression();
        } else if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("var")) {
            acceptIt();
            accept(Token.IDENTIFIER);
            accept(Token.COLON);
            parseTypeDenoter();
        } else {
            System.err.println("Syntax error: expected 'const' or 'var', but found " +
                    currentToken.kind + " (" + currentToken.spelling + ")");
            throw new SyntaxError("Unexpected keyword in declaration");
        }
    }

    //Derivation 13 : Type-denoter ::= Identifier
    private void parseTypeDenoter() throws IOException {
        System.out.println("Parsing Type-denoter...");
        accept(Token.IDENTIFIER);
    }

   //method to check if a token is an operator
    private boolean isOperator(Token token) {
        byte kind = token.kind;
        return kind == Token.PLUS || kind == Token.MINUS || kind == Token.MULT ||
                kind == Token.DIV || kind == Token.LESS || kind == Token.GREATER ||
                kind == Token.EQUAL;
    }

    //Derivation 14 : Custom exception for syntax errors
    public static class SyntaxError extends RuntimeException {
        public SyntaxError(String message) {
            super(message);
        }
    }
}