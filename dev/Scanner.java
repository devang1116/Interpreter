package dev;

import java.util.*;
import java.util.logging.Logger;

//import static dev.TokenType.*;
import dev.Token.*;


class Scanner {
    private final List<Token> tokens = new ArrayList<>();
    private final String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    public static Logger logger = Logger.getLogger("Scanner");
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    TokenType.AND);
        keywords.put("class",  TokenType.CLASS);
        keywords.put("else",   TokenType.ELSE);
        keywords.put("false",  TokenType.FALSE);
        keywords.put("for",    TokenType.FOR);
        keywords.put("fun",    TokenType.FUN);
        keywords.put("if",     TokenType.IF);
        keywords.put("nil",    TokenType.NIL);
        keywords.put("or",     TokenType.OR);
        keywords.put("print",  TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super",  TokenType.SUPER);
        keywords.put("this",   TokenType.THIS);
        keywords.put("true",   TokenType.TRUE);
        keywords.put("var",    TokenType.VAR);
        keywords.put("while",  TokenType.WHILE);
    }

    // Constructor
    Scanner(String source) {
        this.source = source;
    }

    // Scans tokens passed and read through the file
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    // Method to check if reached at end during reading code
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Method to read individual token identifier
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            // Handling for Division and comments to be done
            case '/':
                addToken(TokenType.SLASH);
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c))
                    number(c);
                else if (isAlpha(c)) {
                    identifier();
                } else
                    logger.warning("Unidentified charecter");

        }
    }

    // HELPER: Checks whether the current charecter could be a identifier or a reserved word
    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = TokenType.IDENTIFIER;

        addToken(type);
    }

    // HELPER: Checks whether the current char is number or alphabet
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    // HELPER: Returns whether the current charecter is an alphabet
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    // HELPER: Checks whether we have encountered a number
    private void number(char c) {
        while (isDigit(peek()))
            advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek()))
                advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // HELPER: Returns te next charecter if available
    private char peekNext() {
        if(current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }

    // HELPER: Checks if the charecter is and digit
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // HELPER: Generates string token whenever a string is encountered
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if(isAtEnd()) {
            logger.info("Error: Unterminated string");
            System.exit(64);
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    // HELPER: Method to access a particular char at an index in the source string without changing the index
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    // HELPER: Method to check if current charecter and passed argument is same
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }


    // HELPER: Passes it to the next addToken overloaded method
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // HELPER: Scans the current token and adds it to the list
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // HELPER: Method to get the current token and move ahead
    private char advance() {
        return source.charAt(current++);
    }
}
