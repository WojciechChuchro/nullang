
package com.nullang.ast.expression;

import com.nullang.token.Token;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record HashExpression(
        Token token,
        Map<Expression, Expression> pairs
        ) implements Expression {

    @Override
    public String toString() {
        String entries = pairs.entrySet().stream()
                .map(e -> e.getKey().toString() + ": " + e.getValue().toString())
                .collect(Collectors.joining(", "));

        return "{" + entries + "}";
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }
}
