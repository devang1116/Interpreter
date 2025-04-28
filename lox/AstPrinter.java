package lox;

class AstPrinter implements Expr.Visitor<String>{
    String print(Expr expr) {
        return expr.accept(this);
    }

    //TODO: Implement
    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize(expr.name.lexeme, expr);
    }

    // Call / Handle Literals
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
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
        return parenthesize(expr.name.lexeme, expr);
    }

    // Call / Handle Literals
    public String visitLiteralExpr(Expr.Literal expr) {
        if(expr.value == null)
            return "nil";
        return expr.value.toString();
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

    public static void main(String[] args) {
//        Expr expr = new Expr.Binary(new Expr.Unary( new Token(TokenType.MINUS, "-", null, 1),
//                                                    new Expr.Literal(123)),
//                                    new Token(TokenType.STAR, "*", null, 1),
//                                    new Expr.Grouping(new Expr.Literal(45.56)));
//
//
//        System.out.println(new AstPrinter().print(expr));
    }
}
