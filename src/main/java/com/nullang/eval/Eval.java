package com.nullang.eval;

import java.util.List;
import java.util.Objects;

import com.nullang.ast.*;
import com.nullang.ast.expression.InfixExpression;
import com.nullang.ast.expression.PrefixExpression;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.nullangobject.*;

public class Eval {
    private final static NullangObject NULL = new NullObject();
    private final static NullangObject TRUE = new BooleanObject(true);
    private final static NullangObject FALSE = new BooleanObject(false);

    public NullangObject evaluate(Node node) {
        return switch (node) {
            case Program program ->
                    evalStatements(program.statements);
            case ExpressionStatement exp ->
                    evaluate(exp.expression());
            case IntegerIdentifier intNode ->
                    new IntegerObject(intNode.getValue());
            case BooleanIdentifier booleanNode ->
                    nativeBoolToBooleanObject(booleanNode.value);
            case InfixExpression infix -> {
                var left = evaluate(infix.getLeft());
                var right = evaluate(infix.getRight());
                yield evaluateInfixExpression(infix.getOperator(), left, right);
            }
            case PrefixExpression pe -> {
                var right = evaluate(pe.getRight());
                yield evaluatePrefixExpression(pe.getOperator(), right);
            }
            default ->
                    NULL;
        };
    }

    private NullangObject evaluatePrefixExpression(String operator, NullangObject right) {
        return switch (operator) {
            case "!" ->
                    evaluateBangOperatorExpression(right);
            case "-" ->
                    evaluateMinusPrefixOperatorExpression(right);
            default ->
                    NULL;
        };
    }

    private NullangObject evaluateInfixExpression(String operator, NullangObject left, NullangObject right) {
        if (left.type() == ObjectType.INTEGER_OBJ && right.type() == ObjectType.INTEGER_OBJ) {
            return evaluateIntegerInfixExpression(operator, left, right);
        } else if(Objects.equals(operator, "==")) {
            return nativeBoolToBooleanObject(left == right);
        } else if(Objects.equals(operator, "!=")) {
            return nativeBoolToBooleanObject(left != right);
        }

        return NULL;
    }

    private NullangObject evaluateIntegerInfixExpression(String operator, NullangObject left, NullangObject right) {
        var leftValue = ((IntegerObject) left).value();
        var rightValue = ((IntegerObject) right).value();

        return switch (operator) {
            case "+" ->
                    new IntegerObject(leftValue + rightValue);
            case "-" ->
                    new IntegerObject(leftValue - rightValue);
            case "*" ->
                    new IntegerObject(leftValue * rightValue);
            case "/" ->
                    new IntegerObject(leftValue / rightValue);

            case "<" ->
                    nativeBoolToBooleanObject(leftValue < rightValue);
            case ">" ->
                    nativeBoolToBooleanObject(leftValue > rightValue);
            case "==" ->
                    nativeBoolToBooleanObject(leftValue == rightValue);
            case "!=" ->
                    nativeBoolToBooleanObject(leftValue != rightValue);
            default ->
                    NULL;
        };
    }

    private NullangObject evaluateBangOperatorExpression(NullangObject right) {
        return switch (right) {
            case BooleanObject b when b.value() ->
                    FALSE;
            case BooleanObject b ->
                    TRUE;
            case NullObject n ->
                    TRUE;
            default ->
                    FALSE;
        };
    }

    private NullangObject evaluateMinusPrefixOperatorExpression(NullangObject right) {
        if (right.type() != ObjectType.INTEGER_OBJ) {
            return NULL;
        }

        var negativeValue = -((IntegerObject) right).value();

        return new IntegerObject(negativeValue);
    }

    private NullangObject nativeBoolToBooleanObject(boolean input) {
        if (input) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private NullangObject evalStatements(List<? extends Node> nodes) {
        NullangObject result = null;

        for (Node n : nodes) {
            result = evaluate(n);
        }

        return result;
    }
}
