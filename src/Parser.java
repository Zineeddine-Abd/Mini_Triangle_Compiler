import java.io.IOException;

public class Parser {
    private Token currentToken;
    private Lexer scanner;

    public Parser(String filename) throws IOException {
        scanner = new Lexer(filename);
    }

    private void accept(byte expectedKind) throws IOException {
        if (currentToken.kind == expectedKind) {
            currentToken = scanner.scan();
        } else {
            System.err.println("Syntax error: expected token kind " + expectedKind +
                    ", but found " + currentToken.kind +
                    " (" + currentToken.spelling + ")");
            throw new SyntaxError("Unexpected token");
        }
    }

    private void acceptIt() throws IOException {
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

    // Program ::= single-Command
    private void parseProgram() throws IOException {
        System.out.println("Parsing Program...");
        parseSingleCommand();
    }

    // Command ::= single-Command Command'
    private void parseCommand() throws IOException {
        System.out.println("Parsing Command...");
        parseSingleCommand();
        parseCommandPrime();
    }

    // Command' ::= ; single-Command Command' | ε
    private void parseCommandPrime() throws IOException {
        System.out.println("Parsing Command'...");
        if (currentToken.kind == Token.SEMICOLON) {
            accept(Token.SEMICOLON);
            parseSingleCommand();
            parseCommandPrime();
        }
        // else ε (do nothing)
    }

    // single-Command ::= V-name := Expression
    //                   | Identifier ( Expression )
    //                   | if Expression then single-Command else single-Command
    //                   | while Expression do single-Command
    //                   | let Declaration in single-Command
    //                   | begin Command end
    private void parseSingleCommand() throws IOException {
        System.out.println("Parsing single-Command...");

        switch (currentToken.kind) {
            case Token.IDENTIFIER:
                Token idToken = currentToken;
                acceptIt(); // Consume the identifier

                if (currentToken.kind == Token.ASSIGN) {
                    // V-name := Expression
                    acceptIt(); // Consume :=
                    parseExpression();
                } else if (currentToken.kind == Token.LPAREN) {
                    // Identifier ( Expression )
                    accept(Token.LPAREN);
                    parseExpression();
                    accept(Token.RPAREN);
                } else {
                    System.err.println("Syntax error: expected := or ( after identifier");
                    throw new SyntaxError("Unexpected token after identifier");
                }
                break;

            case Token.KEYWORD:
                if (currentToken.spelling.equals("if")) {
                    // if Expression then single-Command else single-Command
                    acceptIt(); // Consume 'if'
                    parseExpression();

                    if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("then")) {
                        acceptIt(); // Consume 'then'
                        parseSingleCommand();

                        if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("else")) {
                            acceptIt(); // Consume 'else'
                            parseSingleCommand();
                        } else {
                            System.err.println("Syntax error: expected 'else' after then-clause");
                            throw new SyntaxError("Missing 'else' in if-statement");
                        }
                    } else {
                        System.err.println("Syntax error: expected 'then' after if-condition");
                        throw new SyntaxError("Missing 'then' in if-statement");
                    }
                } else if (currentToken.spelling.equals("while")) {
                    // while Expression do single-Command
                    acceptIt(); // Consume 'while'
                    parseExpression();

                    if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("do")) {
                        acceptIt(); // Consume 'do'
                        parseSingleCommand();
                    } else {
                        System.err.println("Syntax error: expected 'do' after while-condition");
                        throw new SyntaxError("Missing 'do' in while-statement");
                    }
                } else if (currentToken.spelling.equals("let")) {
                    // let Declaration in single-Command
                    acceptIt(); // Consume 'let'
                    parseDeclaration();

                    if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("in")) {
                        acceptIt(); // Consume 'in'
                        parseSingleCommand();
                    } else {
                        System.err.println("Syntax error: expected 'in' after declaration");
                        throw new SyntaxError("Missing 'in' in let-statement");
                    }
                } else if (currentToken.spelling.equals("begin")) {
                    // begin Command end
                    acceptIt(); // Consume 'begin'
                    parseCommand();

                    if (currentToken.kind == Token.KEYWORD && currentToken.spelling.equals("end")) {
                        acceptIt(); // Consume 'end'
                    } else {
                        System.err.println("Syntax error: expected 'end' to close begin-block");
                        throw new SyntaxError("Missing 'end' in begin-block");
                    }
                } else {
                    System.err.println("Syntax error: unexpected keyword " + currentToken.spelling);
                    throw new SyntaxError("Unexpected keyword");
                }
                break;

            default:
                System.err.println("Syntax error: expected identifier or keyword, but found " +
                        currentToken.kind + " (" + currentToken.spelling + ")");
                throw new SyntaxError("Unexpected token in single-command");
        }
    }

    // V-name ::= Identifier
    private void parseVname() throws IOException {
        System.out.println("Parsing V-name...");
        accept(Token.IDENTIFIER);
    }

    // Expression ::= primary-Expression Expression'
    private void parseExpression() throws IOException {
        System.out.println("Parsing Expression...");
        parsePrimaryExpression();
        parseExpressionPrime();
    }

    // Expression' ::= Operator primary-Expression Expression' | ε
    private void parseExpressionPrime() throws IOException {
        System.out.println("Parsing Expression'...");
        if (isOperator(currentToken)) {
            // Operator primary-Expression Expression'
            acceptIt(); // Consume the operator
            parsePrimaryExpression();
            parseExpressionPrime();
        }
        // else ε (do nothing)
    }

    // primary-Expression ::= Integer-Literal
    //                      | V-name
    //                      | Operator primary-Expression
    //                      | ( Expression )
    private void parsePrimaryExpression() throws IOException {
        System.out.println("Parsing primary-Expression...");

        switch (currentToken.kind) {
            case Token.INTLITERAL:
                // Integer-Literal
                acceptIt();
                break;

            case Token.IDENTIFIER:
                // V-name
                parseVname();
                break;

            case Token.PLUS:
            case Token.MINUS:
            case Token.MULT:
            case Token.DIV:
            case Token.LESS:
            case Token.GREATER:
            case Token.EQUAL:
                // Operator primary-Expression
                acceptIt(); // Consume the operator
                parsePrimaryExpression();
                break;

            case Token.LPAREN:
                // ( Expression )
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

    // Declaration ::= single-Declaration Declaration'
    private void parseDeclaration() throws IOException {
        System.out.println("Parsing Declaration...");
        parseSingleDeclaration();
        parseDeclarationPrime();
    }

    // Declaration' ::= ; single-Declaration Declaration' | ε
    private void parseDeclarationPrime() throws IOException {
        System.out.println("Parsing Declaration'...");
        if (currentToken.kind == Token.SEMICOLON) {
            accept(Token.SEMICOLON);
            parseSingleDeclaration();
            parseDeclarationPrime();
        }
        // else ε (do nothing)
    }

    // single-Declaration ::= const Identifier ~ Expression
    //                      | var Identifier : Type-denoter
    private void parseSingleDeclaration() throws IOException {
        System.out.println("Parsing single-Declaration...");

        if (currentToken.kind == Token.KEYWORD) {
            if (currentToken.spelling.equals("const")) {
                // const Identifier ~ Expression
                acceptIt(); // Consume 'const'
                accept(Token.IDENTIFIER);
                accept(Token.TILDE);
                parseExpression();
            } else if (currentToken.spelling.equals("var")) {
                // var Identifier : Type-denoter
                acceptIt(); // Consume 'var'
                accept(Token.IDENTIFIER);
                accept(Token.COLON);
                parseTypeDenoter();
            } else {
                System.err.println("Syntax error: expected 'const' or 'var', but found " + currentToken.spelling);
                throw new SyntaxError("Unexpected keyword in declaration");
            }
        } else {
            System.err.println("Syntax error: expected keyword, but found " +
                    currentToken.kind + " (" + currentToken.spelling + ")");
            throw new SyntaxError("Unexpected token in declaration");
        }
    }

    // Type-denoter ::= Identifier
    private void parseTypeDenoter() throws IOException {
        System.out.println("Parsing Type-denoter...");
        accept(Token.IDENTIFIER);
    }

    // Helper method to check if a token is an operator
    private boolean isOperator(Token token) {
        byte kind = token.kind;
        return kind == Token.PLUS || kind == Token.MINUS || kind == Token.MULT ||
                kind == Token.DIV || kind == Token.LESS || kind == Token.GREATER ||
                kind == Token.EQUAL;
    }

    // Custom exception for syntax errors
    public static class SyntaxError extends RuntimeException {
        public SyntaxError(String message) {
            super(message);
        }
    }
}