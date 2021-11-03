package klox.interpreter

import klox.ast.Stmt
import klox.interpreter.Interpreter.RuntimeError

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

class LoxClass(val name: String, val methods: Map<String, LoxFunction>) : LoxCallable {
    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        val instance = LoxInstance(this)
        return instance
    }

    fun findMethod(name: String): LoxFunction? {
        if (methods.containsKey(name)) {
            return methods[name]
        }

        return null
    }

    override fun arity(): Int {
        return 0
    }

    override fun toString() = name
}

class LoxInstance(private val loxClass: LoxClass) {
    private val fields = mutableMapOf<String, Any?>()
    override fun toString() = "${loxClass.name} instance"

    operator fun get(key: Token): Any? {
        if (fields.containsKey(key.lexeme)) {
            fields[key.lexeme]
        }
        loxClass.findMethod(key.lexeme)?.let { return it }
        throw RuntimeError(key, "Undefined property '${key.lexeme}'.")
    }

    operator fun set(name: Token, value: Any?) {
        fields.put(name.lexeme, value)
    }
}