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
        var environment = new Environment(interpreter.globals());

//        for (int i = 0; i < this.declaration.params().size(); i++) {
//            var name = this.declaration.params().get(i).lexeme();
//            environment.define(name, arguments.get(i));
//        }

        var names = this.declaration.params().iterator();
        var values = arguments.iterator();
        while (names.hasNext() && values.hasNext()) {
            environment.define(names.next().lexeme(), values.next());
        }

        interpreter.executeBlock(this.declaration.body(), environment);

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + this.declaration.name().lexeme() + ">";
    }
}
