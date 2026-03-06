package com.nullang.nullangobject;

import com.nullang.ast.Identifier;
import com.nullang.ast.Program;
import com.nullang.ast.statement.BlockStatement;
import com.nullang.eval.Env;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.parser.Parser;
import com.nullang.token.Token;
import com.nullang.token.TokenType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class NullangObjectTest {
    private final Env env = new Env();

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
                Arguments.of(parseInput("foobar"), "identifier not found: foobar"),
                Arguments.of(parseInput("\"hello\" - \"world\""), "unknown operator: STRING - STRING")
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
                Arguments.of("identity function",
                        parseInput("let identity = fn(x) { x; }; identity(5);"),
                        new IntegerObject(5)),
                Arguments.of("constant function",
                        parseInput("let always = fn(x) { 42 }; always(0);"),
                        new IntegerObject(42)),
                Arguments.of("function with arithmetic body",
                        parseInput("let double = fn(x) { x * 2 }; double(7);"),
                        new IntegerObject(14)),
                Arguments.of("function with multiple params",
                        parseInput("let add = fn(x, y) { x + y }; add(3, 4);"),
                        new IntegerObject(7)),
                Arguments.of("function as argument",
                        parseInput("let apply = fn(f, x) { f(x) }; let double = fn(x) { x * 2 }; apply(double, 5);"),
                        new IntegerObject(10)),
                Arguments.of("immediately invoked function",
                        parseInput("fn(x) { x + 1 }(9);"),
                        new IntegerObject(10)),
                Arguments.of("function with conditional body",
                        parseInput("let abs = fn(x) { if (x < 0) { 0 - x } else { x } }; abs(-5);"),
                        new IntegerObject(5)),
                Arguments.of("recursive-like chained calls",
                        parseInput("let addThree = fn(a, b, c) { a + b + c }; addThree(1, 2, 3);"),
                        new IntegerObject(6)),
                Arguments.of("function returning result of another function",
                        parseInput("let double = fn(x) { x * 2 }; let quad = fn(x) { double(double(x)) }; quad(3);"),
                        new IntegerObject(12))
        );
    }

    private static Stream<Arguments> closureStatements() {
        return Stream.of(
                Arguments.of("closure captures outer variable",
                        parseInput("let a = fn(x) { fn(y) { x + y } }; let b = a(2); b(3);"),
                        new IntegerObject(5)),
                Arguments.of("closure captures multiple outer variables",
                        parseInput("let a = fn(x, z) { fn(y) { x + y + z } }; let b = a(2, 10); b(3);"),
                        new IntegerObject(15)),
                Arguments.of("nested closures three levels deep",
                        parseInput("let a = fn(x) { fn(y) { fn(z) { x + y + z } } }; let b = a(1); let c = b(2); c(3);"),
                        new IntegerObject(6)),
                Arguments.of("closure used immediately",
                        parseInput("let a = fn(x) { fn(y) { x + y } }; a(10)(20);"),
                        new IntegerObject(30)),
                Arguments.of("closure over let binding",
                        parseInput("let x = 10; let add = fn(y) { x + y }; add(5);"),
                        new IntegerObject(15)),
                Arguments.of("multiple closures from same factory",
                        parseInput("let make = fn(x) { fn(y) { x + y } }; let addTwo = make(2); let addFive = make(5); addTwo(10) + addFive(10);"),
                        new IntegerObject(27)),
                Arguments.of("closure with arithmetic in outer scope",
                        parseInput("let a = fn(x) { let doubled = x * 2; fn(y) { doubled + y } }; let b = a(3); b(4);"),
                        new IntegerObject(10))
        );
    }

    private static Stream<Arguments> stringStatements() {
        return Stream.of(
                Arguments.of("string statements",
                        parseInput("\"hello world\""),
                        new StringObject("hello world")
                ),
                Arguments.of("concatenation",
                        parseInput("\"hello\" + \" \" + \"world\" "),
                        new StringObject("hello world")
                )
        );
    }

    private static Stream<Arguments> builtin() {
        return Stream.of(
                Arguments.of("len of empty string",
                        parseInput("len(\"\")"),
                        "0"
                ),
                Arguments.of("len of four",
                        parseInput("len(\"four\")"),
                        "4"
                )
        );
    }

    private static Stream<Arguments> puts() {
        return Stream.of(
                Arguments.of("puts single integer",
                        parseInput("puts(1);"),
                        "1"
                ),
                Arguments.of("puts no arguments",
                        parseInput("puts();"),
                        ""
                ),
                Arguments.of("puts multiple arguments",
                        parseInput("puts(\"hello\", \"world\", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);"),
                        "hello\nworld\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10"
                ),
                Arguments.of("puts boolean false",
                        parseInput("puts(false);"),
                        "false"
                )
        );
    }

    private static Stream<Arguments> builtinErrors() {
        return Stream.of(
                Arguments.of("len with integer argument",
                        parseInput("len(1)"),
                        "ERROR: argument to `len` not supported, got INTEGER"
                ),
                Arguments.of("len with integer argument",
                        parseInput("len(true)"),
                        "ERROR: argument to `len` not supported, got BOOLEAN"
                ),
                Arguments.of("len with integer argument",
                        parseInput("len(false)"),
                        "ERROR: argument to `len` not supported, got BOOLEAN"
                ),
                Arguments.of("len with integer argument",
                        parseInput("len(\"wtf\", \"hello\")"),
                        "ERROR: wrong number of arguments. got 2 expected 1"
                )
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

        BooleanObject evaluate = (BooleanObject) e.evaluate(program, env);

        assertThat(evaluate.inspect()).isEqualTo(program.toString());
    }

    @ParameterizedTest
    @MethodSource("zeroBooleanExpressions")
    public void shouldNotEvaluateBoolean_whenZeroProvided(Program program) {
        Eval e = new Eval();

        assertThat(e.evaluate(program, env)).isNotInstanceOfAny(BooleanObject.class);
    }


    @ParameterizedTest
    @MethodSource("bangOperator")
    public void shouldEvaluateBangOperator(Program program, BooleanObject expected) {
        Eval e = new Eval();

        BooleanObject evaluate = (BooleanObject) e.evaluate(program, env);

        assertThat(evaluate.inspect()).isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("integerExpression")
    public void shouldEvaluateIntegerExpression(Program program, IntegerObject expected) {
        Eval e = new Eval();

        IntegerObject evaluate = (IntegerObject) e.evaluate(program, env);

        assertThat(evaluate.inspect()).isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("ifElseExpression")
    public void shouldIfElseExpression(Program program, IntegerObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program, env);

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

        var evaluated = e.evaluate(statement, env);

        assertThat(evaluated.inspect()).isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("errors")
    public void testErrorHandling(Program program, String expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program, env);

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

        var evaluated = e.evaluate(program, env);

        assertThat(evaluated.inspect()).isEqualTo(expected.inspect());
    }


    @ParameterizedTest
    @MethodSource("functionStatements")
    public void testFunctions(String name, Program program, IntegerObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program, new Env());

        assertThat(evaluated)
                .isInstanceOf(IntegerObject.class)
                .extracting(NullangObject::inspect)
                .isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("closureStatements")
    public void testClosures(String name, Program program, IntegerObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program, new Env());

        assertThat(evaluated)
                .isInstanceOf(IntegerObject.class)
                .extracting(NullangObject::inspect)
                .isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("stringStatements")
    public void testString(String name, Program program, StringObject expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program, new Env());

        assertThat(evaluated)
                .isInstanceOf(StringObject.class)
                .extracting(NullangObject::inspect)
                .isEqualTo(expected.inspect());
    }

    @ParameterizedTest
    @MethodSource("builtin")
    public void testBuiltin(String name, Program program, String expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program, new Env());

        assertThat(evaluated)
                .isInstanceOf(IntegerObject.class)
                .extracting(NullangObject::inspect)
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("builtinErrors")
    public void testBuiltinErrors(String name, Program program, String expected) {
        Eval e = new Eval();

        var evaluated = e.evaluate(program, new Env());

        assertThat(evaluated)
                .isInstanceOf(ErrorObject.class)
                .extracting(NullangObject::inspect)
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("puts")
    public void testPuts(String name, Program program, String expected) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;

    System.setOut(new PrintStream(outputStream));

    try {
        Eval e = new Eval();
        var evaluated = e.evaluate(program, new Env());

        String printed = outputStream.toString().replace("\r\n", "\n").trim();

        assertThat(printed).isEqualTo(expected);

    } finally {
        System.setOut(originalOut);
    }
    }
}
