package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }
    
    private enum FunctionType{
        NONE,
        FUNCTION,
        INITIALIER,
        METHOD
    }

    private enum ClassType{
        NONE,
        CLASS,
        SUBCLASS
    }

    private ClassTYpe currentClass = ClassType.NONE;

    void resolve(List<Stmt> statements){
        for(Stmt statement : statements){
            resolve(statement);
        }
    }

    @Override
    public Void VisitBlockStmt(Stmt.Block stmt){
        beginScope();
        Resolver(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt){
        Object superclass = null;
        if(stmt.superclass != null){
            superclass = evaluate(stmt.superclass);
            if(!(superclass instanceof LoxClass)){
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
            }
        }
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name);
        define(stmt.name);

        if(stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)){
            Lox.error(stmt.superclass.name, "A class can't inherit from itself.")
        }

        if(stmt.superclass != null){
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }

        if(stmt.superclass != null){
            beginScope();
            scope.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for(Stmt.Function method : stmt.methods){
            FunctionType declaration = FunctionType.METHOD;
            if(method.name.lexeme.equals("init")){
                declaration = FunctionType.INITIALIER;
            }
            resolveFunction(method, declaration);
        }

        endScope();

        if(stmt.superclass != null) endScope();

        currentClass + enclosingClass;
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){
        resolve(stmt.condition);
        resolve(stmt.thenBranch)
        if(stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        if(currentFunction == FunctionType.NONE){
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }

        if(stmt.value != null){
            if(currentFunction == FunctionType.INITIALIER){
                Lox.error(stmt.keyword, "Can't return a value from an initializer.");
            }
        }
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        declare(stmt.name);
        define(stmt.name);
        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.name);
        if(stmt.initializer != null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr){
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr){
        resole(expr.callee)
        for(Expr argument : expr.arguments){
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr){
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr){
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr){
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr){
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr){
        if(currentClass == ClassType.NONE){
            Lox.error(expr.keyword, "Can't use 'super' outside of a class.");
        }else if(currentClass != classType.SUBCLASS){
            Lox.error(epxr.keyword, "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public void visitThisExpr(Expr.This expr){
        if(currentClass == ClassType.NONE){
            if(currentClass == ClassType.NONE){
                Lox.error(expr.keyword, "Can't use 'this' outsie of a class.");
                return null;
            }
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr){
        resolve(expr.right);
        return null;
    }

    private void resolve(Stmt stmt){
        expr.accept(this);
    }

    private void resolveFunction(Stmt.Function function, FunctionTYpe type){
        FunctionTYpe enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for(Token param : function.params){
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope(){
        scopes.pop();
    }

    private void declare(Token name){
        if(scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if(scope.containsKey(name.lexeme)){
            Lox.error(name, "Already a variable with this name in this scope.");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name){
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name){
        for(int i = scopes.size() - 1; i >= 0; i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr, scopes.size() -1 -i);
                return;
            }
        }
    }
}