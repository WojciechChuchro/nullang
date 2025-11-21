package com.nullang.ast.statement;

import com.nullang.ast.Expression;
import com.nullang.token.Token;

public record ExpressionStatement(
        Token token,
        Expression expression) implements Statement {

    @Override
    public String toString() {
        return expression.toString();
    }

    @Override
    public String getTokenLiteral() {
        return expression.getTokenLiteral();
    }
}
