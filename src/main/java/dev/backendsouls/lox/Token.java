package dev.backendsouls.lox;

public record Token(TokenType tokenType, String lexeme, Object literal, int line) {
    @Override
    public String toString() {
        return this.tokenType + " " + this.lexeme + " " + this.literal;
    }
}
