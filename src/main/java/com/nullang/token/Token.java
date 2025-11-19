package com.nullang.token;

public record Token(TokenType type, String literal) {
    @Override
    public String toString() {
        return literal;
    }
}
