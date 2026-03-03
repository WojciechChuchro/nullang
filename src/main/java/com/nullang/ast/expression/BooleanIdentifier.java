package com.nullang.ast.expression;

import com.nullang.token.Token;

public class BooleanIdentifier implements Expression {
    private final Token token;
    private final boolean value;

    public BooleanIdentifier(boolean value, Token token) {
        this.value = value;
        this.token = token;
    }

    @Override
    public String getTokenLiteral() {
        return token.toString();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public boolean getValue() {
        return value;
    }
}
