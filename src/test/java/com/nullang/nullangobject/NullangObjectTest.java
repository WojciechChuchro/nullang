package com.nullang.nullangobject;

import com.nullang.ast.Node;
import com.nullang.ast.Program;
import com.nullang.ast.Statement;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.parser.Parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;


import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
public class NullangObjectTest {
    private Program parseInput(String input) throws IOException {
        Reader reader = new StringReader(input);
        try (Lexer lexer = new Lexer(reader);
                Parser parser = new Parser(lexer)) {
            return parser.parseProgram();
        }
    }

    @Test
    public void testEval() throws IOException {
       List<Statement> l =
                parseInput(
                                """
                                5
                                """)
                        .statements;
        Eval e = new Eval();
        IntegerType evaluate = (IntegerType) e.evaluate(l.get(0));
        System.out.println(evaluate);
        
        assertThat(evaluate.getValue()).isEqualTo(5);
    }
}
