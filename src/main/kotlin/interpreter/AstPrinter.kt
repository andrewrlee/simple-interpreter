package interpreter

import interpreter.Expr.*
import klox.simple.interpreter.Token.Operator

class AstPrinter : Visitor<String> {
    fun print(expression: Expr) = expression.accept(this)

    override fun visit(expr: Binary): String {
        val left = expr.left.accept(this)
        val operator = (expr.operator as Operator).char
        val right = expr.right.accept(this)
        return "($left $operator $right)"
    }

    override fun visit(expr: Literal): String = expr.value.toString()
}
