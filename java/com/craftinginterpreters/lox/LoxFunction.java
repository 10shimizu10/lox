package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure; 

    private final boolean isInitializeer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializeer) { 
        this.isInitializeer = isInitializeer;
        this.closure = closure;
        this.declaration = declaration;
    }

    LoxFunction bind(LoxInstance instance){
        Enviornment enviornment = new Enviornment(closure);
        enviornment define("this", instance);
        return new LoxFunction(declaration, enviornment, isInitializeer);
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure); // スペルミスを修正
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i)); // スペルミスを修正
        }
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if(isInitializeer) return closure.getAt(0, "this");
            return returnValue.value;
        }
        if(isInitializeer) return closure.getAt(0, "this");
        return null;
    }
}
