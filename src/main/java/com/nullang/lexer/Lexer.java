package com.nullang.lexer;

import com.nullang.token.Token;
import com.nullang.token.TokenType;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;

@Component
public class Lexer implements AutoCloseable {
    private final Reader reader;
    private int currentChar;

    public Lexer(Reader reader) {
        this.reader = reader;
    }

    public boolean hasNext() {
        return currentChar != -1;
    }

    private void skipWhitespace() throws IOException {
        while (hasNext() && Character.isWhitespace(currentChar)) {
            nextChar();
        }
    }

    public char nextChar() throws IOException {
        char result = (char) currentChar;
        currentChar = reader.read();
        return result;
    }

    public Token nextToken() throws IOException {
        skipWhitespace();

        Token token;
        nextChar();

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
            case -1:
                token = new Token(TokenType.EOF, "");
                break;
            default:
                token = new Token(TokenType.ILLEGAL, String.valueOf((char) currentChar));
                break;
        }

        return token;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
