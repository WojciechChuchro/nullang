package com.nullang.token;

public enum TokenType {
    // Special tokens
    ILLEGAL,
    EOF,

    // Identifiers and Literals
    IDENT,
    INT,
    TRUE,
    FALSE,

    // Arithmetic Operators
    PLUS,
    MINUS,
    ASTERISK,
    SLASH,

    // Comparison Operator
    LT,
    GT,
    EQ,
    NOT_EQ,

    // Logical unary operators
    ASSIGN,
    BANG,

    // Delimiters
    COMMA,
    SEMICOLON,

    // Grouping symbols
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,

    // Keywords
    FUNCTION,
    LET,
    IF,
    ELSE,
    RETURN,
}
