package com.nullang.lexer;

import com.nullang.token.Token;
import com.nullang.token.TokenType;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class Lexer implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(Lexer.class);
    private final Reader reader;
    private int currentChar;
    private int peekedChar;
    private static final Map<String, TokenType> keywords = Map.of(
            "fn", TokenType.FUNCTION,
            "let", TokenType.LET,
            "if", TokenType.IF,
            "else", TokenType.ELSE,
            "return", TokenType.RETURN,
            "true", TokenType.TRUE,
            "false", TokenType.FALSE
    );


    public Lexer(Reader reader) {
        this.reader = reader;
        try {
            this.currentChar = reader.read();
            this.peekedChar = reader.read();
        } catch (IOException e) {
            log.error("Exception while creating Lexer object {}", e.getMessage(), e);
        }
    }

    public Token nextToken() {
        skipWhitespace();

        Token token;

        switch (currentChar) {
            case '{':
                token = new Token(TokenType.LBRACE, "{");
                break;
            case '}':
                token = new Token(TokenType.RBRACE, "}");
                break;
            case '(':
                token = new Token(TokenType.LPAREN, "(");
                break;
            case ')':
                token = new Token(TokenType.RPAREN, ")");
                break;
            case ';':
                token = new Token(TokenType.SEMICOLON, ";");
                break;
            case '<':
                token = new Token(TokenType.LT, "<");
                break;
            case '>':
                token = new Token(TokenType.GT, ">");
                break;
            case ',':
                token = new Token(TokenType.COMMA, ",");
                break;
            case '=':
                if (peekedChar == '=') {
                    readChar();
                    token = new Token(TokenType.EQ, "==");
                    break;
                }
                token = new Token(TokenType.ASSIGN, "=");
                break;
            case '!':
                if (peekedChar == '=') {
                    readChar();
                    token = new Token(TokenType.NOT_EQ, "!=");
                    break;
                }
                token = new Token(TokenType.BANG, "!");
                break;
            case '-':
                token = new Token(TokenType.MINUS, "-");
                break;
            case '+':
                token = new Token(TokenType.PLUS, "+");
                break;
            case '*':
                token = new Token(TokenType.ASTERISK, "*");
                break;
            case '/':
                token = new Token(TokenType.SLASH, "/");
                break;
            case -1:
                return new Token(TokenType.EOF, "");
            default:
                if (isLetterOrUnderscore((char) currentChar)) {
                    String identifier = readIdentifier();
                    return lockupIdentifier(identifier);
                } else if (Character.isDigit(currentChar)) {
                    String number = readNumber();
                    return new Token(TokenType.INT, number);
                }

                token = new Token(TokenType.ILLEGAL, String.valueOf((char) currentChar));
                break;
        }

        readChar();
        return token;
    }

    private void readChar() {
        try {
            currentChar = peekedChar;
            peekedChar = reader.read();
        } catch (IOException e) {
            log.error("Exception while reading character {}", e.getMessage(), e);
        }
    }

    private String readIdentifier() {
        StringBuilder sb = new StringBuilder();

        while (isLetterOrUnderscore((char) currentChar)) {
            sb.append((char) currentChar);
            readChar();
        }

        return sb.toString();
    }

    private String readNumber() {
        StringBuilder sb = new StringBuilder();

        while (Character.isDigit(currentChar)) {
            sb.append((char) currentChar);
            readChar();
        }

        return sb.toString();
    }

    private Token lockupIdentifier(String identifier) {
        TokenType type = keywords.getOrDefault(identifier, TokenType.IDENT);
        return new Token(type, identifier);
    }

    private static boolean isLetterOrUnderscore(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private void skipWhitespace() {
        while (Character.isWhitespace(currentChar)) {
            readChar();
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            log.error("Exception while closing reader {}", e.getMessage(), e);
        }
    }
}
