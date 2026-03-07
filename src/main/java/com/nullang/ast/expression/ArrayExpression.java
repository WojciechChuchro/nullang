
package com.nullang.ast.expression;

import com.nullang.ast.Identifier;
import com.nullang.ast.statement.BlockStatement;
import com.nullang.ast.statement.Statement;
import com.nullang.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public record ArrayExpression(
        Token token,
        List<Expression> elements
        ) implements Expression {

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        builder.append(elements.stream().map(Object::toString).collect(Collectors.joining(", ")));
        builder.append("]");

        return builder.toString();
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }
}
