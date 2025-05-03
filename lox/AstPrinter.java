package lox;

class AstPrinter implements Expr.Visitor<String>{
    String print(Expr expr) {
        return expr.accept(this);
    }

    //TODO: Implement
    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize(expr.name.lexeme, expr.value);
    }

    // Call / Handle Literals
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return parenthesize("function", expr.callee);
    }

    // Call / Handle Literals
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    // Call / Handle Literals
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme , expr.right);
    }

    //TODO: Implement
    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    // Call / Handle Literals
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    // Format the expr and present it parenthesized
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for(Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }
}
