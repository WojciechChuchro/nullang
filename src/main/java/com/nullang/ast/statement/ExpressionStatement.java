package com.nullang.ast.statement;

import com.nullang.ast.Expression;
import com.nullang.ast.Node;
import com.nullang.token.Token;

public class ExpressionStatement implements Statement{
    private final Token token;
    private final Expression expression;

    public ExpressionStatement(Token token, Expression expression) {
        this.token = token;
        this.expression = expression;
    }

    public Token getToken() {
        return token;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression.toString();
    }

    @Override
    public String tokenLiteral() {
        return expression.tokenLiteral();
    }

    @Override
    public Node StatementNode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'StatementNode'");
    }
}
