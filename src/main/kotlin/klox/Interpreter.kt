package klox

import klox.TokenType.BANG
import klox.TokenType.BANG_EQUAL
import klox.TokenType.EQUAL_EQUAL
import klox.TokenType.GREATER
import klox.TokenType.GREATER_EQUAL
import klox.TokenType.LESS
import klox.TokenType.LESS_EQUAL
import klox.TokenType.MINUS
import klox.TokenType.OR
import klox.TokenType.PLUS
import klox.TokenType.SLASH
import klox.TokenType.STAR
import klox.ast.Expr
import klox.ast.Expr.Assign
import klox.ast.Expr.Binary
import klox.ast.Expr.Call
import klox.ast.Expr.Grouping
import klox.ast.Expr.Literal
import klox.ast.Expr.Logical
import klox.ast.Expr.Unary
import klox.ast.Expr.Variable
import klox.ast.Expr.Visitor
import klox.ast.Stmt
import klox.ast.Stmt.Block
import klox.ast.Stmt.Expression
import klox.ast.Stmt.If
import klox.ast.Stmt.Print
import klox.ast.Stmt.Var
import klox.ast.Stmt.While

class Interpreter() : Visitor<Any?>, Stmt.Visitor<Unit> {
    class RuntimeError(val token: Token, message: String) : RuntimeException(message)

    private val locals = mutableMapOf<Expr, Int>()
    val globals = Environment()
    var environment = globals

    init {
        globals.define("clock", object : LoxCallable {
            override fun call(interpreter: Interpreter, args: List<Any?>) = System.currentTimeMillis() / 1000.0
            override fun arity() = 0
            override fun toString(): String = "<native fn>"
        })
    }

    fun interpret(stmts: List<Stmt>) = try {
        stmts.forEach { execute(it) }
    } catch (e: RuntimeError) {
        Lox.runtimeError(e)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    override fun visit(expr: Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            GREATER -> doNumberOp(expr, left, right, GREATER_THAN)
            GREATER_EQUAL -> doNumberOp(expr, left, right, GREATER_THAN_EQUALS)
            LESS -> doNumberOp(expr, left, right, LESS_THAN)
            LESS_EQUAL -> doNumberOp(expr, left, right, LESS_THAN_EQUALS)
            EQUAL_EQUAL -> left == right
            BANG_EQUAL -> left != right
            MINUS -> doNumberOp(expr, left, right, Double::minus)
            SLASH -> doNumberOp(expr, left, right, Double::div)
            STAR -> doNumberOp(expr, left, right, Double::times)
            PLUS -> when {
                left is String && right is String -> left + right
                left is Double && right is Double -> left + right
                else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings")
            }
            else -> null
        }
    }

    private fun doNumberOp(
        expr: Binary,
        left: Any?,
        right: Any?,
        perform: (Double, Double) -> Any?
    ) = if (left !is Double || right !is Double) throw RuntimeError(
        expr.operator,
        "Operands must be a numbers"
    ) else perform(left, right)

    override fun visit(expr: Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }
        return evaluate(expr.right)
    }

    override fun visit(expr: Grouping) = evaluate(expr.expression)

    override fun visit(expr: Literal) = expr.value

    override fun visit(expr: Unary): Any? {
        val right = evaluate(expr.right)
        return when (expr.operator.type) {
            BANG -> !isTruthy(right)
            MINUS -> checkDouble(expr, right) { -it }
            else -> null
        }
    }

    override fun visit(stmt: Expression) {
        evaluate(stmt.expression)
    }

    override fun visit(stmt: Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    private fun checkDouble(expr: Unary, operand: Any?, perform: (v: Double) -> Any?): Any? {
        if (operand !is Double) throw RuntimeError(expr.operator, "Operand must be a number")
        return perform(operand)
    }

    private fun evaluate(expression: Expr) = expression.accept(this)

    override fun visit(expr: Variable) = lookupVariable(expr.name, expr)

    private fun lookupVariable(name: Token, expr: Variable): Any? {
        val distance = locals[expr]
        return if (distance != null) {
            environment.getAt(distance, name)
        } else {
            globals[name]
        }
    }

    override fun visit(stmt: Var) {
        val value = stmt.initializer?.let { evaluate(it) }
        environment.define(stmt.name.lexeme, value)
    }

    override fun visit(expr: Assign): Any? {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
        return value
    }

    override fun visit(stmt: Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previous = this.environment
        try {
            this.environment = environment
            statements.forEach { execute(it) }
        } finally {
            this.environment = previous
        }
    }

    override fun visit(stmt: If) {
        when {
            isTruthy(evaluate(stmt.condition)) -> execute(stmt.thenBranch)
            stmt.elseBranch != null -> execute(stmt.elseBranch)
        }
    }

    override fun visit(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override fun visit(expr: Call): Any? {
        val callee = evaluate(expr.callee)
        val arguments = expr.arguments.map { evaluate(it) }
        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes.")
        }
        if (arguments.size != callee.arity()) {
            throw RuntimeError(expr.paren, "Expect ${callee.arity()} args but got ${arguments.size}.")
        }
        return callee.call(this, arguments)
    }

    override fun visit(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visit(stmt: Stmt.Return) {
        val value = stmt.value?.let { evaluate(it) }
        throw Return(value)
    }

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    companion object {
        val GREATER_THAN = { v1: Double, v2: Double -> v1 > v2 }
        val GREATER_THAN_EQUALS = { v1: Double, v2: Double -> v1 >= v2 }
        val LESS_THAN = { v1: Double, v2: Double -> v1 < v2 }
        val LESS_THAN_EQUALS = { v1: Double, v2: Double -> v1 <= v2 }

        private fun isTruthy(right: Any?) = when (right) {
            is Boolean -> right
            null -> false
            else -> true
        }

        private fun stringify(value: Any?): String = when (value) {
            null -> "nil"
            is Double -> {
                val text = value.toString()
                if (text.endsWith(".0")) text.substring(0, text.length - 2) else text
            }
            else -> value.toString()
        }
    }
}