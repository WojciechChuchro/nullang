package com.nullang.ast;

import com.nullang.ast.statement.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Program implements Node {
    public final List<Statement> statements = new ArrayList<>();

    @Override
    public String tokenLiteral() {
        return "";
    }

    @Override
    public String toString() {
        return statements.stream().map(Statement::toString).collect(Collectors.joining());
    }
}
