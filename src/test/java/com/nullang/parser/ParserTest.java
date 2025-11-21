package com.nullang.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.nullang.ast.BooleanIdentifier;
import com.nullang.ast.Identifier;
import com.nullang.ast.IntegerIdentifier;
import com.nullang.ast.Program;
import com.nullang.ast.statement.Statement;
import com.nullang.ast.expression.CallExpression;
import com.nullang.ast.expression.IfExpression;
import com.nullang.ast.expression.InfixExpression;
import com.nullang.ast.expression.PrefixExpression;
import com.nullang.ast.statement.BlockStatement;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.ast.statement.FnStatement;
import com.nullang.ast.statement.LetStatement;
import com.nullang.ast.statement.ReturnStatement;
import com.nullang.lexer.Lexer;
import com.nullang.parser.errors.ParserException;
import com.nullang.token.TokenType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ParserTest {
    private Program parseInput(String input) throws IOException {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader);
                Parser parser = new Parser(lexer)) {
            return parser.parseProgram();
        }
    }

    private ParserException parseInputExpectingException(String input) {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader);
                Parser parser = new Parser(lexer)) {
            return assertThrowsExactly(ParserException.class, parser::parseProgram);
        }
    }

    private void testBooleanLiteral(BooleanIdentifier booleanLiteral, boolean value) {
        System.out.println(booleanLiteral.tokenLiteral());
        assertThat(booleanLiteral.value).isEqualTo(value);
        assertThat(booleanLiteral.tokenLiteral()).isEqualTo(String.valueOf(value));
    }

    @Test
    void testeLetStatement() throws IOException {
        Program program =
                parseInput(
                        """
                        let x = 5;
                        """);

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        Statement stmt = program.statements.get(0);
        assertTrue(stmt instanceof LetStatement, "Statement " + 0 + " should be LetStatement");
        assertEquals("let", stmt.tokenLiteral());

        LetStatement ls = (LetStatement) stmt;
        assertThat(ls.getName().toString()).isEqualTo("x");
        assertThat(ls.getValue()).isInstanceOf(IntegerIdentifier.class);
        assertThat(((IntegerIdentifier) ls.getValue()).getValue()).isEqualTo(5);
        assertThat(((IntegerIdentifier) ls.getValue()).toString()).isEqualTo("5");
    }
    @Test
    void testeLetStatements() throws IOException {
        Program program =
                parseInput(
                        """
                        let x = 5;
                        let y = 10;
                        let foobar = 838383;
                        """);

        assertNotNull(program);
        assertEquals(3, program.statements.size());

        String[] expectedIdentifiers = {"x", "y", "foobar"};
        for (int i = 0; i < expectedIdentifiers.length; i++) {
            Statement stmt = program.statements.get(i);
            assertInstanceOf(LetStatement.class, stmt, "Statement " + i + " should be LetStatement");
            assertEquals("let", stmt.tokenLiteral());
        }
    }

    @Test
    void testReturnStatements() throws IOException {
        Program program =
                parseInput(
                        """
                        return 5;
                        return true;
                        """);

        assertNotNull(program);
        assertEquals(2, program.statements.size());

        assertInstanceOf(ReturnStatement.class, program.statements.get(0));
        ReturnStatement rs =(ReturnStatement) program.statements.get(0);
        ReturnStatement exp = (ReturnStatement) rs;

        assertThat(exp.getReturnValue()).isInstanceOf(IntegerIdentifier.class);
        IntegerIdentifier in = (IntegerIdentifier) exp.getReturnValue();
        assertThat(in.getValue()).isEqualTo(5);
        assertThat(in.toString()).isEqualTo("5");

        assertTrue(program.statements.get(1) instanceof ReturnStatement);
        ReturnStatement rs2 =(ReturnStatement) program.statements.get(1);
        ReturnStatement exp2 = (ReturnStatement) rs2;
        assertThat(exp2.getReturnValue()).isInstanceOf(BooleanIdentifier.class);
        BooleanIdentifier bn = (BooleanIdentifier) exp2.getReturnValue();
        assertThat(bn.toString()).isEqualTo("true");

    }

    @Test
    void testLetStatementMissingAssignment() throws IOException {
        ParserException ex = parseInputExpectingException("let x;");
        assertEquals("Expected '=' after identifier;", ex.getMessage());
    }

    @Test
    void testLetStatementMissingIdentifier() throws IOException {
        ParserException ex = parseInputExpectingException("let =;");
        assertEquals("Peek should be variable name!=", ex.getMessage());
    }

    @Test
    void testIdentifierExpression() throws IOException {
        Program program = parseInput("foobar;");

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        Statement stmt = program.statements.get(0);
        assertTrue(stmt instanceof ExpressionStatement);

        ExpressionStatement exprStmt = (ExpressionStatement) stmt;
        assertEquals("foobar", exprStmt.tokenLiteral());
        assertTrue(exprStmt.expression() instanceof Identifier);
        assertEquals("foobar", ((Identifier) exprStmt.expression()).getValue());
    }

    @Test
    void testIntegerExpression() throws IOException {
        Program program = parseInput("5;");

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        Statement stmt = program.statements.get(0);
        assertTrue(stmt instanceof ExpressionStatement);

        ExpressionStatement exprStmt = (ExpressionStatement) stmt;
        assertTrue(exprStmt.expression() instanceof IntegerIdentifier);
        assertEquals("5", exprStmt.tokenLiteral());
        assertEquals(5, ((IntegerIdentifier) exprStmt.expression()).getValue());
    }

    @Test
    void testMixedExpressions() throws IOException {
        Program program =
                parseInput(
                        """
                        foobar;
                        5;
                        """);

        assertNotNull(program);
        assertEquals(2, program.statements.size());

        // Test identifier expression
        Statement stmt1 = program.statements.get(0);
        assertTrue(stmt1 instanceof ExpressionStatement);
        ExpressionStatement exprStmt1 = (ExpressionStatement) stmt1;
        assertEquals("foobar", ((Identifier) exprStmt1.expression()).getValue());

        // Test integer expression
        Statement stmt2 = program.statements.get(1);
        assertTrue(stmt2 instanceof ExpressionStatement);
        ExpressionStatement exprStmt2 = (ExpressionStatement) stmt2;
        assertEquals(5, ((IntegerIdentifier) exprStmt2.expression()).getValue());
    }

    @Test
    void testBangPrefixExpression() throws IOException {
        Program program = parseInput("!5;");

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        Statement stmt = program.statements.get(0);
        assertTrue(stmt instanceof ExpressionStatement);

        ExpressionStatement exprStmt = (ExpressionStatement) stmt;
        assertTrue(exprStmt.expression() instanceof PrefixExpression);

        PrefixExpression prefixExpr = (PrefixExpression) exprStmt.expression();
        assertEquals("!", prefixExpr.getOperator());
        assertEquals("!", prefixExpr.tokenLiteral());

        assertNotNull(prefixExpr.getRight());
        assertTrue(prefixExpr.getRight() instanceof IntegerIdentifier);

        IntegerIdentifier right = (IntegerIdentifier) prefixExpr.getRight();
        assertEquals(5, right.getValue());
        assertEquals("5", right.tokenLiteral());
    }

    @Test
    void testMinusPrefixExpression() throws IOException {
        Program program = parseInput("-5;");

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        Statement stmt = program.statements.get(0);
        assertTrue(stmt instanceof ExpressionStatement);

        ExpressionStatement exprStmt = (ExpressionStatement) stmt;
        assertTrue(exprStmt.expression() instanceof PrefixExpression);

        PrefixExpression prefixExpr = (PrefixExpression) exprStmt.expression();
        assertEquals("-", prefixExpr.getOperator());

        assertTrue(prefixExpr.getRight() instanceof IntegerIdentifier);
        assertEquals(5, ((IntegerIdentifier) prefixExpr.getRight()).getValue());
    }

    @Test
    void testMultiplePrefixExpressions() throws IOException {
        Program program =
                parseInput(
                        """
                        !5;
                        -5;
                        """);

        assertNotNull(program);
        assertEquals(2, program.statements.size());

        // Test bang prefix
        ExpressionStatement stmt1 = (ExpressionStatement) program.statements.get(0);
        PrefixExpression prefix1 = (PrefixExpression) stmt1.expression();
        assertEquals("!", prefix1.getOperator());

        // Test minus prefix
        ExpressionStatement stmt2 = (ExpressionStatement) program.statements.get(1);
        PrefixExpression prefix2 = (PrefixExpression) stmt2.expression();
        assertEquals("-", prefix2.getOperator());
    }

    private void assertPrefixExpression(String input, String expectedOperator, int expectedValue)
            throws IOException {
        Program program = parseInput(input);

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.statements.get(0);
        PrefixExpression prefixExpr = (PrefixExpression) exprStmt.expression();

        assertEquals(expectedOperator, prefixExpr.getOperator());
        IntegerIdentifier right = (IntegerIdentifier) prefixExpr.getRight();
        assertEquals(expectedValue, right.getValue());
    }

    @Test
    void testPrefixExpressionOperators() throws IOException {
        assertPrefixExpression("!10;", "!", 10);
        assertPrefixExpression("-15;", "-", 15);
    }

    @Test
    void testMultipleInfixExpressions() throws IOException {
        Program program =
                parseInput(
                        """
                            5 + 4;
                            6 - 5;
                            5 * 4;
                            5 / 4;
                            5 > 4;
                            5 < 4;
                            5 == 4;
                            5 != 4;
                        """);

        assertNotNull(program);
        assertEquals(8, program.statements.size());

        ExpressionStatement stmt1 = (ExpressionStatement) program.statements.get(0);
        InfixExpression infix1 = (InfixExpression) stmt1.expression();
        assertEquals("5", infix1.getLeft().tokenLiteral());
        assertEquals("+", infix1.getOperator());
        assertEquals("4", infix1.getRight().tokenLiteral());

        ExpressionStatement stmt2 = (ExpressionStatement) program.statements.get(1);
        InfixExpression infix2 = (InfixExpression) stmt2.expression();
        assertEquals("6", infix2.getLeft().tokenLiteral());
        assertEquals("-", infix2.getOperator());
        assertEquals("5", infix2.getRight().tokenLiteral());
    }

    @Test
    void testMultipleOverallExpressions() throws IOException {
        Program program =
                parseInput(
                        """
                            2 + 2 * 2;
                        """);

        assertNotNull(program);
        assertEquals(1, program.statements.size());
        // TODO: it method program.toString() should return input as examble below
        // assertEquals("2 + (2 * 2)", program.toString());
    }

    @Test
    void testBooleanExpressions() throws IOException {
        Program program =
                parseInput(
                        """
                        true;
                        """);

        assertNotNull(program);
        assertEquals(1, program.statements.size());
        System.out.println(program.statements);
        ExpressionStatement stmt =
                assertInstanceOf(ExpressionStatement.class, program.statements.get(0));
        assertInstanceOf(BooleanIdentifier.class, stmt.expression());
        BooleanIdentifier booleanIdentifier = (BooleanIdentifier) stmt.expression();
        testBooleanLiteral(booleanIdentifier, true);
    }

    @Test
    void testPrefixBooleanExpressions() throws IOException {
        Program program =
                parseInput(
                        """
                        !true;
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        ExpressionStatement stmt = (ExpressionStatement) program.statements.get(0);

        assertThat(stmt.expression()).isInstanceOf(PrefixExpression.class);

        PrefixExpression prefixExpression = (PrefixExpression) stmt.expression();
        assertThat(prefixExpression.getRight()).isInstanceOf(BooleanIdentifier.class);
        assertThat(prefixExpression.getOperator()).isEqualTo("!");

        testBooleanLiteral((BooleanIdentifier) prefixExpression.getRight(), true);
    }

    @Test
    void testInfixBooleanExpressions() throws IOException {
        Program program =
                parseInput(
                        """
                        false == true;
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        ExpressionStatement stmt = (ExpressionStatement) program.statements.get(0);

        assertThat(stmt.expression()).isInstanceOf(InfixExpression.class);

        InfixExpression prefixExpression = (InfixExpression) stmt.expression();
        assertThat(prefixExpression.getRight()).isInstanceOf(BooleanIdentifier.class);
        assertThat(prefixExpression.getLeft()).isInstanceOf(BooleanIdentifier.class);
        assertThat(prefixExpression.getOperator()).isEqualTo("==");

        testBooleanLiteral((BooleanIdentifier) prefixExpression.getRight(), true);
        testBooleanLiteral((BooleanIdentifier) prefixExpression.getLeft(), false);
    }

    @Test
    void testGroupedExpressions() throws IOException {
        Program program =
                parseInput(
                        """
                            1 + (2 + 3) + 4
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        assertThat(program.toString()).isEqualTo("((1 + (2 + 3)) + 4)");
    }

    @Test
    void testIfExpression() throws IOException {
        Program program =
                parseInput(
                        """
                            if (x < y) { x }
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        ExpressionStatement expressionStatement = (ExpressionStatement) program.statements.get(0);

        assertThat(expressionStatement.expression()).isInstanceOf(IfExpression.class);
        IfExpression ifExp = (IfExpression) expressionStatement.expression();
        assertThat(ifExp.getCondition()).isInstanceOf(InfixExpression.class);
        assertThat(ifExp.getCondition().toString()).isEqualTo("(x < y)");
        assertThat(ifExp.getConsequence().statementsSize()).isEqualTo(1);

        assertThat(ifExp.getConsequence().getStatement(0)).isInstanceOf(ExpressionStatement.class);
        ExpressionStatement expStatement =
                (ExpressionStatement) ifExp.getConsequence().getStatement(0);
        assertThat(expStatement.toString()).isEqualTo("x");

        assertThat(ifExp.getAlternative().isEmpty()).isEqualTo(true);

        assertThat(program.toString()).isEqualTo("if (x < y)x");
    }

    @Test
    void testIfElseExpression() throws IOException {
        Program program =
                parseInput(
                        """
                            if (x < y) { x } else { y }
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        ExpressionStatement expressionStatement = (ExpressionStatement) program.statements.get(0);

        assertThat(expressionStatement.expression()).isInstanceOf(IfExpression.class);
        IfExpression ifExp = (IfExpression) expressionStatement.expression();
        assertThat(ifExp.getCondition()).isInstanceOf(InfixExpression.class);
        assertThat(ifExp.getCondition().toString()).isEqualTo("(x < y)");
        assertThat(ifExp.getConsequence().statementsSize()).isEqualTo(1);

        assertThat(ifExp.getConsequence().getStatement(0)).isInstanceOf(ExpressionStatement.class);
        ExpressionStatement expStatement =
                (ExpressionStatement) ifExp.getConsequence().getStatement(0);
        assertThat(expStatement.toString()).isEqualTo("x");

        assertThat(ifExp.getAlternative().isEmpty()).isEqualTo(false);

        assertThat(ifExp.getAlternative().get().getStatement(0))
                .isInstanceOf(ExpressionStatement.class);
        ExpressionStatement altStatement =
                (ExpressionStatement) ifExp.getAlternative().get().getStatement(0);
        assertThat(altStatement.toString()).isEqualTo("y");

        assertThat(program.toString()).isEqualTo("if (x < y)xelse y");
    }

    @Test
    void testFnWithParametersExpression() throws IOException {
        Program program =
                parseInput(
                        """
                            fn(x, y) { x + y }
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        ExpressionStatement expressionStatement = (ExpressionStatement) program.statements.get(0);

        assertThat(expressionStatement.expression()).isInstanceOf(FnStatement.class);
        FnStatement fnStatement = (FnStatement) expressionStatement.expression();
        assertThat(fnStatement.getToken().type()).isEqualTo(TokenType.FUNCTION);
        assertThat(fnStatement.getToken().literal()).isEqualTo("fn");
        assertThat(fnStatement.getBody()).isInstanceOf(BlockStatement.class);
        assertThat(fnStatement.getBody().toString()).isEqualTo("(x + y)");

        assertThat(fnStatement.toString()).isEqualTo("fn(x, y) {(x + y)}");

        assertThat(fnStatement.getParameters()).hasSize(2);
        assertThat(fnStatement.getParameters().get(0).toString()).isEqualTo("x");
        assertThat(fnStatement.getParameters().get(1).toString()).isEqualTo("y");
    }

    @Test
    void testFnWithoutParametersExpression() throws IOException {
        Program program =
                parseInput(
                        """
                            fn() { 1 + 2 }
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        ExpressionStatement expressionStatement = (ExpressionStatement) program.statements.get(0);

        assertThat(expressionStatement.expression()).isInstanceOf(FnStatement.class);
        FnStatement fnStatement = (FnStatement) expressionStatement.expression();
        assertThat(fnStatement.getToken().type()).isEqualTo(TokenType.FUNCTION);
        assertThat(fnStatement.getToken().literal()).isEqualTo("fn");
        assertThat(fnStatement.getBody()).isInstanceOf(BlockStatement.class);
        assertThat(fnStatement.getBody().toString()).isEqualTo("(1 + 2)");
        assertThat(fnStatement.toString()).isEqualTo("fn() {(1 + 2)}");

        assertThat(fnStatement.getParameters()).hasSize(0);
    }

    @Test
    void testCallExpression() throws IOException {
        Program program =
                parseInput(
                        """
                            add(1, 2 * 3, 4 + 5);
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        ExpressionStatement expressionStatement = (ExpressionStatement) program.statements.get(0);
        assertThat(expressionStatement.expression()).isInstanceOf(CallExpression.class);

        CallExpression callExpression = (CallExpression) expressionStatement.expression();
        assertThat(callExpression.getFunction().toString()).isEqualTo("add");
        assertThat(callExpression.getFunction()).isInstanceOf(Identifier.class);
        assertThat(callExpression.toString()).isEqualTo("add(1, (2 * 3), (4 + 5))");
        assertThat(callExpression.getArguments().get(0).toString()).isEqualTo("1");
        assertThat(callExpression.getArguments().get(1).toString()).isEqualTo("(2 * 3)");
        assertThat(callExpression.getArguments().get(2).toString()).isEqualTo("(4 + 5)");

        assertThat(callExpression.getArguments()).hasSize(3);
    }

    @Test
    void testCallFnExpression() throws IOException {
        Program program =
                parseInput(
                        """
                            fn(x, y) { x + y; }(2, 3)
                        """);

        assertThat(program).isNotNull();
        assertThat(program.statements)
                .hasSize(1)
                .allSatisfy(stmt -> assertThat(stmt).isInstanceOf(ExpressionStatement.class));
        ExpressionStatement expressionStatement = (ExpressionStatement) program.statements.get(0);
        assertThat(expressionStatement.expression()).isInstanceOf(CallExpression.class);

        CallExpression callExpression = (CallExpression) expressionStatement.expression();
        assertThat(callExpression.getFunction().toString()).isEqualTo("fn(x, y) {(x + y)}");
        assertThat(callExpression.getFunction()).isInstanceOf(FnStatement.class);
        assertThat(callExpression.toString()).isEqualTo("fn(x, y) {(x + y)}(2, 3)");
        assertThat(callExpression.getArguments().get(0).toString()).isEqualTo("2");
        assertThat(callExpression.getArguments().get(1).toString()).isEqualTo("3");

        assertThat(callExpression.getArguments()).hasSize(2);
    }

    @ParameterizedTest
    @CsvSource({
            "let x = 5;, x, 5",
            "let y = 10;, y, 10",
            "let foobar = 838383;, foobar, 838383"
    })
    void testLetStatementsParameterized(String input, String expectedIdentifier, int expectedValue) throws IOException {
        Program program = parseInput(input);

        assertThat(program).isNotNull();
        assertThat(program.statements).hasSize(1);

        Statement stmt = program.statements.getFirst();
        assertThat(stmt).isInstanceOf(LetStatement.class);

        LetStatement letStmt = (LetStatement) stmt;
        assertThat(letStmt.getName().toString()).isEqualTo(expectedIdentifier);
        assertThat(letStmt.getValue()).isInstanceOf(IntegerIdentifier.class);
        assertThat(((IntegerIdentifier) letStmt.getValue()).getValue()).isEqualTo(expectedValue);
    }
}
