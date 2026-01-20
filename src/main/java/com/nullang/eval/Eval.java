package com.nullang.eval;

import com.nullang.ast.*;
import com.nullang.ast.expression.IfExpression;
import com.nullang.ast.expression.InfixExpression;
import com.nullang.ast.expression.PrefixExpression;
import com.nullang.ast.statement.BlockStatement;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.ast.statement.LetStatement;
import com.nullang.ast.statement.ReturnStatement;
import com.nullang.nullangobject.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Eval {
    private final static NullangObject NULL = new NullObject();
    private final static NullangObject TRUE = new BooleanObject(true);
    private final static NullangObject FALSE = new BooleanObject(false);
    private final static Map<String, NullangObject> ENV = new HashMap<>();

    public NullangObject evaluate(Node node) {
        return switch (node) {
            case Program program ->
                    evalProgram(program.statements);
            case ExpressionStatement exp ->
                    evaluate(exp.expression());
            case IntegerIdentifier intNode ->
                    new IntegerObject(intNode.getValue());
            case BooleanIdentifier booleanNode ->
                    nativeBoolToBooleanObject(booleanNode.value);
            case IfExpression ifExpression ->
                    evaluateIfExpression(ifExpression);
            case BlockStatement blockStatement ->
                    evalBlockStatement(blockStatement);
            case ReturnStatement returnStatement -> {
                var value = evaluate(returnStatement.getReturnValue());
                if (isError(value)) {
                    yield value;
                }
                yield new ReturnValue(value);
            }
            case LetStatement letStatement -> {
                var value = evaluate(letStatement.getValue());
                if (isError(value)) {
                    yield value;
                }
                ENV.put(letStatement.getName().getValue(), value);
                yield value;
            }
            case Identifier identifier ->
                    evalIdentifier(identifier);
            case InfixExpression infix -> {
                var left = evaluate(infix.getLeft());
                if (isError(left)) {
                    yield left;
                }
                var right = evaluate(infix.getRight());
                if (isError(right)) {
                    yield right;
                }
                yield evaluateInfixExpression(infix.getOperator(), left, right);
            }
            case PrefixExpression pe -> {
                var right = evaluate(pe.getRight());
                if (isError(right)) {
                    yield right;
                }
                yield evaluatePrefixExpression(pe.getOperator(), right);
            }
            default ->
                    NULL;
        };
    }

    private NullangObject evalIdentifier(Identifier identifier) {
        if (!ENV.containsKey(identifier.getValue())) {
            return new ErrorObject("identifier not found: " + identifier.getValue());
        }
        return ENV.get(identifier.getValue());
    }

    private NullangObject evaluateIfExpression(IfExpression ifExpression) {
        var condition = evaluate(ifExpression.getCondition());
        if (isError(condition)) {
            return condition;
        }

        if (isTruthy(condition)) {
            return evaluate(ifExpression.getConsequence());
        } else if (ifExpression.getAlternative().isPresent()) {
            return evaluate(ifExpression.getAlternative().get());
        } else {
            return NULL;
        }
    }

    private boolean isTruthy(NullangObject condition) {
        if (condition == NULL) {
            return false;
        } else if (condition == TRUE) {
            return true;
        } else if (condition == FALSE) {
            return false;
        } else {
            return true;
        }
    }

    private NullangObject evaluatePrefixExpression(String operator, NullangObject right) {
        return switch (operator) {
            case "!" ->
                    evaluateBangOperatorExpression(right);
            case "-" ->
                    evaluateMinusPrefixOperatorExpression(right);
            default ->
                    new ErrorObject("unknown operator: " + operator + " " + right.type());
        };
    }

    private NullangObject evaluateInfixExpression(String operator, NullangObject left, NullangObject right) {
        if (left.type() == ObjectType.INTEGER && right.type() == ObjectType.INTEGER) {
            return evaluateIntegerInfixExpression(operator, left, right);
        } else if (Objects.equals(operator, "==")) {
            return nativeBoolToBooleanObject(left == right);
        } else if (Objects.equals(operator, "!=")) {
            return nativeBoolToBooleanObject(left != right);
        } else if (left.type() != right.type()) {
            return new ErrorObject("type mismatch: " + left.type() + " " + operator + " " + right.type());
        }


        return new ErrorObject("unknown operator: " + left.type() + " " + operator + " " + right.type());
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
                    new ErrorObject("unknown operator: " + left.type() + " " + operator + " " + right.type());
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
        if (right.type() != ObjectType.INTEGER) {
            return new ErrorObject("unknown operator: -" + right.type());
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

    private NullangObject evalProgram(List<? extends Node> nodes) {
        NullangObject result = null;

        for (Node n : nodes) {
            result = evaluate(n);
            if (result.type() == ObjectType.RETURN_VALUE || result.type() == ObjectType.ERROR) {
                return result;
            }
        }

        return result;
    }


    private NullangObject evalBlockStatement(BlockStatement block) {
        NullangObject result = null;

        for (Node n : block.getStatements()) {
            result = evaluate(n);
            if (result != null && result.type() == ObjectType.RETURN_VALUE)
                return result;
            else if (result != null && result.type() == ObjectType.ERROR) {
                return result;
            }
        }

        return result;
    }

    private boolean isError(NullangObject obj) {
        if (obj != null) {
            return obj.type() == ObjectType.ERROR;
        }
        return false;
    }
}
