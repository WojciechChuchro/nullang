package com.nullang.token;

public enum TokenType {
    ILLEGAL,
    EOF,

    IDENT,
    INT,

    PLUS,
    MINUS,
    ASTERISK,
    SLASH,
    ASSIGN,
    BANG,
    LT,
    GT,

    COMMA,
    SEMICOLON,

    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,

    FUNCTION,
    LET,
    IF,
    ELSE,
    RETURN,
    TRUE,
    FALSE,

    EQ,
    NOT_EQ
}
