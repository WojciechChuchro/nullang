package com.nullang.ast.expression;

import com.nullang.ast.Expression;
import com.nullang.ast.Node;
import com.nullang.token.Token;

public class PrefixExpression implements Expression{
    private final Token token;
    private final String operator;
    private Expression right;

    public void setRight(Expression right) {
        this.right = right;
    }

    public PrefixExpression(Token token, String operator) {
        this.token = token;
        this.operator = operator;
    }

    public Token getToken() {
        return token;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "PrefixExpression [token=" + token + ", operator=" + operator + ", right=" + right + "]";
    }

    @Override
    public String tokenLiteral() {
        return token.literal;
    }

    @Override
    public Node expressionNode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expressionNode'");
    }

    
}
