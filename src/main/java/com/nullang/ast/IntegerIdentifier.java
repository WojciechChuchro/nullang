package com.nullang.ast;

import com.nullang.token.Token;

public class IntegerIdentifier implements Expression {

    private final Token token;
    private final Integer value;

    public IntegerIdentifier(Token token, Integer value) {
        this.token = token;
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public Node expressionNode() {
        return null;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
