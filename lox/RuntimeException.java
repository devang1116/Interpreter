package lox;

import javax.management.RuntimeErrorException;
import lox.Token;

class RuntimeError extends java.lang.RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
