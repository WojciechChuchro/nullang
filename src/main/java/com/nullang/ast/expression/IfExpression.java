
package com.nullang.ast.expression;

import java.util.Optional;

import com.nullang.ast.Expression;
import com.nullang.ast.statement.BlockStatement;
import com.nullang.token.Token;

public class IfExpression implements Expression{
    private final Token token;
    private final Expression condition;
    private final BlockStatement consequence;
    private final Optional<BlockStatement> alternative;

    public Optional<BlockStatement> getAlternative() {
        return alternative;
    }

    public IfExpression(Token token, Expression condition, BlockStatement alternative, BlockStatement consequence) {
        this.token = token;
        this.condition = condition;
        this.alternative = Optional.ofNullable(alternative);
        this.consequence = consequence;
    }

    public Expression getCondition() {
        return condition;
    }


    public BlockStatement getConsequence() {
        return consequence;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("if ");
        builder.append(condition.toString());
        builder.append(consequence.toString());

        alternative.ifPresent((a) -> {
            builder.append("else ");
            builder.append(a.toString());
        });

        return builder.toString();
    }

    @Override
    public String getTokenLiteral() {
        return token.literal();
    }
}
