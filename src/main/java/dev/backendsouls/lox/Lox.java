package dev.backendsouls.lox;

import dev.backendsouls.lox.tool.AstPrinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.println("> ");
            String line = reader.readLine();

            if (line == null) {
                break;
            }

            run(line);
            Lox.hadError = false;
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (Lox.hadError) {
            System.exit(65);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if (Lox.hadError) {
            return;
        }

        System.out.println(new AstPrinter().print(expression));
    }

    static void error(final int line, final String message) {
        report(line, "", message);
    }

    static void error(final Token token, final String message) {
        if (token.tokenType() == TokenType.EOF) {
            report(token.line(), " at end", message);
            return;
        }

        report(token.line(), " at '" + token.lexeme() + "'", message);
    }

    private static void report(final int line, final String where, final String message) {
        System.err.println("[line " + line + "] Error " + where + ": " + message);

        Lox.hadError = true;
    }
}
