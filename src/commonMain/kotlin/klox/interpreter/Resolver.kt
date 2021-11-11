package klox.interpreter

import klox.Context
import klox.ast.Expr
import klox.ast.Expr.Assign
import klox.ast.Expr.Binary
import klox.ast.Expr.Call
import klox.ast.Expr.Get
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
import klox.interpreter.FunctionType.FUNCTION
import klox.interpreter.FunctionType.INITIALIZER
import klox.interpreter.FunctionType.METHOD

enum class FunctionType { NONE, FUNCTION, INITIALIZER, METHOD }
enum class ClassType { NONE, CLASS, SUBCLASS }

class Resolver(private val context: Context, private val interpreter: Interpreter) : Visitor<Unit>, Stmt.Visitor<Unit> {
    private val scopes = stackOf<MutableMap<String, Boolean>>()
    private var currentFunctionType = FunctionType.NONE
    private var currentClassType = ClassType.NONE

    override fun visit(expr: Assign) {
        resolve(expr.value)
        resolveLocal(expr.value, expr.name)
    }

    override fun visit(expr: Binary) = resolve(expr.left, expr.right)

    override fun visit(expr: Call) = resolve(expr.callee, *expr.arguments.toTypedArray())

    override fun visit(expr: Grouping) = resolve(expr.expression)

    override fun visit(expr: Literal) {}

    override fun visit(expr: Logical) = resolve(expr.left, expr.right)

    override fun visit(expr: Expr.Set) = resolve(expr.value, expr.obj)

    override fun visit(expr: Expr.This) {
        if (currentClassType == ClassType.NONE) {
            context.error(expr.keyword, "Can't refer to 'this' outside of a class.")
            return
        }
        resolveLocal(expr, expr.keyword)
    }

    override fun visit(expr: Variable) {
        if (!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            context.error(expr.name, "Can't read local variable in its own initialiser.")
        }
        resolveLocal(expr, expr.name)
    }

    override fun visit(expr: Get) = resolve(expr.obj)

    override fun visit(expr: Unary) = resolve(expr.right)

    override fun visit(stmt: Block) {
        beginScope()
        resolve(stmt.statements)
        endScope()
    }

    override fun visit(stmt: Expression) = resolve(stmt.expression)

    override fun visit(stmt: Stmt.Class) {
        val enclosingClassType = currentClassType
        currentClassType = ClassType.CLASS

        declare(stmt.name)
        define(stmt.name)

        stmt.superclass?.let {
            currentClassType = ClassType.SUBCLASS
            if (it.name.lexeme == stmt.name.lexeme) {
                context.error(it.name, "A class can't inherit from itself.")
            }
            resolve(it)

            beginScope()
            scopes.peek()["super"] = true
        }

        beginScope()
        scopes.peek()["this"] = true
        stmt.methods.forEach {
            var declaration = METHOD
            if (it.name.lexeme == "init") declaration = INITIALIZER
            resolveFunction(it, declaration)
        }
        endScope()
        stmt.superclass?.also { endScope() }
        currentClassType = enclosingClassType
    }

    override fun visit(expr: Expr.Super) {
        if (currentClassType == ClassType.NONE) {
            context.error(expr.keyword, "Can't use 'super' outside of class.")
        }
        if (currentClassType != ClassType.SUBCLASS) {
            context.error(expr.keyword, "Can't use 'super' inside a class with no super class.")
        }

        resolveLocal(expr, expr.keyword)
    }

    override fun visit(stmt: Stmt.Function) {
        declare(stmt.name)
        define(stmt.name)
        resolveFunction(stmt, FUNCTION)
    }

    override fun visit(stmt: If) {
        resolve(stmt.condition)
        resolve(stmt.thenBranch, stmt.elseBranch)
    }

    override fun visit(stmt: Print) = resolve(stmt.expression)

    override fun visit(stmt: Stmt.Return) {
        if (currentFunctionType == FunctionType.NONE) {
            context.error(stmt.keyword, "Can't return from top level code.")
        }
        stmt.value?.let {
            if (currentFunctionType == INITIALIZER) {
                context.error(stmt.keyword, "Can't return a value from an initializer.")
            }
            resolve(it)
        }
    }

    override fun visit(stmt: While) {
        resolve(stmt.condition)
        resolve(stmt.body)
    }

    override fun visit(stmt: Var) {
        declare(stmt.name)
        stmt.initializer?.let { resolve(it) }
        define(stmt.name)
    }


    fun resolve(statements: List<Stmt>) = statements.forEach { resolve(it) }
    private fun resolve(vararg statements: Stmt?) = statements.forEach { it?.accept(this) }
    private fun resolve(vararg expressions: Expr?) = expressions.forEach { it?.accept(this) }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.size - 1 downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }

    private fun resolveFunction(stmt: Stmt.Function, functionType: FunctionType) {
        val enclosingFunction = currentFunctionType
        currentFunctionType = functionType

        beginScope()
        stmt.params.forEach {
            declare(it)
            define(it)
        }
        resolve(stmt.body)
        endScope()
        currentFunctionType = enclosingFunction
    }

    private fun beginScope() = scopes.push(HashMap())
    private fun endScope() = scopes.pop()

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            context.error(name, "Already a variable declared with this name in this scope.")
        }
        scope[name.lexeme] = false
    }
}

