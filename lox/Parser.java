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
            if(match(TokenType.FUN))
                return function("function");
            if(match(TokenType.VAR))
                return varDeclaration();

            return statement();

        } catch (ParserError e) {
            synchronize();
            return null;
        }
    }

    // DECLARE:
    private Stmt.Function function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + "name.");
        consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name.");

        List<Token> parameters = new ArrayList<>();
        if(!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size() > 255)
                    error(peek(), "Cant have more than 255 parameters.");
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while(check(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> block = block();
        return new Stmt.Function(name, parameters, block);
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
        if(match(TokenType.IF))
            return ifStatement();
        if (match(TokenType.PRINT))
            return printStatement();
        if (match(TokenType.RETURN))
            return returnStatement();
        if (match(TokenType.WHILE))
            return whileStatement();
        if (match(TokenType.FOR))
            return forStatement();
        if (match(TokenType.LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    // HELPER:
    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after return value");
        return new Stmt.Return(keyword, value);
    }

    // HELPER: Handles logic for a for loop
    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'");

        Stmt initialiser;
        if (match(TokenType.SEMICOLON))
            initialiser = null;
        else if (match(TokenType.VAR))
            initialiser = varDeclaration();
        else
            initialiser = expressionStatement();

        Expr condition = null;
        if (!check(TokenType.SEMICOLON))
            condition = expression();

        consume(TokenType.SEMICOLON, "Expect ';' after loop condition");

        Expr increment = null;
        if (!check(TokenType.SEMICOLON))
            increment = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after loop condition");

        Stmt body = statement();
        if (increment != null)
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        if (condition == null)
            condition = new Expr.Literal(true);

        body = new Stmt.While(condition, body);

        if (initialiser != null)
            body = new Stmt.Block(Arrays.asList(initialiser, body));

        return body;
    }

    // HELPER: Handles the logic when encountered a while statement // TODO: ++ operator not working not getting scope/ enclosing
    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after expression");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    // HELPER: Handles the logic when encountered an If statement
    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after this.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(TokenType.ELSE))
            elseBranch = statement();

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // HELPER: Handles logic when a block of statements or { is encountered up till }
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block");
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
        Expr expr = or();

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

    // HELPER: TODO
    private Expr or() {
        Expr expr = and();

        while(match(TokenType.OR)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while(match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
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

        return call();
    }

    // DECLARE:  Handles logic when a function call is begin encountered TODO:
    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN))
                expr = finishCall(expr);
            else
                break;
        }

        return expr;
    }

    // DECLARE : Handles logic when and function call is encountered
    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() > 255)
                    error(peek(), "Cant have more than 255 arguments");
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments");
        return new Expr.Call(callee, paren, arguments);
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
