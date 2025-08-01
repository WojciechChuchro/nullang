package com.nullang.lexer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStreamReader;

@Configuration
public class LexerTestConfiguration {

    ClassPathResource resource = new ClassPathResource("lexer.null");

    @Bean
    public Lexer lexer() throws Exception {
        return new Lexer(new InputStreamReader(resource.getInputStream()));
    }
}
