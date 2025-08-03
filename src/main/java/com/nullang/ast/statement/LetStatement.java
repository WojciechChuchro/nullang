package com.nullang.ast.statement;

import com.nullang.ast.Expression;
import com.nullang.ast.Identifier;
import com.nullang.ast.Node;
import com.nullang.ast.Statement;
import com.nullang.token.Token;

public class LetStatement implements Statement {
    private final Token token;
    public Identifier name;
    public Expression value;

    @Override
    public String toString() {
        return "LetStatement [token=" + token + ", name=" + name + ", value=" + value + "]";
    }

    public LetStatement(Token token) {
        this.token = token;
    }

    @Override
    public String tokenLiteral() {
        return token.literal;
    }

    @Override
    public Node StatementNode() {
        return this;
    }
}
