package dev.backendsouls.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    public LoxFunction(final Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return this.declaration.params().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        var names = this.declaration.params().iterator();
        var values = arguments.iterator();
        var environment = new Environment(interpreter.globals());

        while (names.hasNext() && values.hasNext()) {
            environment.define(names.next().lexeme(), values.next());
        }

        try {
            interpreter.executeBlock(this.declaration.body(), environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + this.declaration.name().lexeme() + ">";
    }
}
