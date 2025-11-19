
package com.nullang.ast.statement;

import java.util.List;
import java.util.stream.Collectors;

import com.nullang.ast.Expression;
import com.nullang.ast.Identifier;
import com.nullang.ast.Node;
import com.nullang.token.Token;

public class FnStatement implements Expression {
    private final Token token;
    private final List<Identifier> parameters;
    private final BlockStatement body;

    public Token getToken() {
        return token;
    }

    public List<Identifier> getParameters() {
        return parameters;
    }

    public BlockStatement getBody() {
        return body;
    }

    public FnStatement(Token token, List<Identifier> parameters, BlockStatement body) {
        this.token = token;
        this.parameters = parameters;
        this.body = body;
    }

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
    public String tokenLiteral() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tokenLiteral'");
    }

    @Override
    public Node expressionNode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expressionNode'");
    }

}
