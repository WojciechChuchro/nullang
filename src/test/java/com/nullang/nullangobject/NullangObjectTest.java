package com.nullang.nullangobject;

import com.nullang.ast.Program;
import com.nullang.ast.statement.Statement;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.parser.Parser;
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
                Arguments.of(parseInput("true;").getFirst()),
                Arguments.of(parseInput("false;").getFirst())
        );
    }

    private static Stream<Arguments> zeroBooleanExpressions() {
        return Stream.of(
                Arguments.of(parseInput("5;").getFirst())
        );
    }


    private static Stream<Arguments> bangOperator() {
        return Stream.of(
                Arguments.of(parseInput("!true;").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("!false;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("!5;").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("!!true;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("!!false;").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("!!5;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("!!!!5;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("!!!!-5;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("!!!-5;").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("true;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("false;").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("true == true;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("false == false;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("false != false;").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("true != true;").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("true != false;").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("1 < 2").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("1 > 2").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("1 < 1").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("1 > 1").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("1 == 1").getFirst(), new BooleanObject(true)),
                Arguments.of(parseInput("1 != 1").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("1 == 2").getFirst(), new BooleanObject(false)),
                Arguments.of(parseInput("1 != 2").getFirst(), new BooleanObject(true))
        );
    }

    private static Stream<Arguments> integerExpression() {
        return Stream.of(
                Arguments.of(parseInput("-5;").getFirst(), new IntegerObject(-5)),
                Arguments.of(parseInput("5;").getFirst(), new IntegerObject(5)),
                Arguments.of(parseInput("-10;").getFirst(), new IntegerObject(-10)),
                Arguments.of(parseInput("10;").getFirst(), new IntegerObject(10)),
                Arguments.of(parseInput("5 + 5").getFirst(), new IntegerObject(10)),
                Arguments.of(parseInput("6 - 9").getFirst(), new IntegerObject(-3)),
                Arguments.of(parseInput("420 / 20").getFirst(), new IntegerObject(21)),
                Arguments.of(parseInput("6 * 6").getFirst(), new IntegerObject(36)),
                Arguments.of(parseInput("(2 + 2) * 2").getFirst(), new IntegerObject(8)),
                Arguments.of(parseInput("2 + 2 * 2").getFirst(), new IntegerObject(6))
        );
    }


    private static Stream<Arguments> ifElseExpression() {
        return Stream.of(
                Arguments.of(parseInput("if (true) { 10 };").getFirst(), new IntegerObject(10)),
                Arguments.of(parseInput("if (false) {10}; ").getFirst(), new IntegerObject(0)),
                Arguments.of(parseInput("if (1) { 10 };").getFirst(), new IntegerObject(10)),
                Arguments.of(parseInput("if ( 1 < 2) { 10 };").getFirst(), new IntegerObject(10)),
                Arguments.of(parseInput("if ( 1 > 2) { 10 };").getFirst(), new IntegerObject(0)),
                Arguments.of(parseInput("if ( 1 < 2) { 10 } else { 20 }").getFirst(), new IntegerObject(10)),
                Arguments.of(parseInput("if ( 1 > 2) { 10 } else { 20 }").getFirst(), new IntegerObject(20)),
                Arguments.of(parseInput("6 * 6").getFirst(), new IntegerObject(36)),
                Arguments.of(parseInput("(2 + 2) * 2").getFirst(), new IntegerObject(8)),
                Arguments.of(parseInput("2 + 2 * 2").getFirst(), new IntegerObject(6))
        );
    }

    private static Stream<Arguments> returnExpression() {
        return Stream.of(
                Arguments.of(parseInputProgram("return 10; 9;"), new IntegerObject(10)),
                Arguments.of(parseInputProgram("9; return 2 * 5; 9;"), new IntegerObject(10)),
                Arguments.of(parseInputProgram("return 10;"), new IntegerObject(10)),
                Arguments.of(parseInputProgram("return 2 * 5; 9;"), new IntegerObject(10)),
                Arguments.of(parseInputProgram("if(10 > 5){9; return 2 * 5; 9;}"), new IntegerObject(10)),
                Arguments.of(parseInputProgram("if (10 > 1) {\n" +
                        "if (10 > 1) {\n" +
                        "return 10;\n" +
                        "}\n" +
                        "return 1;\n" +
                        "}"), new IntegerObject(10))
        );
    }


    private static Stream<Arguments> errors() {
        return Stream.of(
                Arguments.of(parseInput("5 + true").getFirst(), "type mismatch: INTEGER + BOOLEAN"),
                Arguments.of(parseInput("5 + true; 5;").getFirst(), "type mismatch: INTEGER + BOOLEAN"),
                Arguments.of(parseInput("-true").getFirst(), "unknown operator: -BOOLEAN"),
                Arguments.of(parseInput("true + false").getFirst(), "unknown operator: BOOLEAN + BOOLEAN"),
                Arguments.of(parseInput("5; true + false; 5;").getFirst(), "unknown operator: BOOLEAN + BOOLEAN"),
                Arguments.of(parseInput("if (10 > 1) { true + false; }").getFirst(), "unknown operator: BOOLEAN + BOOLEAN"),
                Arguments.of(parseInput("foobar").getFirst(), "identifier not found: foobar")
        );
    }

    private static Stream<Arguments> letStatements() {
        return Stream.of(
                Arguments.of("test1", parseInput("let a = 5; a;").getFirst(), new IntegerObject(5)),
                Arguments.of("test2", parseInput("let a = 5* 5; a;").getFirst(), new IntegerObject(25)),
                Arguments.of("test3", parseInput("let a = 5; let b = a; b").getFirst(), new IntegerObject(5)),
                Arguments.of("test4",parseInput("let a = 5; let b = a; let c = a + b + 5 ; c").getFirst(), new IntegerObject(15))
        );
    }

    private static List<Statement> parseInput(String input) {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader);
             Parser parser = new Parser(lexer)) {
            return parser.parseProgram().statements;
        }

    }

    private static Program parseInputProgram(String input) {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader);
             Parser parser = new Parser(lexer)) {
            return parser.parseProgram();
        }
    }

    @ParameterizedTest
    @MethodSource("oneValidBooleanExpressions")
    public void shouldEvaluateBoolean_whenOneProvided(Statement statement) {
        Eval e = new Eval();

        BooleanObject evaluate = (BooleanObject) e.evaluate(statement);

        assertThat(evaluate.inspect()).isEqualTo(statement.toString());
    }

    @ParameterizedTest
    @MethodSource("zeroBooleanExpressions")
    public void shouldNotEvaluateBoolean_whenZeroProvided(Statement statement) {
        Eval e = new Eval();

        assertThat(e.evaluate(statement)).isNotInstanceOfAny(BooleanObject.class);
    }


    @ParameterizedTest
    @MethodSource("bangOperator")
    public void shouldEvaluateBangOperator(Statement statement, BooleanObject expected) {
        Eval e = new Eval();

        BooleanObject evaluate = (BooleanObject) e.evaluate(statement);

        assertThat(evaluate.inspect()).isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("integerExpression")
    public void shouldEvaluateIntegerExpression(Statement statement, IntegerObject expected) {
        Eval e = new Eval();

        IntegerObject evaluate = (IntegerObject) e.evaluate(statement);

        assertThat(evaluate.inspect()).isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("ifElseExpression")
    public void shouldIfElseExpression(Statement statement, IntegerObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(statement);

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
    public void testErrorHandling(Statement statement, String expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(statement);

        assertThat(evaluated)
                .isInstanceOf(ErrorObject.class)
                .extracting(
                        obj -> ((ErrorObject) obj).inspect()
                )
                .isEqualTo("ERROR: " + expected);
    }


    @ParameterizedTest
    @MethodSource("letStatements")
    public void testLetStatements(String name,Statement statement, IntegerObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(statement);

        assertThat(evaluated.inspect()).isEqualTo(expected.inspect());
    }
}
