package dev;

import java.util.*;
import java.util.logging.Logger;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    public static Logger logger = Logger.getLogger("Resolver");

    // Maintained to maintain hold of what funtion type we are in to maintain rules
    private enum FunctionType {
            FUNCTION,
        NONE,
        METHOD,
        INITIALIZER
    }
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
        logger.info("Inside Interpreter.");
    }


    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    // HELPER:
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    // HELPER: Should maintain scope and function metadata
    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    // HELPER: Should maintain scope resolve parameters and body
    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for(Token param: function.parameters) {
            declare(param);
            define(param);
        }

        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    // HELPER: Resolve the various blocks of if statement
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null)
            resolve(stmt.elseBranch);

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if(stmt.value != null) {
            if(currentFunction == FunctionType.INITIALIZER) {
                Dmp.error(stmt.keyword, "Cant return a value from an initializer");
            }
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }

        declare(stmt.name);
        define(stmt.name);
        return null;
    }

    // HELPER: To mark that a particular var is present inside
    private void define(Token name) {
        if (scopes.isEmpty())
            return;

        scopes.peek().put(name.lexeme, true);
    }

    // HELPER: To make sure that that whether we are inside a scope or in topmost scope
    private void declare(Token name) {
        if (scopes.isEmpty())
            return;

        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme))
            Dmp.error(name, "Already variable present in scope");
        scope.put(name.lexeme, false);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        declare(stmt.name);
        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init"))
                declaration = FunctionType.INITIALIZER;
            resolveFunction(method, declaration);
        }

        define(stmt.name);
        endScope();
        return null;
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement: statements) {
            resolve(statement);
        }
    }

    void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    void resolve(Expr expr) {
        expr.accept(this);
    }

    // HELPER: Keeps track of the stack of scopes currently in scope
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    // HELPER: Pop the scope off the stack when done
    private void endScope() {
        scopes.pop();
    }

    // HELPER: Resolve reference variables and resolve the variable scope assigned
    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
       resolve(expr.value);
       resolveLocal(expr, expr.name);
       return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument: expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    // HELPER: Checks if variable accessed is in current scope or is declared but not initialised
    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty()) {
            Map<String, Boolean> scope = scopes.peek();
            if (scope.containsKey(expr.name.lexeme) && scope.get(expr.name.lexeme) == Boolean.FALSE) {
                Dmp.error(expr.name, "Can't read local variable in its own initializer");
            }
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        resolveLocal(expr, expr.keyword);
        return null;
    }

    // HELPER: Helps resolve the variable initialisations
    private void resolveLocal(Expr expr, Token name) {
        for(int i = scopes.size() - 1 ; i >= 0 ; i--) {
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return ;
            }
        }
    }
}
