package com.nullang.eval;

import com.nullang.ast.*;
import com.nullang.ast.expression.CallExpression;
import com.nullang.ast.expression.IfExpression;
import com.nullang.ast.expression.InfixExpression;
import com.nullang.ast.expression.PrefixExpression;
import com.nullang.ast.statement.*;
import com.nullang.nullangobject.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Eval {
    private final static NullangObject NULL = new NullObject();
    private final static NullangObject TRUE = new BooleanObject(true);
    private final static NullangObject FALSE = new BooleanObject(false);
    private final Env env  = new Env();

    public NullangObject evaluate(Node node, Env evalEnv) {
        return switch (node) {
            case Program program ->
                    evalProgram(program.statements);
            case ExpressionStatement exp ->
                    evaluate(exp.expression(), env);
            case IntegerIdentifier intNode ->
                    new IntegerObject(intNode.getValue());
            case BooleanIdentifier booleanNode ->
                    nativeBoolToBooleanObject(booleanNode.value);
            case IfExpression ifExpression ->
                    evaluateIfExpression(ifExpression);
            case BlockStatement blockStatement ->
                    evalBlockStatement(blockStatement, evalEnv);
            case ReturnStatement returnStatement -> {
                var value = evaluate(returnStatement.getReturnValue(), env);
                if (isError(value)) {
                    yield value;
                }
                yield new ReturnValue(value);
            }
            case LetStatement letStatement -> {
                var value = evaluate(letStatement.getValue(), env);
                if (isError(value)) {
                    yield value;
                }
                env.define(letStatement.getName().getValue(), value);
                yield value;
            }
            case Identifier identifier ->
                    evalIdentifier(identifier);
            case InfixExpression infix -> {
                var left = evaluate(infix.getLeft(), env);
                if (isError(left)) {
                    yield left;
                }
                var right = evaluate(infix.getRight(), env);
                if (isError(right)) {
                    yield right;
                }
                yield evaluateInfixExpression(infix.getOperator(), left, right);
            }
            case PrefixExpression pe -> {
                var right = evaluate(pe.getRight(), env);
                if (isError(right)) {
                    yield right;
                }
                yield evaluatePrefixExpression(pe.getOperator(), right);
            }
            case FnStatement fn -> {
                var params = fn.getParameters();
                var body = fn.getBody();
                yield new FunctionObject(params, body,env);
            }
            case CallExpression callExpression -> {
                var function = evaluate(callExpression.getFunction(), env);
                if (isError(function)) {
                    yield function;
                }
                var args = evalExpressions(callExpression.getArguments());
                if (args.size() == 1 && isError(args.get(0))) {
                    yield args.getFirst();
                }
                yield applyFunction(function, args);
            }
            default ->
                    NULL;
        };
    }

    private NullangObject applyFunction(NullangObject function, List<NullangObject> args) {
        if (function instanceof FunctionObject fn) {
            var extendedEnv = extendedFunctionEnv(fn, args);
            var evaluated = evaluate(fn.body(), extendedEnv);
            if (evaluated instanceof ReturnValue rv) {
                return rv.value();
            }
            return evaluated;
        } else {
            return new ErrorObject("not a function: " + function.type());
        }
    }

    private Env extendedFunctionEnv(FunctionObject fn, List<NullangObject> args) {
        var fnEnv = new Env(fn.env());
        for (int i = 0; i < fn.parameters().size(); i++) {
            fnEnv.define(fn.parameters().get(i).getValue(), args.get(i));
        }
        return fnEnv;
    }

    private NullangObject evalIdentifier(Identifier identifier) {
        if (!env.contains(identifier.getValue())) {
            return new ErrorObject("identifier not found: " + identifier.getValue());
        }
        return env.get(identifier.getValue());
    }

    private List<NullangObject> evalExpressions(List<Expression> expressions) {
        List<NullangObject> result = new ArrayList<>();
        for (Expression exp : expressions) {
            var evaluated = evaluate(exp, env);
            if (isError(evaluated)) {
                return new ArrayList<>(List.of(evaluated));
            }
            result.add(evaluated);
        }
        return result;
    }

    private NullangObject evaluateIfExpression(IfExpression ifExpression) {
        var condition = evaluate(ifExpression.getCondition(), env);
        if (isError(condition)) {
            return condition;
        }

        if (isTruthy(condition)) {
            return evaluate(ifExpression.getConsequence(), env);
        } else if (ifExpression.getAlternative().isPresent()) {
            return evaluate(ifExpression.getAlternative().get(), env);
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
            result = evaluate(n, env);
            if (result.type() == ObjectType.RETURN_VALUE || result.type() == ObjectType.ERROR) {
                return result;
            }
        }

        return result;
    }


    private NullangObject evalBlockStatement(BlockStatement block, Env env) {
        NullangObject result = null;

        for (Node n : block.getStatements()) {
            result = evaluate(n, env);
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
