package com.nullang.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nullang.token.Token;
import com.nullang.token.TokenType;

import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

public class LexerTest {

    private void assertTokens(String input, Token... expectedTokens) throws Exception {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader)) {
            for (Token expected : expectedTokens) {
                Token actual = lexer.nextToken();
                assertEquals(expected.literal, actual.literal);
                assertEquals(expected.type, actual.type);
            }
        }
    }

    @Test
    void testDelimiters() throws Exception {
        assertTokens(
                "{}()",
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.RPAREN, ")"));
    }

    @Test
    void testSemicolon() throws Exception {
        assertTokens(";", new Token(TokenType.SEMICOLON, ";"));
    }

    @Test
    void testIdentifiers() throws Exception {
        assertTokens(
                "five myVar",
                new Token(TokenType.IDENT, "five"),
                new Token(TokenType.IDENT, "myVar"));
    }

    @Test
    void testIntegers() throws Exception {
        assertTokens("34 123", new Token(TokenType.INT, "34"), new Token(TokenType.INT, "123"));
    }

    @Test
    void testIllegalCharacters() throws Exception {
        assertTokens("&", new Token(TokenType.ILLEGAL, "&"));
    }

    @Test
    void testBasicArithmeticOperators() throws Exception {
        assertTokens(
                "+ - * /",
                new Token(TokenType.PLUS, "+"),
                new Token(TokenType.MINUS, "-"),
                new Token(TokenType.ASTERISK, "*"),
                new Token(TokenType.SLASH, "/"));
    }

    @Test
    void testComparisonOperators() throws Exception {
        assertTokens(
                "< > !",
                new Token(TokenType.LT, "<"),
                new Token(TokenType.GT, ">"),
                new Token(TokenType.BANG, "!"));
    }

    @Test
    void testAssignmentOperator() throws Exception {
        assertTokens("=", new Token(TokenType.ASSIGN, "="));
    }

    @Test
    void testEqualityOperators() throws Exception {
        assertTokens("== !=", new Token(TokenType.EQ, "=="), new Token(TokenType.NOT_EQ, "!="));
    }

    @Test
    void testKeywords() throws Exception {
        assertTokens(
                "fn let", new Token(TokenType.FUNCTION, "fn"), new Token(TokenType.LET, "let"));
    }

    @Test
    void testConditionalKeywords() throws Exception {
        assertTokens("if else", new Token(TokenType.IF, "if"), new Token(TokenType.ELSE, "else"));
    }

    @Test
    void testControlFlowKeywords() throws Exception {
        assertTokens("return", new Token(TokenType.RETURN, "return"));
    }

    @Test
    void testBooleanLiterals() throws Exception {
        assertTokens(
                "true false",
                new Token(TokenType.TRUE, "true"),
                new Token(TokenType.FALSE, "false"));
    }

    @Test
    void testEndOfFile() throws Exception {
        assertTokens("", new Token(TokenType.EOF, ""));
    }

    @Test
    void testSimpleExpression() throws Exception {
        assertTokens(
                "let x = 5;",
                new Token(TokenType.LET, "let"),
                new Token(TokenType.IDENT, "x"),
                new Token(TokenType.ASSIGN, "="),
                new Token(TokenType.INT, "5"),
                new Token(TokenType.SEMICOLON, ";"));
    }

    @Test
    void testConsecutiveOperators() throws Exception {
        assertTokens("==!=", new Token(TokenType.EQ, "=="), new Token(TokenType.NOT_EQ, "!="));
    }
}
