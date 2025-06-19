package dev.backendsouls.lox;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!this.isAtEnd()) {
            statements.add(this.declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (this.match(TokenType.VAR)) {
                return this.varDeclaration();
            }

            return this.statement();
        } catch (ParseError error) {
            this.synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Expr initializer = null;
        Token name = this.consume(TokenType.IDENTIFIER, "Expect variable name.");

        if (this.match(TokenType.EQUAL)) {
            initializer = this.expression();
        }

        this.consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (this.match(TokenType.IF)) {
            return this.ifStatement();
        }
        if (this.match(TokenType.PRINT)) {
            return this.printStatement();
        }

        // Block Stmt
        if (this.match(TokenType.LEFT_BRACE)) {
            return new Stmt.Block(this.block());
        }

        return this.expressionStatement();
    }

    private Stmt ifStatement() {
        this.consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        var condition = this.expression();
        this.consume(TokenType.LEFT_PAREN, "Expect ')' after 'if' condition.");

        var thenBranch = this.statement();

        Stmt elseBranch = null;
        if (this.match(TokenType.ELSE)) {
            elseBranch = this.statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private List<Stmt> block() {
        var statements = new ArrayList<Stmt>();

        while (!this.check(TokenType.RIGHT_BRACE) && !this.isAtEnd()) {
            statements.add(this.declaration());
        }

        this.consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");

        return statements;
    }

    private Stmt printStatement() {
        var value = this.expression();
        this.consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        var expr = this.expression();
        this.consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return this.assignment();
    }

    private Expr assignment() {
        var expr = this.or();

        if (this.match(TokenType.EQUAL)) {
            var equals = this.previous();
            var value = this.assignment();

            if (expr instanceof Expr.Variable) {
                var name = ((Expr.Variable) expr).name();
                return new Expr.Assign(name, value);
            }

            this.error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        var expr = this.and();

        while (this.match(TokenType.OR)) {
            var operator = this.previous();
            var right = this.and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        var expr = this.equality();

        while (this.match(TokenType.AND)) {
            var operator = this.previous();
            var right = this.equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        var expr = this.comparison();

        while (this.match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            var operator = this.previous();
            expr = this.getNextBinaryOperation(expr, operator, this.comparison());
        }

        return expr;
    }

    private Expr comparison() {
        var expr = this.term();

        while (this.match(
                TokenType.GREATER,
                TokenType.GREATER_EQUAL,
                TokenType.LESS,
                TokenType.LESS_EQUAL
        )) {
            var operator = this.previous();
            expr = this.getNextBinaryOperation(expr, operator, this.term());
        }

        return expr;
    }

    private Expr term() {
        var expr = this.factor();

        while (this.match(TokenType.MINUS, TokenType.PLUS)) {
            var operator = this.previous();
            expr = this.getNextBinaryOperation(expr, operator, this.factor());
        }

        return expr;
    }

    private Expr factor() {
        var expr = this.unary();

        while (this.match(TokenType.SLASH, TokenType.STAR)) {
            var operator = this.previous();
            expr = this.getNextBinaryOperation(expr, operator, this.unary());
        }

        return expr;
    }

    private Expr unary() {
        if (this.match(TokenType.BANG, TokenType.MINUS)) {
            var operator = this.previous();
            return new Expr.Unary(operator, this.unary());
        }

        return this.primary();
    }

    private Expr getNextBinaryOperation(Expr leftExpr, Token operator, Expr rightExpr) {
        return new Expr.Binary(leftExpr, operator, rightExpr);
    }

    private Expr primary() {
        if (this.match(TokenType.FALSE)) {
            return new Expr.Literal(false);
        }

        if (this.match(TokenType.TRUE)) {
            return new Expr.Literal(true);
        }

        if (this.match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }

        if (this.match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(this.previous().literal());
        }

        if (this.match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(this.previous());
        }

        if (this.match(TokenType.LEFT_PAREN)) {
            var expr = this.expression();
            this.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw this.error(this.peek(), "Expect expression.");
    }

    private void synchronize() {
        this.advance();

        while (!this.isAtEnd()) {
            if (this.previous().tokenType() == TokenType.SEMICOLON) {
                return;
            }

            switch (this.peek().tokenType()) {
                case TokenType.CLASS:
                case TokenType.FUN:
                case TokenType.VAR:
                case TokenType.FOR:
                case TokenType.IF:
                case TokenType.WHILE:
                case TokenType.PRINT:
                case TokenType.RETURN:
                    return;
            }
        }

        this.advance();
    }

    /**
     * Auxiliary Methods
     */

    private Token consume(TokenType tokenType, String message) {
        if (this.check(tokenType)) {
            return this.advance();
        }

        throw this.error(this.peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private boolean match(TokenType... tokenTypes) {
        for (var tokenType : tokenTypes) {
            if (this.check(tokenType)) {
                this.advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType tokenType) {
        if (this.isAtEnd()) {
            return false;
        }

        return this.peek().tokenType() == tokenType;
    }

    private boolean isAtEnd() {
        return this.peek().tokenType() == TokenType.EOF;
    }

    private Token peek() {
        return this.tokens.get(this.current);
    }

    private Token previous() {
        return this.tokens.get(this.current - 1);
    }

    private Token advance() {
        if (!this.isAtEnd()) {
            this.current++;
        }

        return this.previous();
    }

    private static class ParseError extends RuntimeException {
    }
}
