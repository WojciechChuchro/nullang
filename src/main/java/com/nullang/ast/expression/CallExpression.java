package com.nullang.ast.expression;

import com.nullang.ast.Expression;
import com.nullang.ast.Identifier;
import com.nullang.ast.Node;
import com.nullang.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public final class CallExpression implements Expression {
    private final Token token;
    private final Expression function;
    private final List<Expression> arguments;

    public CallExpression(Token token, Expression function, List<Expression> arguments) {
        this.token = token;
        this.function = function;
        this.arguments = arguments;
    }

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
    public String tokenLiteral() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tokenLiteral'");
    }

    @Override
    public Node expressionNode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expressionNode'");
    }

    public Token getToken() {
        return token;
    }

    public Expression getFunction() {
        return function;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
