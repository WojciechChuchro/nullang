package com.nullang.parser;

import com.nullang.ast.BooleanIdentifier;
import com.nullang.ast.Expression;
import com.nullang.ast.Identifier;
import com.nullang.ast.IntegerIdentifier;
import com.nullang.ast.Program;
import com.nullang.ast.Statement;
import com.nullang.ast.expression.InfixExpression;
import com.nullang.ast.expression.PrefixExpression;
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
import java.util.function.Function;
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
    private Map<TokenType, Function<Expression, Expression>> infixParseFns = new HashMap<>();

    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        this.registerPrefix(TokenType.IDENT, () -> new Identifier(curToken, curToken.literal));
        this.registerPrefix(
                TokenType.INT,
                () -> new IntegerIdentifier(curToken, Integer.parseInt(curToken.literal)));
        this.registerPrefix(TokenType.BANG, () -> parsePrefixExpression());
        this.registerPrefix(TokenType.MINUS, () -> parsePrefixExpression());
        this.registerPrefix(TokenType.TRUE, () -> parseBoolean());
        this.registerPrefix(TokenType.FALSE, () -> parseBoolean());
        this.registerPrefix(TokenType.LPAREN, () -> parseGroupedExpression());

        this.registerInfix(TokenType.PLUS, (Expression left) -> parseInfixExpression(left));
        this.registerInfix(TokenType.MINUS, (Expression left) -> parseInfixExpression(left));
        this.registerInfix(TokenType.SLASH, (Expression left) -> parseInfixExpression(left));
        this.registerInfix(TokenType.ASTERISK, (Expression left) -> parseInfixExpression(left));
        this.registerInfix(TokenType.EQ, (Expression left) -> parseInfixExpression(left));
        this.registerInfix(TokenType.NOT_EQ, (Expression left) -> parseInfixExpression(left));
        this.registerInfix(TokenType.LT, (Expression left) -> parseInfixExpression(left));
        this.registerInfix(TokenType.GT, (Expression left) -> parseInfixExpression(left));
        nextToken();
        nextToken();
    }

    private Expression parseGroupedExpression() {
        nextToken();
        Optional<Expression> exp = parseExpression(Precedences.LOWEST);

        if (peekToken.type != TokenType.RPAREN) {
            log.error(
                    "Invalid type for RPAREN in parse grouped expression for current token: "
                            + curToken.type);
            return null;
        }

        nextToken();

        return exp.orElseGet(
                () -> {
                    log.error("Invalid expression in parsegrouped expression! ");
                    return null;
                });
    }

    private Expression parseInfixExpression(Expression left) {
        InfixExpression ex = new InfixExpression(curToken, curToken.literal);
        ex.setLeft(left);

        int p = curPrecedence();
        nextToken();

        ex.setRight(parseExpression(p).get());

        return ex;
    }

    private Expression parsePrefixExpression() {
        PrefixExpression exp = new PrefixExpression(curToken, curToken.literal);

        nextToken();
        parseExpression(Precedences.PREFIX).ifPresent(exp::setRight);

        return exp;
    }

    private Expression parseBoolean() {
        return new BooleanIdentifier(this.curToken.type == TokenType.TRUE, this.curToken);
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
        Program p = new Program();

        while (curToken.type != TokenType.EOF) {
            log.info("parsing token: " + curToken);
            Optional<Statement> statement = parseStatement();
            statement.ifPresent(s -> p.statements.add(s));
            nextToken();
        }

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

    private final void registerInfix(TokenType type, Function<Expression, Expression> fn) {
        this.infixParseFns.put(type, fn);
    }

    private Optional<Statement> parseExpressionStatement() {
        Token stmtToken = curToken;
        Optional<Expression> optionalExpr = parseExpression(Precedences.LOWEST);

        if (optionalExpr.isEmpty()) {
            log.error("No prefix parse function found for: " + curToken.type);
            return Optional.empty();
        }

        Expression expr = optionalExpr.get();
        Statement stmt = new ExpressionStatement(stmtToken, expr);

        if (peekToken.type == TokenType.SEMICOLON) {
            log.info("Skipping semicolon");
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
        while (peekToken.type != TokenType.SEMICOLON && lowest < peekPrecedence()) {
            Function<Expression, Expression> infix = this.infixParseFns.get(peekToken.type);

            if (infix == null) {
                return Optional.of(left);
            }

            nextToken();
            left = infix.apply(left);
        }

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

    private int peekPrecedence() {
        return this.PRECEDENCES.getOrDefault(this.peekToken.type, Precedences.LOWEST);
    }

    private int curPrecedence() {
        return this.PRECEDENCES.getOrDefault(this.curToken.type, Precedences.LOWEST);
    }

    public Token getCurToken() {
        return curToken;
    }

    public Token getPeekToken() {
        return peekToken;
    }

    @Override
    public void close() throws IOException {
        this.lexer.close();
    }
}
