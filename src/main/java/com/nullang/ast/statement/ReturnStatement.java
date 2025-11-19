package com.nullang.ast.statement;


import com.nullang.ast.Expression;
import com.nullang.ast.Node;
import com.nullang.token.Token;

public class ReturnStatement implements Statement{
    private final Token token;
    private Expression returnValue;
    

    public Token getToken() {
        return token;
    }

    public Expression getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Expression returnValue) {
        this.returnValue = returnValue;
    }

    public ReturnStatement(Token token) {
        this.token = token;
    }

    @Override
    public Node StatementNode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String tokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return "ReturnStatement [token=" + token + ", returnValue=" + returnValue + "]";
    }
}
