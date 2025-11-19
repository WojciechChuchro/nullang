package com.nullang.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nullang.token.Token;
import com.nullang.token.TokenType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

public class LexerTokenTest {
    @ParameterizedTest(name = "{index} ⇒ {0}")
    @MethodSource("correctInput")
    void testDelimiters(String name, String input, List<Token> expectedTokens) {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader)) {
            for (Token expected : expectedTokens) {
                assertToken(lexer, expected.literal(), expected.type());
            }
            assertToken(lexer, "", TokenType.EOF);
        }
    }

    private static void assertToken(Lexer lexer, String literal, TokenType type) {
        Token actual = lexer.nextToken();
        assertEquals(literal, actual.literal());
        assertEquals(type, actual.type());
    }

    private static Stream<Arguments> correctInput() {
        return Stream.of(
                // Special Tokens
                Arguments.of("should parse end of file",
                        "",
                        List.of(
                                new Token(TokenType.EOF, "")
                        )),
                Arguments.of("should parse illegal characters",
                        "& ^ % @ #",
                        List.of(
                                new Token(TokenType.ILLEGAL, "&"),
                                new Token(TokenType.ILLEGAL, "^"),
                                new Token(TokenType.ILLEGAL, "%"),
                                new Token(TokenType.ILLEGAL, "@"),
                                new Token(TokenType.ILLEGAL, "#")
                        )),

                // Identifiers and Literals
                Arguments.of("should parse identifiers",
                        "hello_world HelloWorld helloWorld x y",
                        List.of(
                                new Token(TokenType.IDENT, "hello_world"),
                                new Token(TokenType.IDENT, "HelloWorld"),
                                new Token(TokenType.IDENT, "helloWorld"),
                                new Token(TokenType.IDENT, "x"),
                                new Token(TokenType.IDENT, "y")
                        )),
                Arguments.of("should parse integers",
                        "0 42 69 420 2137",
                        List.of(
                                new Token(TokenType.INT, "0"),
                                new Token(TokenType.INT, "42"),
                                new Token(TokenType.INT, "69"),
                                new Token(TokenType.INT, "420"),
                                new Token(TokenType.INT, "2137")
                        )),
                Arguments.of("should parse boolean literals",
                        "true false",
                        List.of(
                                new Token(TokenType.TRUE, "true"),
                                new Token(TokenType.FALSE, "false")
                        )),

                // Arithmetic Operators
                Arguments.of("should parse arithmetic operators",
                        "+ - * /",
                        List.of(
                                new Token(TokenType.PLUS, "+"),
                                new Token(TokenType.MINUS, "-"),
                                new Token(TokenType.ASTERISK, "*"),
                                new Token(TokenType.SLASH, "/")
                        )),

                // Comparison Operators
                Arguments.of("should parse comparison operators",
                        "< > == !=",
                        List.of(
                                new Token(TokenType.LT, "<"),
                                new Token(TokenType.GT, ">"),
                                new Token(TokenType.EQ, "=="),
                                new Token(TokenType.NOT_EQ, "!=")
                        )),

                // Logical/Unary Operators
                Arguments.of("should parse bang operator",
                        "!",
                        List.of(
                                new Token(TokenType.BANG, "!")
                        )),
                Arguments.of("should parse assignment",
                        "=",
                        List.of(
                                new Token(TokenType.ASSIGN, "=")
                        )),

                // Delimiters
                Arguments.of("should parse delimiters",
                        ", ;",
                        List.of(
                                new Token(TokenType.COMMA, ","),
                                new Token(TokenType.SEMICOLON, ";")
                        )),

                // Grouping Symbols
                Arguments.of("should parse grouping symbols",
                        "( ) { }",
                        List.of(
                                new Token(TokenType.LPAREN, "("),
                                new Token(TokenType.RPAREN, ")"),
                                new Token(TokenType.LBRACE, "{"),
                                new Token(TokenType.RBRACE, "}")
                        )),

                // Keywords
                Arguments.of("should parse function keyword",
                        "fn",
                        List.of(
                                new Token(TokenType.FUNCTION, "fn")
                        )),
                Arguments.of("should parse let keyword",
                        "let",
                        List.of(
                                new Token(TokenType.LET, "let")
                        )),
                Arguments.of("should parse if keyword",
                        "if",
                        List.of(
                                new Token(TokenType.IF, "if")
                        )),
                Arguments.of("should parse else keyword",
                        "else",
                        List.of(
                                new Token(TokenType.ELSE, "else")
                        )),
                Arguments.of("should parse return keyword",
                        "return",
                        List.of(
                                new Token(TokenType.RETURN, "return")
                        )),
                Arguments.of("should parse all keywords",
                        "fn let if else return",
                        List.of(
                                new Token(TokenType.FUNCTION, "fn"),
                                new Token(TokenType.LET, "let"),
                                new Token(TokenType.IF, "if"),
                                new Token(TokenType.ELSE, "else"),
                                new Token(TokenType.RETURN, "return")
                        )),

                // Multi-character token disambiguation
                Arguments.of("should distinguish between = and ==",
                        "= ==",
                        List.of(
                                new Token(TokenType.ASSIGN, "="),
                                new Token(TokenType.EQ, "==")
                        )),
                Arguments.of("should distinguish between ! and !=",
                        "! !=",
                        List.of(
                                new Token(TokenType.BANG, "!"),
                                new Token(TokenType.NOT_EQ, "!=")
                        )),

                // Complete integration test
                Arguments.of("should parse complete program",
                        """
                                let x = 5;
                                let y = 10;
                                fn add(a, b) {
                                  return a + b;
                                }
                                if (x < y) {
                                  true
                                } else {
                                  false
                                }
                                """,
                        List.of(
                                // let x = 5;
                                new Token(TokenType.LET, "let"),
                                new Token(TokenType.IDENT, "x"),
                                new Token(TokenType.ASSIGN, "="),
                                new Token(TokenType.INT, "5"),
                                new Token(TokenType.SEMICOLON, ";"),

                                // let y = 10;
                                new Token(TokenType.LET, "let"),
                                new Token(TokenType.IDENT, "y"),
                                new Token(TokenType.ASSIGN, "="),
                                new Token(TokenType.INT, "10"),
                                new Token(TokenType.SEMICOLON, ";"),

                                // fn add(a, b) {
                                new Token(TokenType.FUNCTION, "fn"),
                                new Token(TokenType.IDENT, "add"),
                                new Token(TokenType.LPAREN, "("),
                                new Token(TokenType.IDENT, "a"),
                                new Token(TokenType.COMMA, ","),
                                new Token(TokenType.IDENT, "b"),
                                new Token(TokenType.RPAREN, ")"),
                                new Token(TokenType.LBRACE, "{"),

                                // return a + b;
                                new Token(TokenType.RETURN, "return"),
                                new Token(TokenType.IDENT, "a"),
                                new Token(TokenType.PLUS, "+"),
                                new Token(TokenType.IDENT, "b"),
                                new Token(TokenType.SEMICOLON, ";"),

                                // }
                                new Token(TokenType.RBRACE, "}"),

                                // if (x < y) {
                                new Token(TokenType.IF, "if"),
                                new Token(TokenType.LPAREN, "("),
                                new Token(TokenType.IDENT, "x"),
                                new Token(TokenType.LT, "<"),
                                new Token(TokenType.IDENT, "y"),
                                new Token(TokenType.RPAREN, ")"),
                                new Token(TokenType.LBRACE, "{"),

                                // true
                                new Token(TokenType.TRUE, "true"),

                                // } else {
                                new Token(TokenType.RBRACE, "}"),
                                new Token(TokenType.ELSE, "else"),
                                new Token(TokenType.LBRACE, "{"),

                                // false
                                new Token(TokenType.FALSE, "false"),

                                // }
                                new Token(TokenType.RBRACE, "}")
                        ))
        );
    }
}
