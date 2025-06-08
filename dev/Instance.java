package dev;

import java.util.*;

// Implemented to be able to instantiate a class
public class Instance {
    private Class klass;
    private final Map<String, Object> fields = new HashMap<>();

    Instance(Class klass) {
        this.klass = klass;
    }

    public String toString() {
        return klass.name;
    }

    // HELPER: To get the Instance fields if they are present
    Object get(Token name) {
        if(fields.containsKey(name.lexeme))
            return fields.get(name.lexeme);

        Function method = klass.findMethod(name.lexeme);
        if(method != null)
            return method.bind(this);

        throw new RuntimeError(name , "Undefined property : '"+ name.lexeme + ".'");
    }

    // HELPER: To set the Instance fields
    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
