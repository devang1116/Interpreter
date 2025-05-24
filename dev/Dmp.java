package dev;

import java.io.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class Dmp {
    public static boolean hadError = false;
    public static boolean hadRuntimeError = false;
    public static Logger logger = Logger.getLogger("Base");
    public static final Interpreter interpreter = new Interpreter();
    public static final Resolver resolver = new Resolver(interpreter);

    public static void main(String[] args) throws IOException {
        logger.info("Inside Base Level");
        if (args.length > 1) {
            System.out.println("Usage lox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException {
        logger.info("Inside runPrompt method.");
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(isr);

        for (; ; ) {
            System.out.println(">");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
        }
    }

    // IMPLEMENT: Entry point
    private static void runFile(String path) throws IOException {
        logger.info("Inside runFile method.");
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    // IMPLEMENT: Entry point
    private static void run(String source) {
        logger.info("Inside run method.");
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);

        resolver.resolve(statements);

        interpreter.interpret(statements);
//        logger.info(new AstPrinter().print(statements));
    }

    // ERROR: Prints the location for the error
    static private void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    // ERROR: Handling
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    // ERROR: Handling
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}