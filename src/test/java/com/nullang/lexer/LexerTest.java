package com.nullang.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nullang.token.Token;
import com.nullang.token.TokenType;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

public class LexerTest {
    Reader def = new StringReader("{}();&let return five 34 ! = < >");
    Reader math = new StringReader("* / + -");

    @Test
    void testLexerReadsInput() throws Exception {
        try (Lexer lexer = new Lexer(def); ) {
            Token[] expectedTokens = {
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.ILLEGAL, "&"),
                new Token(TokenType.LET, "let"),
                new Token(TokenType.RETURN, "return"),
                new Token(TokenType.IDENT, "five"),
                new Token(TokenType.INT, "34"),
                new Token(TokenType.BANG, "!"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.LT, "<"),
                new Token(TokenType.GT, ">"),
                new Token(TokenType.EOF, "")
            };
            for (Token expected : expectedTokens) {
                Token actual = lexer.nextToken();
                assertEquals(expected.literal, actual.literal);
                assertEquals(expected.type, actual.type);
            }
        }
    }

    @Test
    void testMathOperators() throws Exception {

        try (Lexer lexer = new Lexer(math); ) {
            Token[] expectedTokens = {
                new Token(TokenType.ASTERISK, "*"),
                new Token(TokenType.SLASH, "/"),
                new Token(TokenType.PLUS, "+"),
                new Token(TokenType.MINUS, "-"),
            };
            for (Token expected : expectedTokens) {
                Token actual = lexer.nextToken();
                assertEquals(expected.literal, actual.literal);
                assertEquals(expected.type, actual.type);
            }
        }
    }
}
