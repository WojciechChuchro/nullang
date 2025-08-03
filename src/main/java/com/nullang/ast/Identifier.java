package com.nullang.ast;

import com.nullang.token.Token;

public class Identifier implements Expression {

    private final Token token;
    private final String value;

    public Identifier(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String tokenLiteral() {
        return token.literal;
    }

    @Override
    public Node expressionNode() {
        return null;
    }

    @Override
    public String toString() {
        return "Identifier [token=" + token + ", value=" + value + "]";
    }
}
