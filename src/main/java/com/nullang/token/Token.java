package com.nullang.token;

public class Token {
    public final TokenType type;
    public final String literal;

    @Override
    public String toString() {
        return literal;
    }

    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }
}
