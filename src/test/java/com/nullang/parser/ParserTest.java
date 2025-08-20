package com.nullang.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nullang.ast.BooleanIdentifier;
import com.nullang.ast.Identifier;
import com.nullang.ast.IntegerIdentifier;
import com.nullang.ast.Program;
import com.nullang.ast.Statement;
import com.nullang.ast.expression.IfExpression;
import com.nullang.ast.expression.InfixExpression;
import com.nullang.ast.expression.PrefixExpression;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.ast.statement.LetStatement;
import com.nullang.ast.statement.ReturnStatement;
import com.nullang.lexer.Lexer;
import com.nullang.parser.errors.ParserException;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ParserTest {
    private Program parseInput(String input) throws IOException {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader);
                Parser parser = new Parser(lexer)) {
            return parser.parseProgram();
        }
    }

    private ParserException parseInputExpectingException(String input) throws IOException {
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
            assertTrue(stmt instanceof LetStatement, "Statement " + i + " should be LetStatement");
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

        for (Statement stmt : program.statements) {
            assertTrue(stmt instanceof ReturnStatement);
            assertEquals("return", stmt.tokenLiteral());
        }
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
        assertTrue(exprStmt.getExpression() instanceof Identifier);
        assertEquals("foobar", ((Identifier) exprStmt.getExpression()).getValue());
    }

    @Test
    void testIntegerExpression() throws IOException {
        Program program = parseInput("5;");

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        Statement stmt = program.statements.get(0);
        assertTrue(stmt instanceof ExpressionStatement);

        ExpressionStatement exprStmt = (ExpressionStatement) stmt;
        assertTrue(exprStmt.getExpression() instanceof IntegerIdentifier);
        assertEquals("5", exprStmt.tokenLiteral());
        assertEquals(5, ((IntegerIdentifier) exprStmt.getExpression()).getValue());
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
        assertEquals("foobar", ((Identifier) exprStmt1.getExpression()).getValue());

        // Test integer expression
        Statement stmt2 = program.statements.get(1);
        assertTrue(stmt2 instanceof ExpressionStatement);
        ExpressionStatement exprStmt2 = (ExpressionStatement) stmt2;
        assertEquals(5, ((IntegerIdentifier) exprStmt2.getExpression()).getValue());
    }

    @Test
    void testBangPrefixExpression() throws IOException {
        Program program = parseInput("!5;");

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        Statement stmt = program.statements.get(0);
        assertTrue(stmt instanceof ExpressionStatement);

        ExpressionStatement exprStmt = (ExpressionStatement) stmt;
        assertTrue(exprStmt.getExpression() instanceof PrefixExpression);

        PrefixExpression prefixExpr = (PrefixExpression) exprStmt.getExpression();
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
        assertTrue(exprStmt.getExpression() instanceof PrefixExpression);

        PrefixExpression prefixExpr = (PrefixExpression) exprStmt.getExpression();
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
        PrefixExpression prefix1 = (PrefixExpression) stmt1.getExpression();
        assertEquals("!", prefix1.getOperator());

        // Test minus prefix
        ExpressionStatement stmt2 = (ExpressionStatement) program.statements.get(1);
        PrefixExpression prefix2 = (PrefixExpression) stmt2.getExpression();
        assertEquals("-", prefix2.getOperator());
    }

    private void assertPrefixExpression(String input, String expectedOperator, int expectedValue)
            throws IOException {
        Program program = parseInput(input);

        assertNotNull(program);
        assertEquals(1, program.statements.size());

        ExpressionStatement exprStmt = (ExpressionStatement) program.statements.get(0);
        PrefixExpression prefixExpr = (PrefixExpression) exprStmt.getExpression();

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
        InfixExpression infix1 = (InfixExpression) stmt1.getExpression();
        assertEquals("5", infix1.getLeft().tokenLiteral());
        assertEquals("+", infix1.getOperator());
        assertEquals("4", infix1.getRight().tokenLiteral());

        ExpressionStatement stmt2 = (ExpressionStatement) program.statements.get(1);
        InfixExpression infix2 = (InfixExpression) stmt2.getExpression();
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
        assertInstanceOf(BooleanIdentifier.class, stmt.getExpression());
        BooleanIdentifier booleanIdentifier = (BooleanIdentifier) stmt.getExpression();
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

        assertThat(stmt.getExpression()).isInstanceOf(PrefixExpression.class);

        PrefixExpression prefixExpression = (PrefixExpression) stmt.getExpression();
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

        assertThat(stmt.getExpression()).isInstanceOf(InfixExpression.class);

        InfixExpression prefixExpression = (InfixExpression) stmt.getExpression();
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

        assertThat(expressionStatement.getExpression()).isInstanceOf(IfExpression.class);
        IfExpression ifExp = (IfExpression) expressionStatement.getExpression();
        assertThat(ifExp.getCondition()).isInstanceOf(InfixExpression.class);
        assertThat(ifExp.getCondition().toString()).isEqualTo("(x < y)");
        assertThat(ifExp.getConsequence().statementsSize()).isEqualTo(1);

        assertThat(ifExp.getConsequence().getStatement(0)).isInstanceOf(ExpressionStatement.class);
        ExpressionStatement expStatement = (ExpressionStatement)ifExp.getConsequence().getStatement(0);
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

        assertThat(expressionStatement.getExpression()).isInstanceOf(IfExpression.class);
        IfExpression ifExp = (IfExpression) expressionStatement.getExpression();
        assertThat(ifExp.getCondition()).isInstanceOf(InfixExpression.class);
        assertThat(ifExp.getCondition().toString()).isEqualTo("(x < y)");
        assertThat(ifExp.getConsequence().statementsSize()).isEqualTo(1);

        assertThat(ifExp.getConsequence().getStatement(0)).isInstanceOf(ExpressionStatement.class);
        ExpressionStatement expStatement = (ExpressionStatement)ifExp.getConsequence().getStatement(0);
        assertThat(expStatement.toString()).isEqualTo("x");

        assertThat(ifExp.getAlternative().isEmpty()).isEqualTo(false);

        assertThat(ifExp.getAlternative().get().getStatement(0)).isInstanceOf(ExpressionStatement.class);
        ExpressionStatement altStatement = (ExpressionStatement)ifExp.getAlternative().get().getStatement(0);
        assertThat(altStatement.toString()).isEqualTo("y");

        assertThat(program.toString()).isEqualTo("if (x < y)xelse y");
    }
}
