package com.nullang.ast.statement;

import com.nullang.ast.expression.Expression;
import com.nullang.token.Token;

public class StringIdentifier implements Expression {
    private final Token token;
    private final String value;

    public StringIdentifier(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }

    @Override
    public String toString() {
        return value;
    }
}
