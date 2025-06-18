package dev.backendsouls.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    void define(String name, Object value) {
        this.values.put(name, value);
    }

    Object get(Token name) {
        if (this.values.containsKey(name.lexeme())) {
            return this.values.get(name.lexeme());
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }
}
