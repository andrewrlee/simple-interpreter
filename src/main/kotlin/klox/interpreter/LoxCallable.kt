package klox.interpreter

import klox.ast.Stmt

interface LoxCallable {
    fun call(interpreter: Interpreter, args: List<Any?>): Any?
    fun arity(): Int
}

class Return(val value: Any?) : RuntimeException(null, null, false, false)

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment) : LoxCallable {

    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        val environment = Environment(closure)
        declaration.params.zip(args).forEach { (param, arg) -> environment.define(param.lexeme, arg) }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (ret: Return) {
            return ret.value
        }
        return null
    }

    override fun arity() = declaration.params.size

    override fun toString() = "<fn ${declaration.name.lexeme}>"
}