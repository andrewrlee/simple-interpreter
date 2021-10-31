package klox

import klox.Interpreter.RuntimeError

class Environment(private val enclosing: Environment? = null) {
    private val values = mutableMapOf<String, Any?>()

    fun define(name: String, value: Any?) = values.put(name, value)

    operator fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }
        if (enclosing != null) {
            return enclosing[name]
        }
        throw RuntimeError(name, "Undefined variable: '${name.lexeme}'")
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

    fun getAt(distance: Int, name: Token): Any? = ancestor(distance).values[name.lexeme]

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    private fun ancestor(distance: Int): Environment {
        var environment = this
        var i = 0
        while(i < distance) {
            environment = environment.enclosing!!
            i++
        }
        return environment
    }
}