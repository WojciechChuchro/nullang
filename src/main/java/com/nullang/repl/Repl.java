package com.nullang.repl;

import com.nullang.lexer.Lexer;
import com.nullang.token.Token;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.io.StringReader;
import java.util.Scanner;

@Component
class Repl implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println("Welcome to nullang! Type your code and press Enter. Type 'exit' to quit.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print(">> ");
                String line = scanner.nextLine().trim();
                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting nullang...");
                    break;
                }
                if (!line.isEmpty()) {
                    try {
                        runLexer(line);
                    } catch (Exception e) {
                        System.err.println("Error processing input: " + e.getMessage());
                    }
                }
            }
        }
        System.out.println("REPL terminated.");
    }

    private void runLexer(String input) throws Exception {
        try (Reader inputReader = new StringReader(input);
             Lexer lexer = new Lexer(inputReader)) {
            Token token;
            do {
                token = lexer.nextToken();
                System.out.printf("%s\t'%s'\n", token.type, token.literal);
            } while (token.type != com.nullang.token.TokenType.EOF);
        }
    }
}
