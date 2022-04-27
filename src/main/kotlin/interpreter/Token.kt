package klox.simple.interpreter

sealed interface Token {
    data class Literal(val number: Double) : Token {
        override fun toString() = "$number"
    }
    enum class Operator(val char: String) : Token {
        PLUS("+"), MINUS("-"), SLASH("/"), STAR("*")
    }
    enum class EndOfFile : Token { EOF }

    companion object {
        private val OPERATORS = Operator.values().associateBy { it.char }
        val toToken = { s: String -> OPERATORS.getOrElse(s) { Literal(s.toDouble()) } }
    }
}