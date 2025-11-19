package com.nullang.parser;

import com.nullang.ast.*;
import com.nullang.ast.expression.CallExpression;
import com.nullang.ast.expression.IfExpression;
import com.nullang.ast.expression.InfixExpression;
import com.nullang.ast.expression.PrefixExpression;
import com.nullang.ast.statement.*;
import com.nullang.lexer.Lexer;
import com.nullang.parser.errors.ParserException;
import com.nullang.token.Token;
import com.nullang.token.TokenType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Parser implements AutoCloseable {
    private final Lexer lexer;
    private Token curToken;
    private Token peekToken;
    private final Map<TokenType, Supplier<Expression>> prefixParseFns = Map.ofEntries(
            Map.entry(TokenType.IDENT, this::parseIdentifier),
            Map.entry(TokenType.INT, this::parseInteger),
            Map.entry(TokenType.BANG, this::parsePrefixExpression),
            Map.entry(TokenType.MINUS, this::parsePrefixExpression),
            Map.entry(TokenType.TRUE, this::parseBoolean),
            Map.entry(TokenType.FALSE, this::parseBoolean),
            Map.entry(TokenType.LPAREN, this::parseGroupedExpression),
            Map.entry(TokenType.IF, this::parseIfExpression),
            Map.entry(TokenType.FUNCTION, this::parseFnExpression)
    );

    private final Map<TokenType, Function<Expression, Expression>> infixParseFns = Map.ofEntries(
            Map.entry(TokenType.PLUS, this::parseInfixExpression),
            Map.entry(TokenType.MINUS, this::parseInfixExpression),
            Map.entry(TokenType.SLASH, this::parseInfixExpression),
            Map.entry(TokenType.ASTERISK, this::parseInfixExpression),
            Map.entry(TokenType.EQ, this::parseInfixExpression),
            Map.entry(TokenType.NOT_EQ, this::parseInfixExpression),
            Map.entry(TokenType.LT, this::parseInfixExpression),
            Map.entry(TokenType.GT, this::parseInfixExpression),
            Map.entry(TokenType.LPAREN, this::parseCallExpression)
    );

    public Parser(Lexer lexer) {
        this.lexer = lexer;

        curToken = lexer.nextToken();
        peekToken = lexer.nextToken();
    }

    public Program parseProgram() {
        Program program = new Program();

        while (curToken.type() != TokenType.EOF) {
            Optional<Statement> statement = parseStatement();
            statement.ifPresent(program.statements::add);
            nextToken();
        }

        return program;
    }

    private Optional<Statement> parseStatement() {
        return switch (curToken.type()) {
            case TokenType.LET ->
                    parseLetStatement();
            case TokenType.RETURN ->
                    parseReturnStatement();
            default ->
                    parseExpressionStatement();
        };
    }

    private Optional<Statement> parseLetStatement() {
        LetStatement st = new LetStatement(curToken);
        if (!consumeIfPeek(TokenType.IDENT)) {
            throw new ParserException("Peek should be variable name!" + peekToken);
        }

        st.setName(new Identifier(curToken, curToken.literal()));

        if (!consumeIfPeek(TokenType.ASSIGN)) {
            throw new ParserException("Expected '=' after identifier" + peekToken);
        }

        nextToken();
        parseExpression(Precedences.LOWEST).ifPresent(st::setValue);
        while (peekToken.type() == TokenType.SEMICOLON) {
            nextToken();
        }

        return Optional.of(st);
    }

    private Optional<Statement> parseReturnStatement() {
        ReturnStatement stm = new ReturnStatement(curToken);

        nextToken();
        parseExpression(Precedences.LOWEST).ifPresent(stm::setReturnValue);
        while (curToken.type() != TokenType.SEMICOLON) {
            nextToken();
        }

        return Optional.of(stm);
    }

    private Optional<Statement> parseExpressionStatement() {
        Token stmtToken = curToken;
        Optional<Expression> optionalExpr = parseExpression(Precedences.LOWEST);

        if (optionalExpr.isEmpty()) {
            return Optional.empty();
        }

        Expression expr = optionalExpr.get();
        Statement stmt = new ExpressionStatement(stmtToken, expr);

        if (peekToken.type() == TokenType.SEMICOLON) {
            nextToken();
        }

        return Optional.of(stmt);
    }

    private Expression parseCallExpression(Expression function) {
        return new CallExpression(curToken, function, parseArguments());
    }

    private List<Expression> parseArguments() {
        List<Expression> arguments = new ArrayList<>();
        if (peekToken.type() == TokenType.RPAREN) {
            nextToken();
            return arguments;
        }

        nextToken();
        parseExpression(Precedences.LOWEST).ifPresent(arguments::add);
        while (peekToken.type() == TokenType.COMMA) {
            nextToken();
            nextToken();
            parseExpression(Precedences.LOWEST).ifPresent(arguments::add);
        }

        consumeIfPeek(TokenType.RPAREN);

        return arguments;
    }

    private Expression parseFnExpression() {
        Token cur = curToken;
        if (!consumeIfPeek(TokenType.LPAREN)) {
            return null;
        }
        List<Identifier> parameters = parseParameters();

        if (!consumeIfPeek(TokenType.LBRACE)) {
            return null;
        }

        BlockStatement body = parseBlockStatement();

        return new FnStatement(cur, parameters, body);
    }

    private List<Identifier> parseParameters() {
        List<Identifier> parameters = new ArrayList<>();

        if (peekToken.type() == TokenType.RPAREN) {
            nextToken();
            return parameters;
        }
        nextToken();
        Identifier first = new Identifier(curToken, curToken.literal());
        parameters.add(first);

        while (peekToken.type() == TokenType.COMMA) {
            nextToken();
            nextToken();

            Identifier ident = new Identifier(curToken, curToken.literal());
            parameters.add(ident);
        }

        if (!consumeIfPeek(TokenType.RPAREN)) {
            nextToken();
            return parameters;
        }

        return parameters;
    }

    private Expression parseIfExpression() {
        if (!consumeIfPeek(TokenType.LPAREN)) {
            return null;
        }
        nextToken();
        Optional<Expression> condition = parseExpression(Precedences.LOWEST);

        if (!consumeIfPeek(TokenType.RPAREN)) {
            return null;
        }

        if (!consumeIfPeek(TokenType.LBRACE)) {
            return null;
        }
        BlockStatement consequence = parseBlockStatement();

        BlockStatement alternative = null;
        if (consumeIfPeek(TokenType.ELSE)) {
            nextToken();
            if (consumeIfPeek(TokenType.LBRACE)) {
                return null;
            }

            alternative = parseBlockStatement();
        }

        Expression con = condition.orElseThrow(() -> new ParserException("No condition provided"));
        return new IfExpression(curToken, con, alternative, consequence);
    }

    private BlockStatement parseBlockStatement() {
        BlockStatement block = new BlockStatement(this.curToken);

        nextToken();
        while (currentTokenIs(TokenType.RBRACE) && currentTokenIs(TokenType.EOF)) {
            Optional<Statement> st = parseStatement();
            st.ifPresent(
                    block::addStatement);
            nextToken();
        }

        return block;
    }

    private Expression parseGroupedExpression() {
        nextToken();
        Optional<Expression> exp = parseExpression(Precedences.LOWEST);

        if (peekToken.type() != TokenType.RPAREN) {
            return null;
        }

        nextToken();

        return exp.orElseThrow(() -> new ParserException("No expression provided"));
    }

    private Expression parseInfixExpression(Expression left) {
        InfixExpression ex = new InfixExpression(curToken, curToken.literal());
        ex.setLeft(left);

        int p = curPrecedence();
        nextToken();

        ex.setRight(parseExpression(p).get());

        return ex;
    }

    private Expression parsePrefixExpression() {
        PrefixExpression exp = new PrefixExpression(curToken, curToken.literal());

        nextToken();
        parseExpression(Precedences.PREFIX).ifPresent(exp::setRight);

        return exp;
    }

    private Expression parseBoolean() {
        return new BooleanIdentifier(this.curToken.type() == TokenType.TRUE, this.curToken);
    }

    private Expression parseInteger() {
        return new IntegerIdentifier(curToken, Integer.parseInt(curToken.literal()));
    }

    private Expression parseIdentifier() {
        return new Identifier(curToken, curToken.literal());
    }

    private Optional<Expression> parseExpression(int lowest) {
        Supplier<Expression> prefix = this.prefixParseFns.getOrDefault(curToken.type(), null);

        if (prefix == null) {
            return Optional.empty();
        }

        Expression left = prefix.get();
        while (peekToken.type() != TokenType.SEMICOLON && lowest < peekPrecedence()) {
            Function<Expression, Expression> infix = this.infixParseFns.get(peekToken.type());

            if (infix == null) {
                return Optional.of(left);
            }

            nextToken();
            left = infix.apply(left);
        }

        return Optional.of(left);
    }



    private boolean currentTokenIs(TokenType tokenType) {
        return curToken.type() != tokenType;
    }

    private boolean consumeIfPeek(TokenType type) {
        if (peekToken.type() == type) {
            nextToken();
            return true;
        }
        return false;
    }

    private int peekPrecedence() {
        return PrecedenceManager.getPrecedence(this.peekToken.type());
    }

    private int curPrecedence() {
        return PrecedenceManager.getPrecedence(this.curToken.type());
    }

    private void nextToken() {
        curToken = peekToken;
        peekToken = lexer.nextToken();
    }

    @Override
    public void close() {
        this.lexer.close();
    }
}
