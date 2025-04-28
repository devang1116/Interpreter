package lox;

import java.util.*;
import java.util.logging.Logger;

import lox.TokenType.*;

class Parser {
    private static class ParserError extends RuntimeException { }

    public static Logger logger = Logger.getLogger("Parser");
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // IMPLEMENT: Parser Entry point
    List<Stmt> parse() {
        try {
            logger.info("Inside Parser.");
            List<Stmt> statements = new ArrayList<>();
            while(!isAtEnd())
                statements.add(declaration());

            return statements;
        } catch (ParserError error) {
            return null;
        }
    }

    // HELPER: Handles variable declartion or returns the expr statement
    private Stmt declaration() {
        try {
            if(match(TokenType.VAR))
                return varDeclaration();

            return statement();

        } catch (ParserError e) {
            synchronize();
            return null;
        }
    }

    // HELPER: Handles variable declaration parsing
    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        Expr initializer = null;

        if (match(TokenType.EQUAL))
            initializer = expression();

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration");
        return new Stmt.Var(name, initializer);
    }

    // HELPER: Returns and matches statement conditions
    private Stmt statement() {
        if (match(TokenType.PRINT))
            return printStatement();
        if (match(TokenType.LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    // HELPER:
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' afte block");
        return statements;
    }

    // HELPER: Prints statement and returns it too
    private Stmt printStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value");
        return new Stmt.Print(expr);
    }

    // HELPER: Reads and returns the expression statement
    private Stmt expressionStatement() {
        Expr expr =  expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value");
        return new Stmt.Expression(expr);
    }

    // DECLARE: Parser logic for an expression to be evaluated
    private Expr expression() {
        return assignment();
    }

    // HELPER: Evaluates the assignment operator to fetch and set variable declaration
    private Expr assignment() {
        Expr expr = equality();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    // HELPER: Iterates over the tokens passed and checks if valid Token
    private boolean match(TokenType... types) {
        for(TokenType type: types) {
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    // HELPER: Returns whether the current Token type matches or is included in the List of tokens
    private boolean check(TokenType type) {
        if(isAtEnd())
            return false;
        return peek().type == type;
    }

    // HELPER: Advances the current pointer
    private Token advance() {
        if(!isAtEnd())
            current++;
        return previous();
    }

    // HELPER: Returns if current index/token is the end of file
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    // HELPER: Returns the current token
    private Token peek() {
        return tokens.get(current);
    }

    // HELPER: Gets previous Token
    private Token previous() {
        return tokens.get(current - 1);
    }

    // DECLARE: Controls flow if statement is not variable declaration
    private Expr equality() {
        Expr expr = comparison();

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL))  {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // DECLARE: Controls flow if expr is not equality
    private Expr comparison() {
        Expr expr = term();

        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // DECLARE: Controls flow if expr is not comparison
    private Expr term() {
        Expr expr = factor();

        while(match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // DECLARE: Controls flow if expr is not add minus ops
    private Expr factor() {
        Expr expr = unary();

        while(match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // DECLARE: Controls flow if expr is not multiply divide ops
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

        if (match(TokenType.IDENTIFIER))
            return new Expr.Variable(previous());

        if(match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression");
    }

    // HELPER: Ending a parenthesized expression
    private Token consume(TokenType type, String message) {
        if(check(type))
            return advance();

        throw error(peek(), message);
    }

    // ERROR: Erro Handling method
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
