package com.nullang.ast.expression;

import com.nullang.ast.Expression;
import com.nullang.token.Token;

public class InfixExpression implements Expression {
    private final Token token;
    private final String operator;
    private Expression left;
    private Expression right;

    public InfixExpression(Token token, String operator) {
        this.token = token;
        this.operator = operator;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public void setRight(Expression right) {
        this.right = right;
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
        return String.format("(%s %s %s)", left, operator, right);
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }
}
