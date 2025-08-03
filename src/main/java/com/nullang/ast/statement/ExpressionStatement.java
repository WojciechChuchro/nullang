package com.nullang.ast.statement;

import com.nullang.ast.Expression;
import com.nullang.ast.Node;
import com.nullang.ast.Statement;
import com.nullang.token.Token;

public class ExpressionStatement implements Statement{
    private final Token token;
    private final Expression expression;

    public ExpressionStatement(Token token, Expression expression) {
        this.token = token;
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "ExpressionStatement [token=" + token + ", expression=" + expression + "]";
    }

    @Override
    public String tokenLiteral() {
        return token.literal;
    }

    @Override
    public Node StatementNode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'StatementNode'");
    }
}
