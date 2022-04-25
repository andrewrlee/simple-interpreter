package klox.simple.interpreter

import klox.simple.interpreter.Token.EndOfFile
import klox.simple.interpreter.Token.Operator
import klox.simple.interpreter.Token.Operator.*
import interpreter.Expr
import interpreter.Expr.Binary
import interpreter.Expr.Literal

class Parser {
    private var current = 0
    private var tokens: List<Token> = emptyList()

    fun parse(tokens: List<Token>): Expr {
        this.tokens = tokens
        return term()
    }

    private fun term(): Expr {
        var expr = factor()
        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(): Expr {
        var expr = primary()
        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = primary()
            expr = Binary(expr, operator, right)
        }
        return expr
    }

    private fun primary(): Expr {
        if (matchNumber()) return Literal((previous() as Token.Literal).number)
        throw RuntimeException("Token: ${peek()}, Expected literal.")
    }

    private fun match(vararg tokenTypes: Operator): Boolean {
        for (type in tokenTypes) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun matchNumber() =
        when {
            isAtEnd() -> false
            peek() is Token.Literal -> {
                advance()
                true
            }
            else -> false
        }

    private fun check(type: Operator) = if (isAtEnd()) false else peek() == type

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() = peek() is EndOfFile
    private fun peek() = tokens[current]
    private fun previous() = tokens[current - 1]
}