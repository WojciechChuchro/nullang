package com.nullang.nullangobject;

import com.nullang.ast.Identifier;
import com.nullang.ast.Program;
import com.nullang.ast.statement.BlockStatement;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.parser.Parser;
import com.nullang.token.Token;
import com.nullang.token.TokenType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class NullangObjectTest {

    private static Stream<Arguments> oneValidBooleanExpressions() {
        return Stream.of(
                Arguments.of(parseInput("true;")),
                Arguments.of(parseInput("false;"))
        );
    }

    private static Stream<Arguments> zeroBooleanExpressions() {
        return Stream.of(
                Arguments.of(parseInput("5;"))
        );
    }


    private static Stream<Arguments> bangOperator() {
        return Stream.of(
                Arguments.of(parseInput("!true;"), new BooleanObject(false)),
                Arguments.of(parseInput("!false;"), new BooleanObject(true)),
                Arguments.of(parseInput("!5;"), new BooleanObject(false)),
                Arguments.of(parseInput("!!true;"), new BooleanObject(true)),
                Arguments.of(parseInput("!!false;"), new BooleanObject(false)),
                Arguments.of(parseInput("!!5;"), new BooleanObject(true)),
                Arguments.of(parseInput("!!!!5;"), new BooleanObject(true)),
                Arguments.of(parseInput("!!!!-5;"), new BooleanObject(true)),
                Arguments.of(parseInput("!!!-5;"), new BooleanObject(false)),
                Arguments.of(parseInput("true;"), new BooleanObject(true)),
                Arguments.of(parseInput("false;"), new BooleanObject(false)),
                Arguments.of(parseInput("true == true;"), new BooleanObject(true)),
                Arguments.of(parseInput("false == false;"), new BooleanObject(true)),
                Arguments.of(parseInput("false != false;"), new BooleanObject(false)),
                Arguments.of(parseInput("true != true;"), new BooleanObject(false)),
                Arguments.of(parseInput("true != false;"), new BooleanObject(true)),
                Arguments.of(parseInput("1 < 2"), new BooleanObject(true)),
                Arguments.of(parseInput("1 > 2"), new BooleanObject(false)),
                Arguments.of(parseInput("1 < 1"), new BooleanObject(false)),
                Arguments.of(parseInput("1 > 1"), new BooleanObject(false)),
                Arguments.of(parseInput("1 == 1"), new BooleanObject(true)),
                Arguments.of(parseInput("1 != 1"), new BooleanObject(false)),
                Arguments.of(parseInput("1 == 2"), new BooleanObject(false)),
                Arguments.of(parseInput("1 != 2"), new BooleanObject(true))
        );
    }

    private static Stream<Arguments> integerExpression() {
        return Stream.of(
                Arguments.of(parseInput("-5;"), new IntegerObject(-5)),
                Arguments.of(parseInput("5;"), new IntegerObject(5)),
                Arguments.of(parseInput("-10;"), new IntegerObject(-10)),
                Arguments.of(parseInput("10;"), new IntegerObject(10)),
                Arguments.of(parseInput("5 + 5"), new IntegerObject(10)),
                Arguments.of(parseInput("6 - 9"), new IntegerObject(-3)),
                Arguments.of(parseInput("420 / 20"), new IntegerObject(21)),
                Arguments.of(parseInput("6 * 6"), new IntegerObject(36)),
                Arguments.of(parseInput("(2 + 2) * 2"), new IntegerObject(8)),
                Arguments.of(parseInput("2 + 2 * 2"), new IntegerObject(6))
        );
    }


    private static Stream<Arguments> ifElseExpression() {
        return Stream.of(
                Arguments.of(parseInput("if (true) { 10 };"), new IntegerObject(10)),
                Arguments.of(parseInput("if (false) {10}; "), new IntegerObject(0)),
                Arguments.of(parseInput("if (1) { 10 };"), new IntegerObject(10)),
                Arguments.of(parseInput("if ( 1 < 2) { 10 };"), new IntegerObject(10)),
                Arguments.of(parseInput("if ( 1 > 2) { 10 };"), new IntegerObject(0)),
                Arguments.of(parseInput("if ( 1 < 2) { 10 } else { 20 }"), new IntegerObject(10)),
                Arguments.of(parseInput("if ( 1 > 2) { 10 } else { 20 }"), new IntegerObject(20)),
                Arguments.of(parseInput("6 * 6"), new IntegerObject(36)),
                Arguments.of(parseInput("(2 + 2) * 2"), new IntegerObject(8)),
                Arguments.of(parseInput("2 + 2 * 2"), new IntegerObject(6))
        );
    }

    private static Stream<Arguments> returnExpression() {
        return Stream.of(
                Arguments.of(parseInput("return 10; 9;"), new IntegerObject(10)),
                Arguments.of(parseInput("9; return 2 * 5; 9;"), new IntegerObject(10)),
                Arguments.of(parseInput("return 10;"), new IntegerObject(10)),
                Arguments.of(parseInput("return 2 * 5; 9;"), new IntegerObject(10)),
                Arguments.of(parseInput("if(10 > 5){9; return 2 * 5; 9;}"), new IntegerObject(10)),
                Arguments.of(parseInput("if (10 > 1) {\n" +
                        "if (10 > 1) {\n" +
                        "return 10;\n" +
                        "}\n" +
                        "return 1;\n" +
                        "}"), new IntegerObject(10))
        );
    }


    private static Stream<Arguments> errors() {
        return Stream.of(
                Arguments.of(parseInput("5 + true"), "type mismatch: INTEGER + BOOLEAN"),
                Arguments.of(parseInput("5 + true; 5;"), "type mismatch: INTEGER + BOOLEAN"),
                Arguments.of(parseInput("-true"), "unknown operator: -BOOLEAN"),
                Arguments.of(parseInput("true + false"), "unknown operator: BOOLEAN + BOOLEAN"),
                Arguments.of(parseInput("5; true + false; 5;"), "unknown operator: BOOLEAN + BOOLEAN"),
                Arguments.of(parseInput("if (10 > 1) { true + false; }"), "unknown operator: BOOLEAN + BOOLEAN"),
                Arguments.of(parseInput("foobar"), "identifier not found: foobar")
        );
    }

    private static Stream<Arguments> letStatements() {
        return Stream.of(
                Arguments.of("test1", parseInput("let a = 5; a;"), new IntegerObject(5)),
                Arguments.of("test2", parseInput("let a = 5* 5; a;"), new IntegerObject(25)),
                Arguments.of("test3", parseInput("let a = 5; let b = a; b"), new IntegerObject(5)),
                Arguments.of("test4", parseInput("let a = 5; let b = a; let c = a + b + 5 ; c"), new IntegerObject(15))
        );
    }

    private static Stream<Arguments> functionStatements() {
        return Stream.of(
                Arguments.of("test1", parseInput("fn(x) { x + 2 };"), "asdf")
        );
    }

    private static Program parseInput(String input) {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader);
             Parser parser = new Parser(lexer)) {
            return parser.parseProgram();
        }
    }

    @ParameterizedTest
    @MethodSource("oneValidBooleanExpressions")
    public void shouldEvaluateBoolean_whenOneProvided(Program program) {
        Eval e = new Eval();

        BooleanObject evaluate = (BooleanObject) e.evaluate(program);

        assertThat(evaluate.inspect()).isEqualTo(program.toString());
    }

    @ParameterizedTest
    @MethodSource("zeroBooleanExpressions")
    public void shouldNotEvaluateBoolean_whenZeroProvided(Program program) {
        Eval e = new Eval();

        assertThat(e.evaluate(program)).isNotInstanceOfAny(BooleanObject.class);
    }


    @ParameterizedTest
    @MethodSource("bangOperator")
    public void shouldEvaluateBangOperator(Program program, BooleanObject expected) {
        Eval e = new Eval();

        BooleanObject evaluate = (BooleanObject) e.evaluate(program);

        assertThat(evaluate.inspect()).isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("integerExpression")
    public void shouldEvaluateIntegerExpression(Program program, IntegerObject expected) {
        Eval e = new Eval();

        IntegerObject evaluate = (IntegerObject) e.evaluate(program);

        assertThat(evaluate.inspect()).isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("ifElseExpression")
    public void shouldIfElseExpression(Program program, IntegerObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program);

        if (evaluated.getClass().equals(NullObject.class)) {
            assertThat(evaluated.inspect()).isEqualTo("null");
        } else {
            assertThat(evaluated.inspect()).isEqualTo(expected.inspect());
        }
    }

    @ParameterizedTest
    @MethodSource("returnExpression")
    public void shouldReturnExpression(Program statement, IntegerObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(statement);

        assertThat(evaluated.inspect()).isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("errors")
    public void testErrorHandling(Program program, String expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program);

        assertThat(evaluated)
                .isInstanceOf(ErrorObject.class)
                .extracting(
                        obj -> ((ErrorObject) obj).inspect()
                )
                .isEqualTo("ERROR: " + expected);
    }


    @ParameterizedTest
    @MethodSource("letStatements")
    public void testLetStatements(String name, Program program, IntegerObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program);

        assertThat(evaluated.inspect()).isEqualTo(expected.inspect());
    }


    @ParameterizedTest
    @MethodSource("functionStatements")
    public void testFunctions(String name, Program program, String fnObject) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program);
        assertThat(evaluated).isInstanceOf(FunctionObject.class);
        FunctionObject fn = (FunctionObject) evaluated;
        assertThat(fn.body().toString()).isEqualTo("(x + 2)");
        assertThat(fn.parameters().size()).isEqualTo(1);
    }
}
