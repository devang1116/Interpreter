package dev;

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
    Object get(Token name) {
        if (values.containsKey(name.lexeme))
            return values.get(name.lexeme);

        if (enclosing != null)
            return enclosing.get(name);

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

    // HELPER: Resolves the enclosing and gets the value from the particular scope
    Object getAt(int distance, String name ) {
        return ancestor(distance).values.get(name);
    }

    // HELPER: Resolve the scope/enclsing and assign value to it
    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value) ;
    }

    // HELPER: Fetches the right enclosing/scope according to the distance
    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }
}
