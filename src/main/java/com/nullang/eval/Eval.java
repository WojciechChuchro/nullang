package com.nullang.eval;

import java.util.List;

import com.nullang.ast.*;
import com.nullang.ast.expression.PrefixExpression;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.nullangobject.*;
import org.slf4j.helpers.NOP_FallbackServiceProvider;

public class Eval {
    private final static NullangObject NULL = new NullObject();
    private final static NullangObject TRUE = new BooleanObject(true);
    private final static NullangObject FALSE = new BooleanObject(false);

    public NullangObject evaluate(Node node) {
        return switch (node) {
            case Program p ->
                    evalStatements(p.statements);
            case ExpressionStatement exp ->
                    evaluate(exp.getExpression());
            case IntegerIdentifier intNode ->
                    new IntegerObject(intNode.getValue());
            case BooleanIdentifier booleanNode ->
                    booleanNode.value ? TRUE : FALSE;
            case PrefixExpression pe -> {
                var right = evaluate(pe.getRight());
                yield evaluatePrefixExpression(pe.getOperator(), right);
            }
            default ->
                    NULL;
        };
    }

    private NullangObject evaluatePrefixExpression(String operator, NullangObject right) {
        switch (operator) {
            case "!":
                return evaluateBangOperatorExpression(right);
            default:
                return NULL;
        }
    }

    private NullangObject evaluateBangOperatorExpression(NullangObject right) {
        return switch (right) {
            case BooleanObject b when b.value() -> FALSE;
            case BooleanObject b -> TRUE;
            case NullObject n -> TRUE;
            default -> FALSE;
        };
    }

    private NullangObject evalStatements(List<? extends Node> nodes) {
        NullangObject result = null;

        for (Node n : nodes) {
            result = evaluate(n);
        }

        return result;
    }
}
