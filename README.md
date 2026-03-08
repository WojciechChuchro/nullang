# Nullang

A tree-walking interpreter for a dynamically-typed programming language, built in Java 21.

Nullang supports integers, booleans, strings, arrays, hash maps, first-class functions, closures, and higher-order functions. It is inspired by the Monkey language from [Writing An Interpreter In Go](https://interpreterbook.com/) by Thorsten Ball, reimplemented from scratch in Java.

## Language Features

**Data types** — integers, booleans, strings, arrays, hash maps

```
let age = 25;
let name = "nullang";
let active = true;
let items = [1, 2, 3];
let person = {"name": "alice", "age": 30};
```

**Arithmetic and comparison operators**

```
let result = (2 + 3) * 4;    // 20
let check = 10 > 5;          // true
let eq = 1 == 1;             // true
"hello" + " " + "world";      // string concatenation
```

**Variables**

```
let x = 10;
let y = x * 2;
```

**Conditionals**

```
if (x > 5) {
  "big"
} else {
  "small"
}
```

**Functions and closures**

```
let add = fn(a, b) { a + b };
add(2, 3);

let makeAdder = fn(x) { fn(y) { x + y } };
let addFive = makeAdder(5);
addFive(10);    // 15
```

**Higher-order functions**

```
let apply = fn(f, x) { f(x) };
let double = fn(x) { x * 2 };
apply(double, 5);    // 10
```

**Arrays and indexing**

```
let arr = [1, 2, 3, 4, 5];
arr[0];              // 1
arr[1 + 1];          // 3
```

**Hash maps**

Keys can be strings, integers, or booleans. Values can be any expression. Indexing with a missing key returns null.

```
let map = {"one": 1, "two": 2, "three": 3};
map["two"];          // 2

let byNum = {1: "one", 2: "two"};
byNum[2];            // "two"

let byBool = {true: "yes", false: "no"};
byBool[true];        // "yes"

let h = {"key": 2 + 3};
h["key"];            // 5
{}["missing"];       // null
```

**Built-in functions**

```
len("hello");        // 5
len([1, 2, 3]);      // 3
puts("hello world"); // prints to stdout
first([1, 2, 3]);    // 1
tail([1, 2, 3]);     // 3
push([1, 2], 3);     // [1, 2, 3] (mutates array)
```

## Architecture

The interpreter follows a classic pipeline:

```
Source Code → Lexer → Tokens → Parser → AST → Evaluator → Result
```

| Stage | Package | Description |
|-------|---------|-------------|
| **Lexer** | `com.nullang.lexer` | Converts source text into a stream of tokens |
| **Parser** | `com.nullang.parser` | Pratt parser that builds an AST from tokens |
| **AST** | `com.nullang.ast` | Tree of expression and statement nodes |
| **Evaluator** | `com.nullang.eval` | Tree-walking evaluator with scoped environments |
| **Objects** | `com.nullang.nullangobject` | Runtime value representations |

## Getting Started

### Prerequisites

- Java 21+
- Gradle

### Run the REPL

```bash
./gradlew run
```

The REPL uses a Python-style prompt: `>>>` for input and `...` when more input is expected (e.g. unclosed braces). Type `exit` or `quit` to leave.

```
Nullang 0.0.1
Type "exit" or "quit" to leave.

>>> let x = 5;
>>> let double = fn(x) { x * 2 };
>>> double(x);
10
>>> fn(a, b) { a + b }(2, 3);
5
>>> exit
```

### Run a file

Run a `.null` file from the project resources:

```bash
./gradlew runFile -Pfile=examples/basics.null
./gradlew runFile -Pfile=examples/demo.null
./gradlew runFile -Pfile=examples/hashes.null
```

Or run the `RunFile` main class with a path (file path or resource path):

```bash
./gradlew run -PmainClass=com.nullang.RunFile -Pfile=examples/functions.null
```

See `src/main/resources/examples/` for sample programs: `basics.null`, `functions.null`, `arrays.null`, `closures.null`, `higher_order.null`, `hashes.null`, `demo.null`.

### Run tests

```bash
./gradlew test
```

### Run with Docker

Build the image and start the REPL:

```bash
docker build -t nullang .
docker run -it nullang
```

Then try the language (type `exit` or `quit` to leave). To run a bundled example file:

```bash
docker run --rm nullang run examples/demo.null
```

To run your own `.null` file, mount it into the container:

```bash
docker run --rm -v "$(pwd)/myfile.null:/script.null" nullang run /script.null
```

## Project Structure

```
src/
├── main/java/com/nullang/
│   ├── Repl.java                   # Interactive REPL
│   ├── RunFile.java                # Run a .null file from path or resources
│   ├── ast/                        # AST nodes
│   │   ├── expression/             # Infix, prefix, call, if, fn, array, hash, index
│   │   └── statement/              # Let, return, block, expression statements
│   ├── eval/                       # Evaluator and environment
│   ├── lexer/                      # Tokenizer
│   ├── nullangobject/              # Runtime objects (int, bool, string, array, hash, fn, error)
│   │   └── HashKey, HashPair, Hashable # Hash key identity and pairs
│   ├── parser/                     # Pratt parser with precedence climbing
│   └── token/                      # Token types
├── main/resources/
│   └── examples/                   # Example .null programs
└── test/java/com/nullang/          # Test suite
```

## License

This project is for educational purposes.
