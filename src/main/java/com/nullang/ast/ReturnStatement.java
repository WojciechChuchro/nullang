
package com.nullang.ast;

import java.beans.Expression;

import com.nullang.token.Token;

public class ReturnStatement implements Statement{
    private final Token token;
    // TODO: add expression 
    private Expression returnValue;
    

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
        return token.literal;
    }

    
}
