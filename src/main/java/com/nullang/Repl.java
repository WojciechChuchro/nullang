package com.nullang;

import com.nullang.ast.Program;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.parser.Parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Repl {

    public static void main(String[] args) throws IOException {
        var eval = new Eval();
        File nullangFile = new File("../resources/file.null");
        FileReader reader = new FileReader(nullangFile);

        try (Lexer lexer = new Lexer(reader)) {
            Program program = new Parser(lexer).parseProgram();
            var res = eval.evaluate(program);

            System.out.println("Evaluated: " + res.inspect());
        }
    }
}
