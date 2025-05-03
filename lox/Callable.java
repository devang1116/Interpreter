package lox;
import java.util.*;

interface Callable {

    // IMPLEMENT: Handles the logic to process a function call
    Object call(Interpreter interpreter, List<Object> arguments);

    // IMPLEMENT: Returns the number of arguments passed to the function
    int arity();
}
