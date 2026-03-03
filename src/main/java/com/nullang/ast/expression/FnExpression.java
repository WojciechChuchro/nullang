
package com.nullang.ast.expression;

import java.util.List;
import java.util.stream.Collectors;

import com.nullang.ast.Identifier;
import com.nullang.ast.statement.BlockStatement;
import com.nullang.token.Token;

public record FnExpression(
        Token token,
        List<Identifier> parameters,
        BlockStatement body) implements Expression {

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(token.literal());
        builder.append("(");
        builder.append(parameters.stream().map(Object::toString).collect(Collectors.joining(", ")));
        builder.append(") {");
        builder.append(body.toString());
        builder.append("}");

        return builder.toString();
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }
}
