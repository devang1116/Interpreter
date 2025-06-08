package dev;

import java.util.*;

public class Class implements Callable{
    final String name;
    private final Map<String, Function> methods;


    Class(String name, Map<String, Function> methods) {
        this.name = name;
        this.methods = methods;
    }

    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Instance instance = new Instance(this);
        Function initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter,arguments);
        }
        return instance;
    }

    // HELPER: Returns if method exists in the class
    Function findMethod(String name) {
        if(methods.containsKey(name))
            return methods.get(name);
        else
            return null;
    }

    @Override
    public int arity() {
        Function initializer = findMethod("init");
        if (initializer == null)
            return 0;

        return initializer.arity();
    }
}
