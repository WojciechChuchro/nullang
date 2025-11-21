package com.nullang.ast.statement;


import com.nullang.ast.Expression;
import com.nullang.token.Token;

public class ReturnStatement implements Statement {
    private final Token token;
    private Expression returnValue;

    public ReturnStatement(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Expression returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return "ReturnStatement [token=" + token + ", returnValue=" + returnValue + "]";
    }
}
