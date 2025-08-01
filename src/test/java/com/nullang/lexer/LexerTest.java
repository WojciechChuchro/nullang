package com.nullang.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nullang.token.Token;
import com.nullang.token.TokenType;

import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

public class LexerTest {
    Reader def = new StringReader("{}();&");

    @Test
    void testLexerReadsInput() throws Exception {
        try (Lexer lexer = new Lexer(def); ) {
            Token[] expectedTokens = {
                new Token(TokenType.LBRACE, "{"),
                new Token(TokenType.RBRACE, "}"),
                new Token(TokenType.LPAREN, "("),
                new Token(TokenType.RPAREN, ")"),
                new Token(TokenType.SEMICOLON, ";"),
                new Token(TokenType.ILLEGAL, "&")
            };
            for (Token expected : expectedTokens) {
                Token actual = lexer.nextToken();
                assertEquals(expected.type, actual.type);
                assertEquals(expected.literal, actual.literal);
            }
        }
    }
}
