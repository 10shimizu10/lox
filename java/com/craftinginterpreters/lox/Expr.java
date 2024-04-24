package com.craftinginterpreters.lox


import java.util.List;

abstract classExpr {
 static classBinary extendsExpr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
 static classGrouping extendsExpr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    final Expr expression;
  }
 static classLiteral extendsExpr {
    Literal(Object value) {
      this.value = value;
    }

    final Object value;
  }
 static classUnary extendsExpr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    final Token operator;
    final Expr right;
  }
}
