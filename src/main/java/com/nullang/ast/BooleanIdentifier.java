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
    public String tokenLiteral() {
        return token.toString();
    }

    @Override
    public Node expressionNode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expressionNode'");
    }

    
}
