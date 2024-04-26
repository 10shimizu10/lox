package com.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Enviornment closure;
    LoxFunction(Stmt.Function declaration, Enviornment closure){
        this.closure = closure;
        this.declaration = declaration;
    }

    @Override
    public String toString(){
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity(){
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments){
        Enviornment environment = new Enviornment(closure);
        for(int i = 0; i < declaration.prams.size(); i++){
            environment.define(declaration.params.get(i).lexme, arguments.get(i));
        }
    }
    try{
        interpreter.executeBlock(declaration.body, environment);
    }catch(Return returnValue){
        return returnValue.value;
    }
    return null;
}

