package dev;

import dev.TokenType.*;
import dev.Expr.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{

    private Environment environment = new Environment();
    final Environment globals = new Environment();
    public static Logger logger = Logger.getLogger("Interpreter");
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", new Callable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis();
            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    // IMPLEMENT: Interpreter entry point
    void interpret(List<Stmt> statements) {
        try {
            logger.info("Inside Interpreter.");

            for (Stmt statement: statements)
                execute(statement);

        } catch (RuntimeError error) {
            Dmp.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR)
            if(isTruthy(left) == true)
                return true;
        else
            if(!isTruthy(left) == true)
                return true;

        return evaluate(expr.right);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                return -(double) right;
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name , environment);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case PLUS:
                if (((Expr.Literal) left).value instanceof Double && ((Expr.Literal) right).value instanceof Double)
                    return ((((double) ((Expr.Literal) left).value)) + ((double) ((Expr.Literal) right).value));
                if (((Expr.Literal) left).value instanceof String && ((Expr.Literal) right).value instanceof String)
                    return ((((String) ((Expr.Literal) left).value)) + ((String) ((Expr.Literal) right).value));

                throw new RuntimeError(expr.operator, "Operands must be of the same type.");
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument: expr.arguments) {
            arguments.add(argument);
        }

        if(!(callee instanceof Callable)) {
            return new RuntimeError(expr.paren, "Can only run functions and classes.");
        }

        Callable function = (Callable) callee;
        if(arguments.size() != function.arity()) {
            return new RuntimeError(expr.paren, "Expected function arguments : "+ function.arity() +" but received " + expr.arguments);
        }
        return function.call(this, arguments);
    }


    // HELPER: Executes and evaluates statements
    private void execute(Stmt statement) {
        statement.accept(this);
    }

    // HELPER: Stringifies the object to present to the user
    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);

            return text;
        }
        if(object instanceof String) {
            return object.toString();
        }

        if (object instanceof Object) {
            return (((Expr.Literal) object).value).toString();
        }

        return object.toString();
    }

    // HELPER: Returns and checks if the operands are of the same type
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be a number");
    }

    // HELPER: Throws Exception if one of the operand is not type Number
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;

        throw new RuntimeError(operator, "Operand must be a number");
    }

    // HELPER: Returns evualation of left right equality
    private boolean isEqual(Object left , Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;

        return left.equals(right);
    }

    // HELPER: Returns null & false as false and rest as true
    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    // HELPER: Call to Visitor's implementation to evaluate
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    // DECLARE: Executes statment considering the state
    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;

        try {
            this.environment = environment;
            for (Stmt statement: statements)
                execute(statement);
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        Function function = new Function(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Object object = evaluate(stmt.condition);

        if (isTruthy(evaluate(stmt.condition)) == true)
            execute(stmt.thenBranch);
        else if(stmt.elseBranch != null)
            execute(stmt.elseBranch);

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object object = stmt.expression;
        Object obj = evaluate(stmt.expression);
        logger.info("Statement : " + stringify(obj));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new Return(value);
    }

    // HELPER: Helps with throwing Exception when return is called wrongly
    class Return extends RuntimeException {
        final Object value;

        Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }

    // HELPER: Checks if initializer statement and then uses environment to define variables
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null)
            value = evaluate(stmt.initializer);

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }

        return null;
    }
}
