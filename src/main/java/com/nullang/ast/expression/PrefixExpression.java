package com.nullang.ast.expression;

import com.nullang.ast.Expression;
import com.nullang.token.Token;

public class PrefixExpression implements Expression {
    private final Token token;
    private final String operator;
    private Expression right;

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

    public void setRight(Expression right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return operator + right;
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }
}
