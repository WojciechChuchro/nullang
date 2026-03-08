package com.nullang;

import com.nullang.ast.Program;
import com.nullang.eval.Env;
import com.nullang.eval.Eval;
import com.nullang.lexer.Lexer;
import com.nullang.nullangobject.NullangObject;
import com.nullang.nullangobject.ObjectType;
import com.nullang.parser.Parser;
import com.nullang.parser.errors.ParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class RunFile {

    public static void main(String[] args) throws IOException {
        String pathArg = getPathArg(args);
        if (pathArg == null || pathArg.isBlank()) {
            System.err.println("Usage: RunFile <file.null>");
            System.err.println("  Or:  gradlew run -PmainClass=com.nullang.RunFile -Pfile=examples/basics.null");
            System.exit(1);
        }

        String source = loadSource(pathArg);
        if (source == null) {
            System.err.println("Could not load file: " + pathArg);
            System.exit(1);
        }

        Env env = new Env();
        Eval eval = new Eval();

        try {
            Program program = parseSource(source);
            NullangObject result = eval.evaluate(program, env);

            if (result.type() == ObjectType.ERROR) {
                System.err.println(result.inspect());
                System.exit(1);
            }

            if (result.type() != ObjectType.NULL) {
                System.out.println(result.inspect());
            }
        } catch (ParserException e) {
            System.err.println("Parse error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String getPathArg(String[] args) {
        String fromProp = System.getProperty("file");
        if (fromProp != null && !fromProp.isBlank()) {
            return fromProp.trim();
        }
        if (args != null && args.length > 0) {
            return args[0].trim();
        }
        return null;
    }

    private static String loadSource(String pathArg) throws IOException {
        Path path = Path.of(pathArg);
        if (Files.isRegularFile(path)) {
            return Files.readString(path, StandardCharsets.UTF_8);
        }

        String resourcePath = pathArg;
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }
        if (!resourcePath.endsWith(".null") && !resourcePath.contains(".")) {
            resourcePath = resourcePath + ".null";
        }

        try (InputStream in = RunFile.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(r)) {
                return br.lines().collect(Collectors.joining("\n"));
            }
        }
    }

    private static Program parseSource(String source) throws IOException {
        try (Lexer lexer = new Lexer(new java.io.StringReader(source));
             Parser parser = new Parser(lexer)) {
            return parser.parseProgram();
        }
    }
}
