package klox.tools

import klox.ast.Expr
import klox.ast.Expr.Binary
import klox.ast.Expr.*
import klox.interpreter.Token
import klox.interpreter.TokenType.MINUS
import klox.interpreter.TokenType.STAR

class AstPrinter : Visitor<String> {
    fun print(expression: Expr) = expression.accept(this)

    override fun visit(expr: Binary): String = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visit(expr: Grouping): String = parenthesize("grouping", expr.expression)

    override fun visit(expr: Literal): String = expr.value?.toString() ?: "nil"

    override fun visit(expr: Unary): String = parenthesize(expr.operator.lexeme, expr.right)

    private fun parenthesize(name: String, vararg exprs: Expr) = "($name ${exprs.joinToString(" ") { it.accept(this) }})"

    override fun visit(expr: Variable) = expr.name.lexeme

    override fun visit(expr: Assign) = parenthesize("assign", expr.value)

    override fun visit(expr: Logical) = parenthesize(expr.operator.lexeme, expr.left, expr.right)

    override fun visit(expr: Call): String {
        TODO("Not yet implemented")
    }

    override fun visit(expr: Get): String {
        TODO("Not yet implemented")
    }

    override fun visit(expr: Expr.Set): String {
        TODO("Not yet implemented")
    }
}

fun main() {
    println(
        AstPrinter().print(
            Binary(
                Unary(Token(MINUS, "-", null, 1), Literal(123)),
                Token(STAR, "*", null, 1),
                Grouping(Literal(45.67))
            )
        )
    )
}