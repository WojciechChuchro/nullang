package com.nullang.parser;

import com.nullang.token.TokenType;

import java.util.Map;

public class PrecedenceManager {
    private static final Map<TokenType, Integer> PRECEDENCES =
            Map.of(
                    TokenType.EQ, Precedences.EQUALS,
                    TokenType.NOT_EQ, Precedences.EQUALS,
                    TokenType.LT, Precedences.LESS_GREATER,
                    TokenType.GT, Precedences.LESS_GREATER,
                    TokenType.PLUS, Precedences.SUM,
                    TokenType.MINUS, Precedences.SUM,
                    TokenType.SLASH, Precedences.PRODUCT,
                    TokenType.LPAREN, Precedences.CALL,
                    TokenType.ASTERISK, Precedences.PRODUCT);

    public static int getPrecedence(TokenType type) {
        return PRECEDENCES.getOrDefault(type, Precedences.LOWEST);
    }
}
