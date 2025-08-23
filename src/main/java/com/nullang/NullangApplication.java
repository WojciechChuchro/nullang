package com.nullang;

import com.nullang.ast.Program;
import com.nullang.lexer.Lexer;
import com.nullang.parser.Parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

public class NullangApplication {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter code to parse:");
        while (true) {
            System.out.print(">> ");
            if (!sc.hasNextLine()) break;
            String input = sc.nextLine();
            if (input.equals("exit")) break;

            try {
                Program program = parseInput(input);
                System.out.println("Parsed: " + program);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    public static Program parseInput(String input) throws IOException {
        try (Lexer lexer = new Lexer(new StringReader(input));
                Parser parser = new Parser(lexer)) {

            return parser.parseProgram();
        }
    }
}
