# Nullang

A tree-walking interpreter for a dynamically-typed programming language, built in Java 21.

Nullang supports integers, booleans, strings, arrays, first-class functions, closures, and higher-order functions. It is inspired by the Monkey language from [Writing An Interpreter In Go](https://interpreterbook.com/) by Thorsten Ball, reimplemented from scratch in Java.

## Language Features

**Data types** — integers, booleans, strings, arrays

```
let age = 25;
let name = "nullang";
let active = true;
let items = [1, 2, 3];
```

**Arithmetic and comparison operators**

```
let result = (2 + 3) * 4;    // 20
let check = 10 > 5;          // true
let eq = "hi" == "hi";       // true
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

**Built-in functions**

```
len("hello");        // 5
puts("hello world"); // prints to stdout
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

Then type expressions interactively:

```
Enter code to parse:
>> let x = 5;
Evaluated: 5
>> let double = fn(x) { x * 2 };
Evaluated: fn([x]{(x * 2)}
>> double(x);
Evaluated: 10
>> exit
```

### Run a file

Place your code in a `.null` file and run it with the `Repl` class:

```bash
./gradlew run -PmainClass=com.nullang.Repl
```

See `src/main/resources/examples/` for sample programs.

### Run tests

```bash
./gradlew test
```

## Project Structure

```
src/
├── main/java/com/nullang/
│   ├── NullangApplication.java     # Interactive REPL
│   ├── Repl.java                   # File-based runner
│   ├── ast/                        # AST nodes
│   │   ├── expression/             # Infix, prefix, call, if, fn, array, index
│   │   └── statement/              # Let, return, block, expression statements
│   ├── eval/                       # Evaluator and environment
│   ├── lexer/                      # Tokenizer
│   ├── nullangobject/              # Runtime objects (int, bool, string, array, fn, error)
│   ├── parser/                     # Pratt parser with precedence climbing
│   └── token/                      # Token types
├── main/resources/
│   └── examples/                   # Example .null programs
└── test/java/com/nullang/          # Test suite
```

## License

This project is for educational purposes.
