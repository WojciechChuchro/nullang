package com.nullang.eval;

import java.util.List;

import com.nullang.ast.BooleanIdentifier;
import com.nullang.ast.IntegerIdentifier;
import com.nullang.ast.Node;
import com.nullang.ast.Program;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.nullangobject.BooleanType;
import com.nullang.nullangobject.IntegerType;
import com.nullang.nullangobject.NullangObject;

public class Eval {
    public NullangObject evaluate(Node node) {
        return switch (node) {
            case Program program -> evalStatements(program.statements);
            case ExpressionStatement exp -> evaluate(exp.getExpression());
            case IntegerIdentifier intNode -> new IntegerType(intNode.getValue());
            case BooleanIdentifier booleanNode -> new BooleanType(booleanNode.value);
            default -> throw new RuntimeException("Unknown node type: " + node);
        };
    }

    public NullangObject evalStatements(List<? extends Node> nodes) {
        NullangObject result = null;

        for (Node n: nodes) {
           result = evaluate(n);
        }

        return result;
    }
}
