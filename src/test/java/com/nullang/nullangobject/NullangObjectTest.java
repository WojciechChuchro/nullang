package com.nullang.nullangobject;

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
                Arguments.of(parseInput("!!!-5;").getFirst(), new BooleanObject(false))
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

    private static List<Statement> parseInput(String input) {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader);
             Parser parser = new Parser(lexer)) {
            return parser.parseProgram().statements;
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
}
