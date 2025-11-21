package com.nullang.ast;

import com.nullang.token.Token;

public class BooleanIdentifier implements Expression{
    public final Token token;
    public boolean value;

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
}
