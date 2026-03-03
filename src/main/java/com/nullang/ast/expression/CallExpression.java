package com.nullang.ast.expression;

import com.nullang.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public record CallExpression(
        Token token,
        Expression function,
        List<Expression> arguments) implements Expression {

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(function.toString());

        builder.append("(");
        builder.append(arguments.stream().map(Object::toString).collect(Collectors.joining(", ")));
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String getTokenLiteral() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tokenLiteral'");
    }
}
