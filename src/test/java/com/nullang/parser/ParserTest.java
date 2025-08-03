package com.nullang.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.nullang.ast.Expression;
import com.nullang.ast.Identifier;
import com.nullang.ast.Program;
import com.nullang.ast.Statement;
import com.nullang.ast.statement.ExpressionStatement;
import com.nullang.ast.statement.LetStatement;
import com.nullang.ast.statement.ReturnStatement;
import com.nullang.lexer.Lexer;
import com.nullang.parser.errors.ParserException;

public class ParserTest {

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

            assertEquals("Expected '=' after identifierToken [type=SEMICOLON, literal=;]", ex.getMessage());
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

            assertEquals("Peek should be variable name!Token [type=ASSIGN, literal==]", ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testExpressions() {
        Reader input =
                new StringReader(
                        """
                            foobar
                        """);

        try (Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer); ) {

            Program program = parser.parseProgram();
            assertNotNull(program, "parseProgram() returned null");

            var statements = program.statements;
            assertEquals(1, statements.size(), "Expected 1 statements");

            String[] expectedIdentifiers = {"foobar"};

            for (int i = 0; i < expectedIdentifiers.length; i++) {
                Statement stmt = statements.get(i);
                assertTrue(stmt instanceof ExpressionStatement, "Statement is not a ExpressionStatement");

                ExpressionStatement stm = (ExpressionStatement) stmt;
                //Expression ex = stm.getExpression();
               // assertTrue(ex instanceof Identifier, "Expressions is not a Identifier");
                assertEquals("foobar", stm.tokenLiteral());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
