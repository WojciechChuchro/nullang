package com.nullang.lexer;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class LexerConfiguration {
    ClassPathResource resource = new ClassPathResource("file.null");

    @Bean
    public Reader lexerReader() throws IOException {
        //return new InputStreamReader(resource.getInputStream());
        return new StringReader("hello lexer!");
    }

    @Bean
    public Lexer lexer(Reader lexerReader) throws Exception {
        return new Lexer(lexerReader);
    }
}
