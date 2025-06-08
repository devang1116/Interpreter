package dev;
import java.util.*;

class Function implements Callable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final Boolean isInitializer;

    Function(Stmt.Function declaration, Environment closure, Boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);

        for(int i = 0; i < declaration.parameters.size() ; i++) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Interpreter.Return returnValue) {
            return returnValue.value;
        }

        if (isInitializer)
            return closure.getAt(0, "this");

        return null;
    }

    // HELPER: Helps us to bind the instance of this to the environment it was called to get hold of the data from there
    Function bind(Instance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new Function(declaration, environment, isInitializer);
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public String toString() {
        return "<fn" + declaration.name.lexeme+ ">";
    }
}
