package com.nullang.nullangobject;

import com.nullang.ast.Statement;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.parser.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class NullangObjectTest {

    private static Stream<Arguments> oneValidBooleanExpressions() throws IOException {
        return Stream.of(
                Arguments.of(parseInput("true;").getFirst()),
                Arguments.of(parseInput("false;").getFirst())
        );
    }

    private static Stream<Arguments> zeroBooleanExpressions() throws IOException {
        return Stream.of(
                Arguments.of(parseInput("5;").getFirst())
        );
    }

    private static List<Statement> parseInput(String input) throws IOException {
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
        
        BooleanType evaluate = (BooleanType) e.evaluate(statement);

        assertThat(evaluate.inspect()).isEqualTo(statement.toString());
    }

    @ParameterizedTest
    @MethodSource("zeroBooleanExpressions")
    public void shouldNotEvaluateBoolean_whenZeroProvided(Statement statement) {
        Eval e = new Eval();

        assertThat(e.evaluate(statement)).isNotInstanceOfAny(BooleanType.class);
    }
}
