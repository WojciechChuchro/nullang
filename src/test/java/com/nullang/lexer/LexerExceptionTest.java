package com.nullang.lexer;

import com.nullang.token.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LexerExceptionTest {

    @Mock
    Reader reader;

    @Test
    void constructorThrowsOnIOException() throws IOException {
        when(reader.read()).thenThrow(new IOException("boom"));

        assertThatThrownBy(() -> new Lexer(reader))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void closeThrowsOnIOException() throws Exception {
        when(reader.read()).thenReturn(-1);

        doThrow(new IOException("boom on close"))
                .when(reader).close();

        Lexer lexer = new Lexer(reader);

        assertThatThrownBy(lexer::close)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void nextTokenThrowsOnReadIOException() throws Exception {
        when(reader.read())
                .thenReturn((int) 'a')
                .thenReturn((int) ' ')
                .thenThrow(new IOException("read failure"));

        Lexer lexer = new Lexer(reader);

        assertThatThrownBy(lexer::nextToken)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void nextTokenThrowsOnIOExceptionInIdentifier() throws Exception {
        when(reader.read())
                .thenReturn((int) 'a')
                .thenReturn((int) 'a')
                .thenReturn((int) ' ')
                .thenThrow(new IOException("boom"));

        Lexer lexer = new Lexer(reader);

        assertThatThrownBy(lexer::nextToken)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void nextTokenThrowsOnIOExceptionInNumber() throws Exception {
        when(reader.read())
                .thenReturn((int) '1')
                .thenReturn((int) ' ')
                .thenThrow(new IOException("boom"));

        Lexer lexer = new Lexer(reader);

        assertThatThrownBy(lexer::nextToken)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void nextTokenThrowsOnIOExceptionAfterToken() throws Exception {
        when(reader.read())
                .thenReturn((int) '=')
                .thenReturn((int) '=')
                .thenThrow(new IOException("boom after =="));

        Lexer lexer = new Lexer(reader);

        assertThatThrownBy(lexer::nextToken)
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void nextTokenThrowsOnRepeatedIOException() throws Exception {
        when(reader.read()).thenThrow(new IOException("continuous failure"));

        assertThatThrownBy(() -> new Lexer(reader))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IOException.class);
    }
}
