package com.nullang.parser;

import com.nullang.ast.Expression;
import com.nullang.ast.Identifier;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

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

    private Map<TokenType, Supplier<Expression>> prefixParseFns = new HashMap<>();

    // private Map<TokenType, InfixParseFn> infixParseFns = new HashMap<>();

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        prefixParseFns.put(TokenType.IDENT, () -> new Identifier(curToken, curToken.literal));

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

        while (curToken.type != TokenType.EOF) {
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

    private final void registerPrefix(TokenType type, Supplier<Expression> fn) {
        this.prefixParseFns.put(type, fn);
    }

    private Optional<Statement> parseExpressionStatement() {
        Optional<Expression> optionalExpr = parseExpression(Precedences.LOWEST);

        if (optionalExpr.isEmpty()) {
            return Optional.empty();
        }

        Expression expr = optionalExpr.get();
        Statement stmt = new ExpressionStatement(curToken, expr);

        if (peekToken.type == TokenType.SEMICOLON) {
            nextToken();
        }

        return Optional.of(stmt);
    }

    private Optional<Expression> parseExpression(int lowest) {
        Supplier<Expression> prefix = this.prefixParseFns.getOrDefault(curToken.type, null);

        if (prefix == null) {
            return Optional.empty();
        }

        Expression left = prefix.get();
        return Optional.of(left);
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
        if (peekToken.type == type) {
            nextToken();
            return true;
        }
        return false;
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
