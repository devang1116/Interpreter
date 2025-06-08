# Interpreter DMP 

A lightweight interpreter for a custom dynamically-typed programming language. This project is built entirely in Java and supports expression evaluation, variable scopes, functions, and object-oriented constructs like classes and methods.

---

## ✨ Features

- Fully tokenized and parsed source code
- Static resolution of variable scopes
- Function declarations and first-class functions
- Class support with methods and `this`
- Error handling and runtime checks
- Object instances and field access

---

## 🛠 Getting Started

### Requirements

- Java 17+
- Maven or your preferred Java build tool

### Build and Run

1. **Clone the repository:**

```bash
git clone https://github.com/devang1116/Interpreter.git
cd java-interpreter
```

2.
```bash
javac -d out src/com/example/interpreter/*.java
java -cp out com.example.interpreter.Main
