package lox;

import java.io.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class Lox {
    public static boolean hadError = false;
    public static Logger logger = Logger.getLogger("TopLevel");

    public static void main(String[] args) throws IOException {
        logger.info("Inside Log Interpreter");
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

    private static void runFile(String path) throws IOException {
        logger.info("Inside runFile method.");
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void run(String source) {
        logger.info("Inside run method.");
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static private void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}