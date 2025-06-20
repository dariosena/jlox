package dev.backendsouls.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;

    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment environment) {
        this.enclosing = environment;
    }

    void define(String name, Object value) {
        this.values.put(name, value);
    }

    Object get(Token name) {
        if (this.values.containsKey(name.lexeme())) {
            return this.values.get(name.lexeme());
        }

        if (this.enclosing != null) {
            return this.enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    void assign(Token name, Object value) {
        if (this.values.containsKey(name.lexeme())) {
            this.values.put(name.lexeme(), value);
            return;
        }

        if (this.enclosing != null) {
            this.enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme() + "'.");
    }
}
