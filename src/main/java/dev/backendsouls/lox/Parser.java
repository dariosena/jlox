package dev.backendsouls.lox;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    private Expr expression() {
        return this.equality();
    }

    private Expr equality() {
        var expr = this.comparison();

        while (this.match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            expr = this.getNextBinaryOperation(expr, this.comparison());
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
            expr = this.getNextBinaryOperation(expr, this.term());
        }

        return expr;
    }

    private Expr term() {
        var expr = this.factor();

        while (this.match(TokenType.MINUS, TokenType.PLUS)) {
            expr = this.getNextBinaryOperation(expr, this.factor());
        }

        return expr;
    }

    private Expr factor() {
        var expr = this.unary();

        while (this.match(TokenType.SLASH, TokenType.STAR)) {
            expr = this.getNextBinaryOperation(expr, this.unary());
        }

        return expr;
    }

    private Expr unary() {
        if (this.match(TokenType.BANG, TokenType.MINUS)) {
            return new Expr.Unary(this.previous(), this.unary());
        }

        return this.primary();
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

        // if (match(LEFT_PAREN)) { .. }
        var expr = this.expression();
        this.consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
        return new Expr.Grouping(expr);
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

    private ParserError error(Token token, String message) {
        Lox.error(token, message);
        return new ParserError();
    }

    private Expr getNextBinaryOperation(Expr leftExpr, Expr rightExpr) {
        return new Expr.Binary(leftExpr, this.previous(), rightExpr);
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

    private static class ParserError extends RuntimeException {
    }
}
