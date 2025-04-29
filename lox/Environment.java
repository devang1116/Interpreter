package lox;

import java.util.*;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name , Object value) {
        values.put(name, value);
    }

    // HELPER: Returns variables according to scope using enclosing and maps
    Object get(Token name, Environment environment) {
        if (environment.values.containsKey(name.lexeme))
            return environment.values.get(name.lexeme);

        if (enclosing != null)
            return enclosing.get(name, enclosing);

        throw new RuntimeError(name, "Undefined Error'" + name.lexeme + "'.");
    }

    // HELPER: Assigns variables according to scope using enclosing
    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme +"'.");
    }
}
