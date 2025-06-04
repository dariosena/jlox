package dev.backendsouls;

public class Token {
    final TokenType tokenType;
    final String lexeme;
    final Object literal;
    final int line;

    public Token(TokenType tokenType, String lexeme, Object literal, int line) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return this.tokenType + " " + this.lexeme + " " + this.literal;
    }
}
