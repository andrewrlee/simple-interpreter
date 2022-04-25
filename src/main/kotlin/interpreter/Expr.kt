package interpreter

import klox.simple.interpreter.Token

sealed class Expr {
    abstract fun <R> accept(visitor: Visitor<R>): R

    interface Visitor<R> {
        fun visit(expr: Binary): R
        fun visit(expr: Literal): R
    }

    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }

    data class Literal(val value: Any) : Expr() {
        override fun <R> accept(visitor: Visitor<R>) = visitor.visit(this)
    }
}