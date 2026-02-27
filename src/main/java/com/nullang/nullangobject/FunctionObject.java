
package com.nullang.nullangobject;

import com.nullang.ast.Identifier;
import com.nullang.ast.statement.BlockStatement;
import com.nullang.eval.Env;

import java.util.List;
import java.util.Map;

public record FunctionObject(
        List<Identifier> parameters,
        BlockStatement body,
        Env env
       ) implements NullangObject {
    @Override
    public ObjectType type() {
        return ObjectType.FUNCTION;
    }

    @Override
    public String inspect() {
        var sb = new StringBuilder();

        sb.append("fn(")
                .append(String.join(", ", parameters.toString()))
                .append("{")
                .append(body.toString()).append("}");

        return sb.toString();
    }
}
