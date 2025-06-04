package dev.backendsouls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();

        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int line = 1;
    private int current = 0;
    private int start = 0;

    public Scanner(final String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {

        while (!this.isAtEnd()) {
            this.start = this.current;

            this.scanToken();
        }

        this.tokens.add(new Token(TokenType.EOF, "", null, this.line));

        return this.tokens;
    }

    private boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    private void scanToken() {
        char c = this.advance();

        switch (c) {
            case '(':
                this.addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                this.addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                this.addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                this.addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                this.addToken(TokenType.COMMA);
                break;
            case '.':
                this.addToken(TokenType.DOT);
                break;
            case '-':
                this.addToken(TokenType.MINUS);
                break;
            case '+':
                this.addToken(TokenType.PLUS);
                break;
            case ';':
                this.addToken(TokenType.SEMICOLON);
                break;
            case '*':
                this.addToken(TokenType.STAR);
                break;
            case '!':
                this.addToken(this.match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                this.addToken(this.match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                this.addToken(this.match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                this.addToken(this.match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (this.match('/')) {
                    while (this.peek() != '\n' && !this.isAtEnd()) {
                        this.advance();
                    }
                } else {
                    this.addToken(TokenType.SLASH);
                }

                break;
            case '"':
                this.string();
                break;
            case 'o':
                if (this.match('r')) {
                    this.addToken(TokenType.OR);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                this.line++;
                break;
            default:
                if (this.isDigit(c)) {
                    this.number();
                } else if (this.isAlpha(c)) {
                    this.identifier();
                } else {
                    Lox.error(this.line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (this.isAlphanumeric(this.peek())) {
            this.advance();
        }

        String text = this.source.substring(this.start, this.current);
        TokenType tokenType = keywords.get(text);

        if (tokenType == null) {
            tokenType = TokenType.IDENTIFIER;
        }

        this.addToken(tokenType);
    }

    private boolean isAlphanumeric(char c) {
        return this.isAlpha(c) || this.isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (this.isDigit(this.peek())) {
            this.advance();
        }

        if (this.peek() == '.' && this.isDigit(this.peekNext())) {
            this.advance();

            while (this.isDigit(this.peek())) {
                this.advance();
            }
        }

        this.addToken(TokenType.NUMBER, Double.parseDouble(this.source.substring(this.start, this.current)));
    }

    private char peekNext() {
        if (this.current + 1 >= this.source.length()) {
            return '\0';
        }

        return this.source.charAt(this.current + 1);
    }

    private void string() {
        while (this.peek() != '"' && !this.isAtEnd()) {
            this.advance();
        }

        if (this.isAtEnd()) {
            Lox.error(this.line, "Unterminated string.");
            return;
        }

        // closing string with "
        this.advance();

        // trim the surrounding quotes.
        var value = this.source.substring(this.start + 1, this.current - 1);

        this.addToken(TokenType.STRING, value);
    }

    private char peek() {
        if (this.isAtEnd()) {
            return '\0';
        }

        return this.source.charAt(this.current);
    }

    private boolean match(final char expected) {
        if (this.isAtEnd()) {
            return false;
        }

        if (this.source.charAt(this.current) != expected) {
            return false;
        }

        this.current++;

        return true;
    }

    private char advance() {
        return this.source.charAt(this.current++);
    }

    private void addToken(TokenType tokenType) {
        this.addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType, Object literal) {

        String text = this.source.substring(this.start, this.current);
        this.tokens.add(new Token(tokenType, text, literal, this.line));
    }
}
