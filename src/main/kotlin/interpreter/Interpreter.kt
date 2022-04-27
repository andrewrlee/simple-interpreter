package klox.simple.interpreter

import klox.simple.interpreter.Token.Operator.*
import interpreter.Expr
import interpreter.Expr.Binary
import interpreter.Expr.Literal
import interpreter.Expr.Visitor

class Interpreter : Visitor<Any> {

    fun evaluate(expression: Expr) = expression.accept(this)

    override fun visit(expr: Binary): Any {
        val left = evaluate(expr.left) as Double
        val right = evaluate(expr.right) as Double
        return when (expr.operator) {
            MINUS -> left - right
            SLASH -> left / right
            STAR -> left * right
            PLUS -> left + right
            else -> throw RuntimeException("${expr.operator} Must be numeric operator")
        }
    }

    override fun visit(expr: Literal) = expr.value
}