package dev.backendsouls.lox.tool;

import dev.backendsouls.lox.Expr;
import dev.backendsouls.lox.Token;
import dev.backendsouls.lox.TokenType;

public class AstPrinter implements Expr.Visitor<String> {
    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }

    String print(Expr expr) {
        return expr.accept(this);
    }

    private String parenthesize(String name, Expr... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expression : expressions) {
            builder.append(" ");
            builder.append(expression.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return this.parenthesize(expr.operator().lexeme(), expr.left(), expr.right());
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return this.parenthesize("group", expr.expression());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value() == null) {
            return "nil";
        }

        return expr.value().toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return this.parenthesize(expr.operator().lexeme(), expr.right());
    }
}
