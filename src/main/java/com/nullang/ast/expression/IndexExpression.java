
package com.nullang.ast.expression;

import com.nullang.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public class IndexExpression implements Expression {
    private final Token token;
        private final Expression left;
        private Expression index;

    public IndexExpression(Token token, Expression left) {
        this.token = token;
        this.left = left;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("(");
        builder.append(left.toString());
        builder.append("[");
        builder.append(index.toString());
        builder.append("])");

        return builder.toString();
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }

    public Expression getIndex() {
        return index;
    }

    public Token getToken() {
        return token;
    }

    public Expression getLeft() {
        return left;
    }

    public void setIndex(Expression index) {
        this.index = index;
    }
}
