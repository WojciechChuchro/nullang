package com.nullang.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nullang.ast.Identifier;
import com.nullang.ast.IntegerIdentifier;
import com.nullang.ast.Program;
import com.nullang.ast.Statement;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.ast.statement.LetStatement;
import com.nullang.ast.statement.ReturnStatement;
import com.nullang.lexer.Lexer;
import com.nullang.parser.errors.ParserException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ParserTest {
    private final Logger log = LoggerFactory.getLogger(ParserTest.class);

    @Test
    public void testLetStatements() {
        Reader input =
                new StringReader(
                        """
                        let x = 5;
                        let y = 10;
                        let foobar = 838383;
                        """);

        try (Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer); ) {

            Program program = parser.parseProgram();
            assertNotNull(program, "parseProgram() returned null");

            var statements = program.statements;
            assertEquals(3, statements.size(), "Expected 3 let statements");

            String[] expectedIdentifiers = {"x", "y", "foobar"};

            for (int i = 0; i < expectedIdentifiers.length; i++) {
                Statement stmt = statements.get(i);
                assertTrue(stmt instanceof LetStatement, "Statement is not a LetStatement");

                LetStatement letStmt = (LetStatement) stmt;
                assertEquals("let", letStmt.tokenLiteral());

                // TODO:
                // Identifier name = letStmt.name;
                // assertNotNull(name, "LetStatement.name is null");
                // assertEquals(expectedIdentifiers[i], name.getValue());
                // assertEquals(expectedIdentifiers[i], name.tokenLiteral());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testReturn() {
        Reader input =
                new StringReader(
                        """
                        return 5;
                        return true;
                        """);

        try (Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer); ) {

            Program program = parser.parseProgram();
            assertNotNull(program, "parseProgram() returned null");

            var statements = program.statements;
            assertEquals(2, statements.size(), "Expected 2 return statements");

            String[] expectedIdentifiers = {"5", "true"};

            for (int i = 0; i < expectedIdentifiers.length; i++) {
                Statement stmt = statements.get(i);
                assertTrue(stmt instanceof ReturnStatement, "Statement is not a LetStatement");

                ReturnStatement returnStmt = (ReturnStatement) stmt;
                assertEquals("return", returnStmt.tokenLiteral());

                // TODO:
                // Identifier name = letStmt.name;
                // assertNotNull(name, "LetStatement.name is null");
                // assertEquals(expectedIdentifiers[i], name.getValue());
                // assertEquals(expectedIdentifiers[i], name.tokenLiteral());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testForAssign() {
        Reader input =
                new StringReader(
                        """
                        let x;
                        """);

        try (Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer); ) {

            ParserException ex =
                    assertThrowsExactly(ParserException.class, () -> parser.parseProgram());

            assertEquals(
                    "Expected '=' after identifierToken [type=SEMICOLON, literal=;]",
                    ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testForIdent() {
        Reader input =
                new StringReader(
                        """
                        let =;
                        """);

        try (Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer); ) {

            ParserException ex =
                    assertThrowsExactly(ParserException.class, () -> parser.parseProgram());

            assertEquals(
                    "Peek should be variable name!Token [type=ASSIGN, literal==]", ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testExpressions() {
        Reader input =
                new StringReader(
                        """
                            foobar;
                            5;
                        """);

        try (Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer); ) {

            Program program = parser.parseProgram();
            assertNotNull(program, "parseProgram() returned null");

            var statements = program.statements;
            assertEquals(2, statements.size(), "Expected 2 statements");


            Statement stmt = statements.get(0);
            assertTrue(
                    stmt instanceof ExpressionStatement, "Statement is not a ExpressionStatement");

            ExpressionStatement stm = (ExpressionStatement) stmt;
            assertEquals("foobar", stm.tokenLiteral());
            assertEquals("foobar", ((Identifier)stm.getExpression()).getValue());

            Statement stmt2 = statements.get(1);
            assertTrue(
                    stmt2 instanceof ExpressionStatement, "Statement is not a ExpressionStatement");
            System.out.println(program);

            ExpressionStatement stm2 = (ExpressionStatement) stmt2;
            assertTrue(
                    stm2.getExpression() instanceof IntegerIdentifier, "Expressions statement should be integer identifier!");

            assertEquals("5", stm2.tokenLiteral());
            assertEquals(5, ((IntegerIdentifier)stm2.getExpression()).getValue());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
