
package com.nullang.ast.statement;

import java.util.ArrayList;
import java.util.List;

import com.nullang.ast.Node;
import com.nullang.ast.Statement;
import com.nullang.token.Token;

public final class BlockStatement implements Statement{
    private final Token token;
    private final List<Statement> statements = new ArrayList<>();

    public BlockStatement(Token token) {
        this.token = token;
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public Statement getStatement(int index) {
        return statements.get(index);
    }
    public int statementsSize() {
        return statements.size();
    }

    @Override
    public String toString() {
        StringBuffer builder = new StringBuffer();
        for (Statement s: this.statements) {
            builder.append(s.toString());
        }
        return builder.toString();
    }



    @Override
    public String tokenLiteral() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tokenLiteral'");
    }

    @Override
    public Node StatementNode() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'StatementNode'");
    }

    
}
