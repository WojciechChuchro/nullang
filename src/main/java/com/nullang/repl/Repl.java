package com.nullang.repl;

import com.nullang.ast.Program;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.parser.Parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

public class Repl {

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter code to parse:");
        while (true) {
            System.out.print(">> ");
            if (!sc.hasNextLine())
                break;

            String input = sc.nextLine();
            if (input.equals("exit"))
                break;

            if (input.trim().isEmpty()) {
                continue;
            }

            Program program = parseInput(input);
            var eval = new Eval();
            var res = eval.evaluate(program);
            System.out.println(res.inspect());
        }
    }

    public static Program parseInput(String input) throws IOException {
        try (Lexer lexer = new Lexer(new StringReader(input));
             Parser parser = new Parser(lexer)) {

            return parser.parseProgram();
        }
    }
}
