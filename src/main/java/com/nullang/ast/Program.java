package com.nullang.ast;

import java.util.ArrayList;
import java.util.List;

public class Program implements Node {
    public final List<Statement> statements = new ArrayList<>();

    @Override
    public String tokenLiteral() {
        return "";
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Program statements [\n");

        for (int i = 0; i < statements.size(); i++) {
            builder.append("\t");
            builder.append(statements.get(i));
            builder.append("\n");
        }

        builder.append("]");
        return builder.toString();
    }
}
