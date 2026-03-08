package com.nullang;

import com.nullang.ast.Program;
import com.nullang.eval.Env;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.nullangobject.NullangObject;
import com.nullang.nullangobject.ObjectType;
import com.nullang.parser.Parser;
import com.nullang.parser.errors.ParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

public class Repl {

    private static final String VERSION = "0.0.1";
    private static final String PROMPT = ">>> ";
    private static final String CONTINUATION = "... ";

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        Env env = new Env();
        Eval eval = new Eval();

        printWelcome();

        StringBuilder buffer = new StringBuilder();
        String prompt = PROMPT;

        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) {
                System.out.println();
                break;
            }

            String line = sc.nextLine();
            if (buffer.isEmpty() && isExitCommand(line)) {
                break;
            }

            buffer.append(line).append("\n");
            String input = buffer.toString();

            try {
                Program program = parseInput(input);
                NullangObject result = eval.evaluate(program, env);

                if (result.type() == ObjectType.ERROR) {
                    System.err.println(result.inspect());
                } else if (result.type() != ObjectType.NULL) {
                    System.out.println(result.inspect());
                }

                buffer.setLength(0);
                prompt = PROMPT;
            } catch (ParserException e) {
                if (isIncompleteInput(input)) {
                    prompt = CONTINUATION;
                } else {
                    System.err.println("Parse error: " + e.getMessage());
                    buffer.setLength(0);
                    prompt = PROMPT;
                }
            }
        }

        sc.close();
    }

    private static void printWelcome() {
        System.out.println("Nullang " + VERSION);
        System.out.println("Type \"exit\" or \"quit\" to leave.");
        System.out.println();
    }

    private static boolean isExitCommand(String line) {
        if (line == null) return true;
        String trimmed = line.trim().toLowerCase();
        return trimmed.equals("exit") || trimmed.equals("quit");
    }

    private static boolean isIncompleteInput(String input) {
        int parens = 0, brackets = 0, braces = 0;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inString) {
                if (c == '\\' && i + 1 < input.length()) {
                    i++;
                    continue;
                }
                if (c == stringChar) {
                    inString = false;
                }
                continue;
            }

            if (c == '"' || c == '\'') {
                inString = true;
                stringChar = c;
                continue;
            }

            switch (c) {
                case '(' -> parens++;
                case ')' -> parens--;
                case '[' -> brackets++;
                case ']' -> brackets--;
                case '{' -> braces++;
                case '}' -> braces--;
                default -> {}
            }
        }

        return parens > 0 || brackets > 0 || braces > 0;
    }

    public static Program parseInput(String input) throws IOException {
        try (Lexer lexer = new Lexer(new StringReader(input));
             Parser parser = new Parser(lexer)) {
            return parser.parseProgram();
        }
    }
}
