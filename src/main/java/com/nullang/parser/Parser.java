package com.nullang.parser;

import com.nullang.ast.Program;
import com.nullang.ast.Statement;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.ast.statement.LetStatement;
import com.nullang.ast.statement.ReturnStatement;
import com.nullang.lexer.Lexer;
import com.nullang.parser.errors.ParserException;
import com.nullang.token.Token;
import com.nullang.token.TokenType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.Expression;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class Parser implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(Parser.class);

    private final Lexer lexer;

    private Token curToken;
    private Token peekToken;
    private static final Map<TokenType, Integer> PRECEDENCES =
            Map.of(
                    TokenType.EQ, Precedences.EQUALS,
                    TokenType.NOT_EQ, Precedences.EQUALS,
                    TokenType.LT, Precedences.LESSGREATER,
                    TokenType.GT, Precedences.LESSGREATER,
                    TokenType.PLUS, Precedences.SUM,
                    TokenType.MINUS, Precedences.SUM,
                    TokenType.SLASH, Precedences.PRODUCT,
                    TokenType.ASTERISK, Precedences.PRODUCT);

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;

        nextToken();
        nextToken();
    }

    private void nextToken() {
        curToken = peekToken;
        try {
            peekToken = lexer.nextToken();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Program parseProgram() throws IOException {
        log.info("Started parsing");
        Program p = new Program();

        while (peekToken.type != TokenType.EOF) {
            Optional<Statement> statement = parseStatement();
            statement.ifPresent(s -> p.statements.add(s));
            nextToken();
        }

        log.info("end parsing");
        return p;
    }

    private Optional<Statement> parseStatement() {
        switch (curToken.type) {
            case TokenType.LET:
                return parseLetStatement();
            case TokenType.RETURN:
                return parseReturnStatement();
            default:
                return parseExpressionStatement();
        }
    }

    private Optional<Statement> parseExpressionStatement() {
        Expression ex = parseExpression(Precedences.LOWEST);
        Statement stm = new ExpressionStatement(curToken, ex);

        return Optional.of(stm);
    }

    private Expression parseExpression(int lowest) {
        int prefix = PRECEDENCES.get(lowest);


        
    }

    private Optional<Statement> parseReturnStatement() {
        Statement stm = new ReturnStatement(curToken);

        nextToken();
        while (curToken.type != TokenType.SEMICOLON) {
            nextToken();
        }

        return Optional.of(stm);
    }

    private Optional<Statement> parseLetStatement() {
        Statement st = new LetStatement(curToken);
        if (!expectPeek(TokenType.IDENT)) {
            throw new ParserException("Peek should be variable name!" + peekToken);
        }

        nextToken();
        if (!expectPeek(TokenType.ASSIGN)) {
            throw new ParserException("Expected '=' after identifier" + peekToken);
        }

        // TODO: right now only skip till semicol
        nextToken();
        while (!currentTokenIs(TokenType.SEMICOLON)) {
            log.info("Skipping semicol");
            nextToken();
        }

        return Optional.of(st);
    }


    private boolean currentTokenIs(TokenType tokenType) {
        return curToken.type == tokenType;
    }

    private boolean expectPeek(TokenType type) {
        return peekToken.type == type;
    }

    public Token getCurToken() {
        return curToken;
    }

    public Token getPeekToken() {
        return peekToken;
    }

    private int peekPrecedence() {
        return PRECEDENCES.getOrDefault(peekToken.type, Precedences.LOWEST);
    }

    private int curPrecedence() {
        return PRECEDENCES.getOrDefault(curToken.type, Precedences.LOWEST);
    }

    @Override
    public void close() throws IOException {
        this.lexer.close();
    }
}
