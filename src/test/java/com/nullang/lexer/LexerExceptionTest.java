package com.nullang.lexer;

import com.nullang.token.Token;
import com.nullang.token.TokenType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Reader;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LexerExceptionTest {

    @Mock
    Reader reader;

    @Test
    void constructorHandlesIOException() throws IOException {

        when(reader.read()).thenThrow(new IOException("boom"));

        Assertions.assertDoesNotThrow(() -> new Lexer(reader));
    }

    @Test
    void closeHandlesIOException() throws Exception {
        when(reader.read()).thenReturn(-1);

        doThrow(new IOException("boom on close"))
                .when(reader).close();

        Lexer lexer = new Lexer(reader);

        Assertions.assertDoesNotThrow(lexer::close);
    }


    @Test
    void readCharHandlesIOException() throws Exception {
        when(reader.read())
                .thenReturn((int) 'a')
                .thenReturn((int) 'b')
                .thenReturn((int) ' ') // TODO: without that causes infinite loop inside readIdentifier
                .thenThrow(new IOException("read failure"));

        Lexer lexer = new Lexer(reader);

        Assertions.assertDoesNotThrow(() -> {
            lexer.nextToken();
        });
    }


    @Test
    void readIdentifierHandlesIOException() throws Exception {
        when(reader.read())
                .thenReturn((int) 'a')
                .thenReturn((int) 'a')
                .thenReturn((int) ' ') // TODO: without that causes infinite loop inside readIdentifier
                .thenThrow(new IOException("boom"));

        Lexer lexer = new Lexer(reader);

        Assertions.assertDoesNotThrow(lexer::nextToken);
    }


    @Test
    void readNumberHandlesIOException() throws Exception {
        when(reader.read())
                .thenReturn((int) '1')
                .thenReturn((int) '1')
                .thenReturn((int) ' ') // TODO: without that causes infinite loop inside readIdentifier
                .thenThrow(new IOException("boom"));

        Lexer lexer = new Lexer(reader);
        Assertions.assertDoesNotThrow(lexer::nextToken);
    }


    @Test
    void equalsTokenHandlesIOException() throws Exception {
        when(reader.read())
                .thenReturn((int) '=')
                .thenReturn((int) '=')
                .thenThrow(new IOException("boom after =="));

        Lexer lexer = new Lexer(reader);

        Token token = lexer.nextToken();
        Assertions.assertEquals(TokenType.EQ, token.type());
    }


    @Test
    void nextTokenSurvivesRepeatedExceptions() throws Exception {
        when(reader.read()).thenThrow(new IOException("continuous failure"));

        Lexer lexer = new Lexer(reader);

        for (int i = 0; i < 5; i++) {
            Assertions.assertDoesNotThrow(lexer::nextToken);
        }
    }
}
