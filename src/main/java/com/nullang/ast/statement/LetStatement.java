package com.nullang.ast.statement;

import com.nullang.ast.Expression;
import com.nullang.ast.Identifier;
import com.nullang.ast.Node;
import com.nullang.ast.Statement;
import com.nullang.token.Token;

public class LetStatement implements Statement {
    private final Token token;
    private Identifier name;
    private Expression value;

    public LetStatement(Token token) {
        this.token = token;
    }

    public Identifier getName() {
        return name;
    }

    public void setName(Identifier name) {
        this.name = name;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "LetStatement [token=" + token + ", name=" + name + ", value=" + value + "]";
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
