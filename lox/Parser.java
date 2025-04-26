package lox;

import java.util.*;
import lox.TokenType.*;

class Parser {
    private static class ParserError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // DECLARE: Intial point of Parsing
    Expr parse() {
        try {
            return expression();
        } catch (ParserError error) {
            return null;
        }
    }

    // DECLARE: Parser logic for an expression to be evaluated
    private Expr expression() {
        Expr expr = comparison();

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL))  {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // DECLARE: Iterates over the tokens passed and checks if valid Token
    private boolean match(TokenType... types) {
        for(TokenType type: types) {
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    // DECLARE: Returns whether the current Token type matches or is included in the List of tokens
    private boolean check(TokenType type) {
        if(isAtEnd())
            return false;
        return peek().type == type;
    }

    // DECLARE: Advances the current pointer
    private Token advance() {
        if(!isAtEnd())
            current++;
        return previous();
    }

    // DECLARE: Returns if current index/token is the end of file
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    // DECLARE: Returns the current token
    private Token peek() {
        return tokens.get(current);
    }

    // DECLARE: Gets previous Token
    private Token previous() {
        return tokens.get(current - 1);
    }

    // DECLARE: Performs comparsion parsing
    private Expr comparison() {
        Expr expr = term();

        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // DECLARE: Returns and processes Addition and Subtraction Expr precedence
    private Expr term() {
        Expr expr = factor();

        while(match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // DECLARE: Returns and processes Multiplication and Division Expr precedence
    private Expr factor() {
        Expr expr = unary();

        while(match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // DECLARE: Returns an unary expression
    private Expr unary() {
        if(match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    // DECLARE: Highest level of precedence, processes a primary expression
    private Expr primary() {
        if(match(TokenType.FALSE))
            return new Expr.Literal(false);
        if(match(TokenType.TRUE))
            return new Expr.Literal(true);
        if(match(TokenType.NIL))
            return new Expr.Literal(null);

        if(match(TokenType.NUMBER, TokenType.STRING))
            return new Expr.Literal(previous().literal);

        if(match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression");
    }

    // DECLARE: Ending a parenthesized expression
    private Token consume(TokenType type, String message) {
        if(check(type))
            return advance();

        throw error(peek(), message);
    }

    // DECLARE: Error Handling method
    private ParserError error(Token token, String message) {
        Lox.error(token, message);
        return new ParserError();
    }

    // TODO: Higher logic to be implemented
    private void synchronize() {
        advance();

        while(!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            return;
        }
    }

}
