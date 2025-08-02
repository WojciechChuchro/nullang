package com.nullang.lexer;

import com.nullang.token.Token;
import com.nullang.token.TokenType;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@Component
public class Lexer implements AutoCloseable {
    private final Reader reader;
    private int currentChar;
    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("let", TokenType.LET);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("return", TokenType.RETURN);
        keywords.put("fn", TokenType.FUNCTION);
    }

    public Lexer(Reader reader) throws IOException {
        this.reader = reader;
        this.currentChar = reader.read();
    }

    public boolean hasNext() {
        return currentChar != -1;
    }

    private void skipWhitespace() throws IOException {
        while (Character.isWhitespace(currentChar)) {
            readChar();
        }
    }

    public char readChar() throws IOException {
        char result = (char) currentChar;
        currentChar = reader.read();
        return result;
    }

    public Token nextToken() throws IOException {
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
            case -1:
                return new Token(TokenType.EOF, "");
            default:
                if (isLetter()) {
                    String identifier = readIdentifier();
                    return lokkupIdentifier(identifier); 
                }

                token = new Token(TokenType.ILLEGAL, String.valueOf((char) currentChar));
                break;
        }

        readChar();
        return token;
    }

    private String readIdentifier() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (isLetter()) {
            System.out.println((char) currentChar);
            sb.append((char) currentChar);
            readChar();
        }

        return sb.toString();
    }

    private Token lokkupIdentifier(String identifier) {
        TokenType type = keywords.getOrDefault(identifier, TokenType.IDENT);
        return new Token(type, identifier);
    }

    private boolean isLetter() {
        return 'a' <= currentChar && currentChar <= 'z'
                || 'A' <= currentChar && currentChar <= 'Z'
                || currentChar == '_';
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
