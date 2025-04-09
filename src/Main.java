import java.io.IOException;

public class Main {
    // Main method for testing
    public static void main(String[] args) {
        try {
            String filePath = "src/factorial.txt";
            Parser parser = new Parser(filePath);
            parser.parse();
        } catch (IOException e) {
            System.err.println("I/O Error: " + e.getMessage());
        } catch (Parser.SyntaxError e) {
            System.err.println("Syntax Error: " + e.getMessage());
        }
    }
}