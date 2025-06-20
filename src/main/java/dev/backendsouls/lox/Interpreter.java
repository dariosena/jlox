package dev.backendsouls.lox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment environment = new Environment();

    public void interpret(List<Stmt> statements) {
        try {
            for (var statement : statements) {
                this.execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        var value = this.evaluate(expr.value());
        this.environment.assign(expr.name(), value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var right = this.evaluate(expr.right());
        var left = this.evaluate(expr.left());

        return switch (expr.operator().tokenType()) {
            case TokenType.BANG_EQUAL -> !this.isEqual(left, right);
            case TokenType.EQUAL_EQUAL -> this.isEqual(left, right);
            case TokenType.GREATER -> {
                this.checkNumberOperands(expr.operator(), left, right);
                yield (double) left > (double) right;
            }
            case TokenType.GREATER_EQUAL -> {
                this.checkNumberOperands(expr.operator(), left, right);
                yield (double) left >= (double) right;
            }
            case TokenType.LESS -> {
                this.checkNumberOperands(expr.operator(), left, right);
                yield (double) left < (double) right;
            }
            case TokenType.LESS_EQUAL -> {
                this.checkNumberOperands(expr.operator(), left, right);
                yield (double) left <= (double) right;
            }
            case TokenType.MINUS -> {
                this.checkNumberOperands(expr.operator(), left, right);
                yield (double) left - (double) right;
            }
            case TokenType.SLASH -> {
                this.checkNumberOperands(expr.operator(), left, right);
                yield (double) left / (double) right;
            }
            case TokenType.STAR -> {
                this.checkNumberOperands(expr.operator(), left, right);
                yield (double) left * (double) right;
            }
            case TokenType.PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    yield left + (String) right;
                }

                throw new RuntimeError(expr.operator(), "Operands must be two numbers or two strings.");
            }
            default -> null;
        };
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return this.evaluate(expr.expression());
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value();
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        var left = this.evaluate(expr.left());

        if (expr.operator().tokenType() == TokenType.OR) {
            if (this.isTruthy(left)) {
                return left;
            }
        } else {
            if (!this.isTruthy(left)) {
                return left;
            }
        }

        return this.evaluate(expr.right());
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        var right = this.evaluate(expr.right());

        return switch (expr.operator().tokenType()) {
            case TokenType.BANG -> !this.isTruthy(right);
            case TokenType.MINUS -> {
                this.checkNumberOperand(expr.operator(), right);
                yield -(double) right;
            }
            default -> null;
        };

    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return this.environment.get(expr.name());
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }

        if (object instanceof Boolean) {
            return (boolean) object;
        }

        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }

        if (left == null) {
            return false;
        }

        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }

        throw new RuntimeError(operator, "Operands must be a numbers.");
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            var text = object.toString();

            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        this.executeBlock(stmt.statements(), new Environment(this.environment));
        return null;
    }

    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previousEnvironment = this.environment;

        try {
            this.environment = environment;

            for (var statement : statements) {
                this.execute(statement);
            }
        } finally {
            this.environment = previousEnvironment;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        this.evaluate(stmt.expression());
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (this.isTruthy(this.evaluate(stmt.condition()))) {
            this.execute(stmt.thenBranch());
        } else if (stmt.elseBranch() != null) {
            this.execute(stmt.elseBranch());
        }

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        var value = this.evaluate(stmt.expression());
        System.out.println(this.stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;

        if (stmt.initializer() != null) {
            value = this.evaluate(stmt.initializer());
        }

        this.environment.define(stmt.name().lexeme(), value);

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (this.isTruthy(this.evaluate(stmt.condition()))) {
            this.execute(stmt.body());
        }

        return null;
    }
}
