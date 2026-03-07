package com.nullang.eval;

import com.nullang.ast.*;
import com.nullang.ast.expression.BooleanIdentifier;
import com.nullang.ast.expression.ArrayExpression;
import com.nullang.ast.expression.CallExpression;
import com.nullang.ast.expression.Expression;
import com.nullang.ast.expression.FnExpression;
import com.nullang.ast.expression.IfExpression;
import com.nullang.ast.expression.IndexExpression;
import com.nullang.ast.expression.InfixExpression;
import com.nullang.ast.expression.PrefixExpression;
import com.nullang.ast.statement.*;
import com.nullang.nullangobject.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Eval {
    private final static NullangObject NULL = new NullObject();
    private final static NullangObject TRUE = new BooleanObject(true);
    private final static NullangObject FALSE = new BooleanObject(false);
    private static final Map<String, Function<List<NullangObject>, NullangObject>> builtInFunctions =
            Map.of(
                    "len", args -> {
                        if (args.size() != 1) {
                            return new ErrorObject("wrong number of arguments. got " + args.size() + " expected 1");
                        }

                        return switch (args.getFirst().type()) {
                            case STRING ->
                                    new IntegerObject(args.getFirst().inspect().length());
                            case ARRAY -> {
                                var arr = (ArrayObject) args.getFirst();
                                yield new IntegerObject(arr.elements().size());
                            }
                            default ->
                                    new ErrorObject("argument to `len` not supported, got " + args.get(0).type());
                        };
                    },
                    "puts", args -> {
                        for (NullangObject arg : args) {
                            System.out.println(arg.inspect());
                        }

                        return NULL;
                    },
                    "first", args -> {
                        if (args.size() != 1) {
                            return new ErrorObject("wrong number of arguments. got " + args.size() + " expected 1");
                        }

                        if (args.getFirst().type() == ObjectType.ARRAY) {
                            var arr = (ArrayObject) args.getFirst();
                            if (arr.elements().isEmpty()) {
                                return NULL;
                            }
                            return arr.elements().getFirst();
                        } else {
                            return new ErrorObject("argument to `first` not supported, got " + args.getFirst().type());
                        }
                    },
                    "tail", args -> {
                        if (args.size() != 1) {
                            return new ErrorObject("wrong number of arguments. got " + args.size() + " expected 1");
                        }

                        if (args.getFirst().type() == ObjectType.ARRAY) {
                            var arr = (ArrayObject) args.getLast();
                            if (arr.elements().isEmpty()) {
                                return NULL;
                            }
                            return arr.elements().getLast();
                        } else {
                            return new ErrorObject("argument to `first` not supported, got " + args.getLast().type());
                        }
                    }
            );

    public NullangObject evaluate(Node node, Env evalEnv) {
        return switch (node) {
            case Program program ->
                    evalProgram(program.statements, evalEnv);
            case ExpressionStatement exp ->
                    evaluate(exp.expression(), evalEnv);
            case IntegerIdentifier intNode ->
                    new IntegerObject(intNode.getValue());
            case StringIdentifier stringIdentifier ->
                    new StringObject(stringIdentifier.getValue());
            case IndexExpression indexExpression -> {
                var left = evaluate(indexExpression.getLeft(), evalEnv);
                if (isError(left)) {
                    yield left;
                }
                var index = evaluate(indexExpression.getIndex(), evalEnv);
                if (isError(index)) {
                    yield index;
                }

                yield evalIndexExpression(left, index);
            }
            case BooleanIdentifier booleanNode ->
                    nativeBoolToBooleanObject(booleanNode.getValue());
            case IfExpression ifExpression ->
                    evaluateIfExpression(ifExpression, evalEnv);
            case BlockStatement blockStatement ->
                    evalBlockStatement(blockStatement, evalEnv);
            case ReturnStatement returnStatement -> {
                var value = evaluate(returnStatement.getReturnValue(), evalEnv);
                if (isError(value)) {
                    yield value;
                }
                yield new ReturnValue(value);
            }
            case LetStatement letStatement -> {
                var value = evaluate(letStatement.getValue(), evalEnv);
                if (isError(value)) {
                    yield value;
                }
                evalEnv.define(letStatement.getName().getValue(), value);
                yield value;
            }
            case Identifier identifier ->
                    evalIdentifier(identifier, evalEnv);
            case InfixExpression infix -> {
                var left = evaluate(infix.getLeft(), evalEnv);
                if (isError(left)) {
                    yield left;
                }
                var right = evaluate(infix.getRight(), evalEnv);
                if (isError(right)) {
                    yield right;
                }
                yield evaluateInfixExpression(infix.getOperator(), left, right);
            }
            case PrefixExpression pe -> {
                var right = evaluate(pe.getRight(), evalEnv);
                if (isError(right)) {
                    yield right;
                }
                yield evaluatePrefixExpression(pe.getOperator(), right);
            }
            case FnExpression fn -> {
                var params = fn.parameters();
                var body = fn.body();
                yield new FunctionObject(params, body, evalEnv);
            }
            case CallExpression callExpression -> {
                var function = evaluate(callExpression.function(), evalEnv);
                if (isError(function)) {
                    yield function;
                }
                var args = evalExpressions(callExpression.arguments(), evalEnv);
                if (args.size() == 1 && isError(args.getFirst())) {
                    yield args.getFirst();
                }
                yield applyFunction(function, args);
            }
            case ArrayExpression arrayExpression -> {
                var elements = evalExpressions(arrayExpression.elements(), evalEnv);
                if (elements.size() == 1 && isError(elements.getFirst())) {
                    yield elements.getFirst();
                }
                yield new ArrayObject(elements);
            }
            default ->
                    NULL;
        };
    }

    private NullangObject evalIndexExpression(NullangObject left, NullangObject index) {
        if(left.type()==ObjectType.ARRAY && index.type() == ObjectType.INTEGER) {
            return evalArrayIndexExpression(left, index);
        } else {
            return new ErrorObject("index operator not supported: " + left.type() + " " + index.type());
        }
    }

    private NullangObject evalArrayIndexExpression(NullangObject array, NullangObject index) {
        var arr = (ArrayObject) array;
        var idx = (IntegerObject) index;

        if (idx.value() < 0 || idx.value() > arr.elements().size()) {
            return NULL;
        }

        return arr.elements().get(idx.value());
    }

    private NullangObject applyFunction(NullangObject function, List<NullangObject> args) {
        if (function instanceof FunctionObject fn) {
            var extendedEnv = extendedFunctionEnv(fn, args);
            var evaluated = evaluate(fn.body(), extendedEnv);
            if (evaluated instanceof ReturnValue rv) {
                return rv.value();
            }
            return evaluated;
        } else if (function instanceof BuiltinFunctionObject fn) {
            return fn.call(args);
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

    private NullangObject evalIdentifier(Identifier identifier, Env env) {
        if (env.contains(identifier.getValue())) {
            return env.get(identifier.getValue());
        }

        if (builtInFunctions.containsKey(identifier.getValue())) {
            return new BuiltinFunctionObject(builtInFunctions.get(identifier.getValue()));
        }

        return new ErrorObject("identifier not found: " + identifier.getValue());
    }

    private List<NullangObject> evalExpressions(List<Expression> expressions, Env env) {
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

    private NullangObject evaluateIfExpression(IfExpression ifExpression, Env env) {
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
        } else if (left.type() == ObjectType.STRING && right.type() == ObjectType.STRING) {
            if (operator.equals("+")) {
                return new StringObject(left.inspect() + right.inspect());
            }

            return new ErrorObject("unknown operator: " + left.type() + " " + operator + " " + right.type());
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

    private NullangObject evalProgram(List<? extends Node> nodes, Env env) {
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
