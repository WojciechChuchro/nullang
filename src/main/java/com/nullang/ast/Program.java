package com.nullang.ast;

import java.util.ArrayList;
import java.util.List;

public class Program implements Node {
    public final List<Statement> statements = new ArrayList<>();

    @Override
    public String tokenLiteral() {
        return "";
    }
}
